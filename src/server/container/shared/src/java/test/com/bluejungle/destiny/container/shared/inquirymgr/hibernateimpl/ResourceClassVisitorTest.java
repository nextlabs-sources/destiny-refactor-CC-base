/*
 * Created on Apr 20, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.util.HashMap;
import java.util.Map;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;

/**
 * This is the resource class visitor test class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/hibernateimpl/ResourceClassVisitorTest.java#3 $
 */

public class ResourceClassVisitorTest extends DACContainerSharedTestCase {

    /**
     * @see com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.DACContainerSharedTestCase#needLDAPAdapter()
     */
    protected boolean needLDAPAdapter() {
        return false;
    }

    /**
     * This test verifies the basic aspects of the class
     */
    public void testResourceClassVisitorClassBasics() {
        ResourceClassVisitor visitor = new ResourceClassVisitor(null);
        visitor.setVariableName("foo");
        assertTrue("The resource class visitor should implement the correct interface", visitor instanceof DefaultPredicateVisitor);
        assertTrue("The resource class visitor should implement the correct interface", visitor instanceof IPQLVisitor);
        assertTrue("The resource class visitor should implement the correct interface", visitor instanceof IResourceClassVisitor);
        assertEquals("By default, the generated HQL should be empty", "", visitor.getHQLExpression());

        //Test the constructor parameters
        Map myMap = new HashMap();
        visitor = new ResourceClassVisitor(null);
        assertNotNull("If the argument map is null, the resource class visitor should create one", visitor.getArguments());
        visitor = new ResourceClassVisitor(myMap);
        assertEquals("If the argument map is not null, the resource class visitor should use the map provided", myMap, visitor.getArguments());
    }

    /**
     * This test verifies the attribute construction
     */
    public void testResourceClassVisitorAttributeConstruction() {
        ResourceClassVisitor visitor = new ResourceClassVisitor(null);
        visitor.setVariableName("foo");
        assertEquals("foo.bar", visitor.getAttributeExpression("bar"));
    }

    /**
     * This test verifies that wildcards are properly replaced from the parsed
     * expression to the HQL expression.
     */
    public void testResourceClassVisitorWildcardReplacement() {
        LifecycleManager lm = (LifecycleManager) ComponentManagerFactory.getComponentManager().getComponent(LifecycleManager.COMP_INFO);
        final String varName = "foo";
        final String pql = "resource resName = DIRECTORY != \"mydir\"";
        ResourceClassVisitor visitor = new ResourceClassVisitor(null);
        visitor.setVariableName(varName);
        try {
            DomainObjectBuilder.processInternalPQL(pql, visitor);
            String expr = visitor.getHQLExpression();
            final String expectedExpr = "(lower(foo.name) NOT LIKE :resource0)";
            assertNotNull("Generated HQL cannot be null", expr);
            assertEquals(expectedExpr, expr.trim());
            Map args = visitor.getArguments();
            assertNotNull("Map should not be null ", args);
            assertEquals("Directory should have a %", "file:/%/mydir/%", args.get("resource0"));
            visitor = new ResourceClassVisitor(null);
            visitor.setVariableName(varName);
            final String pqlWithDoubleStar = "resource resName = NAME != \"**mydir*.java\"";
            DomainObjectBuilder.processInternalPQL(pqlWithDoubleStar, visitor);
            expr = visitor.getHQLExpression();
            assertNotNull("Generated HQL cannot be null", expr);
            final String finalExpectedDoubleStarHQL = "(lower(foo.name) NOT LIKE :resource0)";
            args = visitor.getArguments();
            assertNotNull("Map should not be null ", args);
            assertEquals("** and * in PQL should be handled properly in HQL", "file:/%mydir%.java", args.get("resource0"));
            assertEquals(finalExpectedDoubleStarHQL, expr.trim());
        } catch (PQLException e) {
            fail("No exception should be thrown during parsing");
        }
    }

