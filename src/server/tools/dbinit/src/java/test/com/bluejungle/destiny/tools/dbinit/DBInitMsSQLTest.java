/*
 * Created on Dec 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import net.sf.hibernate.cfg.Environment;

import org.junit.Test;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/DBInitMsSQLTest.java#1 $
 */

public class DBInitMsSQLTest extends DBInitTest {

	/**
	 * @see com.bluejungle.destiny.tools.dbinit.DBInitTestBase#getProperties()
	 */
	@Override
	protected Properties getProperties() {
		Properties props = new Properties();
		props.setProperty(Environment.URL, "jdbc:sqlserver://192.168.64.134:1433;DatabaseName="+ getDatabaseName());
		props.setProperty(Environment.DIALECT, com.bluejungle.framework.datastore.hibernate.dialect.SqlServer2000Dialect.class.getName());
		props.setProperty(Environment.DRIVER, com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName());
		props.setProperty(Environment.USER,"admin");
		props.setProperty(Environment.PASS, "123blue!");
		return props;
	}
	
	@Test
	public void dummyTest(){
		assertNotNull(props);
	}

	@Override
	protected String getDatabaseName() {
		return "profiling";
	}
	
	
}
