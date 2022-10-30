/*
 * Created on Oct 6, 2008
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.bluejungle.framework.utils.Pair;
import com.nextlabs.random.TemplateToken;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/ReporterDataPerfConfig.java#1 $
 */
class ReporterDataPerfConfig extends ReporterDataConfigBase{
    private static final String TEMPLATE_LENGTH_MISMATCH_MESSAGE = 
        "In %s, The numbers of tokens ,%d, doesn't match the number of templates, %d.";
    
    
    private static final String NULL_TOKEN                          = "<null>";
    private static final String DEFAULT_TOKEN_SEPERATOR             = ";";
    
    private static final String VALUE_SEPERATOR                     = "valueSeperator";
    private static final String TOKEN_SEPERATOR                     = "tokenSeperator";
    
    private static final String HACKER_PASSWORD                     = "hackerPassword";
    
    //basic
    private static final String NUMBER_OF_USERS                     = "users";
    private static final String NUMBER_OF_HOSTS                     = "hosts";
    private static final String NUMBER_OF_POLICIES                  = "policies";
    
    //logs
    private static final String START_FROM_DATE                     = "startFromDate";
    private static final String NUMBER_OF_DAYS                      = "daysFrom";
    private static final String RECORDS_PER_SUNDAY                  = "recordsPerSunday";
    private static final String RECORDS_PER_MONDAY                  = "recordsPerMonday";
    private static final String RECORDS_PER_TUESDAY                 = "recordsPerTuesday";
    private static final String RECORDS_PER_WEDNESDAY               = "recordsPerWednesday";
    private static final String RECORDS_PER_THURSDAY                = "recordsPerThursday";
    private static final String RECORDS_PER_FRIDAY                  = "recordsPerFriday";
    private static final String RECORDS_PER_SATURDAY                = "recordsPerSaturday";
    private static final String LOG_RANDOM_PERCENT                  = "logRandomPercent";
    private static final String LOG_LEVEL_DISTRUBUTION              = "logLevelDistrubution";
    
    //logs type
    private static final String PERCENT_OF_DENY_LOGS                = "percentOfDenyLogs";
    private static final String LOG_TYPES                           = "logTypes";    
    
    //application
    private static final String NUMBER_OF_APPLICATIONS              = "applications";
    private static final String APPLICATION_TEMPLATES               = "applicationTemplates";
    private static final String APPLICATIONS_VALUE_TOKENS           = "applicationsValueTokens";
    private static final String APPLICATION_FILE_EXTENSIONS         = "applicationFileExtensions";
    
    //from resources
    private static final String NUMBER_OF_FROM_RESOURCES            = "fromResources";
    private static final String FROM_RESOURCE_TEMPLATES             = "fromResourceTemplates";
    private static final String FROM_RESOURCES_VALUE_TOKENS         = "fromResourcesValueTokens";
    private static final String FROM_RESOURCE_FILE_EXTENSIONS       = "fromResourceFileExtensions";
    
    //to resources
    private static final String NUMBER_OF_TO_RESOURCES              = "toResources";
    private static final String TO_RESOURCE_TEMPLATES               = "toResourceTemplates";
    private static final String TO_RESOURCES_VALUE_TOKENS           = "toResourcesValueTokens";
    private static final String TO_RESOURCE_FILE_EXTENSIONS         = "toResourceFileExtensions";
    
    //custom attribute
    private static final String CUSTOM_ATTRIBUTES_PER_LOG           = "customAttrsPerLog";
    private static final String CUSTOM_ATTRIBUTES                   = "customAttributes";
    private static final String CUSTOM_ATTRIBUTE_TEMPLATES          = "customAttributeTemplates";
    private static final String CUSTOM_ATTRIBUTE_VALUE_TOKENS       = "customAttributeValueTokens";
    
