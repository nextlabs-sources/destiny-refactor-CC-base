/*
 * Created on May 14, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.TestActiveDirectoryEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.TestActiveDirectoryEnrollerGroup;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.TestActiveDirectoryEnrollerLargeOu;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.TestActiveDirectoryEnrollerSmallOuBasic;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.TestActiveDirectoryEnrollerSmallOuComplex;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.TestLdifEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.TestApplicationEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl.TestEnrollmentManagerImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.TestSharePointEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.text.TestJavaPropertiesFileEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.TestRfcFilterEvaluator;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.tools.LocalDestinyDataEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.tools.TestFixedDestinyDataEnroller;
import com.bluejungle.dictionary.DictionaryException;

/**
 * This is the master test suite for the enrollment component 
 * 
 * @author atian 
 * @version $Id:
 */

public class EnrollmentTestSuite {

     /**
     * Returns the test suite
     * 
     * @return the test suite
     * @throws DictionaryException 
     * @throws SQLException 
     * @throws HibernateException 
     */
    public static Test suite() throws HibernateException, SQLException, DictionaryException {
        TestSuite suite = new TestSuite("Enrollment TestSuite");
        suite.addTest(new TestSuite(TestActiveDirectoryEnroller.class, "Active Direcotry Enrollment Test"));
        suite.addTest(new TestSuite(TestSharePointEnroller.class, "Test share point enrollment"));
        suite.addTest(new TestSuite(TestLdifEnroller.class, "Ldif Enrollment Test"));
        suite.addTest(new TestSuite(TestEnrollmentManagerImpl.class, "Enrollment Manager Test"));
        suite.addTest(new TestSuite(TestRfcFilterEvaluator.class, "RFC element type detection filter Test"));
        suite.addTest(new TestSuite(TestApplicationEnroller.class, "Test application enrollment"));
        suite.addTest(new TestSuite(TestJavaPropertiesFileEnroller.class, "Test Location enrollment"));
        suite.addTest(new TestSuite(LocalDestinyDataEnroller.class, "Refresh local enrollment"));
        
        //suite.addTest(new TestSuite(TestActiveDirectoryEnrollerGroup.class, "test ad group"));
        //suite.addTest(new TestSuite(TestActiveDirectoryEnrollerLargeOu.class, "test ad large dataset"));
        suite.addTest(new TestSuite(TestActiveDirectoryEnrollerSmallOuBasic.class, "test ad small dataset with basic cases"));
        //suite.addTest(new TestSuite(TestActiveDirectoryEnrollerSmallOuComplex.class, "test ad small dataset with complex cases"));
        
        return suite;
    }
}
