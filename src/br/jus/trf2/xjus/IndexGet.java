package br.jus.trf2.xjus;

import java.util.ArrayList;
import java.util.List;

import com.crivano.gae.Dao;
import com.googlecode.objectify.Key;

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
			if (idx.max != null)
				i.max = idx.max.toString();

			IndexStatus sts = dao.load(Key.create(IndexStatus.class, i.idx));
			if (sts != null) {
				i.records = sts.records.toString();
				i.last = sts.last;
			}
			resp.list.add(i);
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
