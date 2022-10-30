/*
 * Created on Aug 11, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author sduan
 */
package com.nextlabs.openaz.pdp.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.openaz.xacml.api.Attribute;
import org.apache.openaz.xacml.api.AttributeValue;
import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.RequestAttributes;
import org.apache.openaz.xacml.api.RequestAttributesReference;
import org.apache.openaz.xacml.api.RequestReference;
import org.apache.openaz.xacml.api.XACML3;
import org.apache.openaz.xacml.std.IdentifierImpl;

import com.bluejungle.destiny.agent.pdpapi.IPDPHost;
import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.bluejungle.destiny.agent.pdpapi.PDPApplication;
import com.bluejungle.destiny.agent.pdpapi.PDPHost;
import com.bluejungle.destiny.agent.pdpapi.PDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.PDPResource;
import com.bluejungle.destiny.agent.pdpapi.PDPUser;
import com.nextlabs.openaz.pdp.beans.PDPRequest;
import com.nextlabs.openaz.pepapi.Application;
import com.nextlabs.openaz.pepapi.DiscretionaryPolicies;
import com.nextlabs.openaz.pepapi.Host;
import com.nextlabs.openaz.pepapi.Recipient;
import com.nextlabs.openaz.utils.Constants;

public class PDPRequestUtil {
	
	public static List<PDPRequest> convert(Request request) {
		if(request == null) {
			throw new IllegalArgumentException("null request received");
		}
		
		List<PDPRequest> pdpRequests = new ArrayList<PDPRequest>();
		
		// We don't support combinedDecisions and PolicyIdList
		if(request.getMultiRequests() == null || request.getMultiRequests().isEmpty()) {
			//single request
			pdpRequests.add(buildPDPRequest(request.getRequestAttributes()));
		} else {
			//multi-request
			Collection<RequestReference> requestReferences = request.getMultiRequests();
			for(RequestReference reqRef: requestReferences) {
				List<RequestAttributes> requestAttributes = new ArrayList<RequestAttributes>();
				for(RequestAttributesReference reqAttrsRef: reqRef.getAttributesReferences()) {
					requestAttributes.add(request.getRequestAttributesByXmlId(reqAttrsRef.getReferenceId()));
				}
				pdpRequests.add(buildPDPRequest(requestAttributes));
			}
		}
		return pdpRequests;
	}
	
	private static PDPRequest buildPDPRequest(Collection<RequestAttributes> requestAttributes) {
		if(requestAttributes == null) {
			throw new IllegalArgumentException("null RequestAttributes Collection received");
		}
		
		PDPRequest pdpRequest = new PDPRequest();
		
		for(RequestAttributes reqAttrs: requestAttributes) {
			mapAttributes(reqAttrs, pdpRequest);
		}
		return pdpRequest;
	}
	
