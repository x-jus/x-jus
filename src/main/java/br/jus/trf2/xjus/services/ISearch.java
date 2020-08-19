package br.jus.trf2.xjus.services;

import br.jus.trf2.xjus.IXjus.IndexIdxQueryGetResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;

public interface ISearch {
	void addDocument(String idx, RecordIdGetResponse d) throws Exception;

	void removeIndex(String idx) throws Exception;

	void removeDocument(String idx, String id) throws Exception;

	void query(String idx, String filter, String facets, Integer page, Integer perpage, String acl,
			IndexIdxQueryGetResponse resp) throws Exception;
}
