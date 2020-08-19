package br.jus.trf2.xjus.services.gae;

import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import br.jus.trf2.xjus.services.ITask;

public class GaeTaskImpl implements ITask {
	public static final String QUEUE_REFRESH = "queue-refresh";
	public static final String QUEUE_BUILD = "queue-build";

	@Override
	public int getRefreshTaskCount() {
		Queue queue = QueueFactory.getQueue(QUEUE_REFRESH);
		return queue.fetchStatistics().getNumTasks();
	}

	@Override
	public int getBuildTaskCount() {
		Queue queue = QueueFactory.getQueue(QUEUE_BUILD);
		return queue.fetchStatistics().getNumTasks();
	}

	@Override
	public void addBuildIndex(String idx) {
		final RetryOptions RETRY_OPTIONS = RetryOptions.Builder.withTaskRetryLimit(0);
		Queue queue = QueueFactory.getQueue(QUEUE_BUILD);
		queue.add(TaskOptions.Builder.withUrl("/api/v1/task/" + idx + "/build-step").method(Method.POST)
				.retryOptions(RETRY_OPTIONS)
				.header("Host", ModulesServiceFactory.getModulesService().getVersionHostname(null, null)));

	}

	@Override
	public void addBuildDocument(String idx, String id) {
		final int TASK_AGE_LIMIT_SECONDS = 3600 * 24 * 7; // 7 days
		final int MIN_TASK_BACKOFF = 600; // 10 minute
		final int MAX_TASK_BACKOFF = 3600 * 2; // 2 hours

		final RetryOptions RETRY_OPTIONS = RetryOptions.Builder.withTaskAgeLimitSeconds(TASK_AGE_LIMIT_SECONDS)
				.minBackoffSeconds(MIN_TASK_BACKOFF).maxBackoffSeconds(MAX_TASK_BACKOFF);
		Queue queue = QueueFactory.getQueue(QUEUE_BUILD);

		queue.addAsync(TaskOptions.Builder.withUrl("/api/v1/task/" + idx + "/record/" + id + "/refresh")
				.method(Method.POST).retryOptions(RETRY_OPTIONS)
				.header("Host", ModulesServiceFactory.getModulesService().getVersionHostname(null, null)));
	}

	@Override
	public void addRefreshIndex(String idx) {
		final RetryOptions RETRY_OPTIONS = RetryOptions.Builder.withTaskRetryLimit(0);
		Queue queue = QueueFactory.getQueue(QUEUE_REFRESH);

		queue.add(TaskOptions.Builder.withUrl("/api/v1/task/" + idx + "/refresh-step").method(Method.POST)
				.retryOptions(RETRY_OPTIONS)
				.header("Host", ModulesServiceFactory.getModulesService().getVersionHostname(null, null)));
	}

	@Override
	public void addRefreshDocument(String idx, String id) {
		final int TASK_AGE_LIMIT_SECONDS = 3600 * 24 * 7; // 7 days
		final int MIN_TASK_BACKOFF = 600; // 10 minute
		final int MAX_TASK_BACKOFF = 3600 * 2; // 2 hours

		final RetryOptions RETRY_OPTIONS = RetryOptions.Builder.withTaskAgeLimitSeconds(TASK_AGE_LIMIT_SECONDS)
				.minBackoffSeconds(MIN_TASK_BACKOFF).maxBackoffSeconds(MAX_TASK_BACKOFF);
		Queue queue = QueueFactory.getQueue(QUEUE_REFRESH);
		queue.addAsync(TaskOptions.Builder.withUrl("/api/v1/task/" + idx + "/record/" + id + "/refresh")
				.method(Method.POST).retryOptions(RETRY_OPTIONS)
				.header("Host", ModulesServiceFactory.getModulesService().getVersionHostname(null, null)));
	}

}
