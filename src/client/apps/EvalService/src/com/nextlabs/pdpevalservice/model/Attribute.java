package com.nextlabs.pdpevalservice.model;

public class Attribute {
	
	private String AttributeId;
	
	private Object Value;
	
	private String Issuer;
	
	private String DataType="http://www.w3.org/2001/XMLSchema#string";
	
	private boolean IncludeInResult=false;
	
	public static final String ATTRIBUTEID_RESOURCEID = "urn:oasis:names:tc:xacml:1.0:resource:resource-id";
	
	public static final String ATTRIBUTEID_RESOURCETYPE = "urn:nextlabs:names:evalsvc:1.0:resource:resource-type";
	
	public static final String ATTRIBUTEID_RESOURCE_DIMENSION = "urn:nextlabs:names:evalsvc:1.0:resource:resource-dimension";
	
	public static final String ATTRIBUTEID_ACTION_ID = "urn:oasis:names:tc:xacml:1.0:action:action-id";
	
	public static final String ATTRIBUTEID_USER_ID = "urn:oasis:names:tc:xacml:1.0:subject:subject-id";
	
	public static final String ATTRIBUTEID_APP_ID = "urn:nextlabs:names:evalsvc:1.0:application:application-id";
	
	public static final String ATTRIBVAL_RES_DIMENSION_FROM = "from";
	
	public static final String ATTRIBVAL_RES_DIMENSION_TO = "to";

	//TODO: Check why "ec-us" is to be used
	public static final String ATTRIBVAL_RES_DEFAULTTYPE = "ec-us";
	
	public String getAttributeId() {
		return AttributeId;
	}
	
	public Object getValue() {
		return Value;
	}
	
	public String getIssuer() {
		return Issuer;
	}
	
	public String getDataType() {
		return DataType;
	}
	
	public boolean isIncludeInResult() {
		return IncludeInResult;
	}

}
