package br.jus.trf2.xjus;

import br.jus.trf2.xjus.IXjus.IndexIdxPostRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxPostResponse;
import br.jus.trf2.xjus.IXjus.SearchIndex;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.util.Dao;

public class IndexIdxPost implements IXjus.IIndexIdxPost {

	@Override
	public void run(IndexIdxPostRequest req, IndexIdxPostResponse resp) throws Exception {
		Utils.assertUserCorrente();

		try (IPersistence dao = XjusFactory.getDao()) {
			try {
				Index idx = dao.loadIndex(req.idx);
				if (idx == null) {
					idx = new Index();
					idx.setIdx(req.idx);
				}
				idx.setDescr(req.descr);
				idx.setApi(req.api);
				idx.setToken(req.token);
				idx.setActive(req.active);
				if (req.maxBuild != null)
					idx.setMaxBuild(Integer.parseInt(req.maxBuild));
				if (req.maxRefresh != null)
					idx.setMaxRefresh(Integer.parseInt(req.maxRefresh));
				idx.setSecret(req.secret);
				dao.saveIndex(idx);

				resp.index = new SearchIndex();
				resp.index.idx = idx.getIdx();
				resp.index.descr = idx.getDescr();
				resp.index.api = idx.getApi();
				resp.index.token = idx.getToken();
				resp.index.active = idx.getActive();
				if (idx.getMaxBuild() != null)
					resp.index.maxBuild = idx.getMaxBuild().toString();
				if (idx.getMaxRefresh() != null)
					resp.index.maxRefresh = idx.getMaxRefresh().toString();
				resp.index.secret = idx.getSecret();
			} catch (Exception e) {
				dao.rollback();
			}
		}
	}

	public String getContext() {
		return "atualizar definições de índice";
	}

}
