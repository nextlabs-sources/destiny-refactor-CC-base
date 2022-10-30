package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/AbstractDefinitionTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.nextlabs.util.Path;

/**
 * Tests for the Abstract Definition class.
 *
 * @author Sergey Kalinichenko
 */
public class AbstractDefinitionTests {

    /**
     * The class being tested is abstract - make a derived class for testing.
     */
    private static class TestClass extends AbstractDefinition<TestClass> {
        protected TestClass(Path path) {
            super(path);
        }
        public void accept(IDefinitionVisitor visitor) {
        }
    }

    private static final Path TEST_PATH1 = new Path(
        "context", "test", "path", "one"
    );

    private static final Path TEST_PATH2 = new Path(
        "context", "test", "path", "two"
    );

    private static final String TEST_DESCRIPTION1 = "asdfghjkl";

    private static final String TEST_DESCRIPTION2 = "qwertyuiop";

    @Test
    public void constructor() {
        AbstractDefinition<TestClass> d = new TestClass(TEST_PATH1);
        assertEquals(TEST_PATH1, d.getPath());
    }

    @Test(expected=NullPointerException.class)
    public void constructorNullPath() {
        new TestClass(null);
    }

    @Test
    public void setPath() {
        AbstractDefinition<TestClass> d = new TestClass(TEST_PATH1);
        assertEquals(TEST_PATH1, d.getPath());
        d.setPath(TEST_PATH2);
        assertEquals(TEST_PATH2, d.getPath());
    }

    @Test(expected=NullPointerException.class)
    public void setNullPath() {
        new TestClass(TEST_PATH1).setPath(null);
    }

    @Test
    public void initialDescription() {
        AbstractDefinition<TestClass> d = new TestClass(TEST_PATH1);
        assertNull(d.getDescription());
        assertFalse(d.hasDescription());
    }

    @Test
    public void setDescription() {
        AbstractDefinition<TestClass> d = new TestClass(TEST_PATH1);
        d.setDescription(TEST_DESCRIPTION1);
        assertEquals(TEST_DESCRIPTION1, d.getDescription());
    }

    @Test
    public void replaceDescription() {
        AbstractDefinition<TestClass> d = new TestClass(TEST_PATH1);
        d.setDescription(TEST_DESCRIPTION1);
        assertTrue(d.hasDescription());
        assertEquals(TEST_DESCRIPTION1, d.getDescription());
        d.setDescription(TEST_DESCRIPTION2);
        assertEquals(TEST_DESCRIPTION2, d.getDescription());
        assertTrue(d.hasDescription());
    }

    @Test
    public void setNullDescription() {
        AbstractDefinition<TestClass> d = new TestClass(TEST_PATH1);
        d.setDescription(TEST_DESCRIPTION1);
        assertEquals(TEST_DESCRIPTION1, d.getDescription());
        assertTrue(d.hasDescription());
        d.setDescription(null);
        assertFalse(d.hasDescription());
        assertNull(d.getDescription());
    }

    @Test
    public void hashCodeWorks() {
        AbstractDefinition<TestClass> d1 = new TestClass(TEST_PATH1);
        AbstractDefinition<TestClass> d2 = new TestClass(TEST_PATH1);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    public void equalNullDescription() {
        AbstractDefinition<TestClass> d1 = new TestClass(TEST_PATH1);
        AbstractDefinition<TestClass> d2 = new TestClass(TEST_PATH1);
        assertEquals(d1, d2);
        assertEquals(d2, d1);
    }

    @Test
    public void equalSameDescription() {
        AbstractDefinition<TestClass> d1 = new TestClass(TEST_PATH1);
        AbstractDefinition<TestClass> d2 = new TestClass(TEST_PATH1);
        d1.setDescription(TEST_DESCRIPTION1);
        d2.setDescription(TEST_DESCRIPTION1);
        assertEquals(d1, d2);
        assertEquals(d2, d1);
    }

    @Test
    public void notEqualDifferentDescription() {
        AbstractDefinition<TestClass> d1 = new TestClass(TEST_PATH1);
        AbstractDefinition<TestClass> d2 = new TestClass(TEST_PATH1);
        d1.setDescription(TEST_DESCRIPTION1);
        d2.setDescription(TEST_DESCRIPTION2);
        assertFalse(d1.equals(d2));
        assertFalse(d2.equals(d1));
    }

    @Test
    public void notEqualNullDescription() {
        AbstractDefinition<TestClass> d1 = new TestClass(TEST_PATH1);
        AbstractDefinition<TestClass> d2 = new TestClass(TEST_PATH1);
        d2.setDescription(TEST_DESCRIPTION2);
        assertFalse(d1.equals(d2));
        assertFalse(d2.equals(d1));
    }

    @Test
    public void notEqualToNull() {
        AbstractDefinition<TestClass> d1 = new TestClass(TEST_PATH1);
        assertFalse(d1.equals(null));
    }

    @Test
    public void notEqualToUnknown() {
        AbstractDefinition<TestClass> d1 = new TestClass(TEST_PATH1);
        assertFalse(d1.equals(""));
    }

}
