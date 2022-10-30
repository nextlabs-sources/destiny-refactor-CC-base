package com.bluejungle.destiny.agent.pdpapi;

/*
 * Created on Jan 19, 2011
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/embeddedapi/com/bluejungle/destiny/agent/pdpapi/PDPSDK.java#1 $:
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.destiny.agent.controlmanager.ControlManagerStub;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.controlmanager.PDPRequestTaskImpl;
import com.bluejungle.framework.comp.ComponentManagerFactory;

import com.simontuffs.onejar.Boot;

public class PDPSDK
{
    public static int WAIT_FOREVER = -1;
    public static String MONITOR_APP_ACTION = "ce::monitor_application";

    public static int NOISE_LEVEL_SYSTEM = 1;
    public static int NOISE_LEVEL_APPLICATION = 2;
    public static int NOISE_LEVEL_USER_ACTION = 3;
    
    /**
     * Make a policy query
     *
     * Unless specified, arguments can not be null. Some classes
     * provide a "NONE" value, which can be used if the argument is no
     * relevant. e.g. <code>PDPApplication.NONE</code>
     *
     * If a callback is supplied the timeout value is ignored and
     * callback object's <code>callback</code> method will be called
     * when the query is done. It is the responsibility of the
     * callback object to determine if the query timed out.
     *
     * The objects may be modified in the process of policy
     * evaluation.  For this reason you should not assume that the
     * same object can be used for multiple invocations of this query.
     * New objects should be constructed before each call
     *
     * The enforcement response will contain a result string, either
     * "allow" or "deny". Normally if no policies apply, the result
     * will be allow. Optionally, a result of "dontcare" can be
     * returned under these circumstances. To specify that a
     * "dontcare" result is acceptable, add the attribute key
     * "dont-care-acceptable" with the value "yes" to the "from"
     * resource

     * @param action the action
     * @param resources an array of resources.  This must contain one resource (with the name "from"). If a second resource
     * is specified then it should have the name "to". This would be relevate for user actions that involve a source and destination
     * resource (e.g. copying a file from one location to another)
     * @param user the user
     * @param application the application
     * @param host the host
     * @param performObligations should obligations be performed as a result of this query?
     * @param additionalData used for any additional attribute sets (e.g. "sendto" for email recipients).  Can be null
     * @param noiseLevel NOISE_LEVEL_SYSTEM, NOISE_LEVEL_APPLICATION, or NOISE_LEVEL_USER_ACTION
     * @param timeoutInMs when invoking synchronously, specifies the timeout.  Use WAIT_FOREVER (-1) to wait forever
     * @param cb the callback object.  Specify IPDPSDKCallback.NONE for synchronous calls
     * @return the enforcement response
     *
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
        
        Map<String, Object> attrs = new HashMap<String, Object>();

        Object actionMap = dynamicAttributesNewInstance();
        dynamicAttributesAddMethod(actionMap, "name", action);
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

            try {
                Object pdpTask = pdpRequestTaskImplClassConstructor.newInstance(attrs,
                                                                                !performObligations,
                                                                                noiseLevel,
                                                                                processToken,
                                                                                cb);
                
                controlManagerStubDoPDPRequest.invoke(controlManagerStub, pdpTask);
            } catch (IllegalAccessException iae) {
                throw new PDPException("Unable to call doPDPRequest", iae);
            } catch (InvocationTargetException ite) {
                throw new PDPException("Unable to call doPDPRequest", ite);
            } catch (InstantiationException ie) {
                throw new PDPException("Unable to create PDPRequestTaskImpl object", ie);
            }
                
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
            try {
                Object pdpTask = pdpRequestTaskImplClassConstructor.newInstance(attrs,
                                                                                !performObligations,
                                                                                noiseLevel,
                                                                                processToken,
                                                                                cb);
                
                controlManagerStubDoPDPRequest.invoke(controlManagerStub, pdpTask);
            } catch (IllegalAccessException iae) {
                throw new PDPException("Unable to call doPDPRequest", iae);
            } catch (InvocationTargetException ite) {
                throw new PDPException("Unable to call doPDPRequest", ite);
            } catch (InstantiationException ie) {
                throw new PDPException("Unable to create PDPRequestTaskImpl object", ie);
            }
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
     * @param assistantUserActions a description of the actions the user took (e.g. "User cancelled")
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

        try {
            controlManagerStubLogAssistantData.invoke(controlManagerStub, logId, assistantName, assistantOptions, assistantDescription, assistantUserActions, null);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException(ite);
        }
    }

    static Object dynamicAttributesNewInstance() {
        try {
            return dynamicAttributesConstructor.newInstance();
        } catch (InstantiationException ie) {
            throw new IllegalStateException("Unable to create DynamicAttributes object", ie);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException("Unable to create DynamicAttributes object", iae);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException("Unable to create DynamicAttributes object", ite);
        }
    }
    
    static void dynamicAttributesAddMethod(Object attrs, String key, String value) {
        try {
            dynamicAttributesAdd.invoke(attrs, key, value);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException("Unable to call DynamicAttributes.add", iae);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException("Unable to call DynamicAttributes.add", ite);
        }
    }
    
    static String dynamicAttributesGetStringMethod(Object attrs, String key) {
        try {
            return (String)dynamicAttributesGetString.invoke(attrs, key);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException("Unable to call DynamicAttributes.getString()", iae);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException("Unable to call DynamicAttributes.getString()", ite);
        }
    }
    
    static String[] dynamicAttributesGetStringsMethod(Object attrs, String key) {
        try {
            return (String[])dynamicAttributesGetStrings.invoke(attrs, key);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException("Unable to call DynamicAttributes.getStrings()", iae);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException("Unable to call DynamicAttributes.getStrings()", ite);
        }
    }

    static Set<String> dynamicAttributesKeySetMethod(Object attrs) {
        try {
            return (Set<String>)dynamicAttributesKeySet.invoke(attrs);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException("Unable to call DynamicAttributes.keySet()", iae);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException("Unable to call DynamicAttributes.keySet()", ite);
        }
    }
    
    static void dynamicAttributesRemoveMethod(Object attrs, Object key) {
        try {
            dynamicAttributesRemove.invoke(attrs, key);
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException("Unable to call DynamicAttributes.remove()", iae);
        } catch (InvocationTargetException ite) {
            throw new IllegalStateException("Unable to call DynamicAttributes.remove()", ite);
        }
    }
    
    private static Object controlManagerStub;
    private static Method controlManagerStubDoPDPRequest;
    private static Method controlManagerStubLogAssistantData;
    private static Method controlMngrMain;
    private static Method controlManagerStubGetInstance;
    private static Method dynamicAttributesAdd;
    private static Method dynamicAttributesGetString;
    private static Method dynamicAttributesGetStrings;
    private static Method dynamicAttributesKeySet;
    private static Method dynamicAttributesRemove;
    
    private static Object componentManagerImpl;
    private static Method componentManagerShutdown;
    
    private static Constructor<?> pdpRequestTaskImplClassConstructor;
    private static Constructor<?> dynamicAttributesConstructor;
    
    private static Method getControlMngrMain(ClassLoader cl) throws PDPException {
        
        try {
            Class<?> controlManagerClass = Class.forName("com.bluejungle.destiny.agent.controlmanager.ControlMngr", true, cl);

            try {
                return controlManagerClass.getMethod("main", String[].class);
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find main() method for ControlMngr", nsme);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new PDPException("Unable to find class com.bluejungle.destiny.agent.controlmanager.ControlMngr", cnfe);
        }
    }

    private static void getMethods(ClassLoader cl) throws PDPException {
        Class<?> pdpRequestTaskImplClass = null;
        
        try {
            pdpRequestTaskImplClass = Class.forName("com.bluejungle.destiny.agent.controlmanager.PDPRequestTaskImpl", true, cl);

            try {
                pdpRequestTaskImplClassConstructor = pdpRequestTaskImplClass.getConstructor(Map.class, boolean.class, int.class, Long.class, IPDPSDKCallback.class);
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find constructor for PDPRequestTaskImpl", nsme);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new PDPException("Unable to find class com.bluejungle.destiny.agent.controlmanager.PDPRequestTaskImpl", cnfe);
        }

        try {
            Class<?> dynamicAttributesClass = Class.forName("com.bluejungle.framework.utils.DynamicAttributes", true, cl);

            try {
                dynamicAttributesConstructor = dynamicAttributesClass.getConstructor();
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find constructor for DynamicAttributes", nsme);
            }

            try {
                dynamicAttributesAdd = dynamicAttributesClass.getMethod("add", String.class, String.class);
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find DynamicAttributes.add()", nsme);
            }
            
            try {
                dynamicAttributesGetString = dynamicAttributesClass.getMethod("getString", String.class);
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find DynamicAttributes.getString()", nsme);
            }
            
            try {
                dynamicAttributesGetStrings = dynamicAttributesClass.getMethod("getStrings", String.class);
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find DynamicAttributes.getStrings()", nsme);
            }
            
            try {
                dynamicAttributesKeySet = dynamicAttributesClass.getMethod("keySet");
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find DynamicAttributes.keySet()", nsme);
            }
            try {
                dynamicAttributesRemove = dynamicAttributesClass.getMethod("remove", Object.class);
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find DynamicAttributes.remove()", nsme);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new PDPException("Unable to find class com.bluejungle.framework.utils", cnfe);
        }
        
        try {
            Class<?> controlManagerStubClass = Class.forName("com.bluejungle.destiny.agent.controlmanager.ControlManagerStub", true, cl);
            
            try {
                controlManagerStubGetInstance = controlManagerStubClass.getMethod("getInstance");
                
                try {
                    controlManagerStub = controlManagerStubGetInstance.invoke(null);
                } catch (IllegalAccessException iae) {
                    throw new PDPException("Unable to invoke ControlManagerStub.getInstance()", iae);
                } catch (InvocationTargetException ite) {
                    throw new PDPException("Unable to invoke ControlManagerStub.getInstance()", ite);
                }
                
                try {
                    controlManagerStubDoPDPRequest = controlManagerStubClass.getMethod("doPDPRequest", pdpRequestTaskImplClass);
                } catch (NoSuchMethodException nsme) {
                    throw new PDPException("Unable to find doPDPRequest() method for ControlManagerStub", nsme);
                }
                
                try {
                    controlManagerStubLogAssistantData = controlManagerStubClass.getMethod("logAssistantData", String.class, String.class, String.class, String.class, String.class, String[].class);
                } catch (NoSuchMethodException nsme) {
                    throw new PDPException("Unable to find logAssistantData() method for ControlManagerStub", nsme);
                }
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find getInstance() method for ControlManagerStub", nsme);
            }
        } catch (ClassNotFoundException cnfe) {
            throw new PDPException("Unable to find class com.bluejungle.destiny.agent.controlmanager.ControlManagerStub", cnfe);
        }

        try {
            Class<?> componentManagerFactoryClass = Class.forName("com.bluejungle.framework.comp.ComponentManagerFactory", true, cl);

            try {
                Method componentManagerFactoryGetComponentManager = componentManagerFactoryClass.getMethod("getComponentManager");

                try {
                    componentManagerImpl = componentManagerFactoryGetComponentManager.invoke(null);
                } catch (IllegalAccessException iae) {
                    throw new PDPException("Unable to invoke ComponentManagerFactory.getComponentManager()", iae);
                } catch (InvocationTargetException ite) {
                    throw new PDPException("Unable to invoke ComponentManagerFactory.getComponentManager()", ite);
                }
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find getComponentManager() method for ComponentMangerFactory", nsme);
            }

            try {
                componentManagerShutdown = componentManagerImpl.getClass().getMethod("shutdown");
            } catch (NoSuchMethodException nsme) {
                throw new PDPException("Unable to find shutdown() method for " + componentManagerImpl.getClass().getName());
            }
        } catch (ClassNotFoundException cnfe) {
            throw new PDPException("Unable to find class com.bluejungle.framework.comp.ComponentManagerFactory", cnfe);
        }
    }
    
    /**
     * Do not call this method
     * 
     * The OneJar code was designed for applications and this is assumed to be the application
     * entry point. This is an empty function that exists purely to satisfy it.
     */
    public static void main(String[] ignore) {
    }

    /**
     * Initialize the embedded PDP. This must be called before any policy evaluation queries are made
     * 
     * @param rootDirectory the root directory of the PDP. This specifies the location of the PDP configuration files, bundle, etc.
     */
    public static void initializePDP(String rootDirectory) throws PDPException { 
        // Initialize the onejar code
        try {
            Boot.main(new String[0]);
        } catch (Exception e) {
            throw new PDPException("Error callng Boot.main()", e);
        }
        
        System.setProperty("dpc.install.home", rootDirectory);
        
        ArrayList<String> args = new ArrayList<String>();
        args.add("PORTAL");
        args.add("SDK");
        args.add("RootDirectory=" + rootDirectory);
        
        ClassLoader oneJarClassLoader = Boot.getClassLoader();
        
        try {
            // Static method
            getControlMngrMain(oneJarClassLoader).invoke(null, new Object[] { args.toArray(new String[args.size()]) } );
        } catch (IllegalAccessException iae) {
            throw new PDPException("Unable to invoke ControlMngr.main()", iae);
        } catch (InvocationTargetException ite) {
            throw new PDPException("Unable to invoke ControlMngr.main()", ite);
        }
        
        getMethods(oneJarClassLoader);
    }

    public static void shutdown() throws PDPException {
        try {
            componentManagerShutdown.invoke(componentManagerImpl);
        } catch (IllegalAccessException iae) {
            throw new PDPException("Unable to invoke ComponentManagerImpl.shutdown()", iae);
        } catch (InvocationTargetException ite) {
            throw new PDPException("Unable to invoke ComponentManagerImpl.shutdown()", ite);
        }
    }
}
