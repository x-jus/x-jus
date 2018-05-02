package br.jus.trf2.xjus;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import br.jus.trf2.xjus.IXjus.IndexIdxRefreshPostRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxRefreshPostResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.AllReferencesGetResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;

import com.crivano.gae.Dao;
import com.crivano.gae.HttpGAE;
import com.crivano.gae.Search;
import com.crivano.swaggerservlet.SwaggerUtils;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.googlecode.objectify.Key;

public class IndexIdxRefreshPost implements IXjus.IIndexIdxRefreshPost {
	private static final Logger log = Logger
			.getLogger(IndexIdxRefreshPost.class.getName());

	@Override
	public void run(IndexIdxRefreshPostRequest req,
			IndexIdxRefreshPostResponse resp) throws Exception {
		System.out.println("refrescando índice " + req.idx);

		Dao dao = new Dao();
		Key<Index> key = Key.create(Index.class, req.idx);
		Index idx = dao.load(key);

		AllReferencesGetResponse allRefs = (AllReferencesGetResponse) SwaggerUtils
				.fromJson(
						new String(HttpGAE
								.fetchAsync(idx.token,
										new URL(idx.api + "/all-references"),
										HTTPMethod.GET, null, null).get()
								.getContent(), StandardCharsets.UTF_8),
						AllReferencesGetResponse.class);

		Search.deleteIndex(req.idx);

		for (Reference ref : allRefs.list) {
			RecordIdGetResponse r = (RecordIdGetResponse) SwaggerUtils
					.fromJson(
							new String(HttpGAE
									.fetchAsync(
											idx.token,
											new URL(idx.api + "/record/"
													+ ref.id), HTTPMethod.GET,
											null, null).get().getContent(),
									StandardCharsets.UTF_8),
							RecordIdGetResponse.class);
			Document d = Search.buildDocument(r);
			Search.indexADocument(req.idx, d);
			log.info(r.title);
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
