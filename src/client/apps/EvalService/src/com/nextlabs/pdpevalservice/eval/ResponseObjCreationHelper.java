package com.nextlabs.pdpevalservice.eval;

import java.util.Map;

import com.nextlabs.pdpevalservice.model.AttributeAssignment;
import com.nextlabs.pdpevalservice.model.ObligationOrAdvice;
import com.nextlabs.pdpevalservice.model.Response;
import com.nextlabs.pdpevalservice.model.Result;
import com.nextlabs.pdpevalservice.model.Status;
import com.nextlabs.pdpevalservice.model.StatusCode;

public class ResponseObjCreationHelper {

	public static ObligationOrAdvice[] getObligationArr(
			Map<String, String> obligationsMap) {
		int numObligations = getIntValue(obligationsMap.get(ObligationOrAdvice.ATTR_OBLIGATION_COUNT));
		if(numObligations<=0){
			return null;
		}
		
		ObligationOrAdvice[] obligationArr = new ObligationOrAdvice[numObligations];

        for (int i = 1; i <= numObligations; i++) {
        	//TODO Check if obligationId can be defined as an URI
            String obligationId = obligationsMap.get(ObligationOrAdvice.ATTR_OBLIGATION_NAME + ":" + i);
            int numArguments = getIntValue(obligationsMap.get(ObligationOrAdvice.ATTR_OBLIGATION_NUMVALUES + ":" + i));
            AttributeAssignment[] attributeArr = null;
            // The arguments come in key/value pairs
            if(numArguments == 1){
            	attributeArr = new AttributeAssignment[1];
            }else{                
                attributeArr = new AttributeAssignment[numArguments/2];            	
            }
            int index = 0;
            for (int j = 1; j <= numArguments; j+=2) {
            	AttributeAssignment asgmnt = new AttributeAssignment();
            	String attributeId = obligationsMap.get(ObligationOrAdvice.ATTR_OBLIGATION_VALUE + ":" + i + ":" + j);
            	String attributeValue = obligationsMap.get(ObligationOrAdvice.ATTR_OBLIGATION_VALUE + ":" + i + ":" + (j+1));
            	if(attributeValue==null||attributeValue.length()==0){
            		attributeValue = attributeId;
            		attributeId = null;
            	}
            	asgmnt.setAttributeId(attributeId);
            	asgmnt.setValue(attributeValue);
            	attributeArr[index++]=asgmnt;
            }
            ObligationOrAdvice obl = new ObligationOrAdvice();
            obl.setId(obligationId);
            obl.setAttributeAssignment(attributeArr);
            obligationArr[i-1]= obl;
        }
        return obligationArr;
	}

    private static int getIntValue(String i) {
        try {
            return Integer.parseInt(i);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
	public static Response getErrorResponseObj(String statusMessage, String statusCode) {
		Status status = getStatusObj(statusMessage, statusCode);
		Response response = new Response();
		Result result = new Result();
		result.setStatus(status);
		response.setResult(result);
		return response;
	}

	public static Status getStatusObj(String statusMessage, String statusCode) {
		Status status = new Status();
		if(statusMessage!=null){
			status.setStatusMessage(statusMessage);			
		}
		StatusCode sCode = new StatusCode();
		sCode.setValue(statusCode);
		status.setStatusCode(sCode);
		return status;
	}

}
