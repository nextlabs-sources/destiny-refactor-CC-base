/*
 * Created on Mar 29, 2005
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.destiny.agent.activityjournal;

import com.bluejungle.framework.comp.ComponentManagerFactory;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/src/java/test/com/bluejungle/destiny/agent/activityjournal/UploaderThread.java#1 $:
 */

public class UploaderThread extends Thread {

    IActivityJournal activityJournal = (IActivityJournal) ComponentManagerFactory.getComponentManager().getComponent(IActivityJournal.NAME);
    public void run () {
        for (int i = 0; i < 5; i++){            
	        try {
                Thread.sleep (500);
            } catch (InterruptedException e) {
                throw new IllegalStateException("Upload thread should not have been interrupted.");
            }
	        activityJournal.uploadActivityLogs();
        }
    }
}
