/*
 * Created on Feb 6, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.nextlabs.evaluationconnector.utils.Constants;

/**
 * <p>
 * XMLMultiRequestTest
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
@RunWith(JUnit4.class)
public class XMLMultiRequestTest {

	private static final Log log = LogFactory.getLog(XMLMultiRequestTest.class);

	private static Server server;
	private PoolingHttpClientConnectionManager connectionManager;
	private CloseableHttpClient httpClient;

	/*private static final String HOST = "localhost";
	private static final int PORT = 8080;
	private static final String EVAL_REQUEST_URL = String.format(
			"http://%s:%d/rest-api/PDPConnector/go", HOST, PORT);*/
	
	private static final String HOST = "10.63.0.185";
	private static final int PORT = 58080;
	private static final String EVAL_REQUEST_URL = String.format(
			"http://%s:%d/dpc/PDPConnector/go", HOST, PORT);

	@BeforeClass
	public static void startServer() throws Exception {
		server = new Server();
		server.setStopAtShutdown(true);

		ServerConnector connector = new ServerConnector(server);
		connector.setHost("localhost");
		connector.setPort(8085);
		connector.setName("eval_test");
		server.addConnector(connector);

		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/rest-api");
		webAppContext.setBaseResource(new ResourceCollection(new String[] {
				"C:/P4/Destiny/D_Crux/pcv/Crux_REST_Multi/main/src/server/apps/rest-api-connector/src/WebContent",
				"C:/P4/Destiny/D_Crux/pcv/Crux_REST_Multi/main/src/server/apps/rest-api-connector/src/build" }));
		server.setHandler(webAppContext);
		server.start();
		log.info("Server Started");
	}

	@AfterClass
	public static void shutdownServer() throws Exception {
		server.stop();
		log.info("Server Stoped");
	}

	@Before
	public void initClient() {
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(120);
		connectionManager.closeExpiredConnections();

		httpClient = HttpClients.custom()
				.setConnectionManager(connectionManager).build();
		log.info("HttpClient Created");
	}

	@After
	public void closeClient() {
		try {
			httpClient.close();
		} catch (IOException e) {
			log.info("Error in HttpClient Shutdown, ", e);
		}
	}

	@SuppressWarnings("resource")
	private String loadRequestData(String fileName) {
		String data = "";
		try {
			FileInputStream fileIo = new FileInputStream(fileName);
			byte bytes[] = new byte[fileIo.available()];
			fileIo.read(bytes);
			data = new String(bytes);
			log.debug("Request Data Loaded : " + data);
		} catch (Exception e) {
			log.error("Error occurred in loading request data,", e);
		}
		return data;
	}

	private String multi_request_file = "C:/P4/Destiny/D_Crux/pcv/Crux_REST_Multi/main/src/server/apps/rest-api-connector/src/sample_requests/SampleXACMLRequest_MultiRequest.txt";

	@Test
	public void sendMultiRequest1() throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(EVAL_REQUEST_URL);
		addRequestParameters(post, multi_request_file);
		HttpResponse response = httpClient.execute(post);

		log.info("Response Status :" + response.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

		HttpEntity entity = response.getEntity();
		log.info("Response Content Type : " + entity.getContentLength());

		String reponseContent = TestUtils.getDataFromEntity(entity);
		log.info("Response Content      : " + reponseContent);

		ResponseType responseType = TestUtils.unmarshallXMLResponse(reponseContent);
		assertEquals(2, responseType.getResult().size());
		for (ResultType result : responseType.getResult()) {
			assertFalse(" Should have a decision ", (result.getDecision() == null));
		}
	}

	private String multi_request_additional_attrs_file = "C:/P4/Destiny/D_Crux/pcv/Crux_REST_Multi/main/src/server/apps/rest-api-connector/src/sample_requests/SampleXACMLRequest_MultiRequest_AdditionalAttrs.txt";

	@Test
	public void sendMultiRequest2() throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(EVAL_REQUEST_URL);
		addRequestParameters(post, multi_request_additional_attrs_file);
		HttpResponse response = httpClient.execute(post);

		log.info("Response Status :" + response.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

		HttpEntity entity = response.getEntity();
		log.info("Response Content Type : " + entity.getContent().available());

		String reponseContent = TestUtils.getDataFromEntity(entity);
		log.debug("Response Content      : " + reponseContent);

		ResponseType responseType = TestUtils.unmarshallXMLResponse(reponseContent);
		assertEquals(2, responseType.getResult().size());
		for (ResultType result : responseType.getResult()) {
			assertFalse(" Should have a decision ", (result.getDecision() == null));
		}
	}
	
	private String multi_request_multi_resource_file = "C:/P4/Destiny/D_Crux/pcv/Crux_REST_Multi/main/src/server/apps/rest-api-connector/src/sample_requests/SampleXACMLRequest_MultiRequest_MultiResource.txt";

	@Test
	public void sendMultiRequest3() throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(EVAL_REQUEST_URL);
		addRequestParameters(post, multi_request_multi_resource_file);
		HttpResponse response = httpClient.execute(post);

		log.info("Response Status :" + response.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

		HttpEntity entity = response.getEntity();
		log.info("Response Content Type : " + entity.getContent().available());

		String reponseContent = TestUtils.getDataFromEntity(entity);
		log.debug("Response Content      : " + reponseContent);

		ResponseType responseType = TestUtils.unmarshallXMLResponse(reponseContent);
		assertEquals(2, responseType.getResult().size());
		for (ResultType result : responseType.getResult()) {
			assertFalse(" Should have a decision ", (result.getDecision() == null));
			//assertEquals("Permit", result.getDecision());
		}
	}
	
	/*private String multi_request_error_file_2 = "C:/P4/Destiny/D_Crux/pcv/Crux_REST_Multi/main/src/server/apps/rest-api-connector/src/sample_requests/SampleXACMLRequest_MultiRequest_Error2.txt";

	@Test
	public void sendMultiRequest4() throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(EVAL_REQUEST_URL);
		addRequestParameters(post, multi_request_error_file_2);
		HttpResponse response = httpClient.execute(post);

		log.info("Response Status :" + response.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

		HttpEntity entity = response.getEntity();
		log.info("Response Content Type : " + entity.getContent().available());

		String reponseContent = TestUtils.getDataFromEntity(entity);
		log.debug("Response Content      : " + reponseContent);

		ResponseType responseType = TestUtils.unmarshallXMLResponse(reponseContent);
		assertEquals(1, responseType.getResult().size());
	}	*/
	

	protected void addRequestParameters(HttpPost post, String filename)
			throws UnsupportedEncodingException {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(Constants.SERVICE_TYPE_PARAM,
				Constants.EVAL_REQ_SERVICE));
		nvps.add(new BasicNameValuePair(Constants.DATA_TYPE_PARAM,
				Constants.XML_DATA_TYPE));
		nvps.add(new BasicNameValuePair(Constants.VERSION_PARAM, "1.0"));
		nvps.add(new BasicNameValuePair(Constants.XACML_DATA_PARAM,
				loadRequestData(filename)));

		post.setEntity(new UrlEncodedFormEntity(nvps));
	}

}
