/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.TestUtils;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityLogEntryV4;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.PolicyActivityInfoV3;
import com.nextlabs.domain.log.PolicyActivityInfoV4;
import com.nextlabs.domain.log.PolicyActivityInfoV5;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/test/com/bluejungle/domain/log/PolicyActivityLogEntryTestData.java#1 $
 *
 */
public class PolicyActivityLogEntryTestData {
    
    
    public static final PolicyActivityLogEntry generateRandom() {
        
        PolicyActivityInfo info = PolicyActivityInfoTestData.generateRandom();
        // unlikely to be more than 200 unique policies
        long policyId = TestUtils.rand.nextInt(200);
        long uid = TestUtils.rand.nextLong();
        return new PolicyActivityLogEntry(info, policyId, uid);
    }
    
    public static final PolicyActivityLogEntry[] generateRandom(int numEntries) {
        PolicyActivityLogEntry[] rv = new PolicyActivityLogEntry[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandom();
        }
        return rv;
    }
    
    /**
     * Generate a set of TrackingLongEntries, each with a very
     * large custom attribute value.
     * 
     * @param numEntries number of entries to generate
     * @return an array of random tracking log entries
     */
    public static final PolicyActivityLogEntry[] generateLargeCustAttr(int numEntries) {	    
    	PolicyActivityLogEntry[] entries = generateRandom(numEntries);
    	for (PolicyActivityLogEntry thisEntry : entries) {
    		DynamicAttributes attr = thisEntry.getCustomAttributes();
    		if (attr != null) {
	    		String key = "key" + (attr.size() + 1);
	    		attr.add(key, TestUtils.genRandomString(5000));
    		}
    	}
    	return entries;
    }

    public static final PolicyActivityLogEntryV2 generateRandomV2() {
        
        PolicyActivityInfoV2 info = PolicyActivityInfoTestData.generateRandomV2();
        // unlikely to be more than 200 unique policies
        long policyId = TestUtils.rand.nextInt(200);
        long uid = TestUtils.rand.nextLong();
        return new PolicyActivityLogEntryV2(info, policyId, uid);
    }
    
    public static final PolicyActivityLogEntryV2[] generateRandomV2(int numEntries) {
        PolicyActivityLogEntryV2[] rv = new PolicyActivityLogEntryV2[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomV2();
        }
        return rv;
    }

    public static final PolicyActivityLogEntryV3 generateRandomV3() {
        
        PolicyActivityInfoV3 info = PolicyActivityInfoTestData.generateRandomV3();
        // unlikely to be more than 200 unique policies
        long policyId = TestUtils.rand.nextInt(200);
        long uid = TestUtils.rand.nextLong();
        return new PolicyActivityLogEntryV3(info, policyId, uid);
    }
    
    public static final PolicyActivityLogEntryV3[] generateRandomV3(int numEntries) {
        PolicyActivityLogEntryV3[] rv = new PolicyActivityLogEntryV3[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomV3();
        }
        return rv;
    }
    
    public static final PolicyActivityLogEntryV4 generateRandomV4() {
        
        PolicyActivityInfoV4 info = PolicyActivityInfoTestData.generateRandomV4();
        // unlikely to be more than 200 unique policies
        long policyId = TestUtils.rand.nextInt(200);
        long uid = TestUtils.rand.nextLong();
        return new PolicyActivityLogEntryV4(info, policyId, uid);
    }
    
    public static final PolicyActivityLogEntryV4[] generateRandomV4(int numEntries) {
        PolicyActivityLogEntryV4[] rv = new PolicyActivityLogEntryV4[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomV4();
        }
        return rv;
    }

    public static final PolicyActivityLogEntryV5 generateRandomV5() {
        
        PolicyActivityInfoV5 info = PolicyActivityInfoTestData.generateRandomV5();
        // unlikely to be more than 200 unique policies
        long policyId = TestUtils.rand.nextInt(200);
        long uid = TestUtils.rand.nextLong();
        return new PolicyActivityLogEntryV5(info, policyId, uid);
    }
    
    public static final PolicyActivityLogEntryV5[] generateRandomV5(int numEntries) {
        PolicyActivityLogEntryV5[] rv = new PolicyActivityLogEntryV5[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomV5();
        }
        return rv;
    }
    
    /**
     * Generate a set of TrackingLongEntries, each with a very
     * large custom attribute value.
     * 
     * @param numEntries number of entries to generate
     * @return an array of random tracking log entries
     */
    public static final PolicyActivityLogEntryV2[] generateLargeCustAttrV2(int numEntries) {	    
    	PolicyActivityLogEntryV2[] entries = generateRandomV2(numEntries);
    	for (PolicyActivityLogEntryV2 thisEntry : entries) {
    		DynamicAttributes attr = thisEntry.getCustomAttributes();
    		if (attr != null) {
	    		String key = "key" + (attr.size() + 1);
	    		attr.add(key, TestUtils.genRandomString(5000));
    		}
    	}
    	return entries;
    }

}
