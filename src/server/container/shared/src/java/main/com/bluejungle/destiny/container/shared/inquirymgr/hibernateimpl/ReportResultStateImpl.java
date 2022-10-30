/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportResultStateImpl.java#1 $
 */

class ReportResultStateImpl implements IReportResultState {

    private Long firstRowSeqId;
    private Long lastRowSeqId;
    private Long queryId;

    /**
     * Constructor
     */
    public ReportResultStateImpl() {
        super();
    }

    /**
     * Copy constructor
     * 
     * @param state
     *            state to copy from
     */
    public ReportResultStateImpl(IReportResultState state) {
        //Make sure we have new object, and not simply references
        this.queryId = new Long(state.getQueryId().longValue());
        if (state.getFirstRowSequenceId() != null) {
            this.firstRowSeqId = new Long(state.getFirstRowSequenceId().longValue());
        }
        if (state.getLastRowSequenceId() != null) {
            this.lastRowSeqId = new Long(state.getLastRowSequenceId().longValue());
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState#getFirstRowSequenceId()
     */
    public Long getFirstRowSequenceId() {
        return this.firstRowSeqId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState#getLastRowSequenceId()
     */
    public Long getLastRowSequenceId() {
        return this.lastRowSeqId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.IReportResultState#getQueryId()
     */
    public Long getQueryId() {
        return this.queryId;
    }

    /**
     * Sets the sequence id for the first row in the list of results
     * 
     * @param newId
     *            new id to set
     */
    public void setFirstRowSequenceId(Long newId) {
        this.firstRowSeqId = newId;
    }

    /**
     * Sets the sequence id for the last row in the list of results
     * 
     * @param newId
     *            new id to set
     */
    public void setLastRowSequenceId(Long newId) {
        this.lastRowSeqId = newId;
    }

    /**
     * Sets the id of the stored query that this state refers to
     * 
     * @param newId
     *            new id to set
     */
    public void setQueryId(Long newId) {
        this.queryId = newId;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return ("FirstRowId: " + firstRowSeqId + " LastRowId: " + lastRowSeqId + " QueryId:" + queryId);
    }
}