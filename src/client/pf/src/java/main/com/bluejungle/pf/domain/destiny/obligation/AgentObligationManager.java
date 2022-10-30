/*
 * Created on Dec 28, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.pf.domain.destiny.obligation;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;

import java.util.List;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/obligation/AgentObligationManager.java#1 $:
 */

public class AgentObligationManager extends DObligationManager implements ILogEnabled, IManagerEnabled, IInitializable {

    private static final String CLASSNAME = AgentObligationManager.class.getName();

    public static final ComponentInfo COMP_INFO = new ComponentInfo(IDObligationManager.CLASSNAME, CLASSNAME, IDObligationManager.CLASSNAME, LifestyleType.SINGLETON_TYPE);

    private ICommandExecutor executor;
    private IComponentManager manager;
    private Log log;

    public AgentObligationManager() {
    }

    /**
     * @see IDObligationManager#getObligation(String)
     */
    public LogObligation createLogObligation() {
        return new AgentLogObligation(executor);
    }

    /**
     * @see IDObligationManager#createNotifyObligation(String, String)
     */
    public NotifyObligation createNotifyObligation(String emailAddresses, String body) {
        emailAddresses = sanitizeList( emailAddresses );
        emailAddresses = validateAddresses(emailAddresses);
        return new AgentNotifyObligation(emailAddresses, body, executor);
    }
    
    /**
     * @see DObligationManager#createDisplayObligation(String)
     */
    public DisplayObligation createDisplayObligation(String message) {
        return new AgentDisplayObligation(message);
    }

    /**
     * @see DObligationManager#createCustomObligation(String)
     */
    public CustomObligation createCustomObligation(String pqlName, List arguments) {
        return new AgentCustomObligation(pqlName, arguments);
    }

    /**
     * @see ILogEnabled#setLog(Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see IManagerEnabled#setManager(IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * @see IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }

    /**
     * @see IInitializable#init()
     */
    public void init() {
        executor = (ICommandExecutor) manager.getComponent(ICommandExecutor.NAME);
    }
}
