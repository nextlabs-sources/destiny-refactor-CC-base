/*
 * Created on Dec 10, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bluejungle.domain.action.hibernateimpl.ActionEnumUserType;
import com.bluejungle.framework.utils.Pair;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/ReporterDataFromCSVConfig.java#1 $
 */

public class ReporterDataFromCSVConfig extends ReporterDataConfigBase{
	private static final String PREFIX_FORMAT 					= "format.";
	private static final String PREFIX_DEFAULT 					= "default.";
	
	private static final String SUFFIX_HASHCODE 				= ".hashcode";
	
	private static final String INSERTION_PER_LOG 				= "insertionPerLog";
	private static final String CSV_SEPARATOR 					= "Separator";
	private static final String STOP_ON_UNKNOWN_LINE 			= "stopOnUnknownLine";
	private static final String IS_TRACKING_LOG_IF_NO_POLICY 	= "isTrackingLogIfNoPolicy";
	private static final String CSV_FORMAT 						= "csvFormat";
	private static final String IGNORE_HEADER_ROW 				= "ignoreHeaderRow";
	
	enum CsvColumn{
		ID,
		AGENT_ACTION,	//IActionType
		DB_ACTION,		
		LOG_TIME,
		LOG_TIMESTAMP_MS,
		POLICY_ID,
		POLICY_FULL_NAME,
		ENFORCEMENT,
		USER_ID,
		USER_DISPLAY_NAME,
		LOG_LEVEL,
		FROM_RESOURCE_NAME,
		FROM_RESOURCE_LEGNTH,
		FROM_RESOURCE_CREATED_DATE,
		FROM_RESOURCE_CREATED_TIMESTAMP_MS,
		FROM_RESOURCE_MODIFIED_DATE,
		FROM_RESOURCE_MODIFIED_TIMESTAMP_MS,
		FROM_RESOURCE_OWNER_ID,
		TO_RESOURCE_NAME,
		HOST_NAME,
		HOST_ID,
		IP_ADDRESS,
		APPLICATION_NAME,
		APPLICARION_ID,
		DECISION_REQUEST_ID,
		CUSTOM_ATTR,
		CUSTOM_ATTR_KEY_VALUE,
		CUSTOM_ATTR_KEY,
		CUSTOM_ATTR_VALUE,
		OBLIGATION_LOG,
		OBLIGATION_LOG_NAME,
		OBLIGATION_LOG_ATTR_ONE,
		OBLIGATION_LOG_ATTR_TWO,
		OBLIGATION_LOG_ATTR_THREE,
		OBLIGATION_LOG_TIME,
		OBLIGATION_LOG_TIMESTAMP_MS,
		IGNORE,
	}
	
	//the order is important
	private final List<Pair<Integer, Integer>> insertionList;
	private final String csvSeperater;
	private final boolean isStopOnUnknownLine;
	private final boolean isStopOnUnknownValue;
	private final boolean isTrackingLogIfNoPolicy;
	private final boolean isIgnoreHeaderRow;
	
	
	private final Map<CsvColumn, FormatTemplate> formatMap;
	private final Map<CsvColumn, FormatTemplate> defaultMap;
	
	private final List<CsvColumn> csvOrder;
	private final List<CsvColumn> customOrder;
	private final List<CsvColumn> obligationOrder;
	
