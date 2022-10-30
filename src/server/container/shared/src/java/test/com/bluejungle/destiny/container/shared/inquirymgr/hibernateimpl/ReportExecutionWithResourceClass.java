/*
 * Created on Nov 1, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.Calendar;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.shared.inquirymgr.IInquiry;
import com.bluejungle.destiny.container.shared.inquirymgr.IReport;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IReportResultReader;
import com.bluejungle.destiny.container.shared.inquirymgr.IResultData;
import com.bluejungle.destiny.container.shared.inquirymgr.InquiryTargetDataType;
import com.bluejungle.destiny.container.shared.inquirymgr.InvalidReportArgumentException;
import com.bluejungle.destiny.container.shared.inquirymgr.ReportSummaryType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * This class tests various report execution with resource classes.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionWithResourceClass.java#1 $
 */

public class ReportExecutionWithResourceClass extends BaseReportExecutionTest {

    /**
     * Resource class Names
     */
    private static final String ACCESS_DATE_RESOURCE_CLASS = "accessDateResource";
    private static final String FOLDER1_RESOURCE_CLASS = "folder1Resource";
    private static final String FOLDER1_ON_ANY_DRIVE_RESOURCE_CLASS = "folder1OnAnyDriveResource";
    private static final String NAME_CONTAINS_FOLDER1_RESOURCE_CLASS = "nameContainsFolder1";
    private static final String JUST_UNDER_FOLDER1_RESOURCE_CLASS = "justUnderFolder1";
    private static final String ALL_RESOURCES_ON_C_RESOURCE_CLASS = "allOnC";
    private static final String ALL_RESOURCES_ON_D_RESOURCE_CLASS = "allOnD";
    private static final String ALL_RESOURCES_ON_COMPUTER_RESOURCE_CLASS = "allOnComputer";

