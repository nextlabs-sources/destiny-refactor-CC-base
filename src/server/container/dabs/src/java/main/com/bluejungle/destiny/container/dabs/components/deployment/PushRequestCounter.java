/*
 * Created on Feb 25, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.deployment;

/**
 * This is a very simple class that hold a counter of the processed push
 * requests. It is used by the worker threads in the thread pool when a thread
 * is done processing the push request. The push request counter is initialized
 * with the total number of items to process, and keeps an internal counter of
 * the number of completed tasks. When the number of completed tasks reaches a
 * certain value (threshold), the class sends a notification, and listeners can
 * resume their work.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/components/deployment/PushRequestCounter.java#1 $
 */

final class PushRequestCounter {

    private int counter = 0;
    private int threshold;

    /**
     * Constructor. Once the number of increments reaches the threshold, this
     * object triggers a notification
     * 
     * @param threshold
     *            threshold value
     */
    public PushRequestCounter(int threshold) {
        super();
        this.threshold = threshold;
    }

    /**
     * Increments the counter. If the counter reaches the threshold, fire a
     * notification.
     */
    public void incrementCount() {
        synchronized (this) {
            this.counter++;
            if (this.counter == this.threshold) {
                this.counter = 0;
                this.notify();
            }
        }
    }
}