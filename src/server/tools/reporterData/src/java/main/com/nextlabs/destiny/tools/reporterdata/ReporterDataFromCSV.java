/*
 * Created on Dec 10, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import com.bluejungle.destiny.container.dabs.components.log.hibernateimpl.HibernateLogWriter;
import com.bluejungle.destiny.container.shared.agentmgr.IActionType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IHost;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IPolicy;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.IUser;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.Pair;
import com.nextlabs.destiny.tools.reporterdata.ReporterDataFromCSVConfig.CsvColumn;
import com.nextlabs.destiny.tools.reporterdata.ReporterDataFromCSVConfig.FormatTemplate;

import static com.nextlabs.destiny.tools.reporterdata.ReporterDataFromCSVConfig.CsvColumn.*;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/ReporterDataFromCSV.java#1 $
 */

public class ReporterDataFromCSV extends ReporterDataBase{
	protected static final int FLUSH_EVERY_N_RECORDS = 1000;
	
	protected static final String[] EMPTY_STRINGS = new String[]{""};
	
	private final Session s;
	private final HibernateLogWriter hibernateLogWriter;
	private final ReporterDataFromCSVConfig config;
	
	protected Map<Integer, GenericLog> dataMap = new HashMap<Integer, GenericLog>();
	
	Map<String, ActionEnumType> actionMap;
	
	public ReporterDataFromCSV(Session s, HibernateLogWriter hibernateLogWriter,
			ReporterDataFromCSVConfig config) throws IOException {
		super(false);
		this.s = s;
		this.hibernateLogWriter = hibernateLogWriter;
		this.config = config;
		actionMap = constractActionMap();
		actionMap.put("Rename".toLowerCase(), ActionEnumType.ACTION_RENAME);
	}
	
	protected Map<String, ActionEnumType> constractActionMap(){
		Map<String, ActionEnumType> map = new HashMap<String, ActionEnumType>();
		AgentManager agentManager = new AgentManager();
		for(IAgentType agentType : agentManager.getAgentTypes()){
			for(IActionType actionType : agentType.getActionTypes()){
				String key = actionType.getTitle();
				ActionEnumType action = ActionEnumType.getActionEnum(actionType.getId());
				
				ActionEnumType exitingAction = map.get(key);
				if(exitingAction == null){
					map.put(key.toLowerCase(), action);
				}else if (! exitingAction.equals(action) ) {
					throw new IllegalArgumentException(key + " has two meaning "
							+ exitingAction + "," + action);
				}else{
					//ignore, same action
				}
			}
		}
		return map;
	}
	
	protected ActionEnumType getAction(String name){
		ActionEnumType a = actionMap.get(name.toLowerCase());
		if(a == null){
			throw new IllegalArgumentException("unknown action: " + name);
		}
		return a;
	}
	
