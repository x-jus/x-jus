package br.jus.trf2.xjus;

import java.util.List;

import com.crivano.swaggerservlet.ISwaggerMethod;
import com.crivano.swaggerservlet.ISwaggerModel;
import com.crivano.swaggerservlet.ISwaggerRequest;
import com.crivano.swaggerservlet.ISwaggerResponse;

public interface IXjus {
	public class Id implements ISwaggerModel {
	}

	public class Name implements ISwaggerModel {
	}

	public class Object implements ISwaggerModel {
	}

	public class Acl implements ISwaggerModel {
	}

	public class Refresh implements ISwaggerModel {
	}

	public class Url implements ISwaggerModel {
	}

	public class Code implements ISwaggerModel {
	}

	public class Title implements ISwaggerModel {
	}

	public class Content implements ISwaggerModel {
	}

	public class Snippet implements ISwaggerModel {
	}

	public class Idx implements ISwaggerModel {
	}

	public class Descr implements ISwaggerModel {
	}

	public class Api implements ISwaggerModel {
	}

	public class Token implements ISwaggerModel {
	}

	public class Active implements ISwaggerModel {
	}

	public class MaxBuild implements ISwaggerModel {
	}

	public class MaxRefresh implements ISwaggerModel {
	}

	public class Secret implements ISwaggerModel {
	}

	public class Records implements ISwaggerModel {
	}

	public class Last implements ISwaggerModel {
	}

	public class Status implements ISwaggerModel {
	}

	public class Count implements ISwaggerModel {
	}

	public class RefinementToken implements ISwaggerModel {
	}

	public class Field implements ISwaggerModel {
		public String name;
		public String kind;
		public String value;
	}

	public class SearchIndex implements ISwaggerModel {
		public String idx;
		public String descr;
		public String api;
		public String token;
		public Boolean active;
		public String maxBuild;
		public String maxRefresh;
		public String secret;
		public String records;
		public String last;
	}

	public class Facet implements ISwaggerModel {
		public String name;
		public List<FacetValue> values;
	}

	public class FacetValue implements ISwaggerModel {
		public String name;
		public Double count;
		public String refinementToken;
	}

	public class Record implements ISwaggerModel {
		public String id;
		public String object;
		public String acl;
		public String refresh;
		public String url;
		public String code;
		public String title;
		public String content;
		public List<Field> field;
	}

	public class Error implements ISwaggerModel {
		public String errormsg;
	}

	public class IndexGetRequest implements ISwaggerRequest {
	}

	public class IndexGetResponse implements ISwaggerResponse {
		public List<SearchIndex> list;
	}

	public interface IIndexGet extends ISwaggerMethod {
		public void run(IndexGetRequest req, IndexGetResponse resp)
				throws Exception;
	}

	public class IndexIdxPostRequest implements ISwaggerRequest {
		public String idx;
		public String descr;
		public String api;
		public String token;
		public Boolean active;
		public String maxBuild;
		public String maxRefresh;
		public String secret;
	}

	public class IndexIdxPostResponse implements ISwaggerResponse {
		public SearchIndex index;
	}

	public interface IIndexIdxPost extends ISwaggerMethod {
		public void run(IndexIdxPostRequest req, IndexIdxPostResponse resp)
				throws Exception;
	}

	public class IndexIdxDeleteRequest implements ISwaggerRequest {
		public String idx;
	}

	public class IndexIdxDeleteResponse implements ISwaggerResponse {
		public SearchIndex index;
	}

	public interface IIndexIdxDelete extends ISwaggerMethod {
		public void run(IndexIdxDeleteRequest req, IndexIdxDeleteResponse resp)
				throws Exception;
	}

	public class IndexIdxRefreshPostRequest implements ISwaggerRequest {
		public String idx;
	}

	public class IndexIdxRefreshPostResponse implements ISwaggerResponse {
		public String id;
	}

	public interface IIndexIdxRefreshPost extends ISwaggerMethod {
		public void run(IndexIdxRefreshPostRequest req,
				IndexIdxRefreshPostResponse resp) throws Exception;
	}

	public class IndexIdxQueryGetRequest implements ISwaggerRequest {
		public String idx;
		public String filter;
		public String facets;
		public String page;
		public String perpage;
		public String acl;
	}

	public class IndexIdxQueryGetResponse implements ISwaggerResponse {
		public Double count;
		public List<Facet> facets;
		public List<Record> results;
	}

	public interface IIndexIdxQueryGet extends ISwaggerMethod {
		public void run(IndexIdxQueryGetRequest req,
				IndexIdxQueryGetResponse resp) throws Exception;
	}

	public class IndexIdxRecordIdGetRequest implements ISwaggerRequest {
		public String idx;
		public String id;
	}

	public class IndexIdxRecordIdGetResponse implements ISwaggerResponse {
		public String id;
		public String object;
		public String acl;
		public String refresh;
		public String url;
		public String code;
		public String title;
		public String content;
		public List<Field> field;
	}

	public interface IIndexIdxRecordIdGet extends ISwaggerMethod {
		public void run(IndexIdxRecordIdGetRequest req,
				IndexIdxRecordIdGetResponse resp) throws Exception;
	}

	public class IndexIdxRecordPostRequest implements ISwaggerRequest {
		public String idx;
	}

	public class IndexIdxRecordPostResponse implements ISwaggerResponse {
		public String id;
	}

	public interface IIndexIdxRecordPost extends ISwaggerMethod {
		public void run(IndexIdxRecordPostRequest req,
				IndexIdxRecordPostResponse resp) throws Exception;
	}

	public class IndexIdxRecordIdRefreshPostRequest implements ISwaggerRequest {
		public String idx;
		public String id;
		public Boolean sync;
	}

	public class IndexIdxRecordIdRefreshPostResponse implements
			ISwaggerResponse {
		public String id;
	}

	public interface IIndexIdxRecordIdRefreshPost extends ISwaggerMethod {
		public void run(IndexIdxRecordIdRefreshPostRequest req,
				IndexIdxRecordIdRefreshPostResponse resp) throws Exception;
	}

	public class TaskBuildStepGetRequest implements ISwaggerRequest {
	}

	public class TaskBuildStepGetResponse implements ISwaggerResponse {
		public String status;
	}

	public interface ITaskBuildStepGet extends ISwaggerMethod {
		public void run(TaskBuildStepGetRequest req,
				TaskBuildStepGetResponse resp) throws Exception;
	}

	public class TaskIdxBuildStepPostRequest implements ISwaggerRequest {
		public String idx;
	}

	public class TaskIdxBuildStepPostResponse implements ISwaggerResponse {
		public String status;
	}

	public interface ITaskIdxBuildStepPost extends ISwaggerMethod {
		public void run(TaskIdxBuildStepPostRequest req,
				TaskIdxBuildStepPostResponse resp) throws Exception;
	}

	public class TaskRefreshStepGetRequest implements ISwaggerRequest {
	}

	public class TaskRefreshStepGetResponse implements ISwaggerResponse {
		public String status;
	}

	public interface ITaskRefreshStepGet extends ISwaggerMethod {
		public void run(TaskRefreshStepGetRequest req,
				TaskRefreshStepGetResponse resp) throws Exception;
	}

	public class TaskIdxRefreshStepPostRequest implements ISwaggerRequest {
		public String idx;
	}

	public class TaskIdxRefreshStepPostResponse implements ISwaggerResponse {
		public String status;
	}

	public interface ITaskIdxRefreshStepPost extends ISwaggerMethod {
		public void run(TaskIdxRefreshStepPostRequest req,
				TaskIdxRefreshStepPostResponse resp) throws Exception;
	}

}