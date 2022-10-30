package com.nextlabs.pdpevalservice.filters.transform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import x0CoreSchemaWd17.oasisNamesTcXacml3.AttributeAssignmentType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.AttributeType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.AttributeValueType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.AttributesType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.DecisionType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.ObligationType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.ObligationsType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.RequestDocument;
import x0CoreSchemaWd17.oasisNamesTcXacml3.ResponseType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.ResultType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.StatusCodeType;
import x0CoreSchemaWd17.oasisNamesTcXacml3.StatusType;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;
import com.bluejungle.destiny.agent.pdpapi.PDPApplication;
import com.nextlabs.pdpevalservice.model.Attribute;
import com.nextlabs.pdpevalservice.model.AttributeAssignment;
import com.nextlabs.pdpevalservice.model.Category;
import com.nextlabs.pdpevalservice.model.ObligationOrAdvice;
import com.nextlabs.pdpevalservice.model.Response;
import com.nextlabs.pdpevalservice.model.Result;
import com.nextlabs.pdpevalservice.model.Status;
import com.nextlabs.pdpevalservice.model.StatusCode;

public class XACMLDataTransformer implements IDataTransformer {
	
	private static String endPointXML = "<resources xmlns=\"http://ietf.org/ns/home-documents\" xmlns:atom=\"http://www.w3.org/2005/Atom\">" +
			"<resource rel=\"http://docs.oasis-open.org/ns/xacml/relation/pdp\">" +
			"<atom:link href=\"$ENDPOINT$\"/>" +
			"</resource>" +
			"</resources>";

	private final Log log = LogFactory.getLog(XACMLDataTransformer.class);

	@Override
	public EvalRequest processRequest(ServletRequest req) throws InvalidRequestException, InvalidInputException{
		StringBuffer buff = new StringBuffer();
		String line = null;
		BufferedReader reader = null;
		try {
			reader = req.getReader();
			while ((line = reader.readLine()) != null)
				buff.append(line);
		} catch (Exception e) {
			log.error("Error occurred while reading XACML content from Request", e);
			throw new InvalidRequestException("Error occurred while reading XACML content from Request");
		} finally{
			try {
				reader.close();
			} catch (IOException e) {
				log.error("Error occurred while closing BufferedReader", e);
			}
		}
		log.debug("Input XACML string:"+buff.toString());
		RequestDocument requestDoc = null;
		try {
			requestDoc = RequestDocument.Factory.parse(buff.toString());
		} catch (XmlException e) {
			log.error("Error occurred while parsing XACML content from Request", e);
			throw new InvalidRequestException("Error occurred while parsing XACML content from Request");
		}
		AttributesType[] attributesArr = requestDoc.getRequest().getAttributesArray();
		EvalRequest request = getEvalRequestObj(attributesArr);
		return request;
	}
	
	private EvalRequest getEvalRequestObj(AttributesType[] attributesArr) throws InvalidInputException{
		List<IPDPResource> resList = new ArrayList<IPDPResource>();
		String action = null;
		IPDPUser user = null;
		IPDPApplication application = PDPApplication.NONE;
		for (AttributesType attributes : attributesArr) {
			if(Category.CATEGORYID_RESOURCE.equalsIgnoreCase(attributes.getCategory())){
				resList.add(getResource(attributes));
			}else if(Category.CATEGORYID_ACTION.equalsIgnoreCase(attributes.getCategory())){
				action = getAction(attributes);
			}else if(Category.CATEGORYID_SUBJECT.equalsIgnoreCase(attributes.getCategory())){
				user = getUser(attributes);
			}else if(Category.CATEGORYID_ENVIRONMENT.equalsIgnoreCase(attributes.getCategory())){
				//TODO: Handle environment
			}else if(Category.CATEGORYID_APPLICATION.equalsIgnoreCase(attributes.getCategory())){
				application = getApplication(attributes);
			}
		}
		return PDPObjectConversionHelper.getEvalRequestObj(resList, action, user, application);
	}

	private IPDPUser getUser(AttributesType attributes) throws InvalidInputException {
		Map<String, String> attributeMap = getAttributeMap(attributes);
		return PDPObjectConversionHelper.getUserObj(attributeMap);
	}

	private IPDPApplication getApplication(AttributesType attributes) throws InvalidInputException {
		Map<String, String> attributeMap = getAttributeMap(attributes);
		return PDPObjectConversionHelper.getApplicationObj(attributeMap);
	}