	public ReporterDataFromCSVConfig(File configFile) throws IOException{
		Properties perfConfig = new Properties();
		InputStream is = null;
		try{
			is = new FileInputStream(configFile);
			perfConfig.load(is);
			insertionList 			= parseInsertionTimeString(perfConfig);
			csvSeperater 			= getString(perfConfig, CSV_SEPARATOR, ",").trim();
			isStopOnUnknownLine 	= getBoolean(perfConfig, STOP_ON_UNKNOWN_LINE);
			isTrackingLogIfNoPolicy = getBoolean(perfConfig, IS_TRACKING_LOG_IF_NO_POLICY);
			isIgnoreHeaderRow 		= getBoolean(perfConfig, IGNORE_HEADER_ROW);
			isStopOnUnknownValue 	= true;
			
			
			formatMap = new HashMap<CsvColumn, FormatTemplate>();
			for(Map.Entry<Object, Object> entry : perfConfig.entrySet() ){
				String key = entry.getKey().toString();
				if (key.startsWith(PREFIX_FORMAT)) {
					//remove the prefix
					key = key.substring(PREFIX_FORMAT.length());
					formatMap.put(CsvColumn.valueOf(key), new FormatTemplate(entry.getValue().toString()));
				}
			}
			
			defaultMap = new HashMap<CsvColumn, FormatTemplate>();
			for(Map.Entry<Object, Object> entry : perfConfig.entrySet() ){
				String key = entry.getKey().toString();
				if (key.startsWith(PREFIX_DEFAULT)) {
					//remove the prefix
					key = key.substring(PREFIX_DEFAULT.length());
					defaultMap.put(CsvColumn.valueOf(key), new FormatTemplate(entry.getValue().toString()));
				}
			}
			
			csvOrder = parseCsvColumnFormat(getString(perfConfig, CSV_FORMAT));
			customOrder = parseCsvColumnFormat(getString(perfConfig, 
					PREFIX_FORMAT + CsvColumn.CUSTOM_ATTR));
			obligationOrder = parseCsvColumnFormat(getString(perfConfig, 
					PREFIX_FORMAT + CsvColumn.OBLIGATION_LOG ));
			
		} finally{
			if (is != null) {
				//should do try/catch here
				is.close();
			}
		}
	}
	
	private static final String LOG_INSERTION_SEPERATOR = ";";
	private static final String LOG_ID_OR_OPERATOR = ",";
	private static final String LOG_ID_RANDE_OPERATOR = "-"; 
	private static final Pattern LOG_INSERTION_KEY_VALUE_PATTERN = Pattern.compile(
			"(\\d+((" + LOG_ID_OR_OPERATOR + "\\d+)*|(" + LOG_ID_RANDE_OPERATOR	+ "\\d+)?))=(\\d+)");
	
	List<Pair<Integer, Integer>> parseInsertionTimeString(Properties perfConfig)
			throws IllegalArgumentException {
		List<Pair<Integer, Integer>> list = new LinkedList<Pair<Integer, Integer>>();
		String[] values = getStringArray(perfConfig, INSERTION_PER_LOG, LOG_INSERTION_SEPERATOR);
		for(String value : values){
			value = value.trim();
			if (value.length() == 0) {
				continue;
			}
			Matcher m = LOG_INSERTION_KEY_VALUE_PATTERN.matcher(value);
			if(m.matches()){
				int insertTimes = Integer.parseInt(m.group(5));
				String logsIdStr = m.group(1);
				if (logsIdStr.contains(LOG_ID_OR_OPERATOR)) {
					String[] logsIdStrs = logsIdStr.split(LOG_ID_OR_OPERATOR);
					for (String logIdStr : logsIdStrs) {
						int logId = Integer.parseInt(logIdStr);
						list.add(new Pair<Integer, Integer>(logId, insertTimes));
					}
				} else if (logsIdStr.contains(LOG_ID_RANDE_OPERATOR)) {
					String[] logsIdStrs = logsIdStr.split(LOG_ID_RANDE_OPERATOR);
					
					assert logsIdStrs.length == 2;
					
					int fromLogId = Integer.parseInt(logsIdStrs[0]);
					int toLogId = Integer.parseInt(logsIdStrs[1]);
					if(toLogId < fromLogId){
						String messageTemplate = 
							"Invalid range, the to value %d can't smaller than the from value %d";
						throw new IllegalArgumentException(String.format(messageTemplate, fromLogId, 
								toLogId));
					}
					for (int logId = fromLogId; logId <= toLogId; logId++) {
						list.add(new Pair<Integer, Integer>(logId, insertTimes));
					}
				} else {
					int logId = Integer.parseInt(logsIdStr);
					list.add(new Pair<Integer, Integer>(logId, insertTimes));
				}
			}else{
				throw new IllegalArgumentException("The value of \"" + INSERTION_PER_LOG
						+ "\" is not in correct format. \"" + value + "\" does match "
						+ LOG_INSERTION_KEY_VALUE_PATTERN.pattern() + " PATTERN.");
			}
		}
		return list;
	}
	
