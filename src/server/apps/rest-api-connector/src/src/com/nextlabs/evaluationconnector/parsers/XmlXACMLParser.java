/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.parsers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPHost;
import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.bluejungle.destiny.agent.pdpapi.PDPApplication;
import com.nextlabs.evaluationconnector.beans.PDPRequest;
import com.nextlabs.evaluationconnector.beans.PolicyOnDemand;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.exceptions.InvalidInputException;
import com.nextlabs.evaluationconnector.utils.Constants;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MultiRequestsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;

/**
 * <p>
 * XmlXACMLParser
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class XmlXACMLParser extends AbstractXACMLParser implements XACMLParser {

	private static final Log log = LogFactory.getLog(XmlXACMLParser.class);

	private JAXBContext jaxbContext =  null;

	public void init() throws EvaluationConnectorException {
		getJAXBContext(); 
	}

	@Override
	public List<PDPRequest> parseData(String data) throws InvalidInputException,
			EvaluationConnectorException {
		
		try {
			Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
			long startCounter = System.currentTimeMillis();
			RequestType rootElement = (RequestType) JAXBIntrospector
					.getValue(unmarshaller.unmarshal(new StringReader(data)));

			if (log.isDebugEnabled())
				log.debug(String
						.format("%s, Thread Id:[%s],  JsonXACMLParser -> Time taken to Request Object mapping : %d milis",
								Constants.PERF_LOG_PREFIX, ""
										+ Thread.currentThread().getId(),
								(System.currentTimeMillis() - startCounter)));

			List<PDPRequest> pdpRequests = readElements(rootElement);
			
			return pdpRequests;
		} catch (IllegalArgumentException e) {
			throw new InvalidInputException(e.getLocalizedMessage());
		} catch (Throwable e) {
			throw new EvaluationConnectorException(
					"Error occurred in XML data parsing. [ "
							+ e.getLocalizedMessage() + " ]", e);
		}
	}

	private List<PDPRequest> readElements(RequestType rootElement) throws InvalidInputException {

		if (log.isDebugEnabled())
			log.debug("Read Element data and assign to PDP Request");

		List<PDPRequest> pdpRequests = new ArrayList<PDPRequest>();

		List<AttributesType> attributes = rootElement.getAttributes();
		MultiRequestsType multiRequests = rootElement.getMultiRequests();

		if (multiRequests != null && !multiRequests.getRequestReference().isEmpty()) {
			Map<String, AttributesType> attributesTypeMap = getAttributesTypeMap(attributes);

			List<RequestReferenceType> requestReferences = multiRequests.getRequestReference();

			for (RequestReferenceType requestReference : requestReferences) {
				List<AttributesType> reqAttributes = new ArrayList<AttributesType>();

				List<AttributesReferenceType> attributesReferences = requestReference.getAttributesReference();
				for (AttributesReferenceType attributesReference : attributesReferences) {
					AttributesType attributesRefType = (AttributesType) attributesReference.getReferenceId();
					String referenceId = attributesRefType.getId();
					reqAttributes.add(attributesTypeMap.get(referenceId));
				}
				PDPRequest pdpRequest = populatePDPRequest(reqAttributes);
				pdpRequests.add(pdpRequest);
			}
			
		} else {
			PDPRequest pdpRequest = populatePDPRequest(attributes);
			pdpRequests.add(pdpRequest);
		}
		return pdpRequests;
	}

	private Map<String, AttributesType> getAttributesTypeMap(List<AttributesType> attributes) {
		Map<String, AttributesType> attributesTypeMap = new HashMap<String, AttributesType>();
		for (AttributesType attributesType : attributes) {
			attributesTypeMap.put(attributesType.getId(), attributesType);
		}
		return attributesTypeMap;
	}
	
	private PDPRequest populatePDPRequest(List<AttributesType> attributes) throws InvalidInputException {

		PDPRequest pdpRequest = new PDPRequest();

		List<IPDPResource> resList = new ArrayList<IPDPResource>();
		String action = null;
		IPDPUser user = null;
		IPDPHost host = null;
		List<String> recipients = new ArrayList<String>();
		IPDPApplication application = Constants.PDP_APPLICATION_NONE;
		PolicyOnDemand policyOnDemand = new PolicyOnDemand();
		List<IPDPNamedAttributes> additionalAttrs = new ArrayList<IPDPNamedAttributes>();
		IPDPNamedAttributes environment = null;
		List<AttributesType> otherAttributes = new ArrayList<AttributesType>();

		for (AttributesType attribute : attributes) {
			String category = attribute.getCategory();
			if (CATEGORY_SUBJECT.equalsIgnoreCase(category)) {
				user = getUser(attribute);
			} else if (CATEGORY_RESOURCE.equalsIgnoreCase(category)) {
				resList.add(getResource(attribute));
			} else if (CATEGORY_ACTION.equalsIgnoreCase(category)) {
				action = getAction(attribute);
			} else if (CATEGORY_APPLICATION.equalsIgnoreCase(category)) {
				application = getApplication(attribute);
			} else if (XACMLParser.CATEGORY_POLICY_ON_DEMAND.equalsIgnoreCase(category)) {
				policyOnDemand = getPolicyOnDemandPQL(attribute);
			} else if (CATEGORY_HOST.equalsIgnoreCase(category)) {
				host = getHost(attribute);
			} else if (CATEGORY_RECIPIENT.equalsIgnoreCase(category)) {
				addRecipientAttrs(recipients, additionalAttrs, attribute);
			} else if (XACMLParser.CATEGORY_ENVIRONMENT.equalsIgnoreCase(category)) {
				environment = getEnvironment(attribute);
				additionalAttrs.add(environment);
			} else if (category.startsWith(XACMLParser.CATEGORY_EXTRA_ATTRIBUTES_PREFIX)) {
				otherAttributes.add(attribute);
			}
		}
		
		if (!otherAttributes.isEmpty()) {
			for (AttributesType otherAttr : otherAttributes) {
				String categoryName = otherAttr.getCategory();
				int hyphenIndex = categoryName.lastIndexOf('-');
				String otherAttrName = categoryName.substring(hyphenIndex + 1);  
				IPDPNamedAttributes namedAttr = getOtherAttribute(otherAttr, otherAttrName);
				additionalAttrs.add(namedAttr);
			}
		}
		
		pdpRequest.setUser(user);
		pdpRequest.setAction(action);
		pdpRequest.setApplication(application);
		pdpRequest.setPolicyOnDemand(policyOnDemand);
		pdpRequest.setHost(host);
		pdpRequest.setRecipients(recipients.toArray(new String[recipients.size()]));
		if (!resList.isEmpty() && isResourceListValid(resList)) {
			pdpRequest.setResourceArr(resList.toArray(new IPDPResource[resList.size()]));
		} else {
			throw new InvalidInputException("Resource is either missing or has invalid attributes");
		}
		if (!additionalAttrs.isEmpty()) {
			pdpRequest.setAdditionalData(additionalAttrs.toArray(new IPDPNamedAttributes[additionalAttrs.size()]));
		}

		if (log.isDebugEnabled())
			log.debug("PDPRequest data populated successfully, [ No of Resources : "
					+ pdpRequest.getResourceArr().length + "]");
		
		if (pdpRequest.getAction() != null && pdpRequest.getResourceArr() != null && pdpRequest.getUser() != null) {
			return pdpRequest;
		} else {
			throw new InvalidInputException("Invalid Request :: One or more mandatory attributes are missing");
		}
	}

	private void addRecipientAttrs(List<String> recipients, List<IPDPNamedAttributes> additionalAttrs,
			AttributesType attribute) {
		IPDPNamedAttributes recipient = null;
		if (ATTRIBUTE_ID_RECIPIENT_ID.equalsIgnoreCase(attribute.getAttribute().get(0).getAttributeId())) {
			recipient = getRecipient(attribute);
			additionalAttrs.add(recipient);
		} else {
			addRecipients(attribute, recipients);
		}
	}

	private IPDPUser getUser(AttributesType attribute) {
		
		Map<String, List<String>> attributeMap = getAttributeMap(attribute.getAttribute());

		IPDPUser user = createUser(attributeMap);
		if (log.isDebugEnabled())
			log.debug("PDPUser created from request attributes");
		return user;
	}

	private String getAction(AttributesType attribute) {
		Map<String, List<String>> attributeMap = getAttributeMap(attribute.getAttribute());

		String actionValue = attributeMap.get(ATTRIBUTE_ID_ACTION_ID).get(0);
		if (log.isDebugEnabled())
			log.debug("Retrieve action from request attributes, [ Action : "
					+ actionValue + "]");
		return (actionValue != null) ? actionValue.trim() : null;
	}
	
	private void addRecipients(AttributesType attribute, List<String> recipients){
		Map<String, List<String>> attributeMap = getAttributeMap(attribute.getAttribute());
		List<String> recipientList = attributeMap.get(ATTRIBUTE_ID_RECIPIENT_EMAIL); 
		if(recipientList!=null && !recipientList.isEmpty()){
			recipients.addAll(recipientList);
		}
	}

	private IPDPResource getResource(AttributesType attributes) {
		Map<String, List<String>> attributeMap = getAttributeMap(attributes.getAttribute());
		
		IPDPResource resource = createResource(attributeMap);
		
		if (log.isDebugEnabled())
			log.debug("IPDPResource created from request attributes");
		return resource;
	}

	private IPDPApplication getApplication(AttributesType attribute) {
		Map<String, List<String>> attributeMap = getAttributeMap(attribute.getAttribute());

		IPDPApplication application = createApplication(attributeMap);

		if (log.isDebugEnabled())
			log.debug("IPDPApplication created from request attributes");
		return application;
	}
	
	private IPDPNamedAttributes getEnvironment(AttributesType attribute) {
		Map<String, List<String>> attributeMap = getAttributeMap(attribute.getAttribute());
		IPDPNamedAttributes environment = createPDPNamedAttributes(attributeMap, NAMED_ATTRIBUTE_ENVIRONMENT_NAME);

		if (log.isDebugEnabled())
			log.debug("Environment created from request attributes");
		return environment;
	}
	
	private IPDPNamedAttributes getOtherAttribute(AttributesType attribute, String name) {
		Map<String, List<String>> attributeMap = getAttributeMap(attribute.getAttribute());
		IPDPNamedAttributes pdpNamedAttr = createPDPNamedAttributes(attributeMap, name);

		if (log.isDebugEnabled())
			log.debug("Recipient created from request attributes");
		return pdpNamedAttr;
	}
	
	private IPDPNamedAttributes getRecipient(AttributesType attribute) {
		Map<String, List<String>> attributeMap = getAttributeMap(attribute.getAttribute());
		IPDPNamedAttributes recipient = createPDPNamedAttributes(attributeMap, NAMED_ATTRIBUTE_RECIPIENT_NAME);

		if (log.isDebugEnabled())
			log.debug("Recipient created from request attributes");
		return recipient;
	}
	
	private IPDPHost getHost(AttributesType attribute) throws InvalidInputException {
		Map<String, List<String>> attributeMap = getAttributeMap(attribute.getAttribute());

		if (isHostValid(attributeMap)){
			IPDPHost host = createHost(attributeMap);
			if (log.isDebugEnabled())
				log.debug("PDPHost created from request attributes");
			return host;
		}else{
			throw new InvalidInputException("No IP Address specified for the Host");
		}		
	}
	

	private PolicyOnDemand getPolicyOnDemandPQL(AttributesType attribute) {
		PolicyOnDemand pod = null;
		Map<String, List<String>> attributeMap = getAttributeMap(attribute.getAttribute());

		String podPQL = attributeMap.get(ATTRIBUTE_POD_ID).get(0);
		String ignoreBuldInPolicies = attributeMap
				.get(ATTRIBUTE_POD_IGNORE_BUILT_IN).get(0);

		if (podPQL != null && !podPQL.isEmpty()) {
			pod = new PolicyOnDemand(
					podPQL,
					Boolean.valueOf((ignoreBuldInPolicies != null
							&& !ignoreBuldInPolicies.isEmpty() ? ignoreBuldInPolicies
							: "false")));
		}
		return pod;
	}

	private Map<String, List<String>> getAttributeMap(List<AttributeType> attributeTypes) {
		Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();

		for (AttributeType attrib : attributeTypes) {
			List<String> values = new ArrayList<String>();
			for (AttributeValueType attribValue : attrib.getAttributeValue()) {
				if (attribValue.getContent() != null) {
					for (Object content : attribValue.getContent()) {
						values.add((content != null) ? String.valueOf(content).trim() : null);
						// break;
					}
				}
			}
			attributeMap.put(attrib.getAttributeId(), values);
		}

		return attributeMap;
	}
	
	public JAXBContext getJAXBContext() throws EvaluationConnectorException {
		if (jaxbContext == null) {
			try {
				 jaxbContext = JAXBContext
						.newInstance(RequestType.class);

			} catch (Exception e) {
				throw new EvaluationConnectorException(
						"XACML 3.0 - XML parser failed");
			}
		}
		return jaxbContext;
	}
	
}
