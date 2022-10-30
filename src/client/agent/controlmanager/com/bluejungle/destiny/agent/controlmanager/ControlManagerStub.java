/*
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc., 
 * Redwood City CA, Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 *
 * Author : Dominic Lam
 * Date   : 5/23/2006
 * @author ctlam
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/ControlManagerStub.java#1 $
 *
 */

package com.bluejungle.destiny.agent.controlmanager;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.agent.controlmanager.IPDPJni;
import com.bluejungle.destiny.agent.controlmanager.PDPJni;
import com.bluejungle.destiny.agent.ipc.IIPCStub;
import com.bluejungle.destiny.agent.controlmanager.PDPRequestTaskImpl;
import com.bluejungle.destiny.agent.controlmanager.ServerStubRequestTaskImpl;
import com.bluejungle.destiny.agent.controlmanager.ServerStubRequestTaskImpl.IServerStubResponseHandler;
import com.bluejungle.destiny.agent.controlmanager.ServiceRequestTaskImpl;
import com.bluejungle.destiny.agent.controlmanager.ServiceRequestTaskImpl.IServiceRequestResponseHandler;
import com.bluejungle.framework.utils.CryptUtils;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.threading.ThreadPool;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.utils.CryptUtils;

public class ControlManagerStub {

    public static final int BJ_KERNELPEP = 31;
    static private ControlManagerStub stub = null;

    private static IPDPJni pdpJni = (IPDPJni) ComponentManagerFactory.getComponentManager().getComponent(PDPJni.class);
    private static ITrustedProcessManager trustedProcessManager = ComponentManagerFactory.getComponentManager().getComponent(TrustedProcessManager.COMP_INFO);

    private IThreadPool threadPool = null;
    private Log log;
    private IConfiguration config = null;
    private String requestHandlerClassName;
    
    private int socketHandle = -1;

    // Making an array just to reuse the API from Window
    // This must has only one element  
    //private final int[] dummySocketArray = new int[1];

    /**
     * 
     * @param passwordHash
     *            password entered by user
     * @param profilePasswordHash
     *            password from profile.
     * 
     * @return true if password matches.
     */
    private static boolean IsPasswordCorrect(byte[] passwordHash, byte[] profilePasswordHash) {
        boolean isPasswordCorrect = false;
        if (profilePasswordHash != null && passwordHash.length == profilePasswordHash.length) {
            isPasswordCorrect = true;
            for (int i = 0; i < passwordHash.length; i++) {
                if (passwordHash[i] != profilePasswordHash[i]) {
                    isPasswordCorrect = false;
                    break;
                }
            }
        }
        return isPasswordCorrect;
    }

    public void setConfiguration(IConfiguration config){
        this.config = config;
    }

    public IConfiguration getConfiguration(){
        return this.config;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }
    
    public void setLog(Log log) {
        this.log = log;
    }
    
    protected IThreadPool getThreadPool() {
        return this.threadPool;
    }
    
    protected IPDPJni getOSWrapper() {
        return ControlManagerStub.pdpJni;
    }

    ///////[jzhang]

    public void init() {
    	// initialize logging
        setLog(LogFactory.getLog(ControlManagerStub.class.getName()));
        
    	//initialize Thread Pool
        HashMapConfiguration config = new HashMapConfiguration();

        config.setProperty(IThreadPool.THREADPOOL_SIZE, getThreadPoolSize());
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, "com.bluejungle.destiny.agent.controlmanager.ServerStubRequestHandler");
        
