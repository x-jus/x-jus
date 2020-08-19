package br.jus.trf2.xjus;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.crivano.swaggerservlet.SwaggerAsyncResponse;
import com.crivano.swaggerservlet.SwaggerCall;

import br.jus.trf2.xjus.IXjus.IndexIdxRefreshPostRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxRefreshPostResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexRefreshStatus;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.AllReferencesGetResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ISearch;
import br.jus.trf2.xjus.services.gae.GaeSearchImpl;
import br.jus.trf2.xjus.util.Dao;

public class IndexIdxRefreshPost implements IXjus.IIndexIdxRefreshPost {
	private static final Logger log = Logger.getLogger(IndexIdxRefreshPost.class.getName());

	@Override
	public void run(IndexIdxRefreshPostRequest req, IndexIdxRefreshPostResponse resp) throws Exception {
		Utils.assertUserCorrente();

		ISearch search = new GaeSearchImpl();

		try (IPersistence dao = new Dao()) {

			IndexRefreshStatus sts = new IndexRefreshStatus();
			sts.setIdx(req.idx);
			dao.removeIndexRefreshStatus(req.idx);
			System.out.println("removendo refresh-status para forçar a reindexação de " + req.idx);
			if (false)
				return;

			System.out.println(
					"refrescando índice " + req.idx + " em uma única operação. Isso só deve ser usado para debug!");

			Index idx = dao.loadIndex(req.idx);

			SwaggerAsyncResponse<AllReferencesGetResponse> allRefsAsync = SwaggerCall.callAsync(getContext(),
					idx.getToken(), "get", idx.getApi() + "/all-references?max=" + idx.getMaxBuild(), req,
					AllReferencesGetResponse.class).get(30, TimeUnit.SECONDS);
			AllReferencesGetResponse allRefs = allRefsAsync.getRespOrThrowException();

			search.removeIndex(req.idx);

			for (Reference ref : allRefs.list) {
				SwaggerAsyncResponse<RecordIdGetResponse> recordIdAsync = SwaggerCall.callAsync(getContext(),
						idx.getToken(), "get", idx.getApi() + "/record/" + ref.id, req, RecordIdGetResponse.class)
						.get(30, TimeUnit.SECONDS);
				RecordIdGetResponse r = recordIdAsync.getRespOrThrowException();
				search.addDocument(idx.getIdx(), r);
				log.info(r.title);
			}
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
