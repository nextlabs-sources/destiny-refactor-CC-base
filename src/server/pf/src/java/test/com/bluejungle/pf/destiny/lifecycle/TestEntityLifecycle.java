package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/destiny/lifecycle/TestEntityLifecycle.java#1 $
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.utils.PQLTestUtils;

/**
 * Tests the entity lifecycle components and the lifecycle manager.
 */

public class TestEntityLifecycle extends PFTestWithDataSource {
    
    private static final String[] SAVED_POLICY_NAMES = new String[] { 
        "quick", "brown", "fox", "jumps", "over", "lazy", "dog" };

    private static final String[] SAVED_RESOURCE_NAMES = new String[] { 
        "RESOURCE/big", "RESOURCE/quick", "RESOURCE/yellow", "RESOURCE/dog" };

    private static final String[] SAVED_APP_NAMES = new String[] { 
        "APPLICATION/pole", "APPLICATION/POP", "APPLICATION/Poland", "APPLICATION/pOSITION"
      , "APPLICATION/postfix", "APPLICATION/POSTGRESS", "APPLICATION/North", "APPLICATION/South" };


    private LifecycleManager lm;
    
    public static TestSuite suite() {
        return makeSingleInitializationSuite(TestEntityLifecycle.class);
    }
    
    private static final Date date( String s ) {
        try {
            return new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss" ).parse( s );
        } catch ( ParseException dfe ) {
            return null;
        }
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() {
        try {
            super.setUp();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        lm = cm.getComponent(LifecycleManager.COMP_INFO);
    }

    public void testToPathElements() {
        Collection<String> tst;
        tst = LifecycleManager.toPathElements("Equities"
        +   PQLParser.SEPARATOR + "Trading"
        +   PQLParser.SEPARATOR + "Enforcement"
        +   PQLParser.SEPARATOR + "NoResearchViewing"
        );
        assertNotNull( tst );
        assertEquals( 3, tst.size() );
        String[] res = new String[3];
        tst.toArray( res );
        assertEquals( "Equities", res[0] );
        assertEquals( "Equities"
        +   PQLParser.SEPARATOR
        +   "Trading"
        ,   res[1] );
        assertEquals( "Equities"
        +   PQLParser.SEPARATOR
        +   "Trading"
        +   PQLParser.SEPARATOR
        +   "Enforcement"
        ,   res[2] );

        tst = LifecycleManager.toPathElements("NoResearchViewing");
        assertNotNull( tst );
        assertTrue( tst.isEmpty() );

        tst = LifecycleManager.toPathElements("Equities" + PQLParser.SEPARATOR + "NoResearchViewing");
        assertNotNull( tst );
        assertEquals( 1, tst.size() );
        res = new String[1];
        tst.toArray( res );
        assertEquals( "Equities", res[0] );
    }

    public void testClear() throws HibernateException {
        Session hs = lm.getSessionFactory().openSession();
        try {
            Transaction tx = hs.beginTransaction();
            hs.delete( "from DeploymentEntity" );
            hs.delete( "from DevelopmentEntity" );
            hs.delete( "from DeploymentRecord");
            tx.commit();
        } finally {
            hs.close();
        }
    }

    public void testSeedDevelopmentEntities() throws PQLException, EntityManagementException {
        Collection<DevelopmentEntity> pc = lm.getEntitiesForNames(
            EntityType.POLICY,
            Arrays.asList( SAVED_POLICY_NAMES ),
            LifecycleManager.MAKE_EMPTY
        );
        Collection<DevelopmentEntity> rc = lm.getEntitiesForNames(
            EntityType.COMPONENT,
            Arrays.asList( SAVED_RESOURCE_NAMES ),
            LifecycleManager.MAKE_EMPTY
        );
        Collection<DevelopmentEntity> ac = lm.getEntitiesForNames(
            EntityType.COMPONENT,
            Arrays.asList( SAVED_APP_NAMES ),
            LifecycleManager.MAKE_EMPTY
        );

        for (DevelopmentEntity dev : pc) {
            PQLTestUtils.setStatus( dev, DevelopmentStatus.DRAFT);
        }
        for (DevelopmentEntity dev : rc) {
            PQLTestUtils.setStatus(dev, DevelopmentStatus.DRAFT);
        }
        for (DevelopmentEntity dev : ac) {
            PQLTestUtils.setStatus(dev, DevelopmentStatus.DRAFT);
        }
        lm.saveEntities( pc, LifecycleManager.MUST_EXIST, null );
        lm.saveEntities( rc, LifecycleManager.MUST_EXIST, null );
        lm.saveEntities( ac, LifecycleManager.MUST_EXIST, null );
    }

    public void testGetEntitiesForNames() throws PQLException, EntityManagementException {
        Collection<String> names = Arrays.asList("added", "brown" , "dog", "extra", "fox", "jumps"
                , "lazy", "missing", "over", "quick", "trailing");
        Collection<DevelopmentEntity> res = lm.getEntitiesForNames(
                EntityType.POLICY, names, LifecycleManager.MAKE_EMPTY);
        assertNotNull(res);
        assertEquals(names.size(), res.size());
        Iterator<String> ni = names.iterator();
        for ( Iterator<DevelopmentEntity> iter = res.iterator(); iter.hasNext() && ni.hasNext() ; ) {
            String currName = ni.next();
            DevelopmentEntity dev = iter.next();
            assertEquals( currName, dev.getName() );
            if ( Arrays.asList(SAVED_POLICY_NAMES).contains( currName ) ) {
                assertSame( DevelopmentStatus.DRAFT, dev.getStatus() );
            } else {
                assertSame( DevelopmentStatus.NEW, dev.getStatus() );
            }
        }
    }

    public void testModifyAndSave() throws PQLException, EntityManagementException {
        DevelopmentEntity p = lm.getEntityForName(
                EntityType.POLICY, "quick", LifecycleManager.MAKE_EMPTY);
        assertNotNull( p );
        assertSame( DevelopmentStatus.DRAFT, p.getStatus() );
        PQLTestUtils.setDescription( p, "updated" );
        lm.saveEntity( p, LifecycleManager.MUST_EXIST, null );
        p = lm.getEntityForName( EntityType.POLICY, "quick", LifecycleManager.MUST_EXIST );
        assertNotNull( p );
        assertEquals( p.getDescription(), "updated" );
        assertTrue( p.getPql().contains( "DESCRIPTION \"updated\"" ));
    }
    
    private static void assertEqualsIgnoreCase(char expect, char actual){
        assertEquals(Character.toLowerCase(expect), Character.toLowerCase(actual));
    }

    public void testGetNamesByTemplate() throws PQLException, EntityManagementException {
        Collection<DomainObjectDescriptor> descriptors = lm.getEntityDescriptors(
                EntityType.COMPONENT, "APPLICATION/pO%", false);
        assertNotNull(descriptors);
        assertEquals(6, descriptors.size());
        for (DomainObjectDescriptor descr : descriptors) {
            assertNotNull( descr.getName() );
            assertTrue( descr.getName().length() >= 14 );
            assertEqualsIgnoreCase('p', descr.getName().charAt(12));
            assertEqualsIgnoreCase('o', descr.getName().charAt(13));
        }
        descriptors = lm.getEntityDescriptors( EntityType.COMPONENT, "APPLICATION/POS%", true );
        assertNotNull(descriptors);
        assertEquals(3, descriptors.size());
        for (DomainObjectDescriptor descr : descriptors) {
            assertNotNull(descr.getName());
            assertTrue( descr.getName().length() >= 15 );
            assertEqualsIgnoreCase('p', descr.getName().charAt(12));
            assertEqualsIgnoreCase('o', descr.getName().charAt(13));
            assertEqualsIgnoreCase('s', descr.getName().charAt(14));
        }
        descriptors = lm.getEntityDescriptors( EntityType.COMPONENT, "APPLICATION/pol%", false );
        assertNotNull( descriptors );
        assertEquals( 2, descriptors.size() );
        for (DomainObjectDescriptor descr : descriptors) {
            assertNotNull( descr.getName() );
            assertTrue( descr.getName().length() >= 15 );
            assertEqualsIgnoreCase('p', descr.getName().charAt(12));
            assertEqualsIgnoreCase('o', descr.getName().charAt(13));
            assertEqualsIgnoreCase('l', descr.getName().charAt(14));
        }
    }
    
    public void testGetIdsByTemplate() throws PQLException, EntityManagementException {
        List<EntityType> types = new LinkedList<EntityType>();
        types.add(EntityType.POLICY);
        types.add(EntityType.COMPONENT);
        Collection<Long> ids = lm.getEntityIDs(types, "%o%");
        assertNotNull(ids);
        assertFalse(ids.isEmpty());
        Collection<DevelopmentEntity> dev = lm.getEntitiesForIDs(ids);
        assertNotNull(dev);
        assertEquals(ids.size(), dev.size());
        for (DevelopmentEntity de : dev) {
            assertNotNull(de);
            String name = de.getName();
            assertTrue( name.indexOf('o') != -1 || name.indexOf('O') != -1 );
            assertTrue( de.getType() == EntityType.POLICY || de.getType() == EntityType.COMPONENT);
        }
    }

    public void testGetAllEntities() throws PQLException, EntityManagementException {
        // When we do not specify types, we get an empty collection back.
        assertTrue(lm.getAllEntitiesOfType(Collections.EMPTY_LIST).isEmpty());
        // We should get several entities back for policies and applications
        List<EntityType> types = new LinkedList<EntityType>();
        types.add(EntityType.POLICY);
        types.add(EntityType.COMPONENT);
        Collection<DevelopmentEntity> res = lm.getAllEntitiesOfType(types);
        assertNotNull(res);
        assertEquals(19, res.size());
        for (DevelopmentEntity dev : res) {
            assertNotNull(dev.getId());
        }
    }

    public void testGetByID() throws PQLException, EntityManagementException {
        Collection<DevelopmentEntity> tmp = lm.getEntitiesForNames(
                EntityType.POLICY, Arrays.asList(SAVED_POLICY_NAMES), LifecycleManager.MUST_EXIST );
        assertEquals(SAVED_POLICY_NAMES.length, tmp.size());
        List<Long> ids = new LinkedList<Long>();
        for (DevelopmentEntity dev : tmp) {
            assertNotNull(dev);
            assertNotNull(dev.getId());
            ids.add(dev.getId());
        }
        Collection<DevelopmentEntity> res = lm.getEntitiesForIDs(ids);
        assertEquals(tmp.size(), res.size());
        for (DevelopmentEntity dev : res) {
            assertNotNull(dev);
            assertNotNull(dev.getId());
            assertTrue(ids.contains(dev.getId()));
        }
    }

    /**
     * Verifies that deployment code checks for the date and the approval status.
     * @throws Exception when one is thrown by the lifecycle manager.
     */
    public void testIncorrectDeploymentAttempts() throws PQLException, EntityManagementException {
        Collection<DevelopmentEntity> tmp = lm.getEntitiesForNames(
                EntityType.POLICY, Arrays.asList(SAVED_POLICY_NAMES), LifecycleManager.MUST_EXIST );
        try {
            lm.deployEntities(
                    tmp
                  , date("05/05/2005 05:05:05")
                  , DeploymentType.PRODUCTION
                  , date("01/01/2005 00:00:00")
                  , false
                  , null
            );
            fail( "The operation above must have resulted in an exception due to non-approved status of some entities" );
        } catch ( EntityManagementException eme ) {
            // We're supposed to get here
        }
    }

    public void testDeploy() throws PQLException, EntityManagementException {
        Collection<DevelopmentEntity> tmp = lm.getEntitiesForNames(
                EntityType.POLICY, Arrays.asList(SAVED_POLICY_NAMES), LifecycleManager.MUST_EXIST);
        for (DevelopmentEntity dev : tmp) {
            PQLTestUtils.setStatus(dev, DevelopmentStatus.APPROVED);
            PQLTestUtils.setDescription(dev, "first");
        }
        lm.deployEntities(
                tmp
              , date("05/05/2005 05:05:05")
              , DeploymentType.PRODUCTION
              , date("01/01/2005 00:00:00")
              , false
              , null
        );
        Collection<DeploymentEntity> dep = lm.getDeployedEntitiesForNames(
                EntityType.POLICY
              , Arrays.asList(SAVED_POLICY_NAMES)
              , date("05/06/2005 06:06:06")
              , DeploymentType.PRODUCTION
        );
        assertEquals(tmp.size(), dep.size());
        for (DeploymentEntity de : dep) {
            assertEquals(de.getDescription(), "first");
            assertTrue(de.getPql().contains("DESCRIPTION \"first\""));
            DeploymentRecord dr = de.getDeploymentRecord();
            assertNotNull("deployment record should not be null", dr);
        }
        for (DevelopmentEntity dev : tmp) {
            PQLTestUtils.setDescription( dev, "next" );
        }
        lm.deployEntities(
                tmp
              , date("05/06/2005 06:06:06")
              , DeploymentType.PRODUCTION
              , date("01/01/2005 00:00:00")
              , false
              , null
        );
        dep = lm.getDeployedEntitiesForEntities(tmp, date("05/07/2005 07:07:07"), DeploymentType.PRODUCTION);
        assertEquals(tmp.size(), dep.size());
        for (DeploymentEntity de : dep) {
            assertEquals(de.getDescription(), "next");
            assertTrue(de.getPql().contains("DESCRIPTION \"next\""));
        }
    }

    public void testGetDeploymentEntitiesForIDs() throws PQLException, EntityManagementException {
        Collection<DeploymentEntity> dep = lm.getAllDeployedEntities(
                date( "05/05/2005 06:06:06" ), DeploymentType.PRODUCTION);
        assertTrue("Number of deployed entities should be > 0", dep.size() > 0);
        List<Long> ids = new ArrayList<Long>(dep.size());
        for (DeploymentEntity de : dep) {
            Long id = de.getDevelopmentEntity().getId();
            ids.add(id);
        }

        Collection<DeploymentEntity> newDep = lm.getDeploymentEntitiesForIDs(
                ids, date( "05/05/2005 06:06:06" ), DeploymentType.PRODUCTION);
        assertNotNull("deployment entities should not be null", newDep);
        assertEquals("number deployment entities should be correct", dep.size(), newDep.size());
        assertEquals("number deployment entities should be correct", ids.size(), newDep.size());
        for (DeploymentEntity de : newDep) {
            assertNotNull(de);
        }
    }

    public void testEntityDeploymentHistory() throws PQLException, EntityManagementException {
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, "quick", LifecycleManager.MUST_EXIST );
        Collection<DeploymentHistory> times = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION );
        assertEquals(2, times.size());
        Iterator<DeploymentHistory> iter = times.iterator();
        assertEquals( new TimeRelation(date("05/05/2005 05:05:05"), date("05/06/2005 06:06:06"))
                , iter.next().getTimeRelation() );
        assertEquals( new TimeRelation(date("05/06/2005 06:06:06"), UnmodifiableDate.END_OF_TIME)
                , iter.next().getTimeRelation() );
        iter = times.iterator();
        DeploymentEntity d1 = lm.getDeployedEntityForEntity(
                dev
              , iter.next().getTimeRelation().getActiveFrom()
              , DeploymentType.PRODUCTION );
        DeploymentEntity d2 = lm.getDeployedEntityForEntity( 
                dev
                , iter.next().getTimeRelation().getActiveFrom()
                , DeploymentType.PRODUCTION );
        assertTrue(d1.getPql().contains("DESCRIPTION \"first\""));
        assertTrue(d2.getPql().contains("DESCRIPTION \"next\""));
    }

