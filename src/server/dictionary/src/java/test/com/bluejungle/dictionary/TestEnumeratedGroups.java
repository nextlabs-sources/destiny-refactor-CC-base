/*
 * All sources, binaries and HTML pages (C) Copyright 2006 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/test/com/bluejungle/dictionary/TestEnumeratedGroups.java#1 $
 */

package com.bluejungle.dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.TestSuite;

import com.bluejungle.framework.expressions.PredicateConstants;

/**
 * @author sergey
 *
 */
public class TestEnumeratedGroups extends AbstractDictionaryTest {

    private static Date creationTime;
    private static Date firstModificationTime;
    private static Date secondModificationTime;
    private static Date thirdModificationTime;
    private static Date fourthModificationTime;
    private static final Set<Long> elementIds = new HashSet<Long>();
    private static final Set<Long> userGroupIds = new HashSet<Long>();
    private static final Set<Long> hostGroupIds = new HashSet<Long>();

    private static byte GROUP_COUNT = 16;

    public static TestSuite suite() {
        return new TestSuite(TestEnumeratedGroups.class);
    }

    public void testSetupDictionary() throws Exception {
        init();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assertEquals(0, ((Dictionary)dictionary).getCountedSessionCount());
    }

    public void testSeedInitialData() throws DictionaryException {
        List<IElementBase> data = new ArrayList<IElementBase>();
        String[] path = new String[] {"com", "bluejungle", null, null};
        byte[] key = new byte[2];
        // Seed the groups
        path[2] = "groups";
        key[0] = 1;
        for (key[1] = 0 ; key[1] != GROUP_COUNT ; key[1]++) {
            path[3] = "GROUP#"+key[1];
            data.add( enrollments[0].makeNewEnumeratedGroup(
                new DictionaryPath(path), new DictionaryKey(key)
            ));
        }
        // Seed the users
        path[2] = "users";
        key[0] = 2;
        for (key[1] = -64 ; key[1] != 64 ; key[1]++) {
            path[3] = "USER#"+key[1];
            data.add( enrollments[0].makeNewElement(
                new DictionaryPath(path), userStruct.type, new DictionaryKey(key)
            ));
        }
        // Seed the hosts
        path[2] = "hosts";
        key[0] = 3;
        for (key[1] = 0 ; key[1] != 64 ; key[1]++) {
            path[3] = "HOST#"+key[1];
            data.add( enrollments[0].makeNewElement(
                new DictionaryPath(path), hostStruct.type, new DictionaryKey(key)
            ));
        }
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveElements(data);
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
        IDictionaryIterator<IMElement> all = dictionary.query(
            PredicateConstants.TRUE, creationTime, null, null
        );
        assertNotNull(all);
        int[] counts = countUsersContactsHostsAndApps(all);
        assertNotNull(counts);
        assertEquals(128, counts[0]);
        assertEquals(64, counts[2]);
        IDictionaryIterator<IMGroup> gi = dictionary.getEnumeratedGroups(
            "%", null, null, null
        );
        assertNotNull(gi);
        assertEquals(GROUP_COUNT, countGroups(gi, EnumeratedGroup.class));
    }

    public void testQueryEmptyGroups() throws DictionaryException {
        IMGroup group = dictionary.getGroup(
            new DictionaryPath("com|bluejungle|groups|GROUP#3".split("[|]"))
        ,   creationTime
        );
        assertNotNull(group);
        IDictionaryIterator<IMElement> none = dictionary.query(
            group.getTransitiveMembershipPredicate()
        ,   creationTime
        ,   null
        ,   null
        );
        try {
            assertFalse(none.hasNext());
        } finally {
            none.close();
        }
    }

