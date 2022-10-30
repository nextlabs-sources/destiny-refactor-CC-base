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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/NotFoundException.java#1 $
 */

class NotFoundException extends Exception {
	private static final String MESSAGE  = "%s with \"%s\"=\"%s\" is not found.";
	
	private String name;
	private String field;
	private Object value;
	
	public NotFoundException(String name, String field, Object value) {
		super();
		this.name = name;
		this.field = field;
		this.value = value;
	}

	@Override
	public String getMessage() {
		return String.format(MESSAGE, name, field, value);
	}
	
	
}