	private List<CsvColumn> parseCsvColumnFormat(String str)
			throws IllegalArgumentException {
		List<CsvColumn> list = new LinkedList<CsvColumn>();
		String[] values = str.split(",");
		for (String value : values) {
			list.add(CsvColumn.valueOf(value.trim()));
		}
		return list;
	}
	
	public List<CsvColumn> getCsvOrder() {
		return csvOrder;
	}
	
	public List<CsvColumn> getCustomAttributeOrder() {
		return customOrder;
	}
	
	public List<CsvColumn> getObligationLogORder() {
		return obligationOrder;
	}

	public List<Pair<Integer, Integer>> getInsertionList() {
		return insertionList;
	}

	public String getCsvSeperater() {
		return csvSeperater;
	}

	public boolean isStopOnUnknownLine() {
		return isStopOnUnknownLine;
	}
	
	public boolean isStopOnUnknownValue() {
		return isStopOnUnknownValue;
	}
	
	public boolean isTrackingLogIfNoPolicy() {
		return isTrackingLogIfNoPolicy;
	}

	public FormatTemplate getFormat(CsvColumn column){
		FormatTemplate ft = formatMap.get(column);
		if (ft == null) {
			throw new NullPointerException("No default format for " + column);
		}
		return ft;
	}
	
	public FormatTemplate getDefault(CsvColumn column){
		FormatTemplate ft = defaultMap.get(column);
		if (ft == null) {
			throw new NullPointerException("No default template for " + column);
		}
		return ft;
	}
	
	public boolean isIgnoreHeaderRow(){
		return isIgnoreHeaderRow;
	}
	
	static class FormatTemplate{
		private static final String NULL_VALUE = "<null>";
		private static final String TOKEN_PREFIX = "<";
		private static final String TOKEN_SUFFIX = ">";
		
		String template;
		
		FormatTemplate(String template){
			this.template = template;
		}
		
		SimpleDateFormat getDateFormat() throws IllegalArgumentException{
			return new SimpleDateFormat(template);
		}
		
		String getString(GenericLog log){
			if(template.equals(NULL_VALUE)){
				return null;
			}
			
			try {
				String unWrap = unWrap(template);
				
				if (unWrap.endsWith(SUFFIX_HASHCODE)) {
					unWrap = unWrap.substring(0, unWrap.length() - SUFFIX_HASHCODE.length());
					CsvColumn column = CsvColumn.valueOf(unWrap);
					Object value = getValue(column, log);
					return ReporterDataBase.ipAddressTransform(value.hashCode());
				}
				
				
				return getValue(CsvColumn.valueOf(unWrap), log).toString();
			} catch (IllegalArgumentException e) {
				//keep going
			}
			
			return template;
			
		}
		
		Long getLong(GenericLog log) throws IllegalArgumentException{
			if(template.equals(NULL_VALUE)){
				return null;
			}
			
			try {
				String unWrap = unWrap(template);
				
				try {
					CsvColumn column = CsvColumn.valueOf(unWrap);
					Object value = getValue(column, log);
					if(value instanceof Date){
						return ((Date)value).getTime();
					}
					return Long.parseLong(value.toString());
				} catch (IllegalArgumentException e) {
					//keep going
				}
				
				
				if (unWrap.endsWith(SUFFIX_HASHCODE)) {
					unWrap = unWrap.substring(0, unWrap.length() - SUFFIX_HASHCODE.length());
					CsvColumn column = CsvColumn.valueOf(unWrap);
					Object value = getValue(column, log);
					long hashCode =  value.hashCode();
					return hashCode ^ (hashCode << 32);
				}
			} catch (IllegalArgumentException e) {
				// keep going
			}
			
			return Long.parseLong(template);
		}
		
