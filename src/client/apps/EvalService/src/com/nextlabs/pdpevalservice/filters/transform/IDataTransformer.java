package com.nextlabs.pdpevalservice.filters.transform;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.nextlabs.pdpevalservice.model.Response;

public interface IDataTransformer {
	
	public EvalRequest processRequest(ServletRequest req) throws InvalidRequestException, InvalidInputException;
	
	public void processResponse(ServletResponse servletRes, Response res);

	public void returnEndPoints(ServletResponse servletRes, String endPoint) throws IOException;

}
