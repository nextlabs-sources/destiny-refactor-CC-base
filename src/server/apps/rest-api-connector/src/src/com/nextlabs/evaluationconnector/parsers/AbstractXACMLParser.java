/*
 * Created on Jan 27, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPHost;
import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.bluejungle.destiny.agent.pdpapi.PDPApplication;
import com.bluejungle.destiny.agent.pdpapi.PDPHost;
import com.bluejungle.destiny.agent.pdpapi.PDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.PDPResource;
import com.bluejungle.destiny.agent.pdpapi.PDPUser;

/**
 * <p>
 * AbstractXACMLParser
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public abstract class AbstractXACMLParser implements XACMLParser {

	private static final Log log = LogFactory.getLog(AbstractXACMLParser.class);

	protected IPDPUser createUser(Map<String, List<String>> attributeMap) {
		List<String> keysToSkip = new ArrayList<String>();
		keysToSkip.add(ATTRIBUTE_ID_USER_ID);

		String userId = getAttributeIdValue(ATTRIBUTE_ID_USER_ID, attributeMap);
		IPDPUser user = new PDPUser(userId);
		addAttributes(user, attributeMap, keysToSkip);
		return user;
	}

	/**
	 * <p>
	 * 
	 * </p>
	 *
	 * @param attributeMap
	 * @return
	 */
	protected IPDPResource createResource(Map<String, List<String>> attributeMap) {

		String resourceId = getAttributeIdValue(ATTRIBUTE_ID_RESOURCE_ID, attributeMap);

		String resourceType = getAttributeIdValue(ATTRIBUTEID_RESOURCETYPE, attributeMap);
		
		String resourceDimension = getAttributeIdValue(ATTRIBUTEID_RESOURCE_DIMENSION, attributeMap);
		if (resourceDimension == null) {
			resourceDimension = ATTRIBUTE_ID_RESOURCE_DIMENSION_FROM;
		}
		
		if (log.isDebugEnabled())
			log.debug("Resource Attribute values, [ Dimension : " + resourceDimension + ", Resource Id : " + resourceId
					+ ", Type:" + resourceType + "]");

		IPDPResource resource = new PDPResource(resourceDimension, resourceId, resourceType);

		List<String> keysToSkip = new ArrayList<String>();
		keysToSkip.add(ATTRIBUTE_ID_RESOURCE_ID);
		keysToSkip.add(ATTRIBUTEID_RESOURCETYPE);

		addAttributes(resource, attributeMap, keysToSkip);
		return resource;
	}

	/**
	 * <p>
	 * 
	 * </p>
	 *
	 * @param attributeMap
	 * @return
	 */
	protected IPDPApplication createApplication(Map<String, List<String>> attributeMap) {
		List<String> keysToSkip = new ArrayList<String>();
		keysToSkip.add(ATTRIBUTE_APPLICATION_ID);

		String appId = getAttributeIdValue(ATTRIBUTE_APPLICATION_ID, attributeMap);
		IPDPApplication application = new PDPApplication(appId);
		addAttributes(application, attributeMap, keysToSkip);		
		return application;
	}
	
	protected IPDPNamedAttributes createPDPNamedAttributes(Map<String, List<String>> attributeMap , String name) {
		List<String> keysToSkip = new ArrayList<String>();
		
		IPDPNamedAttributes namedAttribute = new PDPNamedAttributes(name);
		addAttributes(namedAttribute, attributeMap, keysToSkip);		
		return namedAttribute;
	}
	
	
	protected IPDPHost createHost(Map<String, List<String>> attributeMap) {
		List<String> keysToSkip = new ArrayList<String>();
		keysToSkip.add(ATTRIBUTE_ID_IP_ADDR);

		String inetAddr = getAttributeIdValue(ATTRIBUTE_ID_IP_ADDR, attributeMap);
		int ipAddress = convertIpToInt(inetAddr);
		IPDPHost host = new PDPHost(ipAddress);
		addAttributes(host, attributeMap, keysToSkip);
		
		return host;
	}
	
	private int convertIpToInt(String address) {
		int result = 0;
	    for(String part : address.split(Pattern.quote("."))) {
	        // shift the previously parsed bits over by 1 byte
	        result = result << 8;
	        // set the low order bits to the current octet
	        result |= Integer.parseInt(part);
	    }
	    return result;
	}
	
	private String getAttributeIdValue(String attributeId, Map<String, List<String>> attributeMap) {
		String attrIdVal = null;
		if (attributeMap.get(attributeId) != null && !(attributeMap.get(attributeId).isEmpty())) {
			attrIdVal = attributeMap.get(attributeId).get(0);
		}
		return attrIdVal;
	}
	
	protected static void addAttributes(IPDPNamedAttributes namedAttributes, Map<String, List<String>> attributeMap,
			List<String> keysToSkip) {
		for (String key : attributeMap.keySet()) {
			if (key != null && !keysToSkip.contains(key)) {
				List<String> values = attributeMap.get(key);
				if (values != null && !values.isEmpty()) {
					for (String value : values) {
						String simpleKey = getSimplifiedAttribName(key);
						namedAttributes.setAttribute(simpleKey, value);
					}
				}
			}
		}
	}

	private static String getSimplifiedAttribName(String attributeName) {
		String attrName = attributeName;
		int index = attributeName.lastIndexOf(':');

		if (index != -1) {
			attrName = attributeName.substring(index + 1);
			if (XACMLParser.ATTRIBUTE_RESOURCE_NO_CACHE.equals(attrName)) {
				attrName = XACMLParser.ATTRIBUTE_PREFIX_CE + attrName;
			}
		}
		return attrName;
	}
	
	/**
	 * Resource List is valid if min size is 1 and max size is 2 If the list has
	 * 2 resources, permissible values for resource-dimension attributes are
	 * "to" and "from" and both should not be equal
	 * 
	 * @param resList
	 * @return true/false
	 */
	protected boolean isResourceListValid(List<IPDPResource> resList) {
		boolean isValid = true;

		if (resList.size() > 2) {
			isValid = false;
		}
		if (resList.size() == 2) {
			if ((resList.get(0).getName()).equalsIgnoreCase(resList.get(1).getName())) {
				isValid = false;
			}
			if ((resList.get(0).getName().equalsIgnoreCase(ATTRIBUTE_ID_RESOURCE_DIMENSION_TO)
					|| resList.get(0).getName().equalsIgnoreCase(ATTRIBUTE_ID_RESOURCE_DIMENSION_FROM))
					&& (resList.get(1).getName().equalsIgnoreCase(ATTRIBUTE_ID_RESOURCE_DIMENSION_TO)
							|| resList.get(1).getName().equalsIgnoreCase(ATTRIBUTE_ID_RESOURCE_DIMENSION_FROM))) {
				isValid = true;
			} else {
				isValid = false;
			}
		}
		return isValid;
	}

	/**
	 * IP Address is mandatory for a Host
	 * 
	 * @param attributeMap
	 * @return
	 */
	protected boolean isHostValid(Map<String, List<String>> attributeMap) {

		if (getAttributeIdValue(ATTRIBUTE_ID_IP_ADDR, attributeMap) != null) {
			return true;
		} else {
			return false;
		}
	}
}
