/*
 * Created on <unknown>
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 * 
 */
package com.bluejungle.domain.log;

import java.io.ObjectInputStream;


/**
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/test/com/bluejungle/domain/log/PolicyActivityInfoTest.java#1 $
 *
 */
public class PolicyActivityInfoTest extends LogTestCase {

    public void testExternalizable() {
        try { 
        PolicyActivityInfo[] entries = PolicyActivityInfoTestData.generateRandom(100);
        ObjectInputStream in = externalizeData(entries);
        PolicyActivityInfo info = new PolicyActivityInfo();
        for (int i = 0; i < entries.length; i++) {
            info.readExternal(in);
            assertEquals("Deserialized PolicyActivityInfo should equal the original one.", entries[i], info);
        }
    } catch (Exception e) {
        fail(e.getMessage());
    }

    }

}
