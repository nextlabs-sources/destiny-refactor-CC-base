/*
 * Created on May 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

/**
 * This interface contains the name of the properties exposed in the DBInit
 * configuration file
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/IConfigFileProperties.java#1 $
 */

public interface IConfigFileProperties {

    /**
     * Name of the library path property
     */
    String LIBRARY_PATH_PROPERTY = "LibraryPath";

    /**
     * Name of the name property
     */
    String NAME_PROPERTY = "Name";

    
    /**
     * those classes will be called before any schema changes during fresh installation
     */
    String PRE_INSTALL_TASK_PROPERTY_NAME = "preInstallTask";
    
    /**
     * Those classes will be called after the schema is changed during fresh installation
     */
    String INSTALL_TASK_PROPERTY_NAME = "installTask";
    
    
    
    /**
     * those classes will be called before any schema changes during upgrade
     */
    String PRE_UPDATE_TASK_PROPERTY_NAME = "preUpdateTask";
    
    /**
     * those classes will be called between pre,post schema changes during upgrade
     */
    String JAVA_UPDATE_TASK_PROPERTY_NAME = "javaUpdateTask";
    
    /**
     * those classes will be called after the schema is changed during upgrade
     */
    String UPDATA_TASK_PROPERTY_NAME = "updateTask";

    
    /**
     * Value of the separator character
     */
    String SEPARATOR = ";";
}