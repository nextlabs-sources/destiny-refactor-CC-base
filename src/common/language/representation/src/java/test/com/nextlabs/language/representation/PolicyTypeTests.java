package com.nextlabs.language.representation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/PolicyTypeTests.java#1 $
 */

/**
 * Tests for the Policy Type class.
 *
 * @author Sergey Kalinichenko
 */
public class PolicyTypeTests {

    private static final Path TEST_PATH1 = new Path(
        "policy", "tests", "one"
    );

    private static final Path TEST_PATH2 = new Path(
        "policy", "tests", "two"
    );

    private static final String SECTION1 = "by";

    private static final String SECTION2 = "on";

    private static final IReference<IContextType> REF1C =
        IReferenceFactory.DEFAULT.create(123, IContextType.class);

    private static final IReference<IContextType> REF2C =
        IReferenceFactory.DEFAULT.create(TEST_PATH2, IContextType.class);

    private static final IReference<IContextType> REF3C =
        IReferenceFactory.DEFAULT.create(321, IContextType.class);

    private static final IReference<IPolicyType> REF1T =
        IReferenceFactory.DEFAULT.create(123, IPolicyType.class);

    private static final IReference<IPolicyType> REF2T =
        IReferenceFactory.DEFAULT.create(TEST_PATH2, IPolicyType.class);

    private static final IReference<IObligationType> REF1O =
        IReferenceFactory.DEFAULT.create(123, IObligationType.class);

    private static final IReference<IObligationType> REF2O =
        IReferenceFactory.DEFAULT.create(TEST_PATH2, IObligationType.class);

    private static final IReference<IObligationType> REF3O =
        IReferenceFactory.DEFAULT.create(321, IObligationType.class);

    private PolicyType pt1;

    private IPolicyType pt2;

    @Before
    public void prepare() {
        pt1 = new PolicyType(TEST_PATH1);
        pt2 = new PolicyType(TEST_PATH2);
    }

    @Test(expected=NullPointerException.class)
    public void createNullPath() {
        new PolicyType(null);
    }

    @Test(expected=NullPointerException.class)
    public void setBaseNull() {
        pt1.setBase(null);
    }

    @Test
    public void setBase() {
        assertFalse(pt1.hasBase());
        pt1.setBase(REF1T);
        assertTrue(pt1.hasBase());
        assertEquals(REF1C, pt1.getBase());
    }

    @Test
    public void removeBase() {
        assertFalse(pt1.hasBase());
        pt1.setBase(REF1T);
        assertTrue(pt1.hasBase());
        pt1.removeBase();
        assertFalse(pt1.hasBase());
    }

