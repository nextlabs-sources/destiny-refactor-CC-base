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
import com.bluejungle.destiny.types.hosts.v1.Host;
import com.bluejungle.destiny.types.hosts.v1.HostList;
import com.bluejungle.destiny.types.hosts.v1.HostQueryFieldName;
import com.bluejungle.destiny.types.hosts.v1.HostQuerySpec;
import com.bluejungle.destiny.types.hosts.v1.HostQueryTerm;
import com.bluejungle.destiny.types.hosts.v1.HostQueryTermList;
import com.bluejungle.destiny.types.hosts.v1.HostSortFieldName;
import com.bluejungle.destiny.types.hosts.v1.HostSortTerm;
import com.bluejungle.destiny.types.hosts.v1.HostSortTermList;

/**
 * This is the test class for the host API of the component lookup service
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ComponentLookupHostTest.java#1 $
 */

public class ComponentLookupHostTest extends ComponentLookupIFImplTest {

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test to run
     */
    public ComponentLookupHostTest(String testName) {
        super(testName);
    }

    /**
     * This test verifies that if the getHosts() API returns no records, the
     * data structure returned is still correct.
     */
    public void testGetHostsNoRecords() {
        deleteAllHosts();
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            long start = System.currentTimeMillis();
            HostList noHostsResult = componentLookup.getHosts(null);
            long end = System.currentTimeMillis();
            getLog().info("getHosts with no result timing: " + (end - start) + " ms");
            assertNotNull("Result should be returned even if no hosts have been found", noHostsResult);
            assertNull("Result should be returned even if no hosts have been found", noHostsResult.getHosts());
        } catch (RemoteException e) {
            fail("The service invocation should not throw any exception: " + e.getLocalizedMessage());
        }
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetHostsWithQuerySpec() {
        final int nbHosts = 30;
        insertHosts(new Long(0), nbHosts);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            //Query for hosts with a search spec on name *3*
            HostQuerySpec likeStar3StarQuerySpec = new HostQuerySpec();
            HostQueryTermList likeStar3StarQueryTermList = new HostQueryTermList();
            HostQueryTerm likeStar3StarTerm = new HostQueryTerm();
            likeStar3StarTerm.setFieldName(HostQueryFieldName.name);
            likeStar3StarTerm.setExpression("*3*");
            HostQueryTerm[] terms = { likeStar3StarTerm };
            likeStar3StarQueryTermList.setTerms(terms);
            likeStar3StarQuerySpec.setSearchSpec(likeStar3StarQueryTermList);
            long start = System.currentTimeMillis();
            HostList queryResults = componentLookup.getHosts(likeStar3StarQuerySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getHosts());
            int size = queryResults.getHosts().length;
            //We should have three matching rows
            assertEquals("The search spec should return the correct number of records", 3, size);
            for (int index = 0; index < size; index++) {
                Host host = queryResults.getHosts(index);
                assertTrue("Returned records should match the query spec", host.getName().endsWith("3"));
            }
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllPolicies();
        }
    }

    /**
     * This test verifies that users can be fetched with a sort spec on the
     * first name
     */
    public void testGetHostsWithSortSpec() {
        final int nbHosts = 20;
        insertHosts(new Long(0), nbHosts);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            //Query for hosts with a search spec
            HostQuerySpec nameDescQuerySpec = new HostQuerySpec();
            HostSortTermList nameDescSortTermList = new HostSortTermList();
            HostSortTerm nameDescSortTerm = new HostSortTerm();
            nameDescSortTerm.setFieldName(HostSortFieldName.name);
            nameDescSortTerm.setDirection(SortDirection.Descending);
            HostSortTerm[] terms = { nameDescSortTerm };
            nameDescSortTermList.setTerms(terms);
            nameDescQuerySpec.setSortSpec(nameDescSortTermList);
            long start = System.currentTimeMillis();
            HostList sortedHostResults = componentLookup.getHosts(nameDescQuerySpec);
            long end = System.currentTimeMillis();
            getLog().info("getHosts with sort spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", sortedHostResults.getHosts());
            int size = sortedHostResults.getHosts().length;
            assertEquals("The sort spec should return all records", nbHosts, size);
            String lastRecordFirstName = sortedHostResults.getHosts()[0].getName();
            for (int index = 0; index < size; index++) {
                String currentName = sortedHostResults.getHosts()[index].getName();
                int result = currentName.compareTo(lastRecordFirstName);
                assertTrue("Results should be sorted by first name descending", result <= 0);
                lastRecordFirstName = currentName;
            }
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllPolicies();
        }
    }

    public void testGetHostsAll() {
        final int nbHosts = 30;
        insertHosts(new Long(0), nbHosts);
        try {
            ComponentLookupIF componentLookup = getComponentLookup();
            //Query for hosts with a search spec on name *3*
            HostQuerySpec likeStar3StarQuerySpec = new HostQuerySpec();
            HostQueryTermList likeStar3StarQueryTermList = new HostQueryTermList();
            HostQueryTerm likeStar3StarTerm = new HostQueryTerm();
            likeStar3StarTerm.setFieldName(HostQueryFieldName.name);
            likeStar3StarTerm.setExpression("*");
            HostQueryTerm[] terms = { likeStar3StarTerm };
            likeStar3StarQueryTermList.setTerms(terms);
            likeStar3StarQuerySpec.setSearchSpec(likeStar3StarQueryTermList);
            long start = System.currentTimeMillis();
            HostList queryResults = componentLookup.getHosts(likeStar3StarQuerySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
            assertNotNull("There should be hosts returned", queryResults.getHosts());
            int size = queryResults.getHosts().length;
            assertEquals("The search spec should return the correct number of records", 30, size);
        } catch (RemoteException e) {
            fail("Invoking the service interface with a query spec should not fail");
        } finally {
            deleteAllPolicies();
        }
    }

    public void testGetHostsRandomExpressions() throws RemoteException {
        final int nbHosts = 30;
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        //Query for hosts with a search spec on name *3*
        HostQuerySpec likeStar3StarQuerySpec = new HostQuerySpec();
        HostQueryTermList likeStar3StarQueryTermList = new HostQueryTermList();
        HostQueryTerm likeStar3StarTerm = new HostQueryTerm();
        likeStar3StarTerm.setFieldName(HostQueryFieldName.name);
        likeStar3StarTerm.setExpression(RANDOM_SEARCH_EXPR_ONE);
        HostQueryTerm[] terms = { likeStar3StarTerm };
        likeStar3StarQueryTermList.setTerms(terms);
        likeStar3StarQuerySpec.setSearchSpec(likeStar3StarQueryTermList);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(likeStar3StarQuerySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getHosts());
    }

    /**
     * This test verifies getHostClasses() with null input
     *  
     */
    public void testGetHostWithNullQuerySpec() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        componentLookup.getHosts(null);
    }

    /**
     * This test verifies getHostClasses() with null searchSpec and null
     * sortSpec in HostClassQuerySpec.
     */
    public void testGetHostsWithNullSearchSpecAndNullSortSpec() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        querySpec.setSearchSpec(null);
        querySpec.setSortSpec(null);
        componentLookup.getHosts(querySpec);
    }

    /**
     * This test verifies getHostClasses() with null searchSpec in
     * HostClassQuerySpec.
     */
    public void testGetHostsWithNullSearchSpec() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        querySpec.setSearchSpec(null);
        HostSortTermList nameDescSortTermList = new HostSortTermList();
        HostSortTerm nameDescSortTerm = new HostSortTerm();
        nameDescSortTerm.setFieldName(HostSortFieldName.name);
        nameDescSortTerm.setDirection(SortDirection.Descending);
        HostSortTerm[] terms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(terms);
        querySpec.setSortSpec(nameDescSortTermList);
        componentLookup.getHosts(querySpec);
    }

    /**
     * This test verifies that users can be fetched with a search spec with
     * number
     */
    public void testGetHostsWithNumber() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*1");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        assertEquals("The search spec should return the correct number of records", 3, size);
    }
    
    
    /**
     * This test verifies getHostClasses() with null sortSpec in
     * HostClassQuerySpec.
     */
    public void testGetHostsWithNullSortSpec() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec nameLike3QuerySpec = new HostQuerySpec();
        HostQueryTermList like3QueryTermList = new HostQueryTermList();
        HostQueryTerm like3Term = new HostQueryTerm();
        like3Term.setFieldName(HostQueryFieldName.name);
        like3Term.setExpression("*3");
        HostQueryTerm[] terms = { like3Term };
        like3QueryTermList.setTerms(terms);
        nameLike3QuerySpec.setSearchSpec(like3QueryTermList);
        nameLike3QuerySpec.setSortSpec(null);
        componentLookup.getHosts(nameLike3QuerySpec);
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetHostsWithFiveRandomSearchSpec() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression(RANDOM_SEARCH_EXPR_ONE);
        HostQueryTerm searchSpecTerm2 = new HostQueryTerm();
        searchSpecTerm2.setFieldName(HostQueryFieldName.name);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        HostQueryTerm searchSpecTerm3 = new HostQueryTerm();
        searchSpecTerm3.setFieldName(HostQueryFieldName.name);
        searchSpecTerm3.setExpression(RANDOM_SEARCH_EXPR_THREE);
        HostQueryTerm searchSpecTerm4 = new HostQueryTerm();
        searchSpecTerm4.setFieldName(HostQueryFieldName.name);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        HostQueryTerm searchSpecTerm5 = new HostQueryTerm();
        searchSpecTerm5.setFieldName(HostQueryFieldName.name);
        searchSpecTerm5.setExpression(RANDOM_SEARCH_EXPR_FIVE);
        HostQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getHosts());
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetHostsWithRandomSearchSpecAndRandomNullSearchExpression() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression(null);
        HostQueryTerm searchSpecTerm2 = new HostQueryTerm();
        searchSpecTerm2.setFieldName(HostQueryFieldName.name);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        HostQueryTerm searchSpecTerm3 = new HostQueryTerm();
        searchSpecTerm3.setFieldName(HostQueryFieldName.name);
        searchSpecTerm3.setExpression(null);
        HostQueryTerm searchSpecTerm4 = new HostQueryTerm();
        searchSpecTerm4.setFieldName(HostQueryFieldName.name);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        HostQueryTerm searchSpecTerm5 = new HostQueryTerm();
        searchSpecTerm5.setFieldName(HostQueryFieldName.name);
        searchSpecTerm5.setExpression(null);
        HostQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        try {
            HostList queryResults = componentLookup.getHosts(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'expression' is null") > 0);
        }
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetHostsWithRandomSearchSpecAndRandomNullSearchFieldName() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(null);
        searchSpecTerm1.setExpression(RANDOM_SEARCH_EXPR_ONE);
        HostQueryTerm searchSpecTerm2 = new HostQueryTerm();
        searchSpecTerm2.setFieldName(HostQueryFieldName.name);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        HostQueryTerm searchSpecTerm3 = new HostQueryTerm();
        searchSpecTerm3.setFieldName(null);
        searchSpecTerm3.setExpression(RANDOM_SEARCH_EXPR_THREE);
        HostQueryTerm searchSpecTerm4 = new HostQueryTerm();
        searchSpecTerm4.setFieldName(HostQueryFieldName.name);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        HostQueryTerm searchSpecTerm5 = new HostQueryTerm();
        searchSpecTerm5.setFieldName(null);
        searchSpecTerm5.setExpression(RANDOM_SEARCH_EXPR_FIVE);
        HostQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try {
            HostList queryResults = componentLookup.getHosts(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("fieldName") > 0);
        }
    }

    public void testGetAllHostsByStar() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllHostsByPrefix() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("Host_*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllHostsByFirstLetter() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*_*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllHostsBySuffix() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("***");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllHostsByRegularExpression() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("Host_**");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllHostsByExclude() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("?*_?*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length; //We should have three
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetHostsSingleDigits() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm2 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm3 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm4 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm5 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm6 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm7 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm8 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm9 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("Host_1");
        searchSpecTerm2.setFieldName(HostQueryFieldName.name);
        searchSpecTerm2.setExpression("Host_2");
        searchSpecTerm3.setFieldName(HostQueryFieldName.name);
        searchSpecTerm3.setExpression("Host_3");
        searchSpecTerm4.setFieldName(HostQueryFieldName.name);
        searchSpecTerm4.setExpression("Host_4");
        searchSpecTerm5.setFieldName(HostQueryFieldName.name);
        searchSpecTerm5.setExpression("Host_5");
        searchSpecTerm6.setFieldName(HostQueryFieldName.name);
        searchSpecTerm6.setExpression("Host_6");
        searchSpecTerm7.setFieldName(HostQueryFieldName.name);
        searchSpecTerm7.setExpression("Host_7");
        searchSpecTerm8.setFieldName(HostQueryFieldName.name);
        searchSpecTerm8.setExpression("Host_8");
        searchSpecTerm9.setFieldName(HostQueryFieldName.name);
        searchSpecTerm9.setExpression("Host_9");
        HostQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5, searchSpecTerm6, searchSpecTerm7, searchSpecTerm8, searchSpecTerm9 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getHosts());
    }

    public void testGetHostsSingleDigitsOne() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm2 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm3 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm4 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm5 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm6 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm7 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm8 = new HostQueryTerm();
        HostQueryTerm searchSpecTerm9 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("Host_1");
        searchSpecTerm2.setFieldName(HostQueryFieldName.name);
        searchSpecTerm2.setExpression("Host_1");
        searchSpecTerm3.setFieldName(HostQueryFieldName.name);
        searchSpecTerm3.setExpression("Host_1");
        searchSpecTerm4.setFieldName(HostQueryFieldName.name);
        searchSpecTerm4.setExpression("Host_1");
        searchSpecTerm5.setFieldName(HostQueryFieldName.name);
        searchSpecTerm5.setExpression("Host_1");
        searchSpecTerm6.setFieldName(HostQueryFieldName.name);
        searchSpecTerm6.setExpression("Host_1");
        searchSpecTerm7.setFieldName(HostQueryFieldName.name);
        searchSpecTerm7.setExpression("Host_1");
        searchSpecTerm8.setFieldName(HostQueryFieldName.name);
        searchSpecTerm8.setExpression("Host_1");
        searchSpecTerm9.setFieldName(HostQueryFieldName.name);
        searchSpecTerm9.setExpression("Host_1");
        HostQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5, searchSpecTerm6, searchSpecTerm7, searchSpecTerm8, searchSpecTerm9 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetHostsWithOne() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*_*1*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 12, size);
    }

    public void testGetHostsWithFive() throws RemoteException {
        final int nbHosts = 30;
        deleteAllHosts();
        insertHosts(new Long(0), nbHosts);
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*?5");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        assertEquals("The search spec should return the correct number of records", 3, size);
    }

    public void testGetHostsWithExactName() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();

        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("cuba");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetHostsWithRandomWildCards() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*E?*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should not hosts returned", queryResults.getHosts());
        assertEquals("There should be 4 matches", 4, queryResults.getHosts().length);
    }

    public void testGetHostsWithRandomWildCardsAndLetters() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("S?*T*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetHostsWithStartingLetter() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("a*");
        HostQueryTerm searchSpecTerm2 = new HostQueryTerm();
        searchSpecTerm2.setFieldName(HostQueryFieldName.name);
        searchSpecTerm2.setExpression("b*");
        HostQueryTerm searchSpecTerm3 = new HostQueryTerm();
        searchSpecTerm3.setFieldName(HostQueryFieldName.name);
        searchSpecTerm3.setExpression("c*");
        HostQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getHosts());
    }

    public void testGetHostsWithRandomStringOnUniqueRecords() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("[abcdefghijklmnopqrstuvwxyz_+(*&^)%$#@!<> 1234567890]*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getHosts());
    }

    public void testGetHostsWithSpecialCharacter() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*-*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetHostsWithWildCardsAndLettersAndNumbers() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*z0?*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getHosts());
    }

    public void testGetHostsWithRepeatingLettersAndWildCards() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*e*e*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetHostsWithCaseInsensitiveString() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("ToBaGo");
        HostQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }
    public void testGetHostsWithEscapeSequenceBackspace() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*\b*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	HostList queryResults = componentLookup.getHosts(querySpec);
            fail("The test should throw an exception as backspace is not a valid input" );
        } 
        catch (RemoteException e) {
        	assertTrue(e instanceof AxisFault);
        	assertTrue(e.getMessage().indexOf( "java.io.IOException: java.lang.IllegalArgumentException:")> 0);	
        }
    }
    public void testGetHostsWithEscapeSequenceFormfeed() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*\f*");
        HostQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	HostList queryResults = componentLookup.getHosts(querySpec);
            fail("The test should throw an exception as Formfeed is not a valid input" );
        } 
        catch (RemoteException e) {
        	assertTrue(e instanceof AxisFault);
        	assertTrue(e.getMessage().indexOf( "java.io.IOException: java.lang.IllegalArgumentException:")> 0);	
        }
    }
    public void testGetHostsWithEscapeSequenceTab() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*\t*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
            HostList queryResults = componentLookup.getHosts(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
            assertNull("No hosts should be returned",queryResults.getHosts());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an Exception" + e.getLocalizedMessage());
        }
    }
    public void testGetHostsWithEscapeSequenceLinefeed() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*\n*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
            HostList queryResults = componentLookup.getHosts(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
            assertNull("No hosts should be returned",queryResults.getHosts());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an Exception" + e.getLocalizedMessage());
        }
    }
    public void testGetHostsWithEscapeSequenceCarriagereturn() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*\r*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
            HostList queryResults = componentLookup.getHosts(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
            assertNull("No hosts should be returned",queryResults.getHosts());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an Exception" + e.getLocalizedMessage());
        }
    }
    public void testGetHostsWithEscapeSequenceDoublequote() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*\"*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	HostList queryResults = componentLookup.getHosts(querySpec);
        	long end = System.currentTimeMillis();
            getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
            assertNull("No hosts should be returned",queryResults.getHosts());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an Exception" + e.getLocalizedMessage());
        }
    }
    public void testGetHostsWithEscapeSequenceSinglequote() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*\'*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
            HostList queryResults = componentLookup.getHosts(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
            assertNull("No hosts should be returned",queryResults.getHosts());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an Exception" + e.getLocalizedMessage());
        }
    }
    public void testGetHostsWithEscapeSequenceBackslash() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*\\*");
        HostQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
            HostList queryResults = componentLookup.getHosts(querySpec);
            long end = System.currentTimeMillis();
            getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
            assertNull("No hosts should be returned",queryResults.getHosts());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an Exception" + e.getLocalizedMessage());
        }
    }
    public void testGetHostsWithSortSpecDescending() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*");
        HostQueryTerm[] queryTerms = { searchSpecTerm1 };

        HostSortTermList nameDescSortTermList = new HostSortTermList();
        HostSortTerm nameDescSortTerm = new HostSortTerm();
        nameDescSortTerm.setFieldName(HostSortFieldName.name);
        nameDescSortTerm.setDirection(SortDirection.Descending);
        HostSortTerm[] sortTerms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(sortTerms);
        querySpec.setSortSpec(nameDescSortTermList);

        searchSpecTerms.setTerms(queryTerms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 15, size);

        assertEquals(((Host) queryResults.getHosts(0)).getName(), "treasure");
        assertEquals(((Host) queryResults.getHosts(1)).getName(), "tonga2003");
        assertEquals(((Host) queryResults.getHosts(2)).getName(), "tobago");
        assertEquals(((Host) queryResults.getHosts(3)).getName(), "shikoku");
        assertEquals(((Host) queryResults.getHosts(4)).getName(), "penang");
        assertEquals(((Host) queryResults.getHosts(5)).getName(), "maui");
        assertEquals(((Host) queryResults.getHosts(6)).getName(), "cuba");
        assertEquals(((Host) queryResults.getHosts(7)).getName(), "bikini");
        assertEquals(((Host) queryResults.getHosts(8)).getName(), "baixo");
        assertEquals(((Host) queryResults.getHosts(9)).getName(), "azores");
        assertEquals(((Host) queryResults.getHosts(10)).getName(), "angel");
        assertEquals(((Host) queryResults.getHosts(11)).getName(), "TONGA");
        assertEquals(((Host) queryResults.getHosts(12)).getName(), "STBARTS");
        assertEquals(((Host) queryResults.getHosts(13)).getName(), "KADAVU");
        assertEquals(((Host) queryResults.getHosts(14)).getName(), "DX2000-Safdar");
    }

    public void testGetHostsWithSortSpecAscending() throws RemoteException {
        deleteAllHosts();
        insertSampleHosts();
        
        ComponentLookupIF componentLookup = getComponentLookup();
        HostQuerySpec querySpec = new HostQuerySpec();
        HostQueryTermList searchSpecTerms = new HostQueryTermList();
        HostQueryTerm searchSpecTerm1 = new HostQueryTerm();
        searchSpecTerm1.setFieldName(HostQueryFieldName.name);
        searchSpecTerm1.setExpression("*");
        HostQueryTerm[] queryTerms = { searchSpecTerm1 };

        HostSortTermList nameDescSortTermList = new HostSortTermList();
        HostSortTerm nameDescSortTerm = new HostSortTerm();
        nameDescSortTerm.setFieldName(HostSortFieldName.name);
        nameDescSortTerm.setDirection(SortDirection.Ascending);
        HostSortTerm[] sortTerms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(sortTerms);
        querySpec.setSortSpec(nameDescSortTermList);

        searchSpecTerms.setTerms(queryTerms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        HostList queryResults = componentLookup.getHosts(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getHosts());
        int size = queryResults.getHosts().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 15, size);

        assertEquals(((Host) queryResults.getHosts(14)).getName(), "treasure");
        assertEquals(((Host) queryResults.getHosts(13)).getName(), "tonga2003");
        assertEquals(((Host) queryResults.getHosts(12)).getName(), "tobago");
        assertEquals(((Host) queryResults.getHosts(11)).getName(), "shikoku");
        assertEquals(((Host) queryResults.getHosts(10)).getName(), "penang");
        assertEquals(((Host) queryResults.getHosts(9)).getName(), "maui");
        assertEquals(((Host) queryResults.getHosts(8)).getName(), "cuba");
        assertEquals(((Host) queryResults.getHosts(7)).getName(), "bikini");
        assertEquals(((Host) queryResults.getHosts(6)).getName(), "baixo");
        assertEquals(((Host) queryResults.getHosts(5)).getName(), "azores");
        assertEquals(((Host) queryResults.getHosts(4)).getName(), "angel");
        assertEquals(((Host) queryResults.getHosts(3)).getName(), "TONGA");
        assertEquals(((Host) queryResults.getHosts(2)).getName(), "STBARTS");
        assertEquals(((Host) queryResults.getHosts(1)).getName(), "KADAVU");
        assertEquals(((Host) queryResults.getHosts(0)).getName(), "DX2000-Safdar");
    }

}
