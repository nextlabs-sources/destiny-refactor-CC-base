/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;

/**
 * This is an empty implementation for the report result reader. This reader is
 * meant to be used when it is known for sure that the query is not going to
 * return any interesting result, and when only the statistics are of interest
 * to the caller.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportResultReaderEmptyImpl.java#2 $
 */

class ReportResultReaderEmptyImpl implements IReportResultReader {

    /**
     * Constant
     */
    private static final Long ZERO = new Long(0);
    private IReportResultStatistics stats;

    /**
     * Constructor
     * 
     * @param totalCount
     *            count to return for the query statistics
     */
    public ReportResultReaderEmptyImpl(Integer totalCount) {
        this.stats = new EmptyStats(new Long(totalCount.intValue()));
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#close()
     */
    public void close() {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#hasNextResult()
     */
    public boolean hasNextResult() {
        return false;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#getStatistics()
     */
    public IReportResultStatistics getStatistics() {
        return this.stats;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#nextResult()
     */
    public IResultData nextResult() {
        return null;
    }

    /**
     * Empty statistic class
     * 
     * @author ihanen
     */
    protected class EmptyStats implements IReportResultStatistics {

        private Long count;

        /**
         * Constructor
         */
        public EmptyStats(Long newCount) {
            super();
            this.count = newCount;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getAvailableRowCount()
         */
        public Long getAvailableRowCount() {
            return this.count;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getMinValue()
         */
        public Long getMinValue() {
            return ZERO;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getMaxValue()
         */
        public Long getMaxValue() {
            return ZERO;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getSumValue()
         */
        public Long getSumValue() {
            return ZERO;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics#getTotalRowCount()
         */
        public Long getTotalRowCount() {
            return this.count;
        }
    }
}