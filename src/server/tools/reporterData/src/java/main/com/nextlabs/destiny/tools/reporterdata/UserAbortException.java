/*
 * Created on Oct 6, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/UserAbortException.java#1 $
 */

public class UserAbortException extends Exception {
	private static final long serialVersionUID = -8526525832077953936L;

	public UserAbortException(String message) {
		super(message);
	}
	
	public UserAbortException(String message, Throwable cause) {
		super(message, cause);
	}
}
