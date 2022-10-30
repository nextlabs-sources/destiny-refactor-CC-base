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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/AlreadyDefinedException.java#1 $
 */

class AlreadyDefinedException extends Exception {
	private static final String MESSAGE = 
		"Field \"%s\" is already defined. Old value = \"%s\". New value = \"%s\"";
	
	String field;
	Object definedValue;
	Object newValue;
	
	public AlreadyDefinedException(String field, Object definedValue, Object newValue) {
		super();
		this.field = field;
		this.definedValue = definedValue;
		this.newValue = newValue;
	}

	@Override
	public String getMessage() {
		 return String.format(MESSAGE, field, definedValue, newValue);
	}
}
