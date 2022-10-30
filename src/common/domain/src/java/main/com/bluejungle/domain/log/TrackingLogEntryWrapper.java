/*
 * Created on May 12, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * create an read only interface that is match the latest TrackingLogEntry
 * any previous versions need to create a wrapper to fit this interface.
 * This will simply the code in HibernateLogWriter
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/log/TrackingLogEntryWrapper.java#1 $
 */
public interface TrackingLogEntryWrapper {
    long getUid();
    
    long getTimestamp();

    String getAction();

    long getApplicationId();

    String getApplicationName();

    DynamicAttributes getCustomAttr();

    FromResourceInformation getFromResourceInfo();

    long getHostId();

    String getHostIP();

    String getHostName();
    
    int getLevel();

    ToResourceInformation getToResourceInfo();

    long getUserId();

    String getUserName();
}
