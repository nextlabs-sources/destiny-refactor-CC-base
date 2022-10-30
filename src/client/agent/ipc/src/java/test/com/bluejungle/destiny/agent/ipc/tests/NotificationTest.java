/*
 * Created on Jan 26, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.ipc.tests;

import java.util.ArrayList;
import java.util.Calendar;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.ipc.IIPCProxy;
import com.bluejungle.destiny.agent.ipc.IPCProxy;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class NotificationTest extends TestCase {

    private static final String NOTIFY_REQUEST_HANDLER_CLASS = "S-1-5-21-668023798-3031861066-1043980994-2624DestinyNotifyRequestHandler";
    private static final String SHOWNOTIFICATION = "ShowNotification";

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Constructor for NotificationTest.
     * 
     * @param name
     */
    public NotificationTest(String name) {
        super(name);
    }

    public void testNotification() throws InterruptedException {
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IIPCProxy.REQUEST_HANDLER_CLASS_NAME, NOTIFY_REQUEST_HANDLER_CLASS);
        ComponentInfo<IPCProxy> info = new ComponentInfo<IPCProxy>(
        		IPCProxy.class.getName(), 
        		IPCProxy.class, 
        		IIPCProxy.class, 
        		LifestyleType.TRANSIENT_TYPE, 
        		config);
        IIPCProxy proxy = (IIPCProxy) ComponentManagerFactory.getComponentManager().getComponent(info);
        
        while (true)
        {
	        ArrayList<String> inputArgs = new ArrayList<String>();
	        inputArgs.add(Calendar.getInstance().getTime().toString());
	        inputArgs.add("Delete");
	        inputArgs.add("c:\\foo.txt");
	        inputArgs.add("This is another test notification.");
	        ArrayList result = new ArrayList();
	        System.out.println("Sending Request...");
	        proxy.invoke(SHOWNOTIFICATION, inputArgs, result);

            Thread.sleep(2000);
	        
	        inputArgs = new ArrayList<String>();
	        inputArgs.add(Calendar.getInstance().getTime().toString());
	        inputArgs.add("Copy");
	        inputArgs.add("c:\\bar.doc");
	        inputArgs.add("This is notification 1 from policy 1.");
	        inputArgs.add("This is notification 2 from policy 2.");
	        inputArgs.add("This is notification 3 from policy 3.");
	        inputArgs.add("This is notification 4 from policy 4.");
	        result = new ArrayList();
	        System.out.println("Sending Request...");
	        proxy.invoke(SHOWNOTIFICATION, inputArgs, result);

            Thread.sleep(2000);
        }
    }

}
