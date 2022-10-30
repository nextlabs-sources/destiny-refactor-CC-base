/*
 * Created on Dec 1, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */

package com.bluejungle.destiny.agent.controlmanager.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.agent.commandengine.ICommandExecutor;
import com.bluejungle.destiny.agent.commandengine.tests.TestCommandExecutor;
import com.bluejungle.destiny.agent.profile.tests.ProfileReadTest;
import com.bluejungle.destiny.agent.profile.tests.ProfileWriteTest;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author hfriedland
 */
public class ControlManagerTestSuite {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ControlManagerTestSuite.suite());
    }

    public static Test suite() {
        //Initialize test CommandExecutor
        ComponentInfo<TestCommandExecutor> info = new ComponentInfo<TestCommandExecutor>(
        		ICommandExecutor.NAME, 
        		TestCommandExecutor.class, 
        		ICommandExecutor.class, 
        		LifestyleType.SINGLETON_TYPE);
        ICommandExecutor commandExecutor = ComponentManagerFactory.getComponentManager().getComponent(info);
        
        TestSuite suite = new TestSuite("Test for com.bluejungle.destiny.agent.controlmanager.test");
        //$JUnit-BEGIN$
        suite.addTestSuite(ProfileWriteTest.class);
        suite.addTestSuite(ProfileReadTest.class);
        //$JUnit-END$
        return suite;
    }
}
