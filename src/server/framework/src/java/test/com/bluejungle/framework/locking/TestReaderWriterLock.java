/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.locking;

import junit.framework.TestCase;

/**
 * Tests the ReaderWriter lock
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/test/com/bluejungle/framework/locking/TestReaderWriterLock.java#1 $
 */

public class TestReaderWriterLock extends TestCase {

    public static void main(String[] args) {
    }

    /**
     * Constructor for TestReaderWriterLock.
     * 
     * @param arg0
     */
    public TestReaderWriterLock(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * 
     * @throws Exception
     */
    public void testWriteLock() throws Exception {
        ReaderWriterLock rwLock = new ReaderWriterLock();
        AccessLog accessLog = new AccessLog();

        // Acquire the lock first:
        rwLock.acquireWriteLock();

        // Create several reader/writer threads and start them:
        for (int i = 0; i < 20; i++) {
            Thread reader = new Thread(new Reader(rwLock, accessLog));
            reader.start();
        }
        for (int i = 0; i < 20; i++) {
            Thread writer = new Thread(new Writer(rwLock, accessLog));
            writer.start();
        }

        // Sleep for a while to give other threads some time to try to get the
        // lock:
        Thread.sleep(10000); /* wait 10 seconds */

        // Ensure that none of them were able to acquire the requested lock
        assertTrue("No other thread should be able to acquire an acquired write lock", (accessLog.getTotalAccesses() == 0));

        // Release the write lock
        rwLock.releaseWriteLock();

        //Sleep for some more time:
        Thread.sleep(10000); /* wait 10 seconds */

        // Ensure that all of them were able to get hold of the lock during this
        // time.
        assertTrue("All threads should have been able to acquire the released write lock", (accessLog.getTotalAccesses() == 40));
    }

    public void testReadLock() throws Exception {
        ReaderWriterLock rwLock = new ReaderWriterLock();

        // Acquire the lock in advance:
        rwLock.acquireReadLock();
        AccessLog accessLog = new AccessLog();

        // Create several reader threads and start them -- these should go
        // through:
        for (int i = 0; i < 20; i++) {
            Thread reader = new Thread(new Reader(rwLock, accessLog));
            reader.start();
        }

        // Sleep for a while and then ensure that all readers went through:
        Thread.sleep(10000);
        assertTrue("All readers should have been able to acquire the read lock", (accessLog.getNumberOfReads() == 20));

        // Create several writer threads and start them:
        for (int i = 0; i < 20; i++) {
            Thread writer = new Thread(new Writer(rwLock, accessLog));
            writer.start();
        }

        // Sleep for a while to give writers some time to try to get the
        // lock:
        Thread.sleep(10000); /* wait 10 seconds */

        // Ensure no writers could go through:
        assertTrue("No writer thread should have been able to acquire the read lock", (accessLog.getNumberOfWrites() == 0));

        // Now create some readers - these should have to wait until the writers
        // have passed:
        for (int i = 0; i < 20; i++) {
            Thread reader = new Thread(new Reader(rwLock, accessLog));
            reader.start();
        }

        // Wait and then ensure that the readers couldn't go through:
        Thread.sleep(10000);
        assertTrue("All readers should NOT have been able to acquire the read lock", (accessLog.getNumberOfReads() == 20));

        // TRULY Release the write lock
        rwLock.releaseReadLock();

        //Sleep for some more time:
        Thread.sleep(10000); /* wait 10 seconds */

        // Ensure that all remaining writers were able to get hold of the lock
        // during this time.
        assertTrue("All writers should have been able to acquire the released write lock", (accessLog.getNumberOfWrites() == 20));
        assertTrue("All readers should have been able to acquire the read lock", (accessLog.getNumberOfReads() == 40));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Base runnable class for reader/writer access
     * 
     * @author safdar
     */
    private abstract class Accessor implements Runnable {

        /*
         * Protected variables:
         */
        protected AccessLog accessLogger;
        protected ReaderWriterLock rwLock;

        /**
         * Constructor
         * 
         * @param manager
         * @param accessLogger
         */
        public Accessor(ReaderWriterLock lock, AccessLog accessLogger) {
            this.accessLogger = accessLogger;
            this.rwLock = lock;
        }
    }

    /**
     * Reader thread implementation
     * 
     * @author safdar
     */
    private class Reader extends Accessor {

        /**
         * Constructor
         * 
         * @param accessLogger
         */
        public Reader(ReaderWriterLock rwLock, AccessLog accessLogger) {
            super(rwLock, accessLogger);
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                this.rwLock.acquireReadLock();
                this.accessLogger.logReadAccess();
                this.rwLock.releaseReadLock();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Reader class
     * 
     * @author safdar
     */
    private class Writer extends Accessor {

        /**
         * Constructor
         * 
         * @param accessLogger
         */
        public Writer(ReaderWriterLock rwLock, AccessLog accessLogger) {
            super(rwLock, accessLogger);
        }

        /**
         * @see java.lang.Runnable#run()
         */
        public void run() {
            try {
                this.rwLock.acquireWriteLock();
                this.accessLogger.logWriteAccess();
                this.rwLock.releaseWriteLock();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Thes class stores the number of reads and writes that were made by the
     * readers/writers
     * 
     * @author safdar
     */
    private class AccessLog {

        /*
         * Private variables:
         */
        private int nReads = 0;
        private int nWrites = 0;

        public synchronized void logWriteAccess() {
            this.nWrites++;
        }

        public synchronized int getNumberOfWrites() {
            return this.nWrites;
        }

        public synchronized void logReadAccess() {
            this.nReads++;
        }

        public synchronized int getNumberOfReads() {
            return this.nReads;
        }

        public synchronized int getTotalAccesses() {
            return (this.nReads + this.nWrites);
        }
    }
}