/*
 * Created on Dec 16, 2015
 *
 * All sources, binaries and HTML pages (C) copyright 2015 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/rest-api-connector/src/src/com/nextlabs/evaluationconnector/handlers/SAMLEvalRequestHandler.java#1 $:
 */

package com.nextlabs.evaluationconnector.handlers;

import static com.nextlabs.evaluationconnector.utils.Constants.SAML_DATA_TYPE;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.parsers.XACMLParser;
import com.nextlabs.evaluationconnector.parsers.XACMLParserFactory;

public class SAMLEvalRequestHandler extends AbstractEvalRequestHandler {
    private static final Log log = LogFactory.getLog(SAMLEvalRequestHandler.class);

    String getDataTypeName() {
        return "SAML";
    }
    
    XACMLParser getParser() throws EvaluationConnectorException {
        return XACMLParserFactory.getInstance(SAML_DATA_TYPE);
    }
}