	private static void mapAttributes(RequestAttributes requestAttributes, PDPRequest pdpRequest) {
		if(requestAttributes == null || pdpRequest == null) {
			throw new IllegalArgumentException("null RequestAttributes or PDPReqeust received");
		}
		
		if(requestAttributes.getCategory().equals(XACML3.ID_SUBJECT_CATEGORY_ACCESS_SUBJECT)) {
			// subject
			// id is required for subject
			String userId = getSingleAttributeValue(requestAttributes, XACML3.ID_SUBJECT_SUBJECT_ID);
			if(userId == null) {
				throw new IllegalStateException(String.format("Can't get %s value of subject Attributes",
						XACML3.ID_SUBJECT_SUBJECT_ID.stringValue()));
			} else {
				// create a user and set to pdpRequest
				IPDPUser pdpUser = new PDPUser(userId);
				// add other attributes to the pdpUser except the ID
				List<String> keysToSkip = Arrays.asList(new String[] { XACML3.ID_SUBJECT_SUBJECT_ID.stringValue() });
				addNamedAttributes(pdpUser, requestAttributes, keysToSkip);
				pdpRequest.setUser(pdpUser);
			}
		} else if(requestAttributes.getCategory().equals(XACML3.ID_ATTRIBUTE_CATEGORY_ACTION)) {
			// action
			// we only need action String value
			String action = getSingleAttributeValue(requestAttributes, XACML3.ID_ACTION_ACTION_ID);
			if(action == null) {
				throw new IllegalStateException(String.format("Can't get %s value of action Attributes",
						XACML3.ID_ACTION_ACTION_ID.stringValue()));
			} else {
				// set action string to pdpRequest
				pdpRequest.setAction(action);
			}
		} else if(requestAttributes.getCategory().equals(XACML3.ID_ATTRIBUTE_CATEGORY_RESOURCE)) {
			// resource (from)
			// we need to get resource ID and resource Type
			String resourceId = getSingleAttributeValue(requestAttributes, XACML3.ID_RESOURCE_RESOURCE_ID);
			if(resourceId == null) {
				throw new IllegalStateException(String.format("Can't get %s value of resource Attributes",
						XACML3.ID_RESOURCE_RESOURCE_ID.stringValue()));
			}
			
			String resourceType = getSingleAttributeValue(requestAttributes, new IdentifierImpl(PDPRequest.DESTINY_TYPE_KEY));
			if(resourceType == null) {
				// default value for resourceType if not specified
				resourceType = "object";
			}
			
			IPDPResource pdpResource = new PDPResource("from", resourceId, resourceType);
			// add other attributes to the pdpResource except ID and resourceType
			List<String> keysToSkip = Arrays.asList(new String[] { XACML3.ID_RESOURCE_RESOURCE_ID.stringValue(),
					PDPRequest.DESTINY_TYPE_KEY });
			addNamedAttributes(pdpResource, requestAttributes, keysToSkip);
			pdpRequest.setResource(pdpResource);
		} else if(requestAttributes.getCategory().equals(Host.CATEGORY_ID)) {
			// host
			String hostname = getSingleAttributeValue(requestAttributes, Constants.ID_NEXTLABS_HOST_NAME);
			String ipAddr = getSingleAttributeValue(requestAttributes, Constants.ID_NEXTLABS_HOST_IP_ADDR);
			
			IPDPHost pdpHost;
			if(ipAddr != null) {
				pdpHost = new PDPHost(new Integer(ipAddr));
			} else if(hostname != null) {
				pdpHost = new PDPHost(hostname);
			} else {
				// use localhost
				pdpHost = new PDPHost(Host.LOCAL_HOST);
			}
			
			List<String> keysToSkip = Arrays.asList(new String[] { Constants.ID_NEXTLABS_HOST_NAME.stringValue(),
					Constants.ID_NEXTLABS_HOST_IP_ADDR.stringValue() });
			addNamedAttributes(pdpHost, requestAttributes, keysToSkip);
			pdpRequest.setHost(pdpHost);
		} else if(requestAttributes.getCategory().equals(Application.CATEGORY_ID)) {
			// application
			String applicationId = getSingleAttributeValue(requestAttributes, Constants.ID_NEXTLABS_APPLICATION_APPLICATION_ID);
			if(applicationId == null) {
				// use null if the ID is not specified
				applicationId = "null";
			}
			pdpRequest.setApplication(new PDPApplication(applicationId));
		} else if(requestAttributes.getCategory().equals(DiscretionaryPolicies.CATEGORY_ID)) {
			// pql
			String pqlString = getSingleAttributeValue(requestAttributes, Constants.ID_NEXTLABS_POD_POD_ID);
			if(pqlString != null) {
				String ignoredefault = getSingleAttributeValue(requestAttributes, Constants.ID_NEXTLABS_POD_IGNORE_BUILT_IN);
				if(ignoredefault != null && ignoredefault.equals("true")) {
					ignoredefault = "yes";
				} else {
					ignoredefault = "no";
				}
				IPDPNamedAttributes pql = new PDPNamedAttributes("policies");
				pql.setAttribute("pql", pqlString);
				pql.setAttribute("ignoredefault", ignoredefault);
				pdpRequest.addNamedAttributes(pql);
			}
		} else if(requestAttributes.getCategory().equals(XACML3.ID_ATTRIBUTE_CATEGORY_ENVIRONMENT)) {
			// environment
			IPDPNamedAttributes environment = new PDPNamedAttributes("environment");
			addNamedAttributes(environment, requestAttributes, Collections.<String>emptyList());
			pdpRequest.addNamedAttributes(environment);
		} else if(requestAttributes.getCategory().equals(Recipient.CATEGORY_ID)) {
			// recipient
			IPDPNamedAttributes recipient = new PDPNamedAttributes("sendto");
			String recipientId = getSingleAttributeValue(requestAttributes, Constants.ID_NEXTLABS_RECIPIENT_RECIPIENT_ID);
			String[] recipientEmails = getMultiAttributeValue(requestAttributes, Constants.ID_NEXTLABS_RECIPIENT_RECIPIENT_EMAIL);
			if(recipientId != null) {
				recipient.setAttribute("id", recipientId);
			}
			if(recipientEmails != null) {
				for(String email: recipientEmails) {
					recipient.setAttribute("email", email);
				}
			}
			List<String> keysToSkip = Arrays.asList(new String[] { Constants.ID_NEXTLABS_RECIPIENT_RECIPIENT_ID.stringValue(),
					Constants.ID_NEXTLABS_RECIPIENT_RECIPIENT_EMAIL.stringValue() });
			addNamedAttributes(recipient, requestAttributes, keysToSkip);
			pdpRequest.addNamedAttributes(recipient);
		} else {
			// other things, we can add them to additionalData
			IPDPNamedAttributes additionalData = new PDPNamedAttributes(requestAttributes.getCategory().stringValue());
			addNamedAttributes(additionalData, requestAttributes, Collections.<String>emptyList());
			pdpRequest.addNamedAttributes(additionalData);
		}
	}
	
