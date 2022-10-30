/*
 * Created on Apr 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/ISelectElement.java#1 $
 */

public interface ISelectElement {

    /**
     * Returns the name of the variable used for the data object
     * 
     * @return the name of the variable used for the data object
     */
    public String getDOVarName();

    /**
     * Returns the data object class name (e.g. UserDO)
     * 
     * @return the data object class name
     */
    public String getDOClassName();

    /**
     * Returns the data object field name to select
     * 
     * @return the data object field name to select
     */
    public String getFieldName();

    /**
     * Returns the HQL function associated with the select element
     * 
     * @return the HQL function associated with the select element
     */
    public String getFunction();
}