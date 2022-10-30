package com.nextlabs.shared.tools;

/**
 * This exception is thrown when there is error on parsing the command line arguments
 * For example, UniqueChoiceOptionDescriptor only allow max one option is selected in the same group. 
 * And this exception must throw when more than one option is selected.
 * Or try to selected an option that is not in the CompoundDescriptor.
 * 
 * @author hchan
 * @date Mar 30, 2007
 */
public class ParseException extends Exception{
	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}
}
