package br.jus.trf2.xjus.services.jboss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.BytesStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchModule;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.auth0.jwt.internal.com.fasterxml.jackson.databind.ObjectMapper;
import com.crivano.swaggerservlet.SwaggerUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import br.jus.trf2.xjus.IXjus.Facet;
import br.jus.trf2.xjus.IXjus.FacetValue;
import br.jus.trf2.xjus.IXjus.Record;
import br.jus.trf2.xjus.IndexIdxQueryGet;
import br.jus.trf2.xjus.Utils;
import br.jus.trf2.xjus.XjusServlet;
import br.jus.trf2.xjus.record.api.RecordIdGet;
import br.jus.trf2.xjus.services.ISearch;
import br.jus.trf2.xjus.util.Prop;

public class JBossElastic implements ISearch {

	public static ISearch INSTANCE;
	private RestHighLevelClient client;
	private static final ObjectMapper mapper = new ObjectMapper();

	public void initialize() throws URISyntaxException {
		INSTANCE = this;

		URI uri = new URI(Prop.get("elasticsearch.url"));
		RestClientBuilder builder = RestClient.builder(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()));

		if (Prop.get("elasticsearch.auth.basic.user") != null) {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
					Prop.get("elasticsearch.auth.basic.user"), Prop.get("elasticsearch.auth.basic.password")));

			builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {
				@Override
				public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
					return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				}
			});
		}

		RestHighLevelClient cli = new RestHighLevelClient(builder);

		// Create the Java API Client with the same low level client
