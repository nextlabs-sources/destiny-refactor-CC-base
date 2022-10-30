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
 * AttributeAssignment
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class AttributeAssignment implements Serializable {

	private static final long serialVersionUID = -6460227221801311260L;

	private String AttributeId;
	private Object Value;
	private String Category;
	private String DataType;
	private String Issuer;

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
	 * Setter method for attributeId
	 * </p>
	 *
	 * @param attributeId
	 *            the attributeId to set
	 */
	public void setAttributeId(String attributeId) {
		AttributeId = attributeId;
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
	 * Setter method for value
	 * </p>
	 *
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		Value = value;
	}

	/**
	 * <p>
	 * Getter method for category
	 * </p>
	 * 
	 * @return the category
	 */
	public String getCategory() {
		return Category;
	}

	/**
	 * <p>
	 * Setter method for category
	 * </p>
	 *
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		Category = category;
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
	 * Setter method for dataType
	 * </p>
	 *
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(String dataType) {
		DataType = dataType;
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
	 * Setter method for issuer
	 * </p>
	 *
	 * @param issuer
	 *            the issuer to set
	 */
	public void setIssuer(String issuer) {
		Issuer = issuer;
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
