/*
 * Created on Apr 19, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.services.enrollment.types.AttributeType;
import com.bluejungle.destiny.services.enrollment.types.Column;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentType;
import com.bluejungle.destiny.services.enrollment.types.EntityType;
import com.bluejungle.destiny.services.enrollment.types.Realm;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentStatus;
import com.bluejungle.destiny.services.enrollment.types.Profile;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IUpdateRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/service/ServiceHelper.java#1 $
 */

public class ServiceHelper {

    public static final Log log = LogFactory.getLog(ServiceHelper.class);

    private static Map<String, EnrollmentType> classNameToEnrollmentTypeMap;
    static{
        classNameToEnrollmentTypeMap = new HashMap<String, EnrollmentType>();
        classNameToEnrollmentTypeMap.put(EnrollmentTypeEnumType.DIRECTORY.getClassName(),
                EnrollmentType.DIRECTORY);
        classNameToEnrollmentTypeMap.put(EnrollmentTypeEnumType.LDIF.getClassName(),
                EnrollmentType.LDIF);
        classNameToEnrollmentTypeMap.put(EnrollmentTypeEnumType.DOMAINGROUP.getClassName(),
                EnrollmentType.DOMAINGROUP);
        classNameToEnrollmentTypeMap.put(EnrollmentTypeEnumType.PORTAL.getClassName(),
                EnrollmentType.PORTAL);
        classNameToEnrollmentTypeMap.put(EnrollmentTypeEnumType.TEXT.getClassName(),
                EnrollmentType.PROPERTY_FILE);
        classNameToEnrollmentTypeMap.put(EnrollmentTypeEnumType.CLIENT_INFO.getClassName(),
                EnrollmentType.CLIENT_INFO);
    }
    
    private static final char DEFAULT_AXIS_SAFE_CHAR = ' ';
    
    public static String axisSafeString(String str) {
        return ServiceHelper.axisSafeString(str, DEFAULT_AXIS_SAFE_CHAR);
    }
    
    public static String axisSafeString(String str, char replaceInvalidCharWith) {
        // make sure the errorMessage doesn't contains any invalid UTF char that axis can't parse.
        // fix bug 7628
        if(str == null){
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append(c < 0x20 ? replaceInvalidCharWith : c);
        }
        return sb.toString();
    }
    
    public static String[] axisSafeString(String[] strs) {
        if(strs == null){
            return null;
        }
        String[] output = new String[strs.length];
        for(int i =0; i< strs.length; i++){
            output[i] = ServiceHelper.axisSafeString(strs[i]);
        }
        
        return output;
    }
    

    public static Column extractWSColumnFromDO(IElementField field) {
        Column columnToReturn = null;
        if (field != null) {
            columnToReturn = new Column();
            columnToReturn.setDisplayName(field.getLabel());
            columnToReturn.setLogicalName(field.getName());
            columnToReturn.setParentType(EntityType.fromValue(field.getObjectTypeName()));
            columnToReturn.setType(AttributeType.fromValue(field.getType().getName().toUpperCase()));
        }
        return columnToReturn;
    }

    public static Realm extractWSRealmFromDO(IEnrollment enrollment) throws DictionaryException {
        if (enrollment == null) {
            return null;
        }
        
        Realm realmToReturn =  new Realm();
        realmToReturn.setName(enrollment.getDomainName());
        
        List<EnrollmentProperty> properties = new ArrayList<EnrollmentProperty>();
        for (String strName : enrollment.getStrPropertyNames()) {
            EnrollmentProperty property = new EnrollmentProperty();
            property.setKey(axisSafeString(strName));
            property.setValue(axisSafeString(new String[] { enrollment.getStrProperty(strName) }));
            properties.add(property);
        }
        for (String numName : enrollment.getNumPropertyNames()) {
            EnrollmentProperty property = new EnrollmentProperty();
            property.setKey(axisSafeString(numName));
            property.setValue(new String[] { String.valueOf(enrollment.getNumProperty(numName)) });
            properties.add(property);
        }
        for (String strArrayName : enrollment.getStrArrayPropertyNames()) {
            EnrollmentProperty property = new EnrollmentProperty();
            property.setKey(axisSafeString(strArrayName));
            property.setValue(axisSafeString(enrollment.getStrArrayProperty(strArrayName)));
            properties.add(property);
        }
        
        EnrollmentProperty[] propertiesArray = properties.toArray(new EnrollmentProperty[properties.size()]);
        realmToReturn.setProfile(new Profile(propertiesArray));

        IUpdateRecord record = enrollment.getStatus();
        EnrollmentStatus enrollmentStatus = extractStatusFromUpdateRecord(record);
        Calendar nst = enrollment.getNextSyncTime();
        if (nst != null) {
        	enrollmentStatus.setNextSyncTime(nst.toString());
        }
        realmToReturn.setStatus(enrollmentStatus);
                 
        EnrollmentType enrollmentType = classNameToEnrollmentTypeMap.get(enrollment.getType());
        if(enrollmentType == null){
            log.warn("Invalid enrollment type: '" + enrollment.getType() 
                    + "' - will set enrollment type to UNKNOWN");
            realmToReturn.setType(EnrollmentType.UNKNOWN);
        } else {
            realmToReturn.setType(enrollmentType);
        }
        return realmToReturn;
    }
    
    private static EnrollmentStatus extractStatusFromUpdateRecord(IUpdateRecord record) {
        //TODO finish the next syncTime
        if(record == null){
            return new EnrollmentStatus("never sync", "", "", "", "");
        }
//        
//        Date start = record.getStartTime();
//        Calendar startCalendar;
//        if (start != null) {
//            startCalendar = Calendar.getInstance();
//            startCalendar.setTime(start);
//        } else {
//            startCalendar = null;
//        }
//
//        Date end = record.getEndTime();
//        Calendar endCalendar;
//        if (end != null) {
//            endCalendar = Calendar.getInstance();
//            endCalendar.setTime(end);
//        } else {
//            endCalendar = null;
//        }
//        
//        String statusString = record.isSuccessful()? "success" : "failed";
//        // When enrollment end, there is always error message, if there is no error message, it is enrolling
//        if ( record.getErrorMessage() == null ) {
//            statusString = "enrolling";
//        }
//        
//        EnrollmentStatus status = new EnrollmentStatus(
//                axisSafeString(statusString), 
//                startCalendar ,
//                endCalendar, 
//                axisSafeString(record.getErrorMessage()),
//                null
//        );
        
        Date start = record.getStartTime();
        String startTime = (start == null )? new String("Enrollment not started") : start.toString();
        Date end = record.getEndTime();
        String endTime = null;
        String statusString = record.isSuccessful()? "success" : "failed";
        if ( end != null ) {
            endTime = end.toString();
        }
        // When enrollment end, there is always error message, if there is no error message, it is enrolling
        if ( record.getErrorMessage() == null ) {
            statusString = "enrolling";
        }
        
        EnrollmentStatus status = new EnrollmentStatus(
                axisSafeString(statusString), 
                startTime,
                endTime, 
                axisSafeString(record.getErrorMessage()),
                record.getNextSyncTime()
        );
        
        return status;
    }
}
