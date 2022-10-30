// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc;

import java.util.ArrayList;

/**
 * 
 * This interface must be implemented by custom RequestHandlers. Requests
 * received by IPCStub are routed to an instance of IRequestHandler. Most
 * concrete implementations should extend RequestHandlerBase.
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */
public interface IRequestHandler {

    /**
     * @param methodName
     *            name of method to be invoked
     * @param paramArray
     *            input parameter array
     * @param resultParamArray
     *            output parameter array
     * @return true if invocation is successful
     * 
     * this method invokes the method specified.
     */
    public boolean invoke(String methodName, ArrayList paramArray, ArrayList resultParamArray);

}