    public void testGetAllDeployedEntities() throws PQLException, EntityManagementException {
        DevelopmentEntity de = lm.getEntityForName(
                EntityType.COMPONENT, "APPLICATION/app-to-deploy", LifecycleManager.MAKE_EMPTY);
        PQLTestUtils.setStatus( de, DevelopmentStatus.APPROVED );
        lm.deployEntities( 
                Arrays.asList(de)
              , date("05/05/2005 05:05:05")
              , DeploymentType.PRODUCTION
              , date("05/05/2005 05:03:05")
              , false
              , null
        );
        Collection<DeploymentEntity> dep = lm.getAllDeployedEntities(
                date( "05/05/2005 06:06:06" ), DeploymentType.PRODUCTION);
        assertNotNull( dep );
        assertEquals( 8, dep.size() );
        int[] counts = new int[EntityType.getElementCount()];
        for (DeploymentEntity dp : dep) {
            int theType = dp.getType().getType();
            // This is not a testing assertion: we assume this is true.
            assert theType < counts.length;
            counts[theType]++;
        }
        assertEquals("There should be seven deployed policies.", 
                7, counts[EntityType.POLICY.getType()]);
        assertEquals("There should be one deployed component.", 
                1, counts[EntityType.COMPONENT.getType()]);
    }

