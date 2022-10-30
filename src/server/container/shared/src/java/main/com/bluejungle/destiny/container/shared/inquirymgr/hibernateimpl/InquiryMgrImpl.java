/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IInquiryMgr;

/**
 * This is the "on the fly" implementation of the inquiry manager. This inquiry
 * manager implementation only creates inquiry objects that are going to be used
 * in memory and then thrown away.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/InquiryMgrImpl.java#1 $
 */

public class InquiryMgrImpl extends BaseInquiryMgrImpl implements IInquiryMgr {

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IInquiryMgr#createInquiry()
     */
    public IInquiry createInquiry() {
        InquiryImpl newInquiry = new InquiryImpl();
        return newInquiry;
    }

}