/*
 * Created on Apr 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.entityresolver;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
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
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/inquirymgr/entityresolver/UserAndGroupResolverTest.java#2 $
 */

public class UserAndGroupResolverTest extends BaseDestinyTestCase {

    /**
     * This function verifies that the expression is a group entity type and
     * that the name is valid.
     * 
     * @param expression
     *            expression to evaluate
     * @param expectedName
     *            name of the expected entity
     */
    private void checkGroupEntity(final String expression, final String expectedName) {
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
    private void checkUserEntity(final String expression, final String expectedName) {
        IEntityResolver resolver = getResolver();
        assertEquals("User and groups entity resolver should resolve expression properly", EntityExpressionType.ENTITY, resolver.resolve(expression));
        assertEquals("User and groups entity resolver should extract the entity name properly", expectedName, resolver.extractValue(EntityExpressionType.ENTITY, expression));
    }

    /**
     * This function verifies that the expression if a user and usergroup type
     * and that the name is valid
     * 
     * @param expression
     *            expression to evaluate
     * @param expectedName
     *            name of te expression entity
     */
    private void checkUserAndGroupEntity(final String expression, final String expectedName) {
        IEntityResolver resolver = getResolver();
        assertEquals("User and groups entity resolver should resolve expression properly", EntityExpressionType.ENTITY_AND_ENTITY_GROUP, resolver.resolve(expression));
        assertEquals("User and groups entity resolver should extract the entity name properly", expectedName, resolver.extractValue(EntityExpressionType.ENTITY_AND_ENTITY_GROUP, expression));
    }

    /**
     * Returns an instance of the user and user group entity resolver
     * 
     * @return an instance of the user and user group entity resolver
     */
    private UserAndGroupResolverImpl getResolver() {
        ComponentInfo compInfo = new ComponentInfo("userAndGroupsResolver", UserAndGroupResolverImpl.class.getName(), IEntityResolver.class.getName(), LifestyleType.TRANSIENT_TYPE);
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        return (UserAndGroupResolverImpl) compMgr.getComponent(compInfo);
    }

    /**
     * This test verifies the basics of the user and user group resolver.
     */
    public void testUserAndGroupsResolverClassBasics() {
        UserAndGroupResolverImpl resolver = getResolver();
        assertNotNull("The resolver object should exist", resolver);
        assertTrue("User and user group entity resolver should implement the right interfaces", resolver instanceof IEntityResolver);
        assertTrue("User and user group entity resolver should implement the right interfaces", resolver instanceof IUserResolver);
        assertTrue("User and user group entity resolver should implement the right interfaces", resolver instanceof ILogEnabled);
        assertTrue("User and user group entity resolver should implement the right interfaces", resolver instanceof IInitializable);
        assertTrue("User and user group entity resolver should implement the right interfaces", resolver instanceof IConfigurable);
        assertTrue("User and user group entity resolver should implement the right interfaces", resolver instanceof IManagerEnabled);
        assertTrue("User and user group entity resolver should extend the base resolver", resolver instanceof BaseResolverImpl);
        assertNotNull("Patterns should be assigned for users", resolver.getEntityPattern());
        assertNotNull("Patterns should be assigned for user groups", resolver.getEntityGroupPattern());
    }

    /**
     * This test verifies that properly formatted entries are created for a
     * given user or group name.
     */
    public void testUserAndGroupsResolverEntryCreation() {
        UserAndGroupResolverImpl resolver = getResolver();
        final String entityExpr = "Iannis";
        final String groupExpr = "IannisGroup";
        String result = resolver.create(EntityExpressionType.ENTITY, entityExpr);
        assertEquals("The proper expression should be generated for a user entity", UserAndGroupResolverImpl.USER_KEY + entityExpr, result);
        result = resolver.create(EntityExpressionType.ENTITY_GROUP, groupExpr);
        assertEquals("The proper expression should be generated for a group entity", UserAndGroupResolverImpl.USERGROUP_KEY + groupExpr, result);
        result = resolver.create(EntityExpressionType.ENTITY_AND_ENTITY_GROUP, groupExpr);
        assertEquals("The proper expression should be build for mixed type", groupExpr, result);
        result = resolver.create(EntityExpressionType.UNKNOWN, groupExpr);
        assertEquals("The proper expression should be build for unknown type", groupExpr, result);

        final String spacedEntityExpr = "   Iannis   ";
        final String spacedGroupExpr = "IannisGroup   ";
        result = resolver.create(EntityExpressionType.ENTITY, entityExpr);
        assertEquals("The proper expression should be generated for a user entity", UserAndGroupResolverImpl.USER_KEY + spacedEntityExpr.trim(), result);
        result = resolver.create(EntityExpressionType.ENTITY_GROUP, groupExpr);
        assertEquals("The proper expression should be generated for a group entity", UserAndGroupResolverImpl.USERGROUP_KEY + spacedGroupExpr.trim(), result);
        result = resolver.create(EntityExpressionType.ENTITY_AND_ENTITY_GROUP, groupExpr);
        assertEquals("The proper expression should be build for mixed type", spacedGroupExpr.trim(), result);
        result = resolver.create(EntityExpressionType.UNKNOWN, groupExpr);
        assertEquals("The proper expression should be build for unknown type", spacedGroupExpr.trim(), result);
    }

    /**
     * This test verifies that a given expression can be resolved properly.
     */
    public void testUserAndGroupsResolverEntryResolution() {
        //Try with user
        checkUserEntity("(User) Harry S. Truman", "Harry S. Truman");
        checkUserEntity("(User)    Harry S. Truman    ", "Harry S. Truman");
        checkUserEntity("(User) iannis@test.bluejungle.com", "iannis@test.bluejungle.com");
        checkUserEntity("(User)   iannis@test.bluejungle.com   ", "iannis@test.bluejungle.com");
        checkUserEntity("(User)Iannis", "Iannis");
        checkUserEntity("(User)Iannis123", "Iannis123");
        checkUserEntity("(User) Iannis", "Iannis");
        checkUserEntity("(User) Iannis   ", "Iannis");
        checkUserEntity("(User)Iannis   ", "Iannis");
        checkUserEntity("(UsER)Iannis   ", "Iannis");
        checkUserEntity("(USER)Iannis   ", "Iannis");
        checkUserEntity("(user)Iannis   ", "Iannis");

        //Try with groups
        checkGroupEntity("(Group) Group of people", "Group of people");
        checkGroupEntity("(Group)    My !@$% friends...    ", "My !@$% friends...");
        checkGroupEntity("(Group) developers@bluejungle.com", "developers@bluejungle.com");
        checkGroupEntity("(Group)   developers@bluejungle.com   ", "developers@bluejungle.com");
        checkGroupEntity("(Group)Iannis", "Iannis");
        checkGroupEntity("(Group)Iannis123", "Iannis123");
        checkGroupEntity("(Group) Iannis", "Iannis");
        checkGroupEntity("(Group) Iannis   ", "Iannis");
        checkGroupEntity("(Group)Iannis   ", "Iannis");
        checkGroupEntity("(GrOUp)Iannis   ", "Iannis");
        checkGroupEntity("(GROUP)Iannis   ", "Iannis");
        checkGroupEntity("(group)Iannis   ", "Iannis");
    }

    /**
     * This test verifies that ambiguous entries are resolved properly.
     */
    public void testUserAndGroupsResolverAmbiguousResolution() {
        //Try with ambiguous expressions
        IEntityResolver resolver = getResolver();
        checkUserAndGroupEntity(" Harry S. Truman", "Harry S. Truman");
        checkUserAndGroupEntity("    Harry S. Truman    ", "Harry S. Truman");
        checkUserAndGroupEntity(" iannis@test.bluejungle.com", "iannis@test.bluejungle.com");
        checkUserAndGroupEntity("   iannis@test.bluejungle.com   ", "iannis@test.bluejungle.com");
        checkUserAndGroupEntity("Iannis", "Iannis");
        checkUserAndGroupEntity("Iannis123", "Iannis123");
        checkUserAndGroupEntity(" Iannis", "Iannis");
        checkUserAndGroupEntity(" Iannis   ", "Iannis");
        checkUserAndGroupEntity("Iannis   ", "Iannis");
        checkUserAndGroupEntity("(goup)Iannis   ", "(goup)Iannis");
        checkUserAndGroupEntity("(use)Iannis   ", "(use)Iannis");

        //Try with unknown and garbage
        checkUserAndGroupEntity("(*)Iannis", "(*)Iannis");
        checkUserAndGroupEntity("(((*)Iannis", "(((*)Iannis");
        checkUserAndGroupEntity("()Iannis", "()Iannis");
        checkUserAndGroupEntity("(Iannis)", "(Iannis)");
        assertEquals("User and groups entity resolver should resolve expression properly", EntityExpressionType.UNKNOWN, resolver.resolve(null));
        assertEquals("User and groups entity resolver should resolve expression properly", EntityExpressionType.UNKNOWN, resolver.resolve(""));
    }
}