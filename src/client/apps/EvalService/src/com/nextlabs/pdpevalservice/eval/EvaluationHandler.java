package com.nextlabs.pdpevalservice.eval;

import com.nextlabs.pdpevalservice.filters.transform.EvalRequest;
import com.nextlabs.pdpevalservice.model.Response;

public class EvaluationHandler {
	
	public Response processRequest(EvalRequest req, String commType){
		IEvalAdapter evalAdapter = EvaluationAdapterFactory.getInstance().getAdapter(commType);
		Response res = evalAdapter.evaluate(req);
		return res;
	}

}
