package com.bluejungle.pf.destiny.lifecycle;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import junit.framework.Assert;
import junit.framework.TestSuite;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.utils.PQLTestUtils;

/**
 * workflow
 * setup 3 users. user1, user2 and user3
 * 
 *  
 * action                  | modifier | submitter | deployer(latest)
 * user1 create a policy   | 1        |           |
 * user2 re-save wo change | 1        |           |
 * user3 change            | 3        |           |
 * user2 submit            | 3        | 2         |
 * user3 deploy            | 3        | 2         | 3
 * user1 modify            | 3        |           |
 * user1 submit            | 3        | 1         |
 * user2 modify            | 3        |           |
 * unknown submit          | 3        | null      |
 * user1 deploy            | 3        | null      | 1
 * user2 deploy            | 3        | null      | 2
 * user1 OBSOLETE          | 3        | 1         |
 * user3 deactive          | 3        | 1         | 3
 * user2 delete            | 2        |           |
 * 
 * @author hchan
 *
 */
public class TestDeploymentAuditHistory extends PFTestWithDataSource {

    public static TestSuite suite() {
        return makeSingleInitializationSuite(TestDeploymentAuditHistory.class);
    }
    
    /*
     * user ids
     */
    private static final Long USER1 = new Long(11111);
    private static final Long USER2 = new Long(2222222);
    private static final Long USER3 = new Long(333333333);
    
    private static final String POLICY_NAME = "Recurvirostra";
    
    private class CheckPoint {
        LinkedList<Date> points = new LinkedList<Date>();
        
        void mark() {
            points.add(new Date());
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
        }
        
        int size(){
            return points.size();
        }
        
        void clear(){
            points.clear();
        }
       
        Date get(int index){
            return points.get(index);
        }
        
        Date assertWithin(Date date) {
            assertTrue(points.size() >= 2);
            Iterator<Date> iterator = points.descendingIterator();
            Date last = iterator.next();
            Date secondLast = iterator.next();
            
            assertFalse(date.getTime() + " is before " + secondLast.getTime(), date.before(secondLast));
            assertFalse(date.getTime() + " is after " + last.getTime(), date.after(last));
            return date;
        }
    }
    
    private static class ExpectAuditData {
        Date lastUpdated;
        Date created;
        Long owner;
        Date lastModified;
        Long modifier;
        Date submittedTime;
        Long submitter;
        
        void assertEquals(DevelopmentEntity dev) {
            Assert.assertEquals(lastUpdated, dev.getLastUpdated());
            Assert.assertEquals(created, dev.getCreated());
            Assert.assertEquals(owner, dev.getOwner());
            Assert.assertEquals(lastModified, dev.getLastModified());
            Assert.assertEquals(modifier, dev.getModifier());
            Assert.assertEquals(submittedTime, dev.getSubmittedTime());
            Assert.assertEquals(submitter, dev.getSubmitter());
        }
    }
    
    private static ExpectAuditData expectAuditData = new ExpectAuditData();
    private CheckPoint checkpoint;
    private LifecycleManager lm;
    
    protected void setUp() throws Exception {
        super.setUp();
        lm = cm.getComponent(LifecycleManager.COMP_INFO);
        checkpoint = new CheckPoint();
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
    
    private DevelopmentEntity submit(Long user) throws EntityManagementException, PQLException {
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, POLICY_NAME, LifecycleManager.MUST_EXIST);
        expectAuditData.assertEquals(dev);
        
        PQLTestUtils.setStatus(dev, DevelopmentStatus.APPROVED);
        
        checkpoint.mark();
        lm.saveEntity(dev, LifecycleManager.MUST_EXIST, user);
        checkpoint.mark();
        
        expectAuditData.lastUpdated = checkpoint.assertWithin(dev.getLastUpdated());
        expectAuditData.submittedTime = checkpoint.assertWithin(dev.getSubmittedTime());
        expectAuditData.submitter = user;
        
        expectAuditData.assertEquals(dev);
        
        return dev;
    }
    
