/*
 * Created on Feb 9, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.test;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * <p>
 * ClientCustomSSL
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class ClientCustomSSL {
	private static final String HOST = "localhost";
	private static final int PORT = 9443;

	private static final String EVAL_REQUEST_URL = String.format(
			"https://%s:%d/EvaluationConnector/PDPConnector/go", HOST, PORT);

	public final static void main(String[] args) throws Exception {
		System.setProperty("javax.net.debug", "ssl,handshake,record");
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "false");
		System.setProperty("https.protocols", "TLSv1,SSLv3");
		
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		
		FileInputStream instream = new FileInputStream(new File(
				"./sample_requests/keystore/myTrustStore"));
		try {
			trustStore.load(instream, "password".toCharArray());
		} finally {
			instream.close();
		}

		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom()
				.loadTrustMaterial(null, new TrustSelfSignedStrategy())
//				.loadTrustMaterial(null, new TrustSelfSignedStrategy())
				.build();
		
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslsf).build();
		try {
			HttpPost post = new HttpPost(EVAL_REQUEST_URL);

			System.out.println("executing request" + post.getRequestLine());

			CloseableHttpResponse response = httpclient.execute(post);
			try {
				HttpEntity entity = response.getEntity();

				System.out.println("----------------------------------------");
				System.out.println(response.getStatusLine());
				if (entity != null) {
					System.out.println("Response content length: "
							+ entity.getContentLength());
				}
				EntityUtils.consume(entity);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
	}
}
