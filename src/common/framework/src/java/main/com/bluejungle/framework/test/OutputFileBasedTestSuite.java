/*
 * Created on Sep 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Tools/FileBasedTest/src/java/main/com/bluejungle/testing/filecomp/OutputFileBasedTestSuite.java#2 $
 */
public class OutputFileBasedTestSuite extends TestSuite {

    private final List<Test> fileBasedTests = new LinkedList<Test>();
    private final FileBasedTestDirTree fileBasedTestTree;

    /**
     * Create an instance of OutputFileBasedTestSuite. Non file based test
     * methods will not be run
     * 
     * @param theClass
     * @param name
     */
    public OutputFileBasedTestSuite(Class theClass, String name) {
        this(theClass, name, false);
    }

    /**
     * NOTE: NOT TESTED - This constructor has not been tests with
     * includeNonFileBasedTests = true Create an instance of
     * OutputFileBasedTestSuite
     * 
     * @param theClass
     * @param name
     * @param includeNonFileBasedTests
     *            true if non-file based tests methos should be included; false
     *            otherwise
     */
    public OutputFileBasedTestSuite(Class theClass, String name, boolean includeNonFileBasedTests) {
        setName(name);
        if (includeNonFileBasedTests) {
            addTestSuite(theClass);
        }

        String baseFilePath = buildBaseFilePath(name);
        this.fileBasedTestTree = new FileBasedTestDirTree(baseFilePath);

        try {
            discoverTests(theClass);
        } catch (IllegalArgumentException exception) {
            addTest(warning(exception));
        } catch (InstantiationException exception) {
            addTest(warning(exception));
        } catch (IllegalAccessException exception) {
            addTest(warning(exception));
        } catch (InvocationTargetException exception) {
            addTest(warning(exception));
        } catch (NoSuchMethodException exception) {
            addTest(warning(exception));
        }
    }
    
    private static Test warning(final Exception exception) {
        return new TestCase("warning") {
            protected void runTest() {
                StringWriter writer = new StringWriter();
                writer.append("Failed to discover file based tests:\n");
                exception.printStackTrace(new PrintWriter(writer));
                fail(writer.toString());
            }
        };
    }

    /**
     * Create an instance of OutputFileBasedTest
     * 
     * @param theClass
     */
    public OutputFileBasedTestSuite(Class theClass) {
        this(theClass, theClass.getPackage().getName());
    }

    /**
     * @see junit.framework.Test#run(junit.framework.TestResult)
     */
    public void run(TestResult result) {
        this.fileBasedTestTree.cleanAndPrepareForTest();

        super.run(result);
    }

    /**
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IOException
     * 
     */
    public void generateApprovedFiles() throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException, IOException {
        this.fileBasedTestTree.cleanAndPrepareForTest();

        Iterator fileBasedTestIterator = getFileBasedTests().iterator();
        while (fileBasedTestIterator.hasNext()) {
            OutputFileBasedTest nextTest = (OutputFileBasedTest) fileBasedTestIterator.next();
            nextTest.generateApprovedFile();
        }
    }

    /**
     * @see junit.framework.TestSuite#addTestSuite(java.lang.Class)
     */
    public void addTestSuite(Class testClass) {
        super.addTestSuite(testClass);

        try {
            this.discoverTests(testClass);
        } catch (IllegalArgumentException exception) {
            addTest(warning(exception));
        } catch (InstantiationException exception) {
            addTest(warning(exception));
        } catch (IllegalAccessException exception) {
            addTest(warning(exception));
        } catch (InvocationTargetException exception) {
            addTest(warning(exception));
        } catch (NoSuchMethodException exception) {
            addTest(warning(exception));
        }
    }

    /**
     * @see junit.framework.TestSuite#testAt(int)
     */
    public Test testAt(int index) {
        Test testToReturn = null;
        int standardTestCount = super.testCount();
        if (index < standardTestCount) {
            testToReturn = super.testAt(index);
        } else {
            testToReturn = getFileBasedTests().get(index - standardTestCount);
        }

        return testToReturn;
    }

    /**
     * @see junit.framework.TestSuite#testCount()
     */
    public int testCount() {
        return super.testCount() + this.getFileBasedTests().size();
    }

    /**
     * @see junit.framework.TestSuite#tests()
     */
    public Enumeration<Test> tests() {
        return new JoinedEnumeration<Test>(super.tests(), Collections.enumeration(this.getFileBasedTests()));
    }

    /**
     * Retrieve the fileBasedTests.
     * 
     * @return the fileBasedTests.
     */
    private List<Test> getFileBasedTests() {
        return this.fileBasedTests;
    }

