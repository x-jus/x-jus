package br.jus.trf2.xjus.services.jboss;

import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

public final class UnsafeX509ExtendedTrustManager extends X509ExtendedTrustManager {

	public static final X509ExtendedTrustManager INSTANCE = new UnsafeX509ExtendedTrustManager();
	private static final X509Certificate[] EMPTY_CERTIFICATES = new X509Certificate[0];

	private UnsafeX509ExtendedTrustManager() {
	}

	public static X509ExtendedTrustManager getInstance() {
		return INSTANCE;
	}

	@Override
	public void checkClientTrusted(X509Certificate[] certificates, String authType) {

	}

	@Override
	public void checkClientTrusted(X509Certificate[] certificates, String authType, Socket socket) {

	}

	@Override
	public void checkClientTrusted(X509Certificate[] certificates, String authType, SSLEngine sslEngine) {

	}

	@Override
	public void checkServerTrusted(X509Certificate[] certificates, String authType) {

	}

	@Override
	public void checkServerTrusted(X509Certificate[] certificates, String authType, Socket socket) {

	}

	@Override
	public void checkServerTrusted(X509Certificate[] certificates, String authType, SSLEngine sslEngine) {

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return EMPTY_CERTIFICATES;
	}

}