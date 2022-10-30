/*
 * Created on Feb 3, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.bulkTest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * <p>
 * JettyServerMain
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class JettyServerMain {

	private static final Log log = LogFactory
			.getLog(BulkJSONRequestSenderTester.class);
	private static Server server;

	private static final String HOST = "localhost";
	private static final int PORT = 8085;

	@BeforeClass
	public static void startServer() throws Exception {
		server = new Server();
		server.setStopAtShutdown(true);

		ServerConnector connector = new ServerConnector(server);
		connector.setHost(HOST);
		connector.setPort(PORT);
		connector.setName("eval_test");
		server.addConnector(connector);

		WebAppContext webAppContext = new WebAppContext();
		webAppContext.setContextPath("/rest-api");
		webAppContext.setBaseResource(new ResourceCollection(new String[] {
				"./WebContent", "./build" }));
		server.setHandler(webAppContext);
		server.start();
		server.join();
		log.info("Server Started");
	}

	@AfterClass
	public static void shutdownServer() throws Exception {
		server.stop();
		log.info("Server Stoped");
	}

	public static void main(String[] a) throws Exception {

		// PropertiesUtil
		// .createFile("C:/Users/asilva/NextLabs_Workspace/EvaluationConnector/build/performance_JSON.csv");
		startServer();

		// threadPool.submit(new Runnable() {
		//
		// @Override
		// public void run() {
		// try {
		// Thread.sleep(60000);
		//
		// PropertiesUtil.flushToFile();
		//
		// log.info("Captured DATA Written to FILE");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } finally {
		// System.exit(-1);
		// }
		//
		// }
		// });

	}

}
