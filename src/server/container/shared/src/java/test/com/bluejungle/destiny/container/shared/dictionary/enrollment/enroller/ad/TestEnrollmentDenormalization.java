/*
 * Created on Apr 18, 2011
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2011 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.hibernate.HibernateException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.ColumnData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IRealmData;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.BaseEnrollerSharedTestCase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl.LdifEnrollmentProperties;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.dictionary.IUpdateRecord;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/TestEnrollmentDenormalization.java#1 $
 */

public class TestEnrollmentDenormalization extends BaseEnrollerSharedTestCase {

    private static final String AD_DOMAIN                 = "test.ActiveDirectoryEnroller23";
    private static final String AD_DOMAIN_PROPERTY_FILE   = ENROLL_DIR + "test/ad.extrastringinfo.def";
    private static final String AD_DOMAIN_CONNECTION_FILE = ENROLL_DIR + "ad.sample.default.conn";
    
    private static final String LDIF_DOMAIN               = "test.LdifEnroller23";
    private static final String LDIF_PROPERTY_FILE        = ENROLL_DIR + "test/extrastringinfo.def";
    private static final String LDIF_FILE                 = ENROLL_DIR + "test/extrastringinfo.ldif";
   
    private static final ColumnData TEST_COLUMN_1 = new ColumnData(
            "Extra string info", 
            ElementTypeEnumType.USER.getName(), 
            "extrastringinfo", 
            ElementFieldType.STRING.getName());
    
    @Override
    protected String getPasswordPropertyKey(){
        return ActiveDirectoryEnrollmentProperties.PASSWORD;
    }
    
    public void testDeleteAllEnrollments() throws DictionaryException,
            HibernateException, SQLException {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        IConfigurationSession session = dictionary.createSession();
        session.beginTransaction();
        for (IEnrollment enrollment : enrollments) {
            session.deleteEnrollment(enrollment);
        }
        session.commit();
        session.close();

        dbCount.checkDatabaseCount();
    }
    
    public void testAddNewProperty() throws Exception {
        IElementField matchField = null;
        for (IElementField field : enrollmentManager.getColumns()) {
            if (TEST_COLUMN_1.getLogicalName().equals(field.getName())) {
                matchField = field;
            }
        }
        if (matchField == null) {
            enrollmentManager.addColumn(TEST_COLUMN_1);
        }
    }
    
    /**
     * Enroll local domains
     * @throws DictionaryException 
     * @throws SQLException 
     * @throws HibernateException 
     * @throws Exception
     */
    public void testAdEnrollment() throws Exception {
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(0, enrollments.size());
        
        IRealmData data = convert(
                AD_DOMAIN,
                EnrollmentTypeEnumType.DIRECTORY,
                null,   //no override 
                AD_DOMAIN_PROPERTY_FILE,
                AD_DOMAIN_CONNECTION_FILE);
        enrollmentManager.createRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
        
        enrollments = dictionary.getEnrollments();
        assertEquals(1, enrollments.size());
        IEnrollment enrollment = enrollments.iterator().next();
        IUpdateRecord records = enrollment.getStatus();
        assertNotNull(records);
        assertTrue(records.isSuccessful());

        dbCount.resetDatabaseCount();
        assertTrue(dbCount.dictElements > 0);
    }
    
    private static Long johnTylerId;
    
    public void testGetElement1() throws DictionaryException {
        assertNull(johnTylerId);
        IMElementBase elementBase = dictionary.getByUniqueName("john.tyler@linuxtest.bluejungle.com", null);
        assertNotNull(elementBase);
        IMElement element = (IMElement)elementBase;
        Object value = element.getValue("extrastringinfo");
        assertNull(value);
        johnTylerId = element.getInternalKey();
        assertNotNull(johnTylerId);
    }
    
    public void testLdifEnrollments() throws Exception {
        Map<String, String[]> ldif_file1 = new HashMap<String, String[]>(); 
        ldif_file1.put( LdifEnrollmentProperties.LDIF_NAME_PROPERTY, new String[] {LDIF_FILE} );
        
        IRealmData data = convert(
                LDIF_DOMAIN,
                EnrollmentTypeEnumType.LDIF,
                ldif_file1,
                LDIF_PROPERTY_FILE);
        
        enrollmentManager.createRealm(data);
        enrollmentManager.enrollRealm(convert(data.getName()));
        
        dbCount.dictElements++;
        dbCount.dictLeafElements++;
        
        dbCount.checkDatabaseCount();
    }
    
    public void testGetElement2() throws DictionaryException {
        assertNotNull(johnTylerId);
        IMElementBase elementBase = dictionary.getByKey(johnTylerId, null);
        assertNotNull(elementBase);
        IMElement element = (IMElement)elementBase;
        Object value = element.getValue("extrastringinfo");
        assertNotNull(value);
        assertEquals("extrainf0", value);
    }
    
    public void testAdEnrollment2() throws Exception {
        enrollmentManager.enrollRealm(convert(AD_DOMAIN));
        
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        assertEquals(2, enrollments.size());
        IEnrollment enrollment = null;
        for(IEnrollment e : enrollments) {
            if(e.getDomainName().equals(AD_DOMAIN)){
                enrollment = e;
            }
        }
        assertNotNull(enrollment);
        
        IUpdateRecord records = enrollment.getStatus();
        assertNotNull(records);
        assertTrue(records.isSuccessful());

        dbCount.checkDatabaseCount();
    }
    
    public void testLdifEnrollment2() throws Exception {
        enrollmentManager.enrollRealm(convert(LDIF_DOMAIN));
        dbCount.checkDatabaseCount();
    }
}
