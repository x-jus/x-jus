package br.jus.trf2.xjus.services;

import java.util.List;

import br.jus.trf2.xjus.IndexIdxQueryGet;
import br.jus.trf2.xjus.record.api.RecordIdGet;

public interface ISearch {
	void addDocument(String idx, RecordIdGet.Response d) throws Exception;

	void removeIndex(String idx) throws Exception;

	void removeDocument(String idx, String id) throws Exception;

	void query(String idx, String filter, String facets, Integer page, Integer perpage, String acl,
			IndexIdxQueryGet.Response resp) throws Exception;

	void query(String idx, String filter, String facets, Integer page, Integer perpage, String acl, String code,
			String fromDate, String toDate, IndexIdxQueryGet.Response resp) throws Exception;

	List<String> getDocumentIds(String indexName, String idStart, int maxRefresh) throws Exception;

	Long count(String idx) throws Exception;
}
