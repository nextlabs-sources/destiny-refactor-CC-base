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
 * RequestReference
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class RequestReference implements Serializable {

	private static final long serialVersionUID = 2236807739454740520L;
	private List<String> ReferenceId;

	/**
	 * <p>
	 * Getter method for referenceId
	 * </p>
	 * 
	 * @return the referenceId
	 */
	public List<String> getReferenceId() {
		if (ReferenceId == null) {
			ReferenceId = new ArrayList<String>();
		}
		return ReferenceId;
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
