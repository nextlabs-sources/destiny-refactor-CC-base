/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import java.io.ObjectInputStream;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/test/com/bluejungle/domain/log/PolicyAssistantLogEntryTest.java#1 $
 *
 */
public class PolicyAssistantLogEntryTest extends LogTestCase {
    public void testExternalizable() {
        try {
        PolicyAssistantLogEntry[] entries = PolicyAssistantLogEntryTestData.generateRandom(100);
        ObjectInputStream in = externalizeData(entries);
        PolicyAssistantLogEntry entry = new PolicyAssistantLogEntry();
        for (int i = 0; i < entries.length; i++) {
            entry.readExternal(in);
            assertEquals("Deserialized tracking log entry should equal the original", entries[i], entry);
        }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
