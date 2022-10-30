package com.bluejungle.destiny.agent.controlmanager;

/*
 * Created on Jan 20, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/PDPRequestTaskImpl.java#1 $:
 */

import java.util.Map;

import com.bluejungle.destiny.agent.pdpapi.IPDPSDKCallback;
import com.bluejungle.framework.threading.ITask;
import com.bluejungle.framework.utils.DynamicAttributes;

public class PDPRequestTaskImpl implements ITask {
    private Map<String, DynamicAttributes> arguments;
    private boolean ignoreObligations;
    private int loggingLevel;
    private IPDPSDKCallback cb;
    private Long processToken;

    /**
     *
     */
    public PDPRequestTaskImpl(Map<String, DynamicAttributes> arguments, boolean ignoreObligations, int loggingLevel, Long processToken, IPDPSDKCallback cb) {
        this.arguments = arguments;
        this.ignoreObligations = ignoreObligations;
        this.loggingLevel = loggingLevel;
        this.processToken = processToken;
        this.cb = cb;
    }

    public Map<String, DynamicAttributes> getArguments() {
        return arguments;
    }

    public boolean getObligationsFlag() {
        return ignoreObligations;
    }

    public int getLoggingLevel() {
        return loggingLevel;
    }

    public IPDPSDKCallback getCallback() {
        return cb;
    }

    public Long getProcessToken() {
        return processToken;
    }
}
