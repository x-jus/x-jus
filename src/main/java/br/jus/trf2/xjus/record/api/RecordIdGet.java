package br.jus.trf2.xjus.record.api;

import java.util.ArrayList;

import br.jus.trf2.xjus.record.api.IXjusRecordAPI.Field;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetRequest;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;

public class RecordIdGet implements IXjusRecordAPI.IRecordIdGet {

	@Override
	public void run(RecordIdGetRequest req, RecordIdGetResponse resp)
			throws Exception {
		resp.id = req.id;
		resp.url = "http://teste/" + req.id;
		resp.acl = "PUBLIC";
		resp.refresh = "NEVER";
		resp.code = "TRF2-MEM-2018/0000" + req.id.substring(req.id.length());
		resp.field = new ArrayList<>();
		resp.facet = new ArrayList<>();
		if ("documento:0000000001".equals(req.id)) {
			resp.title = "Paralelepípedo";
			resp.content = "Disseram que na minha rua tem paralelepípedo feito de paralelogramos. Seis paralelogramos tem um paralelepípedo. Mil paralelepípedos tem uma paralelepipedovia. Uma paralelepipedovia tem mil paralelogramos. Então uma paralelepipedovia é uma paralelogramolandia?";
			addField(resp, "Tipo", "Trava-língua");
			addFacet(resp, "Tipo", "Trava-língua");
			addField(resp, "Autor", "Desconhecido");
			addFacet(resp, "Autor", "Desconhecido");
		} else if ("documento:0000000002".equals(req.id)) {
			resp.title = "Quatro quadros";
			resp.content = "Há quatro quadros três e três quadros quatro. Sendo que quatro destes quadros são quadrados, um dos quadros quatro e três dos quadros três. Os três quadros que não são quadrados, são dois dos quadros quatro e um dos quadros três.";
			addField(resp, "Tipo", "Trava-língua");
			addFacet(resp, "Tipo", "Trava-língua");
			addField(resp, "Autor", "Desconhecido");
			addFacet(resp, "Autor", "Desconhecido");
		} else if ("documento:0000000003".equals(req.id)) {
			resp.title = "Mafagafos";
			resp.content = "Um ninho de mafagafos tem sete mafagafinhos, quem desmafaguifar um ninho de mafagafos, um bom desmafaguifador será. Assim como eu desmafaguifei, um bom desmafaguifador serei.";
			addField(resp, "Tipo", "Trava-língua");
			addFacet(resp, "Tipo", "Trava-língua");
			addField(resp, "Autor", "Mafagafólogo");
			addFacet(resp, "Autor", "Mafagafólogo");
		} else if ("documento:0000000004".equals(req.id)) {
			resp.title = "Amor é fogo que arde sem se ver";
			resp.content = "Amor é fogo que arde sem se ver; É ferida que dói e não se sente; É um contentamento descontente; É dor que desatina sem doer;";
			addField(resp, "Tipo", "Poema");
			addFacet(resp, "Tipo", "Poema");
			addField(resp, "Autor", "Luís de Camões");
			addFacet(resp, "Autor", "Luís de Camões");
		}
	}

	public void addField(RecordIdGetResponse resp, String name, String value) {
		Field fld = new Field();
		fld.kind = "TEXT";
		fld.name = name;
		fld.value = value;
		resp.field.add(fld);
	}

	public void addFacet(RecordIdGetResponse resp, String name, String value) {
		br.jus.trf2.xjus.record.api.IXjusRecordAPI.Facet facet = new br.jus.trf2.xjus.record.api.IXjusRecordAPI.Facet();
		facet.kind = "KEYWORD";
		facet.name = name;
		facet.value = value;
		resp.facet.add(facet);
	}

	public String getContext() {
		return "obter a lista de índices";
	}
}
