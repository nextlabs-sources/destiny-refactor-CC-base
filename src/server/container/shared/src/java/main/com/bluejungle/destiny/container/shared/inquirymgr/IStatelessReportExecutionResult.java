/*
 * Created on Apr 5, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr;

/**
 * This is the stateless report execution result interface. This interface
 * represents results returned during a stateless report execution.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/branch/Destiny_Beta2/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/IStatelessReportExecutionResult.java#1 $
 */

public interface IStatelessReportExecutionResult extends IReportResultReader {

    /**
     * Returns the state of the results. This state needs to be preserved by the
     * caller for the next call.
     * 
     * @return the current report result state.
     */
    public IReportResultState getResultState();
}