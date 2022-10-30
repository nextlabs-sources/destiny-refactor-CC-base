/*
 * Created on Jan 30, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.nextlabs.evaluationconnector.adaptors.PDPAdapter;
import com.nextlabs.evaluationconnector.adaptors.PDPAdaptorFactory;
import com.nextlabs.evaluationconnector.adaptors.PDPRMIAdaptor;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;

/**
 * <p>
 * PDPAdapterTest
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
@RunWith(JUnit4.class)
public class PDPAdapterTest {
	private static final Log log = LogFactory.getLog(PDPAdapterTest.class);
	private static Server server;

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
				"../WebContent", "../build" }));
		server.setHandler(webAppContext);
		server.start();
		log.info("Server Started");
	}

	@AfterClass
	public static void shutdownServer() throws Exception {
		server.stop();
		log.info("Server Stoped");
	}

	@Test
	public void shouldGetRMIAdapter() throws EvaluationConnectorException {

		PDPAdapter pdpAdapter = PDPAdaptorFactory
				.getInstance(PDPAdapter.RMI_MODE);

		assertNotNull(pdpAdapter);
		assertEquals(PDPRMIAdaptor.class, pdpAdapter.getClass());
	}

	@Test
	public void shouldGetAPIAdapter() throws EvaluationConnectorException {
		
		PDPAdaptorFactory.getInstance(PDPAdapter.CAPI_MODE);
//		Assert.fail("PDP Adapter not yet implemented");
	}

	@Test(expected = EvaluationConnectorException.class)
	public void shouldNotHaveAdapter() throws EvaluationConnectorException {

		PDPAdaptorFactory.getInstance("Test");
		Assert.fail("Should not reach here, No PDP Adapter for \"Test\"");
	}

}
