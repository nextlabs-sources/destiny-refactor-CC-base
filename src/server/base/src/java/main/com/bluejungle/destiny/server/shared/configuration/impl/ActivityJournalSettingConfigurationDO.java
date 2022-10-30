/*
 * Created on Jun 8, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.Calendar;

import com.bluejungle.destiny.server.shared.configuration.IActivityJournalSettingConfigurationDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/impl/ActivityJournalSettingConfigurationDO.java#1 $
 */

public class ActivityJournalSettingConfigurationDO implements IActivityJournalSettingConfigurationDO{
	private Long syncTimeInterval;
	private Calendar syncTimeOfDay;
	private Integer syncTimeoutInMinutes;
	
	
	private Calendar indexRebuildTimeOfDay;
	private DaysOfWeekDO indexRebuildDaysOfWeek;
	private DaysOfMonthDO indexRebuildDaysOfMonth;
	private Boolean indexRebuildAutoRebuildIndexes;
	private Integer indexRebuildTimeoutInMinutes;
	
	private Calendar archiveTimeOfDay;
	private DaysOfWeekDO archiveDaysOfWeek;
	private DaysOfMonthDO archiveDaysOfMonth;
	private Integer archiveDaysOfDataToKeep;
	private Boolean archiveAutoArchive;
	private Integer archiveTimeoutInMinutes;
	
	
	public Long getSyncTimeInterval() {
		return syncTimeInterval;
	}
	public void setSyncTimeInterval(Long syncTimeInterval) {
		this.syncTimeInterval = syncTimeInterval;
	}
	public Calendar getSyncTimeOfDay() {
		return syncTimeOfDay;
	}
	public void setSyncTimeOfDay(Calendar syncTimeOfDay) {
		this.syncTimeOfDay = syncTimeOfDay;
	}
	public Integer getSyncTimeoutInMinutes() {
		return syncTimeoutInMinutes;
	}
	public void setSyncTimeoutInMinutes(Integer syncTimeoutInMinutes) {
		this.syncTimeoutInMinutes = syncTimeoutInMinutes;
	}
	
	public Calendar getIndexRebuildTimeOfDay() {
		return indexRebuildTimeOfDay;
	}
	public void setIndexRebuildTimeOfDay(Calendar indexRebuildTimeOfDay) {
		this.indexRebuildTimeOfDay = indexRebuildTimeOfDay;
	}
	public DaysOfWeekDO getIndexRebuildDaysOfWeek() {
		return indexRebuildDaysOfWeek;
	}
	public void setIndexRebuildDaysOfWeek(DaysOfWeekDO indexRebuildDaysOfWeek) {
		this.indexRebuildDaysOfWeek = indexRebuildDaysOfWeek;
	}
	public DaysOfMonthDO getIndexRebuildDaysOfMonth() {
		return indexRebuildDaysOfMonth;
	}
	public void setIndexRebuildDaysOfMonth(DaysOfMonthDO indexRebuildDaysOfMonth) {
		this.indexRebuildDaysOfMonth = indexRebuildDaysOfMonth;
	}
	public Boolean getIndexRebuildAutoRebuildIndexes() {
		return indexRebuildAutoRebuildIndexes;
	}
	public void setIndexRebuildAutoRebuildIndexes(Boolean indexRebuildAutoRebuildIndexes) {
		this.indexRebuildAutoRebuildIndexes = indexRebuildAutoRebuildIndexes;
	}
	public Integer getIndexRebuildTimeoutInMinutes() {
        return indexRebuildTimeoutInMinutes;
    }
    public void setIndexRebuildTimeoutInMinutes(Integer indexRebuildTimeoutInMinutes) {
        this.indexRebuildTimeoutInMinutes = indexRebuildTimeoutInMinutes;
    }
    
    public Calendar getArchiveTimeOfDay() {
		return archiveTimeOfDay;
	}
	public void setArchiveTimeOfDay(Calendar archiveTimeOfDay) {
		this.archiveTimeOfDay = archiveTimeOfDay;
	}
	public DaysOfWeekDO getArchiveDaysOfWeek() {
		return archiveDaysOfWeek;
	}
	public void setArchiveDaysOfWeek(DaysOfWeekDO archiveDaysOfWeek) {
		this.archiveDaysOfWeek = archiveDaysOfWeek;
	}
	public DaysOfMonthDO getArchiveDaysOfMonth() {
		return archiveDaysOfMonth;
	}
	public void setArchiveDaysOfMonth(DaysOfMonthDO archiveDaysOfMonth) {
		this.archiveDaysOfMonth = archiveDaysOfMonth;
	}
	public Integer getArchiveDaysOfDataToKeep() {
		return archiveDaysOfDataToKeep;
	}
	public void setArchiveDaysOfDataToKeep(Integer archiveDaysOfDataToKeep) {
		this.archiveDaysOfDataToKeep = archiveDaysOfDataToKeep;
	}
	public Boolean getArchiveAutoArchive() {
		return archiveAutoArchive;
	}
	public void setArchiveAutoArchive(Boolean archiveAutoArchive) {
		this.archiveAutoArchive = archiveAutoArchive;
	}
    public Integer getArchiveTimeoutInMinutes() {
        return archiveTimeoutInMinutes;
    }
    public void setArchiveTimeoutInMinutes(Integer archiveTimeoutInMinutes) {
        this.archiveTimeoutInMinutes = archiveTimeoutInMinutes;
    }
	
}
