/*
 * Created on Mar 16, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.i18n;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Factory for list of
 * {@see com.bluejungle.destiny.appframework.i18n.IOptionItemResource}
 * instances.
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/main/com/bluejungle/destiny/appframework/i18n/OptionItemResourceListFactory.java#1 $
 */

public class OptionItemResourceListFactory {

    private static final Map CACHED_OPTION_ITEM_LISTS = new HashMap();
    private static final String INDEX_VALUE_RESOURCE_BUNDLE_KEY_DELIMETER_REGEXP = "\\.";
    private static final IOptionItemResourceValueTypeConverter NO_OP_RESOURCE_MAP_KEY_CONVERTER = new IOptionItemResourceValueTypeConverter() {

        /**
         * @see com.bluejungle.destiny.appframework.i18n.IOptionItemResourceList.IOptionItemResourceValueTypeConverter#convert(java.lang.String)
         */
        public Object convert(String valueToConvert) {
            return valueToConvert;
        }
    };

    private String resourceBundleName;
    private String resourceMapBundleKeyPrefix;
    private IOptionItemResourceValueTypeConverter resourceMapValueConverter;

    /**
     * Create an instance of OptionItemResourceListFactory to retrieve a list of
     * {@see com.bluejungle.destiny.appframework.i18n.IOptionItemResource}
     * instances from the bundle with the specified name. The option items
     * should be specific in the bundle as follows: <br />
     * <br />
     * resourceMapBundleKeyPrefix + {index} + "." + {value} = {label} <br />
     * <br />
     * where "index" specifies an order in the option item list, "value" the
     * value of the option item, and "label", the string to display in the UI as
     * part of the option item list menu
     * 
     * @param resourceBundleName
     * @param resourceMapBundleKeyPrefix
     */
    public OptionItemResourceListFactory(String resourceBundleName, String resourceMapBundleKeyPrefix) {
        this(resourceBundleName, resourceMapBundleKeyPrefix, NO_OP_RESOURCE_MAP_KEY_CONVERTER);
    }

    /**
     * Create an instance of OptionItemResourceListFactory to retrieve a list of
     * {@see com.bluejungle.destiny.appframework.i18n.IOptionItemResource}
     * instances from the bundle with the specified name. The option items
     * should be specific in the bundle as follows: <br />
     * <br />
     * resourceMapBundleKeyPrefix + {index} + "." + {value} = {label} <br />
     * <br />
     * where "index" specifies an order in the option item list, "value" the
     * value of the option item, and "label", the string to display in the UI as
     * part of the option item list menu <br />
     * <br />
     * The final parameter in the constructor specifies a type converter for the
     * value
     * 
     * @param resourceBundleName
     * @param resourceMapBundleKeyPrefix
     */
    public OptionItemResourceListFactory(String resourceBundleName, String resourceMapBundleKeyPrefix, IOptionItemResourceValueTypeConverter resourceMapValueConverter) {
        if (resourceBundleName == null) {
            throw new NullPointerException("resourceBundleName cannot be null.");
        }

        if (resourceMapBundleKeyPrefix == null) {
            throw new NullPointerException("resourceMapBundleKeyPrefix cannot be null.");
        }

        if (resourceMapValueConverter == null) {
            throw new NullPointerException("resourceMapValueConverter cannot be null.");
        }

        this.resourceBundleName = resourceBundleName;
        this.resourceMapBundleKeyPrefix = resourceMapBundleKeyPrefix;
        this.resourceMapValueConverter = resourceMapValueConverter;
    }

    // FIX ME - After 1.5 add template
    /**
     * Retrieve an Option Item List for the specified locale
     */
    public List getOptionItemResources(Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale cannot be null.");
        }

        List optionItemResourcesToReturn = null;
        if (CACHED_OPTION_ITEM_LISTS.containsKey(locale)) {
            optionItemResourcesToReturn = (List) CACHED_OPTION_ITEM_LISTS.get(locale);
        } else {
            optionItemResourcesToReturn = buildOptionItemResourceList(locale);
            CACHED_OPTION_ITEM_LISTS.put(locale, optionItemResourcesToReturn);
        }

        return optionItemResourcesToReturn;
    }

    /**
     * Build the option item list from the associated resource bundle
     * 
     * @param resourceBundleLocale
     * @return the built option item list
     */
    private List buildOptionItemResourceList(Locale resourceBundleLocale) {
        if (resourceBundleLocale == null) {
            throw new NullPointerException("resourceBundleLocale cannot be null.");
        }

        SortedMap sortedOptionItems = new TreeMap();

        ResourceBundle resourceBundle = ResourceBundle.getBundle(this.resourceBundleName, resourceBundleLocale);
        Enumeration resourceBundleKeyEnumeration = resourceBundle.getKeys();
        while (resourceBundleKeyEnumeration.hasMoreElements()) {
            String nextKey = (String) resourceBundleKeyEnumeration.nextElement();
            if (nextKey.startsWith(this.resourceMapBundleKeyPrefix)) {
                String rawOptionItemValue = nextKey.substring(this.resourceMapBundleKeyPrefix.length());
                String[] indexAndValue = rawOptionItemValue.split(INDEX_VALUE_RESOURCE_BUNDLE_KEY_DELIMETER_REGEXP);

                Integer index = new Integer(indexAndValue[0]);
                Object convertedOptionItemResourceValue = this.resourceMapValueConverter.convert(indexAndValue[1]);
                String optionItemResourceString = resourceBundle.getString(nextKey);

                IOptionItemResource optionItemResource = new OptionItemResourceImpl(optionItemResourceString, convertedOptionItemResourceValue);
                sortedOptionItems.put(index, optionItemResource);
            }
        }

        return new LinkedList(sortedOptionItems.values());
    }

    /**
     * A class type converter for the option item list values
     * 
     * @author sgoldstein
     */
    public interface IOptionItemResourceValueTypeConverter {

        /**
         * 
         * @param valueToConvert
         * @return
         */
        public Object convert(String valueToConvert);
    }

    /**
     * Implementaion of the IOptionItemResource interface
     * 
     * @author sgoldstein
     */
    private class OptionItemResourceImpl implements IOptionItemResource {

        private String optionItemResourceString;
        private Object convertedOptionItemResourceValue;

        /**
         * Create an instance of OptionItemResourceImpl
         * 
         * @param optionItemResourceString
         * @param convertedOptionItemResourceValue
         */
        public OptionItemResourceImpl(String optionItemResourceString, Object convertedOptionItemResourceValue) {
            this.optionItemResourceString = optionItemResourceString;
            this.convertedOptionItemResourceValue = convertedOptionItemResourceValue;
        }

        /**
         * @see com.bluejungle.destiny.appframework.i18n.IOptionItemResource#getResource()
         */
        public String getResource() {
            return this.optionItemResourceString;
        }

        /**
         * @see com.bluejungle.destiny.appframework.i18n.IOptionItemResource#getOptionValue()
         */
        public Object getOptionValue() {
            return this.convertedOptionItemResourceValue;
        }
    }
}
