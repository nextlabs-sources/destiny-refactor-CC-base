package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/pdpapi/PDPSDK.java#1 $:
 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.destiny.agent.controlmanager.ControlManagerStub;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.controlmanager.PDPRequestTaskImpl;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.utils.DynamicAttributes;

public class PDPSDK
{
    public static int WAIT_FOREVER = -1;
    public static String MONITOR_APP_ACTION = "ce::monitor_application";

    public static int NOISE_LEVEL_SYSTEM = 1;
    public static int NOISE_LEVEL_APPLICATION = 2;
    public static int NOISE_LEVEL_USER_ACTION = 3;
    
    private static IControlManager controlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
    private static IOSWrapper osWrapper = ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);

    /**
     * Make a policy query
     *
     * @param action the action
     * @param resources an array of resources.  This should contain at least one resource
     * @param user the user
     * @param application the application
     * @param host the host
     * @param performObligations should obligations be performed as a result of this query?
     * @param additionalData used for any additional attribute sets (e.g. "sendto" for email recipients).  Can be null
     * @param noiseLevel NOISE_LEVEL_SYSTEM, NOISE_LEVEL_APPLICATION, or NOISE_LEVEL_USER_ACTION
     * @param timeoutInMs when invoking synchronously, specifies the timeout.  Use WAIT_FOREVER (-1) to wait forever
     * @param cb the callback object.  Specify IPDPSDKCallback.NONE for synchronous calls
     *
     * Notes
     *   Unless specified, arguments can not be null.
     *
     *   If you define you own callback the timeout value is ignored
     *   and we will simply call the callback object when the query is
     *   done.  You must determine if the query timed out yourself
     *
     *   The objects may be modified in the process of policy
     *   evaluation.  For this reason you should not assume that the
     *   same object can be used for multiple invocations of this
     *   query.  New objects should be constructed before each call
     */

    public static IPDPEnforcement PDPQueryDecisionEngine(String action,
                                                         IPDPResource[] resources,
                                                         IPDPUser user,
                                                         IPDPApplication application,
                                                         IPDPHost host,
                                                         boolean performObligations,
                                                         IPDPNamedAttributes[] additionalData,
                                                         int noiseLevel,
                                                         int timeoutInMs,
                                                         IPDPSDKCallback cb) throws PDPTimeout, PDPException, IllegalArgumentException {

        if (action == null) {
            throw new IllegalArgumentException("action is null");
        }

        if (resources == null) {
            throw new IllegalArgumentException("resources is null");
        }

        if (resources.length < 1 || resources.length > 2) {
            throw new IllegalArgumentException("resources array must contain either one or two items.  Contains " + resources.length);
        }

        for (IPDPResource res : resources) {
            if (res == null) {
                throw new IllegalArgumentException("resource is null");
            }
        }

        if (application == null) {
            throw new IllegalArgumentException("application is null");
        }

        if (host == null) {
            throw new IllegalArgumentException("host is null");
        }

        if (cb == null) {
            throw new IllegalArgumentException("Callback object is null.  To request no callback use IPDPSDKCallback.NONE");
        }

        if (timeoutInMs != WAIT_FOREVER && timeoutInMs < 0) {
            throw new IllegalArgumentException("timeout must be either WAIT_FOREVER or >= 0");
        }

        Long processToken = 0L;
        String pid = (application == PDPApplication.NONE) ? null : application.getValue("pid");
        
        if (pid != null && !pid.equals("0")) {
            try {
                processToken = osWrapper.getProcessToken(Integer.parseInt(pid));
            } catch (NumberFormatException e) {
            }
        }
        
        Map<String, DynamicAttributes> attrs = new HashMap<String, DynamicAttributes>();

        DynamicAttributes actionMap = new DynamicAttributes(1);
        actionMap.put("name", action);
        attrs.put("action", actionMap);

        for (IPDPResource resource : resources) {
            resource.addSelfToMap(attrs);
        }

        user.addSelfToMap(attrs);
        application.addSelfToMap(attrs);
        host.addSelfToMap(attrs);
        
        if (additionalData != null) {
            for (IPDPNamedAttributes extraItem : additionalData) {
                if (extraItem == null) {
                    throw new IllegalArgumentException("additionalData contains null entry");
                }
                extraItem.addSelfToMap(attrs);
            }
        }

        if (cb == IPDPSDKCallback.NONE) {
            final Semaphore sem = new Semaphore(1);
            sem.acquireUninterruptibly();
            final IPDPEnforcement[] result = new IPDPEnforcement[1];
            result[0] = null;
            
            cb = new IPDPSDKCallback() {
                public void callback(IPDPEnforcement enforcement) {
                    result[0] = enforcement;
                    sem.release();
                }
            };

            PDPRequestTaskImpl pdpTask = new PDPRequestTaskImpl(attrs,
                                                                !performObligations,
                                                                noiseLevel,
                                                                processToken,
                                                                cb);

            ControlManagerStub.getInstance().doPDPRequest(pdpTask);

            try {
                boolean gotSemaphore = true;

                if (timeoutInMs == WAIT_FOREVER) {
                    sem.acquire();
                } else {
                    gotSemaphore = sem.tryAcquire(timeoutInMs, TimeUnit.MILLISECONDS);
                }

                if (gotSemaphore) {
                    // We got a result.  Return it
                    return result[0];
                } else {
                    // No result
                    throw new PDPTimeout();
                }
            } catch (InterruptedException e) {
                throw new PDPTimeout("Callback was interrupted", e);
            }
        } else {
            PDPRequestTaskImpl pdpTask = new PDPRequestTaskImpl(attrs,
                                                                !performObligations,
                                                                noiseLevel,
                                                                processToken,
                                                                cb);
            
            ControlManagerStub.getInstance().doPDPRequest(pdpTask);

            return null;
        }
    }

    /**
     * Log obligation data
     *
     * @param logId the log id (usually returned in the obligation) of the policy activity log
     * @param assistantName the name of the Policy Assistant (e.g. "Encryption Assistant")
     * @param assistantOptions any options supplied to the assistant
     * @param assistantDescription describes the Policy Asssistant (e.g. "This Policy Assistant performs symmetric encryption of documents with a provided key")
     * @param assistantUserAction a description of the actions the user took (e.g. "User cancelled")
     *
     * Note: None of the arguments can be null.  Empty string is acceptable for everything except the logId
     */
    public static void PDPLogObligationData(String logId, String assistantName, String assistantOptions, String assistantDescription, String assistantUserActions) {
        
        if (logId == null) {
            throw new IllegalArgumentException("logId is null");
        }

        if (assistantName == null) {
            throw new IllegalArgumentException("assistantName is null");
        }

        if (assistantOptions == null) {
            throw new IllegalArgumentException("assistantOptions is null");
        }

        if (assistantDescription == null) {
            throw new IllegalArgumentException("assistantDescription is null");
        }

        if (assistantUserActions == null) {
            throw new IllegalArgumentException("assistantUserActions is null");
        }

        ControlManagerStub.getInstance().logAssistantData(logId, assistantName, assistantOptions, assistantDescription, assistantUserActions, null);
    }
}
