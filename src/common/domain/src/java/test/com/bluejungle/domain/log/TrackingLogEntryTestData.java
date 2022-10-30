/*
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.TestUtils;

/**
 * @author sasha
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/domain/src/java/test/com/bluejungle/domain/log/TrackingLogEntryTestData.java#2 $
 *  
 */
public class TrackingLogEntryTestData {

    /**
     * Generates a random tracking log entry
     * 
     * @return a random tracking log entry
     */
    public static final TrackingLogEntry generateRandom() {
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
        DynamicAttributes fromResAttrs = null;
        int nbAttr = TestUtils.rand.nextInt(4);

        if (nbAttr > 0) {
            fromResAttrs = new DynamicAttributes();
            for (int i = 0; i < nbAttr; i++){
                String key = "key" + i;
                String value = "value" + i;
                fromResAttrs.put(key, value);
            }
        }

        TrackingLogEntry entry = new TrackingLogEntry(from, to, userName, userId, hostName, hostIP, hostId, appName, appId, action, uid, ts, level, fromResAttrs);
        return entry;
    }

    /**
     * Returns an array of random tracking log entries
     * 
     * @param numEntries
     *            number of random entries to generate
     * @return an array of random tracking log entries
     */
    public static final TrackingLogEntry[] generateRandom(int numEntries) {
        TrackingLogEntry[] rv = new TrackingLogEntry[numEntries];
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
    public static final TrackingLogEntry[] generateLargeCustAttr(int numEntries) {	    
    	TrackingLogEntry[] entries = generateRandom(numEntries);
    	for (TrackingLogEntry thisEntry : entries) {
    		DynamicAttributes attr = thisEntry.getCustomAttr();
    		if (attr != null) {
	    		String key = "key" + (attr.size() + 1);
	    		attr.add(key, TestUtils.genRandomString(5000));
    		}
    	}
    	return entries;
    }

    /**
     * Generates a random tracking log entry
     * 
     * @return a random tracking log entry
     */
    public static final TrackingLogEntryV2 generateRandomV2() {
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
        String action = TestUtils.genRandomString(6);
        long uid = TestUtils.rand.nextLong();
        long ts = TestUtils.rand.nextLong();
        int level = TestUtils.rand.nextInt(4);
        DynamicAttributes fromResAttrs = null;
        int nbAttr = TestUtils.rand.nextInt(4);

        if (nbAttr > 0) {
            fromResAttrs = new DynamicAttributes();
            for (int i = 0; i < nbAttr; i++){
                String key = "key" + i;
                String value = "value" + i;
                fromResAttrs.put(key, value);
            }
        }

        TrackingLogEntryV2 entry = new TrackingLogEntryV2(from, to, userName, userId, hostName, hostIP, hostId, appName, appId, action, uid, ts, level, fromResAttrs);
        return entry;
    }

    /**
     * Returns an array of random tracking log entries
     * 
     * @param numEntries
     *            number of random entries to generate
     * @return an array of random tracking log entries
     */
    public static final TrackingLogEntryV2[] generateRandomV2(int numEntries) {
        TrackingLogEntryV2[] rv = new TrackingLogEntryV2[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomV2();
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
    public static final TrackingLogEntryV2[] generateLargeCustAttrV2(int numEntries) {	    
    	TrackingLogEntryV2[] entries = generateRandomV2(numEntries);
    	for (TrackingLogEntryV2 thisEntry : entries) {
    		DynamicAttributes attr = thisEntry.getCustomAttr();
    		if (attr != null) {
	    		String key = "key" + (attr.size() + 1);
	    		attr.add(key, TestUtils.genRandomString(5000));
    		}
    	}
    	return entries;
    }

}
