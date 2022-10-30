/**
 * 
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import java.sql.Timestamp;

/**
 * 
 * <p>
 * SavedReportDO for Save reports in inquiry center.
 * </p>
 *
 *
 * @author Amila Silva
 *
 */
public class SavedReportDO {

	public static final String SAVED_REPORT_ONLY_ME = "only_me";
	public static final String SAVED_REPORT_PUBLIC = "public";
	public static final String SAVED_REPORT_USERS = "users";

	private Long id;
	private String title;
	private String description;
	private String sharedMode;
	private Long ownerId;
	private String criteriaJSON;
	private Timestamp createdDate;
	private Timestamp lastUpdatedDate;
	private String pqlData;
	private boolean deleted;
	private boolean inDashboard;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	public String getSharedMode() {
		return sharedMode;
	}

	public void setSharedMode(String sharedMode) {
		this.sharedMode = sharedMode;
	}

	/**
	 * @return the ownerId
	 */
	public Long getOwnerId() {
		return ownerId;
	}

	/**
	 * @param ownerId
	 *            the ownerId to set
	 */
	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
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

	/**
	 * @return the createdDate
	 */
	public Timestamp getCreatedDate() {
		return createdDate;
	}

	/**
	 * @param createdDate
	 *            the createdDate to set
	 */
	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * @return the lastUpdatedDate
	 */
	public Timestamp getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	/**
	 * @param lastUpdatedDate
	 *            the lastUpdatedDate to set
	 */
	public void setLastUpdatedDate(Timestamp lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
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
	 * @return the inDashboard
	 */
	public boolean isInDashboard() {
		return inDashboard;
	}

	/**
	 * @param inDashboard
	 *            the inDashboard to set
	 */
	public void setInDashboard(boolean inDashboard) {
		this.inDashboard = inDashboard;
	}

	public String getPqlData() {
		return pqlData;
	}

	public void setPqlData(String pqlData) {
		this.pqlData = pqlData;
	}

}
