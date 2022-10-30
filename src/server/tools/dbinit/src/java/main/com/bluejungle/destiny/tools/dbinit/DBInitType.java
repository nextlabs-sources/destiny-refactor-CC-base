/*
 * Created on May 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

/**
 * This class enumerates the differents types of action that the tool can
 * perform.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/DBInitType.java#1 $
 */

public enum DBInitType {
	INSTALL,
	UPGRADE,
	CREATE_SCHEMA,
	DROP_CREATE_SCHEMA,
	UPDATE_SCHEMA,
	PROCESS_SQL_FROM_FILE,
}