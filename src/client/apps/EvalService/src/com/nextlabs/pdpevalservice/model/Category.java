package com.nextlabs.pdpevalservice.model;

public class Category {
	
	public static final String CATEGORYID_SUBJECT = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
	
	public static final String CATEGORYID_RESOURCE = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";
	
	public static final String CATEGORYID_ACTION = "urn:oasis:names:tc:xacml:3.0:attribute-category:action";
	
	public static final String CATEGORYID_ENVIRONMENT = "urn:oasis:names:tc:xacml:3.0:attribute-category:environment";

	public static final String CATEGORYID_APPLICATION = "urn:nextlabs:names:evalsvc:1.0:attribute-category:application";
	
	private String Content;
	
	private String Id;
	
	protected String CategoryId;
	
	private Attribute[] Attribute;
	
	public String getContent() {
		return Content;
	}
	
	public String getId() {
		return Id;
	}
	
	public String getCategoryId() {
		return CategoryId;
	}
	
	public Attribute[] getAttribute() {
		return Attribute;
	}

}
