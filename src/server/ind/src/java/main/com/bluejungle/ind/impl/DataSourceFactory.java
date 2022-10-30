/*
 * Created on Feb 20, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl;

import com.bluejungle.ind.IDataSource;
import com.bluejungle.ind.IDataSourceFactory;
import com.bluejungle.ind.IDataSourceType;
import com.bluejungle.ind.INDException;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/impl/DataSourceFactory.java#1 $
 */

public class DataSourceFactory implements IDataSourceFactory {

    public static final String SHAREPOINT = "SHAREPOINT";
    public static final String SHAREPOINT_IMPL = 
                    "com.bluejungle.ind.impl.sharepoint.SharePointDataSource";
    
    /**
     * Constructor
     */
    public DataSourceFactory() {
        
    }

    /**
     * @see com.bluejungle.ind.IDataSourceFactory#createDataSource(java.lang.String)
     */
    public IDataSource createDataSource(String typeName) throws INDException {
        try {
            if ( SHAREPOINT.equalsIgnoreCase(typeName) ) {
                Class sharepointDataSourceClass = Class.forName(SHAREPOINT_IMPL);
                IDataSourceType type = new DataSourceType(SHAREPOINT, 
                        sharepointDataSourceClass);
                IDataSource source = (IDataSource) sharepointDataSourceClass.newInstance();
                source.setType(type);
                source.setName(SHAREPOINT);
                return source;
            }
        }
        catch (Exception e) {
            throw new INDException("Data Source Factory Error: can not create source " + typeName);
        }        
        return null;
    }

}
