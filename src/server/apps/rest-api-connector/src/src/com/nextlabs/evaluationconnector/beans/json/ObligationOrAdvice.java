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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <p>
 * Obligations
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class ObligationOrAdvice implements Serializable {

	private static final long serialVersionUID = -7621939183240069358L;

	private String Id;
	private List<AttributeAssignment> AttributeAssignment;

	/**
	 * <p>
	 * Getter method for id
	 * </p>
	 * 
	 * @return the id
	 */
	public String getId() {
		return Id;
	}

	/**
	 * <p>
	 * Setter method for id
	 * </p>
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		Id = id;
	}

	/**
	 * <p>
	 * Getter method for attributeAssignment
	 * </p>
	 * 
	 * @return the attributeAssignment
	 */
	public List<AttributeAssignment> getAttributeAssignment() {
		if (AttributeAssignment == null) {
			AttributeAssignment = new ArrayList<AttributeAssignment>();
		}
		return AttributeAssignment;
	}

	/**
	 * <p>
	 * Setter method for attributeAssignment
	 * </p>
	 *
	 * @param attributeAssignment
	 *            the attributeAssignment to set
	 */
	public void setAttributeAssignment(
			List<AttributeAssignment> attributeAssignment) {
		AttributeAssignment = attributeAssignment;
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
