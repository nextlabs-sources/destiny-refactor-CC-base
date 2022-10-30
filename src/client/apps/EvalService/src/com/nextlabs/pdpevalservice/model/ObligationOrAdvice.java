package com.nextlabs.pdpevalservice.model;

public class ObligationOrAdvice {
	
	public static final String ATTR_OBLIGATION_COUNT = "CE_ATTR_OBLIGATION_COUNT";
	
	public static final String ATTR_OBLIGATION_NAME = "CE_ATTR_OBLIGATION_NAME";

	public static final String ATTR_OBLIGATION_NUMVALUES = "CE_ATTR_OBLIGATION_NUMVALUES";
	
	public static final String ATTR_OBLIGATION_VALUE = "CE_ATTR_OBLIGATION_VALUE";
	
	
	private String Id;
	
	private AttributeAssignment[] AttributeAssignment;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public AttributeAssignment[] getAttributeAssignment() {
		return AttributeAssignment;
	}

	public void setAttributeAssignment(AttributeAssignment[] attributeAssignment) {
		AttributeAssignment = attributeAssignment;
	}
	
}
