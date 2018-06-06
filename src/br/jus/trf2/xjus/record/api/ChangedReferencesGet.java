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
		if (req.last == null) {
			{
				Reference ref = new Reference();
				ref.id = "0000000001";
				ref.date = "2018-01-01";
				resp.list.add(ref);
			}
			{
				Reference ref = new Reference();
				ref.id = "0000000002";
				ref.date = "2018-01-02";
				resp.list.add(ref);
			}
		} else if (req.last.equals("2018-01-02;0000000002")) {
			{
				Reference ref = new Reference();
				ref.id = "0000000003";
				ref.date = "2018-01-03";
				resp.list.add(ref);
			}
			{
				Reference ref = new Reference();
				ref.id = "0000000004";
				ref.date = "2018-01-04";
				resp.list.add(ref);
			}
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
