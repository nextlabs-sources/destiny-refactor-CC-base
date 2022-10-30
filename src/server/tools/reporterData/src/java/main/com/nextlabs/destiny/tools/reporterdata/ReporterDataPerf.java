/*
 * Created on Oct 6, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import net.sf.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dabs.components.log.hibernateimpl.HibernateLogWriter;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HostDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserDO;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.action.hibernateimpl.ActionEnumUserType;
import com.bluejungle.domain.log.BaseLogEntry;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.nextlabs.domain.log.PolicyActivityInfoV5;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.random.TemplateToken;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.display.ProgressBar;

/**
 * TODO 
 *  list of possible parameters
 *   - % of actionEnumTypes
 *   - distribution of selected policy
 *   - resource file size
 *   - sid generator;
 *   - from resource, to resource percentage
 *   - number of applications
 *   - ip address generator
 *   - active distribution
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/ReporterDataPerf.java#1 $
 */
class ReporterDataPerf extends ReporterDataBase{
	protected static final String[] EMPTY_STRINGS = new String[]{""};
	private static final Log LOG = LogFactory.getLog(ReporterDataPerf.class);
	private static final int DEFAULT_FLUSH_FREQUENCY = 1000;

	private final Session s;
	private final HibernateLogWriter hibernateLogWriter;
	private final ReporterDataPerfConfig perfConfig;

	protected int flushEveryNRecords = 1000;
	private volatile long totalLogsInserted;
	
	
	public ReporterDataPerf(Session s, HibernateLogWriter hibernateLogWriter,
			ReporterDataPerfConfig perfConfig) throws IOException {
		super(true);
		totalLogsInserted = 0;
		this.s = s;
		this.hibernateLogWriter = hibernateLogWriter;
		this.perfConfig = perfConfig;
		this.flushEveryNRecords = DEFAULT_FLUSH_FREQUENCY;
	}

	public void setFlushFrequency(int everyNRecords){
		flushEveryNRecords = everyNRecords;
	}
	
