package br.jus.trf2.xjus;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import br.jus.trf2.xjus.IXjus.TaskIdxRefreshStepPostRequest;
import br.jus.trf2.xjus.IXjus.TaskIdxRefreshStepPostResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexRefreshStatus;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.AllReferencesGetResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;

import com.crivano.gae.Dao;
import com.crivano.gae.HttpGAE;
import com.crivano.gae.Search;
import com.crivano.swaggerservlet.SwaggerUtils;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.QueueStatistics;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.googlecode.objectify.Key;

public class TaskIdxRefreshStepPost implements IXjus.ITaskIdxRefreshStepPost {

	private static final int TASK_AGE_LIMIT_SECONDS = 3600 * 24 * 7; // 7 days
	private static final int MIN_TASK_BACKOFF = 600; // 10 minute
	private static final int MAX_TASK_BACKOFF = 3600 * 2; // 2 hours
	private static final int MAX_PER_MINUTE_DEFAULT = 10; // 10 per 10 minutes
	private static final int MAX_INDEXES = 100; // Probably not more than 100
	private static final RetryOptions RETRY_OPTIONS = RetryOptions.Builder
			.withTaskAgeLimitSeconds(TASK_AGE_LIMIT_SECONDS)
			.minBackoffSeconds(MIN_TASK_BACKOFF)
			.maxBackoffSeconds(MAX_TASK_BACKOFF);

	@Override
	public void run(TaskIdxRefreshStepPostRequest req,
			TaskIdxRefreshStepPostResponse resp) throws Exception {
		Queue queue = QueueFactory.getQueue(TaskRefreshStepGet.QUEUE_REFRESH);
		resp.status = "OK";

		String idx2 = req.idx;
		System.out.println("revisando índice " + idx2);

		Dao dao = new Dao();
		Key<Index> key = Key.create(Index.class, idx2);
		Index idx = dao.load(key);
		if (idx == null)
			return;

		Key<IndexRefreshStatus> keyStatus = Key.create(
				IndexRefreshStatus.class, idx2);
		IndexRefreshStatus sts = dao.load(keyStatus);

		// Verify the number of tasks in the queue to avoid a fork-bomb
		QueueStatistics stats = queue.fetchStatistics();
		if (stats.getNumTasks() > (MAX_INDEXES + 2 * (idx.maxRefresh == null ? MAX_PER_MINUTE_DEFAULT
				: idx.maxRefresh))) {
			System.out.println("índice " + idx2
					+ " - adiando revisão pois há muitas tarefas ativas - "
					+ stats.getNumTasks());
			return;
		}

		// Query changed IDs since last update
		String qs = "?max="
				+ (idx.maxRefresh == null ? MAX_PER_MINUTE_DEFAULT
						: idx.maxRefresh);
		if (sts != null && sts.id != null)
			qs += "&lastid=" + sts.id;
		AllReferencesGetResponse changedRefs = (AllReferencesGetResponse) SwaggerUtils
				.fromJson(
						new String(HttpGAE
								.fetchAsync(
										idx.token,
										new URL(idx.api + "/all-references"
												+ qs), HTTPMethod.GET, null,
										null).get().getContent(),
								StandardCharsets.UTF_8),
						AllReferencesGetResponse.class);
		if (changedRefs.list == null)
			return;

		// Add to setNovo all returned IDs
		TreeSet<String> setNovo = new TreeSet<>();
		String lastId = sts != null ? sts.id : null;
		for (Reference ref : changedRefs.list) {
			setNovo.add(ref.id);
			lastId = ref.id;
		}

		// Get IDs that as there on the index
		TreeSet<String> setAntigo = getSetAntigo(idx2, sts != null ? sts.id
				: null, lastId, idx.maxRefresh * 2);

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
			if ((oAntigo == null)
					|| (oNovo != null && oNovo.compareTo(oAntigo) > 0)) {
				// O novo não existe entre os antigos, portanto deve ser
				// incluido
				addRefreshTask(queue, idx2, oNovo);
				if (iNovo.hasNext())
					oNovo = iNovo.next();
				else
					oNovo = null;
			} else if (oNovo == null
					|| (oAntigo != null && oAntigo.compareTo(oNovo) > 0)) {
				// O antigo não existe mais no sistema e deve ser excluído do
				// índice
				Search.deleteDocument(idx2, oAntigo);
				if (iAntigo.hasNext())
					oAntigo = iAntigo.next();
				else
					oAntigo = null;
			} else {
				// O registro existe no índice e no webservice e deve ser
				// atualizado
				addRefreshTask(queue, idx2, oNovo);
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
			sts.idx = idx2;
		}
		sts.lastModified = new Date();
		if (lastId != null)
			sts.id = lastId;
		sts.complete = setNovo.size() == 0 && setAntigo.size() == 0;
		dao.save(sts);
	}

	protected TreeSet<String> getSetAntigo(String indexName, String idStart,
			String idLimit, int maxRefresh) {
		TreeSet<String> set = new TreeSet<>();
		{
			while (true) {
				List<String> ids = Search.getDocumentIds(indexName, idStart,
						maxRefresh);
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

	protected void addRefreshTask(Queue queue, String indexName, String id) {
		queue.addAsync(TaskOptions.Builder
				.withUrl(
						"/api/v1/index/" + indexName + "/record/" + id
								+ "/refresh")
				.method(Method.POST)
				.retryOptions(RETRY_OPTIONS)
				.header("Host",
						ModulesServiceFactory.getModulesService()
								.getVersionHostname(null, null)));
	}

	public String getContext() {
		return "avançar um índice";
	}
}
