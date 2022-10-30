/*
 * Created on Mar 16, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.bluejungle.destiny.tools.enrollment.EnrollmentMgrException;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/util/ValidationHelper.java#1 $
 */

public class ValidationHelper {

    /**
     * Validate Sync Interval
     * @param intervalStr
     * @return
     * @throws IllegalArgumentException if the interval is not a number or less than 0
     */
    public static int validateSyncInterval(String intervalStr){
        int interval = 0;
        try {
            interval = Integer.parseInt(intervalStr);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer sync interval: " + intervalStr);
        }
        if ( interval < 0 ) {
            throw new IllegalArgumentException("Invalid sync interval: " + intervalStr);
        }
        return interval;
    }

    private static DateFormat getDateFormatter(String timeFormat) {
        if ( null != timeFormat && !"".equals(timeFormat.trim())){
            return new SimpleDateFormat(timeFormat.trim());
        } else {
            return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
        }
    }
    
    public static Date validateSyncStartTime(String startTime, String timeFormat){
        if ( startTime != null ) {
            long start;
            try {
                start = getDateFormatter(timeFormat).parse(startTime.trim()).getTime();
		long currentTime = System.currentTimeMillis();
                if ( start - currentTime < 0 ) {
                    throw new IllegalArgumentException("Start time is before the current time " + startTime);
                }

                return new Date(start);
            }
            catch (ParseException e) {
                throw new IllegalArgumentException("Invalid auto sync start time:" + startTime + "\nSample start time format is: " + getDateFormatter(timeFormat).format(new Date()));
            }
        }
        return null;
    }
}
