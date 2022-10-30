/*
 * Created on Jul 8, 2014
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import java.io.Serializable;


/**
 * <p>
 * Entity for ReportActivityLogAttributeDO
 * </p>
 * 
 * 
 * @author Amila Silva
 * 
 */
public class ReportActivityLogAttributeDO implements Serializable{

	private Long attributeId;
	private Long policyLogId;
	private String attrValue;


	/**
	 * <p>
	 * Getter method for attributeId
	 * </p>
	 * 
	 * @return the attributeId
	 */
	public Long getAttributeId() {
		return attributeId;
	}

	/**
	 * <p>
	 * Setter method for attributeId
	 * </p>
	 * 
	 * @param attributeId
	 *            the attributeId to set
	 */
	public void setAttributeId(Long attributeId) {
		this.attributeId = attributeId;
	}

	/**
	 * <p>
	 * Getter method for policyLogId
	 * </p>
	 * 
	 * @return the policyLogId
	 */
	public Long getPolicyLogId() {
		return policyLogId;
	}

	/**
	 * <p>
	 * Setter method for policyLogId
	 * </p>
	 * 
	 * @param policyLogId
	 *            the policyLogId to set
	 */
	public void setPolicyLogId(Long policyLogId) {
		this.policyLogId = policyLogId;
	}

	/**
	 * <p>
	 * Getter method for attrValue
	 * </p>
	 * 
	 * @return the attrValue
	 */
	public String getAttrValue() {
		return attrValue;
	}

	/**
	 * <p>
	 * Setter method for attrValue
	 * </p>
	 * 
	 * @param attrValue
	 *            the attrValue to set
	 */
	public void setAttrValue(String attrValue) {
		this.attrValue = attrValue;
	}
	
	@Override
	public boolean equals(Object logAttribute)
	{
        boolean valueToReturn = false;

        if (logAttribute == this) {
            valueToReturn = true;
        } else if ((logAttribute != null) && ((logAttribute instanceof ReportActivityLogAttributeDO))) {
        	ReportActivityLogAttributeDO temp = (ReportActivityLogAttributeDO) logAttribute;
            valueToReturn = this.policyLogId == temp.getPolicyLogId() && this.attributeId == temp.getAttributeId();
        }

        return valueToReturn;
	}
	
	@Override
	public int hashCode()
	{
		int hash = 17;
	    hash = hash * 31 + policyLogId.hashCode();
	    hash = hash * 31 + attributeId.hashCode();
	    return hash;
	}
}
