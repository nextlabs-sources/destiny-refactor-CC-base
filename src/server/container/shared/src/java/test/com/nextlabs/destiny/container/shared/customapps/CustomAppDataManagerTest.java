package com.nextlabs.destiny.container.shared.customapps;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.nextlabs.destiny.container.shared.customapps.hibernateimpl.CustomAppDO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportDataDO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportFileDO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportUIDO;

public class CustomAppDataManagerTest extends BaseContainerSharedTestCase{
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.ACTIVITY_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.MANAGEMENT_REPOSITORY);
    }
    
    static final String BASE_APP_NAME = "TestCustomApp";
    static final String BASE_APP_VERSION = "1.0";
    static final String BASE_APP_DESCRIPTION = "Description";
    
    
    private CustomAppDataManager appDataMgr;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        IComponentManager mgr = ComponentManagerFactory.getComponentManager();
        assertTrue(mgr.isComponentRegistered(DestinyRepository.ACTIVITY_REPOSITORY.getName()));
        assertTrue(mgr.isComponentRegistered(DestinyRepository.MANAGEMENT_REPOSITORY.getName()));
        
        // create manager for re-using among various tests
        appDataMgr = mgr.getComponent(CustomAppDataManager.class);
        
        appDataMgr.deleteAllCustomAppData();
    }
    
    protected void tearDown() throws Exception {
//        appDataMgr.deleteAllCustomAppData();
        super.tearDown();
    }
    
    @Override
    protected Set<DestinyRepository> getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }
    
    public void testCreateCustomAppData() throws Exception {
    
        List<CustomAppDO> customApps  = generateCustomAppData();
        for (CustomAppDO thisApp : customApps ) {
            appDataMgr.createCustomAppData(thisApp);
        }

        // TODO - verify the result 
        for (CustomAppDO thisApp : customApps ) {
            Long customAppId = thisApp.getId(); 
            assertNotNull(customAppId);

            CustomReportUIDO customReportUIDO = thisApp.getReportUI();
            if (customReportUIDO != null) {
                assertNotNull(customReportUIDO.getId());
                assertEquals(customAppId, customReportUIDO.getCustomAppId());
                assertSame(thisApp, customReportUIDO.getCustomApp());
            }
            
            Collection<CustomReportDataDO> customReportDataDOs = thisApp.getCustomReports();
            if (customReportDataDOs != null) {
                for (CustomReportDataDO customReportDataDO : customReportDataDOs) {
                    assertNotNull(customReportDataDO.getId());
                    assertEquals(customAppId, customReportDataDO.getCustomAppId());
                    assertSame(thisApp, customReportDataDO.getCustomApp());

                    List<CustomReportFileDO> reportDesignFiles = customReportDataDO.getReportDesignFiles();
                    if (reportDesignFiles != null) {
                        for (CustomReportFileDO reportDesignFile : reportDesignFiles) {
                            assertNotNull(reportDesignFile.getId());
                        }
                    }
                }
            }
        }
    }

    public void testListCustomAppData() throws Exception {
        List<CustomAppDO> customApps  = generateCustomAppData();
        Map<Long, CustomAppDO> idToAppDOMap = new HashMap<Long, CustomAppDO>(customApps.size());
        for (CustomAppDO thisApp : customApps ) {
            appDataMgr.createCustomAppData(thisApp);
            idToAppDOMap.put(thisApp.getId(), thisApp);
        }
        
        List<CustomAppDO> savedCustomAppDOs = appDataMgr.getAllCustomAppData();
        for (CustomAppDO savedCustomAppDO : savedCustomAppDOs) {
            CustomAppDO customApp = idToAppDOMap.get(savedCustomAppDO.getId());
            assertEquals(customApp, savedCustomAppDO);
        }
    }
    
    protected void assertEquals(CustomAppDO expected, CustomAppDO actual){
        if (expected == null && actual == null){
            return;
        } 
        if (expected == actual) {
            return;
        }
        assertEquals(expected.getId(),          actual.getId());
        assertEquals(expected.getName(),        actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getVersion(),     actual.getVersion());
        assertEquals(expected.getReportUI(),    actual.getReportUI());
        assertCustomReportDataDOEquals(expected.getCustomReports(), actual.getCustomReports());
    }
    
    protected void assertEquals(CustomReportUIDO expected, CustomReportUIDO actual){
        if (expected == null && actual == null){
            return;
        }
        if (expected == actual) {
            return;
        }
        assertEquals(expected.getId(),          actual.getId());
        assertEquals(expected.getCustomAppId(), actual.getCustomAppId());
        assertEquals(actual.getCustomAppId(),   actual.getCustomApp().getId());
        assertEquals(expected.getFileContent(), actual.getFileContent());
    }
    
    protected void assertCustomReportDataDOEquals(List<CustomReportDataDO> expected,
            List<CustomReportDataDO> actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == actual) {
            return;
        }
        int size = expected.size();
        assertEquals(size, actual.size());

        Iterator<CustomReportDataDO> expectedIterator = expected.iterator();
        Iterator<CustomReportDataDO> actualIterator = actual.iterator();
        for (int i = 0; i < size; i++) {
            assertEquals(expectedIterator.next(), actualIterator.next());
        }
    }

    protected void assertEquals(CustomReportDataDO expected, CustomReportDataDO actual) {
        if (expected == null && actual == null){
            return;
        }
        if (expected == actual) {
            return;
        }
        assertEquals(expected.getId(),          actual.getId());
        assertEquals(expected.getCustomAppId(), actual.getCustomAppId());
        assertEquals(actual.getCustomAppId(),   actual.getCustomApp().getId());
        assertEquals(expected.getTitle(),       actual.getTitle());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertCustomReportFileDOEquals(expected.getReportDesignFiles(), actual.getReportDesignFiles());
    }

    protected void assertCustomReportFileDOEquals(List<CustomReportFileDO> expected,
            List<CustomReportFileDO> actual) {
        if (expected == null && actual == null){
            return;
        }
        if (expected == actual) {
            return;
        }
        int size = expected.size();
        assertEquals(actual.toString(), size, actual.size());

        for (int i = 0; i < size; i++) {
            assertEquals(expected.get(i), actual.get(i));
        }
    }

    protected void assertEquals(CustomReportFileDO expected, CustomReportFileDO actual) {
        if (expected == null && actual == null){
            return;
        }
        if (expected == actual) {
            return;
        }
        assertEquals(expected.getId(),      actual.getId());
        assertEquals(expected.getReport().getId(),  actual.getReport().getId());
        assertEquals(expected.getName(),    actual.getName());
        assertEquals(expected.getContent(), actual.getContent());
    }

    private List<CustomAppDO> generateCustomAppData() {
        List<CustomAppDO> customAppsData = new ArrayList<CustomAppDO>();
        for (int i = 0; i < 2; i++) {
            CustomAppDO customApp = new CustomAppDO();
            customApp.setName(BASE_APP_NAME + i);
            customApp.setDescription(BASE_APP_DESCRIPTION + i);
            customApp.setVersion(BASE_APP_VERSION + i) ;
            
            // ui 
            CustomReportUIDO reportUIDO = new CustomReportUIDO();
            reportUIDO.setFileContent("This is a test for keeping the UI content");
            customApp.setReportUI(reportUIDO);
            
            // report 1
            CustomReportDataDO reportData = new CustomReportDataDO();
            reportData.setTitle("test report title");
            reportData.setDescription("ReportDescription");
            List<CustomReportFileDO> designFiles = new ArrayList<CustomReportFileDO>();
            for (int j = 0; j < 4; j++) {
                CustomReportFileDO reportFile = new CustomReportFileDO();
                reportFile.setName("report-file_" + j);
                reportFile.setContent("fooooo_" + j);
                designFiles.add(reportFile);
            }
            reportData.setReportDesignFiles(designFiles);
            
            // report 2
            CustomReportDataDO reportData2 = new CustomReportDataDO();
            reportData2.setTitle("test report title2");
            reportData2.setDescription("ReportDescription2");
            CustomReportFileDO reportFile2 = new CustomReportFileDO();
            reportFile2.setName("report2");
            reportFile2.setContent("fooooo");
            List<CustomReportFileDO> designFiles2 = new ArrayList<CustomReportFileDO>();
            designFiles2.add(reportFile2);
            reportData2.setReportDesignFiles(designFiles2);
            
            List<CustomReportDataDO> custReportList = 
                new ArrayList<CustomReportDataDO>();
            custReportList.add(reportData);
            custReportList.add(reportData2);
    
            customApp.setCustomReports(custReportList);
            customAppsData.add(customApp);
        }
        return customAppsData;
    }
    
}