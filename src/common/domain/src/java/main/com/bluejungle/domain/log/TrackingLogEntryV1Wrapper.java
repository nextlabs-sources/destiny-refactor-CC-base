/*
 * Created on May 12, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * which this to fit TrackingLogEntryWrapper
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/log/TrackingLogEntryV1Wrapper.java#1 $
 */

public class TrackingLogEntryV1Wrapper implements TrackingLogEntryWrapper{
    private final TrackingLogEntry v1;

    public TrackingLogEntryV1Wrapper(TrackingLogEntry v1) {
        this.v1 = v1;
    }

    @Override
    public String getAction() {
        ActionEnumType actionEnumType = v1.getAction();
        return actionEnumType != null ? actionEnumType.getName() : null;
    }

    @Override
    public long getApplicationId() {
        return v1.getApplicationId();
    }

    @Override
    public String getApplicationName() {
        return v1.getApplicationName();
    }

    @Override
    public DynamicAttributes getCustomAttr() {
        return v1.getCustomAttr();
    }

    @Override
    public FromResourceInformation getFromResourceInfo() {
        return v1.getFromResourceInfo();
    }

    @Override
    public String getHostIP() {
        return v1.getHostIP();
    }

    @Override
    public long getHostId() {
        return v1.getHostId();
    }

    @Override
    public String getHostName() {
        return v1.getHostName();
    }

    @Override
    public int getLevel() {
        return v1.getLevel();
    }
    
    @Override
    public long getTimestamp() {
        return v1.getTimestamp();
    }

    @Override
    public ToResourceInformation getToResourceInfo() {
        return v1.getToResourceInfo();
    }

    @Override
    public long getUid() {
        return v1.getUid();
    }

    @Override
    public long getUserId() {
        return v1.getUserId();
    }

    @Override
    public String getUserName() {
        return v1.getUserName();
    }
}
