/*
 * Created on Jan 20, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.parsers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.utils.Constants;

/**
 * <p>
 * EvalResponseFactory
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public final class EvalResponseFactory {
    private static final Log log = LogFactory.getLog(EvalResponseFactory.class);

    private static JSONEvalResponse jsonResponse;
    private static XMLEvalResponse xmlResponse;
    private static SAMLEvalResponse samlResponse;

    /**
     * <p>
     * Get instance of respective Eval Response
     * </p>
     *
     * @param dataType
     *            response data type
     * @return {@link EvalResponse}
     * @throws EvaluationConnectorException
     */
    public synchronized static EvalResponse getInstance(String dataType) throws EvaluationConnectorException {
        if (Constants.JSON_DATA_TYPE.equals(dataType)) {
            if (jsonResponse == null) {
                jsonResponse = new JSONEvalResponse();
                jsonResponse.init();
                log.debug("JSON Response created");
            }
            return jsonResponse;
        } else if (Constants.XML_DATA_TYPE.equals(dataType)) {
            if (xmlResponse == null) {
                xmlResponse = new XMLEvalResponse();
                xmlResponse.init();
                log.debug("XML Response created");
            }
            return xmlResponse;
        } else if (Constants.SAML_DATA_TYPE.equals(dataType)) {
            if (samlResponse == null) {
                samlResponse = new SAMLEvalResponse();
                samlResponse.init();
                log.debug("XML Response created");
            }
            return samlResponse;
        }
                
        return null;
    }
}
