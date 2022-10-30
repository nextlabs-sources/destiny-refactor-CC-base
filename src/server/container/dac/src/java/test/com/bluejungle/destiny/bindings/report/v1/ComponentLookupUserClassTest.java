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
import com.bluejungle.destiny.types.users.v1.UserClass;
import com.bluejungle.destiny.types.users.v1.UserClassList;
import com.bluejungle.destiny.types.users.v1.UserClassQueryFieldName;
import com.bluejungle.destiny.types.users.v1.UserClassQuerySpec;
import com.bluejungle.destiny.types.users.v1.UserClassQueryTerm;
import com.bluejungle.destiny.types.users.v1.UserClassQueryTermList;
import com.bluejungle.destiny.types.users.v1.UserClassSortFieldName;
import com.bluejungle.destiny.types.users.v1.UserClassSortTerm;
import com.bluejungle.destiny.types.users.v1.UserClassSortTermList;

/**
 * This is the test class for the user class API of the component lookup
 * service.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ComponentLookupUserClassTest.java#1 $
 */

public class ComponentLookupUserClassTest extends ComponentLookupIFImplTest {

    /**
     * Constructor
     * 
     * @param testName name of the test to run
     */
    public ComponentLookupUserClassTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that if the getUserClasses() API returns no records,
     * the data structure returned is still correct.
     */
    public void testGetUserClassesWithNoRecords() {
        final int nbUsers = 10;
        deleteAllUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            long start = System.currentTimeMillis();
            UserClassList noUserClassList = componentLookup.getUserClasses(null);
            long end = System.currentTimeMillis();
            getLog().info("GetUserClasses with no result timing: " + (end - start) + " ms");
            assertNotNull("Result should be returned even if no user classes have been found", noUserClassList);
            assertNull("Result should be returned even if no user classes have been found", noUserClassList.getClasses());
        } catch (RemoteException e) {
            fail("The service invocation should not throw any exception");
        }
    }

    /**
     * This test verifies that user classes can be fetched with a search spec on
     * the firstname or last name
     */
    public void testGetUserClassesWithQuerySpec() {
        final int nbClasses = 30;
        insertUserClasses(new Long(0), nbClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec nameLike3QuerySpec = new UserClassQuerySpec();
            UserClassQueryTermList like3QueryTermList = new UserClassQueryTermList();
            UserClassQueryTerm like3Term = new UserClassQueryTerm();
            like3Term.setFieldName(UserClassQueryFieldName.name);
            like3Term.setExpression("*3");
            UserClassQueryTerm[] terms = { like3Term };
            like3QueryTermList.setTerms(terms);
            nameLike3QuerySpec.setSearchSpec(like3QueryTermList);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(nameLike3QuerySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUserClasses with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be user classes returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 3, size);
            for (int index = 0; index < size; index++) {
                UserClass userClass = queryResults.getClasses(index);
                assertTrue("Returned records should match the query spec for name", userClass.getName().endsWith("3"));
                assertTrue("Returned records should match the query spec for name", userClass.getName().endsWith("3"));
            }

            UserClassQuerySpec displayNameLike3QuerySpec = new UserClassQuerySpec();
            like3QueryTermList = new UserClassQueryTermList();
            like3Term = new UserClassQueryTerm();
            like3Term.setFieldName(UserClassQueryFieldName.displayName);
            like3Term.setExpression("*3");
            terms = new UserClassQueryTerm[] { like3Term };
            like3QueryTermList.setTerms(terms);
            displayNameLike3QuerySpec.setSearchSpec(like3QueryTermList);
            start = System.currentTimeMillis();
            queryResults = componentLookup.getUserClasses(displayNameLike3QuerySpec);
            end = System.currentTimeMillis();
            getLog().info("GetUserClasses with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be user classes returned", queryResults.getClasses());
            size = queryResults.getClasses().length;
            assertEquals("The search spec should return the correct number of records", 3, size);
            for (int index = 0; index < size; index++) {
                UserClass userClass = queryResults.getClasses(index);
                assertTrue("Returned records should match the query spec for display name", userClass.getDisplayName().endsWith("3"));
                assertTrue("Returned records should match the query spec for display name", userClass.getDisplayName().endsWith("3"));
            }
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    /**
     * This test verifies that user classes can be fetched with a sort spec on
     * the class name
     */
    public void testGetUserClassesWithSortSpec() {
        final int nbUserClasses = 20;
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            //Query for users with a search spec
            UserClassQuerySpec nameDescQuerySpec = new UserClassQuerySpec();
            UserClassSortTermList nameDescSortTermList = new UserClassSortTermList();
            UserClassSortTerm nameDescSortTerm = new UserClassSortTerm();
            nameDescSortTerm.setFieldName(UserClassSortFieldName.name);
            nameDescSortTerm.setDirection(SortDirection.Descending);
            UserClassSortTerm[] terms = { nameDescSortTerm };
            nameDescSortTermList.setTerms(terms);
            nameDescQuerySpec.setSortSpec(nameDescSortTermList);
            long start = System.currentTimeMillis();
            UserClassList sortedUserClassResults = componentLookup.getUserClasses(nameDescQuerySpec);
            long end = System.currentTimeMillis();
            getLog().info("getUserClasses with sort spec timing: " + (end - start) + " ms");
            assertNotNull("There should be users returned", sortedUserClassResults.getClasses());
            int size = sortedUserClassResults.getClasses().length;
            assertEquals("The sort spec should return all records", nbUserClasses, size);
            String lastRecordName = sortedUserClassResults.getClasses()[0].getName();
            for (int index = 0; index < size; index++) {
                String currentName = sortedUserClassResults.getClasses()[index].getName();
                int result = currentName.compareTo(lastRecordName);
                assertTrue("Results should be sorted by name descending", result <= 0);
                lastRecordName = currentName;
            }
            
            //Query the results with display name this time 
            nameDescQuerySpec = new UserClassQuerySpec();
            nameDescSortTermList = new UserClassSortTermList();
            nameDescSortTerm = new UserClassSortTerm();
            nameDescSortTerm.setFieldName(UserClassSortFieldName.displayName);
            nameDescSortTerm.setDirection(SortDirection.Descending);
            terms = new UserClassSortTerm[] { nameDescSortTerm };
            nameDescSortTermList.setTerms(terms);
            nameDescQuerySpec.setSortSpec(nameDescSortTermList);
            start = System.currentTimeMillis();
            sortedUserClassResults = componentLookup.getUserClasses(nameDescQuerySpec);
            end = System.currentTimeMillis();
            getLog().info("getUserClasses with sort spec timing: " + (end - start) + " ms");
            assertNotNull("There should be users returned", sortedUserClassResults.getClasses());
            size = sortedUserClassResults.getClasses().length;
            assertEquals("The sort spec should return all records", nbUserClasses, size);
            lastRecordName = sortedUserClassResults.getClasses()[0].getDisplayName();
            for (int index = 0; index < size; index++) {
                String currentName = sortedUserClassResults.getClasses()[index].getDisplayName();
                int result = currentName.compareTo(lastRecordName);
                assertTrue("Results display names should be sorted by name descending", result <= 0);
                lastRecordName = currentName;
            }
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllPolicies();
        }
    }

    /**
     * This test verifies getUserClasses() with null input
     */
    public void testGetUserClassesWithNullQuerySpec() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            componentLookup.getUserClasses(null);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    /**
     * This test verifies getUserClasses() with null searchSpec and null
     * sortSpec in UserClassQuerySpec.
     */
    public void testGetUserClassesWithNullSearchSpecAndNullSortSpec() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            querySpec.setSearchSpec(null);
            querySpec.setSortSpec(null);
            componentLookup.getUserClasses(querySpec);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    /**
     * This test verifies getUserClasses() with null searchSpec in
     * UserClassQuerySpec.
     */
    public void testGetUserClassesWithNullSearchSpec() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            querySpec.setSearchSpec(null);
            UserClassSortTermList nameDescSortTermList = new UserClassSortTermList();
            UserClassSortTerm nameDescSortTerm = new UserClassSortTerm();
            nameDescSortTerm.setFieldName(UserClassSortFieldName.name);
            nameDescSortTerm.setDirection(SortDirection.Descending);
            UserClassSortTerm[] terms = { nameDescSortTerm };
            nameDescSortTermList.setTerms(terms);
            querySpec.setSortSpec(nameDescSortTermList);
            componentLookup.getUserClasses(querySpec);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    /**
     * This test verifies that user classes can be fetched by a search with 
     * number
     */
    public void testGetUserClassesWithNumber() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*1");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be user classes returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            assertEquals("The search spec should return the correct number of records", 3, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }
    
    /**
     * This test verifies getUserClasses() with null sortSpec in
     * UserClassQuerySpec.
     */
    public void testGetUserClassesWithNullSortSpec() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec nameLike3QuerySpec = new UserClassQuerySpec();
            UserClassQueryTermList like3QueryTermList = new UserClassQueryTermList();
            UserClassQueryTerm like3Term = new UserClassQueryTerm();
            like3Term.setFieldName(UserClassQueryFieldName.name);
            like3Term.setExpression("*3");
            UserClassQueryTerm[] terms = { like3Term };
            like3QueryTermList.setTerms(terms);
            nameLike3QuerySpec.setSearchSpec(like3QueryTermList);
            nameLike3QuerySpec.setSortSpec(null);
            componentLookup.getUserClasses(nameLike3QuerySpec);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetUserClassesWithFiveRandomSearchSpec() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression(RANDOM_SEARCH_EXPR_ONE);
            UserClassQueryTerm searchSpecTerm2 = new UserClassQueryTerm();
            searchSpecTerm2.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
            UserClassQueryTerm searchSpecTerm3 = new UserClassQueryTerm();
            searchSpecTerm3.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm3.setExpression(RANDOM_SEARCH_EXPR_THREE);
            UserClassQueryTerm searchSpecTerm4 = new UserClassQueryTerm();
            searchSpecTerm4.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
            UserClassQueryTerm searchSpecTerm5 = new UserClassQueryTerm();
            searchSpecTerm5.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm5.setExpression(RANDOM_SEARCH_EXPR_FIVE);
            UserClassQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("There should not be hosts returned", queryResults.getClasses());
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithRandomSearchSpecAndRandomNullSearchExpression() throws RemoteException {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        ComponentLookupIF componentLookup = getComponentLookup();
        UserClassQuerySpec querySpec = new UserClassQuerySpec();
        UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
        UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
        searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
        searchSpecTerm1.setExpression(null);
        UserClassQueryTerm searchSpecTerm2 = new UserClassQueryTerm();
        searchSpecTerm2.setFieldName(UserClassQueryFieldName.name);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        UserClassQueryTerm searchSpecTerm3 = new UserClassQueryTerm();
        searchSpecTerm3.setFieldName(UserClassQueryFieldName.name);
        searchSpecTerm3.setExpression(null);
        UserClassQueryTerm searchSpecTerm4 = new UserClassQueryTerm();
        searchSpecTerm4.setFieldName(UserClassQueryFieldName.name);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        UserClassQueryTerm searchSpecTerm5 = new UserClassQueryTerm();
        searchSpecTerm5.setFieldName(UserClassQueryFieldName.name);
        searchSpecTerm5.setExpression(null);
        UserClassQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try {
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'expression' is null.") > 0);
        }
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetUserClassesWithRandomSearchSpecAndRandomNullSearchFieldName() throws RemoteException {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);

        ComponentLookupIF componentLookup = getComponentLookup();
        UserClassQuerySpec querySpec = new UserClassQuerySpec();
        UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
        UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
        searchSpecTerm1.setFieldName(null);
        searchSpecTerm1.setExpression(RANDOM_SEARCH_EXPR_ONE);
        UserClassQueryTerm searchSpecTerm2 = new UserClassQueryTerm();
        searchSpecTerm2.setFieldName(UserClassQueryFieldName.name);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        UserClassQueryTerm searchSpecTerm3 = new UserClassQueryTerm();
        searchSpecTerm3.setFieldName(null);
        searchSpecTerm3.setExpression(RANDOM_SEARCH_EXPR_THREE);
        UserClassQueryTerm searchSpecTerm4 = new UserClassQueryTerm();
        searchSpecTerm4.setFieldName(UserClassQueryFieldName.name);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        UserClassQueryTerm searchSpecTerm5 = new UserClassQueryTerm();
        searchSpecTerm5.setFieldName(null);
        searchSpecTerm5.setExpression(RANDOM_SEARCH_EXPR_FIVE);
        UserClassQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try {
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("fieldName") > 0);
        }
    }

    public void testGetAllUserClassesByStar() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            assertEquals("The search spec should return the correct number of records", 30, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetAllUserClassesByPrefix() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("UserClass_*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 30, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetAllUserClassesByFirstLetter() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*_*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 30, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetAllUserClassesBySuffix() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("***");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 30, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetAllUserClassesByRegularExpression() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("UserClass_**");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 30, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetAllUserClassesByExclude() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("?*_?*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be user classes returned", queryResults.getClasses());
            int size = queryResults.getClasses().length; //We should have three
            // matching rows
            assertEquals("The search spec should return the correct number of records", 30, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesSingleDigits() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm2 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm3 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm4 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm5 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm6 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm7 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm8 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm9 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("UserClass_1");
            searchSpecTerm2.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm2.setExpression("UserClass_2");
            searchSpecTerm3.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm3.setExpression("UserClass_3");
            searchSpecTerm4.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm4.setExpression("UserClass_4");
            searchSpecTerm5.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm5.setExpression("UserClass_5");
            searchSpecTerm6.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm6.setExpression("UserClass_6");
            searchSpecTerm7.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm7.setExpression("UserClass_7");
            searchSpecTerm8.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm8.setExpression("UserClass_8");
            searchSpecTerm9.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm9.setExpression("UserClass_9");
            UserClassQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5, searchSpecTerm6, searchSpecTerm7, searchSpecTerm8, searchSpecTerm9 };
            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("There should not be user classes returned", queryResults.getClasses());
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesSingleDigitsOne() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm2 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm3 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm4 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm5 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm6 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm7 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm8 = new UserClassQueryTerm();
            UserClassQueryTerm searchSpecTerm9 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("UserClass_1");
            searchSpecTerm2.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm2.setExpression("UserClass_1");
            searchSpecTerm3.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm3.setExpression("UserClass_1");
            searchSpecTerm4.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm4.setExpression("UserClass_1");
            searchSpecTerm5.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm5.setExpression("UserClass_1");
            searchSpecTerm6.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm6.setExpression("UserClass_1");
            searchSpecTerm7.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm7.setExpression("UserClass_1");
            searchSpecTerm8.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm8.setExpression("UserClass_1");
            searchSpecTerm9.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm9.setExpression("UserClass_1");
            UserClassQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5, searchSpecTerm6, searchSpecTerm7, searchSpecTerm8, searchSpecTerm9 };
            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 1, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithOne() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.displayName);
            searchSpecTerm1.setExpression("*_*1*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be user classes returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 12, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithFive() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.displayName);
            searchSpecTerm1.setExpression("*?5");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be user classes returned", queryResults.getClasses());
            int size = queryResults.getClasses().length; //We should have three
            // matching rows
            assertEquals("The search spec should return the correct number of records", 3, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithExactName() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("Engineering");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 1, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithRandomWildCards() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*B?*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("There should not be hosts returned", queryResults.getClasses());
            //int size = queryResults.getClasses().length;
            //We should have three matching rows
            //assertEquals("The search spec should return the correct number of
            // records", 0, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithRandomWildCardsAndLetters() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("D?*t*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be user classes returned", queryResults.getClasses());
            int size = queryResults.getClasses().length; //We should have three
            // matching rows
            assertEquals("The search spec should return the correct number of records", 1, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithStartingLetter() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("D*");
            UserClassQueryTerm searchSpecTerm2 = new UserClassQueryTerm();
            searchSpecTerm2.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm2.setExpression("E*");
            UserClassQueryTerm searchSpecTerm3 = new UserClassQueryTerm();
            searchSpecTerm3.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm3.setExpression("F*");
            UserClassQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3 };
            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("There should not be user classes returned", queryResults.getClasses());
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithRandomStringOnUniqueRecords() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("[abcdefghijklmnopqrstuvwxyz_+(*&^)%$#@!<> 1234567890]*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("There should not be hosts returned", queryResults.getClasses());
            //int size = queryResults.getClasses().length;
            //We should have three matching rows
            //assertEquals("The search spec should return the correct number of
            // records", 0, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithSpace() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("* *");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 4, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithWildCardsAndLettersAndNumbers() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*z0?*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNull("There should not be hosts returned", queryResults.getClasses());
            //int size = queryResults.getClasses().length;
            //We should have three matching rows
            //assertEquals("The search spec should return the correct number of
            // records", 0, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithRepeatingLettersAndWildCards() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*e*e*e*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be user classes returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            assertEquals("The search spec should return the correct number of records", 2, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }
    
    public void testGetUserClassesWithCaseInsensitiveString() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("enGINEerinG");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };
            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be user classes returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            assertEquals("The search spec should return the correct number of records", 1, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    /**
     * The following test verifies whether IllegalArgumentException is thrown when backspace is passed as input to policy criteria
     */
    
    public void testGetUserClassesWithEscapesequenceBackspace() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*\b*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };
            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            fail("The test should thrown an exception as backspace is not a valid input" );
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("java.io.IOException: java.lang.IllegalArgumentException:")> 0 );
        } finally {
            deleteAllUserClasses();
        }
    }
    
    public void testGetUserClassesWithEscapesequenceFormfeed() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*\f*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };
            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            fail("The test should thrown an exception as backspace is not a valid input" );
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("java.io.IOException: java.lang.IllegalArgumentException:")> 0 );
        } finally {
            deleteAllUserClasses();
        }
    }
    
    public void testGetUserClassesWithEscapesequenceTab() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*\t*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUserClasses with search spec timing: " + (end - start) + " ms");
            assertNull("No user classes should be returned", queryResults.getClasses());
        } catch (RemoteException e) {
            fail("The test should not thrown an exception" + e.getLocalizedMessage());
        } finally {
            deleteAllUserClasses();
        }
    }
    
    public void testGetUserClassesWithEscapesequenceLineFeed() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*\n*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUserClasses with search spec timing: " + (end - start) + " ms");
            assertNull("No user classes should be returned", queryResults.getClasses());
        } catch (RemoteException e) {
            fail("The test should not thrown an exception" + e.getLocalizedMessage());
        } finally {
            deleteAllUserClasses();
        }
    }
    
    public void testGetUserClassesWithEscapesequenceCarriagereturn() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*\r*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUserClasses with search spec timing: " + (end - start) + " ms");
            assertNull("No user classes should be returned", queryResults.getClasses());
        } catch (RemoteException e) {
            fail("The test should not thrown an exception" + e.getLocalizedMessage());
        } finally {
            deleteAllUserClasses();
        }
    }
    
    public void testGetUserClassesWithEscapesequenceDoublequote() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*\"*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUserClasses with search spec timing: " + (end - start) + " ms");
            assertNull("No user classes should be returned", queryResults.getClasses());
        } catch (RemoteException e) {
            fail("The test should not thrown an exception" + e.getLocalizedMessage());
        } finally {
            deleteAllUserClasses();
        }
    }
    
    public void testGetUserClassesWithEscapesequenceSinglequote() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*\'*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUserClasses with search spec timing: " + (end - start) + " ms");
            assertNull("No user classes should be returned", queryResults.getClasses());
        } catch (RemoteException e) {
            fail("The test should not thrown an exception" + e.getLocalizedMessage());
        } finally {
            deleteAllUserClasses();
        }
    }
    
    public void testGetUserClassesWithEscapesequenceBackslash() {
        final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertUserClasses(new Long(0), nbUserClasses);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*\\*");
            UserClassQueryTerm[] terms = { searchSpecTerm1 };

            searchSpecTerms.setTerms(terms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUserClasses with search spec timing: " + (end - start) + " ms");
            assertNull("No user classes should be returned", queryResults.getClasses());
        } catch (RemoteException e) {
            fail("The test should not thrown an exception" + e.getLocalizedMessage());
        } finally {
            deleteAllUserClasses();
        }
    }
    
    public void testGetUserClassesWithSortSpecDescending() {
        //final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*");
            UserClassQueryTerm[] queryTerms = { searchSpecTerm1 };

            UserClassSortTermList nameDescSortTermList = new UserClassSortTermList();
            UserClassSortTerm nameDescSortTerm = new UserClassSortTerm();
            nameDescSortTerm.setFieldName(UserClassSortFieldName.name);
            nameDescSortTerm.setDirection(SortDirection.Descending);
            UserClassSortTerm[] sortTerms = { nameDescSortTerm };
            nameDescSortTermList.setTerms(sortTerms);
            querySpec.setSortSpec(nameDescSortTermList);

            searchSpecTerms.setTerms(queryTerms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 13, size);

            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(12)).getName().equals("Desktops"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(11)).getName().equals("Engineering"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(10)).getName().equals("Executive"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(9)).getName().equals("Finance"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(8)).getName().equals("Human Resource"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(7)).getName().equals("Laptops"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(6)).getName().equals("Linux"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(5)).getName().equals("Marketing"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(4)).getName().equals("Offsite"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(3)).getName().equals("Quality Assurance"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(2)).getName().equals("Servers"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(1)).getName().equals("Windows 2003"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(0)).getName().equals("Windows XP"));

        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }

    public void testGetUserClassesWithSortSpecAscending() {
        //final int nbUserClasses = 30;
        deleteAllUserClasses();
        insertSampleUserClasses();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            UserClassQuerySpec querySpec = new UserClassQuerySpec();
            UserClassQueryTermList searchSpecTerms = new UserClassQueryTermList();
            UserClassQueryTerm searchSpecTerm1 = new UserClassQueryTerm();
            searchSpecTerm1.setFieldName(UserClassQueryFieldName.name);
            searchSpecTerm1.setExpression("*");
            UserClassQueryTerm[] queryTerms = { searchSpecTerm1 };

            UserClassSortTermList nameDescSortTermList = new UserClassSortTermList();
            UserClassSortTerm nameDescSortTerm = new UserClassSortTerm();
            nameDescSortTerm.setFieldName(UserClassSortFieldName.name);
            nameDescSortTerm.setDirection(SortDirection.Ascending);
            UserClassSortTerm[] sortTerms = { nameDescSortTerm };
            nameDescSortTermList.setTerms(sortTerms);
            querySpec.setSortSpec(nameDescSortTermList);

            searchSpecTerms.setTerms(queryTerms);
            querySpec.setSearchSpec(searchSpecTerms);
            long start = System.currentTimeMillis();
            UserClassList queryResults = componentLookup.getUserClasses(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetUsers with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getClasses());
            int size = queryResults.getClasses().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 13, size);
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(0)).getName().equals("Desktops"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(1)).getName().equals("Engineering"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(2)).getName().equals("Executive"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(3)).getName().equals("Finance"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(4)).getName().equals("Human Resource"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(5)).getName().equals("Laptops"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(6)).getName().equals("Linux"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(7)).getName().equals("Marketing"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(8)).getName().equals("Offsite"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(9)).getName().equals("Quality Assurance"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(10)).getName().equals("Servers"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(11)).getName().equals("Windows 2003"));
            assertTrue("Return records should match the sort spec", ((UserClass) queryResults.getClasses(12)).getName().equals("Windows XP"));

        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllUserClasses();
        }
    }
}
