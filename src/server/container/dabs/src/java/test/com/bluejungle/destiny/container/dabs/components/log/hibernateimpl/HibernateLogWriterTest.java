/*
 * Created on Jan 24, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dabs.components.log.hibernateimpl;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.dabs.components.log.ILogWriter;
import com.bluejungle.destiny.container.dabs.components.test.BaseDabsComponentTest;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryTestData;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryTestData;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.framework.utils.Pair;

/**
 * Test Case to test
 * com.bluejungle.destiny.container.dabs.components.log.hibernateimpl.HibernateLogWriter
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dabs/src/java/test/com/bluejungle/destiny/container/dabs/components/log/hibernateimpl/HibernateLogWriterTest.java#2 $
 */

public class HibernateLogWriterTest extends BaseDabsComponentTest {

    private final HibernateLogWriter logWriterToTest;
    private final LogWriterTestHelper helper;

    /**
     * Constructor for HibernateLogWriterTest.
     * 
     * @param testName
     */
    public HibernateLogWriterTest() {
        super(HibernateLogWriterTest.class.getName());
        IComponentManager cm = ComponentManagerFactory.getComponentManager();
        ComponentInfo<HibernateLogWriter> logWriterCompInfo = 
            new ComponentInfo<HibernateLogWriter>(
                ILogWriter.COMP_NAME, 
                HibernateLogWriter.class, 
                ILogWriter.class, 
                LifestyleType.SINGLETON_TYPE);
        logWriterToTest = cm.getComponent(logWriterCompInfo);
        helper = LogWriterTestHelper.getInstance();
    }

    /**
     * Setup method
     */
    protected void setUp() throws Exception {
        super.setUp();
        helper.deleteAllLogs();
    }

    /**
     * This test verifies the insertion of policy activity logs
     * @throws DataSourceException 
     * @throws SQLException 
     * @throws HibernateException 
     */
    public void testLogPolicyActivity() throws DataSourceException, HibernateException, SQLException {
        final int nbEntries = 100;
        Map<Long, PolicyActivityLogEntry> entryMap = new HashMap<Long, PolicyActivityLogEntry>(nbEntries);

        PolicyActivityLogEntry[] entries = PolicyActivityLogEntryTestData.generateRandom(nbEntries);
        for (int i = 0; i < nbEntries; i++) {
            PolicyActivityLogEntry entry = entries[i];
            entryMap.put(new Long(entry.getUid()), entry);
        }
        logWriterToTest.log(entries);

        List<PolicyActivityLogEntry> saveEntries = helper.retrievePolicyActivityLogs();
        int i = 0;
        for (PolicyActivityLogEntry entry : saveEntries) {
            assertEquals("Retrieved policy activity log entry should equal the original one.", entryMap.get(new Long(entry.getUid())), entry);
            i++;
        }
        assertEquals("Number of saved entries should be the same as the original.", nbEntries, i);

        // attempt a second save of the same thing -- it should go with no
        // problems
        logWriterToTest.log(entries);
        // but contents of the DB should not be affected
        saveEntries = helper.retrievePolicyActivityLogs();
        i = 0;
        for (PolicyActivityLogEntry entry : saveEntries) {
            assertEquals("Retrieved policy activity log entry should equal the original one.", entryMap.get(new Long(entry.getUid())), entry);
            i++;
        }
        assertEquals("Number of saved entries should be the same as the original.", nbEntries, i);

    }

    /**
     * This test verifies the insertion of tracking activity logs
     * @throws DataSourceException 
     * @throws SQLException 
     * @throws HibernateException 
     */
    public void testLogTrackingActivity() throws DataSourceException, HibernateException, SQLException {
        final int nbEntries = 100;
        Map<Long, TrackingLogEntry> entryMap = new HashMap<Long, TrackingLogEntry>(nbEntries);

        TrackingLogEntry[] entries = TrackingLogEntryTestData.generateRandom(100);
        for (int i = 0; i < nbEntries; i++) {
            TrackingLogEntry entry = entries[i];
            entryMap.put(new Long(entry.getUid()), entry);
        }
        logWriterToTest.log(entries);

        List<TrackingLogEntry> saveEntries = helper.retrieveTrackingActivityLogs();
        int i = 0;

        for (TrackingLogEntry entry : saveEntries) {
            assertEquals("Retrieved tracking log entry should equal the original one.", entryMap.get(new Long(entry.getUid())), entry);
            i++;
        }

        assertEquals("Number of saved entries should be the same as the original.", nbEntries, i);
        // attempt a second save of the same thing -- it should go with no
        // problems
        logWriterToTest.log(entries);
        // but contents of the DB should not be affected
        saveEntries = helper.retrieveTrackingActivityLogs();
        i = 0;
        for (TrackingLogEntry entry : saveEntries) {
            assertEquals("Retrieved tracking log entry should equal the original one.", entryMap.get(new Long(entry.getUid())), entry);
            i++;
        }
        assertEquals("Number of saved entries should be the same as the original.", nbEntries, i);
    }
    
