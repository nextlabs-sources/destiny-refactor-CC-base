/*
 * Created on Feb 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms;

import com.bluejungle.destiny.container.dms.components.configmgr.service.DestinyConfigurationServiceHelper;
import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrSortFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQuerySpec;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTerm;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentQuerySpecImpl;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentSortTermImpl;
import com.bluejungle.destiny.framework.types.RelationalOpDTO;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.IDCCRegistrationStatus;
import com.bluejungle.destiny.server.shared.registration.IEventRegistrationInfo;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderAliasesAlias;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderCookie;
import com.bluejungle.destiny.server.shared.registration.ISharedFolderData;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatCookieImpl;
import com.bluejungle.destiny.server.shared.registration.impl.ComponentHeartbeatInfoImpl;
import com.bluejungle.destiny.server.shared.registration.impl.DCCRegistrationInfoImpl;
import com.bluejungle.destiny.server.shared.registration.impl.SharedFolderCookieImpl;
import com.bluejungle.destiny.services.management.types.AgentDTOQueryField;
import com.bluejungle.destiny.services.management.types.AgentDTOQuerySpec;
import com.bluejungle.destiny.services.management.types.AgentDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.AgentDTOQueryTermList;
import com.bluejungle.destiny.services.management.types.AgentDTOSortTerm;
import com.bluejungle.destiny.services.management.types.AgentDTOSortTermList;
import com.bluejungle.destiny.services.management.types.ApplicationResourceList;
import com.bluejungle.destiny.services.management.types.ComponentHeartbeatInfo;
import com.bluejungle.destiny.services.management.types.ConcreteAgentDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.Cookie;
import com.bluejungle.destiny.services.management.types.DCCRegistrationInformation;
import com.bluejungle.destiny.services.management.types.DCCRegistrationStatus;
import com.bluejungle.destiny.services.management.types.DMSRegistrationOutcome;
import com.bluejungle.destiny.services.management.types.EventRegistrationInfo;
import com.bluejungle.destiny.services.management.types.ORCompositeAgentDTOQueryTerm;
import com.bluejungle.destiny.types.shared_folder.SharedFolderAliasList;
import com.bluejungle.destiny.types.shared_folder.SharedFolderAliases;
import com.bluejungle.destiny.types.shared_folder.SharedFolderAliasesAlias;
import com.bluejungle.destiny.types.shared_folder.SharedFolderData;
import com.bluejungle.destiny.types.shared_folder.SharedFolderDataCookie;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.search.RelationalOp;
import com.bluejungle.version.IVersion;
import com.bluejungle.versionutil.VersionUtil;

import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class performs the conversion of internal types into web service types
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/WebServiceHelper.java#1 $
 */

public class WebServiceHelper {

    /**
     * Converts an agent manager query specification to its internal type.
     * 
     * @param wsQuerySpec
     *            web service query specification to convert
     * @param policyCal
     *            calendar representing the latest timestamp for a policy
     *            deployment.
     * @return the corresponding internal type
     */
    public static IAgentMgrQuerySpec convertAgentMgrQuerySpecToInternalType(AgentDTOQuerySpec wsQuerySpec, Calendar policyCal) {
        if (wsQuerySpec == null) {
            return null;
        }

        IAgentManager agentManager = getAgentManager();
        IAgentMgrQueryTermFactory queryTermFactory = agentManager.getAgentMgrQueryTermFactory();

        final AgentQuerySpecImpl result = new AgentQuerySpecImpl();
        result.setLimit(wsQuerySpec.getLimit());
        AgentDTOQueryTermList wsSearchSpecList = wsQuerySpec.getSearchSpec();
        if (wsSearchSpecList != null) {
            AgentDTOQueryTerm[] wsTerms = wsSearchSpecList.getAgentDTOQueryTerms();
            if (wsTerms != null) {
                int size = wsTerms.length;
                for (int i = 0; i < size; i++) {
                    AgentDTOQueryTerm wsTerm = wsTerms[i];
                    IAgentMgrQueryTerm termToAdd = null;
                    if (wsTerm instanceof ConcreteAgentDTOQueryTerm) {
                        termToAdd = convertConcreteQueryTermToInternalType((ConcreteAgentDTOQueryTerm) wsTerm, policyCal);
                    } else {
                        termToAdd = convertORCompositeQueryTermToInternalType((ORCompositeAgentDTOQueryTerm) wsTerm, policyCal);
                    }
                    
                    result.addSearchSpecTerm(termToAdd);
                }
            }
        }
        AgentDTOSortTermList wsSortTermList = wsQuerySpec.getSortSpec();
        if (wsSortTermList != null) {
            AgentDTOSortTerm[] wsTerms = wsSortTermList.getAgentDTOSortTerms();
            if (wsTerms != null) {
                int size = wsTerms.length;
                for (int i = 0; i < size; i++) {
                    AgentDTOSortTerm wsTerm = wsTerms[i];
                    result.addSortSpecTerm(new AgentSortTermImpl(AgentMgrSortFieldType.fromString(wsTerm.getField().getValue()), wsTerm.isAscending()));
                }
            }
        }
        return result;
    }