    //obligation
    private static final String OBLIGATION_PER_LOG                  = "obligationPerLog";
    private static final String POLICY_ASSISTANCE_NAMES             = "assistantNames";
    private static final String OBLIGATION_ATTR_ONE                 = "obligationAttrOne";
    private static final String OBLIGATION_ATTR_ONE_TEMPLATES       = "obligationAttrOneTemplates";
    private static final String OBLIGATION_ATTR_ONE_VALUE_TOKENS    = "obligationAttrOneValueTokens";
    private static final String OBLIGATION_ATTR_TWO                 = "obligationAttrTwo";
    private static final String OBLIGATION_ATTR_TWO_TEMPLATES       = "obligationAttrTwoTemplates";
    private static final String OBLIGATION_ATTR_TWO_VALUE_TOKENS    = "obligationAttrTwoValueTokens";
    private static final String OBLIGATION_ATTR_THREE               = "obligationAttrThree";
    private static final String OBLIGATION_ATTR_THREE_TEMPLATES     = "obligationAttrThreeTemplates";
    private static final String OBLIGATION_ATTR_THREE_VALUE_TOKENS  = "obligationAttrThreeValueTokens";
    
    //generate data
    private static final String HOSTNAME_TEMPLATE                   = "hostname.template";
    private static final String HOSTNAME_TOKENS                     = "hostname.tokens";
    private static final String USER_DISPLAYNAME_TEMPLATE           = "userDisplayname.template";
    private static final String USER_DISPLAYNAME_TOKENS             = "userDisplayname.tokens";
    private static final String USER_FIRSTNAME_TEMPLATE             = "userFirstname.template";
    private static final String USER_FIRSTNAME_TOKENS               = "userFirstname.tokens";
    private static final String USER_LASTNAME_TEMPLATE              = "userLastname.template";
    private static final String USER_LASTNAME_TOKENS                = "userLastname.tokens";
    private static final String USER_SID_TEMPLATE                   = "userSid.template";
    private static final String USER_SID_TOKENS                     = "userSid.tokens";
    private static final String POLICY_FULLNAME_TEMPLATE            = "policyFullName.template";
    private static final String POLICY_FULLNAME_TOKENS              = "policyFullName.tokens";
    
    //please follow the order of the keys

    //basic
    private final int numberOfUsers;
    private final int numberOfHosts;
    private final int numberOfPolicies;
    
    //logs
    private final Date startDate;
    private final int numberOfDays;
    private final int recordsPerSunday;
    private final int recordsPerMonday;
    private final int recordsPerTuesday;
    private final int recordsPerWednesday;
    private final int recordsPerThursday;
    private final int recordsPerFriday;
    private final int recordsPerSaturday;
    private final int logRandomPercent;
    private final int[] logLevelDistrubution;
    
    //logs type
    private final int percentOfDenyLogs;
    private final PolicyType policyType;
    
    //application
    private final int numberOfApplications;
    private final String[] applicationTemplates;
    private final TemplateToken[][] applicationValueTokens;
    private final String[] applicationFileExtension;
    
    //from resources
    private final int numberOfFromResources;
    private final String[] fromResourceTemplates;
    private final TemplateToken[][] fromResourceValueTokens;
    private final String[] fromResourceFileExtension;
    
    //to resources
    private final int numberOfToResources;
    private final String[] toResourceTemplates;
    private final TemplateToken[][] toResourceValueTokens;
    private final String[] toResourceFileExtension;
    
    //custom attribute
    private final float customAttrsPerLog;
    private final Map<String, String> customAttibToTypeMap;
    private final String[] customAttributes;
    private final String[] customAttributeTemplates;
    private final TemplateToken[][] customAttributeValueTokens;
    
    //obligation
    private final float obligationsPerLog;
    private final String[] obligationsNames;
    private final int numberOfObligationAttrOne;
    private final String[] obligationAttrOneTemplates;
    private final TemplateToken[][] obligationAttrOneValueTokens;
    private final int numberOfObligationAttrThree;
    private final String[] obligationAttrTwoTemplates;
    private final TemplateToken[][] obligationAttrTwoValueTokens;
    private final int numberOfObligationAttrTwo;
    private final String[] obligationAttrThreeTemplates;
    private final TemplateToken[][] obligationAttrThreeValueTokens;
    
    //generate data
    private final String[] hostnameTemplates;
    private final TemplateToken[][] hostnameValueTokens;
    private final String[] userDisplaynameTemplates;
    private final TemplateToken[][] userDisplaynameValueTokens;
    private final String[] userFirstnameTemplates;
    private final TemplateToken[][] userFirstnameValueTokens;
    private final String[] userLastnameTemplates;
    private final TemplateToken[][] userLastnameValueTokens;
    private final String[] userSidTemplates;
    private final TemplateToken[][] userSidValueTokens;
    private final String[] policyFullnameTemplates;
    private final TemplateToken[][] policyFullnameValueTokens;
    
    
    private final boolean isHacker;
    
