/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.adaptors;

import static com.nextlabs.evaluationconnector.utils.Constants.PDP_CONNECTOR_API_RMI_HOST;
import static com.nextlabs.evaluationconnector.utils.Constants.PDP_CONNECTOR_API_RMI_PORT;
import static com.nextlabs.evaluationconnector.utils.PropertiesUtil.getInt;
import static com.nextlabs.evaluationconnector.utils.PropertiesUtil.getString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.nextlabs.destiny.sdk.CEApplication;
import com.nextlabs.destiny.sdk.CEAttributes;
import com.nextlabs.destiny.sdk.CEAttributes.CEAttribute;
import com.nextlabs.destiny.sdk.CEEnforcement;
import com.nextlabs.destiny.sdk.CEEnforcement.CEResponse;
import com.nextlabs.destiny.sdk.CENamedAttributes;
import com.nextlabs.destiny.sdk.CERequest;
import com.nextlabs.destiny.sdk.CEResource;
import com.nextlabs.destiny.sdk.CESdk;
import com.nextlabs.destiny.sdk.CESdkException;
import com.nextlabs.destiny.sdk.CEUser;
import com.nextlabs.destiny.sdk.ICESdk;
import com.nextlabs.evaluationconnector.beans.PDPRequest;
import com.nextlabs.evaluationconnector.beans.ResponseStatusCode;
import com.nextlabs.evaluationconnector.beans.XACMLResponse;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.exceptions.InvalidInputException;
import com.nextlabs.evaluationconnector.utils.Constants;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeAssignmentType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ObligationsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCodeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusType;

