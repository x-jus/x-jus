package br.jus.trf2.xjus.services.jboss;

import java.io.InputStream;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.xml.soap.Text;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;

import com.auth0.jwt.internal.com.fasterxml.jackson.databind.ObjectMapper;
import com.auth0.jwt.internal.org.bouncycastle.util.Strings;
import com.crivano.swaggerservlet.SwaggerUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import br.jus.trf2.xjus.IXjus.Facet;
import br.jus.trf2.xjus.IXjus.FacetValue;
import br.jus.trf2.xjus.IXjus.IndexIdxQueryGetResponse;
import br.jus.trf2.xjus.IXjus.Record;
import br.jus.trf2.xjus.XjusServlet;
import br.jus.trf2.xjus.record.api.RecordIdGet;
import br.jus.trf2.xjus.services.ISearch;
import br.jus.trf2.xjus.util.Prop;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.transform.Settings;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

public class JBossElastic implements ISearch {

	public static ISearch INSTANCE;
//	private RestHighLevelClient client;
	private RestClient lowLevelClient;
	private ElasticsearchClient esc;
	private static final ObjectMapper mapper = new ObjectMapper();

	public void initialize() throws URISyntaxException {
		INSTANCE = this;

		URI uri = new URI(Prop.get("elasticsearch.url"));

		// Create the low-level client
		RestClientBuilder builderH = RestClient.builder(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()));

