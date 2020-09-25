package br.jus.trf2.xjus;

import java.util.Date;
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

	public class LastDate implements ISwaggerModel {
	}

	public class LastId implements ISwaggerModel {
	}

	public class Timestamp implements ISwaggerModel {
	}

	public class Status implements ISwaggerModel {
	}

	public class Count implements ISwaggerModel {
	}

	public class Complete implements ISwaggerModel {
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
		public String buildRecords;
		public String buildLastCount;
		public Date buildLastDate;
		public String buildLastId;
		public String refreshLastId;
		public Date refreshTimestamp;
		public Boolean refreshComplete;
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

	public class User implements ISwaggerModel {
		public String gmail;
		public Boolean admin;
		public String loginUrl;
		public String logoutUrl;
	}

	public class Error implements ISwaggerModel {
		public String errormsg;
	}

	public class UserGetRequest implements ISwaggerRequest {
	}

	public class UserGetResponse implements ISwaggerResponse {
		public User user;
	}

	public interface IUserGet extends ISwaggerMethod {
		public void run(UserGetRequest req, UserGetResponse resp) throws Exception;
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
		public void run(IndexIdxQueryGetRequest req, IndexIdxQueryGetResponse resp) throws Exception;
	}

	public class IndexIdxStatusGetRequest implements ISwaggerRequest {
		public String idx;
	}

	public class IndexIdxStatusGetResponse implements ISwaggerResponse {
		public Double count;
		public Double buildCount;
		public String buildLastId;
		public Date buildLastDate;
		public String refreshLastId;
	}

	public interface IIndexIdxStatusGet extends ISwaggerMethod {
		public void run(IndexIdxStatusGetRequest req, IndexIdxStatusGetResponse resp) throws Exception;
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
		public void run(IndexIdxRecordIdGetRequest req, IndexIdxRecordIdGetResponse resp) throws Exception;
	}

	public class IndexIdxRecordPostRequest implements ISwaggerRequest {
		public String idx;
	}

	public class IndexIdxRecordPostResponse implements ISwaggerResponse {
		public String id;
	}

	public interface IIndexIdxRecordPost extends ISwaggerMethod {
		public void run(IndexIdxRecordPostRequest req, IndexIdxRecordPostResponse resp) throws Exception;
	}

	public class TaskBuildStepGetRequest implements ISwaggerRequest {
	}

	public class TaskBuildStepGetResponse implements ISwaggerResponse {
		public String status;
	}

	public interface ITaskBuildStepGet extends ISwaggerMethod {
		public void run(TaskBuildStepGetRequest req, TaskBuildStepGetResponse resp) throws Exception;
	}

	public class TaskIdxBuildStepPostRequest implements ISwaggerRequest {
		public String idx;
	}

	public class TaskIdxBuildStepPostResponse implements ISwaggerResponse {
		public String status;
	}

	public interface ITaskIdxBuildStepPost extends ISwaggerMethod {
		public void run(TaskIdxBuildStepPostRequest req, TaskIdxBuildStepPostResponse resp) throws Exception;
	}

	public class TaskRefreshStepGetRequest implements ISwaggerRequest {
	}

	public class TaskRefreshStepGetResponse implements ISwaggerResponse {
		public String status;
	}

	public interface ITaskRefreshStepGet extends ISwaggerMethod {
		public void run(TaskRefreshStepGetRequest req, TaskRefreshStepGetResponse resp) throws Exception;
	}

	public class TaskIdxRefreshStepPostRequest implements ISwaggerRequest {
		public String idx;
	}

	public class TaskIdxRefreshStepPostResponse implements ISwaggerResponse {
		public String status;
	}

	public interface ITaskIdxRefreshStepPost extends ISwaggerMethod {
		public void run(TaskIdxRefreshStepPostRequest req, TaskIdxRefreshStepPostResponse resp) throws Exception;
	}

	public class TaskIdxRecordIdRefreshPostRequest implements ISwaggerRequest {
		public String idx;
		public String id;
		public Boolean sync;
	}

	public class TaskIdxRecordIdRefreshPostResponse implements ISwaggerResponse {
		public String id;
	}

	public interface ITaskIdxRecordIdRefreshPost extends ISwaggerMethod {
		public void run(TaskIdxRecordIdRefreshPostRequest req, TaskIdxRecordIdRefreshPostResponse resp)
				throws Exception;
	}

}