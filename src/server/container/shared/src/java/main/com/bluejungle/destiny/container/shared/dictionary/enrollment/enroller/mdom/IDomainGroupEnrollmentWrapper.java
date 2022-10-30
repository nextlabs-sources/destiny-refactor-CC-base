/*
* Created on Aug 27, 2012
*
* All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
* San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
* worldwide.
*
* @author dwashburn
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/mdom/IDomainGroupEnrollmentWrapper.java#1 $:
*/

/**
 * 
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollmentWrapper;

/**
 * @author dwashburn
 *
 */
public interface IDomainGroupEnrollmentWrapper extends IEnrollmentWrapper {

	String getConnectionFilename();
	String getDefinitionFilename();
	String getFilterFilename();
	
	String getDomainName();
	String[] getSubDomains();
	
}
