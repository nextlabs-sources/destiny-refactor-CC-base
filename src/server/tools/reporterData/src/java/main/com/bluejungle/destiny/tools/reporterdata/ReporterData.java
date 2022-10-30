/*
 * Created on Jan 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.reporterdata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.expression.Expression;

import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.FromResourceInformationDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.HostDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyAssistantLogDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.PolicyDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TestPolicyActivityLogEntryDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.TestTrackingActivityLogEntryDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.ToResourceInformationDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserDO;
import com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl.UserGroupDO;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.utils.StringUtils;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TestPolicyActivityLogCustomAttributeDO;
import com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl.TestTrackingActivityLogCustomAttributeDO;
import com.nextlabs.destiny.tools.reporterdata.ReporterDataBase;
import com.nextlabs.destiny.tools.reporterdata.UserAbortException;
import com.nextlabs.shared.tools.impl.InteractiveQuestion;

/**
 * @author ryoung
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/bluejungle/destiny/tools/reporterdata/ReporterData.java#1 $
 */

public class ReporterData extends ReporterDataBase {

    public ReporterData() throws IOException {
    	super(false);
	}

	// constants
    private static final String[] pqls = { "RESOURCE \"Files strictly smaller than 1 KB\" = DESCRIPTION \"Files strictly smaller than 1 KB (1000 bytes)\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.size < 1000))",
            "RESOURCE \"1 KB Files\" = DESCRIPTION \"Files exactly 1KB (1000 bytes) in size\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.size = 1000))",
            "RESOURCE \"Files strictly greater than 1 KB\" = DESCRIPTION \"Files strictly greater than 1 KB (1000 bytes)\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.size > 1000))",
            "RESOURCE \"Files greater than 1 KB\" = DESCRIPTION \"Files greater than or equal to 1 KB (1000 bytes)\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.size >= 1000))",
            "RESOURCE \"Files smaller than 1 KB\" = DESCRIPTION \"Files smaller than or equal to 1 KB (1000 bytes)\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.size <= 1000))",
            "RESOURCE \"Not 1 KB files\" = DESCRIPTION \"Not 1 KB files\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.size != 1000))", "RESOURCE \"Text files\" = (((FALSE OR resource.fso.name = \"**.txt\") AND TRUE) AND (TRUE AND TRUE))",
            "RESOURCE \"XT files\" = (((FALSE OR resource.fso.name = \"**.*XT\") AND TRUE) AND (TRUE AND TRUE))",
            "RESOURCE \"Dir1 files\" = DESCRIPTION \"Dir1 files\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.directory = \"**\\\\Dir1\\\\**\"))",
            "RESOURCE \"All DIR files\" = DESCRIPTION \"All DIR files\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.directory = \"**DIR**\"))",
            "RESOURCE \"Chester Arthur files\" = DESCRIPTION \"Chester Arthur files\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.owner = \"chester.arthur@test.bluejungle.com\"))",
            "RESOURCE \"Toastmaster files\" = DESCRIPTION \"Toastmaster files\"(((FALSE OR TRUE) AND TRUE) AND (TRUE AND resource.fso.owner_group = \"TEST.BLUEJUNGLE.COM:Groups:Toastmasters Club\"))",
            "USER \"Chester Arthur\" = (((FALSE OR user.did = 999) AND TRUE) AND (TRUE AND TRUE))", "USER \"ToastM\" = (((FALSE OR user.ldapgroupid = \"1189\") AND TRUE) AND (TRUE AND TRUE))" };

    private static final String ARTHUR_SID = "S-1-5-21-830805687-550985140-3285839444-1160";
    private static final String FORD_SID = "S-1-5-21-830805687-550985140-3285839444-1170";
    private static long policyAttrID = 0;
    private static long trackingAttrID = 0;
    
    private String cArthurDisplayName = "chester.arthur@test.bluejungle.com";
    private String gFordDisplayName = "gerald.ford@test.bluejungle.com";
    private String jGarfieldDisplayName = "james.garfield@test.bluejungle.com";
    private String jCarterDisplayName = "jimmy.carter@test.bluejungle.com";
    private String hostDisplayName = "3mile.test.bluejungle.com";


