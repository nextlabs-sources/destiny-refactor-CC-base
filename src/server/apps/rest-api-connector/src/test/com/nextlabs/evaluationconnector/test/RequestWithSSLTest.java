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
import java.io.FileNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * <p>
 * RequestWithSSLTest
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
@RunWith(JUnit4.class)
public class RequestWithSSLTest {

	private static final Log log = LogFactory.getLog(RequestWithSSLTest.class);

	private static Server server;
	private PoolingHttpClientConnectionManager connectionManager;
	private CloseableHttpClient httpClient;

	private static final String HOST = "localhost";
	private static final int PORT = 8085;

	private static final String EVAL_REQUEST_URL = String.format(
			"http://%s:%d/rest-api/PDPConnector/go", HOST, PORT);

	@BeforeClass
	public static void startServer() throws Exception {

		System.setProperty("javax.net.debug", "ssl,handshake,record");
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "false");
		System.setProperty("https.protocols", "TLSv1,SSLv3");

		String jettyDistKeystore = "./sample_requests/keystore/keystore";
		String keystorePath = System.getProperty("jetty", jettyDistKeystore);
		File keystoreFile = new File(keystorePath);
		if (!keystoreFile.exists()) {
			throw new FileNotFoundException(keystoreFile.getAbsolutePath());
		}

		server = new Server();
		server.setStopAtShutdown(true);

		SslContextFactory sslContextFactory = new SslContextFactory();
		sslContextFactory.setKeyStorePath(keystoreFile.getAbsolutePath());
		sslContextFactory.setKeyStorePassword("password");
		sslContextFactory.setKeyManagerPassword("password");

		HttpConfiguration https_config = new HttpConfiguration();
		https_config.setSecureScheme("https");
		https_config.setSecurePort(9443);
		https_config.setOutputBufferSize(64768);
		https_config.addCustomizer(new SecureRequestCustomizer());

		ServerConnector https = new ServerConnector(server,
				new SslConnectionFactory(sslContextFactory,
						HttpVersion.HTTP_1_1.asString()),
				new HttpConnectionFactory(https_config));
		https.setHost("localhost");
		https.setPort(9443);
		https.setIdleTimeout(500000);
		server.addConnector(https);

		ServerConnector http = new ServerConnector(server);
		http.setHost("localhost");
		http.setPort(8080);
		http.setName("eval_test");
		server.addConnector(http);

		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/rest-api");
		webAppContext.setBaseResource(new ResourceCollection(new String[] {
				"./WebContent", "./build" }));
		server.setHandler(webAppContext);
		server.start();
		log.info("Server Started");
	}

	@AfterClass
	public static void shutdownServer() throws Exception {
		server.stop();
		log.info("Server Stoped");
	}

	public static void main(String[] a) throws Exception {
		startServer();
	}

}
