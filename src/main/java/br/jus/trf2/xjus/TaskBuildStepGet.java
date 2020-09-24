package br.jus.trf2.xjus;

import java.util.List;

import com.crivano.swaggerservlet.SwaggerUtils;

import br.jus.trf2.xjus.IXjus.TaskBuildStepGetRequest;
import br.jus.trf2.xjus.IXjus.TaskBuildStepGetResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ITask;

public class TaskBuildStepGet implements IXjus.ITaskBuildStepGet {

	@Override
	public void run(TaskBuildStepGetRequest req, TaskBuildStepGetResponse resp) throws Exception {
		resp.status = "OK";

		SwaggerUtils.log(this.getClass()).debug("atualizando índices");

		ITask queue = XjusFactory.getQueue();

		try (IPersistence dao = XjusFactory.getDao()) {
			List<Index> l = dao.loadIndexes();
			for (Index idx : l) {
				if (idx.getActive() && 0 != idx.getMaxBuild()) {
					queue.addBuildIndex(idx.getIdx());
				}
			}
		}
	}

	public String getContext() {
		return "contruir todos os índices";
	}
}
