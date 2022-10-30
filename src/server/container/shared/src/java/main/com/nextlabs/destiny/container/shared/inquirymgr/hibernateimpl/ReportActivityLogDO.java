/*
 * Created on Jul 8, 2014
 * 
 * All sources, binaries and HTML pages (C) copyright 2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 *
 */
package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

import java.sql.Timestamp;

/**
 * <p>
 * Entity for ReportActivityLog
 * 
 * This is entitiy class created for create RPA_LOG table with installer.
 * </p>
 * 
 * 
 * @author Amila Silva
 * 
 */
public class ReportActivityLogDO {

	private Long id;

	private Timestamp timestamp;
	private Long requestDateTime;
	private Long week;
	private Long month;
	private Long day;
	private Long hour;
	private Long minute;

	private Long hostId;
	private String hostIPAddress;
	private String hostName;

	private Long userId;
	private String userName;
	private String userSID;

	private Long applicationId;
	private String applicationName;

	private String action;

	private Long policyId;
	private String policyFullName;
	private String policyName;
	private String policyDecision;
	private Long decisionRequestId;
	private int logLevel;
	private String resourceName;
	private Long resourceSize;
	private String resourceOwnerId;
	private Long resourceCreatedDate;
	private Long resourceModifiedDate;
	private String resourcePrefix;
	private String resourcePath;
	private String resourceShortName;
	private String toResourceName;
	private String attr1;
	private String attr2;
	private String attr3;
	private String attr4;
	private String attr5;
	private String attr6;
	private String attr7;
	private String attr8;
	private String attr9;
	private String attr10;
	private String attr11;
	private String attr12;
	private String attr13;
	private String attr14;
	private String attr15;
	private String attr16;
	private String attr17;
	private String attr18;
	private String attr19;
	private String attr20;
	private String attr21;
	private String attr22;
	private String attr23;
	private String attr24;
	private String attr25;
	private String attr26;
	private String attr27;
	private String attr28;
	private String attr29;
	private String attr30;
	private String attr31;
	private String attr32;
	private String attr33;
	private String attr34;
	private String attr35;
	private String attr36;
	private String attr37;
	private String attr38;
	private String attr39;
	private String attr40;
	private String attr41;
	private String attr42;
	private String attr43;
	private String attr44;
	private String attr45;
	private String attr46;
	private String attr47;
	private String attr48;
	private String attr49;
	private String attr50;
	private String attr51;
	private String attr52;
	private String attr53;
	private String attr54;
	private String attr55;
	private String attr56;
	private String attr57;
	private String attr58;
	private String attr59;
	private String attr60;
	private String attr61;
	private String attr62;
	private String attr63;
	private String attr64;
	private String attr65;
	private String attr66;
	private String attr67;
	private String attr68;
	private String attr69;
	private String attr70;
	private String attr71;
	private String attr72;
	private String attr73;
	private String attr74;
	private String attr75;
	private String attr76;
	private String attr77;
	private String attr78;
	private String attr79;
	private String attr80;
	private String attr81;
	private String attr82;
	private String attr83;
	private String attr84;
	private String attr85;
	private String attr86;
	private String attr87;
	private String attr88;
	private String attr89;
	private String attr90;
	private String attr91;
	private String attr92;
	private String attr93;
	private String attr94;
	private String attr95;
	private String attr96;
	private String attr97;
	private String attr98;
	private String attr99;

	/**
	 * <p>
	 * Getter method for id
	 * </p>
	 * 
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * <p>
	 * Setter method for id
	 * </p>
	 * 
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * <p>
	 * Getter method for timestamp
	 * </p>
	 * 
	 * @return the timestamp
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

	/**
	 * <p>
	 * Setter method for timestamp
	 * </p>
	 * 
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the requestDateTime
	 */
	public Long getRequestDateTime() {
		return requestDateTime;
	}

	/**
	 * @param requestDateTime
	 *            the requestDateTime to set
	 */
	public void setRequestDateTime(Long requestDateTime) {
		this.requestDateTime = requestDateTime;
	}