    public void testGetAllDeployedEntitiesCount() throws PQLException, EntityManagementException {
        DevelopmentEntity de = lm.getEntityForName(
                EntityType.COMPONENT, "APPLICATION/app-to-deploy", LifecycleManager.MAKE_EMPTY );
        PQLTestUtils.setStatus( de, DevelopmentStatus.APPROVED );
        String pql = de.getPql();
        int insertLoc = pql.indexOf("APPROVED")+9;// The length of "approved" + 1
        StringBuffer newPql = new StringBuffer(pql.substring(0, insertLoc-1));
        newPql.append(" hidden ").append(pql.substring(insertLoc));
        de.setPql(newPql.toString());
        lm.deployEntities(
                Arrays.asList(de)
              , date("05/05/2005 05:05:05")
              , DeploymentType.PRODUCTION
              , date("05/05/2005 05:03:05")
              , false
              , null
        );
        int count = lm.getAllDeployedEntitiesCount( 
                date( "05/05/2005 06:06:06" ), DeploymentType.PRODUCTION, true );
        assertEquals( 8, count );
        count = lm.getAllDeployedEntitiesCount(
                date( "05/05/2005 06:06:06" ), DeploymentType.PRODUCTION, true );
        assertEquals( 8, count );
    }

    public void testOutOfOrderDeployment() throws PQLException, EntityManagementException {
        Collection<DevelopmentEntity> tmp = lm.getEntitiesForNames(
                EntityType.POLICY, Arrays.asList( SAVED_POLICY_NAMES ), LifecycleManager.MUST_EXIST );
        for (DevelopmentEntity dev : tmp) {
            PQLTestUtils.setDescription( dev, "latest" );
        }
        lm.deployEntities( 
                tmp
              , date("01/02/2005 01:01:01")
              , DeploymentType.PRODUCTION
              , date("01/01/2005 00:00:00")
              , false
              , null
        );
        Collection<DeploymentEntity> dep = lm.getDeployedEntitiesForNames(
                EntityType.POLICY
              , Arrays.asList(SAVED_POLICY_NAMES)
              , date("05/06/2005 06:06:06")
              , DeploymentType.PRODUCTION
        );
        assertEquals( tmp.size(), dep.size() );
        for (DeploymentEntity de : dep) {
            assertTrue( de.getPql().contains( "DESCRIPTION \"latest\"" ));
        }
    }

