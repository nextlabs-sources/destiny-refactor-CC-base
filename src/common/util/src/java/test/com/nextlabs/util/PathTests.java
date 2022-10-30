package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/PathTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Tests for the Path class.
 *
 * @author Sergey Kalinichenko
 */
public class PathTests {

    private static final String[] SEGMENTS1 = new String[] {
        "Segment1", "segment2", "1 2 3", "SEGMENT 4"
    };

    private static final String[] SEGMENTS2 = new String[] {
        "segment1", "sEgMeNT2", "1 2 3", "segment 4"
    };

    private static final String[] SEGMENTS3 = new String[] {
        "segment1", "sEgMeNT2", "1 2 3"
    };

    private static final String[] SEGMENTS4 = new String[] {
        "segment1", "sEgMeNT2", "0 1 2 3", "segment 4"
    };

    private static final Path p1 = new Path(SEGMENTS1);
    private static final Path p2 = new Path(SEGMENTS2);
    private static final Path p3 = new Path(SEGMENTS3);
    private static final Path p4 = new Path(SEGMENTS4);

    @Test
    public void makeFromStringArray() {
        assertEquals(SEGMENTS1.length, p1.length());
        int i = 0;
        for (String s : p1) {
            assertEquals(SEGMENTS1[i++], s);
        }
    }

    @Test
    public void compareToIsCaseInsensitive() {
        assertEquals(0, p1.compareTo(p2));
    }

    @Test
    public void compareToShorterPath() {
        assertTrue(p3.compareTo(p1) < 0);
    }

    @Test
    public void compareToLongerPath() {
        assertTrue(p1.compareTo(p3) > 0);
    }

    @Test
    public void compareToSameLengthPath() {
        assertTrue(p4.compareTo(p1) < 0);
        assertTrue(p1.compareTo(p4) > 0);
    }

    @Test
    public void hashCodeIsCaseInsensitive() {
        assertEquals(p1.hashCode(), p2.hashCode());
        assertEquals(p1.hashCode(), p1.hashCode());
    }

    @Test
    public void equalsisCaseInsensitive() {
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        assertTrue(p1.equals(p1));
        assertTrue(p2.equals(p2));
        assertTrue(p3.equals(p3));
        assertTrue(p4.equals(p4));
        assertFalse(p1.equals(p3));
        assertFalse(p3.equals(p1));
        assertFalse(p2.equals(p3));
        assertFalse(p3.equals(p2));
        assertFalse(p1.equals(p4));
        assertFalse(p4.equals(p1));
        assertFalse(p2.equals(p4));
        assertFalse(p4.equals(p2));
        assertFalse(p3.equals(p4));
        assertFalse(p3.equals(p4));
    }

    @Test
    public void equalsNull() {
        assertFalse(p1.equals(null));
    }

    @Test
    public void equalsWrongClass() {
        assertFalse(p1.equals(""));
    }

    @Test(expected=NullPointerException.class)
    public void compareToNull() {
        p1.compareTo(null);
    }

    @Test(expected=NullPointerException.class)
    public void createNullSegments() {
        new Path(new String[] {"a", "b", null});
    }

    @Test(expected=IllegalArgumentException.class)
    public void createUntrimmedSegments() {
        new Path(new String[] {" a"});
    }

    @Test(expected=IllegalArgumentException.class)
    public void createEmptySegments() {
        new Path(new String[] {"a", "b", ""});
    }

    @Test(expected=IllegalArgumentException.class)
    public void createEmptyArray() {
        new Path(new String[0]);
    }

    @Test(expected=IllegalArgumentException.class)
    public void createEmptyCollection() {
        new Path(Arrays.asList(new String[0]));
    }

    @Test
    public void createFromCollection() {
        Path p = new Path(Arrays.asList(SEGMENTS1));
        assertEquals(p1, p);
    }

    @Test
    public void toStringWorks() {
        assertEquals("Segment1:segment2:1 2 3:SEGMENT 4", p1.toString());
    }

    @Test(expected=NullPointerException.class)
    public void createFromNullArray() {
        new Path((String[])null);
    }

    @Test(expected=NullPointerException.class)
    public void createFromNullList() {
        new Path((Iterable<String>)null);
    }

    @Test
    public void createFromNameAndParent() {
        Path p = new Path("xyz", p1);
        assertEquals(p1.length()+1, p.length());
        assertTrue(p1.matches(p, p1.length()));
        assertTrue(p.matches(p1, p1.length()));
        assertFalse(p1.matches(p, p.length()));
    }

    @Test(expected=IllegalArgumentException.class)
    public void matchesNegSegments() {
        p1.matches(p2, -1);
    }

    @Test
    public void matchingZeroSegments() {
        assertTrue(p1.matches(p4, 0));
        assertTrue(p1.matches(Path.ROOT, 0));
        assertTrue(Path.ROOT.matches(p1, 0));
    }

