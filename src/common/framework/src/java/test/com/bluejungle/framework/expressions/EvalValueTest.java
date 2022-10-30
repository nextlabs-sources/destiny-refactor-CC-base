/*
 * Created on May 17, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.expressions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import junit.framework.TestCase;

import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IMultivalue;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.framework.expressions.ValueType;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/expressions/EvalValueTest.java#1 $:
 */

public class EvalValueTest extends TestCase {

    /*
     * Class under test for boolean equals(Object)
     */
    public final void testEqualsHashCodeObject() {
        String str = "foo";
        IEvalValue str1 = EvalValue.build(str);
        IEvalValue str2 = EvalValue.build(str);
        assertEquals("foo should be equal to foo", str1, str2);
        int hc1 = str1.hashCode();
        int hc2 = str2.hashCode();
        assertEquals("foo hashcodes should be equal", hc1, hc2);
        
        IEvalValue str3 = EvalValue.build("bar");
        assertFalse("foo should not be equal to bar", str1.equals(str3));
        
        long lng = 3;
        IEvalValue lng1 = EvalValue.build(lng);
        IEvalValue lng2 = EvalValue.build(lng);
        assertEquals("3 should be equal to 3", lng1, lng2);
        hc1 = lng1.hashCode();
        hc2 = lng2.hashCode();
        assertEquals("hashcodes of 3 should be equal", hc1, hc2);
        
        IEvalValue lng3 = EvalValue.build(4);
        assertFalse("3 should not be equal to 4", lng1.equals(lng3));
        
        Date date = new Date();
        IEvalValue date1 = EvalValue.build(date);
        IEvalValue date2 = EvalValue.build(date);
        assertEquals("current dates should be equal", date1, date2);
        hc1 = date1.hashCode();
        hc2 = date2.hashCode();
        assertEquals("hascodes of current date should be equal", hc1, hc2);
        
        date.setTime(0);
        IEvalValue date3 = EvalValue.build(date);
        assertFalse("current date should not be equal 0", date1.equals(date3));
        
        assertFalse("string should not be equal to long", str1.equals(lng1));
        assertFalse("string should not be equal to date", str1.equals(date1));
        assertFalse("string should not be equal to NULL", str1.equals(IEvalValue.NULL));
        
        assertFalse("long should not be equal to date", lng1.equals(date1));
        assertFalse("long should not be equal to NULL", lng1.equals(IEvalValue.NULL));
        
        assertFalse("date should not be equal to NULL", date1.equals(IEvalValue.NULL));
    }

    public void testStringMultivalue() {
        IMultivalue a = Multivalue.create(new String[] {"a","b","c"});
        IMultivalue b = Multivalue.create(new String[] {"a","b","c"});
        IMultivalue c = Multivalue.create(new String[] {"x","y","z"});
        assertSame("Multivalues created of string arrays should have a string element type.", ValueType.STRING, a.getType());
        assertTrue("multivals are equal to themselves", a.equals(a));
        assertTrue("multivals with identical content are equal to each other", a.equals(b));
        assertTrue("multivals with identical content have identical hash codes", a.hashCode()== b.hashCode());
        assertFalse("multivals with different content are not equal to each other", a.equals(c));
        assertTrue("Multivalues contain values from which they are created", a.includes(EvalValue.build("a")));
        assertFalse("Multivalues do not contain values other than these from which they are created", a.includes(EvalValue.build("w")));
        IEvalValue ea = EvalValue.build(a);
        IEvalValue eb = EvalValue.build(b);
        IEvalValue ec = EvalValue.build(c);
        assertTrue("values with multivals are equal to themselves", ea.equals(ea));
        assertTrue("values with multivals with identical content are equal to each other", ea.equals(eb));
        assertTrue("values with multivals with identical content have identical hash codes", ea.hashCode()== eb.hashCode());
        assertFalse("values with multivals with different content are not equal to each other", ea.equals(ec));
    }

