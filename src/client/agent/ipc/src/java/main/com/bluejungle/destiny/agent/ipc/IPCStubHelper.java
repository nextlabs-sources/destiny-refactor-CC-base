/*
 * Created on Dec 8, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.ipc;

/**
 * 
 * This class is required because the windows API ::WaitForMultipleEvents
 * supports waiting on upto MAXIMUM_WAIT_OBJECTS (64) events. Each instance of
 * this class waits on events in its bucket by calling run() on the IPCStub and
 * passing in the bucket number
 * 
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class IPCStubHelper implements Runnable {

    private IPCUserModeStub stub = null;
    private int bucketNumber;

    /**
     * Calls run() on the IPCStub and passes in the bucket number
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        this.stub.run(this.bucketNumber);
    }

    /**
     * Sets the bucketNumber
     * 
     * @param bucketNumber
     *            The bucketNumber to set.
     */
    public void setBucketNumber(int bucketNumber) {
        this.bucketNumber = bucketNumber;
    }

    /**
     * Sets the stub
     * 
     * @param stub
     *            The stub to set.
     */
    public void setStub(IPCUserModeStub stub) {
        this.stub = stub;
    }
}
