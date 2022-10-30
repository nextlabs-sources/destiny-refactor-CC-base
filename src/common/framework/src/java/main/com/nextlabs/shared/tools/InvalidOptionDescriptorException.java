package com.nextlabs.shared.tools;

/**
 * This exception is thrown when
 * - optionid is null
 * - optionid is duplicated
 * - command line indicators are empty
 * - command line indicators are duplicated
 * 
 * 
 * @author hchan
 * @date Mar 29, 2007
 */
public class InvalidOptionDescriptorException extends RuntimeException {
	public InvalidOptionDescriptorException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidOptionDescriptorException(String message) {
		super(message);
	}
}
