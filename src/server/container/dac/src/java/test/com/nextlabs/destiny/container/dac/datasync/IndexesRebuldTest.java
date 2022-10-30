/*
 * Created on Jun 24, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import com.bluejungle.framework.comp.HashMapConfiguration;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/nextlabs/destiny/container/dac/datasync/IndexesRebuldTest.java#1 $
 */

public class IndexesRebuldTest extends BaseDatasyncTestCase{

    public IndexesRebuldTest() {
        super(IndexesRebuldTest.class.getName());
    }
    
    
    public void test() throws HibernateException{
        Session s = getActivityDataSource().getSession();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IDataSyncTask.DIALECT_CONFIG_PARAMETER, getDialect());
        config.setProperty(IDataSyncTask.TASK_UPDATE_PARAMETER, new IDataSyncTaskUpdate(){

            public void addFail(int size) throws IllegalStateException {
            }

            public void addSuccess(int size) throws IllegalStateException {
            }

            public boolean alive() {
                return true;
            }

            public long getUpdateInterval() {
                return Long.MAX_VALUE;
            }

            public void setPrefix(String prefix) {
            }

            public void setTotalSize(int size) throws IllegalStateException {
            }

            public void reset() {
                
            }
            
        });
        try {
            new IndexesRebuild().run(s, Long.MAX_VALUE, config);
        } finally {
            s.close();
        }
        
    }
    

}
