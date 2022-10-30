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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hqlbuilder/IOrderByElement.java#1 $
 */

public interface IOrderByElement {

    /**
     * Returns the order expression
     * 
     * @return the order expression
     */
    public String getExpression();

    /**
     * Returns true of the ordering is ascending, false otherwise
     * 
     * @return true of the ordering is ascending, false otherwise
     */
    public boolean isAcending();
}