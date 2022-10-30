package com.bluejungle.framework.utils;

import java.util.Map;
import java.util.Properties;

public final class PropertiesUtils {

    
    public static byte get(Properties properties, String key, byte defaultValue) {
        String v = properties.getProperty(key);
        if (v == null) {
            return defaultValue;
        }

        return Byte.parseByte(v);
    }
    
    public static short get(Properties properties, String key, short defaultValue) {
        String v = properties.getProperty(key);
        if (v == null) {
            return defaultValue;
        }

        return Short.parseShort(v);
    }
    
    public static int get(Properties properties, String key, int defaultValue) {
        String v = properties.getProperty(key);
        if (v == null) {
            return defaultValue;
        }

        return Integer.parseInt(v);
    }
    
    public static long get(Properties properties, String key, long defaultValue) {
        String v = properties.getProperty(key);
        if (v == null) {
            return defaultValue;
        }

        return Long.parseLong(v);
    }
    
    public static float get(Properties properties, String key, float defaultValue) {
        String v = properties.getProperty(key);
        if (v == null) {
            return defaultValue;
        }

        return Float.parseFloat(v);
    }
    
    public static double get(Properties properties, String key, double defaultValue) {
        String v = properties.getProperty(key);
        if (v == null) {
            return defaultValue;
        }

        return Double.parseDouble(v);
    }
    
    
    
    public static interface KeyMatcher {
        boolean isMatch(Object key);
        
        Object filterKey(Object key);
        
        Object filterValue(Object value);
    }
    
    public static abstract class AbstractKeyMatcher implements KeyMatcher {
        public Object filterKey(Object key) {
            return key;
        }
        
        public Object filterValue(Object value){
            return value;
        }
    }
    
    
    /**
     * return all the properties with matched prefix
     * @param properties
     * @param prefix
     * @return
     */
    public static Properties filter(Properties properties, KeyMatcher matcher) {
        if (properties == null) {
            return null;
        }

        Properties filtered = new Properties();
        for (Map.Entry e : properties.entrySet()) {
            if (matcher.isMatch(e.getKey())) {
                filtered.put(matcher.filterKey(e.getKey()), matcher.filterValue(e.getValue()));
            }
        }

        return filtered;
    }
    
    
    
    
    private PropertiesUtils() {
    }
    
}
