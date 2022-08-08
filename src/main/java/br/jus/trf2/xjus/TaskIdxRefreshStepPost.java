package br.jus.trf2.xjus;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import com.crivano.swaggerservlet.SwaggerAsyncResponse;
import com.crivano.swaggerservlet.SwaggerCall;
import com.crivano.swaggerservlet.SwaggerUtils;

import br.jus.trf2.xjus.IXjus.TaskIdxRefreshStepPostRequest;
import br.jus.trf2.xjus.IXjus.TaskIdxRefreshStepPostResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexRefreshStatus;
import br.jus.trf2.xjus.record.api.AllReferencesGet;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ISearch;
import br.jus.trf2.xjus.services.ITask;

public class TaskIdxRefreshStepPost implements IXjus.ITaskIdxRefreshStepPost {
	private static final int MAX_PER_MINUTE_DEFAULT = 10; // 10 per 10 minutes
	private static final int MAX_INDEXES = 100; // Probably not more than 100

	@Override
	public void run(TaskIdxRefreshStepPostRequest req, TaskIdxRefreshStepPostResponse resp) throws Exception {
		resp.status = "OK";

		ITask queue = XjusFactory.getQueue();
		ISearch search = XjusFactory.getSearch();

		String idx2 = req.idx;
		SwaggerUtils.log(this.getClass()).debug("revisando índice " + idx2);

		try (IPersistence dao = XjusFactory.getDao()) {
			Index idx = dao.loadIndex(req.idx);
			if (idx == null)
				return;

			IndexRefreshStatus sts = dao.loadIndexRefreshStatus(req.idx);

			// Verify the number of tasks in the queue to avoid a fork-bomb
			int count = queue.getRefreshTaskCount();
			if (count > (MAX_INDEXES
					+ 2 * (idx.getMaxRefresh() == null ? MAX_PER_MINUTE_DEFAULT : idx.getMaxRefresh()))) {
				SwaggerUtils.log(this.getClass())
						.info("índice " + idx2 + " - adiando revisão pois há muitas tarefas ativas - " + count);
				return;
			}

			// Query changed IDs since last update
			String qs = "?max=" + (idx.getMaxRefresh() == null ? MAX_PER_MINUTE_DEFAULT : idx.getMaxRefresh());
			if (sts != null && sts.getRefreshLastId() != null)
				qs += "&lastid=" + sts.getRefreshLastId();

			SwaggerAsyncResponse<AllReferencesGet.Response> changedRefsAsync = SwaggerCall
					.callAsync(getContext(), XjusServlet.getInstance().getProperty("index." + idx.getIdx() + ".token"),
							"GET", idx.getApi() + "/all-references" + qs, new AllReferencesGet.Request(),
							AllReferencesGet.Response.class)
					.get(30, TimeUnit.SECONDS);
			AllReferencesGet.Response changedRefs = changedRefsAsync.getRespOrThrowException();

			if (changedRefs.list == null && changedRefs.list.size() == 0) {
				SwaggerUtils.log(this.getClass()).info("índice " + idx2 + " - revisão concluída, reiniciando...");
				dao.deleteIndexRefreshStatus(sts);
				return;
			}

			// Add to setNovo all returned IDs
			TreeSet<String> setNovo = new TreeSet<>();
			String lastId = sts != null ? sts.getRefreshLastId() : null;
			for (Reference ref : changedRefs.list) {
				setNovo.add(ref.id);
				lastId = ref.id;
			}

			// Get IDs that as there on the index
			TreeSet<String> setAntigo = getSetAntigo(search, idx2, sts != null ? sts.getRefreshLastId() : null, lastId,
					idx.getMaxRefresh() * 2);

			// Encaixa a lista que existe no índice (Antigo) com a nova lista que
			// foi retornada (Novo)
			Iterator<String> iNovo = setNovo.iterator();
			Iterator<String> iAntigo = setAntigo.iterator();
			String oAntigo = null;
			String oNovo = null;
			if (iAntigo.hasNext())
				oAntigo = iAntigo.next();
			if (iNovo.hasNext())
				oNovo = iNovo.next();
			while (oAntigo != null || oNovo != null) {
				if ((oAntigo == null) || (oNovo != null && oNovo.compareTo(oAntigo) > 0)) {
					// O novo não existe entre os antigos, portanto deve ser
					// incluido
					queue.addRefreshDocument(idx2, oNovo);
					if (iNovo.hasNext())
						oNovo = iNovo.next();
					else
						oNovo = null;
				} else if (oNovo == null || (oAntigo != null && oAntigo.compareTo(oNovo) > 0)) {
					// O antigo não existe mais no sistema e deve ser excluído do
					// índice
					search.removeDocument(idx2, oAntigo);
					if (iAntigo.hasNext())
						oAntigo = iAntigo.next();
					else
						oAntigo = null;
				} else {
					// O registro existe no índice e no webservice e deve ser
					// atualizado
					queue.addRefreshDocument(idx2, oNovo);
					if (iNovo.hasNext())
						oNovo = iNovo.next();
					else
						oNovo = null;
					if (iAntigo.hasNext())
						oAntigo = iAntigo.next();
					else
						oAntigo = null;
				}
			}

			// Update IndexStatus
			if (sts == null) {
				sts = new IndexRefreshStatus();
				sts.setIdx(idx2);
			}
			sts.setRefreshLastModified(new Date());
			if (lastId != null)
				sts.setRefreshLastId(lastId);
			sts.setRefreshComplete(setNovo.size() == 0 && setAntigo.size() == 0);
			dao.saveIndexRefreshStatus(sts);
		}
	}

	protected TreeSet<String> getSetAntigo(ISearch search, String indexName, String idStart, String idLimit,
			int maxRefresh) throws Exception {
		TreeSet<String> set = new TreeSet<>();
		{
			while (true) {
				List<String> ids = search.getDocumentIds(indexName, idStart, maxRefresh);
				if (ids == null || ids.size() == 0)
					return set;
				for (String s : ids) {
					if (s.compareTo(idLimit) > 0)
						return set;
					set.add(s);
					idStart = s;
				}
			}
		}
	}

	public String getContext() {
		return "avançar um índice";
	}
}
