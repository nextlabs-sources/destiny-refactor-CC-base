package com.nextlabs.expression.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/expression/representation/src/java/test/com/nextlabs/expression/representation/DataTypeTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.nextlabs.util.Path;
import com.nextlabs.util.ref.IReference;
import com.nextlabs.util.ref.IReferenceFactory;

/**
 * Tests for the Data Type classes.
 *
 * @author Sergey Kalinichenko
 */
public class DataTypeTests {

    private static final Path TEST_PATH = new Path("data", "type");

    @Test
    public void unknownWorks() {
        assertNotNull(IDataType.UNKNOWN);
        assertFalse(IDataType.UNKNOWN.isNull());
        assertFalse(IDataType.UNKNOWN.isBoolean());
        assertFalse(IDataType.UNKNOWN.isCode());
        assertFalse(IDataType.UNKNOWN.isCsString());
        assertFalse(IDataType.UNKNOWN.isDate());
        assertFalse(IDataType.UNKNOWN.isDouble());
        assertFalse(IDataType.UNKNOWN.isMultivalue());
        assertFalse(IDataType.UNKNOWN.isInteger());
        assertFalse(IDataType.UNKNOWN.isReference());
        assertFalse(IDataType.UNKNOWN.isString());
        assertEquals("<unknown>", IDataType.UNKNOWN.toString());
        IDataType.UNKNOWN.accept(new DefaultDataTypeVisitor());
        final boolean beenThere[] = new boolean[1];
        IDataType.UNKNOWN.accept(new DefaultDataTypeVisitor() {
            @Override
            public void visitUnknown(IDataType type) {
                assertSame(type, IDataType.UNKNOWN);
                beenThere[0] = true;
            }
        });
        assertTrue(beenThere[0]);
    }

    @Test
    public void nullWorks() {
        assertNotNull(IDataType.NULL);
        assertTrue(IDataType.NULL.isNull());
        assertFalse(IDataType.NULL.isBoolean());
        assertFalse(IDataType.NULL.isCode());
        assertFalse(IDataType.NULL.isCsString());
        assertFalse(IDataType.NULL.isDate());
        assertFalse(IDataType.NULL.isDouble());
        assertFalse(IDataType.NULL.isMultivalue());
        assertFalse(IDataType.NULL.isInteger());
        assertFalse(IDataType.NULL.isReference());
        assertFalse(IDataType.NULL.isString());
        assertEquals("null", IDataType.NULL.toString());
        IDataType.NULL.accept(new DefaultDataTypeVisitor());
        final boolean beenThere[] = new boolean[1];
        IDataType.NULL.accept(new DefaultDataTypeVisitor() {
            @Override
            public void visitNull() {
                beenThere[0] = true;
            }
        });
        assertTrue(beenThere[0]);
    }

    @Test
    public void booleanWorks() {
        assertNotNull(IDataType.BOOLEAN);
        assertFalse(IDataType.BOOLEAN.isNull());
        assertTrue(IDataType.BOOLEAN.isBoolean());
        assertFalse(IDataType.BOOLEAN.isCode());
        assertFalse(IDataType.BOOLEAN.isCsString());
        assertFalse(IDataType.BOOLEAN.isDate());
        assertFalse(IDataType.BOOLEAN.isDouble());
        assertFalse(IDataType.BOOLEAN.isMultivalue());
        assertFalse(IDataType.BOOLEAN.isInteger());
        assertFalse(IDataType.BOOLEAN.isReference());
        assertFalse(IDataType.BOOLEAN.isString());
        assertEquals("boolean", IDataType.BOOLEAN.toString());
        IDataType.BOOLEAN.accept(new DefaultDataTypeVisitor());
        final boolean beenThere[] = new boolean[1];
        IDataType.BOOLEAN.accept(new DefaultDataTypeVisitor() {
            @Override
            public void visitBoolean() {
                beenThere[0] = true;
            }
        });
        assertTrue(beenThere[0]);
    }

    @Test
    public void dateWorks() {
        assertNotNull(IDataType.DATE);
        assertFalse(IDataType.DATE.isNull());
        assertFalse(IDataType.DATE.isBoolean());
        assertFalse(IDataType.DATE.isCode());
        assertFalse(IDataType.DATE.isCsString());
        assertTrue(IDataType.DATE.isDate());
        assertFalse(IDataType.DATE.isDouble());
        assertFalse(IDataType.DATE.isMultivalue());
        assertFalse(IDataType.DATE.isInteger());
        assertFalse(IDataType.DATE.isReference());
        assertFalse(IDataType.DATE.isString());
        assertEquals("date", IDataType.DATE.toString());
        IDataType.DATE.accept(new DefaultDataTypeVisitor());
        final boolean beenThere[] = new boolean[1];
        IDataType.DATE.accept(new DefaultDataTypeVisitor() {
            @Override
            public void visitDate() {
                beenThere[0] = true;
            }
        });
        assertTrue(beenThere[0]);
    }

