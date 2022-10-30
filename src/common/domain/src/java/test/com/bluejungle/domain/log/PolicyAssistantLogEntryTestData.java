/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import com.bluejungle.framework.utils.TestUtils;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/test/com/bluejungle/domain/log/PolicyAssistantLogEntryTestData.java#1 $
 *
 */
public class PolicyAssistantLogEntryTestData {
    public static final PolicyAssistantLogEntry generateRandom(String logIdentifier) {
        String assistantName = TestUtils.genRandomString(32);
        String assistantOptions = TestUtils.genRandomString(32);
        String assistantDescription = TestUtils.genRandomString(256);
        String assistantUserActions = TestUtils.genRandomString(256);
        long uid = TestUtils.rand.nextLong();
        long ts = TestUtils.rand.nextLong();

        return new PolicyAssistantLogEntry(logIdentifier, assistantName, assistantOptions, assistantDescription, assistantUserActions, uid, ts);
    }
    
    /**
     * Returns an array of random policy assistant log entries
     * 
     * @param numEntries
     *            number of random entries to generate
     * @return an array of random policy assistant log entries
     */
    public static final PolicyAssistantLogEntry[] generateRandom(int numEntries) {
        PolicyAssistantLogEntry[] rv = new PolicyAssistantLogEntry[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandom("XX");
        }
        return rv;
    }
    
    /**
     * Returns an array of random policy assistant log entries based on a 
     * specific policy log id
     * 
     * @param policyLogID The policy log ID.
     * @param numEntries
     *            number of random entries to generate
     * @return an array of random policy assistant log entries
     */
    public static final PolicyAssistantLogEntry[] generateRandom(
    		String policyLogID, int numEntries) {
        PolicyAssistantLogEntry[] rv = new PolicyAssistantLogEntry[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandom(policyLogID);
        }
        return rv;
    }
}