	public void createPerformanceData()
			throws Exception{
		final long startTime = System.currentTimeMillis();
		LOG.info("perparing insert data.");
		
		final boolean isInteractive = perfConfig.isInteractive();
		final boolean isHacker = perfConfig.isHacker();
		//prepare
		final UserDO[] users = getSpecificSizeOfObjects(s, perfConfig.getNumberOfUsers(), 
				UserDO.class, isInteractive, isHacker).toArray(new UserDO[]{});
		final HostDO[] hosts = getSpecificSizeOfObjects(s, perfConfig.getNumberOfHosts(), 
				HostDO.class, isInteractive, isHacker).toArray(new HostDO[]{});
		final int[] logLevelDistribution = new int[100];
		final int[] logLevelDistrubutionConfig = perfConfig.getLogLevelDistrubution();
		int index =0;
		for(int logLevel = 0; logLevel < logLevelDistrubutionConfig.length; logLevel++){
			for(int i=0 ; i< logLevelDistrubutionConfig[logLevel]; i++){
				logLevelDistribution[index++] = logLevel + 1;
			}
		}
		
		//if it is a tracking log, the policies is not required.
		final long[] policyIds;
		if(perfConfig.isPolicyLogs() ){
			List<PolicyDO> policies = getSpecificSizeOfObjects(s, perfConfig.getNumberOfPolicies(), 
					PolicyDO.class,	isInteractive, isHacker);
			
			policyIds = new long[policies.size()];
			for (int i = 0; i < policyIds.length; i++) {
				policyIds[i] = policies.get(i).getId();
			}
		}else{
			policyIds = null;
		}

		final String[] fromResources = gernerateStringsFromTemplates(
				perfConfig.getNumberOfFromResources(), 
				perfConfig.getFromResourceTemplates(), 
				perfConfig.getFromResourceValueTokens());
		
		final String[] toResources = gernerateStringsFromTemplates(
				perfConfig.getNumberOfToResources(), 
				perfConfig.getToResourceTemplates(), 
				perfConfig.getToResourceValueTokens());

		final String[] applications = gernerateStringsFromTemplates(
				perfConfig.getNumberOfApplications(), 
				perfConfig.getApplicationTemplates(), 
				perfConfig.getApplicationValueTokens());
		
		initActions();

		final SortedMap<Calendar, Integer> days = new TreeMap<Calendar, Integer>();

		final long totalNumberOfLogsWillBeInserted = creatLogsPerDayMap(perfConfig, days);
		
		final float numberOfObligationLog = perfConfig.getObligationsPerLog();
		
		final String[] obligationAttrOne;
		final String[] obligationAttrTwo;
		final String[] obligationAttrThree;
		
		if (numberOfObligationLog > 0) {
			obligationAttrOne = gernerateStringsFromTemplates(
					perfConfig.getNumberOfObligationAttrOne(), 
					perfConfig.getObligationAttrOneTemplates(), 
					perfConfig.getObligationAttrOneValueTokens());
			
			obligationAttrTwo = gernerateStringsFromTemplates(
					perfConfig.getNumberOfObligationAttrTwo(), 
					perfConfig.getObligationAttrTwoTemplates(), 
					perfConfig.getObligationAttrTwoValueTokens());
			
			obligationAttrThree = gernerateStringsFromTemplates(
					perfConfig.getNumberOfObligationAttrThree(), 
					perfConfig.getObligationAttrThreeTemplates(), 
					perfConfig.getObligationAttrThreeValueTokens());
		} else {
			//those are not using
			obligationAttrOne = null;
			obligationAttrTwo = null;
			obligationAttrThree = null;
		}
		
		//preparation is done
		
		LOG.info("I am going to insert " + totalNumberOfLogsWillBeInserted + " rows");

		Timer timer = new Timer(true);
		final int preMessageLength = 20;
		final int postMessageLength = 22;
		final int barMinLength = 20;
        int barLength = ConsoleDisplayHelper.getScreenWidth() - preMessageLength - postMessageLength;
        if(barLength < barMinLength){
            barLength =  barMinLength;
        }
		final ProgressBar progressBar = new ProgressBar(preMessageLength, barMinLength, postMessageLength);
		progressBar.start();
		
		timer.schedule(new TimerTask(){
            @Override
            public void run() {
                // update the UI
                progressBar.update((float) totalLogsInserted / totalNumberOfLogsWillBeInserted);
                progressBar.setPerMessage("inserted " + totalLogsInserted);
                progressBar.setPostMessage("Time left: "
                        + ConsoleDisplayHelper.formatTime(progressBar.getOverallEstiimateTimeLeft()));
                ConsoleDisplayHelper.redraw(progressBar);
            }
		}, 0, 1000);
		
		
		try {
			//get the latest database unique id
			final String getMaxIdHqlTemplate = "select max(p.id) from %s p"; 
			
			String query = String.format(getMaxIdHqlTemplate, perfConfig.isPolicyLogs()
					? "TestPolicyActivityLogEntryDO"
					: "TestTrackingActivityLogEntryDO");
			Long logId = (Long) s.createQuery(query).uniqueResult();
			if(logId == null){
				logId = 1L;
			}
			
			Long policyAssistanceLogId;
			if (numberOfObligationLog > 0) {
				//disable the policy assistance log insertion
				query = String.format(getMaxIdHqlTemplate, "PolicyAssistantLogDO");
				policyAssistanceLogId = (Long) s.createQuery(query).uniqueResult();
				if(policyAssistanceLogId == null){
					policyAssistanceLogId = 1L;
				}
			}else{
				policyAssistanceLogId = null;
			}
			
			s.close();
			
			List<BaseLogEntry> unsavedData = new LinkedList<BaseLogEntry>();
			List<PolicyAssistantLogEntry> policyAssistantLogs = new LinkedList<PolicyAssistantLogEntry>();
			
			//for each day
			for (Calendar currentDay : days.keySet()) {
				
				long[] timeStamps = generateRandomSortedTimestamps(currentDay, days.get(currentDay));
				
				//for each time
				for(long time : timeStamps){
					
					//some shared data between tracking logs and policy logs
					UserDO user = users[r.nextInt(users.length)];
					HostDO host = hosts[r.nextInt(hosts.length)];
					String application = applications[r.nextInt(applications.length)];
					int logLevel = logLevelDistribution[r.nextInt(logLevelDistribution.length)];
					
			        FromResourceInformation fromResource = new FromResourceInformation(
			        		fromResources[r.nextInt(fromResources.length)], //String name, 
			        		new Long(r.nextInt(500 * 1024 * 1024)), //long size, 
			        		time, //long createdDate, 
			        		time, //long modifiedDate, 
			        		"S-123-456-" + Arrays.hashCode(users) //String ownerId
	        		);
			        		
			        
			        ToResourceInformation toResource = new ToResourceInformation(
			        		toResources[r.nextInt(toResources.length)]);
			        
			        int numberOfcustomAttr = (int)perfConfig.getCustomAttrsPerLog();
			        if( r.nextFloat() < perfConfig.getCustomAttrsPerLog() - numberOfcustomAttr ){
			        	numberOfcustomAttr++;
			        }
			        
			        DynamicAttributes dynamicAttributes = null;
			        Map<String, String> typeMap = perfConfig.getCustomAttibToTypeMap(); // Key : type
                    Map<String, DynamicAttributes> dynamicAttribMap = new LinkedHashMap<String, DynamicAttributes>();
			         
			        if (numberOfcustomAttr > 0) {
			        	dynamicAttributes = new DynamicAttributes();
			            Map<String, String> customAttrs = generateCustomAttrs(
			            		numberOfcustomAttr,
			            		perfConfig.getCustomAttributes(),
			            		perfConfig.getCustomAttributeTemplates(),
			            		perfConfig.getCustomAttributeValueTokens()
			            );
			            for(Map.Entry<String, String> customAttr : customAttrs.entrySet()){
			            	
			            	String type = typeMap.get(customAttr.getKey());
			            	 DynamicAttributes dAttributes = dynamicAttribMap.get(type);
			            	 if(dAttributes == null) {
			            		 dAttributes = new DynamicAttributes();
			            		 dynamicAttribMap.put(type, dAttributes);
			            	 }
			            	 dAttributes.put(customAttr.getKey(), customAttr.getValue());
			            	 dynamicAttributes.put(customAttr.getKey(), customAttr.getValue());
			        	}
			        }
			        
					if(perfConfig.isPolicyLogs()){
						
						PolicyDecisionEnumType decisionType = r.nextInt(100) < perfConfig.getPercentOfDenyLogs() //PolicyDecisionEnumType policyDecision,
						? PolicyDecisionEnumType.POLICY_DECISION_DENY 
						: PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
						
						PolicyActivityInfoV5 pInfo5 = new PolicyActivityInfoV5(
								fromResource, 				//FromResourceInformation fromResourceInfo, 
								toResource, 				//ToResourceInformation toResourceInfo, 
								user.getDisplayName(), 		//String userName, 
								user.getOriginalId(), 		//long userId, 
								host.getName(), 			//String hostName, 
								ipAddressTransform(host.getName().hashCode()), 	//String hostIP,
								host.getOriginalId(),		//long hostId, 
								application, 				//String applicationName, 
								(long)application.hashCode(), //long applicationId, 
								getAction().getName(), 		//ActionEnumType action,
								decisionType, 
								new Long(-1), 				//long decisionRequestId, 
					            time, 						//long ts, 
					            logLevel, 					//int level, 
					            dynamicAttribMap, 			//DynamicAttributes customAttr
					            null           //evaluationAnnotations
					    ); 
						
						unsavedData.add(new PolicyActivityLogEntryV5(
								pInfo5, //PolicyActivityInfo info, 
								policyIds[r.nextInt(policyIds.length)], //long policyId,
								++logId //  long uid 
						));
						
						if (numberOfObligationLog > 0) {
						    int n = (int)perfConfig.getObligationsPerLog();
		                    if( r.nextFloat() < perfConfig.getObligationsPerLog() - numberOfObligationLog ){
		                        n++;
		                    }
							for (int i = 0; i < n; i++) {
								String obligationName = perfConfig.getObligationsNames()[r.nextInt(
										perfConfig.getObligationsNames().length)];
								policyAssistantLogs.add(new PolicyAssistantLogEntry(
											Long.toString(logId), 					//String logIdentifier,
											obligationName, 		//String assistantName,
											obligationAttrOne[r.nextInt(obligationAttrOne.length)], 	//String assistantOptions,
											obligationAttrTwo[r.nextInt(obligationAttrTwo.length)], 	//String assistantDescription,
											obligationAttrThree[r.nextInt(obligationAttrThree.length)], //String assistantUserActions,
											++policyAssistanceLogId, 				//long uid,
											time 									//long ts
								));
							}
						}
					}else{
						unsavedData.add(new TrackingLogEntry(
								fromResource, 			//FromResourceInformation fromResourceInfo,
								toResource, 			//ToResourceInformation toResourceInfo,
								user.getDisplayName(), 	//String userName,
								user.getOriginalId(), 	//long userId,
								host.getName(), 		//String hostName,
								ipAddressTransform(host.getName().hashCode()), //String hostIP,
								host.getOriginalId(), 	//long hostId,
								application, 			//String applicationName,
								(long)application.hashCode(), //long applicationId,
								getAction(), 			//ActionEnumType action,
					            ++logId, 				//long uid,
					            time,  					//long ts,
					            logLevel, 				//int level,
								dynamicAttributes 		//DynamicAttributes customAttr		
						));
					}
					
					if (unsavedData.size() >= flushEveryNRecords) {
						flush(unsavedData, perfConfig.isPolicyLogs());
					}
					
					if (policyAssistantLogs.size() >= flushEveryNRecords) {
						hibernateLogWriter.log(policyAssistantLogs
								.toArray(new PolicyAssistantLogEntry[] {}));
						policyAssistantLogs.clear();
					}
				}
				
				// memory clean up  
				timeStamps = null;
			}
			
			if (unsavedData.size() > 0) {
				flush(unsavedData, perfConfig.isPolicyLogs());
			}
			
			if (policyAssistantLogs.size() >= 0) {
				hibernateLogWriter.log(policyAssistantLogs
						.toArray(new PolicyAssistantLogEntry[] {}));
			}
		} finally {
		    System.out.println();
			HibernateUtils.closeSession(s, null);
			//print statistics
			LOG.info("totally inserted: " + totalLogsInserted + "/"
					+ totalNumberOfLogsWillBeInserted + " in "
					+ ConsoleDisplayHelper.formatTime((System.currentTimeMillis() - startTime)));
			
			// no memory clean up if I am the last one
			
		}
	}

//	protected enum SharepointActionsCategory{
//		MODIFICATION,
//		CONSUMPTION,
//		STORAGE,
//		DELETION,
//		OTHER,
//	}
//	
//	protected Map<SharepointActionsCategory, List<ActionEnumType>> actionsToEnumMap;
//	protected SharepointActionsCategory[] definedOrder;
//	protected RandomWithAverage randomForActions;
	
