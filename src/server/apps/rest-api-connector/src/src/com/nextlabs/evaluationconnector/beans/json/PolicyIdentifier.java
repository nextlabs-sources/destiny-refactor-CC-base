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

/**
 * <p>
 * PolicyIdentifier
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class PolicyIdentifier implements Serializable {

	private static final long serialVersionUID = -6410414233276069504L;

	private List<IdReference> PolicyIdReference;
	private List<IdReference> PolicySetIdReference;

	/**
	 * <p>
	 * Getter method for policyIdReference
	 * </p>
	 * 
	 * @return the policyIdReference
	 */
	public List<IdReference> getPolicyIdReference() {
		if (PolicyIdReference == null) {
			PolicyIdReference = new ArrayList<IdReference>();
		}
		return PolicyIdReference;
	}

	/**
	 * <p>
	 * Setter method for policyIdReference
	 * </p>
	 *
	 * @param policyIdReference
	 *            the policyIdReference to set
	 */
	public void setPolicyIdReference(List<IdReference> policyIdReference) {
		PolicyIdReference = policyIdReference;
	}

	/**
	 * <p>
	 * Getter method for policySetIdReference
	 * </p>
	 * 
	 * @return the policySetIdReference
	 */
	public List<IdReference> getPolicySetIdReference() {
		if (PolicySetIdReference == null) {
			PolicySetIdReference = new ArrayList<IdReference>();
		}
		return PolicySetIdReference;
	}

	/**
	 * <p>
	 * Setter method for policySetIdReference
	 * </p>
	 *
	 * @param policySetIdReference
	 *            the policySetIdReference to set
	 */
	public void setPolicySetIdReference(List<IdReference> policySetIdReference) {
		PolicySetIdReference = policySetIdReference;
	}

}
