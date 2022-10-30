package com.bluejungle.pf.tools;

import java.util.HashMap;
import java.util.Map;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.destiny.tools.dbinit.impl.DBInitConnectionProvider;
import com.bluejungle.destiny.tools.dbinit.seedtasks.SeedHibernateDataSourceImpl;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.pf.destiny.policymap.STRLog;
import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;

/**
 * @author sasha
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/tools/DeploymentToolsBase.java#1 $
 *
 */

public abstract class DeploymentToolsBase {

    private interface Configurator {
        void configure(Configuration config, String host, int port, String instance);
        
        void configure(Configuration config, String jdbcUrl);
    }

    private static Map<String,Configurator> configurators = new HashMap<String,Configurator>();

    public static final String ORACLE_ARGUMENT_VALUE    = "oracle";
    public static final String POSTGRES_ARGUMENT_VALUE  = "postgres";
    public static final String SQLSERVER_ARGUMENT_VALUE = "sqlserver";
    public static final String DB2_ARGUMENT_VALUE       = "db2";

    static {
        configurators.put(ORACLE_ARGUMENT_VALUE
        ,   new Configurator() {
            @Override
            public void configure(Configuration config, String host, int port, String instance) {
                if (port == -1) {
                    throw new NullPointerException("port");
                }
                if (instance == null) {
                    throw new NullPointerException("instance");
                }
                String jdbcUrl = String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, instance);
                configure(config, jdbcUrl);
            }

            @Override
            public void configure(Configuration config, String jdbcUrl) {
                config.setProperty("hibernate.dialect", "net.sf.hibernate.dialect.Oracle9Dialect");
                config.setProperty("hibernate.connection.driver_class", "oracle.jdbc.driver.OracleDriver");
                config.setProperty("hibernate.connection.url", jdbcUrl);
            }
        });
        configurators.put(POSTGRES_ARGUMENT_VALUE
        ,   new Configurator() {
            public void configure(Configuration config, String host, int port, String instance) {
                String jdbcUrl = String.format("jdbc:postgresql://%s%s/pf", host, (port==-1 || port==0) ? "" : ":"+port);
                configure(config, jdbcUrl);
            }

            @Override
            public void configure(Configuration config, String jdbcUrl) {
                config.setProperty("hibernate.dialect", "net.sf.hibernate.dialect.PostgreSQLDialect");
                config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
                config.setProperty("hibernate.connection.url", jdbcUrl);
            }
        });
        configurators.put(SQLSERVER_ARGUMENT_VALUE
        ,   new Configurator() {
            public void configure(Configuration config, String host, int port, String instance) {
                String jdbcUrl = String.format("jdbc:sqlserver://%s%s;DatabaseName=%s;", host, (port==-1 || port==0) ? "" : ":"+port, instance);
                configure(config, jdbcUrl);
            }

            @Override
            public void configure(Configuration config, String jdbcUrl) {
                config.setProperty("hibernate.dialect", "com.bluejungle.framework.datastore.hibernate.dialect.SqlServer2000Dialect");
                config.setProperty("hibernate.connection.driver_class", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
                config.setProperty("hibernate.connection.url", jdbcUrl);
            }
        });
        configurators.put(DB2_ARGUMENT_VALUE
        ,   new Configurator() {
            public void configure(Configuration config, String host, int port, String instance) {
                if (instance == null) {
                    throw new NullPointerException("instance");
                }
                String jdbcUrl = String.format("jdbc:db2://%s%s/%s:currentSchema=PF;", host, (port==-1 || port==0) ? "" : ":"+port, instance);
                configure(config, jdbcUrl);
            }

            @Override
            public void configure(Configuration config, String jdbcUrl) {
                config.setProperty("hibernate.dialect", "net.sf.hibernate.dialect.DB2Dialect");
                config.setProperty("hibernate.connection.driver_class", "com.ibm.db2.jcc.DB2Driver");
                config.setProperty("hibernate.connection.url", jdbcUrl);
            }
        });
    }

    private static final String DATABASE_ARGUMENT_NAME     = "-database";
    private static final String USER_ARGUMENT_NAME         = "-user";
    private static final String PASSWORD_ARGUMENT_NAME     = "-password";
    private static final String HOST_ARGUMENT_NAME         = "-host";
    private static final String PORT_ARGUMENT_NAME         = "-port";
    private static final String INSTANCE_ARGUMENT_NAME     = "-instance";

    protected IComponentManager cm = ComponentManagerFactory.getComponentManager();
    protected LifecycleManager lm;

    protected static int skipParameter(String name) {
        if (PASSWORD_ARGUMENT_NAME.equalsIgnoreCase(name)
        ||  HOST_ARGUMENT_NAME.equalsIgnoreCase(name)
        ||  PORT_ARGUMENT_NAME.equalsIgnoreCase(name)
        ||  USER_ARGUMENT_NAME.equalsIgnoreCase(name)
        ||  DATABASE_ARGUMENT_NAME.equalsIgnoreCase(name)
        ||  INSTANCE_ARGUMENT_NAME.equalsIgnoreCase(name)) {
            return 2;
        } else {
            return -1;
        }
    }
    
