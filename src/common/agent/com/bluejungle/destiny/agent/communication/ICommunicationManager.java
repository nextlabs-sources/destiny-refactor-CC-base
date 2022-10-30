/*
 * Created on Jan 18, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.communication;

import javax.xml.rpc.ServiceException;

import com.bluejungle.destiny.services.agent.AgentServiceIF;
import com.nextlabs.destiny.interfaces.log.v5.LogServiceIF;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface ICommunicationManager {

    public static final String NAME = "Agent Communication Manager";

    /**
     * Reinitialize the Communication Manager. Reset service interfaces if DABS
     * location has changed. Reset push listener if configuration has changed.
     */
    public abstract void reinit();

    /**
     * @return port number that push listener is waiting on.
     */
    public abstract int getPort();

    /**
     * Returns the agentServiceIF.
     * 
     * @return the agentServiceIF.
     * @throws ServiceException
     */
    public abstract AgentServiceIF getAgentServiceIF() throws ServiceException;

    /**
     * Returns the logServiceIF.
     * 
     * @return the logServiceIF.
     * @throws ServiceException
     */
    public abstract LogServiceIF getLogServiceIF() throws ServiceException;
}
