package com.nextlabs.pdpevalservice.filters.transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.bluejungle.destiny.agent.pdpapi.PDPApplication;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nextlabs.pdpevalservice.model.Action;
import com.nextlabs.pdpevalservice.model.Attribute;
import com.nextlabs.pdpevalservice.model.Category;
import com.nextlabs.pdpevalservice.model.Link;
import com.nextlabs.pdpevalservice.model.Request;
import com.nextlabs.pdpevalservice.model.RequestWrapper;
import com.nextlabs.pdpevalservice.model.Resource;
import com.nextlabs.pdpevalservice.model.Resources;
import com.nextlabs.pdpevalservice.model.Response;
import com.nextlabs.pdpevalservice.model.ResponseWrapper;
import com.nextlabs.pdpevalservice.model.Subject;

public class JSONDataTransformer implements IDataTransformer {

	private final Log log = LogFactory.getLog(JSONDataTransformer.class);
	
	@Override
	public EvalRequest processRequest(ServletRequest req) throws InvalidRequestException, InvalidInputException {
		StringBuffer buff = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = req.getReader();
			while ((line = reader.readLine()) != null)
				buff.append(line);
		} catch (Exception e) {
			log.error("Error occurred while reading JSON content from Request", e);
			throw new InvalidRequestException("Error occurred while reading JSON content from Request");
		}
		log.debug("Input JSON string:"+buff.toString());
		Gson gson = new Gson();
		RequestWrapper wrapper = null;
		try{
			wrapper = gson.fromJson(buff.toString(), RequestWrapper.class);
		}catch(Throwable e){
			log.error("Error occurred while converting input into JSON", e);
			throw new InvalidRequestException("Error occurred while converting input into JSON");
		}
		if(wrapper == null){
			log.error("Empty input object");
			throw new InvalidRequestException("Empty input object");			  
		}
		log.debug("JSON input transformation completed");
		Request request = wrapper.getRequest();
		EvalRequest evalReq = generateEvalReqObject(request);		  
		return evalReq;
	}

	private EvalRequest generateEvalReqObject(Request req) throws InvalidRequestException, InvalidInputException {		
		Category[] catArr = req.getCategory();
		List<IPDPResource> resList = new ArrayList<IPDPResource>();
		String action = null;
		IPDPUser user = null;
		IPDPApplication application = PDPApplication.NONE;
		for (Category category : catArr) {
			if(Category.CATEGORYID_RESOURCE.equalsIgnoreCase(category.getCategoryId())){
				resList.add(getResource(category));
			}else if (Category.CATEGORYID_ACTION.equalsIgnoreCase(category.getCategoryId())){
				action = getAction(category);
			}else if (Category.CATEGORYID_SUBJECT.equalsIgnoreCase(category.getCategoryId())){
				user = getUser(category);
			}else if(Category.CATEGORYID_ENVIRONMENT.equalsIgnoreCase(category.getCategoryId())){
				//TODO:Handle Environment
			}else if(Category.CATEGORYID_APPLICATION.equalsIgnoreCase(category.getCategoryId())){
				application = getApplication(category);
			}
		}
		if(req.getAction()!=null && req.getAction().length>0){
			if(action!=null){
				throw new InvalidInputException("Action has already been specified in the 'Category' section");
			}
			Action[] actionArr = req.getAction();
			for (Action act : actionArr) {
				//TODO: Handle if multiple actions passed 
				action = getAction(act);
			}
		}
		if(req.getResource()!=null && req.getResource().length>0){
			if(resList.size()>0){
				throw new InvalidInputException("Resource has already been specified in the 'Category' section"); 
			}
			Resource[] resArr = req.getResource();
			for (Resource resource : resArr) {
				resList.add(getResource(resource));
			}
		}
		if(req.getSubject()!=null && req.getSubject().length>0){
			if(user!=null){
				throw new InvalidInputException("Subject has already been specified in the 'Category' section");
			}
			Subject[] subArr = req.getSubject();
			for (Subject subject : subArr) {
				//TODO: Handle if multiple Subjects passed
				user = getUser(subject);
			}
		}
		if(req.getEnvironment()!=null && req.getEnvironment().length>0){
			//TODO: Handle Environment
		}
		return PDPObjectConversionHelper.getEvalRequestObj(resList, action, user, application);
	}

	private IPDPResource getResource(Category category) throws InvalidInputException {
		Map<String, String> attributeMap = getAttributeMap(category);
		return PDPObjectConversionHelper.getResourceObj(attributeMap);
	}

	private String getAction(Category category) {
		Attribute[] attribArr = category.getAttribute();
		if(attribArr==null){
			return null;
		}
		for (Attribute attribute : attribArr) {
			if(Attribute.ATTRIBUTEID_ACTION_ID.equalsIgnoreCase(attribute.getAttributeId())){
				return (String)attribute.getValue();
			}
		}
		return null;
	}

	private IPDPApplication getApplication(Category category) throws InvalidInputException {
		Map<String, String> attributeMap = getAttributeMap(category);
		return PDPObjectConversionHelper.getApplicationObj(attributeMap);
	}

	private Map<String, String> getAttributeMap(Category category) {
		Attribute[] attribArr = category.getAttribute();
		Map<String, String> attributeMap = new HashMap<String, String>();
		for (Attribute attribute : attribArr) {
			attributeMap.put(attribute.getAttributeId(), (String)attribute.getValue());
		}
		return attributeMap;
	}

	private IPDPUser getUser(Category category) throws InvalidInputException {
		Map<String, String> attributeMap = getAttributeMap(category);
		return PDPObjectConversionHelper.getUserObj(attributeMap);
	}

	@Override
	public void processResponse(ServletResponse servletRes, Response res) {
		if(res==null){
			log.info("Response object is null. Nothing to tranform.");
			return;
		}
		ResponseWrapper wrapper = new ResponseWrapper();
		wrapper.setResponse(res);
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String jsonStr = gson.toJson(wrapper);
		log.debug("Output JSON string:"+jsonStr);
		servletRes.setContentType("application/json");
		try {
			servletRes.getWriter().write(jsonStr);
		} catch (IOException e) {
			log.error("Error occurred while writing response to the stream", e);
			try {
				((HttpServletResponse)servletRes).sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			} catch (IOException e1) {
				log.error("Error occurred while sending error response", e);
			}
		}
	}

	@Override
	public void returnEndPoints(ServletResponse servletRes, String endPoint)
			throws IOException {
		Resources res = new Resources();
		Link link = new Link();
		link.setHref(endPoint);
		Link[] linkArr = {link};
		res.setLink(linkArr);
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String jsonStr = gson.toJson(res);
		log.debug("Output JSON string:"+jsonStr);
		servletRes.setContentType("application/json");
		try {
			servletRes.getWriter().write(jsonStr);
		} catch (IOException e) {
			log.error("Error occurred while writing response to the stream", e);
			((HttpServletResponse)servletRes).sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}

}