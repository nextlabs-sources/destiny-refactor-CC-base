/*
 * Created on Jun 12, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync.log;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.hibernate.HibernateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.datastore.hibernate.SQLHelper;
import com.nextlabs.destiny.container.dac.datasync.Constants;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.AttributeColumnMappingDAOImpl;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.AttributeColumnMappingDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/log/ReportPolicyActivityLog.java#1 $
 */

public class ReportPolicyActivityLog extends ReportLog {
    
	public static final Log LOG = LogFactory.getLog(ReportPolicyActivityLog.class);
	
    public static class CustomAttribute {

    	public static final int CUSTOM_LOG_ID_COLUMN;
        public static final int CUSTOM_ATTR_VALUE_COLUMN;
        public static final int CUSTOM_ATTR_ID_COLUMN;
        
        public static final String RESOURCE = "RESOURCE";
        public static final String USER = "USER";
        public static final String POLICY = "POLICY";
		public static final String OTHERS = "OTHERS";
		
        public static final String USER_RAW_TYPE = "SU";
        public static final String HOST_RAW_TYPE = "SH";
		public static final String APPLICATION_RAW_TYPE = "SA";
        
        static final String INSERT_CUSTOM_ATTR_QUERY_TEMPLATE;
        
        static{   
            
            int j =1;
            CUSTOM_LOG_ID_COLUMN = j++;
            CUSTOM_ATTR_ID_COLUMN = j++;
            CUSTOM_ATTR_VALUE_COLUMN = j++;
            
            INSERT_CUSTOM_ATTR_QUERY_TEMPLATE = 
                    "insert into %s (%s,attr_id,attr_value) values "
                    + SQLHelper.makeInList(j -1);
        }
        