    /**
     * Returns the report execution manager
     * 
     * @return the report execution manager
     */
    protected IReportExecutionMgr getReportExecutionMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("reportExecutionMgrIn10", ReportExecutionMgrIn10.class.getName(), IReportExecutionMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IReportExecutionMgr reportMgr = (IReportExecutionMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * Creates the resource classes for the tests
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        final String lastAccessed = "id null status approved hidden resource " + ACCESS_DATE_RESOURCE_CLASS + " = access_date > \"01/01/1003\"";
        this.sampleDataMgr.createResourceClasses(lastAccessed);
        final String folder1 = "id null status approved hidden resource " + FOLDER1_RESOURCE_CLASS + " = resource.directory = \"folder1\"";
        this.sampleDataMgr.createResourceClasses(folder1);
        final String folder1OnAnyDrive = "id null status approved hidden resource " + FOLDER1_ON_ANY_DRIVE_RESOURCE_CLASS + " = resource.name = \"?:/folder1/**\"";
        this.sampleDataMgr.createResourceClasses(folder1OnAnyDrive);
        final String nameContainsFolder1 = "id null status approved hidden resource " + NAME_CONTAINS_FOLDER1_RESOURCE_CLASS + " = resource.name = \"c:/folder1/**\"";
        this.sampleDataMgr.createResourceClasses(nameContainsFolder1);
        final String justUnderFolder1 = "id null status approved hidden resource " + JUST_UNDER_FOLDER1_RESOURCE_CLASS + " = resource.name = \"c:/folder1/*\"";
        this.sampleDataMgr.createResourceClasses(justUnderFolder1);
        final String allOnC = "id null status approved hidden resource " + ALL_RESOURCES_ON_C_RESOURCE_CLASS + " = resource.name = \"c:/**\"";
        this.sampleDataMgr.createResourceClasses(allOnC);
        final String allOnD = "id null status approved hidden resource " + ALL_RESOURCES_ON_D_RESOURCE_CLASS + " = resource.name = \"d:/**\"";
        this.sampleDataMgr.createResourceClasses(allOnD);
        final String allOnComputer = "id null status approved hidden resource " + ALL_RESOURCES_ON_COMPUTER_RESOURCE_CLASS + " = resource.name = \"e:/**\" OR " + ALL_RESOURCES_ON_C_RESOURCE_CLASS + " OR " + ALL_RESOURCES_ON_D_RESOURCE_CLASS;
        this.sampleDataMgr.createResourceClasses(allOnComputer);
    }

    /**
     * This test verifies that if the resource class contains the access date,
     * the access date parameters is safely ignored during the query.
     */
    public void testResourceClassWithAccessDate() {
        final int nbRecords = 30;
        TestPolicyActivityLogEntryDO modelPolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        Calendar cal2003 = Calendar.getInstance();
        cal2003.set(Calendar.YEAR, 2004);
        cal2003.set(Calendar.MONTH, Calendar.JANUARY);
        cal2003.set(Calendar.DAY_OF_MONTH, 1);
        modelPolicy.setTimestamp(cal2003);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbRecords, modelPolicy);
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by day test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allRecords = reportMgr.createReport();
        IInquiry inquiry = allRecords.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addResource(ACCESS_DATE_RESOURCE_CLASS);
        allRecords.setSummaryType(ReportSummaryType.NONE);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allRecords);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query");
        } else {
            int fetchedResults = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                fetchedResults++;
            }
            assertEquals("The access_date condition should be skipped during the query", nbRecords, fetchedResults);
            reader.close();
        }
    }

    /**
     * This test verifies that if a resource class contains a directory name,
     * the query is working properly.
     */
    public void testResourceClassWithDirectoryName() {
        final int nbFolder1 = 10;
        final int nbFolder2 = 15;
        TestPolicyActivityLogEntryDO folder1Policy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        fromResInfo.setName("file:///c:/folder1/abc.txt");
        folder1Policy.setFromResourceInfo(fromResInfo);
        TestPolicyActivityLogEntryDO folder2Policy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo2 = new FromResourceInformationDO();
        fromResInfo2.setName("file:///c:/folder2/abc.txt");
        folder2Policy.setFromResourceInfo(fromResInfo2);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbFolder1, folder1Policy);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbFolder1), nbFolder2, folder2Policy);
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by day test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport folder1Records = reportMgr.createReport();
        IInquiry inquiry = folder1Records.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addResource(FOLDER1_RESOURCE_CLASS);
        folder1Records.setSummaryType(ReportSummaryType.NONE);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(folder1Records);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query");
        } else {
            int fetchedResults = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                fetchedResults++;
            }
            assertEquals("The folder location condition should be evaluated properly during the query", nbFolder1, fetchedResults);
            reader.close();
        }
    }

    /**
     * This test verifies that if a resource class contains a directory name,
     * the query is working properly.
     */
    public void testResourceClassWithFileName() {
        final int nbJustFolder1 = 10;
        final int nbSubFolder = 15;
        TestPolicyActivityLogEntryDO folder1Policy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        fromResInfo.setName("file:///c:/folder1/abc.txt");
        folder1Policy.setFromResourceInfo(fromResInfo);
        TestPolicyActivityLogEntryDO folder2Policy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo2 = new FromResourceInformationDO();
        fromResInfo2.setName("file:///c:/folder1/folder2/123.txt");
        folder2Policy.setFromResourceInfo(fromResInfo2);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbJustFolder1, folder1Policy);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbJustFolder1), nbSubFolder, folder2Policy);
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by day test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport justFolder1Records = reportMgr.createReport();
        IInquiry inquiry = justFolder1Records.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addResource(JUST_UNDER_FOLDER1_RESOURCE_CLASS);
        justFolder1Records.setSummaryType(ReportSummaryType.NONE);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(justFolder1Records);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query");
        } else {
            int fetchedResults = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                fetchedResults++;
            }
            //The query should return both results - it treats * as **
            assertEquals("The folder location condition should be evaluated properly during the query", nbJustFolder1 + nbSubFolder, fetchedResults);
            reader.close();
        }

        IReport justSubFolderRecords = reportMgr.createReport();
        inquiry = justSubFolderRecords.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addResource(NAME_CONTAINS_FOLDER1_RESOURCE_CLASS);
        justSubFolderRecords.setSummaryType(ReportSummaryType.NONE);

        try {
            reader = reportExecutionMgr.executeReport(justSubFolderRecords);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query");
        } else {
            int fetchedResults = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                fetchedResults++;
            }
            //The query should return only all records as well
            assertEquals("The folder location condition should be evaluated properly during the query", nbJustFolder1 + nbSubFolder, fetchedResults);
            reader.close();
        }
    }
    
    
    public void testResourceClassWithFileNameAndQuestionMark() {
        final int nbFolder1 = 10;
        final int nbFolder2 = 15;
        TestPolicyActivityLogEntryDO folder1Policy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        fromResInfo.setName("file:///c:/folder1/abc.txt");
        folder1Policy.setFromResourceInfo(fromResInfo);
        TestPolicyActivityLogEntryDO folder2Policy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo2 = new FromResourceInformationDO();
        fromResInfo2.setName("file:///c:/folder2/123.txt");
        folder2Policy.setFromResourceInfo(fromResInfo2);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbFolder1, folder1Policy);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbFolder1), nbFolder2, folder2Policy);
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by day test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport justFolder1Records = reportMgr.createReport();
        IInquiry inquiry = justFolder1Records.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addResource(FOLDER1_ON_ANY_DRIVE_RESOURCE_CLASS);
        justFolder1Records.setSummaryType(ReportSummaryType.NONE);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(justFolder1Records);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query");
        } else {
            int fetchedResults = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                fetchedResults++;
            }
            assertEquals("The folder location condition should be evaluated properly during the query", nbFolder1, fetchedResults);
            reader.close();
        }
    }

    /**
     * This test verifies that if a resource class refers to other resource
     * classes, they get properly added to the query
     */
    public void testResourceClassNestedReferences() {
        int nbCResources = 10;
        int nbDResources = 15;
        int nbEResources = 22;
        int nbFResources = 31;
        int nbExpectedAllComputer = nbCResources + nbDResources + nbEResources;
        TestPolicyActivityLogEntryDO cPolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        fromResInfo.setName("file:///c:/abc.txt");
        cPolicy.setFromResourceInfo(fromResInfo);
        TestPolicyActivityLogEntryDO dPolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo2 = new FromResourceInformationDO();
        fromResInfo2.setName("file:///d:/folder2/123.txt");
        dPolicy.setFromResourceInfo(fromResInfo2);
        TestPolicyActivityLogEntryDO ePolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo3 = new FromResourceInformationDO();
        fromResInfo3.setName("file:///e:/folder3/d.txt");
        ePolicy.setFromResourceInfo(fromResInfo3);
        TestPolicyActivityLogEntryDO fPolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo4 = new FromResourceInformationDO();
        fromResInfo4.setName("file:///f:/folder3/d.txt");
        fPolicy.setFromResourceInfo(fromResInfo4);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbCResources, cPolicy);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbCResources), nbDResources, dPolicy);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbCResources + nbDResources), nbEResources, ePolicy);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbCResources + nbDResources + nbEResources), nbFResources, fPolicy);
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by day test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allComputerRecords = reportMgr.createReport();
        IInquiry inquiry = allComputerRecords.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addResource(ALL_RESOURCES_ON_COMPUTER_RESOURCE_CLASS);
        allComputerRecords.setSummaryType(ReportSummaryType.NONE);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allComputerRecords);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query");
        } else {
            int fetchedResults = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                fetchedResults++;
            }
            assertEquals("The nested resource classes shoudl be included in the results", nbExpectedAllComputer, fetchedResults);
            reader.close();
        }
    }

    /**
     * This test verifies that if two resource classes are combined with AND,
     * the results make sense 
     */
    public void testResourceClassWithMultipleResourceClassNames() {
        int nbCResources = 10;
        int nbDResources = 15;
        int nbEResources = 22;
        int nbExpectedResults = nbCResources + nbDResources;
        TestPolicyActivityLogEntryDO cPolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo = new FromResourceInformationDO();
        fromResInfo.setName("file:///c:/abc.txt");
        cPolicy.setFromResourceInfo(fromResInfo);
        TestPolicyActivityLogEntryDO dPolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo2 = new FromResourceInformationDO();
        fromResInfo2.setName("file:///d:/folder2/123.txt");
        dPolicy.setFromResourceInfo(fromResInfo2);
        TestPolicyActivityLogEntryDO ePolicy = this.sampleDataMgr.getBasicPolicyLogRecord();
        FromResourceInformationDO fromResInfo3 = new FromResourceInformationDO();
        fromResInfo3.setName("file:///e:/folder3/d.txt");
        ePolicy.setFromResourceInfo(fromResInfo3);
        Session s = null;
        try {
            s = this.getActivityDateSource().getSession();
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(0), nbCResources, cPolicy);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbCResources), nbDResources, dPolicy);
            this.sampleDataMgr.insertIdenticalPolicyLogRecords(s, new Long(nbCResources + nbDResources), nbEResources, ePolicy);
        } catch (HibernateException e) {
            fail("Error when inserting data records for summary by day test");
        } finally {
            HibernateUtils.closeSession(s, null);
        }

        IReportMgr reportMgr = getReportMgr();
        IReportExecutionMgr reportExecutionMgr = getReportExecutionMgr();
        IReport allComputerRecords = reportMgr.createReport();
        IInquiry inquiry = allComputerRecords.getInquiry();
        inquiry.setTargetData(InquiryTargetDataType.POLICY);
        inquiry.addResource(ALL_RESOURCES_ON_C_RESOURCE_CLASS);
        inquiry.addResource(ALL_RESOURCES_ON_D_RESOURCE_CLASS);
        allComputerRecords.setSummaryType(ReportSummaryType.NONE);

        IReportResultReader reader = null;
        try {
            reader = reportExecutionMgr.executeReport(allComputerRecords);
        } catch (InvalidReportArgumentException e) {
            fail("The report should not have bad arguments");
        } catch (DataSourceException e) {
            fail("The report execution should not have throw exception");
        }
        assertNotNull("The reader object should not be null", reader);
        if (!reader.hasNextResult()) {
            fail("There should be records returned for the query");
        } else {
            int fetchedResults = 0;
            while (reader.hasNextResult()) {
                IResultData resultData = reader.nextResult();
                assertNotNull("result record should exist", resultData);
                fetchedResults++;
            }
            assertEquals("The nested resource classes shoudl be included in the results", nbExpectedResults, fetchedResults);
            reader.close();
        }
    }
}
