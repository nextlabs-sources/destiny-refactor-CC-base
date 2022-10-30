/*
 * Created on Oct 3, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Environment;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.destiny.container.dabs.components.log.hibernateimpl.HibernateLogWriter;
import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList;
import com.bluejungle.destiny.tools.dbinit.seedtasks.SeedHibernateDataSourceImpl;
import com.bluejungle.destiny.tools.reporterdata.ReporterData;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl.ConfigurationFileParser;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;

import static com.nextlabs.destiny.tools.reporterdata.ReporterDataOptionDescriptorEnum.*;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/ReporterDataConsole.java#1 $
 */
public class ReporterDataConsole extends ConsoleApplicationBase{
	private static final Log LOG = LogFactory.getLog(ReporterDataConsole.class);
	
	private static final String CONFIGURATION_DIGESTER_FILENAME = "configuration.digester.rules.reporterData.xml";
	private static final String CONFIGURATION_XML_FILENAME = ConfigurationFileParser.CONFIG_DATA_FILE_NAME;
	
	private static final String INTERNAL_CONFIGURATION_DIGESTER_FILENAME =
			"/com/nextlabs/destiny/tools/reporterdata/" + CONFIGURATION_DIGESTER_FILENAME;

	private final IConsoleApplicationDescriptor options;
	
	public ReporterDataConsole() throws InvalidOptionDescriptorException{
		options = new ReporterDataOptionDescriptorEnum();
	}
	
	public static void main(String[] args) {
		try {
			ReporterDataConsole console = new ReporterDataConsole();
			console.parseAndExecute(args);
		} catch (Exception e) {
			LOG.fatal(e);
		}
	}
	
