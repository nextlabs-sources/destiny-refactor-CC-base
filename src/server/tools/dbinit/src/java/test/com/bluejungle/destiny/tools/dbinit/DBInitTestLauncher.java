/*
 * Created on May 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit;

import com.bluejungle.destiny.tools.dbinit.DBInit;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.impl.InteractiveQuestion;

import static com.bluejungle.destiny.tools.dbinit.DBInitOptionDescriptorEnum.*;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/test/com/bluejungle/destiny/tools/dbinit/DBInitTestLauncher.java#1 $
 */

public class DBInitTestLauncher{
	private static final String SCHEMA_OUTPUT_FOLDER = "c:\\temp\\";

	private static enum Database{
		ACTIVITY,
		DICTIONARY,
		MGMT,
		PF,
		;
		
		String getLibraryPath(String root){
			return root + this.name().toLowerCase() + "\\";
//			switch (this) {
//			case ACTIVITY:
//				return LIBRARY_PATH + "activity\\";
//			case DICTIONARY:
//				return LIBRARY_PATH + "dictionary\\";
//			case MGMT:
//				return LIBRARY_PATH + "mgmt\\";
//			case PF:
//				return LIBRARY_PATH + "pf\\";
//			default:
//				throw new IllegalArgumentException();
//			}
		}
		
		String getConfigDir(String root){
//			return getLibraryPath(root) + this.name().toLowerCase() + ".cfg";
			switch (this) {
			case ACTIVITY:
				return getLibraryPath(root) + "activity.cfg";
			case DICTIONARY:
				return getLibraryPath(root) + "dictionary.cfg";
			case MGMT:
				return getLibraryPath(root) + "mgmt_template.cfg";
			case PF:
				return getLibraryPath(root) + "pf.cfg";
			default:
				throw new IllegalArgumentException();
			}
		}
		
		String getSchemaFileName(String suffix){
			return SCHEMA_OUTPUT_FOLDER + this.name().toLowerCase() + "_" + suffix + ".txt";
		}
	}
	
	public static void main(String[] args) throws Exception {
		InteractiveQuestion.InteractiveMCItems<Database> databaseItems = 
            new InteractiveQuestion.InteractiveMCItems<Database>();
		for(Database d : Database.values()){
		    databaseItems.add(d.name().substring(0,1).toLowerCase(), d.name(), d);
		}
		final Database db = InteractiveQuestion.multiChoice(databaseItems);
		
		String buildRoot = System.getProperty("build.root.dir");
		if (buildRoot == null) {
		    buildRoot = InteractiveQuestion.prompt("Where is your build.root.dir?");
        }
		final String libraryPath = buildRoot + "\\tools\\dbInit\\";
	    final String connectinPath = buildRoot + "\\run\\";
	    
	    InteractiveQuestion.InteractiveMCItems<OptionId<Boolean>> items = 
	        new InteractiveQuestion.InteractiveMCItems<OptionId<Boolean>>();
	    final OptionId[] availableActions = new OptionId[]{
	            INSTALL_OID,
	            UPGRADE_OID,
	            CREATE_SCHEMA_OID,
	            DROP_CREATE_SCHEMA_OID,
	            UPDATE_SCHEMA_OID,
	            PROCESS_SQL_FILE_OID,
	    };
	    
	    for (int i = 0; i < availableActions.length; i++) {
            items.add(Integer.toString(i + 1), availableActions[i].getName(), availableActions[i]);
        }
	    final OptionId action = InteractiveQuestion.multiChoice(items);
		
    	final String fromV = "5.0.0";
    	final String toV = "5.5.0";
    	
    	System.out.println("Please wait");
    	if(action == INSTALL_OID){
    		args = new String[] { "-" + action,
    				"-" + CONFIG_OID, db.getConfigDir(libraryPath),
    				"-libraryPath", db.getLibraryPath(libraryPath),
    				"-connection", connectinPath, };
    	}else if (action == UPGRADE_OID) {
    		args=new String[]{"-" +  action,
    				"-" + CONFIG_OID, db.getConfigDir(libraryPath), 
    				"-fromV", fromV,
    				"-toV", toV,
    				"-libraryPath",db.getLibraryPath(libraryPath),
    				"-connection", connectinPath, };
    	}else if (action == UPDATE_SCHEMA_OID) {
    	    args=new String[]{"-" +  action,
                    "-" + CONFIG_OID, db.getConfigDir(libraryPath), 
                    "-libraryPath",db.getLibraryPath(libraryPath),
                    "-connection", connectinPath,
                    "-schema", db.getSchemaFileName(action.getName()),
    	    };
    	} else {
	    	args=new String[]{"-" +  action,
					"-" + CONFIG_OID, db.getConfigDir(libraryPath), 
					"-schema", db.getSchemaFileName(action.getName()),
					"-libraryPath", db.getLibraryPath(libraryPath),
					"-connection", connectinPath, };
    	}
    	
    	DBInit.main(args);
//		PfJavaUpdateTask update = new PfJavaUpdateTask();
//		update.execute(createDefaultConnection(), null, IJavaUpdateTask.VERSION_2_0, IJavaUpdateTask.VERSION_2_5);
    }
	
//	private static Connection createDefaultConnection() throws SQLException, HibernateException{
//		Properties props = new Properties();
//		final String url = "jdbc:postgresql://192.168.64.130:5432/pf";
//		props.setProperty(Environment.URL, url);
//		props.setProperty(Environment.DIALECT, "net.sf.hibernate.dialect.PostgreSQLDialect");
//		props.setProperty(Environment.USER,"root");
//		props.setProperty(Environment.PASS, "123blue!");
//		props.setProperty(Environment.DRIVER, "org.postgresql.Driver");
//
//		Connection connection = ConnectionProviderFactory.newConnectionProvider(props).getConnection();
//		return connection;
//	}
}