    @Test
    public void doubleWorks() {
        assertNotNull(IDataType.DOUBLE);
        assertFalse(IDataType.DOUBLE.isNull());
        assertFalse(IDataType.DOUBLE.isBoolean());
        assertFalse(IDataType.DOUBLE.isCode());
        assertFalse(IDataType.DOUBLE.isCsString());
        assertFalse(IDataType.DOUBLE.isDate());
        assertFalse(IDataType.DOUBLE.isMultivalue());
        assertFalse(IDataType.DOUBLE.isInteger());
        assertTrue(IDataType.DOUBLE.isDouble());
        assertFalse(IDataType.DOUBLE.isReference());
        assertFalse(IDataType.DOUBLE.isString());
        assertEquals("double", IDataType.DOUBLE.toString());
        IDataType.DOUBLE.accept(new DefaultDataTypeVisitor());
        final boolean beenThere[] = new boolean[1];
        IDataType.DOUBLE.accept(new DefaultDataTypeVisitor() {
            @Override
            public void visitDouble() {
                beenThere[0] = true;
            }
        });
        assertTrue(beenThere[0]);
    }

    @Test
    public void integerWorks() {
        assertNotNull(IDataType.INTEGER);
        assertFalse(IDataType.INTEGER.isNull());
        assertFalse(IDataType.INTEGER.isBoolean());
        assertFalse(IDataType.INTEGER.isCode());
        assertFalse(IDataType.INTEGER.isCsString());
        assertFalse(IDataType.INTEGER.isDate());
        assertFalse(IDataType.INTEGER.isDouble());
        assertFalse(IDataType.INTEGER.isMultivalue());
        assertTrue(IDataType.INTEGER.isInteger());
        assertFalse(IDataType.INTEGER.isReference());
        assertFalse(IDataType.INTEGER.isString());
        assertEquals("integer", IDataType.INTEGER.toString());
        IDataType.INTEGER.accept(new DefaultDataTypeVisitor());
        final boolean beenThere[] = new boolean[1];
        IDataType.INTEGER.accept(new DefaultDataTypeVisitor() {
            @Override
            public void visitInteger() {
                beenThere[0] = true;
            }
        });
        assertTrue(beenThere[0]);
    }

    @Test
    public void stringWorks() {
        assertNotNull(IDataType.STRING);
        assertFalse(IDataType.STRING.isNull());
        assertFalse(IDataType.STRING.isBoolean());
        assertFalse(IDataType.STRING.isCode());
        assertFalse(IDataType.STRING.isCsString());
        assertFalse(IDataType.STRING.isDate());
        assertFalse(IDataType.STRING.isDouble());
        assertFalse(IDataType.STRING.isMultivalue());
        assertFalse(IDataType.STRING.isInteger());
        assertFalse(IDataType.STRING.isReference());
        assertTrue(IDataType.STRING.isString());
        assertEquals("string", IDataType.STRING.toString());
        IDataType.STRING.accept(new DefaultDataTypeVisitor());
        final boolean beenThere[] = new boolean[1];
        IDataType.STRING.accept(new DefaultDataTypeVisitor() {
            @Override
            public void visitString(boolean caseSensitive) {
                beenThere[0] = true;
                assertFalse(caseSensitive);
            }
        });
        assertTrue(beenThere[0]);
    }

    @Test
    public void csStringWorks() {
        assertNotNull(IDataType.CS_STRING);
        assertFalse(IDataType.CS_STRING.isNull());
        assertFalse(IDataType.CS_STRING.isBoolean());
        assertFalse(IDataType.CS_STRING.isCode());
        assertTrue(IDataType.CS_STRING.isCsString());
        assertFalse(IDataType.CS_STRING.isDate());
        assertFalse(IDataType.CS_STRING.isDouble());
        assertFalse(IDataType.CS_STRING.isMultivalue());
        assertFalse(IDataType.CS_STRING.isInteger());
        assertFalse(IDataType.CS_STRING.isReference());
        assertFalse(IDataType.CS_STRING.isString());
        assertEquals("case sensitive string", IDataType.CS_STRING.toString());
        IDataType.CS_STRING.accept(new DefaultDataTypeVisitor());
        final boolean beenThere[] = new boolean[1];
        IDataType.CS_STRING.accept(new DefaultDataTypeVisitor() {
            @Override
            public void visitString(boolean caseSensitive) {
                beenThere[0] = true;
                assertTrue(caseSensitive);
            }
        });
        assertTrue(beenThere[0]);
    }