    public ReporterDataPerfConfig(File configFile) throws IOException{
        Properties perfConfig = new Properties();
        InputStream is = null;
        String tempStr;
        String[] tempArray;
        try{
            is = new FileInputStream(configFile);
            perfConfig.load(is);
            
            String hackerPassowrd = perfConfig.getProperty(HACKER_PASSWORD);
            //hacker doesn't have password
            isHacker = hackerPassowrd != null && hackerPassowrd.length() == 0;
            
            final String seperator = 
                    getString(perfConfig, VALUE_SEPERATOR, DEFAULT_VALUE_SEPERATOR);
            final String tokenSeperator = 
                    getString(perfConfig, TOKEN_SEPERATOR, DEFAULT_TOKEN_SEPERATOR);
            
            //basic
            numberOfUsers    = getNonNegativeInteger(perfConfig, NUMBER_OF_USERS);
            numberOfHosts    = getNonNegativeInteger(perfConfig, NUMBER_OF_HOSTS);
            numberOfPolicies = getNonNegativeInteger(perfConfig, NUMBER_OF_POLICIES);
            
            //logs
            tempStr = perfConfig.getProperty(START_FROM_DATE);
            if(tempStr != null){
                try {
                    startDate = new SimpleDateFormat("MM/d/yyyy").parse(tempStr);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }else{
                startDate = new Date();
            }
            numberOfDays         = getNonNegativeInteger(perfConfig, NUMBER_OF_DAYS);
            recordsPerSunday     = getNonNegativeInteger(perfConfig, RECORDS_PER_SUNDAY);
            recordsPerMonday     = getNonNegativeInteger(perfConfig, RECORDS_PER_MONDAY);
            recordsPerTuesday    = getNonNegativeInteger(perfConfig, RECORDS_PER_TUESDAY);
            recordsPerWednesday  = getNonNegativeInteger(perfConfig, RECORDS_PER_WEDNESDAY);
            recordsPerThursday   = getNonNegativeInteger(perfConfig, RECORDS_PER_THURSDAY);
            recordsPerFriday     = getNonNegativeInteger(perfConfig, RECORDS_PER_FRIDAY);
            recordsPerSaturday   = getNonNegativeInteger(perfConfig, RECORDS_PER_SATURDAY);
            logRandomPercent     = getNonNegativeInteger(perfConfig, LOG_RANDOM_PERCENT);
            logLevelDistrubution = getIntegerArray(perfConfig, LOG_LEVEL_DISTRUBUTION, 
                            seperator,    NON_NEGATIVE_INTEGER_RANGE);
            //check logLevelDistrubution
            if (logLevelDistrubution.length != 3) {
                throw new IllegalArgumentException(LOG_LEVEL_DISTRUBUTION
                        + " is incorrect format. It must have 3 integers but you only have "
                        + logLevelDistrubution.length);
            }
            int total = 0;
            for (int l : logLevelDistrubution) {
                total += l;
            }
            if (total != 100) {
                throw new IllegalArgumentException(LOG_LEVEL_DISTRUBUTION
                        + " is incorrect format. The sum of the values must be 100 but it is "
                        + total);
            }
            
            
            //logs type
            percentOfDenyLogs = getInteger(perfConfig, PERCENT_OF_DENY_LOGS,
                    new Pair<Integer, Integer>(0, 100));
            policyType = PolicyType.valueOf(getString(perfConfig, LOG_TYPES));
            
            //application
            numberOfApplications = getNonNegativeInteger(perfConfig, NUMBER_OF_APPLICATIONS);
            applicationTemplates = getStringArray(perfConfig, APPLICATION_TEMPLATES, seperator);
            convertNullTokenToNull(applicationTemplates);
            applicationFileExtension = getStringArray(perfConfig, APPLICATION_FILE_EXTENSIONS, seperator);
            tempArray = getStringArray(perfConfig, APPLICATIONS_VALUE_TOKENS, seperator);
            if (tempArray.length != applicationTemplates.length) {
                throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE,
                        APPLICATIONS_VALUE_TOKENS, tempArray.length, applicationTemplates.length));
            }
            applicationValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            
            //from resources
            numberOfFromResources = getNonNegativeInteger(perfConfig, NUMBER_OF_FROM_RESOURCES);
            fromResourceTemplates = getStringArray(perfConfig, FROM_RESOURCE_TEMPLATES, seperator);
            convertNullTokenToNull(fromResourceTemplates);
            fromResourceFileExtension =    getStringArray(perfConfig, FROM_RESOURCE_FILE_EXTENSIONS, seperator);
            tempArray = getStringArray(perfConfig, FROM_RESOURCES_VALUE_TOKENS, seperator);
            if (tempArray.length != fromResourceTemplates.length) {
                throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE, 
                        FROM_RESOURCES_VALUE_TOKENS, tempArray.length, fromResourceTemplates.length));
            }
            fromResourceValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            
            
            
            //to resources
            numberOfToResources = getNonNegativeInteger(perfConfig, NUMBER_OF_TO_RESOURCES);
            toResourceTemplates = getStringArray(perfConfig, TO_RESOURCE_TEMPLATES, seperator);
            convertNullTokenToNull(toResourceTemplates);
            toResourceFileExtension = getStringArray(perfConfig, TO_RESOURCE_FILE_EXTENSIONS, seperator);
            tempArray = getStringArray(perfConfig, TO_RESOURCES_VALUE_TOKENS, seperator);
            if (tempArray.length != toResourceTemplates.length) {
                throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE,
                        TO_RESOURCES_VALUE_TOKENS, tempArray.length, toResourceTemplates.length));
            }
            toResourceValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            
            
            //custom attribute
            customAttrsPerLog = getFloat(perfConfig, CUSTOM_ATTRIBUTES_PER_LOG,
                    new Pair<Float, Float>(0f, Float.MAX_VALUE));
               
           customAttibToTypeMap = getCustomAttributeMap(perfConfig, CUSTOM_ATTRIBUTES, tokenSeperator, seperator);
           Set<String> attSet = customAttibToTypeMap.keySet();
           String[] keys = new String[attSet.size()];
           customAttributes = attSet.toArray(keys);
           
