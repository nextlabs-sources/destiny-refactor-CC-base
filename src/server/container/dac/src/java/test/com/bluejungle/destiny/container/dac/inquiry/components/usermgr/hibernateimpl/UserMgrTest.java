/*
 * Created on Apr 11, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dac.inquiry.components.usermgr.hibernateimpl;

import com.bluejungle.destiny.container.dac.BaseDACComponentTestCase;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserClassMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgr;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQueryTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserClassMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserClassMgrSortFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserMgrQueryFieldType;
import com.bluejungle.destiny.container.dac.inquiry.components.usermgr.UserMgrSortFieldType;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUserGroup;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.SampleDataMgr;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserGroupDO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.utils.SortDirectionType;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * This is the test class for the user manager component.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/container/dac/inquiry/components/usermgr/hibernateimpl/UserMgrTest.java#1 $
 */

public class UserMgrTest extends BaseDACComponentTestCase {

    private SampleDataMgr sampleDataMgr = new SampleDataMgr();

    /**
     * Returns the sample data manager
     * 
     * @return the sample data manager
     */
    protected SampleDataMgr getSampleDataMgr() {
        return this.sampleDataMgr;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        deleteUserAndGroupRecords();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        deleteUserAndGroupRecords();
        super.tearDown();
    }

    /**
     * Returns the activity data source
     * 
     * @return the activity data source
     */
    protected IHibernateRepository getActivityDataSource() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) compMgr.getComponent(DestinyRepository.ACTIVITY_REPOSITORY.getName());
        return dataSource;
    }

    /**
     * Returns a user manager instance
     * 
     * @return a user manager instance
     */
    protected UserMgrImpl getUserMgr() {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IUserMgr.DATASOURCE_CONFIG_PARAM, getActivityDataSource());
        ComponentInfo info = new ComponentInfo("userMgr", UserMgrImpl.class.getName(), IUserMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        return (UserMgrImpl) compMgr.getComponent(info);
    }

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test to run
     */
    public UserMgrTest(String testName) {
        super(testName);

    }

    /**
     * Deletes all the user records from the database.
     */
    protected void deleteUserAndGroupRecords() throws HibernateException {
        Session s = null;
        try {
            s = getActivityDataSource().getSession();
            this.sampleDataMgr.deleteUsersAndGroups(s);
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Inserts a number of user records
     * 
     * @throws HibernateException
     */
    protected void insertMixedCaseUserRecords() throws HibernateException {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            String[] nameList = new String[] { "newUser", "NewUser2", "readUser", "WriteUser" };
            int size = nameList.length;
            for (int i = 0; i < size; i++) {
                UserDO newUser = new UserDO();
                newUser.setOriginalId(new Long(i));
                newUser.setDisplayName(nameList[i]);
                newUser.setFirstName(nameList[i]);
                newUser.setLastName(nameList[i]);
                newUser.setSID(nameList[i]);
                newUser.setTimeRelation(new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME));
                s.save(newUser);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Inserts a number of user records
     * 
     * @throws HibernateException
     */
    protected void insertMixedCaseUserGroupRecords() throws HibernateException {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            String[] nameList = new String[] { "newUserGroup", "NewUserGroup2", "readUserGroup", "WriteUserGroup" };
            int size = nameList.length;
            for (int i = 0; i < size; i++) {
                UserGroupDO newUserGroup = new UserGroupDO();
                newUserGroup.setOriginalId(new Long(i));
                newUserGroup.setDisplayName(nameList[i]);
                newUserGroup.setName(nameList[i]);
                newUserGroup.setTimeRelation(new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME));
                newUserGroup.setEnrollmentType("ActiveDirectoryEnrollment");
                s.save(newUserGroup);
            }
            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Insert a number of user records
     * 
     * @param nbUsers
     *            nb of users to insert
     * @throws HibernateException
     */
    protected void insertUserRecords(final int nbUsers) throws HibernateException {
        insertUserRecords(nbUsers, new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME));
    }

    /**
     * Insert a number of user group records
     * 
     * @param nbUsers
     *            nb of users to insert
     * @throws HibernateException
     */
    protected void insertUserGroupRecords(final int nbUsers) throws HibernateException {
        insertUserGroupRecords(nbUsers, new TimeRelation(UnmodifiableDate.START_OF_TIME, UnmodifiableDate.END_OF_TIME));
    }

    /**
     * Inserts a number of user records
     * 
     * @param nbUsers
     *            number of user records to insert
     * @param tr
     *            time relation for the user
     * @throws HibernateException
     */
    protected void insertUserRecords(final int nbUsers, TimeRelation tr) throws HibernateException {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            for (int i = 0; i < nbUsers; i++) {
                UserDO newUser = new UserDO();
                newUser.setOriginalId(new Long(i));
                newUser.setDisplayName("User_" + i);
                newUser.setFirstName("FirstName_" + i);
                newUser.setLastName("LastName_" + i);
                newUser.setSID("SID_" + i);
                newUser.setTimeRelation(tr);
                s.save(newUser);
            }

            // Add also the users that should never be queried
            UserDO unknownUser = new UserDO();
            unknownUser.setOriginalId(IHasId.UNKNOWN_ID);
            unknownUser.setDisplayName(IHasId.UNKNOWN_NAME);
            unknownUser.setFirstName("Unknown");
            unknownUser.setLastName("Unknown");
            unknownUser.setSID("Unknown");
            unknownUser.setTimeRelation(tr);
            s.save(unknownUser);

            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * Inserts a number of user group records
     * 
     * @param nbUsers
     *            number of user records to insert
     * @param tr
     *            time relation for the user
     * @throws HibernateException
     */
    protected void insertUserGroupRecords(final int nbUsers, TimeRelation tr) throws HibernateException {
        Session s = null;
        Transaction t = null;
        try {
            s = getActivityDataSource().getSession();
            t = s.beginTransaction();
            for (int i = 0; i < nbUsers; i++) {
                UserGroupDO newUserGroup = new UserGroupDO();
                newUserGroup.setOriginalId(new Long(i));
                newUserGroup.setDisplayName("UserGroup_" + i);
                newUserGroup.setName("Name_" + i);
                newUserGroup.setTimeRelation(tr);
                newUserGroup.setEnrollmentType("EnrollmentType_" + i);
                s.save(newUserGroup);
            }

            t.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    /**
     * This test verifies the basic features of the user mgr class
     */
    public void testUserMgrClassBasics() {
        UserMgrImpl userMgr = getUserMgr();
        assertTrue("User Mgr should implement the right interface", userMgr instanceof ILogEnabled);
        assertTrue("User Mgr should implement the right interface", userMgr instanceof IConfigurable);
        assertTrue("User Mgr should implement the right interface", userMgr instanceof IManagerEnabled);
        assertTrue("User Mgr should implement the right interface", userMgr instanceof IUserMgr);
        assertTrue("User Mgr should implement the right interface", userMgr instanceof IInitializable);
        assertEquals("User mgr should have a datasource", getActivityDataSource(), userMgr.getDataSource());
        assertNotNull("User mgr should have a log", userMgr.getLog());
        assertNotNull("User mgr should have a comp manager", userMgr.getManager());
    }

    /**
     * This test verifies that the instantiation of the user mgr works properly
     */
    public void testUserMgrInstantiation() {

        // Try with bad config
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        ComponentInfo info = new ComponentInfo("badUserMgr", UserMgrImpl.class.getName(), IUserMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, new HashMapConfiguration());
        boolean exThrown = false;
        try {
            compMgr.getComponent(info);
        } catch (RuntimeException e) {
            exThrown = true;
        }
        assertTrue("User manager should refuse configurations without a data source", exThrown);

        UserMgrImpl userMgr = getUserMgr();
        assertNotNull("User manager should be created", userMgr);
    }

    /**
     * This test verifies that all records can be fetched properly
     */
    public void testGetAllUsers() throws HibernateException, DataSourceException {
        final int nbUsers = 10;
        UserMgrImpl userMgr = getUserMgr();
        deleteUserAndGroupRecords();
        insertUserRecords(nbUsers);

        List results = userMgr.getUsers(null);
        assertEquals("All the users records should be returned", nbUsers, results.size());
        Iterator it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            assertTrue("Results should be of type IUser", result instanceof IUser);
            IUser user = (IUser) result;
            assertNotNull("Each record should have a first name", user.getFirstName());
            assertNotNull("Each record should have a last name", user.getLastName());
            assertNotNull("Each record should have a display name", user.getDisplayName());
        }

        // Try with History
        deleteUserAndGroupRecords();
        TimeRelation oldRecords = new TimeRelation(new Date(0), new Date(1));
        insertUserRecords(nbUsers, oldRecords);
        TimeRelation nowRecords = new TimeRelation(new Date(), UnmodifiableDate.END_OF_TIME);
        insertUserRecords(nbUsers * 2, nowRecords); // Insert some more active
        // users for variability
        results = userMgr.getUsers(null);
        assertEquals("Only the active users should be returned", nbUsers * 2, results.size());
        it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            assertTrue("Results should be of type IUser", result instanceof IUser);
            IUser user = (IUser) result;
            assertNotNull("Each record should have a first name", user.getFirstName());
            assertNotNull("Each record should have a last name", user.getLastName());
            assertNotNull("Each record should have a display name", user.getDisplayName());
        }
    }

    /**
     * This test verifies that the search spec part works properly
     */
    public void testGetUsersWithSearchSpec() throws HibernateException, DataSourceException {
        UserMgrImpl userMgr = getUserMgr();
        insertUserRecords(21);
        MockUserQuerySpec querySpec = new MockUserQuerySpec();
        MockUserQueryTerm like2 = new MockUserQueryTerm(UserMgrQueryFieldType.USER_ID, "User_2*");
        querySpec.setSearchSpecTerms(new IUserMgrQueryTerm[] { like2 });
        List results = userMgr.getUsers(querySpec);
        assertEquals("All the users records should be returned", 2, results.size());
        Iterator it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            IUser user = (IUser) result;
            assertTrue("Each record should start with the right expression", user.getDisplayName().startsWith("User_2"));
        }

        // Test with History
        deleteUserAndGroupRecords();
        TimeRelation oldRecords = new TimeRelation(new Date(0), new Date(1));
        insertUserRecords(30, oldRecords);
        TimeRelation nowRecords = new TimeRelation(new Date(), UnmodifiableDate.END_OF_TIME);
        insertUserRecords(23, nowRecords);
        results = userMgr.getUsers(querySpec);
        assertEquals("All active users records should be returned that have user id which starts with User_2", 4, results.size());
        it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            IUser user = (IUser) result;
            assertTrue("Each record should start with the right expression", user.getDisplayName().startsWith("User_2"));
        }
    }

    /**
     * This test verifies that the multiple search specs work properly
     */
    public void testGetUsersWithMultipleSearchSpec() throws DataSourceException, HibernateException {
        UserMgrImpl userMgr = getUserMgr();
        insertUserRecords(21);
        MockUserQuerySpec querySpec = new MockUserQuerySpec();
        MockUserQueryTerm like2 = new MockUserQueryTerm(UserMgrQueryFieldType.USER_ID, "User_2*");
        MockUserQueryTerm fnLike = new MockUserQueryTerm(UserMgrQueryFieldType.FIRST_NAME, "FirstName*");
        querySpec.setSearchSpecTerms(new IUserMgrQueryTerm[] { like2, fnLike });
        List results = userMgr.getUsers(querySpec);
        assertEquals("All the users records should be returned", 2, results.size());
        Iterator it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            IUser user = (IUser) result;
            assertTrue("Each record should start with the right expression", user.getDisplayName().startsWith("User_2"));
            assertTrue("Each record should start with the right expression", user.getFirstName().startsWith("FirstName"));
        }

        // Try with History
        deleteUserAndGroupRecords();
        TimeRelation oldRecords = new TimeRelation(new Date(0), new Date(1));
        insertUserRecords(30, oldRecords);
        TimeRelation nowRecords = new TimeRelation(new Date(), UnmodifiableDate.END_OF_TIME);
        insertUserRecords(23, nowRecords);
        results = userMgr.getUsers(querySpec);
        assertEquals("All the users records should be returned", 4, results.size());
        it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            IUser user = (IUser) result;
            assertTrue("Each record should start with the right expression", user.getDisplayName().startsWith("User_2"));
            assertTrue("Each record should start with the right expression", user.getFirstName().startsWith("FirstName"));
        }
    }

    /**
     * This test verifies that the sort spec part works properly
     * 
     * @throws HibernateException
     */
    public void testGetUsersWithSortSpec() throws HibernateException {
        UserMgrImpl userMgr = getUserMgr();
        final int nbRecords = 10;
        insertUserRecords(nbRecords);
        try {
            MockUserQuerySpec querySpec = new MockUserQuerySpec();
            MockUserSortTerm sortOnFirstNameDesc = new MockUserSortTerm(UserMgrSortFieldType.FIRST_NAME, SortDirectionType.DESCENDING);
            querySpec.setSortSpecTerms(new IUserMgrSortTerm[] { sortOnFirstNameDesc });
            List results = userMgr.getUsers(querySpec);
            assertEquals("All the users records should be returned", nbRecords, results.size());
            String lastFirstName = null;
            final Iterator it = results.iterator();
            while (it.hasNext()) {
                Object result = it.next();
                assertTrue("Results should be of type IUser", result instanceof IUser);
                IUser user = (IUser) result;
                if (lastFirstName == null) {
                    lastFirstName = user.getFirstName();
                }
                assertTrue("Records should be ordered by descending first name", user.getFirstName().compareTo(lastFirstName) <= 0);
                lastFirstName = user.getFirstName();
            }
        } catch (DataSourceException e) {
            fail("No data source exception should be thrown : " + e.getLocalizedMessage());
        }
    }

    /**
     * This test verifies that multiple sort specs can work properly
     * 
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void testGetUsersWithMultipleSortSpecs() throws DataSourceException, HibernateException {
        UserMgrImpl userMgr = getUserMgr();
        final int nbRecords = 10;
        insertUserRecords(nbRecords);

        MockUserQuerySpec querySpec = new MockUserQuerySpec();
        MockUserSortTerm sortOnFirstNameDesc = new MockUserSortTerm(UserMgrSortFieldType.FIRST_NAME, SortDirectionType.DESCENDING);
        MockUserSortTerm sortOnLastNameAsc = new MockUserSortTerm(UserMgrSortFieldType.LAST_NAME, SortDirectionType.ASCENDING);
        querySpec.setSortSpecTerms(new IUserMgrSortTerm[] { sortOnFirstNameDesc, sortOnLastNameAsc });
        List results = userMgr.getUsers(querySpec);
        assertEquals("All the users records should be returned", nbRecords, results.size());
        String lastFirstName = null;
        final Iterator it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            assertTrue("Results should be of type IUser", result instanceof IUser);
            IUser user = (IUser) result;
            if (lastFirstName == null) {
                lastFirstName = user.getFirstName();
            }
            assertTrue("Records should be ordered by descending first name", user.getFirstName().compareTo(lastFirstName) <= 0);
            lastFirstName = user.getFirstName();
        }
    }

    /**
     * This test verifies that the query spec works fine
     * 
     * @throws HibernateException
     * @throws DataSourceException
     */
    public void testGetUsersWithCaseInsensiveSearch() throws HibernateException, DataSourceException {
        insertMixedCaseUserRecords();
        UserMgrImpl userMgr = getUserMgr();

        // Try the no match query
        String userMatch = "User*";
        MockUserQuerySpec searchSpec = new MockUserQuerySpec();
        MockUserQueryTerm searchTerm = new MockUserQueryTerm(UserMgrQueryFieldType.USER_ID, userMatch);
        searchSpec.setSearchSpecTerms(new IUserMgrQueryTerm[] { searchTerm });
        List results = userMgr.getUsers(searchSpec);
        assertEquals("Only the matching user records should be returned", 0, results.size());

        // Try starting with
        userMatch = "new*";
        searchTerm = new MockUserQueryTerm(UserMgrQueryFieldType.USER_ID, userMatch);
        searchSpec.setSearchSpecTerms(new IUserMgrQueryTerm[] { searchTerm });
        results = userMgr.getUsers(searchSpec);
        assertEquals("Only the matching policy records should be returned", 2, results.size());
        Iterator it = results.iterator();
        Object result = it.next();
        assertTrue("Results should be of type IUser", result instanceof IUser);
        IUser user = (IUser) result;
        assertTrue("The matching record should have a matching name", user.getDisplayName().toLowerCase().startsWith("new"));

        // Try combined search
        userMatch = "*ser";
        searchTerm = new MockUserQueryTerm(UserMgrQueryFieldType.USER_ID, userMatch);
        userMatch = "R*";
        MockUserQueryTerm searchLikeR = new MockUserQueryTerm(UserMgrQueryFieldType.USER_ID, userMatch);
        searchSpec.setSearchSpecTerms(new IUserMgrQueryTerm[] { searchTerm, searchLikeR });
        results = userMgr.getUsers(searchSpec);
        assertEquals("Only the matching user records should be returned", 1, results.size());
    }

    public void testGetUserClasses() throws HibernateException, DataSourceException {
        getAllUserClassesTest();
        getUserClassesWithSearchSpecTest();
        getUserClassesWithMultipleSearchSpecTest();
        getUserClassesWithSortSpecTest();
        getUserClassesWithMultipleSortSpecsTest();
        getUserClassesWithCaseInsensiveSearchTest();

    }

    private void getAllUserClassesTest() throws HibernateException, DataSourceException {
        final int nbUserGroups = 10;
        UserMgrImpl userMgr = getUserMgr();
        deleteUserAndGroupRecords();
        insertUserGroupRecords(nbUserGroups);

        List results = userMgr.getUserClasses(null);
        assertEquals("All the users class records should be returned", nbUserGroups, results.size());
        Iterator it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            assertTrue("Results should be of type IUserGroup", result instanceof IUserGroup);
            IUserGroup userGroup = (IUserGroup) result;
            assertNotNull("Each record should have a display name", userGroup.getDisplayName());
            assertNotNull("Each record should have a name", userGroup.getName());
            assertNotNull("Each record should have an origianl id", userGroup.getOriginalId());
        }

        // Try with History
        deleteUserAndGroupRecords();
        TimeRelation oldRecords = new TimeRelation(new Date(0), new Date(1));
        insertUserGroupRecords(nbUserGroups, oldRecords);
        TimeRelation nowRecords = new TimeRelation(new Date(), UnmodifiableDate.END_OF_TIME);
        insertUserGroupRecords(nbUserGroups * 2, nowRecords); // Insert some more
        // active
        // users for variability
        results = userMgr.getUserClasses(null);
        assertEquals("Only the active user classes should be returned", nbUserGroups * 2, results.size());
        it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            assertTrue("Results should be of type IUserGroup", result instanceof IUserGroup);
            IUserGroup userGroup = (IUserGroup) result;
            assertNotNull("Each record should have a display name", userGroup.getDisplayName());
            assertNotNull("Each record should have a name", userGroup.getName());
            assertNotNull("Each record should have an origianl id", userGroup.getOriginalId());
        }
    }

    /**
     * This test verifies that the search spec part works properly
     */
    private void getUserClassesWithSearchSpecTest() throws HibernateException, DataSourceException {
        UserMgrImpl userMgr = getUserMgr();
        deleteUserAndGroupRecords();
        insertUserGroupRecords(21);
        MockUserClassQuerySpec querySpec = new MockUserClassQuerySpec();
        MockUserClassQueryTerm like2 = new MockUserClassQueryTerm(UserClassMgrQueryFieldType.DISPLAY_NAME, "UserGroup_2*");
        querySpec.setSearchSpecTerms(new IUserClassMgrQueryTerm[] { like2 });
        List results = userMgr.getUserClasses(querySpec);
        assertEquals("All the user class records should be returned", 2, results.size());
        Iterator it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            IUserGroup userGroup = (IUserGroup) result;
            assertTrue("Each record should start with the right expression", userGroup.getDisplayName().startsWith("UserGroup_2"));
        }

        // Test with History
        deleteUserAndGroupRecords();
        TimeRelation oldRecords = new TimeRelation(new Date(0), new Date(1));
        insertUserGroupRecords(30, oldRecords);
        TimeRelation nowRecords = new TimeRelation(new Date(), UnmodifiableDate.END_OF_TIME);
        insertUserGroupRecords(23, nowRecords);
        results = userMgr.getUserClasses(querySpec);
        assertEquals("All active user class records should be returned that have user id which starts with UserGroup_2", 4, results.size());
        it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            IUserGroup user = (IUserGroup) result;
            assertTrue("Each record should start with the right expression", user.getDisplayName().startsWith("UserGroup_2"));
        }
    }

    /**
     * This test verifies that the multiple search specs work properly
     */
    private void getUserClassesWithMultipleSearchSpecTest() throws DataSourceException, HibernateException {
        UserMgrImpl userMgr = getUserMgr();
        deleteUserAndGroupRecords();
        insertUserGroupRecords(21);
        MockUserClassQuerySpec querySpec = new MockUserClassQuerySpec();
        MockUserClassQueryTerm like2 = new MockUserClassQueryTerm(UserClassMgrQueryFieldType.DISPLAY_NAME, "UserGroup_2*");
        MockUserClassQueryTerm nameLike = new MockUserClassQueryTerm(UserClassMgrQueryFieldType.NAME, "Name*");
        querySpec.setSearchSpecTerms(new IUserClassMgrQueryTerm[] { like2, nameLike });
        List results = userMgr.getUserClasses(querySpec);
        assertEquals("All the users records should be returned", 2, results.size());
        Iterator it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            IUserGroup user = (IUserGroup) result;
            assertTrue("Each record should start with the right expression", user.getDisplayName().startsWith("UserGroup_2"));
            assertTrue("Each record should start with the right expression", user.getName().startsWith("Name"));
        }

        // Try with History
        deleteUserAndGroupRecords();
        TimeRelation oldRecords = new TimeRelation(new Date(0), new Date(1));
        insertUserGroupRecords(30, oldRecords);
        TimeRelation nowRecords = new TimeRelation(new Date(), UnmodifiableDate.END_OF_TIME);
        insertUserGroupRecords(23, nowRecords);
        results = userMgr.getUserClasses(querySpec);
        assertEquals("All the user class records should be returned", 4, results.size());
        it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            IUserGroup user = (IUserGroup) result;
            assertTrue("Each record should start with the right expression", user.getDisplayName().startsWith("UserGroup_2"));
            assertTrue("Each record should start with the right expression", user.getName().startsWith("Name"));
        }
    }

    /**
     * This test verifies that the sort spec part works properly
     * 
     * @throws HibernateException
     * @throws DataSourceException
     */
    private void getUserClassesWithSortSpecTest() throws HibernateException, DataSourceException {
        UserMgrImpl userMgr = getUserMgr();
        final int nbRecords = 10;
        deleteUserAndGroupRecords();
        insertUserGroupRecords(nbRecords);

        MockUserClassQuerySpec querySpec = new MockUserClassQuerySpec();
        MockUserClassSortTerm sortOnFirstNameDesc = new MockUserClassSortTerm(UserClassMgrSortFieldType.DISPLAY_NAME, SortDirectionType.DESCENDING);
        querySpec.setSortSpecTerms(new IUserClassMgrSortTerm[] { sortOnFirstNameDesc });
        List results = userMgr.getUserClasses(querySpec);
        assertEquals("All the user classes records should be returned", nbRecords, results.size());
        String lastName = null;
        final Iterator it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            assertTrue("Results should be of type IUserGroup", result instanceof IUserGroup);
            IUserGroup userGroup = (IUserGroup) result;
            if (lastName == null) {
                lastName = userGroup.getName();
            }
            assertTrue("Records should be ordered by descending name", userGroup.getName().compareTo(lastName) <= 0);
            lastName = userGroup.getName();
        }
    }

    /**
     * This test verifies that multiple sort specs can work properly
     * 
     * @throws DataSourceException
     * @throws HibernateException
     */
    public void getUserClassesWithMultipleSortSpecsTest() throws DataSourceException, HibernateException {
        UserMgrImpl userMgr = getUserMgr();
        final int nbRecords = 10;
        deleteUserAndGroupRecords();
        insertUserGroupRecords(nbRecords);

        MockUserClassQuerySpec querySpec = new MockUserClassQuerySpec();
        MockUserClassSortTerm sortOnFirstNameDesc = new MockUserClassSortTerm(UserClassMgrSortFieldType.NAME, SortDirectionType.DESCENDING);
        MockUserClassSortTerm sortOnLastNameAsc = new MockUserClassSortTerm(UserClassMgrSortFieldType.DISPLAY_NAME, SortDirectionType.ASCENDING);
        querySpec.setSortSpecTerms(new IUserClassMgrSortTerm[] { sortOnFirstNameDesc, sortOnLastNameAsc });
        List results = userMgr.getUserClasses(querySpec);
        assertEquals("All the user class records should be returned", nbRecords, results.size());
        String lastName = null;
        final Iterator it = results.iterator();
        while (it.hasNext()) {
            Object result = it.next();
            assertTrue("Results should be of type IUserGroup", result instanceof IUserGroup);
            IUserGroup userGroup = (IUserGroup) result;
            if (lastName == null) {
                lastName = userGroup.getName();
            }
            assertTrue("Records should be ordered by descending first name", userGroup.getName().compareTo(lastName) <= 0);
            lastName = userGroup.getName();
        }
    }

    /**
     * This test verifies that the query spec works fine
     * 
     * @throws HibernateException
     * @throws DataSourceException
     */
    public void getUserClassesWithCaseInsensiveSearchTest() throws HibernateException, DataSourceException {
        deleteUserAndGroupRecords();
        insertMixedCaseUserGroupRecords();
        UserMgrImpl userMgr = getUserMgr();

        // Try the no match query
        String userGroupMatch = "UserGroup*";
        MockUserClassQuerySpec searchSpec = new MockUserClassQuerySpec();
        MockUserClassQueryTerm searchTerm = new MockUserClassQueryTerm(UserClassMgrQueryFieldType.NAME, userGroupMatch);
        searchSpec.setSearchSpecTerms(new IUserClassMgrQueryTerm[] { searchTerm });
        List results = userMgr.getUserClasses(searchSpec);
        assertEquals("Only the matching user group records should be returned", 0, results.size());

        // Try starting with
        userGroupMatch = "new*";
        searchTerm = new MockUserClassQueryTerm(UserClassMgrQueryFieldType.NAME, userGroupMatch);
        searchSpec.setSearchSpecTerms(new IUserClassMgrQueryTerm[] { searchTerm });
        results = userMgr.getUserClasses(searchSpec);
        assertEquals("Only the matching policy records should be returned", 2, results.size());
        Iterator it = results.iterator();
        Object result = it.next();
        assertTrue("Results should be of type IUserGrouop", result instanceof IUserGroup);
        IUserGroup user = (IUserGroup) result;
        assertTrue("The matching record should have a matching name", user.getName().toLowerCase().startsWith("new"));

        // Try combined search
        userGroupMatch = "*serGroup";
        searchTerm = new MockUserClassQueryTerm(UserClassMgrQueryFieldType.NAME, userGroupMatch);
        userGroupMatch = "R*";
        MockUserClassQueryTerm searchLikeR = new MockUserClassQueryTerm(UserClassMgrQueryFieldType.DISPLAY_NAME, userGroupMatch);
        searchSpec.setSearchSpecTerms(new IUserClassMgrQueryTerm[] { searchTerm, searchLikeR });
        results = userMgr.getUserClasses(searchSpec);
        assertEquals("Only the matching user group records should be returned", 1, results.size());
    }

    /**
     * Dummy user query specification class
     * 
     * @author ihanen
     */
    protected class MockUserQuerySpec implements IUserMgrQuerySpec {

        private int limit;
        private IUserMgrQueryTerm[] searchSpecTerms = new IUserMgrQueryTerm[0];
        private IUserMgrSortTerm[] sortSpecTerms = new IUserMgrSortTerm[0];

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec#getSearchSpecTerms()
         */
        public IUserMgrQueryTerm[] getSearchSpecTerms() {
            return this.searchSpecTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec#getSortSpecTerms()
         */
        public IUserMgrSortTerm[] getSortSpecTerms() {
            return this.sortSpecTerms;
        }

        /**
         * Set the search spec terms
         * 
         * @param newTerms
         *            new terms to set
         */
        public void setSearchSpecTerms(IUserMgrQueryTerm[] newTerms) {
            this.searchSpecTerms = newTerms;
        }

        /**
         * Sets the sort spec terms
         * 
         * @param newTerms
         *            new terms to set
         */
        public void setSortSpecTerms(IUserMgrSortTerm[] newTerms) {
            this.sortSpecTerms = newTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQuerySpec#getLimit()
         */
        public int getLimit() {
            return this.limit;
        }

        /**
         * Sets the limit
         * 
         * @param newLimit
         *            new limit to set
         */
        public void setLimit(int newLimit) {
            this.limit = newLimit;
        }
    }

    /**
     * Dummy user query term class
     * 
     * @author ihanen
     */
    protected class MockUserQueryTerm implements IUserMgrQueryTerm {

        private UserMgrQueryFieldType fieldName;
        private String expression;

        /**
         * Constructor
         * 
         * @param field
         *            field to query on
         * @param expr
         *            query expression
         */
        public MockUserQueryTerm(UserMgrQueryFieldType field, String expr) {
            this.fieldName = field;
            this.expression = expr;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQueryTerm#getFieldName()
         */
        public UserMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }
    }

    /**
     * Dummy user sort term class
     * 
     * @author ihanen
     */
    protected class MockUserSortTerm implements IUserMgrSortTerm {

        private UserMgrSortFieldType fieldName;
        private SortDirectionType direction;

        /**
         * Constructor
         * 
         * @param field
         *            field to sort on
         * @param dir
         *            sorting direction
         */
        public MockUserSortTerm(UserMgrSortFieldType field, SortDirectionType dir) {
            this.fieldName = field;
            this.direction = dir;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm#getFieldName()
         */
        public UserMgrSortFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm#getDirection()
         */
        public SortDirectionType getDirection() {
            return this.direction;
        }
    }

    /**
     * Dummy user query specification class
     * 
     * @author ihanen
     */
    protected class MockUserClassQuerySpec implements IUserClassMgrQuerySpec {

        private int limit;
        private IUserClassMgrQueryTerm[] searchSpecTerms = new IUserClassMgrQueryTerm[0];
        private IUserClassMgrSortTerm[] sortSpecTerms = new IUserClassMgrSortTerm[0];

        public IUserClassMgrQueryTerm[] getSearchSpecTerms() {
            return this.searchSpecTerms;
        }

        public IUserClassMgrSortTerm[] getSortSpecTerms() {
            return this.sortSpecTerms;
        }

        /**
         * Set the search spec terms
         * 
         * @param newTerms
         *            new terms to set
         */
        public void setSearchSpecTerms(IUserClassMgrQueryTerm[] newTerms) {
            this.searchSpecTerms = newTerms;
        }

        /**
         * Sets the sort spec terms
         * 
         * @param newTerms
         *            new terms to set
         */
        public void setSortSpecTerms(IUserClassMgrSortTerm[] newTerms) {
            this.sortSpecTerms = newTerms;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.UserClassMgr.IUserClassMgrQuerySpec#getLimit()
         */
        public int getLimit() {
            return this.limit;
        }

        /**
         * Sets the limit
         * 
         * @param newLimit
         *            new limit to set
         */
        public void setLimit(int newLimit) {
            this.limit = newLimit;
        }
    }

    /**
     * Dummy user query term class
     * 
     * @author ihanen
     */
    protected class MockUserClassQueryTerm implements IUserClassMgrQueryTerm {

        private UserClassMgrQueryFieldType fieldName;
        private String expression;

        /**
         * Constructor
         * 
         * @param field
         *            field to query on
         * @param expr
         *            query expression
         */
        public MockUserClassQueryTerm(UserClassMgrQueryFieldType field, String expr) {
            this.fieldName = field;
            this.expression = expr;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQueryTerm#getFieldName()
         */
        public UserClassMgrQueryFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrQueryTerm#getExpression()
         */
        public String getExpression() {
            return this.expression;
        }
    }

    /**
     * Dummy user sort term class
     * 
     * @author ihanen
     */
    protected class MockUserClassSortTerm implements IUserClassMgrSortTerm {

        private UserClassMgrSortFieldType fieldName;
        private SortDirectionType direction;

        /**
         * Constructor
         * 
         * @param field
         *            field to sort on
         * @param dir
         *            sorting direction
         */
        public MockUserClassSortTerm(UserClassMgrSortFieldType field, SortDirectionType dir) {
            this.fieldName = field;
            this.direction = dir;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm#getFieldName()
         */
        public UserClassMgrSortFieldType getFieldName() {
            return this.fieldName;
        }

        /**
         * @see com.bluejungle.destiny.container.dac.inquiry.components.usermgr.IUserMgrSortTerm#getDirection()
         */
        public SortDirectionType getDirection() {
            return this.direction;
        }
    }
}