    @Test(expected=NullPointerException.class)
    public void codeFromNull() {
        DataType.makeCode(null);
    }

    @Test(expected=NullPointerException.class)
    public void codeWithNull() {
        DataType.makeCode(Arrays.asList(new String[] {null}));
    }

    @Test
    public void codeToString() {
        IDataType dt = DataType.makeCode(Arrays.asList(new String[]{"a","b"}));
        assertEquals("code(a, b)", dt.toString());
    }

    @Test
    public void codeWorks() {
        IDataType dt = DataType.makeCode(Arrays.asList(new String[]{"a","b"}));
        assertFalse(dt.isNull());
        assertFalse(dt.isBoolean());
        assertTrue(dt.isCode());
        assertFalse(dt.isCsString());
        assertFalse(dt.isDate());
        assertFalse(IDataType.INTEGER.isDouble());
        assertFalse(dt.isMultivalue());
        assertFalse(dt.isInteger());
        assertFalse(dt.isReference());
        assertFalse(dt.isString());
        assertSame(dt, dt.asCode());
    }

    @Test
    public void codeEqualitySameOrder() {
        IDataType d1 = DataType.makeCode(Arrays.asList(new String[]{"a","b"}));
        IDataType d2 = DataType.makeCode(Arrays.asList(new String[]{"a","b"}));
        IDataType d3 = DataType.makeCode(Arrays.asList(new String[]{"c","d"}));
        IDataType d4 = DataType.makeCode(Arrays.asList(new String[]{"a"}));
        assertEquals(d1, d1);
        assertEquals(d1, d2);
        assertFalse(d1.equals(d3));
        assertFalse(d3.equals(d1));
        assertFalse(d1.equals(d4));
        assertFalse(d4.equals(d1));
        assertFalse(d1.equals(""));
    }

    @Test
    public void codeEqualityDifferentOrder() {
        IDataType d1 = DataType.makeCode(Arrays.asList(new String[]{"a","b"}));
        IDataType d2 = DataType.makeCode(Arrays.asList(new String[]{"b","a"}));
        assertEquals(d1, d2);
        assertEquals(d2, d1);
    }

    @Test
    public void testCodeVisitor() {
        DataType.makeCode(Arrays.asList(new String[] {"a"})).accept(
            new DefaultDataTypeVisitor()
        );
        final boolean beenThere[] = new boolean[1];
        DataType.makeCode(Arrays.asList(new String[] {"a"})).accept(
            new DefaultDataTypeVisitor() {
                /**
                 * @see DefaultDataTypeVisitor#visitCode(ICodeDataType)
                 */
                @Override
                public void visitCode(ICodeDataType codes) {
                    beenThere[0] = true;
                    assertNotNull(codes);
                    Iterator<String> iter = codes.iterator();
                    assertTrue(iter.hasNext());
                    assertEquals("a", iter.next());
                    assertFalse(iter.hasNext());
                }
            }
        );
        assertTrue(beenThere[0]);
    }

    @Test
    public void codeHashCode() {
        IDataType d1 = DataType.makeCode(Arrays.asList(new String[]{"a","b"}));
        IDataType d2 = DataType.makeCode(Arrays.asList(new String[]{"a","b"}));
        assertEquals(d1.hashCode(), d2.hashCode());
        int hashCode1 = d1.hashCode();
        assertEquals(hashCode1, d1.hashCode());
    }

    @Test
    public void codeHashCodeCached() {
        List<String> codes = new ArrayList<String>();
        char[] code = new char[4];
        for ( char a = 'a' ; a != 'z'+1 ; a++) {
            code[0] = a;
            for (char b = (char)(a+1) ; b != 'z'+1 ; b++ ) {
                code[1] = b;
                for (char c = (char)(b+1) ; c != 'z'+1 ; c++ ) {
                    code[2] = c;
                    for (char d = (char)(c+1) ; d != 'z'+1 ; d++ ) {
                        code[3] = d;
                        codes.add(new String(code));
                    }
                }
            }
        }
        long start = System.currentTimeMillis();
        int repeatLimit = 1000;
        for (int i = 0 ; i != repeatLimit ; i++) {
            int hc = 0;
            for ( String s : codes ) {
                hc ^= s.hashCode();
            }
        }
        long notCached = System.currentTimeMillis() - start;
        Object c = DataType.makeCode(codes);
        start = System.currentTimeMillis();
        for (int i = 0 ; i != repeatLimit ; i++) {
            c.hashCode();
        }
        long cached = System.currentTimeMillis() - start;
        assertTrue(""+cached+", "+notCached, 10*cached < notCached);
    }

