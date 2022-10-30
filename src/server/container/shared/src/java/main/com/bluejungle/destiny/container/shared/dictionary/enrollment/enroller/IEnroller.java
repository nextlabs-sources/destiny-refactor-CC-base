/*
 * Created on Mar 27, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller;

import java.util.Map;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;

/**
 * This interface represents the enroller responsible for a given type of
 * enrollment source. The enroller is responsible for both the
 * full-synchronization as well as incremental synchronization (if supported).
 * It is the responsibility of the enroller to persist all the information
 * necessary for the above. Properties (string, number, binary) can be set on an
 * enrollment record for the same.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/IEnroller.java#1 $
 */

public interface IEnroller {

    /**
     * This method is invoked whenever an enrollment record is created, or
     * updated. It should validate that the enrollment definition/properties are
     * valid.
     * 
     * @param enrollment
     * @throws EnrollmentValidationException if the enrollment data is invalid
     * @throws DictionaryException if there is any dictionary error
     */
    void process(IEnrollment enrollment, Map<String, String[]> properties,
            IDictionary dictionary) throws EnrollmentValidationException, DictionaryException;

    /**
     * This is the synchronization method. A failure in enrollment should be
     * signalled by throwing an EnrollmentFailedException. It should be assumed
     * that the caller of this API might display the error message obtained via
     * calling getMessage() or getLocalizedMessage() on this exception object.
     * 
     * @param enrollment
     * @param dictionary
     * @throws EnrollmentValidationException Even the inforamtion is already check in the creation time. 
     * 				However, the information may be outdated.
     * @throws EnrollmentSyncException if any exception happens during sync
     * @throws DictionaryException if there is any dictionary error before or after the sync. 
     *              Don't try to throw this during the sync.
     */
    boolean sync(IEnrollment enrollment, IDictionary dictionary)
            throws EnrollmentValidationException, EnrollmentSyncException, DictionaryException;
    
    /**
     * 
     * @return a string that represent the enrollment type this enroller can handle
     */
   String getEnrollmentType();
}