	protected List<ActionEnumType> allActions = new ArrayList<ActionEnumType>();
	
	protected void initActions(){
	    new ActionEnumUserType();
		allActions.addAll(ActionEnumType.elements());
//		actionsToEnumMap = new HashMap<SharepointActionsCategory, List<ActionEnumType>>();
//		if(isForSharepoint){
//			List<ActionEnumType> list;
//			list = new ArrayList<ActionEnumType>();
//			list.add(ActionEnumType.ACTION_EDIT);
//			list.add(ActionEnumType.ACTION_RENAME);
//			allActions.removeAll(list);
//			actionsToEnumMap.put(SharepointActionsCategory.MODIFICATION, list);
//			
//			list = new ArrayList<ActionEnumType>();
//			list.add(ActionEnumType.ACTION_OPEN);
//			allActions.removeAll(list);
//			actionsToEnumMap.put(SharepointActionsCategory.CONSUMPTION, list);
//			
//			list = new ArrayList<ActionEnumType>();
//			list.add(ActionEnumType.ACTION_EXPORT);
//			list.add(ActionEnumType.ACTION_COPY);
//			list.add(ActionEnumType.ACTION_MOVE);
//			allActions.removeAll(list);
//			actionsToEnumMap.put(SharepointActionsCategory.STORAGE, list);
//			
//			list = new ArrayList<ActionEnumType>();
//			list.add(ActionEnumType.ACTION_DELETE);
//			allActions.removeAll(list);
//			actionsToEnumMap.put(SharepointActionsCategory.DELETION, list);
//			
//			definedOrder = new SharepointActionsCategory[]{
//					SharepointActionsCategory.STORAGE,
//					SharepointActionsCategory.OTHER,
//					SharepointActionsCategory.MODIFICATION,
//					SharepointActionsCategory.DELETION,
//					SharepointActionsCategory.CONSUMPTION,
//			};
//			
//			randomForActions = new RandomWithAverage(0, 4, 2); 
//		}else{
//			randomForActions = null;
//		}
//		actionsToEnumMap.put(SharepointActionsCategory.OTHER, allActions);
	}
	
