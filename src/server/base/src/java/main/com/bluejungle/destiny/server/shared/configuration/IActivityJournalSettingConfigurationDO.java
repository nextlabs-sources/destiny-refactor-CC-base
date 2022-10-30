/*
 * Created on Jun 8, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

import java.util.Calendar;

import com.bluejungle.destiny.server.shared.configuration.impl.DaysOfMonthDO;
import com.bluejungle.destiny.server.shared.configuration.impl.DaysOfWeekDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/IActivityJournalSettingConfigurationDO.java#1 $
 */

public interface IActivityJournalSettingConfigurationDO {
	Calendar getSyncTimeOfDay();
	Long getSyncTimeInterval();
	Integer getSyncTimeoutInMinutes();

	Calendar getIndexRebuildTimeOfDay();
	DaysOfWeekDO getIndexRebuildDaysOfWeek();
	DaysOfMonthDO getIndexRebuildDaysOfMonth();
	Boolean getIndexRebuildAutoRebuildIndexes();
	Integer getIndexRebuildTimeoutInMinutes();

	Calendar getArchiveTimeOfDay();
	DaysOfWeekDO getArchiveDaysOfWeek();
	DaysOfMonthDO getArchiveDaysOfMonth();
	Integer getArchiveDaysOfDataToKeep();
	Boolean getArchiveAutoArchive();
	Integer getArchiveTimeoutInMinutes();
}
