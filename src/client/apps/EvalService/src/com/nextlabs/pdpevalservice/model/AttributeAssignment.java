package com.nextlabs.pdpevalservice.model;

public class AttributeAssignment {

	private String AttributeId;
	
	private String Value;
	
	private String Category;
	
	private String DataType;
	
	private String Issuer;

	public String getAttributeId() {
		return AttributeId;
	}

	public void setAttributeId(String attributeId) {
		AttributeId = attributeId;
	}

	public String getValue() {
		return Value;
	}

	public void setValue(String value) {
		Value = value;
	}

	public String getCategory() {
		return Category;
	}

	public void setCategory(String category) {
		Category = category;
	}

	public String getDataType() {
		return DataType;
	}

	public void setDataType(String dataType) {
		DataType = dataType;
	}

	public String getIssuer() {
		return Issuer;
	}

	public void setIssuer(String issuer) {
		Issuer = issuer;
	}
	
}
