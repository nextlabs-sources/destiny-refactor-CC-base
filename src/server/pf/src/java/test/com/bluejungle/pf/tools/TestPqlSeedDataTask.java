package com.bluejungle.pf.tools;

/*
 * Created on Feb 15, 2007
 *
 * All sources, binaries and HTML pages (C) copyright 2007
 * by NextLabs Inc., San Mateo, CA.
 * Ownership remains with NextLabs Inc, All rights reserved worldwide.
 */

import java.io.FileOutputStream;
import java.util.Properties;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource;

/**
 * This test suite checks the PQL Seed Data Task
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/tools/TestPqlSeedDataTask.java#1 $
 */
public class TestPqlSeedDataTask extends PFTestWithDataSource {
    private static final Properties SHARED_SEED_DATA_PROPERTIES = new Properties();
    static {
        SHARED_SEED_DATA_PROPERTIES.put("ALL_USERS_GROUP_ID", "1");
        SHARED_SEED_DATA_PROPERTIES.put("SAMPLE_DATA_GROUP_ID", "5");
    }

    public void testClear() throws HibernateException {
        Session hs = ((IHibernateRepository)(ComponentManagerFactory.getComponentManager()).getComponent( DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName())).getSession();
        try {
            Transaction tx = hs.beginTransaction();
            hs.delete( "from DeploymentEntity" );
            hs.delete( "from DevelopmentEntity" );
            hs.delete( "from DeploymentRecord");
            tx.commit();
        } finally {
            hs.close();
        }
    }

    public void testSeedData() throws Exception {
        FileOutputStream outputStream = new FileOutputStream("shared_seed_data.properties");
        SHARED_SEED_DATA_PROPERTIES.store(outputStream, "test shared seed data properties");
        outputStream.close();
        
        PQLSeedDataTask task = new PQLSeedDataTask();
        Properties props = new Properties();
        props.setProperty(PQLSeedDataTask.SEED_PQL_DIR_PROPERTY,System.getProperty("src.root.dir")+"/seed_data");
        HashMapConfiguration conf = new HashMapConfiguration();
        conf.setProperty(ISeedDataTask.CONFIG_PROPS_CONFIG_PARAM, props);
        task.setConfiguration(conf);
        Log log = LogFactory.getLog(TestPqlSeedDataTask.class);
        task.setLog(log);
        task.setManager(ComponentManagerFactory.getComponentManager());
        task.execute();
    }

}
