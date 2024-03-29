package br.jus.trf2.xjus.record.api;

import java.sql.Date;
import java.util.ArrayList;

import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Reference;

public class ChangedReferencesGet implements IXjusRecordAPI.IChangedReferencesGet {

	@Override
	public void run(Request req, Response resp, XjusRecordAPIContext ctx) throws Exception {
		resp.list = new ArrayList<>();
		if (req.lastdate == null) {
			{
				Reference ref = new Reference();
				ref.id = "0000000001";
				ref.date = Date.valueOf("2018-01-01");
				resp.list.add(ref);
			}
			{
				Reference ref = new Reference();
				ref.id = "0000000002";
				ref.date = Date.valueOf("2018-01-02");
				resp.list.add(ref);
			}
		} else if (req.lastdate.equals(Date.valueOf("2018-01-02"))) {
			{
				Reference ref = new Reference();
				ref.id = "0000000003";
				ref.date = Date.valueOf("2018-01-03");
				resp.list.add(ref);
			}
			{
				Reference ref = new Reference();
				ref.id = "0000000004";
				ref.date = Date.valueOf("2018-01-04");
				resp.list.add(ref);
			}
		}
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
