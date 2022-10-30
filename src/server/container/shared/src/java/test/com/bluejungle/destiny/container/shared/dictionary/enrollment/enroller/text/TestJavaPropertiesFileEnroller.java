/*
 * Created on Mar 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.DuplicateEntryException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.TestSharePointEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.TestEnroller;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.configuration.DestinyRepository;


/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/text/TestJavaPropertiesFileEnroller.java#1 $
 */

public class TestJavaPropertiesFileEnroller extends BaseContainerSharedTestCase {

    public static final Set<DestinyRepository> REQUIRED_DATA_REPOSITORIES = new HashSet<DestinyRepository>();
    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
    }

    private IDictionary dictionary;
    private TestEnroller enroller;
    
    private static final String ENROLL_DIR = SRC_ROOT_DIR + "/server/tools/enrollment/etc/";
    private static String TEST_DIR;
    static{
    	String testFilesDirStr = System.getProperty("test_files.dir");
    	if(testFilesDirStr == null){
    		testFilesDirStr = SRC_ROOT_DIR + "/../test_files";
    	}
    	TEST_DIR = testFilesDirStr + "/com/bluejungle/pf/tools/";
    	
    }
    
    private static final String SITE_DOMAIN  = "Site_EnrollmentTest";
    private static String SITE_DOMAIN_PROPERTY_FILE  = ENROLL_DIR + "site.sample.default.def";
    private static String SITE_SAMPLE_FILE  = TEST_DIR + "locations.txt";
    
    /**
     * Start local data enroller by Junit
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestSharePointEnroller.class);
    }

    /**
     * @see com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase#getDataRepositories()
     */
    protected Set<DestinyRepository> getDataRepositories() {
        return REQUIRED_DATA_REPOSITORIES;
    }

    
    /*
     * @see TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();
        this.enroller = new TestEnroller();
        this.dictionary = (IDictionary) ComponentManagerFactory.getComponentManager().getComponent(Dictionary.COMP_INFO);
    }

    /**
     * Clean all enrollments
     * @throws DictionaryException
     */
    public void testDeleteSiteEnrollments() throws DictionaryException {
        Collection<IEnrollment> enrollments = this.dictionary.getEnrollments();
        Iterator<IEnrollment> itor = enrollments.iterator();
        IConfigurationSession session = this.dictionary.createSession();
        session.beginTransaction();
        while (itor.hasNext()) {
            IEnrollment enrollment = (IEnrollment) itor.next();
            if ( SITE_DOMAIN.equalsIgnoreCase( enrollment.getDomainName() ) ) {
                session.deleteEnrollment(enrollment);
                break;
            }           
        }
        session.commit();
        session.close();
    }
     
    /**
     * Enroll local domains
     * @throws IOException 
     * @throws DuplicateEnrollmentException 
     * @throws DictionaryException 
     * @throws InvalidConfigurationException 
     * @throws EntryNotFoundException 
     * @throws FileNotFoundException 
     * @throws DuplicateEntryException 
     * @throws EnrollmentValidationException 
     * @throws EnrollerCreationException 
     * @throws Exception
     */
    public void testCreateSiteEnrollment() throws Exception {
        Map<String, String[]> update = new HashMap<String, String[]>();
        update.put( JavaPropertiesEnrollmentProperties.SITE_FILE_NAME_PROPERTY, new String[] {SITE_SAMPLE_FILE} );
        this.enroller.enroll(SITE_DOMAIN, 
                new String[] {SITE_DOMAIN_PROPERTY_FILE },  
                EnrollmentTypeEnumType.TEXT, update);
    }  
    
    public void testSiteEnrollmentResult() throws DictionaryException {
        IMElement site = this.dictionary.getElement("vpn", new Date());
        assertNotNull( site );
        site = this.dictionary.getElement("intranet", new Date());
        assertNotNull( site );
    }
    
    public void testUpdateSiteEnrollment() throws Exception {
        this.enroller.update(SITE_DOMAIN, null, EnrollmentTypeEnumType.TEXT, null);
    }
      
    public void testSiteEnrollmentUpdate() throws DictionaryException {
        IMElement site = this.dictionary.getElement("vpn", new Date());
        assertNotNull( site );
        site = this.dictionary.getElement("intranet", new Date());
        assertNotNull( site );
    }

}
