/*
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author nnallagatla
 *
 */
public class MonitorDO {

	public static final String SAVED_REPORT_ONLY_ME = "only_me";
	public static final String SAVED_REPORT_PUBLIC = "public";
	public static final String SAVED_REPORT_USERS = "users";

	private boolean autoDismiss;
	private boolean sendEmail;
	private String emailAddress;
	private String alertMessage;
	private Timestamp createdAt;
	private Timestamp lastUpdatedAt;
	private boolean active;
	private boolean deleted;
	private boolean archived;

	private String criteriaJSON;
	private Map<String, MonitorTagDO> tags;

	private Long id;
	private String name;
	private String description;
	private String monitorUID;

	private String sharedMode;
	private Long ownerId;
	private String pqlData;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public MonitorDO() {

	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @param description
	 */
	public MonitorDO(Long id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	/**
	 * <p>
	 * Getter method for autoDismiss
	 * </p>
	 * 
	 * @return the autoDismiss
	 */
	public boolean isAutoDismiss() {
		return autoDismiss;
	}

	/**
	 * <p>
	 * Setter method for autoDismiss
	 * </p>
	 *
	 * @param autoDismiss
	 *            the autoDismiss to set
	 */
	public void setAutoDismiss(boolean autoDismiss) {
		this.autoDismiss = autoDismiss;
	}

	/**
	 * @return the sendEmail
	 */
	public boolean isSendEmail() {
		return sendEmail;
	}

	/**
	 * @param sendEmail
	 *            the sendEmail to set
	 */
	public void setSendEmail(boolean sendEmail) {
		this.sendEmail = sendEmail;
	}

	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @param emailAddress
	 *            the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
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
	 * @return the createdAt
	 */
	public Timestamp getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * @return the lastUpdatedAt
	 */
	public Timestamp getLastUpdatedAt() {
		return lastUpdatedAt;
	}

	/**
	 * @param lastUpdatedAt
	 *            the lastUpdatedAt to set
	 */
	public void setLastUpdatedAt(Timestamp lastUpdatedAt) {
		this.lastUpdatedAt = lastUpdatedAt;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @param deleted
	 *            the deleted to set
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * @return the tags
	 */
	public Map<String, MonitorTagDO> getTags() {
		return tags;
	}

	/**
	 * @param tags
	 *            the tags to set
	 */
	public void setTags(Map<String, MonitorTagDO> tags) {
		this.tags = tags;
	}

	/**
	 * @return the archived
	 */
	public boolean isArchived() {
		return archived;
	}

	/**
	 * @param archived
	 *            the archived to set
	 */
	public void setArchived(boolean archived) {
		this.archived = archived;
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
	 * @return the criteriaJSON
	 */
	public String getCriteriaJSON() {
		return criteriaJSON;
	}

	/**
	 * @param criteriaJSON
	 *            the criteriaJSON to set
	 */
	public void setCriteriaJSON(String criteriaJSON) {
		this.criteriaJSON = criteriaJSON;
	}

	public String getSharedMode() {
		return sharedMode;
	}

	public void setSharedMode(String sharedMode) {
		this.sharedMode = sharedMode;
	}

	public Long getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
	}

	public String getPqlData() {
		return pqlData;
	}

	public void setPqlData(String pqlData) {
		this.pqlData = pqlData;
	}

	@Override
	public String toString() {
		return String
				.format("MonitorDO [autoDismiss=%s, sendEmail=%s, emailAddress=%s, alertMessage=%s, active=%s, archived=%s, id=%s, name=%s, description=%s, monitorUID=%s, sharedMode=%s, ownerId=%s]",
						autoDismiss, sendEmail, emailAddress, alertMessage,
						active, archived, id, name, description, monitorUID,
						sharedMode, ownerId);
	}

}
