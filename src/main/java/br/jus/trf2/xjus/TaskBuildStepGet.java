package br.jus.trf2.xjus;

import java.util.List;

import br.jus.trf2.xjus.IXjus.TaskBuildStepGetRequest;
import br.jus.trf2.xjus.IXjus.TaskBuildStepGetResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ITask;
import br.jus.trf2.xjus.services.gae.GaeTaskImpl;
import br.jus.trf2.xjus.util.Dao;

public class TaskBuildStepGet implements IXjus.ITaskBuildStepGet {

	@Override
	public void run(TaskBuildStepGetRequest req, TaskBuildStepGetResponse resp) throws Exception {
		resp.status = "OK";

		System.out.println("atualizando índices");

		ITask queue = new GaeTaskImpl();

		try (IPersistence dao = new Dao()) {
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
