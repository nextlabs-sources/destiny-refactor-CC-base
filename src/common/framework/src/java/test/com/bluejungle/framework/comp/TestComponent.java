/*
 * Created on Feb 8, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.comp;

import com.bluejungle.framework.comp.ComponentImplBase;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.ICompleteComponent;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author Sasha Vladimirov
 * 
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/comp/test/TestComponent.java#4 $
 */

public class TestComponent extends ComponentImplBase implements IHasComponentInfo<TestComponent>, ICompleteComponent {

    private boolean inited = false;
    private boolean started = false;
    private boolean stopped = false;
    private boolean disposed = false;

    public static int count = 0;

    public static final String NAME = TestComponent.class.getName();

    private static final String TEST_PROP_NAME = "testPropName";
    public static final String TEST_PROP_VALUE = "testPropValue";

    private static final ComponentInfo<TestComponent> COMP_INFO;

    static {
        HashMapConfiguration config = new HashMapConfiguration(1);
        config.setProperty(TEST_PROP_NAME, TEST_PROP_VALUE);

        COMP_INFO = new ComponentInfo<TestComponent>(
        		NAME, 
        		TestComponent.class, 
        		null, 
        		LifestyleType.SINGLETON_TYPE, 
        		config);
    }

    public TestComponent() {
        count++;
    }

    public ComponentInfo<TestComponent> getComponentInfo() {
        return COMP_INFO;
    }

    public void setConfiguration(IConfiguration config) {
        super.setConfiguration(config);
        log.debug("called setConfiguration");
    }

    public void init() {
        inited = true;
        log.debug("Called init");
    }

    public void dispose() {
        disposed = true;
        count--;
        log.debug("Called dispose");
    }

    public void start() {
        started = true;
        log.debug("Called start");
    }

    public void stop() {
        stopped = true;
        log.debug("Called stop");
    }

    public String toString() {
        return ("Test Component: " + super.toString());
    }

    public boolean isInited() {
        return inited;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public boolean isStopped() {
        return stopped;
    }

    public String getValue() {
        return (String) configuration.get(TEST_PROP_NAME);
    }
}