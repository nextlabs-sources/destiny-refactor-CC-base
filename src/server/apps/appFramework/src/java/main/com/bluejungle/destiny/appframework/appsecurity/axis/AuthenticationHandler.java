/*
 * Created on Feb 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.appsecurity.axis;

import java.util.Iterator;

import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.bluejungle.destiny.types.basic.v1.Properties;
import com.bluejungle.destiny.types.basic.v1.Property;
import com.bluejungle.destiny.types.basic.v1.PropertyValues;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;

/**
 * The AuthenticationHandler is an Axis Handler which handles authentication and
 * secure session responsiblities for destiny applications. During a request, is
 * will add either authentication information, secure session information, or
 * both to the SOAP Header. During a response, it will look for a secure session
 * in the response SOAP Header and store it in the Secure Session Context for
 * retrieval on subsequent requests.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/apps/inquiryCenter/src/java/main/com/bluejungle/destiny/inquirycenter/security/axis/AuthenticationHandler.java#1 $
 */

public class AuthenticationHandler extends BasicHandler {

    private static final String SECURE_SESSION_HEADER_ELEMENT_NAME = "secureSession";
    private static final String SECURE_SESSION_TYPES_PREFIX = "ss";
    private static final String SECURE_SESSION_TYPE_NAMESPACE = "http://com.bluejungle/destiny/FIXME";
    private static final String SECURE_SESSION_KEY_ELEMENT_NAME = "key";
    private static final String WS_SECURITY_TYPES_PREFIX = "wsse";
    private static final String WS_SECURITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String WS_SECURITY_HEADER_ELEMENT_NAME = "Security";
    private static final String WS_SECURITY_USERNAME_TOKEN_ELEMENT_NAME = "UsernameToken";
    private static final String WS_SECURITY_USERNAME_ELEMENT_NAME = "Username";
    private static final String WS_SECURITY_PASSWORD_ELEMENT_NAME = "Password";
    private static final String SECURE_SESSION_PROPERTY_ELEMENT_NAME = "property";
    private static final String SECURE_SESSION_PROPERTY_VALUE_ELEMENT_NAME = "value";

    /**
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext messageContext) throws AxisFault {

        if (!messageContext.getPastPivot()) {
            try {
                provideSecureSessionCredentials(messageContext);
                provideAuthenticationCredentials(messageContext);
            } catch (SOAPException exception) {
                throw new AxisFault("Failed to handle authentication request", exception);
            }
        } else {
            try {
                handleSecureSessionResponse(messageContext);
            } catch (SOAPException exception) {
                throw new AxisFault("Failed to handle authentication response", exception);
            }
        }
    }

    /**
     * Provide a secure session if the request SOAP Header is a secure session
     * exists in the current context
     * 
     * @param messageContext
     * @throws AxisFault
     * @throws SOAPException
     */
    private void provideSecureSessionCredentials(MessageContext messageContext) throws AxisFault, SOAPException {
        SecureSession currentSession = SecureSessionVaultGateway.getSecureSession();
        if (currentSession != null) {
            SOAPEnvelope soapEnvelope = messageContext.getRequestMessage().getSOAPEnvelope();
            SOAPHeader soapHeader = soapEnvelope.getHeader();

            Name secureSessionElementName = soapEnvelope.createName(SECURE_SESSION_HEADER_ELEMENT_NAME, SECURE_SESSION_TYPES_PREFIX, SECURE_SESSION_TYPE_NAMESPACE);
            SOAPHeaderElement secureSessionElement = soapHeader.addHeaderElement(secureSessionElementName);

            Name secureSessionKeyElementName = soapEnvelope.createName(SECURE_SESSION_KEY_ELEMENT_NAME);
            SOAPElement secureSessionKeyElement = secureSessionElement.addChildElement(secureSessionKeyElementName);
            secureSessionKeyElement.addTextNode(currentSession.getKey());
        }
    }

