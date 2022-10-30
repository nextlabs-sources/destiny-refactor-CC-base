package com.bluejungle.pf.engine.destiny;

/*
 * Created on Nov 04, 2008
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/RegularExpressionInformationManager.java#1 $
 */

import java.util.Date;
import java.util.Map;

public class RegularExpressionInformationManager implements IRegularExpressionInformationManager {
    /**
     * The time at which this information was built
     */
    private long buildTime;

    /**
     * The mapping from names to regular expressions
     */
    Map<String, String> regularExpressions = null;

    /**
     * Creates a regular expression manager for the specified initialization
     * data.
     *
     * @param rexps A map from regular expression names to regular expression values
     */
    public RegularExpressionInformationManager(Date buildTime, Map<String, String> rexps) {
        if (buildTime == null) {
            throw new NullPointerException("buildTime");
        }

        if (rexps == null) {
            throw new NullPointerException("rexps");
        }

        this.buildTime = buildTime.getTime();
        regularExpressions = rexps;
    }

    public String getRegularExpressionByName(String name) {
        return regularExpressions.get(name);
    }

    /**
     * See IRegularExpressionInformationManager.java#getRegularExpressionsByName(String)
     */
    public String[] getRegularExpressionsByName(String[] names) {
        if (names == null) {
            return null;
        }

        String[] ret = new String[names.length];
        for (int i = 0; i != names.length; i++) {
            ret[i] = getRegularExpressionByName(names[i]);
        }

        return ret;
    }

    /**
     * Returns the build time of this manager
     *
     * @return the build time of this manager
     */
    public Date getBuildTime() {
        return new Date(buildTime);
    }
}