	private ActionEnumType getAction() {
//		SharepointActionsCategory pickedCategory = randomForActions != null
//				? definedOrder[randomForActions.next()]
//				: SharepointActionsCategory.OTHER;
//		
//		List<ActionEnumType> actions = actionsToEnumMap.get(pickedCategory);
		
		return allActions.get(r.nextInt(allActions.size()));
	}

	protected void flush(List<? extends BaseLogEntry> unsavedData, boolean isPolicyLog)
            throws DataSourceException {

        if (isPolicyLog) {
            hibernateLogWriter.log(unsavedData.toArray(new PolicyActivityLogEntryV5[unsavedData
                    .size()]));
        } else {
            hibernateLogWriter.log(unsavedData.toArray(new TrackingLogEntry[unsavedData.size()]));
        }

        totalLogsInserted += unsavedData.size();

        unsavedData.clear();
    }
	
	

	protected long creatLogsPerDayMap(ReporterDataPerfConfig perfConfig,
			SortedMap<Calendar, Integer> days) {
		Calendar day = Calendar.getInstance();
		day.setTime(perfConfig.getStartDate());
		// start from the first day. The order may matter. 
		// That's why it is better to start the oldest log.
		day.add(Calendar.DATE, -perfConfig.getNumberOfDays());
		
		long totalNumberOfLogsWillBeInserted = 0;
		
		for (int i = 0; i < perfConfig.getNumberOfDays(); i++) {
			int numberOfLogsForCurrentDay;
			
			switch(day.get(Calendar.DAY_OF_WEEK) ){
			case Calendar.SUNDAY:
				numberOfLogsForCurrentDay = perfConfig.getRecordsPerSunday();
				break;
			case Calendar.MONDAY:
				numberOfLogsForCurrentDay = perfConfig.getRecordsPerMonday();
				break;
			case Calendar.TUESDAY:
				numberOfLogsForCurrentDay = perfConfig.getRecordsPerTuesday();
				break;
			case Calendar.WEDNESDAY:
				numberOfLogsForCurrentDay = perfConfig.getRecordsPerWednesday();
				break;
			case Calendar.THURSDAY:
				numberOfLogsForCurrentDay = perfConfig.getRecordsPerThursday();
				break;
			case Calendar.FRIDAY:
				numberOfLogsForCurrentDay = perfConfig.getRecordsPerFriday();
				break;
			case Calendar.SATURDAY:
				numberOfLogsForCurrentDay = perfConfig.getRecordsPerSaturday();
				break;
			default:
				throw new RuntimeException("should never been here");
			}
			
			if( perfConfig.getLogRandomPercent() > 0){
				//randomize the number of logs
				if(r.nextBoolean()){
					//less logs
					numberOfLogsForCurrentDay *= (1 - r.nextInt(perfConfig.getLogRandomPercent()) / 100F);
				}else{
					//more logs
					numberOfLogsForCurrentDay *= (1 + r.nextInt(perfConfig.getLogRandomPercent()) / 100F);
				}
			}
			
			days.put((Calendar)(day.clone()), numberOfLogsForCurrentDay);
			
			totalNumberOfLogsWillBeInserted += numberOfLogsForCurrentDay;
			
			// next day
			day.add(Calendar.DATE, 1);
		}
		return totalNumberOfLogsWillBeInserted;
	}
	
