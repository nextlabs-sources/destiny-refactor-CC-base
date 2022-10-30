/*
 * Created on Feb 22, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.axis.types.NonNegativeInteger;
import org.apache.axis.types.PositiveInteger;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;

import com.bluejungle.destiny.server.shared.configuration.IActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.IApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IAuthenticatorConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationArgumentDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDABSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDACComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDCSFComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDEMComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDMSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IExternalDomainConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IGenericComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlerConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IMgmtConsoleComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IReporterComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IUserAccessConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IUserRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ActionConfigDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ActionListConfigDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ApplicationUserConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.AuthenticatorConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.CustomObligationArgumentDO;
import com.bluejungle.destiny.server.shared.configuration.impl.CustomObligationConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.CustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DABSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DACComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DCCComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DCSFComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DEMComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DMSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DaysOfMonthDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DaysOfWeekDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ExternalDomainConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.FileSystemLogConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.GenericComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.MessageHandlerConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.MessageHandlersConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.MgmtConsoleComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.PropertyDO;
import com.bluejungle.destiny.server.shared.configuration.impl.PropertyList;
import com.bluejungle.destiny.server.shared.configuration.impl.RegularExpressionConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ReporterComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ActivityJournalSettingConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.ResourceAttributeConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.TrustedDomainsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.UserAccessConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.impl.UserRepositoryConfigurationDO;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.impl.DCCServerEventImpl;
import com.bluejungle.destiny.server.shared.registration.DMSRegistrationResult;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatCookie;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatResponse;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatCookieImpl;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatResponseImpl;
import com.bluejungle.destiny.server.shared.registration.impl.DCCRegistrationStatusImpl;
import com.bluejungle.destiny.server.shared.registration.impl.EventRegistrationInfoImpl;
import com.bluejungle.destiny.services.dcsf.types.DestinyEvent;
import com.bluejungle.destiny.services.dcsf.types.DestinyEventProperty;
import com.bluejungle.destiny.services.dcsf.types.DestinyEventPropertyList;
import com.bluejungle.destiny.services.management.types.ActionConfig;
import com.bluejungle.destiny.services.management.types.ActionListConfig;
import com.bluejungle.destiny.services.management.types.ApplicationResourceList;
import com.bluejungle.destiny.services.management.types.ApplicationUserConfiguration;
import com.bluejungle.destiny.services.management.types.ArchiveOperation;
import com.bluejungle.destiny.services.management.types.AuthenticatorConfiguration;
import com.bluejungle.destiny.services.management.types.ComponentHeartbeatInfo;
import com.bluejungle.destiny.services.management.types.ComponentHeartbeatUpdate;
import com.bluejungle.destiny.services.management.types.ConnectionPoolConfiguration;
import com.bluejungle.destiny.services.management.types.ConnectionPoolConfigurationList;
import com.bluejungle.destiny.services.management.types.Cookie;
import com.bluejungle.destiny.services.management.types.CustomObligation;
import com.bluejungle.destiny.services.management.types.CustomObligationArgument;
import com.bluejungle.destiny.services.management.types.CustomObligationArguments;
import com.bluejungle.destiny.services.management.types.CustomObligationArgumentValue;
import com.bluejungle.destiny.services.management.types.CustomObligations;
import com.bluejungle.destiny.services.management.types.DABSConfiguration;
import com.bluejungle.destiny.services.management.types.DACConfiguration;
import com.bluejungle.destiny.services.management.types.DCCConfiguration;
import com.bluejungle.destiny.services.management.types.DCCRegistrationInformation;
import com.bluejungle.destiny.services.management.types.DCCRegistrationStatus;
import com.bluejungle.destiny.services.management.types.DCSFConfiguration;
import com.bluejungle.destiny.services.management.types.DEMConfiguration;
import com.bluejungle.destiny.services.management.types.DMSConfiguration;
import com.bluejungle.destiny.services.management.types.DMSRegistrationOutcome;
import com.bluejungle.destiny.services.management.types.DPSConfiguration;
import com.bluejungle.destiny.services.management.types.EventRegistrationInfo;
import com.bluejungle.destiny.services.management.types.ExternalDomainConfiguration;
import com.bluejungle.destiny.services.management.types.FileSystemLogConfiguration;
import com.bluejungle.destiny.services.management.types.GenericConfiguration;
import com.bluejungle.destiny.services.management.types.IndexesRebuildOperation;
import com.bluejungle.destiny.services.management.types.MessageHandler;
import com.bluejungle.destiny.services.management.types.MessageHandlers;
import com.bluejungle.destiny.services.management.types.MgmtConsoleConfiguration;
import com.bluejungle.destiny.services.management.types.Property;
import com.bluejungle.destiny.services.management.types.RegularExpressions;
import com.bluejungle.destiny.services.management.types.RegularExpressionDef;
import com.bluejungle.destiny.services.management.types.ReporterConfiguration;
import com.bluejungle.destiny.services.management.types.ActivityJournalSettingConfiguration;
import com.bluejungle.destiny.services.management.types.RepositoryConfiguration;
import com.bluejungle.destiny.services.management.types.RepositoryConfigurationList;
import com.bluejungle.destiny.services.management.types.ResourceAttributeDef;
import com.bluejungle.destiny.services.management.types.SyncOperation;
import com.bluejungle.destiny.services.management.types.TrustedDomainsConfiguration;
import com.bluejungle.destiny.services.management.types.UserAccessConfiguration;
import com.bluejungle.destiny.services.management.types.UserRepositoryConfiguration;
import com.bluejungle.destiny.types.shared_folder.SharedFolderDataCookie;
import com.bluejungle.versionutil.VersionUtil;

/**
 * This utility class converts the web service representation of the server
 * configuration objects into an internal representation that is not relying on
 * Axis.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/src/java/main/com/bluejungle/destiny/container/dcsf/WebServiceHelper.java#1 $
 */

