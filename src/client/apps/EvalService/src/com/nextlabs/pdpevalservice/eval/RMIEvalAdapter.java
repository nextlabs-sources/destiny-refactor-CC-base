package com.nextlabs.pdpevalservice.eval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.nextlabs.destiny.sdk.CEApplication;
import com.nextlabs.destiny.sdk.CEAttributes;
import com.nextlabs.destiny.sdk.CEAttributes.CEAttribute;
import com.nextlabs.destiny.sdk.CEEnforcement;
import com.nextlabs.destiny.sdk.CEEnforcement.CEResponse;
import com.nextlabs.destiny.sdk.CEResource;
import com.nextlabs.destiny.sdk.CESdk;
import com.nextlabs.destiny.sdk.CESdkException;
import com.nextlabs.destiny.sdk.CEUser;
import com.nextlabs.destiny.sdk.ICESdk;
import com.nextlabs.pdpevalservice.filters.transform.EvalRequest;
import com.nextlabs.pdpevalservice.model.Attribute;
import com.nextlabs.pdpevalservice.model.ObligationOrAdvice;
import com.nextlabs.pdpevalservice.model.Response;
import com.nextlabs.pdpevalservice.model.Result;
import com.nextlabs.pdpevalservice.model.Status;
import com.nextlabs.pdpevalservice.model.StatusCode;

public class RMIEvalAdapter implements IEvalAdapter {

	private final Log log = LogFactory.getLog(RMIEvalAdapter.class); 
	
	private static final String KEY_RES_ID = "ce::id";
	
	private static final String KEY_RES_TYPE = "ce::destinytype";
	
	private static final String KEY_USER_ID = "id";
	
	private static final String KEY_USER_NAME = "name";
	
	private static final String KEY_APP_NAME = "name";
	
	private CESdk sdk = null;
	
	private long handle = 0;
	
	public RMIEvalAdapter(){
		initializeSDK();
	}

	private void initializeSDK() {
		sdk = new CESdk();
		int rmiPortNum = getRMIPortNumber();
		String hostName = getRemoteHostName();
		try {
			handle = sdk.Initialize(null, null, hostName, rmiPortNum, 10);
		} catch (CESdkException e) {
			log.error("Error occurred while initializing SDK", e);
		}
	}
	
	@Override
	public Response evaluate(EvalRequest req) {
		Response response = null;
		try{
			synchronized (this) {
				if(handle==0){
					//Handle is invalid. Try initializing again..
					initializeSDK();
				}
				if(handle == 0){
					log.error("CE SDK Initialization failed.");
					response = ResponseObjCreationHelper.getErrorResponseObj("CE SDK Initialization failed", StatusCode.STATUSCODE_PROC_ERROR);
					return response;
				}
			}
			CEApplication app = getApplication(req.getApplication());
			CEUser user = getUser(req.getUser());
			//TODO: Check what should be passed as recipients
			String[] recipients = null;
			//TODO check if we always need to use localhost
			int localHost = sdk.IPAddressToInteger("127.0.0.1");
			log.info("localHost IP=" + Integer.toHexString(localHost));
			String action = req.getAction();
			IPDPResource[]  resArr = req.getResourceArr();
			CEResource source = null;
			CEResource dest = null;
			for (IPDPResource res : resArr) {
				String dimensionVal = res.getName();
				if(Attribute.ATTRIBVAL_RES_DIMENSION_FROM.equals(dimensionVal)){
					source = getResource(res);
				}else if(Attribute.ATTRIBVAL_RES_DIMENSION_TO.equals(dimensionVal)) {
					dest = getResource(res);
				}
			}
			CEEnforcement enforcementResult = sdk.CheckResources(handle,
					action, 
					source, null, 
					dest, null, 
					user, null,
					app, null,
					recipients, 
					localHost, // ip address
					true, // perform obligations
					ICESdk.CE_NOISE_LEVEL_USER_ACTION, 
					10000);//timeout
			response = getResponse(enforcementResult);
			log.debug("Query result:"+enforcementResult.getResponseAsString());
//			System.out.println("Obligations:");
//			for (String s : enforcementResult.getObligations().toArray()) {
//				System.out.println(s);
//			}
//			String logIdentifier = "id";
//			String obligationName = "MyObligation";
//			String[] attributesArray = { "options", "aOptions", "desc",
//					"aDescription", "actions", "aUserActions" };
//			CEAttributes attributes = new CEAttributes(attributesArray);
//			System.out.println("Calling LogObligationData");
//			sdk.LogObligationData(handle, logIdentifier, obligationName,
//					attributes);
		}catch(CESdkException e){
			log.error("Error occurred while evaluating the request", e);
			response = ResponseObjCreationHelper.getErrorResponseObj(e.getMessage(), StatusCode.STATUSCODE_PROC_ERROR);
		}
		return response;
	}

