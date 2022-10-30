package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/test/com/nextlabs/language/representation/ContextTypeTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.nextlabs.expression.representation.DataType;
import com.nextlabs.expression.representation.IDataType;
import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the Context Type class.
 *
 * @author Sergey Kalinichenko
 */
public class ContextTypeTests {

    private static final Path TEST_PATH = new Path(
        "context", "test", "path"
    );

    private static final String TEST_ATTR1 = "a1";
    private static final String TEST_ATTR2 = "a2";
    private static final String TEST_ATTR3 = "a3";
    private static final String TEST_ATTR4 = "a4";

    private static IReference<IContextType> TEST_BASE =
        IReferenceFactory.DEFAULT.create(123, IContextType.class);

    private ContextType ct;

    @Before
    public void prepareContext() {
        ct = new ContextType(TEST_PATH);
    }

    @Test
    public void constructor() {
        assertEquals(TEST_PATH, ct.getPath());
    }

    @Test(expected=NullPointerException.class)
    public void constructorNullPath() {
        new ContextType(null);
    }

    @Test
    public void setBaseNotNull() {
        assertNull(ct.getBase());
        assertFalse(ct.hasBase());
        ct.setBase(TEST_BASE);
        assertEquals(TEST_BASE, ct.getBase());
        assertTrue(ct.hasBase());
    }

    @Test
    public void setBaseNull() {
        ct.setBase(TEST_BASE);
        assertEquals(TEST_BASE, ct.getBase());
        assertTrue(ct.hasBase());
        ct.setBase(null);
        assertNull(ct.getBase());
        assertFalse(ct.hasBase());
    }

    @Test(expected=NullPointerException.class)
    public void addAttributeNullName() {
        ct.addAttribute(null, IDataType.BOOLEAN, true);
    }

