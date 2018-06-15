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

import com.crivano.gae.Dao;
import com.googlecode.objectify.Key;

public class IndexGet implements IXjus.IIndexGet {

	@Override
	public void run(IndexGetRequest req, IndexGetResponse resp)
			throws Exception {
		Utils.assertUserCorrente();
		
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
			if (idx.maxBuild != null)
				i.maxBuild = idx.maxBuild.toString();
			if (idx.maxRefresh != null)
				i.maxRefresh = idx.maxRefresh.toString();
			i.secret = idx.secret;

			IndexStatus sts = dao.load(Key.create(IndexStatus.class, i.idx));
			if (sts != null) {
				i.records = sts.records.toString();
			}
			IndexBuildStatus stsBuild = dao.load(Key.create(
					IndexBuildStatus.class, i.idx));
			if (stsBuild != null) {
				i.buildRecords = stsBuild.records.toString();
				i.buildLastDate = stsBuild.lastdate;
				i.buildLastId = stsBuild.lastid;
				if (stsBuild.lastCount != null)
					i.buildLastCount = stsBuild.lastCount.toString();
			}
			IndexRefreshStatus stsRefresh = dao.load(Key.create(
					IndexRefreshStatus.class, i.idx));
			if (stsRefresh != null) {
				i.refreshComplete = stsRefresh.complete;
				i.refreshLastId = stsRefresh.id;
				i.refreshTimestamp = stsRefresh.lastModified;
			}
			resp.list.add(i);
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
