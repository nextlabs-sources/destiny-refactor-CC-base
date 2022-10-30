/*
 * Created on May 12, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/log/PolicyAssistantLogEntryWrapper.java#1 $
 */

public interface PolicyAssistantLogEntryWrapper {
    long getUid();
    
    long getTimestamp();
    
    String getLogIdentifier();

    String getAssistantName();

    String getAttrOne();

    String getAttrTwo();

    String getAttrThree();
}
