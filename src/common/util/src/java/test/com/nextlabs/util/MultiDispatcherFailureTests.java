package com.nextlabs.util;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/util/src/java/test/com/nextlabs/util/MultiDispatcherFailureTests.java#1 $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Date;

import org.junit.Test;

/**
 * This class tests the MultiDispatcher's ability to handle invalid arguments.
 *
 * @author Sergey Kalinichenko
 */
public class MultiDispatcherFailureTests {

    private interface TestInterface {
        boolean process(Object a, Object b);
    }

    private interface TestInterfaceMultipleMethods {
        Object process(Object a);
        int process1(Object b);
    }

    private interface TestInterfaceZeroArguments {
        Object process();
    }

    private static class TestProcessor {
        public static boolean process(Date lhs, Date rhs) {
            return lhs.equals(rhs);
        }
    }

    private static class WrongReturnTestProcessor {
        public static Object process(Object a, Object b) {
            return null;
        }
    }

    private static class NonStaticMethodsProcessor {
        public boolean process(Object a, Object b) {
            return false;
        }
    }

    private static class PrivateMethodProcessor {
        @SuppressWarnings("unused")
        private static boolean process(Object a, Object b) {
            return false;
        }
    }

    private static class MissingMethodProcessor {
        public static boolean process1(Object a, Object b) {
            return false;
        }
    }

    private static class WrongArgCountProcessor {
        public static boolean process(Object a, Object b, Object c) {
            return false;
        }
    }

    private static class IncompatibleArgumentProcessor {
        public static boolean process(Object a, int b) {
            return false;
        }
    }

    private interface TestInterfaceNumberReturn {
        Number process(Object a);
    }

    private static class TestProcessorIntReturn {
        public static Integer process(Object a) {
            return 0;
        }
    }

    private static class TestProcessorThrowsIAE {
        public static boolean process(Object a, Object b) {
            throw new IllegalArgumentException("test");
        }
    }

    private static class TestProcessorIncompatibleCombinations {
        public static boolean process(String a, Number b) {
            return true;
        }
        public static boolean process(Number a, String b) {
            return false;
        }
    }

    @Test(expected=NullPointerException.class)
    public void nullInterfacesProhibited() {
        MultiDispatcher.create(
            null
        ,   TestProcessor.class
        );
    }

    @Test(expected=NullPointerException.class)
    public void nullProcessorsProhibited() {
        MultiDispatcher.create(
            TestInterfaceMultipleMethods.class
        ,   null
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void multipleMethodsProhibited() {
        MultiDispatcher.create(
            TestInterfaceMultipleMethods.class
        ,   TestProcessor.class
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void zeroArgumentProhibited() {
        MultiDispatcher.create(
            TestInterfaceZeroArguments.class
        ,   TestProcessor.class
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void nonInterfaceProhibited() {
        MultiDispatcher.create(
            TestProcessor.class
        ,   TestProcessor.class
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void wrongReturnProhibited() {
        MultiDispatcher.create(
            TestInterface.class
        ,   WrongReturnTestProcessor.class
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void nonStaticProcessorMethodsProhibited() {
        MultiDispatcher.create(
            TestInterface.class
        ,   NonStaticMethodsProcessor.class
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void emptyProcessorProhibited() {
        MultiDispatcher.create(
            TestInterface.class
        ,   MissingMethodProcessor.class
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void wrongArgumentCountProhibited() {
        MultiDispatcher.create(
            TestInterface.class
        ,   WrongArgCountProcessor.class
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void incompatibleArgumentsProhibited() {
        MultiDispatcher.create(
            TestInterface.class
        ,   IncompatibleArgumentProcessor.class
        );
    }

    @Test(expected=IllegalArgumentException.class)
    public void nonPublicProcessorMethodsProhibited() {
        MultiDispatcher.create(
            TestInterface.class
        ,   PrivateMethodProcessor.class
        );
    }

    @Test
    public void compatibleReturnsAccepted() {
        TestInterfaceNumberReturn p = MultiDispatcher.create(
            TestInterfaceNumberReturn.class
        ,   TestProcessorIntReturn.class
        );
        assertNotNull("MultiDispatcher.create returned null", p);
    }

    @Test
    public void createWorks() {
        TestInterface p = MultiDispatcher.create(
            TestInterface.class
        ,   TestProcessor.class
        );
        assertNotNull("MultiDispatcher.create returned null", p);
        p.process(new Date(1), new Date(1));
    }

    @Test(expected=IllegalArgumentException.class)
    public void exceptionsGetRethrown() {
        TestInterface p = MultiDispatcher.create(
            TestInterface.class
        ,   TestProcessorThrowsIAE.class
        );
        p.process(null, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void incompatibleCallsTrapped() {
        TestInterface p = MultiDispatcher.create(
            TestInterface.class
        ,   TestProcessorIncompatibleCombinations.class
        );
        p.process(1, 1);
    }

    @Test
    public void constructorIsPrivate() throws Exception {
        Constructor<?>[] cc = MultiDispatcher.class.getDeclaredConstructors();
        assertEquals(1, cc.length);
        Constructor<?> c = cc[0];
        assertNotNull(c);
        assertTrue(Modifier.isPrivate(c.getModifiers()));
        c.setAccessible(true);
        c.newInstance(new Object[0]);
    }

    @Test
    public void checkClasses() {
        new IncompatibleArgumentProcessor();
        IncompatibleArgumentProcessor.process(null, 1);
        new MissingMethodProcessor();
        MissingMethodProcessor.process1(null, null);
        new NonStaticMethodsProcessor().process(null, null);
        new PrivateMethodProcessor();
        PrivateMethodProcessor.process(null, null);
        new TestProcessorIncompatibleCombinations();
        TestProcessorIncompatibleCombinations.process(1, null);
        TestProcessorIncompatibleCombinations.process(null, 1);
        new TestProcessorIntReturn();
        TestProcessorIntReturn.process(null);
        new TestProcessorThrowsIAE();
        new WrongArgCountProcessor();
        WrongArgCountProcessor.process(null, null, null);
        new WrongReturnTestProcessor();
        WrongReturnTestProcessor.process(null, null);
        new TestProcessor();
    }

}
