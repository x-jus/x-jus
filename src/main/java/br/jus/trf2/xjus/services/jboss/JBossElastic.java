package br.jus.trf2.xjus.services.jboss;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.RequestLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;

import com.crivano.swaggerservlet.SwaggerUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import br.jus.trf2.xjus.IXjus.Facet;
import br.jus.trf2.xjus.IXjus.FacetValue;
import br.jus.trf2.xjus.IXjus.IIndexIdxQueryGet;
import br.jus.trf2.xjus.IXjus.Record;
import br.jus.trf2.xjus.XjusServlet;
import br.jus.trf2.xjus.record.api.RecordIdGet;
import br.jus.trf2.xjus.services.ISearch;
import br.jus.trf2.xjus.util.Prop;

public class JBossElastic implements ISearch {

	public static ISearch INSTANCE;
//	private RestHighLevelClient client;
	private RestClient lowLevelClient;

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

		boolean fConnectException;
		do {
			fConnectException = false;
			try {
				for (String idx : Prop.getList("indexes")) {

					try {
						fetch("HEAD", idx);
						SwaggerUtils.log(this.getClass()).info("Conectado com o ElasticSearch.");
					} catch (NoSuchElementException ex) {
						// Create index
						JsonObject jo = (JsonObject) new JsonParser().parse(Prop.get("index." + idx + ".create.json"));
						JsonObject resp = fetch("PUT", idx, jo);
						String result = resp.get("result").getAsString();
						if (!"created".equals(result))
							throw new RuntimeException("Erro criando índice no ElastiSearch: " + idx);
						SwaggerUtils.log(this.getClass()).info("Criado índice " + idx + " no ElasticSearch.");
					}
				}
			} catch (Exception ex) {
				SwaggerUtils.log(this.getClass())
						.info("Não foi possível conectar com o ElasticSearch, tentando novamente em 5 segundos.", ex);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				fConnectException = true;
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
			lowLevelClient.close();
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
		builder.addProperty("dateref", r.dateref);

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

		builder.addProperty("date", SwaggerUtils.dateAdapter.format(new Date()));

		JsonObject resp = fetch("PUT", idx + "/_doc/" + r.id, builder);

		String result = resp.get("result").getAsString();
		if (!"created".equals(result) && !"updated".equals(result)) {
			throw new RuntimeException("Erro criando documento no ElastiSearch: " + r.id);
		}
	}

	@Override
	public void removeIndex(String idx) throws Exception {

	}

	@Override
	public void removeDocument(String idx, String id) throws Exception {
		try {
			fetch("DELETE", idx + "/_doc/" + id);
		} catch (NoSuchElementException ex) {
			// swallow delete exception when document is already deleted
		}
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
			IIndexIdxQueryGet.Response resp) throws Exception {

		String json = XjusServlet.getInstance().getProperty("index." + idx + ".query.json");

		json = json.replace("\"__QUERY_STRING__\"", new JsonPrimitive(filter).toString());

		// Facets
		//
		String sFacets = "";
		if (facets != null && !facets.trim().isEmpty()) {
			for (String f : facets.split(",")) {
				String[] a = f.split(":", 3);

				if (a.length == 3) {
//					addRangeFilter(a, boolQueryBuilder);
				} else if (a.length == 2) {
					JsonObject o = new JsonObject();
					JsonObject oterm = new JsonObject();
					o.add("term", oterm);
					JsonObject ofacet = new JsonObject();
					oterm.add(a[0], ofacet);
					ofacet.addProperty("value", a[1]);
					ofacet.addProperty("boost", 1.0);
					sFacets += "," + o.toString();
				}
			}
		}
		json = json.replace(",\"__FACETS__\"", sFacets);

		// ACL
		//
		if (acl == null)
			acl = "PUBLIC";
		String[] acls = acl.split(";");
		String sacls = "";
		JsonArray aacls = new JsonArray();
		for (String s : acls) {
			JsonObject o = new JsonObject();
			JsonObject oterm = new JsonObject();
			o.add("term", oterm);
			JsonObject oacl = new JsonObject();
			oterm.add("acl", oacl);
			oacl.addProperty("value", s);
			oacl.addProperty("boost", 1.0);
			aacls.add(o);
		}
		json = json.replace("\"__ACLS__\"", aacls.toString());

		// Max per Page and Offset
		int offset = 0;
		if (page != null && perpage != null) {
			offset = (page - 1) * perpage;
		}
		json = json.replace("\"__FIRST_RESULT__\"", "" + offset);
		json = json.replace("\"__MAX_RESULTS__\"", "" + perpage);

		JsonObject esresp = fetch("GET", "/" + idx + "/_search", json);

		resp.count = esresp.getAsJsonObject("hits").getAsJsonObject("total").get("value").getAsDouble();
		resp.results = new ArrayList<>();
		for (JsonElement hitElement : esresp.getAsJsonObject("hits").getAsJsonArray("hits")) {
			JsonObject hit = hitElement.getAsJsonObject();
			JsonObject src = hit.get("_source").getAsJsonObject();
			Record r = new Record();
			r.id = hit.get("_id").getAsString();
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

		JsonObject facetNames = esresp.getAsJsonObject("aggregations");
		resp.facets = new ArrayList<>();
		for (Entry<String, JsonElement> agg : facetNames.entrySet()) {
			Facet f = new Facet();
			String name = agg.getKey();
			JsonObject v = agg.getValue().getAsJsonObject();
			f.name = name;
			if (v.get("meta") != null && v.get("meta").getAsJsonObject().get("title") != null)
				f.name = v.get("meta").getAsJsonObject().get("title").getAsString();
			f.values = new ArrayList<>();

			for (JsonElement bucket : v.get("buckets").getAsJsonArray()) {
				JsonObject b = bucket.getAsJsonObject();
				FacetValue fv = new FacetValue();
				fv.name = b.get("key").getAsString();
				fv.count = b.get("doc_count").getAsDouble();
				String field = name;
				if (v.get("meta") != null && v.get("meta").getAsJsonObject().get("field") != null)
					field = v.get("meta").getAsJsonObject().get("field").getAsString();
				fv.refinementToken = field + ":" + fv.name;
				f.values.add(fv);
			}
			resp.facets.add(f);
		}
	}

//	private void addRangeFilter(String[] a, BoolQueryBuilder boolQueryBuilder) {
//		if (!a[1].trim().isEmpty()) {
//			BoolQueryBuilder rangeStartQuery = new BoolQueryBuilder().should(QueryBuilders.rangeQuery(a[0]).gte(a[1]));
//
//			boolQueryBuilder.filter(rangeStartQuery);
//		}
//
//		if (!a[2].trim().isEmpty()) {
//			BoolQueryBuilder rangeEndQuery = new BoolQueryBuilder().should(QueryBuilders.rangeQuery(a[0]).lte(a[2]));
//
//			boolQueryBuilder.filter(rangeEndQuery);
//		}
//	}

	private String value(JsonObject hit, String field) {
		String f = (String) hit.get("_source").getAsJsonObject().get(field).getAsString();
		if (f == null)
			return null;
		return f;
	}

	private String array(JsonObject hit, String field) {
		StringBuilder sb = new StringBuilder();
		JsonArray f = hit.get("_source").getAsJsonObject().get(field).getAsJsonArray();
		if (f == null)
			return null;
		for (JsonElement o : f) {
			if (sb.length() > 0)
				sb.append(";");
			sb.append(o.getAsString());
		}
		return sb.toString();
	}

	private String highlights(JsonObject hit, String field) {
		if (hit.get("highlight") == null || hit.get("highlight").getAsJsonObject().get("content") == null)
			return null;
		JsonArray fragments = hit.get("highlight").getAsJsonObject().get("content").getAsJsonArray();
		if (fragments == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (JsonElement o : fragments) {
			if (sb.length() > 0)
				sb.append(" ... ");
			sb.append(o.getAsString());
		}
		return sb.toString();
	}

	@Override
	public List<String> getDocumentIds(String idx, String startId, int count) throws Exception {

		String json = XjusServlet.getInstance().getProperty("index." + idx + ".list.ids.json");

		json = json.replace("\"__MAX_RESULTS__\"", "" + count);
		json = json.replace("__START_ID__", startId != null ? startId : "");

		JsonObject esresp = fetch("GET", "/" + idx + "/_search", json);

		List<String> docIds = new ArrayList<>();
		int i = 0;
		for (JsonElement hitElement : esresp.getAsJsonObject("hits").getAsJsonArray("hits")) {
			JsonObject hit = hitElement.getAsJsonObject();
			Record r = new Record();
			docIds.add(hit.get("_id").getAsString());
			i++;
			if (count <= i)
				break;
		}
		return docIds;
	}

	private JsonObject fetch(String method, String path) {
		return fetch(method, path, (JsonObject) null);
	}

	private JsonObject fetch(String method, String path, String body) {
		JsonObject jsonObject = new JsonParser().parse(body).getAsJsonObject();
		return fetch(method, path, jsonObject);
	}

	private JsonObject fetch(String method, String path, JsonObject body) {
		Request request = new Request(method, path);
		if (body != null)
			request.setJsonEntity(body.toString());
		Response response;
		try {
			response = lowLevelClient.performRequest(request);
		} catch (ResponseException e) {
			if (e.getResponse().getStatusLine().getStatusCode() == 404)
				throw new NoSuchElementException();
			throw new RuntimeException("Erro reportado pelo Elasticsearch", e);
		} catch (IOException e) {
			throw new RuntimeException("Erro reportado pelo Elasticsearch", e);
		}
		RequestLine requestLine = response.getRequestLine();
		HttpHost host = response.getHost();
		int statusCode = response.getStatusLine().getStatusCode();

		if ("HEAD".equals(method) && statusCode == 404)
			throw new NoSuchElementException();

		Header[] headers = response.getHeaders();
		String responseBody;
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				responseBody = EntityUtils.toString(entity);
				return new JsonParser().parse(responseBody).getAsJsonObject();
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException("Erro convertendo resultado do Elasticsearch", e);
		}
	}

}