/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.beans;

import java.io.Serializable;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AdviceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusType;

/**
 * <p>
 * XACMLResponse - XACML 3.0 compliance response bean
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class XACMLResponse implements Serializable {

    private static final long serialVersionUID = -6977956319956001974L;
    private String status;

    // for XML
    private ResponseType responseType;

    private Long requestStartTime = 0L;
    private Long pdpEvalTime = 0L;

    /**
     * <p>
     * Getter method for status
     * </p>
     * 
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * <p>
     * Setter method for status
     * </p>
     *
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * <p>
     * Getter method for responseType
     * </p>
     * 
     * @return the responseType
     */
    public ResponseType getResponseType() {
        return responseType;
    }

    /**
     * <p>
     * Setter method for responseType
     * </p>
     *
     * @param responseType
     *            the responseType to set
     */
    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    /**
     * @return the requestStartTime
     */
    public Long getRequestStartTime() {
        return requestStartTime;
    }

    /**
     * @param requestStartTime the requestStartTime to set
     */
    public void setRequestStartTime(Long requestStartTime) {
        this.requestStartTime = requestStartTime;
    }

    /**
     * @return the pdpEvalTime
     */
    public Long getPdpEvalTime() {
        return pdpEvalTime;
    }

    /**
     * @param pdpEvalTime the pdpEvalTime to set
     */
    public void setPdpEvalTime(Long pdpEvalTime) {
        this.pdpEvalTime = pdpEvalTime;
    }

}
