/*
 * Created on Nov 13, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import com.bluejungle.destiny.tools.enrollment.EnrollmentMgr;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filter/SelectiveFilterConfiguration.java#1 $
 */

public class SelectiveFilterConfiguration {

    public static SelectiveFilterConfiguration SINGLETON;
    static {
        String enrollToolHome = System.getProperty(EnrollmentMgr.ENROLLMENT_TOOL_HOME);        
        String configFile = (new File(enrollToolHome + "/selectivefilter.properties")).getPath();
        Properties filterProperties = new Properties();
        try {
            filterProperties.load(new FileInputStream(new File(configFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SINGLETON = new SelectiveFilterConfiguration(filterProperties);
    }

    private static final String GROUP_REFERENCE_ATTRIBUTE = "group.reference.attribute";
    private static final String USER_REFERENCE_ATTRIBUTE = "user.reference.attribute";
    private static final String HOST_REFERENCE_ATTRIBUTE = "host.reference.attribute";
    private static final String HIERARCHY_RETAINER_FILTER = "hierarchy.retainer.filter";
    private static final String ALL_VALID_HOSTS_FILTER = "all.valid.hosts.filter";
    private static final String ALL_VALID_USERS_FILTER = "all.valid.users.filter";
    private static final String ALL_VALID_GROUPS_FILTER = "all.valid.groups.filter";
    private static final String USERS_COLUMN_HEADER = "users.column.header";
    private static final String HOSTS_COLUMN_HEADER = "hosts.column.header";
    private static final String GROUPS_COLUMN_HEADER = "groups.column.header";
    private static final String CELL_DELIMITER = "cell.delimiter";

    /*
     * Private members:
     */
    private Properties properties;

    /**
     * Constructor
     *  
     */
    protected SelectiveFilterConfiguration(Properties properties) {
        super();
        this.properties = properties;
    }

    public String getGroupReferenceAttribute() {
        return this.properties.getProperty(GROUP_REFERENCE_ATTRIBUTE);
    }

    public String getUserReferenceAttribute() {
        return this.properties.getProperty(USER_REFERENCE_ATTRIBUTE);
    }

    public String getHostReferenceAttribute() {
        return this.properties.getProperty(HOST_REFERENCE_ATTRIBUTE);
    }

    public String getHierarchyRetainerFilter() {
        return this.properties.getProperty(HIERARCHY_RETAINER_FILTER);
    }

    public String getAllValidHostsFilter() {
        return this.properties.getProperty(ALL_VALID_HOSTS_FILTER);
    }

    public String getAllValidUsersFilter() {
        return this.properties.getProperty(ALL_VALID_USERS_FILTER);
    }

    public String getAllValidGroupsFilter() {
        return this.properties.getProperty(ALL_VALID_GROUPS_FILTER);
    }
    
    public String getUsersColumnHeader() {
        return this.properties.getProperty(USERS_COLUMN_HEADER);
    }

    public String getHostsColumnHeader() {
        return this.properties.getProperty(HOSTS_COLUMN_HEADER);
    }

    public String getGroupsColumnHeader() {
        return this.properties.getProperty(GROUPS_COLUMN_HEADER);
    }
    
    public String getCellDelimiter() {
        return this.properties.getProperty(CELL_DELIMITER);
    }
}
