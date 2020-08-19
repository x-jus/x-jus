package br.jus.trf2.xjus;

import java.util.ArrayList;
import java.util.List;

import br.jus.trf2.xjus.IXjus.IndexGetRequest;
import br.jus.trf2.xjus.IXjus.IndexGetResponse;
import br.jus.trf2.xjus.IXjus.SearchIndex;
import br.jus.trf2.xjus.model.Index;
import br.jus.trf2.xjus.model.IndexBuildStatus;
import br.jus.trf2.xjus.model.IndexRefreshStatus;
import br.jus.trf2.xjus.model.IndexStatus;
import br.jus.trf2.xjus.services.IPersistence;
import br.jus.trf2.xjus.util.Dao;

public class IndexGet implements IXjus.IIndexGet {

	@Override
	public void run(IndexGetRequest req, IndexGetResponse resp) throws Exception {
		Utils.assertUserCorrente();

		resp.list = new ArrayList<>();

		try (IPersistence dao = new Dao()) {
			List<Index> l = dao.loadIndexes();
			for (Index idx : l) {
				SearchIndex i = new SearchIndex();
				i.idx = idx.getIdx();
				i.active = idx.getActive();
				i.descr = idx.getDescr();
				i.api = idx.getApi();
				i.token = idx.getToken();
				if (idx.getMaxBuild() != null)
					i.maxBuild = idx.getMaxBuild().toString();
				if (idx.getMaxRefresh() != null)
					i.maxRefresh = idx.getMaxRefresh().toString();
				i.secret = idx.getSecret();

				IndexStatus sts = dao.loadIndexStatus(idx.getIdx());
				if (sts != null) {
					i.records = sts.getRecords().toString();
				}
				IndexBuildStatus stsBuild = dao.loadIndexBuildStatus(idx.getIdx());
				if (stsBuild != null) {
					i.buildRecords = stsBuild.getBuildRecords().toString();
					i.buildLastDate = stsBuild.getBuildLastdate();
					i.buildLastId = stsBuild.getBuildLastid();
					if (stsBuild.getBuildLastCount() != null)
						i.buildLastCount = stsBuild.getBuildLastCount().toString();
				}
				IndexRefreshStatus stsRefresh = dao.loadIndexRefreshStatus(idx.getIdx());
				if (stsRefresh != null) {
					i.refreshComplete = stsRefresh.isRefreshComplete();
					i.refreshLastId = stsRefresh.getRefreshLastId();
					i.refreshTimestamp = stsRefresh.getRefreshLastModified();
				}
				resp.list.add(i);
			}
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
