package com.nextlabs.pdpevalservice.filters.transform;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.pdpevalservice.endpoints.EvalService;
import com.nextlabs.pdpevalservice.eval.ResponseObjCreationHelper;
import com.nextlabs.pdpevalservice.model.Response;
import com.nextlabs.pdpevalservice.model.StatusCode;

public class DataTransformationFilter implements Filter {
	
	private final Log log = LogFactory.getLog(DataTransformationFilter.class);
	
	public static final String ATTR_EVALREQUEST = "evalrequest";

	public static final String ATTR_EVALRESPONSE = "evalresponse";
	
	public static final String ATTR_PROC_STARTTIME = "proc_starttime";

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		boolean isInputValid = false;
		long startTime = System.currentTimeMillis();
		try {
			transformInput(req);
			isInputValid = true;
		} catch (InvalidRequestException e) {
			log.error("Invalid Request. Sending back status code 400");
			((HttpServletResponse)res).sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		} catch (InvalidInputException e) {
			log.error("Invalid input passed:" + e.getMessage());
			Response response = ResponseObjCreationHelper.getErrorResponseObj(e.getMessage(), StatusCode.STATUSCODE_MISSING_ATTRIB);
			req.setAttribute(ATTR_EVALRESPONSE, response);
		}
		if(isInputValid){
			chain.doFilter(req, res);			
		}
		try {
			transformOutput(req, res);
		} catch (InvalidRequestException e) {
			log.error("Invalid Request. Sending back status code 400");
			((HttpServletResponse)res).sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		} catch (IOException e) {
			log.error("Error occurred while transforming output. Sending back status code 400");
			((HttpServletResponse)res).sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}
		log.info("Time taken for processing Authorization request:"+(System.currentTimeMillis()-startTime) + "(ms)");
	}

	private void transformOutput(ServletRequest servletReq, ServletResponse servletRes) throws InvalidRequestException, IOException {
		long startTime = System.currentTimeMillis();
		log.info("Starting Transformation of Output");
		IDataTransformer transformer = getTransformer(servletReq);
		if(!EvalService.isEvaluationRequest(servletReq)){
			String endPoint = (String)servletReq.getAttribute(ATTR_EVALRESPONSE);
			transformer.returnEndPoints(servletRes, endPoint);
		}else{
			Response response = (Response)servletReq.getAttribute(ATTR_EVALRESPONSE);
			transformer.processResponse(servletRes, response);			
		}
		log.info("Transformation of Output completed");
		long timeTaken = System.currentTimeMillis() - startTime;
		log.debug("Time taken for transforming output:"+timeTaken+ " (ms)");
	}

	private void transformInput(ServletRequest req) throws InvalidRequestException, InvalidInputException {
		if(!EvalService.isEvaluationRequest(req)){
			log.info("Not an evaluation request. No input to transform");
			return;
		}
		long startTime = System.currentTimeMillis();
		log.info("Starting Transformation of Input");
		IDataTransformer transformer = getTransformer(req);
		if(transformer==null){
			log.error("Invalid Content Type set in HTTPRequest");
			throw new InvalidRequestException("Invalid Content Type set in HTTPRequest");
		}
		EvalRequest requestObj = transformer.processRequest(req);
		req.setAttribute(ATTR_EVALREQUEST, requestObj);
		log.info("Transformation of Input completed");
		long timeTaken = System.currentTimeMillis() - startTime;
		log.debug("Time taken for transforming input:"+timeTaken+ " (ms)");
	}

	private IDataTransformer getTransformer(ServletRequest req) throws InvalidRequestException {
		String contentType = req.getContentType();
		IDataTransformer transformer = null;
		if(contentType==null ){
			if(EvalService.isEvaluationRequest(req)){
				log.error("Content Type not set in HTTPRequest");
				throw new InvalidRequestException("Content Type not set in HTTPRequest");
			}else{
				String dataType = req.getParameter(EvalService.PARAM_DATATYPE);
				if(dataType == null){
					log.error("Parameter DATATYPE not passed in URL");
					throw new InvalidRequestException("Parameter DATATYPE not passed in URL");				
				}else{
					if(dataType.equalsIgnoreCase(DataTransformerFactory.DATATYPE_JSON)){
						contentType = DataTransformerFactory.CONTENTTYPE_JSON;
					}else if(dataType.equalsIgnoreCase(DataTransformerFactory.DATATYPE_XACML)){
						contentType = DataTransformerFactory.CONTENTTYPE_XACML_XML;
					}else{
						log.error("Invalid DATATYPE passed in URL");
						throw new InvalidRequestException("Invalid DATATYPE passed in URL");					
					}
				}
			}
		}
		transformer = DataTransformerFactory.getInstance().getDataTransformer(contentType);
		return transformer;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}