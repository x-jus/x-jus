package br.jus.trf2.xjus;

import java.util.List;

import br.jus.trf2.xjus.IXjus.TaskRefreshStepGetRequest;
import br.jus.trf2.xjus.IXjus.TaskRefreshStepGetResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ITask;
import br.jus.trf2.xjus.services.gae.GaeTaskImpl;
import br.jus.trf2.xjus.util.Dao;

public class TaskRefreshStepGet implements IXjus.ITaskRefreshStepGet {

	@Override
	public void run(TaskRefreshStepGetRequest req, TaskRefreshStepGetResponse resp) throws Exception {
		resp.status = "OK";

		System.out.println("revisando índices");

		ITask queue = new GaeTaskImpl();

		try (IPersistence dao = new Dao()) {
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
