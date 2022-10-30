package com.bluejungle.pf.destiny.policymap;

import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource;

//All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc, Redwood City CA,
//Ownership remains with Blue Jungle Inc, All rights reserved worldwide.

/**
 * TODO Write file summary here.
 * 
 * @author pkeni
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/destiny/policymap/ServerTargetResolverTest.java#1 $
 */

/**
 * @author pkeni
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServerTargetResolverTest extends PFTestWithDataSource {

    private IComponentManager cm;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(ServerTargetResolverTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        cm = ComponentManagerFactory.getComponentManager();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Constructor for ServerTargetResolverTest.
     * @param arg0
     */
    public ServerTargetResolverTest(String arg0) {
        super(arg0);
    }
    
    public void testResolutionMapping() throws Exception {
        Session hs = ((SessionFactory) cm.getComponent( DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName() )).openSession();
        Transaction tx = hs.beginTransaction();
        hs.delete("from ResolutionMapping");
        /*
        ResolutionMapping mapping = new ResolutionMapping(new Long(1), new Long(1), MapType.ACTION_POLICY);
        hs.save(mapping);
        mapping = new ResolutionMapping(new Long(1), new Long(1), MapType.SUBJECT_GROUP);
        hs.save(mapping);
        mapping = new ResolutionMapping(new Long(1), new Long(1), MapType.SUBJECT_POLICY);
        hs.save(mapping);
        */
        tx.commit();
        hs.close();
    }
}
