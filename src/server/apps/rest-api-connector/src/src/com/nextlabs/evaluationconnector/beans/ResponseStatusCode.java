/*
 * Created on Jan 22, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.beans;

/**
 * <p>
 * ResponseStatusCode
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public enum ResponseStatusCode {

	OK("urn:oasis:names:tc:xacml:1.0:status:ok"), 
	PROCESSING_ERROR("urn:oasis:names:tc:xacml:1.0:status:processing-error"), 
	MISSING_ATTRIB("urn:oasis:names:tc:xacml:1.0:status:missing-attribute"),
	SYNTAX_ERROR("urn:oasis:names:tc:xacml:1.0:status:syntax-error");

	private String value;

	private ResponseStatusCode(String value) {
		this.value = value;
	}

	/**
	 * <p>
	 * Getter method for value
	 * </p>
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
