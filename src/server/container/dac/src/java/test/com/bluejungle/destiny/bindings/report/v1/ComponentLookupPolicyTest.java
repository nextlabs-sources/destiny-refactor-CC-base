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
import com.bluejungle.destiny.types.policies.v1.Policy;
import com.bluejungle.destiny.types.policies.v1.PolicyList;
import com.bluejungle.destiny.types.policies.v1.PolicyQueryFieldName;
import com.bluejungle.destiny.types.policies.v1.PolicyQuerySpec;
import com.bluejungle.destiny.types.policies.v1.PolicyQueryTerm;
import com.bluejungle.destiny.types.policies.v1.PolicyQueryTermList;
import com.bluejungle.destiny.types.policies.v1.PolicySortFieldName;
import com.bluejungle.destiny.types.policies.v1.PolicySortTerm;
import com.bluejungle.destiny.types.policies.v1.PolicySortTermList;

/**
 * This is the test class for the policy API of the component loookup service.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/test/com/bluejungle/destiny/bindings/report/v1/ComponentLookupPolicyTest.java#1 $
 */

public class ComponentLookupPolicyTest extends ComponentLookupIFImplTest {

    /**
     * Constructor
     * 
     * @param testName
     *            name of the test
     */
    public ComponentLookupPolicyTest(String testName) {
        super(testName);
    }
        
    /**
     * This test verifies that if the API returns no records, the data structure
     * returned is still correct.
     */
    public void testGetPoliciesNoRecords() throws RemoteException {
        final int nbPolicies = 10;
        deleteAllPolicies();
        ComponentLookupIF componentLookup = getComponentLookup();
        long start = System.currentTimeMillis();
        PolicyList noPolicyResults = componentLookup.getPolicies(null);
        long end = System.currentTimeMillis();
        getLog().info("GetPolicies with no result timing: " + (end - start) + " ms");
        assertNotNull("Result should be returned even if no policies have been found", noPolicyResults);
        assertNull("Result should be returned even if no policies have been found", noPolicyResults.getPolicies());
    }

    /**
     * This test verifies that policies can be fetched with a search spec on the
     * policy name
     */
    public void testGetPoliciesWithQuerySpec() throws RemoteException {
        final int nbPolicies = 10;
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        //Query for policies with a search spec
        PolicyQuerySpec only3QuerySpec = new PolicyQuerySpec();
        PolicyQueryTermList only3QueryTermList = new PolicyQueryTermList();
        PolicyQueryTerm likePolicy3Term = new PolicyQueryTerm();
        likePolicy3Term.setFieldName(PolicyQueryFieldName.Name);
        likePolicy3Term.setExpression("Policy_3*");
        PolicyQueryTerm[] terms = { likePolicy3Term };
        only3QueryTermList.setTerms(terms);
        only3QuerySpec.setSearchSpec(only3QueryTermList);
        long start = System.currentTimeMillis();
        PolicyList likePolicy3Results = componentLookup.getPolicies(only3QuerySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetPolicies with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", likePolicy3Results.getPolicies());
        int size = likePolicy3Results.getPolicies().length;
        assertEquals("The search spec should return one record", 1, size);
        Policy policy = likePolicy3Results.getPolicies(0);
        assertEquals("The policy name should match", "Policy_3", policy.getName());
    }

    /**
     * This test verifies that policies can be fetched with a search spec on the
     * policy name
     */
    public void testGetPoliciesWithSortSpec() throws RemoteException {
        final int nbPolicies = 20;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        //Query for policies with a search spec
        PolicyQuerySpec nameDescQuerySpec = new PolicyQuerySpec();
        PolicySortTermList nameDescSortTermList = new PolicySortTermList();
        PolicySortTerm nameDescSortTerm = new PolicySortTerm();
        nameDescSortTerm.setFieldName(PolicySortFieldName.Name);
        nameDescSortTerm.setDirection(SortDirection.Descending);
        PolicySortTerm[] terms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(terms);
        nameDescQuerySpec.setSortSpec(nameDescSortTermList);
        long start = System.currentTimeMillis();
        PolicyList sortedPolicyResults = componentLookup.getPolicies(nameDescQuerySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetPolicies with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", sortedPolicyResults.getPolicies());
        int size = sortedPolicyResults.getPolicies().length;
        assertEquals("The search spec should return all records", nbPolicies, size);
        String lastRecordName = sortedPolicyResults.getPolicies()[0].getName();
        for (int index = 0; index < size; index++) {
            String currentName = sortedPolicyResults.getPolicies()[index].getName();
            int result = currentName.compareTo(lastRecordName);
            assertTrue("Results should be sorted by name descending", result <= 0);
            lastRecordName = currentName;
        }
    }

    public void testGetPoliciesAll() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        //Query for hosts with a search spec on name *3*
        PolicyQuerySpec likeStar3StarQuerySpec = new PolicyQuerySpec();
        PolicyQueryTermList likeStar3StarQueryTermList = new PolicyQueryTermList();
        PolicyQueryTerm likeStar3StarTerm = new PolicyQueryTerm();
        likeStar3StarTerm.setFieldName(PolicyQueryFieldName.Name);
        likeStar3StarTerm.setExpression("*");
        PolicyQueryTerm[] terms = { likeStar3StarTerm };
        likeStar3StarQueryTermList.setTerms(terms);
        likeStar3StarQuerySpec.setSearchSpec(likeStar3StarQueryTermList);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(likeStar3StarQuerySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetPoliciesRandomExpressions() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        //Query for hosts with a search spec on name *3*
        PolicyQuerySpec likeStar3StarQuerySpec = new PolicyQuerySpec();
        PolicyQueryTermList likeStar3StarQueryTermList = new PolicyQueryTermList();
        PolicyQueryTerm likeStar3StarTerm = new PolicyQueryTerm();
        likeStar3StarTerm.setFieldName(PolicyQueryFieldName.Name);
        likeStar3StarTerm.setExpression(RANDOM_SEARCH_EXPR_ONE);
        PolicyQueryTerm[] terms = { likeStar3StarTerm };
        likeStar3StarQueryTermList.setTerms(terms);
        likeStar3StarQuerySpec.setSearchSpec(likeStar3StarQueryTermList);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(likeStar3StarQuerySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getPolicies());
    }

    /**
     * This test verifies getHostClasses() with null input
     *  
     */
    public void testGetPoliciesWithNullQuerySpec() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        componentLookup.getPolicies(null);
    }

    /**
     * This test verifies getHostClasses() with null searchSpec and null
     * sortSpec in HostClassQuerySpec.
     */
    public void testGetPoliciesWithNullSearchSpecAndNullSortSpec() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        querySpec.setSearchSpec(null);
        querySpec.setSortSpec(null);
        componentLookup.getPolicies(querySpec);
    }

