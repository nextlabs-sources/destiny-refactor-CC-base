/*
 * Created on Dec 1, 2004
 * All sources, binaries and HTML pages 
 * (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, 
 * Ownership remains with Blue Jungle Inc, 
 * All rights reserved worldwide. 
 */
package com.bluejungle.framework.threading;

import com.bluejungle.framework.threading.ITask;


/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/threading/TestTask.java#1 $:
 */

public class TestTask implements ITask {

    private String name;
    
    TestTask (String name){
        this.name = name;
    }
    
    /**
     * Returns the name.
     * @return the name.
     */
    public String getName() {
        return this.name;
    }
}
