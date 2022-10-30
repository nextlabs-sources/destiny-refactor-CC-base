/*
 * Created on Apr 7, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.seedtasks;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.destiny.tools.dbinit.javaupdate.IJavaUpdateTask;
import com.bluejungle.destiny.tools.dbinit.javaupdate.JavaUpdateException;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedUpdateTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.datastore.hibernate.seed.seedtasks.SeedDataTaskBase;
import com.bluejungle.version.IVersion;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/seedtasks/DatabaseViewDropper.java#1 $
 */

public class DatabaseViewDropper extends SeedDataTaskBase implements ISeedUpdateTask, IJavaUpdateTask {
    private static final String[] VIEW_NAMES = new String[]{
        "policy_log_v1",
        "APP_USER_VIEW",
        "policy_custom_attribute_v1",
        "policy_obligation_log_v1",
        "tracking_log_v1",
        "tracking_custom_attribute_v1",
        "policy_log_v2",
        "policy_custom_attribute_v2",
        "policy_attr_mapping_v2"
    };

    @Override
    public void execute(Connection connection, ConfigurationMod cm, IVersion fromVersion,
			IVersion toVersion) throws JavaUpdateException {
        try {
            execute();
        } catch (SeedDataTaskException e) {
            throw new JavaUpdateException(e);
        }
    }

    @Override
    public void execute(IVersion fromV, IVersion toV) throws SeedDataTaskException {
        execute();
    }

    @Override
    public void execute() throws SeedDataTaskException {
        final DialectExtended dialectX = (DialectExtended)getConfiguration().get(ISeedDataTask.DIALECT_EXTENDED_CONFIG_PARAM);
        new MicroTask() {
            @Override
            public void run(Connection connection) throws SeedDataTaskException, SQLException,
                    HibernateException {
                List<String> sqls = new LinkedList<String>();
                DatabaseMetaData meta = connection.getMetaData();
                for (String viewName : VIEW_NAMES) {
                    viewName = DatabaseHelper.matchToDbStoreCase(viewName);
                    if (DatabaseHelper.isViewExist(meta, dialectX, viewName)) {
                        sqls.add(dialectX.dropView(viewName));
                    }
                }
                DatabaseHelper.processSqlStatements(connection, sqls);
            }
        }.run();
    }
}