    /**
     * This test verifies getHostClasses() with null searchSpec in
     * HostClassQuerySpec.
     */
    public void testGetPoliciesWithNullSearchSpec() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        querySpec.setSearchSpec(null);
        PolicySortTermList nameDescSortTermList = new PolicySortTermList();
        PolicySortTerm nameDescSortTerm = new PolicySortTerm();
        nameDescSortTerm.setFieldName(PolicySortFieldName.Name);
        nameDescSortTerm.setDirection(SortDirection.Descending);
        PolicySortTerm[] terms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(terms);
        querySpec.setSortSpec(nameDescSortTermList);
        componentLookup.getPolicies(querySpec);
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetPoliciesWithNumber() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*1");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        assertEquals("The search spec should return the correct number of records", 3, size);
    }
    
    /**
     * This test verifies getHostClasses() with null sortSpec in
     * HostClassQuerySpec.
     */
    public void testGetPoliciesWithNullSortSpec() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec nameLike3QuerySpec = new PolicyQuerySpec();
        PolicyQueryTermList like3QueryTermList = new PolicyQueryTermList();
        PolicyQueryTerm like3Term = new PolicyQueryTerm();
        like3Term.setFieldName(PolicyQueryFieldName.Name);
        like3Term.setExpression("*3");
        PolicyQueryTerm[] terms = { like3Term };
        like3QueryTermList.setTerms(terms);
        nameLike3QuerySpec.setSearchSpec(like3QueryTermList);
        nameLike3QuerySpec.setSortSpec(null);
        componentLookup.getPolicies(nameLike3QuerySpec);
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetPoliciesWithFiveRandomSearchSpec() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression(RANDOM_SEARCH_EXPR_ONE);
        PolicyQueryTerm searchSpecTerm2 = new PolicyQueryTerm();
        searchSpecTerm2.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        PolicyQueryTerm searchSpecTerm3 = new PolicyQueryTerm();
        searchSpecTerm3.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm3.setExpression(RANDOM_SEARCH_EXPR_THREE);
        PolicyQueryTerm searchSpecTerm4 = new PolicyQueryTerm();
        searchSpecTerm4.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        PolicyQueryTerm searchSpecTerm5 = new PolicyQueryTerm();
        searchSpecTerm5.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm5.setExpression(RANDOM_SEARCH_EXPR_FIVE);
        PolicyQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getPolicies());
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */
    public void testGetPoliciesWithRandomSearchSpecAndRandomNullSearchExpression() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression(null);
        PolicyQueryTerm searchSpecTerm2 = new PolicyQueryTerm();
        searchSpecTerm2.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        PolicyQueryTerm searchSpecTerm3 = new PolicyQueryTerm();
        searchSpecTerm3.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm3.setExpression(null);
        PolicyQueryTerm searchSpecTerm4 = new PolicyQueryTerm();
        searchSpecTerm4.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        PolicyQueryTerm searchSpecTerm5 = new PolicyQueryTerm();
        searchSpecTerm5.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm5.setExpression(null);
        PolicyQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try {
            PolicyList queryResults = componentLookup.getPolicies(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'expression' is null.") > 0);
        }
    }

    /**
     * This test verifies that users can be fetched with a search spec on the
     * firstname or last name
     */

    public void testGetPoliciesWithRandomSearchSpecAndRandomNullSearchFieldName() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(null);
        searchSpecTerm1.setExpression(RANDOM_SEARCH_EXPR_ONE);
        PolicyQueryTerm searchSpecTerm2 = new PolicyQueryTerm();
        searchSpecTerm2.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm2.setExpression(RANDOM_SEARCH_EXPR_TWO);
        PolicyQueryTerm searchSpecTerm3 = new PolicyQueryTerm();
        searchSpecTerm3.setFieldName(null);
        searchSpecTerm3.setExpression(RANDOM_SEARCH_EXPR_THREE);
        PolicyQueryTerm searchSpecTerm4 = new PolicyQueryTerm();
        searchSpecTerm4.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm4.setExpression(RANDOM_SEARCH_EXPR_FOUR);
        PolicyQueryTerm searchSpecTerm5 = new PolicyQueryTerm();
        searchSpecTerm5.setFieldName(null);
        searchSpecTerm5.setExpression(RANDOM_SEARCH_EXPR_FIVE);
        PolicyQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try {
            PolicyList queryResults = componentLookup.getPolicies(querySpec);
        } catch (RemoteException e) {
            assertTrue(e instanceof AxisFault);
            assertTrue(e.getMessage().indexOf("Non nillable element 'fieldName' is null.") > 0);
        }
    }

    public void testGetAllPoliciesByStar() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllPoliciesByPrefix() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("Policy_*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllPoliciesByFirstLetter() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*_*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllPoliciesBySuffix() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("***");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllPoliciesByRegularExpression() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("Policy_**");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetAllPoliciesByExclude() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("?*_?*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        assertEquals("The search spec should return the correct number of records", 30, size);
    }

    public void testGetPoliciesSingleDigits() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm2 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm3 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm4 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm5 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm6 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm7 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm8 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm9 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("Policy_1");
        searchSpecTerm2.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm2.setExpression("Policy_2");
        searchSpecTerm3.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm3.setExpression("Policy_3");
        searchSpecTerm4.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm4.setExpression("Policy_4");
        searchSpecTerm5.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm5.setExpression("Policy_5");
        searchSpecTerm6.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm6.setExpression("Policy_6");
        searchSpecTerm7.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm7.setExpression("Policy_7");
        searchSpecTerm8.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm8.setExpression("Policy_8");
        searchSpecTerm9.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm9.setExpression("Policy_9");
        PolicyQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5, searchSpecTerm6, searchSpecTerm7, searchSpecTerm8, searchSpecTerm9 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be policies returned", queryResults.getPolicies());
    }

    public void testGetPoliciesSingleDigitsOne() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm2 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm3 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm4 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm5 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm6 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm7 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm8 = new PolicyQueryTerm();
        PolicyQueryTerm searchSpecTerm9 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("Policy_1");
        searchSpecTerm2.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm2.setExpression("Policy_1");
        searchSpecTerm3.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm3.setExpression("Policy_1");
        searchSpecTerm4.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm4.setExpression("Policy_1");
        searchSpecTerm5.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm5.setExpression("Policy_1");
        searchSpecTerm6.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm6.setExpression("Policy_1");
        searchSpecTerm7.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm7.setExpression("Policy_1");
        searchSpecTerm8.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm8.setExpression("Policy_1");
        searchSpecTerm9.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm9.setExpression("Policy_1");
        PolicyQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3, searchSpecTerm4, searchSpecTerm5, searchSpecTerm6, searchSpecTerm7, searchSpecTerm8, searchSpecTerm9 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetPoliciesWithOne() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*_*1*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 12, size);
    }

    public void testGetPoliciesWithFive() throws RemoteException {
        final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*?5");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        assertEquals("The search spec should return the correct number of records", 3, size);
    }

    public void testGetPoliciesWithExactName() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("Backup");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetPoliciesWithRandomWildCards() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*Z*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getPolicies());
    }

    public void testGetPoliciesWithRandomWildCardsAndLetters() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("B?*k*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }

    public void testGetPoliciesWithStartingLetter() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("O*");
        PolicyQueryTerm searchSpecTerm2 = new PolicyQueryTerm();
        searchSpecTerm2.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm2.setExpression("P*");
        PolicyQueryTerm searchSpecTerm3 = new PolicyQueryTerm();
        searchSpecTerm3.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm3.setExpression("Q*");
        PolicyQueryTerm[] terms = { searchSpecTerm1, searchSpecTerm2, searchSpecTerm3 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be policies returned", queryResults.getPolicies());
    }

    public void testGetPoliciesWithRandomStringOnUniqueRecords() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("[abcdefghijklmnopqrstuvwxyz_+(*&^)%$#@!<> 1234567890]*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getPolicies());
    }

    public void testGetPoliciesWithSpace() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("* *");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        assertEquals("The search spec should return the correct number of records", 5, size);
    }

    public void testGetPoliciesWithWildCardsAndLettersAndNumbers() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*z0?*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNull("There should not be hosts returned", queryResults.getPolicies());
    }

    public void testGetPoliciesWithRepeatingLettersAndWildCards() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*e*e*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };

        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be hosts returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 4, size);
    }

    public void testGetPoliciesWithCaseInsensitiveString() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("sTANDARd");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        assertEquals("The search spec should return the correct number of records", 1, size);
    }
    
    /**
     * The following test verifies whether IllegalArgumentException is thrown when backspace is passed as input to policy criteria
     */
    public void testGetPoliciesWithEscapeSequenceBackspace() throws RemoteException {
    	final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*\b*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	PolicyList queryResults = componentLookup.getPolicies(querySpec);
            fail("The test should throw an exception as backspace is not a valid input" );
        } 
        catch (RemoteException e) {
        	assertTrue(e instanceof AxisFault);
        	assertTrue(e.getMessage().indexOf( "java.io.IOException: java.lang.IllegalArgumentException:")> 0);	
        }
    }
    
    public void testGetPoliciesWithEscapeSequenceFormfeed() throws RemoteException {
    	final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*\f*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	PolicyList queryResults = componentLookup.getPolicies(querySpec);
            fail("The test should throw an exception as form feed is not a valid input" );
        } 
        catch (RemoteException e) {
        	assertTrue(e instanceof AxisFault);
        	assertTrue(e.getMessage().indexOf( "java.io.IOException: java.lang.IllegalArgumentException:")> 0);	
        }
    }
    
    public void testGetPoliciesWithEscapeSequenceTab() throws RemoteException {
    	final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*\t*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	PolicyList queryResults = componentLookup.getPolicies(querySpec);
        	long end = System.currentTimeMillis();
            getLog().info("GetPolicies with search spec timing: " + (end - start) + " ms");
        	assertNull("No policies should be returned", queryResults.getPolicies());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage() );
        }
    }
    
    public void testGetPoliciesWithEscapeSequenceLinefeed() throws RemoteException {
    	final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*\n*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	PolicyList queryResults = componentLookup.getPolicies(querySpec);
        	long end = System.currentTimeMillis();
            getLog().info("GetPolicies with search spec timing: " + (end - start) + " ms");
        	assertNull("No policies should be returned", queryResults.getPolicies());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage() );
        }
    }
    
    public void testGetPoliciesWithEscapeSequenceCarriagereturn() throws RemoteException {
    	final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*\r*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	PolicyList queryResults = componentLookup.getPolicies(querySpec);
        	long end = System.currentTimeMillis();
            getLog().info("GetPolicies with search spec timing: " + (end - start) + " ms");
        	assertNull("No policies should be returned", queryResults.getPolicies());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage() );
        }
    }
    
    public void testGetPoliciesWithEscapeSequenceDoublequote() throws RemoteException {
    	final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*\"*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	PolicyList queryResults = componentLookup.getPolicies(querySpec);
        	long end = System.currentTimeMillis();
            getLog().info("GetPolicies with search spec timing: " + (end - start) + " ms");
        	assertNull("No policies should be returned", queryResults.getPolicies());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage() );
        }
    }
    
    public void testGetPoliciesWithEscapeSequenceSinglequote() throws RemoteException {
    	final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*\'*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	PolicyList queryResults = componentLookup.getPolicies(querySpec);
        	long end = System.currentTimeMillis();
            getLog().info("GetPolicies with search spec timing: " + (end - start) + " ms");
        	assertNull("No policies should be returned", queryResults.getPolicies());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage() );
        }
    }
    
    public void testGetPoliciesWithEscapeSequenceBackslash() throws RemoteException {
    	final int nbPolicies = 30;
        deleteAllPolicies();
        insertPolicies(new Long(0), nbPolicies);
        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*\\*");
        PolicyQueryTerm[] terms = { searchSpecTerm1 };
        searchSpecTerms.setTerms(terms);
        querySpec.setSearchSpec(searchSpecTerms);
        try{
        	long start = System.currentTimeMillis();
        	PolicyList queryResults = componentLookup.getPolicies(querySpec);
        	long end = System.currentTimeMillis();
            getLog().info("GetPolicies with search spec timing: " + (end - start) + " ms");
        	assertNull("No policies should be returned", queryResults.getPolicies());
        } 
        catch (RemoteException e) {
        	fail("The test should not throw an exception" + e.getLocalizedMessage() );
        }
    }
    
    public void testGetPoliciesWithSortSpecDescending() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*");
        PolicyQueryTerm[] queryTerms = { searchSpecTerm1 };

        PolicySortTermList nameDescSortTermList = new PolicySortTermList();
        PolicySortTerm nameDescSortTerm = new PolicySortTerm();
        nameDescSortTerm.setFieldName(PolicySortFieldName.Name);
        nameDescSortTerm.setDirection(SortDirection.Descending);
        PolicySortTerm[] sortTerms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(sortTerms);
        querySpec.setSortSpec(nameDescSortTermList);

        searchSpecTerms.setTerms(queryTerms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 15, size);
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(14)).getName().equals("Backup"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(13)).getName().equals("Contractors Policy"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(12)).getName().equals("Copy"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(11)).getName().equals("Delete"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(10)).getName().equals("Email"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(9)).getName().equals("File Server"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(8)).getName().equals("HR Policy"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(7)).getName().equals("Instant Messenger"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(6)).getName().equals("Laptops"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(5)).getName().equals("Move"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(4)).getName().equals("Open"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(3)).getName().equals("Print"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(2)).getName().equals("Removable Storage"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(1)).getName().equals("Security"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(0)).getName().equals("Standard"));

    }

    public void testGetPoliciesWithSortSpecAscending() throws RemoteException {
        deleteAllPolicies();
        insertSamplePolicies();

        ComponentLookupIF componentLookup = getComponentLookup();
        PolicyQuerySpec querySpec = new PolicyQuerySpec();
        PolicyQueryTermList searchSpecTerms = new PolicyQueryTermList();
        PolicyQueryTerm searchSpecTerm1 = new PolicyQueryTerm();
        searchSpecTerm1.setFieldName(PolicyQueryFieldName.Name);
        searchSpecTerm1.setExpression("*");
        PolicyQueryTerm[] queryTerms = { searchSpecTerm1 };

        PolicySortTermList nameDescSortTermList = new PolicySortTermList();
        PolicySortTerm nameDescSortTerm = new PolicySortTerm();
        nameDescSortTerm.setFieldName(PolicySortFieldName.Name);
        nameDescSortTerm.setDirection(SortDirection.Ascending);
        PolicySortTerm[] sortTerms = { nameDescSortTerm };
        nameDescSortTermList.setTerms(sortTerms);
        querySpec.setSortSpec(nameDescSortTermList);

        searchSpecTerms.setTerms(queryTerms);
        querySpec.setSearchSpec(searchSpecTerms);
        long start = System.currentTimeMillis();
        PolicyList queryResults = componentLookup.getPolicies(querySpec);
        long end = System.currentTimeMillis();
        getLog().info("GetHosts with search spec timing: " + (end - start) + " ms");
        assertNotNull("There should be policies returned", queryResults.getPolicies());
        int size = queryResults.getPolicies().length;
        //We should have three matching rows
        assertEquals("The search spec should return the correct number of records", 15, size);

        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(0)).getName().equals("Backup"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(1)).getName().equals("Contractors Policy"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(2)).getName().equals("Copy"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(3)).getName().equals("Delete"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(4)).getName().equals("Email"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(5)).getName().equals("File Server"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(6)).getName().equals("HR Policy"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(7)).getName().equals("Instant Messenger"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(8)).getName().equals("Laptops"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(9)).getName().equals("Move"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(10)).getName().equals("Open"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(11)).getName().equals("Print"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(12)).getName().equals("Removable Storage"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(13)).getName().equals("Security"));
        assertTrue("Return records should match the sort spec", ((Policy) queryResults.getPolicies(14)).getName().equals("Standard"));
    }
}