    private DevelopmentEntity deploy(Long user) throws EntityManagementException {
        return deployOrUndeploy(user, true);
    }
    
    private DevelopmentEntity undeploy(Long user) throws EntityManagementException {
        return deployOrUndeploy(user, false);
    }
    
    private DevelopmentEntity deployOrUndeploy(Long user, boolean isDeploy) throws EntityManagementException {
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, POLICY_NAME, LifecycleManager.MUST_EXIST);
        expectAuditData.assertEquals(dev);
        
        checkpoint.mark();
        if (isDeploy) {
            lm.deployEntities(Collections.singleton(dev), new Date(),
                    DeploymentType.PRODUCTION, false, user);
        } else {
            lm.undeployEntities(Collections.singleton(dev), new Date(),
                    DeploymentType.PRODUCTION, false, user);
        }
        checkpoint.mark();
     
        expectAuditData.assertEquals(dev);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertFalse(history.isEmpty());
        
        DeploymentHistory latest = null;
        for(DeploymentHistory dh : history) {
            if (latest == null) {
                latest = dh;
            } else if (dh.getDeployTime().after(latest.getDeployTime())) {
                latest = dh;
            }
        }

        assertEquals(user, latest.getDeployer());
        checkpoint.assertWithin(latest.getDeployTime());
        assertEquals(dev.getLastModified(), latest.getLastModified());
        assertEquals(dev.getModifier(), latest.getModifier());
        assertEquals(dev.getSubmittedTime(), latest.getSubmittedTime());
        assertEquals(dev.getSubmitter(), latest.getSubmitter());
        
