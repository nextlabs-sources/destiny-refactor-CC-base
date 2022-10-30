/*
 * Created on Feb 26, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl;

import com.bluejungle.ind.IExternalResource;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/impl/ExternalResource.java#1 $
 */

public class ExternalResource implements IExternalResource {

    private String url;
    private String id;
    private String name;
    private String type;
    private boolean hasChildren;
    private IExternalResource[] children = null;
    
    /**
     * Constructor
     */
    public ExternalResource() {
    }

    public ExternalResource(IExternalResource[] children, String id, String url, String name, String type, boolean hasChild) {
        this.children = children;
        this.id = id;
        this.url = url;
        this.name = name;
        this.type = type;
        this.hasChildren = hasChild;
    }
    
    public ExternalResource(String id, String url, String name) {
        this.children = null;
        this.id = id;
        this.url = url;
        this.name = name;
        this.hasChildren = false;
    }
    
    /**
     * @see com.bluejungle.ind.IExternalResource#getChildren()
     */
    public IExternalResource[] getChildren() {
        return children;
    }
    
    /**
     * @see com.bluejungle.ind.IExternalResource#setChildren(com.bluejungle.ind.IExternalResource[])
     */
    public void setChildren(IExternalResource[] children) {
        this.children = children;
    }
    
    /**
     * @see com.bluejungle.ind.IExternalResource#getURL()
     */
    public String getURL() {
        return url;
    }
    
    /**
     * @see com.bluejungle.ind.IExternalResource#setURL(java.lang.String)
     */
    public void setURL(String url) {
        this.url = url;
    }
    
    /**
     * @see com.bluejungle.ind.IExternalResource#getID()
     */
    public String getID() {
        return id;
    }

    /**
     * @see com.bluejungle.ind.IExternalResource#setID(java.lang.String)
     */
    public void setID(String ID) {
        this.id = ID;
    }
    
    /**
     * @see com.bluejungle.ind.IExternalResource#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * @see com.bluejungle.ind.IExternalResource#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @see com.bluejungle.ind.IExternalResource#getType()
     */
    public String getType() {
        return type;
    }

    /**
     * @see com.bluejungle.ind.IExternalResource#setType(java.lang.String)
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * @see com.bluejungle.ind.IExternalResource#hasChildren()
     */
    public boolean hasChildren() {
        return hasChildren;
    }

    /**
     * @see com.bluejungle.ind.IExternalResource#setHasChildren(boolean)
     */
    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }
}
