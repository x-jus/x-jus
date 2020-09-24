package br.jus.trf2.xjus;

import java.util.concurrent.TimeUnit;

import com.crivano.swaggerservlet.SwaggerAsyncResponse;
import com.crivano.swaggerservlet.SwaggerCall;
import com.crivano.swaggerservlet.SwaggerUtils;

import br.jus.trf2.xjus.IXjus.TaskIdxRecordIdRefreshPostRequest;
import br.jus.trf2.xjus.IXjus.TaskIdxRecordIdRefreshPostResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetRequest;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ISearch;

public class TaskIdxRecordIdRefreshPost implements IXjus.ITaskIdxRecordIdRefreshPost {
	@Override
	public void run(TaskIdxRecordIdRefreshPostRequest req, TaskIdxRecordIdRefreshPostResponse resp) throws Exception {
		SwaggerUtils.log(this.getClass()).debug("índice " + req.idx + " - registro " + req.id + " - buscando");

		ISearch search = XjusFactory.getSearch();

		try (IPersistence dao = XjusFactory.getDao()) {
			Index idx = dao.loadIndex(req.idx);
			if (idx == null)
				return;

			boolean removed = false;
			try {
				SwaggerAsyncResponse<RecordIdGetResponse> recordIdAsync = SwaggerCall
						.callAsync(getContext(), idx.getToken(), "GET", idx.getApi() + "/record/" + req.id,
								new RecordIdGetRequest(), RecordIdGetResponse.class)
						.get(30, TimeUnit.SECONDS);
				RecordIdGetResponse r = recordIdAsync.getRespOrThrowException();
				if ("REMOVED".equals(r.status)) {
					removed = true;
				} else {
					search.addDocument(idx.getIdx(), r);
					SwaggerUtils.log(this.getClass())
							.debug("índice " + req.idx + " - registro " + req.id + " - gravado");
				}
			} catch (Exception ex) {
				if ("REMOVED".equals(ex.getMessage()))
					removed = true;
			}
			if (removed) {
				search.removeDocument(req.idx, req.id);
				SwaggerUtils.log(this.getClass()).debug("índice " + req.idx + " - registro " + req.id + " - removido");
			}
		}
	}

	public String getContext() {
		return "reindexar";
	}
}
