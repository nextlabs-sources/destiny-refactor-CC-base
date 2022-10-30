/*
 * Created on May 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

import static com.bluejungle.destiny.tools.dbinit.DBInitOptionDescriptorEnum.*;

import java.io.File;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.version.IVersion;

/**
 * This interface represents a seed request. The seed request takes all the
 * parameters from the caller and can be executed.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/IDBInit.java#1 $
 */

public interface IDBInit {

//    /**
//     * Config parameter to set the name of the configuration file
//     */
//    PropertyKey<File> CONFIG_FILE_CONFIG_PARAM = new PropertyKey<File>(CONFIG_OPTION_ID.getName());

	PropertyKey<File> SCHEMA_FILE_CONFIG_PARAM = new PropertyKey<File>(SCHEMA_OPTION_ID.getName());
    
//    PropertyKey<String> CONNECTION_CONFIG_PARAM = new PropertyKey<String>(CONNECTION_CONFIG_DATA_OPTION_ID.getName());
    
//    /**
//     * Config parameter to set the path to the DO library
//     */
//    PropertyKey<String> LIBRARY_PATH_CONFIG_PARAM = new PropertyKey<String>(LIBRARY_PATH_OPTION_ID.getName());

    PropertyKey<IVersion> FROM_VERSION_PARAM = new PropertyKey<IVersion>(FROM_VERSION_OID.getName());
    
    PropertyKey<IVersion> TO_VERSION_PARAM = new PropertyKey<IVersion>(TO_VERSION_OID.getName());
    
    /**
     * Executes the task
     * 
     * @throws DBInitException
     *             when the execution fails
     */
    void execute(DBInitType action) throws DBInitException;
    
    
}