public class WebServiceHelper {

    private static final Map<DMSRegistrationOutcome, DMSRegistrationResult> DMS_REG_OUTCOME_TO_RESULT =
        new HashMap<DMSRegistrationOutcome, DMSRegistrationResult>();

    static {
        DMS_REG_OUTCOME_TO_RESULT.put(DMSRegistrationOutcome.Failed, DMSRegistrationResult.FAILURE);
        DMS_REG_OUTCOME_TO_RESULT.put(DMSRegistrationOutcome.Pending, DMSRegistrationResult.PENDING);
        DMS_REG_OUTCOME_TO_RESULT.put(DMSRegistrationOutcome.Success, DMSRegistrationResult.SUCCESS);
    }

    /**
     * Converts the web service app user configuration to the internal type
     * 
     * @param wsConfig
     *            web service app user configuration
     * @return internal application user configuration
     */
    public static IApplicationUserConfigurationDO convertAppUserConfiguration(ApplicationUserConfiguration wsConfig) {
        final ApplicationUserConfigurationDO result = new ApplicationUserConfigurationDO();
        result.setAuthenticationMode(wsConfig.getAuthenticationMode().getValue());
        result.setUserRepositoryConfiguration(convertUserRepositoryConfig(wsConfig.getUserRepositoryConfiguration()));
        result.setExternalDomainConfiguration(convertExternalDomainConfiguration(wsConfig.getExternalDomainConfiguration()));
        return result;
    }

    protected static void convertDCCConfiguration(DCCConfiguration wsConfig, DCCComponentConfigurationDO config) {
        config.setHeartbeatInterval(new Integer(wsConfig.getHeartbeatRate().intValue()));
        
        // BaseConfiguration
        config.setProperties(convertPropertyList(wsConfig.getProperties()));
    }
    
    protected static RegularExpressionConfigurationDO[] convertRegularExpressionConfigurationDO(RegularExpressions wsConfig) {
        RegularExpressionDef[] regexps = wsConfig.getRegexp();

        if (regexps == null || regexps.length == 0) {
            return null;
        } else {
            RegularExpressionConfigurationDO[] results = new RegularExpressionConfigurationDO[regexps.length];
            
            for (int i = 0; i != regexps.length; i++) {
                results[i] = new RegularExpressionConfigurationDO(regexps[i].getName(), regexps[i].getValue());
            }

            return results;
        }
    }


    protected static FileSystemLogConfigurationDO convertFileSystemLogConfigurationDO(FileSystemLogConfiguration wsconfig) {
        FileSystemLogConfigurationDO result = new FileSystemLogConfigurationDO();
        PositiveInteger tempPI;
        NonNegativeInteger tempNI;
        if ((tempPI = wsconfig.getThreadPoolMaximumSize()) != null) {
            result.setThreadPoolMaximumSize(tempPI.intValue());
        }
         
        if ((tempNI = wsconfig.getThreadPoolKeepAliveTime()) != null) {
            result.setThreadPoolKeepAliveTime(tempNI.intValue());
        }
        
        if ((tempNI = wsconfig.getLogInsertTaskIdleTime()) != null) {
            result.setLogInsertTaskIdleTime(tempNI.intValue());
        }
        
        if ((tempNI = wsconfig.getLogTimeout()) != null) {
            result.setLogTimeout(tempNI.intValue());
        }
        
        if ((tempNI = wsconfig.getTimeoutCheckerFrequency()) != null) {
            result.setTimeoutCheckerFrequency(tempNI.intValue());
        }
        
        if ((tempPI = wsconfig.getQueueManagerUploadSize()) != null) {
            result.setQueueManagerUploadSize(tempPI.longValue());
        }
        
        if ((tempPI = wsconfig.getMaxHandleFileSizePerThread()) != null) {
            result.setMaxHandleFileSizePerThread(tempPI.longValue());
        }
        return result;
    }

