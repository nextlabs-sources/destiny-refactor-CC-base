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
 * Result
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class Result implements Serializable {

	private static final long serialVersionUID = 6153664301606675140L;

	private String Decision;
	private Status Status;
	private List<ObligationOrAdvice> Obligations;
	private List<ObligationOrAdvice> AssociatedAdvice;
	private List<Attribute> Attributes;
	private List<PolicyIdentifier> PolicyIdentifierList;

	/**
	 * <p>
	 * Getter method for decision
	 * </p>
	 * 
	 * @return the decision
	 */
	public String getDecision() {
		return Decision;
	}

	/**
	 * <p>
	 * Setter method for decision
	 * </p>
	 *
	 * @param decision
	 *            the decision to set
	 */
	public void setDecision(String decision) {
		Decision = decision;
	}

	/**
	 * <p>
	 * Getter method for status
	 * </p>
	 * 
	 * @return the status
	 */
	public Status getStatus() {
		return Status;
	}

	/**
	 * <p>
	 * Setter method for status
	 * </p>
	 *
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Status status) {
		Status = status;
	}

	/**
	 * <p>
	 * Getter method for obligations
	 * </p>
	 * 
	 * @return the obligations
	 */
	public List<ObligationOrAdvice> getObligations() {
		if (Obligations == null) {
			Obligations = new ArrayList<ObligationOrAdvice>();
		}
		return Obligations;
	}

	/**
	 * <p>
	 * Setter method for obligations
	 * </p>
	 *
	 * @param obligations
	 *            the obligations to set
	 */
	public void setObligations(List<ObligationOrAdvice> obligations) {
		Obligations = obligations;
	}

	/**
	 * <p>
	 * Getter method for associatedAdvice
	 * </p>
	 * 
	 * @return the associatedAdvice
	 */
	public List<ObligationOrAdvice> getAssociatedAdvice() {
		if (AssociatedAdvice == null) {
			AssociatedAdvice = new ArrayList<ObligationOrAdvice>();
		}
		return AssociatedAdvice;
	}

	/**
	 * <p>
	 * Setter method for associatedAdvice
	 * </p>
	 *
	 * @param associatedAdvice
	 *            the associatedAdvice to set
	 */
	public void setAssociatedAdvice(List<ObligationOrAdvice> associatedAdvice) {
		AssociatedAdvice = associatedAdvice;
	}

	/**
	 * <p>
	 * Getter method for attributes
	 * </p>
	 * 
	 * @return the attributes
	 */
	public List<Attribute> getAttributes() {
		if (Attributes == null) {
			Attributes = new ArrayList<Attribute>();
		}
		return Attributes;
	}

	/**
	 * <p>
	 * Getter method for policyIdentifierList
	 * </p>
	 * 
	 * @return the policyIdentifierList
	 */
	public List<PolicyIdentifier> getPolicyIdentifierList() {
		if (PolicyIdentifierList == null) {
			PolicyIdentifierList = new ArrayList<PolicyIdentifier>();
		}
		return PolicyIdentifierList;
	}

	/**
	 * <p>
	 * Setter method for policyIdentifierList
	 * </p>
	 *
	 * @param policyIdentifierList
	 *            the policyIdentifierList to set
	 */
	public void setPolicyIdentifierList(
			List<PolicyIdentifier> policyIdentifierList) {
		PolicyIdentifierList = policyIdentifierList;
	}

	/**
	 * <p>
	 * Setter method for attributes
	 * </p>
	 *
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(List<Attribute> attributes) {
		Attributes = attributes;
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
