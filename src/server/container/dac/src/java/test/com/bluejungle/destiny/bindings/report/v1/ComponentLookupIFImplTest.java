/*
 * Created on Apr 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HostDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserGroupDO;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ComponentLookupIFImplTest.java#2 $
 */

public abstract class ComponentLookupIFImplTest extends BaseReportServiceTest {

    private static final Log LOG = LogFactory.getLog(ComponentLookupIFImplTest.class.getName());
    protected static final String HOST_NAME_PATTERN = "Host_";
    protected static final String HOST_CLASS_NAME_PATTERN = "HostClass_";
    protected static final String POLICY_NAME_PATTERN = "Policy_";
    protected static final String USER_CLASS_PATTERN = "UserClass_";
    protected static final String USER_CLASS_ENROLLMENT_PATTERN = "EnrollType_";
    protected static final String USER_NAME_PATTERN = "User_";
    protected static final String RANDOM_SEARCH_EXPR_ONE = "dsdfsdfsdfakjg2354up@#$@#$@#$#@$#@$#@$T$#^$%&%$&#%UDHQEWT#!%$!%!#$%";
    protected static final String RANDOM_SEARCH_EXPR_TWO = "\\\\\\\"K:JPYENGl;jd[f9utn;/abh){}}EIR(U{Dh:>?$fd-wui)]";
    protected static final String RANDOM_SEARCH_EXPR_THREE = "~!#$$&*IAETadl239470t;j;%$&#0=9a0=1u[q[yjhhw;;;;;";
    protected static final String RANDOM_SEARCH_EXPR_FOUR = "k;jup;w387r;34126%*70/''.;k@%&195-`7T&*o8-086`~~j;7-=-R)_+}|tiq";
    protected static final String RANDOM_SEARCH_EXPR_FIVE = "\\\\][][09951@#$@#^))|{\"?>:<~!@$^!/,./[\t?>{}#$%&>\"?>./.'\\[]435&$^35\"]";
    protected static int userSID = 0;
    
    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public ComponentLookupIFImplTest(String testName) {
        super(testName);
    }

