/*
 * Created on Feb 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * The persistent inquiry interface extends the base inquiry interface. A
 * persistent inquiry, unlike the base inquiry, can be persisted to the
 * database.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IPersistentInquiry.java#1 $
 */

public interface IPersistentInquiry extends IInquiry {

    /**
     * Returns the inquiry Id
     * 
     * @return the inquiry Id
     */
    public Long getId();
}