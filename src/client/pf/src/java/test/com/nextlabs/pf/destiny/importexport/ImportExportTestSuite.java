/*
 * Created on Sep 19, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.pf.destiny.importexport;

import com.nextlabs.pf.destiny.importexport.impl.ImporterTest;
import com.nextlabs.pf.destiny.importexport.impl.UIExporterTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/test/com/nextlabs/pf/destiny/importexport/ImportExportTestSuite.java#1 $
 */

public class ImportExportTestSuite {
	private static final Class[] testClasses = new Class[]{
		UIExporterTest.class,
		ImporterTest.class,
	};
	
    public static void main(String[] args) {
    	for(Class testClass : testClasses){
    		junit.swingui.TestRunner.run(testClass);
    	}
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for com.nextlabs.pf.destiny.importexport");
        for(Class testClass : testClasses){
        	suite.addTestSuite(testClass);
        }
        return suite;
    }
}