//            customAttributes = getStringArray(perfConfig, CUSTOM_ATTRIBUTES, seperator);
            customAttributeTemplates = getStringArray(perfConfig, CUSTOM_ATTRIBUTE_TEMPLATES, seperator);
            convertNullTokenToNull(customAttributeTemplates);
            tempArray = getStringArray(perfConfig, CUSTOM_ATTRIBUTE_VALUE_TOKENS, seperator);
            if (tempArray.length != customAttributeTemplates.length) {
                throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE,
                        CUSTOM_ATTRIBUTE_VALUE_TOKENS, tempArray.length, customAttributeTemplates.length));
            }
            customAttributeValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            
            
            //obligation
            if (policyType == PolicyType.POLICY) {
                obligationsPerLog = getFloat(perfConfig, OBLIGATION_PER_LOG, new Pair<Float, Float>(0f,
                                Float.MAX_VALUE));
            } else {
                obligationsPerLog = 0;
            }
            
            if (obligationsPerLog > 0) {
                obligationsNames = getStringArray(perfConfig, POLICY_ASSISTANCE_NAMES, seperator);
                
                numberOfObligationAttrOne = getNonNegativeInteger(perfConfig, OBLIGATION_ATTR_ONE);
                obligationAttrOneTemplates =
                        getStringArray(perfConfig, OBLIGATION_ATTR_ONE_TEMPLATES, seperator);
                tempArray = getStringArray(perfConfig, OBLIGATION_ATTR_ONE_VALUE_TOKENS, seperator);
                if (tempArray.length != obligationAttrOneTemplates.length) {
                    throw new IllegalArgumentException(String.format(
                            TEMPLATE_LENGTH_MISMATCH_MESSAGE, OBLIGATION_ATTR_ONE_VALUE_TOKENS,
                            tempArray.length, obligationAttrOneTemplates.length));
                }
                obligationAttrOneValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
                
                numberOfObligationAttrTwo = getNonNegativeInteger(perfConfig, OBLIGATION_ATTR_TWO);
                obligationAttrTwoTemplates =
                        getStringArray(perfConfig, OBLIGATION_ATTR_TWO_TEMPLATES, seperator);
                tempArray = getStringArray(perfConfig, OBLIGATION_ATTR_TWO_VALUE_TOKENS, seperator);
                if (tempArray.length != obligationAttrTwoTemplates.length) {
                    throw new IllegalArgumentException(String.format(
                            TEMPLATE_LENGTH_MISMATCH_MESSAGE, OBLIGATION_ATTR_TWO_VALUE_TOKENS,
                            tempArray.length, obligationAttrTwoTemplates.length));
                }
                obligationAttrTwoValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
                
                numberOfObligationAttrThree = getNonNegativeInteger(perfConfig, OBLIGATION_ATTR_THREE);
                obligationAttrThreeTemplates =
                        getStringArray(perfConfig, OBLIGATION_ATTR_THREE_TEMPLATES, seperator);
                tempArray = getStringArray(perfConfig, OBLIGATION_ATTR_THREE_VALUE_TOKENS, seperator);
                if (tempArray.length != obligationAttrThreeTemplates.length) {
                    throw new IllegalArgumentException(String.format(
                            TEMPLATE_LENGTH_MISMATCH_MESSAGE, OBLIGATION_ATTR_THREE_VALUE_TOKENS,
                            tempArray.length, obligationAttrThreeTemplates.length));
                }
                obligationAttrThreeValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            }else{
                obligationsNames = null;
                
                numberOfObligationAttrOne      = 0;
                obligationAttrOneTemplates     = null;
                obligationAttrOneValueTokens   = null;
                
                numberOfObligationAttrTwo      = 0;
                obligationAttrTwoTemplates     = null;
                obligationAttrTwoValueTokens   = null;
                
                numberOfObligationAttrThree    = 0;                
                obligationAttrThreeTemplates   = null;
                obligationAttrThreeValueTokens = null;
            }
            
            if( perfConfig.containsKey(HOSTNAME_TEMPLATE)){
                hostnameTemplates = getStringArray(perfConfig, HOSTNAME_TEMPLATE, seperator);
                tempArray = getStringArray(perfConfig, HOSTNAME_TOKENS, seperator);
                if (tempArray.length != hostnameTemplates.length) {
                    throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE,
                            HOSTNAME_TOKENS, tempArray.length, hostnameTemplates.length));
                }
                hostnameValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            }else{
                hostnameTemplates = null;
                hostnameValueTokens= null;
            }
            
            if( perfConfig.containsKey(USER_DISPLAYNAME_TEMPLATE)){
                userDisplaynameTemplates = getStringArray(perfConfig, USER_DISPLAYNAME_TEMPLATE, seperator);
                tempArray = getStringArray(perfConfig, USER_DISPLAYNAME_TOKENS, seperator);
                if (tempArray.length != userDisplaynameTemplates.length) {
                    throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE,
                            USER_DISPLAYNAME_TOKENS, tempArray.length, userDisplaynameTemplates.length));
                }
                userDisplaynameValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            }else{
                userDisplaynameTemplates = null;
                userDisplaynameValueTokens = null;
            }
            
            if( perfConfig.containsKey(USER_LASTNAME_TEMPLATE)){
                userLastnameTemplates = getStringArray(perfConfig, USER_LASTNAME_TEMPLATE, seperator);
                tempArray = getStringArray(perfConfig, USER_LASTNAME_TOKENS, seperator);
                if (tempArray.length != userLastnameTemplates.length) {
                    throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE,
                            USER_LASTNAME_TOKENS, tempArray.length, userLastnameTemplates.length));
                }
                userLastnameValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            }else{
                userLastnameTemplates = null;
                userLastnameValueTokens = null;
            }
            
            if( perfConfig.containsKey(USER_FIRSTNAME_TEMPLATE)){
                userFirstnameTemplates = getStringArray(perfConfig, USER_FIRSTNAME_TEMPLATE, seperator);
                tempArray = getStringArray(perfConfig, USER_FIRSTNAME_TOKENS, seperator);
                if (tempArray.length != userFirstnameTemplates.length) {
                    throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE,
                            USER_FIRSTNAME_TOKENS, tempArray.length, userFirstnameTemplates.length));
                }
                userFirstnameValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            }else{
                userFirstnameTemplates = null;
                userFirstnameValueTokens = null;
            }
            
            if( perfConfig.containsKey(USER_SID_TEMPLATE)){
                userSidTemplates = getStringArray(perfConfig, USER_SID_TEMPLATE, seperator);
                tempArray = getStringArray(perfConfig, USER_SID_TOKENS, seperator);
                if (tempArray.length != userSidTemplates.length) {
                    throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE,
                            USER_SID_TOKENS, tempArray.length, userSidTemplates.length));
                }
                userSidValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            }else{
                userSidTemplates = null;
                userSidValueTokens = null;
            }
            
            if( perfConfig.containsKey(POLICY_FULLNAME_TEMPLATE)){
                policyFullnameTemplates = getStringArray(perfConfig, POLICY_FULLNAME_TEMPLATE, seperator);
                tempArray = getStringArray(perfConfig, POLICY_FULLNAME_TOKENS, seperator);
                if (tempArray.length != policyFullnameTemplates.length) {
                    throw new IllegalArgumentException(String.format(TEMPLATE_LENGTH_MISMATCH_MESSAGE,
                            POLICY_FULLNAME_TOKENS, tempArray.length, policyFullnameTemplates.length));
                }
                policyFullnameValueTokens = parseTemplateTokens(tempArray, tokenSeperator);
            }else{
                policyFullnameTemplates = null;
                policyFullnameValueTokens = null;
            }
            
        } finally {
            if (is != null) {
                //should do try/catch here
                is.close();
            }
        }
    }
    
    protected TemplateToken[][] parseTemplateTokens(String[] templateTokensStrs, String tokenSeperator){
        TemplateToken[][] result = new TemplateToken[templateTokensStrs.length][];
        for (int i = 0; i < templateTokensStrs.length; i++) {
            String templatesTokensStr = templateTokensStrs[i];

            String[] tokenStrs = templatesTokensStr.split(tokenSeperator);
            result[i] = new TemplateToken[tokenStrs.length];
            
            for (int j = 0; j < tokenStrs.length; j++) {
                result[i][j] = tokenStrs[j].equals(NULL_TOKEN) 
                        ? null 
                        : new Token(tokenStrs[j], this);
            }
        }
        
        return result;
    }
    
    protected void convertNullTokenToNull(String[] array){
        for(int i=0; i< array.length;i++){
            if (array[i].equals(NULL_TOKEN)) {
                array[i] = null;
            }
        }
    }
    
    @Override
    public boolean isHacker() {
        return isHacker;
    }

    //basic
    public int getNumberOfUsers() {
        return numberOfUsers;
    }

    public int getNumberOfHosts() {
        return numberOfHosts;
    }

    public int getNumberOfPolicies() {
        return numberOfPolicies;
    }
    
    
    
    //logs
    public Date getStartDate() {
        return startDate;
    }
    
    public int getNumberOfDays() {
        return numberOfDays;
    }
    
    public int getRecordsPerSunday() {
        return recordsPerSunday;
    }

    public int getRecordsPerMonday() {
        return recordsPerMonday;
    }

    public int getRecordsPerTuesday() {
        return recordsPerTuesday;
    }

    public int getRecordsPerWednesday() {
        return recordsPerWednesday;
    }

    public int getRecordsPerThursday() {
        return recordsPerThursday;
    }

    public int getRecordsPerFriday() {
        return recordsPerFriday;
    }

    public int getRecordsPerSaturday() {
        return recordsPerSaturday;
    }

    public int getLogRandomPercent() {
        return logRandomPercent;
    }
    
    public int[] getLogLevelDistrubution(){
        return logLevelDistrubution;
    }
    
    
    
    //logs type
    public int getPercentOfDenyLogs() {
        return percentOfDenyLogs;
    }
    
    /**
     * TODO, maybe more in the future
     * if it is not a policy log, it is a tracking log
     */
    public boolean isPolicyLogs() {
        return policyType == PolicyType.POLICY;
    }
    
    
    
    //application
    public int getNumberOfApplications(){
        return numberOfApplications;
    }
    
    public String[] getApplicationTemplates() {
        return applicationTemplates;
    }

    public TemplateToken[][] getApplicationValueTokens() {
        return applicationValueTokens;
    }

    
    
    //from resources
    public int getNumberOfFromResources() {
        return numberOfFromResources;
    }
    
    public String[] getFromResourceTemplates() {
        return fromResourceTemplates;
    }

    public TemplateToken[][] getFromResourceValueTokens() {
        return fromResourceValueTokens;
    }
    
    
    
    //to resources
    public int getNumberOfToResources() {
        return numberOfToResources;
    }

    public String[] getToResourceTemplates() {
        return toResourceTemplates;
    }
    
    public TemplateToken[][] getToResourceValueTokens() {
        return toResourceValueTokens;
    }
    
    //custom attribute
    public float getCustomAttrsPerLog() {
        return customAttrsPerLog;
    }
    
    public String[] getCustomAttributes() {
        return customAttributes;
    }

    public Map<String, String> getCustomAttibToTypeMap() {
		return customAttibToTypeMap;
	}

	public String[] getCustomAttributeTemplates() {
        return customAttributeTemplates;
    }

    public TemplateToken[][] getCustomAttributeValueTokens() {
        return customAttributeValueTokens;
    }

    //obligation
    public float getObligationsPerLog() {
        return obligationsPerLog;
    }

    public String[] getObligationsNames() {
        return obligationsNames;
    }

    public int getNumberOfObligationAttrOne() {
        return numberOfObligationAttrOne;
    }
    

    public String[] getObligationAttrOneTemplates() {
        return obligationAttrOneTemplates;
    }

    public TemplateToken[][] getObligationAttrOneValueTokens() {
        return obligationAttrOneValueTokens;
    }

    public int getNumberOfObligationAttrTwo() {
        return numberOfObligationAttrTwo;
    }
    
    
    public String[] getObligationAttrTwoTemplates() {
        return obligationAttrTwoTemplates;
    }

    public TemplateToken[][] getObligationAttrTwoValueTokens() {
        return obligationAttrTwoValueTokens;
    }

    public int getNumberOfObligationAttrThree() {
        return numberOfObligationAttrThree;
    }

    public String[] getObligationAttrThreeTemplates() {
        return obligationAttrThreeTemplates;
    }

    public TemplateToken[][] getObligationAttrThreeValueTokens() {
        return obligationAttrThreeValueTokens;
    }

    
    //generate data
    public String[] getHostnameTemplates() {
        return hostnameTemplates;
    }

    public TemplateToken[][] getHostnameValueTokens() {
        return hostnameValueTokens;
    }
    
    public String[] getUserDisplaynameTemplates() {
        return userDisplaynameTemplates;
    }

    public TemplateToken[][] getUserDisplaynameValueTokens() {
        return userDisplaynameValueTokens;
    }

    public String[] getUserFirstnameTemplates() {
        return userFirstnameTemplates;
    }

    public TemplateToken[][] getUserFirstnameValueTokens() {
        return userFirstnameValueTokens;
    }

    public String[] getUserLastnameTemplates() {
        return userLastnameTemplates;
    }

    public TemplateToken[][] getUserLastnameValueTokens() {
        return userLastnameValueTokens;
    }

    public String[] getUserSidTemplates() {
        return userSidTemplates;
    }

    public TemplateToken[][] getUserSidValueTokens() {
        return userSidValueTokens;
    }

    public String[] getPolicyFullnameTemplates() {
        return policyFullnameTemplates;
    }

    public TemplateToken[][] getPolicyFullnameValueTokens() {
        return policyFullnameValueTokens;
    }



    static class Token extends TemplateToken {
        public Token(String str, final ReporterDataPerfConfig config){
            super(str, config);
        }
        
        @Override
        protected String[] getListedValues(String value, Object... optionals) {
            ReporterDataPerfConfig config = (ReporterDataPerfConfig)optionals[0];
            if (value.equals(APPLICATION_FILE_EXTENSIONS)) {
                return config.applicationFileExtension;
            } else if (value.equals(FROM_RESOURCE_FILE_EXTENSIONS)) {
                return config.fromResourceFileExtension;
            } else if (value.equals(TO_RESOURCE_FILE_EXTENSIONS)) {
                return config.toResourceFileExtension;
            } else {
                return super.getListedValues(value, optionals);
            }
        }
    }
}
