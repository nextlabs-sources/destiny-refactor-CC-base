/*
 * Created on Jun 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.type;

import java.util.Calendar;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/type/DayOfWeek.java#1 $
 */

public class DayOfWeek{
	private final byte dayOfWeek;
	
	public DayOfWeek(Calendar c){
		dayOfWeek = (byte)c.get(Calendar.DAY_OF_WEEK);
	}
	
	public byte getDayOfWeek(){
		return dayOfWeek;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dayOfWeek;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof Number) {
            return ((Number) obj).byteValue() == dayOfWeek;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            return ((DayOfWeek) obj).dayOfWeek == dayOfWeek;
        }
    }
	
}
