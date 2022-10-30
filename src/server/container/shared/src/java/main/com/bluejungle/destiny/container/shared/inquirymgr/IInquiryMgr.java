/*
 * Created on Feb 23, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This is the inquiry manager interface. The inquiry manager creates "on the
 * fly" inquiry objects.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IInquiryMgr.java#1 $
 */

public interface IInquiryMgr {

    /**
     * Returns an in-memory inquiry
     * 
     * @return a new in memory inquiry
     */
    public IInquiry createInquiry();
}