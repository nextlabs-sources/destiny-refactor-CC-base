/*
 * Created on Feb 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.deployment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.policydeploymentmgr.ITargetStatus;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.TargetAgentDO;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.threading.ITask;

/**
 * This is the push worker implementation class. This class picks up a task,
 * extract host and port information, and connect to the remote machine on the
 * given port. If the operation succeeds or fails, an appropriate status is
 * saved.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/src/java/main/com/bluejungle/destiny/container/dabs/components/deployment/PushWorkerThreadImpl.java#1 $
 */

public class PushWorkerThreadImpl implements ILogEnabled, IPushWorker {

    private static final String PUSH_COMMAND = "x";
    private static Log log;

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see com.bluejungle.framework.threading.IWorker#doWork(com.bluejungle.framework.threading.ITask)
     */
    public void doWork(ITask task) {

        //Check that we have the right object
        if (task == null) {
            log.error("Null task passed to DABS push worker thread.");
            throw new NullPointerException("The push worker thread task cannot be null");
        }
        if (!(task instanceof PushRequest)) {
            log.error("Invalid push request passed to DABS push worker thread.");
            throw new IllegalArgumentException("Task should be a push request object");
        }

        PushRequest pushRequest = (PushRequest) task;
        TargetAgentDO targetAgent = (TargetAgentDO) pushRequest.getAgent();
        targetAgent.setStatus(ITargetStatus.IN_PROGRESS);
        String targetAgentHostName = targetAgent.getAgent().getHost();
        Integer port = targetAgent.getAgent().getPushPort();

        Socket socket = null;
        try {
            //Performs the connection with the agent
            socket = new Socket(targetAgentHostName, port.intValue());
            PrintWriter output = null;
            output = new PrintWriter(socket.getOutputStream());
            output.print(PUSH_COMMAND);
            output.flush();
            //Reads the answer
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            in.read();
            socket.close();
            targetAgent.setStatus(ITargetStatus.SUCCEEDED);
        } catch (UnknownHostException e) {
            if (log.isDebugEnabled()) {
                log.debug("Push failed to host " + targetAgentHostName + " on port " + port + ":", e);
            }
            targetAgent.setStatus(ITargetStatus.FAILED);
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Push failed to host " + targetAgentHostName + " on port " + port + ":", e);
            }
            targetAgent.setStatus(ITargetStatus.FAILED);
        }

        //Notify that the task of the worker is complete
        PushRequestCounter prc = pushRequest.getPushRequestCounter();
        if (log.isTraceEnabled()) {
            log.trace("Completed push for '" + targetAgentHostName + "' on port " + port + ".");
        }
        synchronized (prc) {
            prc.incrementCount();
        }
        if (log.isTraceEnabled()) {
            log.trace("Incremented counter for '" + targetAgentHostName + "' on port " + port + ".");
        }
    }
}