	private static String getSingleAttributeValue(RequestAttributes requestAttributes, Identifier identifier) {
		if(requestAttributes == null || identifier == null) {
			throw new IllegalArgumentException("null requestAttributes or identifier received");
		}
		Iterator<Attribute> attributeIter = requestAttributes.getAttributes(identifier);
		if(attributeIter.hasNext()) {
			Collection<AttributeValue<?>> attrValue = attributeIter.next().getValues();
			if(!attrValue.isEmpty()) {
				return attrValue.iterator().next().getValue().toString();
			}
		}
		return null;
	}
	
	private static String[] getMultiAttributeValue(RequestAttributes requestAttributes, Identifier identifier) {
		if(requestAttributes == null || identifier == null) {
			throw new IllegalArgumentException("null requestAttributes or identifier received");
		}
		Iterator<Attribute> attributeIter = requestAttributes.getAttributes(identifier);
		if(attributeIter.hasNext()) {
			Collection<AttributeValue<?>> attrValues = attributeIter.next().getValues();
			List<String> returnValues = new ArrayList<String>();
			for(AttributeValue<?> attrValue: attrValues) {
				returnValues.add(attrValue.getValue().toString());
			}
			return (String[]) returnValues.toArray();
		}
		return null;
	}
	
	private static void addNamedAttributes(IPDPNamedAttributes dest, RequestAttributes source, List<String> keysToSkip) {
		if(source == null || dest == null || keysToSkip == null) {
			throw new IllegalArgumentException("null source or dest or keysToSkip received");
		}
		
		Collection<Attribute> attributes = source.getAttributes();
		for(Attribute attr: attributes) {
			String key = attr.getAttributeId().stringValue();
			if(!keysToSkip.contains(key)) {
				Collection<AttributeValue<?>> values = attr.getValues();
				for(AttributeValue<?> attrValue: values) {
					//PDPSDK only deals with String value
					dest.setAttribute(key, attrValue.getValue().toString());
				}
			}
		}
	}
	
}
