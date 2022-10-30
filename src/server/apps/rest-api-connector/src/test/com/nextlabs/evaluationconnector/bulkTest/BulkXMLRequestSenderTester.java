/*
 * Created on Feb 3, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */

/*
 * Created on Jan 30, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.bulkTest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
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

import com.nextlabs.evaluationconnector.test.TestUtils;
import com.nextlabs.evaluationconnector.utils.Constants;

/**
 * <p>
 * BulkXMLRequestSenderTest
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
// @RunWith(JUnit4.class)
public class BulkXMLRequestSenderTester {

	private static final Log log = LogFactory
			.getLog(BulkJSONRequestSenderTester.class);
	private static Server server;
	private PoolingHttpClientConnectionManager connectionManager;
	private int threadPoolSize = 10;
	private ExecutorService threadPool;

	private static final String HOST = "localhost";
	private static final int PORT = 8085;

	private static final String EVAL_REQUEST_URL = String.format(
			"http://%s:%d/EvaluationConnector/PDPConnector/go", HOST, PORT);

	private String success_request_file = "./sample_requests/SampleXACMLRequest.txt";
	private String missing_id_request_file = "./sample_requests/SampleXACMLRequest_AllowedFile.txt";
	private String allowed_request_file = "./sample_requests/SampleXACMLRequest_AllowedFile.txt";
	private String duplicate_subject_request_file = "./sample_requests/SampleXACMLRequest_MissingResID.txt";
	private String deny_request_file = "./sample_requests/SampleXACMLRequest.txt";
	private String request_with_pod1 = "./sample_requests/SampleXACMLRequestPOD1.txt";
	private String request_with_pod2 = "./sample_requests/SampleXACMLRequestPOD2.txt";

	private int noOfRequest = 1000;

	@Test
	public void sendBulkMessages() throws ClientProtocolException, IOException,
			InterruptedException {
		final List<String> requestData = new ArrayList<String>();
		requestData.add(loadRequestData(success_request_file));
		requestData.add(loadRequestData(missing_id_request_file));
		requestData.add(loadRequestData(allowed_request_file));
		requestData.add(loadRequestData(deny_request_file));
		requestData.add(loadRequestData(duplicate_subject_request_file));
		requestData.add(loadRequestData(request_with_pod1));
		requestData.add(loadRequestData(request_with_pod2));

		Random random = new Random();

		for (int i = 1; i <= noOfRequest; i++) {
			final int count = i;
			log.info("-------------->>>>>>>>>>>>>>>>>   \n\n\t\t\t NEXT  :"
					+ count);
			final int pick = (int) random.nextInt(4) + 1;
			threadPool.submit(new Runnable() {

				@Override
				public void run() {
					CloseableHttpClient httpClient = HttpClients.custom()
							.setConnectionManager(connectionManager).build();

					HttpPost post = new HttpPost(EVAL_REQUEST_URL);
					try {
						addRequestParameters(post, requestData.get(pick));

						long startCounter = System.currentTimeMillis();
						HttpResponse response = httpClient.execute(post);
						if (log.isInfoEnabled())
							log.info(String
									.format("%s, Thread Id:[%s],  BulkJSONRequestSenderTest --------------------------------------------->  Single evaluation end to end Time : %d milis",
											Constants.PERF_LOG_PREFIX,
											"" + Thread.currentThread().getId(),
											(System.currentTimeMillis() - startCounter)));
						log.info("-------------->>>>>>>>>>>>>>>>>   Response Status :"
								+ response.getStatusLine().getStatusCode()
								+ ", Count :" + count);
						
						String responseContent = TestUtils
								.getDataFromEntity(response.getEntity());
						log.info("-------------->>>>>>>>>>>>>>>>>   Response Content : "
								+ responseContent);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						post.releaseConnection();

					}

				}
			});
		}
	}

	public static void main(String[] a) throws Exception {
		try {

			BulkJSONRequestSenderTester t = new BulkJSONRequestSenderTester();
			t.initClient();
			t.sendBulkMessages();

		} finally {
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

	protected void addRequestParameters(HttpPost post, String payLoadData)
			throws UnsupportedEncodingException {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair(Constants.SERVICE_TYPE_PARAM,
				Constants.EVAL_REQ_SERVICE));
		nvps.add(new BasicNameValuePair(Constants.DATA_TYPE_PARAM,
				Constants.XML_DATA_TYPE));
		nvps.add(new BasicNameValuePair(Constants.VERSION_PARAM, "1.0"));
		nvps.add(new BasicNameValuePair(Constants.XACML_DATA_PARAM, payLoadData));

		post.setEntity(new UrlEncodedFormEntity(nvps));
	}

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
		webAppContext.setContextPath("/EvaluationConnector");
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

	@Before
	public void initClient() {
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(120);
		connectionManager.closeExpiredConnections();

		threadPool = Executors.newFixedThreadPool(threadPoolSize);
		log.info("HttpClient Created");
	}

	@After
	public void closeClient() {
		threadPool.shutdown();
		log.info("HttpClient Shutdown");
	}
}
