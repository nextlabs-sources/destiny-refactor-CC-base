/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import com.bluejungle.framework.utils.TestUtils;

/**
 * The purpose of this class is to provide enough sample data to aid in testing
 * of ResourceInformation and other classes that may incorporate instances of
 * ResourceInformation
 * 
 * 
 * @author sasha
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/domain/src/java/test/com/bluejungle/domain/log/ResourceInformationTestData.java#1 $
 *  
 */
public class ResourceInformationTestData {

    /**
     * Returns the requested number of ResourceInformation instances with
     * randomly-generated data inside. Names are 64 characters long, owner ids
     * are 45 characters long.
     * 
     * No guarantees are made regarding statistical properties of "randomness".
     * 
     * @param numEntries
     *            number of random instances to generated
     * @return randomly-generated instances.
     */
    public static final FromResourceInformation[] generateRandomFrom(int numEntries) {
        FromResourceInformation[] rv = new FromResourceInformation[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomFrom();
        }
        return rv;
    }

    /**
     * Returns an instance of FromResourceInformation with randomly-generated
     * data inside. Names are 64 characters long. Owner ids are 45 characters
     * long No guarantees are made regarding statistical properties of
     * "randomness".
     * 
     * @return randomly-generated instance
     */
    public static final FromResourceInformation generateRandomFrom() {
        String name = "\\\\some.server.domain\\some_share_name\\" + TestUtils.genRandomString(85);
        String ownerId = "S-1-5-21-668023798-3031861066-1043980994-" + TestUtils.genRandomNumericString(4);
        long size = TestUtils.rand.nextInt(Integer.MAX_VALUE);
        long createdDate = TestUtils.rand.nextInt(Integer.MAX_VALUE);
        long modifiedDate = TestUtils.rand.nextInt(Integer.MAX_VALUE);
        return new FromResourceInformation(name, size, createdDate, modifiedDate, ownerId);
    }

    /**
     * Returns the requested number of ToResourceInformation instances with
     * randomly-generated data inside. Names are 64 characters long
     * 
     * No guarantees are made regarding statistical properties of "randomness".
     * 
     * @param numEntries
     *            number of random instances to generated
     * @return randomly-generated instances.
     */
    public static final ToResourceInformation[] generateRandomTo(int numEntries) {
        ToResourceInformation[] rv = new ToResourceInformation[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomTo();
        }
        return rv;
    }

    /**
     * Returns an instance of ToResourceInformation with randomly-generated data
     * inside. Names are 64 characters long.
     * 
     * @return randomly-generated instance
     */
    public static final ToResourceInformation generateRandomTo() {
        String name = "\\\\some.server.domain\\some_share_name\\" + TestUtils.genRandomString(85);
        return new ToResourceInformation(name);
    }
}
