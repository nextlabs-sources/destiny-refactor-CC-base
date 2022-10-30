/*
 * Created on Aug 11, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author sduan
 */
package com.nextlabs.openaz.pdp.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.openaz.xacml.api.Attribute;
import org.apache.openaz.xacml.api.AttributeValue;
import org.apache.openaz.xacml.api.Request;
import org.apache.openaz.xacml.api.RequestAttributes;
import org.apache.openaz.xacml.api.RequestAttributesReference;
import org.apache.openaz.xacml.api.RequestDefaults;
import org.apache.openaz.xacml.api.RequestReference;
import org.w3c.dom.Node;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributeValueType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AttributesType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.ContentType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.MultiRequestsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestDefaultsType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestReferenceType;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.RequestType;

public class RequestTypeUtil {
	
	public static RequestType convert(Request request) {
		if (request == null) {
			throw new IllegalArgumentException("Null Request");
		} else if(request.getRequestAttributes() == null) {
			throw new IllegalArgumentException("Null Attributes for RequestType");
		} 
		
		RequestType requestType = new RequestType();
		requestType.setCombinedDecision(request.getCombinedDecision());
		requestType.setReturnPolicyIdList(request.getReturnPolicyIdList());
		
		Map<String, AttributesType> attributesTypeRef = new HashMap<String, AttributesType>();
		
		for(RequestAttributes requestAttributes: request.getRequestAttributes()) {
			AttributesType attributesType = parseAttributes(requestAttributes);
			requestType.getAttributes().add(attributesType);
			attributesTypeRef.put(requestAttributes.getXmlId(), attributesType);
		}
		
		if(request.getMultiRequests() != null) {
			MultiRequestsType multiRequestType = new MultiRequestsType();
			for(RequestReference requestReference: request.getMultiRequests()) {
				multiRequestType.getRequestReference().add(
						parseRequestReferenceType(attributesTypeRef, requestReference));
			}
			requestType.setMultiRequests(multiRequestType);
		}
		
		if(request.getRequestDefaults() != null) {
			RequestDefaults requestDefaults = request.getRequestDefaults();
			requestType.setRequestDefaults(parseRequestDefaultsType(requestDefaults));
		}
		return requestType;
	}

	private static AttributesType parseAttributes(RequestAttributes requestAttributes) {
		if (requestAttributes == null) {
			throw new IllegalArgumentException("Null AttributesType");
		} else if (requestAttributes.getCategory() == null) {
			throw new IllegalArgumentException("Null categoryId for AttributesType");
		}
		AttributesType attributesType = new AttributesType();
		// set CategoryId
		attributesType.setCategory(requestAttributes.getCategory().stringValue());
		// set Id
		if(requestAttributes.getXmlId() != null) {
			attributesType.setId(requestAttributes.getXmlId());
		}
		// set Content
		if(requestAttributes.getContentRoot() != null) {
			ContentType contentType = new ContentType();
			Node nodeContentRoot = requestAttributes.getContentRoot();
			contentType.getContent().add(nodeContentRoot);
			attributesType.setContent(contentType);
		}
		// set Attribute list
		for(Attribute attribute: requestAttributes.getAttributes()) {
			attributesType.getAttribute().add(parseAttributeType(attribute));
		}
		
		return attributesType;
	}
	
	private static AttributeType parseAttributeType(Attribute attribute) {
		if(attribute == null) {
			throw new IllegalArgumentException("Null Attribute");
		} else if(attribute.getAttributeId() == null) {
			throw new IllegalArgumentException("Null AttributeId for AttributeType");
		} else if(attribute.getValues() == null) {
			throw new IllegalArgumentException("Null AttributeValue for AttributeType");
		}
		
		AttributeType attributeType = new AttributeType();
		// set AttributeId
		attributeType.setAttributeId(attribute.getAttributeId().stringValue());
		// set Issuer
		attributeType.setIssuer(attribute.getIssuer());
		// set IncludeInResult
		attributeType.setIncludeInResult(attribute.getIncludeInResults());
		// set AttributeValue
		for(AttributeValue<?> attributeValue: attribute.getValues()) {
			attributeType.getAttributeValue().add(parseAttributeValueType(attributeValue));
		}
		return attributeType;
	}

	private static AttributeValueType parseAttributeValueType(AttributeValue<?> attributeValue) {
		if(attributeValue == null) {
			throw new IllegalArgumentException("Null attributeValue");
		} else if(attributeValue.getDataTypeId() == null) {
			throw new IllegalArgumentException("Null DataType for AttributeValueType");
		}
		AttributeValueType attributeValueType = new AttributeValueType();
		// set DataType
		attributeValueType.setDataType(attributeValue.getDataTypeId().stringValue());
		// set Content
		attributeValueType.getContent().add(attributeValue.getValue());
		
		return attributeValueType;
	}

	private static RequestReferenceType parseRequestReferenceType(Map<String, AttributesType> attributesTypeRef, RequestReference requestReference) {
		if(attributesTypeRef == null) {
			throw new IllegalArgumentException("Null attributesTypeRef");
		} else if (requestReference == null) {
			throw new IllegalArgumentException("Null RequestReference");
		} else if (requestReference.getAttributesReferences() == null || requestReference.getAttributesReferences().isEmpty()) {
			throw new IllegalArgumentException("No AttributesReference in RequestReference");
		}
		RequestReferenceType requestReferenceType = new RequestReferenceType();
		
		for(RequestAttributesReference requestAttributesReference: requestReference.getAttributesReferences()) {
			requestReferenceType.getAttributesReference().add(parseAttributesReferenceType(attributesTypeRef, requestAttributesReference));
		}
		return requestReferenceType;
	}
	
	private static AttributesReferenceType parseAttributesReferenceType(Map<String, AttributesType> attributesTypeRef, RequestAttributesReference requestAttributesReference) {
		if(attributesTypeRef == null) {
			throw new IllegalArgumentException("Null attributesTypeRef");
		} else if (requestAttributesReference == null) {
			throw new IllegalArgumentException("Null AttributesReference");
		} else if (requestAttributesReference.getReferenceId() == null) {
			throw new IllegalArgumentException("Null referenceId for AttributesReference");
		}
		AttributesReferenceType attributesReferenceType = new AttributesReferenceType();
		attributesReferenceType.setReferenceId(
				attributesTypeRef.get(requestAttributesReference.getReferenceId()));
		return attributesReferenceType;
	}

	private static RequestDefaultsType parseRequestDefaultsType(RequestDefaults requestDefaults) {
		if (requestDefaults == null) {
			throw new IllegalArgumentException("Null RequestDefaults");
		}
		RequestDefaultsType requestDefaultsType = new RequestDefaultsType();
		
		String uriXPathVersion = "";
		if(requestDefaults.getXPathVersion() != null) {
			uriXPathVersion = requestDefaults.getXPathVersion().toString();
		}
		requestDefaultsType.setXPathVersion(uriXPathVersion);
		return requestDefaultsType;
	}
}