    @Test
    public void codesIterable() {
        ICodeDataType c =
            DataType.makeCode(Arrays.asList(new String[] {"a","b"}));
        Iterator<String> it = c.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void codeContainment() {
        ICodeDataType c =
            DataType.makeCode(Arrays.asList(new String[] {"a","b"}));
        assertTrue(c.contains("a"));
        assertTrue(c.contains("b"));
        assertFalse(c.contains("c"));
        assertFalse(c.contains("ab"));
        assertFalse(c.contains(""));
    }

    @Test(expected=NullPointerException.class)
    public void multivalueFromNull() {
        DataType.makeMultivalue(null);
    }

    @Test
    public void multivalueToString() {
        IDataType dt = DataType.makeMultivalue(IDataType.STRING);
        assertEquals("multivalued string", dt.toString());
    }

    @Test
    public void multivalueWorks() {
        IDataType dt = DataType.makeMultivalue(IDataType.STRING);
        assertFalse(dt.isNull());
        assertFalse(dt.isBoolean());
        assertFalse(dt.isCode());
        assertFalse(dt.isCsString());
        assertFalse(dt.isDate());
        assertFalse(IDataType.INTEGER.isDouble());
        assertTrue(dt.isMultivalue());
        assertFalse(dt.isInteger());
        assertFalse(dt.isReference());
        assertFalse(dt.isString());
        assertSame(dt, dt.asMultivalue());
    }

    @Test
    public void multivalueEquality() {
        IDataType d1 = DataType.makeMultivalue(IDataType.STRING);
        IDataType d2 = DataType.makeMultivalue(IDataType.CS_STRING);

        assertTrue(d1.equals(d1));
        assertFalse(d1.equals(d2));
    }

    @Test
    public void multivalueInequalityToNull() {
        IDataType d1 = DataType.makeMultivalue(IDataType.STRING);
        assertFalse(d1.equals(null));
    }

    @Test
    public void multivalueInequalityToUnknownType() {
        IDataType d1 = DataType.makeMultivalue(IDataType.STRING);
        assertFalse(d1.equals(""));
    }

    @Test
    public void multivalueHashCode() {
        IDataType d1 = DataType.makeMultivalue(IDataType.STRING);
        IDataType d2 = DataType.makeMultivalue(IDataType.STRING);
        assertEquals(d1.hashCode(), d2.hashCode());
        int hashCode1 = d1.hashCode();
        assertEquals(hashCode1, d1.hashCode());
    }

    @Test
    public void multivalueInnerType() {
        IMultivalueDataType d1 = DataType.makeMultivalue(IDataType.STRING);
        IMultivalueDataType d2 = DataType.makeMultivalue(d1);
        assertEquals(d1.getInnerType(), IDataType.STRING);
        assertEquals(
            ((IMultivalueDataType)d2.getInnerType()).getInnerType()
        ,   IDataType.STRING
        );
        assertEquals("multivalued multivalued string", d2.toString());
    }

    @Test
    public void testMultivalueVisitor() {
        DataType.makeMultivalue(IDataType.STRING).accept(
            new DefaultDataTypeVisitor()
        );
        final boolean beenThere[] = new boolean[1];
        DataType.makeMultivalue(IDataType.STRING).accept(
            new DefaultDataTypeVisitor() {
                /**
                 * @see DefaultDataTypeVisitor#visitCode(ICodeDataType)
                 */
                @Override
                public void visitMultivalue(IMultivalueDataType mvType) {
                    beenThere[0] = true;
                    assertNotNull(mvType);
                    assertEquals("multivalued string", mvType.toString());
                }
            }
        );
        assertTrue(beenThere[0]);
    }

    @Test(expected=NullPointerException.class)
    public void referenceFromNull() {
        DataType.makeReference(null);
    }

    @Test
    public void referenceToString() {
        IDataType dt = DataType.makeReference(ref(123,IExpression.class));
        assertEquals("id 123", dt.toString());
    }

    @Test
    public void referenceWorks() {
        IDataType dt = DataType.makeReference(ref(123,IExpression.class));
        assertFalse(dt.isNull());
        assertFalse(dt.isBoolean());
        assertFalse(dt.isCode());
        assertFalse(dt.isCsString());
        assertFalse(dt.isDate());
        assertFalse(IDataType.INTEGER.isDouble());
        assertFalse(dt.isMultivalue());
        assertFalse(dt.isInteger());
        assertTrue(dt.isReference());
        assertFalse(dt.isString());
        assertSame(dt, dt.asReference());
    }

    @Test
    public void referenceEquality() {
        IDataType d1 = DataType.makeReference(ref(123,IExpression.class));
        IDataType d2 = DataType.makeReference(ref(123,IExpression.class));
        IDataType d3 = DataType.makeReference(ref(TEST_PATH,IExpression.class));
        assertEquals(d1, d1);
        assertEquals(d1, d2);
        assertFalse(d1.equals(d3));
        assertFalse(d3.equals(d1));
        assertFalse(d1.equals(123));
    }

    @Test
    public void referenceHashCode() {
        IDataType d1 = DataType.makeReference(ref(123,IExpression.class));
        IDataType d2 = DataType.makeReference(ref(123,IExpression.class));
        assertEquals(d1.hashCode(), d2.hashCode());
        int hashCode1 = d1.hashCode();
        assertEquals(hashCode1, d1.hashCode());
    }

    @Test
    public void testReferenceVisitor() {
        DataType.makeReference(ref(123,IExpression.class)).accept(
            new DefaultDataTypeVisitor()
        );
        final boolean beenThere[] = new boolean[1];
        DataType.makeReference(ref(123,IExpression.class)).accept(
            new DefaultDataTypeVisitor() {
                /**
                 * @see DefaultDataTypeVisitor#visitCode(ICodeDataType)
                 */
                @Override
                public void visitReference(IReferenceDataType refType) {
                    beenThere[0] = true;
                    assertNotNull(refType);
                    IReference<?> ref = refType.getReferencedContext();
                    assertNotNull(ref);
                    assertFalse(ref.isByPath());
                    assertEquals(123L, ref.getId());
                }
            }
        );
        assertTrue(beenThere[0]);
    }

    @Test
    public void defaultVisitor() {
        new DataType() {}.accept(new DefaultDataTypeVisitor() {});
        final boolean beenThere[] = new boolean[1];
        new DataType() {}.accept(new DefaultDataTypeVisitor() {
            @Override
            public void visitUnknown(IDataType dataType) {
                beenThere[0] = true;
            }
        });
        assertTrue(beenThere[0]);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void nullIsNotCode() {
        IDataType.NULL.asCode();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void nullIsNotReference() {
        IDataType.NULL.asReference();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void nullIsNotMultivalue() {
        IDataType.NULL.asMultivalue();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void booleanIsNotCode() {
        IDataType.BOOLEAN.asCode();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void booleanIsNotReference() {
        IDataType.BOOLEAN.asReference();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void booleanIsNotMultivalue() {
        IDataType.BOOLEAN.asMultivalue();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void doubleIsNotCode() {
        IDataType.DOUBLE.asCode();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void doubleIsNotReference() {
        IDataType.DOUBLE.asReference();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void doubleIsNotMultivalue() {
        IDataType.DOUBLE.asMultivalue();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void integerIsNotCode() {
        IDataType.DOUBLE.asCode();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void integerIsNotReference() {
        IDataType.DOUBLE.asReference();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void integerIsNotMultivalue() {
        IDataType.DOUBLE.asMultivalue();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void dateIsNotCode() {
        IDataType.DATE.asCode();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void dateIsNotReference() {
        IDataType.DATE.asReference();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void dateIsNotMultivalue() {
        IDataType.DATE.asMultivalue();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void stringIsNotCode() {
        IDataType.STRING.asCode();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void stringIsNotReference() {
        IDataType.STRING.asReference();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void stringIsNotMultivalue() {
        IDataType.STRING.asMultivalue();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void csStringIsNotCode() {
        IDataType.CS_STRING.asCode();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void csStringIsNotReference() {
        IDataType.CS_STRING.asReference();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void csStringIsNotMultivalue() {
        IDataType.CS_STRING.asMultivalue();
    }

    private static final <T> IReference<T> ref(Path p, Class<T> type) {
        return IReferenceFactory.DEFAULT.create(p, type);
    }

    private static final <T> IReference<T> ref(long id, Class<T> type) {
        return IReferenceFactory.DEFAULT.create(id, type);
    }

}
