package br.jus.trf2.xjus;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.crivano.swaggerservlet.SwaggerAsyncResponse;
import com.crivano.swaggerservlet.SwaggerCall;
import com.crivano.swaggerservlet.SwaggerUtils;

import br.jus.trf2.xjus.IXjus.TaskIdxBuildStepPostRequest;
import br.jus.trf2.xjus.IXjus.TaskIdxBuildStepPostResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexBuildStatus;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.ChangedReferencesGetResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ITask;
import br.jus.trf2.xjus.services.gae.GaeTaskImpl;
import br.jus.trf2.xjus.util.Dao;

public class TaskIdxBuildStepPost implements IXjus.ITaskIdxBuildStepPost {
	private static final int MAX_PER_MINUTE_DEFAULT = 10; // 10 per minute
	private static final int MAX_INDEXES = 100; // Probably not more than 100

	@Override
	public void run(TaskIdxBuildStepPostRequest req, TaskIdxBuildStepPostResponse resp) throws Exception {
		resp.status = "OK";

		ITask queue = new GaeTaskImpl();

		System.out.println("atualizando índice " + req.idx);

		try (IPersistence dao = new Dao()) {
			Index idx = dao.loadIndex(req.idx);
			if (idx == null)
				return;

			IndexBuildStatus sts = dao.loadIndexBuildStatus(idx.getIdx());

			// Verify the number of tasks in the queue to avoid a fork-bomb
			int count = queue.getBuildTaskCount();
			if (count > (MAX_INDEXES + 2 * (idx.getMaxBuild() == null ? MAX_PER_MINUTE_DEFAULT : idx.getMaxBuild()))) {
				System.out.println(
						"índice " + req.idx + " - adiando atualização pois há muitas tarefas ativas - " + count);
				return;
			}

			// Query changed IDs since last update
			String qs = "?max=" + (idx.getMaxBuild() == null ? MAX_PER_MINUTE_DEFAULT : idx.getMaxBuild());
			if (sts != null && sts.getBuildLastdate() != null)
				qs += "&lastdate=" + SwaggerUtils.format(sts.getBuildLastdate());
			if (sts != null && sts.getBuildLastid() != null)
				qs += "&lastid=" + sts.getBuildLastid();

			SwaggerAsyncResponse<ChangedReferencesGetResponse> changedRefsAsync = SwaggerCall
					.callAsync(getContext(), idx.getToken(), "get", idx.getApi() + "/changed-references" + qs, req,
							ChangedReferencesGetResponse.class)
					.get(30, TimeUnit.SECONDS);
			ChangedReferencesGetResponse changedRefs = changedRefsAsync.getRespOrThrowException();

			if (changedRefs.list == null)
				return;

			// Add tasks to refresh each ID
			for (Reference ref : changedRefs.list) {
				queue.addBuildDocument(req.idx, ref.id);
			}

			// Update IndexStatus
			if (sts == null) {
				sts = new IndexBuildStatus();
				sts.setIdx(req.idx);
				sts.setBuildRecords(0L);
			}
			sts.setBuildLastModified(new Date());
			if (changedRefs.list.size() > 0) {
				Reference last = changedRefs.list.get(changedRefs.list.size() - 1);
				sts.setBuildLastdate(last.date);
				sts.setBuildLastid(last.id);
			}
			sts.setBuildLastCount(changedRefs.list.size());
			sts.setBuildRecords(sts.getBuildRecords() + sts.getBuildLastCount());
			dao.saveIndexBuildStatus(sts);
		}
	}

	public String getContext() {
		return "avançar um índice";
	}
}
