package com.nextlabs.pdpevalservice.model;

public class StatusCode {
	
	public static final String STATUSCODE_OK = "urn:oasis:names:tc:xacml:1.0:status:ok";

	public static final String STATUSCODE_PROC_ERROR = "urn:oasis:names:tc:xacml:1.0:status:processing-error";
	
	public static final String STATUSCODE_MISSING_ATTRIB = "urn:oasis:names:tc:xacml:1.0:status:missing-attribute";
	
	private String Value;
	
	private StatusCode StatusCode;

	public String getValue() {
		return Value;
	}

	public void setValue(String value) {
		Value = value;
	}

	public StatusCode getStatusCode() {
		return StatusCode;
	}

	public void setStatusCode(StatusCode statusCode) {
		StatusCode = statusCode;
	}

}
