/*
 * Created on Dec 22, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/IllegalFormatException.java#1 $
 */

public class IllegalFormatException extends Exception {
	private static final String MESSAGE = "The value of %s is not in correct format. \"%s\" does match %s pattern.";
	
	private String field;
	private String expectedFormat;
	private Object inputValue;
	
	public IllegalFormatException(String field, String expectedFormat, Object inputValue) {
		super();
		this.field = field;
		this.expectedFormat = expectedFormat;
		this.inputValue = inputValue;
	}

	@Override
	public String getMessage() {
		return String.format(MESSAGE, field, inputValue, expectedFormat);
	}
}
