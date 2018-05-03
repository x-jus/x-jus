package br.jus.trf2.xjus.record.api;

import java.util.List;

import com.crivano.swaggerservlet.ISwaggerMethod;
import com.crivano.swaggerservlet.ISwaggerModel;
import com.crivano.swaggerservlet.ISwaggerRequest;
import com.crivano.swaggerservlet.ISwaggerResponse;

public interface IXjusRecordAPI {
	public class Id implements ISwaggerModel {
	}

	public class Object implements ISwaggerModel {
	}

	public class Acl implements ISwaggerModel {
	}

	public class Refresh implements ISwaggerModel {
	}

	public class Last implements ISwaggerModel {
	}

	public class Url implements ISwaggerModel {
	}

	public class Code implements ISwaggerModel {
	}

	public class Title implements ISwaggerModel {
	}

	public class Content implements ISwaggerModel {
	}

	public class Field implements ISwaggerModel {
		public String name;
		public String kind;
		public String value;
	}

	public class Facet implements ISwaggerModel {
		public String name;
		public String kind;
		public String value;
	}

	public class Status implements ISwaggerModel {
	}

	public class Reference implements ISwaggerModel {
		public String id;
	}

	public class Error implements ISwaggerModel {
		public String errormsg;
	}

	public class AllReferencesGetRequest implements ISwaggerRequest {
		public String id;
		public String max;
	}

	public class AllReferencesGetResponse implements ISwaggerResponse {
		public List<Reference> list;
	}

	public interface IAllReferencesGet extends ISwaggerMethod {
		public void run(AllReferencesGetRequest req,
				AllReferencesGetResponse resp) throws Exception;
	}

	public class ChangedReferencesGetRequest implements ISwaggerRequest {
		public String last;
		public String max;
	}

	public class ChangedReferencesGetResponse implements ISwaggerResponse {
		public List<Reference> list;
		public String last;
	}

	public interface IChangedReferencesGet extends ISwaggerMethod {
		public void run(ChangedReferencesGetRequest req,
				ChangedReferencesGetResponse resp) throws Exception;
	}

	public class RecordIdGetRequest implements ISwaggerRequest {
		public String id;
	}

	public class RecordIdGetResponse implements ISwaggerResponse {
		public String id;
		public String status;
		public String acl;
		public String refresh;
		public String url;
		public String code;
		public String title;
		public String content;
		public List<Field> field;
		public List<Facet> facet;
	}

	public interface IRecordIdGet extends ISwaggerMethod {
		public void run(RecordIdGetRequest req, RecordIdGetResponse resp)
				throws Exception;
	}

}