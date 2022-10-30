/*
 * Created on Dec 18, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.datastore.hibernate.seed;

import java.util.Properties;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/seed/ISeedUpdateTask.java#1 $
 */

public interface ISeedUpdateTask extends IConfigurable {
    
    PropertyKey<IHibernateRepository> HIBERNATE_DATA_SOURCE_CONFIG_PARAM = ISeedDataTask.HIBERNATE_DATA_SOURCE_CONFIG_PARAM;

    PropertyKey<Properties> CONFIG_PROPS_CONFIG_PARAM = ISeedDataTask.CONFIG_PROPS_CONFIG_PARAM;
    
    String DIALECT_EXTENDED_CONFIG_PARAM = ISeedDataTask.DIALECT_EXTENDED_CONFIG_PARAM;
    
    //TODO combine this with IDBInitUpdateTask
    IVersion VERSION_1_6    = new VersionDefaultImpl(1, 6, 0, 0, 0);
    IVersion VERSION_2_0    = new VersionDefaultImpl(2, 0, 0, 0, 0);
    IVersion VERSION_2_5    = new VersionDefaultImpl(2, 5, 0, 0, 0);
    IVersion VERSION_3_0    = new VersionDefaultImpl(3, 0, 0, 0, 0);
    IVersion VERSION_3_1    = new VersionDefaultImpl(3, 1, 0, 0, 0);
    IVersion VERSION_3_5    = new VersionDefaultImpl(3, 5, 0, 0, 0);
    IVersion VERSION_3_5_1  = new VersionDefaultImpl(3, 5, 1, 0, 0);
    IVersion VERSION_3_6    = new VersionDefaultImpl(3, 6, 0, 0, 0);
    IVersion VERSION_4_0    = new VersionDefaultImpl(4, 0, 0, 0, 0);
    IVersion VERSION_4_5    = new VersionDefaultImpl(4, 5, 0, 0, 0);
    IVersion VERSION_4_6    = new VersionDefaultImpl(4, 6, 0, 0, 0);
    IVersion VERSION_5_0    = new VersionDefaultImpl(5, 0, 0, 0, 0);
    
    void execute(IVersion fromV, IVersion toV) throws SeedDataTaskException;
}