    /**
     * This test verifies that the HQL expression are properly chosen when
     * operators deal with "Long" types.
     */
    public void testResourceClassVisitorOperatorsForLong() {
        try {
            String pql = "resource resName = SIZE != 50";
            ResourceClassVisitor visitor = new ResourceClassVisitor(null);
            visitor.setVariableName("foo");
            DomainObjectBuilder.processInternalPQL(pql, visitor);
            String expr = visitor.getHQLExpression();
            assertEquals("NotEqual with long should be handled", "(foo.size != 50)", visitor.getHQLExpression());

            pql = "resource resName = SIZE >= 50";
            visitor = new ResourceClassVisitor(null);
            visitor.setVariableName("foo");
            DomainObjectBuilder.processInternalPQL(pql, visitor);
            expr = visitor.getHQLExpression();
            assertEquals("'Greater than' with long should be handled", "(foo.size >= 50)", visitor.getHQLExpression());

            pql = "resource resName = SIZE <= 50";
            visitor = new ResourceClassVisitor(null);
            visitor.setVariableName("foo");
            DomainObjectBuilder.processInternalPQL(pql, visitor);
            expr = visitor.getHQLExpression();
            assertEquals("'Lower than' with long should be handled", "(foo.size <= 50)", visitor.getHQLExpression());

            pql = "resource resName = SIZE = 50";
            visitor = new ResourceClassVisitor(null);
            visitor.setVariableName("foo");
            DomainObjectBuilder.processInternalPQL(pql, visitor);
            expr = visitor.getHQLExpression();
            assertEquals("'equals' with long should be handled", "(foo.size = 50)", visitor.getHQLExpression());
        } catch (PQLException e) {
            fail("No exception should be thrown during parsing");
        }
    }

    /**
     * This test verifies that the resource visitor can handle an empty resource
     * class.
     */
    public void testResourceClassVisitorForEmptyResourceClass() {
        LifecycleManager lm = (LifecycleManager) ComponentManagerFactory.getComponentManager().getComponent(LifecycleManager.COMP_INFO);
        final String pql = "resource EmptyOne = DESCRIPTION \"It is empty\" (((FALSE OR TRUE) AND TRUE) AND (TRUE AND TRUE))";
        ResourceClassVisitor visitor = new ResourceClassVisitor(null);
        visitor.setVariableName("foo");
        try {
            DomainObjectBuilder.processInternalPQL(pql, visitor);
            final String expr = visitor.getHQLExpression();
            final String expectedResult = "(((( 1=0 OR  1=1) AND  1=1) AND ( 1=1 AND  1=1)))";
            assertEquals("Empty resource class names should be handled", expectedResult, expr);
        } catch (PQLException e) {
            fail("No exception should be thrown during parsing");
        }
    }

    /**
     * This test verifies that the addEntityToVisit flag is working properly
     */
    public void testResourceClassVisitorAddNewEntity() {
        ResourceClassVisitor visitor = new ResourceClassVisitor(null);
        visitor.setVariableName("foo");
        visitor.addNewEntityToVisit(true);
        assertEquals("The first addition should not modify the generated HQL", "", visitor.getHQLExpression());
        visitor.addNewEntityToVisit(false);
        assertEquals("Subsequent addition should add the correct element", " OR ", visitor.getHQLExpression());
    }

    /**
     * This test verifies that the resource expression, if it has wildcard, is
     * processed properly to a valid HQL expression
     */
    public void testResourceClassVisitorWildcardProcessing() {
        ResourceClassVisitor visitor = new ResourceClassVisitor(null);
        visitor.setVariableName("foo");
        assertEquals("Wildcard should be processed properly", "", visitor.processWildcards(""));
        assertEquals("Wildcard should be processed properly", "abc", visitor.processWildcards("abc"));
        assertEquals("Wildcard should be processed properly", "abc_abc", visitor.processWildcards("abc?abc"));
        assertEquals("Wildcard should be processed properly", "abc%abc", visitor.processWildcards("abc*abc"));
        assertEquals("Wildcard should be processed properly", "abc%abc", visitor.processWildcards("abc**abc"));
        assertEquals("Wildcard should be processed properly", "abc%_%abc", visitor.processWildcards("abc*?*abc"));
    }
}
