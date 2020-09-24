package br.jus.trf2.xjus;

import java.util.List;

import com.crivano.swaggerservlet.SwaggerUtils;

import br.jus.trf2.xjus.IXjus.TaskRefreshStepGetRequest;
import br.jus.trf2.xjus.IXjus.TaskRefreshStepGetResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ITask;

public class TaskRefreshStepGet implements IXjus.ITaskRefreshStepGet {

	@Override
	public void run(TaskRefreshStepGetRequest req, TaskRefreshStepGetResponse resp) throws Exception {
		resp.status = "OK";

		SwaggerUtils.log(this.getClass()).debug("revisando índices");

		ITask queue = XjusFactory.getQueue();

		try (IPersistence dao = XjusFactory.getDao()) {
			List<Index> l = dao.loadIndexes();
			for (Index idx : l) {
				if (idx.getActive() && 0 != idx.getMaxRefresh()) {
					queue.addRefreshIndex(idx.getIdx());
				}
			}
		}
	}

	public String getContext() {
		return "atualizar todos os índices";
	}
}
