/*
 * Created on Feb 22, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller;

import java.util.Map;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentThreadException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * This wrapper interface controls enrollment activity - full-synch + change
 * tracking
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/controller/IEnrollmentController.java#1 $
 */

public interface IEnrollmentController {
    PropertyKey<String> MAIL_HANDLER_NAME = new PropertyKey<String>("EnrollmentMailHandler");
    
    /**
     * The purpose of this method is to simplify obtaining the enroller that
     * will validate the enrollment for the calling web-service. This method
     * should validate a newly created enrollment (before it has been
     * persisted).
     * 
     * @param enrollment
     * @param propertiesMap
     * @throws EnrollerCreationException if the enroller can't be created
     * @throws EnrollmentValidationException if the enrollment contains invalid information
     */
    public void process(IEnrollment enrollment, Map<String, String[]> propertiesMap)
            throws EnrollmentValidationException, EnrollerCreationException,
            EnrollmentThreadException, DictionaryException;
        
    /**
     * remove a recurring enrollment from the control list
     * @throws EntryNotFoundException if the enrollment doesn't exist
     */
    public void deleteEnrollmentThread(IEnrollment enrollment) throws EntryNotFoundException,
            EnrollmentThreadException;
    
    /**
     * Sync the enrollment 
     * @param enrollment
     * @throws EnrollmentFailedException if anything wrong during the enrollment sync
     */
    public void sync(IEnrollment enrollment) throws EnrollmentValidationException,
            EnrollmentSyncException, EnrollmentThreadException, DictionaryException;
}