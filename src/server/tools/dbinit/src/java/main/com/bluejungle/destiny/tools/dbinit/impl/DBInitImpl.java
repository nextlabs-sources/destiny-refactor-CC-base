/*
 * Created on May 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.tools.dbinit.DBInitException;
import com.bluejungle.destiny.tools.dbinit.DBInitType;
import com.bluejungle.destiny.tools.dbinit.IConfigFileProperties;
import com.bluejungle.destiny.tools.dbinit.IDBInit;
import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.ConfigurationMod.Action;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseHelper;
import com.bluejungle.destiny.tools.dbinit.hibernate.DatabaseMetadataMod;
import com.bluejungle.destiny.tools.dbinit.hibernate.dialect.DialectExtended;
import com.bluejungle.destiny.tools.dbinit.javaupdate.IJavaUpdateTask;
import com.bluejungle.destiny.tools.dbinit.javaupdate.JavaUpdateException;
import com.bluejungle.destiny.tools.dbinit.seedtasks.SeedHibernateDataSourceImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedUpdateTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.version.IVersion;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.ConfigurationFileParser;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.connection.ConnectionProviderFactory;
import net.sf.hibernate.dialect.Dialect;

/**
 * This is the seed task implementation
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernateImpl/DBInitImpl.java#1 $
 */

public class DBInitImpl implements IDBInit {
    /**
	 * 
	 */
	private static final String CONFIGURATION_FILE_NAME = "configuration.xml";
	private static final int HIBERNATE_CONNECTION_DEFAULT_POOL_SIZE = 20;
    private static final String SLASH = System.getProperty("file.separator");
    private static final String RELATIVE_PATH = SLASH + "server" + SLASH + "configuration" + SLASH;
    private static final Log LOG = LogFactory.getLog(DBInitImpl.class);
 
    private final IConfiguration configuration;
    private final IComponentManager manager;
    private final boolean quiet;
    private final Properties configProps;
    private final ConfigurationMod hibernateCfg;
    private final Connection connection;
    
