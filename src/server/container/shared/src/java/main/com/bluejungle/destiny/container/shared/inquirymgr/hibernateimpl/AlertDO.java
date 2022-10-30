package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.sql.Timestamp;

/**
 * @author nnallagatla
 * 
 */
public class AlertDO {
	
	private long id;
	private MonitorDO monitor;
	private String monitorUID;
	private Timestamp triggeredAt;
	private String level;
	private boolean deleted;
	private long day;
	private long month;
	private int year;
	private String monitorName;
	private String alertMessage;
	private boolean dismissed;
	
	private Long hiddenByUserId;
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the monitor
	 */
	public MonitorDO getMonitor() {
		return monitor;
	}

	/**
	 * @param monitor
	 *            the monitor to set
	 */
	public void setMonitor(MonitorDO monitor) {
		this.monitor = monitor;
	}

	/**
	 * @return the triggeredDateTime
	 */
	public Timestamp getTriggeredAt() {
		return triggeredAt;
	}

	/**
	 * @param triggeredDateTime
	 *            the triggeredDateTime to set
	 */
	public void setTriggeredAt(Timestamp triggeredAt) {
		this.triggeredAt = triggeredAt;
	}

	/**
	 * @return the monitorName
	 */
	public String getMonitorName() {
		return monitorName;
	}

	/**
	 * @param monitorName
	 *            the monitorName to set
	 */
	public void setMonitorName(String monitorName) {
		this.monitorName = monitorName;
	}

	/**
	 * @return the alertMessage
	 */
	public String getAlertMessage() {
		return alertMessage;
	}

	/**
	 * @param alertMessage
	 *            the alertMessage to set
	 */
	public void setAlertMessage(String alertMessage) {
		this.alertMessage = alertMessage;
	}

	/**
	 * <p>
	 * Getter method for dismissed
	 * </p>
	 * 
	 * @return the dismissed
	 */
	public boolean isDismissed() {
		return dismissed;
	}

	/**
	 * <p>
	 *  Setter method for dismissed
	 * </p>
	 *
	 * @param dismissed the dismissed to set
	 */
	public void setDismissed(boolean dismissed) {
		this.dismissed = dismissed;
	}

	/**
	 * @return the level
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * @param level
	 *            the level to set
	 */
	public void setLevel(String level) {
		this.level = level;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * @return the monitorUID
	 */
	public String getMonitorUID() {
		return monitorUID;
	}

	/**
	 * @param monitorUID
	 *            the monitorUID to set
	 */
	public void setMonitorUID(String monitorUID) {
		this.monitorUID = monitorUID;
	}

	/**
	 * @return the day
	 */
	public long getDay() {
		return day;
	}

	/**
	 * @param day
	 *            the day to set
	 */
	public void setDay(long day) {
		this.day = day;
	}

	/**
	 * @return the month
	 */
	public long getMonth() {
		return month;
	}

	/**
	 * @param month
	 *            the month to set
	 */
	public void setMonth(long month) {
		this.month = month;
	}

	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year
	 *            the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	public Long getHiddenByUserId() {
		return hiddenByUserId;
	}

	public void setHiddenByUserId(Long hiddenByUserId) {
		this.hiddenByUserId = hiddenByUserId;
	}
}
