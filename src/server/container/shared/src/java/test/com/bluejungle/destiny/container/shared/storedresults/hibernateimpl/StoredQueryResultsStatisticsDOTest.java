/*
 * Created on Sep 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.storedresults.hibernateimpl;

import com.bluejungle.destiny.container.shared.storedresults.IStoredQueryResultsStatistics;
import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * This is the test class for the stored query results statistics class
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/storedresults/hibernateimpl/StoredQueryResultsStatisticsDOTest.java#1 $
 */

public class StoredQueryResultsStatisticsDOTest extends BaseDestinyTestCase {

    /**
     * This test verifies the basics aspects of the class
     */
    public void testStoredQueryResultsStatisticsDOClassBasics() {
        StoredQueryResultsStatisticsDO stats = new StoredQueryResultsStatisticsDO();
        assertTrue(stats instanceof IStoredQueryResultsStatistics);
    }
}