    /**
     * @param wsTerm
     * @param policyCal
     * @param queryTermFactory
     * @return
     */
    private static IAgentMgrQueryTerm convertConcreteQueryTermToInternalType(ConcreteAgentDTOQueryTerm concreteWSTerm, Calendar policyCal) {
        IAgentManager agentManager = getAgentManager();
        IAgentMgrQueryTermFactory queryTermFactory = agentManager.getAgentMgrQueryTermFactory();
        
        AgentDTOQueryField wsField = concreteWSTerm.getAgentDTOQueryField();
        RelationalOpDTO wsOperator = concreteWSTerm.getOperator();
        if (AgentDTOQueryField.POLICY_UP_TO_DATE.equals(wsField)) {
            concreteWSTerm = new ConcreteAgentDTOQueryTerm();
            concreteWSTerm.setAgentDTOQueryField(wsField);
            concreteWSTerm.setOperator(RelationalOpDTO.greater_than_equals);
            concreteWSTerm.setValue(new Long(policyCal.getTimeInMillis()));
        } else if (AgentDTOQueryField.TYPE.equals(wsField)) {
            concreteWSTerm.setOperator(RelationalOpDTO.equals);
            // Swap values here - watch out
            concreteWSTerm.setValue(agentManager.getAgentType(concreteWSTerm.getValue().toString()));
        }
        return queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.fromString(wsField.getValue()), RelationalOp.getRelationalOp(concreteWSTerm.getOperator().getValue()), concreteWSTerm.getValue());
    }

    /**
     * @param term
     * @param policyCal
     * @return
     */
    private static IAgentMgrQueryTerm convertORCompositeQueryTermToInternalType(ORCompositeAgentDTOQueryTerm term, Calendar policyCal) {
        IAgentManager agentManager = getAgentManager();
        IAgentMgrQueryTermFactory queryTermFactory = agentManager.getAgentMgrQueryTermFactory();
        
        Set<IAgentMgrQueryTerm> internalQueryTermsToOr = new HashSet<IAgentMgrQueryTerm>();
        
        AgentDTOQueryTermList queryTermList = term.getAgentDTOQueryTerms();
        AgentDTOQueryTerm[] queryTerms = queryTermList.getAgentDTOQueryTerms();
        for (int i = 0; i < queryTerms.length; i++) {
            AgentDTOQueryTerm nextTerm = queryTerms[i];
            IAgentMgrQueryTerm internalQueryTerm;
            if (nextTerm instanceof ORCompositeAgentDTOQueryTerm) {
                internalQueryTerm = convertORCompositeQueryTermToInternalType((ORCompositeAgentDTOQueryTerm) nextTerm, policyCal);
            } else {
                internalQueryTerm = convertConcreteQueryTermToInternalType((ConcreteAgentDTOQueryTerm) nextTerm, policyCal);
            }
            internalQueryTermsToOr.add(internalQueryTerm);
        }
        
        return queryTermFactory.getORCompositeQueryTerm(internalQueryTermsToOr);
    }
    
    /**
     * Converts the DCC registration information web service type into an
     * internal type
     * 
     * @param wsRegInfo
     *            web service DCC registration info
     * @return the internal representation of the DCC registration info
     */
    public static IDCCRegistrationInfo convertDCCRegistration(DCCRegistrationInformation wsRegInfo) {
        IDCCRegistrationInfo result = new DCCRegistrationInfoImpl();
        result.setComponentName(wsRegInfo.getComponentName());
        result.setComponentType(ServerComponentType.fromString(wsRegInfo.getComponentType()));
        result.setComponentTypeDisplayName(wsRegInfo.getComponentTypeDisplayName());
        IVersion version = VersionUtil.convertWSVersionToIVersion(wsRegInfo.getVersion());
        result.setComponentVersion(version);
        try {
            result.setComponentURL(new URL(wsRegInfo.getComponentURL().toString()));
            result.setEventListenerURL(new URL(wsRegInfo.getEventListenerURL().toString()));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL expression");
        }
        ApplicationResourceList appResourcesList = wsRegInfo.getApplicationResources();
        if (appResourcesList != null) {
            String[] appResources = appResourcesList.getResources();
            if (appResources != null) {
                Set<String> resouces = new HashSet<String>(appResources.length);
                Collections.addAll(resouces, appResources);
                result.setApplicationResources(resouces);
            }
        }
        
        return result;
    }

    /**
     * Converts the component heartbeat info to its internal type
     * 
     * @param wsInfo
     *            web service object
     * @return the converted web service object to its internal type
     */
    public static IComponentHeartbeatInfo convertComponentHeartbeatInfo(ComponentHeartbeatInfo wsInfo) {
        IComponentHeartbeatInfo result = new ComponentHeartbeatInfoImpl();
        result.setComponentName(wsInfo.getCompName());
        result.setComponentType(ServerComponentType.fromString(wsInfo.getCompType()));
        Cookie wsCookie = wsInfo.getLastReceivedCookie();
        if (wsCookie != null) {
            result.setHeartbeatCookie(new ComponentHeartbeatCookieImpl(wsCookie.getUpdateTimestamp()));
        }
        SharedFolderDataCookie wsSharedFolderCookie = wsInfo.getSharedFolderDataCookie();
        if (wsSharedFolderCookie != null) {
            result.setSharedFolderCookie(new SharedFolderCookieImpl(wsSharedFolderCookie.getTimestamp()));
        }
        return result;
    }

    public static ISharedFolderCookie convertSharedFolderCookieData(SharedFolderDataCookie wsCookie) {
        final Calendar cal;
        if (wsCookie == null) {
            cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
        } else {
            cal = wsCookie.getTimestamp();
        }
        return new SharedFolderCookieImpl(cal);
    }

    /**
     * Converts the DCC registration status from its internal type to a web
     * service type
     * 
     * @param status
     *            internal DCC registration status
     * @return the corresponding web service type
     */
    public static DCCRegistrationStatus convertDCCRegistrationStatusToServiceType(IDCCRegistrationStatus status) {
        DestinyConfigurationServiceHelper helper = DestinyConfigurationServiceHelper.getInstance();
        DCCRegistrationStatus result = new DCCRegistrationStatus();
        result.setResult(DMSRegistrationOutcome.fromString(status.getRegistrationResult().getName()));
        result.setApplicationUserConfiguration(helper.extractApplicationUserConfigurationData(status.getApplicationUserConfiguration()));
        result.setMessageHandlers(helper.extractMessageHandlersConfigurationData(status.getMessageHandlersConfiguration()));
        result.setCustomObligationConfiguration(helper.extractCustomObligationsConfigurationData(status.getCustomObligationsConfiguration()));
        result.setActionListConfig(helper.extractActionListConfigData(status.getActionListConfig()));
        result.setConfiguration(helper.extractDCCComponentConfigurationData(status.getComponentConfiguration()));
        result.setRepositories(helper.extractRepositoryList(status.getRepositoryConfigurations().getRepositoriesAsSet()));
        
        return result;
    }

    public static SharedFolderData convertFromSharedFolderData(ISharedFolderData data) {
        if (data == null) {
            return null;
        }
        SharedFolderData result = new SharedFolderData();
        result.setCookie(new SharedFolderDataCookie(data.getCookie().getTimestamp()));
        SharedFolderAliasList wsAliasList = new SharedFolderAliasList();
        ISharedFolderAlias[] sharedFolderAliases = data.getAliases();
        if (sharedFolderAliases != null) {
            int size = sharedFolderAliases.length;
            SharedFolderAliases wsSharedFolderAliases[] = new SharedFolderAliases[size];
            for (int i = 0; i < size; i++) {
                SharedFolderAliases wsSharedFolderAlias = new SharedFolderAliases();
                ISharedFolderAlias folderAlias = sharedFolderAliases[i];
                ISharedFolderAliasesAlias[] sharedFolderAliasesAliases = folderAlias.getAliases();
                if (sharedFolderAliasesAliases != null) {
                    int aliasSize = sharedFolderAliasesAliases.length;
                    SharedFolderAliasesAlias[] wsSharedFolderAliasesAliases = new SharedFolderAliasesAlias[aliasSize];
                    for (int j = 0; j < aliasSize; j++) {
                        ISharedFolderAliasesAlias currentAlias = sharedFolderAliasesAliases[j];
                        SharedFolderAliasesAlias wsAlias = new SharedFolderAliasesAlias();
                        wsAlias.setName(currentAlias.getName());
                        wsSharedFolderAliasesAliases[j] = wsAlias;
                    }
                    wsSharedFolderAlias.setAlias(wsSharedFolderAliasesAliases);
                    wsSharedFolderAliases[i] = wsSharedFolderAlias;
                }
            }
            wsAliasList.setAliases(wsSharedFolderAliases);
        }
        result.setAliasList(wsAliasList);
        return result;
    }

    public static EventRegistrationInfo[] convertEventRegistrationsInfo(IEventRegistrationInfo[] events) {
        if(events == null){
            return new EventRegistrationInfo[0];
        }
        
        EventRegistrationInfo[] wsEvents = new EventRegistrationInfo[events.length];
        for (int i = 0; i < wsEvents.length; i++) {
            IEventRegistrationInfo event = events[i];
            EventRegistrationInfo wsEvent = new EventRegistrationInfo();
            wsEvent.setActive(event.isActive());
            try {
                wsEvent.setCallbackURL(new URI(event.getCallbackURL().toString()));
            } catch (MalformedURIException e) {
                throw new IllegalArgumentException("Invalid URL");
            }
            wsEvent.setEventName(event.getName());
            wsEvents[i] = wsEvent;
        }
        return wsEvents;
    }

    /**
     * Returns an instance of the IAgentManager *
     * 
     * @return IAgentManager
     */
    private static IAgentManager getAgentManager() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        return (IAgentManager) compMgr.getComponent(IAgentManager.COMP_NAME);
    }
}
