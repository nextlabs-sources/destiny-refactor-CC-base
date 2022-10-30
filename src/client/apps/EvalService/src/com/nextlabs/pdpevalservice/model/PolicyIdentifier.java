package com.nextlabs.pdpevalservice.model;

public class PolicyIdentifier {

	private IdReference[] PolicyIdReference;
	
	private IdReference[] PolicySetIdReference;

	public IdReference[] getPolicyIdReference() {
		return PolicyIdReference;
	}

	public void setPolicyIdReference(IdReference[] policyIdReference) {
		PolicyIdReference = policyIdReference;
	}

	public IdReference[] getPolicySetIdReference() {
		return PolicySetIdReference;
	}

	public void setPolicySetIdReference(IdReference[] policySetIdReference) {
		PolicySetIdReference = policySetIdReference;
	}
	
}