	/**
	 * <p>
	 * Getter method for month
	 * </p>
	 * 
	 * @return the month
	 */
	public Long getMonth() {
		return month;
	}

	/**
	 * <p>
	 * Setter method for month
	 * </p>
	 * 
	 * @param month
	 *            the month to set
	 */
	public void setMonth(Long month) {
		this.month = month;
	}

	/**
	 * @return the week
	 */
	public Long getWeek() {
		return week;
	}

	/**
	 * @param week
	 *            the week to set
	 */
	public void setWeek(Long week) {
		this.week = week;
	}

	/**
	 * <p>
	 * Getter method for day
	 * </p>
	 * 
	 * @return the day
	 */
	public Long getDay() {
		return day;
	}

	/**
	 * <p>
	 * Setter method for day
	 * </p>
	 * 
	 * @param day
	 *            the day to set
	 */
	public void setDay(Long day) {
		this.day = day;
	}

	/**
	 * @return the hour
	 */
	public Long getHour() {
		return hour;
	}

	/**
	 * @param hour
	 *            the hour to set
	 */
	public void setHour(Long hour) {
		this.hour = hour;
	}

	/**
	 * @return the minute
	 */
	public Long getMinute() {
		return minute;
	}

	/**
	 * @param minute
	 *            the minute to set
	 */
	public void setMinute(Long minute) {
		this.minute = minute;
	}

	/**
	 * <p>
	 * Getter method for hostId
	 * </p>
	 * 
	 * @return the hostId
	 */
	public Long getHostId() {
		return hostId;
	}

	/**
	 * <p>
	 * Setter method for hostId
	 * </p>
	 * 
	 * @param hostId
	 *            the hostId to set
	 */
	public void setHostId(Long hostId) {
		this.hostId = hostId;
	}

	/**
	 * <p>
	 * Getter method for hostIPAddress
	 * </p>
	 * 
	 * @return the hostIPAddress
	 */
	public String getHostIPAddress() {
		return hostIPAddress;
	}

	/**
	 * <p>
	 * Setter method for hostIPAddress
	 * </p>
	 * 
	 * @param hostIPAddress
	 *            the hostIPAddress to set
	 */
	public void setHostIPAddress(String hostIPAddress) {
		this.hostIPAddress = hostIPAddress;
	}

	/**
	 * <p>
	 * Getter method for hostName
	 * </p>
	 * 
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * <p>
	 * Setter method for hostName
	 * </p>
	 * 
	 * @param hostName
	 *            the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * <p>
	 * Getter method for userId
	 * </p>
	 * 
	 * @return the userId
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * <p>
	 * Setter method for userId
	 * </p>
	 * 
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * <p>
	 * Getter method for userName
	 * </p>
	 * 
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * <p>
	 * Setter method for userName
	 * </p>
	 * 
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * <p>
	 * Getter method for userSID
	 * </p>
	 * 
	 * @return the userSID
	 */
	public String getUserSID() {
		return userSID;
	}

	/**
	 * <p>
	 * Setter method for userSID
	 * </p>
	 * 
	 * @param userSID
	 *            the userSID to set
	 */
	public void setUserSID(String userSID) {
		this.userSID = userSID;
	}

	/**
	 * <p>
	 * Getter method for applicationId
	 * </p>
	 * 
	 * @return the applicationId
	 */
	public Long getApplicationId() {
		return applicationId;
	}

	/**
	 * <p>
	 * Setter method for applicationId
	 * </p>
	 * 
	 * @param applicationId
	 *            the applicationId to set
	 */
	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	/**
	 * <p>
	 * Getter method for applicationName
	 * </p>
	 * 
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * <p>
	 * Setter method for applicationName
	 * </p>
	 * 
	 * @param applicationName
	 *            the applicationName to set
	 */
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * <p>
	 * Getter method for action
	 * </p>
	 * 
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * <p>
	 * Setter method for action
	 * </p>
	 * 
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * <p>
	 * Getter method for policyId
	 * </p>
	 * 
	 * @return the policyId
	 */
	public Long getPolicyId() {
		return policyId;
	}