    /**
     * Builds the base file path for all test file directories.
     * 
     * @param testSuiteName
     *            TODO
     * @return The built base file path
     */
    private String buildBaseFilePath(String testSuiteName) {
        String pathToReturn = System.getProperty("src.root.dir");
        if (pathToReturn == null) {
            throw new NullPointerException("Do you forget to set 'src.root.dir'?");
        }
        pathToReturn += File.separator + "test_files" + File.separator;
        // A bit of a hack. Doesn't really belong here
        String subPath = testSuiteName.replaceAll("[.]", "\\" + File.separator);
        pathToReturn += subPath;

        return pathToReturn;
    }

    private void discoverTests(Class testClass) throws IllegalArgumentException,
            InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        Object testObject = getTestConstructor(testClass).newInstance(new Object[] { getName() });

        Method[] allMethods = testClass.getMethods();
        for (int i = 0; i < allMethods.length; i++) {
            Method nextMethod = allMethods[i];
            if (isOutputFileBasedTest(nextMethod)) {
                Test testToAdd = new OutputFileBasedTest(testObject, nextMethod);
                this.fileBasedTests.add(testToAdd);
            }
        }
    }

    /**
     * @param nextMethod
     * @return
     */
    private boolean isOutputFileBasedTest(Method nextMethod) {
        String testMethodName = nextMethod.getName();
        Class[] methodParameterTypes = nextMethod.getParameterTypes();

        boolean valueToReturn = testMethodName.startsWith("test");
        valueToReturn &= ((methodParameterTypes.length == 1) && (methodParameterTypes[0].equals(OutputStream.class)));
        valueToReturn &= (Modifier.isPublic(nextMethod.getModifiers()));

        return valueToReturn;
    }

    private class OutputFileBasedTest extends TestCase {

        private Method testMethod;
        private Object testObject;

        /**
         * Create an instance of OutputFileBasedTest
         * 
         * @param testObject
         * 
         * @param testMethod
         */
        public OutputFileBasedTest(Object testObject, Method testMethod) {
            super(testMethod.getName());
            if (testObject == null) {
                throw new NullPointerException("testObject cannot be null.");
            }

            this.testObject = testObject;
            this.testMethod = testMethod;
        }

        /**
         * @see junit.framework.TestCase#runTest()
         */
        protected void runTest() throws Throwable {
            Method testMethod = getTestMethod();

            OutputStream nextTestOutput = OutputFileBasedTestSuite.this.fileBasedTestTree.createOutputFile(testMethod.getName());
            testMethod.invoke(getTestObject(), new Object[] { nextTestOutput });
            nextTestOutput.close();
            File diffFile = OutputFileBasedTestSuite.this.fileBasedTestTree.runComparison(testMethod.getName());
            if (diffFile.length() > 0) {
                StringBuffer errorMessage = new StringBuffer("File based test failed.  Differences written to, ");
                errorMessage.append(diffFile.getCanonicalPath());
                errorMessage.append(".  The first 100 lines are shown below:\n");
                BufferedReader fileReader = new BufferedReader(new FileReader(diffFile));
                String lineRead = null;
                for (int i = 0; ((i < 100) && ((lineRead = fileReader.readLine()) != null)); i++) {
                    errorMessage.append(lineRead);
                }
                fail(errorMessage.toString());
            }
        }

        /**
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         * @throws IllegalArgumentException
         * @throws IOException
         * 
         */
        public void generateApprovedFile() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
            Method testMethod = getTestMethod();

            OutputStream nextTestOutput = OutputFileBasedTestSuite.this.fileBasedTestTree.createApprovedFile(testMethod.getName());
            testMethod.invoke(getTestObject(), new Object[] { nextTestOutput });
            nextTestOutput.close();
        }

        /**
         * @return
         */
        private Method getTestMethod() {
            return this.testMethod;
        }

        /**
         * Retrieve the testObject.
         * 
         * @return the testObject.
         */
        private Object getTestObject() {
            return this.testObject;
        };
    }

    /**
     * @author sgoldstein
     */
    public class JoinedEnumeration<T> implements Enumeration<T> {

        private Enumeration<T> one;
        private Enumeration<T> two;

        /**
         * Create an instance of JoinedEnumeration
         * 
         * @param one
         * @param two
         */
        public JoinedEnumeration(Enumeration<T> one, Enumeration<T> two) {
            if (one == null) {
                throw new NullPointerException("one cannot be null.");
            }
            if (two == null) {
                throw new NullPointerException("two cannot be null.");
            }

            this.one = one;
            this.two = two;
        }

        /**
         * @see java.util.Enumeration#hasMoreElements()
         */
        public boolean hasMoreElements() {
            return one.hasMoreElements() || two.hasMoreElements();
        }

        /**
         * @see java.util.Enumeration#nextElement()
         */
        public T nextElement() {
            T objectToReturn;

            if (one.hasMoreElements()) {
                objectToReturn = one.nextElement();
            } else {
                objectToReturn = two.nextElement();
            }

            return objectToReturn;
        }

    }
}
