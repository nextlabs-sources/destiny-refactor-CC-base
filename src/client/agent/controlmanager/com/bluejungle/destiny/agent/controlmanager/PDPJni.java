// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.controlmanager;

import com.bluejungle.framework.comp.ComponentImplBase;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * 
 * @version $Id:
 *  //depot/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/OSWrapper.java#1
 */
public class PDPJni extends ComponentImplBase implements IPDPJni, IHasComponentInfo<PDPJni> {

    public static final String NAME = PDPJni.class.getName();

    private static final ComponentInfo<PDPJni> COMP_INFO = new ComponentInfo<PDPJni>(
    		NAME, 
    		PDPJni.class, 
    		IPDPJni.class, 
    		LifestyleType.SINGLETON_TYPE);


    /**
     * @return ComponentInfo to help creating an instance with Component Manager
     * 
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<PDPJni> getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#start()
     */
    public void start() {

    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public void stop() {

    }
    public native boolean SendPolicyResponse(long handle, String uniqueNumber, long allow, String[] attrs);

    public native boolean SendPolicyMultiResponse(long handle, String uniqueNumber, Object[] responses);

    public native boolean SendServiceResponse(long handle, String reqId, Object[] response);
}