		Integer getInteger(GenericLog log) throws IllegalArgumentException{
			if(template.equals(NULL_VALUE)){
				return null;
			}
			
			try {
				String unWrap = unWrap(template);
				
				try {
					CsvColumn column = CsvColumn.valueOf(unWrap);
					Object value = getValue(column, log);
					return Integer.parseInt(value.toString());
				} catch (IllegalArgumentException e) {
					//keep going
				}
				
				
				if (unWrap.endsWith(SUFFIX_HASHCODE)) {
					unWrap = unWrap.substring(0, unWrap.length() - SUFFIX_HASHCODE.length());
					CsvColumn column = CsvColumn.valueOf(unWrap);
					Object value = getValue(column, log);
					return  value.hashCode();
				}
			} catch (IllegalArgumentException e) {
				// keep going
			}
			
			return Integer.parseInt(template);
		}
		
		
		String getTemplateStr(){
			return template;
		}
		
		int getInteger(){
			return Integer.parseInt(template);
		}

		@Override
		public String toString() {
			return template;
		}
		
		static String getToken(CsvColumn column){
			return getToken(column.name());
		}
		
		static String getToken(String str){
			return TOKEN_PREFIX + str + TOKEN_SUFFIX;
		}
		
		static CsvColumn unToken(String str){
			return CsvColumn.valueOf(unWrap(str));
		}
		
		private static String unWrap(String str){
			if(str.startsWith(TOKEN_PREFIX) && str.endsWith(TOKEN_SUFFIX)){
				return str.substring(TOKEN_PREFIX.length(), str.length() - TOKEN_SUFFIX.length());
			}else{
				throw new IllegalArgumentException(str);
			}
		}
		
		public static Object getValue(CsvColumn column, GenericLog log) {
			switch (column) {
			case ID:
				return log.getId();
			case AGENT_ACTION:
				return log.getAction();
			case DB_ACTION:
				return new ActionEnumUserType().getCodeByType(log.getAction());
			case LOG_TIME:
				return new Date(log.getTs());
			case LOG_TIMESTAMP_MS:
				return log.getTs();
			case POLICY_ID:
				return log.getPolicy();
			case POLICY_FULL_NAME:
				return log.getPolicy().getFullName();
			case ENFORCEMENT:
				return log.getPolicyDecision();
			case USER_ID:
				return log.getUser().getOriginalId();
			case USER_DISPLAY_NAME:
				return log.getUser().getDisplayName();
			case LOG_LEVEL:
				return log.getLevel();
			case FROM_RESOURCE_NAME:
				return log.getFromResourceName();
			case FROM_RESOURCE_LEGNTH:
				return log.getFromResourceSize();
			case FROM_RESOURCE_CREATED_DATE:
				return new Date(log.getFromResourceCreatedDate());
			case FROM_RESOURCE_CREATED_TIMESTAMP_MS:
				return log.getFromResourceCreatedDate();
			case FROM_RESOURCE_MODIFIED_DATE:
				return new Date(log.getFromResourceModifiedDate());
			case FROM_RESOURCE_MODIFIED_TIMESTAMP_MS:
				return log.getFromResourceModifiedDate();
			case FROM_RESOURCE_OWNER_ID:
				return log.getFromResourceOwnerId();
			case TO_RESOURCE_NAME:
				return log.getToResource();
			case HOST_NAME:
				return log.getHost().getName();
			case HOST_ID:
				return log.getHost().getOriginalId();
			case IP_ADDRESS:
				return log.getHostIP();
			case APPLICATION_NAME:
				return log.getApplicationName();
			case APPLICARION_ID:
				return log.getApplicationId();
			case DECISION_REQUEST_ID:
				return log.getDecisionRequestId();
			// the following are not supported, you can implement them if you want.
			case CUSTOM_ATTR:
			case CUSTOM_ATTR_KEY_VALUE:
			case CUSTOM_ATTR_KEY:
			case CUSTOM_ATTR_VALUE:
			case OBLIGATION_LOG:
			case OBLIGATION_LOG_NAME:
			case OBLIGATION_LOG_ATTR_ONE:
			case OBLIGATION_LOG_ATTR_TWO:
			case OBLIGATION_LOG_ATTR_THREE:
			case OBLIGATION_LOG_TIME:
			case OBLIGATION_LOG_TIMESTAMP_MS:
			case IGNORE:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalArgumentException("unknown csvColumn: " + column);
			}
		}
		
	}
}