    public void testAddChildren() throws DictionaryException {
        Set<IElementBase> toSave = new HashSet<IElementBase>();
        byte[] groupKey = new byte[] {1, 0};
        for (groupKey[1] = 0 ; groupKey[1] != GROUP_COUNT ; groupKey[1]++) {
            IMGroup group = enrollments[0].getGroup(
                new DictionaryKey(groupKey), null
            );
            assertNotNull(group);
            byte[] userKey = new byte[] {2, 0};
            int from = groupKey[1];
            from *= 8;
            from -=64;
            int to = from+8;
            for ( userKey[1] = (byte)from; userKey[1] != to ; userKey[1]++ ) {
                IMElement user = enrollments[0].getElement(
                    new DictionaryKey(userKey), null
                );
                assertNotNull(user);
                group.addChild(user);
                toSave.add(user);
            }
            byte[] hostKey = new byte[] {3, 0};
            for ( hostKey[1] = groupKey[1]; hostKey[1] < 64 ; hostKey[1] += 16 ) {
                IMElement host = enrollments[0].getElement(
                    new DictionaryKey(hostKey), null
                );
                assertNotNull(host);
                group.addChild(host);
                toSave.add(host);
            }
            /*
             * The following code connects groups 1..6 to children with numbers
             * 2*n+1 and 2*n+2, forming the following tree:
             *
             * 0
             * +-1
             * | +-3
             * | | +-7
             * | | +-8
             * | +-4
             * |   +-9
             * |   +-10
             * +-2
             *   +-5
             *   | +-11
             *   | +-12
             *   +-6
             *     +-13
             *     +-14
             */
            if (groupKey[1] < GROUP_COUNT/2-1) {
                byte[] chGroupKey = new byte[] {1, (byte)(2*(groupKey[1]+1))};
                IMGroup chGroup = enrollments[0].getGroup(
                    new DictionaryKey(chGroupKey), null
                );
                assertNotNull(chGroup);
                group.addChild(chGroup);
                chGroupKey[1]--;
                chGroup = enrollments[0].getGroup(
                    new DictionaryKey(chGroupKey), null
                );
                assertNotNull(chGroup);
                group.addChild(chGroup);
            }
            
            toSave.add(group);
        }
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
        firstModificationTime = new Date();
    }

