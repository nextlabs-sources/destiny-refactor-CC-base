package com.nextlabs.pdpevalservice.filters.transform;

public class InvalidRequestException extends Exception {

	private static final long serialVersionUID = -5189058185167393670L;
	
	public InvalidRequestException(String message){
		super(message);
	}

}
