/*
 * Created on Feb 28, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.dictionary.IEnrollment;

/**
 * This interface represents a factory for generating enroller instances for a
 * given enrollment. It is upto the discretion of the factory implementation to
 * either cache copies of the enroller, or generate a new instance each time.
 * 
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/IEnrollerFactory.java#1 $
 */

public interface IEnrollerFactory {

    public IEnroller getEnroller(IEnrollment enrollment) throws EnrollerCreationException;
}