    public void testLongMultivalue() {
        IMultivalue a = Multivalue.create(new Long[] {new Long(1),new Long(2),new Long(3)});
        IMultivalue b = Multivalue.create(new Long[] {new Long(1),new Long(2),new Long(3)});
        IMultivalue c = Multivalue.create(new Long[] {new Long(10),new Long(20),new Long(30)});
        assertSame("Multivalues created of Long arrays should have a string element type.", ValueType.LONG, a.getType());
        assertTrue("multivals are equal to themselves", a.equals(a));
        assertTrue("multivals with identical content are equal to each other", a.equals(b));
        assertTrue("multivals with identical content have identical hash codes", a.hashCode()== b.hashCode());
        assertFalse("multivals with different content are not equal to each other", a.equals(c));
        assertTrue("Multivalues contain values from which they are created", a.includes(EvalValue.build(1)));
        assertFalse("Multivalues do not contain values other than these from which they are created", a.includes(EvalValue.build(98765)));
        IEvalValue ea = EvalValue.build(a);
        IEvalValue eb = EvalValue.build(b);
        IEvalValue ec = EvalValue.build(c);
        assertTrue("values with multivals are equal to themselves", ea.equals(ea));
        assertTrue("values with multivals with identical content are equal to each other", ea.equals(eb));
        assertTrue("values with multivals with identical content have identical hash codes", ea.hashCode()== eb.hashCode());
        assertFalse("values with multivals with different content are not equal to each other", ea.equals(ec));
    }

    public void testDateMultivalue() throws ParseException {
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        IMultivalue a = Multivalue.create(new Date[] {df.parse("1/1/2007"),df.parse("2/1/2007"),df.parse("3/1/2007")});
        IMultivalue b = Multivalue.create(new Date[] {df.parse("1/1/2007"),df.parse("2/1/2007"),df.parse("3/1/2007")});
        IMultivalue c = Multivalue.create(new Date[] {df.parse("1/10/2007"),df.parse("1/11/2007"),df.parse("1/12/2007")});
        assertSame("Multivalues created of Date arrays should have a string element type.", ValueType.DATE, a.getType());
        assertTrue("multivals are equal to themselves", a.equals(a));
        assertTrue("multivals with identical content are equal to each other", a.equals(b));
        assertTrue("multivals with identical content have identical hash codes", a.hashCode()== b.hashCode());
        assertFalse("multivals with different content are not equal to each other", a.equals(c));
        assertTrue("Multivalues contain values from which they are created", a.includes(EvalValue.build(df.parse("1/1/2007"))));
        assertFalse("Multivalues do not contain values other than these from which they are created", a.includes(EvalValue.build(df.parse("1/1/1987"))));
        assertFalse("Multivalues do not contain values of types other than their own", a.includes(EvalValue.build(df.parse("1/1/2007").getTime())));
        IEvalValue ea = EvalValue.build(a);
        IEvalValue eb = EvalValue.build(b);
        IEvalValue ec = EvalValue.build(c);
        assertTrue("values with multivals are equal to themselves", ea.equals(ea));
        assertTrue("values with multivals with identical content are equal to each other", ea.equals(eb));
        assertTrue("values with multivals with identical content have identical hash codes", ea.hashCode()== eb.hashCode());
        assertFalse("values with multivals with different content are not equal to each other", ea.equals(ec));
    }

    public void testEmptyMultivalue() {
        IMultivalue a = Multivalue.create(new String[0]);
        assertSame(IMultivalue.EMPTY, a);
        IMultivalue b = Multivalue.create(Arrays.asList(new String[0]));
        assertSame(IMultivalue.EMPTY, b);
    }

    public void testConvertedMultivalue() {
        IMultivalue a = Multivalue.create(new Long[] {new Long(1),new Long(2),new Long(3)});
        IMultivalue b = Multivalue.create(new String[] {"1", "2", "3"}, ValueType.LONG);
        assertTrue("multivals with identical content are equal to each other", a.equals(b));
    }

}
