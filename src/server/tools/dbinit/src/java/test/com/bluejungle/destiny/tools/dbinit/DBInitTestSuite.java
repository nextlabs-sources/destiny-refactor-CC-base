/*
 * Created on Dec 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/DBInitTestSuite.java#1 $
 */


@RunWith(Suite.class)
@SuiteClasses( { 
	DBInitPostgreTest.class,
	DBInitOracleTest.class,
//	DBInitMsSQLTest.class,
	} )
	
public class DBInitTestSuite {
	public static Test suite() {
		JUnit4TestAdapter test = new JUnit4TestAdapter(DBInitTestSuite.class);
		return test;
	}
}
