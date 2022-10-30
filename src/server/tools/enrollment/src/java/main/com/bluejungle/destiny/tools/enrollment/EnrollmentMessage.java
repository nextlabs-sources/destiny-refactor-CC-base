/*
 * Created on Aug 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment;

import static com.bluejungle.destiny.tools.enrollment.EnrollmentMgrOptionDescriptorEnum.VERBOSE_OPTIONS_ID;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/EnrollmentMessage.java#1 $
 */

public interface EnrollmentMessage {
    String CHECK_PARAMETER = "Check the value of %s, parameter '-%s'.";
    
    String RUN_LIST = "You can run '" + EnrollmentMgrOptionDescriptorEnum.LIST_OPTIONS_ID.getName() 
            +"' command to display all enrollment.";
    
    String USE_VERBOSE_MODE = 
        "You can get extended detail error message thru verbose mode. The verbose mode can be switched on by by putting '-" + VERBOSE_OPTIONS_ID.getName() + "' in your command line arguments.";
    
    String CHECK_SERVER_LOG = "Check server log.";
    
    String CONTACT_ADMIN = "Contact administrator.";
    
}
