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
 * StatusCode
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class StatusCode implements Serializable {

	private static final long serialVersionUID = 36338574900259621L;

	private String Value;

	/**
	 * <p>
	 * Getter method for value
	 * </p>
	 * 
	 * @return the value
	 */
	public String getValue() {
		return Value;
	}

	/**
	 * <p>
	 * Setter method for value
	 * </p>
	 *
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		Value = value;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
