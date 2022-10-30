/**
 * SecureSessionServiceV1Impl.java
 * 
 * This file was auto-generated from WSDL by the Apache Axis 1.2RC1 Sep 29, 2004
 * (08:29:40 EDT) WSDL2Java emitter.
 */

package com.bluejungle.destiny.container.shared.securesession.service;

import java.util.Enumeration;
import java.util.Properties;

import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.securesession.ISecureSession;
import com.bluejungle.destiny.container.shared.securesession.service.axis.AuthenticationHandlerConstants;
import com.bluejungle.destiny.types.basic.v1.Property;
import com.bluejungle.destiny.types.basic.v1.PropertyValues;
import com.bluejungle.destiny.types.secure_session.v1.AccessDeniedFault;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

/**
 * Implementation of the Secure Session Web Service
 * 
 * @author sgoldstein
 */
public class SecureSessionServiceV1Impl implements com.bluejungle.destiny.interfaces.secure_session.v1.SecureSessionServiceIF {

    private static final Log LOG = LogFactory.getLog(SecureSessionServiceV1Impl.class.getName());

    /**
     * Intialize a secure session
     */
    public SecureSession initSession() throws java.rmi.RemoteException {
        return retrieveCreatedSession();
    }

    /**
     * Retrieve the session created by the authentication handler
     * 
     * @return the secure session created by the authentication handler
     * @throws AccessDeniedFault
     */
    private SecureSession retrieveCreatedSession() throws AccessDeniedFault {
        // Retrieve the username from the service request
        MessageContext currentContext = MessageContext.getCurrentContext();
        ISecureSession currentSecureSession = (ISecureSession) currentContext.getProperty(AuthenticationHandlerConstants.SECURE_SESSION_PROPERTY_NAME);
        if (currentSecureSession == null) {
            LOG.debug("Current session not found in message context.  Handler may not be properly configured.");
            throw new AccessDeniedFault();
        }

        Properties secureSessionProperties = currentSecureSession.getProperties();
        Property[] propertyDTOArray = new Property[secureSessionProperties.size()];
        Enumeration propertyNames = secureSessionProperties.propertyNames();
        for (int i = 0; propertyNames.hasMoreElements(); i++) {
            String nextPropertyName = (String) propertyNames.nextElement();
            String[] propertyValue = { secureSessionProperties.getProperty(nextPropertyName) };
            PropertyValues propertyDTOValues = new PropertyValues(propertyValue);
            propertyDTOArray[i] = new Property(nextPropertyName, propertyDTOValues);
        }
        
        String secureSessionKey = currentSecureSession.generateKey();
        
        return new SecureSession(secureSessionKey, new com.bluejungle.destiny.types.basic.v1.Properties(propertyDTOArray));
    }
}
