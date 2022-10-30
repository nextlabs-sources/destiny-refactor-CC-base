/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.handlers;

import com.nextlabs.evaluationconnector.beans.XACMLResponse;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;

/**
 * <p>
 * EvalRequestHandler
 * 
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public interface EvalRequestHandler {

	/**
	 * <p>
	 * Handle the evaluation request.
	 * </p>
	 *
	 * @param data
	 *            xacml 3.0 compliance data
	 * @return {@link XACMLResponse}
	 * @throws EvaluationConnectorException
	 */
	public XACMLResponse handle(String data)
			throws EvaluationConnectorException;
}