    protected static TrustedDomainsConfigurationDO convertTrustedDomainsConfigurationDO(TrustedDomainsConfiguration wsConfig) {
        TrustedDomainsConfigurationDO result = new TrustedDomainsConfigurationDO();
        String[] data = wsConfig.getMutuallyTrusted();
        if (data != null) {
            for ( int i = 0 ; i != data.length ; i++ ) {
                result.addMutuallyTrusted(data[i]);
            }
        }
        return result;
    }

    protected static List<ICustomObligationArgumentDO> convertCustomObligationArguments(CustomObligationArguments args) {
        if (args == null || args.getArgument() == null) {
            return Collections.emptyList();
        }

        List<ICustomObligationArgumentDO> newArgs = new ArrayList<ICustomObligationArgumentDO>();
        int numArguments = args.getArgument().length;
        for (int i = 0; i < numArguments; i++) {
            CustomObligationArgument arg = args.getArgument(i);

            String defaultValue = null;
            List<String> newValues = new ArrayList<String>();
            CustomObligationArgumentValue[] valueList = arg.getValue();

            if (valueList != null) {
                for (int j = 0; j < valueList.length; j++) {
                    String val = valueList[j].get_value();
                    newValues.add(val);
                    
                    // Take first value marked as default to be default
                    if (valueList[j].is_default() && defaultValue == null) {
                        defaultValue = val;
                    }
                }
            }

            newArgs.add(new CustomObligationArgumentDO(arg.getName(), newValues, defaultValue, arg.isUsereditable(), arg.isHidden()));
        }
        return newArgs;
    }

    protected static ICustomObligationConfigurationDO convertSingleCustomObligation(CustomObligation conf) {
        CustomObligationConfigurationDO obl = new CustomObligationConfigurationDO();

        obl.setDisplayName(conf.getDisplayName());
        obl.setRunAt(conf.getRunAt().getValue());
        obl.setRunBy(conf.getRunBy().getValue());
        
        String execPath = conf.getExecPath();
        if (execPath == null || execPath.equals("")) {
            obl.setInvocationString(conf.getName());
        } else {
            obl.setInvocationString(execPath);
        }

        obl.setArguments(convertCustomObligationArguments(conf.getArguments()));
        return obl;
    }

    protected static ICustomObligationsConfigurationDO convertCustomObligationConfiguration(CustomObligations conf) {
        CustomObligation[] obligations = conf.getObligation();
        ICustomObligationsConfigurationDO convertedConfiguration = new CustomObligationsConfigurationDO();

        if (obligations != null) {
            for (int i = 0; i != obligations.length; i++) {
                convertedConfiguration.addCustomObligation(convertSingleCustomObligation(obligations[i]));
            }
        }

        return convertedConfiguration;
    }

    protected static IDABSComponentConfigurationDO convertDABSConfiguration(DABSConfiguration wsConfig) {
        DABSComponentConfigurationDO result = new DABSComponentConfigurationDO();
        convertDCCConfiguration(wsConfig, result);
        result.setTrustedDomainsConfiguration(convertTrustedDomainsConfigurationDO(wsConfig.getTrustedDomainsConfiguration()));
        result.setFileSystemLogConfiguration(convertFileSystemLogConfigurationDO(wsConfig.getFileSystemLogConfiguration()));
        result.setRegularExpressionsConfiguration(convertRegularExpressionConfigurationDO(wsConfig.getRegexps()));
        return result;
    }

    protected static IDACComponentConfigurationDO convertDACConfiguration(DACConfiguration wsConfig) {
        DACComponentConfigurationDO result = new DACComponentConfigurationDO();
        convertDCCConfiguration(wsConfig, result);
        ActivityJournalSettingConfigurationDO reporterSyncDo = new ActivityJournalSettingConfigurationDO();
		ActivityJournalSettingConfiguration reporterSync = wsConfig.getActivityJournalSettingConfiguration();
		String tempStr;
		NonNegativeInteger tempNni;
		
		
		//set sync operation
		SyncOperation syncOperation = reporterSync.getSyncOperation();
        if ((tempNni = syncOperation.getTimeInterval()) != null) {
            reporterSyncDo.setSyncTimeInterval(tempNni.longValue());
        }
		if ((tempStr = syncOperation.getTimeOfDay()) != null) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(Long.parseLong(tempStr));
			reporterSyncDo.setSyncTimeOfDay(c);
		}
		reporterSyncDo.setSyncTimeoutInMinutes(syncOperation.getTimeoutInMinutes());
		
