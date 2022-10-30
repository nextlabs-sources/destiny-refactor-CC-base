package com.nextlabs.pdpevalservice.filters.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPHost;
import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.bluejungle.destiny.agent.pdpapi.PDPApplication;
import com.bluejungle.destiny.agent.pdpapi.PDPHost;
import com.bluejungle.destiny.agent.pdpapi.PDPResource;
import com.bluejungle.destiny.agent.pdpapi.PDPUser;
import com.nextlabs.pdpevalservice.model.Attribute;

public class PDPObjectConversionHelper {
	
	private static final Log log = LogFactory.getLog(PDPObjectConversionHelper.class);

	public static IPDPResource getResourceObj(Map<String, String> attributeMap) throws InvalidInputException {
		try{
			//TODO check if the platform has defined constants for "from" and "to" 
			String resDimension = Attribute.ATTRIBVAL_RES_DIMENSION_FROM;
			String resId = attributeMap.get(Attribute.ATTRIBUTEID_RESOURCEID);
			String resType = attributeMap.get(Attribute.ATTRIBUTEID_RESOURCETYPE);
			if(resType==null){
				resType = Attribute.ATTRIBVAL_RES_DEFAULTTYPE;
			}
			if(attributeMap.get(Attribute.ATTRIBUTEID_RESOURCE_DIMENSION)!=null || 
					Attribute.ATTRIBVAL_RES_DIMENSION_TO.equalsIgnoreCase(attributeMap.get(Attribute.ATTRIBUTEID_RESOURCE_DIMENSION))){
				resDimension = Attribute.ATTRIBVAL_RES_DIMENSION_TO;
			}		
			PDPResource res = new PDPResource(resDimension, resId, resType);
			List<String> keysToSkip = new ArrayList<String>();
			keysToSkip.add(Attribute.ATTRIBUTEID_RESOURCEID);
			keysToSkip.add(Attribute.ATTRIBUTEID_RESOURCETYPE);
			addAttributesWithException(res, attributeMap, keysToSkip);
			return res;
		}catch(Exception e){
			log.error("Error occurred while creating 'Resource' object", e);
			throw new InvalidInputException("Unable to create instance of 'Resource': "+e.getMessage());			
		}
	}

	public static void addAttributesWithException(IPDPNamedAttributes namedObject, 
			Map<String, String> attributeMap, List<String> keysToSkip) {
        for (String key : attributeMap.keySet()) {
            if (null!=key && !keysToSkip.contains(key)) {
                String value = (String)attributeMap.get(key);
                if(value!=null){
                	String simpleKey = getSimplifiedAttribName(key);
                	namedObject.setAttribute(simpleKey, value);
                }
            }
        }
	}
	
	private static String getSimplifiedAttribName(String attributeName) {
		int index = attributeName.lastIndexOf(':');
        if (index != -1) {
            return attributeName.substring(index+1);
        }
        return attributeName;
	}
	
	public static IPDPApplication getApplicationObj(Map<String, String> attributeMap) throws InvalidInputException {
		try{
			String appId = attributeMap.get(Attribute.ATTRIBUTEID_APP_ID);
			IPDPApplication app = new PDPApplication(appId);
			List<String> keysToSkip = new ArrayList<String>();
			keysToSkip.add(Attribute.ATTRIBUTEID_APP_ID);
			addAttributesWithException(app, attributeMap, keysToSkip);
			return app;
		}catch(Exception e){
			log.error("Error occurred while creating 'Application' object", e);
			throw new InvalidInputException("Unable to create instance of 'Application': "+e.getMessage());			
		}
	}

	public static IPDPUser getUserObj(Map<String, String> attributeMap) throws InvalidInputException {
		try{
			String userId = attributeMap.get(Attribute.ATTRIBUTEID_USER_ID);
			IPDPUser user = new PDPUser(userId);
			List<String> keysToSkip = new ArrayList<String>();
			keysToSkip.add(Attribute.ATTRIBUTEID_USER_ID);
			addAttributesWithException(user, attributeMap, keysToSkip);
			return user;			
		}catch(Exception e){
			log.error("Error occurred while creating 'User' object", e);
			throw new InvalidInputException("Unable to create instance of 'User': "+e.getMessage());
		}
	}
	
	public static IPDPHost getHost() {
        // Use localhost
		//TODO: Check why localhost is hardcoded
		int ip = 2130706433;
		return new PDPHost(ip);
//		CESdk sdk = new CESdk();
//		PDPHost host = null;
//		try {
//			host = new PDPHost(sdk.IPAddressToInteger("127.0.0.1"));
//		} catch (CESdkException e) {
//			e.printStackTrace();
//		}
//		return host;
	}

	public static EvalRequest getEvalRequestObj(List<IPDPResource> resList,
			String action, IPDPUser user, IPDPApplication application) {
		IPDPResource[] resourceArr = resList.toArray(new IPDPResource[resList.size()]);
		IPDPHost host = PDPObjectConversionHelper.getHost();
		EvalRequest evalReq = new EvalRequest();
		evalReq.setAction(action);
		evalReq.setAdditionalData(null);
		evalReq.setApplication(application);
		evalReq.setHost(host);
		evalReq.setResourceArr(resourceArr);
		evalReq.setUser(user);
		return evalReq;
	}

}
