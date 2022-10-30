/*
 * Created on Dec 3, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.URL;
import java.rmi.RemoteException;
import java.text.MessageFormat;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.services.dcsf.DCSFServiceIF;
import com.bluejungle.destiny.services.dcsf.DCSFServiceLocator;
import com.bluejungle.framework.threading.ITask;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/RemoteEventDispatchWorkerImpl.java#1 $:
 */

public class RemoteEventDispatchWorkerImpl extends Thread implements IRemoteEventDispatchWorker {

    /*
     * Log related constants:
     */
    private static final MessageFormat SENDING_EVENT_MSG = new MessageFormat("Sending event: ''{0}'' to remote location: ''{1}''.");
    private static final MessageFormat SENT_EVENT_MSG = new MessageFormat("Succesfully sent event: ''{0}'' to remote location: ''{1}''");
    private static final MessageFormat SEND_EVENT_ERROR_MSG = new MessageFormat("Error when dispatching event: ''{0}'' to remote location: ''{1}''");

    protected Log log;
    private DCSFServiceLocator locator;
    
    public RemoteEventDispatchWorkerImpl() {
        super("RemoteEventDispatchWorker");
    }

    /**
     * This function calls the remote DCSF web service and notify about the
     * event
     * 
     * @param taskDefinition
     *            task definition
     */
    public void doWork(ITask taskDefinition) {

        if (!(taskDefinition instanceof RemoteEventDispatchRequest)) {
            throw new IllegalArgumentException("The task has to be an instance of RemoteEventDispatchRequest");
        }

        RemoteEventDispatchRequest request = (RemoteEventDispatchRequest) taskDefinition;
        IDCCServerEvent event = request.getEvent();
        URL targetDCSFLocation = request.getRemoteLocation();

        this.locator.setDCSFServiceIFPortEndpointAddress(targetDCSFLocation.toString());
        try {
            if (getLog().isDebugEnabled()) {
                getLog().debug(SENDING_EVENT_MSG.format(new Object[] { event.getName(), targetDCSFLocation }));
            }

            DCSFServiceIF dcsfService = this.locator.getDCSFServiceIFPort();
            dcsfService.notifyEvent(WebServiceHelper.convertDCCServerEventToServiceType(event));

            if (getLog().isDebugEnabled()) {
                getLog().debug(SENT_EVENT_MSG.format(new Object[] { event.getName(), targetDCSFLocation }));
            }
        } catch (ServiceException serviceEx) {
            getLog().error(SEND_EVENT_ERROR_MSG.format(new Object[] { event.getName(), targetDCSFLocation }), serviceEx);
        } catch (RemoteException remoteEx) {
            getLog().error(SEND_EVENT_ERROR_MSG.format(new Object[] { event.getName(), targetDCSFLocation }), remoteEx);
        }

    }

    /**
     * Cleanup function
     */
    public void dispose() {
        this.interrupt();
        this.locator = null;
    }

    /**
     * Initialization function.
     */
    public void init() {
        this.locator = getDCSFServiceLocator();
        this.setDaemon(true);
        this.start();
    }

    /**
     * This method is here to support JUNIT. It returns a DCSF service locator.
     * 
     * @return the DCSF service locator
     */
    protected DCSFServiceLocator getDCSFServiceLocator() {
        return new DCSFServiceLocator();
    }

    /**
     * Sets the log object
     * 
     * @param newLog
     *            new log object to set
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    public Log getLog() {
        return this.log;
    }

}