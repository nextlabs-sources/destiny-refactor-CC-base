/*
 * Created on May 5, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/common/BasicLDAPEnrollmentProperties.java#1 $
 */

public interface BasicLDAPEnrollmentProperties {

    /*
     * Supported types properties:
     */
    String ENROLL_USERS 					= "enroll.users";
    String ENROLL_CONTACTS 					= "enroll.contacts";
    String ENROLL_COMPUTERS 				= "enroll.computers";
    String ENROLL_APPLICATIONS 				= "enroll.applications";
    String ENROLL_GROUPS					= "enroll.groups";
    
    /*
     * Should missing attributes be skipped or should we write an empty
     * value to the dictionary
     */
    String STORE_MISSING_ATTRIBUTES = "store.missing.attributes";

    /*
     * Auto sync parameters:
     */
    String START_TIME 						= "ScheduledSyncTime".toLowerCase();
    String PULL_INTERVAL 					= "ScheduledSyncInterv".toLowerCase();
    String TIME_FORMAT						= "ScheduledSyncTimeFormat".toLowerCase();

    
    /*
     * These are attributes that are typically required of all enrollments:
     */
    String STATIC_ID_ATTRIBUTE 				= "entry.attributefor.staticid";
   

    String GROUP_ENUMERATION_ATTRIBUTE 		= "group.attributefor.enumeration";

    String STRING_DATATYPE_TOKEN 			= "string.";
    String CSSTRING_DATATYPE_TOKEN 			= "cs-string.";
    String MULTISTRING_DATATYPE_TOKEN 		= "multi-string.";
    String LONGSTRING_DATATYPE_TOKEN 		= "long-string.";
    String NUMBER_DATATYPE_TOKEN 			= "number.";
    String DATE_DATATYPE_TOKEN 				= "date.";
    
    String[] DATA_TYPE_TOKENS = new String[] { 
    		STRING_DATATYPE_TOKEN, 
    		CSSTRING_DATATYPE_TOKEN,
			MULTISTRING_DATATYPE_TOKEN, 
			LONGSTRING_DATATYPE_TOKEN, 
			NUMBER_DATATYPE_TOKEN,
			DATE_DATATYPE_TOKEN
	};
    
    String REQUIREMENT_TOKEN 				= "requirements";
    String GROUP_REQUIREMENTS 				= "group." + REQUIREMENT_TOKEN;
    String STRUCTURAL_GROUP_REQUIREMNTS		= "structure." + REQUIREMENT_TOKEN;
    String OTHER_REQUIREMNTS				= "other." + REQUIREMENT_TOKEN;

    /*
     * Searchable attribute prefixes:
     */
    String USER_SEARCHABLE_PREFIX 			= "user.";
    String USER_REQUIREMENTS 				= USER_SEARCHABLE_PREFIX + REQUIREMENT_TOKEN;
    
    String CONTACT_SEARCHABLE_PREFIX 		= "contact.";
    String CONTACT_REQUIREMENTS 			= CONTACT_SEARCHABLE_PREFIX + REQUIREMENT_TOKEN;

    String COMPUTER_SEARCHABLE_PREFIX 		= "computer.";
    String COMPUTER_REQUIREMENTS 			= COMPUTER_SEARCHABLE_PREFIX + REQUIREMENT_TOKEN;

    String APPLICATION_SEARCHABLE_PREFIX 	= "application.";
    String APPLICATION_REQUIREMENTS 		= APPLICATION_SEARCHABLE_PREFIX + REQUIREMENT_TOKEN;
    
    String SITE_SEARCHABLE_PREFIX 			= "site.";
    
    String FORMATTER_SUFFIX                 = ".formatter";
}
