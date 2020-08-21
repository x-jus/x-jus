package br.jus.trf2.xjus.services.jboss;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.crivano.swaggerservlet.SwaggerUtils;

import br.jus.trf2.xjus.IXjus.IndexIdxQueryGetResponse;
import br.jus.trf2.xjus.IXjus.Record;
import br.jus.trf2.xjus.record.api.IXjusRecordAPI.RecordIdGetResponse;
import br.jus.trf2.xjus.services.ISearch;

@Singleton
@Startup
public class JBossElastic implements ISearch {

	public static ISearch INSTANCE;
	private RestHighLevelClient client;

	@PostConstruct
	public void initialize() {
		INSTANCE = this;
		RestHighLevelClient cli = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")));

		try {
			// Delete index
			try {
				DeleteIndexRequest request = new DeleteIndexRequest("test");
				AcknowledgedResponse deleteIndexResponse = cli.indices().delete(request, RequestOptions.DEFAULT);
			} catch (Exception ex) {
				SwaggerUtils.log(this.getClass()).warn("Não consegui remover o índice", ex);
			}

			// Create index
			{
				CreateIndexRequest request = new CreateIndexRequest("test");
				XContentBuilder builder = XContentFactory.jsonBuilder();
				builder.startObject();
				{
					builder.startObject("properties");
					{
						builder.startObject("id");
						{
							builder.field("type", "keyword");
						}
						builder.endObject();
						builder.startObject("acl");
						{
							builder.field("type", "keyword");
						}
						builder.endObject();
						builder.startObject("code");
						{
							builder.field("type", "keyword");
						}
						builder.endObject();
						builder.startObject("title");
						{
							builder.field("type", "text");
						}
						builder.endObject();
						builder.startObject("content");
						{
							builder.field("type", "text");
						}
						builder.endObject();
						builder.startObject("date");
						{
							builder.field("type", "date");
						}
						builder.endObject();
						builder.startObject("url");
						{
							builder.field("type", "keyword");
						}
						builder.endObject();
						builder.startObject("facet_*");
						{
							builder.field("type", "keyword");
						}
						builder.endObject();
					}
					builder.endObject();
				}
				builder.endObject();
				request.mapping("record", builder);
				CreateIndexResponse createIndexResponse = cli.indices().create(request, RequestOptions.DEFAULT);
			}

			try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
				PutMappingRequest request = new PutMappingRequest("test");
				final XContentBuilder builder = jsonBuilder.startObject().startObject("record")
						.startObject("properties").startObject("id").field("type", "keyword").field("fielddata", true)
						.endObject().endObject().endObject().endObject();
				AcknowledgedResponse putMappingResponse = cli.indices().putMapping(request, RequestOptions.DEFAULT);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		client = cli;
	}

	@PreDestroy
	public void stop() {
		try {
			client.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addDocument(String idx, RecordIdGetResponse r) throws Exception {
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		{
			builder.field("id", r.id);
			if (r.content != null)
				builder.field("content", r.content);
			builder.field("code", r.code);
			builder.field("url", r.url);

			if (r.title != null)
				builder.field("title", r.title);

			if (r.acl != null) {
				String[] split = r.acl.split(";");
				builder.array("acl", split);
			}

			if (r.field != null) {
				for (br.jus.trf2.xjus.record.api.IXjusRecordAPI.Field f : r.field) {
					builder.field("field_" + f.name, f.value);
				}
			}

			if (r.facet != null) {
				for (br.jus.trf2.xjus.record.api.IXjusRecordAPI.Facet f : r.facet) {
					if ("FLOAT".equals(f.kind))
						builder.field("facet_" + f.name, Double.valueOf(f.value));
					else
						builder.field("facet_" + f.name, f.value);
				}
			}

			builder.timeField("date", new Date());
		}
		builder.endObject();

		IndexRequest request = new IndexRequest(idx).id(r.id).source(builder);
		IndexResponse response = client.index(request, RequestOptions.DEFAULT);
	}

	@Override
	public void removeIndex(String idx) throws Exception {

	}

	@Override
	public void removeDocument(String idx, String id) throws Exception {
		DeleteRequest request = new DeleteRequest(idx, id);
		DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
	}

	@Override
	public void query(String idx, String filter, String facets, Integer page, Integer perpage, String acl,
			IndexIdxQueryGetResponse resp) throws Exception {
		SearchRequest searchRequest = new SearchRequest(idx);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("user", "kimchy");
//		searchSourceBuilder.query(matchQueryBuilder);

		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		boolQueryBuilder.must(new QueryStringQueryBuilder(filter));

		if (acl == null)
			acl = "PUBLIC";
		String[] acls = acl.split(";");
		for (String s : acls) {
			boolQueryBuilder.should(new TermQueryBuilder("acl", s));
		}
		boolQueryBuilder.minimumShouldMatch(1);
		searchSourceBuilder.query(boolQueryBuilder);

		searchRequest.source(searchSourceBuilder);

		int offset = 0;
		if (page != null && perpage != null) {
			offset = (page - 1) * perpage;
		}

		String[] includeFields = new String[] { "acl", "url", "code", "title" };
		String[] excludeFields = new String[] {};
		searchSourceBuilder.fetchSource(includeFields, excludeFields);

		HighlightBuilder highlightBuilder = new HighlightBuilder();
		HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("content");
		highlightTitle.highlighterType("unified");
		highlightBuilder.field(highlightTitle);
		searchSourceBuilder.highlighter(highlightBuilder);

		searchSourceBuilder.from(offset);
		searchSourceBuilder.size(perpage);

//		if (facets != null) {
//			String[] a = facets.split(",");
//			for (String f : a)
//				queryBuilder.addFacetRefinementFromToken(f);
//		}

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		resp.results = new ArrayList<>();
		for (SearchHit hit : searchResponse.getHits()) {
			Record r = new Record();
			r.id = hit.getId();
			// r.object;
			r.acl = array(hit, "acl");
//			r.refresh;
			r.url = value(hit, "url");
			r.code = value(hit, "code");
			r.title = value(hit, "title");
			r.content = highlights(hit, "content");
			// r.field;
			resp.results.add(r);
		}
	}

	private String value(SearchHit hit, String field) {
		String f = (String) hit.getSourceAsMap().get(field);
		if (f == null)
			return null;
		return f;
	}

	private String array(SearchHit hit, String field) {
		StringBuilder sb = new StringBuilder();
		List<String> f = (List<String>) hit.getSourceAsMap().get(field);
		if (f == null)
			return null;
		for (String o : f) {
			if (sb.length() > 0)
				sb.append(";");
			sb.append(o.toString());
		}
		return sb.toString();
	}

	private String highlights(SearchHit hit, String field) {
		Text[] fragments = hit.getHighlightFields().get(field).fragments();
		if (fragments == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (Text o : fragments) {
			if (sb.length() > 0)
				sb.append(" ... ");
			sb.append(o.string());
		}
		return sb.toString();
	}

	@Override
	public List<String> getDocumentIds(String idx, String startId, int count) throws Exception {

		SearchRequest searchRequest = new SearchRequest(idx);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.rangeQuery("id").from(startId).includeLower(false));
//		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("user", "kimchy");
//		searchSourceBuilder.query(matchQueryBuilder);

		String[] includeFields = new String[] {};
		String[] excludeFields = new String[] {};
		searchSourceBuilder.fetchSource(includeFields, excludeFields);

		searchSourceBuilder.sort("id", SortOrder.ASC);
		searchSourceBuilder.size(count);
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		List<String> docIds = new ArrayList<>();
		String id = startId;
		int i = 0;

		for (SearchHit hit : searchResponse.getHits()) {
			id = hit.getId();
			docIds.add(id);
			i++;
			if (count <= i)
				break;
		}
		return docIds;
	}

}