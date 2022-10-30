/*
 * Created on Apr 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * This is a very simple test class for the empty reader implementation.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportResultReaderEmptyImplTest.java#1 $
 */

public class ReportResultReaderEmptyImplTest extends BaseDestinyTestCase {

    /**
     * Verifies the basics of the class and a few other simple properties
     */
    public void testReportResultReaderEmptyImplClassBasics() {
        Integer totalCount = new Integer(15);
        ReportResultReaderEmptyImpl emptyReader = new ReportResultReaderEmptyImpl(totalCount);
        assertTrue("The class should implement the right interface", emptyReader instanceof IReportResultReader);
        assertNotNull("Statistics should always be available", emptyReader.getStatistics());
        assertNull("No next results should ever be available", emptyReader.nextResult());
        assertFalse("No next results should ever be available", emptyReader.hasNextResult());
    }

    /**
     * This test verifies the various setters and getters
     */
    public void testReportResultReaderEmptyImplSettersAndGetters() {
        final Integer totalCount = new Integer(15);
        final Long zero = new Long(0);
        ReportResultReaderEmptyImpl emptyReader = new ReportResultReaderEmptyImpl(totalCount);
        assertEquals("Available row count should be correct", new Long(totalCount.intValue()), emptyReader.getStatistics().getAvailableRowCount());
        assertEquals("Total row count should be correct", new Long(totalCount.intValue()), emptyReader.getStatistics().getTotalRowCount());
        assertEquals("Min value should be correct", zero, emptyReader.getStatistics().getMinValue());
        assertEquals("Max value should be correct", zero, emptyReader.getStatistics().getMaxValue());
        assertEquals("Sum Value should be correct", zero, emptyReader.getStatistics().getSumValue());
    }
}