/**
 * <p>
 * RMI Adaptor implementation for PDP
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class PDPRMIAdaptor implements PDPAdapter {

	private static final Log log = LogFactory.getLog(PDPRMIAdaptor.class);

	private static final String KEY_RES_ID = "ce::id";
	private static final String KEY_RES_TYPE = "ce::destinytype";
	private static final String KEY_USER_ID = "id";
	private static final String KEY_USER_NAME = "name";
	private static final String KEY_APP_NAME = "name";
	private static final String KEY_APP_PID = "pid";
	private static final String KEY_HOST_INET = "inet_address";

	private static final String ATTR_OBLIGATION_COUNT = "CE_ATTR_OBLIGATION_COUNT";
	private static final String ATTR_OBLIGATION_NAME = "CE_ATTR_OBLIGATION_NAME";
	private static final String ATTR_OBLIGATION_NUMVALUES = "CE_ATTR_OBLIGATION_NUMVALUES";
	private static final String ATTR_OBLIGATION_VALUE = "CE_ATTR_OBLIGATION_VALUE";

	private CESdk sdk;
	private String rmiHostName = "127.0.0.1";
	private int rmiPortNo = 1099;
	private long sdkInitialized = 0;

	@Override
	public void init() throws EvaluationConnectorException {
		try {
			synchronized (this) {
				rmiHostName = getString(PDP_CONNECTOR_API_RMI_HOST);
				rmiPortNo = getInt(PDP_CONNECTOR_API_RMI_PORT);

				this.sdk = new CESdk(rmiHostName, rmiPortNo);

				if (log.isInfoEnabled())
					log.info(String
							.format("PDP RMI Adapter initialization : [ Host: %s, Port : %d ]",
									rmiHostName, rmiPortNo));
				sdkInitialized = 1;
			}
		} catch (CESdkException e) {
			throw new EvaluationConnectorException(
					"Error occurred while initializing SDK", e);
		}
	}

	@Override
	public XACMLResponse evaluate(List<PDPRequest> pdpRequests) throws EvaluationConnectorException {
		XACMLResponse response = new XACMLResponse();
		ResponseType responseType = null;
		try {
			if (sdkInitialized == 0) {
				init();
			}

			long startCounter = System.nanoTime();
			List<CEEnforcement> enforcementResults = invokeSDK(pdpRequests, response);

			if (log.isDebugEnabled())
				log.debug(String.format(
						"%s, Thread Id:[%s],  PDPRMIAdaptor -> Total time taken by RMI request Evaluation : %d nano",
						Constants.PERF_LOG_PREFIX, "" + Thread.currentThread().getId(),
						(System.nanoTime() - startCounter)));

			long responseStartCounter = System.nanoTime();
			responseType = getResponse(enforcementResults);
			response.setStatus(ResponseStatusCode.OK.getValue());
			response.setResponseType(responseType);

			if (log.isDebugEnabled())
				log.debug(String.format(
						"%s, Thread Id:[%s],  PDPRMIAdaptor -> Total time for by RMI response creation : %d nano",
						Constants.PERF_LOG_PREFIX, "" + Thread.currentThread().getId(),
						(System.nanoTime() - responseStartCounter)));

		} catch (Exception e) {
			log.error("Error occurred while evaluating the request, ", e);
			responseType = getErrorResponse(e.getLocalizedMessage(), ResponseStatusCode.PROCESSING_ERROR);
			response.setStatus(ResponseStatusCode.PROCESSING_ERROR.getValue());
			response.setResponseType(responseType);
		}

		if (log.isDebugEnabled())
			log.debug("XACML Response Generated after request evaluation, [ XACML Response Status : "
					+ response.getStatus() + "] ");
		return response;
	}

	private ResponseType getErrorResponse(String errorMessage,
			ResponseStatusCode statusCode) {
		ResponseType response = new ResponseType();

		ResultType result = new ResultType();
		response.getResult().add(result);
		result.setDecision(DecisionType.INDETERMINATE);
		StatusType status = getResponseStatus(errorMessage, statusCode);
		result.setStatus(status);

		return response;
	}

	private StatusType getResponseStatus(String errorMessage,
			ResponseStatusCode statusCode) {
		StatusType status = new StatusType();
		status.setStatusMessage(errorMessage);

		StatusCodeType codeType = new StatusCodeType();
		codeType.setValue(statusCode.getValue());
		status.setStatusCode(codeType);

		return status;
	}
	
	private StatusType getResponseStatus(String message, CEResponse ceResponse) {
		
		StatusType status = new StatusType();
		StatusCodeType codeType = new StatusCodeType();
		
		if(CEResponse.ERROR == ceResponse) {
			status.setStatusMessage("Error");
			codeType.setValue(ResponseStatusCode.PROCESSING_ERROR.getValue());
		} else if (CEResponse.DONTCARE == ceResponse) {
			status.setStatusMessage("Not Applicable");
			codeType.setValue(ResponseStatusCode.OK.getValue());	
		} else {
			status.setStatusMessage(message);
			codeType.setValue(ResponseStatusCode.OK.getValue());	
		}
		
		status.setStatusCode(codeType);
		return status;
	}

	private ResponseType getResponse(List<CEEnforcement> enforcementResults) {
		ResponseType response = new ResponseType();

		for (CEEnforcement enforcement : enforcementResults) {
			ResultType result = getResult(enforcement.getResponse());
			StatusType status = getResponseStatus("success", enforcement.getResponse());
//			StatusType status = getResponseStatus("success",
//					ResponseStatusCode.OK);

			ObligationsType obligations = getObligations(enforcement
					.getObligations());

			result.setStatus(status);
			result.setObligations(obligations);

			response.getResult().add(result);
		}

		if (log.isDebugEnabled())
			log.debug("Response created from CE Enforcement Result.");
		return response;
	}

	private ObligationsType getObligations(CEAttributes obligations) {
		ObligationsType obligationsType = new ObligationsType();

		List<CEAttribute> attribList = obligations.getAttributes();
		Map<String, String> obligationsMap = new HashMap<String, String>();

		for (CEAttribute ceAttribute : attribList) {
			obligationsMap.put(ceAttribute.getKey(), ceAttribute.getValue());
		}

		int obligationsCount = getIntValue(obligationsMap
				.get(ATTR_OBLIGATION_COUNT));
		if (obligationsCount < 1) {
			return obligationsType;
		}

		for (int i = 1; i <= obligationsCount; i++) {

			ObligationType obligationType = new ObligationType();
			String obligationId = obligationsMap.get(ATTR_OBLIGATION_NAME + ":"
					+ i);
			obligationType.setObligationId(obligationId);

			int numObligationAttribs = getIntValue(obligationsMap
					.get(ATTR_OBLIGATION_NUMVALUES + ":" + i));
			for (int j = 1; j <= numObligationAttribs; j += 2) {
				String attributeId = obligationsMap.get(ATTR_OBLIGATION_VALUE
						+ ":" + i + ":" + j);
				String attributeValue = obligationsMap
						.get(ATTR_OBLIGATION_VALUE + ":" + i + ":" + (j + 1));

				if (attributeValue == null || attributeValue.isEmpty()) {
					attributeValue = attributeId;
				}

				AttributeAssignmentType assignmentType = new AttributeAssignmentType();
				assignmentType.setAttributeId(attributeId);
				assignmentType.getContent().add(attributeValue);

				obligationType.getAttributeAssignment().add(assignmentType);
			}
			obligationsType.getObligation().add(obligationType);
		}

		return obligationsType;
	}

	private int getIntValue(String value) {
		if (value != null && isNumeric(value)) {
			return Integer.valueOf(value);
		} else {
			return 0;
		}
	}

	public static boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}

	private ResultType getResult(CEResponse ceResponse) {
		ResultType result = new ResultType();
		if (ceResponse == CEResponse.ALLOW) {
			result.setDecision(DecisionType.PERMIT);
			
		} else if (ceResponse == CEResponse.DENY) {
			result.setDecision(DecisionType.DENY);
			
		}  else if (ceResponse == CEResponse.DONTCARE) {
			result.setDecision(DecisionType.NOT_APPLICABLE);

		} else if (ceResponse == CEResponse.ERROR) {
			result.setDecision(DecisionType.INDETERMINATE);

		} else {
			result.setDecision(DecisionType.INDETERMINATE);
		}
		return result;
	}

	private List<CEEnforcement> invokeSDK(List<PDPRequest> pdpRequests, XACMLResponse response)
			throws EvaluationConnectorException {
		try {
			// Get CE Requests from PDP Request
			List<CERequest> ceRequests = createCERequests(pdpRequests);
			List<CEEnforcement> enforcementResults = new ArrayList<CEEnforcement>();

			Long startTime = System.nanoTime();
			for (PDPRequest pdpRequest : pdpRequests) {
				int ipAddress = 0;
				if (pdpRequest.getHost() != null && pdpRequest.getHost().getValue(KEY_HOST_INET) != null) {
					ipAddress = Integer.parseInt(pdpRequest.getHost().getValue(KEY_HOST_INET));
				}
				enforcementResults = sdk.checkResources(ceRequests, pdpRequest.getPolicyOnDemand().getPql(),
						pdpRequest.getPolicyOnDemand().isIgnoreBuildInPolicies(), ipAddress, 10000);
			}

			long pdpEvalTime = System.nanoTime() - startTime;
			response.setPdpEvalTime(pdpEvalTime);

			if (log.isDebugEnabled())
				log.debug("PDP Enforcement Results size:" + enforcementResults.size());

			return enforcementResults;
		} catch (Exception e) {
			throw new EvaluationConnectorException("Error occurred while invokeSDK, ", e);
		}
	}
	/**
	 * <p>
	 * 
	 * </p>
	 *
	 * @param pdpRequest
	 * @param action
	 * @param app
	 * @param user
	 * @param recipients
	 * @return
	 */
	protected List<CERequest> createCERequests(List<PDPRequest> pdpRequests) throws InvalidInputException {
		List<CERequest> ceRequests = new ArrayList<CERequest>();

		for (PDPRequest pdpRequest : pdpRequests) {
			String action = pdpRequest.getAction();
			CEUser user = getUser(pdpRequest.getUser());
			CEAttributes userAttributes = getAttributes(pdpRequest.getUser(),
					Arrays.asList(KEY_USER_ID, KEY_USER_NAME));

			CEApplication app = getApplication(pdpRequest.getApplication());
			CEAttributes appAttributes = getAttributes(pdpRequest.getApplication(),
					Arrays.asList(KEY_APP_NAME, KEY_APP_PID));

			String[] recipients = pdpRequest.getRecipients();

			CEResource source = null;
			CEAttributes sourceAttributes = null;
			CEResource dest = null;
			CEAttributes destAttributes = null;

			for (IPDPResource resource : pdpRequest.getResourceArr()) {

				String dimensionVal = resource.getName();
				if ("from".equals(dimensionVal)) {
					source = getResource(resource);
					sourceAttributes = getAttributes(resource,
							Arrays.asList(KEY_RES_ID, KEY_RES_TYPE));
				} else if ("to".equals(dimensionVal)) {
					dest = getResource(resource);
					destAttributes = getAttributes(resource,
							Arrays.asList(KEY_RES_ID, KEY_RES_TYPE));
				}
			}
			
			//add additional attributes
			CENamedAttributes[] additionalAttrs = getAdditionalAttributes(pdpRequest.getAdditionalData());
			
			/**
			 * if source is null, destination should be null as well check for
			 * source, send the request only if it is not null
			 */

			if (source != null) {
				CERequest ceRequest = new CERequest(action, source, sourceAttributes, dest, destAttributes, user,
						userAttributes, app, appAttributes, recipients, additionalAttrs, true,
						ICESdk.CE_NOISE_LEVEL_USER_ACTION);
				ceRequests.add(ceRequest);
			} else {
				throw new InvalidInputException("No from resource specified in the request");
			}
		}

		return ceRequests;
	}

	private CEApplication getApplication(IPDPApplication application) {
		String appName = application.getValue(KEY_APP_NAME);

		CEApplication app = new CEApplication(appName, null);
		return app;
	}

	private CEUser getUser(IPDPUser user) {
		String userId = user.getValue(KEY_USER_ID);
		String userName = user.getValue(KEY_USER_NAME);
		if (userName == null) {
			userName = userId;
		}
		CEUser ceUser = new CEUser(userName, userId);
		return ceUser;
	}

	private CEAttributes getAttributes(IPDPNamedAttributes pdpNamedAttributes, List<String> keysToSkip) {
		CEAttributes ceAttributes = new CEAttributes();
		
		for (String key : pdpNamedAttributes.keySet()) {
			if(! keysToSkip.contains(key)) {
				String[] values = pdpNamedAttributes.getValues(key);
				for (String value : values) {
					if (value != null) {
						ceAttributes.add(key, value);
					}
				}
			}
		}

		return ceAttributes;
	}
	
	private CEResource getResource(IPDPResource res) {
		String resId = res.getValue(KEY_RES_ID);
		String resType = res.getValue(KEY_RES_TYPE);
		CEResource ceRes = new CEResource(resId, resType);
		return ceRes;
	}
	
	private CENamedAttributes[] getAdditionalAttributes(IPDPNamedAttributes[] additionalData) {
		CENamedAttributes[] additionalAttrs = null;

		if (additionalData != null && additionalData.length > 0) {
			additionalAttrs = new CENamedAttributes[additionalData.length];

			for (int i = 0; i < additionalData.length; i++) {
				CENamedAttributes namedAttributes = new CENamedAttributes(additionalData[i].getName());

                                for (String key : additionalData[i].keySet()) {
                                    for (String value : additionalData[i].getValues(key)) {
                                        namedAttributes.add(key, value);
                                    }
                                }
                                
				additionalAttrs[i] = namedAttributes;
			}
		}
		return additionalAttrs;
	}

	public void closeConnection() {
		try {
			if (log.isDebugEnabled())
				log.debug("About to close SDK");
			if (sdk != null)
				// TODO close the connection

				log.info("RMI Connection closed successfully");
		} catch (Exception e) {
			log.error("Error occurred while closing the SDK", e);
		}
	}

}
