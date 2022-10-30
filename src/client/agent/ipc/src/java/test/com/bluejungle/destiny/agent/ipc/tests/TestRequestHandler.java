// All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
//Redwood City CA, Ownership remains with Blue Jungle Inc,
//All rights reserved worldwide.

package com.bluejungle.destiny.agent.ipc.tests;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.bluejungle.destiny.agent.ipc.RequestHandlerBase;

/**
 * 
 * Test Request Handler class
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 *  
 */
public class TestRequestHandler extends RequestHandlerBase {

    /**
     * Asserts if method name or parameters are not as expected. Adds 2
     * parameters to resultParamArray
     * 
     * @see com.bluejungle.destiny.agent.ipc.IRequestHandler#invoke(java.lang.String,
     *      java.util.ArrayList, java.util.ArrayList)
     */
    public boolean invoke(String methodName, ArrayList paramArray, ArrayList resultParamArray) {

        System.out.print("Request received, Method name: ");
        System.out.println(methodName);

        TestCase.assertEquals("Method name is incorrect", methodName, "method1");
//        TestCase.assertEquals("Param count is incorrect", paramArray.size(), 2);
        TestCase.assertEquals("Param1 is incorrect", (String) paramArray.get(0), "param1");
        TestCase.assertEquals("Param2 is incorrect", (String) paramArray.get(1), "param2");

        resultParamArray.add("retval1");
        resultParamArray.add("retval2");

        return true;
    }

}