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
 * Request
 * </p>
 *
 *
 * @author Amila Silva
 *
 */

public class Request implements Serializable {

	private static final long serialVersionUID = 2046078744787261069L;
	private boolean CombinedDecision = false;
	private boolean ReturnPolicyIdList = false;
	private String XPathVersion = "http://www.w3.org/TR/1999/REC-xpath-19991116";
	private List<Category> Category;
	private List<Subject> Subject;
	private List<Action> Action;
	private List<Resource> Resource;
	private List<Environment> Environment;
	private MultiRequests MultiRequests;

	/**
	 * <p>
	 * Getter method for combinedDecision
	 * </p>
	 * 
	 * @return the combinedDecision
	 */
	public boolean isCombinedDecision() {
		return CombinedDecision;
	}

	/**
	 * <p>
	 * Getter method for returnPolicyIdList
	 * </p>
	 * 
	 * @return the returnPolicyIdList
	 */
	public boolean isReturnPolicyIdList() {
		return ReturnPolicyIdList;
	}

	/**
	 * <p>
	 * Getter method for xPathVersion
	 * </p>
	 * 
	 * @return the xPathVersion
	 */
	public String getXPathVersion() {
		return XPathVersion;
	}

	/**
	 * <p>
	 * Getter method for category
	 * </p>
	 * 
	 * @return the category
	 */
	public List<Category> getCategory() {
		if (Category == null) {
			Category = new ArrayList<Category>();
		}
		return Category;
	}

	/**
	 * <p>
	 * Getter method for subject
	 * </p>
	 * 
	 * @return the subject
	 */
	public List<Subject> getSubject() {
		if (Subject == null) {
			Subject = new ArrayList<Subject>();
		}
		return Subject;
	}

	/**
	 * <p>
	 * Getter method for action
	 * </p>
	 * 
	 * @return the action
	 */
	public List<Action> getAction() {
		if (Action == null) {
			Action = new ArrayList<Action>();
		}
		return Action;
	}

	/**
	 * <p>
	 * Getter method for resource
	 * </p>
	 * 
	 * @return the resource
	 */
	public List<Resource> getResource() {
		if (Resource == null) {
			Resource = new ArrayList<Resource>();
		}
		return Resource;
	}

	/**
	 * <p>
	 * Getter method for environment
	 * </p>
	 * 
	 * @return the environment
	 */
	public List<Environment> getEnvironment() {
		if (Environment == null) {
			Environment = new ArrayList<Environment>();
		}
		return Environment;
	}

	/**
	 * <p>
	 * Getter method for multiRequests
	 * </p>
	 * 
	 * @return the multiRequests
	 */
	public MultiRequests getMultiRequests() {
		return MultiRequests;
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
