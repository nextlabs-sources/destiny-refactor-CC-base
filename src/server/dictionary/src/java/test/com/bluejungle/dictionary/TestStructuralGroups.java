package com.bluejungle.dictionary;

/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestStructuralGroups.java#1 $
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import junit.framework.TestSuite;

import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.PredicateConstants;

public class TestStructuralGroups extends AbstractDictionaryTest {

    private static Date creationTime;

    private static Date preMoveTime;

    private static Long userIdToQuery;

    private static Long groupIdToQuery;

    public static TestSuite suite() {
        return new TestSuite(TestStructuralGroups.class);
    }

    public void testSetupDictionary() throws Exception {
        init();
    }

    public void testSeedInitialData() throws DictionaryException {
        List<IElementBase> toSave = new ArrayList<IElementBase>();
        seedRecursive(0, new byte[4], toSave);
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveElements(toSave);
            session.commit();
        } catch ( DictionaryException dex ) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(true, null);
        }
        creationTime = new Date();
    }

    public void testSeedDataWasSuccessful() throws DictionaryException {
        IDictionaryIterator<IMElement> ei = dictionary.query(
            PredicateConstants.TRUE, creationTime, null, null
        );
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(315, counts[0]);
        assertEquals(168, counts[2]);
        IDictionaryIterator<IMGroup> gi = dictionary.getStructuralGroups(
            DictionaryPath.ROOT
        ,   "%"
        ,   null
        ,   null
        ,   null
        );
        assertEquals(210, countGroups(gi, StructuralGroup.class));
    }

    public void testQueryGroupByContainedElementType() throws DictionaryException {
        IDictionaryIterator<IMGroup> gi = dictionary.getStructuralGroups(
            DictionaryPath.ROOT
        ,   "%"
        ,   userStruct.type
        ,   null
        ,   null
        );
        assertEquals(105, countGroups(gi, StructuralGroup.class));

        gi = dictionary.getStructuralGroups(
            DictionaryPath.ROOT
        ,   "%"
        ,   hostStruct.type
        ,   null
        ,   null
        );
        assertEquals(84, countGroups(gi, StructuralGroup.class));
        
        gi = dictionary.getStructuralGroups(
                DictionaryPath.ROOT
            ,   "%"
            ,   contactStruct.type
            ,   null
            ,   null
        );
        assertEquals(0, countGroups(gi, StructuralGroup.class));
    }

    public void testQueryGroupByGroupNameTemplate() throws DictionaryException {
        IDictionaryIterator<IMGroup> gi = dictionary.getStructuralGroups(
            DictionaryPath.ROOT
        ,   "b%" // The middle-tier groups have names starting in 'b'
        ,   null
        ,   null
        ,   null
        );
        assertEquals(50, countGroups(gi, StructuralGroup.class));
    }

    public void testQueryGroupByGroupNameTemplateAndContainedType() throws DictionaryException {
        IDictionaryIterator<IMGroup> gi = dictionary.getStructuralGroups(
            DictionaryPath.ROOT
        ,   "%0" // Each tier has a group with the name ending in 0
        ,   userStruct.type
        ,   null
        ,   null
        );
        assertEquals(30, countGroups(gi, StructuralGroup.class));
    }

    public void testQueryElementsByPartialPath() throws DictionaryException {
        IDictionaryIterator<IMElement> ei = dictionary.query(
            dictionary.condition(new DictionaryPath("com|bluejungle|a3".split("[|]")), false)
        ,   null
        ,   null
        ,   null
        );
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(63, counts[0]);
        assertEquals(42, counts[2]);
        ei = dictionary.query(
            dictionary.condition(new DictionaryPath("com|bluejungle|a2".split("[|]")), false)
        ,   null
        ,   null
        ,   null
        );
        counts = countUsersContactsHostsAndApps(ei);
        assertEquals(0, counts[0]);
        assertEquals(0, counts[1]);
    }

    public void testQueryByElementTypeAndPath() throws DictionaryException {
        CompositePredicate pred = new CompositePredicate(
            BooleanOp.AND
        ,   dictionary.condition(new DictionaryPath("com|bluejungle|a3".split("[|]")), false));
        pred.addPredicate(dictionary.condition(userStruct.type));
        IDictionaryIterator<IMElement> ei = dictionary.query(
            pred
        ,   null
        ,   null
        ,   null
        );
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(63, counts[0]);
        assertEquals(0, counts[1]);
    }

    public void testRetrieveSubelementsByType() throws DictionaryException {
        IMGroup group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a9".split("[|]")), null
        );
        IDictionaryIterator<IMElement> ei = group.getAllChildElements();
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(63, counts[0]);
        assertEquals(42, counts[2]);
    }

    public void testRetrieveSubgroups() throws DictionaryException {
        IMGroup group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a8".split("[|]")), null
        );
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(20, countGroups(gi, StructuralGroup.class));
        gi = group.getDirectChildGroups();
        assertEquals(5, countGroups(gi, StructuralGroup.class));
    }

    public void testQueryByElementTypeAndGroup() throws DictionaryException {
        CompositePredicate pred = new CompositePredicate(
            BooleanOp.AND
        ,   dictionary.getGroup(
                new DictionaryPath("com|bluejungle|a3".split("[|]"))
            ,   null
            ).getTransitiveMembershipPredicate()
        );
        pred.addPredicate(dictionary.condition(userStruct.type));
        IDictionaryIterator<IMElement> ei = dictionary.query(
            pred
        ,   null
        ,   null
        ,   null
        );
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(63, counts[0]);
        assertEquals(0, counts[1]);
    }

    public void testRenameSingleStructuralGroup() throws DictionaryException {
        IMGroup group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a8".split("[|]")), null
        );
        assertNotNull(group);
        group.setPath(new DictionaryPath("com|bluejungle|movedto|here".split("[|]")));
        IEnrollmentSession session = enrollments[0].createSession();
        try {
            session.beginTransaction();
            session.saveElements(Arrays.asList(new IElementBase[] {group} ));
            session.commit();
        } finally {
            session.close(true, null);
        }
        // Verify the rename of the group object itself
        assertEquals(
            new DictionaryPath("com|bluejungle|movedto|here".split("[|]"))
        ,   group.getPath()
        );
    }
    
    public void testRenameSingleGroupSucceeded() throws DictionaryException {
        // Verify that the new group is there
        IMGroup group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|movedto|here".split("[|]")), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(20, countGroups(gi, StructuralGroup.class));
        // Verify that the old group is remembered as of its creation time
        group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a8".split("[|]")), creationTime
        );
        assertNotNull(group);
        gi = group.getAllChildGroups();
        assertEquals(20, countGroups(gi, StructuralGroup.class));
        // Verify that the old group is currently not there
        group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a8".split("[|]")), null
        );
        assertNull(group);
    }

    public void testRenameMultipleUnrelatedStructuralGroups() throws DictionaryException {
        IMGroup group1 = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a7".split("[|]")), null
        );
        assertNotNull(group1);
        IMGroup group2 = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a6".split("[|]")), null
        );
        assertNotNull(group2);
        group1.setPath(new DictionaryPath("com|place1".split("[|]")));
        group2.setPath(new DictionaryPath("com|place2".split("[|]")));
        IEnrollmentSession session = enrollments[0].createSession();
        try {
            session.beginTransaction();
            session.saveElements(Arrays.asList(new IElementBase[] {group1, group2} ));
            session.commit();
        } finally {
            session.close(true, "sucess");
        }
        // Verify the rename of the groups object itself
        assertEquals(
            new DictionaryPath("com|place1".split("[|]"))
        ,   group1.getPath()
        );
        assertEquals(
            new DictionaryPath("com|place2".split("[|]"))
        ,   group2.getPath()
        );
    }

    public void testRenameMultipleUnrelatedStructuralGroupsSucceeded() throws DictionaryException {
        // Verify that the new groups are there
        IMGroup group1 = dictionary.getGroup(
            new DictionaryPath("com|place1".split("[|]")), null
        );
        assertNotNull(group1);
        IMGroup group2 = dictionary.getGroup(
            new DictionaryPath("com|place2".split("[|]")), null
        );
        assertNotNull(group2);
        IDictionaryIterator<IMGroup> gi1 = group1.getAllChildGroups();
        assertEquals(20, countGroups(gi1, StructuralGroup.class));
        IDictionaryIterator<IMElement> ei = dictionary.query(
            group1.getTransitiveMembershipPredicate(), null, null, null
        );
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(63, counts[0]);
        assertEquals(0, counts[1]);

        IDictionaryIterator<IMGroup> gi2 = group2.getAllChildGroups();
        assertEquals(20, countGroups(gi2, StructuralGroup.class));
        ei = dictionary.query(
            group2.getTransitiveMembershipPredicate(), null, null, null
        );
        counts = countUsersContactsHostsAndApps(ei);
        assertEquals(0, counts[0]);
        assertEquals(42, counts[2]);

        // Verify that the old groups are remembered as of their creation time
        group1 = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a7".split("[|]")), creationTime
        );
        assertNotNull(group1);
        gi1 = group1.getAllChildGroups();
        assertEquals(20, countGroups(gi1, StructuralGroup.class));

        ei = dictionary.query(
            group1.getTransitiveMembershipPredicate(), creationTime, null, null
        );
        counts = countUsersContactsHostsAndApps(ei);
        assertEquals(63, counts[0]);
        assertEquals(0, counts[1]);

        group2 = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a6".split("[|]")), creationTime
        );
        assertNotNull(group2);
        gi2 = group2.getAllChildGroups();
        assertEquals(20, countGroups(gi2, StructuralGroup.class));
        ei = dictionary.query(
            group2.getTransitiveMembershipPredicate(), creationTime, null, null
        );
        counts = countUsersContactsHostsAndApps(ei);
        assertEquals(0, counts[0]);
        assertEquals(42, counts[2]);

        // Verify that the old groups are currently not there
        group1 = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a7".split("[|]")), null
        );
        assertNull(group1);
        group2 = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a6".split("[|]")), null
        );
        assertNull(group2);
    }

    public void testRenameMultipleRelatedStructuralGroups() throws DictionaryException {
        IMGroup group1 = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a3".split("[|]")), null
        );
        assertNotNull(group1);
        IMGroup group2 = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a3|b1".split("[|]")), null
        );
        assertNotNull(group2);
        group1.setPath(new DictionaryPath("com|redsea|island".split("[|]")));
        group2.setPath(new DictionaryPath("com|redsea|island|b1".split("[|]")));
        IEnrollmentSession session = enrollments[0].createSession();
        try {
            session.beginTransaction();
            session.saveElements(Arrays.asList(new IElementBase[] {group1, group2} ));
            session.commit();
        } finally {
            session.close(true, "success");
        }
        // Verify the rename of the groups object itself
        assertEquals(
            new DictionaryPath("com|redsea|island".split("[|]"))
        ,   group1.getPath()
        );
        assertEquals(
            new DictionaryPath("com|redsea|island|b1".split("[|]"))
        ,   group2.getPath()
        );
    }

    public void testRenameMultipleRelatedStructuralGroupsSucceeded() throws DictionaryException {
        // Verify that the new group is there
        IMGroup group = dictionary.getGroup(
            new DictionaryPath("com|redsea|island".split("[|]")), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(20, countGroups(gi, StructuralGroup.class));
        IDictionaryIterator<IMElement> ei = dictionary.query(
            group.getTransitiveMembershipPredicate(), null, null, null
        );
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(63, counts[0]);
        assertEquals(42, counts[2]);

        // Verify that the old group is remembered as of its creation time
        group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a3".split("[|]")), creationTime
        );
        assertNotNull(group);
        gi = group.getAllChildGroups();
        assertEquals(20, countGroups(gi, StructuralGroup.class));
        ei = dictionary.query(
            group.getTransitiveMembershipPredicate(), creationTime, null, null
        );
        counts = countUsersContactsHostsAndApps(ei);
        assertEquals(63, counts[0]);
        assertEquals(42, counts[2]);

        // Verify that the old group is currently not there
        group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a7".split("[|]")), null
        );
        assertNull(group);
    }

    public void testRenameGroupsByAddingAsChild() throws DictionaryException {
        preMoveTime = new Date();
        IMGroup toGroup = dictionary.getGroup(
            new DictionaryPath("com|redsea|island".split("[|]")), null
        );
        assertNotNull(toGroup);
        IMGroup fromGroup = dictionary.getGroup(
            new DictionaryPath("com|place2".split("[|]")), null
        );
        assertNotNull(fromGroup);
        toGroup.addChild(fromGroup);
        IEnrollmentSession session = enrollments[0].createSession();
        try {
            session.beginTransaction();
            session.saveElements(Arrays.asList(new IElementBase[] {fromGroup} ));
            session.commit();
        } finally {
            session.close(true, null);
        }
        assertEquals(
            new DictionaryPath("com|redsea|island|place2".split("[|]"))
        ,   fromGroup.getPath()
        );
    }

    public void testRenameGroupsByAddingAsChildSucceeded() throws DictionaryException {
        // Verify that the new group is there
        IMGroup group = dictionary.getGroup(
            new DictionaryPath("com|redsea|island|place2".split("[|]")), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(20, countGroups(gi, StructuralGroup.class));
        // Verify that the original group is remembered as of its creation time
        group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a6".split("[|]")), creationTime
        );
        assertNotNull(group);
        gi = group.getAllChildGroups();
        assertEquals(20, countGroups(gi, StructuralGroup.class));
        // Verify that the original group is currently not there
        group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a6".split("[|]")), null
        );
        assertNull(group);
        // Verify that the pre-move group is remembered
        group = dictionary.getGroup(
            new DictionaryPath("com|place2".split("[|]")), preMoveTime
        );
        assertNotNull(group);
        gi = group.getAllChildGroups();
        assertEquals(20, countGroups(gi, StructuralGroup.class));
    }

    public void testRenameElenentsByAddingAsChild() throws DictionaryException {
        IMGroup toGroup = dictionary.getGroup(
            new DictionaryPath("com|redsea|island".split("[|]")), null
        );
        assertNotNull(toGroup);
        IDictionaryIterator<IMElement> ei = dictionary.query(
            dictionary.condition(new DictionaryPath("com|bluejungle|a1|b2|c0".split("[|]")), false)
        ,   null
        ,   null
        ,   null
        );
        assertNotNull(ei);
        assertTrue(ei.hasNext());
        List<IElementBase> toSave = new ArrayList<IElementBase>(100);
        try {
            while (ei.hasNext()) {
                IElement e = ei.next();
                assertNotNull(e);
                toGroup.addChild(e);
                toSave.add(e);
            }
        } finally {
            ei.close();
        }
        IEnrollmentSession session = enrollments[0].createSession();
        try {
            session.beginTransaction();
            session.saveElements(toSave);
            session.commit();
        } finally {
            session.close(true, null);
        }
        for (IElementBase e : toSave) {
            assertTrue(toGroup.getPath().isParentOf(e.getPath()));
        }
    }

    public void testRenameElenentsByAddingAsChildSucceeded() throws DictionaryException {
        IMGroup toGroup = dictionary.getGroup(
            new DictionaryPath("com|redsea|island".split("[|]")), null
        );
        assertNotNull(toGroup);
        IDictionaryIterator<IMElement> ei = dictionary.query(
            toGroup.getTransitiveMembershipPredicate(), null, null, null
        );
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertEquals(66, counts[0]);
        assertEquals(84, counts[2]);
        ei = dictionary.query(
            dictionary.condition(new DictionaryPath("com|bluejungle|a1|b2|c0".split("[|]")), false)
        ,   preMoveTime
        ,   null
        ,   null
        );
        counts = countUsersContactsHostsAndApps(ei);
        assertEquals(3, counts[0]);
        assertEquals(0, counts[1]);
    }

    public void testQueryChanges() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMGroup> gi = dictionary.getStructuralGroups(
            dictionary.changedCondition(null, now)
        ,   null
        ,   now
        ,   null
        );
        assertEquals(210, countGroups(gi, StructuralGroup.class));
    }

    public void testQueryChangesWithUsers() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMGroup> gi = dictionary.getStructuralGroups(
            dictionary.changedCondition(null, now)
        ,   userStruct.type
        ,   now
        ,   null
        );
        assertEquals(104, countGroups(gi, StructuralGroup.class));
    }

    public void testQueryChangesWithUsersSinceFirstUpdate() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMGroup> gi = dictionary.getStructuralGroups(
            dictionary.changedCondition(preMoveTime, now)
        ,   userStruct.type
        ,   now
        ,   null
        );
        assertEquals(0, countGroups(gi, StructuralGroup.class));
    }

    public void testStructMembershipChanges() throws DictionaryException {
        IMGroup group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|a9".split("[|]")), null
        );
        assertNotNull(group);
        IGroupChanges changes = group.getChanges(
            hostStruct.type
        ,   null
        ,   null
        );
        assertNotNull(changes);
        try {
            IDictionaryIterator<Long> added = changes.getKeysOfAddedMembers();
            assertNotNull(added);
            int count = 0;
            while(added.hasNext()) {
                assertNotNull(added.next());
                count++;
            }
            assertEquals(42, count);
            IDictionaryIterator<Long> removed = changes.getKeysOfRemovedMembers();
            assertNotNull(removed);
            count = 0;
            while(removed.hasNext()) {
                assertNotNull(removed.next());
                count++;
            }
            assertEquals(0, count);
        } finally {
            changes.close();
        }
    }

    public void testQueryUserByUniqueName() throws DictionaryException {
        IMElement element = dictionary.getElement("usery1", null);
        assertNotNull(element);
        userIdToQuery = element.getInternalKey();
        assertTrue("usery1".equalsIgnoreCase(element.getUniqueName()));
        assertFalse("usery1".equals(element.getUniqueName()));
        element = dictionary.getElement("unknown", null);
        assertNull(element);
    }

    public void testQueryGroupByUniqueName() throws DictionaryException {
        IMGroup group = dictionary.getGroup("GROUP50396928", null);
        assertNotNull(group);
        groupIdToQuery = group.getInternalKey();
        assertTrue("GROUP50396928".equalsIgnoreCase(group.getUniqueName()));
        assertFalse("GROUP50396928".equals(group.getUniqueName()));
        group = dictionary.getGroup("unknown", null);
        assertNull(group);
    }

    public void testQueryElementByInternalKey() throws DictionaryException {
        IMElement element = dictionary.getElement(userIdToQuery, null);
        assertNotNull(element);
        assertEquals("userY1", element.getUniqueName());
    }

    public void testQueryGroupByInternalKey() throws DictionaryException {
        IMGroup group = dictionary.getGroup(groupIdToQuery, null);
        assertNotNull(group);
        assertEquals("Group50396928", group.getUniqueName());
    }

    /**
     * This method adds groups, users, and hosts.
     * There are 10 groups at the top level,
     * five at the next level down,
     * and two groups at the lowest level.
     *
     * Groups descending from odd-numbered roots contain 3 users.
     * Groups descending from roots divisible by 3 contain 2 hosts.
     * As the result, groups descending from roots 3, 6, and 9 contain
     * both users (3) and hosts (2).
     *
     * @param n the current level; when it is three, we add a group.
     * @param pos a four-byte "coordinate" of the group.
     * @param res the <code>Collection</code> to which we add groups.
     * The caller is expected to save that collection.
     */
    private void seedRecursive( int n, byte[] pos, Collection<IElementBase> res) {
        if ( n == 3 ) {
            return;
        }
        int numBase = pos[0];
        numBase *= 256;
        numBase += pos[1];
        numBase *= 256;
        numBase += pos[2];
        numBase *= 256;
        for ( pos[n] = 0 ; pos[n] != 10/(n+1) ; pos[n]++ ) {
            int num = numBase+pos[n];
            seedRecursive(n+1, pos, res);
            String[] pathStrBase = new String[] {
                "com"
            ,   "bluejungle"
            ,   "a"+pos[0]
            ,   "b"+pos[1]
            ,   "c"+pos[2] };
            String[] pathStr = new String[n+3];
            System.arraycopy(pathStrBase, 0, pathStr, 0, pathStr.length);
            DictionaryPath path = new DictionaryPath(pathStr);
            for ( int i = n+1 ; i != pos.length ; i++ ) {
                pos[i] = -1;
            }
            pos[3] = 0;
            IMElementBase element;
            res.add(
                element = enrollments[0].makeNewStructuralGroup(
                    path
                ,   new DictionaryKey(pos)
                )
            );
            element.setUniqueName("Group"+num);
            if (pos[0] % 2 != 0) {
                pos[3] = 1;
                res.add(
                    element = enrollments[0].makeNewElement(
                        new DictionaryPath("userX", path)
                    ,   userStruct.type
                    ,   new DictionaryKey(pos)
                    )
                );
                element.setUniqueName("userX"+num);
                pos[3] = 2;
                res.add(
                    element = enrollments[0].makeNewElement(
                        new DictionaryPath("userY", path)
                    ,   userStruct.type
                    ,   new DictionaryKey(pos)
                    )
                );
                element.setUniqueName("userY"+num);
                pos[3] = 3;
                res.add(
                    element = enrollments[0].makeNewElement(
                        new DictionaryPath("userZ", path)
                    ,   userStruct.type
                    ,   new DictionaryKey(pos)
                    )
                );
                element.setUniqueName("userZ"+num);
            }
            if (pos[0] % 3 == 0) {
                pos[3] = 4;
                res.add(
                    element = enrollments[0].makeNewElement(
                        new DictionaryPath("hostQ", path)
                    ,   hostStruct.type
                    ,   new DictionaryKey(pos)
                    )
                );
                element.setUniqueName("hostQ"+num);
                pos[3] = 5;
                res.add(
                    element = enrollments[0].makeNewElement(
                        new DictionaryPath("hostR", path)
                    ,   hostStruct.type
                    ,   new DictionaryKey(pos)
                    )
                );
                element.setUniqueName("hostR"+num);
            }
        }
    }

}
