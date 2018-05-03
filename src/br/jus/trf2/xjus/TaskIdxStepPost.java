package br.jus.trf2.xjus;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import br.jus.trf2.xjus.IXjus.TaskIdxStepPostRequest;
import br.jus.trf2.xjus.IXjus.TaskIdxStepPostResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.ChangedReferencesGetResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;

import com.crivano.gae.Dao;
import com.crivano.gae.HttpGAE;
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

public class TaskIdxStepPost implements IXjus.ITaskIdxStepPost {

	private static final int TASK_AGE_LIMIT_SECONDS = 3600 * 24 * 7; // 7 days
	private static final int MIN_TASK_BACKOFF = 600; // 10 minute
	private static final int MAX_TASK_BACKOFF = 3600 * 2; // 2 hours
	private static final int MAX_PER_MINUTE_DEFAULT = 10; // 10 per minute
	private static final int MAX_INDEXES = 100; // Probably not more than 100
	private static final RetryOptions RETRY_OPTIONS = RetryOptions.Builder
			.withTaskAgeLimitSeconds(TASK_AGE_LIMIT_SECONDS)
			.minBackoffSeconds(MIN_TASK_BACKOFF)
			.maxBackoffSeconds(MAX_TASK_BACKOFF);

	@Override
	public void run(TaskIdxStepPostRequest req, TaskIdxStepPostResponse resp)
			throws Exception {
		Queue queue = QueueFactory.getDefaultQueue();
		resp.status = "OK";

		System.out.println("atualizando índice " + req.idx);

		Dao dao = new Dao();
		Key<Index> key = Key.create(Index.class, req.idx);
		Index idx = dao.load(key);
		if (idx == null)
			return;

		Key<IndexStatus> keyStatus = Key.create(IndexStatus.class, req.idx);
		IndexStatus sts = dao.load(keyStatus);

		// Verify the number of tasks in the queue to avoid a fork-bomb
		QueueStatistics stats = queue.fetchStatistics();
		if (stats.getNumTasks() > MAX_INDEXES + 2
				* (idx.max == null ? MAX_PER_MINUTE_DEFAULT : idx.max))
			return;

		// Query changed IDs since last update
		String qs = "?max="
				+ (idx.max == null ? MAX_PER_MINUTE_DEFAULT : idx.max);
		if (sts != null && sts.last != null)
			qs += "&last=" + sts.last;
		ChangedReferencesGetResponse changedRefs = (ChangedReferencesGetResponse) SwaggerUtils
				.fromJson(
						new String(HttpGAE
								.fetchAsync(
										idx.token,
										new URL(idx.api + "/changed-references"
												+ qs), HTTPMethod.GET, null,
										null).get().getContent(),
								StandardCharsets.UTF_8),
						ChangedReferencesGetResponse.class);
		if (changedRefs.list == null || changedRefs.last == null)
			return;

		// Add tasks to refresh each ID
		for (Reference ref : changedRefs.list) {
			queue.addAsync(TaskOptions.Builder
					.withUrl(
							"/api/v1/index/" + req.idx + "/record/" + ref.id
									+ "/refresh")
					.method(Method.POST)
					.retryOptions(RETRY_OPTIONS)
					.header("Host",
							ModulesServiceFactory.getModulesService()
									.getVersionHostname(null, null)));
		}

		// Update IndexStatus
		if (sts == null) {
			sts = new IndexStatus();
			sts.idx = req.idx;
			sts.records = 0L;
		}
		sts.lastModified = new Date();
		if (changedRefs.last != null)
			sts.last = changedRefs.last;
		sts.records += changedRefs.list.size();
		dao.save(sts);
	}

	public String getContext() {
		return "avançar um índice";
	}
}
