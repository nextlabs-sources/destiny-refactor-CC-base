/*
 * Created on Dec 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.ipc;

import java.util.ArrayList;

import com.bluejungle.framework.comp.IInitializable;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface IIPCProxy extends IInitializable {

    public static final String REQUEST_HANDLER_CLASS_NAME = "REQUEST_HANDLER_CLASS_NAME";

    /**
     * sends method invocation request using IPC mechanism to server and parses
     * the response and returns it in resultParams
     * 
     * @param methodName
     *            name of method to invoke
     * @param inputParams
     *            array of strings specifying input parameters
     * @param resultParams
     *            array of strings containing result parameters returned from
     *            the invocation
     * @return true if invocation is successful, false
     */
    public abstract boolean invoke(String methodName, ArrayList inputParams, ArrayList resultParams);

    /**
     * send disconnect request to the server. general cleanup.
     */
    public abstract void uninit();
}