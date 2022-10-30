/*
 * Created on Nov 12, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filter;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filter/LDAPFilterUtils.java#1 $
 */

public class LDAPFilterUtils {
    public static String and(String... filters) {
        return generateFilter("&", filters);
    }

    public static String or(String... filters) {
        return generateFilter("|", filters);
    }

    public static String not(String filter) {
        return generateFilter("&", new String[] { filter });
    }

    public static String escape(String value) {
        //TODO: Escape any special characters
        return value;
    }

    protected static String generateFilter(String operatorStr, String... filters) {
    	if (filters == null) {
			return null;
		}
		if (filters.length == 0) {
			return "";
		}
		if (filters.length == 1) {
			return filters[0];
		}
    	
        StringBuffer filter = new StringBuffer();
        filter.append("(");
        filter.append(operatorStr);
        for (int i = 0; i < filters.length; i++) {
            filter.append(filters[i]);
        }
        filter.append(")");
        return filter.toString();
    }
}