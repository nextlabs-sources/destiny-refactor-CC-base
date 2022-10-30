/*
 * Created on Jan 27, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.beans.json;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <p>
 * ResponseWrapper
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class ResponseWrapper implements Serializable {

	private static final long serialVersionUID = 6507221248851999122L;
	private Response Response;

	public ResponseWrapper() {
		super();
		Response = new Response();
	}

	/**
	 * <p>
	 * Getter method for response
	 * </p>
	 * 
	 * @return the response
	 */
	public Response getResponse() {
		return Response;
	}

	/**
	 * <p>
	 * Setter method for response
	 * </p>
	 *
	 * @param response
	 *            the response to set
	 */
	public void setResponse(Response response) {
		Response = response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
