/*
 * Created on Feb 20, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.ind.IDataSource;
import com.bluejungle.ind.IDataSourceFactory;
import com.bluejungle.ind.IDataSourceManager;
import com.bluejungle.ind.INDException;


/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/impl/DataSourceManager.java#1 $
 */

public class DataSourceManager implements IDataSourceManager, IInitializable, IConfigurable, IManagerEnabled {

    /** The component manager */
    private IComponentManager manager;

    /** Configuration */
    private IConfiguration config;

    /* Data Source Factory */
    private static IDataSourceFactory dataSourceFactory;
    
    /* Component Info */
    public static final ComponentInfo COMP_INFO = new ComponentInfo(DataSourceManager.class.getName(), 
                        DataSourceManager.class.getName(), null, LifestyleType.SINGLETON_TYPE);
    
    /* Data Source Cache */
    private Map<Integer, IDataSource> dataSources = new HashMap<Integer, IDataSource>();
    
    /**
     * Constructor
     */
    public DataSourceManager() {
    }

    /**
     * @see com.bluejungle.ind.IDataSourceManager#addDataSource(com.bluejungle.ind.IDataSource)
     */
    public int addDataSource(IDataSource source) throws INDException {
        if ( source.getConnection() != null ) {
            if ( source.testConnection() == true ) {
                int id = source.getConnection().hashCode();
                dataSources.put(new Integer(id), source);
                return id;
            }
        }
        return 0;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceManager#getDataSoruceByID(long)
     */
    public IDataSource getDataSoruceByID(int id) {
        return dataSources.get(new Integer(id));
    }

    /**
     * @see com.bluejungle.ind.IDataSourceManager#removeDataSource(long)
     */
    public void removeDataSource(int id) {
    }

    /**
     * @see com.bluejungle.ind.IDataSourceManager#updateDataSource(long, com.bluejungle.ind.IDataSource)
     */
    public void updateDataSource(int id, IDataSource source) {
    }
    
    /**
     * Get Configuration
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return config;
    }
    
    /**
     * Get Manager
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }
    

    public void init() {
        dataSourceFactory = new DataSourceFactory();
    }

    public void setConfiguration( IConfiguration config ) {
        this.config = config;
    }

    public void setManager( IComponentManager manager ) {
        this.manager = manager;
    }
 
    public IDataSource createDataSource(String type) throws INDException {
        return dataSourceFactory.createDataSource(type);
    }
}