		//set indexes rebuild operation
		IndexesRebuildOperation indexesRebuildOperation = reporterSync.getIndexesRebuildOperation();
		if ((tempStr = indexesRebuildOperation.getTimeOfDay()) != null) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(Long.parseLong(tempStr));
			reporterSyncDo.setIndexRebuildTimeOfDay(c);
		}
		if (indexesRebuildOperation.getDaysOfWeek() != null) {
			String[] daysOfWeek = indexesRebuildOperation.getDaysOfWeek().getDayOfWeek();
			reporterSyncDo.setIndexRebuildDaysOfWeek(convertDaysOfWeek(daysOfWeek));
		}
		if (indexesRebuildOperation.getDaysOfMonth() != null) {
			byte[] daysOfMonth = indexesRebuildOperation.getDaysOfMonth().getDayOfMonth();
			reporterSyncDo.setIndexRebuildDaysOfMonth(convertDaysOfMonth(daysOfMonth));
		}
		reporterSyncDo.setIndexRebuildAutoRebuildIndexes(indexesRebuildOperation.isAutoRebuildIndexes());
		reporterSyncDo.setIndexRebuildTimeoutInMinutes(indexesRebuildOperation.getTimeoutInMinutes());
		
		//set archive operation
		ArchiveOperation archiveOperation = reporterSync.getArchiveOperation();
		if ((tempStr = archiveOperation.getTimeOfDay()) != null) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(Long.parseLong(tempStr));
			reporterSyncDo.setArchiveTimeOfDay(c);
		}
		if (archiveOperation.getDaysOfWeek() != null) {
			String[] daysOfWeek = archiveOperation.getDaysOfWeek().getDayOfWeek();
			reporterSyncDo.setArchiveDaysOfWeek(convertDaysOfWeek(daysOfWeek));
		}
		if (archiveOperation.getDaysOfMonth() != null) {
			byte[] daysOfMonth = archiveOperation.getDaysOfMonth().getDayOfMonth();
			reporterSyncDo.setArchiveDaysOfMonth(convertDaysOfMonth(daysOfMonth));
		}
		reporterSyncDo.setArchiveDaysOfDataToKeep(archiveOperation.getDaysOfDataToKeep());
		reporterSyncDo.setArchiveAutoArchive(archiveOperation.isAutoArchive());
		reporterSyncDo.setArchiveTimeoutInMinutes(archiveOperation.getTimeoutInMinutes());
        
		result.setActivityJournalSettingConfiguration(reporterSyncDo);
        return result;
    }
    
    private static DaysOfWeekDO convertDaysOfWeek(String[] daysOfWeek) {
    	DaysOfWeekDO daysOfWeekDO = new DaysOfWeekDO();
    	BitSet bits = new BitSet(7);
        for (String dayOfWeek : daysOfWeek) {
            int v = Integer.parseInt(dayOfWeek);
            bits.set(v);
        }
        daysOfWeekDO.setDaysOfWeek(bits);
        return daysOfWeekDO;
    }

    private static DaysOfMonthDO convertDaysOfMonth(byte[] daysOfMonth) {
        DaysOfMonthDO daysOfMonthDO = new DaysOfMonthDO();
        BitSet bits = new BitSet(31);
        for (byte dayOfMonth : daysOfMonth) {
            bits.set(dayOfMonth);
        }
        daysOfMonthDO.setDaysOfMonth(bits);
        return daysOfMonthDO;
    }
    
    /**
     * Converts the DCC registration status web service type to its internal
     * type
     * 
     * @param wsStatus
     *            web service object to convert
     * @return the internal representation of the DCC registration status
     */
    public static IDCCRegistrationStatus convertDCCRegistrationStatus(DCCRegistrationStatus wsStatus) {
        DCCRegistrationStatusImpl result = new DCCRegistrationStatusImpl(
                convertAppUserConfiguration(wsStatus.getApplicationUserConfiguration()),
                convertMessageHandlersConfigurationDO(wsStatus.getMessageHandlers()),                 
                convertCustomObligationConfiguration(wsStatus.getCustomObligationConfiguration()),
                convertActionListConfig(wsStatus.getActionListConfig()),
                convertDCCComponentConfiguration(wsStatus.getConfiguration()),
                convertRepositoryConfigurationList(wsStatus.getRepositories()),
                convertDMSRegistrationResult(wsStatus.getResult())
        );
        return result;
    }

    /**
     * Converts the DCSF web service type configuration into its internal type
     * 
     * @param wsConfig
     *            web service object to convert
     * @return the corresponding internal type
     */
    protected static IDCSFComponentConfigurationDO convertDCSFConfiguration(DCSFConfiguration wsConfig) {
        DCSFComponentConfigurationDO result = new DCSFComponentConfigurationDO();
        convertDCCConfiguration(wsConfig, result);
        return result;
    }

    /**
     * Converts the DEM web service type configuration into its internal type
     * 
     * @param wsConfig
     *            web service object to convert
     * @return the corresponding internal type
     */
    protected static IDEMComponentConfigurationDO convertDEMConfiguration(DEMConfiguration wsConfig) {
        DEMComponentConfigurationDO result = new DEMComponentConfigurationDO();
        convertDCCConfiguration(wsConfig, result);
        PositiveInteger temp = wsConfig.getReporterCacheRefreshRate();
        if(temp != null){
        	result.setReporterCacheRefreshRate(temp.intValue());
        }
        
        return result;
    }

    /**
     * Converts the DMS web service type configuration into its internal type
     * 
     * @param wsConfig
     *            web service object to convert
     * @return the corresponding internal type
     */
    protected static IDMSComponentConfigurationDO convertDMSConfiguration(DMSConfiguration wsConfig) {
        DMSComponentConfigurationDO result = new DMSComponentConfigurationDO();
        convertDCCConfiguration(wsConfig, result);
        return result;
    }

    /**
     * Converts the DMS registration result web service object to its internal
     * type
     * 
     * @param wsResult
     *            web service object to convert
     * @return the internal representation of the DMS registration result.
     */
    protected static DMSRegistrationResult convertDMSRegistrationResult(DMSRegistrationOutcome wsResult) {
        return DMS_REG_OUTCOME_TO_RESULT.get(wsResult);
    }

    /**
     * Converts the DPS web service type configuration into its internal type
     * 
     * @param wsConfig
     *            web service object to convert
     * @return the corresponding internal type
     */
    protected static IDPSComponentConfigurationDO convertDPSConfiguration(DPSConfiguration wsConfig) {
        DPSComponentConfigurationDO result = new DPSComponentConfigurationDO();
        convertDCCConfiguration(wsConfig, result);
        result.setDeploymentTime(wsConfig.getDeploymentTime());
        result.setLifecycleManagerGraceWindow(wsConfig.getLifecycleManagerGraceWindow().intValue());

        ResourceAttributeDef[] attrs = wsConfig.getCustomAttributes().getResourceAttribute();
        if (attrs != null) {
            for (int i = 0 ; i != attrs.length ; i++) {
                result.addCustomResourceAttribute(
                    new ResourceAttributeConfigurationDO(
                        attrs[i].getGroup()
                    ,   attrs[i].getDisplayName()
                    ,   attrs[i].getName()
                    ,   attrs[i].getType()
                    ,   attrs[i].getAttribute()
                    ,   attrs[i].getValue()
                    )
                );
            }
        }
        return result;
    }

    /**
     * Converts the internal hearbeat info to its web service type
     * 
     * @param info
     *            internal heartbeat info to be converted
     * @return the converted web service type
     */
    public static ComponentHeartbeatInfo convertComponentHeartbeatInfo(IComponentHeartbeatInfo info) {
        ComponentHeartbeatInfo result = new ComponentHeartbeatInfo();
        result.setCompName(info.getComponentName());
        result.setCompType(info.getComponentType().getName());
        IComponentHeartbeatCookie cookie = info.getHeartbeatCookie();
        if (cookie != null) {
            result.setLastReceivedCookie(new Cookie(info.getHeartbeatCookie().getUpdateTimestamp()));
        }
        
        ISharedFolderCookie sharedFolderCookie = info.getSharedFolderCookie();
        if (sharedFolderCookie != null) {
            result.setSharedFolderDataCookie(new SharedFolderDataCookie(info.getSharedFolderCookie().getTimestamp()));
        }
        return result;
    }

    /**
     * Converts the component heartbeat response from a web service type to the
     * internal type
     * 
     * @param wsResponse
     *            web service object to convert
     * @return the internal type
     */
    public static IComponentHeartbeatResponse convertComponentHeartbeatResponse(ComponentHeartbeatUpdate wsResponse) {
        ComponentHeartbeatResponseImpl result = new ComponentHeartbeatResponseImpl();
        result.setConfiguration(null); //No configuration update for now.
        Cookie wsCookie = wsResponse.getCookie();
        if (wsCookie != null) {
            result.setCookie(new ComponentHeartbeatCookieImpl(wsResponse.getCookie().getUpdateTimestamp()));
        }
        result.setSharedFolderData(SharedFolderWebServiceHelper.getInstance().convertToSharedFolderData(wsResponse.getSharedFolderData()));
        EventRegistrationInfo[] eventRegistrations = wsResponse.getEventRegistrations();
        if (eventRegistrations != null) {
            for (int i=0; i<eventRegistrations.length; i++) {
                result.addEventRegistrationInfo(convertEventRegistrationInfo(eventRegistrations[i]));
            }
        }
        
        return result;
    }
    
    /**
     * Converts the Mgmt Console web service type configuration into its
     * internal type
     * 
     * @param wsConfig
     *            web service object to convert
     * @return the corresponding internal type
     */
    protected static IMgmtConsoleComponentConfigurationDO convertMgmtConsoleConfiguration(MgmtConsoleConfiguration wsConfig) {
        MgmtConsoleComponentConfigurationDO result = new MgmtConsoleComponentConfigurationDO();
        convertDCCConfiguration(wsConfig, result);
        return result;
    }

    /**
     * Converts the Reporter web service type configuration into its internal
     * type
     * 
     * @param wsConfig
     *            web service object to convert
     * @return the corresponding internal type
     */
    protected static IReporterComponentConfigurationDO convertReporterConfiguration(ReporterConfiguration wsConfig) {
        ReporterComponentConfigurationDO result = new ReporterComponentConfigurationDO();
        convertDCCConfiguration(wsConfig, result);
        result.setShowSharePointReports(wsConfig.getShowSharePointReports().intValue());
        
        NonNegativeInteger tempNI;
        if ((tempNI = wsConfig.getReportGenerationFrequency()) != null) {
			result.setReportGenerationFrequency(tempNI.longValue());
		}
        
        return result;
    }
    
    protected static IGenericComponentConfigurationDO convertGenericConfiguration(GenericConfiguration wsConfig) {
        GenericComponentConfigurationDO result = new GenericComponentConfigurationDO();
        convertDCCConfiguration(wsConfig, result);
        result.setName(wsConfig.getName());
        
        return result;
    }

    /**
     * Converts the ActionList web service type configuration into its internal type
     * 
     * @param wsConfig
     *            web service object to convert
     * @return the corresponding internal type
     */
    protected static IActionListConfigDO convertActionListConfig(ActionListConfig wsConfig) {
    	ActionListConfigDO result = new ActionListConfigDO();
    	ActionConfig[] actions;
    	
    	if (wsConfig != null) {
    		actions = wsConfig.getAction();
    	
    		if (actions != null) {
    			if (actions.length > 0) {
    				ActionConfigDO[] actionConfigDOArray = new ActionConfigDO[actions.length];
    				for (int i = 0 ; i != actions.length ; i++) {
    					actionConfigDOArray[i] = new ActionConfigDO();
    					actionConfigDOArray[i].setName(actions[i].getName());
    					actionConfigDOArray[i].setDisplayName(actions[i].getDisplayName());
    					actionConfigDOArray[i].setShortName(actions[i].getShortName());
    					actionConfigDOArray[i].setCategory(actions[i].getCategory());
    				}
    				result.setActions(actionConfigDOArray);
    			}
    		}
    	}

        return result;
    }
    
    /**
     * Converts the DCC configuration web service type into its internal type
     * 
     * @param wsConfig
     *            web service object to convert
     * @return the corresponding internal type
     */
    public static IDCCComponentConfigurationDO convertDCCComponentConfiguration(DCCConfiguration wsConfig) {
        IDCCComponentConfigurationDO result;
        if (wsConfig instanceof DABSConfiguration) {
            result = convertDABSConfiguration((DABSConfiguration) wsConfig);
        } else if (wsConfig instanceof DACConfiguration) {
            result = convertDACConfiguration((DACConfiguration) wsConfig);
        } else if (wsConfig instanceof DCSFConfiguration) {
            result = convertDCSFConfiguration((DCSFConfiguration) wsConfig);
        } else if (wsConfig instanceof DEMConfiguration) {
            result = convertDEMConfiguration((DEMConfiguration) wsConfig);
        } else if (wsConfig instanceof DMSConfiguration) {
            result = convertDMSConfiguration((DMSConfiguration) wsConfig);
        } else if (wsConfig instanceof DPSConfiguration) {
            result = convertDPSConfiguration((DPSConfiguration) wsConfig);
        } else if (wsConfig instanceof MgmtConsoleConfiguration) {
            result = convertMgmtConsoleConfiguration((MgmtConsoleConfiguration) wsConfig);
        } else if (wsConfig instanceof ReporterConfiguration) {
            result = convertReporterConfiguration((ReporterConfiguration) wsConfig);
//        } else if (wsConfig instanceof GenericConfiguration ) {
        } else if (wsConfig instanceof GenericConfiguration) {
            result = convertGenericConfiguration((GenericConfiguration) wsConfig);
        } else {
            throw new IllegalArgumentException("Invalid DCC configuration type");
        }
        return result;
    }

    /**
     * Converts the external domain configuration web service object to its
     * internal type
     * 
     * @param wsConfig
     *            web service object
     * @return the converted web service object to its internal type
     */
    protected static IExternalDomainConfigurationDO convertExternalDomainConfiguration(ExternalDomainConfiguration wsConfig) {
        ExternalDomainConfigurationDO result = null;
        if (wsConfig!=null) {
	        result = new ExternalDomainConfigurationDO();
	        result.setDomainName(wsConfig.getDomainName());
	        result.setAuthenticatorConfiguration(convertAuthenticatorConfiguration(wsConfig.getAuthenticatorConfiguration()));
	        result.setUserAccessConfiguration(convertUserAccessConfiguration(wsConfig.getUserAccessConfiguration()));
        }
        return result;
    }

    /**
     * Converts the web service authentication configuration object to its
     * internal type
     * 
     * @param wsConfig
     *            web service configuration object
     * @return the converted web service object to its internal type
     */
    protected static IAuthenticatorConfigurationDO convertAuthenticatorConfiguration(AuthenticatorConfiguration wsConfig) {
        AuthenticatorConfigurationDO result = new AuthenticatorConfigurationDO();
        result.setAuthenticatorClassName(wsConfig.getAuthenticatorClassName());
        result.setProperties(convertPropertyList(wsConfig.getProperties()));
        return result;
    }

    /**
     * Converts user access configuration web service to its internal type
     * 
     * @param wsConfig
     *            web service object
     * @return the converted web service object to its internal type
     */
    protected static IUserAccessConfigurationDO convertUserAccessConfiguration(UserAccessConfiguration wsConfig) {
        UserAccessConfigurationDO result = new UserAccessConfigurationDO();
        result.setProperties(convertPropertyList(wsConfig.getProperties()));
        result.setProviderClassName(wsConfig.getUserAccessProviderClassName());
        return result;
    }

    /**
     * Converts the web service user repository configuration to its internal
     * type
     * 
     * @param wsConfig
     *            web service user repository configuration
     * @return the web service user repository configuration converted to its
     *         internal type
     */
    protected static IUserRepositoryConfigurationDO convertUserRepositoryConfig(UserRepositoryConfiguration wsConfig) {
        UserRepositoryConfigurationDO result = new UserRepositoryConfigurationDO();
        result.setProviderClassName(wsConfig.getProviderClassName());
        result.setProperties(convertPropertyList(wsConfig.getProperties()));
        return result;
    }

    /**
     * Converts the property list from the web service type to the internal type
     * 
     * @param wsList
     *            web service property list
     * @return the internal type property list
     */
    protected static PropertyList convertPropertyList(com.bluejungle.destiny.services.management.types.PropertyList wsList) {
        PropertyList result = new PropertyList();
        if (wsList != null) {
            Property[] wsPropList = wsList.getProperty();
            if (wsPropList != null) {
                int size = wsPropList.length;
                for (int i = 0; i < size; i++) {
                    Property wsProp = wsPropList[i];
                    PropertyDO prop = new PropertyDO();
                    prop.setName(wsProp.getName());
                    prop.setValue(wsProp.getValue());
                    result.addProperty(prop);
                }
            }
        }
        return result;
    }

    /**
     * Convert the repository configuration list web service object into its
     * internal type
     * 
     * @param wsConfig
     *            web service object to convert
     * @return the internal representation of the repository configuration list
     */
    public static com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList convertRepositoryConfigurationList(RepositoryConfigurationList wsConfig) {
        com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList result = new com.bluejungle.destiny.server.shared.configuration.impl.RepositoryConfigurationList();

        //Process the connection pools
        ConnectionPoolConfigurationList wsConfigPools = wsConfig.getConnectionPools();
        if (wsConfigPools != null) {
            ConnectionPoolConfiguration[] wsConnectionPoolConfigs = wsConfigPools.getConnectionPool();
            int wsConfigPoolSize = wsConnectionPoolConfigs.length;
            for (int i = 0; i < wsConfigPoolSize; i++) {
                result.addConnectionPool(convertConnectionPoolConfiguration(wsConnectionPoolConfigs[i]));
            }
        }

        //Process the repositories
        RepositoryConfiguration[] wsRepositoryConfigs = wsConfig.getRepository();
        if (wsRepositoryConfigs != null) {
            int wsRepositoryConfigSize = wsRepositoryConfigs.length;
            for (int i = 0; i < wsRepositoryConfigSize; i++) {
                result.addRepository(convertRepositoryConfiguration(wsRepositoryConfigs[i]));
            }
        }

        return result;
    }

    /**
     * Converts a connection pool configuration web service object into its
     * internal type
     * 
     * @param wsConfig
     *            connection pool configuration web service object
     * @return the internal representation of the connection pool configuration.
     */
    protected static ConnectionPoolConfigurationDO convertConnectionPoolConfiguration(ConnectionPoolConfiguration wsConfig) {
        ConnectionPoolConfigurationDO result = new ConnectionPoolConfigurationDO();
        result.setJDBCConnectString(wsConfig.getConnectString().toString());
        result.setDriverClassName(wsConfig.getDriverClassName());
        result.setMaxPoolSize(new Integer(wsConfig.getMaxPoolSize().intValue()));
        result.setName(wsConfig.getName());
        result.setPassword(wsConfig.getPassword());
        result.setProperties(convertPropertyList(wsConfig.getProperties()));
        result.setUserName(wsConfig.getUsername());
        return result;
    }

    /**
     * Converts a DCC registration info internal type to a web service type
     * 
     * @param info
     *            internal DMS registration information
     * @return the converted web service object
     */
    public static DCCRegistrationInformation convertRegistatrationInfoToWSType(IDCCRegistrationInfo info) {
        DCCRegistrationInformation result = new DCCRegistrationInformation();
        result.setComponentName(info.getComponentName());
        result.setComponentType(info.getComponentType().getName());
        result.setComponentTypeDisplayName(info.getComponentTypeDisplayName());
        result.setVersion(VersionUtil.convertIVersionToWSVersion(info.getComponentVersion()));
        Set<String> appResouces = info.getApplicationResources();
        ApplicationResourceList applicationResourceList = new ApplicationResourceList(
                appResouces == null 
                    ? null 
                    : appResouces.toArray(new String[appResouces.size()]));
        result.setApplicationResources(applicationResourceList);
        try {
            result.setComponentURL(new URI(info.getComponentURL().toString()));
            result.setEventListenerURL(new URI(info.getEventListenerURL().toString()));
        } catch (MalformedURIException e) {
            throw new IllegalArgumentException("Illegal URL used in DMS registration");
        }
        return result;

    }

    /**
     * Converts the repository configuration web service object to its internal
     * representation
     * 
     * @param wsConfig
     *            the web service object to convert
     * @return the internal representation of the web service object
     */
    protected static RepositoryConfigurationDO convertRepositoryConfiguration(RepositoryConfiguration wsConfig) {
        RepositoryConfigurationDO result = new RepositoryConfigurationDO();
        result.setConnectionPoolConfiguration(convertConnectionPoolConfiguration(wsConfig.getConnectionPoolConfiguration()));
        result.setConnectionPoolName(wsConfig.getConnectionPoolName());
        result.setName(wsConfig.getName());
        result.setProperties(convertPropertyList(wsConfig.getProperties()));
        return result;
    }

    /**
     * Returns an internal event object based on a web service event object
     * 
     * @param wsEvent
     *            web service event object
     * @return the corresponding internal event object
     */
    public static IDCCServerEvent convertToDCCServerEvent(DestinyEvent wsEvent) {
        DCCServerEventImpl result = new DCCServerEventImpl(wsEvent.getEventName());
        DestinyEventPropertyList wsPropList = wsEvent.getProperties();
        if (wsPropList != null) {
            DestinyEventProperty[] wsPropArray = wsPropList.getProperties();
            if (wsPropArray != null) {
                int size = wsPropArray.length;
                for (int i = 0; i < size; i++) {
                    DestinyEventProperty wsProp = wsPropArray[i];
                    result.getProperties().setProperty(wsProp.getName(), wsProp.getValue());
                }
            }
        }
        return result;
    }

    /**
     * Converts the internal event object to a web service event object
     * 
     * @param event
     *            internal event object
     * @return the corresponding web service object
     */
    public static DestinyEvent convertDCCServerEventToServiceType(IDCCServerEvent event) {
        DestinyEvent wsResult = new DestinyEvent();
        wsResult.setEventName(event.getName());
        final int size = event.getProperties().size();
        if (size > 0) {
            final DestinyEventPropertyList wsPropList = new DestinyEventPropertyList();
            final DestinyEventProperty[] wsPropArray = new DestinyEventProperty[size];
            Enumeration<?> propEnum = event.getProperties().propertyNames();
            int i = 0;
            while (propEnum.hasMoreElements()) {
                final String propName = (String) propEnum.nextElement();
                final String propValue = event.getProperties().getProperty(propName);
                DestinyEventProperty wsProp = new DestinyEventProperty();
                wsProp.setName(propName);
                wsProp.setValue(propValue);
                wsPropArray[i] = wsProp;
                i++;
            }
            wsPropList.setProperties(wsPropArray);
            wsResult.setProperties(wsPropList);
        }
        return wsResult;
    }
    
    private static IEventRegistrationInfo convertEventRegistrationInfo(EventRegistrationInfo infoToConvert) {
        IEventRegistrationInfo resultToReturn = null;
        try {
            resultToReturn = new EventRegistrationInfoImpl(infoToConvert.getEventName(), new URL(infoToConvert.getCallbackURL().toString()), infoToConvert.isActive());
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Illegal URL used in Event Registration");
        }
        
        return resultToReturn;
    }
    
    private static IMessageHandlerConfigurationDO convertMessageHandlerConfigurationDO(MessageHandler wsConfig){
        MessageHandlerConfigurationDO result = new MessageHandlerConfigurationDO();
        if (wsConfig != null) {
            result.setName(wsConfig.getName());
            result.setClassName(wsConfig.getClassName());
            result.setProperties(convertPropertyList(wsConfig.getProperties()));
        }
        return result;
    }
    
    private static IMessageHandlersConfigurationDO convertMessageHandlersConfigurationDO(MessageHandlers wsConfig){
        MessageHandlersConfigurationDO result = new MessageHandlersConfigurationDO();
        if(wsConfig != null){
            MessageHandler[] handlers = wsConfig.getMessageHandler();
            if(handlers != null){
                for(MessageHandler handler : handlers){
                    result.addMessageHandlerConfiguration(convertMessageHandlerConfigurationDO(handler));
                }
            }
        }
        return result;
    }
}
