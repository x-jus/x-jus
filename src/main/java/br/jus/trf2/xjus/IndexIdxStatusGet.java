package br.jus.trf2.xjus;

import javax.enterprise.context.RequestScoped;

import com.crivano.swaggerservlet.SwaggerUtils;

import br.jus.trf2.xjus.IXjus.IndexIdxStatusGetRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxStatusGetResponse;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexBuildStatus;
import br.jus.trf2.xjus.model.IndexRefreshStatus;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.services.ISearch;
import br.jus.trf2.xjus.services.ITask;

@RequestScoped
public class IndexIdxStatusGet implements IXjus.IIndexIdxStatusGet {

	@Override
	public void run(IndexIdxStatusGetRequest req, IndexIdxStatusGetResponse resp) throws Exception {
		ISearch search = XjusFactory.getSearch();

		try (IPersistence dao = XjusFactory.getDao()) {
			Index idx = dao.loadIndex(req.idx);
			IndexBuildStatus bsts = dao.loadIndexBuildStatus(req.idx);
			IndexRefreshStatus rsts = dao.loadIndexRefreshStatus(req.idx);
			resp.buildCount = (double) bsts.getBuildRecords();
			resp.buildLastDate = bsts.getBuildLastdate();
			resp.buildLastId = bsts.getBuildLastid();
			resp.buildCursor = bsts.getBuildCursor();
			resp.refreshLastId = rsts.getRefreshLastId();
			Long count = search.count(req.idx);
			if (count != null)
				resp.count = (double) count;
			
			ITask queue = XjusFactory.getQueue();
			resp.buildQueuedTaskCount = queue.getBuildTaskCount();
			resp.refreshQueuedTaskCount = queue.getRefreshTaskCount();
		}
	}

	public String getContext() {
		return "pesquisar";
	}
}