		if (Prop.get("elasticsearch.auth.basic.user") != null) {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
					Prop.get("elasticsearch.auth.basic.user"), Prop.get("elasticsearch.auth.basic.password")));

			builderH.setHttpClientConfigCallback(new HttpClientConfigCallback() {
				@Override
				public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
					if (Prop.getBool("elasticsearch.ssl.skip.hostname.verification")) {
						try {
							SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(null,
									(x509Certificates, s) -> true);
							final SSLContext sslContext = sslBuilder.build();
							// httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
							httpClientBuilder.setSSLHostnameVerifier((s, sslSession) -> true);
							httpClientBuilder.setSSLContext(sslContext);
						} catch (Exception ex) {
							throw new RuntimeException("Não foi possível desabilitar a verificação de SSL", ex);
						}
					}
					return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				}
			});
		}

		lowLevelClient = builderH.build();

		// Create the transport with a Jackson mapper
		ElasticsearchTransport transport = new RestClientTransport(lowLevelClient, new JacksonJsonpMapper());

		// And create the API client
		ElasticsearchClient esc = new ElasticsearchClient(transport);

		boolean fConnectException;
		do {
			fConnectException = false;
			try {
				for (String idx : Prop.getList("indexes")) {

					boolean exists = esc.indices().exists(ExistsRequest.of(e -> e.index(idx))).value();
					SwaggerUtils.log(this.getClass())
							.info("Conexão com o ElasticSearch em " + Prop.get("elasticsearch.url") + " OK.");
					if (!exists) {
						// Create index
						InputStream json = this.getClass().getResourceAsStream("create-index-request.json");
						CreateIndexRequest req = CreateIndexRequest.of(b -> b.index(idx).withJson(json));
						boolean created = esc.indices().create(req).acknowledged();
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

	@PreDestroy
	public void stop() {
		try {
			esc._transport().close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void addDocument(String idx, RecordIdGet.Response r) throws Exception {
		JsonObject builder = new JsonObject();
		builder.addProperty("id", r.id);
		if (r.content != null)
			builder.addProperty("content", r.content);
		builder.addProperty("code", r.code);
		builder.addProperty("url", r.url);

		if (r.title != null)
			builder.addProperty("title", r.title);

		if (r.acl != null) {
			String[] split = r.acl.split(";");
			JsonArray acl = new JsonArray();
			for (String s : split)
				acl.add(s);
			builder.add("acl", acl);
		}

		if (r.field != null) {
			for (br.jus.trf2.xjus.record.api.IXjusRecordAPI.Field f : r.field) {
				builder.addProperty("field_" + f.name, f.value);
			}
		}

		if (r.facet != null) {
			for (br.jus.trf2.xjus.record.api.IXjusRecordAPI.Facet f : r.facet) {
				if ("FLOAT".equals(f.kind))
					builder.addProperty("facet_" + f.name, Double.valueOf(f.value));
				else
					builder.addProperty("facet_" + f.name, f.value);
			}
		}

		builder.addProperty("date", SwaggerUtils.format(new Date()));

		String json = builder.getAsString();
		System.out.println(json);

		IndexRequest request = IndexRequest.of(i -> i.index(idx).id(r.id).withJson(new StringReader(json)));
		IndexResponse response = esc.index(request);

//		IndexRequest request = new IndexRequest(idx).id(r.id).source(builder);
//		IndexResponse response = client.index(request, RequestOptions.DEFAULT);
	}

	@Override
	public void removeIndex(String idx) throws Exception {

	}

	@Override
	public void removeDocument(String idx, String id) throws Exception {
		DeleteRequest request = DeleteRequest.of(i -> i.index(idx).id(id));
		DeleteResponse deleteResponse = esc.delete(request);
	}

	@Override
	public Long count(String idx) throws Exception {
		Request request = new Request("GET", idx + "/_count");
		Response response = lowLevelClient.performRequest(request);
		String json = EntityUtils.toString(response.getEntity());
		JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
		return jsonObject.get("count").getAsLong();
	}

	@Override
	public void query(String idx, String filter, String facets, Integer page, Integer perpage, String acl,
			IndexIdxQueryGetResponse resp) throws Exception {
	

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
				String[] a = f.split(":", 3);

				if (a.length == 3)
					addRangeFilter(a, boolQueryBuilder);
				else if (a.length == 2)
					boolQueryBuilder.must(new TermQueryBuilder(a[0], a[1]));
			}
		}
		
		JsonArray acl = new JsonArray();
		

		{
			"term": {
				"acl": {
					"value": "PUBLIC",
					"boost": 1.0
				}
			}
		}


		if (acl == null)
			acl = "PUBLIC";
		String[] acls = acl.split(";");
		for (String s : acls) {
			boolQueryBuilder.should(new TermQueryBuilder("acl", s));
		}
		boolQueryBuilder.minimumShouldMatch(1);
		searchSourceBuilder.query(boolQueryBuilder);

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

		String json = Strings.toString(searchSourceBuilder);
		System.out.println(json);

		SearchRequest searchRequest = SearchRequest.of(i -> i.index(idx).withJson(new StringReader(json)));

//
//		System.err.println(b.string());
//
//		searchSourceBuilder.toXContent(builder, params);

//		if (facets != null) {
//			String[] a = facets.split(",");
//			for (String f : a)
//				queryBuilder.addFacetRefinementFromToken(f);
//		}

		SearchResponse searchResponse = esc.search(searchRequest, null);

		resp.count = (double) searchResponse.hits().total().value();
		resp.results = new ArrayList<>();
		for (co.elastic.clients.elasticsearch.core.search.Hit hit : (List<Hit>) searchResponse.hits().hits()) {
			Record r = new Record();
			r.id = hit.id();
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
			Aggregate g = searchResponse.aggregations().get(agg);

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

	private String value(Hit hit, String field) {
		String f = (String) hit.fields().get(field);
		if (f == null)
			return null;
		return f;
	}

	private String array(Hit hit, String field) {
		StringBuilder sb = new StringBuilder();
		List<String> f = (List<String>) hit.fields().get(field);
		if (f == null)
			return null;
		for (String o : f) {
			if (sb.length() > 0)
				sb.append(";");
			sb.append(o.toString());
		}
		return sb.toString();
	}

	private String highlights(Hit hit, String field) {
		if (hit == null || hit.highlight() == null || hit.highlight().get(field) == null)
			return null;
		Text[] fragments = hit.highlight().get(field).fragments();
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