	private String getAction(AttributesType attributes) {
		AttributeType[] attribArr = attributes.getAttributeArray();		
		for (AttributeType attribute : attribArr) {
			if(Attribute.ATTRIBUTEID_ACTION_ID.equalsIgnoreCase(attribute.getAttributeId())){
				String nodeVal = getAttributeValue(attribute);
				return nodeVal;
			}
		}
		return null;
	}

	private IPDPResource getResource(AttributesType attributes) throws InvalidInputException {
		Map<String, String> attributeMap = getAttributeMap(attributes);
		return PDPObjectConversionHelper.getResourceObj(attributeMap);
	}

	private Map<String, String> getAttributeMap(AttributesType attributes) {
		AttributeType[] attributeArr = attributes.getAttributeArray();
		Map<String, String> attributeMap = new HashMap<String, String>();
		for (AttributeType attribute : attributeArr) {
			String nodeVal = getAttributeValue(attribute);
			attributeMap.put(attribute.getAttributeId(), nodeVal);

		}
		return attributeMap;
	}

	private String getAttributeValue(AttributeType attributeType) {
		AttributeValueType val = attributeType.getAttributeValueArray()[0];
		NodeList childNodes = val.getDomNode().getChildNodes();
		String nodeVal = "";
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if(node.getNodeType() == Node.TEXT_NODE && node.getNodeValue()!=null){
				nodeVal = node.getNodeValue().trim();
			}
		}
		return nodeVal;
	}

	@Override
	public void processResponse(ServletResponse servletRes, Response res) {
		if(res==null){
			log.info("Response object is null. Nothing to tranform.");
			return;
		}
		Result iResult = res.getResult();
		ResponseType resp = ResponseType.Factory.newInstance();
		ResultType oResult = resp.addNewResult();
		updateResult(oResult, iResult);
		log.debug("Output XML string:"+oResult.xmlText());
		servletRes.setContentType("application/xml");
		XmlOptions options =  new XmlOptions();
		options.setCharacterEncoding("utf8");
		try {
			resp.save(servletRes.getWriter(), options);
		} catch (IOException e) {
			log.error("Error occurred while writing response to the stream", e);
			((HttpServletResponse)servletRes).setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
	}

	private void updateResult(ResultType oResult, Result iResult) {
		String result = iResult.getDecision();
		if(result!=null){
			if(result.equals(Result.DECISION_DENY)){
				oResult.setDecision(DecisionType.DENY);			
			}else if(result.equals(Result.DECISION_PERMIT)){
				oResult.setDecision(DecisionType.PERMIT);
			}else if(result.equals(Result.DECISION_NA)){
				oResult.setDecision(DecisionType.NOT_APPLICABLE);
			}else if(result.equals(Result.DECISION_INDETERMINATE)){
				oResult.setDecision(DecisionType.INDETERMINATE);
			}			
		}
		if(iResult.getObligations()!=null && iResult.getObligations().length>0){
			ObligationsType obligations = oResult.addNewObligations();
			updateObligations(obligations, iResult.getObligations());
		}
		updateStatus(oResult, iResult);
	}

	private void updateStatus(ResultType oResult, Result iResult) {
		Status iStatus = iResult.getStatus();
		if(iStatus!=null){
			StatusType status = oResult.addNewStatus();
			String statusMsg = iStatus.getStatusMessage();
			if(statusMsg!=null && statusMsg.length()>0){
				status.setStatusMessage(iStatus.getStatusMessage());
			}
			StatusCode iStatusCode = iStatus.getStatusCode();
			if(iStatusCode!=null){
				StatusCodeType statusCode = status.addNewStatusCode();
				statusCode.setValue(iStatusCode.getValue());			
			}			
		}
	}

	private void updateObligations(ObligationsType obligations,
			ObligationOrAdvice[] iObligations) {
		for (ObligationOrAdvice iObl : iObligations) {
			ObligationType obligation = obligations.addNewObligation();
			obligation.setObligationId(iObl.getId());
			AttributeAssignment[] iAttAssgnmnt = iObl.getAttributeAssignment();
			for (AttributeAssignment att : iAttAssgnmnt) {
				AttributeAssignmentType attAssgn = obligation.addNewAttributeAssignment();
				attAssgn.setAttributeId(att.getAttributeId());
				Node nd = attAssgn.getDomNode();
				Text text = nd.getOwnerDocument().createTextNode(att.getValue());
				nd.appendChild(text);
			}
		}
		
	}

	@Override
	public void returnEndPoints(ServletResponse response, String endPoint) throws IOException {
		response.setContentType("application/xml");
        PrintWriter writer = response.getWriter();
        String output = endPointXML.replace("$ENDPOINT$", endPoint);
        writer.println(output);
        writer.flush();
	}

}