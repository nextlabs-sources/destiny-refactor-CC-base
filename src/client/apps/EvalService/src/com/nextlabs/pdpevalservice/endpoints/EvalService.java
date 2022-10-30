package com.nextlabs.pdpevalservice.endpoints;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextlabs.pdpevalservice.eval.ConfigHandler;
import com.nextlabs.pdpevalservice.eval.EvaluationAdapterFactory;
import com.nextlabs.pdpevalservice.eval.EvaluationHandler;
import com.nextlabs.pdpevalservice.filters.transform.DataTransformationFilter;
import com.nextlabs.pdpevalservice.filters.transform.EvalRequest;
import com.nextlabs.pdpevalservice.model.Response;

public class EvalService extends HttpServlet {
	
	private final Log log = LogFactory.getLog(EvalService.class); 
	
	public static final String PARAM_GETENTRYPOINTS = "GETENTRYPOINTS";
			
	public static final String PARAM_SERVICE = "SERVICE";
	
	public static final String PARAM_DATATYPE = "DATATYPE";
	
	public static final String EVAL_LINK = "/EvalService/pdp";

	private static final long serialVersionUID = -7898229565056339247L;
	
	public static final String DEPLOYMENT_MODE = "DEPLOYMENT_MODE";
	
	public static final String DEP_MODE_EMBEDDED = "EMBEDDED";
	
	public static final String DEP_MODE_EXTERNAL = "EXTERNAL";
	
	public void init() throws ServletException{
		super.init();
		ConfigHandler handler = ConfigHandler.getInstance();
		handler.processParams(getServletContext());
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response){
		processReq(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response){
		processReq(request, response);
	}

	private void processReq(HttpServletRequest request, HttpServletResponse response) {
		try {
			String requestURI = request.getRequestURI();
			log.info("Request URI:"+requestURI);			
			if(isEvaluationRequest(request)){
				log.info("About to process Evaluation Request");
				handleEvalRequest(request);				
			}else{
				if(PARAM_GETENTRYPOINTS.equalsIgnoreCase(request.getParameter(PARAM_SERVICE))){
					log.info("About to return end points");
					String endPoint = getEndPoint(request.getContextPath());
					request.setAttribute(DataTransformationFilter.ATTR_EVALRESPONSE, endPoint);
					return;
				}
				//handle other requests if needed
			}
		} catch (Exception e) {
			//TODO:Return proper error code
			log.error("Error occured while processing request", e);
		}
	}
	
	public static boolean isEvaluationRequest(ServletRequest req){
		HttpServletRequest request = (HttpServletRequest)req;
		String requestURI = request.getRequestURI();
		if(!requestURI.equalsIgnoreCase(EvalService.getEndPoint(request.getContextPath()))){
			return false;
		}
		return true;
	}
	
	public static String getEndPoint(String contextPath) {
		return contextPath+EvalService.EVAL_LINK;
	}

	private void handleEvalRequest(HttpServletRequest request) {
		EvaluationHandler handler = new EvaluationHandler();
		EvalRequest req = (EvalRequest) request.getAttribute(DataTransformationFilter.ATTR_EVALREQUEST);
		String commType = getCommType();
		Response res = handler.processRequest(req, commType);
		request.setAttribute(DataTransformationFilter.ATTR_EVALRESPONSE, res);
	}

	private String getCommType() {
		String depMode = getDeploymentMode();
		if(DEP_MODE_EXTERNAL.equals(depMode)){
			return EvaluationAdapterFactory.COMM_TYPE_RMI;
		}
		return EvaluationAdapterFactory.COMM_TYPE_API;
	}

	private String getDeploymentMode(){
		String deploymentMode = getServletContext().getInitParameter(DEPLOYMENT_MODE);
		if(deploymentMode!=null && DEP_MODE_EXTERNAL.equalsIgnoreCase(deploymentMode)){
			return DEP_MODE_EXTERNAL;
		}
		return DEP_MODE_EMBEDDED;
	}
	
}
