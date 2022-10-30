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

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * <p>
 * Status
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class Status implements Serializable {

	private static final long serialVersionUID = -7140712317132403853L;

	private String StatusMessage;
	private String StatusDetail;
	private StatusCode StatusCode;

	/**
	 * <p>
	 * Getter method for statusMessage
	 * </p>
	 * 
	 * @return the statusMessage
	 */
	public String getStatusMessage() {
		return StatusMessage;
	}

	/**
	 * <p>
	 * Setter method for statusMessage
	 * </p>
	 *
	 * @param statusMessage
	 *            the statusMessage to set
	 */
	public void setStatusMessage(String statusMessage) {
		StatusMessage = statusMessage;
	}

	/**
	 * <p>
	 * Getter method for statusDetail
	 * </p>
	 * 
	 * @return the statusDetail
	 */
	public String getStatusDetail() {
		return StatusDetail;
	}

	/**
	 * <p>
	 * Setter method for statusDetail
	 * </p>
	 *
	 * @param statusDetail
	 *            the statusDetail to set
	 */
	public void setStatusDetail(String statusDetail) {
		StatusDetail = statusDetail;
	}

	/**
	 * <p>
	 * Getter method for statusCode
	 * </p>
	 * 
	 * @return the statusCode
	 */
	public StatusCode getStatusCode() {
		return StatusCode;
	}

	/**
	 * <p>
	 * Setter method for statusCode
	 * </p>
	 *
	 * @param statusCode
	 *            the statusCode to set
	 */
	public void setStatusCode(StatusCode statusCode) {
		StatusCode = statusCode;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
