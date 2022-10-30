/*
 * Created on April 24, 2006
 *
 * All sources, binaries and HTML pages (C) copyright 2006 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.dictionary.tools;


import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IMElementType;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.datastore.hibernate.seed.seedtasks.SeedDataTaskBase;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/dictionary/src/java/main/com/bluejungle/dictionary/tools/EnrollmentSeedDataTask.java#1 $
 */


/**
 * This class is responsible for inserting seed data into dictionary for external data enrollment
 *
 */
public class EnrollmentSeedDataTask extends SeedDataTaskBase {
	private EnrollmentSharedSeedTask enrollmentSharedSeedTask;
	
	/**
     * @see com.bluejungle.destiny.tools.dbinit.seedtasks.SeedDataTaskBase#init()
     */
	@Override
    public void init() {
        super.init();
        enrollmentSharedSeedTask = new EnrollmentSharedSeedTask(); 
        enrollmentSharedSeedTask.init();
    }
	
    /**
     * @see com.bluejungle.destiny.tools.dbinit.ISeedDataTask#execute()
     */
    public void execute() throws SeedDataTaskException {
        getLog().trace("Invoking the enrollment seed data importer...");
        if ( enrollmentSharedSeedTask.getDictionary() == null ) {
            throw new SeedDataTaskException("Failed to add seed data for enrollment, dictionary is not setup!");
        }
        try {
            addEnrollmentSeedData();
        } catch (DictionaryException e) {
            throw new SeedDataTaskException("Failed to add seed data for enrollment, reason:" + e);
        }
        getLog().trace("Enrollment seed data import completed successfully");
    }

    /**
     * Add seed enrollment data for user type, computer type and aplication type
     *
     * @throws DictionaryException
     */
    private void addEnrollmentSeedData() throws DictionaryException {
        // add seed data for User type
        IMElementType userType = enrollmentSharedSeedTask.createUserType();

        // add seed data for Contact type
        IMElementType contactType = enrollmentSharedSeedTask.createContactType();
        // add seed data for computer type
        IMElementType hostType = enrollmentSharedSeedTask.createHostType();

        // add seed data for application type
        IMElementType appType = enrollmentSharedSeedTask.createApplicationType();
        
        // add seed data for site type
        IMElementType siteType = enrollmentSharedSeedTask.createSiteType();

        // add seed data for site type
        IMElementType clientInfoType = enrollmentSharedSeedTask.createClientInfoType();
        
        enrollmentSharedSeedTask.save(
        		userType, 
        		contactType, 
        		hostType, 
        		appType, 
        		siteType,
				clientInfoType);
       
    }
}
