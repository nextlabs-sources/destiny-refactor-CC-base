/*
 * Created on Jan 23, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl;

import java.util.Date;
import java.util.List;

import com.bluejungle.ind.IDataSource;
import com.bluejungle.ind.IDataSourceConnection;
import com.bluejungle.ind.IDataSourceType;
import com.bluejungle.ind.IExternalResource;
import com.bluejungle.ind.INDException;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;


/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/impl/DataSource.java#1 $
 */

public abstract class DataSource implements IDataSource {

    protected String name;
    protected IDataSourceType type;
    protected IDataSourceConnection connection;
    protected Date creationTime;
    
    /**
     * Constructor
     */
    public DataSource(String name, IDataSourceType type) {
        this.name = name;
        this.type = type;
    }
    
    public DataSource() {
        
    }

    /**
     * @see com.bluejungle.ind.IDataSource#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see com.bluejungle.ind.IDataSource#getType()
     */
    public IDataSourceType getType() {
        return type;
    }

    /**
     * @see com.bluejungle.ind.IDataSource#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @see com.bluejungle.ind.IDataSource#setType(com.bluejungle.ind.IDataSourceType)
     */
    public void setType(IDataSourceType type) {
        this.type = type;
    }

    /**
     * @see com.bluejungle.ind.IDataSource#getConnection()
     */
    public IDataSourceConnection getConnection() {
        return this.connection;
    }
    
    /**
     * @see com.bluejungle.ind.IDataSource#setConnection(com.bluejungle.ind.IDataSourceConnection)
     */
    public void setConnection(IDataSourceConnection connection) throws INDException {
        this.connection = connection;
    }
    
    public abstract void getResourceTreeNodeChildren(IExternalResource node) throws INDException;
  
    public abstract boolean testConnection() throws INDException;
    
    public abstract List<IExternalResource> getResourcePreview(DomainObjectDescriptor descriptor) throws INDException;
    
}