	/**
	 * <p>
	 * Setter method for policyId
	 * </p>
	 * 
	 * @param policyId
	 *            the policyId to set
	 */
	public void setPolicyId(Long policyId) {
		this.policyId = policyId;
	}

	/**
	 * <p>
	 * Getter method for policyFullName
	 * </p>
	 * 
	 * @return the policyFullName
	 */
	public String getPolicyFullName() {
		return policyFullName;
	}

	/**
	 * <p>
	 * Setter method for policyFullName
	 * </p>
	 * 
	 * @param policyFullName
	 *            the policyFullName to set
	 */
	public void setPolicyFullName(String policyFullName) {
		this.policyFullName = policyFullName;
	}

	/**
	 * <p>
	 * Getter method for policyName
	 * </p>
	 * 
	 * @return the policyName
	 */
	public String getPolicyName() {
		return policyName;
	}

	/**
	 * <p>
	 * Setter method for policyName
	 * </p>
	 * 
	 * @param policyName
	 *            the policyName to set
	 */
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	/**
	 * <p>
	 * Getter method for policyDecision
	 * </p>
	 * 
	 * @return the policyDecision
	 */
	public String getPolicyDecision() {
		return policyDecision;
	}

	/**
	 * <p>
	 * Setter method for policyDecision
	 * </p>
	 * 
	 * @param policyDecision
	 *            the policyDecision to set
	 */
	public void setPolicyDecision(String policyDecision) {
		this.policyDecision = policyDecision;
	}

	/**
	 * <p>
	 * Getter method for decisionRequestId
	 * </p>
	 * 
	 * @return the decisionRequestId
	 */
	public Long getDecisionRequestId() {
		return decisionRequestId;
	}

	/**
	 * <p>
	 * Setter method for decisionRequestId
	 * </p>
	 * 
	 * @param decisionRequestId
	 *            the decisionRequestId to set
	 */
	public void setDecisionRequestId(Long decisionRequestId) {
		this.decisionRequestId = decisionRequestId;
	}

	/**
	 * <p>
	 * Getter method for logLevel
	 * </p>
	 * 
	 * @return the logLevel
	 */
	public int getLogLevel() {
		return logLevel;
	}

	/**
	 * <p>
	 * Setter method for logLevel
	 * </p>
	 * 
	 * @param logLevel
	 *            the logLevel to set
	 */
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	/**
	 * <p>
	 * Getter method for resourceName
	 * </p>
	 * 
	 * @return the resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * <p>
	 * Setter method for resourceName
	 * </p>
	 * 
	 * @param resourceName
	 *            the resourceName to set
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * <p>
	 * Getter method for resourceSize
	 * </p>
	 * 
	 * @return the resourceSize
	 */
	public Long getResourceSize() {
		return resourceSize;
	}

	/**
	 * <p>
	 * Setter method for resourceSize
	 * </p>
	 * 
	 * @param resourceSize
	 *            the resourceSize to set
	 */
	public void setResourceSize(Long resourceSize) {
		this.resourceSize = resourceSize;
	}

	/**
	 * <p>
	 * Getter method for resourceOwnerId
	 * </p>
	 * 
	 * @return the resourceOwnerId
	 */
	public String getResourceOwnerId() {
		return resourceOwnerId;
	}

	/**
	 * <p>
	 * Setter method for resourceOwnerId
	 * </p>
	 * 
	 * @param resourceOwnerId
	 *            the resourceOwnerId to set
	 */
	public void setResourceOwnerId(String resourceOwnerId) {
		this.resourceOwnerId = resourceOwnerId;
	}

	/**
	 * <p>
	 * Getter method for resourceCreatedDate
	 * </p>
	 * 
	 * @return the resourceCreatedDate
	 */
	public Long getResourceCreatedDate() {
		return resourceCreatedDate;
	}

