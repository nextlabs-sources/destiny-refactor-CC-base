/*
 * Created on Feb 5, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.beans;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <p>
 * PolicyOnDemandCategory
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class PolicyOnDemand {

	private String pql;
	private boolean ignoreBuildInPolicies;

	public PolicyOnDemand() {
		super();

	}

	public PolicyOnDemand(String pql) {
		super();
		this.pql = pql;
		this.ignoreBuildInPolicies = false;
	}

	public PolicyOnDemand(String pql, boolean ignoreBuildInPolicies) {
		super();
		this.pql = pql;
		this.ignoreBuildInPolicies = ignoreBuildInPolicies;
	}

	/**
	 * <p>
	 * Getter method for pql
	 * </p>
	 * 
	 * @return the pql
	 */
	public String getPql() {
		return pql;
	}

	/**
	 * <p>
	 * Setter method for pql
	 * </p>
	 *
	 * @param pql
	 *            the pql to set
	 */
	public void setPql(String pql) {
		this.pql = pql;
	}

	/**
	 * <p>
	 * Getter method for ignoreBuildInPolicies
	 * </p>
	 * 
	 * @return the ignoreBuildInPolicies
	 */
	public boolean isIgnoreBuildInPolicies() {
		return ignoreBuildInPolicies;
	}

	/**
	 * <p>
	 * Setter method for ignoreBuildInPolicies
	 * </p>
	 *
	 * @param ignoreBuildInPolicies
	 *            the ignoreBuildInPolicies to set
	 */
	public void setIgnoreBuildInPolicies(boolean ignoreBuildInPolicies) {
		this.ignoreBuildInPolicies = ignoreBuildInPolicies;
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
