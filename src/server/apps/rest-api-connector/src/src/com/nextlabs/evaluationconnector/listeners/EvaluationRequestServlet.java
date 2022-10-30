/*
 * Created on Jan 19, 2015
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.evaluationconnector.listeners;

import static com.nextlabs.evaluationconnector.utils.Constants.CONTENT_TYPE_JSON;
import static com.nextlabs.evaluationconnector.utils.Constants.CONTENT_TYPE_XML;
import static com.nextlabs.evaluationconnector.utils.Constants.DATA_TYPE_PARAM;
import static com.nextlabs.evaluationconnector.utils.Constants.ENTRY_POINT_SERVICE;
import static com.nextlabs.evaluationconnector.utils.Constants.EVAL_REQ_SERVICE;
import static com.nextlabs.evaluationconnector.utils.Constants.SERVICE_TYPE_PARAM;
import static com.nextlabs.evaluationconnector.utils.Constants.VERSION_PARAM;
import static com.nextlabs.evaluationconnector.utils.Constants.XACML_DATA_PARAM;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.nextlabs.evaluationconnector.adaptors.PDPAdaptorFactory;
import com.nextlabs.evaluationconnector.beans.ResponseStatusCode;
import com.nextlabs.evaluationconnector.beans.XACMLResponse;
import com.nextlabs.evaluationconnector.beans.json.ResponseWrapper;
import com.nextlabs.evaluationconnector.exceptions.EvaluationConnectorException;
import com.nextlabs.evaluationconnector.parsers.JSONEvalResponse;
import com.nextlabs.evaluationconnector.utils.Constants;
import com.nextlabs.evaluationconnector.utils.PropertiesUtil;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResponseType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ResultType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusCodeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.StatusType;

/**
 * <p>
 * EvaluationRequestServlet <br/>
 * All the valid requests are received by this servlet.
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class EvaluationRequestServlet extends HttpServlet {

	private static final long serialVersionUID = -2369335666339153274L;

	private static final Log log = LogFactory
			.getLog(EvaluationRequestServlet.class);

	@Override
	public void init() throws ServletException {
		log.info("Initializing the EvaluationRequestServlet - Started");
		String path = getServletContext().getInitParameter("PropertiesFile");
		try {
			final InputStream inStream = getServletContext()
					.getResourceAsStream(path);
			PropertiesUtil.init(inStream);

			log.info("Initializing the EvaluationRequestServlet - Done");
		} catch (Exception e) {
			log.error("Initializing the EvaluationRequestServlet - Failed", e);
			throw new ServletException(e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getRequestURI().contains("/pdp") || req.getRequestURI().contains("/go")) {
			RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/help.html");
			dispatcher.forward(req, resp);
		} else {
			processEntryPointRequest(req, resp);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		if (req.getRequestURI().contains("/pdp") || req.getRequestURI().contains("/go")) {
			doProcess(req, resp);		
		} else {	
			processEntryPointRequest(req, resp);
		}
	}

	/**
	 * <p>
	 * This will process GET and POST the HTTP requests to this servlet.
	 * </p>
	 *
	 * @param request
	 *            {@link HttpServletRequest}
	 * @param response
	 *            {@link HttpServletResponse}
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void doProcess(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (log.isDebugEnabled())
			log.info("Evalution Request came to process");

		long startTime = System.nanoTime();

		try {
			if(isRESTRequest(request)) {
				handleREST(request, response);
			} else {
				handleParamBasePost(request, response);
			}
		} catch (EvaluationConnectorException e) {
			log.error("Error occurred in Evaluation Connector Request processing, ", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, e.getMessage());

		} catch (Exception e) {
			log.error("Error occurred in doProcess , ", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR, e.getMessage());

		} finally {
			long endTime = System.nanoTime();
			if (log.isInfoEnabled())
				log.info(String.format(
						"Evalution Request successfully processed, [ Response : %d, Time taken : %d  nano]",
						response.getStatus(), (endTime - startTime)));
		}
	}
	
	private void handleREST(HttpServletRequest request, HttpServletResponse response)
			throws EvaluationConnectorException, IOException, ServletException {
		if (isValidREST(request)) {
			String serviceType = request.getHeader(SERVICE_TYPE_PARAM);
			String dataType = (request.getContentType().equals(CONTENT_TYPE_JSON)) ? "json" : "xml";
			String data = getRequestBodyData(request, response);

			if (serviceType.equalsIgnoreCase(EVAL_REQ_SERVICE)) {
				getEvaluationRequestListener().onReceived(request, response, dataType, data);
			} else if (serviceType.equalsIgnoreCase(ENTRY_POINT_SERVICE)) {

			}
		} else {
			handleInvalidRequest(request, response);
		}
	}

	private String getRequestBodyData(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		StringBuffer buffer = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				buffer.append(line);
		} catch (Exception e) {
			log.error("Error encountered in reading the request body", e);
		}

		if (buffer.toString().isEmpty()) {
			handleInvalidRequest(request, response, "No Request body found or error in reading request body");
		}
		return buffer.toString();
	}

	private void handleParamBasePost(HttpServletRequest request, HttpServletResponse response)
			throws EvaluationConnectorException, IOException, ServletException {
		if (isValidParamRequest(request)) {
			String serviceType = request.getParameter(SERVICE_TYPE_PARAM);
			String dataType = request.getParameter(DATA_TYPE_PARAM);
			String data = request.getParameter(Constants.XACML_DATA_PARAM);

			if (serviceType.equalsIgnoreCase(EVAL_REQ_SERVICE)) {
				getEvaluationRequestListener().onReceived(request, response, dataType, data);
			} else if (serviceType.equalsIgnoreCase(ENTRY_POINT_SERVICE)) {

			}
		} else {
			handleInvalidRequest(request, response);
		}
	}
	
	/**
	 * <p>
	 * Processes GET request, server responds by sending a standard XACML
	 * response (in XACML format) specifying the end points
	 * </p>
	 * 
	 * @param request
	 * 			  {@link HttpServletRequest}
	 * @param response
	 * 			 {@link HttpServletResponse}
	 * @throws ServletException
	 * @throws IOException
	 */
	private void processEntryPointRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter pWriter = response.getWriter();

		String responseStr = PropertiesUtil.getString("pdp.connector.get.request.std.response");
		response.setContentType(CONTENT_TYPE_XML);

		pWriter.println(responseStr);
		pWriter.flush();
		pWriter.close();
	}
	
	
	private boolean isRESTRequest(HttpServletRequest request) {
		String data = request.getParameter(XACML_DATA_PARAM);
		if(data == null) {
			return true;
		}
		return false;
	}
	
	
	private boolean isValidREST(HttpServletRequest request) {
		boolean isValid = true;
		
		String serviceType = request.getHeader(SERVICE_TYPE_PARAM);
		String dataType = request.getContentType();
		String version = request.getHeader(VERSION_PARAM);

		if ((serviceType == null || "".equals(serviceType)) || (dataType == null || "".equals(dataType)) 
				|| (version == null || "".equals(version))) {
			isValid = false;
		}

		return isValid;
	}
	
	/**
	 * <p>
	 * Checks if the given POST request is valid
	 * </p>
	 *
	 * @param request
	 *            {@link HttpServletRequest}
	 * @return true/false
	 */
	private boolean isValidParamRequest(HttpServletRequest request) {

		boolean isValid = true;

		String serviceType = request.getParameter(SERVICE_TYPE_PARAM);
		String dataType = request.getParameter(DATA_TYPE_PARAM);
		String data = request.getParameter(XACML_DATA_PARAM);
		String version = request.getParameter(VERSION_PARAM);

		if ((serviceType == null || "".equals(serviceType)) || (dataType == null || "".equals(dataType))
				|| (data == null || "".equals(data)) || (version == null || "".equals(version))) {
			isValid = false;
		}

		return isValid;
	}
	
	private void handleInvalidRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		handleInvalidRequest(request, response, null);
	}
	
	/**
	 * 
	 * <p>
	 * Handles invalid/incorrect POST requests made to the server
	 * </p>
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleInvalidRequest(HttpServletRequest request, HttpServletResponse response, String errorMessage)
			throws ServletException, IOException {

		log.debug("Should return valid XACML response with appropriate error message.");

		long startCounter = System.nanoTime();
		JSONEvalResponse evalResponse = new JSONEvalResponse();
		XACMLResponse xacmlResponse = new XACMLResponse();
		xacmlResponse.setRequestStartTime(startCounter);
		
		String errorMsg = (errorMessage == null) ? "Bad Request - One or more attributes are missing" : errorMessage;
		ResponseType responseType = getErrorResponse(errorMsg, ResponseStatusCode.MISSING_ATTRIB);
		xacmlResponse.setResponseType(responseType);
		xacmlResponse.setStatus(ResponseStatusCode.MISSING_ATTRIB.getValue());

		ResponseWrapper responseWrapper = evalResponse.populateJsonResponse(xacmlResponse);
		Gson jsonSerializer = new Gson();
		String responseString = jsonSerializer.toJson(responseWrapper);

		response.setContentType(CONTENT_TYPE_JSON);
		response.setContentLength(responseString.length());

		PrintWriter pWriter = response.getWriter();
		pWriter.write(responseString);
		pWriter.flush();
		pWriter.close();

	}
	
	private ResponseType getErrorResponse(String errorMessage, ResponseStatusCode statusCode) {
		ResponseType response = new ResponseType();

		ResultType result = new ResultType();
		response.getResult().add(result);
		result.setDecision(DecisionType.INDETERMINATE);
		StatusType status = getResponseStatus(errorMessage, statusCode);
		result.setStatus(status);

		return response;
	}

	protected StatusType getResponseStatus(String errorMessage,
			ResponseStatusCode statusCode) {
		StatusType status = new StatusType();
		status.setStatusMessage(errorMessage);

		StatusCodeType codeType = new StatusCodeType();
		codeType.setValue(statusCode.getValue());
		status.setStatusCode(codeType);

		return status;
	}

	@Override
	public void destroy() {
		super.destroy();
		PDPAdaptorFactory.closeConnection();
	}

	/**
	 * <p>
	 * Create new EvaluationRequestListener for every new request.
	 * </p>
	 *
	 * @return {@link EvaluationRequestListener}
	 */
	private EvaluationRequestListener getEvaluationRequestListener() {

		return new EvaluationRequestListener();
	}

}
