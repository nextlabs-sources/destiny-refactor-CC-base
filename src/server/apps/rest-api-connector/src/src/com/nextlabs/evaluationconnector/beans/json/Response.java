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
 * Response
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class Response implements Serializable {

	private static final long serialVersionUID = 6601086235718312958L;

	private List<Result> Result;

	/**
	 * <p>
	 * Getter method for result
	 * </p>
	 * 
	 * @return the result
	 */
	public List<Result> getResult() {
		if (Result == null) {
			Result = new ArrayList<Result>();
		}
		return Result;
	}

	/**
	 * <p>
	 * Setter method for result
	 * </p>
	 *
	 * @param result
	 *            the result to set
	 */
	public void setResult(List<Result> result) {
		Result = result;
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