    /**
     * Provide authentication credentials if they exist in the current
     * Authentication context
     * 
     * @param messageContext
     * @throws AxisFault
     */
    private void provideAuthenticationCredentials(MessageContext messageContext) throws AxisFault, SOAPException {
        AuthenticationContext currentContext = AuthenticationContext.getCurrentContext();
        String username = currentContext.getUsername();
        String password = currentContext.getPassword();

        if ((username != null) && (!username.equals(""))) {
            SOAPEnvelope soapEnvelope = messageContext.getRequestMessage().getSOAPEnvelope();
            SOAPHeader soapHeader = soapEnvelope.getHeader();

            Name securityElementName = soapEnvelope.createName(WS_SECURITY_HEADER_ELEMENT_NAME, WS_SECURITY_TYPES_PREFIX, WS_SECURITY_NAMESPACE);
            SOAPHeaderElement securityElement = soapHeader.addHeaderElement(securityElementName);
            securityElement.setMustUnderstand(true);
            securityElement.setActor(""); // FIX ME - Is this correct? No.
            // Change after removing wss4j

            Name usernameTokenElementName = soapEnvelope.createName(WS_SECURITY_USERNAME_TOKEN_ELEMENT_NAME, WS_SECURITY_TYPES_PREFIX, WS_SECURITY_NAMESPACE);
            SOAPElement usernameTokenElement = securityElement.addChildElement(usernameTokenElementName);

            Name usernameElementName = soapEnvelope.createName(WS_SECURITY_USERNAME_ELEMENT_NAME, WS_SECURITY_TYPES_PREFIX, WS_SECURITY_NAMESPACE);
            SOAPElement usernameElement = usernameTokenElement.addChildElement(usernameElementName);
            usernameElement.addTextNode(username);

            Name passwordElementName = soapEnvelope.createName(WS_SECURITY_PASSWORD_ELEMENT_NAME, WS_SECURITY_TYPES_PREFIX, WS_SECURITY_NAMESPACE);
            SOAPElement passwordElement = usernameTokenElement.addChildElement(passwordElementName);
            passwordElement.addTextNode(password);
        }
    }

    /**
     * Process the response soap header if it contains a secure session
     * 
     * @param messageContext
     * @throws AxisFault
     * @throws SOAPException
     */
    private void handleSecureSessionResponse(MessageContext messageContext) throws AxisFault, SOAPException {
        SOAPEnvelope soapEnvelope = messageContext.getResponseMessage().getSOAPEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        Name secureSessionElementName = soapEnvelope.createName(SECURE_SESSION_HEADER_ELEMENT_NAME, "", SECURE_SESSION_TYPE_NAMESPACE);
        Iterator secureSessionElements = soapHeader.getChildElements(secureSessionElementName);

        // Assume only one secure session header element
        if (secureSessionElements.hasNext()) {
            SOAPHeaderElement secureSessionElement = (SOAPHeaderElement) secureSessionElements.next();
            // Currently, the secure session has two child element - the key and
            // properties
            String secureSessionKey = extractSecureSessionKey(secureSessionElement);
            Properties secureSessionProperties = extractSecureSessionProperties(secureSessionElement);
            SecureSessionVaultGateway.setSecureSession(new SecureSession(secureSessionKey, secureSessionProperties));
        }
    }

    /**
     * Extract the secure session key from the Secure Session Soap Header
     * Element
     * 
     * @param secureSessionElement
     */
    private String extractSecureSessionKey(SOAPHeaderElement secureSessionElement) {
        Node secureSessionKeyElement = (Node) secureSessionElement.getFirstChild();
        
        return secureSessionKeyElement.getValue();
    }

    /**
     * Extract the secure session properties from the Secure Session Soap Header
     * Element
     * 
     * @param secureSessionElement
     */
    private Properties extractSecureSessionProperties(SOAPHeaderElement secureSessionElement) {
        NodeList propertyElements = secureSessionElement.getElementsByTagName(SECURE_SESSION_PROPERTY_ELEMENT_NAME);
        Property[] propertiesArray = new Property[propertyElements.getLength()];
        for (int i = 0; i < propertyElements.getLength(); i++) {
            org.w3c.dom.Node nextPropertyNode = propertyElements.item(i);
            org.w3c.dom.Node propertyNameNode = nextPropertyNode.getFirstChild();
            String propertyName = propertyNameNode.getNodeValue();
            NodeList propertyValues = ((Element) nextPropertyNode).getElementsByTagName(SECURE_SESSION_PROPERTY_VALUE_ELEMENT_NAME);
            String[] propertyValuesArray = new String[propertyValues.getLength()];
            for (int j = 0; j < propertyValues.getLength(); j++) {
                org.w3c.dom.Node valueNode = propertyValues.item(j);
                propertyValuesArray[j] = valueNode.getNodeValue();
            }

            PropertyValues nextPropertyValues = new PropertyValues(propertyValuesArray);
            propertiesArray[i] = new Property(propertyName, nextPropertyValues);
        }

        return new Properties(propertiesArray);
    }
}