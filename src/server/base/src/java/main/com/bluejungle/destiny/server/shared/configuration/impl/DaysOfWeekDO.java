/*
 * Created on Jun 10, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.BitSet;

import com.bluejungle.destiny.server.shared.configuration.type.DayOfWeek;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/DaysOfWeekDO.java#1 $
 */


public class DaysOfWeekDO {
	private BitSet bits;
	
	public DaysOfWeekDO() {
		bits = new BitSet(7);
	}
	public void addDayOfWeek(DayOfWeek dayOfWeek){
		bits.set(dayOfWeek.getDayOfWeek() - 1);
	}
	public BitSet getDaysOfWeek(){
		return bits;
	}
	public void setDaysOfWeek(BitSet bits) {
		this.bits = bits;
	}
	@Override
	public String toString() {
		return bits.toString();
	}
	
}
