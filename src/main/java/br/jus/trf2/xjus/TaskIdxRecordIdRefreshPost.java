package br.jus.trf2.xjus;

import java.util.concurrent.TimeUnit;

import com.crivano.swaggerservlet.SwaggerAsyncResponse;
import com.crivano.swaggerservlet.SwaggerCall;

import br.jus.trf2.xjus.IXjus.TaskIdxRecordIdRefreshPostRequest;
import br.jus.trf2.xjus.IXjus.TaskIdxRecordIdRefreshPostResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ISearch;
import br.jus.trf2.xjus.services.gae.GaeSearchImpl;
import br.jus.trf2.xjus.util.Dao;

public class TaskIdxRecordIdRefreshPost implements IXjus.ITaskIdxRecordIdRefreshPost {
	@Override
	public void run(TaskIdxRecordIdRefreshPostRequest req, TaskIdxRecordIdRefreshPostResponse resp) throws Exception {
		System.out.println("índice " + req.idx + " - registro " + req.id + " - buscando");

		ISearch search = new GaeSearchImpl();

		try (IPersistence dao = new Dao()) {
			Index idx = dao.loadIndex(req.idx);
			if (idx == null)
				return;

			SwaggerAsyncResponse<RecordIdGetResponse> recordIdAsync = SwaggerCall.callAsync(getContext(),
					idx.getToken(), "get", idx.getApi() + "/record/" + req.id, req, RecordIdGetResponse.class)
					.get(30, TimeUnit.SECONDS);
			RecordIdGetResponse r = recordIdAsync.getRespOrThrowException();

			if ("REMOVED".equals(r.status)) {
				search.removeDocument(req.idx, r.id);
				System.out.println("índice " + req.idx + " - registro " + req.id + " - removido");
			} else {
				search.addDocument(idx.getIdx(), r);
				System.out.println("índice " + req.idx + " - registro " + req.id + " - gravado");
			}
		}
	}

	public String getContext() {
		return "reindexar";
	}
}
