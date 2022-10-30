/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import java.io.ObjectInputStream;

/**
 * @author sasha
 */
public class ResourceInformationTest extends LogTestCase {

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public ResourceInformationTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that the "from resource" serialization works
     */
    public void testExternalizableFrom() {
        try {
            FromResourceInformation[] randomData = ResourceInformationTestData.generateRandomFrom(100);
            ObjectInputStream in = externalizeData(randomData);

            FromResourceInformation testRI = new FromResourceInformation();
            for (int i = 0; i < randomData.length; i++) {
                testRI.readExternal(in);
                assertEquals("Deserialized ResourceInformation should equal the original one. ", randomData[i], testRI);
            }
            in.close();

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    /**
     * This test verifies that the "to resource" serialization works
     */
    public void testExternalizableTo() {
        try {
            ToResourceInformation[] randomData = ResourceInformationTestData.generateRandomTo(100);
            ObjectInputStream in = externalizeData(randomData);

            ToResourceInformation testRI = new ToResourceInformation();
            for (int i = 0; i < randomData.length; i++) {
                testRI.readExternal(in);
                assertEquals("Deserialized ResourceInformation should equal the original one. ", randomData[i], testRI);
            }
            in.close();

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
