package com.nextlabs.pdpevalservice.model;

public class Request {
	
	private boolean CombinedDecision=false;
	private boolean ReturnPolicyIdList=false;
	private String XPathVersion="http://www.w3.org/TR/1999/REC-xpath-19991116";
	private Category[] Category;
	private Subject[] Subject;
	private Action[] Action;
	private Resource[] Resource;
	private Environment[] Environment;
	
	public Request(){
	}

	public boolean isCombinedDecision() {
		return CombinedDecision;
	}

	public boolean isReturnPolicyIdList() {
		return ReturnPolicyIdList;
	}

	public String getXPathVersion() {
		return XPathVersion;
	}

	public Category[] getCategory() {
		return Category;
	}

	public Subject[] getSubject() {
		return Subject;
	}

	public Action[] getAction() {
		return Action;
	}

	public Resource[] getResource() {
		return Resource;
	}

	public Environment[] getEnvironment() {
		return Environment;
	}
	
}
