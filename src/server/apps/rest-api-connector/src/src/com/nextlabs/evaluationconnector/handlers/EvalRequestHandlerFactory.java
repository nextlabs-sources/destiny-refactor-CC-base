/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.handlers;

import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.utils.Constants;

/**
 * <p>
 * EvalRequestHandlerFactory
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class EvalRequestHandlerFactory {
    public static EvalRequestHandler createHandler(String dataType) throws EvaluationConnectorException {
        
        if (dataType.equalsIgnoreCase(Constants.JSON_DATA_TYPE)) {
            return new JSONEvalRequestHandler();
        } else if (dataType.equalsIgnoreCase(Constants.XML_DATA_TYPE)) {
            return new XMLEvalRequestHandler();
        } else if (dataType.equalsIgnoreCase(Constants.SAML_DATA_TYPE)) {
            return new SAMLEvalRequestHandler();
        } else
            throw new EvaluationConnectorException("No Handler Found for Data type " + dataType + ".");
    }
}