	protected Map<String,String> generateCustomAttrs(
			int number, 
			String[] customAttrs,
			String[] customAttributeTemplates, 
			TemplateToken[][] tokens){
		String[] values = gernerateStringsFromTemplates(number, customAttributeTemplates, tokens);
		Map<String, String> resources = new HashMap<String, String>(number);
		for (int i = 0; i < number; i++) {
			resources.put(customAttrs[r.nextInt(customAttrs.length)], values[i]);
		}
		return resources;
	}
	
	protected String[] gernerateStringsFromTemplates(
			int number, 
			String[] templates,
			TemplateToken[][] tokenss) {
		String[] resources = new String[number];
		
		for (int i = 0; i < number; i++) {
			resources[i] = gernerateStringFromTemplates(templates, tokenss, i);
		}
		return resources;
	}
	
	protected String gernerateStringFromTemplates(
			String[] templates,
			TemplateToken[][] tokenss,
			long id) {
		int randomIndex = r.nextInt(templates.length);
		String template = templates[randomIndex];
		if (template != null) {
			TemplateToken[] tokens = tokenss[randomIndex];
			Object[] tokenValues = getTokenValues(id, tokens);
			return String.format(template, tokenValues);
		} else {
			return null;
		}
	}
	
	/**
	 * sorted order
	 * @param date
	 * @param size
	 * @return
	 */
	protected long[] generateRandomSortedTimestamps(Calendar date, int size) {
		long[] timestamps = new long[size];
		
		Calendar c = (Calendar) date.clone();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long theBeginningOfTheDay = c.getTimeInMillis();
		
		for (int i = 0; i < size; i++) {
			timestamps[i] = theBeginningOfTheDay + r.nextInt(1000 * 60 * 60 * 24);
		}
		Arrays.sort(timestamps);
		return timestamps;
	}
	