	/**
	 * <p>
	 * Setter method for resourceCreatedDate
	 * </p>
	 * 
	 * @param resourceCreatedDate
	 *            the resourceCreatedDate to set
	 */
	public void setResourceCreatedDate(Long resourceCreatedDate) {
		this.resourceCreatedDate = resourceCreatedDate;
	}

	/**
	 * <p>
	 * Getter method for resourceModifiedDate
	 * </p>
	 * 
	 * @return the resourceModifiedDate
	 */
	public Long getResourceModifiedDate() {
		return resourceModifiedDate;
	}

	/**
	 * <p>
	 * Setter method for resourceModifiedDate
	 * </p>
	 * 
	 * @param resourceModifiedDate
	 *            the resourceModifiedDate to set
	 */
	public void setResourceModifiedDate(Long resourceModifiedDate) {
		this.resourceModifiedDate = resourceModifiedDate;
	}

	/**
	 * <p>
	 * Getter method for resourcePrefix
	 * </p>
	 * 
	 * @return the resourcePrefix
	 */
	public String getResourcePrefix() {
		return resourcePrefix;
	}

	/**
	 * <p>
	 * Setter method for resourcePrefix
	 * </p>
	 * 
	 * @param resourcePrefix
	 *            the resourcePrefix to set
	 */
	public void setResourcePrefix(String resourcePrefix) {
		this.resourcePrefix = resourcePrefix;
	}

	/**
	 * <p>
	 * Getter method for resourcePath
	 * </p>
	 * 
	 * @return the resourcePath
	 */
	public String getResourcePath() {
		return resourcePath;
	}

	/**
	 * <p>
	 * Setter method for resourcePath
	 * </p>
	 * 
	 * @param resourcePath
	 *            the resourcePath to set
	 */
	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	/**
	 * <p>
	 * Getter method for resourceShortName
	 * </p>
	 * 
	 * @return the resourceShortName
	 */
	public String getResourceShortName() {
		return resourceShortName;
	}

	/**
	 * <p>
	 * Setter method for resourceShortName
	 * </p>
	 * 
	 * @param resourceShortName
	 *            the resourceShortName to set
	 */
	public void setResourceShortName(String resourceShortName) {
		this.resourceShortName = resourceShortName;
	}

	/**
	 * <p>
	 * Getter method for toResourceName
	 * </p>
	 * 
	 * @return the toResourceName
	 */
	public String getToResourceName() {
		return toResourceName;
	}

	/**
	 * <p>
	 * Setter method for toResourceName
	 * </p>
	 * 
	 * @param toResourceName
	 *            the toResourceName to set
	 */
	public void setToResourceName(String toResourceName) {
		this.toResourceName = toResourceName;
	}

	public String getAttr1() {
		return attr1;
	}

	public void setAttr1(String attr1) {
		this.attr1 = attr1;
	}

	public String getAttr2() {
		return attr2;
	}

	public void setAttr2(String attr2) {
		this.attr2 = attr2;
	}

	public String getAttr3() {
		return attr3;
	}

	public void setAttr3(String attr3) {
		this.attr3 = attr3;
	}

	public String getAttr4() {
		return attr4;
	}

	public void setAttr4(String attr4) {
		this.attr4 = attr4;
	}

	public String getAttr5() {
		return attr5;
	}

	public void setAttr5(String attr5) {
		this.attr5 = attr5;
	}

	public String getAttr6() {
		return attr6;
	}

	public void setAttr6(String attr6) {
		this.attr6 = attr6;
	}

	public String getAttr7() {
		return attr7;
	}

	public void setAttr7(String attr7) {
		this.attr7 = attr7;
	}

	public String getAttr8() {
		return attr8;
	}

	public void setAttr8(String attr8) {
		this.attr8 = attr8;
	}

	public String getAttr9() {
		return attr9;
	}

	public void setAttr9(String attr9) {
		this.attr9 = attr9;
	}

	public String getAttr10() {
		return attr10;
	}