    public void createDashboardData(final Session s, File dashboardFile) throws MappingException, HibernateException, ParseException, IOException {
    	// clear the Activity Logs
        clearActivityData(s);
        
        // create the dummy policies
//        Long[] policyIds = insertDummyPolicies(s);
//        final Long p1Id = policyIds[0];
//        final Long p7Id = policyIds[1];
//        final Long p8Id = policyIds[2];
//        final Long p9Id = policyIds[3];
//        final Long p10Id = policyIds[4];
//        final Long p11Id = policyIds[5];
//        final Long p12Id = policyIds[6];
//        final Long p13Id = policyIds[7];
//        final Long p14Id = policyIds[8];
//        final Long p15Id = policyIds[9];
//        final Long p16Id = policyIds[10];
//        final Long p17Id = policyIds[11];
//        final Long p18Id = policyIds[12];
//        final Long p19Id = policyIds[13];
//        final Long p20Id = policyIds[14];
//        final Long p21Id = policyIds[15];
//        final Long p22Id = policyIds[16];

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(dashboardFile)));
        String data;
        
        Transaction t = s.beginTransaction();
        long logID = 0;
        while ((data = bufferedReader.readLine()) != null){
            StringTokenizer tokens = new StringTokenizer(data, ";");
            String token = tokens.nextToken();
            if (token.equals("Policy")){
                // Interval
                token = tokens.nextToken(";");
                String interval = token;
                
                // # of records
                token = tokens.nextToken(";");
                int numRecords = Integer.parseInt(token);
               
                // Action
                token = tokens.nextToken(";");
                ActionEnumType action = ActionEnumType.getActionEnum(token);   
                
                // Time 
//                token = tokens.nextToken();
//                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//                Date date = sdf.parse(token);
//                Calendar time = Calendar.getInstance();
//                time.setTime(date);
                
                // Policy 
                token = tokens.nextToken(";");
                PolicyDO policy = (PolicyDO)s.createCriteria(PolicyDO.class).add(Expression.eq("fullName", token)).uniqueResult();
                if (policy == null){
                    final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
                    Transaction t1 = s.beginTransaction();
                    policy = new PolicyDO();
                    policy.setFullName(token);
                    if (maxId == null) {
                        policy.setId(new Long(1));
                    } else {
                        //Give the policy a unique id number
                        policy.setId(new Long(maxId.longValue() + 1));
                    }
                    s.save(policy);
                    t1.commit();
                }
                Long policyId = policy.getId();
                
                // Enforcement
                token = tokens.nextToken(";");
                PolicyDecisionEnumType enforcement;
                if (token.equals("Allow")){
                    enforcement = PolicyDecisionEnumType.POLICY_DECISION_ALLOW;
                } else {
                    enforcement = PolicyDecisionEnumType.POLICY_DECISION_DENY;
                }
                
                // User
                token = tokens.nextToken(";");
                UserDO user = (UserDO) s.createCriteria(UserDO.class).add(Expression.eq("displayName", token)).uniqueResult();
                
                // Log Level
                token = tokens.nextToken(";");
                int level = Integer.parseInt(token);
                
                // From Resource Name
                token = tokens.nextToken(";");
                String fromResource = token;
                
                // To Resource Name 
                token = tokens.nextToken(";");
                String toResource = token;
                
                // Host Name
                token = tokens.nextToken(";");
                String hostName = token;
                HostDO host = (HostDO) s.createCriteria(HostDO.class).add(Expression.eq("name", hostName)).uniqueResult();
                
                // Host IP
                token = tokens.nextToken(";");
                String hostIP = token;
                
                // Application 
                token = tokens.nextToken(";");
                String application = token;
                
                // Attributes
                String name = null, ownerId = null;
                long size = 0;
                Calendar createdDate = null, modifiedDate = null;
                LinkedHashMap<String, String> customAttributes = new LinkedHashMap<String, String>();
                while (tokens.hasMoreTokens()){
                    token = tokens.nextToken(";");
                    if (token.startsWith("Name")){
                        name = token.substring(5);
                    } else if (token.startsWith("Size")){
                        if (token.endsWith("MB")){
                            size = Long.parseLong(token.substring(5,token.indexOf(' ')))*1024*1024;
                        } else  {
                            size = Long.parseLong(token.substring(5,token.indexOf(' ')))*1024;
                        }
                    } else if (token.startsWith("Created Date")){
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = sdf.parse(token.substring(13));
                        Calendar time = Calendar.getInstance();
                        time.setTime(date);
                        createdDate = time;
                    } else if (token.startsWith("Modified Date")){
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = sdf.parse(token.substring(14));
                        Calendar time = Calendar.getInstance();
                        time.setTime(date);
                        modifiedDate = time;
                    } else if (token.startsWith("Owner ID")){
                        ownerId = token.substring(9);
                    } else {
                        customAttributes.put(token.substring(0, token.indexOf("=")), token.substring(token.indexOf("=")+1)); 
                    }
                }
                
                if (interval.equals("Weekly")){
                    while (numRecords > 0){
                        for (int i = 0; i < 7; i++){
                            Calendar time = Calendar.getInstance();
                            time.add(Calendar.DAY_OF_MONTH, -1*i);
                            insertSinglePolicyActivityLogData(s, user, null, host, null, null, 
                                    application, logID, action, time, policyId, enforcement, 
                                    fromResource, size, ownerId, createdDate, modifiedDate, 
                                    toResource, level, customAttributes);
                            i++;
                            numRecords--;
                            logID++;
                            if (numRecords <= 0){
                                break;
                            }
                            if (i == 7 && numRecords > 0){
                                i = 0;
                            }
                        }
                    }
                } else {
                    while (numRecords > 0){
                        for (int i = 0; i < 30; i++){
                            Calendar time = Calendar.getInstance();
                            time.add(Calendar.DAY_OF_MONTH, -1*i);
                            insertSinglePolicyActivityLogData(s, user, null, host, null, null, 
                                    application, logID, action, time, policyId, enforcement, 
                                    fromResource, size, ownerId, createdDate, modifiedDate, 
                                    toResource, level, customAttributes);
                            i++;
                            numRecords--;
                            logID++;
                            if (numRecords <= 0){
                                break;
                            }
                            if (i == 29 && numRecords > 0){
                                i = 0;
                            }
                        }
                    }
                }
            } else {

            }
            t.commit();
        }
    }

    /**
     * Creates hard-coded data to enter into tables. Configures the hibernate
     * session.
     * 
     * @throws Exception
     */
    public void createActivityData(final Session s) throws HibernateException, MappingException, MissingUserException {
        // query database, find Chester Arthur and Gerald Ford
        UserDO cArthur = (UserDO) s.createCriteria(UserDO.class).add(Expression.eq("displayName", cArthurDisplayName)).uniqueResult();
        UserDO gFord = (UserDO) s.createCriteria(UserDO.class).add(Expression.eq("displayName", gFordDisplayName)).uniqueResult();
        UserDO jGarfield = (UserDO) s.createCriteria(UserDO.class).add(Expression.eq("displayName", jGarfieldDisplayName)).uniqueResult();
        UserDO jCarter = (UserDO) s.createCriteria(UserDO.class).add(Expression.eq("displayName", jCarterDisplayName)).uniqueResult();
        UserDO blankUser = new UserDO();
        blankUser.setOriginalId(new Long(-1));
        
        if (cArthur == null) // if you couldn't find them, fail
        {
            throw new MissingUserException("UserDO not found: " + cArthurDisplayName);
        }
        if (gFord == null) // if you couldn't find them, fail
        {
            throw new MissingUserException("UserDO not found: " + gFordDisplayName);
        }

        Long[] policyIds = insertDummyPolicies(s);
        final Long p1Id = policyIds[0];
        final Long p7Id = policyIds[1];
        final Long p8Id = policyIds[2];
        final Long p9Id = policyIds[3];
        final Long p10Id = policyIds[4];
        final Long p11Id = policyIds[5];
        final Long p12Id = policyIds[6];
        final Long p13Id = policyIds[7];
        final Long p14Id = policyIds[8];
        final Long p15Id = policyIds[9];
        final Long p16Id = policyIds[10];
        final Long p17Id = policyIds[11];
        final Long p18Id = policyIds[12];
        final Long p19Id = policyIds[13];
        final Long p20Id = policyIds[14];
        final Long p21Id = policyIds[15];
        final Long p22Id = policyIds[16];

        // query database, find host SVALBARD
        HostDO host1 = (HostDO) s.createCriteria(HostDO.class).add(Expression.eq("name", hostDisplayName)).uniqueResult();
        if (host1 == null) // if it doesn't exist, create it
//        HostDO host1 = new HostDO();
        {
            Long maxId = (Long) s.createQuery("select max(h.id) from HostDO h").uniqueResult();
            Transaction t = s.beginTransaction();
            host1 = new HostDO();
            host1.setName("SVALBARD.bluejungle.com");
            host1.setId(maxId);
//            host1.setOriginalId(new Long(-1));
            if (maxId == null) {
                host1.setId(new Long(1));
            } else {
                host1.setId(new Long(maxId.longValue() + 1));
            }
            s.save(host1);
            t.commit();
        }

        // clear the Activity Logs
        clearActivityData(s);
        
        Transaction t = s.beginTransaction();
        
        // some hard-coded strings
        String qavm06HostName = "qavm06.test.bluejungle.com";
        String qavm06IPAddress = "10.187.6.11";
        String capeHostName = "cape.test.bluejungle.com";
        String cape06IPAddress = "10.187.6.11";
        String notepadAppName = "c:\\windows\\system32\\notepad.exe";
        String explorerAppName = "c:\\windows\\explorer.exe";
        String ieAppName = "c:\\program files\\internet explorer\\iexplore.exe";
        String wordAppName = "c:\\program files\\microsoft office\\office11\\winword.exe";
        String powerpointAppName = "c:\\program files\\microsoft office\\office11\\powerpnt.exe";
        String excelAppName = "c:\\program files\\microsoft office\\office11\\excel.exe";
        String outlookAppName = "c:\\program files\\microsoft office\\office11\\outlook.exe";
        String cArthurFQDN = "chester.arthur@test.bluejungle.com";
        
        // BEGIN HARD-CODED DATA CREATION
        try {
            int i = 0;

            // row 1 
            i++;
            Calendar time = getTimestamp(2006, Calendar.DECEMBER, 1);
            Calendar createDate = getTimestamp(2007, Calendar.JANUARY, 5);
            Calendar modifiedDate = getTimestamp(2007, Calendar.JANUARY, 5);
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress,
                                              notepadAppName, new Long(i), ActionEnumType.ACTION_CHANGE_ATTRIBUTES, 
                                              time, p1Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file:///c:/dir1/resource1.txt", new Long(1331200), ARTHUR_SID, 
                                              createDate, modifiedDate, null, 3, null);
            insertSingleTrackingActivityLogData(s, cArthur, "chester.arthur@TEST", host1, qavm06HostName, qavm06IPAddress, 
                                                notepadAppName, new Long(i), ActionEnumType.ACTION_CHANGE_ATTRIBUTES, 
                                                time, "file:///c:/dir1/resource1.txt", new Long(1331200), ARTHUR_SID, 
                                                createDate, modifiedDate, null, 3, null);
            
            // row 2 
            i++;
            time = getTimestamp(2006, Calendar.DECEMBER, 3);
            createDate = getTimestamp(2005, Calendar.MARCH, 1);
            modifiedDate = getTimestamp(2007, Calendar.MAY, 1);
            insertSinglePolicyActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_CHANGE_SECURITY, 
                                              time, p7Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file://fs1/share1/dir2/fsresource.doc", new Long(1536000), ARTHUR_SID, 
                                              createDate, modifiedDate, null, 1, null);
            insertSingleTrackingActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_CHANGE_SECURITY, 
                                                time, "file://fs1/share1/dir1/fsresource.doc", new Long(1536000), ARTHUR_SID, 
                                                createDate, modifiedDate, null, 1, null);
            
            // row 3 
            i++;
            time = getTimestamp(2008, Calendar.DECEMBER, 4);
            createDate = getTimestamp(2006, Calendar.JANUARY, 6);
            modifiedDate = getTimestamp(2007, Calendar.JANUARY, 5);
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, null, cape06IPAddress, 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_COPY, 
                                              time, p8Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file:///c:/dir2/resource2.doc", new Long(512000), FORD_SID, 
                                              createDate, modifiedDate, "file:///c:/dir2/Copy of resource2.doc", 2, null);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, null, 
                                                wordAppName, new Long(i), ActionEnumType.ACTION_COPY, 
                                                time, "file:///c:/dir2/resource2.doc", new Long(512000), FORD_SID, 
                                                createDate, modifiedDate, "file:///c:/dir2/Copy of resource2.doc", 2, null);
            
            // row 4
            i++;
            time = getTimestamp(2008, Calendar.DECEMBER, 5);
            createDate = getTimestamp(2005, Calendar.DECEMBER, 5);
            modifiedDate = getTimestamp(2007, Calendar.JUNE, 3);
            insertSinglePolicyActivityLogData(s, blankUser, "Administrator@CAPE", host1, null, cape06IPAddress, 
                                              powerpointAppName, new Long(i), ActionEnumType.ACTION_EDIT, 
                                              time, p9Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file://fs1/share1/dir3/fsresource-2.ppt", new Long(2355200), FORD_SID, 
                                              createDate, modifiedDate, null, 3, null);
            insertSingleTrackingActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                                powerpointAppName, new Long(i), ActionEnumType.ACTION_EDIT, 
                                                time, "file://fs1/share1/dir3/fsresource-2.ppt", new Long(2355200), FORD_SID, 
                                                createDate, modifiedDate, null, 3, null);
            
            // row 5
            i++;
            time = getTimestamp(2008, Calendar.DECEMBER, 15);
            createDate = getTimestamp(2004, Calendar.NOVEMBER, 3);
            modifiedDate = getTimestamp(2007, Calendar.JANUARY, 5);
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_CHANGE_SECURITY, 
                                              time, p10Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file:///c:/dir1/resource with space.xls", new Long(3379200), FORD_SID, 
                                              createDate, modifiedDate, null, 1, null);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_CHANGE_SECURITY, 
                                                time, "file:///c:/dir1/resource with space.xls", new Long(3379200), FORD_SID, 
                                                createDate, modifiedDate, null, 1, null);
            
            // row 6 
            i++;
            time = getTimestamp(2008, Calendar.DECEMBER, 25);
            createDate = getTimestamp(2003, Calendar.APRIL, 2);
            modifiedDate = getTimestamp(2007, Calendar.SEPTEMBER, 14);
            insertSinglePolicyActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_DELETE, 
                                              time, p1Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file://fs1/share1/dir1/fsresource$with$dollar.rtf", new Long(1638400), FORD_SID, 
                                              createDate, modifiedDate, null, 2, null);
            insertSingleTrackingActivityLogData(s, blankUser, "Administrator@QAVM06", host1, null, cape06IPAddress,
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_DELETE, 
                                                time, "file://fs1/share1/dir1/fsresource$with$dollar.rtf", new Long(1638400), FORD_SID, 
                                                createDate, modifiedDate, null, 2, null);
            
            // row 7 
            i++;
            time = getTimestamp(2007, Calendar.JANUARY, 15);
            createDate = getTimestamp(2007, Calendar.MAY, 1);
            modifiedDate = getTimestamp(2007, Calendar.MAY, 1);
            insertSinglePolicyActivityLogData(s, cArthur, "chester.arthur@TEST", host1, qavm06HostName, qavm06IPAddress, 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_COPY, 
                                              time, p11Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file:///c:/dir2/ReSoURce1.txt", new Long(1331200), ARTHUR_SID, 
                                              createDate, modifiedDate, "//fs2/share/target.txt", 3, null);
            insertSingleTrackingActivityLogData(s, cArthur, "chester.arthur@TEST", host1, qavm06HostName, qavm06IPAddress, 
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_COPY, 
                                                time, "file:///c:/dir2/ReSoURce1.txt", new Long(1331200), ARTHUR_SID, 
                                                createDate, modifiedDate, "//fs2/share/target.txt", 3, null);
            
            // row 8 
            i++;
            time = getTimestamp(2007, Calendar.JANUARY, 10);
            createDate = getTimestamp(2006, Calendar.AUGUST, 2);
            modifiedDate = getTimestamp(2007, Calendar.NOVEMBER, 29);
            insertSinglePolicyActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_MOVE, 
                                              time, p11Id, PolicyDecisionEnumType.POLICY_DECISION_DENY,
                                              "file://fs1/share1/long dir with space/another directory/loooooooooooong/fsresource with a very long paaaaaath.ppt", new Long(2048000), FORD_SID, 
                                              createDate, modifiedDate, "file:///[removablemedia]?", 1, null);
            insertSingleTrackingActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_MOVE, 
                                                time, "file://K/share1/long dir with space/another directory/loooooooooooong/fsresource with a very long paaaaaath.ppt", new Long(2048000), FORD_SID,
                                                createDate, modifiedDate, "file:///[removablemedia]?", 1, null);
            
            // row 9 
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 28);
            createDate = getTimestamp(2006, Calendar.JUNE, 6);
            modifiedDate = getTimestamp(2007, Calendar.MARCH, 25);
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, qavm06HostName, null, 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                              time, p11Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file:///c:/dir1/!@#$%^&*().txt", new Long(1126400), FORD_SID, 
                                              createDate, modifiedDate, null, 2, null);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                                time, "file:///c:/dir1/!@#$%^&*().txt", new Long(1126400), FORD_SID, 
                                                createDate, modifiedDate, null, 2, null);
            
            // row 10 
            i++;
            time = getTimestamp(2009, Calendar.FEBRUARY, 2);
            createDate = getTimestamp(2007, Calendar.MARCH, 1);
            modifiedDate = getTimestamp(2007, Calendar.MAY, 1);
            insertSinglePolicyActivityLogData(s, gFord, "gerald.ford@QAVM06", host1, capeHostName, cape06IPAddress, 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_PRINT, 
                                              time, p11Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file://fs1/share1/dir1/fsresouRCe.doc", new Long(1536000), ARTHUR_SID, 
                                              createDate, modifiedDate, null, 3, null);
            insertSingleTrackingActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_PRINT, 
                                                time, "file://fs1/share1/dir1/fsresouRCe.doc", new Long(1536000), ARTHUR_SID, 
                                                createDate, modifiedDate, null, 3, null);
            
            // row 11 
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 2);
            createDate = getTimestamp(2006, Calendar.JANUARY, 6);
            modifiedDate = getTimestamp(2007, Calendar.JANUARY, 5);
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_RENAME, 
                                              time, p20Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file:///[mydesktop]/resOURce2.doc", new Long(512000), ARTHUR_SID, 
                                              createDate, modifiedDate, "file:///[mydesktop]/renamed.doc", 3, null);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_RENAME, 
                                                time, "file:///[mydesktop]/resOURce2.doc", new Long(512000), ARTHUR_SID, 
                                                createDate, modifiedDate, "file:///[mydesktop]/renamed.doc", 1, null);
            
            // row 12
            i++;
            time = getTimestamp(2009, Calendar.FEBRUARY, 24);
            createDate = getTimestamp(2007, Calendar.JULY, 15);
            modifiedDate = getTimestamp(2007, Calendar.AUGUST, 15);
            insertSinglePolicyActivityLogData(s, gFord, null, host1, null, qavm06IPAddress, 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_SEND_EMAIL, 
                                              time, p21Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file:///c:/folder2/abcde.doc", new Long(1024000), ARTHUR_SID, 
                                              createDate, modifiedDate, null, 3, null);
            insertSingleTrackingActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                                wordAppName, new Long(i), ActionEnumType.ACTION_SEND_EMAIL, 
                                                time, "file:///c:/folder2/abcde.doc", new Long(1024000), ARTHUR_SID, 
                                                createDate, modifiedDate, null, 2, null);
            
            // row 13 
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 2);
            createDate = getTimestamp(2005, Calendar.JANUARY, 1);
            modifiedDate = getTimestamp(2005, Calendar.JANUARY, 1);
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_SEND_IM, 
                                              time, p22Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file:///[mydocuments]/resource10.txt", new Long(1331200), FORD_SID, 
                                              createDate, modifiedDate, null, 3, null);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_SEND_IM, 
                                                time, "file:///[mydocuments]/resource10.txt", new Long(1331200), FORD_SID, 
                                                createDate, modifiedDate, null, 3, null);
            
            // row 14 
            i++;
            time = getTimestamp(2009, Calendar.FEBRUARY, 28);
            LinkedHashMap<String, String> customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "Requirements for Reporter");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/Report_prd.doc");
            customAttributes.put("Created By", "sharepoint2007\\Administrator");
            customAttributes.put("Modified By", "sharepoint2007\\Administrator");
            customAttributes.put("Date Created", "12-25-2007");
            customAttributes.put("Date Modified", "01-06-2005");
            customAttributes.put("Format", "doc");
            customAttributes.put("Keywords", "REPPRD");
            customAttributes.put("Publisher", "James Garfield");
            customAttributes.put("Revision", "1");
            customAttributes.put("Source", "sourcedoc");
            customAttributes.put("Status", "Reviewed");
            customAttributes.put("Subject", "Reporter");
            customAttributes.put("Version", "1.0");
            customAttributes.put("Custom", " ");
            insertSinglePolicyActivityLogData(s, jGarfield, null, host1, qavm06HostName, qavm06IPAddress, 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_EXPORT, 
                                              time, p12Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/Report_prd.doc", null, FORD_SID, 
                                              null, null, null, 3, customAttributes);
            insertSingleTrackingActivityLogData(s, jGarfield, null, host1, qavm06HostName, qavm06IPAddress, 
                                                wordAppName, new Long(i), ActionEnumType.ACTION_EXPORT, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/Report_prd.doc", null, FORD_SID, 
                                                null, null, null, 3, customAttributes);
            
            // row 15 
            i++;
            time = getTimestamp(2007, Calendar.FEBRUARY, 14);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "Requirements document");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/prd1.xls");
            customAttributes.put("Created By", "sharepoint2007\\Administrator");
            customAttributes.put("Modified By", "sharepoint2007\\Administrator");
            customAttributes.put("Date Created", "11-05-2007");
            customAttributes.put("Date Modified", "04-09-2007");
            customAttributes.put("Format", "xls");
            customAttributes.put("Keywords", "REQ");
            customAttributes.put("Publisher", "Jimmy Carter");
            customAttributes.put("Revision", "R5");
            customAttributes.put("Source", "sourcedoc1");
            customAttributes.put("Status", "Scheduled");
            customAttributes.put("Subject", "Platform Requirements");
            customAttributes.put("Version", "1.1A");
            customAttributes.put("Custom", " ");
            insertSinglePolicyActivityLogData(s, jCarter, null, host1, qavm06HostName, qavm06IPAddress, 
                                              excelAppName, new Long(i), ActionEnumType.ACTION_ATTACH, 
                                              time, p13Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/prd1.xls", null, FORD_SID, 
                                              null, null, null, 3, customAttributes);
            insertSingleTrackingActivityLogData(s, jCarter, null, host1, qavm06HostName, qavm06IPAddress, 
                                                excelAppName, new Long(i), ActionEnumType.ACTION_ATTACH, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/prd1.xls", null, FORD_SID, 
                                                null, null, null, 3, customAttributes);     
            
            // row 16 
            i++;
            time = getTimestamp(2009, Calendar.FEBRUARY, 1);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "Requirements for Policy Author");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/PA_prd.doc");
            customAttributes.put("Created By", "sharepoint2007\\Administrator");
            customAttributes.put("Modified By", "sharepoint2007\\Administrator");
            customAttributes.put("Date Created", "07-15-2007");
            customAttributes.put("Date Modified", "02-15-2007");
            customAttributes.put("Format", "doc");
            customAttributes.put("Keywords", "PAPRD");
            customAttributes.put("Publisher", "James Garfield");
            customAttributes.put("Revision", "2.1");
            customAttributes.put("Source", "sourcedoc2");
            customAttributes.put("Status", "Published");
            customAttributes.put("Subject", "Policy Author");
            customAttributes.put("Version", "V0.7");
            customAttributes.put("Custom", " ");
            insertSinglePolicyActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.12", 
                                              excelAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                              time, p14Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/PA_prd.doc", null, FORD_SID, 
                                              null, null, null, 3, customAttributes);
            insertSingleTrackingActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.12", 
                                                excelAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/PA_prd.doc", null, FORD_SID, 
                                                null, null, null, 3, customAttributes);  
            
            // row 17 
            i++;
            time = getTimestamp(2009, Calendar.MARCH, 1);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "Requirements for Policy Author");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/PA_prd.doc");
            customAttributes.put("Created By", "sharepoint2007\\Administrator");
            customAttributes.put("Modified By", "sharepoint2007\\Administrator");
            customAttributes.put("Date Created", "07-15-2007");
            customAttributes.put("Date Modified", "02-15-2007");
            customAttributes.put("Format", "doc");
            customAttributes.put("Keywords", "PAPRD");
            customAttributes.put("Publisher", "James Garfield");
            customAttributes.put("Revision", "2.1");
            customAttributes.put("Source", "sourcedoc2");
            customAttributes.put("Status", "Final");
            customAttributes.put("Subject", "Policy Author");
            customAttributes.put("Version", "V0.7");
            customAttributes.put("Custom", " ");
            insertSinglePolicyActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.13", 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_COPY, 
                                              time, p15Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/PA_prd.doc", null, FORD_SID, 
                                              null, null, "SharePoint://sharepoint2007:80/sharepoint2007/ReporterSite/Requirements library/copy of PA_prd.doc", 3, customAttributes);   
            insertSingleTrackingActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.13", 
                                                explorerAppName, new Long(i), ActionEnumType.ACTION_COPY, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library/PA_prd.doc", null, FORD_SID, 
                                                null, null, "SharePoint://sharepoint2007:80/sharepoint2007/ReporterSite/Requirements library/copy of PA_prd.doc", 3, customAttributes);            
            
            // row 18 
            i++;
            time = getTimestamp(2009, Calendar.MARCH, 2);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Name", "Requirements library_Sharepoint");
            customAttributes.put("Description", "Library for Sharepoint 2007");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library_Sharepoint");
            customAttributes.put("Type", " ");
            customAttributes.put("SubType", " ");
            insertSinglePolicyActivityLogData(s, jCarter, null, host1, qavm06HostName, qavm06IPAddress, 
                                              excelAppName, new Long(i), ActionEnumType.ACTION_EDIT, 
                                              time, p16Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library_Sharepoint", null, FORD_SID, 
                                              null, null, null, 3, customAttributes);   
            insertSingleTrackingActivityLogData(s, jCarter, null, host1, qavm06HostName, qavm06IPAddress, 
                                                excelAppName, new Long(i), ActionEnumType.ACTION_EDIT, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library_Sharepoint", null, FORD_SID, 
                                                null, null, null, 3, customAttributes);            
            
            // row 19 
            i++;
            time = getTimestamp(2007, Calendar.MARCH, 15);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "Reporter Site");
            customAttributes.put("Description", "Reporter Site");
            customAttributes.put("Resource Signature", "http://sharepoint2007.test.bluejungle.com/ReporterSite");
            insertSinglePolicyActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.13", 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                              time, p17Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com:80/sharepoint2007/ReporterSite", null, FORD_SID, 
                                              null, null, null, 3, customAttributes); 
            insertSingleTrackingActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.13", 
                                                wordAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com:80/sharepoint2007/ReporterSite", null, FORD_SID, 
                                                null, null, null, 3, customAttributes);            
            
            // row 20 
            i++;
            time = getTimestamp(2009, Calendar.APRIL, 19);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "calendar of events");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar");
            customAttributes.put("Created By", "sharepoint2007\\Administrator");
            customAttributes.put("Modified By", "sharepoint2007\\Administrator");
            customAttributes.put("Custom", " ");
            insertSinglePolicyActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.13", 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_MOVE, 
                                              time, p18Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar", null, FORD_SID, 
                                              null, null, "SharePoint://sharepoint2007:80/sharepoint2007/alpha", 3, customAttributes); 
            insertSingleTrackingActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.13", 
                                                wordAppName, new Long(i), ActionEnumType.ACTION_MOVE, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar", null, FORD_SID, 
                                                null, null, "SharePoint://sharepoint2007:80/sharepoint2007/alpha", 3, customAttributes); 
            
            // row 21 
            i++;
            time = getTimestamp(2008, Calendar.DECEMBER, 21);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "Reporter meeting");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar/meeting");
            customAttributes.put("Created By", "sharepoint2007\\Administrator");
            customAttributes.put("Modified By", "sharepoint2007\\Administrator");
            customAttributes.put("Custom", " ");
            insertSinglePolicyActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.13", 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_DELETE, 
                                              time, p19Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar/meeting", null, FORD_SID, 
                                              null, null, null, 3, customAttributes); 
            insertSingleTrackingActivityLogData(s, jGarfield, null, host1, qavm06HostName, "10.187.6.13", 
                                                wordAppName, new Long(i), ActionEnumType.ACTION_DELETE, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar/meeting", null, FORD_SID, 
                                                null, null, null, 3, customAttributes); 

            // row 22
            i++;
            time = getTimestamp(2009, Calendar.MARCH, 2);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Name", "Requirements library_Sharepoint");
            customAttributes.put("Description", "Library for Sharepoint 2007");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library_Sharepoint");
            customAttributes.put("Type", " ");
            customAttributes.put("SubType", " ");
            insertSinglePolicyActivityLogData(s, blankUser, "SharePoint2007\\Administrator", host1, qavm06HostName, "10.187.6.11", 
                                              excelAppName, new Long(i), ActionEnumType.ACTION_EDIT, 
                                              time, p16Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library_Sharepoint", null, null, 
                                              null, null, null, 2, customAttributes); 
            insertSingleTrackingActivityLogData(s, blankUser, "SharePoint2007\\Administrator", host1, qavm06HostName, "10.187.6.11", 
                                                excelAppName, new Long(i), ActionEnumType.ACTION_EDIT, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/Requirements library_Sharepoint", null, null, 
                                                null, null, null, 2, customAttributes); 

            // row 23
            i++;
            time = getTimestamp(2007, Calendar.MARCH, 15);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "Reporter Site");
            customAttributes.put("Description", "Reporter Site");
            customAttributes.put("Resource Signature", "http://sharepoint2007.test.bluejungle.com/ReporterSite");
            insertSinglePolicyActivityLogData(s, jCarter, "test\\jimmy.carter", host1, qavm06HostName, "10.187.6.13", 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                              time, p17Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com:80/sharepoint2007/ReporterSite", null, null, 
                                              null, null, null, 1, customAttributes); 
            insertSingleTrackingActivityLogData(s, jCarter, "test\\jimmy.carter", host1, qavm06HostName, "10.187.6.13", 
                                                wordAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com:80/sharepoint2007/ReporterSite", null, null, 
                                                null, null, null, 1, customAttributes); 
            
            // row 24
            i++;
            time = getTimestamp(2009, Calendar.APRIL, 19);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "calendar of events");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar");
            customAttributes.put("Created by", "sharepont2007\\Administrator");
            customAttributes.put("Modified by", "sharepont2007\\Administrator");
            customAttributes.put("Custom", "~");
            insertSinglePolicyActivityLogData(s, jCarter, "qavm06\\jimmy.carter", host1, qavm06HostName, "10.187.6.13", 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_MOVE, 
                                              time, p18Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar", null, null, 
                                              null, null, "SharePoint://sharepoint2007:80/sharepoint2007/alpha", 1, customAttributes); 
            insertSingleTrackingActivityLogData(s, jCarter, "qavm06\\jimmy.carter", host1, qavm06HostName, "10.187.6.13", 
                                                wordAppName, new Long(i), ActionEnumType.ACTION_MOVE, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar", null, null, 
                                                null, null, "SharePoint://sharepoint2007:80/sharepoint2007/alpha", 1, customAttributes); 
            
            // row 25
            i++;
            time = getTimestamp(2008, Calendar.DECEMBER, 21);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Title", "Reporter meeting");
            customAttributes.put("Resource Signature", "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar/meeting");
            customAttributes.put("Created by", "sharepoint2007\\Administrator");
            customAttributes.put("Modified by", "sharepont2007\\Administrator");
            customAttributes.put("Custom", "~");
            insertSinglePolicyActivityLogData(s, blankUser, "SharePoint2007\\system", host1, qavm06HostName, "10.187.6.13", 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_DELETE, 
                                              time, p19Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar/meeting", null, null, 
                                              null, null, null, 2, customAttributes); 
            insertSingleTrackingActivityLogData(s, blankUser, "SharePoint2007\\system", host1, qavm06HostName, "10.187.6.13", 
                                                wordAppName, new Long(i), ActionEnumType.ACTION_DELETE, 
                                                time, "SharePoint://sharepoint2007.test.bluejungle.com/ReporterSite/calendar/meeting", null, null, 
                                                null, null, null, 2, customAttributes); 
            
            // row 26
            i++;
            time = getTimestamp(2006, Calendar.DECEMBER, 1);
            createDate = getTimestamp(2007, Calendar.JANUARY, 5);
            modifiedDate = getTimestamp(2007, Calendar.JANUARY, 5);
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, qavm06HostName, "10.187.6.11", 
                                              notepadAppName, new Long(i), ActionEnumType.ACTION_RUN, 
                                              time, p1Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file:///c:/windows/system32/notepad.exe", new Long(67000), "S-1-5-32-544", 
                                              createDate, modifiedDate, null, 3, null);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                notepadAppName, new Long(i), ActionEnumType.ACTION_RUN, 
                                                time, "file:///c:/dir1/resource1.txt", new Long(67000), "S-1-5-32-544", 
                                                createDate, modifiedDate, null, 3, null);
            
            // row 27
            i++;
            time = getTimestamp(2006, Calendar.DECEMBER, 1);
            createDate = getTimestamp(2007, Calendar.JANUARY, 5);
            modifiedDate = getTimestamp(2007, Calendar.JANUARY, 5);
            insertSinglePolicyActivityLogData(s, jCarter, null, host1, qavm06HostName, "10.187.6.11", 
                                              wordAppName, new Long(i), ActionEnumType.ACTION_RUN, 
                                              time, p1Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file:///c:/program files/microsoft office/office11/winword.exe", new Long(1331200), "S-1-5-32-544", 
                                              createDate, modifiedDate, null, 3, null);
            
            
            
            // row 28
            i++;
            time = getTimestamp(2008, Calendar.DECEMBER, 5);
            createDate = getTimestamp(2005, Calendar.DECEMBER, 5);
            modifiedDate = getTimestamp(2007, Calendar.JUNE, 3);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Sent To", "john.smith@hotmail.com, jimmy carter (1977-81), vvalsaraj");
            customAttributes.put("Obligations", "Remove hidden data");
            insertSinglePolicyActivityLogData(s, jCarter, null, host1, null, "10.187.6.11", 
                                              powerpointAppName, new Long(i), ActionEnumType.ACTION_SEND_EMAIL, 
                                              time, p9Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file://fs1/share1/dir3/fsresource-2.ppt", new Long(2662400), "S-1-5-21-830805687-550985140-3285839444-1170", 
                                              createDate, modifiedDate, null, 3, customAttributes);
            insertSingleTrackingActivityLogData(s, jCarter, null, host1, null, "10.187.6.11", 
                                                outlookAppName, new Long(i), ActionEnumType.ACTION_SEND_EMAIL, 
                                                time, "file://fs1/share1/dir3/fsresource-2.ppt", new Long(2662400), 
                                                "S-1-5-21-830805687-550985140-3285839444-1170", 
                                                createDate, modifiedDate, null, 3, customAttributes);
            
            
            // row 29
            i++;
            time = getTimestamp(2008, Calendar.DECEMBER, 5);
            createDate = getTimestamp(2007, Calendar.MARCH, 1);
            modifiedDate = getTimestamp(2007, Calendar.MAY, 1);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Sent To", "john.smith@hotmail.com");
            customAttributes.put("Obligations", "Append");
            insertSinglePolicyActivityLogData(s, gFord, "gerald.ford@QAVM06", host1, capeHostName, "10.187.6.11", 
                                              explorerAppName, new Long(i), ActionEnumType.ACTION_SEND_EMAIL, 
                                              time, p11Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file://fs1/share1/dir1/fsresouRCe.doc", new Long(1331200), "S-1-5-21-830805687-550985140-3285839444-1160", 
                                              createDate, modifiedDate, null, 3, customAttributes);
            insertSingleTrackingActivityLogData(s, gFord, "gerald.ford@QAVM06", host1, capeHostName, "10.187.6.11", 
                                                outlookAppName, new Long(i), ActionEnumType.ACTION_SEND_EMAIL, 
                                                time, "file://fs1/share1/dir1/fsresouRCe.doc", new Long(1331200), 
                                                "S-1-5-21-830805687-550985140-3285839444-1160", 
                                                createDate, modifiedDate, null, 3, customAttributes);
            
            // row 30
            i++;
            time = getTimestamp(2006, Calendar.DECEMBER, 1);
            createDate = getTimestamp(2007, Calendar.JANUARY, 5);
            modifiedDate = getTimestamp(2007, Calendar.JANUARY, 5);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("URL", "http://yahoo.com");
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, qavm06HostName, "10.187.6.11", 
                                              ieAppName, new Long(i), ActionEnumType.ACTION_RUN, 
                                              time, p1Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file:///c:/program files/internet explorer/iexplore.exe", new Long(67000), "S-1-5-32-544", 
                                              createDate, modifiedDate, null, 3, customAttributes);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, "10.187.6.11", 
                                                ieAppName, new Long(i), ActionEnumType.ACTION_RUN, 
                                                time, "file:///c:/program files/internet explorer/iexplore.exe", new Long(67000), 
                                                "S-1-5-32-544", 
                                                createDate, modifiedDate, null, 3, customAttributes);
            
            // row 31
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 28);
            createDate = getTimestamp(2006, Calendar.JUNE, 6);
            modifiedDate = getTimestamp(2007, Calendar.MARCH, 25);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("URL", "http://yahoo.com");
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, qavm06HostName, null, 
                                              ieAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                              time, p11Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file:///c:/dir1/!@#$%^&*().txt", new Long(1331200), "S-1-5-21-830805687-550985140-3285839444-1170", 
                                              createDate, modifiedDate, null, 2, customAttributes);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, null, 
                                                ieAppName, new Long(i), ActionEnumType.ACTION_OPEN, 
                                                time, "file:///c:/dir1/!@#$%^&*().txt", new Long(1331200), 
                                                "S-1-5-21-830805687-550985140-3285839444-1170", 
                                                createDate, modifiedDate, null, 2, customAttributes);
            
            // row 32
            i++;
            time = getTimestamp(2006, Calendar.DECEMBER, 1);
            createDate = getTimestamp(2007, Calendar.JANUARY, 5);
            modifiedDate = getTimestamp(2007, Calendar.JANUARY, 5);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Sent To", "john.smith@hotmail.com, jimmy carter (1977-81), vvalsaraj");
            customAttributes.put("Obligations", "Warn User");
            customAttributes.put("User Action", "Cancel");
            insertSinglePolicyActivityLogData(s, jCarter, null, host1, capeHostName, "10.187.6.11", 
                                              "c:\\program files\\Microsoft Office Communicator\\communicator.exe", new Long(i), ActionEnumType.ACTION_MEETING, 
                                              time, p1Id, PolicyDecisionEnumType.POLICY_DECISION_ALLOW, 
                                              "file:///c:/dir1/resource1.txt", new Long(1331200), "S-1-5-21-830805687-550985140-3285839444-1170", 
                                              createDate, modifiedDate, null, 2, customAttributes);
            insertSingleTrackingActivityLogData(s, jCarter, null, host1, capeHostName, "10.187.6.11", 
                                                "c:\\program files\\Microsoft Office Communicator\\communicator.exe", new Long(i), ActionEnumType.ACTION_MEETING, 
                                                time,  "file:///c:/dir1/resource1.txt", new Long(1331200), 
                                                "S-1-5-21-830805687-550985140-3285839444-1170", 
                                                createDate, modifiedDate, null, 2, customAttributes);
            
            
            
            // row 33
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 28);
            createDate = getTimestamp(2004, Calendar.NOVEMBER, 3);
            modifiedDate = getTimestamp(2007, Calendar.JANUARY, 5);
            customAttributes = new LinkedHashMap<String, String>();
            customAttributes.put("Sent To", "john.smith@hotmail.com");
            customAttributes.put("Obligations", "Append");
            insertSinglePolicyActivityLogData(s, cArthur, null, host1, qavm06HostName, "10.187.6.11", 
                                              "c:\\program files\\Microsoft Office Communicator\\communicator.exe", new Long(i), ActionEnumType.ACTION_AVD, 
                                              time, p11Id, PolicyDecisionEnumType.POLICY_DECISION_DENY, 
                                              "file:///c:/dir1/resource with space.xls", new Long(3379200), "S-1-5-32-544", 
                                              createDate, modifiedDate, null, 3, customAttributes);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, "10.187.6.11", 
                                                "c:\\program files\\Microsoft Office Communicator\\communicator.exe", new Long(i), ActionEnumType.ACTION_AVD, 
                                                time, "file:///c:/dir1/resource with space.xls", 
                                                new Long(3379200), "S-1-5-32-544", 
                                                createDate, modifiedDate, null, 3, customAttributes);
            
            // row 34
            i++;
            time = getTimestamp(2008, Calendar.DECEMBER, 31);
            insertSingleTrackingActivityLogData(s, gFord, "gerald.ford@QAVM06", host1, capeHostName, cape06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_ABNORMAL_AGENT_SHUTDOWN, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/bin/complianceAgentService.exe", null, null, 
                                                null, null, null, 1, null);
            
            // row 35
            i++;
            time = getTimestamp(2009, Calendar.FEBRUARY, 28);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_STOP_AGENT, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/bin/complianceAgentService.exe", null, null, 
                                                null, null, null, 2, null);
            
            // row 36
            i++;
            time = getTimestamp(2009, Calendar.MARCH, 20);
            insertSingleTrackingActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_START_AGENT, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/bin/complianceAgentService.exe", null, null, 
                                                null, null, null, 3, null);
            
            // row 37
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 31);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_ACCESS_AGENT_BINARIES, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/bin/bundle.bin", null, null, 
                                                null, null, null, 1, null);
            
            // row 38
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 31);
            insertSingleTrackingActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_ACCESS_AGENT_CONFIG, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/config/config.dat", null, null, 
                                                null, null, null, 2, null);
            
            // row 39
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 31);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_ACCESS_AGENT_LOGS, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/logs/pa_activity.log2", null, null, 
                                                null, null, null, 3, null);
            
            // row 40
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 31);
            insertSingleTrackingActivityLogData(s, gFord, null, host1, capeHostName, cape06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_AGENT_USER_LOGIN, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/bin/complianceAgentService.exe", null, null, 
                                                null, null, null, 1, null);
            
            // row 41
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 31);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_AGENT_USER_LOGOUT, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/bin/complianceAgentService.exe", null, null, 
                                                null, null, null, 2, null);
            
            // row 42
            i++;
            time = getTimestamp(2007, Calendar.MARCH, 2);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_INVALID_BUNDLE, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/bin/complianceAgentService.exe", null, null, 
                                                null, null, null, 3, null);
            
            // row 43
            i++;
            time = getTimestamp(2009, Calendar.MARCH, 10);
            insertSingleTrackingActivityLogData(s, gFord, null, host1, capeHostName, null, 
                                                null, new Long(i), ActionEnumType.ACTION_BUNDLE_RECEIVED, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/bin/complianceAgentService.exe", null, null, 
                                                null, null, null, 1, null);
            
            // row 44
            i++;
            time = getTimestamp(2009, Calendar.JANUARY, 15);
            insertSingleTrackingActivityLogData(s, cArthur, null, host1, qavm06HostName, qavm06IPAddress, 
                                                null, new Long(i), ActionEnumType.ACTION_ACCESS_AGENT_BUNDLE, 
                                                time, "file:///c:/program files/compliant enterprise/compliance agent/bin/complianceAgentService.exe", null, null, 
                                                null, null, null, 3, null);

            t.commit(); // finalizes all the changes to the tables
        }

        catch (HibernateException e) {
            System.out.println(e);
        } finally {
            HibernateUtils.closeSession(s, null); // end hibernate session
        }
    }

    /**
     * Clears up activity data
     * 
     * @param s
     *            hibernate session to use
     * @throws HibernateException
     *             if an database exception occurs
     */
    private void clearActivityData(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            Connection con = s.connection();
            final PreparedStatement deletePolicyActivityCustomAttr = con.prepareStatement("delete FROM policy_custom_attr");
            deletePolicyActivityCustomAttr.execute();
            final PreparedStatement deleteDocumentActivityCustomAttr = con.prepareStatement("delete FROM tracking_custom_attr");
            deleteDocumentActivityCustomAttr.execute();
            final PreparedStatement deletePolicyActivity = con.prepareStatement("delete FROM policy_activity_log");
            deletePolicyActivity.execute();
            final PreparedStatement deleteDocumentActivity = con.prepareStatement("delete FROM tracking_activity_log");
            deleteDocumentActivity.execute();
            t.commit();
        } catch (SQLException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw new HibernateException(e);
        }
    }
    
    /**
     * Clears up the performance data
     * 
     * @param s
     *            hibernate session to use
     * @throws HibernateException
     *             if an database exception occurs
     */
    private void clearPerformanceData(Session s) throws HibernateException {
        Transaction t = null;
        try {
            t = s.beginTransaction();
            Connection con = s.connection();
            final PreparedStatement deletePolicyActivity = con.prepareStatement("delete FROM policy_activity_log");
            deletePolicyActivity.execute();
            final PreparedStatement deleteDocumentActivity = con.prepareStatement("delete FROM tracking_activity_log");
            deleteDocumentActivity.execute();
            t.commit();
        } catch (SQLException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw new HibernateException(e);
        }
    }
    
    /**
     * This function insert the performance data into the database. This
     * function picks one user randomly in the user table, and insert the
     * relevant number of rows in the document activity and policy activity
     * table.
     * 
     * @throws HibernateException
     *             if an hibernate exception occurs
     * @throws MappingException
     *             if some data source issue occurs
     * @throws MissingUserException
     *             if no user can be found
     */
    public void createPerformanceData(final Session s, int perfRowCount) throws HibernateException, MappingException,
			MissingUserException {
    	clearPerformanceData(s);
        //Pick a random user id among existing users
        UserDO user = getRandomUser(s);
        //Pick a host id among existing hosts
        HostDO host = getRandomHost(s);

        Long[] policyIds = insertDummyPolicies(s);
        final Long p1Id = policyIds[0];
        String[] usernames = {"jimmy.carter@test.bluejungle.com", "gerald.ford@test.bluejungle.com", 
                              "chester.arthur@test.blujungle.com", "john.tyler@test.bluejungle.com", 
                              "james.garfield@test.bluejungle.com", "abraham.lincoln@test.bluejungle.com"};
        
        String[] hostnames = {"angel.bluejungle.com", "baixo.bluejungle.com", 
                              "pemba.blujungle.com", "nassau.bluejungle.com", 
                              "treasure.bluejungle.com", "svalbard.bluejungle.com", 
                              "seneca.bluejungle.com", "zurio.bluejungle.com"};

        final Calendar time = getTimestamp(2007, Calendar.NOVEMBER, 1);
        final Calendar endTime = getTimestamp(2007, Calendar.DECEMBER, 1);
        final Calendar createDate = getTimestamp(2005, Calendar.JANUARY, 1);
        Transaction t = null;
        int id = 0;
        try {

            while (time.before(endTime)){
                t = s.beginTransaction();
                int max = 0;
                if (time.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    time.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
                    max = new Double(perfRowCount * .20 * Math.random()).intValue();
                } else {
                    max = perfRowCount;
                }
                for (int i = 0; i < max; i++) {
                    int userIndex = new Double(Math.random()*7).intValue()%7;
                    if (userIndex == 6){
                        // do nothing
                    } else {
                        user.setDisplayName(usernames[userIndex]);
                    }
                    int hostIndex = new Double(Math.random()*8).intValue()%8;
                    host.setName(hostnames[hostIndex]);
                    
                    insertPolicyActivityLogData(s, user, host, new Long(id), ActionEnumType.ACTION_CHANGE_ATTRIBUTES, time, new Long((i % 17) + 1), PolicyDecisionEnumType.POLICY_DECISION_ALLOW, "file:///c:/dir1/resource1.txt", new Long(1000), ARTHUR_SID, createDate, null, (i % 3 == 0) ? 3 : i % 3);
                    //insertTrackingActivityLogData(s, user, host, new Long(id), ActionEnumType.ACTION_CHANGE_ATTRIBUTES, time, "file:///c:/dir1/resource1.txt", new Long(1000), ARTHUR_SID, createDate, null, (i % 3 == 0) ? 3 : i % 3);
                    //Flush session every 1000 records

                    id++;
                }
                s.flush();
                t.commit();
//                displayMessage("Completed " + i + " rows");
                s.clear();
                t = s.beginTransaction();
                time.add(Calendar.DAY_OF_MONTH, 1);
//                if (time.get(Calendar.DAY_OF_MONTH) == 1){
//                    time.add(Calendar.MONTH, 1);
//                }
                if (Math.random() > 0.15){
                    perfRowCount = perfRowCount - new Double(59 * Math.random()).intValue();
                } else {
                    perfRowCount = perfRowCount + new Double(31 * Math.random()).intValue();
                }
                user = getRandomUser(s);
                host = getRandomHost(s);
            }
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(t, null);
            throw e;
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }
    
    /**
     * Display a message to the standard output
     * 
     * @param message
     *            message to display
     */
    private static void displayMessage(final String message) {
        System.out.println(message);
    }

    /**
     * Returns a random host id from a user already in the host table
     * 
     * @param s
     *            hibernate session to use
     * @return a random existing host id
     * @throws HibernateException
     */
    private HostDO getRandomHost(Session s) throws HibernateException {
        Criteria crit = s.createCriteria(HostDO.class).add(Expression.gt("id", new Long(0)));
        crit.setMaxResults(1);
        final List result = crit.list();
        if (result.size() > 0) {
            return ((HostDO) result.get(0));
        }
        return null;
    }

    /**
     * Returns a random user from a user already in the user table
     * 
     * @param s
     *            hibernate session to use
     * @return a random existing user
     * @throws HibernateException
     */
    private UserDO getRandomUser(Session s) throws HibernateException {
        Criteria crit = s.createCriteria(UserDO.class).add(Expression.gt("id", new Long(0)));
        crit.setMaxResults(1);
        final List result = crit.list();
        if (result.size() > 0) {
            return ((UserDO) result.get(0));
        }
        return null;
    }
    
    /**
     * Returns a timestamp. The time is set to 3:10:05 PM.
     * 
     * @param year
     *            year number (calendar format)
     * @param month
     *            month number (calendar format)
     * @param day
     *            day number (calendar format)
     * @return the timestamp.
     */
    private Calendar getTimestamp(final int year, final int month, final int day) {
        Calendar result = Calendar.getInstance();
        result.set(Calendar.YEAR, year);
        result.set(Calendar.MONTH, month);
        result.set(Calendar.DAY_OF_MONTH, day);
        result.set(Calendar.HOUR_OF_DAY, 15);
        result.set(Calendar.MINUTE, 10);
        result.set(Calendar.SECOND, 5);
        result.set(Calendar.MILLISECOND, 0);
        return result;
    }

    /**
     * This function creates a Hibernate session configured with the user input
     * parameters.
     * 
     * @return a full configured hibernate session
     * @throws MappingException
     *             if class mapping exception occurs
     * @throws HibernateException
     *             if creating the hibernate session fails.
     */
    public Session initHibernateSession(Properties extraHibernateProperties)
			throws HibernateException, MappingException {
        SessionFactory sessions = initHibernateSessionFactory(extraHibernateProperties);
        return sessions.openSession();
    }
    
    public SessionFactory initHibernateSessionFactory(Properties extraHibernateProperties)
			throws HibernateException, MappingException {
		Configuration cfg = new Configuration();
		cfg.addClass(UserDO.class);
		cfg.addClass(PolicyDO.class);
		cfg.addClass(HostDO.class);
		cfg.addClass(TestPolicyActivityLogEntryDO.class);
		cfg.addClass(TestTrackingActivityLogEntryDO.class);
		cfg.addClass(UserGroupDO.class);
		cfg.addClass(TestPolicyActivityLogCustomAttributeDO.class);
		cfg.addClass(TestTrackingActivityLogCustomAttributeDO.class);
		cfg.addClass(PolicyAssistantLogDO.class);

		// this clause compile error, the comipler was looking for org\dom4j\Element.class
		// cfg.addProperties(extraHibernateProperties);

		cfg.getProperties().putAll(extraHibernateProperties);

//		cfg.setProperty("hibernate.order_updates", "true");
		return cfg.buildSessionFactory();
	}

    /**
     * This function adds two dummy policies into the policy table. If the
     * policies already exist, they are not inserted.
     * 
     * @param s
     *            Hibernate session to use
     * @return the id of the two policies
     * @throws HibernateException
     */
    public Long[] insertDummyPolicies(Session s) throws HibernateException {

        //query database, find policy1 and policy2
        PolicyDO policy1 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "APolicy1")).uniqueResult();
        PolicyDO policy7 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "APolicy12")).uniqueResult();
        PolicyDO policy8 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "APolicy13")).uniqueResult();
        PolicyDO policy9 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "APolicy14")).uniqueResult();
        PolicyDO policy10 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("fullName", "APolicy15")).uniqueResult();
        PolicyDO policy11 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "looooooooooooooooooooooongBPolicy2")).uniqueResult();
        PolicyDO policy12 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "CPolicy32")).uniqueResult();
        PolicyDO policy13 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "DPolicy44")).uniqueResult();
        PolicyDO policy14 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "CPolicy33")).uniqueResult();
        PolicyDO policy15 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "CPolicy3")).uniqueResult();
        PolicyDO policy16 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "DPolicy46")).uniqueResult();
        PolicyDO policy17 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "CPolicy37")).uniqueResult();
        PolicyDO policy18 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "CPolicy48")).uniqueResult();
        PolicyDO policy19 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "CPolicy59")).uniqueResult();
        PolicyDO policy20 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|longpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpoli")).uniqueResult();
        PolicyDO policy21 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()")).uniqueResult();
        PolicyDO policy22 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "longpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpoli")).uniqueResult();
        PolicyDO policy23 = (PolicyDO) s.createCriteria(PolicyDO.class).add(Expression.eq("name", "APolicy18")).uniqueResult();
        
        
        if (policy1 == null) // if policy1 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy1 = new PolicyDO();
            policy1.setFullName("/Afolder1/APolicy1");
            if (maxId == null) {
                policy1.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy1.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy1);
            t.commit();
        }
        if (policy7 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy7 = new PolicyDO();
            policy7.setFullName("/A2folder1/APolicy12");
            if (maxId == null) {
                policy7.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy7.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy7);
            t.commit();
        }
        if (policy8 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy8 = new PolicyDO();
            policy8.setFullName("/A3folder1/APolicy13");
            if (maxId == null) {
                policy8.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy8.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy8);
            t.commit();
        }
        if (policy9 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy9 = new PolicyDO();
            policy9.setFullName("/A4folder1/APolicy14");
            if (maxId == null) {
                policy9.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy9.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy9);
            t.commit();
        }
        if (policy10 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy10 = new PolicyDO();
            policy10.setFullName("/A5folder1/APolicy15");
            if (maxId == null) {
                policy10.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy10.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy10);
            t.commit();
        }
        if (policy11 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy11 = new PolicyDO();
            policy11.setFullName("/Bfolder2/Bsubfolder1/Bsubfolder2/Bsubfolder3/looooooooooooooooooooooongBPolicy2");
            if (maxId == null) {
                policy11.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy11.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy11);
            t.commit();
        }
        if (policy12 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy12 = new PolicyDO();
            policy12.setFullName("/Cfolder32/CPolicy32");
            if (maxId == null) {
                policy12.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy12.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy12);
            t.commit();
        }
        if (policy13 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy13 = new PolicyDO();
            policy13.setFullName("/Dfolder44/DPolicy44");
            if (maxId == null) {
                policy13.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy13.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy13);
            t.commit();
        }
        if (policy14 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy14 = new PolicyDO();
            policy14.setFullName("/Cfolder33/CPolicy33");
            if (maxId == null) {
                policy14.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy14.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy14);
            t.commit();
        }
        if (policy15 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy15 = new PolicyDO();
            policy15.setFullName("/Cfolder3/CPolicy3");
            if (maxId == null) {
                policy15.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy15.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy15);
            t.commit();
        }
        if (policy16 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy16 = new PolicyDO();
            policy16.setFullName("/Dfolder46/DPolicy46");
            if (maxId == null) {
                policy16.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy16.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy16);
            t.commit();
        }
        if (policy17 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy17 = new PolicyDO();
            policy17.setFullName("/Cfolder37/CPolicy37");
            if (maxId == null) {
                policy17.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy17.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy17);
            t.commit();
        }
        if (policy18 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy18 = new PolicyDO();
            policy18.setFullName("/Cfolder38/CPolicy48");
            if (maxId == null) {
                policy18.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy18.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy18);
            t.commit();
        }
        if (policy19 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy19 = new PolicyDO();
            policy19.setFullName("/Cfolder39/CPolicy59");
            if (maxId == null) {
                policy19.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy19.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy19);
            t.commit();
        }
        if (policy20 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy20 = new PolicyDO();
            policy20.setFullName("/Bfolder2/Bsubfolder1/Bsubfolder2/Bsubfolder3/!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|longpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpoli");
            if (maxId == null) {
                policy20.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy20.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy20);
            t.commit();
        }
        if (policy21 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy21 = new PolicyDO();
            policy21.setFullName("/Bfolder2/Bsubfolder1/Bsubfolder2/Bsubfolder3/!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()\"|!@#%^&()");
            if (maxId == null) {
                policy21.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy21.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy21);
            t.commit();
        }
        if (policy22 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy22 = new PolicyDO();
            policy22.setFullName("/Bfolder2/Bsubfolder1/Bsubfolder2/Bsubfolder3/longpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpolicylongpoli");
            if (maxId == null) {
                policy22.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy22.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy22);
            t.commit();
        }
        if (policy23 == null) // if policy2 doesn't exist, create it.
        {
            final Long maxId = (Long) s.createQuery("select max(p.id) from PolicyDO p").uniqueResult();
            Transaction t = s.beginTransaction();
            policy23 = new PolicyDO();
            policy23.setFullName("/A4folder1/APolicy18");
            if (maxId == null) {
                policy23.setId(new Long(1));
            } else {
                //Give the policy a unique id number
                policy23.setId(new Long(maxId.longValue() + 1));
            }
            s.save(policy23);
            t.commit();
        }
        return new Long[] { policy1.getId(), policy7.getId(), policy8.getId(), policy9.getId(),
                            policy10.getId(), policy11.getId(), policy12.getId(), policy13.getId(), 
                            policy14.getId(), policy15.getId(), policy16.getId(), policy17.getId(), 
                            policy18.getId(), policy19.getId(), policy20.getId(), policy21.getId(), 
                            policy22.getId()};
    }

    /**
     * Sets up a Data Object with properties as suggested by the parameters,
     * then saves the changes to the PolicyActivityLogData table.
     * 
     * @param s
     * @param user
     * @param host
     * @param id
     * @param action
     * @param time
     * @param policyId
     * @param enforcement
     * @param fr_name
     * @param fr_size
     * @param fr_owner
     * @param fr_createDate
     * @param tr_name
     * @throws Exception
     */
    protected void insertPolicyActivityLogData(Session s, 
    		UserDO user, 
    		HostDO host, 
    		Long id,
			ActionEnumType action, 
			Calendar time, 
			Long policyId,
			PolicyDecisionEnumType enforcement, 
			String fr_name, 
			Long fr_size, 
			String fr_owner,
			Calendar fr_createDate,
            String tr_name, 
            int level) throws HibernateException {
        TestPolicyActivityLogEntryDO testDO = new TestPolicyActivityLogEntryDO();
        FromResourceInformationDO fromResourceDO = new FromResourceInformationDO();
        ToResourceInformationDO toResourceDO = new ToResourceInformationDO();
        fromResourceDO.setName(fr_name);
        fromResourceDO.setSize(fr_size);
        fromResourceDO.setOwnerId(fr_owner);
        fromResourceDO.setModifiedDate(time);
        fromResourceDO.setCreatedDate(fr_createDate); // set access date doesn't
        // exist
        toResourceDO.setName(tr_name);
        testDO.setFromResourceInfo(fromResourceDO);
        testDO.setToResourceInfo(toResourceDO);
        testDO.setId(id);
        testDO.setAction(action);
        testDO.setTimestamp(time);
        testDO.setPolicyId(policyId);
        testDO.setPolicyDecision(enforcement);
        testDO.setUserId(user.getOriginalId());
        testDO.setUserName(user.getDisplayName());
        testDO.setHostId(host.getId());
        testDO.setHostName(host.getName());
        testDO.setApplicationId(new Long(-1));
        testDO.setHostIPAddress("192.168.2.1");
        testDO.setApplicationName("c:/abc.exe");
        testDO.setDecisionRequestId(new Long(-1));
        testDO.setLevel(level);
        testDO.setHostName("SVALBARD.bluejungle.com");
        s.save(testDO);
    }
    
    private void insertSinglePolicyActivityLogData(Session s, UserDO user, String userName, HostDO host, 
                                                   String hostName, String hostIP, String applicationName,
                                                   Long id, ActionEnumType action, Calendar time, 
                                                   Long policyId, PolicyDecisionEnumType enforcement, String fr_name, 
                                                   Long fr_size, String fr_owner, Calendar fr_createDate, 
                                                   Calendar fr_modifiedDate, 
                                                   String tr_name, int level, LinkedHashMap customAttributes) throws HibernateException {
        TestPolicyActivityLogEntryDO testDO = new TestPolicyActivityLogEntryDO();
        FromResourceInformationDO fromResourceDO = new FromResourceInformationDO();
        ToResourceInformationDO toResourceDO = new ToResourceInformationDO();
        fromResourceDO.setName(fr_name);
        fromResourceDO.setSize(fr_size);
        fromResourceDO.setOwnerId(fr_owner);
        fromResourceDO.setModifiedDate(fr_modifiedDate);
        fromResourceDO.setCreatedDate(fr_createDate); 
        toResourceDO.setName(tr_name);
        testDO.setFromResourceInfo(fromResourceDO);
        testDO.setToResourceInfo(toResourceDO);
        testDO.setId(id);
        testDO.setAction(action);
        testDO.setTimestamp(time);
        testDO.setPolicyId(policyId);
        testDO.setPolicyDecision(enforcement);
        testDO.setUserId(user.getOriginalId());
        if (userName != null){
            testDO.setUserName(userName);
        } else {
            testDO.setUserName(user.getDisplayName());
        }
        testDO.setHostId(host.getId());
        testDO.setHostName(hostName);
        testDO.setHostIPAddress(hostIP);
        testDO.setApplicationId(new Long(-1));
        testDO.setApplicationName(applicationName);
        testDO.setDecisionRequestId(new Long(-1));
        testDO.setLevel(level);
        s.save(testDO);
        
        if (customAttributes != null){
            Iterator attrIter = customAttributes.entrySet().iterator();
            while (attrIter.hasNext()){
                Map.Entry<String, String> entry = (Map.Entry<String, String>)attrIter.next();
                TestPolicyActivityLogCustomAttributeDO attrDO = new TestPolicyActivityLogCustomAttributeDO();
//                attrDO.setId(policyAttrID);

                attrDO.setValue(entry.getValue());
                s.update(testDO);
                attrDO.setRecord(testDO);
                attrDO.setKey(entry.getKey());
                s.save(attrDO);      
                policyAttrID++;
            }
        }
    }

    /**
     * Sets up a Data Object with properties as suggested by the parameters,
     * then saves the changes to the TrackingActivityLogData table.
     * 
     * @param s
     * @param user
     * @param host
     * @param id
     * @param action
     * @param time
     * @param fr_name
     * @param fr_size
     * @param fr_owner
     * @param fr_createDate
     * @param tr_name
     * @throws Exception
     */
    protected void insertTrackingActivityLogData(Session s, UserDO user, HostDO host, Long id, ActionEnumType action, Calendar time, String fr_name, Long fr_size, String fr_owner, Calendar fr_createDate, String tr_name, int level) throws HibernateException {
        TestTrackingActivityLogEntryDO testDO = new TestTrackingActivityLogEntryDO();
        FromResourceInformationDO fromResourceDO = new FromResourceInformationDO();
        ToResourceInformationDO toResourceDO = new ToResourceInformationDO();
        fromResourceDO.setName(fr_name);
        fromResourceDO.setSize(fr_size);
        fromResourceDO.setOwnerId(fr_owner);
        fromResourceDO.setModifiedDate(time);
        fromResourceDO.setCreatedDate(fr_createDate);
        toResourceDO.setName(tr_name);
        testDO.setFromResourceInfo(fromResourceDO);
        testDO.setToResourceInfo(toResourceDO);
        testDO.setId(id);
        testDO.setAction(action);
        testDO.setTimestamp(time);
        testDO.setUserId(user.getOriginalId());
        testDO.setUserName(user.getDisplayName());
        testDO.setHostId(host.getId());
        testDO.setHostIPAddress("192.168.2.1");
        testDO.setHostName("SVALBARD.bluejungle.com");
        testDO.setApplicationId(new Long(-1));
        testDO.setLevel(level);
        s.save(testDO);
    }
    
    private void insertSingleTrackingActivityLogData(Session s, UserDO user, String userName, HostDO host, 
                                                     String hostName, String hostIP, String applicationName,
                                                     Long id, ActionEnumType action, Calendar time, 
                                                     String fr_name, Long fr_size, String fr_owner, 
                                                     Calendar fr_createDate, Calendar fr_modifiedDate, 
                                                     String tr_name, int level, 
                                                     LinkedHashMap customAttributes) throws HibernateException {
        TestTrackingActivityLogEntryDO testDO = new TestTrackingActivityLogEntryDO();
        FromResourceInformationDO fromResourceDO = new FromResourceInformationDO();
        ToResourceInformationDO toResourceDO = new ToResourceInformationDO();
        fromResourceDO.setName(fr_name);
        fromResourceDO.setSize(fr_size);
        fromResourceDO.setOwnerId(fr_owner);
        fromResourceDO.setModifiedDate(fr_modifiedDate);
        fromResourceDO.setCreatedDate(fr_createDate);
        toResourceDO.setName(tr_name);
        testDO.setFromResourceInfo(fromResourceDO);
        testDO.setToResourceInfo(toResourceDO);
        testDO.setId(id);
        testDO.setAction(action);
        testDO.setTimestamp(time);
        testDO.setUserId(user.getOriginalId());
        if (userName != null){
            testDO.setUserName(userName);
        } else {
            testDO.setUserName(user.getDisplayName());
        }
        testDO.setHostId(host.getId());
        testDO.setHostIPAddress(hostIP);
        testDO.setHostName(hostName);
        testDO.setApplicationName(applicationName);
        testDO.setApplicationId(new Long(-1));
        testDO.setLevel(level);
        s.save(testDO);
        
        if (customAttributes != null){
            Iterator attrIter = customAttributes.entrySet().iterator();
            while (attrIter.hasNext()){
                Map.Entry<String, String> entry = (Map.Entry<String, String>)attrIter.next();
                TestTrackingActivityLogCustomAttributeDO attrDO = new TestTrackingActivityLogCustomAttributeDO();
//                attrDO.setId(trackingAttrID);
                attrDO.setKey(entry.getKey());
                attrDO.setValue(entry.getValue());
                s.update(testDO);
                attrDO.setRecord(testDO);
                s.save(attrDO);      
                trackingAttrID++;
            }
        }
    }
    
    public void readInput(File inputFile) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        cArthurDisplayName = bufferedReader.readLine();
        gFordDisplayName = bufferedReader.readLine();
        jGarfieldDisplayName = bufferedReader.readLine();
        jCarterDisplayName = bufferedReader.readLine();
        bufferedReader.readLine();
        hostDisplayName = bufferedReader.readLine();
    }
    
    public void readInput(String inputFileStr) throws IOException{
    	File inputFile = new File(inputFileStr);
    	readInput(inputFile);
    }
}
