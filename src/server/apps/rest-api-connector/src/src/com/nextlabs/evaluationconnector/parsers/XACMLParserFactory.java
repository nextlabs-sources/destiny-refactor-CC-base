/*
 * Created on Jan 21, 2015
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
 * This will create relevant XACMLParser for the given data type.
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public final class XACMLParserFactory {

    private static final Log log = LogFactory.getLog(XACMLParserFactory.class);

    private static XmlXACMLParser xmlXACMLParser;
    private static JsonXACMLParser jsonXACMLParser;
    private static SamlXACMLParser samlXACMLParser;

    /**
     * <p>
     * Get instance of respective XACML Parser
     * </p>
     *
     * @param dataType
     *            response data type
     * @return {@link XACMLParser}
     * @throws EvaluationConnectorException
     */
    public static synchronized XACMLParser getInstance(String dataType)
        throws EvaluationConnectorException {
        if (Constants.JSON_DATA_TYPE.equals(dataType)) {
            if (jsonXACMLParser == null) {
                jsonXACMLParser = new JsonXACMLParser();
                jsonXACMLParser.init();
                log.info("JSON XACML Parser created");
            }
            return jsonXACMLParser;
        } else if (Constants.XML_DATA_TYPE.equals(dataType)) {
            if (xmlXACMLParser == null) {
                xmlXACMLParser = new XmlXACMLParser();
                xmlXACMLParser.init();
                log.info("XML XACML Parser created");
            }
            return xmlXACMLParser;
        } else if (Constants.SAML_DATA_TYPE.equals(dataType)) {
            if (samlXACMLParser == null) {
                samlXACMLParser = new SamlXACMLParser();
                samlXACMLParser.init();
                log.info("SAML XACML Parser created");
            }
            return samlXACMLParser;
        } else {
            throw new EvaluationConnectorException("Unable to find parser for " + dataType);
        }
    }
}
