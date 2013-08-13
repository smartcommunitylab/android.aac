/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.ac.network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class HttpsClientBuilder {

	private static String TAG = "HttpsClientBuilder";

	public static HttpClient getNewHttpClient(HttpParams inParams) {
		HttpClient client = null;

		if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO) {
			/*
			 * Android 2.2 "Froyo" has 2 bugs here: SSLSocketFactory's
			 * createSocket method has to be overwritten creating a custom
			 * SSLSocketFactory; the default X509HostnameVerifier does not
			 * support wildcard certificates (like *.smartcampuslab.it) so a
			 * custom class that try to verify the URL without the wildcart is
			 * needed. This fixes are used for Froyo devices only.
			 */
			client = getWildcartHttpClient(inParams);
		} else {
			client = getDefaultHttpClient(inParams);
		}

		return client;
	}

	private static HttpClient getDefaultHttpClient(HttpParams inParams) {
		if (inParams != null) {
			return new DefaultHttpClient(inParams);
		} else {
			return new DefaultHttpClient();
		}
	}

	private static HttpClient getAcceptAllHttpClient(HttpParams inParams) {
		HttpClient client = null;

		HttpParams params = inParams != null ? inParams : new BasicHttpParams();

		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

			// IMPORTANT: use CustolSSLSocketFactory for 2.2
			SSLSocketFactory sslSocketFactory = new SSLSocketFactory(trustStore);
			if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.FROYO) {
				sslSocketFactory = new CustomSSLSocketFactory(trustStore);
			}

			sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			registry.register(new Scheme("https", sslSocketFactory, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			client = new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			client = new DefaultHttpClient(params);
		}

		return client;
	}

	private static HttpClient getWildcartHttpClient(HttpParams inParams) {
		HttpClient client = null;

		HttpParams params = inParams != null ? inParams : new BasicHttpParams();

		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

			SSLSocketFactory sslSocketFactory = new CustomSSLSocketFactory(trustStore);
			final X509HostnameVerifier delegate = sslSocketFactory.getHostnameVerifier();
			if (!(delegate instanceof WildcardVerifier)) {
				sslSocketFactory.setHostnameVerifier(new WildcardVerifier(delegate));
			}
			registry.register(new Scheme("https", sslSocketFactory, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			client = new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			client = new DefaultHttpClient(params);
		}

		return client;
	}

	/*
	 * Custom classes
	 */
	private static class WildcardVerifier extends AbstractVerifier {
		private final X509HostnameVerifier delegate;

		public WildcardVerifier(final X509HostnameVerifier delegate) {
			this.delegate = delegate;
		}

		@Override
		public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
			boolean ok = false;
			try {
				delegate.verify(host, cns, subjectAlts);
			} catch (SSLException e) {
				for (String cn : cns) {
					if (cn.startsWith("*.")) {
						try {
							delegate.verify(host, new String[] { cn.substring(2) }, subjectAlts);
							ok = true;
						} catch (Exception e1) {
							Log.e(HttpsClientBuilder.TAG, e1.getMessage());
						}
					}
				}
				if (!ok) {
					throw e;
				}
			}
		}
	}

	private static class CustomSSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public CustomSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
				UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

}