	public void setAttr10(String attr10) {
		this.attr10 = attr10;
	}

	public String getAttr11() {
		return attr11;
	}

	public void setAttr11(String attr11) {
		this.attr11 = attr11;
	}

	public String getAttr12() {
		return attr12;
	}

	public void setAttr12(String attr12) {
		this.attr12 = attr12;
	}

	public String getAttr13() {
		return attr13;
	}

	public void setAttr13(String attr13) {
		this.attr13 = attr13;
	}

	public String getAttr14() {
		return attr14;
	}

	public void setAttr14(String attr14) {
		this.attr14 = attr14;
	}

	public String getAttr15() {
		return attr15;
	}

	public void setAttr15(String attr15) {
		this.attr15 = attr15;
	}

	public String getAttr16() {
		return attr16;
	}

	public void setAttr16(String attr16) {
		this.attr16 = attr16;
	}

	public String getAttr17() {
		return attr17;
	}

	public void setAttr17(String attr17) {
		this.attr17 = attr17;
	}

	public String getAttr18() {
		return attr18;
	}

	public void setAttr18(String attr18) {
		this.attr18 = attr18;
	}

	public String getAttr19() {
		return attr19;
	}

	public void setAttr19(String attr19) {
		this.attr19 = attr19;
	}

	public String getAttr20() {
		return attr20;
	}

	public void setAttr20(String attr20) {
		this.attr20 = attr20;
	}

	public String getAttr21() {
		return attr21;
	}

	public void setAttr21(String attr21) {
		this.attr21 = attr21;
	}

	public String getAttr22() {
		return attr22;
	}

	public void setAttr22(String attr22) {
		this.attr22 = attr22;
	}

	public String getAttr23() {
		return attr23;
	}

	public void setAttr23(String attr23) {
		this.attr23 = attr23;
	}

	public String getAttr24() {
		return attr24;
	}

	public void setAttr24(String attr24) {
		this.attr24 = attr24;
	}

	public String getAttr25() {
		return attr25;
	}

	public void setAttr25(String attr25) {
		this.attr25 = attr25;
	}

	public String getAttr26() {
		return attr26;
	}

	public void setAttr26(String attr26) {
		this.attr26 = attr26;
	}

	public String getAttr27() {
		return attr27;
	}

	public void setAttr27(String attr27) {
		this.attr27 = attr27;
	}

	public String getAttr28() {
		return attr28;
	}

	public void setAttr28(String attr28) {
		this.attr28 = attr28;
	}

	public String getAttr29() {
		return attr29;
	}

	public void setAttr29(String attr29) {
		this.attr29 = attr29;
	}

	public String getAttr30() {
		return attr30;
	}

	public void setAttr30(String attr30) {
		this.attr30 = attr30;
	}

	public String getAttr31() {
		return attr31;
	}

	public void setAttr31(String attr31) {
		this.attr31 = attr31;
	}

	public String getAttr32() {
		return attr32;
	}

	public void setAttr32(String attr32) {
		this.attr32 = attr32;
	}

	public String getAttr33() {
		return attr33;
	}

	public void setAttr33(String attr33) {
		this.attr33 = attr33;
	}

	public String getAttr34() {
		return attr34;
	}

	public void setAttr34(String attr34) {
		this.attr34 = attr34;
	}

	public String getAttr35() {
		return attr35;
	}

	public void setAttr35(String attr35) {
		this.attr35 = attr35;
	}

	public String getAttr36() {
		return attr36;
	}

	public void setAttr36(String attr36) {
		this.attr36 = attr36;
	}

	public String getAttr37() {
		return attr37;
	}

	public void setAttr37(String attr37) {
		this.attr37 = attr37;
	}

	public String getAttr38() {
		return attr38;
	}

	public void setAttr38(String attr38) {
		this.attr38 = attr38;
	}

	public String getAttr39() {
		return attr39;
	}

	public void setAttr39(String attr39) {
		this.attr39 = attr39;
	}

	public String getAttr40() {
		return attr40;
	}

