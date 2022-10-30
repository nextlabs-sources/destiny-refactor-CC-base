package com.nextlabs.pdpevalservice.eval;

import com.nextlabs.pdpevalservice.filters.transform.EvalRequest;
import com.nextlabs.pdpevalservice.model.Response;

public interface IEvalAdapter {
	
	public Response evaluate(EvalRequest req);
	
}
