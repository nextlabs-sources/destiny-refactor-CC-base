/**
 * 
 */
package com.nextlabs.evaluationconnector.adaptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPEnforcement;
import com.bluejungle.destiny.agent.pdpapi.IPDPHost;
import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPSDKCallback;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.bluejungle.destiny.agent.pdpapi.PDPApplication;
import com.bluejungle.destiny.agent.pdpapi.PDPException;
import com.bluejungle.destiny.agent.pdpapi.PDPHost;
import com.bluejungle.destiny.agent.pdpapi.PDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.PDPResource;
import com.bluejungle.destiny.agent.pdpapi.PDPSDK;
import com.bluejungle.destiny.agent.pdpapi.PDPTimeout;
import com.bluejungle.destiny.agent.pdpapi.PDPUser;
import com.nextlabs.destiny.sdk.CEApplication;
import com.nextlabs.destiny.sdk.CEAttributes;
import com.nextlabs.destiny.sdk.CEAttributes.CEAttribute;
import com.nextlabs.destiny.sdk.CEEnforcement;
import com.nextlabs.destiny.sdk.CEEnforcement.CEResponse;
import com.nextlabs.destiny.sdk.CENamedAttributes;
import com.nextlabs.destiny.sdk.CERequest;
import com.nextlabs.destiny.sdk.CEResource;
import com.nextlabs.destiny.sdk.CESdkException;
import com.nextlabs.destiny.sdk.CESdkTimeoutException;
import com.nextlabs.destiny.sdk.CEUser;
import com.nextlabs.destiny.sdk.ICESdk;
import com.nextlabs.evaluationconnector.beans.PDPRequest;
import com.nextlabs.evaluationconnector.beans.PolicyOnDemand;
import com.nextlabs.evaluationconnector.beans.ResponseStatusCode;
import com.nextlabs.evaluationconnector.beans.XACMLResponse;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.exceptions.InvalidInputException;
import com.nextlabs.evaluationconnector.parsers.XACMLParser;
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
 * PDP Direct Adapter which connects directly with PDP, Only for local seem less
 * integration
 * </p>
 * 
 * @author Amila Silva
 *
 */
public class PDPDirectAdaptor implements PDPAdapter {

	private static final Log log = LogFactory.getLog(PDPDirectAdaptor.class);

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

	private static final IPDPHost LOCALHOST = new PDPHost(0x7F000001); // 127.0.0.1

	@Override
	public void init() throws EvaluationConnectorException {
		log.info("Nothing to initialized");
	}

