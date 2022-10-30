package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/MultiDispatcherStressTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Date;

import org.junit.Test;

/**
 * This class verifies that MultiDispatcher performs reasonably
 * by invoking the same method repeatedly for one second
 * and then comparing the result to a count that we expect
 * to be exceeded even on the slowest equipment.
 * Any of these tests failing means one of two things:
 * - The computer on which the tests ran is either too slow or overloaded, or
 * - Incompatible changes were made to the MultiDispatcher.
 *
 * @author Sergey Kalinichenko
 */
public class MultiDispatcherStressTests {

    private interface TestInterface {
        int process(Object a, Object b);
    }

    private static class TestProcessor {
        public static int process(Object a, Object b) {
            return 0;
        }
        public static int process(Number a, Object b) {
            return 1;
        }
        public static int process(Date a, Object b) {
            return 2;
        }
        public static int process(String a, Object b) {
            return 3;
        }
        public static int process(Object a, Number b) {
            return 4;
        }
        public static int process(Object a, Date b) {
            return 5;
        }
        public static int process(Object a, String b) {
            return 6;
        }
        public static int process(Integer a, Object b) {
            return 7;
        }
    }

    @Test
    public void testPerformance() throws Exception {
        new TestProcessor();
        long whenToStop;
        Date d = new Date(1);
        Method process0 = TestProcessor.class.getMethod(
            "process", new Class<?>[] {Object.class, Object.class}
        );
        Method process1 = TestProcessor.class.getMethod(
            "process", new Class<?>[] {Number.class, Object.class}
        );
        Method process2 = TestProcessor.class.getMethod(
            "process", new Class<?>[] {Date.class, Object.class}
        );
        Method process3 = TestProcessor.class.getMethod(
            "process", new Class<?>[] {String.class, Object.class}
        );
        Method process4 = TestProcessor.class.getMethod(
            "process", new Class<?>[] {Object.class, Number.class}
        );
        Method process5 = TestProcessor.class.getMethod(
            "process", new Class<?>[] {Object.class, Date.class}
        );
        Method process6 = TestProcessor.class.getMethod(
            "process", new Class<?>[] {Object.class, String.class}
        );
        Method process7 = TestProcessor.class.getMethod(
            "process", new Class<?>[] {Integer.class, Object.class}
        );

        Object[] arg0 = new Object[] {this, this};
        Object[] arg1 = new Object[] {1f, this};
        Object[] arg2 = new Object[] {d, this};
        Object[] arg3 = new Object[] {"", this};
        Object[] arg4 = new Object[] {this, 1.1};
        Object[] arg5 = new Object[] {this, d};
        Object[] arg6 = new Object[] {this, ""};
        Object[] arg7 = new Object[] {1, this};
        // Time the base case
        whenToStop = System.currentTimeMillis()+1000;
        double baseCount = 0;
        do {
            assertEquals(0, process0.invoke(null, arg0));
            assertEquals(1, process1.invoke(null, arg1));
            assertEquals(2, process2.invoke(null, arg2));
            assertEquals(3, process3.invoke(null, arg3));
            assertEquals(4, process4.invoke(null, arg4));
            assertEquals(5, process5.invoke(null, arg5));
            assertEquals(6, process6.invoke(null, arg6));
            assertEquals(7, process7.invoke(null, arg7));
            baseCount++;
        } while (System.currentTimeMillis() < whenToStop);

        // Time the test case
        TestInterface md = MultiDispatcher.create(
            TestInterface.class
        ,   TestProcessor.class
        );
        whenToStop = System.currentTimeMillis()+1000;
        double count = 0;
        do {
            assertEquals(0, md.process(this, this));
            assertEquals(1, md.process(1f, this));
            assertEquals(2, md.process(d, this));
            assertEquals(3, md.process("", this));
            assertEquals(4, md.process(this, 1.1));
            assertEquals(5, md.process(this, d));
            assertEquals(6, md.process(this, ""));
            assertEquals(7, md.process(1, this));
            count++;
        } while (System.currentTimeMillis() < whenToStop);
        double slowdown = baseCount/count;
        System.err.println(String.format(
            "Base count: %.0f, Reflection count: %.0f, Slowdown, times: %.2f"
        ,   baseCount
        ,   count
        ,   slowdown)
        );
        assertTrue(
            "Slowdown of more than two times detected: "+slowdown
        ,   (slowdown < 2)
        );
    }

}