	private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");
	

	
	public void parseDataFile(File dataFile) throws Exception{
		Map<String, ActionEnumType> actionMap = constractActionMap();
		actionMap.put("Rename", ActionEnumType.ACTION_RENAME);
		
		final List<CsvColumn> csvColumnOrder = config.getCsvOrder();
		
		BufferedReader reader = null;
		String line = null;
		int lineNumber = 1;
		int colunnIndex = -1;
		try {
			reader = new BufferedReader(new FileReader(dataFile));
			//read header and ignore it
			if(config.isIgnoreHeaderRow()){
				reader.readLine();
				lineNumber++;
			}
			
			LINE_LOOP:
			while ((line = reader.readLine()) != null) {
				lineNumber++;
				
				//ignore if the line is empty
				if(line.trim().length() == 0){
					continue;
				}
				
				String[] values = line.split(config.getCsvSeperater());
				
				GenericLog log = new GenericLog();
				
				colunnIndex = 0;
				for (int i = 0; i < csvColumnOrder.size(); i++, colunnIndex++) {
					if(colunnIndex >= values.length){
						String message = "I don't have enought value";
						if(config.isStopOnUnknownLine()){
							throw new IllegalArgumentException(message);
						}else{
							System.err.println(message + ", line " + lineNumber + " is skipped");
							continue LINE_LOOP;
						}
						
					}
					String value = values[colunnIndex].trim();
					switch (csvColumnOrder.get(i)) {
					case ID: 
						try {
							log.setId(Integer.parseInt(value));
						} catch (NumberFormatException e) {
							String merrorMessage =
									"I can't understand this line " + lineNumber + ": " + line;
							if(config.isStopOnUnknownLine()){
								throw new IllegalArgumentException(merrorMessage);
							}else{
								System.err.println(merrorMessage);
								continue LINE_LOOP;
							}
						}
						break;
					case AGENT_ACTION:
						log.setAction(getAction( value) );
						break;
					case DB_ACTION:
						//TODO
						break;
					case LOG_TIME: {
						Date date = config.getFormat(CsvColumn.LOG_TIME).getDateFormat().parse(value);
						log.setTs(date.getTime());
					}
						break;
					case LOG_TIMESTAMP_MS:
						log.setTs(Long.parseLong(value));
						break;
					case POLICY_ID:
						log.setPolicy(getPolicy(Long.parseLong(value), s));
						break;
					case POLICY_FULL_NAME:
						if(value.length() > 0 ){
							IPolicy policy = createOrGetPolicies(value, s);
							log.setPolicy(policy);
						}
						break;
					case ENFORCEMENT:
						final PolicyDecisionEnumType enforcement;
						if (value.equalsIgnoreCase(PolicyDecisionEnumType.POLICY_DECISION_DENY.getName())
								|| value.equalsIgnoreCase("d")) {
							enforcement = PolicyDecisionEnumType.POLICY_DECISION_DENY;
						} else if (value.equalsIgnoreCase(PolicyDecisionEnumType.POLICY_DECISION_ALLOW.getName())
								|| value.equalsIgnoreCase("a")
								|| value.equalsIgnoreCase("Allow/Monitor")) {
							enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
						}else{
							String merrorMessage = "I can't understand enforcement value: " 
								+ value + ", skip to next line";
							if(config.isStopOnUnknownValue()){
								throw new IllegalArgumentException(merrorMessage);
							}else{
								System.err.println(merrorMessage);
								continue LINE_LOOP;
							}
						}
						log.setPolicyDecision(enforcement);
						break;
					case USER_ID:{
						IUser user = getUser( Long.parseLong(value), s);
						log.setUser(user);
					}	
						break;
					case USER_DISPLAY_NAME: {
						IUser user = getUser(value, s);
						log.setUser(user);
					}
						break;
					case LOG_LEVEL: 
						log.setLevel(Integer.parseInt(value));
						break;
					case FROM_RESOURCE_NAME: 
						log.setFromResourceName(value);
						break;
					case FROM_RESOURCE_LEGNTH:
						log.setFromResourceSize(Long.parseLong(value));
						break;
					case FROM_RESOURCE_CREATED_DATE: {
						Date date =	config.getFormat(FROM_RESOURCE_CREATED_DATE).getDateFormat().parse(
										value);
						log.setFromResourceCreatedDate(date.getTime());
					}
						break;
					case FROM_RESOURCE_CREATED_TIMESTAMP_MS: 
						log.setFromResourceCreatedDate(Long.parseLong(value));
						break;
					case FROM_RESOURCE_MODIFIED_DATE: {
						Date date =	config.getFormat(FROM_RESOURCE_MODIFIED_DATE).getDateFormat()
										.parse(value);
						log.setFromResourceModifiedDate(date.getTime());
					}
						break;
					case FROM_RESOURCE_MODIFIED_TIMESTAMP_MS:
						log.setFromResourceModifiedDate(Long.parseLong(value));
						break;
					case FROM_RESOURCE_OWNER_ID: 
						log.setFromResourceOwnerId(value);
						break;
					case TO_RESOURCE_NAME:
						if(value.length() == 0){
							log.setToResource(config.getDefault(TO_RESOURCE_NAME).getString(log));
						}else{
							log.setToResource(value);
						}
						break;
					case HOST_NAME:{
						IHost host = getHost(value, s);
						log.setHost(host);
					}
						break;
					case HOST_ID: {
						IHost host = getHost(Long.parseLong(value), s);
						log.setHost(host);
					}
						break;
					case IP_ADDRESS: {
						Matcher m = IP_ADDRESS_PATTERN.matcher(value);
						if (m.matches()) {
							String ipAddress = m.group(1);
							log.setHostIP(ipAddress);
						} else {
							throw new IllegalFormatException(IP_ADDRESS.name(), IP_ADDRESS_PATTERN
									.pattern(), value);
						}
					}
						break;
					case APPLICATION_NAME: 
						log.setApplicationName(value);
						break;
					case APPLICARION_ID: 
						log.setApplicationId(Long.parseLong(value));
						break;
					case DECISION_REQUEST_ID:
						log.setDecisionRequestId(Long.parseLong(value));
						break;
					case CUSTOM_ATTR: {
						List<CsvColumn> customAttrOrder = config.getCustomAttributeOrder();
						String tempKey = null;
						String tempValue = null;
						for (int j = 0; j < customAttrOrder.size(); j++) {
							switch(customAttrOrder.get(j)){
							case CUSTOM_ATTR_KEY_VALUE: 
								String format = config.getFormat(CUSTOM_ATTR_KEY_VALUE).getTemplateStr();
								int keyTokenIndex = format.indexOf(FormatTemplate.getToken(CUSTOM_ATTR_KEY));
								if (keyTokenIndex >= 0) {
									format = format.replace(FormatTemplate.getToken(CUSTOM_ATTR_KEY), "(.*)");
								}
								
								int valueTokenIndex = format.indexOf(FormatTemplate.getToken(CUSTOM_ATTR_VALUE));
								if (valueTokenIndex >= 0) {
									format = format.replace(FormatTemplate.getToken(CUSTOM_ATTR_VALUE), "(.*)");
								}
								
								//TODO constraint at this moment
								if(keyTokenIndex < 0 || valueTokenIndex <0){
									throw new IllegalFormatException(CUSTOM_ATTR_KEY_VALUE.name(),
											CUSTOM_ATTR_KEY + " + " + CUSTOM_ATTR_VALUE, format);
								}
								
								Matcher m = Pattern.compile(format).matcher(value);
								if (m.matches()) {
									int keyTokenGroupId;
									int valueTokenGroupId;
									if(keyTokenIndex < valueTokenIndex){
										keyTokenGroupId = 1;
										valueTokenGroupId = 2;
									}else{
										keyTokenGroupId = 2;
										valueTokenGroupId = 1;
									}
									
									tempKey = m.group(keyTokenGroupId);
									tempValue = m.group(valueTokenGroupId);
								} else {
									throw new IllegalFormatException(CUSTOM_ATTR_KEY_VALUE.name(), format, value);
								}
								break;
							case CUSTOM_ATTR_KEY:
								tempKey = value;
								break;
							case CUSTOM_ATTR_VALUE:
								tempValue = value;
								break;
							case IGNORE: 
								//ignore
								break;
							default:
								throw new IllegalArgumentException("unknown column: "
										+ customAttrOrder.get(j));
							}
							if(tempKey == null){
								tempKey = config.getDefault(CUSTOM_ATTR_KEY).getString(log);
							}
							if(tempValue == null){
								tempValue = config.getDefault(CUSTOM_ATTR_VALUE).getString(log);
							}
							
							if (tempKey == null 
									&& tempValue == null 
									&& config.getDefault(CUSTOM_ATTR).getString(log) != null) {
								throw new NotFoundException(CUSTOM_ATTR.name(), "customAttribute", "not null");
							}
							
							log.addDynamicAttribute(tempKey, tempValue);
							if (j < customAttrOrder.size() - 1) {
								value = values[++colunnIndex];
							}
						}
					}
						break;
					case OBLIGATION_LOG:{
						String tempName = null;
						String tempAttr1 = null;
						String tempAttr2 = null;
						String tempAttr3 = null;
						Long tempTime = null;
						
						
						List<CsvColumn> obligationLogOrder = config.getObligationLogORder();
						for (int j = 0; j < obligationLogOrder.size(); j++) {
							switch (obligationLogOrder.get(j)) {
							case OBLIGATION_LOG_NAME:
								tempName = value;
								break;
							case OBLIGATION_LOG_ATTR_ONE:
								tempAttr1 = value;
								break;
							case OBLIGATION_LOG_ATTR_TWO:
								tempAttr2 = value;
								break;
							case OBLIGATION_LOG_ATTR_THREE:
								tempAttr3 = value;
								break;
							case OBLIGATION_LOG_TIME:
								tempTime = config.getFormat(CsvColumn.OBLIGATION_LOG_TIME)
												.getDateFormat().parse(value).getTime();
								break;
							case OBLIGATION_LOG_TIMESTAMP_MS:
								tempTime = Long.parseLong(value);
								break;
							case IGNORE:
								//ignore
								break;
							default:
								throw new IllegalArgumentException("unknown column: "
										+ obligationLogOrder.get(j));
							}
							if (j < obligationLogOrder.size() - 1) {
								value = values[++colunnIndex];
							}
						}
						if (tempName == null) {
							tempName = config.getDefault(OBLIGATION_LOG_NAME).getString(log);
						}
						if (tempAttr1 == null) {
							tempAttr1 = config.getDefault(OBLIGATION_LOG_ATTR_ONE).getString(log);
						}
						if (tempAttr2 == null) {
							tempAttr2 = config.getDefault(OBLIGATION_LOG_ATTR_TWO).getString(log);
						}
						if (tempAttr3 == null) {
							tempAttr3 = config.getDefault(OBLIGATION_LOG_ATTR_THREE).getString(log);
						}
						if (tempTime == null) {
							tempTime = config.getDefault(OBLIGATION_LOG_TIME).getLong(log);
						}
						
						if (tempName == null 
								&& tempTime == null 
								&& config.getDefault(OBLIGATION_LOG).getString(log) != null) {
							throw new NotFoundException(OBLIGATION_LOG.name(), "obligationLog", "not null");
						} 
						
						log.addPolicyAssistantLogEntry(new PolicyAssistantLogEntry(Long.toString(0),
								tempName, 
								tempAttr1,		//attr_two
								tempAttr2,	//attr_one
								tempAttr3,	//attr_three
								0,
								tempTime));
					}
						break;
					case IGNORE: 
						//ignore
						break;
					default:
						throw new IllegalArgumentException("unknown column: " + csvColumnOrder.get(i));
					}
				}
				
				patchWithDefaulitValue(log);
				
				dataMap.put(log.getId(), log);
			}
		}catch(Exception e){
			System.err.println("Error on line " + lineNumber + ", column # " + colunnIndex);
			System.err.println("Reading line: " + line);
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private void patchWithDefaulitValue(GenericLog log) {
		//put default value
		if (log.getTs() == null) {
			log.setTs(config.getDefault(LOG_TIME).getLong(log));
		}

		if (log.getLevel() == null) {
			log.setLevel(config.getDefault(LOG_LEVEL).getInteger(log));
		}

		if (log.getApplicationName() == null) {
			log.setApplicationName(config.getDefault(APPLICATION_NAME).getString(log));
		}

		if (log.getApplicationId() == null) {
			log.setApplicationId(config.getDefault(APPLICARION_ID).getLong(log));
		}
		
		if(log.getDecisionRequestId() == null){
			log.setDecisionRequestId(config.getDefault(DECISION_REQUEST_ID).getLong(log));
		}
		
		
		//TODO default POLICY
		//TODO default USER
		//TODO default HOST
		
		if (log.getHostIP() == null) {
			log.setHostIP(config.getDefault(IP_ADDRESS).getString(log));
		}

		if (log.getFromResourceName() == null) {
			log.setFromResourceName(config.getDefault(FROM_RESOURCE_NAME).getString(log));
		}

		if (log.getFromResourceSize() == null) {
			log.setFromResourceSize(config.getDefault(FROM_RESOURCE_LEGNTH).getLong(log));
		}
		
		if (log.getFromResourceCreatedDate() == null) {
			log.setFromResourceCreatedDate(config.getDefault(FROM_RESOURCE_CREATED_DATE).getLong(log));
		}
			
		if (log.getFromResourceModifiedDate() == null) {
			log.setFromResourceModifiedDate(config.getDefault(FROM_RESOURCE_MODIFIED_DATE).getLong(log));
		}
		
		if (log.getFromResourceOwnerId() == null) {
			log.setFromResourceOwnerId(config.getDefault(FROM_RESOURCE_OWNER_ID).getString(log));
		}
		
		if (log.getToResource() == null) {
			log.setToResource(config.getDefault(TO_RESOURCE_NAME).getString(log));
		}
		
		//TODO default action
	}
	
	public void insertData() throws HibernateException, DataSourceException{
		int totalNumberOfLogs = 0; 
		
		//check all log id exist
		for(Pair<Integer, Integer> insertionPerLog : config.getInsertionList()){
			int logId = insertionPerLog.first();
			int times = insertionPerLog.second();
			totalNumberOfLogs += times;
			
			if (!dataMap.containsKey(logId)) {
				throw new IllegalArgumentException("There is no such log id " + logId);
			}
			
			if (times < 0) {
				throw new IllegalArgumentException("Can't insert logId " + logId
						+ " negative time " + times);
			}
		}
		
		System.out.println("I am going to insert " + totalNumberOfLogs + " logs");
		
		final String getMaxIdHqlTemplate = "select max(p.id) from %s p"; 
		
		Long policyLogId = (Long) s.createQuery(String.format(getMaxIdHqlTemplate, 
				"TestPolicyActivityLogEntryDO")).uniqueResult();
		if(policyLogId == null){
			policyLogId = 1L;
		}
		
	
		Long trackingLogId = (Long) s.createQuery(String.format(getMaxIdHqlTemplate, 
				"TestTrackingActivityLogEntryDO")).uniqueResult();
		if(trackingLogId == null){
			trackingLogId = 1L;
		}
		
		Long policyAssistantLogId = (Long) s.createQuery(String.format(getMaxIdHqlTemplate, 
				"PolicyAssistantLogDO")).uniqueResult();
		if(policyAssistantLogId == null){
			policyAssistantLogId = 1L;
		}
		
		s.close();
		
		for(Pair<Integer, Integer> insertionPerLog : config.getInsertionList()){
			GenericLog genericLog = dataMap.get(insertionPerLog.first());
			Map<LogRowType, List<BaseLogEntry>> unsavedData = new HashMap<LogRowType, List<BaseLogEntry>>();
			List<PolicyAssistantLogEntry> unsavedPolicyAssistantData = new LinkedList<PolicyAssistantLogEntry>();
			
			for( int i = 0; i < insertionPerLog.second(); i++){
				if (config.isTrackingLogIfNoPolicy() && genericLog.getPolicy() == null) {
					List<BaseLogEntry> logs = unsavedData.get(LogRowType.TRACKING);
					if (logs == null) {
						logs = new LinkedList<BaseLogEntry>();
						unsavedData.put(LogRowType.TRACKING, logs);
					}
					logs.add(genericLog.getTrackingLog(++trackingLogId));
				}else{
					List<BaseLogEntry> logs = unsavedData.get(LogRowType.POLICY);
					if (logs == null) {
						logs = new LinkedList<BaseLogEntry>();
						unsavedData.put(LogRowType.POLICY, logs);
					}
					logs.add(genericLog.getPolicyActivityLog(++policyLogId));
					List<PolicyAssistantLogEntry> policyAssisantLogs =
							genericLog.getPolicyAssistantLogs(++policyAssistantLogId, policyLogId);
					policyAssistantLogId += policyAssisantLogs.size();
					unsavedPolicyAssistantData.addAll(policyAssisantLogs);
				}
			}
			
			//flush
			for(Map.Entry<LogRowType, List<BaseLogEntry>> entry : unsavedData.entrySet()){
				switch (entry.getKey()) {
				case POLICY:
					hibernateLogWriter.log(entry.getValue().toArray(new PolicyActivityLogEntry[] {}));
					break;
				case TRACKING:
					hibernateLogWriter.log(entry.getValue().toArray(new TrackingLogEntry[] {}));
					break;
				default:
					throw new IllegalArgumentException("unknown logRowType: " + entry.getKey());
				}
			}
			unsavedData.clear();

			if (unsavedPolicyAssistantData.size() > 0) {
				hibernateLogWriter.log(unsavedPolicyAssistantData.toArray(new PolicyAssistantLogEntry[] {}));
				unsavedPolicyAssistantData.clear();
			}
		}
	}
	
	protected PolicyAssistantLogEntry clone(PolicyAssistantLogEntry entry, long id, long policyLogId){
		return new PolicyAssistantLogEntry(
				Long.toString(policyLogId),
				entry.getAssistantName(), 
				entry.getAttrOne(),		
				entry.getAttrTwo(),
				entry.getAttrThree(),	
				id,
				entry.getTimestamp()
				
		);
	}
	
	enum LogRowType{
		POLICY,
		TRACKING,
	}
}
