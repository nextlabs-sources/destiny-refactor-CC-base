/*
 * Created on Jun 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.PropertyKey;

import net.sf.hibernate.Session;
import net.sf.hibernate.dialect.Dialect;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/IDataSyncTask.java#1 $
 */

public interface IDataSyncTask{
	
	enum SyncType{
		SYNC,
		INDEXES_REBUILD,
		ARCHIVE
	};
	
	PropertyKey<IDataSyncTaskUpdate> TASK_UPDATE_PARAMETER = new PropertyKey<IDataSyncTaskUpdate>("update");
	PropertyKey<Dialect> DIALECT_CONFIG_PARAMETER = new PropertyKey<Dialect>("dialect");
	
	
	SyncType getType();
	
	/**
	 * 
	 * @param session, hibernate session, you can get the connection from this.
	 * @param timeout, the task should completed within the timeout. Otherwise you may get an interrupted.
	 * @param config, extra configuration of the task. Never null. 
	 * 					If no extra configuration, empty configuration should be given.
	 */
	void run(Session session, long timeout, IConfiguration config);
}
