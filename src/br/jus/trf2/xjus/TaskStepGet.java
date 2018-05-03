package br.jus.trf2.xjus;

import java.util.List;

import br.jus.trf2.xjus.IXjus.TaskStepGetRequest;
import br.jus.trf2.xjus.IXjus.TaskStepGetResponse;

import com.crivano.gae.Dao;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class TaskStepGet implements IXjus.ITaskStepGet {

	private static final RetryOptions RETRY_OPTIONS = RetryOptions.Builder
			.withTaskRetryLimit(0);

	@Override
	public void run(TaskStepGetRequest req, TaskStepGetResponse resp)
			throws Exception {
		resp.status = "OK";

		System.out.println("atualizando índices");

		Queue queue = QueueFactory.getDefaultQueue();

		Dao dao = new Dao();
		List<Index> l = dao.loadAll(Index.class);
		for (Index idx : l) {
			if (idx.active) {
				queue.add(TaskOptions.Builder
						.withUrl("/api/v1/task/" + idx.idx + "/step")
						.method(Method.POST)
						.retryOptions(RETRY_OPTIONS)
						.header("Host",
								ModulesServiceFactory.getModulesService()
										.getVersionHostname(null, null)));
			}
		}
	}

	public String getContext() {
		return "avançar todos os índices";
	}
}