        return dev;
    }
    
    private DevelopmentEntity toDraft(Long user) throws EntityManagementException, PQLException {
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, POLICY_NAME, LifecycleManager.MUST_EXIST);
        expectAuditData.assertEquals(dev);
        
        PQLTestUtils.setStatus(dev, DevelopmentStatus.DRAFT);
        
        checkpoint.mark();
        lm.saveEntity(dev, LifecycleManager.MUST_EXIST, user);
        checkpoint.mark();
        
        expectAuditData.lastUpdated = checkpoint.assertWithin(dev.getLastUpdated());
        expectAuditData.submittedTime = null;
        expectAuditData.submitter = null;
        expectAuditData.assertEquals(dev);
        
        return dev;
    }
    
    public void testUser1CreatePolicy() throws EntityManagementException, PQLException {
        final Long user = USER1;
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, POLICY_NAME, LifecycleManager.MAKE_EMPTY);
        expectAuditData.assertEquals(dev);
        
        PQLTestUtils.setStatus(dev, DevelopmentStatus.DRAFT);
        checkpoint.mark();
        lm.saveEntity(dev, LifecycleManager.MUST_EXIST, user);
        checkpoint.mark();
        
        expectAuditData.lastUpdated = checkpoint.assertWithin(dev.getLastUpdated());
        expectAuditData.created = checkpoint.assertWithin(dev.getCreated());
        expectAuditData.lastModified = checkpoint.assertWithin(dev.getLastModified());
        expectAuditData.modifier = user;
        
        expectAuditData.assertEquals(dev);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertTrue(history.isEmpty());
    }
    
    public void testUser2SaveWithoutChanges() throws EntityManagementException {
        final Long user = USER2;
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, POLICY_NAME, LifecycleManager.MUST_EXIST);
        expectAuditData.assertEquals(dev);
        
        lm.saveEntity(dev, LifecycleManager.MUST_EXIST, user);
        expectAuditData.assertEquals(dev);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertTrue(history.isEmpty());
    }
    
    public void testUser3SaveWithChanges() throws EntityManagementException, PQLException {
        final Long user = USER3;
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, POLICY_NAME, LifecycleManager.MUST_EXIST);
        expectAuditData.assertEquals(dev);
        
        PQLTestUtils.setDescription(dev, "new Description");
        
        checkpoint.mark();
        lm.saveEntity(dev, LifecycleManager.MUST_EXIST, user);
        checkpoint.mark();
        
        expectAuditData.lastUpdated = checkpoint.assertWithin(dev.getLastUpdated());
        expectAuditData.lastModified = checkpoint.assertWithin(dev.getLastModified());
        expectAuditData.modifier = user;
        
        expectAuditData.assertEquals(dev);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertTrue(history.isEmpty());
    }
    
    public void testUser2Submit() throws EntityManagementException, PQLException {
        final Long user = USER2;
        DevelopmentEntity dev = submit(user);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertTrue(history.isEmpty());
    }
    
    public void testUser3Deploy() throws EntityManagementException {
        final Long user = USER3;
        DevelopmentEntity dev = deploy(user);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertEquals(1, history.size());
    }
    
    public void testUser1Modify() throws EntityManagementException, PQLException {
        final Long user = USER1;
        DevelopmentEntity dev = toDraft(user);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertEquals(1, history.size());
    }
    
    public void testUser1Submit() throws EntityManagementException, PQLException {
        final Long user = USER1;
        DevelopmentEntity dev = submit(user);

        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertEquals(1, history.size());
    }
    
    public void testUser2Modify() throws EntityManagementException, PQLException {
        final Long user = USER2;
        DevelopmentEntity dev = toDraft(user);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertEquals(1, history.size());
    }
    
    public void testUnknownSubmit() throws EntityManagementException, PQLException {
        final Long user = null;
        DevelopmentEntity dev = submit(user);

        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertEquals(1, history.size());
    }
    
    public void testUser1Deploy() throws EntityManagementException, PQLException {
        final Long user = USER1;
        DevelopmentEntity dev = deploy(user);

        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertEquals(2, history.size());
    }
    
    public void testUser1Obsolete() throws EntityManagementException, PQLException {
        final Long user = USER1;
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, POLICY_NAME, LifecycleManager.MUST_EXIST);
        expectAuditData.assertEquals(dev);
        
        PQLTestUtils.setStatus(dev, DevelopmentStatus.OBSOLETE);
        
        checkpoint.mark();
        lm.saveEntity(dev, LifecycleManager.MUST_EXIST, user);
        checkpoint.mark();
        
        expectAuditData.lastUpdated = checkpoint.assertWithin(dev.getLastUpdated());
        expectAuditData.submittedTime = checkpoint.assertWithin(dev.getSubmittedTime());
        expectAuditData.submitter = user;
        expectAuditData.assertEquals(dev);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertEquals(2, history.size());
    }
    
    public void testUser3Undeploy() throws EntityManagementException, PQLException {
        final Long user = USER3;
        DevelopmentEntity dev = undeploy(user);

        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertEquals(3, history.size());
    }
    
    public void testUser2Delete() throws EntityManagementException, PQLException {
        final Long user = USER2;
        DevelopmentEntity dev = lm.getEntityForName(
                EntityType.POLICY, POLICY_NAME, LifecycleManager.MUST_EXIST);
        expectAuditData.assertEquals(dev);
        
        PQLTestUtils.setStatus(dev, DevelopmentStatus.DELETED);
        
        checkpoint.mark();
        lm.saveEntity(dev, LifecycleManager.MUST_EXIST, user);
        checkpoint.mark();
        
        expectAuditData.lastUpdated = checkpoint.assertWithin(dev.getLastUpdated());
        expectAuditData.lastModified = checkpoint.assertWithin(dev.getLastModified());
        expectAuditData.modifier = user;
        expectAuditData.assertEquals(dev);
        
        Collection<DeploymentHistory> history = lm.getDeploymentHistoryForEntityId(
                dev.getId(), DeploymentType.PRODUCTION);
        assertEquals(3, history.size());
    }
}
