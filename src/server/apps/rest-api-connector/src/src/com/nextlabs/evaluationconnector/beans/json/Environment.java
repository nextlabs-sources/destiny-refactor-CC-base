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
 * Environment
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class Environment extends Category {

	private static final long serialVersionUID = 4992212306776994313L;

	public Environment() {
		CategoryId = XACMLParser.CATEGORY_ENVIRONMENT;
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
