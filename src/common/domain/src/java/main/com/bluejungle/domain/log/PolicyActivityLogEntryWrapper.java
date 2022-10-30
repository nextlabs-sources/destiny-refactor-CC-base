/*
 * Created on May 12, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import java.util.Collection;
import java.util.Map;

import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.IPair;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/log/PolicyActivityLogEntryWrapper.java#1 $
 */

/**
 * create an read only interface that is match the latest PolicyLogEntry
 * any previous versions need to create a wrapper to fit this interface.
 * This will simply the code in HibernateLogWriter
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/log/PolicyActivityLogEntryWrapper.java#1 $
 */
public interface PolicyActivityLogEntryWrapper {
    long getUid();
    
    long getTimestamp();
    
    long getPolicyId();
    
    String getAction();

    long getApplicationId();

    String getApplicationName();

    long getDecisionRequestId();

    FromResourceInformation getFromResourceInfo();

    long getHostId();

    String getHostName();

    String getHostIP();

    PolicyDecisionEnumType getPolicyDecision();

    ToResourceInformation getToResourceInfo();

    long getUserId();

    String getUserName();

    int getLevel();

    Map<String, DynamicAttributes> getAttributesMap();
    
    DynamicAttributes getCustomAttributes();

    Collection<IPair<String, String>> getPolicyTags();
}
