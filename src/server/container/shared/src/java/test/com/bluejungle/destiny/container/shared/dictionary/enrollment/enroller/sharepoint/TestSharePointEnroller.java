/*
 * Created on Jan 19, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.BaseEnrollerSharedTestCase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl.SharePointEnrollmentProperties;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionarySizeCount;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMGroup;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/TestSharePointEnroller.java#1 $
 */

public class TestSharePointEnroller extends BaseEnrollerSharedTestCase {
    private static final String SP_DOMAIN  = "Sharepoint2007";
    private static final String USER_DOMAIN = 
        "Sharepoint2007";
//            "TEST";
//            null;
    private static final String USERNAME = 
        "Administrator"; 
//        "jimmy.carter";
    private static final String PASSWORD = 
        "123blue!"; 
//        USERNAME;
    private static String SP_DOMAIN_PROPERTY_FILE  		= ENROLL_DIR + "sp.sample.default.def";
    private static String SP_DOMAIN_CONNECTION_FILE  	= ENROLL_DIR + "sp.sample.default.conn";
    
    private static int[] dbRows = new int[7];
    
    /**
     * Start local data enroller by Junit
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestSharePointEnroller.class);
    }

    protected String getPasswordPropertyKey(){
    	return SharePointEnrollmentProperties.PASSWORD_PROPERTY;
    }
    
    protected boolean clearDatabaseBeforeStart(){
    	//depends on TestActiveDirectoryEnroller
    	return false;
    }
    
    /**
     * Clean all enrollments
     * @throws DictionaryException
     * @throws SQLException 
     * @throws HibernateException 
     */
    public void testDeleteSharePointEnrollments() throws DictionaryException, HibernateException,
			SQLException {
		Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        IConfigurationSession session = dictionary.createSession();
        session.beginTransaction();
        for (IEnrollment enrollment : enrollments) {
			if (SP_DOMAIN.equals(enrollment.getDomainName())) {
				session.deleteEnrollment(enrollment);
				break;
			}
		}
        session.commit();
        session.close();
        dbCount.resetDatabaseCount();
    }
     
    /**
     * Enroll local domains
     * @throws DictionaryException 
     * @throws SQLException 
     * @throws HibernateException 
     * @throws Exception
     */
    
   // public void testCreateSharepointEnrollment() throws Exception {
    //    Map<String, String[]> encode_password = encodePassword(PASSWORD); 
    //    encode_password.put( SharePointEnrollmentProperties.PORTALS_PROPERTY, 
     //           new String[] { "http://sharepoint2007.bluejungle.com/" } ); //, "/MySite" } );
      //  encode_password.put( SharePointEnrollmentProperties.LOGIN_PROPERTY, 
    //            new String[] { USERNAME } );
    //    encode_password.put( SharePointEnrollmentProperties.DOMAIN_PROPERTY, 
   //             new String[] { USER_DOMAIN } );
        
   //     IRealmData data = convert(
   //             SP_DOMAIN, 
   //             EnrollmentTypeEnumType.PORTAL, 
    //            encode_password,
    //            SP_DOMAIN_PROPERTY_FILE,
    //            SP_DOMAIN_CONNECTION_FILE
    //    );
    //    enrollmentManager.createRealm(data);
   //     enrollmentManager.enrollRealm(convert(data.getName()));
        
  //      IEnrollment enrollment = dictionary.getEnrollment(SP_DOMAIN);
   //     assertNotNull(enrollment);
   //     assertTrue(enrollment.getStatus().isSuccessful());
        
   //     dbRows = DictionarySizeCount.add(dbRows, new int[] { 42, 8, 42, 44, 0, 0, 0 });
    //    dbCount.checkDatabaseCount(dbRows);
  //  }  
    
//    public void testSharePointGroups() throws DictionaryException {
//        IMGroup group = this.dictionary.getGroup("SHAREPOINT2007:Groups:Andys Site Members", new Date());
//        assertNotNull( group );
//        group = this.dictionary.getGroup("SHAREPOINT2007:Groups:Test Group", new Date());
//        assertNotNull( group );
//    }
    
//    public void testUpdateSharepointEnrollment() throws Exception {
//        Map<String, String[]> encode_password = encodePassword(PASSWORD);
 //       encode_password.put( SharePointEnrollmentProperties.PORTALS_PROPERTY, 
 //               new String[] {"http://sharepoint2007.bluejungle.com/", "http://sharepoint2007.bluejungle.com/sites/fixed" } );
  //      encode_password.put( SharePointEnrollmentProperties.LOGIN_PROPERTY, 
  //              new String[] { USERNAME } );
  //      encode_password.put( SharePointEnrollmentProperties.DOMAIN_PROPERTY, 
   //             new String[] { USER_DOMAIN } );
        
   //     IRealmData data = convert(
    //            SP_DOMAIN, 
     //           EnrollmentTypeEnumType.PORTAL, 
    //            encode_password,
     //           SP_DOMAIN_PROPERTY_FILE,
     //           SP_DOMAIN_CONNECTION_FILE
    //    );
    //    enrollmentManager.updateRealm(data);
    //    enrollmentManager.enrollRealm(convert(data.getName()));
        
    //    dbRows = DictionarySizeCount.add(dbRows, new int[] { 30, 10, 30, 62, 0, 0, 0 });
    //    dbCount.checkDatabaseCount(dbRows);
  //  }
      
  //  public void testSharePointGroupsAfterUpdate() throws Exception{
  //    IMGroup group = this.dictionary.getGroup("SHAREPOINT2007:Groups:Andys Site Members", new Date());
  //      assertNotNull( group );
 //       group = this.dictionary.getGroup("SHAREPOINT2007:Groups:Test Group", new Date());
 //       assertNotNull( group );
 //   }
    
//    public void testSharePointGroupsMembers() throws DictionaryException {
//        IMGroup group = this.dictionary.getGroup("SHAREPOINT2007:Groups:Andy Tian Group", new Date());
//        assertNotNull( group );
//        IDictionaryIterator<IMElement> members = group.getDirectChildElements();
//        assertNotNull(members);
//        int count =count(members);
//        assertEquals(3, count);
//        members = group.getAllChildElements();
//        assertNotNull(members);
//        count = count(members);
//        assertEquals(6, count);
//    }
}