    protected void setupDatasource(String[] args) throws HibernateException {
        String userName = "root";
        String host = "localhost";
        String password = null;
        String instance = null;
        int port = -1;

        // Parse the arguments
        String dbType = POSTGRES_ARGUMENT_VALUE;

        for ( int i = 0 ; i < args.length ; i++ ) {
            if (PASSWORD_ARGUMENT_NAME.equalsIgnoreCase(args[i]) && i != args.length-1) {
                password = args[++i];
            } else if (USER_ARGUMENT_NAME.equalsIgnoreCase(args[i]) && i != args.length-1) {
                userName = args[++i];
            } else if (HOST_ARGUMENT_NAME.equalsIgnoreCase(args[i]) && i != args.length-1) {
                host = args[++i];
            } else if (PORT_ARGUMENT_NAME.equalsIgnoreCase(args[i]) && i != args.length-1) {
                port = Integer.parseInt(args[++i]);
            } else if (INSTANCE_ARGUMENT_NAME.equalsIgnoreCase(args[i]) && i != args.length-1) {
                instance = args[++i];
            } else if (DATABASE_ARGUMENT_NAME.equalsIgnoreCase(args[i]) && i != args.length-1) {
                dbType = args[++i];
            }
        }

        setupDatasource(userName, host, password, instance, port, dbType);
    }
    
    protected Configurator getConfigurator(String dbType) {
        Configurator configurator = configurators.get(dbType);
        if (configurator == null) {
            throw new IllegalArgumentException("Unexpected DB type: "+dbType);
        }
        return configurator;
    }
    
    protected Configuration createHibernateConfig(String userName,String password) {
        // Do the common part of the configuration
        Configuration hibernateCfg = new Configuration();
        hibernateCfg.setProperty("hibernate.query.substitutions", "true 1, false 0, yes 'Y', no 'N'");
        hibernateCfg.setProperty("hibernate.connection.pool_size", "5");
        hibernateCfg.setProperty("hibernate.connection.username", userName);
        hibernateCfg.setProperty("hibernate.connection.password", password);
        hibernateCfg.setProperty("hibernate.connection.provider_class", DBInitConnectionProvider.class.getName());
        String showSQL = System.getProperty("hibernate.show_sql");
        if (showSQL == null) {
            showSQL = "false";
        }
        hibernateCfg.setProperty("hibernate.show_sql", showSQL);
        return hibernateCfg;
    }
    
    protected void postSetupDatasource(Configuration hibernateCfg) throws HibernateException {
        // TODO: figure out a more maintainable way of doing this
        hibernateCfg.addClass(DevelopmentEntity.class);
        hibernateCfg.addClass(DeploymentEntity.class);
        hibernateCfg.addClass(DeploymentRecord.class);
        hibernateCfg.addClass(STRLog.class);
       
        SessionFactory sf = hibernateCfg.buildSessionFactory();
        HashMapConfiguration dataSourceConfig = new HashMapConfiguration();
        dataSourceConfig.setProperty(SeedHibernateDataSourceImpl.SESSION_FACTORY_CONFIG_PARAM, sf);
        ComponentInfo<IHibernateRepository> compInfo = new ComponentInfo<IHibernateRepository>(
                DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName()
              , SeedHibernateDataSourceImpl.class
              , IHibernateRepository.class
              , LifestyleType.TRANSIENT_TYPE
              , dataSourceConfig
        );
        cm.registerComponent(compInfo, true);
        lm = cm.getComponent(LifecycleManager.COMP_INFO);
    }

    protected void setupDatasource(String userName, String host, String password, String instance, int port, String dbType) throws MappingException, HibernateException {
        Configurator configurator = getConfigurator(dbType);
        Configuration hibernateCfg = createHibernateConfig(userName, password);
    
        // Do the database-specific configuration
        configurator.configure(hibernateCfg, host, port, instance);
    
        postSetupDatasource(hibernateCfg);
    }
    
    protected void setupDatasource(String userName, String password, String dbType, String jdbcUrl) throws MappingException, HibernateException {
        Configurator configurator = getConfigurator(dbType);
        Configuration hibernateCfg = createHibernateConfig(userName, password);
    
        // Do the database-specific configuration
        configurator.configure(hibernateCfg, jdbcUrl);
    
        postSetupDatasource(hibernateCfg);
    }
    
    public void fullClean() throws HibernateException {
        IHibernateRepository hds = (IHibernateRepository) ComponentManagerFactory.getComponentManager().getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());
        Session session = hds.getSession();
        Transaction tx = session.beginTransaction();
        session.delete("from DeploymentEntity");
        session.delete("from DevelopmentEntity");
        session.delete("from DeploymentRecord");
        tx.commit();
        session.close();
    }    

}
