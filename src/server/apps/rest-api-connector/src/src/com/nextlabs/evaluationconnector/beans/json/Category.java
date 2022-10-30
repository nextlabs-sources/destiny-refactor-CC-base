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
 * Category
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class Category implements Serializable {

	private static final long serialVersionUID = 337650581414178502L;
	protected String CategoryId;
	protected String Id;
	protected String Content;
	protected List<Attribute> Attribute;

	/**
	 * <p>
	 * Getter method for categoryId
	 * </p>
	 * 
	 * @return the categoryId
	 */
	public String getCategoryId() {
		return CategoryId;
	}

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
	 * Getter method for content
	 * </p>
	 * 
	 * @return the content
	 */
	public String getContent() {
		return Content;
	}

	/**
	 * <p>
	 * Getter method for attribute
	 * </p>
	 * 
	 * @return the attribute
	 */
	public List<Attribute> getAttribute() {
		if (Attribute == null) {
			Attribute = new ArrayList<Attribute>();
		}
		return Attribute;
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
