/*
 * Created on Jan 23, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind;

import java.util.List;

import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/IDataSource.java#1 $
 */

public interface IDataSource {
    
    public String getName();
    
    public void setName(String name);
    
    public IDataSourceType getType();
    
    public void setType(IDataSourceType type);
    
    public IDataSourceConnection getConnection();
    
    public void setConnection(IDataSourceConnection connection) throws INDException;
    
    public boolean testConnection() throws INDException;
    
    void getResourceTreeNodeChildren(IExternalResource node) throws INDException;
    
    List<IExternalResource> getResourcePreview(DomainObjectDescriptor descriptor) throws INDException;
    
}
