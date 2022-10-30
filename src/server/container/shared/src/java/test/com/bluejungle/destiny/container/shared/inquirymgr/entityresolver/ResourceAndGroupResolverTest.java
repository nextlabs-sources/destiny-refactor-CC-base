/*
 * Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.entityresolver;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.test.BaseDestinyTestCase;

/**
 * This is the test class for the user and user groups resolver.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/entityresolver/ResourceAndGroupResolverTest.java#1 $
 */

public class ResourceAndGroupResolverTest extends BaseDestinyTestCase {

    /**
     * This function verifies that the expression is a group entity type and
     * that the name is valid.
     * 
     * @param expression
     *            expression to evaluate
     * @param expectedName
     *            name of the expected entity
     */
    private void checkGroupEntity(String expression, String expectedName) {
        IEntityResolver resolver = getResolver();
        assertEquals("User and groups entity resolver should resolve expression properly", EntityExpressionType.ENTITY_GROUP, resolver.resolve(expression));
        assertEquals("User and groups entity resolver should extract the entity name properly", expectedName, resolver.extractValue(EntityExpressionType.ENTITY_GROUP, expression));
    }

    /**
     * This function verifies that the expression is an user entity type and
     * that the name is valid.
     * 
     * @param expression
     *            expression to evaluate
     * @param expectedName
     *            name of the expected entity
     */
    private void checkResourceEntity(String expression, String expectedName) {
        IEntityResolver resolver = getResolver();
        assertEquals("Resource and groups entity resolver should resolve expression properly", EntityExpressionType.ENTITY, resolver.resolve(expression));
        assertEquals("Resource and groups entity resolver should extract the entity name properly", expectedName, resolver.extractValue(EntityExpressionType.ENTITY, expression));
    }

    /**
     * Returns an instance of the resource and user group entity resolver
     * 
     * @return an instance of the resource and user group entity resolver
     */
    private ResourceAndGroupResolverImpl getResolver() {
        HashMapConfiguration config = new HashMapConfiguration();
        ComponentInfo compInfo = new ComponentInfo("resourceAndGroupsResolver", ResourceAndGroupResolverImpl.class.getName(), IEntityResolver.class.getName(), LifestyleType.TRANSIENT_TYPE, config);
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        return (ResourceAndGroupResolverImpl) compMgr.getComponent(compInfo);
    }

    /**
     * This test verifies the basics of the resource and resource group
     * resolver.
     */
    public void testResourceAndGroupsResolverClassBasics() {
        ResourceAndGroupResolverImpl resolver = getResolver();
        assertNotNull(resolver);
        assertTrue("Resource and resource group entity resolver should implement the right interfaces", resolver instanceof IEntityResolver);
        assertTrue("Resource and resource group entity resolver should implement the right interfaces", resolver instanceof ILogEnabled);
        assertTrue("Resource and resource group entity resolver should implement the right interfaces", resolver instanceof IInitializable);
        assertTrue("Resource and resource group entity resolver should implement the right interfaces", resolver instanceof IConfigurable);
        assertTrue("Resource and resource group entity resolver should implement the right interfaces", resolver instanceof IManagerEnabled);
        assertTrue("Resource and resource group entity resolver should extend the base resolver", resolver instanceof BaseResolverImpl);
        assertNull("Patterns should not be assigned for resources", resolver.getEntityPattern());
        assertNull("Patterns should not be assigned for resource groups", resolver.getEntityGroupPattern());
    }

    /**
     * This test verifies that properly formatted entries are created for a
     * given resource or group name.
     */
    public void testResourceAndGroupsResolverEntryCreation() {
        ResourceAndGroupResolverImpl resolver = getResolver();
        final String entityExpr = "   \\\\STBARTS\\c$\\c:\\test\\text.txt   ";
        final String groupExpr = "textFiles";
        String result = resolver.create(EntityExpressionType.ENTITY, entityExpr);
        assertEquals("The proper expression should be generated for a resource entity", entityExpr.trim(), result);
        result = resolver.create(EntityExpressionType.ENTITY_GROUP, groupExpr);
        assertEquals("The proper expression should be generated for a group entity", groupExpr.trim(), result);
        result = resolver.create(EntityExpressionType.ENTITY_AND_ENTITY_GROUP, groupExpr);
        assertEquals("No expression should be build for mixed type", groupExpr.trim(), result);
    }

    /**
     * This test verifies that a given expression can be resolved properly.
     */
    public void testResourceAndGroupsResolverEntryResolution() {
        //Try with remote resource
        String resName = "\\\\STBARTS\\c$\\test\\test.txt";
        checkResourceEntity("  " + resName, resName);
        checkResourceEntity(" " + resName + " ", resName);
        checkResourceEntity(resName, resName);

        //Try with local resource
        resName = "c:\\myFolder\\test.txt";
        checkResourceEntity("  " + resName, resName);
        checkResourceEntity(" " + resName + " ", resName);
        checkResourceEntity(resName, resName);

        //Try with local resource
        resName = "Z:\\MYREP\\myFIle.txt";
        checkResourceEntity("  " + resName, resName);
        checkResourceEntity(" " + resName + " ", resName);
        checkResourceEntity(resName, resName);

        //Try with remote resource
        resName = "\\myhost\\*test.txt";
        checkResourceEntity("  " + resName, resName);
        checkResourceEntity(" " + resName + " ", resName);
        checkResourceEntity(resName, resName);

        //Try with remote resource
        resName = "\\myhost\\te?t.txt";
        checkResourceEntity("  " + resName, resName);
        checkResourceEntity(" " + resName + " ", resName);
        checkResourceEntity(resName, resName);
        
        //Try with Sharepoint resource name
        resName = "SharePoint://sharepoint2007.bluejungle.com/ReporterSite";
        checkResourceEntity("  " + resName, resName);
        checkResourceEntity(" " + resName + " ", resName);
        checkResourceEntity(resName, resName);

        //Try with groups
        //TODO: we temporarily took out resource group based
        //      queries, need to add this back when we bring
        //      resource group based queries back
//        checkGroupEntity("resGroupName", "resGroupName");
//        checkGroupEntity(" resGroupName ", "resGroupName");
//        checkGroupEntity("   resGroupName   ", "resGroupName");
//        checkGroupEntity("   resGroup-Name   ", "resGroup-Name");

        //Try with unknown and garbage
        IEntityResolver resolver = getResolver();
        assertEquals("Resource and groups entity resolver should resolve expression properly", EntityExpressionType.ENTITY, resolver.resolve("(*)Iannis"));
        assertEquals("Resource and groups entity resolver should resolve expression properly", EntityExpressionType.ENTITY, resolver.resolve("(((*)Iannis"));
//        assertEquals("Resource and groups entity resolver should resolve expression properly", EntityExpressionType.ENTITY_GROUP, resolver.resolve("Iannis"));
//        assertEquals("Resource and groups entity resolver should resolve expression properly", EntityExpressionType.ENTITY_GROUP, resolver.resolve("()Iannis"));
//        assertEquals("Resource and groups entity resolver should resolve expression properly", EntityExpressionType.UNKNOWN, resolver.resolve(null));
//        assertEquals("Resource and groups entity resolver should resolve expression properly", EntityExpressionType.UNKNOWN, resolver.resolve(""));
//        assertEquals("Resource and groups entity resolver should resolve expression properly", EntityExpressionType.ENTITY_GROUP, resolver.resolve("(Iannis)"));
    }
}