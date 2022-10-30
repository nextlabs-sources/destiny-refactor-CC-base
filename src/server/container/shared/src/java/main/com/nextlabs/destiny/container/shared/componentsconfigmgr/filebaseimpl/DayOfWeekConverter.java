/*
 * Created on Jun 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.bluejungle.destiny.server.shared.configuration.type.DayOfWeek;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/componentsconfigmgr/filebaseimpl/DayOfWeekConverter.java#1 $
 */

public class DayOfWeekConverter extends CalendarConverter {

	private static final Map<String, SimpleDateFormat> POSSIBLE_FORMATS;
	static{
		POSSIBLE_FORMATS = new HashMap<String, SimpleDateFormat>();
		POSSIBLE_FORMATS.put(".{3}",  new SimpleDateFormat("EEE"));
		POSSIBLE_FORMATS.put(".{4,}", new SimpleDateFormat("EEEE"));
	}
	
	public DayOfWeekConverter() {
		super(POSSIBLE_FORMATS);
	}

	@Override
	public Object convert(Class type, Object value) {
		Calendar c = (Calendar) super.convert(type, value);
		return new DayOfWeek(c);
	}
}
