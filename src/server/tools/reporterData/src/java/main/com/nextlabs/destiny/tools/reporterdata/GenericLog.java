/*
 * Created on Dec 22, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;

import java.util.LinkedList;
import java.util.List;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/GenericLog.java#1 $
 */

public class GenericLog {
	private Integer id;	//this is not the databaseId
	
	private String fromResourceName;
	private Long fromResourceSize;
	private Long fromResourceCreatedDate;
	private Long fromResourceModifiedDate;
	private String fromResourceOwnerId;
	private String toResource; 
	private IUser user; 
	private IHost host; 
	private String hostIP;
	private String applicationName; 
	private Long applicationId; 
	private ActionEnumType action;
	private PolicyDecisionEnumType policyDecision; 
	private Long decisionRequestId; 
	private Long ts; 
	private Integer level;
    
	private DynamicAttributes dynamicAttributes = new DynamicAttributes();
	private List<PolicyAssistantLogEntry> policyAssistantLogEntires = new LinkedList<PolicyAssistantLogEntry>();
    
	private IPolicy policy;
    
	public int getId() {
		return id;
	}
	public void setId(int id) throws AlreadyDefinedException {
		if (this.id != null && this.id != id) {
			throw new AlreadyDefinedException("id", this.id, id);
		}
		this.id = id;
	}
	
	public String getFromResourceName() {
		return fromResourceName;
	}
	public void setFromResourceName(String fromResourceName) {
		this.fromResourceName = fromResourceName;
	}
	public Long getFromResourceSize() {
		return fromResourceSize;
	}
	public void setFromResourceSize(Long fromResourceSize) {
		this.fromResourceSize = fromResourceSize;
	}
	public Long getFromResourceCreatedDate() {
		return fromResourceCreatedDate;
	}
	public void setFromResourceCreatedDate(Long fromResourceCreatedDate) {
		this.fromResourceCreatedDate = fromResourceCreatedDate;
	}
	public Long getFromResourceModifiedDate() {
		return fromResourceModifiedDate;
	}
	public void setFromResourceModifiedDate(Long fromResourceModifiedDate) {
		this.fromResourceModifiedDate = fromResourceModifiedDate;
	}
	public String getFromResourceOwnerId() {
		return fromResourceOwnerId;
	}
	public void setFromResourceOwnerId(String fromResourceOwnerId) {
		this.fromResourceOwnerId = fromResourceOwnerId;
	}
	public String getToResource() {
		return toResource;
	}
	public void setToResource(String toResource) {
		this.toResource = toResource;
	}
	public IUser getUser() {
		return user;
	}
	public void setUser(IUser user) {
		this.user = user;
	}
	public IHost getHost() {
		return host;
	}
	public void setHost(IHost host) {
		this.host = host;
	}
	public String getHostIP() {
		return hostIP;
	}
	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public Long getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}
	public ActionEnumType getAction() {
		return action;
	}
	public void setAction(ActionEnumType action) {
		this.action = action;
	}
	public PolicyDecisionEnumType getPolicyDecision() {
		return policyDecision;
	}
	public void setPolicyDecision(PolicyDecisionEnumType policyDecision) {
		this.policyDecision = policyDecision;
	}
	public Long getDecisionRequestId() {
		return decisionRequestId;
	}
	public void setDecisionRequestId(Long decisionRequestId) {
		this.decisionRequestId = decisionRequestId;
	}
	public Long getTs() {
		return ts;
	}
	public void setTs(Long ts) {
		this.ts = ts;
	}
	public Integer getLevel() {
		return level;
	}
	public void setLevel(Integer level) {
		this.level = level;
	}
	public IPolicy getPolicy() {
		return policy;
	}
	public void setPolicy(IPolicy policy) {
		this.policy = policy;
	}
    public void addDynamicAttribute(String key, String value){
    	dynamicAttributes.put(key, value);
    }
	public DynamicAttributes getDynamicAttributes() {
		return dynamicAttributes;
	}
	public void addPolicyAssistantLogEntry(PolicyAssistantLogEntry log){
		policyAssistantLogEntires.add(log);
	}
	public List<PolicyAssistantLogEntry> getPolicyAssistantLogEntires() {
		return policyAssistantLogEntires;
	}
	
	public FromResourceInformation getFromResourceInformation(){
		return new FromResourceInformation(
				fromResourceName,	//String name, 
				fromResourceSize,	//long size, 
				fromResourceCreatedDate,	//long createdDate, 
				fromResourceModifiedDate,	//long modifiedDate, 
				fromResourceOwnerId		//String ownerId
		);
	}
	
	public ToResourceInformation getToResourceInformation(){
		return toResource != null ? new ToResourceInformation(toResource) : null;
	}
	
	public PolicyActivityLogEntry getPolicyActivityLog(long id){
		return new PolicyActivityLogEntry(
			new PolicyActivityInfo(
					getFromResourceInformation(), 	//FromResourceInformation fromResourceInfo, 
					getToResourceInformation(), 	//ToResourceInformation toResourceInfo, 
					user.getDisplayName(), 			//String userName, 
					user.getOriginalId(), 			//long userId, 
					host.getName(), 				//String hostName, 
					hostIP, 						//String hostIP,
					host.getOriginalId(),			//long hostId, 
					applicationName,				//String applicationName, 
					applicationId,					//long applicationId, 
					action,							//ActionEnumType action,
					policyDecision,					//PolicyDecisionEnumType policyDecision, 
					decisionRequestId,			 	//long decisionRequestId, 
					ts, 							//long ts, 
					level,							//int level, 
					dynamicAttributes				//DynamicAttributes customAttr
			),
			policy.getId(),
			id
		);
	}
	
	public List<PolicyAssistantLogEntry> getPolicyAssistantLogs(long assistantLogIdStartFrom,
			long policyLogId) {
		List<PolicyAssistantLogEntry> logs = new LinkedList<PolicyAssistantLogEntry>();
		for(PolicyAssistantLogEntry entry : policyAssistantLogEntires){
			logs.add(new PolicyAssistantLogEntry(
					Long.toString(policyLogId),
					entry.getAssistantName(), 
					entry.getAttrOne(),		
					entry.getAttrTwo(),
					entry.getAttrThree(),	
					assistantLogIdStartFrom++,
					entry.getTimestamp()
					
			));
		}
		return logs;
	}
	
	public TrackingLogEntry getTrackingLog(long id){
		return new TrackingLogEntry(
				getFromResourceInformation(),	//FromResourceInformation fromResourceInfo, 
				getToResourceInformation(), 		//ToResourceInformation toResourceInfo, 
				user.getDisplayName(), 			//String userName, 
				user.getOriginalId(), 			//long userId, 
				host.getName(), 				//String hostName, 
				hostIP, 						//String hostIP,
				host.getOriginalId(),			//long hostId, 
				applicationName,				//String applicationName, 
				applicationId,					//long applicationId, 
				action,							//ActionEnumType action,
				id,
				ts, 							//long ts, 
				level,							//int level, 
				dynamicAttributes				//DynamicAttributes customAttr
		);
	}
}
