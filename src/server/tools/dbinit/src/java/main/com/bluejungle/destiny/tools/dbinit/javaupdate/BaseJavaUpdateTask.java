package com.bluejungle.destiny.tools.dbinit.javaupdate;

/*
 * Created on Feb 06, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/javaupdate/BaseJavaUpdateTask.java#1 $:
 */

import java.sql.Connection;

import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.version.IVersion;

public abstract class BaseJavaUpdateTask implements IJavaUpdateTask {
    public abstract void execute(Connection connection, ConfigurationMod cm, IVersion fromVersion, IVersion toVersion) throws JavaUpdateException;

    private IConfiguration config;

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration()
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return config;
    }
}
