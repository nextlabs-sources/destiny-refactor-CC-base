/*
 * Created on Mar 16, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment;

import java.net.ConnectException;
import java.net.UnknownHostException;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/EnrollmentMgrException.java#1 $
 */

public class EnrollmentMgrException extends Exception {
    public EnrollmentMgrException(String msg) {
        super(msg);
    }

    public EnrollmentMgrException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EnrollmentMgrException(com.bluejungle.destiny.types.secure_session.v1.AccessDeniedFault fault) {
        this("The username or password is not correct.", fault);
    }
    
    public EnrollmentMgrException(com.bluejungle.destiny.framework.types.UnauthorizedCallerFault fault) {
        this("Failed to authorize.", fault);
    }
    
    public EnrollmentMgrException(com.bluejungle.destiny.framework.types.ServiceNotReadyFault fault) {
        this("Service is not ready.", fault);
    }
    
    public EnrollmentMgrException(com.bluejungle.destiny.services.enrollment.types.DictionaryFault fault) {
        this(fault.getMsg() + "  Dictionary database problem. Please contact your system administration for more information.", fault);
    }
    
    public EnrollmentMgrException(com.bluejungle.destiny.services.enrollment.types.EnrollmentInternalFault fault) {
        this(fault.getMsg(), fault);
    }
    
    public EnrollmentMgrException(com.bluejungle.destiny.services.enrollment.types.NotFoundFault fault) {
        this(fault.getMsg(), fault);
    }
    
    public EnrollmentMgrException(com.bluejungle.destiny.services.enrollment.types.DuplicatedFault fault) {
        this(fault.getMsg(), fault);
    }
    
    public EnrollmentMgrException(com.bluejungle.destiny.services.enrollment.types.InvalidConfigurationFault fault) {
        this(fault.getMsg() + "  Check configuration.", fault);
    }
    
    public EnrollmentMgrException(com.bluejungle.destiny.services.enrollment.types.EnrollmentFailedFault fault) {
        this(fault.getMsg(), fault);
    }
    
    public static EnrollmentMgrException create(java.rmi.RemoteException fault) {
        Throwable cause = fault.getCause();
        if (cause instanceof UnknownHostException ) {
            return new EnrollmentMgrException("Unknown Host: " + cause.getMessage() + ".  Check parameters.", fault);
        } else if (cause instanceof ConnectException) {
            return new EnrollmentMgrException ("Cannot connect to Policy Server.  Make sure it is running, and check parameters.", fault);
        } else {        
            return new EnrollmentMgrException("Remote Exception: " + fault.getMessage(), fault);
        }
    }
    
}
