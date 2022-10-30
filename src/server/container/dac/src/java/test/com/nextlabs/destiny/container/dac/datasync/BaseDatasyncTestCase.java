/*
 * Created on Jun 16, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.dialect.Dialect;

import com.bluejungle.destiny.container.dac.BaseDACComponentTestCase;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/nextlabs/destiny/container/dac/datasync/BaseDatasyncTestCase.java#1 $
 */

public abstract class BaseDatasyncTestCase extends BaseDACComponentTestCase {
    public BaseDatasyncTestCase(String testName) {
        super(testName);
    }

    protected Dialect getDialect() throws HibernateException {
        IHibernateRepository datasource = getActivityDataSource();
        
        assertNotNull(datasource);
        
        String dialectString = (String) datasource.getConfiguration().get("hibernate.dialect");
        assertNotNull(dialectString);
        Properties prop = new Properties();
        prop.put(Environment.DIALECT, dialectString);
        Dialect d = Dialect.getDialect(prop);
        assertNotNull(d);
        return d;
    }
}
