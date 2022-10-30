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
 * IdReference
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class IdReference implements Serializable {

	private static final long serialVersionUID = -7376819129664690660L;
	private String Id;
	private String Version;

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
	 * Getter method for version
	 * </p>
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return Version;
	}

	/**
	 * <p>
	 * Setter method for version
	 * </p>
	 *
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		Version = version;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
