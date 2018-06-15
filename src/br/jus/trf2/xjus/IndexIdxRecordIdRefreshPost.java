package br.jus.trf2.xjus;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import br.jus.trf2.xjus.IXjus.IndexIdxRecordIdRefreshPostRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxRecordIdRefreshPostResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;

import com.crivano.gae.Dao;
import com.crivano.gae.HttpGAE;
import com.crivano.gae.Search;
import com.crivano.swaggerservlet.SwaggerUtils;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.googlecode.objectify.Key;

public class IndexIdxRecordIdRefreshPost implements
		IXjus.IIndexIdxRecordIdRefreshPost {
	@Override
	public void run(IndexIdxRecordIdRefreshPostRequest req,
			IndexIdxRecordIdRefreshPostResponse resp) throws Exception {
		System.out.println("índice " + req.idx + " - registro " + req.id
				+ " - buscando");

		Dao dao = new Dao();
		Key<Index> key = Key.create(Index.class, req.idx);
		Index idx = dao.load(key);
		if (idx == null)
			return;

		RecordIdGetResponse r = (RecordIdGetResponse) SwaggerUtils.fromJson(
				new String(HttpGAE
						.fetchAsync(idx.token,
								new URL(idx.api + "/record/" + req.id),
								HTTPMethod.GET, null, null).get().getContent(),
						StandardCharsets.UTF_8), RecordIdGetResponse.class);
		if ("REMOVED".equals(r.status)) {
			Search.deleteDocument(req.idx, r.id);
			System.out.println("índice " + req.idx + " - registro " + req.id
					+ " - removido");
		} else {
			Document d = Search.buildDocument(r);
			Search.indexADocument(req.idx, d);
			System.out.println("índice " + req.idx + " - registro " + req.id
					+ " - gravado");
		}
	}

	public String getContext() {
		return "reindexar";
	}
}
