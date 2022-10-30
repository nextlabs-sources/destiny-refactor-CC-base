/*
 * Created on Feb 9, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dabs;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.ResourceInformationTestData;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.TestUtils;

/**
 * this is the utility class that is used to generate v1 log data for the dabs log
 * service unit test (LogServiceTest.java)
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dabs/src/java/test/com/nextlabs/destiny/container/dabs/SampleDABSLogDataMgr.java#1 $
 */

public class SampleDABSLogDataMgr {

    public static final PolicyActivityLogEntry generateRandomPALog() {
        
        FromResourceInformation from = ResourceInformationTestData.generateRandomFrom();
        boolean hasTo = TestUtils.rand.nextBoolean();
        ToResourceInformation to = hasTo ? ResourceInformationTestData.generateRandomTo() : null;
        String userName = TestUtils.genRandomString(20);
        long userId = TestUtils.rand.nextLong();
        String hostName = TestUtils.genRandomString(32);
        String hostIP = TestUtils.getRandomIPAddress();
        long hostId = TestUtils.rand.nextLong();
        String appName = TestUtils.genRandomString(27);
        long appId = TestUtils.rand.nextLong();
        ActionEnumType action = ActionEnumType.getActionEnum(TestUtils.rand.nextInt(ActionEnumType.numElements()));
        PolicyDecisionEnumType decision = PolicyDecisionEnumType.getPolicyDecisionEnum(TestUtils.rand.nextInt(PolicyDecisionEnumType.numElements()));
        long decisionRequestId = TestUtils.rand.nextLong();
        long ts = TestUtils.rand.nextLong();
        int level = TestUtils.rand.nextInt(3);
        PolicyActivityInfo info = new PolicyActivityInfo(from,
                                                         to,
                                                         userName,
                                                         userId,
                                                         hostName,
                                                         hostIP,
                                                         hostId,
                                                         appName,
                                                         appId,
                                                         action,
                                                         decision,
                                                         decisionRequestId,
                                                         ts,
                                                         level, 
                                                         null);
        
        // unlikely to be more than 200 unique policies
        long policyId = TestUtils.rand.nextInt(200);
        long uid = TestUtils.rand.nextLong();
        return new PolicyActivityLogEntry(info, policyId, uid);
    }
    
    public static final PolicyActivityLogEntry[] generateRandomPALog(int numEntries) {
        PolicyActivityLogEntry[] rv = new PolicyActivityLogEntry[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomPALog();
        }
        return rv;
    }
    
    /**
     * Generates a random tracking log entry
     * 
     * @return a random tracking log entry
     */
    public static final TrackingLogEntry generateRandomTRLog() {
        FromResourceInformation from = ResourceInformationTestData.generateRandomFrom();
        ToResourceInformation to;
        boolean hasTo = TestUtils.rand.nextBoolean();
        if (hasTo) {
            to = ResourceInformationTestData.generateRandomTo();
        } else {
            to = null;
        }

        String userName = TestUtils.genRandomString(32);
        long userId = TestUtils.rand.nextLong();
        String hostName = TestUtils.genRandomString(32);
        String hostIP = TestUtils.genRandomString(14);
        long hostId = TestUtils.rand.nextLong();
        String appName = TestUtils.genRandomString(32);
        long appId = TestUtils.rand.nextLong();
        ActionEnumType action = ActionEnumType.getActionEnum(TestUtils.rand.nextInt(ActionEnumType.numElements()));
        long uid = TestUtils.rand.nextLong();
        long ts = TestUtils.rand.nextLong();
        int level = TestUtils.rand.nextInt(4);

        TrackingLogEntry entry = new TrackingLogEntry(from, 
                                                      to, 
                                                      userName, 
                                                      userId, 
                                                      hostName, 
                                                      hostIP, 
                                                      hostId, 
                                                      appName, 
                                                      appId, 
                                                      action, 
                                                      uid, 
                                                      ts, 
                                                      level,
                                                      null);
        return entry;
    }

    /**
     * Returns an array of random tracking log entries
     * 
     * @param numEntries
     *            number of random entries to generate
     * @return an array of random tracking log entries
     */
    public static final TrackingLogEntry[] generateRandomTRLog(int numEntries) {
        TrackingLogEntry[] rv = new TrackingLogEntry[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomTRLog();
        }
        return rv;
    }
}