    enum PolicyActivityLogField{
        FROM_RESOURCE_INFO("fromResourceInfo"){
            @Override
            boolean expectGood(TestType type) {
                switch (type) {
                case NULL:
                    return false;
                default:
                    return super.expectGood(type);
                }
            }
            
        },
        FROM_RESOURCE_INFO_NAME(FROM_RESOURCE_INFO, null),
        FROM_RESOURCE_INFO_SIZE(FROM_RESOURCE_INFO, "size"), 
        FROM_RESOURCE_INFO_CREATEDDATE(FROM_RESOURCE_INFO, "createdDate"), 
        FROM_RESOURCE_INFO_MODIFIEDDATE(FROM_RESOURCE_INFO, "modifiedDate"), 
        FROM_RESOURCE_INFO_OWNERID(FROM_RESOURCE_INFO, "ownerId"),
        TO_RESOURCE_INFO("toResourceInfo"),
        TO_RESOURCE_INFO_NAME(TO_RESOURCE_INFO, null), 
        USER_NAME("userName", 255L){
            @Override
            boolean expectGood(TestType type) {
                switch (type) {
                case NULL:
                    return false;
                default:
                    return super.expectGood(type);
                }
            }
            
        },
        USER_ID("userId"), 
        HOST_NAME("hostName", 255L), 
        HOST_IP("hostIP", 15L),
        HOST_ID("hostId"), 
        APPLICATION_NAME("applicationName", 255L), 
        APPLICATION_ID("applicationId"), 
        ACTION("action"){
            @Override
            boolean expectGood(TestType type) {
                switch (type) {
                case NULL:
                    return false;
                default:
                    return super.expectGood(type);
                }
            }
        },
        POLICY_DECISION("policyDecision"){
            @Override
            boolean expectGood(TestType type) {
                switch (type) {
                case NULL:
                    return false;
                default:
                    return super.expectGood(type);
                }
            }
        }, 
        DECISION_REQUEST_ID("decisionRequestId"), 
        TS("ts"), 
        LEVEL("level"),
        CUSTOM_ATTRIBUTE("customAttr"),
        CUSTOM_ATTRIBUTE_TYPE("customAttrType"),
        CUSTOM_ATTRIBUTE_NAME(CUSTOM_ATTRIBUTE, null){
            @Override
            boolean expectGood(TestType type) {
                switch (type) {
                case NULL:
                    return false;
                default:
                    return super.expectGood(type);
                }
            }
        },
        CUSTOM_ATTRIBUTE_VALUE(CUSTOM_ATTRIBUTE, null){
            @Override
            boolean expectGood(TestType type) {
                switch (type) {
                case NULL:
                    return false;
                default:
                    return super.expectGood(type);
                }
            }
        },
        ;

        private final List<String> fieldNames;
        private final Long max;
        
        PolicyActivityLogField(String fieldName){
            this(fieldName, null);
        }
        
        PolicyActivityLogField(String fieldName, Long max){
            fieldNames = Collections.singletonList(fieldName);
            this.max = max;
        }
        
        PolicyActivityLogField(PolicyActivityLogField parent, String fieldName){
            this(parent, fieldName, null);
        }
        
        PolicyActivityLogField(PolicyActivityLogField parent, String fieldName, Long max){
            fieldNames = new LinkedList<String>(parent.fieldNames);
            fieldNames.add(fieldName);
            this.max = max;
        }
        
        
        boolean expectGood(TestType type){
            return true;
         // out of range is not a problem anymore
//            switch (type) {
//            case OUT_OF_RANGE_HIGH:
//                if (max != null) {
//                    return false;
//                } else {
//                    return true;
//                }
//                
//            default:
//                return true;
//            }
        }
        
