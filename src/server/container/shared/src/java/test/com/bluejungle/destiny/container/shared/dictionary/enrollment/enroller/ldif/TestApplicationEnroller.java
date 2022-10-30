package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.DuplicateEntryException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl.LdifEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.TestEnroller;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.domain.enrollment.ApplicationReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;

public class TestApplicationEnroller extends BaseContainerSharedTestCase {

    public static final Set REQUIRED_DATA_REPOSITORIES = new HashSet();

    static {
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.DICTIONARY_REPOSITORY);
        REQUIRED_DATA_REPOSITORIES.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
    }

    private IDictionary dictionary;
    private TestEnroller enroller;
    
    private static String DATA_DIR = System.getProperty("src.root.dir") + "/server/tools/enrollment/etc/";
    
    private static final String LDIF_DOMAIN  = "TestAgentEnrollment";
    private static String LDIF_PROPERTY_FILE  = DATA_DIR + "app.sample.default.def";
    private static String APP_LDIF_FILE  = DATA_DIR + "applications.ldif";
    
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestApplicationEnroller.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
        this.enroller = new TestEnroller();
        this.dictionary = ComponentManagerFactory.getComponentManager().getComponent(Dictionary.COMP_INFO);
	}

    /**
     * Clean all enrollments
     * @throws DictionaryException
     */
    public void testDeleteApplicationEnrollment() throws DictionaryException {
        IEnrollment enrollment = this.dictionary.getEnrollment(LDIF_DOMAIN);
        if (enrollment != null) {
			IConfigurationSession session = this.dictionary.createSession();
			session.beginTransaction();
			session.deleteEnrollment(enrollment);
			session.commit();
			session.close();
		}
    }
    
    /**
     * Enroll local domains
     * @throws IOException 
     * @throws DuplicateEnrollmentException 
     * @throws DictionaryException 
     * @throws InvalidConfigurationException 
     * @throws EntryNotFoundException 
     * @throws FileNotFoundException 
     * @throws EnrollmentFailedException 
     * @throws DuplicateEntryException 
     * @throws EnrollmentValidationException 
     * @throws EnrollerCreationException 
     * @throws Exception
     */
    public void testApplicationEnrollment() throws Exception {
        Map ldif_file1 = new HashMap(); 
        ldif_file1.put( LdifEnrollmentProperties.LDIF_NAME_PROPERTY, new String[] {APP_LDIF_FILE} );
        this.enroller.enroll(LDIF_DOMAIN, 
                new String[] {LDIF_PROPERTY_FILE},  
                EnrollmentTypeEnumType.LDIF,
                ldif_file1);
    }
	

    public void testApplications() throws Exception {
    	// make sure slapadd exists after enrollment
    	IElementType appType = this.dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
        IElementField fingerPrint = appType.getField(ApplicationReservedFieldEnumType.APP_FINGER_PRINT.getName());
        IDictionaryIterator<IMElement> slapadd = this.dictionary.query(
        		new Relation(RelationOp.EQUALS, fingerPrint, Constant.build("slapadd.exe:(null):1:0:837360")), 
                new Date(), null, null);

        try {
            assertTrue( slapadd.hasNext() );
        }
        finally {
            slapadd.close();
        }
        
        // make sure there are 26 apps enrolled
        IDictionaryIterator<IMElement> apps = this.dictionary.query( dictionary.condition(appType), new Date(), null, null);
        assertTrue( apps.hasNext() );
        int appCount = this.enroller.count(apps);
        assertTrue( appCount ==  26);
    }
    	
}
