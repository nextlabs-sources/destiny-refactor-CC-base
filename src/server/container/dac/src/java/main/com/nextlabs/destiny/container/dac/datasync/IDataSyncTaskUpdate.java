/*
 * Created on Jun 19, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/IDataSyncTaskUpdate.java#1 $
 */

/**
 * Any sync task should call this during sync. 
 * It will give the status the manager how fast/well the sync is running.
 * If nothing is updated after the update interval, the manager may timeout the task 
 *  and the task have to stop immediately.
 * Notice that not all method renew the timeout.
 * This class can also tell if the task is dead or alive.
 */
public interface IDataSyncTaskUpdate {
    /**
     * if any update doesn't happen within the interval. The task may be interrupted.
     */
    long getUpdateInterval();
    
    /**
     * calling this method may or may not renew the timeout
     * @param prefix
     */
    void setPrefix(String prefix);
    
    /**
     * calling this method will renew the timeout.
     * @param size the estimated size of total items
     * @throws IllegalStateException if the you are dead(interrupted)
     */
    void setTotalSize(int size) throws IllegalStateException;

    /**
     * calling this method will renew the timeout.
     * @param size
     * @throws IllegalStateException if the you are dead(interrupted)
     */
    void addSuccess(int size) throws IllegalStateException;

    /**
     * calling this method will renew the timeout.
     * @param size
     * @throws IllegalStateException if the you are dead(interrupted)
     */
    void addFail(int size) throws IllegalStateException;

    /**
     * 
     * @return true if you are alive. 
     */
    boolean alive();
    
    /**
     * reset success, fail, total and prefix
     * calling this method may or may not renew the timeout
     */
    void reset();
}