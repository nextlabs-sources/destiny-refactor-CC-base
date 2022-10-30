/*
 * Created on Jul 01, 2009
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import com.bluejungle.destiny.framework.types.TimeUnits;

/**
 * A utility class to help manipulate TimeUnits.  Note that TimeInterval defines TimeUnit,
 * which is probably more units classes than we need.
 * @author amorgan
 * @version $Id //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */
public class TimeUnitsUtils {
    public static long getMultiplier(TimeUnits units) {
        long multiplier = 1;
        if (units == TimeUnits.milliseconds) {
            multiplier = 1;
        } else if (units == TimeUnits.seconds) {
            multiplier = 1000;
        } else if (units == TimeUnits.minutes) {
            multiplier = 60000;
        } else if (units == TimeUnits.hours) {
            multiplier = 3600000;
        } else if (units == TimeUnits.days) {
            multiplier = 86400000;
        }

        return multiplier;
    }
}

