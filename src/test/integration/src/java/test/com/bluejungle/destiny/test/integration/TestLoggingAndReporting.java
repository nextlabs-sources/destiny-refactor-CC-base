/*
 * Created on Oct 18, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.test.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.rpc.ServiceException;

import net.sf.hibernate.HibernateException;

import org.apache.axis.AxisFault;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.FileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.appframework.appsecurity.axis.SecureSessionVaultGateway;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginInfo;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginMgr;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.remote.RemoteLoginManager;
import com.bluejungle.destiny.appframework.appsecurity.test.MockSecureSessionVault;
import com.bluejungle.destiny.bindings.report.v1.ComponentLookupIFBindingStub;
import com.bluejungle.destiny.bindings.report.v1.ComponentLookupIFImplTest;
import com.bluejungle.destiny.bindings.report.v1.ReportExecutionIFBindingStub;
import com.bluejungle.destiny.bindings.report.v1.ReportLibraryIFBindingStub;
import com.bluejungle.destiny.bindings.secure_session.v1.SecureSessionServiceIFBindingStub;
import com.bluejungle.destiny.container.dac.BaseDACComponentTestCase;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportMgrImpl;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.SampleDataMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TestPolicyActivityLogEntryDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TestTrackingActivityLogEntryDO;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.destiny.interfaces.report.v1.ComponentLookupIF;
import com.bluejungle.destiny.interfaces.report.v1.ReportExecutionIF;
import com.bluejungle.destiny.interfaces.report.v1.ReportLibraryIF;
import com.bluejungle.destiny.interfaces.secure_session.v1.SecureSessionServiceIF;
import com.bluejungle.destiny.services.management.CommProfileDTOQuery;
import com.bluejungle.destiny.services.management.ProfileServiceIF;
import com.bluejungle.destiny.services.management.ProfileServiceLocator;
import com.bluejungle.destiny.services.management.types.ActivityJournalingSettingsDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTOList;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTermSet;
import com.bluejungle.destiny.types.actions.v1.ActionList;
import com.bluejungle.destiny.types.actions.v1.ActionType;
import com.bluejungle.destiny.types.basic.v1.SortDirection;
import com.bluejungle.destiny.types.basic.v1.StringList;
import com.bluejungle.destiny.types.effects.v1.EffectList;
import com.bluejungle.destiny.types.effects.v1.EffectType;
import com.bluejungle.destiny.types.report.v1.Report;
import com.bluejungle.destiny.types.report.v1.ReportSortFieldName;
import com.bluejungle.destiny.types.report.v1.ReportSortSpec;
import com.bluejungle.destiny.types.report.v1.ReportSummaryType;
import com.bluejungle.destiny.types.report.v1.ReportTargetType;
import com.bluejungle.destiny.types.report_result.v1.DetailResultList;
import com.bluejungle.destiny.types.report_result.v1.DocumentActivityDetailResult;
import com.bluejungle.destiny.types.report_result.v1.PolicyActivityDetailResult;
import com.bluejungle.destiny.types.report_result.v1.ReportDetailResult;
import com.bluejungle.destiny.types.report_result.v1.ReportState;
import com.bluejungle.domain.types.ActionTypeDTO;
import com.bluejungle.domain.types.ActionTypeDTOList;
import com.bluejungle.domain.types.AgentTypeDTO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/test/integration/src/java/test/com/bluejungle/destiny/test/integration/TestLoggingAndReporting.java#1 $
 */

public class TestLoggingAndReporting extends BaseDACComponentTestCase {

