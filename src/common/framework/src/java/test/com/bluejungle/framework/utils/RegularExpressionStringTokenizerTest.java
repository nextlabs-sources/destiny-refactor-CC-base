/*
 * Created on Sep 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import com.bluejungle.framework.utils.RegularExpressionStringTokenizer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/test/com/bluejungle/framework/utils/RegularExpressionStringTokenizerTest.java#1 $
 */

public class RegularExpressionStringTokenizerTest extends TestCase {

    private RegularExpressionStringTokenizer tokenizerToTest;
    private final static List TEST_CASES = new LinkedList();
    static {
        ASingleTest testOne = new ASingleTest();
        testOne.input = "The lazy fox jumped over the silver moon";
        testOne.reDelim = " ";
        testOne.returnDelim = false;
        testOne.expectedResults = new String[] { "The", "lazy", "fox", "jumped", "over", "the", "silver", "moon" };

        TEST_CASES.add(testOne);
        
        ASingleTest testTwo = new ASingleTest();
        testTwo.input = "The 11 lazy 55 fox jumped 23 over 566 the silver 3453455 moon";
        testTwo.reDelim = "[0-9]+";
        testTwo.returnDelim = true;
        testTwo.expectedResults = new String[] { "The ", "11", " lazy ", "55", " fox jumped ", "23", " over ", "566", " the silver ", "3453455", " moon" };

        TEST_CASES.add(testTwo);
    }

    public void testAll() {
        Iterator testCasesIterator = TEST_CASES.iterator();
        while (testCasesIterator.hasNext()) {
            ASingleTest nextItem = (ASingleTest) testCasesIterator.next();

            this.tokenizerToTest = new RegularExpressionStringTokenizer(nextItem.input, nextItem.reDelim, nextItem.returnDelim);
            List tokensFound = new LinkedList();
            while (this.tokenizerToTest.hasNext()) {
                tokensFound.add(this.tokenizerToTest.next());
            }
            String[] tokensFoundAsArray = (String[]) tokensFound.toArray(new String[tokensFound.size()]);
            assertTrue("testAll - Ensure that the tokenized results are as expected", Arrays.equals(nextItem.expectedResults, tokensFoundAsArray));
        }
    }

    private static class ASingleTest {

        private String input;
        private String reDelim;
        public boolean returnDelim;
        private String[] expectedResults;
    }
}
