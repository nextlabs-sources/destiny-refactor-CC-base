/*
 * Created on Mar 3, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.service.axis;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.securesession.ISecureSession;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/securesession/service/axis/AuthenticationResponseFlowHandler.java#1 $
 */

public class AuthenticationResponseFlowHandler extends BasicHandler {

    private static final Log LOG = LogFactory.getLog(AuthenticationResponseFlowHandler.class.getName());
    
    private static final String SECURE_SESSION_TYPES_PREFIX = "ss";
        
    /**
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        ISecureSession currentSecureSession = (ISecureSession)msgContext.getProperty(AuthenticationHandlerConstants.SECURE_SESSION_PROPERTY_NAME);
        if (currentSecureSession != null) {
            try {
                addSecureSessionHeader(msgContext, currentSecureSession);
            } catch (AxisFault axisFault) {
                LOG.error("Failed to add secure session key.  Client session may time out.", axisFault);
            } catch (SOAPException exception) {
                LOG.error("Failed to add secure session key.  Client session may time out.", exception);
            }
        }
    }

    /**
     * Add the secure session header to the response messages
     * 
     * @param msgContext
     * @param currentSecureSession
     * @throws SOAPException
     * @throws AxisFault
     */
    private void addSecureSessionHeader(MessageContext msgContext, ISecureSession currentSecureSession) throws AxisFault, SOAPException {
        SOAPEnvelope soapEnvelope = msgContext.getResponseMessage().getSOAPEnvelope();        
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        
        Name secureSessionElementName = soapEnvelope.createName(AuthenticationHandlerConstants.SECURE_SESSION_HEADER_ELEMENT_NAME, SECURE_SESSION_TYPES_PREFIX, AuthenticationHandlerConstants.SECURE_SESSION_TYPE_NAMESPACE); 
        SOAPHeaderElement secureSessionElement = soapHeader.addHeaderElement(secureSessionElementName);
        
        Name secureSessionKeyElementName = soapEnvelope.createName(AuthenticationHandlerConstants.SECURE_SESSION_KEY_ELEMENT_NAME);
        SOAPElement secureSessionKeyElement = secureSessionElement.addChildElement(secureSessionKeyElementName);
        secureSessionKeyElement.addTextNode(currentSecureSession.generateKey());
    }
}