    /**
     * Deletes all hosts
     */
    protected void deleteAllHosts() {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            s.delete("from HostDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to delete hosts");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Deletes all policies
     */
    protected void deleteAllPolicies() {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            s.delete("from PolicyDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to delete policies");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Deletes all users
     */
    protected void deleteAllUsers() {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            s.delete("from UserDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to delete users");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Deletes all user classes
     */
    protected void deleteAllUserClasses() {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            s.delete("from UserGroupDO");
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to delete user classes");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

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
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Inserts hosts in the database
     * 
     * @param startingId
     *            id of the first inserted record
     * @param nbHosts
     *            nb of records to insert
     */
    protected void insertHosts(Long startingId, int nbHosts) {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            for (int i = 0; i < nbHosts; i++) {
                HostDO host = new HostDO();
                int index = i + startingId.intValue();
                Long id = new Long(index);
                host.setId(id);
                host.setOriginalId(id);
                String hostName = HOST_NAME_PATTERN + index;
                host.setName(hostName);
                TimeRelation hostTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
                host.setTimeRelation(hostTimeRelation);
                s.save(host);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to insert host records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Insert host records
     * 
     * @param hostName
     *            name of the host class
     * @param Id
     *            id for the host class
     */
    protected void insertHost(String hostName, Long Id) {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            HostDO host = new HostDO();
            int index = Id.intValue();
            Long id = new Long(index);
            host.setId(id);
            host.setOriginalId(id);
            String className = hostName;
            host.setName(className);
            TimeRelation hostTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
            host.setTimeRelation(hostTimeRelation);
            s.save(host);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to insert host class records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }
    
    /**
     * Insert sample host records
     */
    protected void insertSampleHosts() {
        insertHost("tobago", new Long(0));
        insertHost("baixo", new Long(1));
        insertHost("treasure", new Long(2));
        insertHost("bikini", new Long(3));
        insertHost("cuba", new Long(4));
        insertHost("TONGA", new Long(5));
        insertHost("tonga2003", new Long(6));
        insertHost("STBARTS", new Long(7));
        insertHost("KADAVU", new Long(8));
        insertHost("angel", new Long(9));
        insertHost("azores", new Long(10));
        insertHost("DX2000-Safdar", new Long(11));
        insertHost("maui", new Long(12));
        insertHost("shikoku", new Long(13));
        insertHost("penang", new Long(14));
    }

    /**
     * Inserts policies in the database
     * 
     * @param startingId
     *            id of the first policy to be inserted
     * @param nbPolicies
     *            number of policies to insert
     */
    protected void insertPolicies(Long startingId, int nbPolicies) {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            for (int i = 0; i < nbPolicies; i++) {
                PolicyDO policy = new PolicyDO();
                int index = i + startingId.intValue();
                policy.setId(new Long(index));
                String policyName = POLICY_NAME_PATTERN + index;
                policy.setFullName(policyName);
                s.save(policy);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to insert policy records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Insert policy records
     * 
     * @param policyName
     *            name of the policy
     * @param Id
     *            id for the policy
     */
    protected void insertPolicy(String policyName, Long Id) {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            PolicyDO policy = new PolicyDO();
            int index = Id.intValue();
            policy.setId(new Long(index));
            String className = policyName;
            policy.setFullName(className);
            s.save(policy);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to insert host class records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }
    
    /**
     * Insert sampe policy records
     */
    protected void insertSamplePolicies() {
        insertPolicy("Open", new Long(0));
        insertPolicy("Copy", new Long(1));
        insertPolicy("Instant Messenger", new Long(2));
        insertPolicy("Move", new Long(3));
        insertPolicy("File Server", new Long(4));
        insertPolicy("Print", new Long(5));
        insertPolicy("Contractors Policy", new Long(6));
        insertPolicy("Standard", new Long(7));
        insertPolicy("HR Policy", new Long(8));
        insertPolicy("Security", new Long(9));
        insertPolicy("Backup", new Long(10));
        insertPolicy("Removable Storage", new Long(11));
        insertPolicy("Laptops", new Long(12));
        insertPolicy("Delete", new Long(13));
        insertPolicy("Email", new Long(14));
    }

    /**
     * Inserts users in the database
     * 
     * @param startingId
     *            id of the first inserted record
     * @param nbUsers
     *            nb of records to insert
     */
    protected void insertUsers(Long startingId, int nbUsers) {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            for (int i = 0; i < nbUsers; i++) {
                UserDO user = new UserDO();
                int index = i + startingId.intValue();
                user.setOriginalId(new Long(index));
                String userName = USER_NAME_PATTERN + index;
                user.setDisplayName(userName);
                user.setFirstName("FirstName_" + index);
                user.setLastName("LastName_" + index);
                String SID = "";
                if (i > 9){
                    SID = "S-1-5-21-668023798-3031861066-1043980994-26" + String.valueOf(i);
                } else {
                    SID = "S-1-5-21-668023798-3031861066-1043980994-260" + String.valueOf(i);
                }
                user.setSID(SID);
                TimeRelation userTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
                user.setTimeRelation(userTimeRelation);
                s.save(user);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to insert user records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    protected void insertUser(String lastName, String firstName, Long Id) {
        Session s = null;
        Transaction t = null;
        String last = "last";
        String first = "first";
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            if (lastName != null) {
                last = lastName;
            }
            if (firstName != null) {
                first = firstName;
            }
            UserDO user = new UserDO();
            int index = Id.intValue();
            user.setOriginalId(new Long(index));
            String userName = firstName + "_" + lastName;
            user.setDisplayName(userName);
            user.setLastName(last);
            user.setFirstName(first);
            String SID = "";
            if (userSID > 9){
                SID = "S-1-5-21-668023798-3031861066-1043980994-26" + String.valueOf(userSID);
            } else {
                SID = "S-1-5-21-668023798-3031861066-1043980994-260" + String.valueOf(userSID);
            }
            user.setSID(SID);
            userSID = userSID + 1;
            TimeRelation userTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
            user.setTimeRelation(userTimeRelation);
            s.save(user);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to insert user records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }
    
    /**
     * Inserts sample users in the database
     */
    protected void insertSampleUsers() {
        insertUser("Lin", "Robert", new Long(0));
        insertUser("Hanen", "Iannis", new Long(1));
        insertUser("Rashid", "Fuad", new Long(2));
        insertUser("Tong", "Kevin", new Long(3));
        insertUser("Lim", "David", new Long(4));
        insertUser("Vladimirov", "Sasha", new Long(5));
        insertUser("Sarna", "Chander", new Long(6));
        insertUser("Kalinichenko", "Sergey", new Long(7));
        insertUser("Han", "Andy", new Long(8));
        insertUser("Keni", "Prabhat", new Long(9));
        insertUser("Goldstein", "Scott", new Long(10));
        insertUser("Kureishy", "Safdar", new Long(11));
        insertUser("Lim", "Keng", new Long(12));
        insertUser("Achanta", "Hima", new Long(13));
        insertUser("Ng", "Bobby", new Long(14));
    }

    /**
     * Insert user class records
     * 
     * @param startingId
     *            index of the first record
     * @param nbClasses
     *            nb of records to insert
     */
    protected void insertUserClasses(Long startingId, int nbClasses) {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            for (int i = 0; i < nbClasses; i++) {
                UserGroupDO userClass = new UserGroupDO();
                int index = i + startingId.intValue();
                userClass.setOriginalId(new Long(index));
                String className = USER_CLASS_PATTERN + index;
                userClass.setName(className);
                userClass.setDisplayName(USER_CLASS_PATTERN + "display" + index);
                TimeRelation userClassTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
                userClass.setTimeRelation(userClassTimeRelation);
                userClass.setEnrollmentType(USER_CLASS_ENROLLMENT_PATTERN + index);
                s.save(userClass);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to insert user class records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Insert host class records
     * 
     * @param hostClassName
     *            name of the host class
     * @param Id
     *            id for the host class
     */
    protected void insertUserClass(String userClassName, Long Id) {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDateSource().getSession();
            t = s.beginTransaction();
            UserGroupDO userClass = new UserGroupDO();
            int index = Id.intValue();
            userClass.setOriginalId(new Long(index));
            String className = userClassName;
            userClass.setName(className);
            userClass.setDisplayName(className);
            TimeRelation userClassTimeRelation = new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME);
            userClass.setTimeRelation(userClassTimeRelation);
            userClass.setEnrollmentType("ActiveDirectoryEnrollment");
            s.save(userClass);
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            fail("failed to insert user class records");
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }
    
    /**
     * Insert sample user class records
     */
    protected void insertSampleUserClasses() {
        insertUserClass("Engineering", new Long(0));
        insertUserClass("Finance", new Long(1));
        insertUserClass("Human Resource", new Long(2));
        insertUserClass("Executive", new Long(3));
        insertUserClass("Quality Assurance", new Long(4));
        insertUserClass("Offsite", new Long(5));
        insertUserClass("Laptops", new Long(6));
        insertUserClass("Desktops", new Long(7));
        insertUserClass("Servers", new Long(8));
        insertUserClass("Windows XP", new Long(9));
        insertUserClass("Linux", new Long(10));
        insertUserClass("Windows 2003", new Long(11));
        insertUserClass("Marketing", new Long(12));
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllHosts();
        deleteAllPolicies();
        deleteAllUsers();
        deleteAllUserClasses();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        deleteAllHosts();
        deleteAllPolicies();
        deleteAllUsers();
        deleteAllUserClasses();
        super.tearDown();
    }
}
