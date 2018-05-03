package br.jus.trf2.xjus;

import br.jus.trf2.xjus.IXjus.IndexIdxPostRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxPostResponse;
import br.jus.trf2.xjus.IXjus.SearchIndex;

import com.crivano.gae.Dao;
import com.googlecode.objectify.Key;

public class IndexIdxPost implements IXjus.IIndexIdxPost {

	@Override
	public void run(IndexIdxPostRequest req, IndexIdxPostResponse resp)
			throws Exception {
		Dao dao = new Dao();
		Key<Index> key = Key.create(Index.class, req.idx);
		Index idx = dao.load(key);
		if (idx == null) {
			idx = new Index();
			idx.idx = req.idx;
		}
		idx.descr = req.descr;
		idx.api = req.api;
		idx.token = req.token;
		idx.active = req.active;
		if (req.max != null)
			idx.max = Integer.parseInt(req.max);
		dao.save(idx);

		resp.index = new SearchIndex();
		resp.index.idx = idx.idx;
		resp.index.descr = idx.descr;
		resp.index.api = idx.api;
		resp.index.token = idx.token;
		resp.index.active = idx.active;
		if (idx.max != null)
			resp.index.max = idx.max.toString();
	}

	public String getContext() {
		return "atualizar definições de índice";
	}

}
