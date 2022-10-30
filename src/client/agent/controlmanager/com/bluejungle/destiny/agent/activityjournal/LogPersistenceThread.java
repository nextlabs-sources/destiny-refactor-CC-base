/*
 * Created on Mar 28, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.activityjournal;


/**
 * This thread will wake periodically and call the activity journal to persist
 * log entries to disk
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class LogPersistenceThread extends Thread{

    ActivityJournal activityJournal = null;
    
    public LogPersistenceThread (ActivityJournal activityJournal) {
        super("LogPersistenceThread");
        this.activityJournal = activityJournal;
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep (ActivityJournal.LOG_PERSISTENCE_TIMEOUT);
                this.activityJournal.persistLogs();
            } catch (InterruptedException e) {
                break;
            }            
        }
    }
}