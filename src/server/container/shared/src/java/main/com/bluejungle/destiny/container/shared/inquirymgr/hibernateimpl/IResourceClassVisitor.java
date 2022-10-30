/*
 * Created on Nov 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

/**
 * This is the resource class visitor interface. This interface exposes various
 * API to control the behavior of the resource class visitor.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/IResourceClassVisitor.java#1 $
 */

public interface IResourceClassVisitor {

    /**
     * This API should be called before visiting an entity
     * 
     * @param first
     *            set to true before adding the first entity, false afterwards
     */
    public void addNewEntityToVisit(boolean first);

    /**
     * Sets the current HQL variable to be used when building the HQL
     * expression. The variable name is used before any attribute name.
     * 
     * @param varName
     *            variable name to set (cannot be null or empty)
     */
    public void setVariableName(final String varName);
}