    @Test(expected=NullPointerException.class)
    public void addAttributeNullType() {
        ct.addAttribute(TEST_ATTR1, null, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addAttributeEmpty() {
        ct.addAttribute("", IDataType.STRING, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addAttributeInvalidName() {
        ct.addAttribute("abc ", IDataType.DATE, true);
    }

    @Test(expected=NullPointerException.class)
    public void addAttributeTemplateNullName() {
        ct.addAttributeTemplate(null, IDataType.BOOLEAN, true);
    }

    @Test(expected=NullPointerException.class)
    public void addAttributeTemplateNullType() {
        ct.addAttributeTemplate(TEST_ATTR1, null, true);
    }

    @Test
    public void addAttributeTemplateEmpty() {
        ct.addAttributeTemplate("", IDataType.STRING, true);
    }

    @Test(expected=IllegalArgumentException.class)
    public void addAttributeTemplateInvalidName() {
        ct.addAttributeTemplate("abc ", IDataType.DATE, true);
    }

    @Test
    public void addAttribute() {
        assertEquals(0, ct.getAttributeCount());
        ct.addAttribute(TEST_ATTR1, IDataType.DATE, true);
        ContextType.IAttribute attr = ct.getAttribute(TEST_ATTR1);
        assertNotNull(attr);
        assertEquals(TEST_ATTR1, attr.getName());
        assertTrue(attr.isUnique());
        assertFalse(attr.isTemplate());
        assertEquals(1, ct.getAttributeCount());
        assertTrue(ct.hasAttribute(TEST_ATTR1));
        assertFalse(ct.hasAttribute(TEST_ATTR2));
    }

    @Test
    public void addAttributeTemplate() {
        assertEquals(0, ct.getAttributeTemplateCount());
        ct.addAttributeTemplate(TEST_ATTR1, IDataType.DATE, true);
        ContextType.IAttribute attr = ct.getAttribute(TEST_ATTR1);
        assertNotNull(attr);
        assertEquals(TEST_ATTR1, attr.getName());
        assertTrue(attr.isUnique());
        assertTrue(attr.isTemplate());
        assertEquals(1, ct.getAttributeTemplateCount());
        ContextType.IAttribute attr1 = ct.getAttribute(TEST_ATTR1+"1");
        assertNotNull(attr1);
        assertEquals(TEST_ATTR1, attr.getName());
        assertTrue(attr.isUnique());
        assertTrue(attr.isTemplate());
        assertTrue(ct.hasAttribute(TEST_ATTR1));
        assertFalse(ct.hasAttribute(TEST_ATTR2));
    }

    @Test(expected=NullPointerException.class)
    public void hasAttributeNullName() {
        ct.hasAttribute(null);
    }

    @Test(expected=NullPointerException.class)
    public void getAttributeNullName() {
        ct.getAttribute(null);
    }

    @Test
    public void getNonExistentAttribute() {
        assertNull(ct.getAttribute(TEST_ATTR1+"_not_there"));
    }

    @Test
    public void removeAttribute() {
        ct.addAttribute(TEST_ATTR1, IDataType.DATE, true);
        assertEquals(1, ct.getAttributeCount());
        ContextType.IAttribute attr = ct.getAttribute(TEST_ATTR1);
        ct.removeAttribute(attr);
        assertEquals(0, ct.getAttributeCount());
    }

    @Test
    public void removeAttributeByName() {
        ContextType.IAttribute origAttr =
            ct.addAttribute(TEST_ATTR1, IDataType.DATE, true);
        assertEquals(1, ct.getAttributeCount());
        ContextType.IAttribute attr =
            ct.removeAttribute(TEST_ATTR1);
        assertEquals(0, ct.getAttributeCount());
        assertSame(origAttr, attr);
    }

    @Test
    public void removeAttributeTemplate() {
        ct.addAttributeTemplate(TEST_ATTR1, IDataType.DATE, true);
        assertEquals(1, ct.getAttributeTemplateCount());
        ContextType.IAttribute attr = ct.getAttribute(TEST_ATTR1);
        ct.removeAttributeTemplate(attr);
        assertEquals(0, ct.getAttributeTemplateCount());
    }

    @Test
    public void removeAttributeTemplateByName() {
        ContextType.IAttribute origAttr =
            ct.addAttributeTemplate(TEST_ATTR1, IDataType.DATE, true);
        assertEquals(1, ct.getAttributeTemplateCount());
        ContextType.IAttribute attr =
            ct.removeAttributeTemplate(TEST_ATTR1);
        assertEquals(0, ct.getAttributeTemplateCount());
        assertSame(origAttr, attr);
    }

    @Test(expected=NullPointerException.class)
    public void removeAttributeTemplateByNameNull() {
        ct.removeAttribute((String)null);
    }

    @Test(expected=NullPointerException.class)
    public void removeAttributeByNameNull() {
        ct.removeAttributeTemplate((String)null);
    }

    @Test(expected=NullPointerException.class)
    public void removeAttributeTemplateNull() {
        ct.removeAttribute((ContextType.IAttribute)null);
    }

    @Test(expected=NullPointerException.class)
    public void removeAttributeNull() {
        ct.removeAttributeTemplate((ContextType.IAttribute)null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeAttributeTemplateWrongKind() {
        ct.addAttributeTemplate(TEST_ATTR1, IDataType.DATE, true);
        ContextType.IAttribute attr = ct.getAttribute(TEST_ATTR1);
        ct.removeAttribute(attr);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeAttributeWrongKind() {
        ct.addAttribute(TEST_ATTR1, IDataType.DATE, true);
        ContextType.IAttribute attr = ct.getAttribute(TEST_ATTR1);
        ct.removeAttributeTemplate(attr);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeAttributeTemplateWrongContext1() {
        ct.addAttributeTemplate(TEST_ATTR1, IDataType.DATE, true);
        ContextType other = new ContextType(TEST_PATH);
        other.addAttribute(TEST_ATTR1, IDataType.DATE, true);
        ContextType.IAttribute attr = other.getAttribute(TEST_ATTR1);
        ct.removeAttributeTemplate(attr);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeAttributeWrongContext1() {
        ct.addAttribute(TEST_ATTR1, IDataType.DATE, true);
        ContextType other = new ContextType(TEST_PATH);
        other.addAttribute(TEST_ATTR1, IDataType.DATE, false);
        ContextType.IAttribute attr = other.getAttribute(TEST_ATTR1);
        ct.removeAttribute(attr);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeAttributeTemplateWrongContext2() {
        ct.addAttributeTemplate(TEST_ATTR1, IDataType.DATE, true);
        ContextType other = new ContextType(TEST_PATH);
        other.addAttributeTemplate(TEST_ATTR2, IDataType.DATE, true);
        ContextType.IAttribute attr = other.getAttribute(TEST_ATTR2);
        ct.removeAttributeTemplate(attr);
    }

    @Test(expected=IllegalArgumentException.class)
    public void removeAttributeWrongContext2() {
        ct.addAttribute(TEST_ATTR1, IDataType.DATE, true);
        ContextType other = new ContextType(TEST_PATH);
        other.addAttribute(TEST_ATTR2, IDataType.DATE, true);
        ContextType.IAttribute attr = other.getAttribute(TEST_ATTR2);
        ct.removeAttribute(attr);
    }

    @Test
    public void toStringWorks() {
        ct.setBase(TEST_BASE);
        ct.addAttribute(
            TEST_ATTR1
        ,   DataType.makeMultivalue(IDataType.BOOLEAN)
        ,   false
        );
        ct.addAttribute(TEST_ATTR2, IDataType.DATE, false);
        ct.addAttributeTemplate(
            TEST_ATTR3
        ,   DataType.makeMultivalue(IDataType.STRING)
        ,   false
        );
        ct.addAttributeTemplate(TEST_ATTR4, IDataType.DOUBLE, true);
        assertEquals("context "+TEST_PATH+" : "+TEST_BASE+" {\n"
        +"    "+TEST_ATTR1+" : multivalued boolean\n"
        +"    "+TEST_ATTR2+" : date\n"
        +"    "+TEST_ATTR3+"* : multivalued string\n"
        +"    "+TEST_ATTR4+"*@ : double\n}"
        , ct.toString());
    }

    @Test
    public void equalsAndHashCodeOfAttributes() {
        ct.setBase(TEST_BASE);
        ct.addAttribute(
            TEST_ATTR1
        ,   DataType.makeMultivalue(IDataType.BOOLEAN)
        ,   false
        );
        ct.addAttribute(TEST_ATTR2, IDataType.DATE, false);
        ct.addAttributeTemplate(
            TEST_ATTR3
        ,   DataType.makeMultivalue(IDataType.STRING)
        ,   false
        );
        ct.addAttributeTemplate(TEST_ATTR4, IDataType.DOUBLE, true);
        Object[] attr = new Object[] {
            ct.getAttribute(TEST_ATTR1)
        ,   ct.getAttribute(TEST_ATTR2)
        ,   ct.getAttribute(TEST_ATTR3)
        ,   ct.getAttribute(TEST_ATTR4)
        };
        int eqCount = 0;
        for ( int i = 0 ; i != attr.length ; i++ ) {
            for ( int j = i ; j != attr.length ; j++) {
                if (attr[i].equals(attr[j])) {
                    eqCount++;
                    assertEquals(attr[i].hashCode(), attr[j].hashCode());
                }
            }
        }
        assertEquals(attr.length, eqCount);
        assertFalse(attr[0].equals(null));
    }

    @Test
    public void accept() {
        final boolean visited[] = new boolean[1];
        ct.accept(new DefaultDefinitionVisitor() {
            @Override
            public void visitContextType(IContextType contextType) {
                assertSame(ct, contextType);
                visited[0] = true;
            }
        });
        assertTrue(visited[0]);
    }

    @Test
    public void equalityEmpty() {
        IContextType ct1 = new ContextType(TEST_PATH);
        IContextType ct2 = new ContextType(TEST_PATH);
        assertEquals(ct1, ct2);
        assertEquals(ct2, ct1);
        assertEquals(ct1, ct1);
        assertEquals(ct2, ct2);
    }

    @Test
    public void inequalityOneSideEmpty() {
        IContextType ct1 = new ContextType(TEST_PATH);
        ContextType ct2 = new ContextType(TEST_PATH);
        ct2.addAttribute(TEST_ATTR1, IDataType.BOOLEAN, false);
        assertEquals(ct1, ct1);
        assertEquals(ct2, ct2);
        assertFalse(ct1.equals(ct2));
        assertFalse(ct2.equals(ct1));
    }

    @Test
    public void inequalityOneSideEmptyBase() {
        ContextType ct1 = new ContextType(TEST_PATH);
        ct1.setBase(TEST_BASE);
        IContextType ct2 = new ContextType(TEST_PATH);
        assertEquals(ct1, ct1);
        assertEquals(ct2, ct2);
        assertFalse(ct1.equals(ct2));
        assertFalse(ct2.equals(ct1));
    }

    @Test
    public void equalityWithBases() {
        ContextType ct1 = new ContextType(TEST_PATH);
        ct1.setBase(TEST_BASE);
        ContextType ct2 = new ContextType(TEST_PATH);
        ct2.setBase(TEST_BASE);
        assertEquals(ct1, ct1);
        assertEquals(ct2, ct2);
        assertEquals(ct1, ct2);
        assertEquals(ct2, ct1);
        assertEquals(ct1.hashCode(), ct2.hashCode());
    }

    @Test
    public void inequalityWithUnknown() {
        assertFalse(ct.equals(""));
    }

}
