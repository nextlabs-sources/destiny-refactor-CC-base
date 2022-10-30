/*
 * Created on May 29, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/IFileSystemLogConfigurationDO.java#1 $
 */

public interface IFileSystemLogConfigurationDO {
	Integer getThreadPoolMaximumSize();

	Integer getThreadPoolKeepAliveTime();

	Integer getLogInsertTaskIdleTime();

	Integer getLogTimeout();
	
	Integer getTimeoutCheckerFrequency();
	
	Long getQueueManagerUploadSize();

	Long getMaxHandleFileSizePerThread();
	
}
