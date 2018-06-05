package com.crivano.gae;

import java.util.ArrayList;
import java.util.List;

import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Facet;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.Query.Builder;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.StatusCode;

public class Search {
	public static class SearchResults {
		public Results<ScoredDocument> result;
	}

	public static SearchResults search(String indexName, String filter,
			String facets, Integer page, Integer perpage, String acl) {
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
		com.google.appengine.api.search.Index index = SearchServiceFactory
				.getSearchService().getIndex(indexSpec);

		if (acl == null)
			acl = "PUBLIC";

		String filterWithACL = filter.trim();
		if (filterWithACL.length() > 0)
			filterWithACL = "(" + filterWithACL + ") AND ";
		filterWithACL += "(";
		String[] split = acl.split(";");
		for (int i = 0; i < split.length; i++) {
			if (i > 0)
				filterWithACL += " OR ";
			filterWithACL += "acl:" + split[i];
		}
		filterWithACL += ")";
		System.out.println(filterWithACL);

		int offset = 0;
		if (page != null && perpage != null) {
			offset = (page - 1) * perpage;
		}

		Builder queryBuilder = Query.newBuilder();
		queryBuilder.setEnableFacetDiscovery(true);
		queryBuilder.setOptions(QueryOptions.newBuilder().setOffset(offset)
				.setFieldsToReturn("url", "code", "title")
				.setFieldsToSnippet("content"));
		if (facets != null) {
			String[] a = facets.split(",");
			for (String f : a)
				queryBuilder.addFacetRefinementFromToken(f);
		}
		Results<ScoredDocument> result = index.search(queryBuilder
				.build(filterWithACL));
		SearchResults r = new SearchResults();
		r.result = result;
		return r;
	}

	public static void indexADocument(String indexName, Document document) {
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
		com.google.appengine.api.search.Index index = SearchServiceFactory
				.getSearchService().getIndex(indexSpec);

		final int maxRetry = 3;
		int attempts = 0;
		int delay = 2;
		while (true) {
			try {
				index.put(document);
			} catch (PutException e) {
				if (StatusCode.TRANSIENT_ERROR.equals(e.getOperationResult()
						.getCode()) && ++attempts < maxRetry) { // retrying
					try {
						Thread.sleep(delay * 1000);
					} catch (InterruptedException e1) {
					}
					delay *= 2; // easy exponential backoff
					continue;
				} else {
					throw e; // otherwise throw
				}
			}
			break;
		}
	}

	public static Document buildDocument(RecordIdGetResponse r)
			throws Exception {
		com.google.appengine.api.search.Document.Builder builder = Document
				.newBuilder().setId(r.id);

		if (r.content != null)
			builder.addField(Field.newBuilder().setName("content")
					.setText(r.content));

		builder.addField(Field.newBuilder().setName("code").setAtom(r.code));
		builder.addField(Field.newBuilder().setName("url").setAtom(r.url));

		if (r.title != null)
			builder.addField(Field.newBuilder().setName("title")
					.setText(r.title));

		if (r.acl != null) {
			String[] split = r.acl.split(";");
			for (String s : split)
				builder.addField(Field.newBuilder().setName("acl").setAtom(s));
		}

		if (r.field != null) {
			for (br.jus.trf2.xjus.record.api.IXjusRecordAPI.Field f : r.field) {
				builder.addField(Field.newBuilder().setName(f.name)
						.setAtom(f.value));
			}
		}

		if (r.facet != null) {
			for (br.jus.trf2.xjus.record.api.IXjusRecordAPI.Facet f : r.facet) {
				if ("FLOAT".equals(f.kind))
					builder.addFacet(Facet.withNumber(f.name,
							Double.valueOf(f.value)));
				else
					builder.addFacet(Facet.withAtom(f.name, f.value));
			}
		}
		Document doc = builder.build();
		return doc;
	}

	public static void deleteDocument(String indexName, String id) {
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
		com.google.appengine.api.search.Index index = SearchServiceFactory
				.getSearchService().getIndex(indexSpec);
		index.delete(id);
	}

	public static long deleteIndex(String indexName) {
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
		com.google.appengine.api.search.Index index = SearchServiceFactory
				.getSearchService().getIndex(indexSpec);

		index.deleteSchema();

		long l = 0;
		// looping because getRange by default returns up to 100 documents
		// at a time
		while (true) {
			List<String> docIds = new ArrayList<>();
			// Return a set of doc_ids.
			GetRequest request = GetRequest.newBuilder()
					.setReturningIdsOnly(true).build();
			GetResponse<Document> response = index.getRange(request);
			if (response.getResults().isEmpty()) {
				break;
			}
			for (Document doc : response) {
				docIds.add(doc.getId());
				l++;
			}
			index.delete(docIds);
		}
		return l;
	}

	public static List<String> getDocumentIds(String indexName, String startId,
			int count) {
		IndexSpec indexSpec = IndexSpec.newBuilder().setName(indexName).build();
		com.google.appengine.api.search.Index index = SearchServiceFactory
				.getSearchService().getIndex(indexSpec);

		List<String> docIds = new ArrayList<>();
		String id = startId;
		int i = 0;

		// looping because getRange by default returns up to 100 documents
		// at a time
		while (true) {
			// Return a set of doc_ids.
			GetRequest request = GetRequest.newBuilder()
					.setReturningIdsOnly(true).setStartId(id)
					.setLimit(count - i > 100 ? 100 : count - i)
					.setIncludeStart(false).build();
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
