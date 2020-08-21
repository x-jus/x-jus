package br.jus.trf2.xjus;

import br.jus.trf2.xjus.IXjus.IndexIdxDeleteRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxDeleteResponse;
import br.jus.trf2.xjus.IXjus.SearchIndex;
import br.jus.trf2.xjus.services.IPersistence;

public class IndexIdxDelete implements IXjus.IIndexIdxDelete {

	@Override
	public void run(IndexIdxDeleteRequest req, IndexIdxDeleteResponse resp) throws Exception {
		Utils.assertUserCorrente();

		try (IPersistence dao = XjusFactory.getDao()) {
			try {
				dao.removeIndex(req.idx);
			} catch (Exception e) {
				dao.rollback();
			}
		}
		resp.index = new SearchIndex();
		resp.index.idx = req.idx;
	}

	public String getContext() {
		return "remover índice";
	}

}
