/*
 * Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.ResourceAndGroupResolverTest;
import com.bluejungle.destiny.container.shared.inquirymgr.entityresolver.UserAndGroupResolverTest;
import com.bluejungle.destiny.container.shared.inquirymgr.hqlbuilder.HQLBuilderTestSuite;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/QueryEngineTestSuite.java#1 $
 */

public class QueryEngineTestSuite {

    /**
     * Returns the test suite
     * 
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Query Engine");
        suite.addTest(new TestSuite(DataModelTest.class, "Data Model"));
        suite.addTest(new TestSuite(UserAndGroupResolverTest.class, "User And Group resolver"));
        suite.addTest(new TestSuite(ResourceAndGroupResolverTest.class, "Resource And Group resolver"));
        suite.addTest(HQLBuilderTestSuite.suite());
        suite.addTest(new TestSuite(ResourceClassVisitorTest.class, "Resource Class visitor"));
        suite.addTest(new TestSuite(ReportExecutionMgrTest.class, "Report -> Query elements"));
        suite.addTest(new TestSuite(ReportExecutionTest.class, "Query Execution - Stateful"));
        suite.addTest(new TestSuite(ReportExecutionStatelessTest.class, "Query Execution - Stateless"));
        suite.addTest(new TestSuite(ReportExecutionWithHistoryTest.class, "Query Execution With Historical Data"));
        //TODO: temporarily taken out due to the removal of recourse class based queries
//        suite.addTest(new TestSuite(ReportExecutionWithResourceClass.class, "Query Execution - Resource Classes"));
        suite.addTest(new TestSuite(ReportExecutionMgrStatelessImplTest.class, "Report Execution Mgr - Stateless"));
        suite.addTest(new TestSuite(ReportExecutionCountOnlyTest.class, "Query Execution - Count only"));
        suite.addTest(ReportResultReaderTestSuite.suite());
        return suite;
    }
}