    public void testAddChildrenWasSuccessfulDirect() throws DictionaryException {
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 15}), null
        );
        assertNotNull(group);
        userGroupIds.add(group.getInternalKey());
        hostGroupIds.add(group.getInternalKey());
        IDictionaryIterator<IMElement> ei = dictionary.query(
            group.getTransitiveMembershipPredicate()
        ,   firstModificationTime
        ,   null
        ,   null
        );
        assertNotNull(ei);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(8, counts[0]);
        assertEquals(4, counts[2]);
        ei = dictionary.query(
            group.getTransitiveMembershipPredicate()
        ,   firstModificationTime
        ,   null
        ,   null
        );
        assertNotNull(ei);
        try {
            while (ei.hasNext()) {
                elementIds.add(ei.next().getInternalKey());
            }
        } finally {
            ei.close();
        }
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(0, countGroups(gi, EnumeratedGroup.class));
        group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 0}), null
        );
        assertNotNull(group);
        gi = group.getDirectChildGroups();
        assertNotNull(gi);
        assertEquals(2, countGroups(gi, EnumeratedGroup.class));
    }

    public void testQueryByIds() throws DictionaryException {
        Set<Long> allIds = new HashSet<Long>();
        allIds.addAll(elementIds);
        allIds.addAll(userGroupIds);
        allIds.addAll(hostGroupIds);
        assertFalse(allIds.isEmpty());
        IDictionaryIterator<IMElementBase> all = dictionary.getElementsById(allIds, null);
        try {
            int count = 0;
            while (all.hasNext()) {
                count++;
                IMElementBase e = all.next();
                assertNotNull(e);
                Long id = e.getInternalKey();
                assertNotNull(id);
                assertTrue(allIds.contains(id));
                if (e instanceof IElement) {
                    assertTrue(elementIds.contains(id));
                } else {
                    assertTrue(userGroupIds.contains(id) || hostGroupIds.contains(id));
                }
            }
            assertEquals(allIds.size(), count);
        } finally {
            all.close();
        }
    }

    public void testAddChildrenWasSuccessfulFirstLevelIndirect() throws DictionaryException {
        // Group 6 has two child groups - 11 and 12. Children of
        // all three groups (6, 11, and 12) should show up in the
        // corresponding transitive membership query for 6.
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 6}), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(2, countGroups(gi, EnumeratedGroup.class));
        IDictionaryIterator<IMElement> ei = dictionary.query(
            group.getTransitiveMembershipPredicate()
        ,   firstModificationTime
        ,   null
        ,   null
        );
        assertNotNull(ei);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(24, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(12, counts[2]);
        assertEquals(0, counts[3]);
        // The direct query for 6 should return only its own children
        // (i.e. no children of 11 or 12 should be returned).
        ei = dictionary.query(
            group.getDirectMembershipPredicate()
        ,   firstModificationTime
        ,   null
        ,   null
        );
        assertNotNull(ei);
        counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(8, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(4, counts[2]);
        assertEquals(0, counts[3]);
    }

    public void testAddChildrenWasSuccessfulAllLevelsIndirect() throws DictionaryException {
        // Group 2 has two child groups, each having two children.
        // Children of all seven groups (2, 5, 6, 11, 12, 13 and 14)
        // should show up in the corresponding transitive membership query for 2.
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 2}), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(6, countGroups(gi, EnumeratedGroup.class));
        IDictionaryIterator<IMElement> ei = dictionary.query(
            group.getTransitiveMembershipPredicate()
        ,   firstModificationTime
        ,   null
        ,   null
        );
        assertNotNull(ei);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(56, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(28, counts[2]);
        assertEquals(0, counts[3]);
        // The direct query for 2 should return only its own children:
        ei = dictionary.query(
            group.getDirectMembershipPredicate()
        ,   firstModificationTime
        ,   null
        ,   null
        );
        assertNotNull(ei);
        counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(8, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(4, counts[2]);
        assertEquals(0, counts[3]);
    }

    public void testAddChildrenCircular() throws DictionaryException {
        IMGroup childGroup = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 11}), null
        );
        assertNotNull(childGroup);
        // Group 2 is a parent of group 11
        IMGroup parentGroup = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 2}), null
        );
        assertNotNull(parentGroup);
        // Create a circular reference by adding a parent
        // as a child to one of its existing children:
        childGroup.addChild(parentGroup);
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveElements(
                Arrays.asList(new IElementBase[] {childGroup})
            );
            session.commit();
        } catch ( DictionaryException dex ) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(true, null);
        }
        secondModificationTime = new Date();
    }

    public void testAddChildrenCircularWasSuccessful() throws DictionaryException {
        // Group 2, 5, and 11 form a dependency cycle.
        // As the result, all groups reachable from the cycle
        // are now reachable from any of these groups
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 5}), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(6, countGroups(gi, EnumeratedGroup.class));
        IDictionaryIterator<IMElement> ei = dictionary.query(
            group.getTransitiveMembershipPredicate()
        ,   secondModificationTime
        ,   null
        ,   null
        );
        assertNotNull(ei);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(56, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(28, counts[2]);
        assertEquals(0, counts[3]);
    }

    public void testRemoveChildrenCircular() throws DictionaryException {
        IMGroup childGroup = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 11}), null
        );
        assertNotNull(childGroup);
        IMGroup parentGroup = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 2}), null
        );
        assertNotNull(parentGroup);
        childGroup.removeChild(parentGroup);
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveElements(
                Arrays.asList(new IElementBase[] {childGroup})
            );
            session.commit();
        } catch ( DictionaryException dex ) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(true, null);
        }
        thirdModificationTime = new Date();
    }
    
    public void testMemberRemovalChangesWithHostsSinceSecondModification() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMGroup> gi = dictionary.getEnumeratedGroups(
            dictionary.changedCondition(secondModificationTime, now)
        ,   hostStruct.type
        ,   null
        ,   null
        );
        // group 2 changed in testRemoveChildrenCircular()
        assertEquals(1, countGroups(gi, EnumeratedGroup.class));
    }

    public void testRemoveChildrenCircularWasSuccessful() throws DictionaryException {
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 5}), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(2, countGroups(gi, EnumeratedGroup.class));
        IDictionaryIterator<IMElement> ei = dictionary.query(
            group.getTransitiveMembershipPredicate()
        ,   thirdModificationTime
        ,   null
        ,   null
        );
        assertNotNull(ei);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(24, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(12, counts[2]);
        assertEquals(0, counts[3]);
    }

    public void testRemoveChildren() throws DictionaryException {
        IMGroup group15 = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 15}), null
        );
        assertNotNull(group15);
        IDictionaryIterator<IMElement> ei = group15.getAllChildElements();
        assertNotNull(ei);
        try {
            assertTrue(ei.hasNext());
            // Delete every other child element of the group 
            boolean oddUser = false;
            boolean oddHost = false;
            while (ei.hasNext()) {
                IElement child = ei.next();
                if (child.getType().equals(userStruct.type)) {
                    if (oddUser) {
                        group15.removeChild(child);
                    }
                    oddUser = !oddUser;
                } else if (child.getType().equals(hostStruct.type)) {
                    if (oddHost) {
                        group15.removeChild(child);
                    }
                    oddHost = !oddHost;
                } else {
                    fail("Unknown element type");
                }
            }
        } finally {
            ei.close();
        }
        IMGroup group0 = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 0}), null
        );
        assertNotNull(group0);
        IDictionaryIterator<IMGroup> gi = group0.getDirectChildGroups();
        assertNotNull(gi);
        try {
            assertTrue(gi.hasNext());
            // Delete the first child group of the group 
            IGroup child = gi.next();
            group0.removeChild(child);
        } finally {
            gi.close();
        }
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.saveElements(Arrays.asList(new IElementBase[] {group15, group0}));
            session.commit();
        } catch ( DictionaryException dex ) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(true, null);
        }
    }

    public void testMemberRemovalChangesWithHostsSinceThirdModification() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMGroup> gi = dictionary.getEnumeratedGroups(
            dictionary.changedCondition(thirdModificationTime, now)
        ,   hostStruct.type
        ,   null
        ,   null
        );
        // group 0 and group 15 changed in testRemoveChildren()
        assertEquals(2, countGroups(gi, EnumeratedGroup.class));
    }
    
    public void testRemoveChildrenWasSuccessfulDirect() throws DictionaryException {
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 15}), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMElement> ei = group.getAllChildElements();
        assertNotNull(ei);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(4, counts[0]);
        assertEquals(2, counts[2]);
        group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 0}), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getDirectChildGroups();
        assertNotNull(gi);
        assertEquals(1, countGroups(gi, EnumeratedGroup.class));
    }

    public void testRemoveChildrenWasSuccessfulIndirect() throws DictionaryException {
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 0}), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertNotNull(gi);
        assertEquals(7, countGroups(gi, EnumeratedGroup.class));
    }

    public void testRemoveBridgeNodeGroups() throws DictionaryException {
        // Remove four "bridge" groups 
        IMGroup group3 = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 3}), null
        );
        assertNotNull(group3);
        IMGroup group4 = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 4}), null
        );
        assertNotNull(group4);
        IMGroup group5 = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 5}), null
        );
        assertNotNull(group5);
        IMGroup group6 = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 6}), null
        );
        assertNotNull(group6);
        IEnrollmentSession session = enrollments[0].createSession();
        assertNotNull(session);
        try {
            session.beginTransaction();
            session.deleteElements(
                Arrays.asList(
                    new IElementBase[] {group3, group4, group5, group6}
                )
            );
            session.commit();
        } catch ( DictionaryException dex ) {
            session.rollback();
            fail(dex.getMessage());
        } finally {
            session.close(true, null);
        }
        fourthModificationTime = new Date();
    }

    public void testEnumMembershipChanges() throws DictionaryException {
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 0}), null
        );
        assertNotNull(group);
        IGroupChanges changes = group.getChanges(
            userStruct.type
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
            assertEquals(120, count);
            IDictionaryIterator<Long> removed = changes.getKeysOfRemovedMembers();
            assertNotNull(removed);
            count = 0;
            while(removed.hasNext()) {
                assertNotNull(removed.next());
                count++;
            }
            assertEquals(104, count);
        } finally {
            changes.close();
        }
    }

    public void testRemoveBridgeNodeGroupsWasSuccessful() throws DictionaryException {
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 0}), null
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertEquals(1, countGroups(gi, EnumeratedGroup.class));
        IDictionaryIterator<IMElement> ei = dictionary.query(
            group.getTransitiveMembershipPredicate()
        ,   fourthModificationTime
        ,   null
        ,   null
        );
        assertNotNull(ei);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(16, counts[0]);
        assertEquals(0, counts[1]);
        assertEquals(8, counts[2]);
        assertEquals(0, counts[3]);
    }

    public void testQueryAsOfCreationTime() throws DictionaryException {
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 15}), firstModificationTime
        );
        assertNotNull(group);
        IDictionaryIterator<IMElement> ei = group.getAllChildElements();
        assertNotNull(ei);
        int[] counts = countUsersContactsHostsAndApps(ei);
        assertNotNull(counts);
        assertEquals(8, counts[0]);
        assertEquals(4, counts[2]);
        group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 0}), firstModificationTime
        );
        assertNotNull(group);
        IDictionaryIterator<IMGroup> gi = group.getDirectChildGroups();
        assertNotNull(gi);
        assertEquals(2, countGroups(gi, EnumeratedGroup.class));
    }

    public void testDeleteEnumeratedGroup() throws DictionaryException {
        IMGroup group = enrollments[0].getGroup(
            new DictionaryKey(new byte[] {1, 0}), null
        );
        assertNotNull(group);
        IEnrollmentSession es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
            es.deleteElements(Arrays.asList(new IElementBase[] {group}));
            es.commit();
        } catch (DictionaryException de) {
            try {
                es.rollback();
            } catch (Exception ignored) {
            }
            fail(de.getMessage());
        } finally {
            es.close(true, null);
        }
    }

    public void testQueryChanges() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMGroup> gi = dictionary.getEnumeratedGroups(
            dictionary.changedCondition(null, now)
        ,   null
        ,   null
        ,   null
        );
        assertEquals(16, countGroups(gi, EnumeratedGroup.class));
    }

    public void testQueryChangesWithHosts() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMGroup> gi = dictionary.getEnumeratedGroups(
            dictionary.changedCondition(null, now)
        ,   hostStruct.type
        ,   null
        ,   null
        );
        // FIXME All 15 groups should be returned, not the 11 remainig ones.
        assertEquals(11, countGroups(gi, EnumeratedGroup.class));
    }

    public void testQueryChangesWithHostsSinceFirstModification() throws DictionaryException {
        Date now = new Date();
        IDictionaryIterator<IMGroup> gi = dictionary.getEnumeratedGroups(
            dictionary.changedCondition(firstModificationTime, now)
        ,   hostStruct.type
        ,   null
        ,   null
        );
        assertEquals(2, countGroups(gi, EnumeratedGroup.class));
    }


    
    public void testGetEnumeratedGroupsWithLevels() throws DictionaryException {
        /*
         * Making a group level structure like this:
         *  group0
         *    - group1
         *       - group2 
         *         - group3
         *           - elmement4
         *  because element4 is a user, so, 
         *      getEnumeratedGroups(userData.type) should return 4 groups  
         */
        DictionaryPath gp = new DictionaryPath(new String[] {"test", "level", "0", "group0"});
        IMGroup group0 = enrollments[0].makeNewEnumeratedGroup(
            gp
        ,   new DictionaryKey(new byte[] {12,2,2,4,5,6,7,8,0})
        );
        assertNotNull(group0);
        
        gp = new DictionaryPath(new String[] {"test", "level", "1", "group1"});
        IMGroup group1 = enrollments[0].makeNewEnumeratedGroup(
            gp
        ,   new DictionaryKey(new byte[] {12,2,2,4,5,6,7,8,1})
        );
        assertNotNull(group1);
        group0.addChild(group1);
        
        gp = new DictionaryPath(new String[] {"test", "level", "2", "group2"});
        IMGroup group2 = enrollments[0].makeNewEnumeratedGroup(
            gp
        ,   new DictionaryKey(new byte[] {12,2,2,4,5,6,7,8,2})
        );
        assertNotNull(group2);
        group1.addChild(group2);

        gp = new DictionaryPath(new String[] {"test", "level", "3", "group3"});
        IMGroup group3 = enrollments[0].makeNewEnumeratedGroup(
            gp
        ,   new DictionaryKey(new byte[] {12,2,2,4,5,6,7,8,3})
        );
        assertNotNull(group3);
        group2.addChild(group3);
        
        gp = new DictionaryPath(new String[] {"test", "level", "4", "element4"});
        IElement element4 = enrollments[0].makeNewElement(
                gp
            ,   userStruct.type
            ,   new DictionaryKey(new byte[] {12,2,3,4,5,6,7,8,4})
            );
        assertNotNull(element4);
        group3.addChild(element4);
        
        IEnrollmentSession es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
            es.saveElements(Arrays.asList(new IElementBase[] {group0, group1, group2, group3, element4}));
            es.commit();
        } finally {
            es.close(true, "");
        }
        
        IDictionaryIterator<IMGroup> gi = dictionary.getEnumeratedGroups(
            dictionary.condition(new DictionaryPath(new String[] {"test", "level"}), false)
        ,   userStruct.type
        ,   null
        ,   null
        );
        
        assertEquals(4, countGroups(gi, EnumeratedGroup.class));
    }
    
    
    public void testAddForwardRefsToGroup() throws DictionaryException {
        DictionaryPath gp = new DictionaryPath(new String[] {"test", "forward", "group"});
        IMGroup group = enrollments[0].makeNewEnumeratedGroup(
            gp
        ,   new DictionaryKey(new byte[] {1,2,3,4,5,6,7,8,0})
        );
        assertNotNull(group);
        DictionaryPath fe1 = new DictionaryPath(new String[] {"forward", "element", "path", "one"});
        DictionaryPath fe2 = new DictionaryPath(new String[] {"forward", "element", "path", "two"});
        DictionaryPath fe3 = new DictionaryPath(new String[] {"forward", "element", "path", "three"});
        DictionaryPath fg1 = new DictionaryPath(new String[] {"forward", "group", "path", "one"});
        DictionaryPath fg2 = new DictionaryPath(new String[] {"forward", "group", "path", "two"});
        DictionaryPath fg3 = new DictionaryPath(new String[] {"forward", "group", "path", "three"});
        List<IReferenceable> refCollection =
            enrollments[0].getProvisionalReferences(
                // The order of elements does not matter in this API
                Arrays.asList(new DictionaryPath[] {fe1, fg1, fe2, fg2, fe3, fg3})
            );
        assertNotNull(refCollection);
        assertEquals(6, refCollection.size());
        // Add forward references as group children.
        group.addChild(refCollection.get(0));
        group.addChild(refCollection.get(1));
        group.addChild(refCollection.get(2));
        group.addChild(refCollection.get(3));
        group.addChild(refCollection.get(4));
        group.addChild(refCollection.get(5));
        IEnrollmentSession es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
            es.saveElements(Arrays.asList(new IElementBase[] {group}));
            es.commit();
        } finally {
            es.close(true, "");
        }
        // The group should not have leaf children yet
        IDictionaryIterator<IMElement> ei = group.getAllChildElements();
        assertNotNull(ei);
        try {
            assertFalse(ei.hasNext());
        } finally {
            ei.close();
        }
        // The group should not have group children yet
        IDictionaryIterator<IMGroup> gi = group.getAllChildGroups();
        assertNotNull(gi);
        try {
            assertFalse(gi.hasNext());
        } finally {
            gi.close();
        }
        
        // Adding the same reference multiple times is allowed.
        group.addChild(refCollection.get(0));
        group.addChild(refCollection.get(1));
        group.addChild(refCollection.get(2));
        group.addChild(refCollection.get(3));
        es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
            es.saveElements(Arrays.asList(new IElementBase[] {group}));
            es.commit();
        } finally {
            es.close(true, "");
        }
        // The group should still have no leaf children
        ei = group.getAllChildElements();
        assertNotNull(ei);
        try {
            assertFalse(ei.hasNext());
        } finally {
            ei.close();
        }
        // The group should still have no group children
        gi = group.getAllChildGroups();
        assertNotNull(gi);
        try {
            assertFalse(gi.hasNext());
        } finally {
            gi.close();
        }

        // Now "materialize" one group and one element
        IElement element1 = enrollments[0].makeNewElement(
            fe1
        ,   userStruct.type
        ,   new DictionaryKey(new byte[] {1,2,3,4,5,6,7,8,1})
        );
        IGroup group1 = enrollments[0].makeNewEnumeratedGroup(
            fg1
        ,   new DictionaryKey(new byte[] {1,2,3,4,5,6,7,8,2})
        );
        es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
            es.saveElements(Arrays.asList(new IElementBase[] {element1, group1}));
            es.commit();
        } finally {
            es.close(true, "");
        }

        // The group should have one leaf child now
        ei = group.getAllChildElements();
        assertNotNull(ei);
        int[] cnt = countUsersContactsHostsAndApps(ei);
        assertEquals(1, cnt[0]);
        assertEquals(0, cnt[1]);
        assertEquals(0, cnt[2]);
        assertEquals(0, cnt[3]);

        // The group should have one group child now
        gi = group.getAllChildGroups();
        assertNotNull(gi);
        assertEquals(1, countGroups(gi, EnumeratedGroup.class));
        // Now "materialize" the remaining groups and elements
        IElement element2 = enrollments[0].makeNewElement(
            fe2
        ,   hostStruct.type
        ,   new DictionaryKey(new byte[] {1,2,3,4,5,6,7,8,3})
        );
        IElement element3 = enrollments[0].makeNewElement(
            fe3
        ,   appStruct.type
        ,   new DictionaryKey(new byte[] {1,2,3,4,5,6,7,8,4})
        );
        IGroup group2 = enrollments[0].makeNewEnumeratedGroup(
            fg2
        ,   new DictionaryKey(new byte[] {1,2,3,4,5,6,7,8,5})
        );
        IGroup group3 = enrollments[0].makeNewEnumeratedGroup(
            fg3
        ,   new DictionaryKey(new byte[] {1,2,3,4,5,6,7,8,6})
        );
        es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
            es.saveElements(Arrays.asList(new IElementBase[] {element2, group2, element3, group3}));
            es.commit();
        } finally {
            es.close(true, "");
        }

        // The group should have one three leaf children now
        ei = group.getAllChildElements();
        assertNotNull(ei);
        cnt = countUsersContactsHostsAndApps(ei);
        assertEquals(1, cnt[0]);
        assertEquals(0, cnt[1]);
        assertEquals(1, cnt[2]);
        assertEquals(1, cnt[3]);

        // The group should have three group children now
        gi = group.getAllChildGroups();
        assertNotNull(gi);
        assertEquals(3, countGroups(gi, EnumeratedGroup.class));
    }
    
    public void testGroupsWithLargeMembers() throws DictionaryException {
        int numberOfGroups = 1;
        int sizeOfGroup = 1100;
        Collection<IElementBase> toSave = new ArrayList<IElementBase>(numberOfGroups);
        for(int groupId=0; groupId<numberOfGroups; groupId++){
            DictionaryPath gp = new DictionaryPath(new String[] {"test", "large", "user", "group", 
                    Integer.toString(groupId)});
        
            IMGroup group = enrollments[0].makeNewEnumeratedGroup(
                gp
            ,   new DictionaryKey(gp.toString().getBytes())
            );
            assertNotNull(group);
            toSave.add(group);
            
            IMElement[] paths = new IMElement[sizeOfGroup];
            
            for (int memeberCount = 0; memeberCount < sizeOfGroup; memeberCount++) {
                DictionaryPath path = new DictionaryPath(new String[] { "test", "large", "users",
                                Integer.toString(groupId) + "_" + Integer.toString(memeberCount) });
                paths[memeberCount] = enrollments[0].makeNewElement(
                        path, 
                        userStruct.type, 
                        new DictionaryKey(path.toString().getBytes())
                );
                group.addChild(paths[memeberCount]);
                toSave.add(paths[memeberCount]);
            }
        }
        
        IEnrollmentSession es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
            es.saveElements(toSave);
            es.commit();
        } finally {
            es.close(true, "");
        }
        
        
        toSave.clear();
        Date now = new Date();
        for (int groupId = 0; groupId < numberOfGroups; groupId++) {
            DictionaryPath gp = new DictionaryPath(new String[] {"test", "large", "user", "group", 
                    Integer.toString(groupId)});
            IMGroup group = enrollments[0].getGroup(new DictionaryKey(gp.toString().getBytes()), now);
            IDictionaryIterator<IMElement> it = group.getAllChildElements();
            try {
                while(it.hasNext()){
                    group.removeChild(it.next());
                }
            } finally {
                it.close();
            }
            toSave.add(group);
        }
        
        es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
            es.saveElements(toSave);
            es.commit();
        } finally {
            es.close(true, "");
        }
    }
    
    public void testGroupsWithLargeReferenceMembers() throws DictionaryException {
        int numberOfGroups = 3;
        int sizeOfGroup = 1000;
        Collection<IElementBase> groups = new ArrayList<IElementBase>(numberOfGroups);
        for(int groupId=0; groupId<numberOfGroups; groupId++){
            DictionaryPath gp = new DictionaryPath(new String[] {"test", "large", "ref", "group", Integer.toString(groupId)});
            IMGroup group = enrollments[0].makeNewEnumeratedGroup(
                gp
            ,   new DictionaryKey(gp.toString().getBytes())
            );
            assertNotNull(group);
            
            DictionaryPath[] paths = new DictionaryPath[sizeOfGroup];
            
            for (int memeberCount = 0; memeberCount < sizeOfGroup; memeberCount++) {
                paths[memeberCount] = new DictionaryPath(new String[] { "test", "large", "refs",
                        Integer.toString(groupId) + "_" + Integer.toString(memeberCount) });
            }
            
            Collection<IReferenceable> refCollection =
                enrollments[0].getProvisionalReferences(
                    // The order of elements does not matter in this API
                    Arrays.asList(paths)
                );
            assertNotNull(refCollection);
            assertEquals(1000, refCollection.size());
            
            for(IReferenceable ref : refCollection){
                group.addChild(ref);
            }
        }
        
        
        IEnrollmentSession es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
            es.saveElements(groups);
            es.commit();
        } finally {
            es.close(true, "");
        }
    }
    
    public void testGroupsWithLargeGroupMembers() throws DictionaryException {
        int numberOfGroup  = 3;
        int sizeOfGroup = 1000;

        List<IMGroup> parents = new ArrayList<IMGroup>(numberOfGroup);
        for (int i = 0; i < numberOfGroup; i++) {
            DictionaryPath gp = new DictionaryPath(
                    new String[] { "test", "groups", "in", "group", "parent" + i });
            IMGroup parentGroup = enrollments[0].makeNewEnumeratedGroup(
                    gp, new DictionaryKey(gp.toString().getBytes()));
            assertNotNull(parentGroup);
            parents.add(parentGroup);
        }
       
        List<List<IElementBase>> childrenGroups = new ArrayList<List<IElementBase>>(numberOfGroup);
        
        for(IMGroup parent : parents){
            String parentName = Integer.toString(parent.hashCode());
            List<IElementBase> childrenGroup = new ArrayList<IElementBase>(sizeOfGroup);
            for (int childrenId = 0; childrenId < sizeOfGroup; childrenId++) {
                DictionaryPath  gp = new DictionaryPath(
                        new String[] {"test", "groups", "in", "group", parentName, Integer.toString(childrenId)});
                IMGroup group = enrollments[0].makeNewEnumeratedGroup(
                    gp, new DictionaryKey(gp.toString().getBytes()));
                assertNotNull(group);
                childrenGroup.add(group);
            }
            childrenGroups.add(childrenGroup);
        }
        
        IEnrollmentSession es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            int saveBatchSize = 512;
            List<IElementBase> willSaveList = new ArrayList<IElementBase>(saveBatchSize);
            Iterator<List<IElementBase>> itt = childrenGroups.iterator();
            while (itt.hasNext()) {
                Iterator<IElementBase> it = itt.next().iterator();
                while (it.hasNext()) {
                    willSaveList.add(it.next());
                    if (willSaveList.size() >= saveBatchSize) {
                        es.beginTransaction();
                        es.saveElements(willSaveList);
                        es.commit();
                        willSaveList = new ArrayList<IElementBase>(saveBatchSize);
                    }
                }
            }
            
            if (!willSaveList.isEmpty()) {
                es.beginTransaction();
                es.saveElements(willSaveList);
                es.commit();
            }
            
            
            for (int i = 0; i < numberOfGroup; i++) {
                IMGroup parent = parents.get(i);
                List<IElementBase> groupMembers = childrenGroups.get(i);
                
                Collection<DictionaryPath> paths = new ArrayList<DictionaryPath>(sizeOfGroup);
                for(IElementBase gm : groupMembers){
                    paths.add(gm.getPath());
                }
                
                Collection<IReferenceable> refCollection =
                        enrollments[0].getProvisionalReferences(paths);
                
                for(IReferenceable ref : refCollection){
                    parent.addChild(ref);
                }
            }
            
            es.beginTransaction();
			es.saveElements(Arrays.asList(parents.toArray(new IElementBase[parents.size()])));
            es.commit();
        } finally {
            es.close(true, "");
        }
    }
    
    private void testGroupInGroupInGroup(int numberOfGroup, int depthOfTheGroup)
            throws DictionaryException {
        List<IMGroup> groups = new ArrayList<IMGroup>(numberOfGroup * numberOfGroup);
        for(int j=0; j< numberOfGroup; j++){
            IMGroup child = null; 
            for (int i = 0; i < depthOfTheGroup; i++) {
                DictionaryPath gp = new DictionaryPath(
                        new String[] { "test", "groups", "groups", Integer.toString(j), Integer.toString(i) });
                IMGroup group = enrollments[0].makeNewEnumeratedGroup(
                        gp, new DictionaryKey(gp.toString().getBytes()));
                if(child != null){
                    group.addChild(child);
                }
                child = group;
                groups.add(group);
            }
        }
        
        IEnrollmentSession es = enrollments[0].createSession();
        assertNotNull(es);
        try {
            es.beginTransaction();
			es.saveElements(Arrays.asList(groups.toArray(new IElementBase[groups.size()])));
            es.commit();
        } finally {
            es.close(true, "");
        }
    }
    
    public void testGroupInGroupInGroup1() throws DictionaryException {
        int numberOfGroup  = 50;
        int depthOfTheGroup = 10;
        
        testGroupInGroupInGroup(numberOfGroup, depthOfTheGroup);
    }

    public void testGroupInGroupInGroup2() throws DictionaryException {
        int numberOfGroup  = 5;
        int depthOfTheGroup = 100;
        
        testGroupInGroupInGroup(numberOfGroup, depthOfTheGroup);
    }
 
    public void testCleanupEnrollments() throws DictionaryException {
        IConfigurationSession cs = dictionary.createSession();
        assertNotNull(cs);
        boolean isSuccess = false;
        try {
            cs.beginTransaction();
            cs.deleteEnrollment(enrollments[0]);
            cs.commit();
            isSuccess = true;
        } finally {
            if (isSuccess) {
                cs.rollback();
            }
            cs.close();
        }
    }

}
