/*
 * Created on Jan 4, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.hibernate.dialect;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernate/dialect/DbURLFormat.java#1 $
 */

public abstract class DbURLFormat {
	public static final String URL_HOSTNAME 		= "<host>";
	public static final String URL_PORT 			= "<port>";
	public static final String URL_DATABASE 		= "<database>";
	public static final String URL_DB_SERVER_NAME 	= "<dbservername>";
	public static final String URL_FILE 			= "<file>";
	public static final String URL_SCHEMA 			= "<schema>";
	public static final String URL_SID 				= "<sid>";
	
	public abstract String getURLFormat();
	
	public abstract int getDefaultPort();
	
	public String getURL(String hostname, String database){
		return getURLFormat()
				.replace(URL_HOSTNAME, hostname)
				.replace(URL_DATABASE, database)
				.replace(URL_PORT, "" + getDefaultPort());
	}
}
