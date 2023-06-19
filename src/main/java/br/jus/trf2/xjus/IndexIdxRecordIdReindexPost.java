package br.jus.trf2.xjus;

import javax.enterprise.context.RequestScoped;

import com.crivano.swaggerservlet.SwaggerUtils;

import br.jus.trf2.xjus.services.ITask;

@RequestScoped
public class IndexIdxRecordIdReindexPost implements IXjus.IIndexIdxRecordIdReindexPost {

	@Override
	public void run(Request req, Response resp, XjusContext ctx) throws Exception {
		resp.id = req.id;
		SwaggerUtils.log(this.getClass()).debug("reindexando registro " + req.id + " do índice " + req.idx);

		if (req.sync != null && req.sync) {
			IndexIdxRecordIdReindexPost.Request req2 = new IndexIdxRecordIdReindexPost.Request();
			req2.id = req.id;
			req2.idx = req.idx;
			IndexIdxRecordIdReindexPost.Response resp2 = new IndexIdxRecordIdReindexPost.Response();
			new IndexIdxRecordIdReindexPost().run(req2, resp2, ctx);
		} else {
			ITask queue = XjusFactory.getQueue();
			queue.addRefreshDocument(req.idx, req.id);
		}
	}

	public String getContext() {
		return "reindexar registro";
	}
}
