/*
 * Created on Mar 28, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.dictionary.IEnrollment;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/EnrollerFactoryImpl.java#1 $
 */

public class EnrollerFactoryImpl implements IEnrollerFactory {

    /**
     * Constructor
     *  
     */
    public EnrollerFactoryImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory#getEnrollmentProvider(null)
     */
    public IEnroller getEnroller(IEnrollment enrollment) throws EnrollerCreationException {
        if (enrollment == null) {
			throw new NullPointerException("enrollment is null");
		}
		String type = enrollment.getType();
		if (type == null) {
			throw new NullPointerException("Enrollment type is null");
		}
        
		final String commonErrorMesssage = "Some required libraries for enroller '" + type + "'";
		
        IEnroller enrollerToReturn;
        try {
            Class<?> enrollerClass = Class.forName(type);
            enrollerToReturn = (IEnroller) enrollerClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new EnrollerCreationException(commonErrorMesssage + " could not be located", e);
        } catch (IllegalAccessException e) {
            throw new EnrollerCreationException(commonErrorMesssage + " could not be accessed", e);
        } catch (InstantiationException e) {
            throw new EnrollerCreationException(commonErrorMesssage + " could not be instantiated", e);
        } catch (ClassCastException e) {
            throw new EnrollerCreationException("Enroller class '" + type 
            		+ "' does not respect enroller interface '" + IEnroller.class.getName() + "'");
        }
        return enrollerToReturn;
    }

}