/* 
 * All sources, binaries and HTML pages (C) copyright 2004-2014 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import com.nextlabs.destiny.container.shared.inquirymgr.SharedLib;

/**
 * @author nnallagatla
 *
 */
public interface Constants {
	public static final String REPORT_POLICY_ACTIVITY_LOG_TABLE="RPA_LOG";
	public static final String REPORT_POLICY_CUSTOM_ATTR_TABLE="RPA_LOG_ATTR";
	public static final String REPORT_POLICY_ACTIVITY_MAPPING_TABLE="RPA_LOG_MAPPING";
	public static final String ARCHIVE_POLICY_ACTIVITY_LOG_TABLE="APA_LOG";
	public static final String ARCHIVE_POLICY_CUSTOM_ATTR_TABLE="APA_LOG_ATTR";
	public static final String OLD_REPORT_POLICY_ACTIVITY_LOG_TABLE= SharedLib.REPORT_PA_TABLE;
	public static final String OLD_REPORT_POLICY_CUSTOM_ATTR_TABLE=	SharedLib.REPORT_PA_CUST_ATTR_TABLE;
    public static final String NUMBER_OF_EXTENDED_ATTRS_PROPERTY="numberOfExtendedAttrs";
    public static final String REPORT_MAX_RECORD_FETCH_COUNT="report.max.records";
    public static final String POLICY_TAGS = "POLICY_TAGS";
    
    public static final String USER_ATTRIBUTES_BLACKLIST_PROPERTY = "userAttributesBlackList";
    public static final String RESOURCE_ATTRIBUTES_BLACKLIST_PROPERTY = "resourceAttributesBlackList";
    public static final String POLICY_ATTRIBUTES_BLACKLIST_PROPERTY = "policyAttributesBlackList";
    public static final String BLACKLIST_SEPARATOR= ",";
}
