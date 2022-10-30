/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.parsers;

import java.util.List;

import com.nextlabs.evaluationconnector.beans.PDPRequest;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.exceptions.InvalidInputException;

/**
 * <p>
 * XACMLParser
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public interface XACMLParser {

	public static final String CATEGORY_SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
	public static final String CATEGORY_RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
	public static final String CATEGORY_ACTION = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
	public static final String CATEGORY_ENVIRONMENT = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";
	public static final String CATEGORY_APPLICATION = "urn:nextlabs:names:evalsvc:1.0:attribute-category:application";
	public static final String CATEGORY_POLICY_ON_DEMAND = "urn:nextlabs:names:evalsvc:1.0:attribute-category:pod";
	public static final String CATEGORY_HOST = "urn:nextlabs:names:evalsvc:1.0:attribute-category:host";
	public static final String CATEGORY_RECIPIENT = "urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject";
	public static final String CATEGORY_EXTRA_ATTRIBUTES_PREFIX = "urn:nextlabs:names:evalsvc:1.0:attribute-category:environment";
	public static final String CATEGORY_OBLIGATION_ATTR_ASSIGNMENT = "urn:nextlabs:names:evalsvc:1.0:attribute-category:obligation-attrAssignment";

	public static final String ATTRIBUTE_ID_USER_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
	public static final String ATTRIBUTE_ID_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	public static final String ATTRIBUTE_ID_RESOURCE_ID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	public static final String ATTRIBUTEID_RESOURCETYPE = "urn:nextlabs:names:evalsvc:1.0:resource:resource-type";
	public static final String ATTRIBUTEID_RESOURCE_DIMENSION = "urn:nextlabs:names:evalsvc:1.0:resource:resource-dimension";
	public static final String ATTRIBUTE_ID_RESOURCE_DIMENSION_FROM = "from";
	public static final String ATTRIBUTE_RESOURCE_NO_CACHE = "nocache";
	public static final String ATTRIBUTE_PREFIX_CE = "ce::";
	public static final String ATTRIBUTE_ID_RESOURCE_DIMENSION_TO = "to";
	public static final String ATTRIBUTE_RESOURCE_TYPE_DEFAULTTYPE = "ec-us";
	public static final String ATTRIBUTE_APPLICATION_ID = "urn:nextlabs:names:evalsvc:1.0:application:application-id";
	public static final String ATTRIBUTE_POD_ID = "urn:nextlabs:names:evalsvc:1.0:pod:pod-id";
	public static final String ATTRIBUTE_POD_IGNORE_BUILT_IN = "urn:nextlabs:names:evalsvc:1.0:pod:pod-ignore-built-in";
	public static final String ATTRIBUTE_ID_HOST_NAME = "urn:nextlabs:names:evalsvc:1.0:host:name";
	public static final String ATTRIBUTE_ID_IP_ADDR = "urn:nextlabs:names:evalsvc:1.0:host:inet_address";
	public static final String ATTRIBUTE_ID_RECIPIENT_EMAIL = "urn:nextlabs:names:evalsvc:1.0:recipient:email";
	public static final String ATTRIBUTE_ID_RECIPIENT_ID = "urn:nextlabs:names:evalsvc:1.0:recipient:id";
	public static final String NAMED_ATTRIBUTE_ENVIRONMENT_NAME = "environment";
	public static final String NAMED_ATTRIBUTE_RECIPIENT_NAME = "sendto";
	
	public static final String XACML_STRING_DATA_TYPE = "http://www.w3.org/2001/XMLSchema#string";
	
	/**
	 * <p>
	 * Parser initialization
	 * </p>
	 *
	 * @throws EvaluationConnectorException
	 *             throws in any error
	 */
	void init() throws EvaluationConnectorException;

	/**
	 * <p>
	 * Parse Data according to XACML 3.0 standards
	 * </p>
	 *
	 * @param data
	 *            request data
	 * @return {@link PDPRequest}
	 * @throws EvaluationConnectorException
	 *             throws in any error
	 */
	List<PDPRequest> parseData(String data) throws InvalidInputException,
			EvaluationConnectorException;

}
