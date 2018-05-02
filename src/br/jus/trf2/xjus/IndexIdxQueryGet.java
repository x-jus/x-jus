package br.jus.trf2.xjus;

import java.util.ArrayList;

import br.jus.trf2.xjus.IXjus.Facet;
import br.jus.trf2.xjus.IXjus.FacetValue;
import br.jus.trf2.xjus.IXjus.IndexIdxQueryGetRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxQueryGetResponse;
import br.jus.trf2.xjus.IXjus.Record;

import com.crivano.gae.Search;
import com.crivano.gae.Search.SearchResults;
import com.google.appengine.api.search.FacetResult;
import com.google.appengine.api.search.FacetResultValue;
import com.google.appengine.api.search.ScoredDocument;

public class IndexIdxQueryGet implements IXjus.IIndexIdxQueryGet {

	@Override
	public void run(IndexIdxQueryGetRequest req, IndexIdxQueryGetResponse resp)
			throws Exception {
		Integer page = Integer.valueOf(req.page);
		Integer perpage = Integer.valueOf(req.perpage);

		if (page == null || page < 1)
			page = 1;

		if (perpage == null || perpage > 100)
			perpage = 20;

		SearchResults ret = Search.search(req.idx, req.filter, req.facets,
				page, perpage);

		resp.count = (double) ret.result.getNumberFound();

		resp.facets = new ArrayList<>();
		for (FacetResult f : ret.result.getFacets()) {
			Facet facet = new Facet();
			facet.name = f.getName();
			facet.values = new ArrayList<>();
			for (FacetResultValue v : f.getValues()) {
				FacetValue value = new FacetValue();
				value.name = v.getLabel();
				value.count = (double) v.getCount();
				value.refinementToken = v.getRefinementToken();
				facet.values.add(value);
			}
			resp.facets.add(facet);
		}

		resp.results = new ArrayList<>();
		for (ScoredDocument r : ret.result.getResults()) {
			Record rec = new Record();
			rec.url = r.getFields("url").iterator().next().getAtom();
			rec.title = r.getFields("title").iterator().next().getText();
			rec.code = r.getFields("code").iterator().next().getAtom();
			rec.content = r.getExpressions().iterator().next().getHTML();
			resp.results.add(rec);
		}

	}

	public String getContext() {
		return "pesquisar";
	}
}
