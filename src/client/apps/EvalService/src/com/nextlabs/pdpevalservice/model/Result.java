package com.nextlabs.pdpevalservice.model;

public class Result {
	
	public static final String DECISION_PERMIT = "Permit";
	
	public static final String DECISION_DENY = "Deny";
	
	public static final String DECISION_NA = "NotApplicable";
	
	public static final String DECISION_INDETERMINATE = "Indeterminate";
	
	private String Decision;
	
	private Status Status;
	
	private ObligationOrAdvice[] Obligations;
	
	private ObligationOrAdvice[] AssociatedAdvice;
	
	private Attribute[] Attributes;
	
	private PolicyIdentifier[] PolicyIdentifierList;

	public String getDecision() {
		return Decision;
	}

	public void setDecision(String decision) {
		Decision = decision;
	}

	public Status getStatus() {
		return Status;
	}

	public void setStatus(Status status) {
		Status = status;
	}

	public ObligationOrAdvice[] getObligations() {
		return Obligations;
	}

	public void setObligations(ObligationOrAdvice[] obligations) {
		Obligations = obligations;
	}

	public ObligationOrAdvice[] getAssociatedAdvice() {
		return AssociatedAdvice;
	}

	public void setAssociatedAdvice(ObligationOrAdvice[] associatedAdvice) {
		AssociatedAdvice = associatedAdvice;
	}

	public Attribute[] getAttributes() {
		return Attributes;
	}

	public void setAttributes(Attribute[] attributes) {
		Attributes = attributes;
	}

	public PolicyIdentifier[] getPolicyIdentifierList() {
		return PolicyIdentifierList;
	}

	public void setPolicyIdentifierList(PolicyIdentifier[] policyIdentifierList) {
		PolicyIdentifierList = policyIdentifierList;
	}

	
}
