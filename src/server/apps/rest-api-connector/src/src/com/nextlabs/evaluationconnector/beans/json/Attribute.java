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
 * Attribute
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class Attribute implements Serializable {

	private static final long serialVersionUID = 2382809780964414783L;
	private String AttributeId;
	private Object Value;
	private String Issuer;
	private String DataType = "http://www.w3.org/2001/XMLSchema#string";
	private boolean IncludeInResult = false;

	/**
	 * <p>
	 * Getter method for attributeId
	 * </p>
	 * 
	 * @return the attributeId
	 */
	public String getAttributeId() {
		return AttributeId;
	}

	/**
	 * <p>
	 * Getter method for value
	 * </p>
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return Value;
	}

	/**
	 * <p>
	 * Getter method for issuer
	 * </p>
	 * 
	 * @return the issuer
	 */
	public String getIssuer() {
		return Issuer;
	}

	/**
	 * <p>
	 * Getter method for dataType
	 * </p>
	 * 
	 * @return the dataType
	 */
	public String getDataType() {
		return DataType;
	}

	/**
	 * <p>
	 * Getter method for includeInResult
	 * </p>
	 * 
	 * @return the includeInResult
	 */
	public boolean isIncludeInResult() {
		return IncludeInResult;
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