	private Response getResponse(CEEnforcement enforcementResult) {
		Result result = getResult(enforcementResult.getResponse());
		ObligationOrAdvice[] obligations = getObligations(enforcementResult.getObligations());
		Status status = ResponseObjCreationHelper.getStatusObj(null, StatusCode.STATUSCODE_OK);
		result.setStatus(status);
		result.setObligations(obligations);
		Response res = new Response();
		res.setResult(result);
		return res;
	}
	
	private ObligationOrAdvice[] getObligations(CEAttributes obligations) {
		List<CEAttribute> attribList = obligations.getAttributes();
		Map<String, String> obligationsMap = new HashMap<String, String>();		
		for (CEAttribute ceAttribute : attribList) {
			obligationsMap.put(ceAttribute.getKey(), ceAttribute.getValue());
		}		
		return ResponseObjCreationHelper.getObligationArr(obligationsMap);
	}

	private Result getResult(CEResponse ceResponse) {
		Result res = new Result();		
		if(ceResponse == CEResponse.ALLOW){
			res.setDecision(Result.DECISION_PERMIT);
		}else if(ceResponse == CEResponse.DENY){
			res.setDecision(Result.DECISION_DENY);
		}else{
			res.setDecision(Result.DECISION_INDETERMINATE);
		}
		return res;
	}

	private CEApplication getApplication(IPDPApplication application) {
		String appName = application.getValue(KEY_APP_NAME);
		//TODO check how to get path of application
		CEApplication app = new CEApplication(appName, null/*path to application*/);
		return app;
	}

	private CEUser getUser(IPDPUser user) {
		String userId = user.getValue(KEY_USER_ID);
		String userName = user.getValue(KEY_USER_NAME);
		if(userName == null){
			userName = userId;
		}
		CEUser ceUser = new CEUser(userId, userName);
		return ceUser;
	}

	private CEResource getResource(IPDPResource res) {
		String resId = res.getValue(KEY_RES_ID);
		String resType = res.getValue(KEY_RES_TYPE);
		CEResource ceRes = new CEResource(resId, resType);
		return ceRes;
	}

	private int getRMIPortNumber() {
		ConfigHandler handler = ConfigHandler.getInstance();
		String rmiPort = handler.getParamValue(ConfigHandler.RMIPORT);
		if(rmiPort!=null && rmiPort.length()>0){
			try{
				int port = Integer.parseInt(rmiPort);
				return port;
			}catch(NumberFormatException e){
				log.error("Invalid port number specified. Using the default - 1099", e);
				return 1099;
			}
		}
		return 1099;
	}

	private String getRemoteHostName() {
		ConfigHandler handler = ConfigHandler.getInstance();
		String remoteHost = handler.getParamValue(ConfigHandler.RMIREMOTEHOSTNAME);
		if(remoteHost!=null && remoteHost.length()>0){
			return remoteHost;
		}
		return "localhost";
	}
	
	protected void finalize(){
		try {
			log.debug("About to close SDK");
			sdk.Close(handle, 100);
		} catch (CESdkException e) {
			log.error("Error occurred while closing the SDK", e);
		}
	}

}