	@Override
	protected void execute(ICommandLine commandLine) {
		LOG.info("ReporterData starts");
		File configurationFolder = getValue(commandLine,
				SERVER_CONFIGURATION_FOLDER_OPTION_ID);
		try {
			ReporterData reporterData = new ReporterData();
			
			IRepositoryConfigurationDO repConf = parseServerConfigFile(configurationFolder,
					DestinyRepository.ACTIVITY_REPOSITORY.getName());
			
			Properties hibernateProp = parseRepositoryConfiguration(repConf);
			
			//create a hibernate sessionfactgory and session
			final SessionFactory sessionFactory = reporterData
					.initHibernateSessionFactory(hibernateProp);
			
			Session s = sessionFactory.openSession();
			
			if (commandLine.isOptionExist(REGULAR_OPTION_ID)) {
				reporterData.createActivityData(s);
			} else if (commandLine.isOptionExist(DASHBOARD_DATA_OPTION_ID)) {
				File dashboardFile = getValue(commandLine, DASHBOARD_DATA_OPTION_ID);
				reporterData.createDashboardData(s, dashboardFile);
			} else if (commandLine.isOptionExist(PERFORMANCE_OPTION_ID)) {
				File perfConfigFile = getValue(commandLine, PERFORMANCE_OPTION_ID);
				ReporterDataPerfConfig perfConfig = new ReporterDataPerfConfig(perfConfigFile);
				HibernateLogWriter hibernateLogWriter = initHibernateLogWriter(sessionFactory);
				new ReporterDataPerf(s, hibernateLogWriter, perfConfig).createPerformanceData();
			} else if (commandLine.isOptionExist(UNKNOWN_INPUT_OPTION_ID)) {
				reporterData.readInput(getValue(commandLine, UNKNOWN_INPUT_OPTION_ID));
			} else if (commandLine.isOptionExist(CSV_DATA_OPTION_ID)) {
				ReporterDataFromCSVConfig config =
						new ReporterDataFromCSVConfig(getValue(commandLine,	CSV_CONFIG_OPTION_ID));
				File csvDataFile = getValue(commandLine, CSV_DATA_OPTION_ID);
				
				HibernateLogWriter hibernateLogWriter = initHibernateLogWriter(sessionFactory);
				ReporterDataFromCSV reporterDataCsv = new ReporterDataFromCSV(s, hibernateLogWriter, config);
				reporterDataCsv.parseDataFile(csvDataFile);
				reporterDataCsv.insertData();
			}
			LOG.info("Data insertion complete successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected HibernateLogWriter initHibernateLogWriter(SessionFactory sf) {
		IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
		HashMapConfiguration dataSourceConfig = new HashMapConfiguration();
		dataSourceConfig.setProperty(SeedHibernateDataSourceImpl.SESSION_FACTORY_CONFIG_PARAM, sf);

		ComponentInfo<SeedHibernateDataSourceImpl> compInfo = 
			new ComponentInfo<SeedHibernateDataSourceImpl>(
				DestinyRepository.ACTIVITY_REPOSITORY.getName(),
				SeedHibernateDataSourceImpl.class, 
				IHibernateRepository.class,
				LifestyleType.SINGLETON_TYPE, 
				dataSourceConfig);

		//register the hibernate log writer.
		IHibernateRepository result = componentManager.getComponent(compInfo);
		if(result == null){
			throw new RuntimeException("fail to create hibernate log writer");
		}
		
		ComponentInfo<HibernateLogWriter> persistenceLogWriterCompInfo = 
			new ComponentInfo<HibernateLogWriter>(
				HibernateLogWriter.COMP_NAME, 
				HibernateLogWriter.class, 
				ILogWriter.class, 
				LifestyleType.SINGLETON_TYPE);
		HibernateLogWriter writer = componentManager.getComponent(persistenceLogWriterCompInfo);
		if(writer == null){
			throw new NullPointerException(HibernateLogWriter.class.getName());
		}
		return writer;
        
	}
	
	/**
	 * load the configuration.xml from server. 
	 * @param configurationFolder
	 * @param repositoryName
	 * @return
	 * @throws IOException
	 */
	protected IRepositoryConfigurationDO parseServerConfigFile(File configurationFolder,
			String repositoryName) throws IOException {
		ConfigurationFileParser configurationFileParser = loadConfiguration(configurationFolder);
		
		IComponentManager manager = ComponentManagerFactory.getComponentManager();
		DestinyConfigurationStoreImpl confStore = manager.getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
		
		confStore.cacheActionListConfig(configurationFileParser.getActionListConfig());

		
		RepositoryConfigurationList repositoryConfigurationList = configurationFileParser.getRepositories();
		
		if (repositoryConfigurationList == null) {
			throw new NullPointerException("RepositoryConfigurationList, probably the parameter '"
					+ SERVER_CONFIGURATION_FOLDER_OPTION_ID + "' is not correct.");
		}
		
		Set<? extends IRepositoryConfigurationDO> repositories = repositoryConfigurationList.getRepositoriesAsSet();
		for (IRepositoryConfigurationDO repConf : repositories) {
			if(repositoryName.equals(repConf.getName())){
				return repConf;
			}
		}
		throw new IllegalArgumentException("Can't find repository : " + repositoryName);
	}

    protected ConfigurationFileParser loadConfiguration(File configurationFolder)
            throws IOException {
		File serverConfigDataFile = new File(configurationFolder, CONFIGURATION_XML_FILENAME);
		
		//same folder as the running folder
		File serverConfigDigesterFile = new File(CONFIGURATION_DIGESTER_FILENAME);
		if (!serverConfigDigesterFile.exists()) {
			LOG.warn("The digester file \"" + serverConfigDigesterFile.getAbsolutePath()
					+ "\" is not found.");
			
			File tempFDir = new File(System.getProperty("java.io.tmpdir"));
			//TODO can tempDir be null?
			
			File tempFile;
			String fileName = CONFIGURATION_DIGESTER_FILENAME;
			final String fileNameTempalte = "configuration.digester.rules.dbOnly.%02d.xml"; 
			final int maxFileTry = 20;
			int i = 1;
			do {
				tempFile = new File(tempFDir, fileName);
				fileName = String.format(fileNameTempalte, i++);
				if (i >= maxFileTry) {
					throw new IOException("Fail to create file in " + tempFDir.getAbsolutePath());
				}
			} while (tempFile.exists());
			
			//delete temp file on exit
			tempFile.deleteOnExit();
			
			LOG.debug("Load internal digester file into \"" + tempFile.getAbsolutePath()
					+ "\" The temp file will be deleted on exit.");
			
			InputStream importFileIs = this.getClass().getResourceAsStream(INTERNAL_CONFIGURATION_DIGESTER_FILENAME);
			
			BufferedReader bf = null;
			FileWriter writer = new FileWriter(tempFile);
			try {
				bf = new BufferedReader(new InputStreamReader(importFileIs));
				String line;
				while ((line = bf.readLine()) != null) {
					writer.write(line);
				}
				//unnecessary, the close() will flush too
				writer.flush();
			} finally {
				if (bf != null) {
					bf.close();
				}
				if (writer != null) {
					writer.close();
				}
			}
			serverConfigDigesterFile = tempFile;
		}
		
		ConfigurationFileParser configurationFileParser = new ConfigurationFileParser();
		configurationFileParser.parseConfig(serverConfigDataFile, serverConfigDigesterFile);
		LOG.debug("Confguration is loaded successfully");
        return configurationFileParser;
    }

	private Properties parseRepositoryConfiguration(IRepositoryConfigurationDO repConf) {
		Properties hibernateProperties = new Properties();
		hibernateProperties.setProperty(Environment.DIALECT, repConf.getProperties().getProperty(
				Environment.DIALECT));
		
		IConnectionPoolConfigurationDO connectionPoolConf = repConf.getConnectionPoolConfiguration();
		hibernateProperties.setProperty(Environment.URL, connectionPoolConf.getJDBCConnectString());
		hibernateProperties.setProperty(Environment.DRIVER, connectionPoolConf.getDriverClassName());
		hibernateProperties.setProperty(Environment.USER, connectionPoolConf.getUserName());
		hibernateProperties.setProperty(Environment.PASS, connectionPoolConf.getPassword());
        return hibernateProperties;
	}
	
	
	@Override
	protected IConsoleApplicationDescriptor getDescriptor() {
		return options;
	}

}