    @Test(expected=NullPointerException.class)
    public void addContextSectionNullSectionName() {
        pt1.addContext(null, REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addContextSectionNonTrimmedSectionName() {
        pt1.addContext(" aaa", REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addContextSectionEmptySectionName() {
        pt1.addContext("", REF1C);
    }

    @Test(expected=NullPointerException.class)
    public void addContextSectionNullReference() {
        pt1.addContext(SECTION1, null);
    }

    @Test
    public void addContextSection() {
        assertEquals(0, pt1.getContextCount(SECTION1));
        assertFalse(pt1.hasContext(SECTION1, REF1C));
        pt1.addContext(SECTION1, REF1C);
        assertEquals(1, pt1.getContextCount(SECTION1));
        assertTrue(pt1.hasContext(SECTION1, REF1C));
    }

    @Test
    public void addContextSectionDuplicate() {
        assertEquals(0, pt1.getContextCount(SECTION1));
        assertFalse(pt1.hasContext(SECTION1, REF1C));
        pt1.addContext(SECTION1, REF1C);
        pt1.addContext(SECTION1, REF1C);
        assertEquals(2, pt1.getContextCount(SECTION1));
        assertTrue(pt1.hasContext(SECTION1, REF1C));
    }

    @Test(expected=NullPointerException.class)
    public void addContextSectionIndexNullSectionName() {
        pt1.addContext(null, 0, REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addContextSectionIndexNonTrimmedSectionName() {
        pt1.addContext(" aaa", 0, REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addContextSectionIndexEmptySectionName() {
        pt1.addContext("", 0, REF1C);
    }

    @Test(expected=NullPointerException.class)
    public void addContextSectionIndexNullReference() {
        pt1.addContext(SECTION1, 0, null);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addContextSectionIndexNegative() {
        pt1.addContext(SECTION1, -1, REF1C);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addContextSectionIndexLarge() {
        pt1.addContext(SECTION1, 1, REF1C);
    }

    @Test
    public void addContextSectionIndex() {
        assertEquals(0, pt1.getContextCount(SECTION1));
        assertFalse(pt1.hasContext(SECTION1, REF1C));
        assertFalse(pt1.hasContext(SECTION1, REF2C));
        pt1.addContext(SECTION1, 0, REF1C);
        pt1.addContext(SECTION1, 0, REF2C);
        assertEquals(2, pt1.getContextCount(SECTION1));
        assertTrue(pt1.hasContext(SECTION1, REF1C));
        assertTrue(pt1.hasContext(SECTION1, REF2C));
        assertEquals(REF2C, pt1.getContext(SECTION1, 0));
        assertEquals(REF1C, pt1.getContext(SECTION1, 1));
    }

    @Test
    public void addContextSectionIndexDuplicate() {
        assertEquals(0, pt1.getContextCount(SECTION1));
        assertFalse(pt1.hasContext(SECTION1, REF1C));
        pt1.addContext(SECTION1, 0, REF1C);
        pt1.addContext(SECTION1, 0, REF1C);
        assertEquals(2, pt1.getContextCount(SECTION1));
        assertTrue(pt1.hasContext(SECTION1, REF1C));
        assertEquals(REF1C, pt1.getContext(SECTION1, 0));
        assertEquals(REF1C, pt1.getContext(SECTION1, 1));
    }

    @Test(expected=NullPointerException.class)
    public void addContextsSectionNullSectionName() {
        pt1.addContexts(null, Collections.singleton(REF1C));
    }

    @Test(expected=IllegalArgumentException.class)
    public void addContextsSectionNonTrimmedSectionName() {
        pt1.addContexts(" aaa", Collections.singleton(REF1C));
    }

    @Test(expected=IllegalArgumentException.class)
    public void addContextsSectionEmptySectionName() {
        pt1.addContexts("", Collections.singleton(REF1C));
    }

    @Test(expected=NullPointerException.class)
    public void addContextsSectionNullReferences() {
        pt1.addContexts(SECTION1, null);
    }

    @Test(expected=NullPointerException.class)
    public void addContextsSectionNullReferencesElement() {
        pt1.addContexts(
            SECTION1
        ,   Collections.singleton((IReference<IContextType>)null)
        );
    }

    @Test
    public void addContextsSection() {
        assertEquals(0, pt1.getContextCount(SECTION1));
        assertFalse(pt1.hasContext(SECTION1, REF1C));
        pt1.addContexts(SECTION1, Collections.singleton(REF1C));
        assertEquals(1, pt1.getContextCount(SECTION1));
        assertTrue(pt1.hasContext(SECTION1, REF1C));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addContextsSectionDuplicate() {
        assertEquals(0, pt1.getContextCount(SECTION1));
        assertFalse(pt1.hasContext(SECTION1, REF1C));
        pt1.addContexts(SECTION1, Arrays.asList(REF1C, REF1C));
        assertEquals(2, pt1.getContextCount(SECTION1));
        assertTrue(pt1.hasContext(SECTION1, REF1C));
    }

    @Test(expected=NullPointerException.class)
    public void getContextSectionNullSectionName() {
        pt1.getContext(null, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void getContextSectionNonTrimmedSectionName() {
        pt1.getContext(" aaa", 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void getContextSectionEmptySectionName() {
        pt1.getContext("", 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void getContextSectionUnknownSectionName() {
        pt1.getContext(SECTION1, 0);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getContextSectionIndexNegative() {
        pt1.addContext(SECTION1, REF1C);
        pt1.getContext(SECTION1, -1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getContextSectionIndexLarge() {
        pt1.addContext(SECTION1, REF1C);
        pt1.getContext(SECTION1, 1);
    }

    @Test
    public void getContextSection() {
        pt1.addContext(SECTION1, REF1C);
        assertEquals(REF1C, pt1.getContext(SECTION1, 0));
    }

    @Test(expected=NullPointerException.class)
    public void setContextSectionIndexNullSectionName() {
        pt1.addContext(SECTION1, REF1C);
        pt1.setContext(null, 0, REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setContextSectionIndexNonTrimmedSectionName() {
        pt1.addContext(SECTION1, REF1C);
        pt1.setContext(" aaa", 0, REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setContextSectionIndexEmptySectionName() {
        pt1.addContext(SECTION1, REF1C);
        pt1.setContext("", 0, REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setContextSectionIndexUnknownSectionName() {
        pt1.setContext(SECTION1, 0, REF1C);
    }

    @Test(expected=NullPointerException.class)
    public void setContextSectionIndexNullReference() {
        pt1.addContext(SECTION1, REF1C);
        pt1.setContext(SECTION1, 0, null);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setContextSectionIndexNegative() {
        pt1.addContext(SECTION1, REF1C);
        pt1.setContext(SECTION1, -1, REF1C);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setContextSectionIndexLarge() {
        pt1.addContext(SECTION1, REF1C);
        pt1.setContext(SECTION1, 1, REF1C);
    }

    @Test
    public void setContextSectionIndex() {
        pt1.addContext(SECTION1, REF3C);
        pt1.addContext(SECTION1, REF3C);
        assertFalse(pt1.hasContext(SECTION1, REF1C));
        assertFalse(pt1.hasContext(SECTION1, REF2C));
        pt1.setContext(SECTION1, 1, REF1C);
        pt1.setContext(SECTION1, 0, REF2C);
        assertTrue(pt1.hasContext(SECTION1, REF1C));
        assertTrue(pt1.hasContext(SECTION1, REF2C));
        assertEquals(REF2C, pt1.getContext(SECTION1, 0));
        assertEquals(REF1C, pt1.getContext(SECTION1, 1));
    }

    @Test
    public void setContextSectionIndexDuplicate() {
        pt1.addContext(SECTION1, REF1C);
        pt1.addContext(SECTION1, REF2C);
        assertEquals(2, pt1.getContextCount(SECTION1));
        assertTrue(pt1.hasContext(SECTION1, REF1C));
        assertTrue(pt1.hasContext(SECTION1, REF2C));
        pt1.setContext(SECTION1, 1, REF1C);
        assertEquals(REF1C, pt1.getContext(SECTION1, 0));
        assertEquals(REF1C, pt1.getContext(SECTION1, 1));
    }

    @Test(expected=NullPointerException.class)
    public void removeContextSectionNullSectionName() {
        pt1.removeContext(null, REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeContextSectionNonTrimmedSectionName() {
        pt1.removeContext(" aaa", REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeContextSectionEmptySectionName() {
        pt1.removeContext("", REF1C);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeContextSectionUnknownSectionName() {
        pt1.removeContext(SECTION1, REF1C);
    }

    @Test(expected=NullPointerException.class)
    public void removeContextSectionNullReference() {
        pt1.addContext(SECTION1, REF2C);
        pt1.removeContext(SECTION1, null);
    }

    @Test
    public void removeContextSection() {
        pt1.addContext(SECTION2, REF2C);
        assertEquals(1, pt1.getContextCount(SECTION2));
        assertTrue(pt1.hasSection(SECTION2));
        assertTrue(pt1.removeContext(SECTION2, REF2C));
        assertEquals(0, pt1.getContextCount(SECTION2));
        assertFalse(pt1.hasSection(SECTION2));
    }

    @Test
    public void removeContextSectionDuplicate() {
        pt1.addContext(SECTION2, REF2C);
        pt1.addContext(SECTION2, REF2C);
        assertEquals(2, pt1.getContextCount(SECTION2));
        assertTrue(pt1.hasSection(SECTION2));
        assertTrue(pt1.removeContext(SECTION2, REF2C));
        assertEquals(0, pt1.getContextCount(SECTION2));
        assertFalse(pt1.hasSection(SECTION2));
    }

    @Test(expected=NullPointerException.class)
    public void removeContextSectionIndexNullSectionName() {
        pt1.removeContext(null, 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeContextSectionIndexNonTrimmedSectionName() {
        pt1.removeContext(" aaa", 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeContextSectionIndexEmptySectionName() {
        pt1.removeContext("", 0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeContextSectionIndexUnknownSectionName() {
        pt1.removeContext(SECTION1, 0);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeContextSectionIndexIndexNegative() {
        pt1.addContext(SECTION1, REF2C);
        pt1.removeContext(SECTION1, -1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeContextSectionIndexIndexLarge() {
        pt1.addContext(SECTION1, REF2C);
        pt1.removeContext(SECTION1, 1);
    }

    @Test
    public void removeContextSectionIndex() {
        pt1.addContext(SECTION2, REF2C);
        assertEquals(1, pt1.getContextCount(SECTION2));
        assertTrue(pt1.hasSection(SECTION2));
        assertEquals(REF2C, pt1.removeContext(SECTION2, 0));
        assertEquals(0, pt1.getContextCount(SECTION2));
        assertFalse(pt1.hasSection(SECTION2));
    }

    @Test
    public void removeContextSectionIndexDuplicate() {
        pt1.addContext(SECTION2, REF2C);
        pt1.addContext(SECTION2, REF2C);
        assertEquals(2, pt1.getContextCount(SECTION2));
        assertTrue(pt1.hasSection(SECTION2));
        assertEquals(REF2C, pt1.removeContext(SECTION2, 0));
        assertEquals(1, pt1.getContextCount(SECTION2));
        assertTrue(pt1.hasSection(SECTION2));
        assertEquals(REF2C, pt1.removeContext(SECTION2, 0));
        assertEquals(0, pt1.getContextCount(SECTION2));
        assertFalse(pt1.hasSection(SECTION2));
    }

    @Test(expected=NullPointerException.class)
    public void getContextsSectionNullSectionName() {
        pt1.getContexts(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void getContextsSectionNonTrimmedSectionName() {
        pt1.getContexts(" aaa");
    }

    @Test(expected=IllegalArgumentException.class)
    public void getContextsSectionEmptySectionName() {
        pt1.getContexts("");
    }

    @Test(expected=IllegalArgumentException.class)
    public void getContextsSectionUnknownSectionName() {
        pt1.getContexts(SECTION1);
    }

    @Test
    public void getContextsSection() {
        pt1.addContext(SECTION1, REF1C);
        pt1.addContext(SECTION1, REF2C);
        pt1.addContext(SECTION1, REF3C);
        Iterable<IReference<IContextType>> i = pt1.getContexts(SECTION1);
        assertNotNull(i);
        Iterator<IReference<IContextType>> iter = i.iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        assertEquals(REF1C, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(REF2C, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(REF3C, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void setRequiredNullSectionName() {
        pt1.setSectionRequired(null, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setRequiredNonTrimmedSectionName() {
        pt1.setSectionRequired(" aaa", true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void setRequiredEmptySectionName() {
        pt1.setSectionRequired("", true);
    }

    @Test
    public void setRequiredDoesNotAddSection() {
        pt1.setSectionRequired(SECTION1, true);
        assertFalse(pt1.hasSection(SECTION1));
        assertFalse(pt1.isSectionRequired(SECTION1));
        assertFalse(pt1.getRequiredSections().iterator().hasNext());
    }

    @Test
    public void setRequiredSectionBefore() {
        pt1.setSectionRequired(SECTION1, true);
        pt1.addContext(SECTION1, REF1C);
        assertTrue(pt1.isSectionRequired(SECTION1));
        Iterator<String> iter = pt1.getRequiredSections().iterator();
        assertTrue(iter.hasNext());
        assertEquals(SECTION1, iter.next());
        assertFalse(iter.hasNext());
        pt1.setSectionRequired(SECTION1, false);
        assertTrue(pt1.hasSection(SECTION1));
        assertFalse(pt1.isSectionRequired(SECTION1));
        assertFalse(pt1.getRequiredSections().iterator().hasNext());
        pt1.setSectionRequired(SECTION1, true);
        pt1.removeContext(SECTION1, 0);
        assertFalse(pt1.hasSection(SECTION1));
        assertFalse(pt1.getRequiredSections().iterator().hasNext());
    }

    @Test
    public void setRequiredSectionAfter() {
        pt1.addContext(SECTION1, REF1C);
        assertFalse(pt1.isSectionRequired(SECTION1));
        pt1.setSectionRequired(SECTION1, true);
        assertTrue(pt1.isSectionRequired(SECTION1));
        Iterator<String> iter = pt1.getRequiredSections().iterator();
        assertTrue(iter.hasNext());
        assertEquals(SECTION1, iter.next());
        assertFalse(iter.hasNext());
        pt1.setSectionRequired(SECTION1, false);
        assertTrue(pt1.hasSection(SECTION1));
        assertFalse(pt1.isSectionRequired(SECTION1));
        assertFalse(pt1.getRequiredSections().iterator().hasNext());
        pt1.setSectionRequired(SECTION1, true);
        pt1.removeContext(SECTION1, 0);
        assertFalse(pt1.hasSection(SECTION1));
        assertFalse(pt1.getRequiredSections().iterator().hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void isSectionRequiredNullSectionName() {
        pt1.isSectionRequired(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void isSectionRequiredNonTrimmedSectionName() {
        pt1.isSectionRequired(" aaa");
    }

    @Test(expected=IllegalArgumentException.class)
    public void isSectionRequiredEmptySectionName() {
        pt1.isSectionRequired("");
    }

    @Test
    public void getContextSections() {
        pt1.addContext(SECTION1, REF1C);
        pt1.addContext(SECTION1, REF2C);
        pt1.addContext(SECTION2, REF3C);
        Iterable<IPolicyType.IContextSection> i = pt1.getContextSections();
        assertNotNull(i);
        Iterator<IPolicyType.IContextSection> iter = i.iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        IPolicyType.IContextSection cs1 = iter.next();
        assertNotNull(cs1);
        assertEquals(SECTION1, cs1.getSection());
        assertSame(pt1, cs1.getPolicyType());
        Iterator<IReference<IContextType>> ir = cs1.iterator();
        assertNotNull(ir);
        assertTrue(ir.hasNext());
        assertEquals(REF1C, ir.next());
        assertTrue(ir.hasNext());
        assertEquals(REF2C, ir.next());
        assertFalse(ir.hasNext());
        assertTrue(iter.hasNext());
        IPolicyType.IContextSection cs2 = iter.next();
        assertNotNull(cs2);
        assertEquals(SECTION2, cs2.getSection());
        assertSame(pt1, cs2.getPolicyType());
        ir = cs2.iterator();
        assertNotNull(ir);
        assertTrue(ir.hasNext());
        assertEquals(REF3C, ir.next());
        assertFalse(ir.hasNext());
        assertEquals(cs1, cs1);
        assertFalse(cs1.equals(cs2));
        assertFalse(cs2.equals(cs1));
        assertTrue(cs1.hashCode() != cs2.hashCode());
        assertFalse(cs1.equals(null));
        assertFalse(cs1.equals(123));
    }

    @Test
    public void getSectionNames1() {
        pt1.addContext(SECTION1, REF1C);
        pt1.addContext(SECTION2, REF2C);
        Iterator<String> iter = pt1.getSectionNames().iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        assertEquals(SECTION1, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(SECTION2, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test
    public void getSectionNames2() {
        pt1.addContext(SECTION2, REF2C);
        pt1.addContext(SECTION1, REF1C);
        Iterator<String> iter = pt1.getSectionNames().iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        assertEquals(SECTION2, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(SECTION1, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void addContextNullReference() {
        pt1.addContext(null);
    }

    @Test
    public void addContext() {
        assertEquals(0, pt1.getContextCount());
        assertFalse(pt1.hasContext(REF1C));
        pt1.addContext(REF1C);
        assertEquals(1, pt1.getContextCount());
        assertTrue(pt1.hasContext(REF1C));
    }

    @Test
    public void addContextDuplicate() {
        assertEquals(0, pt1.getContextCount());
        assertFalse(pt1.hasContext(REF1C));
        pt1.addContext(REF1C);
        pt1.addContext(REF1C);
        assertEquals(2, pt1.getContextCount());
        assertTrue(pt1.hasContext(REF1C));
    }

    @Test(expected=NullPointerException.class)
    public void addContextIndexNullReference() {
        pt1.addContext(0, null);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addContextIndexNegative() {
        pt1.addContext(-1, REF1C);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addContextIndexLarge() {
        pt1.addContext(1, REF1C);
    }

    @Test
    public void addContextIndex() {
        assertEquals(0, pt1.getContextCount());
        assertFalse(pt1.hasContext(REF1C));
        assertFalse(pt1.hasContext(REF2C));
        pt1.addContext(0, REF1C);
        pt1.addContext(0, REF2C);
        assertEquals(2, pt1.getContextCount());
        assertTrue(pt1.hasContext(REF1C));
        assertTrue(pt1.hasContext(REF2C));
        assertEquals(REF2C, pt1.getContext(0));
        assertEquals(REF1C, pt1.getContext(1));
    }

    @Test
    public void addContextIndexDuplicate() {
        assertEquals(0, pt1.getContextCount());
        assertFalse(pt1.hasContext(REF1C));
        pt1.addContext(0, REF1C);
        pt1.addContext(0, REF1C);
        assertEquals(2, pt1.getContextCount());
        assertTrue(pt1.hasContext(REF1C));
        assertEquals(REF1C, pt1.getContext(0));
        assertEquals(REF1C, pt1.getContext(1));
    }

    @Test(expected=NullPointerException.class)
    public void addContextsNullReferences() {
        pt1.addContexts(null);
    }

    @Test(expected=NullPointerException.class)
    public void addContextsNullReferencesElement() {
        pt1.addContexts(Collections.singleton(
            (IReference<IContextType>)null)
        );
    }

    @Test
    public void addContexts() {
        assertEquals(0, pt1.getContextCount());
        assertFalse(pt1.hasContext(REF1C));
        pt1.addContexts(Collections.singleton(REF1C));
        assertEquals(1, pt1.getContextCount());
        assertTrue(pt1.hasContext(REF1C));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addContextsDuplicate() {
        assertEquals(0, pt1.getContextCount());
        assertFalse(pt1.hasContext(REF1C));
        pt1.addContexts(Arrays.asList(REF1C, REF1C));
        assertEquals(2, pt1.getContextCount());
        assertTrue(pt1.hasContext(REF1C));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getContextIndexNegative() {
        pt1.addContext(REF1C);
        pt1.getContext(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getContextIndexLarge() {
        pt1.addContext(REF1C);
        pt1.getContext(1);
    }

    @Test
    public void getContext() {
        pt1.addContext(REF1C);
        assertEquals(REF1C, pt1.getContext(0));
    }

    @Test(expected=NullPointerException.class)
    public void setContextIndexNullReference() {
        pt1.addContext(REF1C);
        pt1.setContext(0, null);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setContextIndexNegative() {
        pt1.addContext(REF1C);
        pt1.setContext(-1, REF1C);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setContextIndexLarge() {
        pt1.addContext(REF1C);
        pt1.setContext(1, REF1C);
    }

    @Test
    public void setContextIndex() {
        pt1.addContext(REF3C);
        pt1.addContext(REF3C);
        assertFalse(pt1.hasContext(REF1C));
        assertFalse(pt1.hasContext(REF2C));
        pt1.setContext(1, REF1C);
        pt1.setContext(0, REF2C);
        assertTrue(pt1.hasContext(REF1C));
        assertTrue(pt1.hasContext(REF2C));
        assertEquals(REF2C, pt1.getContext(0));
        assertEquals(REF1C, pt1.getContext(1));
    }

    @Test
    public void setContextIndexDuplicate() {
        pt1.addContext(REF1C);
        pt1.addContext(REF2C);
        assertEquals(2, pt1.getContextCount());
        assertTrue(pt1.hasContext(REF1C));
        assertTrue(pt1.hasContext(REF2C));
        pt1.setContext(1, REF1C);
        assertEquals(REF1C, pt1.getContext(0));
        assertEquals(REF1C, pt1.getContext(1));
    }

    @Test(expected=NullPointerException.class)
    public void removeContextNullReference() {
        pt1.addContext(REF2C);
        pt1.removeContext(null);
    }

    @Test
    public void removeContext() {
        pt1.addContext(REF2C);
        assertEquals(1, pt1.getContextCount());
        assertTrue(pt1.removeContext(REF2C));
        assertEquals(0, pt1.getContextCount());
    }

    @Test
    public void removeContextDuplicate() {
        pt1.addContext(REF2C);
        pt1.addContext(REF2C);
        assertEquals(2, pt1.getContextCount());
        assertTrue(pt1.removeContext(REF2C));
        assertEquals(0, pt1.getContextCount());
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeContextIndexIndexNegative() {
        pt1.addContext(REF2C);
        pt1.removeContext(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeContextIndexIndexLarge() {
        pt1.addContext(REF2C);
        pt1.removeContext(1);
    }

    @Test
    public void removeContextIndex() {
        pt1.addContext(REF2C);
        assertEquals(1, pt1.getContextCount());
        assertEquals(REF2C, pt1.removeContext(0));
        assertEquals(0, pt1.getContextCount());
    }

    @Test
    public void removeContextIndexDuplicate() {
        pt1.addContext(REF2C);
        pt1.addContext(REF2C);
        assertEquals(2, pt1.getContextCount());
        assertEquals(REF2C, pt1.removeContext(0));
        assertEquals(1, pt1.getContextCount());
        assertEquals(REF2C, pt1.removeContext(0));
        assertEquals(0, pt1.getContextCount());
    }

    @Test
    public void getContexts() {
        pt1.addContext(REF1C);
        pt1.addContext(REF2C);
        pt1.addContext(REF3C);
        Iterable<IReference<IContextType>> i = pt1.getContexts();
        assertNotNull(i);
        Iterator<IReference<IContextType>> iter = i.iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        assertEquals(REF1C, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(REF2C, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(REF3C, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void addObligationNullReference() {
        pt1.addObligation(null);
    }

    @Test
    public void addObligationDuplicate() {
        pt1.addObligation(REF1O);
        pt1.addObligation(REF1O);
        assertEquals(2, pt1.getObligationCount());
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF1O, pt1.getObligation(1));
    }

    @Test
    public void addObligationOrder1() {
        pt1.addObligation(REF1O);
        pt1.addObligation(REF2O);
        assertEquals(2, pt1.getObligationCount());
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF2O, pt1.getObligation(1));
    }

    @Test
    public void addObligationOrder2() {
        pt1.addObligation(REF2O);
        pt1.addObligation(REF1O);
        assertEquals(2, pt1.getObligationCount());
        assertEquals(REF2O, pt1.getObligation(0));
        assertEquals(REF1O, pt1.getObligation(1));
    }

    @Test(expected=NullPointerException.class)
    public void addObligationsNullReferenceList() {
        pt1.addObligations(null);
    }

    @Test(expected=NullPointerException.class)
    public void addObligationsNullReferenceElement() {
        pt1.addObligations(Collections.singleton(
            (IReference<IObligationType>)null
        ));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addObligationsDuplicate() {
        pt1.addObligations(
            Arrays.asList(REF1O, REF1O)
        );
        assertEquals(2, pt1.getObligationCount());
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF1O, pt1.getObligation(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addObligationsOrder1() {
        pt1.addObligations(
            Arrays.asList(REF1O, REF2O)
        );
        assertEquals(2, pt1.getObligationCount());
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF2O, pt1.getObligation(1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void addObligationsOrder2() {
        pt1.addObligations(
            Arrays.asList(REF2O, REF1O)
        );
        assertEquals(2, pt1.getObligationCount());
        assertEquals(REF2O, pt1.getObligation(0));
        assertEquals(REF1O, pt1.getObligation(1));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getObligationNegativeIndex() {
        pt1.getObligation(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getObligationLargeIndex() {
        pt1.getObligation(0);
    }

    @Test(expected=NullPointerException.class)
    public void addObligationIndexNullReference() {
        pt1.addObligation(0, null);
    }

    @Test
    public void addObligationIndexDuplicate() {
        pt1.addObligation(0, REF1O);
        pt1.addObligation(0, REF1O);
        assertEquals(2, pt1.getObligationCount());
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF1O, pt1.getObligation(1));
    }

    @Test
    public void addObligationIndex() {
        pt1.addObligation(REF3O);
        pt1.addObligation(0, REF1O);
        pt1.addObligation(1, REF2O);
        assertEquals(3, pt1.getObligationCount());
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF2O, pt1.getObligation(1));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addObligationNegativeIndex() {
        pt1.addObligation(-1, REF1O);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void addObligationLargeIndex() {
        pt1.addObligation(1, REF1O);
    }

    @Test(expected=NullPointerException.class)
    public void setObligationIndexNullReference() {
        pt1.addObligation(REF1O);
        pt1.setObligation(0, null);
    }

    @Test
    public void setObligationIndexDuplicate() {
        pt1.addObligation(REF3O);
        pt1.addObligation(REF3O);
        pt1.setObligation(0, REF1O);
        pt1.setObligation(1, REF1O);
        assertEquals(2, pt1.getObligationCount());
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF1O, pt1.getObligation(1));
    }

    @Test
    public void setObligation() {
        pt1.addObligation(REF3O);
        pt1.addObligation(REF3O);
        pt1.setObligation(0, REF1O);
        pt1.setObligation(1, REF2O);
        assertEquals(2, pt1.getObligationCount());
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF2O, pt1.getObligation(1));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setObligationNegativeIndex() {
        pt1.setObligation(-1, REF1O);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void setObligationLargeIndex() {
        pt1.setObligation(1, REF1O);
    }

    @Test
    public void getObligations() {
        pt1.addObligation(REF1O);
        pt1.addObligation(REF2O);
        pt1.addObligation(REF3O);
        Iterable<IReference<IObligationType>> i = pt1.getObligations();
        assertNotNull(i);
        Iterator<IReference<IObligationType>> iter = i.iterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        assertEquals(REF1O, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(REF2O, iter.next());
        assertTrue(iter.hasNext());
        assertEquals(REF3O, iter.next());
        assertFalse(iter.hasNext());
    }

    @Test(expected=NullPointerException.class)
    public void hasObligationNullReference() {
        pt1.hasObligation(null);
    }

    @Test
    public void hasObligation() {
        assertFalse(pt1.hasObligation(REF1O));
        assertFalse(pt1.hasObligation(REF2O));
        assertFalse(pt1.hasObligation(REF3O));
        pt1.addObligation(REF1O);
        assertTrue(pt1.hasObligation(REF1O));
        assertFalse(pt1.hasObligation(REF2O));
        assertFalse(pt1.hasObligation(REF3O));
        pt1.addObligation(REF2O);
        assertTrue(pt1.hasObligation(REF1O));
        assertTrue(pt1.hasObligation(REF2O));
        assertFalse(pt1.hasObligation(REF3O));
        pt1.addObligation(REF3O);
        assertTrue(pt1.hasObligation(REF1O));
        assertTrue(pt1.hasObligation(REF2O));
        assertTrue(pt1.hasObligation(REF3O));
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeObligationNegativeIndex() {
        pt1.removeObligation(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void removeObligationLargeIndex() {
        pt1.removeObligation(0);
    }

    @Test
    public void removeObligationIndex() {
        pt1.addObligation(REF1O);
        pt1.addObligation(REF2O);
        pt1.addObligation(REF3O);
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF2O, pt1.getObligation(1));
        assertEquals(REF3O, pt1.getObligation(2));
        pt1.removeObligation(1);
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF3O, pt1.getObligation(1));
        pt1.removeObligation(0);
        assertEquals(REF3O, pt1.getObligation(0));
    }

    @Test(expected=NullPointerException.class)
    public void removeObligationNullReference() {
        pt1.removeObligation(null);
    }

    @Test
    public void removeObligation() {
        pt1.addObligation(REF1O);
        pt1.addObligation(REF2O);
        pt1.addObligation(REF3O);
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF2O, pt1.getObligation(1));
        assertEquals(REF3O, pt1.getObligation(2));
        pt1.removeObligation(REF2O);
        assertEquals(REF1O, pt1.getObligation(0));
        assertEquals(REF3O, pt1.getObligation(1));
        pt1.removeObligation(REF1O);
        assertEquals(REF3O, pt1.getObligation(0));
    }

    @Test
    public void accept() {
        final boolean visited[] = new boolean[1];
        pt1.accept(new DefaultDefinitionVisitor() {
            @Override
            public void visitPolicyType(IPolicyType pt) {
                assertSame(pt1, pt);
                visited[0] = true;
            }
        });
        assertTrue(visited[0]);
    }

    @Test
    public void hashCodeWorks() {
        int h1 = pt1.hashCode();
        int h2 = pt2.hashCode();
        assertEquals(h1, pt1.hashCode());
        assertEquals(h2, pt2.hashCode());
        assertTrue(h1 != h2);
    }

    @Test
    public void equalityToSelf() {
        assertEquals(pt1, pt1);
    }

    @Test
    public void equalityToSame() {
        IPolicyType anotherPt1 = new PolicyType(TEST_PATH1);
        assertEquals(pt1, anotherPt1);
        assertEquals(anotherPt1, pt1);
    }

    @Test
    public void inequalityToNull() {
        assertFalse(pt1.equals(null));
    }

    @Test
    public void inequalityToUnknown() {
        assertFalse(pt1.equals(""));
    }

    @Test
    public void inequalityDifferentBase() {
        IPolicyType anotherPt1 = new PolicyType(TEST_PATH1);
        assertEquals(pt1, anotherPt1);
        pt1.setBase(REF2T);
        assertFalse(pt1.equals(anotherPt1));
        assertFalse(anotherPt1.equals(pt1));
    }

    @Test
    public void inequalityDifferentObligations() {
        IPolicyType anotherPt1 = new PolicyType(TEST_PATH1);
        assertEquals(pt1, anotherPt1);
        pt1.addObligation(REF2O);
        assertFalse(pt1.equals(anotherPt1));
        assertFalse(anotherPt1.equals(pt1));
    }

    @Test
    public void inequalityDifferentContexts() {
        IPolicyType anotherPt1 = new PolicyType(TEST_PATH1);
        assertEquals(pt1, anotherPt1);
        pt1.addContext(REF2C);
        assertFalse(pt1.equals(anotherPt1));
        assertFalse(anotherPt1.equals(pt1));
    }

    @Test
    public void inequalityFewerSections() {
        IPolicyType anotherPt1 = new PolicyType(TEST_PATH1);
        assertEquals(pt1, anotherPt1);
        pt1.addContext(SECTION1, REF2C);
        assertFalse(pt1.equals(anotherPt1));
        assertFalse(anotherPt1.equals(pt1));
    }

    @Test
    public void inequalityFewerContextsInSections() {
        PolicyType anotherPt1 = new PolicyType(TEST_PATH1);
        assertEquals(pt1, anotherPt1);
        pt1.addContext(SECTION1, REF1C);
        pt1.addContext(SECTION1, REF2C);
        anotherPt1.addContext(SECTION1, REF2C);
        assertFalse(pt1.equals(anotherPt1));
        assertFalse(anotherPt1.equals(pt1));
    }

    @Test
    public void inequalityDifferentSections() {
        PolicyType anotherPt1 = new PolicyType(TEST_PATH1);
        assertEquals(pt1, anotherPt1);
        pt1.addContext(SECTION1, REF2C);
        anotherPt1.addContext(SECTION2, REF2C);
        assertFalse(pt1.equals(anotherPt1));
        assertFalse(anotherPt1.equals(pt1));
    }

    @Test
    public void inequalityDifferentSectionRequiredAfterAdding() {
        PolicyType anotherPt1 = new PolicyType(TEST_PATH1);
        assertEquals(pt1, anotherPt1);
        pt1.addContext(SECTION1, REF2C);
        anotherPt1.addContext(SECTION1, REF2C);
        assertEquals(pt1, anotherPt1);
        assertEquals(anotherPt1, pt1);
        pt1.setSectionRequired(SECTION1, true);
        assertFalse(pt1.equals(anotherPt1));
        assertFalse(anotherPt1.equals(pt1));
    }

    @Test
    public void inequalityDifferentSectionRequiredBeforeAdding() {
        PolicyType anotherPt1 = new PolicyType(TEST_PATH1);
        assertEquals(pt1, anotherPt1);
        pt1.addContext(SECTION1, REF2C);
        anotherPt1.addContext(SECTION1, REF2C);
        assertEquals(pt1, anotherPt1);
        assertEquals(anotherPt1, pt1);
        pt1.setSectionRequired(SECTION2, true);
        assertFalse(pt1.equals(anotherPt1));
        assertFalse(anotherPt1.equals(pt1));
    }

    @Test
    public void toStringNoBase() {
        assertEquals(
            "policy type "+TEST_PATH1
        ,   pt1.toString()
        );
    }

    @Test
    public void toStringWithBase() {
        IReference<IPolicyType> baseRef =
            IReferenceFactory.DEFAULT.create(pt2.getPath(), IPolicyType.class);
        pt1.setBase(baseRef);
        assertEquals(
            "policy type "+TEST_PATH1+" extends " + baseRef
        ,   pt1.toString()
        );
    }

    @Test
    public void toStringWithObligation() {
        pt1.addObligation(REF2O);
        assertEquals(
            "policy type "+TEST_PATH1+
            "\nwith obligation "+REF2O
        ,   pt1.toString()
        );
    }

    @Test
    public void toStringWithObligations() {
        pt1.addObligation(REF1O);
        pt1.addObligation(REF2O);
        assertEquals(
            "policy type "+TEST_PATH1+
            "\nwith obligation "+REF1O+", "+REF2O
        ,   pt1.toString()
        );
    }

    @Test
    public void toStringWithContext() {
        pt1.addContext(REF2C);
        assertEquals(
            "policy type "+TEST_PATH1+
            "\nwith\n    "+REF2O
        ,   pt1.toString()
        );
    }

    @Test
    public void toStringWithContexts() {
        pt1.addContext(REF1C);
        pt1.addContext(REF2C);
        assertEquals(
            "policy type "+TEST_PATH1+
            "\nwith\n    "+REF1C+"\n    "+REF2C
        ,   pt1.toString()
        );
    }

    @Test
    public void toStringWithSectionOneReference() {
        pt1.addContext(SECTION1, REF1C);
        assertEquals(
            "policy type "+TEST_PATH1+
            "\n    "+SECTION1+" "+REF1C
        ,   pt1.toString()
        );
    }

    @Test
    public void toStringWithSectionTwoReferences() {
        pt1.addContext(SECTION1, REF1C);
        pt1.addContext(SECTION1, REF2C);
        assertEquals(
            "policy type "+TEST_PATH1+
            "\n    "+SECTION1+" "+REF1C+", "+REF2C
        ,   pt1.toString()
        );
    }

    @Test
    public void toStringWithRequiredSection() {
        pt1.addContext(SECTION1, REF1C);
        pt1.setSectionRequired(SECTION1, true);
        assertEquals(
            "policy type "+TEST_PATH1+
            "\n    +"+SECTION1+" "+REF1C
        ,   pt1.toString()
        );
    }

    @Test
    public void toStringWithMultipleSections() {
        pt1.addContext(SECTION1, REF1C);
        pt1.addContext(SECTION2, REF2C);
        assertEquals(
            "policy type "+TEST_PATH1+
            "\n    "+SECTION1+" "+REF1C+
            "\n    "+SECTION2+" "+REF2C
        ,   pt1.toString()
        );
    }

}