    private static final Log LOG = LogFactory.getLog(ComponentLookupIFImplTest.class.getName());
    protected SampleDataMgr sampleDataMgr = new SampleDataMgr();
    private static String CLIENT_CONFIG_FILE = "client-config.wsdd";
    private static final String SERVICE_LOCATION = "http://localhost:8081/dac/services/SecureSessionService";
    private static ProfileServiceIF dmsProfileService;
    private int heartBeatRate = 10;
    private int logRate = 65;
    private static String srcRoot = System.getProperty("src.root.dir");
    private static String cmd2Path = srcRoot + "\\etc\\cmd2";
    private static String buildRoot = System.getProperty("build.root.dir");
    private static String testFilePath = buildRoot + "\\integration-test\\reporting-test.txt";
    private static String copyFilePath = buildRoot + "\\integration-test\\file-copy.txt";
    private static String copyToFilePath = buildRoot + "\\integration-test\\file-copy-to.txt";
    private static String moveFilePath = buildRoot + "\\integration-test\\move-file.txt";
    private static String renameFilePath = buildRoot + "\\integration-test\\rename-file.txt";
    private static String renameToFilePath = buildRoot + "\\integration-test\\renameTo-file.txt";
    private static String moveToFilePath = buildRoot + "\\integration-test\\move-to\\move-file.txt";
    private static String attributeFilePath = buildRoot + "\\integration-test\\attribute.txt";
    private static String username = System.getProperty("user.name") + "@bluejungle.com";
    private static int COMM_PROFILE_UPDATE_TIME = 35000;
    private static int TRACKING_LOG_UPDATE_TIME = 30000;
    private static int POLICY_LOG_UPDATE_TIME = 20000;
 
