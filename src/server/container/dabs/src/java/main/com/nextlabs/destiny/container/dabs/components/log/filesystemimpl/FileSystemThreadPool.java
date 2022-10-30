/*
 * Created on May 29, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.log.filesystemimpl;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IStartable;
import com.bluejungle.framework.comp.LifestyleType;
import com.nextlabs.destiny.container.dabs.components.log.ITaskFactory;
import com.nextlabs.destiny.container.dabs.components.log.IThreadPool;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/container/dabs/components/log/filesystemimpl/FileSystemThreadPool.java#1 $
 */

public class FileSystemThreadPool implements IThreadPool, ILogEnabled, IInitializable, IConfigurable, IStartable {
	public static final ComponentInfo<FileSystemThreadPool> COMP_INFO = 
		new ComponentInfo<FileSystemThreadPool>(
			"fileSystemThreadPool", 
			FileSystemThreadPool.class,
			IThreadPool.class, 
			LifestyleType.SINGLETON_TYPE
			);
	
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 32;
	private static final int DEFAULT_KEEP_ALIVE_TIME = 60000;
	private static final long WARNING_FREQUENCY = 5 * 60 * 1000; //at most even 5 minutes
	
	
	ThreadPoolExecutorMod threadPool;
	
	private Log log;
	private IConfiguration configuration;
	private Timer restTimer;
	
	private int maximumPoolSize;
	private ITaskFactory logInsertTaskFactory;
	
	public void init() {
		maximumPoolSize = configuration.get(MAXIMUM_POOL_SIZE_PARAM, DEFAULT_MAXIMUM_POOL_SIZE);
		if(maximumPoolSize <= 0){
			throw new IllegalArgumentException("maximumPoolSize must be a positive number.");
		}
		
		int keepAliveTime = configuration.get(KEEP_ALIVE_TIME_PARAM, DEFAULT_KEEP_ALIVE_TIME);
		if(keepAliveTime < 0){
			throw new IllegalArgumentException("keepAliveTime must be a non negative number.");
		}
		
		threadPool = new ThreadPoolExecutorMod(
				0, 	//corePoolSize
				maximumPoolSize, //maximumPoolSize
				keepAliveTime, //keepAliveTime
                TimeUnit.MILLISECONDS, //unit
                new SynchronousQueue<Runnable>() //BlockingQueue<Runnable> workQueue
		);
		
		restTimer = new Timer("FSThreadPoolWarning", true);
	}

	public IConfiguration getConfiguration() {
		return this.configuration;
	}

	public void setConfiguration(IConfiguration config) {
		this.configuration = config;
	}

	public void start() {
		threadPool.prestartAllCoreThreads();
	}

	public void stop() {
		threadPool.shutdownNow();
	}
	
	public Log getLog() {
		return log;
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public int getPoolSize() {
		return threadPool.getEstimatedTaskCount();
	}
	
	private AtomicBoolean isWarningLogged = new AtomicBoolean(false);
	
	public boolean addTask(){
		if(getPoolSize() < threadPool.getMaximumPoolSize()){
			threadPool.execute(logInsertTaskFactory.getTask());
			return true;
		}
		
		if (!isWarningLogged.get()) {
			synchronized (isWarningLogged) {
				if (!isWarningLogged.get()) {
					getLog().warn(
							"File System Thread pool reaches max size (" + maximumPoolSize
									+ "). Please increase the thread pool size");
					isWarningLogged.set(true);
				}
			}
			
			restTimer.schedule(new TimerTask(){
				@Override
				public void run() {
					isWarningLogged.set(false);
				}
				
			}, WARNING_FREQUENCY);
			
		}
		return false;
		
	}
	
	protected class ThreadPoolExecutorMod extends ThreadPoolExecutor{
		//volatile to allow concurrent read
		private AtomicInteger count = new AtomicInteger();
		
		public ThreadPoolExecutorMod(int corePoolSize, int maximumPoolSize, long keepAliveTime,
				TimeUnit unit, BlockingQueue<Runnable> workQueue) {
			super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			count.decrementAndGet();
			if (t != null && !isShutdown()) {
				getLog().error("logInsertTask fail", t);
			}
		}

		@Override
		public void execute(Runnable command) {
			count.incrementAndGet();
			super.execute(command);
		}

		public int getEstimatedTaskCount(){
			return count.get();
		}
	}

	public void setTaskFactory(ITaskFactory taskFactory) {
		this.logInsertTaskFactory = taskFactory;
	}
}
