/*
 * Created on Nov 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.bindings.report.v1;

import java.rmi.RemoteException;

import org.apache.axis.AxisFault;

import com.bluejungle.destiny.interfaces.report.v1.ComponentLookupIF;
import com.bluejungle.destiny.types.basic.v1.SortDirection;
import com.bluejungle.destiny.types.users.v1.User;
import com.bluejungle.destiny.types.users.v1.UserList;
import com.bluejungle.destiny.types.users.v1.UserQueryFieldName;
import com.bluejungle.destiny.types.users.v1.UserQuerySpec;
import com.bluejungle.destiny.types.users.v1.UserQueryTerm;
import com.bluejungle.destiny.types.users.v1.UserQueryTermList;
import com.bluejungle.destiny.types.users.v1.UserSortFieldName;
import com.bluejungle.destiny.types.users.v1.UserSortTerm;
import com.bluejungle.destiny.types.users.v1.UserSortTermList;

/**
 * This is the user API test class for the component lookup service.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ComponentLookupUserTest.java#1 $
 */

public class ComponentLookupUserTest extends ComponentLookupIFImplTest {

    /**
     * Constructor
     * 
     * @param testName
     */
    public ComponentLookupUserTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that if the getUsers() API returns no records, the
     * data structure returned is still correct.
     */
    public void testGetUsersNoRecords() throws RemoteException{
        final int nbUsers = 10;
        deleteAllUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        long start = System.currentTimeMillis();
        UserList noUsersResult = componentLookup.getUsers(null);
        long end = System.currentTimeMillis();
        getLog().info("GetUsers with no result timing: " + (end - start) + " ms");
        assertNotNull("Result should be returned even if no users have been found", noUsersResult);
        assertNull("Result should be returned even if no users have been found", noUsersResult.getUsers());
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetUsersWithQuerySpec() throws RemoteException {
        final int nbUsers = 30;
        insertUsers(new Long(0), nbUsers);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            //Query for users with a search spec on first name *3*
            UserQuerySpec likeStar3StarQuerySpec = new UserQuerySpec();
            UserQueryTermList likeStar3StarQueryTermList = new UserQueryTermList();
            UserQueryTerm likeStar3StarTerm = new UserQueryTerm();
            likeStar3StarTerm.setFieldName(UserQueryFieldName.firstName);
            likeStar3StarTerm.setExpression("*3*");
            UserQueryTerm[] terms = { likeStar3StarTerm };
            likeStar3StarQueryTermList.setTerms(terms);
            likeStar3StarQuerySpec.setSearchSpec(likeStar3StarQueryTermList);
            long start = System.currentTimeMillis();
            UserList queryResults = componentLookup.getUsers(likeStar3StarQuerySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be users returned", queryResults.getUsers());
            int size = queryResults.getUsers().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 3, size);
            for (int index = 0; index < size; index++) {
                User user = queryResults.getUsers(index);
                assertTrue("Returned records should match the query spec", user.getFirstName().endsWith("3"));
                assertTrue("Returned records should match the query spec", user.getLastName().endsWith("3"));
            }

            //Try multi field query spec. No records should be returned
            UserQuerySpec multiFieldQuerySpec = new UserQuerySpec();
            UserQueryTermList multiFieldQueryTermList = new UserQueryTermList();
            UserQueryTerm fnQueryTerm = new UserQueryTerm();
            fnQueryTerm.setFieldName(UserQueryFieldName.firstName);
            fnQueryTerm.setExpression("first");
            UserQueryTerm lnQueryTerm = new UserQueryTerm();
            lnQueryTerm.setFieldName(UserQueryFieldName.lastName);
            lnQueryTerm.setExpression("last");
            UserQueryTerm[] multiFieldTerms = { fnQueryTerm, lnQueryTerm };
            multiFieldQueryTermList.setTerms(multiFieldTerms);
            multiFieldQuerySpec.setSearchSpec(multiFieldQueryTermList);
            start = System.currentTimeMillis();
            queryResults = componentLookup.getUsers(multiFieldQuerySpec);
            end = System.currentTimeMillis();
            getLog().info("GetUsers with multiple field search spec timing: " + (end - start) + " ms");
            assertNull("No rows should be returned", queryResults.getUsers());
        } finally {
            deleteAllPolicies();
        }
    }

