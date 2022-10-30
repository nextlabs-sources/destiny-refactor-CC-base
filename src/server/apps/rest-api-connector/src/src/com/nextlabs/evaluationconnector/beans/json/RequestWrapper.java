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
 * JSONRequest
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class RequestWrapper implements Serializable {

	private static final long serialVersionUID = -4251000631439648898L;
	private Request Request;

	/**
	 * <p>
	 * Getter method for request
	 * </p>
	 * 
	 * @return the request
	 */
	public Request getRequest() {
		return Request;
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
