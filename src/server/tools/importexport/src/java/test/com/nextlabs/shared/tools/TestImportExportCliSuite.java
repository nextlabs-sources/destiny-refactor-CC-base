package com.nextlabs.shared.tools;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * TODO description
 *
 * @author hchan
 * @date Jul 19, 2007
 */
@RunWith(Suite.class)
@SuiteClasses( { 
	EntityImportTest.class, 
	EntityExportTest.class} )
	
public class TestImportExportCliSuite {
	public static Test suite() {
		return new JUnit4TestAdapter(TestImportExportCliSuite.class);
	}
}
