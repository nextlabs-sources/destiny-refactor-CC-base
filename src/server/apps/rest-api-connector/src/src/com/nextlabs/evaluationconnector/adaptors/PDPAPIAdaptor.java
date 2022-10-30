/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.adaptors;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.evaluationconnector.beans.PDPRequest;
import com.nextlabs.evaluationconnector.beans.XACMLResponse;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;

/**
 * <p>
 * C API Adaptor implementation for PDP
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class PDPAPIAdaptor implements PDPAdapter {

	private static final Log log = LogFactory.getLog(PDPAPIAdaptor.class);

	@Override
	public void init() throws EvaluationConnectorException {
		log.warn("No Implementation found for C-API yet");
		throw new EvaluationConnectorException(
				"No Implementation found for C-API yet");
	}

	@Override
	public XACMLResponse evaluate(List<PDPRequest> pdpRequests)
			throws EvaluationConnectorException {
		log.warn("No Implementation found for C-API yet");
		throw new EvaluationConnectorException(
				"No Implementation found for C-API yet");
	}

	@Override
	public void closeConnection() {
		log.warn("No Implementation found for C-API yet");
	}

}
