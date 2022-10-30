package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/MultiDispatcherAccuracyTests.java#1 $
 */

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This class tests MultiDispatcher's ability to dispatch calls to
 * methods based on the dynamic types of the arguments passed in.
 *
 * @author Sergey Kalinichenko
 */
@RunWith(value=Parameterized.class)
@SuiteClasses(value={MultiDispatcherAccuracyTests.class})
public class MultiDispatcherAccuracyTests {

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList( new Object[][] {
            { null, null, true }
        ,   { 1, 1, true }
        ,   { 1, 0, false }
        ,   { 1, 1.0, true }
        ,   { 1.5, 1.5, true }
        ,   { 1.5, 1.6, false }
        ,   { "1", 1, true }
        ,   { 1.5, "1.5", true }
        ,   { "1.5", 1.5, true }
        ,   { 1.5, null, false }
        ,   { null, 1.6, false }
        ,   { "abc", "xyz", false }
        ,   { "", "", true }
        ,   { "abc", "abc", true }
        ,   { "abc", "ABC", false }
        ,   { new Date(0), new Date(1), false }
        ,   { new Date(1), new Date(1), true }
        ,   { new Date(1), 1, true }
        ,   { 1, new Date(1), true }
        ,   { null, new Date(1), false }
        ,   { new Date(1), null, false }
        ,   { getDateFormat().parse("01/02/2003 12:13:14")
            , "1/2/2003 12:13:14", true }
        ,   { getDateFormat().parse("01/02/2003 12:13:15")
            , "1/2/2003 12:13:14", false }
        ,   { "1/2/2003 12:13:14"
            , getDateFormat().parse("01/02/2003 12:13:14"), true }
        ,   { "1/2/2003 12:13:14"
            , getDateFormat().parse("01/02/2003 12:13:15"), false }
        });
    }

    private static DateFormat getDateFormat() {
        return new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    }

    private Object lhs;
    private Object rhs;
    private boolean res;

    private static final TestInterface md =
        MultiDispatcher.create(TestInterface.class, TestProcessor.class);

    public MultiDispatcherAccuracyTests(Object lhs, Object rhs, boolean res) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.res = res;
    }

    private interface TestInterface {
        boolean process(Object a, Object b);
    }

    private static class TestProcessor {

        public static boolean process(Date lhs, Date rhs) {
            return lhs.equals(rhs);
        }
        public static boolean process(Date lhs, Number rhs) {
            return lhs.getTime() == rhs.longValue();
        }
        public static boolean process(Date lhs, Object rhs) {
            return false;
        }
        public static boolean process(Date lhs, String rhs) {
            try {
                return lhs.equals(getDateFormat().parse(rhs));
            } catch (ParseException ignored) {
                return false;
            }
        }
        public static boolean process(Number lhs, Date rhs) {
            return lhs.longValue() == rhs.getTime();
        }
        public static boolean process(Number lhs, Number rhs) {
            return lhs.equals(rhs)
            || lhs.doubleValue() == rhs.doubleValue();
        }
        public static boolean process(Number lhs, Object rhs) {
            return false;
        }
        public static boolean process(Number lhs, String rhs) {
            try {
                return lhs.doubleValue() == Double.parseDouble(rhs);
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        public static boolean process(Object lhs, Date rhs) {
            return false;
        }
        public static boolean process(Object lhs, Number rhs) {
            return false;
        }
        public static boolean process(Object lhs, Object rhs) {
            return (lhs==null && rhs==null)
            || lhs != null && lhs.equals(rhs);
        }
        public static boolean process(Object lhs, String rhs) {
            return false;
        }
        public static boolean process(String lhs, Date rhs) {
            try {
                return rhs.equals(getDateFormat().parse(lhs));
            } catch (ParseException ignored) {
                return false;
            }
        }
        public static boolean process(String lhs, Number rhs) {
            try {
                return rhs.doubleValue() == Double.parseDouble(lhs);
            } catch (NumberFormatException ignored) {
                return false;
            }
        }
        public static boolean process(String lhs, Object rhs) {
            return false;
        }
        public static boolean process(String lhs, String rhs) {
            return lhs.equals(rhs);
        }
    }

    @Test
    public void test() {
        assertEquals(res, md.process(lhs, rhs));
    }
}
