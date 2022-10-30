package com.bluejungle.framework.utils;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 * 
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/utils/TimeRelation.java#1 $
 */

import java.io.Serializable;
import java.util.Date;

public class TimeRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Date activeFrom;

    private final Date activeTo;

    public TimeRelation( Date from, Date to ) {
        activeFrom = UnmodifiableDate.forDate( from );
        activeTo = UnmodifiableDate.forDate( to );
    }

    public Date getActiveFrom() {
        return activeFrom;
    }

    public Date getActiveTo() {
        return activeTo;
    }

    public TimeRelation close( Date asOf ) {
        return new TimeRelation( getActiveFrom(), asOf );
    }

    public static TimeRelation open( Date asOf ) {
        return new TimeRelation( asOf, UnmodifiableDate.END_OF_TIME );
    }

    public boolean isClosed() {
        return !activeTo.equals(UnmodifiableDate.END_OF_TIME);
    }

    public boolean equals( Object other ) {
        if ( other == this ) {
            return true;
        }
        if ( other == null ) {
            return false;
        }
        if ( other instanceof TimeRelation ) {
            TimeRelation that = (TimeRelation)other;
            return this.activeFrom.equals( that.activeFrom )
                && this.activeTo.equals( that.activeTo );
        } else {
            return false;
        }
    }

    public int hashCode() {
        return activeFrom.hashCode() + activeTo.hashCode();
    }
}
