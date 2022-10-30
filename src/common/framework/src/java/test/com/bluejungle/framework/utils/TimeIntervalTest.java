/*
 * Created on Feb 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import com.bluejungle.framework.test.BaseDestinyTestCase;
import com.bluejungle.framework.utils.TimeInterval;

/**
 * Unit Test for TimeInveral utility class
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/utils/test/TimeIntervalTest.java#1 $
 */

public class TimeIntervalTest extends BaseDestinyTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TimeIntervalTest.class);
    }

    public void testGetTime() {
        int time = 10;
        TimeInterval timeInterval = new TimeInterval(time, TimeInterval.TimeUnit.DAYS);

        assertEquals("Ensure create with time is correct", time, timeInterval.getTime());

        // Now set time and check again
        int setTime = 50;
        timeInterval.setTime(setTime);
        assertEquals("Ensure set time is correct", setTime, timeInterval.getTime());
    }

    public void testSetTime() {
        int time = 10;
        TimeInterval timeInterval = new TimeInterval(time, TimeInterval.TimeUnit.DAYS);

        assertEquals("Ensure create with time is correct", time, timeInterval.getTime());

        // Now set time and check again
        int setTime = 50;
        timeInterval.setTime(setTime);
        assertEquals("Ensure set time is correct", setTime, timeInterval.getTime());
    }

    public void testGetTimeInMilliseconds() {
        int timeInDays = 5;
        TimeInterval timeInterval = new TimeInterval(timeInDays, TimeInterval.TimeUnit.DAYS);
        assertEquals("Ensure day translation to millis is correct", timeInDays * 24 * 60 * 60 * 1000, timeInterval.getTimeInMilliseconds());

        timeInterval = new TimeInterval(timeInDays * 24, TimeInterval.TimeUnit.HOURS);
        assertEquals("Ensure hour translation to millis is correct", timeInDays * 24 * 60 * 60 * 1000, timeInterval.getTimeInMilliseconds());

        timeInterval = new TimeInterval(timeInDays * 24 * 60, TimeInterval.TimeUnit.MINUTES);
        assertEquals("Ensure minutes translation to millis is correct", timeInDays * 24 * 60 * 60 * 1000, timeInterval.getTimeInMilliseconds());

        timeInterval = new TimeInterval(timeInDays * 24 * 60 * 60, TimeInterval.TimeUnit.SECONDS);
        assertEquals("Ensure seconds translation to millis is correct", timeInDays * 24 * 60 * 60 * 1000, timeInterval.getTimeInMilliseconds());

        timeInterval = new TimeInterval(timeInDays * 24 * 60 * 60 * 1000, TimeInterval.TimeUnit.MILLISECONDS);
        assertEquals("Ensure milliseconds translation to millis is correct", timeInDays * 24 * 60 * 60 * 1000, timeInterval.getTimeInMilliseconds());
    }

    public void testGetTimeUnit() {
        TimeInterval.TimeUnit timeUnit = TimeInterval.TimeUnit.DAYS;
        TimeInterval timeInterval = new TimeInterval(10, timeUnit);

        assertEquals("Ensure create with time unit is correct", timeUnit, timeInterval.getTimeUnit());

        // Now set time and check again
        TimeInterval.TimeUnit setTimeUnit = TimeInterval.TimeUnit.MINUTES;
        timeInterval.setTimeUnit(setTimeUnit);
        assertEquals("Ensure set time unit is correct", setTimeUnit, timeInterval.getTimeUnit());
    }

    public void testSetTimeUnit() {
        TimeInterval.TimeUnit timeUnit = TimeInterval.TimeUnit.DAYS;
        TimeInterval timeInterval = new TimeInterval(10, timeUnit);

        assertEquals("Ensure create with time unit is correct", timeUnit, timeInterval.getTimeUnit());

        // Now set time and check again
        TimeInterval.TimeUnit setTimeUnit = TimeInterval.TimeUnit.MINUTES;
        timeInterval.setTimeUnit(setTimeUnit);
        assertEquals("Ensure set time unit is correct", setTimeUnit, timeInterval.getTimeUnit());
    }

}