/*
 * Created on Sep 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.inquirymgr.IReportExecutionMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.IStatelessReportExecutionMgr;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * This test class verifies the report execution with the stateless report
 * execution manager implementation
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ReportExecutionStatelessTest.java#1 $
 */

public class ReportExecutionStatelessTest extends ReportExecutionTest {

    /**
     * Return the stateless implementation of the report execution manager
     * 
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportExecutionTest#getReportExecutionMgr()
     */
    protected IReportExecutionMgr getReportExecutionMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IReportExecutionMgr.DATA_SOURCE_CONFIG_PARAM, getActivityDateSource());
        ComponentInfo compInfo = new ComponentInfo("reportStatelessExecutionMgr", ReportExecutionMgrStatelessImpl.class.getName(), IStatelessReportExecutionMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IStatelessReportExecutionMgr reportMgr = (IStatelessReportExecutionMgr) compMgr.getComponent(compInfo);
        return reportMgr;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ReportExecutionTest#testReportExecutionMgrInstantiation()
     */
    public void testReportExecutionMgrInstantiation() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration badConfig = new HashMapConfiguration();
        ComponentInfo compInfo = new ComponentInfo("reportExecutionMgrInstance", ReportExecutionMgrStatelessImpl.class.getName(), IReportExecutionMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, badConfig);
        boolean exThrown = false;
        try {
            IReportExecutionMgr reportMgr = (IReportExecutionMgr) compMgr.getComponent(compInfo);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("Report execution manager cannot work unless a data source is given in configuration.", exThrown);

        //Pass the good config now
        try {
            IReportExecutionMgr reportMgr = getReportExecutionMgr();
        } catch (RuntimeException e) {
            fail("No exception should be fired when creating report execution manager");
        }
    }
}