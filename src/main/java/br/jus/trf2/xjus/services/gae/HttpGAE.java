package br.jus.trf2.xjus.services.gae;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.crivano.swaggerservlet.IHTTP;
import com.crivano.swaggerservlet.ISwaggerRequest;
import com.crivano.swaggerservlet.ISwaggerResponse;
import com.crivano.swaggerservlet.SwaggerError;
import com.crivano.swaggerservlet.SwaggerException;
import com.crivano.swaggerservlet.SwaggerUtils;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class HttpGAE implements IHTTP {
	public static String convertStreamToString(java.io.InputStream is) {
		@SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is, "UTF-8")
				.useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static byte[] convertStreamToByteArray(java.io.InputStream is)
			throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		return buffer.toByteArray();
	}

	@Override
	public <T extends ISwaggerResponse> T fetch(String authorization,
			String url, String method, ISwaggerRequest req, Class<T> clazzResp)
			throws Exception {
		String contentType = null;
		byte[] payload = null;
		URL u = new URL(url);

		if (req != null && ("POST".equals(method) || "PUT".equals(method))) {
			String body = SwaggerUtils.toJson(req);
			payload = body.getBytes(StandardCharsets.UTF_8);
			contentType = "application/json";
		}
		Future<HTTPResponse> fut = fetchAsync(authorization, u,
				HTTPMethod.valueOf(method), payload, contentType);
		HTTPResponse r = fut.get(50, TimeUnit.SECONDS);

		int responseCode = r.getResponseCode();

		if (responseCode >= 400 && responseCode < 600) {
			String body = new String(r.getContent(), StandardCharsets.UTF_8);
			SwaggerError err = (SwaggerError) SwaggerUtils.fromJson(body,
					SwaggerError.class);
			String errormsg = "HTTP ERROR: " + Integer.toString(responseCode);
			if (err != null && err.errormsg != null)
				errormsg = err.errormsg;
			throw new SwaggerException(errormsg, responseCode, null, req, err, null);
		}

		String respString = new String(r.getContent(), StandardCharsets.UTF_8);
		T resp = (T) SwaggerUtils.fromJson(respString, clazzResp);
		return resp;
	}

	public static Future<HTTPResponse> fetchAsync(String authorization,
			URL url, HTTPMethod method, byte[] payload, String contentType)
			throws MalformedURLException, IOException,
			UnsupportedEncodingException {
		URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
		com.google.appengine.api.urlfetch.FetchOptions lFetchOptions = com.google.appengine.api.urlfetch.FetchOptions.Builder
				.doNotValidateCertificate().setDeadline(60.0);
		HTTPRequest request = new HTTPRequest(url, method, lFetchOptions);
		request.setHeader(new HTTPHeader("User-Agent", "SwaggerServletGAE"));
		if (authorization != null)
			request.setHeader(new HTTPHeader("Authorization", authorization));
		if (contentType != null)
			request.setHeader(new HTTPHeader("Content-Type", contentType));
		if (payload != null)
			request.setPayload(payload);
		Future<HTTPResponse> response = fetcher.fetchAsync(request);

		return response;
	}

}