        config.setProperty(IThreadPool.THREADPOOL_NAME, "Serverstub");
        config.setProperty(IIPCStub.STUB_CONFIG_PARAM, this);
        ComponentInfo<ThreadPool> info = new ComponentInfo<ThreadPool>(
        		"ServerStubRequestHandler_ThreadPool", 
        		ThreadPool.class, 
        		IThreadPool.class, 
        		LifestyleType.TRANSIENT_TYPE, 
        		config);
        this.threadPool = (IThreadPool) ComponentManagerFactory.getComponentManager().getComponent(info);
        }
    
    public synchronized static ControlManagerStub getInstance() {
        // this is a singleton -- create if does not exist
        // otherwise return existing object
        if (stub == null) {
            stub = new ControlManagerStub();
            stub.init();
        }
        return stub;
    }    

    private static int getThreadPoolSize() {
        Integer size = Integer.getInteger("nextlabs.evaluation.threadpoolsize");

        return (size == null) ? 10 : size;
    } 

    public void doPolicyEvaluation(Object[] inputParams)
    {
    	final IThreadPool threadPool = getThreadPool();
    	try
    	{
            String requestID = (String)inputParams[0];  //always the first
            long socketHandle = Long.parseLong((String)inputParams[1]); //always the second
            threadPool.doWork (new ServerStubRequestTaskImpl(getOSWrapper(),
                                                             requestID,
                                                             inputParams,
                                                             socketHandle,
                                                             new IServerStubResponseHandler() {
                                                                 public void respond(ServerStubRequestTaskImpl request, int resultCode, String[] obligations) {
                                                                     request.getOSWrapper().SendPolicyResponse(request.getHandle(),
                                                                                                               request.getRequestNumber(),
                                                                                                               resultCode,
                                                                                                               obligations);
                                                                 }
                                                             }
                ));
    	} catch (Exception e) {
            getLog().error ("Error while process Policy Evaluation request.", e);    		
    	}    	
    }

    public void doMultiPolicyEvaluation(Object[] multiInputParams) {
        final int numRequests = multiInputParams.length;

        final Object[] results = new Object[numRequests];
        final AtomicInteger numResponsesProcessed = new AtomicInteger(0);

        final IThreadPool threadPool = getThreadPool();
        try {
            for (int i = 0; i < numRequests; i++) {
                Object[] inputParams = (Object[])multiInputParams[i];

                String requestID = (String)inputParams[0];
                long socketHandle = Long.parseLong((String)inputParams[1]);

                final int myIndex = i;
                threadPool.doWork(new ServerStubRequestTaskImpl(getOSWrapper(),
                                                                requestID,
                                                                inputParams,
                                                                socketHandle,
                                                                new IServerStubResponseHandler() {
                                                                    public void respond(ServerStubRequestTaskImpl request, int resultCode, String[] obligations) {
                                                                        synchronized (results) {
                                                                            Object[] enforcement = { resultCode, obligations } ;
                                                                            results[myIndex] = (Object)enforcement;
                                                                            
                                                                            if (numResponsesProcessed.incrementAndGet() == numRequests) {
                                                                                request.getOSWrapper().SendPolicyMultiResponse(request.getHandle(),
                                                                                                                               request.getRequestNumber(),
                                                                                                                               results);
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                ));
            }
        } catch(Exception e) {
            getLog().error("Error while processing Policy Evaluation request", e);
        }
    }

    public Object[] doPolicyEvaluationSync(Object[] inputParams, long timeoutInMilliseconds) {
    	final IThreadPool threadPool = getThreadPool();
    	try
    	{
            String requestID = (String)inputParams[0];  //always the first
            long socketHandle = Long.parseLong((String)inputParams[1]); //always the second

            final Semaphore sem = new Semaphore(1);
            sem.acquireUninterruptibly();

            final ArrayList<Object> results = new ArrayList<Object>();

            threadPool.doWork (new ServerStubRequestTaskImpl(getOSWrapper(),
                                                             requestID,
                                                             inputParams,
                                                             socketHandle,
                                                             new IServerStubResponseHandler() {
                                                                 public void respond(ServerStubRequestTaskImpl request, int resultCode, String[] obligations) {
                                                                     results.add(Integer.toString(resultCode));
                                                                     for (String obligation : obligations) {
                                                                         results.add(obligation);
                                                                     }
                                                                     sem.release();
                                                                 }
                                                             }));

            getLog().debug("Waiting for response to query " + requestID);
            try {
                if (!sem.tryAcquire(timeoutInMilliseconds, TimeUnit.MILLISECONDS)) {
                    getLog().warn("Timeout happened when waiting for response to query " + requestID);
                    return null;
                }
            } catch (InterruptedException e) {
                getLog().warn("Exception happened when waiting for response to query " + requestID + ". " + e);
                return null;
            }
            getLog().debug("Got response to query " + requestID);
            return results.toArray(new Object[results.size()]);
        }
        catch(Exception e) {
            getLog().error("Error while processing policy evaluation request", e);
            return null;
        }

    }

    public void doPDPRequest(PDPRequestTaskImpl task) {
        final IThreadPool threadPool = getThreadPool();
       try {
            threadPool.doWork(task);
        } catch (Exception e) {
            getLog().error("Exception throws while evaluating ITask request", e);
        }
    }

    public void notifyUser(String userId, String time, String action, String enforcement, String resource, String message) {
        IControlManager myControlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);

        myControlManager.notifyUser(userId, time, action, enforcement, resource, message);
    }

    public void logDecision(String logEntity, String userDecision, String[] attributes) {
        IControlManager myControlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);

        myControlManager.logDecision(logEntity, userDecision, attributes);
    }

    // handle logon event 
    public void handleLogonEvent(String userSID) {
    	getLog().info("Logged on user SID: " + userSID);
        IControlManager myControlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);

        myControlManager.handleLogonEvent(userSID);
    }

    // handle logoff event 
    public void handleLogoffEvent(String userSID) {
    	getLog().info("Logged off user SID: " + userSID);
        IControlManager myControlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);

        myControlManager.handleLogoffEvent(userSID);
    }


    public void logAssistantData(String logIdentifier,
                                 String assistantName,
                                 String assistantOptions,
                                 String assistantDescription,
                                 String assistantUserActions,
                                 String[] ignoredAttributes) {
        IControlManager myControlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
        
        myControlManager.logAssistantData(logIdentifier, assistantName, assistantOptions, assistantDescription, assistantUserActions);
    }

    public void evaluateGenericFunctionCall(long handle, Object[] query) {
        final IThreadPool threadPool = getThreadPool();
        try
        {
            threadPool.doWork(new ServiceRequestTaskImpl(getOSWrapper(),
                                                         query,
                                                         handle,
                                                         new IServiceRequestResponseHandler() {
                                                             public void respond(ServiceRequestTaskImpl request, String reqId, Object[] args) {
                                                                 request.getOSWrapper().SendServiceResponse(request.getHandle(), reqId, args);
                                                             }
                                                         }));
        } catch(Exception e) {
            getLog().error ("Error while processing service request.", e);    		
        }
    }

    public Object[] evaluateGenericFunctionCallSync(Object[] query, long timeoutInMilliseconds) {
        final IThreadPool threadPool = getThreadPool();
        
        try {
            final Semaphore sem = new Semaphore(1);
            sem.acquireUninterruptibly();

            final ArrayList<Object> results = new ArrayList<Object>();

            threadPool.doWork(new ServiceRequestTaskImpl(getOSWrapper(),
                                                         query,
                                                         -1,
                                                         new IServiceRequestResponseHandler() {
                                                             public void respond(ServiceRequestTaskImpl request, String reqId, Object[] responseValues) {
                                                                 for (Object responseValue : responseValues) {
                                                                     results.add(responseValue);
                                                                 }
                                                                 sem.release();
                                                             }
                                                         }));
            try {
                if (!sem.tryAcquire(timeoutInMilliseconds, TimeUnit.MILLISECONDS)) {
                    getLog().warn("Timeout happened when waiting for response to query");
                    return null;
                }

            } catch (InterruptedException e) {
                getLog().warn("Exception happened when waiting for response to query");
                return null;
            }
            return results.toArray(new Object[results.size()]);
        } catch (Exception e) {
            getLog().error("Error while processing service request.", e);
            return null;
        }
    }

    public boolean makeTrusted(int processID, String password) {
        if (StopAgentVerify(password)) {
            getLog().info("Password (" + password + ") succeeded.  Marking process " + processID + " as trusted");
            trustedProcessManager.addTrustedProcess(processID);
            return true;
        } else {
            getLog().info("Password (" + password + ") failed.  Not marking process " + processID + " as trusted");
        }

        return false;
    }

    public void run() {
    	System.out.println("I am in ControlManagerStub!!!!");
    }

    public void stop() {
        threadPool.stop();
    }

    /**
     * @see com.bluejungle.destiny.agent.ipc.IRequestHandler
     * #StopAgentVerifyPasswd(java.lang.String)
     * Verify the provided password to stop the PDP agent.
     */
    public boolean StopAgentVerify(String passwd) {
	try {
	    boolean isPasswordCorrect = false;
	    // HACK Robert 11/06/07 for Bug#5895: 
        // this is temporary solution for stopping enforcer without password
        if (passwd.startsWith("CE")){ 
            passwd = passwd.substring(2);
            byte[] passwordHash = CryptUtils.digest(passwd, "SHA1", 0);
            IControlManager myControlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
            byte[] profilePasswordHash = myControlManager.getCommunicationProfile().getPasswordHash();
            isPasswordCorrect = IsPasswordCorrect(passwordHash, 
						        profilePasswordHash);
        } else {
            isPasswordCorrect = true;
        }
        return isPasswordCorrect;
	} catch (Exception e) {
	    System.out.println("StopAgentVerify error: "+ e);
	}
	    return false;
    }
}