	public void setAttr40(String attr40) {
		this.attr40 = attr40;
	}

	public String getAttr41() {
		return attr41;
	}

	public void setAttr41(String attr41) {
		this.attr41 = attr41;
	}

	public String getAttr42() {
		return attr42;
	}

	public void setAttr42(String attr42) {
		this.attr42 = attr42;
	}

	public String getAttr43() {
		return attr43;
	}

	public void setAttr43(String attr43) {
		this.attr43 = attr43;
	}

	public String getAttr44() {
		return attr44;
	}

	public void setAttr44(String attr44) {
		this.attr44 = attr44;
	}

	public String getAttr45() {
		return attr45;
	}

	public void setAttr45(String attr45) {
		this.attr45 = attr45;
	}

	public String getAttr46() {
		return attr46;
	}

	public void setAttr46(String attr46) {
		this.attr46 = attr46;
	}

	public String getAttr47() {
		return attr47;
	}

	public void setAttr47(String attr47) {
		this.attr47 = attr47;
	}

	public String getAttr48() {
		return attr48;
	}

	public void setAttr48(String attr48) {
		this.attr48 = attr48;
	}

	public String getAttr49() {
		return attr49;
	}

	public void setAttr49(String attr49) {
		this.attr49 = attr49;
	}

	public String getAttr50() {
		return attr50;
	}

	public void setAttr50(String attr50) {
		this.attr50 = attr50;
	}

	public String getAttr51() {
		return attr51;
	}

	public void setAttr51(String attr51) {
		this.attr51 = attr51;
	}

	public String getAttr52() {
		return attr52;
	}

	public void setAttr52(String attr52) {
		this.attr52 = attr52;
	}

	public String getAttr53() {
		return attr53;
	}

	public void setAttr53(String attr53) {
		this.attr53 = attr53;
	}

	public String getAttr54() {
		return attr54;
	}

	public void setAttr54(String attr54) {
		this.attr54 = attr54;
	}

	public String getAttr55() {
		return attr55;
	}

	public void setAttr55(String attr55) {
		this.attr55 = attr55;
	}

	public String getAttr56() {
		return attr56;
	}

	public void setAttr56(String attr56) {
		this.attr56 = attr56;
	}

	public String getAttr57() {
		return attr57;
	}

	public void setAttr57(String attr57) {
		this.attr57 = attr57;
	}

	public String getAttr58() {
		return attr58;
	}

	public void setAttr58(String attr58) {
		this.attr58 = attr58;
	}

	public String getAttr59() {
		return attr59;
	}

	public void setAttr59(String attr59) {
		this.attr59 = attr59;
	}

	public String getAttr60() {
		return attr60;
	}

	public void setAttr60(String attr60) {
		this.attr60 = attr60;
	}

	public String getAttr61() {
		return attr61;
	}

	public void setAttr61(String attr61) {
		this.attr61 = attr61;
	}

	public String getAttr62() {
		return attr62;
	}

	public void setAttr62(String attr62) {
		this.attr62 = attr62;
	}

	public String getAttr63() {
		return attr63;
	}

	public void setAttr63(String attr63) {
		this.attr63 = attr63;
	}

	public String getAttr64() {
		return attr64;
	}

	public void setAttr64(String attr64) {
		this.attr64 = attr64;
	}

	public String getAttr65() {
		return attr65;
	}

	public void setAttr65(String attr65) {
		this.attr65 = attr65;
	}

	public String getAttr66() {
		return attr66;
	}

	public void setAttr66(String attr66) {
		this.attr66 = attr66;
	}

	public String getAttr67() {
		return attr67;
	}

	public void setAttr67(String attr67) {
		this.attr67 = attr67;
	}

	public String getAttr68() {
		return attr68;
	}

	public void setAttr68(String attr68) {
		this.attr68 = attr68;
	}

	public String getAttr69() {
		return attr69;
	}

	public void setAttr69(String attr69) {
		this.attr69 = attr69;
	}

	public String getAttr70() {
		return attr70;
	}

