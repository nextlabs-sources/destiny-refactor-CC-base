/*
 * Created on Dec 17, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;

import net.sf.hibernate.cfg.Environment;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/DBInitPostgreTest.java#1 $
 */

public class DBInitPostgreTest extends DBInitTest {

	/**
	 * @see com.bluejungle.destiny.tools.dbinit.DBInitTestBase#getProperties()
	 */
	@Override
	protected Properties getProperties() {
		Properties props = new Properties();
		props.setProperty(Environment.URL, "jdbc:postgresql://192.168.64.130:5432/" + getDatabaseName());
		props.setProperty(Environment.DIALECT, net.sf.hibernate.dialect.PostgreSQLDialect.class.getName());
		props.setProperty(Environment.DRIVER, org.postgresql.Driver.class.getName());
		props.setProperty(Environment.USER,"admin");
		props.setProperty(Environment.PASS, "123blue!");
		return props;
	}
	
	@Test
	public void dummyTest(){
		assertNotNull(props);
	}

}
