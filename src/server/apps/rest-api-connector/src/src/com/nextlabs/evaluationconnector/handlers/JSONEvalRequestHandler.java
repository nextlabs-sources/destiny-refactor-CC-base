/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.handlers;

import static com.nextlabs.evaluationconnector.utils.Constants.JSON_DATA_TYPE;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.parsers.XACMLParser;
import com.nextlabs.evaluationconnector.parsers.XACMLParserFactory;

/**
 * <p>
 * JSONEvalRequestHandler
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class JSONEvalRequestHandler extends AbstractEvalRequestHandler {

    private static final Log log = LogFactory.getLog(JSONEvalRequestHandler.class);

    String getDataTypeName() {
        return "JSON";
    }

    XACMLParser getParser() throws EvaluationConnectorException {
        return XACMLParserFactory.getInstance(JSON_DATA_TYPE);
    }
}
