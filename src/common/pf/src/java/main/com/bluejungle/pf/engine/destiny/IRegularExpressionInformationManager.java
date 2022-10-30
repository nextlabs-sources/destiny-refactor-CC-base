package com.bluejungle.pf.engine.destiny;

/*
 * Created on Nov 04, 2008
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/IRegularExpressionInformationManager.java#1 $
 */

public interface IRegularExpressionInformationManager {
    /**
     * The default regular expression resolver
     */
    IRegularExpressionInformationManager DEFAULT = new IRegularExpressionInformationManager() {
            private final String[] EMPTY = new String[0];

            public String getRegularExpressionByName(String name) {
                return "";
            }

            public String[] getRegularExpressionsByName(String[] names) {
                return EMPTY;
            }
        };

    /**
     * Given a name, return the corresponding regular expression
     */
    String getRegularExpressionByName(String name);

    /**
     * Given an array of names, return an array of their corresponding regular expressions
     */
    String[] getRegularExpressionsByName(String[] name);
}
