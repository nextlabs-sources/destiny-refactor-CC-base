package com.bluejungle.destiny.agent.controlmanager;
/*
 * Created on Jan 19, 2009
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.Calendar;
import java.util.TimerTask;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.framework.comp.ComponentImplBase;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.PropertyKey;

public class HeartbeatRecordManager extends ComponentImplBase implements IHeartbeatRecordManager {
    public static final PropertyKey<String> BASE_DIR_PROPERTY_NAME= new PropertyKey<String>("rootDirectory");
    private static final String DEFAULT_BASE_DIR = ".";
    private String baseDir = DEFAULT_BASE_DIR;

    private long lastSuccessfulHeartbeatTime;
    private long lastReferencedSystemTime;

    // The amount of time we are prepared for the clock to roll back before we
    // trigger the error state.
    private static final long ROLLBACK_MARGIN = 60 * 60 * 1000;

    private long accumulatedRollback = 0;

    private static final String LAST_HEARTBEAT_FILE = "config/heartbeat.info";
    private static final String LAST_SYSTEMTIME_FILE = "config/systime.info";
    private Log log;
    private IConfiguration config;

    private boolean doRollbackDetection = false;
    private boolean detectedManipulation = false;

    /**
     * @see IInitializable#init()
     */
    public void init() {

        IConfiguration configuration = getConfiguration();
        if (configuration != null) {
            baseDir = configuration.get(BASE_DIR_PROPERTY_NAME, DEFAULT_BASE_DIR);
        }

        lastSuccessfulHeartbeatTime = loadLastSuccessfulHeartbeat();
        lastReferencedSystemTime = loadLastSystemTime();

        long currentSystemTime = Calendar.getInstance().getTimeInMillis();

        // Have we gone back in time?  We compare the last heartbeat against a system time.
        // If we recorded a prior system time we use that.  If we didn't, we use the time
        // right now.
        if (lastReferencedSystemTime == -1) {
            if (lastSuccessfulHeartbeatTime > currentSystemTime) {
                accumulatedRollback = lastSuccessfulHeartbeatTime - currentSystemTime;
            }
            lastReferencedSystemTime = currentSystemTime;
        } else if (lastReferencedSystemTime > currentSystemTime) {
            accumulatedRollback = lastReferencedSystemTime - currentSystemTime;
        }

        // doRollbackDetection hasn't been initialized yet, so we have to force a check.
        detectClockManipulation(true);

        getLog().debug("Saved heartbeat: " + lastSuccessfulHeartbeatTime + ", saved system time: " + lastReferencedSystemTime);
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IHeartbeatRecordManager#updateLastSuccessfulHeartbeat
     */
    public synchronized void updateLastSuccessfulHeartbeat(Calendar when) {
        // Once the system heartbeats we are back to normal
        detectedManipulation = false;
        accumulatedRollback = 0;

        lastSuccessfulHeartbeatTime = when.getTimeInMillis();

        if (lastSuccessfulHeartbeatTime < lastReferencedSystemTime) {
            // The user rolled back, but that's okay.  We have done a heartbeat so we reset
            // all the clocks to make things look okay.
            recordState();
        }

        getLog().debug("Saving heartbeat time: " + lastSuccessfulHeartbeatTime);
        saveLastSuccessfulHeartbeat(lastSuccessfulHeartbeatTime);
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IHeartbeatRecordManager#getTimeSinceLastSuccessfulHeartbeat
     */
    public synchronized long getTimeSinceLastSuccessfulHeartbeat() {
        detectClockManipulation();

        if (detectedManipulation) {
            return Integer.MAX_VALUE;
        } else {
            long currentSystemTime = Calendar.getInstance().getTimeInMillis();
            return (currentSystemTime - lastSuccessfulHeartbeatTime)/1000;
        }
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IHeartbeatRecordManager#recordState
     */
    public synchronized void recordState() {
        if (doRollbackDetection) {
            saveLastSystemTime();
        }
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IHeartbeatRecordManager#detectRollback
     */
    public void detectRollback(boolean doRollbackDetection) {
        this.doRollbackDetection = doRollbackDetection;

        // Just in case
        if (!this.doRollbackDetection) {
            detectedManipulation = false;
        }
    }
    
    private void detectClockManipulation() {
        detectClockManipulation(doRollbackDetection);
    }

    private void detectClockManipulation(boolean doRollbackDetection) {
        if (!doRollbackDetection || detectedManipulation) {
            return;
        }

        // Check to see that the system time hasn't gone backwards, etc
        long currentSystemTime = Calendar.getInstance().getTimeInMillis();

        if (currentSystemTime < lastReferencedSystemTime) {
            accumulatedRollback += (lastReferencedSystemTime - currentSystemTime);
        }

        if (accumulatedRollback > ROLLBACK_MARGIN) {
            getLog().warn("Detected rollback of " + accumulatedRollback/(60 * 1000) + " minutes.  Treating as error");
            detectedManipulation = true;
        } else {
            // New checkpoint
            lastReferencedSystemTime = currentSystemTime;
        }

        return;
    }


    private void saveLastSuccessfulHeartbeat(long hb) {
        serializeLong(lastSuccessfulHeartbeatTime, baseDir + File.separator + LAST_HEARTBEAT_FILE);
    }

    private long loadLastSuccessfulHeartbeat() {
        return deserializeLong(baseDir + File.separator + LAST_HEARTBEAT_FILE);
    }

    private void saveLastSystemTime() {
        // Once someone has messed with the time we don't want to start recording the
        // bogus value
        if (detectedManipulation) {
            getLog().warn("Not saving system time due to detected manipulation\n");
        } else {
            lastReferencedSystemTime = Calendar.getInstance().getTimeInMillis();
            getLog().debug("Saving system time: " + lastReferencedSystemTime);
            serializeLong(lastReferencedSystemTime, baseDir + File.separator + LAST_SYSTEMTIME_FILE);
        }
    }

    private long loadLastSystemTime() {
        long lastSystemTime = deserializeLong(baseDir + File.separator + LAST_SYSTEMTIME_FILE);

        return lastSystemTime;
    }

    private void serializeLong(long timeval, String filename) {
        ObjectOutputStream out = null;
        FileOutputStream fout = null;
        
        try {
            fout = new FileOutputStream(filename);
            out = new ObjectOutputStream(fout);
            out.writeLong(timeval);
            out.flush();
        } catch (IOException e) {
            getLog().error("Unable to write " + filename + " file", e);
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    getLog().error("Unable to close " + filename + " file", e);
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    getLog().error("Unable to close " + filename + " file", e);
                }
            }
        }
    }

    private long deserializeLong(String filename) {
        long ret = -1;
        ObjectInputStream in = null;
        FileInputStream fin = null;
        try {
            File inFile = new File(filename);
            if (inFile.exists()) {
                fin = new FileInputStream(inFile);
                in = new ObjectInputStream(fin);
                ret = in.readLong();
            }
        } catch (IOException e) {
            getLog().error("Unable to read " + filename + ".", e);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    getLog().warn("Could not close " + filename, e);
                }
            }
            
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    getLog().warn("Could not close " + filename, e);
                }
            }
        }
        return ret;
    }
}
