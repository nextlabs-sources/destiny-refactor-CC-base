package com.nextlabs.pdpevalservice.model;

public class Status {

	private String StatusMessage;
	
	private String StatusDetail;
	
	private StatusCode StatusCode;

	public String getStatusMessage() {
		return StatusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		StatusMessage = statusMessage;
	}

	public String getStatusDetail() {
		return StatusDetail;
	}

	public void setStatusDetail(String statusDetail) {
		StatusDetail = statusDetail;
	}

	public StatusCode getStatusCode() {
		return StatusCode;
	}

	public void setStatusCode(StatusCode statusCode) {
		StatusCode = statusCode;
	}
	
}