	public void setAttr70(String attr70) {
		this.attr70 = attr70;
	}

	public String getAttr71() {
		return attr71;
	}

	public void setAttr71(String attr71) {
		this.attr71 = attr71;
	}

	public String getAttr72() {
		return attr72;
	}

	public void setAttr72(String attr72) {
		this.attr72 = attr72;
	}

	public String getAttr73() {
		return attr73;
	}

	public void setAttr73(String attr73) {
		this.attr73 = attr73;
	}

	public String getAttr74() {
		return attr74;
	}

	public void setAttr74(String attr74) {
		this.attr74 = attr74;
	}

	public String getAttr75() {
		return attr75;
	}

	public void setAttr75(String attr75) {
		this.attr75 = attr75;
	}

	public String getAttr76() {
		return attr76;
	}

	public void setAttr76(String attr76) {
		this.attr76 = attr76;
	}

	public String getAttr77() {
		return attr77;
	}

	public void setAttr77(String attr77) {
		this.attr77 = attr77;
	}

	public String getAttr78() {
		return attr78;
	}

	public void setAttr78(String attr78) {
		this.attr78 = attr78;
	}

	public String getAttr79() {
		return attr79;
	}

	public void setAttr79(String attr79) {
		this.attr79 = attr79;
	}

	public String getAttr80() {
		return attr80;
	}

	public void setAttr80(String attr80) {
		this.attr80 = attr80;
	}

	public String getAttr81() {
		return attr81;
	}

	public void setAttr81(String attr81) {
		this.attr81 = attr81;
	}

	public String getAttr82() {
		return attr82;
	}

	public void setAttr82(String attr82) {
		this.attr82 = attr82;
	}

	public String getAttr83() {
		return attr83;
	}

	public void setAttr83(String attr83) {
		this.attr83 = attr83;
	}

	public String getAttr84() {
		return attr84;
	}

	public void setAttr84(String attr84) {
		this.attr84 = attr84;
	}

	public String getAttr85() {
		return attr85;
	}

	public void setAttr85(String attr85) {
		this.attr85 = attr85;
	}

	public String getAttr86() {
		return attr86;
	}

	public void setAttr86(String attr86) {
		this.attr86 = attr86;
	}

	public String getAttr87() {
		return attr87;
	}

	public void setAttr87(String attr87) {
		this.attr87 = attr87;
	}

	public String getAttr88() {
		return attr88;
	}

	public void setAttr88(String attr88) {
		this.attr88 = attr88;
	}

	public String getAttr89() {
		return attr89;
	}

	public void setAttr89(String attr89) {
		this.attr89 = attr89;
	}

	public String getAttr90() {
		return attr90;
	}

	public void setAttr90(String attr90) {
		this.attr90 = attr90;
	}

	public String getAttr91() {
		return attr91;
	}

	public void setAttr91(String attr91) {
		this.attr91 = attr91;
	}

	public String getAttr92() {
		return attr92;
	}

	public void setAttr92(String attr92) {
		this.attr92 = attr92;
	}

	public String getAttr93() {
		return attr93;
	}

	public void setAttr93(String attr93) {
		this.attr93 = attr93;
	}

	public String getAttr94() {
		return attr94;
	}

	public void setAttr94(String attr94) {
		this.attr94 = attr94;
	}

	public String getAttr95() {
		return attr95;
	}

	public void setAttr95(String attr95) {
		this.attr95 = attr95;
	}

	public String getAttr96() {
		return attr96;
	}

	public void setAttr96(String attr96) {
		this.attr96 = attr96;
	}

	public String getAttr97() {
		return attr97;
	}

	public void setAttr97(String attr97) {
		this.attr97 = attr97;
	}

	public String getAttr98() {
		return attr98;
	}

	public void setAttr98(String attr98) {
		this.attr98 = attr98;
	}

	public String getAttr99() {
		return attr99;
	}

	public void setAttr99(String attr99) {
		this.attr99 = attr99;
	}

}
