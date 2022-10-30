/*
 * Created on Mar 13, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.common;

/**
 * When there is a duplicated column or enrollment  
 * 
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/common/DuplicateEntryException.java#1 $
 */

public class DuplicateEntryException extends EnrollmentException {

	private static final long serialVersionUID = 1L;

	public DuplicateEntryException(String type, String dulicatedEntry) {
        super(String.format("The %s, %s already exists. Please supply a correct value.", type,
                dulicatedEntry));
    }

}
