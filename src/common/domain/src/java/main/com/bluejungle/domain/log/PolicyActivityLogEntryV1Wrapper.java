/*
 * Created on May 12, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.IPair;
import com.nextlabs.domain.log.PolicyActivityInfoV5;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/main/com/bluejungle/domain/log/PolicyActivityLogEntryV1Wrapper.java#1 $
 */

public class PolicyActivityLogEntryV1Wrapper implements PolicyActivityLogEntryWrapper {
    private final PolicyActivityLogEntry v1;
    
    public PolicyActivityLogEntryV1Wrapper(PolicyActivityLogEntry v1) {
        this.v1 = v1;
    }

    @Override
    public String getAction() {
        return v1.getAction().getName();
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
    public DynamicAttributes getCustomAttributes() {
        return v1.getCustomAttributes();
    }

    @Override
    public long getDecisionRequestId() {
        return v1.getDecisionRequestId();
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
    public PolicyDecisionEnumType getPolicyDecision() {
        return v1.getPolicyDecision();
    }

    @Override
    public long getPolicyId() {
        return v1.getPolicyId();
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
    
    @Override
    public Map<String, DynamicAttributes> getAttributesMap(){
    	Map <String, DynamicAttributes> attributesMap = new HashMap<String, DynamicAttributes>();
    	attributesMap.put(PolicyActivityInfoV5.FROM_RESOURCE_ATTRIBUTES_TAG, getCustomAttributes());
    	return attributesMap;
    }
    
    @Override
    public List<IPair<String, String>> getPolicyTags() {
        return Collections.<IPair<String, String>>emptyList();
    }
}
