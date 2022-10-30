/*
 * Created on Feb 20, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl;

import com.bluejungle.ind.IDataSourceType;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/impl/DataSourceType.java#1 $
 */

public class DataSourceType implements IDataSourceType {

    private String typeName; 
    private Class implClass;
    
    /**
     * Constructor
     */
    public DataSourceType(String type, Class implClass) {
        this.typeName = type;
        this.implClass = implClass;
    }

    /**
     * Constructor
     */
    public DataSourceType(String type, String implClassName) throws ClassNotFoundException {
        this.typeName = type;
        this.implClass = Class.forName(implClassName);
    }
    
    /**
     * @see com.bluejungle.ind.IDataSourceType#getImplClass()
     */
    public Class getImplClass() {
        return implClass;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceType#getName()
     */
    public String getTypeName() {
        return typeName;
    }

}
