/*
 * Created on Dec 18, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.javaupdate;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/javaupdate/JavaUpdateException.java#1 $
 */

public class JavaUpdateException extends Exception {
	private static final long serialVersionUID = 5705210792416366222L;
	
	public enum Type{
		PRE_CHECK,
		SQL,
		HIBERNATE,
		UNKNOWN,
	}
	
	private final Type type;
	
	public JavaUpdateException() {
		this(Type.UNKNOWN);
	}

	public JavaUpdateException(String message, Throwable cause) {
		this(message, cause,Type.UNKNOWN);
	}

	public JavaUpdateException(String message) {
		this(message,Type.UNKNOWN);
	}

	public JavaUpdateException(Throwable cause) {
		this(cause,Type.UNKNOWN);
	}

	public JavaUpdateException(Type type) {
		super();
		this.type = type;
	}

	public JavaUpdateException(String message, Throwable cause, Type type) {
		super(message, cause);
		this.type = type;
	}

	public JavaUpdateException(String message, Type type) {
		super(message);
		this.type = type;
	}

	public JavaUpdateException(Throwable cause, Type type) {
		super(cause);
		this.type = type;
	}
	
	public static final JavaUpdateException reachMaxLength(String tableName, String columnName,
			int maxLength, int numberOfColumn) {
		return new JavaUpdateException(tableName + "." + columnName + " has " + numberOfColumn
				+ " row(s) of data is longer than allowed length " + maxLength, Type.PRE_CHECK);
	}
}
