package br.jus.trf2.xjus.services.gae;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.FacetResult;
import com.google.appengine.api.search.FacetResultValue;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;

import br.jus.trf2.xjus.IXjus.Facet;
import br.jus.trf2.xjus.IXjus.FacetValue;
import br.jus.trf2.xjus.IXjus.IndexIdxQueryGetResponse;
import br.jus.trf2.xjus.IXjus.Record;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;
import br.jus.trf2.xjus.services.ISearch;
import br.jus.trf2.xjus.services.gae.Search.SearchResults;

public class GaeSearchImpl implements ISearch {

	@Override
	public void addDocument(String idx, RecordIdGetResponse r) throws Exception {
		Document d = Search.buildDocument(r);
		Search.indexADocument(idx, d);
	}

	@Override
	public void removeIndex(String idx) {
		Search.deleteIndex(idx);

	}

	@Override
	public void removeDocument(String idx, String id) {
		Search.deleteDocument(idx, id);
	}

	@Override
	public void query(String idx, String filter, String facets, Integer page, Integer perpage, String acl,
			IndexIdxQueryGetResponse resp) {
		SearchResults ret = Search.search(idx, filter, facets, page, perpage, acl);

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

	public List<String> getDocumentIds(String indexName, String startId, int count) {
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
		com.google.appengine.api.search.Index index = SearchServiceFactory.getSearchService().getIndex(indexSpec);

		List<String> docIds = new ArrayList<>();
		String id = startId;
		int i = 0;

		// looping because getRange by default returns up to 100 documents
		// at a time
		while (true) {
			// Return a set of doc_ids.
			GetRequest request = GetRequest.newBuilder().setReturningIdsOnly(true).setStartId(id)
					.setLimit(count - i > 100 ? 100 : count - i).setIncludeStart(false).build();
			GetResponse<Document> response = index.getRange(request);
			if (response.getResults().isEmpty()) {
				break;
			}
			for (Document doc : response) {
				id = doc.getId();
				docIds.add(id);
				i++;
			}
			if (count <= i)
				break;
		}
		return docIds;
	}

}
