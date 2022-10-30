/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.handlers;

import static com.nextlabs.evaluationconnector.utils.Constants.PDP_CONNECTOR_API_MODE;
import static com.nextlabs.evaluationconnector.utils.PropertiesUtil.getString;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.evaluationconnector.adaptors.PDPAdapter;
import com.nextlabs.evaluationconnector.adaptors.PDPAdaptorFactory;
import com.nextlabs.evaluationconnector.beans.PDPRequest;
import com.nextlabs.evaluationconnector.beans.ResponseStatusCode;
import com.nextlabs.evaluationconnector.beans.XACMLResponse;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.exceptions.InvalidInputException;
import com.nextlabs.evaluationconnector.parsers.XACMLParser;
import com.nextlabs.evaluationconnector.utils.Constants;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCodeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusType;

/**
 * <p>
 * AbstractEvalRequestHandler
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public abstract class AbstractEvalRequestHandler implements EvalRequestHandler {

    private static final Log log = LogFactory
                                   .getLog(AbstractEvalRequestHandler.class);

    /**
     * <p>
     * Template method to evaluate the PDP request.
     * </p>
     *
     * @param data
     *            xacml request string
     * @param xacmlParser
     *            respective xacml parser
     * @return {@link XACMLResponse}
     * @throws EvaluationConnectorException
     */
    protected XACMLResponse evaluateOnPDP(String data, XACMLParser xacmlParser)
        throws EvaluationConnectorException {
	
        XACMLResponse xacmlResponse = new XACMLResponse();
        try {
            List<PDPRequest> pdpRequests = xacmlParser.parseData(data);

            PDPAdapter pdpAdaptor = PDPAdaptorFactory
                                    .getInstance(getString(PDP_CONNECTOR_API_MODE));

            xacmlResponse = pdpAdaptor.evaluate(pdpRequests);

            if (log.isDebugEnabled())
                log.debug("Received the response from the PDP Request evaluation");

        } catch (InvalidInputException e) {
            log.warn("Invalid input for evaluate request On PDP,  ["
                     + e.getLocalizedMessage() + "] ");
            ResponseType responseType = getErrorResponse(
                e.getLocalizedMessage(), ResponseStatusCode.MISSING_ATTRIB);
            xacmlResponse.setResponseType(responseType);
            xacmlResponse.setStatus(ResponseStatusCode.MISSING_ATTRIB
                                    .getValue());

        } catch (EvaluationConnectorException e) {
            log.error("Error occurred during the evaluate request On PDP", e);
            ResponseType responseType = getErrorResponse(
                e.getLocalizedMessage(),
                ResponseStatusCode.PROCESSING_ERROR);
            xacmlResponse.setResponseType(responseType);
            xacmlResponse.setStatus(ResponseStatusCode.PROCESSING_ERROR
                                    .getValue());
        }
        return xacmlResponse;
    }

    protected ResponseType getErrorResponse(String errorMessage,
                                            ResponseStatusCode statusCode) {
        ResponseType response = new ResponseType();

        ResultType result = new ResultType();
        response.getResult().add(result);
        result.setDecision(DecisionType.INDETERMINATE);
        StatusType status = getResponseStatus(errorMessage, statusCode);
        result.setStatus(status);

        return response;
    }

    protected StatusType getResponseStatus(String errorMessage,
                                           ResponseStatusCode statusCode) {
        StatusType status = new StatusType();
        status.setStatusMessage(errorMessage);

        StatusCodeType codeType = new StatusCodeType();
        codeType.setValue(statusCode.getValue());
        status.setStatusCode(codeType);

        return status;
    }
    

    abstract String getDataTypeName();
    abstract XACMLParser getParser() throws EvaluationConnectorException;
    
    public XACMLResponse handle(String data) throws EvaluationConnectorException {
        log.debug(getDataTypeName() + " request came for evaluation");

        long startCounter = System.currentTimeMillis();

        XACMLResponse xacmlResponse = evaluateOnPDP(data, getParser());

        if (log.isDebugEnabled()) {
            log.debug(String.format("%s, Thread Id:[%s],  %s -> Total Request Evaluation Time : %d milis",
                                    Constants.PERF_LOG_PREFIX,
                                    "" + Thread.currentThread().getId(),
                                    getDataTypeName(),
                                    (System.currentTimeMillis() - startCounter)));
        
            log.debug("XACML Response came after " + getDataTypeName() + " request evaluation, [ Result status : " + xacmlResponse.getStatus() + " ]");
        }
    
        return xacmlResponse;
    }       

}
