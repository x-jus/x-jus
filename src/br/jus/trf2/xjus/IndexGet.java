package br.jus.trf2.xjus;

import java.util.ArrayList;
import java.util.List;

import com.crivano.gae.Dao;

import br.jus.trf2.xjus.IXjus.IndexGetRequest;
import br.jus.trf2.xjus.IXjus.IndexGetResponse;
import br.jus.trf2.xjus.IXjus.SearchIndex;

public class IndexGet implements IXjus.IIndexGet {

	@Override
	public void run(IndexGetRequest req, IndexGetResponse resp)
			throws Exception {
		resp.list = new ArrayList<>();

		Dao dao = new Dao();
		List<Index> l = dao.loadAll(Index.class);
		for (Index idx : l) {
			SearchIndex i = new SearchIndex();
			i.idx = idx.idx;
			i.active = idx.active;
			i.descr = idx.descr;
			i.api = idx.api;
			i.token = idx.token;
			// i.records = 2345.0;
			resp.list.add(i);
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
