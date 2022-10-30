/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.parsers;

import static com.nextlabs.evaluationconnector.utils.PropertiesUtil.isNotNullOrEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPHost;
import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.google.gson.Gson;
import com.nextlabs.evaluationconnector.beans.PDPRequest;
import com.nextlabs.evaluationconnector.beans.PolicyOnDemand;
import com.nextlabs.evaluationconnector.beans.json.Action;
import com.nextlabs.evaluationconnector.beans.json.Attribute;
import com.nextlabs.evaluationconnector.beans.json.Category;
import com.nextlabs.evaluationconnector.beans.json.Environment;
import com.nextlabs.evaluationconnector.beans.json.MultiRequests;
import com.nextlabs.evaluationconnector.beans.json.Request;
import com.nextlabs.evaluationconnector.beans.json.RequestReference;
import com.nextlabs.evaluationconnector.beans.json.RequestWrapper;
import com.nextlabs.evaluationconnector.beans.json.Resource;
import com.nextlabs.evaluationconnector.beans.json.Subject;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.exceptions.InvalidInputException;
import com.nextlabs.evaluationconnector.utils.Constants;

/**
 * <p>
 * JsonXACMLParser
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class JsonXACMLParser extends AbstractXACMLParser implements XACMLParser {

	private static final Log log = LogFactory.getLog(JsonXACMLParser.class);

	private Gson gson = null;

	@Override
	public void init() {
		gson = new Gson();
	}
	
	@Override
	public List<PDPRequest> parseData(String data) throws InvalidInputException,
			EvaluationConnectorException {
		
		try {
			long startCounter = System.currentTimeMillis();
			RequestWrapper request = gson.fromJson(data, RequestWrapper.class);

			if (log.isDebugEnabled())
				log.debug(String
						.format("%s, Thread Id:[%s],  JsonXACMLParser -> Time taken to Request Object mapping : %d milis",
								Constants.PERF_LOG_PREFIX, ""
										+ Thread.currentThread().getId(),
								(System.currentTimeMillis() - startCounter)));
			List<PDPRequest> pdpRequests = mapToPDPRequest(request);
			
			return pdpRequests;
		} catch (InvalidInputException e) {
			throw e;
		} catch (Throwable e) {
			throw new EvaluationConnectorException(
					"Error occurred in JSON data parsing. [ "
							+ e.getLocalizedMessage() + " ]", e);
		}
	}

	private List<PDPRequest> mapToPDPRequest(RequestWrapper requestWrapper) throws InvalidInputException {

		if (log.isDebugEnabled())
			log.debug("JSON data will assign to PDP Request");

		List<PDPRequest> pdpRequests = new ArrayList<PDPRequest>();

		Request request = requestWrapper.getRequest();
		MultiRequests multiRequests = request.getMultiRequests();

		if (multiRequests != null && !multiRequests.getRequestReference().isEmpty()) {
			List<RequestReference> requestReferences = multiRequests.getRequestReference();

			Map<String, Category> categoryMap = populateCategoryMap(request);
			
			for (RequestReference requestReference : requestReferences) {
				List<Category> categoryList = new ArrayList<Category>();
				List<String> referenceIds = requestReference.getReferenceId();
				for (String referenceId : referenceIds) {
					categoryList.add(categoryMap.get(referenceId));
				}

				PDPRequest pdpRequest = populatePDPRequestData(null, categoryList);
				pdpRequests.add(pdpRequest);
			}
			
		} else {
			PDPRequest pdpRequest = populatePDPRequestData(request, null);
			pdpRequests.add(pdpRequest);
		}
		return pdpRequests;
	}
	
	private Map<String, Category> populateCategoryMap(Request request) {

		Map<String, Category> categoryMap = new HashMap<String, Category>();
		List<Category> categs = new ArrayList<>(request.getCategory());
		categs.addAll(request.getSubject());
		categs.addAll(request.getResource());
		categs.addAll(request.getAction());
		categs.addAll(request.getEnvironment());

		for (Category c : categs) {
			categoryMap.put(c.getId(), c);
		}

		return categoryMap;
	}
	
	private PDPRequest populatePDPRequestData(Request request, List<Category> categories) throws InvalidInputException {

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
		List<Category> otherAttributes = new ArrayList<Category>();

		if (categories == null) {
			categories = request.getCategory();
		}

		for (Category category : categories) {

			if (XACMLParser.CATEGORY_SUBJECT.equalsIgnoreCase(category.getCategoryId())) {
				user = getUser(category);
			} else if (XACMLParser.CATEGORY_ACTION.equalsIgnoreCase(category.getCategoryId())) {
				action = getAction(category);
			} else if (XACMLParser.CATEGORY_RESOURCE.equalsIgnoreCase(category.getCategoryId())) {
				resList.add(getResource(category));
			} else if (XACMLParser.CATEGORY_APPLICATION.equalsIgnoreCase(category.getCategoryId())) {
				application = getApplication(category);
			} else if (XACMLParser.CATEGORY_POLICY_ON_DEMAND.equalsIgnoreCase(category.getCategoryId())) {
				policyOnDemand = getPolicyOnDemandPQL(category);
			} else if (XACMLParser.CATEGORY_HOST.equalsIgnoreCase(category.getCategoryId())) {
				host = getHost(category);
			} else if (XACMLParser.CATEGORY_RECIPIENT.equalsIgnoreCase(category.getCategoryId())) {
				addRecipientAttrs(recipients, additionalAttrs, category);
			} else if (XACMLParser.CATEGORY_ENVIRONMENT.equalsIgnoreCase(category.getCategoryId())) {
				environment = getEnvironment(category);
				additionalAttrs.add(environment);
			} else if (category.getCategoryId().startsWith(XACMLParser.CATEGORY_EXTRA_ATTRIBUTES_PREFIX)) {
				otherAttributes.add(category);
			}
		}
		
		if (!otherAttributes.isEmpty()) {
			for (Category otherAttr : otherAttributes) {
				String categoryName = otherAttr.getCategoryId();
				int hyphenIndex = categoryName.lastIndexOf('-');
				String otherAttrName = categoryName.substring(hyphenIndex + 1);  
				IPDPNamedAttributes namedAttr = getOtherAttribute(otherAttr, otherAttrName);
				additionalAttrs.add(namedAttr);
			}
		}

		if (request != null) {
			if (!request.getAction().isEmpty()) {
				if (isNotNullOrEmpty(action))
					throw new InvalidInputException("Action has already been specified in the 'Category' section");
				List<Action> actions = request.getAction();
				for (Action act : actions) {
					action = getAction(act);
					if (isNotNullOrEmpty(action))
						break;
				}
			}

			if (!request.getResource().isEmpty()) {
				if (resList.size() > 0)
					throw new InvalidInputException("Resources have already been specified in the 'Category' section");
				List<Resource> resources = request.getResource();
				for (Resource resource : resources) {
					IPDPResource ipdResource = getResource(resource);
					resList.add(ipdResource);
				}
			}

			if (!request.getSubject().isEmpty()) {
				if (user != null)
					throw new InvalidInputException("Subject has already been specified in the 'Category' section");
				List<Subject> subjects = request.getSubject();
				for (Subject subject : subjects) {
					user = getUser(subject);
					break;
				}
			}

			if (!request.getEnvironment().isEmpty()) {
				if (environment != null)
					throw new InvalidInputException("Environment has already been specified in the 'Category' section");
				List<Environment> environments = request.getEnvironment();
				for (Environment env : environments) {
					environment = getEnvironment(env);
					break;
				}
			}
		}
		pdpRequest.setUser(user);
		pdpRequest.setAction(action);
		pdpRequest.setApplication(application);
		pdpRequest.setPolicyOnDemand(policyOnDemand);
		if (!recipients.isEmpty()) {
			pdpRequest.setRecipients(recipients.toArray(new String[recipients.size()]));
		}
		if (!resList.isEmpty() && isResourceListValid(resList)) {
			pdpRequest.setResourceArr(resList.toArray(new IPDPResource[resList.size()]));
		} else {
			throw new InvalidInputException("Resource is either missing or has invalid attributes");
		}
		if (!additionalAttrs.isEmpty()) {
			pdpRequest.setAdditionalData(additionalAttrs.toArray(new IPDPNamedAttributes[additionalAttrs.size()]));
		}
		pdpRequest.setHost(host);

		if (pdpRequest.getAction() != null && pdpRequest.getResourceArr() != null && pdpRequest.getUser() != null) {
			if (log.isInfoEnabled())
				log.info("PDPRequest data populated successfully, [ No of Resources : "
						+ pdpRequest.getResourceArr().length + "]");
			return pdpRequest;
		} else {
			throw new InvalidInputException("Invalid Request :: One or more mandatory attributes are missing");
		}
	}

	private void addRecipientAttrs(List<String> recipients, List<IPDPNamedAttributes> additionalAttrs,
			Category category) {
		IPDPNamedAttributes recipient = null;
		if (ATTRIBUTE_ID_RECIPIENT_ID.equalsIgnoreCase(category.getAttribute().get(0).getAttributeId())) {
			recipient = getRecipient(category);
			additionalAttrs.add(recipient);
		} else {
			addRecipients(category, recipients);
		}
	}

	private IPDPUser getUser(Category category) {
		Map<String, List<String>> attributeMap = getAttributeMap(category);
		IPDPUser user = createUser(attributeMap);
		if (log.isDebugEnabled())
			log.debug("PDPUser created from request attributes");
		return user;
	}

	private String getAction(Category category) {
		Map<String, List<String>> attributeMap = getAttributeMap(category);
		String actionValue = attributeMap.get(ATTRIBUTE_ID_ACTION_ID).get(0); 
		if (log.isDebugEnabled())
			log.debug("Retrive action from request attributes, [ Action : "
					+ actionValue + "]");
		return (actionValue != null) ? actionValue.trim() : null;
	}

	private IPDPApplication getApplication(Category category) {
		Map<String, List<String>> attributeMap = getAttributeMap(category);
		IPDPApplication application = createApplication(attributeMap);

		if (log.isDebugEnabled())
			log.debug("IPDPApplication created from request attributes");
		return application;
	}
	
	private IPDPNamedAttributes getEnvironment(Category category) {
		Map<String, List<String>> attributeMap = getAttributeMap(category);
		IPDPNamedAttributes environment = createPDPNamedAttributes(attributeMap, NAMED_ATTRIBUTE_ENVIRONMENT_NAME);

		if (log.isDebugEnabled())
			log.debug("Environment created from request attributes");
		return environment;
	}
	
	private IPDPNamedAttributes getRecipient(Category category) {
		Map<String, List<String>> attributeMap = getAttributeMap(category);
		IPDPNamedAttributes recipient = createPDPNamedAttributes(attributeMap, NAMED_ATTRIBUTE_RECIPIENT_NAME);

		if (log.isDebugEnabled())
			log.debug("Recipient created from request attributes");
		return recipient;
	}
	
	private IPDPNamedAttributes getOtherAttribute(Category category, String name) {
		Map<String, List<String>> attributeMap = getAttributeMap(category);
		IPDPNamedAttributes pdpNamedAttr = createPDPNamedAttributes(attributeMap, name);

		if (log.isDebugEnabled())
			log.debug("Extra attributes created from request attributes");
		return pdpNamedAttr;
	}
	
	private IPDPHost getHost(Category category) throws InvalidInputException {
		Map<String, List<String>> attributeMap = getAttributeMap(category);
		
		if (isHostValid(attributeMap)){
			IPDPHost host = createHost(attributeMap);
			if (log.isDebugEnabled())
				log.debug("PDPHost created from request attributes");
			return host;
		}else{
			throw new InvalidInputException("No INET Address specified for the Host");
		}
	}
	
	private void addRecipients(Category category, List<String> recipients){
		Map<String, List<String>> attributeMap = getAttributeMap(category);
		List<String> recipientList = attributeMap.get(ATTRIBUTE_ID_RECIPIENT_EMAIL); 
		if(recipientList!=null && !recipientList.isEmpty()){
			recipients.addAll(recipientList);
		}
	}

	private PolicyOnDemand getPolicyOnDemandPQL(Category category) {
		PolicyOnDemand pod = null;
		Map<String, List<String>> attributeMap = getAttributeMap(category);
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

	private IPDPResource getResource(Category category) {
		Map<String, List<String>> attributeMap = getAttributeMap(category);

		IPDPResource resource = createResource(attributeMap);

		if (log.isDebugEnabled())
			log.debug("IPDPResource created from request attributes");
		return resource;
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<String>> getAttributeMap(Category category) {
		Map<String, List<String>> attributeMap = new HashMap<String, List<String>>();
		for (Attribute attribute : category.getAttribute()) {
			List<String> values = new ArrayList<String>();
			
			if (attribute.getValue() != null) {
				if (attribute.getValue() instanceof java.util.ArrayList) {
					values.addAll((List<String>) attribute.getValue());
				} else if (attribute.getValue() instanceof String) {
					values.add(String.valueOf(attribute.getValue()));
				}
			}
			
			attributeMap.put(attribute.getAttributeId(), values);
		}
		return attributeMap;
	}
	

}
