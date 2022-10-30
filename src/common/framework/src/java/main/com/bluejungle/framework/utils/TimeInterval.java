/*
 * Created on Feb 4, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.bluejungle.framework.patterns.EnumBase;

/**
 * A Utility class that represents a particular interval of time
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/TimeInterval.java#1 $
 */

public class TimeInterval implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int time;
    private TimeUnit timeUnit;

    /**
     * Create a time interval in TimeUnit.SECONDS
     * 
     * @param time the numerical time value of this time interval
     */
    public TimeInterval(int time) {
        super();
        this.time = time;
        this.timeUnit = TimeUnit.SECONDS;
    }
    
    /**
     * Create a time interval with the provided time and time unit
     * 
     * @param time the numerical time value of this time interval
     * @param time unit the time unit of this time interval
     */
    public TimeInterval(int time, TimeUnit timeUnit) {
        super();
        this.time = time;
        this.timeUnit = timeUnit;
    }

    /**
     * Returns the time in the time unit specified in this TimeInterval
     * @return the time.
     */
    public int getTime() {
        return this.time;
    }
    
    /**
     * Sets the time in the currently associated Time Unit
     * @param time The time to set.
     */
    public void setTime(int time) {
        this.time = time;
    }
    
    /**
     * Retrieve the time in milliseconds
     * @return the time in milliseconds
     */
    public int getTimeInMilliseconds() {
        int originalTime = getTime();
        TimeUnit originalTimeUnit = getTimeUnit();
        
        return originalTime * originalTimeUnit.getMultiplier();
    }
    
    /**
     * Returns the TimeUnit associated with this TimeInterval
     * @return the TimeUnit.
     */
    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }
    
    /**
     * Sets the timeUnit of the currently associated time.  Note that this does not change the value of the time itself
     * @param timeUnit The timeUnit to set.
     */
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object objectToTest) {
        boolean valueToReturn = false;
        
        if (this == objectToTest) {
            valueToReturn = true;
        } else if ((objectToTest != null) && (this.getClass() == objectToTest.getClass())) {
            TimeInterval timeIntervalToTest = (TimeInterval)objectToTest;
            valueToReturn = ((this.getTime() == timeIntervalToTest.getTime()) && (this.getTimeUnit().equals(timeIntervalToTest.getTimeUnit())));
        }
        
        return valueToReturn;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        // Not an ideal hashcode, but I can't come up with a better one at the moment       
        return this.getTime() ^ this.getTimeUnit().getType();
    }
    
    /**
     * TimeUnit represents time measurement units
     * @author sgoldstein
     */
    public static class TimeUnit extends EnumBase implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public static final TimeUnit MILLISECONDS = new TimeUnit("milliseconds", 4, 1);        
        public static final TimeUnit SECONDS = new TimeUnit("seconds", 3, MILLISECONDS.getMultiplier() * 1000);
        public static final TimeUnit MINUTES = new TimeUnit("minutes", 2, SECONDS.getMultiplier() * 60);
        public static final TimeUnit HOURS = new TimeUnit("hours", 1, MINUTES.getMultiplier() * 60);
        public static final TimeUnit DAYS = new TimeUnit("days", 0, HOURS.getMultiplier() * 24);
                
        private static final Map NAME_TO_TIME_UNIT_MAP = new HashMap();        
        static
        {
            NAME_TO_TIME_UNIT_MAP.put(DAYS.getName(), DAYS);
            NAME_TO_TIME_UNIT_MAP.put(HOURS.getName(), HOURS);
            NAME_TO_TIME_UNIT_MAP.put(MINUTES.getName(), MINUTES);
            NAME_TO_TIME_UNIT_MAP.put(SECONDS.getName(), SECONDS);
            NAME_TO_TIME_UNIT_MAP.put(MILLISECONDS.getName(), MILLISECONDS);
        }
        
        private int multiplier;
        
        /**
         * Create a TimeUnit.
         * @param name
         * @param type
         */
        private TimeUnit(String name, int type, int multiplier) {
            super(name, type);
            this.multiplier = multiplier;
        }
        
        /**
         * Retrieve a TimeUnit by name
         * @param name the name of the TimeUnit to retrieve
         * @return the TimeUnit associated with the provided name
         */
        public static TimeUnit getTimeUnit(String name) {
            if (name == null) {
                throw new NullPointerException("name cannot be null");
            }
                       
            if (!NAME_TO_TIME_UNIT_MAP.containsKey(name)) {
                throw new IllegalArgumentException(name + " is not a valid name for a TimeUnit");
            }
            
            return (TimeUnit)NAME_TO_TIME_UNIT_MAP.get(name);
        }
        
        /**
         * Return the multiplying factor with milliseconds being the base
         * @return the multiplying factor with milliseconds being the base
         */
        private int getMultiplier() {
            return this.multiplier;
        }
    }
}
