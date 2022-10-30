package com.nextlabs.pdpevalservice.eval;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.pdpapi.IPDPEnforcement;
import com.bluejungle.destiny.agent.pdpapi.PDPException;
import com.bluejungle.destiny.agent.pdpapi.PDPSDK;
import com.bluejungle.destiny.agent.pdpapi.PDPTimeout;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.nextlabs.pdpevalservice.filters.transform.EvalRequest;
import com.nextlabs.pdpevalservice.model.ObligationOrAdvice;
import com.nextlabs.pdpevalservice.model.Response;
import com.nextlabs.pdpevalservice.model.Result;
import com.nextlabs.pdpevalservice.model.Status;
import com.nextlabs.pdpevalservice.model.StatusCode;

public class APIEvalAdapter implements IEvalAdapter {
	
	private final Log log = LogFactory.getLog(APIEvalAdapter.class); 

	@Override
	public Response evaluate(EvalRequest req) {
		Response response = null;
		try {
			long startTime = System.currentTimeMillis();
			IPDPEnforcement enf = PDPSDK.PDPQueryDecisionEngine(req.getAction(),
					req.getResourceArr(),
			        req.getUser(),
			        req.getApplication(),
			        req.getHost(),
			        req.isPerformObligations(),
			        req.getAdditionalData(),
			        req.getNoiseLevel(),
			        req.getTimeoutInMins(),
			        req.getCallBack());
			log.info("Evaluation Completed");
			long endTime = System.currentTimeMillis();
			log.info("Time taken for evaluating the request: "+(endTime-startTime)+" ms");
			response = getResponse(enf);
		} catch (IllegalArgumentException e) {
			log.error("Illegal Argument Error occurred while evaluating request", e);
			response = ResponseObjCreationHelper.getErrorResponseObj(e.getMessage(), StatusCode.STATUSCODE_MISSING_ATTRIB);
		} catch (PDPTimeout e) {
			log.error("Timeout Error occurred while evaluating request", e);
			response = ResponseObjCreationHelper.getErrorResponseObj(e.getMessage(), StatusCode.STATUSCODE_PROC_ERROR);
		} catch (PDPException e) {
			log.error("Error occurred while evaluating request", e);
			response = ResponseObjCreationHelper.getErrorResponseObj(e.getMessage(), StatusCode.STATUSCODE_PROC_ERROR);
		} catch(Exception e){
			log.error("Error occurred while evaluating request", e);
			response = ResponseObjCreationHelper.getErrorResponseObj(e.getMessage(), StatusCode.STATUSCODE_PROC_ERROR);
		}
		return response;
	}

	private Response getResponse(IPDPEnforcement enf) {
		String decision = getDecisionStr(enf);
		Result result = new Result();
		result.setDecision(decision);
		Response response = new Response();
		response.setResult(result);
		Status status = ResponseObjCreationHelper.getStatusObj(null, StatusCode.STATUSCODE_OK);
		result.setStatus(status);
		//TODO: Associated Advice, Attributes and Policy Identifier list are not supported by PDP API right now - need to confirm.
		String[] obligationsArr = enf.getObligations();
		ObligationOrAdvice[] obligations = getObligations(obligationsArr);
		result.setObligations(obligations);
		return response;
	}
	
	private ObligationOrAdvice[] getObligations(String[] obligationsArr) {
		Map<String, String> obligationsMap = convertObligationsToMap(obligationsArr);
		return ResponseObjCreationHelper.getObligationArr(obligationsMap);
	}

    private static Map<String, String> convertObligationsToMap(String[] obligationsArr) {
        Map<String, String> res = new HashMap<String, String>();
        if (obligationsArr != null) {
            for (int i = 0; i < obligationsArr.length; i+=2) {
                res.put(obligationsArr[i], obligationsArr[i+1]);
            }
        }
        return res;
    }
	
	private String getDecisionStr(IPDPEnforcement enf) {
		String resultStr = Result.DECISION_NA;
		if(enf.getResult().equals(IDEffectType.ALLOW_NAME)){
			resultStr = Result.DECISION_PERMIT;
		}else if(enf.getResult().equals(IDEffectType.DENY_NAME)){
			resultStr = Result.DECISION_DENY;
		}//TODO: Need to handle Result.DECISION_INDETERMINATE 
		return resultStr;
	}

}