	protected String[] generateRandomSentence(int maxNumberOfWords, int maxLength, int number) {
		String[] list = new String[number];
		for (int i = 0; i < number; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < r.nextInt(maxNumberOfWords); j++) {
				sb.append(dictionary.getRandomWord()).append(" ");
			}
			sb.append(dictionary.getRandomWord());

			String str = sb.toString();
			list[i] = str.length() > maxLength ? str.substring(0, maxLength) : str;
		}
		return list;
	}
	
	public Object[] getTokenValues(Object id, TemplateToken ... tokens){
		Object[] values = new Object[tokens.length];
		
		int i=0;
		for (TemplateToken token : tokens) {
		    values[i++] = token.getTokenValues(dictionary, r, id);
		}
		return values;
	}
	
	@Override
	protected String getRandomHostname(long id){
    	return gernerateStringFromTemplates(perfConfig.getHostnameTemplates(), 
    			perfConfig.getHostnameValueTokens(), id);
	}
    
	@Override
    protected String getRandomUserDisplayname(long id){
    	return gernerateStringFromTemplates(perfConfig.getUserDisplaynameTemplates(), 
    			perfConfig.getUserDisplaynameValueTokens(), id);
    }
    
	@Override
    protected String getRandomUserFirstname(long id){
    	return gernerateStringFromTemplates(perfConfig.getUserFirstnameTemplates(), 
    			perfConfig.getUserFirstnameValueTokens(), id);
    }
    
	@Override
    protected String getRandomUserLastname(long id){
    	return gernerateStringFromTemplates(perfConfig.getUserLastnameTemplates(), 
    			perfConfig.getUserLastnameValueTokens(), id);
    }
    
	@Override
    protected String getRandomUserSidname(long id){
    	return gernerateStringFromTemplates(perfConfig.getUserSidTemplates(), 
    			perfConfig.getUserSidValueTokens(), id);
    }
    
	@Override
    protected String getRandomPolicyFullName(long id){
    	return gernerateStringFromTemplates(perfConfig.getPolicyFullnameTemplates(), 
    			perfConfig.getPolicyFullnameValueTokens(), id);
    }

}
