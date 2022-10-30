/*
 * Created on Jan 23, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/IDataSourceManager.java#1 $
 */

public interface IDataSourceManager {

    public IDataSource createDataSource(String type) throws INDException;
        
    public int addDataSource(IDataSource source) throws INDException;
    
    public IDataSource getDataSoruceByID(int id);
    
    public void removeDataSource(int id);
    
    public void updateDataSource(int id, IDataSource source);
    
}