//		ElasticsearchTransport transport = new RestClientTransport(builder.build(), new JacksonJsonpMapper());
//		ElasticsearchClient esClient = new ElasticsearchClient(transport);

		boolean fConnectException;
		do {
			fConnectException = false;
			try {
				for (String idx : Prop.getList("indexes")) {

					GetIndexRequest requestExists = new GetIndexRequest(idx);
					boolean exists = cli.indices().exists(requestExists, RequestOptions.DEFAULT);
					SwaggerUtils.log(this.getClass())
							.info("Conexão com o ElasticSearch em " + Prop.get("elasticsearch.url") + " OK.");
					if (!exists) {
						// Create index
						{
							CreateIndexRequest request = new CreateIndexRequest(idx);
							String json = Utils.convertStreamToString(
									this.getClass().getResourceAsStream("create-index-request.json"));
							request.source(json, XContentType.JSON);
							CreateIndexResponse createIndexResponse = cli.indices().create(request,
									RequestOptions.DEFAULT);
						}

//						try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
//							PutMappingRequest request = new PutMappingRequest(idx);
//							String json = Utils.convertStreamToString(
//									this.getClass().getResourceAsStream("create-index-request.json"));
//							request.source(json, XContentType.JSON);
//							org.elasticsearch.action.support.master.AcknowledgedResponse putMappingResponse = cli
//									.indices().putMapping(request, RequestOptions.DEFAULT);
//						}

						// Create index

						// Nato: Desativado porque parece que vai precisar de uma versão do Elastic
						// superior a 7.17
						// por enquanto a criação do índice tem que ser feita na mão, infelizmente.
//						InputStream json = this.getClass().getResourceAsStream("create-index-request.json");
//						CreateIndexRequest req = CreateIndexRequest.of(b -> b.index(idx).withJson(json));
//						boolean created = esClient.indices().create(req).acknowledged();

					} else {
						// Delete index
//					try {
//						DeleteIndexRequest request = new DeleteIndexRequest(idx);
//						AcknowledgedResponse deleteIndexResponse = cli.indices().delete(request, RequestOptions.DEFAULT);
//					} catch (Exception ex) {
//						SwaggerUtils.log(this.getClass()).warn("Não consegui remover o índice", ex);
//					}
					}

				}
			} catch (ConnectException ex) {
				SwaggerUtils.log(this.getClass()).info("Não foi possível conectar com o ElasticSearch em "
						+ Prop.get("elasticsearch.url") + ", tentando novamente em 5 segundos.");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				fConnectException = true;
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} while (fConnectException);

		client = cli;
	}

	private void waitForXjusServletToLoad() {
		boolean ok = false;
		do {
			try {
				Prop.getList("indexes");
				ok = true;
			} catch (NullPointerException ex1) {
				try {
					Thread.sleep(1000);
				} catch (Exception ex2) {
				}
			}
		} while (!ok);
	}

	private static class BAOS extends BytesStream {

		private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();

		@Override
		public void writeByte(byte b) throws IOException {
			delegate.write(b);
		}

		@Override
		public void writeBytes(byte[] b, int offset, int length) throws IOException {
			delegate.write(b, offset, length);
		}

		@Override
		public void flush() throws IOException {
			delegate.flush();
		}

		@Override
		public void close() throws IOException {
			flush();
		}

		@Override
		public void reset() throws IOException {
			delegate.reset();
		}

		@Override
		public BytesReference bytes() {
			return null;
		}

		@Override
		public String toString() {
			return new String(delegate.toByteArray());
		}
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
	public void addDocument(String idx, RecordIdGet.Response r) throws Exception {
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
	public Long count(String idx) throws Exception {
		RestClient lowLevelClient = client.getLowLevelClient();
		Request request = new Request("GET", idx + "/_count");
		Response response = lowLevelClient.performRequest(request);
		String json = EntityUtils.toString(response.getEntity());
		JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
		return jsonObject.get("count").getAsLong();

	}

	@Override
	public void query(String idx, String filter, String facets, Integer page, Integer perpage, String acl,
			IndexIdxQueryGet.Response resp) throws Exception {
		this.query(idx, filter, facets, page, perpage, acl, null, null, null, resp);
	}

	@Override
	public void query(String idx, String filter, String facets, Integer page, Integer perpage, String acl, String code,
			String fromDate, String toDate, IndexIdxQueryGet.Response resp) throws Exception {
		SearchRequest searchRequest = new SearchRequest(idx);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		SearchModule searchModule = new SearchModule(Settings.EMPTY, false, Collections.emptyList());
		try (XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
				new NamedXContentRegistry(searchModule.getNamedXContents()), null,
				XjusServlet.getInstance().getProperty("index." + idx + ".query.json"))) {
			searchSourceBuilder.parseXContent(parser);
		}

		// SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//		MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("user", "kimchy");
//		searchSourceBuilder.query(matchQueryBuilder);

		// This is necessary to output facets in the same order that they are defined in
		// the .query.json property
		List<String> facetNames = new ArrayList<>();
		try (StringReader sr = new StringReader(searchSourceBuilder.aggregations().toString());
				JsonReader reader = new JsonReader(sr)) {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				facetNames.add(name);
				reader.skipValue();
			}
			reader.endObject();
		}

		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		boolQueryBuilder.must(new QueryStringQueryBuilder(filter));

		if (facets != null && !facets.trim().isEmpty()) {
			for (String f : facets.split(",")) {
				String[] a = f.split(":", 2);

				boolQueryBuilder.must(new TermQueryBuilder(a[0], a[1]));
			}
		}

		if (code != null && !code.trim().isEmpty()) {
			QueryStringQueryBuilder queryStringQueryBuilder = new QueryStringQueryBuilder("*" + code);
			queryStringQueryBuilder.defaultField("code");
			boolQueryBuilder.filter(queryStringQueryBuilder);
		}

		if (fromDate != null && !fromDate.trim().isEmpty()) {
			BoolQueryBuilder rangeStartQuery = new BoolQueryBuilder()
					.should(QueryBuilders.rangeQuery("date").from(fromDate));
			boolQueryBuilder.filter(rangeStartQuery);
		}

		if (toDate != null && !toDate.trim().isEmpty()) {
			BoolQueryBuilder rangeEndQuery = new BoolQueryBuilder().should(QueryBuilders.rangeQuery("date").to(toDate));
			boolQueryBuilder.filter(rangeEndQuery);
		}

		if (acl == null)
			acl = "PUBLIC";
		/*
		 * Caso propriedade verify.acls esteja com false, a verificação das ACLs será
		 * ignorada na pesquisa
		 */
		String[] acls = Prop.getBool("verify.acls") ? acl.split(";") : new String[] {};
		for (String s : acls) {
			boolQueryBuilder.should(new TermQueryBuilder("acl", s));
		}
		boolQueryBuilder.minimumShouldMatch(Prop.getBool("verify.acls") ? 1 : 0);
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

//
//		System.err.println(b.string());
//
//		searchSourceBuilder.toXContent(builder, params);

//		if (facets != null) {
//			String[] a = facets.split(",");
//			for (String f : a)
//				queryBuilder.addFacetRefinementFromToken(f);
//		}

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

		resp.count = (double) searchResponse.getHits().getTotalHits().value;
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

		resp.facets = new ArrayList<>();
		for (String agg : facetNames) {
			Facet f = new Facet();
			Terms g = searchResponse.getAggregations().get(agg);

			String name = g.getName();
			f.name = name;
			if (g.getMetadata() != null && g.getMetadata().get("title") != null)
				f.name = g.getMetadata().get("title").toString();
			f.values = new ArrayList<>();

			for (Bucket bucket : g.getBuckets()) {
				FacetValue fv = new FacetValue();
				fv.name = bucket.getKeyAsString();
				fv.count = (double) bucket.getDocCount();
				String field = name;
				if (g.getMetadata() != null && g.getMetadata().get("field") != null)
					field = g.getMetadata().get("field").toString();
				fv.refinementToken = field + ":" + bucket.getKeyAsString();
				f.values.add(fv);
			}
			resp.facets.add(f);
		}
	}

	private void addRangeFilter(String[] a, BoolQueryBuilder boolQueryBuilder) {
		if (!a[1].trim().isEmpty()) {
			BoolQueryBuilder rangeStartQuery = new BoolQueryBuilder().should(QueryBuilders.rangeQuery(a[0]).gte(a[1]));

			boolQueryBuilder.filter(rangeStartQuery);
		}

		if (!a[2].trim().isEmpty()) {
			BoolQueryBuilder rangeEndQuery = new BoolQueryBuilder().should(QueryBuilders.rangeQuery(a[0]).lte(a[2]));

			boolQueryBuilder.filter(rangeEndQuery);
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
		if (hit == null || hit.getHighlightFields() == null || hit.getHighlightFields().get(field) == null)
			return null;
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