/*
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc., 
 * Redwood City CA, Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 *
 * Author : Dominic Lam
 * Date   : 5/23/2006
 * Note   : Linux Kernel interface is different as we are using socket
 * 
 */

/*
 * @author ctlam
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/ipc/src/java/main/com/bluejungle/destiny/agent/ipc/ServerStub.java#1 $
 *
 */

package com.bluejungle.destiny.agent.ipc;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.IThreadPool;
import com.bluejungle.framework.threading.ThreadPool;
import com.bluejungle.framework.comp.IConfiguration;
import org.apache.commons.logging.Log;

public class ServerStub {//extends IPCStubBase {

    public static final int BJ_KERNELPEP = 31;
    static private ServerStub stub = null;

    private static IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);

    private IThreadPool threadPool = null;
    private Log log;
    private IConfiguration config = null;
    private String requestHandlerClassName;
    private int threadPoolSize = 3;    
    
    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    private long socketHandle = -1;

    // Making an array just to reuse the API from Window
    // This must has only one element  
    private final int[] dummySocketArray = new int[1];

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
    
    protected IOSWrapper getOSWrapper() {
        return ServerStub.osWrapper;
    }
    ///////[jzhang]

    public void init() {
    	//initialize Thread Pool
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IThreadPool.THREADPOOL_SIZE, new Integer(this.threadPoolSize));
        config.setProperty(IThreadPool.WORKER_CLASS_NAME, "com.bluejungle.destiny.agent.controlmanager.ServerStubRequestHandler");
        
        config.setProperty(IThreadPool.THREADPOOL_NAME, "Serverstub");
        config.setProperty(IIPCStub.STUB_CONFIG_PARAM, this);
        ComponentInfo<ThreadPool> info = new ComponentInfo<ThreadPool>(
        		"ServerStubRequestHandler_ThreadPool", 
        		ThreadPool.class, 
        		IThreadPool.class, 
        		LifestyleType.TRANSIENT_TYPE, config);
        this.threadPool = ComponentManagerFactory.getComponentManager().getComponent(info);
    }
    
    public static ServerStub getInstance() {
        // this is a singleton -- create if does not exist
        // otherwise return existing object
        if (stub == null) {
            stub = new ServerStub();
            stub.init();
        }
        return stub;
    }    
    
    public void doPolicyEvaluation(Object[] inputParams)
    {
       System.out.println("I am in doPolicyEvaluation@ServerStub!!!!" + inputParams[0]);
       final IThreadPool threadPool = getThreadPool();
       try
       {
           String index = (String)inputParams[0];  //always the first
           long socketHandle = Long.parseLong((String)inputParams[inputParams.length-1]); //always the last
           threadPool.doWork (new ServerStubRequestTaskImpl(getOSWrapper(), index, inputParams, socketHandle));
       } catch (Exception e) {
           getLog().error ("Error while process Policy Evaluation request.", e);    		
       }
    	
    }
    
    public void run() {
    	System.out.println("I am in ServerStub!!!!");
    }

    public void stop() {
        getOSWrapper().shutdownSocket(socketHandle);
    }

}
