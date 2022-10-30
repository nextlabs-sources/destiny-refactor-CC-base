/*
 * Created on Jan 20, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.adaptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;

/**
 * <p>
 * This factory class will generates the respective API adaptor for PDP
 * evaluation.
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public final class PDPAdaptorFactory {

	private static final Log log = LogFactory.getLog(PDPAdaptorFactory.class);

	private static PDPAPIAdaptor apiAdaptor;
	private static PDPRMIAdaptor rmiAdaptor;
	private static PDPDirectAdaptor directAdaptor;

	private static boolean initialized = false;

	private static void initAdaptors(String apiMode)
			throws EvaluationConnectorException {
		if (apiAdaptor == null && apiMode.equals(PDPAdapter.CAPI_MODE)) {
			apiAdaptor = new PDPAPIAdaptor();
			apiAdaptor.init();
			if (log.isInfoEnabled())
				log.info("Created new PDP C-API adaptor successfully");
			initialized = true;

		} else if (rmiAdaptor == null && apiMode.equals(PDPAdapter.RMI_MODE)) {
			rmiAdaptor = new PDPRMIAdaptor();
			rmiAdaptor.init();
			if (log.isInfoEnabled())
				log.info("Created new PDP RMI adaptor successfully");
			initialized = true;
		}  else if (directAdaptor == null && apiMode.equals(PDPAdapter.DIRECT_MODE)) {
			directAdaptor = new PDPDirectAdaptor();
			directAdaptor.init();
			if (log.isInfoEnabled())
				log.info("New PDP direct adaptor created successfully");
			initialized = true;
		}

	}

	/**
	 * <p>
	 * Get Instance of PDP Adapter according to the given api mode
	 * </p>
	 *
	 * @param apiMode
	 *            RMI / C-API mode
	 * @return {@link PDPAdapter}
	 */
	public static PDPAdapter getInstance(String apiMode)
			throws EvaluationConnectorException {

		if (!initialized)
			initAdaptors(apiMode);

		if (PDPAdapter.CAPI_MODE.equals(apiMode)) {
			return apiAdaptor;

		} else if (PDPAdapter.RMI_MODE.equals(apiMode)) {
			return rmiAdaptor;
			
		} else if (PDPAdapter.DIRECT_MODE.equals(apiMode)) {
			return directAdaptor;
			
		} else {
			throw new EvaluationConnectorException(
					"Invalid API mode to create instance of PDP Adaptor");
		}
	}

	public static void closeConnection() {
		if (apiAdaptor != null) {
			apiAdaptor.closeConnection();
			if (log.isInfoEnabled())
				log.info("PDP API adaptor connection closed successfully");
		}

		if (rmiAdaptor != null) {
			rmiAdaptor.closeConnection();
			if (log.isInfoEnabled())
				log.info("PDP RMI adaptor connection closed successfully");
		}
	}
}