        Long getMax(){
            return max;
        }
    }

    enum TestType{
        NULL,
        OUT_OF_RANGE_LOW,
        OUT_OF_RANGE_HIGH,
        INVALID,
        BOUNDARY,
    }

    protected PolicyActivityInfo generateGoodPALog(){
      //Make it a bad entry with a null resource name
        FromResourceInformation fromResourceInfo = new FromResourceInformation();
        fromResourceInfo.setCreatedDate(System.currentTimeMillis()-2000);
        fromResourceInfo.setModifiedDate(System.currentTimeMillis() -1000);
        fromResourceInfo.setName("file:\\c:\from resource\name.txt");
        fromResourceInfo.setOwnerId("S-1-5-32-544");
        fromResourceInfo.setSize(1024);
        DynamicAttributes customAttr = new DynamicAttributes(1);
        customAttr.add("keyy", "vvalue");
        PolicyActivityInfo info = new PolicyActivityInfo(
                fromResourceInfo,                   // FromResourceInformation fromResourceInfo, 
                new ToResourceInformation("file:\\d\to resource file.doc"), // ToResourceInformation toResourceInfo, 
                "username@unittest.nextlabs.com",   // String userName, 
                14328948573223L,                    // long userId, 
                "hostname.unittest.nextlabs.com",   // String hostName, 
                "10.0.0.2",                         // String hostIP,
                2132454844L,                        // long hostId, 
                "application name",                 // String applicationName, 
                3311L,                              // long applicationId, 
                ActionEnumType.ACTION_EDIT,         // ActionEnumType action,
                PolicyDecisionEnumType.POLICY_DECISION_DENY, //PolicyDecisionEnumType policyDecision, 
                4L,                                 // long decisionRequestId, 
                System.currentTimeMillis(),         // long ts, 
                2,                                  // int level, 
                customAttr                          // DynamicAttributes customAttr
        );
        return info;
    }
    
    protected void log(boolean expectGood, PolicyActivityInfo... logs){
        log(expectGood, 0, logs);
    }
    
    protected void log(boolean expectGood, int startFromId, PolicyActivityInfo... logs){
        PolicyActivityLogEntry[] entries = new PolicyActivityLogEntry[logs.length];
        for (int i = 0; i < logs.length; i++) {
            entries[i] = new PolicyActivityLogEntry(logs[i], startFromId + i);
        }
        try {
            logWriterToTest.log(entries);
            assertTrue("log should be insert successfully", expectGood);
        } catch (DataSourceException e) {
            assertFalse("log should be failed to insert. " + e, expectGood);
        }
    }
    
    protected IPair<Object, Field> getObjectAndField(Object o, PolicyActivityLogField field) throws SecurityException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field f = null;
        Object obj = o;
        Iterator<String> it = field.fieldNames.iterator();
        while (it.hasNext()) {
            String fieldName = it.next();
            if (fieldName == null) {
                return new Pair<Object, Field>(obj, null);
            }
            f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);

