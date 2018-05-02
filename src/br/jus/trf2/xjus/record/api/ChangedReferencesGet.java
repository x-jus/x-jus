package br.jus.trf2.xjus.record.api;

import java.util.ArrayList;

import br.jus.trf2.xjus.record.api.IXjusRecordAPI.ChangedReferencesGetRequest;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.ChangedReferencesGetResponse;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;

public class ChangedReferencesGet implements
		IXjusRecordAPI.IChangedReferencesGet {

	@Override
	public void run(ChangedReferencesGetRequest req,
			ChangedReferencesGetResponse resp) throws Exception {
		resp.list = new ArrayList<>();

		{
			Reference ref = new Reference();
			ref.id = "documento:0000000002";
			resp.list.add(ref);
		}
		{
			Reference ref = new Reference();
			ref.id = "documento:0000000003";
			resp.list.add(ref);
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
