/*
 * Created on Nov 9, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.util;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.ParseException;

import org.junit.Test;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/test/com/bluejungle/destiny/tools/enrollment/util/TestValidationHelper.java#1 $
 */

public class TestValidationHelper {
	@Test
	public void future(){
		//GMT: Fri, 25 Dec 3007 20:00:00 GMT
		//Your timezone: Friday, December 25, 3007 12:00:00 PM
		assertEquals(32755521600000L,ValidationHelper.validateSyncStartTime("Dec 25, 3007 0:00 PM").getTime());
		
		//GMT: Fri, 25 Dec 3007 08:00:00 GMT
		//Your timezone: Friday, December 25, 3007 12:00:00 AM
		assertEquals(32755478400000L,ValidationHelper.validateSyncStartTime("Dec 25, 3007 0:00 AM").getTime());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void past(){
		//GMT: Fri, 25 Dec 3007 20:00:00 GMT
		//Your timezone: Friday, December 25, 3007 12:00:00 PM
		assertEquals(32755521600000L,ValidationHelper.validateSyncStartTime("Dec 25, 1999 0:00 PM").getTime());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void yyForamt(){
		// year is 2050
		// but it is ... NaN
		ValidationHelper.validateSyncStartTime("Dec 25, 50 0:00 PM").getTime();
	}
	
	@Test
	public void weirdDayForamt(){
		// GMT: Sat, 02 Jan 3008 20:00:00 GMT
		// Your timezone: Saturday, January 02, 3008 12:00:00 PM
		assertEquals(32756212800000L,ValidationHelper.validateSyncStartTime("Dec 33, 3007 0:00 PM").getTime());
	}
	
	@Test
	public void weirdTimeForamt(){
		// GMT: Sat, 26 Dec 3007 21:00:00 GMT
		// Your timezone: Saturday, December 26, 3007 1:00:00 PM
		assertEquals(32755611600000L,ValidationHelper.validateSyncStartTime("Dec 25, 3007 25:00 PM").getTime());
	}
}
