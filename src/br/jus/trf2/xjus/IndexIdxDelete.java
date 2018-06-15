package br.jus.trf2.xjus;

import br.jus.trf2.xjus.IXjus.IndexIdxDeleteRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxDeleteResponse;
import br.jus.trf2.xjus.IXjus.SearchIndex;
import br.jus.trf2.xjus.model.Index;

import com.crivano.gae.Dao;
import com.googlecode.objectify.Key;

public class IndexIdxDelete implements IXjus.IIndexIdxDelete {

	@Override
	public void run(IndexIdxDeleteRequest req, IndexIdxDeleteResponse resp)
			throws Exception {
		Utils.assertUserCorrente();

		Dao dao = new Dao();
		Key<Index> key = Key.create(Index.class, req.idx);
		dao.del(key);

		resp.index = new SearchIndex();
		resp.index.idx = req.idx;
	}

	public String getContext() {
		return "remover índice";
	}

}