    public DBInitImpl(File serverInstallFolder, File configFile, String libraryFoldersStr,
                      IConfiguration configuration, boolean isQuiet) throws Exception {
    	this.configuration = configuration;
        this.quiet = isQuiet;
        manager = ComponentManagerFactory.getComponentManager();
        
        configProps = FileHelper.loadProperties(configFile);
        
        if (libraryFoldersStr == null) {
            throw new NullPointerException("library path cannot be null");
        }
        String[] libDirs = splitTerms(libraryFoldersStr);
        if (libDirs == null) {
            throw new InvalidParameterException("No lib directory specified");
        }
        hibernateCfg = new ConfigurationMod();
        for (String libDirStr : libDirs) {
            File libDir = new File(libDirStr);
            File[] jars = libDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".jar");
                }
            });
            for (File jar : jars) {
                hibernateCfg.addJar(jar);
            }
        }

        //default value go first
        
        hibernateCfg.setProperty(Environment.POOL_SIZE, "" + HIBERNATE_CONNECTION_DEFAULT_POOL_SIZE);
        hibernateCfg.setProperty(Environment.CONNECTION_PROVIDER, DBInitConnectionProvider.class.getName());
        hibernateCfg.setProperty(Environment.SHOW_SQL, quiet ? "false " : "true");

        ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
        
        File configFolder = new File(serverInstallFolder, RELATIVE_PATH);
        
        // set config.xml path to system property
        File ccConfigFile = new File(configFolder, CONFIGURATION_FILE_NAME);
        System.setProperty("cc.config.file", ccConfigFile.getAbsolutePath());
        
		configurationFileParser.parseConfig(configFolder);

        RepositoryConfigurationList repositoryConfigurationList = configurationFileParser.getRepositories();
        
        Set<? extends IRepositoryConfigurationDO> repositories = repositoryConfigurationList.getRepositoriesAsSet();

        String repositoryName = configProps.getProperty(IConfigFileProperties.NAME_PROPERTY);

        for (IRepositoryConfigurationDO repConf : repositories) {
            if(repositoryName.equals(repConf.getName())){
                hibernateCfg.setProperty(Environment.DIALECT, 
                                         repConf.getProperties().getProperty(Environment.DIALECT));

                IConnectionPoolConfigurationDO connectionPoolConf = repConf.getConnectionPoolConfiguration();

                hibernateCfg.setProperty(Environment.URL,       connectionPoolConf.getJDBCConnectString());
                hibernateCfg.setProperty(Environment.DRIVER,    connectionPoolConf.getDriverClassName());
                hibernateCfg.setProperty(Environment.USER,      connectionPoolConf.getUserName());
                hibernateCfg.setProperty(Environment.PASS,      connectionPoolConf.getPassword());
                hibernateCfg.setProperty(Environment.POOL_SIZE, Integer.toString(connectionPoolConf.getMaxPoolSize()));
            }
        }
        
        Properties props = new Properties();
        Dialect dialect = Dialect.getDialect(hibernateCfg.getProperties());
        props.putAll(dialect.getDefaultProperties());
        props.putAll(hibernateCfg.getProperties());
        connection = ConnectionProviderFactory.newConnectionProvider(props).getConnection();
        if (!connection.getAutoCommit()) {
            connection.commit();
            connection.setAutoCommit(true);
        }
        
        //create dialectX
        DialectExtended dialectX = DialectExtended.getDialectExtended(dialect);
        
        DatabaseMetadataMod meta = new DatabaseMetadataMod(connection, dialectX);
        hibernateCfg.init(meta, dialectX);
        String name = repositoryName.substring(0, repositoryName.indexOf('.'));
        hibernateCfg.constructMapping(name, connection);
    }
 
    /**
     * @see com.bluejungle.destiny.tools.dbinit.IDBInit#execute()
     */
    @Override
	public void execute(DBInitType action) throws DBInitException {
        long startTime = System.currentTimeMillis();
        LOG.trace("start DBInitImpl.execute()");

        try {
            //major actions
            LOG.info("start action " + action);
            switch (action) {
                case INSTALL: {
                    processInstallTask((String) configProps.get(IConfigFileProperties.PRE_INSTALL_TASK_PROPERTY_NAME));
                
                    List<String> stats = hibernateCfg.generateSchema(Action.DROP_EXIST_MAPPED_THEN_CREATE_SCHEMA);

                    processSqlStatements(stats);

                    //process seed data
                    processInstallTask((String) configProps.get(IConfigFileProperties.INSTALL_TASK_PROPERTY_NAME));
                }
                    break;
                case UPGRADE: {
                    processJavaUpdateTask((String) configProps.get(IConfigFileProperties.PRE_UPDATE_TASK_PROPERTY_NAME));
                
                
                    startTime = System.currentTimeMillis();
                    LOG.info("start pre-schema");
                    List<String> stats = hibernateCfg.generateSchema(Action.PRE_SCHEMA);
                    processSqlStatements(stats);
                    LOG.info("done pre-schema, took " + (System.currentTimeMillis() - startTime) + "ms");
                
                    //update database mapping
                    hibernateCfg.updateDatabaseMapping(connection);

                    //run java task
                    processJavaUpdateTask((String) configProps.get(IConfigFileProperties.JAVA_UPDATE_TASK_PROPERTY_NAME));
                
                    //update database mapping
                    hibernateCfg.updateDatabaseMapping(connection);

                    startTime = System.currentTimeMillis();
                    LOG.info("start post-schema");
                    //complete schema update, just dropping and change attributes
                    stats = hibernateCfg.generateSchema(Action.POST_SCHEMA);
                    processSqlStatements(stats);
                    LOG.info("done post-schema, took " + (System.currentTimeMillis() - startTime) + "ms");
                
                
                    //process upgrade seed data
                    processUpgradeTask((String)configProps.get(IConfigFileProperties.UPDATA_TASK_PROPERTY_NAME));
                }
                    break;
                case CREATE_SCHEMA: {
                    List<String> sqls = hibernateCfg.generateSchema(Action.CREATE_SCHEMA);
                    writeToFile(sqls);
                }
                    break;
                case DROP_CREATE_SCHEMA: {
                    List<String> sqls = hibernateCfg.generateSchema(Action.DROP_EXIST_MAPPED_THEN_CREATE_SCHEMA);
                    writeToFile(sqls);
                }
                    break;
                case UPDATE_SCHEMA: {
                    List<String> sqls = hibernateCfg.generateSchema(Action.PRE_SCHEMA);
                    sqls.addAll(hibernateCfg.generateSchema(Action.POST_SCHEMA));
                    writeToFile(sqls);
                }
                    break;
                case PROCESS_SQL_FROM_FILE: {
                    File schemaFile = configuration.get(IDBInit.SCHEMA_FILE_CONFIG_PARAM);
                    List<String> sqls = FileHelper.readFromFile(schemaFile);
                    processSqlStatements(sqls);
                }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal action '" + action + "'.");
            }
        } catch (FileNotFoundException e) {
            throw new DBInitException(e);
        } catch (HibernateException e) {
            throw new DBInitException(e);
        } catch (IOException e) {
            throw new DBInitException(e);
        } catch (SQLException e) {
            throw new DBInitException(e);
        }finally{
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new DBInitException(e);
                }
            }
        }
        LOG.info("completely done");
    }
 
 
    private void processSqlStatements(List<String> statements) throws DBInitException {
        List<SQLException> results = DatabaseHelper.processSqlStatements(connection, statements);
        boolean prefect = true;
        StringBuilder sb = new StringBuilder("Error on processing sql statements.");
        sb.append(ConsoleDisplayHelper.NEWLINE);
        for (int i = 0; i < results.size(); i++) {
            SQLException sqlException = results.get(i);
            String statement = statements.get(i);
            //it is ok if drop failed
            if (sqlException != null) {
                prefect = false;
                sb.append(sqlException.getMessage()).append("    on \"")
                    .append(statement).append("\"")
                    .append(ConsoleDisplayHelper.NEWLINE);
            }
        }
        if (!prefect) {
            throw new DBInitException(sb.toString());
        }
    }


    private void writeToFile(List<String> dataToOut) throws IOException {
        File file = configuration.get(IDBInit.SCHEMA_FILE_CONFIG_PARAM);
        FileHelper.writeToFile(file, dataToOut);
    }
 
    /**
     * Returns whether the work should be done quietly
     * 
     * @return whether the work should be done quietly
     */
    public boolean isQuiet() {
        return this.quiet;
    }
 
    /**
     * Returns a new instance of an hibernate data source that can be used by
     * the seed data classes.
     * 
     * @return a new hibernate data source
     */
    protected IHibernateRepository createHibernateRepository() throws HibernateException {
        String repositoryName = (String)configProps.get(IConfigFileProperties.NAME_PROPERTY);
        if(manager.isComponentRegistered(repositoryName)){
            return (IHibernateRepository)manager.getComponent(repositoryName);
        }
        
        SessionFactory sf = hibernateCfg.buildSessionFactory();
        HashMapConfiguration dataSourceConfig = new HashMapConfiguration();
        dataSourceConfig.setProperty(SeedHibernateDataSourceImpl.SESSION_FACTORY_CONFIG_PARAM, sf);
        ComponentInfo<SeedHibernateDataSourceImpl> compInfo = 
            new ComponentInfo<SeedHibernateDataSourceImpl>(
                repositoryName, 
            SeedHibernateDataSourceImpl.class,
            IHibernateRepository.class, 
            LifestyleType.SINGLETON_TYPE, 
            dataSourceConfig);
        return manager.getComponent(compInfo);
    }
    
    
    private abstract class InterfaceTask<T>{
        
        abstract Class<T> getInterface();
        
        String getName(){
            return getInterface().getName();
        }
        
        IConfiguration getTaskConfiguration(){
            return null;
        }
        
        abstract void execute(T task) throws Exception;
        
        void run(String classesStr){
            String[] classNames  = splitTerms(classesStr);
            if (classNames == null) {
                LOG.info("no " + getName());
                return;
            }
            
            IConfiguration config = getTaskConfiguration();
            
            for (String className : classNames) {
                Class<T> interfaze = getInterface();
                Class<T> clazz;
                try {
                    clazz = (Class<T>)Class.forName(className);
                    if (!interfaze.isAssignableFrom(clazz)){
                        LOG.error("'" + className + "' does not implement interface '" + interfaze + "'.");
                        continue;
                    }
                } catch (ClassNotFoundException e) {
                    LOG.error("'" + className + "' does not exists.", e);
                    continue;
                }
                
                ComponentInfo<T> seedDataCompInfo = 
                    new ComponentInfo<T>(
                        className, 
                    clazz,
                    interfaze, 
                    LifestyleType.TRANSIENT_TYPE, 
                    config);
                T seedTask =  manager.getComponent(seedDataCompInfo);
                try {
                    LOG.info("Task being: " + className);
                    execute(seedTask);
                    LOG.info("Task completed succesfully : " + className);
                } catch (RuntimeException e) {
                    LOG.error("Unexpected error in " + className, e);
                } catch (Exception e) {
                    LOG.error("Error in " + className, e);
                }
            }
        }
    }

    private void processInstallTask(String classesStr) throws HibernateException {
        final HashMapConfiguration taskConfig = new HashMapConfiguration();
        taskConfig.setProperty(ISeedDataTask.HIBERNATE_DATA_SOURCE_CONFIG_PARAM, createHibernateRepository());
        taskConfig.setProperty(ISeedDataTask.CONFIG_PROPS_CONFIG_PARAM, configProps);
        taskConfig.setProperty(ISeedDataTask.DIALECT_EXTENDED_CONFIG_PARAM, hibernateCfg.getDialect());

        new InterfaceTask<ISeedDataTask>(){
            @Override
            Class<ISeedDataTask> getInterface() {
                return ISeedDataTask.class;
            }
            
            @Override
            IConfiguration getTaskConfiguration() {
                return taskConfig;
            }

            @Override
            void execute(ISeedDataTask task) throws SeedDataTaskException {
                task.execute();
            }
        }.run(classesStr);
    }
 
    private void processUpgradeTask(String classesStr) throws HibernateException {
        final HashMapConfiguration taskConfig = new HashMapConfiguration();
        taskConfig.setProperty(ISeedUpdateTask.HIBERNATE_DATA_SOURCE_CONFIG_PARAM, createHibernateRepository());
        taskConfig.setProperty(ISeedUpdateTask.CONFIG_PROPS_CONFIG_PARAM, configProps);
        taskConfig.setProperty(ISeedUpdateTask.DIALECT_EXTENDED_CONFIG_PARAM, hibernateCfg.getDialect());

        final IVersion fromVersion = configuration.get(IDBInit.FROM_VERSION_PARAM);
        final IVersion toVersion   = configuration.get(IDBInit.TO_VERSION_PARAM);
  
        new InterfaceTask<ISeedUpdateTask>(){
            @Override
            Class<ISeedUpdateTask> getInterface() {
                return ISeedUpdateTask.class;
            }
            
            @Override
            IConfiguration getTaskConfiguration() {
                return taskConfig;
            }

            @Override
                void execute(ISeedUpdateTask task) throws SeedDataTaskException {
                task.execute(fromVersion, toVersion);
            }
        }.run(classesStr);
    }
 
    private void processJavaUpdateTask(String classesStr) throws HibernateException {
        final HashMapConfiguration taskConfig = new HashMapConfiguration();
        taskConfig.setProperty(IJavaUpdateTask.CONFIG_PROPS_CONFIG_PARAM, configProps);
        taskConfig.setProperty(IJavaUpdateTask.HIBERNATE_DATA_SOURCE_CONFIG_PARAM, createHibernateRepository());

        final IVersion fromVersion = configuration.get(IDBInit.FROM_VERSION_PARAM);
        final IVersion toVersion   = configuration.get(IDBInit.TO_VERSION_PARAM);
     
        new InterfaceTask<IJavaUpdateTask>(){
            @Override
            Class<IJavaUpdateTask> getInterface() {
                return IJavaUpdateTask.class;
            }

            @Override
            IConfiguration getTaskConfiguration() {
                return taskConfig;
            }

            @Override
            void execute(IJavaUpdateTask task) throws JavaUpdateException {
                task.execute(connection, hibernateCfg, fromVersion, toVersion);
            }
        }.run(classesStr);
    }
 
 
    /**
     * Splits an expression based on a separator
     * 
     * @param expression
     *            expression to analyze
     * @return an array of all the separated terms.
     */
    private String[] splitTerms(String expression){
        LOG.info("Trying to split classes: " +expression);
        if (expression == null || expression.trim().length() == 0) {
            return null;
        }
     
        String[] result = null;
        StringTokenizer tokenizer = new StringTokenizer(expression, IConfigFileProperties.SEPARATOR);
        int size = tokenizer.countTokens();
        result = new String[size];
        for (int i = 0; i < size; i++) {
            result[i] = tokenizer.nextToken().trim();
        }
        return result;
    }
}
