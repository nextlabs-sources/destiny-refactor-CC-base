/*
 * Created on Aug 24, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.inquirymgr;

import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.LockMode;

import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.ReporterDataHolderDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/nextlabs/destiny/container/shared/inquirymgr/ReportDataHolderManagerMemory.java#1 $
 */

public class ReportDataHolderManagerMemory extends ReportDataHolderManager {

    private final Properties memory = new Properties();
    
    public void init() {
        
    }

    ReporterDataHolderDO getDo(String key, LockMode lockMode) throws HibernateException {
        String value = (String) memory.get(key);
        ReporterDataHolderDO holder = new ReporterDataHolderDO();
        holder.setProperty(key);
        holder.setValue(value);
        return holder;
    }

    public boolean add(String key, String value) throws HibernateException {
        memory.put(key, value);
        return true;
    }

    /**
     * 
     * @param key
     * @param value
     * @return true if the value is updated. If the entry is deleted, it will return false; 
     * @throws HibernateException
     */
    public boolean update(String key, String value) throws HibernateException {
        boolean exist = memory.containsKey(key);
        if (exist) {
            memory.put(key, value);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean saveOrUpdate(String key, String value) throws HibernateException {
        memory.put(key, value);
        return true;
        
    }

    public String remove(String key) throws HibernateException {
        return (String)memory.remove(key);
    }
}