    public void testDeploymentHistoryAfterOutOfOrderDeployment() throws PQLException, EntityManagementException {
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, "quick", LifecycleManager.MUST_EXIST );
        Collection<DeploymentHistory> times = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION );
        assertEquals( 1, times.size() );
        Iterator<DeploymentHistory> iter = times.iterator();
        assertEquals( new TimeRelation( date("01/02/2005 01:01:01"), UnmodifiableDate.END_OF_TIME)
                , iter.next().getTimeRelation() );
        iter = times.iterator();
        DeploymentEntity dep = lm.getDeployedEntityForEntity(
                dev, iter.next().getTimeRelation().getActiveFrom(), DeploymentType.PRODUCTION );
        assertTrue( dep.getPql().contains( "DESCRIPTION \"latest\"" ));
    }

   /**
     * Verifies that undeployment code checks for the date.
     * @throws Exception when one is thrown by the lifecycle manager.
     */
    public void testIncorrectUndeploymentAttempts() throws PQLException, EntityManagementException {
        Collection<DeploymentEntity> dep = lm.getDeployedEntitiesForNames(
                EntityType.POLICY
              , Arrays.asList(SAVED_POLICY_NAMES)
              , date("05/06/2005 06:06:06")
              , DeploymentType.PRODUCTION
        );
        assertNotNull( dep );
        assertFalse( dep.isEmpty() );
        DeploymentEntity de = (DeploymentEntity)dep.iterator().next();
        DeploymentRecord rec = de.getDeploymentRecord();
        try {
            lm.cancelDeploymentOfEntities( rec, date("01/01/2006 00:00:00") );
            fail( "The operation above must have resulted in an exception due to an attempt of late undeployment." );
        } catch ( EntityManagementException eme ) {
            // We're supposed to get here
        }
    }

    public void testPurgeOverridenDeployments() throws PQLException, EntityManagementException {
        Collection<DevelopmentEntity> tmp = lm.getEntitiesForNames(
                EntityType.POLICY
              , Arrays.asList(SAVED_POLICY_NAMES)
              , LifecycleManager.MUST_EXIST
        );
        for (DevelopmentEntity dev : tmp) {
            PQLTestUtils.setDescription(dev, "ultimate");
        }
        lm.deployEntities(
                tmp
              , date("10/10/2005 10:10:10")
              , DeploymentType.PRODUCTION
              , date("07/07/2005 07:07:07")
              , false
              , null
        );
        Collection<DeploymentEntity> dep = lm.getDeployedEntitiesForNames(
                EntityType.POLICY
              , Arrays.asList( SAVED_POLICY_NAMES)
              , date("11/11/2005 11:11:11")
              , DeploymentType.PRODUCTION
        );
        assertEquals( tmp.size(), dep.size() );
        for (DeploymentEntity de : dep) {
            assertTrue( de.getPql().contains( "DESCRIPTION \"ultimate\"" ));
        }
    }

    public void testCreatingNewEntitiesOnTopOfDeletedOnes() throws PQLException, EntityManagementException {
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, "delete-create", LifecycleManager.MAKE_EMPTY );
        PQLTestUtils.setStatus( dev, DevelopmentStatus.APPROVED );
        lm.saveEntity( dev, LifecycleManager.MUST_EXIST, null );
        dev = lm.getEntityForName( EntityType.POLICY, "delete-create", LifecycleManager.MUST_EXIST );
        assertNotNull( dev );
        assertNotNull( dev.getId() );
        Long oldId = dev.getId();
        for ( int i = 0 ; i != 10 ; i++ ) {
            dev = lm.getEntityForName( EntityType.POLICY, "delete-create", LifecycleManager.MUST_EXIST );
            PQLTestUtils.setStatus( dev, DevelopmentStatus.DELETED );
            lm.saveEntity( dev, LifecycleManager.MUST_EXIST, null );
            // Now the entity is deleted - verify that we get
            // a new entity if we try requesting it.
            dev = lm.getEntityForName( EntityType.POLICY, "delete-create", LifecycleManager.MAKE_EMPTY );
            assertNotNull( dev );
            assertSame( DevelopmentStatus.NEW, dev.getStatus() );
            // The new entity must have a different ID
            assertFalse( oldId.equals( dev.getId() ) );
            lm.saveEntity( dev, LifecycleManager.MUST_EXIST, null );
        }
    }

    public void testGlobalDeploymentHistory() throws PQLException, EntityManagementException {
        Date from = date("01/01/2005 00:00:00");
        Date to = date("10/10/2005 00:00:00");
        Collection<DeploymentRecord> c = lm.getDeploymentHistory( from, to );
        assertNotNull( c );
        assertEquals( 2, c.size() );
        for (DeploymentRecord dr : c) {
            assertTrue( from.before( dr.getAsOf() ) || from.equals( dr.getAsOf() ) );
            assertTrue( to.after( dr.getAsOf() ) || to.equals( dr.getAsOf() ) );
        }
        c = lm.getDeploymentHistory( null, date("01/01/2005 00:00:01") );
        assertNotNull( c );
        assertEquals( 0, c.size() );
        c = lm.getDeploymentHistory( date("01/01/2005 00:00:01"), null );
        assertNotNull( c );
        assertEquals( 3, c.size() );
    }

    public void testDeploymentRecordsForDeploymentRecord() throws PQLException, EntityManagementException {
        Collection<DeploymentRecord> c = lm.getDeploymentHistory( null, null );
        assertEquals( 3, c.size() );
        int[] countsNeed = new int[] { 1, 7, 7 };
        int[] countsHave = new int[countsNeed.length];
        int pos = 0;
        for (DeploymentRecord rec : c) {
            Collection<DeploymentEntity> d = lm.getDeployedEntitiesForDeploymentRecord( rec );
            countsHave[pos++] = d.size();
        }
        Arrays.sort( countsHave );
        for ( int i = 0 ; i != countsNeed.length ; i++ ) {
            assertEquals( countsNeed[i], countsHave[i] );
        }
    }

    public void testDeployCancelCycle() throws PQLException, EntityManagementException {
        String[] names = new String[] { "flip", "flop" };
        Date[] seq = new Date[] { 
                date("05/05/2005 00:00:00")
              , date("05/04/2005 00:00:00")
              , date("05/03/2005 00:00:00")
              , date("05/02/2005 00:00:00")
              , date("05/01/2005 00:00:00") 
        };

        // Make the development entities
        Collection<DevelopmentEntity> devs = lm.getEntitiesForNames(
                EntityType.POLICY, Arrays.asList( names ), LifecycleManager.MAKE_EMPTY );
        for (DevelopmentEntity dev : devs) {
            PQLTestUtils.setStatus(dev, DevelopmentStatus.APPROVED);
        }
        assertEquals( 2, devs.size() );

        // Deploy the base
        for (DevelopmentEntity dev : devs) {
            PQLTestUtils.setDescription( dev, "base" );
        }
        lm.deployEntities(
                devs
              , date("01/01/2000 00:00:00")
              , DeploymentType.PRODUCTION
              , date("01/01/1999 00:00:00")
              , false
              , null
        );

        // Deploy five versions in reverse order of time
        Date now = date( "01/01/2005 00:00:00" );
        Date then = date( "10/10/2010 00:00:00" );

        for ( int i = 0 ; i != seq.length ; i++ ) {
            for (DevelopmentEntity dev : devs) {
                PQLTestUtils.setDescription( dev, "base "+i);
            }
            lm.deployEntities( devs, seq[i], DeploymentType.PRODUCTION, now, false, null);
            // Make sure that the deployment was successful
            Collection<DeploymentEntity> deps = lm.getDeployedEntitiesForEntities(
                    devs, then, DeploymentType.PRODUCTION );
            assertEquals( 2, deps.size() );
            for (DeploymentEntity dep : deps) {
                assertEquals( seq[i], dep.getActiveFrom() );
                assertTrue( dep.getPql().contains( "DESCRIPTION \"base "+i+"\"" ));
            }
        }

        // Undeploy the entities starting from the last one
        for ( int i = 0 ; i != seq.length ; i++ ) {
            Collection<DeploymentEntity> deps = lm.getDeployedEntitiesForEntities(
                    devs, then, DeploymentType.PRODUCTION);
            assertNotNull( deps );
            assertEquals( 2, deps.size() );
            DeploymentRecord rec = null;
            for (DeploymentEntity dep : deps) {
                rec = dep.getDeploymentRecord();
                assertEquals( seq[seq.length-1-i], dep.getActiveFrom() );
                assertTrue( dep.getPql().contains( "DESCRIPTION \"base "+(seq.length-1-i)+"\""));
            }
            assertNotNull( rec );
            lm.cancelDeploymentOfEntities( rec, now );
            // Ensure that the base is now back in place for the removed time interval
            // Get the date immediately preceeding the seq[...] value
            Calendar cal = Calendar.getInstance();
            cal.setTime( seq[seq.length-1-i]);
            cal.add( Calendar.SECOND, -1 );
            Date toCheck = UnmodifiableDate.forTime( cal.getTimeInMillis() );
            // Check the value on the date of toCheck
            deps = lm.getDeployedEntitiesForEntities( devs, toCheck, DeploymentType.PRODUCTION );
            assertNotNull( deps );
            assertEquals( 2, deps.size() );
            for (DeploymentEntity dep : deps) {
                assertTrue( "Unexpected record on "+toCheck, dep.getPql().contains( "DESCRIPTION \"base\"" ));
            }
        }
        // Check that we are now back to the base
        for (DeploymentEntity dep : lm.getDeployedEntitiesForEntities(devs, then, DeploymentType.PRODUCTION)) {
            assertTrue( dep.getPql().contains( "DESCRIPTION \"base\"" ));
        }
    }

    public void testUndeployment() throws PQLException, EntityManagementException {
        String[] names = new String[] { "blip", "blop" };

        // Make the development entities
        Collection<DevelopmentEntity> devs = lm.getEntitiesForNames(
                EntityType.POLICY, Arrays.asList( names ), LifecycleManager.MAKE_EMPTY );
        assertEquals( 2, devs.size() );

        // Deploy and undeploy five versions
        Date now = date( "01/01/2005 00:00:00" );
        Date then = date( "10/10/2010 00:00:00" );

        Calendar cal = Calendar.getInstance();
        cal.setTime( date( "05/01/2005 00:00:00" ) );
        DeploymentRecord rec = null;
        for ( int i = 0 ; i != 5 ; i++ ) {
            // Deploy the record with a PQL string indicating the iteration number
            for (DevelopmentEntity dev : devs) {
                PQLTestUtils.setDescription( dev, ""+i );
                PQLTestUtils.setStatus( dev, DevelopmentStatus.APPROVED );
            }
            lm.deployEntities( devs, cal.getTime(), DeploymentType.PRODUCTION, now, false, null);
            // Make sure that the deployment was successful
            Collection<DeploymentEntity> deps = lm.getDeployedEntitiesForEntities(
                    devs, then, DeploymentType.PRODUCTION );
            assertEquals( 2, deps.size() );
            for (DeploymentEntity dep : deps) {
                assertEquals( cal.getTime(), dep.getActiveFrom() );
                assertTrue( dep.getPql().contains( "DESCRIPTION \"" + i +"\"" ));
            }

            for (DevelopmentEntity dev : devs) {
                PQLTestUtils.setStatus( dev, DevelopmentStatus.OBSOLETE );
            }
            // Now undeploy it 12 hours later
            cal.add( Calendar.HOUR, 12 );
            rec = lm.undeployEntities( devs, cal.getTime(), DeploymentType.PRODUCTION, now, false, null);
            // Make sure the undeployment was successful
            Collection<DeploymentEntity> undeps = lm.getDeployedEntitiesForEntities(
                    devs, then, DeploymentType.PRODUCTION );
            assertTrue( undeps.isEmpty() );
            // Prepare the calendar for the next iteration
            cal.add( Calendar.HOUR, 12 );
        }
        cal.add( Calendar.HOUR, -13 );
        // Cancel the last undeployment
        assertNotNull( rec );
        lm.cancelDeploymentOfEntities( rec, cal.getTime() );
        // Ensure that our last deployment is back
        Collection<DeploymentEntity> deps = lm.getDeployedEntitiesForEntities(
                devs, then, DeploymentType.PRODUCTION );
        assertEquals( 2, deps.size() );
        cal.add( Calendar.HOUR, 1 );
        for (DeploymentEntity dep : deps) {
            assertEquals( cal.getTime(), dep.getActiveFrom() );
            assertTrue( dep.getPql().contains( "DESCRIPTION \"4\"" ));
        }
    }

    public void testPurge() throws PQLException, EntityManagementException {
        DevelopmentEntity dev = lm.getEntityForName( EntityType.POLICY, "purge", LifecycleManager.MAKE_EMPTY );
        PQLTestUtils.setStatus( dev, DevelopmentStatus.APPROVED );
        lm.deployEntities(
                Arrays.asList(dev)
              , date("01/01/3000 00:00:00")
              , DeploymentType.TESTING
              , date("01/01/2999 00:00:00")
              , false
              , null
        );
    }

    public void testConversionsToIDAndBack() throws PQLException, EntityManagementException {
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, "this-name-will-be-changed", LifecycleManager.MAKE_EMPTY );
        String initialPQL =
            "ID 0 STATUS EMPTY POLICY ConversionTest"
        +   " FOR (group=\"RESOURCE/Rg1\" OR group=\"RESOURCE/Rg2\")"
        +   " ON (group=\"ACTION/Ag1\" OR group=\"ACTION/Ag2\")"
        +   " BY ((GROUP=\"USER/Ug1\" OR GROUP=\"USER/Ug2\")" 
        +      " AND (GROUP=\"APPLICATION/Ap1\" OR GROUP=\"APPLICATION/Ap2\")"
        +	   " AND (GROUP=\"HOST/Hg1\" OR GROUP=\"HOST/Hg2\"))"
        +   " DO deny";
        dev.setPql( initialPQL );
        lm.saveEntity( dev, LifecycleManager.MAKE_EMPTY, null );
        assertEquals( "ConversionTest", dev.getName() );
        // Remove all spaces and IDs from the resulting PQL before comparing...
        String pql = dev.getPql().replaceAll("[0-9\\n\\r]", "").replaceAll(" +", " ");
        String expected = "ID STATUS EMPTY POLICY ConversionTest"
        +   " FOR (ID OR ID )"
        +   " ON (ID OR ID )"
        +   " BY ((ID OR ID ) AND (ID OR ID ) AND (ID OR ID ))"
        +   " DO deny";
        assertEquals( expected, pql );
        // Convert the IDs back to names
        lm.resolveIds( Arrays.asList(dev) );
        String processedPQL = dev.getPql().replaceAll("[\\n\\r]", "").replaceAll(" +", " ").replaceAll("ID [0-9]+ ", "");
        assertEquals( initialPQL.substring(5).toLowerCase(), processedPQL.toLowerCase() );

        // Perform the same test on deployed entities: first, deploy the entities
        Collection<DevelopmentEntity> rg = lm.getEntitiesForNames(
                EntityType.COMPONENT, Arrays.asList("RESOURCE/Rg1", "RESOURCE/Rg2"), LifecycleManager.MAKE_EMPTY);
        Collection<DevelopmentEntity> ag = lm.getEntitiesForNames(
                EntityType.COMPONENT, Arrays.asList("ACTION/Ag1", "ACTION/Ag2"), LifecycleManager.MAKE_EMPTY);
        Collection<DevelopmentEntity> ug = lm.getEntitiesForNames(
                EntityType.COMPONENT, Arrays.asList("USER/Ug1", "USER/Ug2"), LifecycleManager.MAKE_EMPTY);
        Collection<DevelopmentEntity> pg = lm.getEntitiesForNames(
                EntityType.COMPONENT, Arrays.asList("APPLICATION/Ap1", "APPLICATION/Ap2"), LifecycleManager.MAKE_EMPTY);
        Collection<DevelopmentEntity> hg = lm.getEntitiesForNames(
                EntityType.COMPONENT, Arrays.asList("HOST/Hg1", "HOST/Hg2"), LifecycleManager.MAKE_EMPTY);
        Collection<DevelopmentEntity> all = new LinkedList<DevelopmentEntity>();
        all.addAll( rg );
        all.addAll( ag );
        all.addAll( ug );
        all.addAll( pg );
        all.addAll( hg );
        for (DevelopmentEntity d : all) {
            assertNotNull( d.getId() );
            PQLTestUtils.setStatus( d, DevelopmentStatus.APPROVED );
        }
        PQLTestUtils.setStatus( dev, DevelopmentStatus.APPROVED );
        all.add( dev );
        lm.deployEntities( 
                all
              , date("02/02/2007 00:00:00")
              , DeploymentType.TESTING
              , date("02/02/2005 00:00:00")
              , false
              , null
        );
        // Read back the deployed policy
        DeploymentEntity dep = lm.getDeployedEntityForEntity(
                dev, date("02/03/2007 00:00:00"), DeploymentType.TESTING );
        lm.resolveIds( Arrays.asList(dep), date("02/03/2007 00:00:00"), DeploymentType.TESTING );
        String deployedPQL = dep.getPql()
                .replaceAll("[\\n\\r]", "")
                .replaceAll(" +", " ")
                .replaceAll("ID [0-9]+ ", "");
        assertEquals( "status approved "+initialPQL.substring(18).toLowerCase(), deployedPQL.toLowerCase() );
    }

    public void testInvalidRename() throws PQLException, EntityManagementException {
        String[] names = new String[] { "to-be-renamed-1", "to-be-renamed-2", "to-be-renamed-3" };
        Collection<DevelopmentEntity> devCollection = lm.getEntitiesForNames(
                EntityType.POLICY, Arrays.asList( names ), LifecycleManager.MAKE_EMPTY );
        DevelopmentEntity[] devs = devCollection.toArray( new DevelopmentEntity[devCollection.size()] );
        assertEquals( 3, devs.length );
        devs[0].setPql("policy quick for empty on empty by empty do allow");
        devs[1].setPql("policy brown for empty on empty by empty do allow");
        devs[2].setPql("policy fox for empty on empty by empty do allow");
        try {
            lm.saveEntities( devCollection, LifecycleManager.MUST_EXIST, null );
            fail("The saveEntities call must have resulted in an exception");
        } catch ( DuplicateEntityException dee ) {
            assertEquals( 3, dee.getDuplicatedNames().size() );
            assertTrue( dee.getDuplicatedNames().contains("quick") );
            assertTrue( dee.getDuplicatedNames().contains("brown") );
            assertTrue( dee.getDuplicatedNames().contains("fox") );
        }
        devs[0].setPql("policy OK1 for empty on empty by empty do allow");
        devs[1].setPql("policy OK2 for empty on empty by empty do allow");
        devs[2].setPql("policy lazy for empty on empty by empty do allow");
        try {
            lm.saveEntities( devCollection, LifecycleManager.MUST_EXIST, null );
            fail("The saveEntities call must have resulted in an exception");
        } catch ( DuplicateEntityException dee ) {
            assertEquals( 1, dee.getDuplicatedNames().size() );
            assertTrue( dee.getDuplicatedNames().contains( "lazy" ) );
        }
        devs[2].setPql("policy OK3 for empty on empty by empty do allow");
        lm.saveEntities( devCollection, LifecycleManager.MUST_EXIST, null );
    }

    public void testEightAndEighteen() throws PQLException, EntityManagementException {
        String[] names = new String[] { "Eight", "Eighteen" };
        Collection<DevelopmentEntity> devCollection = lm.getEntitiesForNames(
                EntityType.POLICY, Arrays.asList( names ), LifecycleManager.MAKE_EMPTY );
        lm.saveEntities( devCollection, LifecycleManager.MUST_EXIST, null );
        Collection<DomainObjectDescriptor> reRead = lm.getEntityDescriptors(
                EntityType.POLICY, "%eight%", false );
        assertNotNull( reRead );
        assertEquals( 2, reRead.size() );
    }

    public void testImplicitPolicyFolders() throws Exception {
        Collection<DevelopmentEntity> devCollection = lm.getEntitiesForNames(
            EntityType.POLICY
        ,   Arrays.asList("Equities" + 
                PQLParser.SEPARATOR + "Trading" + 
                PQLParser.SEPARATOR + "Enforcement" + 
                PQLParser.SEPARATOR + "NoResearchViewing")
        ,   LifecycleManager.MAKE_EMPTY);
        lm.saveEntities( devCollection, LifecycleManager.MAKE_EMPTY, null );
        Collection<DomainObjectDescriptor> pfs = lm.getEntityDescriptors(
                EntityType.FOLDER, "Eq%", false );
        assertNotNull( pfs );
        assertEquals( 3, pfs.size() );
        DomainObjectDescriptor[] dods = new DomainObjectDescriptor[3];
        pfs.toArray( dods );
        assertEquals( "Equities", dods[0].getName() );
        assertEquals( "Equities" + PQLParser.SEPARATOR + "Trading", dods[1].getName() );
        assertEquals( "Equities" + PQLParser.SEPARATOR + "Trading" + PQLParser.SEPARATOR + "Enforcement", dods[2].getName() );
        Collection<DevelopmentEntity> pfCollection = lm.getEntitiesForNames(
            EntityType.FOLDER
        ,   Arrays.asList("Equities" + PQLParser.SEPARATOR + "Trading1" + PQLParser.SEPARATOR + "Enforcement1")
        ,   LifecycleManager.MAKE_EMPTY);
        lm.saveEntities( pfCollection, LifecycleManager.MAKE_EMPTY, null );
        pfs = lm.getEntityDescriptors( EntityType.FOLDER, "Eq%", false );
        assertNotNull( pfs );
        assertEquals( 5, pfs.size() );
        dods = new DomainObjectDescriptor[5];
        pfs.toArray( dods );
        assertEquals( "Equities", dods[0].getName() );
        assertEquals( "Equities" + PQLParser.SEPARATOR + "Trading", dods[1].getName() );
        assertEquals( "Equities" + PQLParser.SEPARATOR + "Trading" + PQLParser.SEPARATOR + "Enforcement", dods[2].getName() );
        assertEquals( "Equities" + PQLParser.SEPARATOR + "Trading1", dods[3].getName() );
        assertEquals( "Equities" + PQLParser.SEPARATOR + "Trading1" + PQLParser.SEPARATOR + "Enforcement1", dods[4].getName() );
    }

    public void testGetNewSaveAndGetEmpty() throws PQLException, EntityManagementException {
        String[] names = new String[] { "RESOURCE/GetNewSaveAndGetEmpty" };
        Collection<DevelopmentEntity> devCollection = lm.getEntitiesForNames(
                EntityType.COMPONENT, Arrays.asList( names ), LifecycleManager.MAKE_EMPTY );
        assertNotNull( devCollection );
        assertFalse( devCollection.isEmpty() );
        assertEquals( 1, devCollection.size() );
        DevelopmentEntity dev = (DevelopmentEntity)devCollection.iterator().next();
        assertSame( DevelopmentStatus.NEW, dev.getStatus() );
        lm.saveEntities( devCollection, LifecycleManager.MUST_EXIST, null );
        assertSame( DevelopmentStatus.EMPTY, dev.getStatus() );
        devCollection = lm.getEntitiesForNames(
                EntityType.COMPONENT, Arrays.asList( names ), LifecycleManager.MAKE_EMPTY );
        assertNotNull( devCollection );
        assertFalse( devCollection.isEmpty() );
        assertEquals( 1, devCollection.size() );
        dev = (DevelopmentEntity)devCollection.iterator().next();
        assertSame( DevelopmentStatus.EMPTY, dev.getStatus() );
    }

    public void testVeryLongNames() throws PQLException, EntityManagementException {
    	int len = 800;
        String[] names = new String[] {
            "RESOURCE/This name has 800 characters --"
        +   "--------------------------------------------------------"
        +   "----BBB---L-----U---U--EEEEE----------------------------"
        +   "----B--B--L-----U---U--E--------------------------------"
        +   "----BBB---L-----U---U--EEE------------------------------"
        +   "----B--B--L-----U---U--E--------------------------------"
        +   "----BBB---LLLL---UUU---EEEEE----------------------------"
        +   "--------------------------------------------------------"
        +   "--------------J-U---U-N---N--GGG--L----EEEEE-!!---------"
        +   "--------------J-U---U-NN--N-G-----L----E-----!!---------"
        +   "--------------J-U---U-N-N-N-G-GGG-L----EEE---!!---------"
        +   "----------J---J-U---U-N--NN-G---G-L----E----------------"
        +   "-----------JJJ---UUU--N---N--GGG--LLLL-EEEEE-!!---------"
        +   "--------------------------------------------------------"
        +   "--- yes, exactly 800 characters!" };
        assertEquals(len, names[0].length());
        Collection<DevelopmentEntity> devCollection = lm.getEntitiesForNames(
                EntityType.COMPONENT, Arrays.asList( names ), LifecycleManager.MAKE_EMPTY );
        assertNotNull( devCollection );
        assertFalse( devCollection.isEmpty() );
        assertEquals( 1, devCollection.size() );
        DevelopmentEntity dev = (DevelopmentEntity)devCollection.iterator().next();
        assertEquals( len, dev.getName().length() );
        lm.saveEntity( dev, LifecycleManager.MUST_EXIST, null );
        dev.setPql( dev.getPql().replaceAll( "STATUS EMPTY", "STATUS APPROVED" ) );
        lm.saveEntity( dev, LifecycleManager.MUST_EXIST, null );
        assertSame( DevelopmentStatus.APPROVED, dev.getStatus() );
        Collection<DomainObjectDescriptor> descrs = lm.getEntityDescriptors(
                EntityType.COMPONENT, "RESOURCE/this name has 800 characters%", false );
        assertNotNull( descrs );
        assertEquals( 1, descrs.size() );
        DomainObjectDescriptor[] dod = descrs.toArray( new DomainObjectDescriptor[1] );
        assertEquals( names[0], dod[0].getName() );
        lm.deployEntities(
                devCollection
              , date("05/05/2005 05:05:05")
              , DeploymentType.PRODUCTION
              , date("01/01/2005 00:00:00")
              , false
              , null
        );
        Collection<DeploymentEntity> dep = lm.getDeployedEntitiesForEntities(
                devCollection, date("05/06/2005 06:06:06"), DeploymentType.PRODUCTION );
        assertNotNull( dep );
        assertEquals( 1, dep.size() );
        DeploymentEntity[] deps = dep.toArray( new DeploymentEntity[1] );
        assertEquals( names[0], deps[0].getName() );
    }

    public void testReadyForDeployment() throws PQLException, EntityManagementException {
        int cnt = lm.getCountOfEntitiesReadyForDeployment(
            date("05/05/2005 05:05:05")
        ,   Arrays.asList(
                EntityType.COMPONENT
            ,   EntityType.LOCATION
            ,   EntityType.POLICY
            ,   EntityType.FOLDER
            )
        );
        assertEquals( 11, cnt );
    }

    public void testCleanUp() throws HibernateException {
        lm.getSessionFactory().close();
    }

}
