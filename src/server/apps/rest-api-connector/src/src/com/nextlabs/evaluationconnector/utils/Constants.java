/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.utils;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.PDPApplication;

/**
 * <p>
 * Evalution Connector Constants
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public interface Constants {

	public static final String SERVICE_TYPE_PARAM = "Service";
	public static final String DATA_TYPE_PARAM = "DataType";
	public static final String XACML_DATA_PARAM = "data";
	public static final String VERSION_PARAM = "Version";

	public static final String CONTENT_TYPE_XML = "application/xml";
	public static final String CONTENT_TYPE_XACML_XML = "application/xacml+xml";
	public static final String CONTENT_TYPE_SAML_XACML = "application/saml+xacml";
	public static final String CONTENT_TYPE_JSON = "application/json";

	public static final String ENTRY_POINT_SERVICE = "GET_ENTRY_POINT";
	public static final String EVAL_REQ_SERVICE = "EVAL";
	public static final String JSON_DATA_TYPE = "json";
	public static final String XML_DATA_TYPE = "xml";
	public static final String SAML_DATA_TYPE = "saml";

	/**
	 * Properties file keys goes here
	 */
	public static final String PDP_CONNECTOR_API_MODE = "pdp.connector.api.mode";
	public static final String PDP_CONNECTOR_API_RMI_HOST = "pdp.connector.api.rmi.host";
	public static final String PDP_CONNECTOR_API_RMI_PORT = "pdp.connector.api.rmi.port";
    public static final String SAML_REQUEST_SKIP_SIGNATURE_CHECK = "saml.request.skip.signature.check";
    public static final String SAML_RESPONSE_KEYSTORE_FILENAME = "saml.keystore.filename";
    public static final String SAML_RESPONSE_KEYSTORE_ENTRY_ID = "saml.keystore.key_id";
    public static final String SAML_RESPONSE_KEYSTORE_PASSWORD = "saml.keystore.password";
    
	// Response Header Keys
	public static final String REST_API_RESPONSE_TIME = "rest_api_response_time";
	public static final String POLICY_EVAL_TIME = "policy_eval_time";

	// Performance Mode
	public static final String PERF_LOG_PREFIX = "\t PERFORMANCE ->>>> ";
	public static boolean IS_PERFORMANCE_MODE = false;
	
	public static final IPDPApplication PDP_APPLICATION_NONE = new PDPApplication("Unknown");

}
