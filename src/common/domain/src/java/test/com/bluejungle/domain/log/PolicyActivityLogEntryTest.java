package com.bluejungle.domain.log;

import java.io.ObjectInputStream;


public class PolicyActivityLogEntryTest extends LogTestCase {

    public void testExternalizable() {
        try {
            PolicyActivityLogEntry[] entries = PolicyActivityLogEntryTestData.generateRandom(100);
            ObjectInputStream in = externalizeData(entries);
            PolicyActivityLogEntry entry = new PolicyActivityLogEntry();
            for (int i = 0; i < entries.length; i++) {
                entry.readExternal(in);
                assertEquals("Deserialized policy activity log entry should equal the original.", entries[i], entry);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
