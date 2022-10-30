/*
 * Created on Jun 10, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.BitSet;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/DaysOfMonthDO.java#1 $
 */

public class DaysOfMonthDO {
	
	private BitSet bits;
	
	public DaysOfMonthDO(){
		bits = new BitSet(31);
	}
	
	public void addDayOfMonth(Byte dayOfMonth){
		bits.set(dayOfMonth);
	}	
	public BitSet getDaysOfMonth(){
		return bits;
	}
	public void setDaysOfMonth(BitSet bits){
		this.bits = bits;
	}

	@Override
	public String toString() {
		return bits.toString();
	}
	
	
}
