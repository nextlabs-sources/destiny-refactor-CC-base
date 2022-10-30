/*
 * Created on Mar 3, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.common;

import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import com.bluejungle.destiny.appframework.appsecurity.axis.AuthenticationContext;
import com.bluejungle.destiny.interfaces.secure_session.v1.SecureSessionServiceIF;
import com.bluejungle.destiny.services.secure_session.v1.SecureSessionServiceLocator;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

/**
 * This is the secure session manager implementation. This class allows a
 * standalone client to connect securely to a remote web service.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/common/src/java/main/com/bluejungle/destiny/tools/common/SecureSessionMgrImpl.java#1 $
 */

public class SecureSessionMgrImpl implements ISecureSessionMgr {

    /**
     * Constructor
     */
    public SecureSessionMgrImpl() {
        super();
    }

    public void login(URL location, String username, String password) throws SecureLoginException {
        AuthenticationContext authContext = AuthenticationContext.getCurrentContext();
        authContext.setUsername(username);
        authContext.setPassword(password);

        try {
            SecureSessionServiceLocator locator = new SecureSessionServiceLocator();
            locator.setSecureSessionServiceEndpointAddress(location.toString());
            SecureSessionServiceIF service = locator.getSecureSessionService();
            SecureSession s = service.initSession();
        } catch (ServiceException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } finally {
            // Clear the auth context to remove remnant of auth credentials
            //AuthenticationContext.clearCurrentContext();
        }
    }

    /**
     * @see com.bluejungle.destiny.tools.common.ISecureSessionMgr#logout()
     */
    public void logout() {
        AuthenticationContext.clearCurrentContext();
    }

}
