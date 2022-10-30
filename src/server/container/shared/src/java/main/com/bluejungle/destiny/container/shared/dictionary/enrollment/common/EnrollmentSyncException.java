/*
 * Created on Mar 26, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.common;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/common/EnrollmentSyncException.java#1 $
 */

public class EnrollmentSyncException extends EnrollmentException {
	private final String entry;
	private final boolean isEntryCurrent;
	
	public EnrollmentSyncException(String message, String currentEntry) {
        super(message);
        this.entry = currentEntry;
        this.isEntryCurrent = true;
    }
	
	public EnrollmentSyncException(Throwable cause) {
        this(cause, null);
    }

    public EnrollmentSyncException(Throwable cause, String currentEntry) {
        super(cause);
        this.entry = currentEntry;
        this.isEntryCurrent = true;
    }
    
    public EnrollmentSyncException(Throwable cause, String currentEntry, boolean isEntryCurrent) {
        super(cause);
        this.entry = currentEntry;
        this.isEntryCurrent = isEntryCurrent;
    }

    public EnrollmentSyncException(String message, Throwable cause, String currentEntry) {
        super(message, cause);
        this.entry = currentEntry;
        this.isEntryCurrent = true;
    }

	@Override
	public String getMessage() {
	    StringBuilder sb = new StringBuilder(super.getMessage());
	    
		if (entry != null) {
		    sb.append(". The ")
		      .append(isEntryCurrent ? "current" : "last successful")
		      .append(" entry is '")
		      .append(entry)
		      .append("'.");
		}
		
		return sb.toString();
	}
}
