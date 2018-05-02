package br.jus.trf2.xjus;

import br.jus.trf2.xjus.IXjus.IndexIdxRecordIdRefreshPostRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxRecordIdRefreshPostResponse;

public class IndexIdxRecordIdRefreshPost implements
		IXjus.IIndexIdxRecordIdRefreshPost {
	@Override
	public void run(IndexIdxRecordIdRefreshPostRequest req,
			IndexIdxRecordIdRefreshPostResponse resp) throws Exception {
		// TODO Auto-generated method stub

	}

	public String getContext() {
		return "reindexar";
	}
}