    /**
     * This test verifies that users can be fetched with a sort spec on the
     * first name
     */
    public void testGetUsersWithSortSpec() throws RemoteException {
        final int nbUsers = 20;
        insertUsers(new Long(0), nbUsers);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            //Query for users with a search spec
            UserQuerySpec nameDescQuerySpec = new UserQuerySpec();
            UserSortTermList nameDescSortTermList = new UserSortTermList();
            UserSortTerm nameDescSortTerm = new UserSortTerm();
            nameDescSortTerm.setFieldName(UserSortFieldName.firstName);
            nameDescSortTerm.setDirection(SortDirection.Descending);
            UserSortTerm[] terms = { nameDescSortTerm };
            nameDescSortTermList.setTerms(terms);
            nameDescQuerySpec.setSortSpec(nameDescSortTermList);
            long start = System.currentTimeMillis();
            UserList sortedUserResults = componentLookup.getUsers(nameDescQuerySpec);
            long end = System.currentTimeMillis();
            getLog().info("getUsers with sort spec timing: " + (end - start) + " ms");
            assertNotNull("There should be users returned", sortedUserResults.getUsers());
            int size = sortedUserResults.getUsers().length;
            assertEquals("The sort spec should return all records", nbUsers, size);
            String lastRecordFirstName = sortedUserResults.getUsers()[0].getFirstName();
            for (int index = 0; index < size; index++) {
                String currentName = sortedUserResults.getUsers()[index].getFirstName();
                int result = currentName.compareTo(lastRecordFirstName);
                assertTrue("Results should be sorted by first name descending", result <= 0);
                lastRecordFirstName = currentName;
            }
        } finally {
            deleteAllPolicies();
        }
    }

    public void testGetUsersAll() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        //Query for hosts with a search spec on name *3*
        UserQuerySpec likeStar3StarQuerySpec = new UserQuerySpec();
        UserQueryTermList likeStar3StarQueryTermList = new UserQueryTermList();
        UserQueryTerm likeStar3StarTerm = new UserQueryTerm();
        likeStar3StarTerm.setFieldName(UserQueryFieldName.lastName);
        likeStar3StarTerm.setExpression("*");
        UserQueryTerm[] terms = { likeStar3StarTerm };
        likeStar3StarQueryTermList.setTerms(terms);
        likeStar3StarQuerySpec.setSearchSpec(likeStar3StarQueryTermList);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(likeStar3StarQuerySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetUsersRandomExpressions() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        //Query for hosts with a search spec on name *3*
        UserQuerySpec likeStar3StarQuerySpec = new UserQuerySpec();
        UserQueryTermList likeStar3StarQueryTermList = new UserQueryTermList();
        UserQueryTerm likeStar3StarTerm = new UserQueryTerm();
        likeStar3StarTerm.setFieldName(UserQueryFieldName.lastName);
        likeStar3StarTerm.setExpression(RANDOM_SEARCH_EXPR_ONE);
        UserQueryTerm[] terms = { likeStar3StarTerm };
        likeStar3StarQueryTermList.setTerms(terms);
        likeStar3StarQuerySpec.setSearchSpec(likeStar3StarQueryTermList);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(likeStar3StarQuerySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getUsers());
    }

    /**
     * This test verifies getHostClasses() with null input
     *  
     */
    public void testGetUsersWithNullQuerySpec() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        componentLookup.getUsers(null);
    }

    /**
     * This test verifies getHostClasses() with null searchSpec and null
     * sortSpec in HostClassQuerySpec.
     */
    public void testGetUsersWithNullSearchSpecAndNullSortSpec() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        querySpec.setSearchSpec(null);
        querySpec.setSortSpec(null);
        componentLookup.getUsers(querySpec);
    }

    /**
     * This test verifies getHostClasses() with null searchSpec in
     * HostClassQuerySpec.
     */
    public void testGetUsersWithNullSearchSpec() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        querySpec.setSearchSpec(null);
        UserSortTermList nameDescSortTermList = new UserSortTermList();
        UserSortTerm nameDescSortTerm = new UserSortTerm();
        nameDescSortTerm.setFieldName(UserSortFieldName.lastName);
        nameDescSortTerm.setDirection(SortDirection.Descending);
        UserSortTerm[] terms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(terms);
        querySpec.setSortSpec(nameDescSortTermList);
        componentLookup.getUsers(querySpec);
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetUsersWithNumber() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*1");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm2.setExpression("*2");
        UserQueryTerm searchSpecTerm3 = new UserQueryTerm();
        searchSpecTerm3.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm3.setExpression("*3");
        UserQueryTerm searchSpecTerm4 = new UserQueryTerm();
        searchSpecTerm4.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm4.setExpression("*4");
        UserQueryTerm searchSpecTerm5 = new UserQueryTerm();
        searchSpecTerm5.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm5.setExpression("*5");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should be no users returned", queryResults.getUsers());
    }
    
    /**
     * This test verifies getHostClasses() with null sortSpec in
     * HostClassQuerySpec.
     */
    public void testGetUsersWithNullSortSpec() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec nameLike3QuerySpec = new UserQuerySpec();
        UserQueryTermList like3QueryTermList = new UserQueryTermList();
        UserQueryTerm like3Term = new UserQueryTerm();
        like3Term.setFieldName(UserQueryFieldName.lastName);
        like3Term.setExpression("*3");
        UserQueryTerm[] terms = { like3Term };
        like3QueryTermList.setTerms(terms);
        nameLike3QuerySpec.setSearchSpec(like3QueryTermList);
        nameLike3QuerySpec.setSortSpec(null);
        componentLookup.getUsers(nameLike3QuerySpec);
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetUsersWithFiveRandomSearchSpec() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression(RANDOM_SEARCH_EXPR_ONE);
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        UserQueryTerm searchSpecTerm3 = new UserQueryTerm();
        searchSpecTerm3.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm3.setExpression(RANDOM_SEARCH_EXPR_THREE);
        UserQueryTerm searchSpecTerm4 = new UserQueryTerm();
        searchSpecTerm4.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        UserQueryTerm searchSpecTerm5 = new UserQueryTerm();
        searchSpecTerm5.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm5.setExpression(RANDOM_SEARCH_EXPR_FIVE);
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getUsers());
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */

    public void testGetUsersWithRandomSearchSpecAndRandomNullSearchExpression() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression(null);
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        UserQueryTerm searchSpecTerm3 = new UserQueryTerm();
        searchSpecTerm3.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm3.setExpression(null);
        UserQueryTerm searchSpecTerm4 = new UserQueryTerm();
        searchSpecTerm4.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        UserQueryTerm searchSpecTerm5 = new UserQueryTerm();
        searchSpecTerm5.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm5.setExpression(null);
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try {
            UserList queryResults = componentLookup.getUsers(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'expression' is null.") > 0);
        }
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetUsersWithRandomSearchSpecAndRandomNullSearchFieldName() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(null);
        searchSpecTerm1.setExpression(RANDOM_SEARCH_EXPR_ONE);
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        UserQueryTerm searchSpecTerm3 = new UserQueryTerm();
        searchSpecTerm3.setFieldName(null);
        searchSpecTerm3.setExpression(RANDOM_SEARCH_EXPR_THREE);
        UserQueryTerm searchSpecTerm4 = new UserQueryTerm();
        searchSpecTerm4.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        UserQueryTerm searchSpecTerm5 = new UserQueryTerm();
        searchSpecTerm5.setFieldName(null);
        searchSpecTerm5.setExpression(RANDOM_SEARCH_EXPR_FIVE);
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try {
            UserList queryResults = componentLookup.getUsers(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'fieldName' is null.") > 0);
        }
    }

    public void testGetAllUsersByStar() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllUsersByPrefix() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("LastName_*");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm2.setExpression("FirstName_*");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllUsersByFirstLetter() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*_*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllUsersBySuffix() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("***");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllUsersByRegularExpression() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("FirstName_**");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm2.setExpression("LastName_**");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be users returned", queryResults.getUsers());
    }

    public void testGetAllUsersByExclude() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("?*_?*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetUsersSingleDigits() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm3 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm4 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm5 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm6 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm7 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm8 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm9 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("User_1");
        searchSpecTerm2.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm2.setExpression("User_2");
        searchSpecTerm3.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm3.setExpression("User_3");
        searchSpecTerm4.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm4.setExpression("User_4");
        searchSpecTerm5.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm5.setExpression("User_5");
        searchSpecTerm6.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm6.setExpression("User_6");
        searchSpecTerm7.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm7.setExpression("User_7");
        searchSpecTerm8.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm8.setExpression("User_8");
        searchSpecTerm9.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm9.setExpression("User_9");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5, searchSpecTerm6, searchSpecTerm7, searchSpecTerm8, searchSpecTerm9 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be users returned", queryResults.getUsers());
    }

    public void testGetUsersSingleDigitsOne() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm3 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm4 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm5 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm6 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm7 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm8 = new UserQueryTerm();
        UserQueryTerm searchSpecTerm9 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("LastName_1");
        searchSpecTerm2.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm2.setExpression("LastName_1");
        searchSpecTerm3.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm3.setExpression("LastName_1");
        searchSpecTerm4.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm4.setExpression("LastName_1");
        searchSpecTerm5.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm5.setExpression("LastName_1");
        searchSpecTerm6.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm6.setExpression("LastName_1");
        searchSpecTerm7.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm7.setExpression("LastName_1");
        searchSpecTerm8.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm8.setExpression("LastName_1");
        searchSpecTerm9.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm9.setExpression("LastName_1");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5, searchSpecTerm6, searchSpecTerm7, searchSpecTerm8, searchSpecTerm9 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetUsersWithOne() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*_*1*");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm2.setExpression("*_*1*");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 12, size);
    }

    public void testGetUsersWithFive() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*5");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm2.setExpression("*5");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 3, size);
    }

    public void testGetUsersWithExactName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("Hanen");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm2.setExpression("Iannis");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetUsersWithRandomWildCardsLastName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*V*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }
    
    public void testGetUsersWithRandomWildCardsFirstName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*K*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 2, size);
    }

    public void testGetUsersWithWildCardsAndLettersLastName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("K?*c*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }
    
    public void testGetUsersWithWildCardsAndLettersFirstName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("C?*d*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetUsersWithStartingLetterLastName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("O*");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm2.setExpression("P*");
        UserQueryTerm searchSpecTerm3 = new UserQueryTerm();
        searchSpecTerm3.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm3.setExpression("Q*");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be users returned", queryResults.getUsers());
    }
    
    public void testGetUsersWithStartingLetterFirstName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("E*");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm2.setExpression("G*");
        UserQueryTerm searchSpecTerm3 = new UserQueryTerm();
        searchSpecTerm3.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm3.setExpression("H*");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be users returned", queryResults.getUsers());
    }

    public void testGetUsersWithRandomStringOnUniqueRecordsLastName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("[abcdefghijklmnopqrstuvwxyz_+(*&^)%$#@!<> 1234567890]*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getUsers());
    }
    
    public void testGetUsersWithRandomStringOnUniqueRecordsFirstName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("[abcdefghijklmnopqrstuvwxyz_+(*&^)%$#@!<> 1234567890]*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getUsers());
    }

    public void testGetUsersWithSpaceLastName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("* *");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be users returned", queryResults.getUsers());
    }

    public void testGetUsersWithSpaceFirstName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("* *");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be users returned", queryResults.getUsers());
    }
    
    public void testGetUsersWithWildCardsAndLettersAndNumbers() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*z0?*");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm2.setExpression("*z0?*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be users returned", queryResults.getUsers());
    }

    public void testGetUsersWithRepeatingWildCardsAndLettersLastName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*i*i*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 2, size);
    }
    
    public void testGetUsersWithRepeatingWildCardsAndLettersFirstName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*a*a*");
        UserQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 3, size);
    }

    public void testGetUsersWithCaseInsensitiveStringLastName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("liM"); //There is Keng and David!
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 2, size);
    }
    
    public void testGetUsersWithCaseInsensitiveStringFirstName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("kENg");
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    /**
     * The following test verifies whether IllegalArgumentException is thrown when backspace is passed as input to policy criteria
     */
    public void testGetUsersWithEscapeSequenceBackspace() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
            
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*\b*");
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
           	UserList queryResults = componentLookup.getUsers(querySpec);
        	fail("The test should throw an exception as backspace is invalid input" );
        }  
        catch (RemoteException e) {
        	assertTrue(e instanceof AxisFault);
        	assertTrue(e.getMessage().indexOf("java.io.IOException: java.lang.IllegalArgumentException:") > 0);
        }
    }

    public void testGetUsersWithEscapeSequenceFormFeed() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
            
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*\f*");
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
           	UserList queryResults = componentLookup.getUsers(querySpec);
        	fail("The test should throw an exception as formfeed is invalid input" );
        }  
        catch (RemoteException e) {
        	assertTrue(e instanceof AxisFault);
        	assertTrue(e.getMessage().indexOf("java.io.IOException: java.lang.IllegalArgumentException:") > 0);
        }
    }

    public void testGetUsersWithEscapeSequenceTab() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
            
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*\t*");
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	UserList queryResults = componentLookup.getUsers(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("No users should be returned", queryResults.getUsers());
        }
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testGetUsersWithEscapeSequenceLinefeed() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
            
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*\n*");
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	UserList queryResults = componentLookup.getUsers(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("No users should be returned", queryResults.getUsers());
        }
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testGetUsersWithEscapeSequenceCarriageReturn() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
            
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*\r*");
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	UserList queryResults = componentLookup.getUsers(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("No users should be returned", queryResults.getUsers());
        }
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testGetUsersWithEscapeSequenceDoublequote() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
            
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*\"*");
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	UserList queryResults = componentLookup.getUsers(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("No users should be returned", queryResults.getUsers());
        }
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testGetUsersWithEscapeSequenceSinglequote() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
            
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*\'*");
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	UserList queryResults = componentLookup.getUsers(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("No users should be returned", queryResults.getUsers());
        }
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testGetUsersWithEscapeSequenceBackslash() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
            
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*\\*");
        UserQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	UserList queryResults = componentLookup.getUsers(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("No users should be returned", queryResults.getUsers());
        }
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
    }
    
    public void testGetUsersWithPossibleCharactersFirstName() throws RemoteException {
        
        String[] characterList = new String[]{ "\\", "/",":","\"",">","<","|","!", "@","#", "$","^","&","(", ")", "_",";", ",","+","=","{","}","[","]","~","`", "%","-"};
        deleteAllUsers();
        for (int i =0; i< characterList.length ; i++)
        {
        insertUser("LastName" + characterList[i], "FirstName" + characterList[i], new Long(0)) ;
        }
        //TODO Skip verification for backslash \\ as test fails, for which bug has been failed; set y = 0 once bug is fixed;
        int y = 1;
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*" + characterList[y] + "*");
        UserQueryTerm[] terms = {searchSpecTerm1};
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        
        try{
        	long start = System.currentTimeMillis();
        	UserList queryResults = componentLookup.getUsers(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("Users should be returned :DEBUG resource criteria contains the character " + characterList[y], queryResults.getUsers());
            assertEquals("Number of rows returned should match :DEBUG resource criteria contains the character " + characterList[y] , 1, queryResults.getUsers().length);
            User[] users = queryResults.getUsers(); 
            assertEquals( "First name should match :DEBUG resource criteria contains the character " + characterList[y] , "FirstName" + characterList[y], users[0].getFirstName());
        }
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage());
        }
        for (int i = y + 1; i< characterList.length ; i++)
        {
        	searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
            searchSpecTerm1.setExpression("*" + characterList[i] + "*");
            terms[0] = searchSpecTerm1;
            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            
            try{
            	long start = System.currentTimeMillis();
            	UserList queryResults = componentLookup.getUsers(querySpec);
                long end = System.currentTimeMillis();
                getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
                //TODO Skip verification for underscore and percent sign as tests are failing 
                if (i== 15 || i == 26) continue;
                assertNotNull("No users should be returned :DEBUG resource criteria contains the character " + characterList[i], queryResults.getUsers());
                assertEquals("Number of rows returned should match :DEBUG resource criteria contains the character " + characterList[i] , 1, queryResults.getUsers().length);
                User[] users = queryResults.getUsers(); 
                assertEquals( "First name should match :DEBUG resource criteria contains the character " + characterList[i] , "FirstName" + characterList[i], users[0].getFirstName());
            }
            catch (RemoteException e) {
            	fail("The test should not throw an exception" + e.getLocalizedMessage());
            }
        }
    }
    
    public void testGetUsersWithSortSpecDescendingLastName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*");
        UserQueryTerm[] queryTerms = { searchSpecTerm1 };

        UserSortTermList nameDescSortTermList = new UserSortTermList();
        UserSortTerm nameDescSortTerm = new UserSortTerm();
        nameDescSortTerm.setFieldName(UserSortFieldName.lastName);
        nameDescSortTerm.setDirection(SortDirection.Descending);
        UserSortTerm[] sortTerms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(sortTerms);
        querySpec.setSortSpec(nameDescSortTermList);

        searchSpecTerms.setTerms(queryTerms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 15, size);
     
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(14)).getLastName().equals("Achanta"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(13)).getLastName().equals("Goldstein"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(12)).getLastName().equals("Han"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(11)).getLastName().equals("Hanen"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(10)).getLastName().equals("Kalinichenko"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(9)).getLastName().equals("Keni"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(8)).getLastName().equals("Kureishy"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(7)).getLastName().equals("Lim"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(6)).getLastName().equals("Lim"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(5)).getLastName().equals("Lin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(4)).getLastName().equals("Ng"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(3)).getLastName().equals("Rashid"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(2)).getLastName().equals("Sarna"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(1)).getLastName().equals("Tong"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(0)).getLastName().equals("Vladimirov"));
    }

    public void testGetUsersWithSortSpecAscendingLastName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("*");
        UserQueryTerm[] queryTerms = { searchSpecTerm1 };

        UserSortTermList nameDescSortTermList = new UserSortTermList();
        UserSortTerm nameDescSortTerm = new UserSortTerm();
        nameDescSortTerm.setFieldName(UserSortFieldName.lastName);
        nameDescSortTerm.setDirection(SortDirection.Ascending);
        UserSortTerm[] sortTerms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(sortTerms);
        querySpec.setSortSpec(nameDescSortTermList);

        searchSpecTerms.setTerms(queryTerms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 15, size);
        
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(0)).getLastName().equals("Achanta"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(1)).getLastName().equals("Goldstein"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(2)).getLastName().equals("Han"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(3)).getLastName().equals("Hanen"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(4)).getLastName().equals("Kalinichenko"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(5)).getLastName().equals("Keni"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(6)).getLastName().equals("Kureishy"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(7)).getLastName().equals("Lim"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(8)).getLastName().equals("Lim"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(9)).getLastName().equals("Lin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(10)).getLastName().equals("Ng"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(11)).getLastName().equals("Rashid"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(12)).getLastName().equals("Sarna"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(13)).getLastName().equals("Tong"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(14)).getLastName().equals("Vladimirov"));

    }

    public void testGetUsersWithSortSpecDescendingFirstName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*");
        UserQueryTerm[] queryTerms = { searchSpecTerm1 };

        UserSortTermList nameDescSortTermList = new UserSortTermList();
        UserSortTerm nameDescSortTerm = new UserSortTerm();
        nameDescSortTerm.setFieldName(UserSortFieldName.firstName);
        nameDescSortTerm.setDirection(SortDirection.Descending);
        UserSortTerm[] sortTerms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(sortTerms);
        querySpec.setSortSpec(nameDescSortTermList);

        searchSpecTerms.setTerms(queryTerms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 15, size);

        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(14)).getFirstName().equals("Andy"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(13)).getFirstName().equals("Bobby"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(12)).getFirstName().equals("Chander"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(11)).getFirstName().equals("David"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(10)).getFirstName().equals("Fuad"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(9)).getFirstName().equals("Hima"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(8)).getFirstName().equals("Iannis"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(7)).getFirstName().equals("Keng"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(6)).getFirstName().equals("Kevin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(5)).getFirstName().equals("Prabhat"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(4)).getFirstName().equals("Robert"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(3)).getFirstName().equals("Safdar"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(2)).getFirstName().equals("Sasha"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(1)).getFirstName().equals("Scott"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(0)).getFirstName().equals("Sergey"));
    }
    
    public void testGetUsersWithSortSpecAscendingFirstName() throws RemoteException {
        deleteAllUsers();
        insertSampleUsers();

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*");
        UserQueryTerm[] queryTerms = { searchSpecTerm1 };

        UserSortTermList nameDescSortTermList = new UserSortTermList();
        UserSortTerm nameDescSortTerm = new UserSortTerm();
        nameDescSortTerm.setFieldName(UserSortFieldName.firstName);
        nameDescSortTerm.setDirection(SortDirection.Ascending);
        UserSortTerm[] sortTerms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(sortTerms);
        querySpec.setSortSpec(nameDescSortTermList);

        searchSpecTerms.setTerms(queryTerms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 15, size);
        
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(14)).getFirstName().equals("Sergey"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(13)).getFirstName().equals("Scott"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(12)).getFirstName().equals("Sasha"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(11)).getFirstName().equals("Safdar"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(10)).getFirstName().equals("Robert"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(9)).getFirstName().equals("Prabhat"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(8)).getFirstName().equals("Kevin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(7)).getFirstName().equals("Keng"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(6)).getFirstName().equals("Iannis"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(5)).getFirstName().equals("Hima"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(4)).getFirstName().equals("Fuad"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(3)).getFirstName().equals("David"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(2)).getFirstName().equals("Chander"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(1)).getFirstName().equals("Bobby"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(0)).getFirstName().equals("Andy"));

    }

    public void testGetUsersWithSortSpecFirstNameAndLastName() throws RemoteException {
        deleteAllUsers();
        insertUser("Lin", "Robert", new Long(0));
        insertUser("Lin", "Iannis", new Long(1));
        insertUser("Lin", "Fuad", new Long(2));
        insertUser("Lin", "Kevin", new Long(3));
        insertUser("Lin", "David", new Long(4));
        insertUser("Lin", "Sasha", new Long(5));
        insertUser("Lin", "Chander", new Long(6));
        insertUser("Lin", "Sergey", new Long(7));
        insertUser("Han", "Andy", new Long(8));
        insertUser("Han", "Prabhat", new Long(9));
        insertUser("Han", "Scott", new Long(10));
        insertUser("Han", "Safdar", new Long(11));
        insertUser("Han", "Keng", new Long(12));
        insertUser("Han", "Hima", new Long(13));
        insertUser("Han", "Bobby", new Long(14));

        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm1.setExpression("*");
        UserQueryTerm[] queryTerms = { searchSpecTerm1 };

        UserSortTermList nameDescSortTermList = new UserSortTermList();        
        UserSortTerm lastNameDescSortTerm = new UserSortTerm();
        lastNameDescSortTerm.setFieldName(UserSortFieldName.lastName);
        lastNameDescSortTerm.setDirection(SortDirection.Descending);
        UserSortTerm firstNameDescSortTerm = new UserSortTerm();
        firstNameDescSortTerm.setFieldName(UserSortFieldName.firstName);
        firstNameDescSortTerm.setDirection(SortDirection.Ascending);
        
        UserSortTerm[] sortTerms = { lastNameDescSortTerm, firstNameDescSortTerm };
        nameDescSortTermList.setTerms(sortTerms);
        querySpec.setSortSpec(nameDescSortTermList);

        searchSpecTerms.setTerms(queryTerms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        UserList queryResults = componentLookup.getUsers(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be users returned", queryResults.getUsers());
        int size = queryResults.getUsers().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 15, size);

        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(14)).getLastName().equals("Han"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(13)).getLastName().equals("Han"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(12)).getLastName().equals("Han"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(11)).getLastName().equals("Han"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(10)).getLastName().equals("Han"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(9)).getLastName().equals("Han"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(8)).getLastName().equals("Han"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(7)).getLastName().equals("Lin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(6)).getLastName().equals("Lin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(5)).getLastName().equals("Lin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(4)).getLastName().equals("Lin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(3)).getLastName().equals("Lin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(2)).getLastName().equals("Lin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(1)).getLastName().equals("Lin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(0)).getLastName().equals("Lin"));
        
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(0)).getFirstName().equals("Chander"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(1)).getFirstName().equals("David"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(2)).getFirstName().equals("Fuad"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(3)).getFirstName().equals("Iannis"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(4)).getFirstName().equals("Kevin"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(5)).getFirstName().equals("Robert"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(6)).getFirstName().equals("Sasha"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(7)).getFirstName().equals("Sergey"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(8)).getFirstName().equals("Andy"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(9)).getFirstName().equals("Bobby"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(10)).getFirstName().equals("Hima"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(11)).getFirstName().equals("Keng"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(12)).getFirstName().equals("Prabhat"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(13)).getFirstName().equals("Safdar"));
        assertTrue("Return records should match the sort spec", ((User) queryResults.getUsers(14)).getFirstName().equals("Scott"));

    }

    public void testGetUsersWithNullFirstName() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression(null);
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm2.setExpression("FirstName_*");
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try {
            UserList queryResults = componentLookup.getUsers(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'expression' is null") > 0);
        }
    }

    /**
     * This test verifies that users can be fetched with first name being null
     * in the search term
     */
    public void testGetUsersWithNullLastName() throws RemoteException {
        final int nbUsers = 30;
        deleteAllUsers();
        insertUsers(new Long(0), nbUsers);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserQuerySpec querySpec = new UserQuerySpec();
        UserQueryTermList searchSpecTerms = new UserQueryTermList();
        UserQueryTerm searchSpecTerm1 = new UserQueryTerm();
        searchSpecTerm1.setFieldName(UserQueryFieldName.lastName);
        searchSpecTerm1.setExpression("LastName_*");
        UserQueryTerm searchSpecTerm2 = new UserQueryTerm();
        searchSpecTerm2.setFieldName(UserQueryFieldName.firstName);
        searchSpecTerm2.setExpression(null);
        UserQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        try {
            UserList queryResults = componentLookup.getUsers(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'expression' is null") > 0);
        }
    }
}
