package br.jus.trf2.xjus;

import br.jus.trf2.xjus.IXjus.IndexIdxRecordPostRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxRecordPostResponse;

public class IndexIdxRecordPost implements IXjus.IIndexIdxRecordPost {
	@Override
	public void run(IndexIdxRecordPostRequest req,
			IndexIdxRecordPostResponse resp) throws Exception {
		Utils.assertUserCorrente();

	}

	public String getContext() {
		return "reindexar um registro";
	}
}
