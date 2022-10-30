/*
 * Created on Apr 26, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl;

import java.util.Iterator;
import java.util.Map;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/dictionary/enrollment/controller/defaultimpl/MockEnrollerFactoryImpl.java#1 $
 */

public class MockEnrollerFactoryImpl implements IEnrollerFactory {
	static long processDelay = 0;
	static long syncDelay = 0;
	
    /**
     * Constructor
     *  
     */
    public MockEnrollerFactoryImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory#getEnroller(com.bluejungle.dictionary.IEnrollment)
     */
    public IEnroller getEnroller(IEnrollment enrollment) throws EnrollerCreationException {
        return new MockEnrollerImpl();
    }

    private class MockEnrollerImpl implements IEnroller {
    	
        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnroller#process(com.bluejungle.dictionary.IEnrollment,
         *      java.util.Map, com.bluejungle.dictionary.IDictionary)
         */
        public void process(IEnrollment enrollment, Map properties, IDictionary dictionary) throws EnrollmentValidationException {
            if(properties!=null) {
                for (Iterator iter = properties.keySet().iterator(); iter.hasNext();) {
                    String key = (String) iter.next();
                    String[] value = (String[]) properties.get(key);
                    enrollment.setStrArrayProperty(key, value);
                }
            }
            try {
				Thread.sleep(processDelay);
			} catch (InterruptedException e) {
				System.err.println(e);
				throw new EnrollmentValidationException(e);
			}
        }

        /**
         * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnroller#sync(com.bluejungle.dictionary.IEnrollment,
         *      com.bluejungle.dictionary.IDictionary)
         */
        public boolean sync(IEnrollment enrollment, IDictionary dictionary) throws EnrollmentValidationException, EnrollmentSyncException {
        	try {
				Thread.sleep(syncDelay);
			} catch (InterruptedException e) {
				System.err.println(e);
				return false;
			}
        	return true;
        }

        public String getEnrollmentType() {
            return "mock";
        }
    }

}