    @Test
    public void negativeMatching() {
        assertFalse(p1.matches(p4, 3));
    }

    @Test
    public void getParent() {
        Path p = p1.getParent();
        assertEquals(3, p.length());
        p = p.getParent();
        assertEquals(2, p.length());
        p = p.getParent();
        assertEquals(1, p.length());
        p = p.getParent();
        assertEquals(0, p.length());
        assertSame(Path.ROOT, p);
    }

    @Test
    public void getParentOfRoot() {
        assertSame(Path.ROOT, Path.ROOT.getParent());
    }

    @Test
    public void getName() {
        assertEquals(SEGMENTS1[3], p1.getName());
    }

    @Test(expected=IllegalStateException.class)
    public void getNameOfRoot() {
        Path.ROOT.getName();
    }

    @Test
    public void rename() {
        Path p = p1.rename("xyz");
        assertTrue(p.matches(p1, 3));
        assertEquals("xyz", p.getName());
    }

    @Test
    public void isRoot() {
        assertTrue(Path.ROOT.isRoot());
        assertFalse(p1.isRoot());
        assertFalse(p2.isRoot());
        assertFalse(p3.isRoot());
        assertFalse(p4.isRoot());
    }

    @Test
    public void getWorks() {
        for ( int i = 0 ; i != SEGMENTS1.length ; i++ ) {
            assertEquals(SEGMENTS1[i], p1.get(i));
        }
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getNegativeIndex() {
        p1.get(-1);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getBorderlineIndex() {
        p1.get(4);
    }

    @Test(expected=IndexOutOfBoundsException.class)
    public void getLargeIndex() {
        p1.get(100000000);
    }

    @Test
    public void serializationDeserialization() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(p1);
        oos.writeObject(p2);
        oos.writeObject(p3);
        oos.writeObject(p4);
        oos.writeObject(Path.ROOT);
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Path p1d = (Path)ois.readObject();
        Path p2d = (Path)ois.readObject();
        Path p3d = (Path)ois.readObject();
        Path p4d = (Path)ois.readObject();
        Path root = (Path)ois.readObject();
        assertEquals(p1, p1d);
        assertEquals(p2, p2d);
        assertEquals(p3, p3d);
        assertEquals(p4, p4d);
        assertSame(Path.ROOT, root);
    }

    @Test(expected=IllegalArgumentException.class)
    public void deserializationWithBlanks() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(new Path(new String[] {"a"}));
        // Alter the stream data to replace string "a" with ""
        byte[] data = baos.toByteArray();
        data[44] = 6;
        data[50] = 0;
        data[51] = ObjectStreamConstants.TC_ENDBLOCKDATA;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
    }

    @Test(expected=NullPointerException.class)
    public void writeExternalToNull() throws Exception {
        p1.writeExternal(null);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void removalNotSupported() {
        Iterator<String> iter = p1.iterator();
        iter.remove();
    }

    @Test(expected=NullPointerException.class)
    public void moveNullParent() {
        p1.moveTo(null, 1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void moveExactLevel() {
        p1.moveTo(p4, 4);
    }

    @Test(expected=IllegalArgumentException.class)
    public void moveLargeLevel() {
        p1.moveTo(p4, 400);
    }

    @Test(expected=IllegalArgumentException.class)
    public void moveNegativeLevel() {
        p1.moveTo(p4, -1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void moveToChild() {
        p1.getParent().moveTo(p1, 1);
    }

    @Test(expected=IllegalArgumentException.class)
    public void moveToSelf() {
        p1.moveTo(p1, 0);
    }

    @Test
    public void move1() {
        Path p = p1.moveTo(p3, 0);
        assertEquals(SEGMENTS1.length+SEGMENTS3.length, p.length());
        List<String> tmp = new ArrayList<String>();
        for (String s : p) {
            tmp.add(s);
        }
        for ( int i = 0 ; i != SEGMENTS3.length ; i++) {
            assertEquals(SEGMENTS3[i], tmp.get(i));
        }
        for ( int i = 0 ; i != SEGMENTS1.length ; i++) {
            assertEquals(SEGMENTS1[i], tmp.get(i+SEGMENTS3.length));
        }
    }

    @Test
    public void move2() {
        Path p = p1.moveTo(p3, 2);
        assertEquals(SEGMENTS1.length+SEGMENTS3.length-2, p.length());
        List<String> tmp = new ArrayList<String>();
        for (String s : p) {
            tmp.add(s);
        }
        for ( int i = 0 ; i != SEGMENTS3.length ; i++) {
            assertEquals(SEGMENTS3[i], tmp.get(i));
        }
        for ( int i = 2 ; i != SEGMENTS1.length ; i++) {
            assertEquals(SEGMENTS1[i], tmp.get(i+SEGMENTS3.length-2));
        }
    }

}
