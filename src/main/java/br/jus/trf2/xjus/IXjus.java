package br.jus.trf2.xjus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.crivano.swaggerservlet.ISwaggerMethod;
import com.crivano.swaggerservlet.ISwaggerModel;
import com.crivano.swaggerservlet.ISwaggerRequest;
import com.crivano.swaggerservlet.ISwaggerResponse;

public interface IXjus {
	public static class Id implements ISwaggerModel {
	}

	public static class Name implements ISwaggerModel {
	}

	public static class Object implements ISwaggerModel {
	}

	public static class Acl implements ISwaggerModel {
	}

	public static class Refresh implements ISwaggerModel {
	}

	public static class Url implements ISwaggerModel {
	}

	public static class Code implements ISwaggerModel {
	}

	public static class Title implements ISwaggerModel {
	}

	public static class Content implements ISwaggerModel {
	}

	public static class Snippet implements ISwaggerModel {
	}

	public static class Idx implements ISwaggerModel {
	}

	public static class Descr implements ISwaggerModel {
	}

	public static class Api implements ISwaggerModel {
	}

	public static class Token implements ISwaggerModel {
	}

	public static class Active implements ISwaggerModel {
	}

	public static class MaxBuild implements ISwaggerModel {
	}

	public static class MaxRefresh implements ISwaggerModel {
	}

	public static class Secret implements ISwaggerModel {
	}

	public static class Records implements ISwaggerModel {
	}

	public static class LastDate implements ISwaggerModel {
	}

	public static class LastId implements ISwaggerModel {
	}

	public static class Timestamp implements ISwaggerModel {
	}

	public static class Status implements ISwaggerModel {
	}

	public static class Count implements ISwaggerModel {
	}

	public static class Complete implements ISwaggerModel {
	}

	public static class RefinementToken implements ISwaggerModel {
	}

	public static class Field implements ISwaggerModel {
		public String name;
		public String kind;
		public String value;
	}

	public static class SearchIndex implements ISwaggerModel {
		public String idx;
		public String descr;
		public String api;
		public String token;
		public Boolean active;
		public String maxBuild;
		public String maxRefresh;
		public String secret;
		public String records;
		public String buildRecords;
		public String buildLastCount;
		public Date buildLastDate;
		public String buildLastId;
		public String refreshLastId;
		public Date refreshTimestamp;
		public Boolean refreshComplete;
	}

	public static class Facet implements ISwaggerModel {
		public String name;
		public List<FacetValue> values = new ArrayList<>();
	}

	public static class FacetValue implements ISwaggerModel {
		public String name;
		public Double count;
		public String refinementToken;
	}

	public static class Record implements ISwaggerModel {
		public String id;
		public String object;
		public String acl;
		public String refresh;
		public String url;
		public String code;
		public String title;
		public String content;
		public List<Field> field = new ArrayList<>();
	}

	public static class User implements ISwaggerModel {
		public String gmail;
		public Boolean admin;
		public String loginUrl;
		public String logoutUrl;
	}

	public static class Error implements ISwaggerModel {
		public String errormsg;
	}

	public interface IUserGet extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
		}

		public static class Response implements ISwaggerResponse {
			public User user;
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface IIndexIdxQueryGet extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
			public String idx;
			public String filter;
			public String facets;
			public String page;
			public String perpage;
			public String acl;
		}

		public static class Response implements ISwaggerResponse {
			public Double count;
			public List<Facet> facets = new ArrayList<>();
			public List<Record> results = new ArrayList<>();
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface IIndexIdxStatusGet extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
			public String idx;
		}

		public static class Response implements ISwaggerResponse {
			public Double count;
			public Double buildCount;
			public String buildLastId;
			public Date buildLastDate;
			public String refreshLastId;
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface IIndexIdxRecordIdGet extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
			public String idx;
			public String id;
		}

		public static class Response implements ISwaggerResponse {
			public String id;
			public String object;
			public String acl;
			public String refresh;
			public String url;
			public String code;
			public String title;
			public String content;
			public List<Field> field = new ArrayList<>();
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface IIndexIdxRecordPost extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
			public String idx;
		}

		public static class Response implements ISwaggerResponse {
			public String id;
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface IIndexIdxRecordIdReindexPost extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
			public String idx;
			public String id;
			public Boolean sync;
		}

		public static class Response implements ISwaggerResponse {
			public String id;
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface ITaskBuildStepGet extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
		}

		public static class Response implements ISwaggerResponse {
			public String status;
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface ITaskIdxBuildStepPost extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
			public String idx;
		}

		public static class Response implements ISwaggerResponse {
			public String status;
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface ITaskRefreshStepGet extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
		}

		public static class Response implements ISwaggerResponse {
			public String status;
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface ITaskIdxRefreshStepPost extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
			public String idx;
		}

		public static class Response implements ISwaggerResponse {
			public String status;
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

	public interface ITaskIdxRecordIdRefreshPost extends ISwaggerMethod {
		public static class Request implements ISwaggerRequest {
			public String idx;
			public String id;
			public Boolean sync;
		}

		public static class Response implements ISwaggerResponse {
			public String id;
		}

		public void run(Request req, Response resp, XjusContext ctx) throws Exception;
	}

}
