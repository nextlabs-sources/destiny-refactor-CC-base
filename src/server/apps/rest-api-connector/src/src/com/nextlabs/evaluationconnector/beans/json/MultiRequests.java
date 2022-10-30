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
 * MultiRequests
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class MultiRequests implements Serializable {

	private static final long serialVersionUID = 3718055551375965371L;
	private List<RequestReference> RequestReference;

	/**
	 * <p>
	 * Getter method for requestReference
	 * </p>
	 * 
	 * @return the requestReference
	 */
	public List<RequestReference> getRequestReference() {
		if (RequestReference == null) {
			RequestReference = new ArrayList<RequestReference>();
		}
		return RequestReference;
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
