/*
 * Created on Mar 1, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment;

import java.util.Hashtable;

import com.bluejungle.destiny.tools.common.SecureClientSocketFactory;

/**
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/EnrollmentMgrSocketFactory.java#1 $
 */

public class EnrollmentMgrSocketFactory extends SecureClientSocketFactory {

    private static final String KEYSTORE_NAME = "/security/enrollment-keystore.jks";

    /**
     * Constructor
     * 
     * @param params
     */
    public EnrollmentMgrSocketFactory(Hashtable params) {
        super(params);
    }

    /**
     * @see com.bluejungle.destiny.tools.common.SecureClientSocketFactory#getKeystoreName()
     */
    public String getKeystoreName() {
        String enrollToolHome = System.getProperty(EnrollmentMgr.ENROLLMENT_TOOL_HOME);
        return enrollToolHome + KEYSTORE_NAME;
    }

	/* (non-Javadoc)
	 * @see com.bluejungle.destiny.tools.common.SecureClientSocketFactory#getPassword()
	 */
	@Override
	public String getPassword() {
		return System.getProperty(EnrollmentMgr.ENROLL_TOOL_KEYSTORE_PASSWORD, 
				super.getPassword()); // Get default password from superclass by default
	}
}
