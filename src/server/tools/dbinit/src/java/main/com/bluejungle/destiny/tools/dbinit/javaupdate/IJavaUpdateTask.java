package com.bluejungle.destiny.tools.dbinit.javaupdate;

import java.sql.Connection;

import java.util.Properties;

import com.bluejungle.destiny.tools.dbinit.IDBinitUpdateTask;
import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.version.IVersion;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/javaupdate/IJavaUpdateTask.java#1 $
 */
public interface IJavaUpdateTask extends IConfigurable, IDBinitUpdateTask {
    PropertyKey<Properties> CONFIG_PROPS_CONFIG_PARAM = new PropertyKey<Properties>("configFileProps");
    PropertyKey<IHibernateRepository> HIBERNATE_DATA_SOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("hbs");

    public void execute(Connection connection, ConfigurationMod cm, IVersion fromVersion,
			IVersion toVersion) throws JavaUpdateException;
}
