/*
 * Created on Mar 17, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.i18n;

import com.bluejungle.destiny.appframework.i18n.IOptionItemResource;
import com.bluejungle.destiny.appframework.i18n.OptionItemResourceListFactory;
import com.bluejungle.destiny.appframework.i18n.OptionItemResourceListFactory.IOptionItemResourceValueTypeConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * JUnit test for OptionItemResourceListFactor
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/test/com/bluejungle/destiny/appframework/appsecurity/i18n/OptionItemResourceListFactoryTest.java#1 $
 */

public class OptionItemResourceListFactoryTest extends TestCase {

    private static final List EXPECTED_RESULTS = new ArrayList(4);
    static {
        EXPECTED_RESULTS.add(new OptionItemResourceImpl(new Integer(5), "foo"));
        EXPECTED_RESULTS.add(new OptionItemResourceImpl(new Integer(25), "bar"));
        EXPECTED_RESULTS.add(new OptionItemResourceImpl(new Integer(7), "alpha"));
        EXPECTED_RESULTS.add(new OptionItemResourceImpl(new Integer(434), "beta"));
    }

    private static final String BUNDLE_KEY_PREFIX = "bundle_key_prexif.";

    /*
     * Test method for
     * 'com.bluejungle.destiny.appframework.i18n.OptionItemResourceListFactory.getOptionItemResources(Locale)'
     */
    public void testGetOptionItemResources() {
        OptionItemResourceListFactory factoryToTest = new OptionItemResourceListFactory(TestResourceBundle.class.getName(), BUNDLE_KEY_PREFIX, new IOptionItemResourceValueTypeConverter() {
            public Object convert(String valueToConvert) {
                return new Integer(valueToConvert);
            }
        });
        List optionItemResource = factoryToTest.getOptionItemResources(Locale.US);
        
        assertEquals("Ensure number of items returned as expected", EXPECTED_RESULTS.size(), optionItemResource.size());
        
        Iterator optionItemResourceIterator = optionItemResource.iterator();
        for (int i = 0; optionItemResourceIterator.hasNext(); i++) {
            IOptionItemResource nextOptionItemResource = (IOptionItemResource) optionItemResourceIterator.next();
            assertEquals("Ensure item resource as expected", ((IOptionItemResource) EXPECTED_RESULTS.get(i)).getResource(), nextOptionItemResource.getResource());
            assertEquals("Ensure item value as expected", ((IOptionItemResource) EXPECTED_RESULTS.get(i)).getOptionValue(), nextOptionItemResource.getOptionValue());
        }

        // Test caching
        assertTrue("Test local caching", optionItemResource == factoryToTest.getOptionItemResources(Locale.US));

        // Test null pointer
        try {
            factoryToTest.getOptionItemResources(null);
            fail("Providing null Locale should produce NPE");
        } catch (NullPointerException exception) {
        }
    }

    private static class OptionItemResourceImpl implements IOptionItemResource {

        private Object optionValue;
        private String resource;

        /**
         * Create an instance of OptionItemResourceImpl
         * 
         * @param optionValue
         * @param resource
         */
        public OptionItemResourceImpl(Object optionValue, String resource) {
            this.optionValue = optionValue;
            this.resource = resource;
        }

        /**
         * @see com.bluejungle.destiny.appframework.i18n.IOptionItemResource#getOptionValue()
         */
        public Object getOptionValue() {
            return this.optionValue;
        }

        /**
         * @see com.bluejungle.destiny.appframework.i18n.IOptionItemResource#getResource()
         */
        public String getResource() {
            return this.resource;
        }

    }

    /**
     * @author sgoldstein
     */
    public static class TestResourceBundle extends ListResourceBundle {

        private Object[][] contents;

        public TestResourceBundle() {
            contents = new Object[EXPECTED_RESULTS.size()][2];
            Iterator expectedResultsItertor = EXPECTED_RESULTS.iterator();
            for (int i = 0; expectedResultsItertor.hasNext(); i++) {
                IOptionItemResource nextItemResource = (IOptionItemResource) expectedResultsItertor.next();
                Object nextItemValue = nextItemResource.getOptionValue();
                String nextItemResourceString = nextItemResource.getResource();
                String bundleKey = OptionItemResourceListFactoryTest.BUNDLE_KEY_PREFIX + i + "." + nextItemValue;
                Object[] nextResourcePair = { bundleKey, nextItemResourceString };
                this.contents[i] = nextResourcePair;
            }
        }

        /**
         * @see java.util.ListResourceBundle#getContents()
         */
        protected Object[][] getContents() {
            return this.contents;
        }

    }
}
