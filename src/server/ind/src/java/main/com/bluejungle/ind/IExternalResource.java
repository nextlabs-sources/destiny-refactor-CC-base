/*
 * Created on Feb 26, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/IExternalResource.java#1 $
 */

public interface IExternalResource {
    
    String getType();
    void setType(String type);
    
    String getName();
    void setName(String name);
    
    String getURL();
    void setURL(String url);
    
    String getID();
    void setID(String ID);
    
    boolean hasChildren();
    void setHasChildren(boolean hasChildren);
    
    IExternalResource[] getChildren();
    void setChildren(IExternalResource[] children);
    
}