    /**
     * Returns the data source for the activity repository
     * 
     * @return the data source for the activity repository
     */
    protected IHibernateRepository getActivityDateSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());
        return dataSource;
    }
    
    /**
     * Returns the report manager
     * 
     * @return the report manager
     */
    protected IReportMgr getReportMgr() {
        HashMapConfiguration reportMgrConfig = new HashMapConfiguration();
        ComponentInfo compInfo = new ComponentInfo(IReportMgr.COMP_NAME, ReportMgrImpl.class.getName(), IReportMgr.class.getName(), LifestyleType.SINGLETON_TYPE, reportMgrConfig);
        IReportMgr result = (IReportMgr) ComponentManagerFactory.getComponentManager().getComponent(compInfo);
        return result;
    }
    
    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Adds a few dynamic mapping to the data source, in order to insert dummy
     * records in the log table.
     * 
     * @see com.bluejungle.destiny.container.dcc.test.BaseDCCComponentTestCase#setupDataSourceDynamicMappings()
     */
    protected List setupDataSourceDynamicMappings() {
        List list = super.setupDataSourceDynamicMappings();
        if (list == null) {
            list = new ArrayList();
        }
        list.add(TestPolicyActivityLogEntryDO.class);
        list.add(TestTrackingActivityLogEntryDO.class);
        return list;
    }
    
    /**
     * Returns a new instance of a component lookup client.
     * 
     * @return a component lookup client
     */
    protected ComponentLookupIF getComponentLookup() {
        URL location = null;
        try {
            location = new URL("http://localhost:8081/dac/services/ComponentLookup");
        } catch (MalformedURLException e) {
            fail("Invalid URL for report component lookup location");
        }
        ComponentLookupIF lookup = null;
        try {
            lookup = new ComponentLookupIFBindingStub(location, getNewService());
        } catch (AxisFault e1) {
            fail("No axis fault should be thrown when creating report component lookup service : " + e1.getLocalizedMessage());
        }
        return lookup;
    }

    /**
     * Returns a new service client object
     * 
     * @return a new service client object
     */
    private Service getNewService() {
        return new org.apache.axis.client.Service();
    }

    /**
     * Returns a new instance of the report execution client
     * 
     * @return a report execution service client
     */
    protected ReportExecutionIF getReportExecution() {
        URL location = null;
        try {
            location = new URL("http://localhost:8081/dac/services/ReportExecution");
        } catch (MalformedURLException e) {
            fail("Invalid URL for report execution service location");
        }
        ReportExecutionIFBindingStub execution = null;
        try {
            execution = new ReportExecutionIFBindingStub(location, getNewService());
        } catch (AxisFault e1) {
            fail("No axis fault should be thrown when creating report execution service : " + e1.getLocalizedMessage());
        }
        return execution;
    }

    /**
     * Returns a new instance of the report library client
     * 
     * @return a report library service client
     */
    protected ReportLibraryIF getReportLibrary() {
        URL location = null;
        try {
            location = new URL("http://localhost:8081/dac/services/ReportLibrary");
        } catch (MalformedURLException e) {
            fail("Invalid URL for report execution library location");
        }
        ReportLibraryIF library = null;
        try {
            library = new ReportLibraryIFBindingStub(location, getNewService());
        } catch (AxisFault e1) {
            fail("No axis fault should be thrown when creating report library service : " + e1.getLocalizedMessage());
        }
        return library;
    }


    protected class MockLoginInfo implements ILoginInfo {

        private String userName;
        private String password;

        public MockLoginInfo(String name, String pass) {
            this.userName = name;
            this.password = pass;
        }

        /**
         * @see com.bluejungle.destiny.webui.framework.loginmgr.ILoginInfo#getApplicationName()
         */
        public String getApplicationName() {
            return null;
        }

        /**
         * @see com.bluejungle.destiny.webui.framework.loginmgr.ILoginInfo#getUserName()
         */
        public String getUserName() {
            return this.userName;
        }

        /**
         * @see com.bluejungle.destiny.webui.framework.loginmgr.ILoginInfo#getPassword()
         */
        public String getPassword() {
            return this.password;
        }
    }

    public static class TestRemoteLoginMgr extends RemoteLoginManager {

        /**
         * @see com.bluejungle.destiny.appframework.appsecurity.loginmgr.remote.RemoteLoginManager#getSecureSessionService(java.lang.String)
         */
        protected SecureSessionServiceIF getSecureSessionService(String serviceLocation) throws ServiceException, RemoteException {
            SecureSessionServiceIF serviceToReturn = null;

            try {
                URL location = new URL(serviceLocation);
                Service clientService = new org.apache.axis.client.Service();
                serviceToReturn = new SecureSessionServiceIFBindingStub(location, clientService);
            } catch (MalformedURLException e) {
                // Should never happen
                fail("Bad URL when authenticating user" + e.getMessage());
            }

            return serviceToReturn;
        }
    }
    
    
    /**
     * Constructor
     * 
     */
    public TestLoggingAndReporting(String testName) {
        super(testName);
    }
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        SecureSessionVaultGateway.setSecureSessionVault(new MockSecureSessionVault());

        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        //Sets the remote login manager to test
        HashMapConfiguration componentConfig = new HashMapConfiguration();
        componentConfig.setProperty(TestRemoteLoginMgr.SECURE_SESSION_SERVICE_ENDPOINT_PROP_NAME, SERVICE_LOCATION);
        ComponentInfo componentInfo = new ComponentInfo(ILoginMgr.COMP_NAME, TestRemoteLoginMgr.class.getName(), ILoginMgr.class.getName(), LifestyleType.SINGLETON_TYPE, componentConfig);
        ILoginMgr loginMgr = (ILoginMgr) compMgr.getComponent(componentInfo);
        //Simulates a user login
        loginMgr.login(new MockLoginInfo("Administrator", "123blue!"));
        
        ProfileServiceLocator profileSvcLocator = new ProfileServiceLocator();
        profileSvcLocator.setProfileServiceIFPortEndpointAddress("http://localhost:8081/dms/services/ProfileServiceIFPort");
        dmsProfileService = profileSvcLocator.getProfileServiceIFPort();
        assertNotNull("DMS Profile Service not created properly", dmsProfileService);
    }


    public void testPolicyActivityJournaling() throws ServiceNotReadyFault, RemoteException, HibernateException, InterruptedException {
        Thread.sleep(POLICY_LOG_UPDATE_TIME);
        
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setSummaryType(ReportSummaryType.None);
        newReport.setEffects(new EffectList(new EffectType[]{EffectType.allow})); 
        newReport.setActions(new ActionList(new ActionType[]{ActionType.EDIT, ActionType.OPEN})); 
        newReport.setPolicies(new StringList(new String[]{"policy2", "policy4"}));
        newReport.setSortSpec(new ReportSortSpec(ReportSortFieldName.None, SortDirection.Ascending));
        newReport.setTarget(ReportTargetType.PolicyEvents);
        newReport.setUsers(new StringList(new String[]{username}));
        
        ReportExecutionIF reportExecution = getReportExecution();
        ReportDetailResult result = (ReportDetailResult)reportExecution.executeReport(newReport, 10, -1);
        long availRowCount = result.getAvailableRowCount();
        long totalRowCount = result.getTotalRowCount();
        assertEquals("there should be two entries in the log", 2, totalRowCount);
        assertNotNull("There should be a result returned", result);
        ReportState state = result.getState();
        assertNotNull("The result should come with a state", state);
        DetailResultList resultList = result.getData();
        PolicyActivityDetailResult detailResult = (PolicyActivityDetailResult) resultList.getResults(0);
        ActionType action = detailResult.getAction();
        assertEquals("action should not be EDIT", "EDIT", action.toString());

        result = (ReportDetailResult)reportExecution.getNextResultSet(state, 15);
        assertNotNull("There should be a result returned", result);
        state = result.getState();
        assertNotNull("The result should come with a state", state);
    }
    
 
    public void testTrackOPEN() throws UnauthorizedCallerFault, RemoteException, InterruptedException, FileNotFoundException, IOException {
        CommProfileDTOQueryTerm[] queryTerms = { 
                new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop Enforcer Default Profile")
        };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        ActivityJournalingSettingsDTO customJournalSetting = comDTO.getCustomActivityJournalingSettings();
        ActionTypeDTOList customJournalSettingAction = customJournalSetting.getLoggedActivities();
        ActionTypeDTO[] customJournalAction = new ActionTypeDTO[]{ActionTypeDTO.OPEN};
        customJournalSettingAction.setAction(customJournalAction);
        comDTO.setCustomActivityJournalingSettings(customJournalSetting);
        comDTO.setCurrentActivityJournalingSettings(customJournalSetting);
        
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(COMM_PROFILE_UPDATE_TIME);
        
        File testFile = new File(testFilePath);
        FileReader fileRead = new FileReader(testFile);
        fileRead.read();
        
        Thread.sleep(TRACKING_LOG_UPDATE_TIME);
        
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setSummaryType(ReportSummaryType.None);
        //newReport.setEffects(new EffectList(new EffectType[]{EffectType.allow})); 
        newReport.setActions(new ActionList(new ActionType[]{ActionType.OPEN})); 
        newReport.setSortSpec(new ReportSortSpec(ReportSortFieldName.None, SortDirection.Ascending));
        newReport.setTarget(ReportTargetType.ActivityJournal);
        newReport.setResourceNames(new StringList(new String[]{testFilePath}));
        //newReport.setUsers(new StringList(new String[]{"rlin@bluejungle.com"}));
        
        ReportExecutionIF reportExecution = getReportExecution();
        ReportDetailResult result = (ReportDetailResult)reportExecution.executeReport(newReport, 10, -1);
        long availRowCount = result.getAvailableRowCount();
        long totalRowCount = result.getTotalRowCount();
        assertEquals("there should be one entry in the log", 1, totalRowCount);
        assertNotNull("There should be a result returned", result);
        ReportState state = result.getState();
        assertNotNull("The result should come with a state", state);
        DetailResultList resultList = result.getData();
        DocumentActivityDetailResult detailResult = (DocumentActivityDetailResult) resultList.getResults(0);
        ActionType action = detailResult.getAction();
        assertEquals("action should not be OPEN", "OPEN", action.toString());

        result = (ReportDetailResult)reportExecution.getNextResultSet(state, 15);
        assertNotNull("There should be a result returned", result);
        state = result.getState();
        assertNotNull("The result should come with a state", state);
    }
    
    public void testTrackEDIT() throws UnauthorizedCallerFault, RemoteException, InterruptedException, FileNotFoundException, IOException {
        CommProfileDTOQueryTerm[] queryTerms = { 
                new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop Enforcer Default Profile")
        };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        ActivityJournalingSettingsDTO customJournalSetting = comDTO.getCustomActivityJournalingSettings();
        ActionTypeDTOList customJournalSettingAction = customJournalSetting.getLoggedActivities();
        ActionTypeDTO[] customJournalAction = new ActionTypeDTO[]{ActionTypeDTO.EDIT};
        customJournalSettingAction.setAction(customJournalAction);
        comDTO.setCustomActivityJournalingSettings(customJournalSetting);
        comDTO.setCurrentActivityJournalingSettings(customJournalSetting);
        
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(COMM_PROFILE_UPDATE_TIME);
        
        File testFile = new File(testFilePath);
        FileWriter fileWrite = new FileWriter(testFile);
        fileWrite.write("I wrote to this file in TestLoggingAndReporting.testTrackEDIT");
        fileWrite.close();
        
        Thread.sleep(TRACKING_LOG_UPDATE_TIME);
        
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setSummaryType(ReportSummaryType.None);
        //newReport.setEffects(new EffectList(new EffectType[]{EffectType.allow})); 
        newReport.setActions(new ActionList(new ActionType[]{ActionType.EDIT})); 
        newReport.setSortSpec(new ReportSortSpec(ReportSortFieldName.None, SortDirection.Ascending));
        newReport.setTarget(ReportTargetType.ActivityJournal);
        //newReport.setUsers(new StringList(new String[]{"rlin@bluejungle.com"}));
        
        ReportExecutionIF reportExecution = getReportExecution();
        ReportDetailResult result = (ReportDetailResult)reportExecution.executeReport(newReport, 10, -1);
        long availRowCount = result.getAvailableRowCount();
        long totalRowCount = result.getTotalRowCount();
        assertEquals("there should be one entry in the log", 1, totalRowCount);
        assertNotNull("There should be a result returned", result);
        ReportState state = result.getState();
        assertNotNull("The result should come with a state", state);
        DetailResultList resultList = result.getData();
        DocumentActivityDetailResult detailResult = (DocumentActivityDetailResult) resultList.getResults(0);
        ActionType action = detailResult.getAction();
        assertEquals("action should not be EDIT", "EDIT", action.toString());
    }
    
    public void testTrackDELETE() throws UnauthorizedCallerFault, RemoteException, InterruptedException, FileNotFoundException, IOException {
        CommProfileDTOQueryTerm[] queryTerms = { 
                new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop Enforcer Default Profile")
        };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        ActivityJournalingSettingsDTO customJournalSetting = comDTO.getCustomActivityJournalingSettings();
        ActionTypeDTOList customJournalSettingAction = customJournalSetting.getLoggedActivities();
        ActionTypeDTO[] customJournalAction = new ActionTypeDTO[]{ActionTypeDTO.DELETE};
        customJournalSettingAction.setAction(customJournalAction);
        comDTO.setCustomActivityJournalingSettings(customJournalSetting);
        comDTO.setCurrentActivityJournalingSettings(customJournalSetting);
        
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(COMM_PROFILE_UPDATE_TIME);
        
        File testFile = new File(testFilePath);
        testFile.delete();
        
        Thread.sleep(TRACKING_LOG_UPDATE_TIME);
        
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setSummaryType(ReportSummaryType.None);
        //newReport.setEffects(new EffectList(new EffectType[]{EffectType.allow})); 
        newReport.setActions(new ActionList(new ActionType[]{ActionType.DELETE})); 
        newReport.setSortSpec(new ReportSortSpec(ReportSortFieldName.None, SortDirection.Ascending));
        newReport.setTarget(ReportTargetType.ActivityJournal);
        //newReport.setUsers(new StringList(new String[]{"rlin@bluejungle.com"}));
        
        ReportExecutionIF reportExecution = getReportExecution();
        ReportDetailResult result = (ReportDetailResult)reportExecution.executeReport(newReport, 10, -1);
        long availRowCount = result.getAvailableRowCount();
        long totalRowCount = result.getTotalRowCount();
        assertEquals("there should be one entry in the log", 1, totalRowCount);
        assertNotNull("There should be a result returned", result);
        ReportState state = result.getState();
        assertNotNull("The result should come with a state", state);
        DetailResultList resultList = result.getData();
        DocumentActivityDetailResult detailResult = (DocumentActivityDetailResult) resultList.getResults(0);
        ActionType action = detailResult.getAction();
        assertEquals("action should not be DELETE", "DELETE", action.toString());
    }

    public void testTrackCOPY() throws UnauthorizedCallerFault, RemoteException, InterruptedException, FileNotFoundException, IOException {
        CommProfileDTOQueryTerm[] queryTerms = { 
                new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop Enforcer Default Profile")
        };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        ActivityJournalingSettingsDTO customJournalSetting = comDTO.getCustomActivityJournalingSettings();
        ActionTypeDTOList customJournalSettingAction = customJournalSetting.getLoggedActivities();
        ActionTypeDTO[] customJournalAction = new ActionTypeDTO[]{ActionTypeDTO.COPY};
        customJournalSettingAction.setAction(customJournalAction);
        comDTO.setCustomActivityJournalingSettings(customJournalSetting);
        comDTO.setCurrentActivityJournalingSettings(customJournalSetting);
        
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(COMM_PROFILE_UPDATE_TIME);
        
        String copyString = cmd2Path + " /c COPY " + copyFilePath + " " + copyToFilePath;
        Process p = Runtime.getRuntime().exec(copyString);
        p.waitFor();
        
        Thread.sleep(TRACKING_LOG_UPDATE_TIME);
        
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setSummaryType(ReportSummaryType.None);
        //newReport.setEffects(new EffectList(new EffectType[]{EffectType.allow})); 
        newReport.setActions(new ActionList(new ActionType[]{ActionType.COPY})); 
        newReport.setSortSpec(new ReportSortSpec(ReportSortFieldName.None, SortDirection.Ascending));
        newReport.setTarget(ReportTargetType.ActivityJournal);
        //newReport.setUsers(new StringList(new String[]{"rlin@bluejungle.com"}));
        
        ReportExecutionIF reportExecution = getReportExecution();
        ReportDetailResult result = (ReportDetailResult)reportExecution.executeReport(newReport, 10, -1);
        long availRowCount = result.getAvailableRowCount();
        long totalRowCount = result.getTotalRowCount();
        assertEquals("there should be two entries in the log", 1, totalRowCount);
        assertNotNull("There should be a result returned", result);
        ReportState state = result.getState();
        assertNotNull("The result should come with a state", state);
        DetailResultList resultList = result.getData();
        DocumentActivityDetailResult detailResult = (DocumentActivityDetailResult) resultList.getResults(0);
        ActionType action = detailResult.getAction();
        assertEquals("action should not be COPY", "COPY", action.toString());
    }

    public void testTrackMOVE() throws UnauthorizedCallerFault, RemoteException, InterruptedException, FileNotFoundException, IOException {
        CommProfileDTOQueryTerm[] queryTerms = { 
                new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop Enforcer Default Profile")
        };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        ActivityJournalingSettingsDTO customJournalSetting = comDTO.getCustomActivityJournalingSettings();
        ActionTypeDTOList customJournalSettingAction = customJournalSetting.getLoggedActivities();
        ActionTypeDTO[] customJournalAction = new ActionTypeDTO[]{ActionTypeDTO.MOVE};
        customJournalSettingAction.setAction(customJournalAction);
        comDTO.setCustomActivityJournalingSettings(customJournalSetting);
        comDTO.setCurrentActivityJournalingSettings(customJournalSetting);
        
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(COMM_PROFILE_UPDATE_TIME);
        
        File testFile = new File(moveFilePath);
        testFile.renameTo(new File(moveToFilePath));
        
        Thread.sleep(TRACKING_LOG_UPDATE_TIME);
        
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setSummaryType(ReportSummaryType.None);
        //newReport.setEffects(new EffectList(new EffectType[]{EffectType.allow})); 
        newReport.setActions(new ActionList(new ActionType[]{ActionType.MOVE})); 
        newReport.setSortSpec(new ReportSortSpec(ReportSortFieldName.None, SortDirection.Ascending));
        newReport.setTarget(ReportTargetType.ActivityJournal);
        //newReport.setUsers(new StringList(new String[]{"rlin@bluejungle.com"}));
        
        ReportExecutionIF reportExecution = getReportExecution();
        ReportDetailResult result = (ReportDetailResult)reportExecution.executeReport(newReport, 10, -1);
        long availRowCount = result.getAvailableRowCount();
        long totalRowCount = result.getTotalRowCount();
        assertEquals("there should be one entry in the log", 1, totalRowCount);
        assertNotNull("There should be a result returned", result);
        ReportState state = result.getState();
        assertNotNull("The result should come with a state", state);
        DetailResultList resultList = result.getData();
        DocumentActivityDetailResult detailResult = (DocumentActivityDetailResult) resultList.getResults(0);
        ActionType action = detailResult.getAction();
        assertEquals("action should not be MOVE", "MOVE", action.toString());
    }
    
    public void testTrackRENAME() throws UnauthorizedCallerFault, RemoteException, InterruptedException, FileNotFoundException, IOException {
        CommProfileDTOQueryTerm[] queryTerms = { 
                new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop Enforcer Default Profile")
        };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        ActivityJournalingSettingsDTO customJournalSetting = comDTO.getCustomActivityJournalingSettings();
        ActionTypeDTOList customJournalSettingAction = customJournalSetting.getLoggedActivities();
        ActionTypeDTO[] customJournalAction = new ActionTypeDTO[]{ActionTypeDTO.RENAME};
        customJournalSettingAction.setAction(customJournalAction);
        comDTO.setCustomActivityJournalingSettings(customJournalSetting);
        comDTO.setCurrentActivityJournalingSettings(customJournalSetting);
        
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(COMM_PROFILE_UPDATE_TIME);
        
        File testFile = new File(renameFilePath);
        testFile.renameTo(new File(renameToFilePath));
        
        Thread.sleep(TRACKING_LOG_UPDATE_TIME);
        
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setSummaryType(ReportSummaryType.None);
        //newReport.setEffects(new EffectList(new EffectType[]{EffectType.allow})); 
        newReport.setActions(new ActionList(new ActionType[]{ActionType.RENAME})); 
        newReport.setSortSpec(new ReportSortSpec(ReportSortFieldName.None, SortDirection.Ascending));
        newReport.setTarget(ReportTargetType.ActivityJournal);
        //newReport.setUsers(new StringList(new String[]{"rlin@bluejungle.com"}));
        
        ReportExecutionIF reportExecution = getReportExecution();
        ReportDetailResult result = (ReportDetailResult)reportExecution.executeReport(newReport, 10, -1);
        long availRowCount = result.getAvailableRowCount();
        long totalRowCount = result.getTotalRowCount();
        assertEquals("there should be one entry in the log", 1, totalRowCount);
        assertNotNull("There should be a result returned", result);
        ReportState state = result.getState();
        assertNotNull("The result should come with a state", state);
        DetailResultList resultList = result.getData();
        DocumentActivityDetailResult detailResult = (DocumentActivityDetailResult) resultList.getResults(0);
        ActionType action = detailResult.getAction();
        assertEquals("action should not be RENAME", "RENAME", action.toString());
    }

    
    public void testTrackCHANGE_ATTRIBUTES() throws UnauthorizedCallerFault, RemoteException, InterruptedException, FileNotFoundException, IOException {
        CommProfileDTOQueryTerm[] queryTerms = { 
                new CommProfileDTOQueryTerm(CommProfileDTOQueryField.name, "Desktop Enforcer Default Profile")
        };
        CommProfileDTOQueryTermSet singleTermQueryTermSet = new CommProfileDTOQueryTermSet();
        singleTermQueryTermSet.setCommProfileDTOQueryTerm(queryTerms);
        CommProfileDTOQuery singleTermDTOQuery = new CommProfileDTOQuery();
        singleTermDTOQuery.setCommProfileDTOQueryTermSet(singleTermQueryTermSet);
        singleTermDTOQuery.setFetchSize(1);
        CommProfileDTOList profilesRetrieved = dmsProfileService.getCommProfiles(singleTermDTOQuery);
        CommProfileDTO comDTO = profilesRetrieved.getCommProfileDTO(0);
        assertEquals("retrieved incorrect comm profile", AgentTypeDTO.DESKTOP, comDTO.getAgentType());
        ActivityJournalingSettingsDTO customJournalSetting = comDTO.getCustomActivityJournalingSettings();
        ActionTypeDTOList customJournalSettingAction = customJournalSetting.getLoggedActivities();
        ActionTypeDTO[] customJournalAction = new ActionTypeDTO[]{ActionTypeDTO.CHANGE_ATTRIBUTES};
        customJournalSettingAction.setAction(customJournalAction);
        comDTO.setCustomActivityJournalingSettings(customJournalSetting);
        comDTO.setCurrentActivityJournalingSettings(customJournalSetting);
        
        dmsProfileService.updateCommProfile(comDTO);
        Thread.sleep(COMM_PROFILE_UPDATE_TIME);
        
        File testFile = new File(attributeFilePath);
        testFile.setReadOnly();
        
        Thread.sleep(TRACKING_LOG_UPDATE_TIME);
        
        final Report newReport = new Report();
        //Assign the variables to the report
        newReport.setSummaryType(ReportSummaryType.None);
        //newReport.setEffects(new EffectList(new EffectType[]{EffectType.allow})); 
        newReport.setActions(new ActionList(new ActionType[]{ActionType.CHANGE_ATTRIBUTES})); 
        newReport.setSortSpec(new ReportSortSpec(ReportSortFieldName.None, SortDirection.Ascending));
        newReport.setTarget(ReportTargetType.ActivityJournal);
        //newReport.setUsers(new StringList(new String[]{"rlin@bluejungle.com"}));
        
        ReportExecutionIF reportExecution = getReportExecution();
        ReportDetailResult result = (ReportDetailResult)reportExecution.executeReport(newReport, 10, -1);
        long availRowCount = result.getAvailableRowCount();
        long totalRowCount = result.getTotalRowCount();
        assertEquals("there should be one entry in the log", 1, totalRowCount);
        assertNotNull("There should be a result returned", result);
        ReportState state = result.getState();
        assertNotNull("The result should come with a state", state);
        DetailResultList resultList = result.getData();
        DocumentActivityDetailResult detailResult =(DocumentActivityDetailResult) resultList.getResults(0);
        ActionType action = detailResult.getAction();
        assertEquals("action should not be CHANGE_ATTRIBUTES", "CHANGE_ATTRIBUTES", action.toString());
    }
 
}
