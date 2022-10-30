/*
 * Created on Jan 27, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.beans.json;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.nextlabs.evaluationconnector.parsers.XACMLParser;

/**
 * <p>
 * Action
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class Action extends Category {

	private static final long serialVersionUID = 3383181395210238152L;

	public Action() {
		CategoryId = XACMLParser.CATEGORY_ACTION;
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
