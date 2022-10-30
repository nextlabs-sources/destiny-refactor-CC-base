/*
 * Created on Jul 7, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.seedtasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod.Action;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository.DbType;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedUpdateTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.datastore.hibernate.seed.seedtasks.SeedDataTaskBase;
import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.hibernate.HibernateException;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/seedtasks/ActivityCustomIndexSeedTask.java#1 $
 */

public class ActivityCustomIndexSeedTask extends SeedDataTaskBase implements ISeedUpdateTask{
    private static final Log LOG = LogFactory.getLog(ActivityCustomIndexSeedTask.class);
    
    // this is from ALPO
    private static final String MSSQL_FINE_TUNE_SQL_PATH = "/com/bluejungle/destiny/tools/dbinit/seedtasks/mssql_finetune.sql";
    private static final String SQL_FILE_SQL_PATH = "/com/bluejungle/destiny/tools/dbinit/seedtasks/";
    
    // http://msdn.microsoft.com/en-us/library/ms188783.aspx
    private static final Pattern MSSQL_CREATE_INDEX_PATTERN = Pattern.compile(
            "^CREATE\\s+(?:UNIQUE\\s+)?(?:(?:CLUSTERED)|(?:NONCLUSTERED)\\s+)?INDEX\\s+(\\S+)\\s+ON\\s+(\\S+)\\s*\\(", 
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    // http://msdn.microsoft.com/en-us/library/ms188038%28v=SQL.100%29.aspx
    private static final Pattern MSSQL_CREATE_STATISTICS_PATTERN = Pattern.compile(
            "^CREATE\\s+STATISTICS\\s+(\\S+)\\s+ON\\s*(\\S+)\\s*\\(", 
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
    //TODO get this from SharedLib
    private static final String REPORT_PA_TABLE = "REPORT_POLICY_ACTIVITY_LOG";
    
    private DialectExtended dialectX;
	private IVersion fromV;
	private IVersion toV;
	private Action action;
    
    @Override
    public void execute(IVersion fromV, IVersion toV) throws SeedDataTaskException {
    	this.action = Action.UPDATE_SCHEMA;
		this.fromV = fromV;
		this.toV = toV;
        execute();
    }
    
    @Override
	public void execute() throws SeedDataTaskException {
        dialectX = (DialectExtended) getConfiguration().get(
                ISeedDataTask.DIALECT_EXTENDED_CONFIG_PARAM);
        
        new MicroTask() {
            @Override
            public void run(Connection connection) throws SeedDataTaskException, SQLException,
                    HibernateException {
                createIndex(connection, REPORT_PA_TABLE, 
                        "from_resource_name", "time", "log_level", "policy_decision");
                
                createIndex(connection, REPORT_PA_TABLE, 
                        "time", "log_level", "policy_decision", "user_name");
                
                if(getHibernateDataSource().getDatabaseType() == DbType.MS_SQL){
                    createMSSQLspecific(connection);
                }
                
                insertInitalData(connection, getHibernateDataSource().getDatabaseType());
                
                schemaUpdateFor8_0Changes(connection, getHibernateDataSource().getDatabaseType());
                
                adConfigMigrateForUpdateFromOPL(connection, getHibernateDataSource().getDatabaseType());
                
                schemaUpdateFor8_0_4Changes(connection, getHibernateDataSource().getDatabaseType());
				
				schemaUpdateFor8_0_6Changes(connection, getHibernateDataSource().getDatabaseType());
            }

        }.run();
    }

    /**
     * generate index name from <code>tableName</code>, and hashCode
     *  tableName maximum takes 15 character,
     *  hashCode maximum take 10 character, positive number only
     *  and fixed suffix "Index"
     * 
     * If the index name is already exist, the new index won't be created
     *  
     * @param tableName
     * @param columns the order is very important
     * @throws SQLException 
     */
    private void createIndex(
            Connection connection, 
            String tableName, 
            String... columns
    ) throws SQLException {
        if(columns == null){
            throw new NullPointerException("columns");
        }
        
        tableName = DatabaseHelper.matchToDbStoreCase(tableName);
        final int prime = 31;
        int hashCode = 1;
        for(String column : columns){
            hashCode = prime * hashCode + column.hashCode();
        }
        String indexName = generateIndexName(tableName, hashCode);
        
        if (!DatabaseHelper.isIndexExists(connection.getMetaData(), dialectX, tableName, indexName)) {
            DatabaseHelper.createIndex(connection, dialectX, indexName, tableName, columns);
        }
    }
    
    private String generateIndexName(String tableName, int hashCode) {
        String indexName = tableName.length() > 15 
                ? tableName.substring(0, 15) 
                : tableName;
        
        indexName += Math.abs(hashCode);
        indexName += "Index";
        
        return DatabaseHelper.matchToDbStoreCase(indexName);
    }
    
    private String[] readSqlFile(String resource) throws SeedDataTaskException{
        InputStream is = DatabaseViewCreator.class.getResourceAsStream(resource);
        StringWriter writer = new StringWriter();
        int c;
        try {
            while ((c = is.read()) != -1) {
                writer.write(c);
            }
        } catch (IOException e) {
            throw new SeedDataTaskException(e);
        }
        String content = writer.toString();
        String[] sqls = content.split(";");
        for (int i = 0; i < sqls.length; i++) {
            sqls[i] = sqls[i].trim();
        }
        return sqls;
    }
    
    private String noQuoteName(String tableName) {
        assert tableName != null;
        
        //maybe I should use dialectX.openQuote() and dialectX.closeQuote()
        if (tableName.startsWith("[") && tableName.endsWith("]")) {
            return tableName.substring(1, tableName.length() -1);
        }
        return tableName;
    }
    
    private void schemaUpdateFor8_0Changes(Connection connection, DbType dbType) {
		IVersion v802 = new VersionDefaultImpl(8, 0, 2, 0, 0);
		
		LOG.info("Schema update for version 8.0 --> started [ DB Type : " + dbType + ", version : "+ fromV + " ]");
		
		if (action == null || (v802.compareTo(fromV) > 0)) {
			List<String> sqlStatements = new ArrayList<String>();
			if (dbType == DbType.ORACLE) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "oracle_schema_update_v8.sql");
			} else if (dbType == DbType.MS_SQL) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "mssql_schema_update_v8.sql");
			} else if (dbType == DbType.POSTGRESQL) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "postgres_schema_update_v8.sql");
			}

			for (String sqlStatement : sqlStatements) {
				LOG.info("Schema update SQL  --> " + sqlStatement);
				try {
					DatabaseHelper.processSqlStatement(connection, sqlStatement);
					connection.commit();
				} catch (Exception e) {
					LOG.warn("Exception while executing the above sql" + e.getMessage());
				}
			}
		}
		
		LOG.info("Schema update for version 8.0 completed");

	}
    
    private void schemaUpdateFor8_0_4Changes(Connection connection, DbType dbType) {
    	IVersion v804 = new VersionDefaultImpl(8, 0, 4, 0, 0);
		if (action == null || (action == Action.UPDATE_SCHEMA && v804.compareTo(fromV) > 0)) {
			LOG.info("Schema update for version 8.0.4 --> update from [ DB Type : " + dbType + ", version : "+ fromV + " ]");
			List<String> sqlStatements = new ArrayList<String>();
			if (dbType == DbType.ORACLE) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "oracle_schema_update_v8_0_4.sql");
			} else if (dbType == DbType.MS_SQL) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "mssql_schema_update_v8_0_4.sql");
			} else if (dbType == DbType.POSTGRESQL) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "postgres_schema_update_v8_0_4.sql");
			}

			for (String sqlStatement : sqlStatements) {
				LOG.info("Schema update SQL  --> " + sqlStatement);
				try {
					DatabaseHelper.processSqlStatement(connection, sqlStatement);
					connection.commit();
				} catch (Exception e) {
					LOG.warn("Exception while executing the above sql" + e.getMessage());
				}
			}
		}
		
		LOG.info("Schema update for version 8.0.4 completed");

	}
	
	 private void schemaUpdateFor8_0_6Changes(Connection connection, DbType dbType) {
    	IVersion v806 = new VersionDefaultImpl(8, 0, 6, 0, 0);
		if (action == null || (action == Action.UPDATE_SCHEMA && v806.compareTo(fromV) > 0)) {
			LOG.info("Schema update for version 8.0.6 --> update from [ DB Type : " + dbType + ", version : "+ fromV + " ]");
			List<String> sqlStatements = new ArrayList<String>();
			if (dbType == DbType.ORACLE) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "oracle_schema_update_v8_0_6.sql");
			} else if (dbType == DbType.MS_SQL) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "mssql_schema_update_v8_0_6.sql");
			} else if (dbType == DbType.POSTGRESQL) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "postgres_schema_update_v8_0_6.sql");
			}

			for (String sqlStatement : sqlStatements) {
				LOG.info("Schema update SQL  --> " + sqlStatement);
				try {
					DatabaseHelper.processSqlStatement(connection, sqlStatement);
					connection.commit();
				} catch (Exception e) {
					LOG.warn("Exception while executing the above sql" + e.getMessage());
				}
			}
		}
		
		LOG.info("Schema update for version 8.0.6 completed");

	}

	/** Migrate AD configuration from config.xml as in OPL, to database, as in OPN
	 * @param connection
	 * @param databaseType
	 */
	private void adConfigMigrateForUpdateFromOPL(Connection connection, DbType databaseType) {
		IVersion v803 = new VersionDefaultImpl(8, 0, 3, 0, 0);
		if(action == null || (action == Action.UPDATE_SCHEMA && v803.compareTo(fromV) > 0)) {
			LOG.info("migrate AD config from xml to database for updating from OPL started");
			try {
				File configFile = new File(System.getProperty("cc.config.file"));
				if(!configFile.exists() || !configFile.isFile()) {
					LOG.error("config.xml not found or is not file. AD configuration migration completed");
					return;
				}
				SAXReader reader = new SAXReader();
				Document document = reader.read(configFile);
				Node propertiesNode = document.selectSingleNode( "/*[name()='DestinyConfiguration']/*[name()='ApplicationUserConfiguration']/*[name()='ExternalDomainConfiguration']/*[name()='UserAccessConfiguration']/*[name()='Properties']" );
				
				if(propertiesNode == null) {
					LOG.info("AD configuration not found. AD configuration migration completed");
					return;
				}
				
				@SuppressWarnings("rawtypes")
				Iterator iterator = ((Element)propertiesNode).elementIterator();
				Map<String, String> adConfig = new HashMap<String, String>();
				while(iterator.hasNext()) {
					Element ele = (Element)iterator.next();
					String key = null, value = null;
					for ( int i = 0, size = ele.nodeCount(); i < size; i++ ) {
			            Node node = ele.node(i);
			            if ( node instanceof Element) {
			            	if(node.getName().equals("Name")) {
			            		key = ((Element) node).getTextTrim();
			            	}
			            	else if(node.getName().equals("Value")) {
			            		value = ((Element) node).getTextTrim();
			            	}
			            }
			        }
					if(null != key && value != null) {
						adConfig.put(key, value);
					}
				}
				Node domainNameNode = document.selectSingleNode("/*[name()='DestinyConfiguration']/*[name()='ApplicationUserConfiguration']/*[name()='ExternalDomainConfiguration']/*[name()='DomainName']");
				
				String sqlStatement = null;
				switch(databaseType) {
					case MS_SQL:
						sqlStatement = "INSERT INTO AUTH_HANDLER_REGISTRY " + 
								"(CREATED_DATE, LAST_UPDATED_BY, LAST_UPDATED, CREATED_BY, VERSION, ACCOUNT_ID, CONFIG_DATA_JSON, NAME, TYPE, USER_ATTRS_JSON)" + 
								"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
						break;
					case ORACLE:
						sqlStatement = "INSERT INTO AUTH_HANDLER_REGISTRY " + 
								"(ID, CREATED_DATE, LAST_UPDATED_BY, LAST_UPDATED, CREATED_BY, VERSION, ACCOUNT_ID, CONFIG_DATA_JSON, NAME, TYPE, USER_ATTRS_JSON)" + 
								"VALUES(HIBERNATE_SEQUENCE.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
						break;
					case POSTGRESQL:
						break;
				}
				
				ObjectMapper jsonMapper = new ObjectMapper();
				Map<String, String> jsonObj = new HashMap<String, String>();
				String protocol = Boolean.valueOf(adConfig.get("useSSL")) ? "ldaps://" : "ldap://"; 
				jsonObj.put("ldapUrl", protocol + adConfig.get("server.name") + ":" + adConfig.get("server.port"));
				jsonObj.put("ldapDomain", domainNameNode.getText());
				jsonObj.put("baseDn", adConfig.get("root.dn"));
				String username = adConfig.get("login.dn").substring(0, adConfig.get("login.dn").indexOf("@" + domainNameNode.getText()));
				jsonObj.put("username", username);
				jsonObj.put("password", adConfig.get("login.password"));
				jsonObj.put("userPrincipalFilter", "userPrincipalcName={dn}");
				jsonObj.put("userSearchBase", adConfig.get("user.searchspec"));
				
				Map<String, String> userAttrObj = new HashMap<String, String>();
				userAttrObj.put("username", adConfig.get("user.attributefor.login"));
				userAttrObj.put("lastName", adConfig.get("user.attributefor.lastname"));
				userAttrObj.put("firstName", adConfig.get("user.attributefor.firstname"));
				Set<String> lowerCaseAttr = new HashSet<String>();
				lowerCaseAttr.add("username");
				lowerCaseAttr.add("lastname");
				lowerCaseAttr.add("firstname");
				for(Entry<String, String> entry : adConfig.entrySet()) {
					if(!entry.getKey().startsWith("user.attributefor.")) continue;
					String attr = entry.getKey().substring("user.attributefor.".length());
					if(lowerCaseAttr.contains(attr.toLowerCase())) continue;
					userAttrObj.put(attr, entry.getValue());
					lowerCaseAttr.add(attr.toLowerCase());
				}
				Date now = new Date();
				Object[] parameters = new Object[] {now, 0, now, 0, 0, "", jsonMapper.writeValueAsString(jsonObj), domainNameNode.getText(), "LDAP", jsonMapper.writeValueAsString(userAttrObj)};
				long authHandlerId = DatabaseHelper.processSqlPreparedStatementsAndReturnInsertedId(connection, sqlStatement, "ID", parameters);

	            String updateAllImportedUser = "UPDATE APPLICATION_USER SET AUTH_HANDLER_ID=%s WHERE USER_TYPE='imported'";
	            DatabaseHelper.processSqlStatement(connection, String.format(updateAllImportedUser, authHandlerId));
	            LOG.info("all imported users are updated with this new auth_handler_id:" + authHandlerId);
	            LOG.info("migrate AD config from xml to database for updating from OPL completed");
			} catch (Exception e) {
				LOG.error("Exception migrating AD", e);
			}
		}
	}
	
    private void insertInitalData(Connection connection, DbType dbType) throws SQLException {
    	IVersion v76 = new VersionDefaultImpl(7, 6, 0, 0, 0);
		
    	LOG.info("Initial data for version 7.5 will be start inserting in a while. [ DB Type : " + dbType + ", version : "+ fromV + "]");
    	
		if (action == null || (v76.compareTo(fromV) > 0)) {

			List<String> sqlStatements = new ArrayList<String>();
			if (dbType == DbType.ORACLE) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "reporter_v7_5_oracle.sql");
			} else if (dbType == DbType.MS_SQL) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "reporter_v7_5_mssql.sql");
			} else if (dbType == DbType.POSTGRESQL) {
				sqlStatements = getSqls(SQL_FILE_SQL_PATH + "reporter_v7_5_postgres.sql");
			}
			
			for (String sqlStatement : sqlStatements) {
				DatabaseHelper.processSqlStatement(connection, sqlStatement);
			}
		}
    	
    	LOG.info("Inserting of initial data for version 7.5 is completed. ");
    }
    
    protected List<String> getSqls(String sqlFilePath){
        InputStream is = ActivityCustomIndexSeedTask.class.getResourceAsStream(sqlFilePath);
        if (is == null) {
        	LOG.error("Can't find file, " + sqlFilePath + ".");
            return null;
        }
        try {
            return readSql(is);
        } catch (IOException e) {
        	LOG.error("Can't read file, " + sqlFilePath, e);
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            	LOG.warn("Can't close inputstream");
            }
        }
    }

    private List<String> readSql(InputStream is) throws IOException {
        StringWriter writer = new StringWriter();
        int c;
        while ((c = is.read()) != -1) {
            writer.write(c);
        }
        
        String content = writer.toString();
        String[] sqls = content.split(";");
        
        List<String> output = new ArrayList<String>();
        for (String sql : sqls) {
        	output.add(sql);
        }
        return output;
    }
    
    private void createMSSQLspecific(Connection connection) throws SeedDataTaskException, SQLException{
        LOG.info("Creating Microsoft specific index, Transact-SQL");
        // http://msdn.microsoft.com/en-us/library/ms188783.aspx
        
        //don't create statistic yet since it is not in the databasemetadata
        String[] sqls = readSqlFile(MSSQL_FINE_TUNE_SQL_PATH);
        
        List<String> willExecuteSqls = new LinkedList<String>();
        
        Matcher m;
        //only read create index but not statistic, warning the rest
        for(String sql : sqls){
            m = MSSQL_CREATE_INDEX_PATTERN.matcher(sql);
            if (m.find()) {
                String indexName = m.group(1);
                String tableName = m.group(2);
                
                if (!DatabaseHelper.isIndexExists(connection.getMetaData(), dialectX, noQuoteName(tableName),
                        noQuoteName(indexName))) {
                    willExecuteSqls.add(sql);
                } else {
                    LOG.info("Index '" + indexName + "' is already created on table '" + tableName +"'.");
                }
                continue;
            }
            
            m = MSSQL_CREATE_STATISTICS_PATTERN.matcher(sql);
            if (m.find()) {
                String statisticsName = m.group(1);
                String tableName = m.group(2);
                LOG.info("Statistics is not supported in dbinit yet. sql = " + sql);
                continue;
            }
            
            LOG.info("Unknown sql statement, it is not create index neither create statistics. sql = " + sql);
        }
        
        DatabaseHelper.processSqlStatements(connection, willExecuteSqls);
    }
}