        public long id;
        public String attributeType;
        public String attributeName;
        public String attributeValue;
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (id ^ (id >>> 32));
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CustomAttribute other = (CustomAttribute) obj;
            if (id != other.id)
                return false;
            return true;
        }
    }
    
    protected static int DEFAULT_NUMBER_OF_EXTENDED_ATTRS = 100;
    protected static String DEFAULT_ATTR_PREFIX = "attr";
	private int numberOfAdditionalAttributes = DEFAULT_NUMBER_OF_EXTENDED_ATTRS;
    private String attributePrefix = DEFAULT_ATTR_PREFIX;
    private Map<String, Map<String, AttributeColumnMappingDO>> attrColumnNameMapping;
    private boolean[] columnsInUse;
    
    public static void generateInsertQuery(int numberOfAdditionalAttrs, String attributePrefix)
    {	
        INSERT_LOG_QUERY = "insert into " + Constants.REPORT_POLICY_ACTIVITY_LOG_TABLE
                + " (id,time, request_date_time, month_nb, week_nb, day_nb, hour_nb, minute_nb "
                + ",host_id,host_ip,host_name"
                + ",user_id,user_name,user_sid"
                + ",application_id,application_name"
                + ",action,policy_id,policy_name,policy_fullname,policy_decision" 
                + ",decision_request_id,log_level"
                + ",from_resource_name"
                + ",from_resource_prefix,from_resource_path,from_resource_short_name"
                + ",from_resource_size,from_resource_owner_id"
                + ",from_resource_created_date,from_resource_modified_date"
                + ",to_resource_name" 
                + getExtendedAttributeStringInsertSQL(attributePrefix, numberOfAdditionalAttrs)
                + ") values " + SQLHelper.makeInList(TO_RESOURCE_NAME + numberOfAdditionalAttrs);
    }    
    
    public ReportPolicyActivityLog(AttributeColumnMappingInfoWrapper wrapper)
    {
    	attrColumnMapInfo = wrapper;
    	
    	numberOfAdditionalAttributes = wrapper.getNumberOfAdditionalAttributes();
    	attributePrefix = wrapper.getAttributePrefix();
    	attrColumnNameMapping = wrapper.getAttrColumnNameMapping();
    	columnsInUse = wrapper.getColumnsInUse();
    }
    
    public Timestamp time;
    public long dateTime;
    public long month;
    public long week;
    public long day;
    public long hour;
    public long minute;
    
    public long hostId;
    public String hostIp;
    public String hostName;
    
    public long userId;
    public String userName;
    public String userSid;
    
    public long applicationId;
    public String applicationName;
    
    public String action;
    
    public int logLevel;
    
    public long policyId;
    public String policyFullname;
    public String policyName;
    public String policyDecision;
    public long decisionRequestId;
    
    public String fromResourceName;
    public Long fromResourceSize;
    public String fromResourceOwnerId;
    public Long fromResourceCreatedDate;
    public Long fromResourceModifiedDate;

    public String fromResourcePrefix;
    public String fromResourcePath;
    public String fromResourceShortName;

    public String toResourceName;
    
    public List<CustomAttribute> attrs;
    
    public Map<String, String> dynamicUserAttrs;
    
    public Map<String, String> userAttrs;
    
    public Map<String, String> policyAttrs;
    
    
    protected AttributeColumnMappingInfoWrapper attrColumnMapInfo;
	
    protected static final int ID;
    
    protected static final int TIME;
    protected static final int DATE_TIME;
    protected static final int MONTH;
    protected static final int WEEK;
    protected static final int DAY;
    protected static final int HOUR;
    protected static final int MINUTE;
    
    protected static final int HOST_ID;
    protected static final int HOST_IP;
    protected static final int HOST_NAME;
    
    protected static final int USER_ID;
    protected static final int USER_NAME;
    protected static final int USER_SID;

    protected static final int APP_ID;
    protected static final int APP_NAME;
    
    protected static final int ACTION;
    
    protected static final int POLICY_ID;
    protected static final int POLICY_NAME ;
    protected static final int POLICY_FULLNAME ;
    protected static final int POLICY_DECISION;
    protected static final int DECISION_REQUEST_ID;
    protected static final int LEVEL;
    
    protected static final int FROM_RESOURCE_NAME;
    protected static final int FROM_RESOURCE_PREFIX;
    protected static final int FROM_RESOURCE_PATH;
    protected static final int FROM_RESOURCE_SHORT_NAME;
    protected static final int FROM_RESOURCE_SIZE;
    protected static final int FROM_RESOURCE_OWNER_ID;
    protected static final int FROM_RESOURCE_CREATED_DATE;
    protected static final int FROM_RESOURCE_MODIFIED_DATE;
    
    protected static final int TO_RESOURCE_NAME;
    
    protected static String INSERT_LOG_QUERY;
    
    public static final String INSERT_CUSTOM_ATTR_QUERY;
    
    static {
        int i = 1;
        ID = i++;
        
        TIME = i++;
        DATE_TIME = i++;
        MONTH = i++;
        WEEK = i++;
        DAY = i++;
        HOUR = i++;
        MINUTE = i++;
        
        HOST_ID = i++;
        HOST_IP = i++;
        HOST_NAME = i++;
        
        USER_ID = i++;
        USER_NAME = i++;
        USER_SID = i++;

        APP_ID = i++;
        APP_NAME = i++;
        
        ACTION = i++;
        
        POLICY_ID = i++;
        POLICY_NAME = i++;
        POLICY_FULLNAME = i++;
        POLICY_DECISION = i++;
        DECISION_REQUEST_ID = i++;
        LEVEL = i++;
        
        FROM_RESOURCE_NAME = i++;
        FROM_RESOURCE_PREFIX = i++;
        FROM_RESOURCE_PATH = i++;
        FROM_RESOURCE_SHORT_NAME = i++;
        
        FROM_RESOURCE_SIZE = i++;
        FROM_RESOURCE_OWNER_ID = i++;
        FROM_RESOURCE_CREATED_DATE = i++;
        FROM_RESOURCE_MODIFIED_DATE = i++;
        
        TO_RESOURCE_NAME = i++;

        INSERT_CUSTOM_ATTR_QUERY = String.format(
                CustomAttribute.INSERT_CUSTOM_ATTR_QUERY_TEMPLATE,
                Constants.REPORT_POLICY_CUSTOM_ATTR_TABLE,
                "policy_log_id"
        );
        generateInsertQuery(DEFAULT_NUMBER_OF_EXTENDED_ATTRS, DEFAULT_ATTR_PREFIX);
    }
    
    /**
     * This method returns the SQL string for insert
     * @param attrPrefix
     * @param numberOfAttributes
     * @return
     */
    public static String getExtendedAttributeStringInsertSQL(String attrPrefix, int numberOfAttributes)
    {
    	if (numberOfAttributes <= 0)
    	{
    		return "";
    	}
    	
    	StringBuilder strB = new StringBuilder();
    	
    	for (int i = 0; i< numberOfAttributes; i++)
    	{
			strB.append(", ");
    		strB.append(attrPrefix).append(i+1);
    	}
    	
    	return strB.toString();
    }
	
	/**
	 * This method, creates an instance of AttributeColumnMappingDO and inserts in database through
	 * AttributeColumnMappingDAO. It also updates maps that keep track of current mappings in use
	 * 
	 * @param attributeName
	 * @param attrType
	 * @param columnsInUse
	 * @return
	 */
    protected AttributeColumnMappingDO insertMapping(String attributeName,
			String attrType, boolean[] columnsInUse) {    	
    	AttributeColumnMappingDO mapping = null;
		synchronized (columnsInUse) {
			int lowestAvailableColumnIndex = getFreeColumn(columnsInUse);
			mapping = new AttributeColumnMappingDO();
			mapping.setAttributeName(attributeName);
			mapping.setAttributeType(attrType);
			mapping.setDataType("STRING");
			mapping.setDynamic(true);

			if (lowestAvailableColumnIndex != -1) {
				mapping.setColumnName(attributePrefix
						+ lowestAvailableColumnIndex);
			}

			boolean status = false;
			try {
				status = AttributeColumnMappingDAOImpl.addMapping(mapping);
			} catch (HibernateException e) {
				LOG.error(e);
			}
			/*
			 * update the mapping if insertion is successful
			 */
			if (status)
			{
				if (attrColumnNameMapping.get(attrType) == null)
				{
					attrColumnNameMapping.put(attrType, new HashMap<String, AttributeColumnMappingDO>());
				}
				attrColumnNameMapping.get(attrType).put(mapping.getAttributeName().toLowerCase(), mapping);
				/*
				 * if attribute has been mapped to one of the columns in 
				 * main table, then set that the column has been taken
				 */
				if (lowestAvailableColumnIndex != -1)
				{
					columnsInUse[lowestAvailableColumnIndex] = true;
				}
				LOG.info("Inserted Attribute Mapping for " + mapping.getAttributeName() 
						+ " to " + mapping.getColumnName() + " with ID: " + mapping.getId());
			}
			else
			{
				LOG.info("Inserted Attribute Mapping for " + mapping.getAttributeName() 
						+ " to " + mapping.getColumnName() + " with ID: " + mapping.getId());
			}
		}
		return mapping;
	}

    /**
     * This method goes through the columns currently in use and returns the index of first free column.
     * If attr15 is the first free column, then 15 is returned
     * 
     * @param columnsInUse
     * @return
     */
	private int getFreeColumn(boolean[] columnsInUse) {
		
		if (columnsInUse == null)
		{
			return -1;
		}
		
		for ( int i =1; i <= numberOfAdditionalAttributes; i++)
		{
			if (!columnsInUse[i])
			{
				return i;
			}
		}
		return -1;
	}

    public void setCustomAttributesValue(PreparedStatement statement) throws SQLException {
    	setPolicyAttributesValue(statement);
        setUserAttributesValue(statement);
        if (attrs != null) {
            for (CustomAttribute attr : attrs) {
            	AttributeColumnMappingDO mapping = getMappedColumnForCustomAttr(attr, false);
            	
            	if (mapping != null && mapping.getColumnName() != null)
            	{
            		/*
            		 * we would have already inserted this attribute as part of main table 
            		 */
            		continue;
            	}
            	
            	if (mapping == null)
            	{
            		//this cannot happen as we would have inserted mapping already at setValue()
            		LOG.error("Mapping cannot be null");
            		
            		continue;
            	}
            	
                statement.setLong(CustomAttribute.CUSTOM_LOG_ID_COLUMN,     this.id);
                statement.setLong(CustomAttribute.CUSTOM_ATTR_ID_COLUMN,  mapping.getId());                
                statement.setString(CustomAttribute.CUSTOM_ATTR_VALUE_COLUMN, attr.attributeValue);
                statement.addBatch();
            }
            
        }
    }
    
    /**
     * This method sets the user attribute values
     * 
     * @param statement
     * @throws SQLException
     */
	public void setPolicyAttributesValue(PreparedStatement statement) throws SQLException
	{
        if (policyAttrs != null) {
        	//LOG.info("setting user attributes");
        	Map<String, AttributeColumnMappingDO> policyAttrMap = 
        			attrColumnNameMapping.get(CustomAttribute.POLICY);
        	
            for (Iterator<Map.Entry<String,String>> it = policyAttrs.entrySet().iterator(); it.hasNext(); ) {
            	
            	Map.Entry<String, String> record = it.next();
            	
            	AttributeColumnMappingDO mapping = policyAttrMap.get(record.getKey().toLowerCase());
            	
            	if (mapping != null && mapping.getColumnName() != null)
            	{
            		/*
            		 * we would have already inserted this attribute as part of main table 
            		 */
            		
            		continue;
            	}
            	
            	if (mapping == null)
            	{
            		//this cannot happen as we would have inserted mapping already at setValue()
            		LOG.error("Mapping cannot be null");
            		
            		continue;
            	}
                
                statement.setLong(CustomAttribute.CUSTOM_LOG_ID_COLUMN,     this.id);
                statement.setLong(CustomAttribute.CUSTOM_ATTR_ID_COLUMN,  mapping.getId());                
                statement.setString(CustomAttribute.CUSTOM_ATTR_VALUE_COLUMN, record.getValue());
                
                statement.addBatch();
            }
        }
	}
    
    /**
     * This method sets the user attribute values
     * 
     * @param statement
     * @throws SQLException
     */
	public void setUserAttributesValue(PreparedStatement statement) throws SQLException
	{
		Map<String, String> attributes = new HashMap<String, String>();
		/*
       	 * overwrite enrolled attributes with dynamic attributes
       	 */
		if (userAttrs != null){
			attributes.putAll(userAttrs);
		}
		if (dynamicUserAttrs != null){
			attributes.putAll(dynamicUserAttrs);
		}
        if (!attributes.isEmpty()) {
        	//LOG.info("setting user attributes");
        	Map<String, AttributeColumnMappingDO> userAttrMap = 
        			attrColumnNameMapping.get(CustomAttribute.USER);
        	
            for (Iterator<Map.Entry<String,String>> it = attributes.entrySet().iterator(); it.hasNext(); ) {
            	
            	Map.Entry<String, String> record = it.next();
            	
            	AttributeColumnMappingDO mapping = userAttrMap.get(record.getKey().toLowerCase());
            	
            	if (mapping != null && mapping.getColumnName() != null)
            	{
            		/*
            		 * we would have already inserted this attribute as part of main table 
            		 */
            		
            		continue;
            	}
            	
            	if (mapping == null)
            	{
            		//this cannot happen as we would have inserted mapping already at setValue()
            		LOG.error("Mapping cannot be null");
            		
            		continue;
            	}
                
                statement.setLong(CustomAttribute.CUSTOM_LOG_ID_COLUMN,     this.id);
                statement.setLong(CustomAttribute.CUSTOM_ATTR_ID_COLUMN,  mapping.getId());                
                statement.setString(CustomAttribute.CUSTOM_ATTR_VALUE_COLUMN, record.getValue());
                
                statement.addBatch();
            }
        }
	}
	

	
    public void setValue(PreparedStatement statement) throws SQLException {
        statement.setLong(  ID,                          id);
        
        statement.setTimestamp(TIME,                     time);

        statement.setLong(  DATE_TIME,                   dateTime);
        statement.setLong(  MONTH,                       month);
        statement.setLong(  WEEK,                        week);
        statement.setLong(  DAY,                         day);
        statement.setLong(  HOUR,                        hour);
        statement.setLong(  MINUTE,                      minute);

        statement.setLong(  HOST_ID,                     hostId);
        statement.setString(HOST_IP,                     hostIp);
        statement.setString(HOST_NAME,                   hostName);

        statement.setLong(  USER_ID,                     userId);
        statement.setString(USER_NAME,                   userName);
        statement.setString(USER_SID,                    userSid);

        statement.setLong(  APP_ID,                      applicationId);
        statement.setString(APP_NAME,                    applicationName);

        statement.setString(ACTION,                      action);

		statement.setLong(  POLICY_ID,                   policyId);
        statement.setString(POLICY_NAME,                 policyName);
        statement.setString(POLICY_FULLNAME,             policyFullname);
        statement.setString(POLICY_DECISION,             policyDecision);
        statement.setLong(  DECISION_REQUEST_ID,         decisionRequestId);
        
        statement.setInt(   LEVEL,                       logLevel);

        statement.setString(FROM_RESOURCE_NAME,          fromResourceName);
        statement.setString(FROM_RESOURCE_PREFIX,        fromResourcePrefix);
        statement.setString(FROM_RESOURCE_PATH,          fromResourcePath);
        statement.setString(FROM_RESOURCE_SHORT_NAME,    fromResourceShortName);

        if(fromResourceSize != null){
            statement.setLong(FROM_RESOURCE_SIZE,        fromResourceSize);
        }else{
            statement.setNull(FROM_RESOURCE_SIZE,        java.sql.Types.BIGINT);
        }
        
        statement.setString(FROM_RESOURCE_OWNER_ID,      fromResourceOwnerId);
        
        if(fromResourceCreatedDate != null){
            statement.setLong(FROM_RESOURCE_CREATED_DATE,    fromResourceCreatedDate);
        } else {
            statement.setNull(FROM_RESOURCE_CREATED_DATE, java.sql.Types.BIGINT);
        }
        
        if (fromResourceModifiedDate != null) {
            statement.setLong(FROM_RESOURCE_MODIFIED_DATE, fromResourceModifiedDate);
        } else {
            statement.setNull(FROM_RESOURCE_MODIFIED_DATE, java.sql.Types.BIGINT);
        }

        statement.setString(TO_RESOURCE_NAME, toResourceName);
        /*
         * set additional attributes as well
         */
       	
       	int [] attrFlags = new int[numberOfAdditionalAttributes + 1];
       	
       	Map<String, String> resolvedUserAttr = new HashMap<String, String>();
       	/*
       	 * overwrite enrolled attributes with dynamic attributes
       	 */
       	if (userAttrs != null) {
       		resolvedUserAttr.putAll(userAttrs);
       	}
       	if (dynamicUserAttrs != null) {
       		resolvedUserAttr.putAll(dynamicUserAttrs);
       	}
       	
		if (!resolvedUserAttr.isEmpty()) {
			
			Map<String, AttributeColumnMappingDO> userAttrMap = 
					attrColumnNameMapping.get(CustomAttribute.USER);
			
			if (userAttrMap == null)
			{
				userAttrMap =  new HashMap<String, AttributeColumnMappingDO>();
				attrColumnNameMapping.put(CustomAttribute.USER, userAttrMap);
			}
			
			for (Iterator<Map.Entry<String,String>> it = resolvedUserAttr.entrySet().iterator(); it.hasNext(); ) {
				
				Map.Entry<String, String> record = it.next();
				
				AttributeColumnMappingDO mapping = userAttrMap.get(record.getKey().toLowerCase());

				if (mapping == null) {
					
					//insert a new entry into the mapping table
					mapping = insertMapping(record.getKey(), CustomAttribute.USER, 
							columnsInUse);
				}

				String column = mapping.getColumnName();

				if (column == null) {					
					/*
					 * if mapping is NULL then we will insert this attribute in the child table
					 *  which will be handled in the setCustomAttributesValue() part
					 */
					continue;
				}
				
				int index = Integer.parseInt(column.replace(attributePrefix, ""));
				
				int parameterIndex = TO_RESOURCE_NAME + index;
				
				attrFlags[index] = 1;
				
				statement.setString(parameterIndex, record.getValue());
			}			
		}
       	
		
		if (policyAttrs != null) {
			
			Map<String, AttributeColumnMappingDO> policyAttrMap = 
					attrColumnNameMapping.get(CustomAttribute.POLICY);
			
			if (policyAttrMap == null)
			{
				policyAttrMap =  new HashMap<String, AttributeColumnMappingDO>();
				attrColumnNameMapping.put(CustomAttribute.POLICY, policyAttrMap);
			}
			
			for (Iterator<Map.Entry<String,String>> it = policyAttrs.entrySet().iterator(); it.hasNext(); ) {
				
				Map.Entry<String, String> record = it.next();
				
				AttributeColumnMappingDO mapping = policyAttrMap.get(record.getKey().toLowerCase());

				if (mapping == null) {
					
					//insert a new entry into the mapping table
					mapping = insertMapping(record.getKey(), CustomAttribute.POLICY, 
							columnsInUse);
				}

				String column = mapping.getColumnName();

				if (column == null) {					
					/*
					 * if mapping is NULL then we will insert this attribute in the child table
					 *  which will be handled in the setCustomAttributesValue() part
					 */
					continue;
				}
				
				int index = Integer.parseInt(column.replace(attributePrefix, ""));
				
				int parameterIndex = TO_RESOURCE_NAME + index;
				
				attrFlags[index] = 1;
				
				statement.setString(parameterIndex, record.getValue());
			}			
		}
		
		if (attrs != null) {
			
			for (CustomAttribute attr : attrs) {
		
				AttributeColumnMappingDO mappedColumn = getMappedColumnForCustomAttr(attr, true);

				if (mappedColumn != null && mappedColumn.getColumnName() == null) {					
					/*
					 * if mapping is NULL then we will insert this attribute in the child table
					 *  which will be handled in the setCustomAttributesValue() part
					 */
					continue;
				}
				String column = mappedColumn.getColumnName();
				
				int index = Integer.parseInt(column.replace(attributePrefix, ""));
				
				int parameterIndex = TO_RESOURCE_NAME + index;
				
				attrFlags[index] = 1;
				
				statement.setString(parameterIndex, attr.attributeValue);
			}			
		}  
		
		for (int i = 1; i <= numberOfAdditionalAttributes; i++)
		{
			if (attrFlags[i] != 1)
			{
				statement.setNull(TO_RESOURCE_NAME + i, java.sql.Types.VARCHAR);
			}
		}
		
        statement.addBatch();
    }
    
	private AttributeColumnMappingDO getMappedColumnForCustomAttr(CustomAttribute attr, boolean insertIfMissing) {
		/*
		possible rawType values: RF, RT, SU, SA, SH
		*/
		String rawType = attr.attributeType;
		String type = CustomAttribute.RESOURCE;

		if (CustomAttribute.USER_RAW_TYPE.equals(rawType)){
			type = CustomAttribute.USER;
		}
		else if (CustomAttribute.HOST_RAW_TYPE.equals(rawType) || CustomAttribute.APPLICATION_RAW_TYPE.equals(rawType)){
			type = CustomAttribute.OTHERS;			
		}
		
		Map<String, AttributeColumnMappingDO> attrMap = attrColumnNameMapping.get(type);

		if (attrMap == null) {
			attrMap =  new HashMap<String, AttributeColumnMappingDO>();
			attrColumnNameMapping.put(type, attrMap);
		}

		AttributeColumnMappingDO mapping = attrMap.get(attr.attributeName.toLowerCase());

		/*
		create a new mapping only if it does not exist and insertIfMissing flag is passed as true
		*/
		if (mapping == null && insertIfMissing) {
		//insert a new entry into the mapping table
			mapping = insertMapping(attr.attributeName, type, 
				columnsInUse);
		}
		
		return mapping;
	}
	
	public static String getInsertQueryString() {
		return INSERT_LOG_QUERY;
	}
}
