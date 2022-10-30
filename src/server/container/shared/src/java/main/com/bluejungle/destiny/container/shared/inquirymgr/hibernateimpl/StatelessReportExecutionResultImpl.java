/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultStatistics;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionResult;

/**
 * This is the implementation class for the stateless report execution results.
 * This interface exposes a reader to allows browsing the (maybe partial) list
 * of data retrieved, and the state of the query so that the caller can come
 * back later.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/StatelessReportExecutionResultImpl.java#1 $
 */

public class StatelessReportExecutionResultImpl implements IStatelessReportExecutionResult {

    private IStatelessReportExecutionResult resultReader;

    /**
     * Constructor
     * 
     * @param newReader
     *            result reader object
     * @param newState
     *            state object
     */
    public StatelessReportExecutionResultImpl(final IStatelessReportExecutionResult newReader) {
        super();
        if (newReader == null) {
            throw new NullPointerException("Reader cannot be null");
        }
        this.resultReader = newReader;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#close()
     */
    public void close() {
        getResultReader().close();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#getAvailableRowCount()
     */
    public Long getAvailableRowCount() {
        return getResultReader().getStatistics().getAvailableRowCount();
    }

    /**
     * Returns the result reader
     * 
     * @return the result reader
     */
    protected IStatelessReportExecutionResult getResultReader() {
        return this.resultReader;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionResult#getResultState()
     */
    public IReportResultState getResultState() {
        return this.getResultReader().getResultState();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#getStatistics()
     */
    public IReportResultStatistics getStatistics() {
        return getResultReader().getStatistics();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#hasNextResult()
     */
    public boolean hasNextResult() {
        return getResultReader().hasNextResult();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader#nextResult()
     */
    public IResultData nextResult() {
        return getResultReader().nextResult();
    }
}