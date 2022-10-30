package com.nextlabs.pdpevalservice.filters.transform;

public class InvalidInputException extends Exception {

	private static final long serialVersionUID = -319932316623791563L;

	public InvalidInputException(String message){
		super(message);
	}
}