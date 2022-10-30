package com.bluejungle.destiny.tools.dbinit;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.tools.dbinit.impl.DBInitImpl;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;

/**
 * The front end of DBInit
 *
 * @author hchan
 * @date Jun 28, 2007
 */
public class DBInit extends ConsoleApplicationBase{
    private static final Log LOG = LogFactory.getLog(DBInit.class);

    /**
     * Return status for the tool
     */
    private static final int RETURN_STATUS_OK = 0;
    private static final int RETURN_STATUS_ERR_PARAMS = -2;
    private static final int RETURN_STATUS_ERR_EXEC = -1;

    private final IConsoleApplicationDescriptor options;
    private int returnStatus;

    public DBInit() throws InvalidOptionDescriptorException{
        options = new DBInitOptionDescriptorEnum();
    }

    public static void main(String[] args) {
        try {
            DBInit dbInitCli = new DBInit();
            dbInitCli.parseAndExecute(args);
            System.exit(dbInitCli.getReturnStatus());
        } catch (Exception e) {
            printException(e);
            System.exit(RETURN_STATUS_ERR_PARAMS);
        }
    }

    private int getReturnStatus(){
        return returnStatus;
    }


    protected void execute(ICommandLine commandLine) {
        try {
            DBInitType action = null;
            for(OptionId id : OID_TO_ENUM_MAP.keySet()){
                if (commandLine.isOptionExist(id)) {
                    action = OID_TO_ENUM_MAP.get(id);
                    break;
                }
            }
            if(action == null){
                throw new DBInitException("can't find action type");
            }
            
            HashMapConfiguration dbInitConfig = new HashMapConfiguration();
            
            File schemaFile = getValue(commandLine, DBInitOptionDescriptorEnum.SCHEMA_OPTION_ID);
            dbInitConfig.setProperty(IDBInit.SCHEMA_FILE_CONFIG_PARAM, schemaFile);
            
            if(action == DBInitType.UPGRADE){
                String value;
                value = getValue(commandLine, DBInitOptionDescriptorEnum.FROM_VERSION_OID);
                IVersion fromVersion = VersionDefaultImpl.getValue(value);
                dbInitConfig.setProperty(IDBInit.FROM_VERSION_PARAM, fromVersion);
                

                value = getValue(commandLine, DBInitOptionDescriptorEnum.TO_VERSION_OID);
                IVersion toVersion = VersionDefaultImpl.getValue(value);
                dbInitConfig.setProperty(IDBInit.TO_VERSION_PARAM, toVersion);

                if(fromVersion.compareTo(toVersion) >= 0){
                    throw new DBInitException("You can't upgrade to older version.");
                }
            }
            
            File serverHome = getValue(commandLine, DBInitOptionDescriptorEnum.SERVER_INSTALL_HOME_OID);
            File configFile = getValue(commandLine, DBInitOptionDescriptorEnum.CONFIG_OID);
            String libraryStr = getValue(commandLine, DBInitOptionDescriptorEnum.LIBRARY_PATH_OID);
            boolean isQuite = getValue(commandLine, DBInitOptionDescriptorEnum.QUIET_OID);
            
            IDBInit dbinitBackEnd = new DBInitImpl(serverHome, configFile, libraryStr, dbInitConfig, isQuite);
            
            dbinitBackEnd.execute(action);
            returnStatus = RETURN_STATUS_OK;
        } catch (Exception e) {
            LOG.error("Dbinit failed.", e);
            returnStatus = RETURN_STATUS_ERR_EXEC;
//            printException(e);
        }
    }



    @Override
    protected void printUsage() {
        super.printUsage();
        returnStatus = RETURN_STATUS_OK;
    }

    private static final Map<OptionId<Boolean>, DBInitType> OID_TO_ENUM_MAP;
    static{
        OID_TO_ENUM_MAP = new HashMap<OptionId<Boolean>, DBInitType>();
        OID_TO_ENUM_MAP.put(DBInitOptionDescriptorEnum.INSTALL_OID,             DBInitType.INSTALL);
        OID_TO_ENUM_MAP.put(DBInitOptionDescriptorEnum.UPGRADE_OID,             DBInitType.UPGRADE);
        OID_TO_ENUM_MAP.put(DBInitOptionDescriptorEnum.CREATE_SCHEMA_OID,       DBInitType.CREATE_SCHEMA);
        OID_TO_ENUM_MAP.put(DBInitOptionDescriptorEnum.DROP_CREATE_SCHEMA_OID,  DBInitType.DROP_CREATE_SCHEMA);
        OID_TO_ENUM_MAP.put(DBInitOptionDescriptorEnum.UPDATE_SCHEMA_OID,       DBInitType.UPDATE_SCHEMA);
        OID_TO_ENUM_MAP.put(DBInitOptionDescriptorEnum.PROCESS_SQL_FILE_OID,    DBInitType.PROCESS_SQL_FROM_FILE);
    }
    
    protected IConsoleApplicationDescriptor getDescriptor() {
        return options;
    }
}
