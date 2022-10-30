package com.bluejungle.framework.comp;

import org.apache.commons.logging.Log;


// Copyright Blue Jungle, Inc.

/**
 * ILogEnabled interface should be implemented by any component that
 * wishes to use the logging service.
 *
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/comp/ILogEnabled.java#1 $
 */
public interface ILogEnabled
{

	public void setLog(Log log);
	public Log getLog();
}
