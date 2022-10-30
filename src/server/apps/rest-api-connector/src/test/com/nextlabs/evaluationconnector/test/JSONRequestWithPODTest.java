/*
 * Created on Feb 5, 2015
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
import org.eclipse.jetty.http.MimeTypes;
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

import com.nextlabs.evaluationconnector.beans.json.ResponseWrapper;
import com.nextlabs.evaluationconnector.utils.Constants;

/**
 * <p>
 * JSONRequestWithPODTest
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
@RunWith(JUnit4.class)
public class JSONRequestWithPODTest {

	private static final Log log = LogFactory.getLog(JSONRequestTest.class);

	private static Server server;
	private PoolingHttpClientConnectionManager connectionManager;
	private CloseableHttpClient httpClient;
	
	/*private static final String HOST = "localhost";
	private static final int PORT = 8080;
	private static final String EVAL_REQUEST_URL = String.format(
			"http://%s:%d/rest-api/PDPConnector/go", HOST, PORT);
	*/
	
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
		log.info("HttpClient Shutdown");
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

	private String pod_request_file1 = "C:/P4/Destiny/D_Crux/pcv/Crux_REST_Multi/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_POD_1.txt";

	@Test
	public void sendPolicyOnDemand1Request() throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(EVAL_REQUEST_URL);
		addRequestParameters(post, pod_request_file1);

		HttpResponse response = httpClient.execute(post);
		log.info("sendSuccessRequest :" + MimeTypes.Type.APPLICATION_JSON.asString());

		log.info("Response Status :" + response.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

		HttpEntity entity = response.getEntity();
		log.info("Response Content Type : " + entity.getContent().available());

		ResponseWrapper responseWrapper = TestUtils.jsonResponse(TestUtils.getDataFromEntity(entity));
		assertEquals(1, responseWrapper.getResponse().getResult().size());
		assertFalse(" Should have a Decision ",
				(responseWrapper.getResponse().getResult().get(0).getDecision() == null));
	}

	//private String pod_request_file2 = "../sample_requests/SampleJSONRequest_POD_2.txt";
	private String pod_request_file2 = "C:/P4/Destiny/D_Crux/pcv/Crux_REST_Multi/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_POD_2.txt";

	@Test
	public void sendAllowedRequest() throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(EVAL_REQUEST_URL);
		addRequestParameters(post, pod_request_file2);

		HttpResponse response = httpClient.execute(post);
		log.info("sendSuccessRequest :" + MimeTypes.Type.APPLICATION_JSON.asString());

		log.info("Response Status :" + response.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

		HttpEntity entity = response.getEntity();
		log.info("Response Content Type : " + entity.getContent().available());

		ResponseWrapper responseWrapper = TestUtils.jsonResponse(TestUtils.getDataFromEntity(entity));
		assertEquals(1, responseWrapper.getResponse().getResult().size());
		assertFalse(" Should have a Decision ",
				(responseWrapper.getResponse().getResult().get(0).getDecision() == null));
	}

	private String pod_request_file3 = "C:/P4/Destiny/D_Crux/pcv/Crux_REST_Multi/main/src/server/apps/rest-api-connector/src/sample_requests/SampleJSONRequest_POD_3.txt";

	@Test
	public void sendPODRequestWithAddlData() throws ClientProtocolException, IOException {
		HttpPost post = new HttpPost(EVAL_REQUEST_URL);
		addRequestParameters(post, pod_request_file3);

		HttpResponse response = httpClient.execute(post);
		log.info("sendSuccessRequest :" + MimeTypes.Type.APPLICATION_JSON.asString());

		log.info("Response Status :" + response.getStatusLine().getStatusCode());
		assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());

		HttpEntity entity = response.getEntity();
		log.info("Response Content Type : " + entity.getContent().available());

		ResponseWrapper responseWrapper = TestUtils.jsonResponse(TestUtils.getDataFromEntity(entity));
		assertEquals(1, responseWrapper.getResponse().getResult().size());
		assertFalse(" Should have a Decision ",
				(responseWrapper.getResponse().getResult().get(0).getDecision() == null));
	}
	
	protected void addRequestParameters(HttpPost post, String filename)
			throws UnsupportedEncodingException {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(Constants.SERVICE_TYPE_PARAM,
				Constants.EVAL_REQ_SERVICE));
		nvps.add(new BasicNameValuePair(Constants.DATA_TYPE_PARAM,
				Constants.JSON_DATA_TYPE));
		nvps.add(new BasicNameValuePair(Constants.VERSION_PARAM, "1.0"));
		nvps.add(new BasicNameValuePair(Constants.XACML_DATA_PARAM,
				loadRequestData(filename)));

		post.setEntity(new UrlEncodedFormEntity(nvps));
	}

}
