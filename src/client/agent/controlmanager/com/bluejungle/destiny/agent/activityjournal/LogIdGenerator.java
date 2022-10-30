/*
 * Created on Mar 29, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.activityjournal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * This class is used to generate unique ids for log entries. It uses the agent
 * Id as the high 32 bits of the id and a continuously increasing number as the
 * lower 32 bits. The number is stored to disk to guarantee that the log id will
 * be unique across restarts of the agent.
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/activityjournal/LogIdGenerator.java#1 $:
 */

public class LogIdGenerator implements IHasComponentInfo<LogIdGenerator>, IConfigurable, IInitializable{
    public static final String NAME = LogIdGenerator.class.getName();
    public static final PropertyKey<Long> AGENT_REGISTRATION_ID = new PropertyKey<Long>("agentId");
    public static final PropertyKey<String> BASE_DIR_PROPERTY_NAME = new PropertyKey<String>("baseDirProperty");
    private static final String DEFAULT_BASE_DIR = ".";
    private static String baseDir = DEFAULT_BASE_DIR;

    private static final ComponentInfo<LogIdGenerator> COMP_INFO =
			new ComponentInfo<LogIdGenerator>(
					NAME, 
					LogIdGenerator.class, 
					LogIdGenerator.class,
					LifestyleType.SINGLETON_TYPE);

    IConfiguration config;

    static final String LOG_ID_FILE = "logid.dat";
    static final int ID_BUFFER = 1000;
    static final int ID_MIN_BUFFER = 100;

    long bufferEnd = ID_BUFFER;
    long id;

    /**
     * @return ComponentInfo to help creating an instance with Component Manager
     * 
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<LogIdGenerator> getComponentInfo() {
        return COMP_INFO;
    }

    private void initializeId(long agentId) {
        this.id = agentId << 32; // we assume that agent id is <32 bits
        File file = new File(baseDir + File.separator + ActivityJournal.LOG_SUB_DIR + File.separator + LOG_ID_FILE);
        if (file.exists()) {
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                this.id = ((Long) ois.readObject()).longValue();
                ois.close();
                this.updateIdFile();
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } catch (ClassNotFoundException e) {
            }
        }

    }

    /**
     * save id file
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void updateIdFile() {
        File file = new File(baseDir + File.separator + ActivityJournal.LOG_SUB_DIR + File.separator + LOG_ID_FILE);
        ObjectOutputStream oos;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            oos = new ObjectOutputStream(new FileOutputStream(file));
            this.bufferEnd = this.id + ID_BUFFER;
            oos.writeObject(new Long(this.bufferEnd));
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the next id in the sequence
     */
    public synchronized long getNextId() {
        this.id++;

        if (this.id >= this.bufferEnd - ID_MIN_BUFFER) {
            this.updateIdFile();
        }

        return (this.id);
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return config;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        baseDir = getConfiguration().get(BASE_DIR_PROPERTY_NAME, DEFAULT_BASE_DIR);
        initializeId(getConfiguration().get(AGENT_REGISTRATION_ID));
    }
}
