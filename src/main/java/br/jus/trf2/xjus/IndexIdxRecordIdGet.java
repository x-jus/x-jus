package br.jus.trf2.xjus;

import br.jus.trf2.xjus.IXjus.IndexIdxRecordIdGetRequest;
import br.jus.trf2.xjus.IXjus.IndexIdxRecordIdGetResponse;

public class IndexIdxRecordIdGet implements IXjus.IIndexIdxRecordIdGet {

	@Override
	public void run(IndexIdxRecordIdGetRequest req,
			IndexIdxRecordIdGetResponse resp) throws Exception {
		Utils.assertUserCorrente();

	}

	public String getContext() {
		return "obter um registro";
	}
}