	@Override
	public XACMLResponse evaluate(List<PDPRequest> pdpRequests) throws EvaluationConnectorException {
		XACMLResponse response = new XACMLResponse();
		ResponseType responseType = null;
		try {
			long startCounter = System.nanoTime();
			List<CEEnforcement> enforcementResults = callSDK(pdpRequests, response);

			if (log.isDebugEnabled())
				log.debug(String.format(
						"%s, Thread Id:[%s],  PDPDirectAdaptor -> Total time taken by Direct API Evaluation : %d nano",
						Constants.PERF_LOG_PREFIX, "" + Thread.currentThread().getId(),
						(System.nanoTime() - startCounter)));

			long responseStartCounter = System.currentTimeMillis();
			responseType = getResponse(enforcementResults);
			response.setStatus(ResponseStatusCode.OK.getValue());
			response.setResponseType(responseType);

			if (log.isDebugEnabled())
				log.debug(String.format(
						"%s, Thread Id:[%s],  PDPDirectAdaptor -> Total time for by Direct API response creation : %d nano",
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

	private List<CEEnforcement> callSDK(List<PDPRequest> pdpRequests, XACMLResponse response)
			throws EvaluationConnectorException {
		try {

			// Get CE Requests from PDP Request
			List<CERequest> ceRequests = createCERequests(pdpRequests);
			
			List<CEEnforcement> enforcementResults = new ArrayList<CEEnforcement>();

			long startTime = System.nanoTime();

			enforcementResults = checkResources(ceRequests, pdpRequests, 10000);

			long pdpEvalTime = System.nanoTime() - startTime;
			response.setPdpEvalTime(pdpEvalTime);

			if (log.isDebugEnabled())
				log.debug("PDP Enforcement Results size:" + enforcementResults.size());

			return enforcementResults;
		} catch (Exception e) {
			throw new EvaluationConnectorException("Error occurred while invokeSDK, ", e);
		}
	}
	

	private List<CEEnforcement> checkResources(List<CERequest> requests,
			List<PDPRequest> pdpRequests,
			int timeout) throws CESdkTimeoutException, CESdkException {
		LinkedList<CheckableCallback> callbacks = new LinkedList<CheckableCallback>();
	
		int index = 0;
		for (CERequest request : requests) {	
				PDPRequest pdpRequest = pdpRequests.get(index);
				
				int ipAddress = 0;
			if (pdpRequest.getHost() != null && pdpRequest.getHost().getValue(KEY_HOST_INET) != null) {
				ipAddress = Integer.parseInt(pdpRequest.getHost().getValue(KEY_HOST_INET));
			}
				
				CENamedAttributes[] additionalAttrs = getAdditionalAttributes(request,pdpRequest);

				CheckableCallback cb = new CheckableCallback();
				callbacks.add(cb);
				
				checkResources(request.getAction(), request.getSource(),
						request.getSourceAttributes(), request.getDest(),
						request.getDestAttributes(), request.getUser(),
						request.getUserAttributes(), request.getApplication(),
						request.getApplicationAttributes(), additionalAttrs,
						request.getRecipients(), ipAddress,
						request.getPerformObligations(), request.getNoiseLevel(),
						timeout, cb);
			index++;
		}

		/*
		 * There's no need to collect the results in the order they finish. We
		 * need all of them before the timeout happens, so why not just start at
		 * the beginning?
		 * 
		 * If we don't get all the response we give up. We do not return partial
		 * results.
		 */
		ArrayList<CEEnforcement> results = new ArrayList<CEEnforcement>(
				requests.size());
		long waitTime = timeout;

		for (CheckableCallback cb : callbacks) {
			log.debug("Wait time is " + waitTime + " milliseconds");

			if (waitTime <= 0) {
				throw new CESdkTimeoutException();
			}

			/*
			 * This time is not particularly precise, despite the name. On
			 * Windows it is usually some multiple of 16ms (with 0 as an
			 * option). It doesn't matter. If you have a 5s timeout then we
			 * don't guarantee that it will be *exactly* 5 seconds.
			 * 
			 * More accurate calls are available, but they are slower.
			 */
			long start = System.currentTimeMillis();
			try {
				CEEnforcement enf = cb.getResult(waitTime,
						TimeUnit.MILLISECONDS);

				if (enf == null) {
					throw new CESdkTimeoutException();
				}

				results.add(enf);
			} catch (InterruptedException e) {
				throw new CESdkTimeoutException(e);
			}
			waitTime -= System.currentTimeMillis() - start;
		}

		return results;
	}

	private CEEnforcement checkResources(String action, CEResource source,
			CEAttributes sourceAttrs, CEResource dest, CEAttributes destAttrs,
			CEUser user, CEAttributes userAttrs, CEApplication app,
			CEAttributes appAttrs, CENamedAttributes[] additionalAttrs,
			String[] recipients, int ipAddress, boolean performObligations,
			int noiseLevel, int timeout, IPDPSDKCallback cb)
			throws CESdkTimeoutException, CESdkException {

		if (log.isDebugEnabled())
			log.debug("PDP SDK checkResources call back");

		int resource_count = 1;

		if ((dest != null) && (!(dest.getName().length() == 0))) {
			resource_count++; // We have a target attribute
		}

		IPDPResource[] resources = new IPDPResource[resource_count];

		resources[0] = buildResource("from", source, sourceAttrs);

		if ((dest != null) && (!(dest.getName().length() == 0))) {
			resources[1] = buildResource("to", dest, destAttrs);
		}

		IPDPUser theUser = buildUser(user, userAttrs);

		IPDPNamedAttributes[] additionalData = buildAdditionalData(recipients,
				additionalAttrs);

		IPDPApplication application = buildApplication(app, appAttrs);

		IPDPHost host = buildHost(ipAddress);

		IPDPEnforcement ret = null;
		try {
			ret = PDPSDK.PDPQueryDecisionEngine(action, resources, theUser,
					application, host, performObligations, additionalData,
					noiseLevel, timeout, cb);
		} catch (PDPTimeout e) {
			throw new CESdkTimeoutException("PDP query timedout");
		} catch (PDPException e) {
			throw new CESdkException("PDP query exception");
		}

		// The return value will be null if we have a callback, otherwise it
		// won't
		if (ret != null) {
			return new CEEnforcement(ret.getResult(), new CEAttributes(
					ret.getObligations()));
		}
		return null;
	}

	private IPDPResource buildResource(final String dimensionName,
			final CEResource resource, final CEAttributes resourceAttrs) {
		IPDPResource theResource = new PDPResource(dimensionName,
				resource.getName(), resource.getType());

		if (resourceAttrs != null) {
			for (CEAttribute attr : resourceAttrs.getAttributes()) {
				theResource.setAttribute(attr.getKey().toLowerCase(),
						attr.getValue());
			}
		}
		return theResource;
	}

	private IPDPUser buildUser(final CEUser user, final CEAttributes userAttrs) {
		IPDPUser theUser = new PDPUser(user.getId(), user.getName());

		if (userAttrs != null) {
			for (CEAttribute attr : userAttrs.getAttributes()) {
				theUser.setAttribute(attr.getKey(), attr.getValue());
			}
		}

		return theUser;
	}

	private IPDPApplication buildApplication(final CEApplication app,
			final CEAttributes appAttrs) {
		IPDPApplication application = Constants.PDP_APPLICATION_NONE;

		if (!(app.getName().length() == 0)) {
			// TODO: do we need an actual PID here?
			application = new PDPApplication(app.getName(), 0L /* PID */);
			if (appAttrs != null) {
				for (CEAttribute attr : appAttrs.getAttributes()) {
					application.setAttribute(attr.getKey(), attr.getValue());
				}
			}
		}

		return application;
	}

	private IPDPHost buildHost(final int ipAddress) {
		IPDPHost host = LOCALHOST;

		if (ipAddress != 0) {
			host = new PDPHost(ipAddress);
		}

		return host;
	}

	private IPDPNamedAttributes[] buildAdditionalData(String[] recipients,
			CENamedAttributes[] additionalAttrs) {
		int additionalDataLength = 0;
		
		if (recipients != null && recipients.length > 0) {
			additionalDataLength++;
		}

		if (additionalAttrs != null) {
			additionalDataLength += additionalAttrs.length;
		}

		IPDPNamedAttributes[] additionalData = null;

		if (additionalDataLength > 0) {
			additionalData = new IPDPNamedAttributes[additionalDataLength];

			int index = 0;

			if (additionalAttrs != null) {
				for (CENamedAttributes namedAttrs : additionalAttrs) {
					additionalData[index] = new PDPNamedAttributes(
							namedAttrs.getName());
					for (CEAttribute attr : namedAttrs.getAttributes()) {
						additionalData[index].setAttribute(attr.getKey(),
								attr.getValue());
					}
					index++;
				}
			}
			if (recipients != null && recipients.length > 0) {
				additionalData[index] = new PDPNamedAttributes("sendto");
				for (String recipient : recipients) {
					additionalData[index].setAttribute("email", recipient);
				}
			}
		}
		
		return additionalData;
	}

	/**
	 * This class allows us to wait on the result of each individual callback.
	 * It would seem like Future<T> would do this, but the current
	 * implementations create a thread to run the action. We put a work unit on
	 * a queue to be handled by a thread pool, with the result returned via
	 * callback.
	 */
	private static class CheckableCallback implements IPDPSDKCallback {
		ArrayBlockingQueue<CEEnforcement> q = new ArrayBlockingQueue<CEEnforcement>(
				1);

		public void callback(IPDPEnforcement result) {
			q.add(new CEEnforcement(result.getResult(), new CEAttributes(result
					.getObligations())));
		}

		public boolean hasResult() {
			return q.peek() != null;
		}

		public CEEnforcement getResult() throws InterruptedException {
			return q.take();
		}

		public CEEnforcement getResult(long timeout, TimeUnit unit)
				throws InterruptedException {
			return q.poll(timeout, unit);
		}
	}

	/**
	 * <p>
	 * Create CE Request ready from PDPRequest object
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

			String[] recipients = getRecipients(pdpRequest.getRecipients());
			
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

			/**
			 * if source is null, destination should be null as well 
			 * check for source, send the request only if it is not null
			 */
			if (source != null) {
				CERequest ceRequest = new CERequest(action, source, sourceAttributes, dest, destAttributes, user,
						userAttributes, app, appAttributes, recipients, null, true, ICESdk.CE_NOISE_LEVEL_USER_ACTION);
				ceRequests.add(ceRequest);
			} else {
				throw new InvalidInputException("No from resource specified in the request");
			}
		}
		
		return ceRequests;
	}
	
	/**
	 * <p>
	 * Get the additional attributes from PDPRequest object
	 * </p>
	 *
	 * @param pdpRequest
	 * @param action
	 * @param app
	 * @param user
	 * @param recipients
	 * @return
	 */
	private CENamedAttributes[] getAdditionalAttributes(CERequest ceRequest, PDPRequest pdpRequest) {

		// add additional attributes
		IPDPNamedAttributes[] additionalData = pdpRequest.getAdditionalData();
		CENamedAttributes[] additionalAttrs = null;

		if (additionalData != null && additionalData.length > 0) {
			additionalAttrs = new CENamedAttributes[additionalData.length];

			for (int i = 0; i < additionalData.length; i++) {
				CENamedAttributes namedAttributes = new CENamedAttributes(additionalData[i].getName());

                                for (String key : additionalData[i].keySet()) {
                                    for (String value: additionalData[i].getValues(key)) {
                                        namedAttributes.add(key, value);
                                    }
                                }
                                
				additionalAttrs[i] = namedAttributes;
			}
		}

		// Join additional PQL into the already existing additional
		// attributes in request
			PolicyOnDemand policyOnDemand = pdpRequest.getPolicyOnDemand();
			if (policyOnDemand != null) {
				String additionalPQL = pdpRequest.getPolicyOnDemand().getPql();
				boolean ignoreBuiltinPolicies = pdpRequest.getPolicyOnDemand().isIgnoreBuildInPolicies();
				int newLength = (ceRequest.getAdditionalAttributes() == null ? 0
						: ceRequest.getAdditionalAttributes().length) + (additionalPQL != null ? 1 : 0);

				if (newLength != 0) {
					CENamedAttributes origAttrs[] = ceRequest.getAdditionalAttributes();
					additionalAttrs = new CENamedAttributes[newLength];

					if (origAttrs != null) {
						System.arraycopy(origAttrs, 0, additionalAttrs, 0, origAttrs.length);
					}

					if (additionalPQL != null) {
						CENamedAttributes policies = new CENamedAttributes("policies");
						policies.add("pql", additionalPQL);
						policies.add("ignoredefault", ignoreBuiltinPolicies ? "yes" : "no");

						additionalAttrs[additionalAttrs.length - 1] = policies;
					}
				}
			}

		return additionalAttrs;
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
                    if (!keysToSkip.contains(key)) {
                        for (String value : pdpNamedAttributes.getValues(key)) {
                            if (value != null) {
                                ceAttributes.add(key,value);
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
	
	private String[] getRecipients(String[] pdpRecipients){
		String[] recipients = null;
		if (pdpRecipients != null && pdpRecipients.length > 0) {
			recipients = new String[pdpRecipients.length];
			for (int l = 0; l < pdpRecipients.length; l++) {
				recipients[l] = pdpRecipients[l];
			}
		}
		return recipients;
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

	private StatusType getResponseStatus(String message,
			ResponseStatusCode statusCode) {
		StatusType status = new StatusType();
		status.setStatusMessage(message);

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
					attributeValue = "";
				}

				AttributeAssignmentType assignmentType = new AttributeAssignmentType();
				assignmentType.setAttributeId(attributeId);
				assignmentType.getContent().add(attributeValue);
				assignmentType.setCategory(XACMLParser.CATEGORY_OBLIGATION_ATTR_ASSIGNMENT);
				assignmentType.setDataType(XACMLParser.XACML_STRING_DATA_TYPE);

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

		} else if (ceResponse == CEResponse.DONTCARE) {
			result.setDecision(DecisionType.NOT_APPLICABLE);

		} else if (ceResponse == CEResponse.ERROR) {
			result.setDecision(DecisionType.INDETERMINATE);

		} else {
			result.setDecision(DecisionType.INDETERMINATE);
		}
		return result;
	}

	@Override
	public void closeConnection() {
		log.info("Nothing to close");
	}

}
