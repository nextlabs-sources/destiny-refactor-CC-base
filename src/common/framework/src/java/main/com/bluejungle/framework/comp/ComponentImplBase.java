package com.bluejungle.framework.comp;

import org.apache.commons.logging.Log;

// Copyright Blue Jungle, Inc.

/**
 * ComponentImplBase is a convenience base class that can be extended by
 * any class that wishes to use all (or some) component services.
 *
 * @author Sasha Vladimirov
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/comp/ComponentImplBase.java#1 $
 */
public abstract class ComponentImplBase
		implements ICompleteComponent
{
    protected Log log;
    protected IComponentManager manager;
    protected IConfiguration configuration;
    
    public void setLog(Log log)
    {
        this.log = log;
    }
    
    public void setManager(IComponentManager mgr)
    {
        this.manager = mgr;
    }
    
    public IComponentManager getManager(){
        return manager;
    }
    
    
    public Log getLog() {
        return log;
    }
    
    
    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    public void dispose() {
    }

    public void start() {
    }

    public void stop() {
    }
}
