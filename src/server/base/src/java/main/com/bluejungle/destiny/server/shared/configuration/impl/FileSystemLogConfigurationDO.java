/*
 * Created on May 29, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import com.bluejungle.destiny.server.shared.configuration.IFileSystemLogConfigurationDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/FileSystemLogConfigurationDO.java#1 $
 */

public class FileSystemLogConfigurationDO implements IFileSystemLogConfigurationDO{
	private Integer threadPoolMaximumSize;
	private Integer threadPoolKeepAliveTime;
	
	private Integer logInsertTaskIdleTime;
	
	private Integer logTimeout;
	private Integer timeoutCheckerFrequency;
	
	private Long queueManagerUploadSize;
	private Long maxHandleFileSizePerThread;
	
	
	public Integer getThreadPoolMaximumSize() {
		return threadPoolMaximumSize;
	}
	public void setThreadPoolMaximumSize(Integer threadPoolMaximumSize) {
		this.threadPoolMaximumSize = threadPoolMaximumSize;
	}
	public Integer getThreadPoolKeepAliveTime() {
		return threadPoolKeepAliveTime;
	}
	public void setThreadPoolKeepAliveTime(Integer threadPoolKeepAliveTime) {
		this.threadPoolKeepAliveTime = threadPoolKeepAliveTime;
	}
	public Integer getLogInsertTaskIdleTime() {
		return logInsertTaskIdleTime;
	}
	public void setLogInsertTaskIdleTime(Integer logInsertTaskIdleTime) {
		this.logInsertTaskIdleTime = logInsertTaskIdleTime;
	}
	public Integer getLogTimeout() {
		return logTimeout;
	}
	public void setLogTimeout(Integer logTimeout) {
		this.logTimeout = logTimeout;
	}
	public Integer getTimeoutCheckerFrequency() {
		return timeoutCheckerFrequency;
	}
	public void setTimeoutCheckerFrequency(Integer timeoutCheckerFrequency) {
		this.timeoutCheckerFrequency = timeoutCheckerFrequency;
	}
	public Long getQueueManagerUploadSize() {
		return queueManagerUploadSize;
	}
	public void setQueueManagerUploadSize(Long queueManagerUploadSize) {
		this.queueManagerUploadSize = queueManagerUploadSize;
	}
	public Long getMaxHandleFileSizePerThread() {
		return maxHandleFileSizePerThread;
	}
	public void setMaxHandleFileSizePerThread(Long maxHandleFileSizePerThread) {
		this.maxHandleFileSizePerThread = maxHandleFileSizePerThread;
	}
	
	
}
