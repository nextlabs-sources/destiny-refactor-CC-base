/*
 * Created on Mar 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.locking;

/**
 * This class implements a reader-writer lock mechanism. Currently both readers
 * and writers have equal preference if a reader releases its lock. A writer
 * however will give preference to other writers when releasing the write lock.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/framework/src/java/main/com/bluejungle/framework/locking/ReaderWriterLock.java#1 $
 */

public class ReaderWriterLock {

    /*
     * Private variables:
     */
    private int nReaders;
    private boolean isWriting;
    private int nWritersWaiting;

    /**
     * Constructor
     *  
     */
    public ReaderWriterLock() {
        super();
        this.nReaders = 0;
        this.isWriting = false;
        this.nWritersWaiting = 0;
    }

    /**
     * Acquire the read lock. Blocks if write lock is being held by another
     * thread. Multiple readers can acquire the read lock without blocking.
     */
    public synchronized void acquireReadLock() throws InterruptedException {
        while ((this.isWriting) || (this.nWritersWaiting > 0)) {
            this.wait();
        }
        this.nReaders++;
    }

    /**
     * Releases the read lock. Any writer that is waiting to write is signalled.
     */
    public synchronized void releaseReadLock() {
        this.nReaders--;
        if (this.nReaders == 0) {
            this.notifyAll();
        }
    }

    /**
     * Acquire the write lock. Blocks if a read lock is being held by another
     * thread.
     */
    public synchronized void acquireWriteLock() throws InterruptedException {
        while ((this.nReaders > 0) || (this.isWriting)) {
            this.nWritersWaiting++;
            this.wait();
            this.nWritersWaiting--;
        }
        this.isWriting = true;
    }

    /**
     * Releases the write lock. Any reader that is waiting to read is signalled.
     */
    public synchronized void releaseWriteLock() {
        this.isWriting = false;
        this.notifyAll();
    }
}