            if (it.hasNext()) {
                obj = f.get(obj);
            }
        }
        return new Pair<Object, Field>(obj, f);
    }
    
    /**
     * may return more than one PolicyActivityInfo such as invalid char maybe more than one test case.
     * @param field
     * @param testType
     * @return
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    protected PolicyActivityInfo[] generateBadPALog(PolicyActivityLogField field, TestType testType)
            throws SecurityException, NoSuchFieldException, IllegalArgumentException,
            IllegalAccessException {
        List<PolicyActivityInfo> logs = new LinkedList<PolicyActivityInfo>();
        
        PolicyActivityInfo log = generateGoodPALog();
        logs.add(log);
        if(field.fieldNames != null){
            
            IPair<Object, Field> objAndField = getObjectAndField(log, field);
            Object obj = objAndField.first();
            Field f = objAndField.second();
            
            if(f != null){
                reflectSetValue(field, testType, obj, f, logs);
            }else{
                // can't do reflection
                
                // so far, the fields that can't do reflection are string
                String[] strings = getBadStringValues(field, testType);
                if (strings != null) {
                    switch(field){
                    case FROM_RESOURCE_INFO_NAME:
                        log.getFromResourceInfo().setName(strings[0]);

                        for (int i = 1; i < strings.length; i++) {
                            PolicyActivityInfo log2 = generateGoodPALog();
                            logs.add(log2);
                            log2.getFromResourceInfo().setName(strings[i]);
                        }
                        break;
                    case TO_RESOURCE_INFO_NAME:
                        log.getToResourceInfo().setName(strings[0]);

                        for (int i = 1; i < strings.length; i++) {
                            PolicyActivityInfo log2 = generateGoodPALog();
                            logs.add(log2);
                            log2.getToResourceInfo().setName(strings[i]);
                        }
                        break;
                    case CUSTOM_ATTRIBUTE_TYPE:
                        for (int i = 0; i < strings.length; i++) {
                            log.getCustomAttr().add(strings[i], "type");
                        }
                        break;    
                    case CUSTOM_ATTRIBUTE_NAME:
                        for (int i = 0; i < strings.length; i++) {
                            log.getCustomAttr().add(strings[i], "value");
                        }
                        break;
                    case CUSTOM_ATTRIBUTE_VALUE:
                        for (int i = 0; i < strings.length; i++) {
                            log.getCustomAttr().add("key" + i, strings[i]);
                        }
                        break;
                    }
                }
                
            }
        }
        return logs.toArray(new PolicyActivityInfo[logs.size()]);
    }

    protected void reflectSetValue(PolicyActivityLogField field, TestType testType, Object obj,
            Field f, List<PolicyActivityInfo> logs) throws IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchFieldException {
        Class<?> type = f.getType();
        if( type.isAssignableFrom(FromResourceInformation.class)){
            switch(testType){
            case NULL:
                f.set(obj, null);
                break;
            }
        } else if (type.isAssignableFrom(ToResourceInformation.class)) {
            switch(testType){
            case NULL:
                f.set(obj, null);
                break;
            }
        } else if (type.isAssignableFrom(String.class)) {
            switch(testType){
            case NULL:
                if(!type.isPrimitive()){
                    f.set(obj, null);
                }
                break;
            case OUT_OF_RANGE_LOW:
                f.set(obj, "");
                break;
            case OUT_OF_RANGE_HIGH:
                Long max = field.getMax();
                if(max != null){
                    // go 1 char more than the limit
                    StringBuilder sb = new StringBuilder(max.intValue() + 1);
                    for (int i = 0; i < max + 1; i++) {
                        sb.append("h");
                    }
                    f.set(obj, sb.toString());
                }
                break;
            case INVALID:
            case BOUNDARY:
                String[] strings = getBadStringValues(field, testType);
                if (strings != null) {
                    f.set(obj, strings[0]);

                    for (int i = 1; i < strings.length; i++) {
                        PolicyActivityInfo log = generateGoodPALog();
                        logs.add(log);
                        IPair<Object, Field> objAndField = getObjectAndField(log, field);
                        obj = objAndField.first();

                        f.set(obj, strings[i]);
                    }
                }
                break;
            }
        } else if (type.isAssignableFrom(Long.class)) {
            switch(testType){
            case NULL:
                f.set(obj, null);
                break;
            case OUT_OF_RANGE_LOW:
                f.set(obj, Long.MIN_VALUE);
                break;
            case OUT_OF_RANGE_HIGH:
                f.set(obj, Long.MAX_VALUE);
                break;
            }
        } else if (type.isAssignableFrom(long.class)) {
            switch(testType){
            case OUT_OF_RANGE_LOW:
                f.set(obj, Long.MIN_VALUE);
                break;
            case OUT_OF_RANGE_HIGH:
                f.set(obj, Long.MAX_VALUE);
                break;
            }
        } else if (type.isAssignableFrom(Integer.class)) {
            switch(testType){
            case NULL:
                if(!type.isPrimitive()){
                    f.set(obj, null);
                }
                break;
            case OUT_OF_RANGE_LOW:
                f.set(obj, Integer.MIN_VALUE);
                break;
            case OUT_OF_RANGE_HIGH:
                f.set(obj, Integer.MAX_VALUE);
                break;
            }
        } else if (type.isAssignableFrom(int.class)) {
            switch(testType){
            case OUT_OF_RANGE_LOW:
                f.set(obj, Integer.MIN_VALUE);
                break;
            case OUT_OF_RANGE_HIGH:
                f.set(obj, Integer.MAX_VALUE);
                break;
            }
        } else if (type.isAssignableFrom(ActionEnumType.class)) {
            switch(testType){
            case NULL:
                f.set(obj, null);
                break;
            }
        } else if (type.isAssignableFrom(PolicyDecisionEnumType.class)) {
            switch(testType){
            case NULL:
                f.set(obj, null);
                break;
            }
        } else if (type.isAssignableFrom(DynamicAttributes.class)) {
            switch(testType){
            case NULL:
                f.set(obj, null);
                break;
            case OUT_OF_RANGE_LOW:
                DynamicAttributes customAttr = (DynamicAttributes)f.get(obj);
                customAttr.clear();
            }
        } else {
            throw new IllegalArgumentException(type.getName());
        }
    }
    
    protected String[] getBadStringValues(PolicyActivityLogField field, TestType testType){
        switch (testType) {
        case NULL:
            return new String[] { null };
        case OUT_OF_RANGE_LOW:
            return new String[] { "" };
        case OUT_OF_RANGE_HIGH:
            Long max = field.getMax();
            if (max != null) {
                //go 1 more than the limit
                StringBuilder sb = new StringBuilder(max.intValue() + 1);
                for (int i = 0; i < max + 1; i++) {
                    sb.append("h");
                }
                return new String[] { sb.toString() };
            }
            return null;
        case INVALID:
            char[] badChars = new char[] { 0x00, 0x7F };

            String[] badStrings = new String[badChars.length];
            for (int i = 0; i < badChars.length; i++) {
                badStrings[i] = Character.toString(badChars[i]);
            }
            return badStrings;
        case BOUNDARY:
            char[] boundaryChars = new char[] { 0x80, ' ' };
            String[] boundaryStrings = new String[boundaryChars.length];
            for (int i = 0; i < boundaryChars.length; i++) {
                boundaryStrings[i] = Character.toString(boundaryChars[i]);
            }
            return boundaryStrings;
        default:
            throw new IllegalArgumentException(testType.name());
        }
    }
    

    public void testGoodPolicyActivityLog() {
        PolicyActivityInfo logInfo = generateGoodPALog();
        log(true, logInfo);
    }
    
//    public void testAllBadPALog() throws Exception {
//        StringBuilder sb = new StringBuilder();

//        int startFromId = 0;
//        for(PolicyActivityLogField field : PolicyActivityLogField.values()){
//            for(TestType testType : TestType.values()){
//                PolicyActivityInfo[] infos = generateBadPALog(field, testType);
//                if(infos != null){
//                    PolicyActivityLogEntry[] entries = new PolicyActivityLogEntry[infos.length];
//                    for (int i = 0; i < infos.length; i++) {
//                        //any policy id should work
//                        entries[i] = new PolicyActivityLogEntry(infos[i], 0, startFromId + i);
//                    }
//                    boolean expectGood = field.expectGood(testType);
//                    try {
//                        logWriterToTest.log(entries);
//                        if (!expectGood) {
//                            sb.append("log should be failed to insert, field=" + field + ", type=" + testType + "\n");
//                        }
//                    } catch (Exception e) {
//                        if (expectGood) {
//                            e.printStackTrace();
//                            sb.append("log should be insert successfully, field=" + field + ", type=" + testType + "\n");
                            
 //                       }
  //                  }
     //               startFromId += infos.length;
       //         }
       //     }
    //    }
        
  //      if (sb.length() > 0) {
   //         fail(sb.toString());
   //     }
 //   }

    /**
     * This test verifies that an exception is thrown if a bad tracking activity
     * log record is inserted
     * @throws DataSourceException 
     */
    public void testTrackingActivityLogFailureException() throws DataSourceException {
        final int nbEntries = 5;
        TrackingLogEntry[] entries = new TrackingLogEntry[5];
        for (int i = 0; i < nbEntries; i++) {
            TrackingLogEntry entry = TrackingLogEntryTestData.generateRandom();
            //Make it a bad entry
            entry.setAction(null);
            entries[i] = entry;
        }
        boolean exThrown = false;
        try {
            logWriterToTest.log(entries);
        } catch (NullPointerException e) {
            exThrown = true;
        }
        assertTrue("An exception should be thrown for invalid log entries", exThrown);
    }
}
