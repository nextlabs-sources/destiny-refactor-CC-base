/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.exceptions;

/**
 * <p>
 * EvaluationException- This will handle all Evaluation Connector related
 * exceptions.
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class EvaluationConnectorException extends Exception {

	private static final long serialVersionUID = 3235761677224915327L;

	public EvaluationConnectorException() {
		super();
	}

	public EvaluationConnectorException(String message, Throwable cause) {
		super(message, cause);
	}

	public EvaluationConnectorException(String message) {
		super(message);
	}

	public EvaluationConnectorException(Throwable cause) {
		super(cause);
	}

}
