/*
 * Created on May 29, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs.components.log;

import com.bluejungle.framework.comp.IStartable;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/nextlabs/destiny/container/dabs/components/log/IThreadPool.java#1 $
 */

public interface IThreadPool extends IStartable{
	PropertyKey<Integer> MAXIMUM_POOL_SIZE_PARAM = new PropertyKey<Integer>("maximumPoolSize");
	PropertyKey<Integer> KEEP_ALIVE_TIME_PARAM = new PropertyKey<Integer>("keepAliveTime");

	/**
	 * 
	 * @return the current pool size
	 */
	int getPoolSize();
	
	/**
	 * 
	 * @param the new task
	 * @return true if the task is accepted
	 */
	boolean addTask();
	
	void setTaskFactory(ITaskFactory taskFactory);
}
