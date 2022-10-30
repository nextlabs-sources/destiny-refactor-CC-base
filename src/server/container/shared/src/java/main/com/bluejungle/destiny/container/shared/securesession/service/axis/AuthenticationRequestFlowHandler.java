/*
 * Created on Feb 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.securesession.service.axis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticatedUser;
import com.bluejungle.destiny.container.shared.securesession.ISecureSession;
import com.bluejungle.destiny.container.shared.securesession.ISecureSessionManager;
import com.bluejungle.destiny.container.shared.securesession.SecureSessionPersistenceException;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.types.secure_session.v1.AccessDeniedFault;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;

/**
 * Request flow handler responsible for reading and processing the security
 * headers required for web services calls to DAC. These headers can either
 * contain a WS Security compliant username token (non hashed password) or a
 * secure session token
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/securesession/service/axis/AuthenticationRequestFlowHandler.java#1 $
 */
// FIX ME - Should eventually be refactored into multiple classes. Also, should
// be modified to account for additional WS Security tokens
public class AuthenticationRequestFlowHandler extends BasicHandler {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log LOG = LogFactory.getLog(AuthenticationRequestFlowHandler.class);
    private static final String CLIENT_APPLICATION_OPTION_NAME = "clientApplication";
    
    private static final String CLIENT_APP_ADMINISTRATOR = "Management Console";
	private static final String CLIENT_APP_POLICY_AUTHOR = "Policy Author";
	private static final String CLIENT_APP_REPORTER= "Inquiry Center";
	private static final String CLIENT_APP_REPORTER_ADMIN = "Inquiry Center Admin";
	private static final String CLIENT_APP_REPORTER_USER = "Inquiry Center User";

    private LifecycleManager lifecycleManager;
    private String clientApplication;
    
    private Log AUDIT_LOG;

    /**
     * @see org.apache.axis.Handler#init()
     */
    public void init() {
        super.init();
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        this.lifecycleManager = componentManager.getComponent(LifecycleManager.COMP_INFO);

        this.clientApplication = (String) getOption(CLIENT_APPLICATION_OPTION_NAME);
        if (this.clientApplication == null) {
            throw new IllegalStateException("Client application not specified.");
        }
        
        String auditLoggerName = "com.nextlabs.audit.AuthenticationRequestFlowHandler." 
            + clientApplication.toLowerCase().replaceAll("\\s+", "_");
        
        AUDIT_LOG = LogFactory.getLog(auditLoggerName);
        
        if (AUDIT_LOG.isInfoEnabled()) {
            LOG.info("audit is on for " + auditLoggerName);
        } else {
            LOG.info("audit is off for " + auditLoggerName);
        }
    }

    /**
     * @see org.apache.axis.Handler#invoke(org.apache.axis.MessageContext)
     */
    public void invoke(MessageContext msgContext) throws AxisFault {
        boolean passedAuthenticationCheckpoint;
        try {
            boolean passedSessionCheckpoint = isAllowedThroughSessionCheckPoint(msgContext);
            if (passedSessionCheckpoint) {
                passedAuthenticationCheckpoint = true;
            } else {
                passedAuthenticationCheckpoint = isAllowedThroughAuthenticationCheckpoint(msgContext);
            }
        } catch (SOAPException exception) {
            throw new AxisFault("Failed to parse SOAP message", exception);
        }
        
        if (!passedAuthenticationCheckpoint) {
            throw new AccessDeniedFault();
        }
    }

    /**
     * Look for authentication credentials (username/password) and determine if
     * the credentials are valid
     * 
     * @param msgContext
     * @throws AxisFault
     * @throws SOAPException
     * @throws EntityManagementException
     * @throws PQLException
     */
    private boolean isAllowedThroughAuthenticationCheckpoint(MessageContext msgContext) throws AxisFault, SOAPException {
        boolean isValidAuthentication = false;

        SOAPElement usernameTokenElement = findUsernameTokenNode(msgContext);
        if (usernameTokenElement != null) {
            final String username = extractUsername(usernameTokenElement);
            final String password = extractPassword(usernameTokenElement);

            IAuthenticatedUser authenticatedUser = null;
            
            try {
                getLog().trace("Attempting authentication for user '" + username + "'");
                IApplicationUserManager userManager = getApplicationUserManager();

                // Authenticate user:
                authenticatedUser = userManager.authenticateUser(username, password);
                boolean hasClientAccess;
                if (authenticatedUser.getLogin().equals(IApplicationUserManager.SUPER_USER_USERNAME)) {
                    // The Super-User can always log-in to the application:
                    hasClientAccess = true;
                } else {
                    // For all other users, we check the roles permissions:
                    getLog().trace("Checking roles permission for user '" + authenticatedUser.getUniqueName() + "'");
                    hasClientAccess = isAuthorizedForClientApplication(authenticatedUser);
                    getLog().trace("Roles permission check for user '" + username + "' returned " + hasClientAccess);
                }
                if (hasClientAccess) {
                    Properties properties = new Properties();
                    properties.setProperty(ISecureSession.USERNAME_PROP_NAME, username);
                    properties.setProperty(ISecureSession.PRINCIPALNAME_PROP_NAME, authenticatedUser.getUniqueName());
                    properties.setProperty(ISecureSession.ID_PROP_NAME, authenticatedUser.getDestinyId().toString());
                    properties.setProperty(ISecureSession.CAN_CHANGE_PASSWORD, 
                            String.valueOf(userManager.canChangePassword(authenticatedUser)));
                    ISecureSession secureSession = getSecureSessionManager().createSession(properties);
                    setSessionForCall(secureSession, msgContext);
                    isValidAuthentication = true;
                }
            } catch (AuthenticationFailedException exception) {
                getLog().debug("Authentication failed.  Invalid username/password.");
            } catch (UserManagementAccessException e) {
                getLog().debug("Could not access the user management system." 
                        + " Perhaps the directory server is down or temporarily inaccessible." 
                        + " Please contact the System Administrator and/or try again later.");
            } catch (PQLException exception) {
                getLog().trace("PQLException thrown when checking roles permission for '" + username + "'", exception);
                /**
                 * FIX ME - Reusing AxisFault not ideal here
                 */
                throw new AxisFault("Failed to verify application access for authenticating user.", exception);
            } catch (EntityManagementException exception) {
                getLog().trace("EntityManagementException thrown when checking roles permission for '" + username + "'", exception);
                /**
                 * FIX ME - Reusing AxisFault not ideal here
                 */
                throw new AxisFault("Failed to verify application access for authenticating user.", exception);
            } catch (SecureSessionPersistenceException exception) {
                getLog().trace("SecureSessionPersistenceException thrown when authenticating '" + username + "'", exception);
                /**
                 * FIX ME - Reusing AxisFault not ideal here
                 */
                throw new AxisFault("Failed to create secure session", exception);
            } finally {
                if (AUDIT_LOG.isInfoEnabled()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(isValidAuthentication ? "LOGIN_SUCCESS: " : "LOGIN_FAIL: ")
                      .append("app = " + clientApplication)
                      .append(", username = " + username);
//                    
                    Long userId = null;
                    
                    if (authenticatedUser != null) {
                        userId = authenticatedUser.getDestinyId();
                        sb.append(", userid = " + userId);
                    }
                    
                    String source = (String)msgContext.getProperty(Constants.MC_REMOTE_ADDR);
                    sb.append(", remote-address = " + source);
                    
                    AUDIT_LOG.info(sb.toString());
                }
            }
        } else {
            getLog().warn("Authenticatin request flow handler could not extract user name token element from message");
        }

        return isValidAuthentication;
    }

    /**
     * Determine is a Secure Session is present in the soap header and if it is
     * valid
     * 
     * @param msgContext
     * @return
     * @throws ServiceNotReadyFault
     * @throws SOAPException
     */
    private boolean isAllowedThroughSessionCheckPoint(MessageContext msgContext)
            throws ServiceNotReadyFault, SOAPException, AxisFault {
        boolean valueToReturn = false;

        ISecureSessionManager secureSessionManager = getSecureSessionManager();

        SOAPEnvelope soapEnvelope = msgContext.getRequestMessage().getSOAPEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        Name secureSessionElementName = soapEnvelope.createName(
                AuthenticationHandlerConstants.SECURE_SESSION_HEADER_ELEMENT_NAME
              , ""
              , AuthenticationHandlerConstants.SECURE_SESSION_TYPE_NAMESPACE
        );
        Iterator secureSessionElements = soapHeader.getChildElements(secureSessionElementName);

        // Assume only one secure session header element
        if (secureSessionElements.hasNext()) {
            SOAPHeaderElement secureSessionElement = (SOAPHeaderElement) secureSessionElements.next();

            // Currently, the secure session has one child element - the key
            javax.xml.soap.Node secureSessionKeyElement = (javax.xml.soap.Node) secureSessionElement.getFirstChild();
            if (secureSessionKeyElement != null) {
                String secureSessionKey = secureSessionKeyElement.getValue();
                if (secureSessionKey != null) {
                    try {
                        ISecureSession secureSession = secureSessionManager.getSessionByKey(secureSessionKey);
                        if (secureSession != null) {
                            valueToReturn = true;
                            setSessionForCall(secureSession, msgContext);
                        }
                    } catch (SecureSessionPersistenceException exception) {
                        getLog().error("Error while retrieving secure session.  User request may be denied.", exception);
                    }
                }
            }

            // Detach Secure Session Header Element
            secureSessionElement.detachNode();
        }

        return valueToReturn;
    }

    /**
     * If a valid username token node is present, find it and return it.
     * Otherwise, return null
     * 
     * @param msgContext
     *            the message context for the current web service call
     * @return the username token node or null if it is not pressent in the
     *         current message
     * @throws AxisFault
     * @throws SOAPException
     */
    private SOAPElement findUsernameTokenNode(MessageContext msgContext) throws AxisFault, SOAPException {
        SOAPElement usernameTokenToReturn = null;

        SOAPEnvelope soapEnvelope = msgContext.getRequestMessage().getSOAPEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();
        Name securityElementName = soapEnvelope.createName(
                AuthenticationHandlerConstants.WS_SECURITY_HEADER_ELEMENT_NAME
              , ""
              , AuthenticationHandlerConstants.WS_SECURITY_NAMESPACE
        );
        Iterator<SOAPHeaderElement> securityElements = soapHeader.getChildElements(securityElementName);
        while (securityElements.hasNext()) {
            SOAPHeaderElement securityElement = securityElements.next();
            // FIX ME - Check actor

            /**
             * Is this the best place to set it as processed? Probably not, but
             * we're going to read it and we won't have a reference to it later
             */
            ((org.apache.axis.message.SOAPHeaderElement) securityElement).setProcessed(true);

            Name usernameTokenElementName = soapEnvelope.createName(
                    AuthenticationHandlerConstants.WS_SECURITY_USERNAME_TOKEN_ELEMENT_NAME
                  , ""
                  , AuthenticationHandlerConstants.WS_SECURITY_NAMESPACE
            );
            Iterator usernameTokenElements = securityElement.getChildElements(usernameTokenElementName);
            if (usernameTokenElements.hasNext()) {
                usernameTokenToReturn = (SOAPElement) usernameTokenElements.next();
            }
        }

        return usernameTokenToReturn;
    }

    /**
     * Returns the log object
     * 
     * @return the log object
     */
    protected Log getLog() {
        return LOG;
    }

    /**
     * Extract username from username token
     * 
     * @param usernameTokenNode
     *            the username token from which to extract the username
     * @return the extracted username of null if one if not present
     */
    private String extractUsername(SOAPElement usernameTokenNode) {
        if (usernameTokenNode == null) {
            throw new NullPointerException("usernameTokenNode cannot be null.");
        }

        String usernameToReturn = null;

        NodeList usernameNodes = usernameTokenNode.getElementsByTagNameNS(
                AuthenticationHandlerConstants.WS_SECURITY_NAMESPACE
              , AuthenticationHandlerConstants.WS_SECURITY_USERNAME_ELEMENT_NAME);
        Node usernameNode = usernameNodes.item(0);
        if (usernameNode != null) {
            Node usernameTextNode = usernameNode.getLastChild();
            if (usernameTextNode != null) {
                usernameToReturn = usernameTextNode.getNodeValue();
            }
        }

        return usernameToReturn;
    }

    /**
     * Extract password from username token
     * 
     * @param usernameTokenNode
     *            the username token from which to extract the password
     * @return the extracted password or null if one if not present
     */
    private String extractPassword(SOAPElement usernameTokenNode) {
        if (usernameTokenNode == null) {
            throw new NullPointerException("usernameTokenNode cannot be null.");
        }

        String passwordToReturn = null;

        NodeList passwordNodes = usernameTokenNode.getElementsByTagNameNS(
                AuthenticationHandlerConstants.WS_SECURITY_NAMESPACE
              , AuthenticationHandlerConstants.WS_SECURITY_PASSWORD_ELEMENT_NAME);
        Node passwordNode = passwordNodes.item(0);
        if (passwordNode != null) {
            Node passwordTextNode = passwordNode.getLastChild();
            if (passwordTextNode != null) {
                passwordToReturn = passwordTextNode.getNodeValue();
            }
        }

        return passwordToReturn;
    }

    /**
     * Determine if the specified LDAP user is authorized for the configured
     * client application
     * 
     * @param authenticatedUser
     * @return true if the user is authorized; false otherwise
     * @throws EntityManagementException
     * @throws PQLException
     */
    private boolean isAuthorizedForClientApplication(
            IAuthenticatedUser authenticatedUser)
            throws EntityManagementException, PQLException {
        IDSubject userSubject = new Subject(
                null
              , authenticatedUser.getUniqueName()
              , authenticatedUser.getFirstName() + " " + authenticatedUser.getLastName()
              , authenticatedUser.getDestinyId()
              , SubjectType.USER);

        List<DevelopmentEntity> appEntities = getComponent(this.clientApplication);
        boolean isAuthorizedForClientApplication = false;
        
		for (DevelopmentEntity appEntity : appEntities) {
			getLog().trace(
					"Checking if '" + authenticatedUser.getUniqueName()
							+ "' has access to '" + this.clientApplication
							+ "'");
			DomainObjectBuilder appObjectBuilder = new DomainObjectBuilder(
					appEntity.getPql());
			IDSpec appSpec = (SpecBase) appObjectBuilder.processSpec();

			DomainObjectBuilder accessPolicyBuilder = new DomainObjectBuilder(
					appEntity.getApPql());
			AccessPolicy accessPolicy = (AccessPolicy) accessPolicyBuilder
					.processAccessPolicy();
			appSpec.setAccessPolicy(accessPolicy);

			// We don't need id, real name for the purposes of ownership
			appSpec.setOwner(new Subject(appEntity.getOwner().toString(),
					appEntity.getOwner().toString(), appEntity.getOwner()
							.toString(), appEntity.getOwner(), SubjectType.USER));

			isAuthorizedForClientApplication |= appSpec.checkAccess(
					userSubject, DAction.getAction(IDAction.READ_NAME));
		}
        
        return isAuthorizedForClientApplication;
    }

	private List<DevelopmentEntity> getComponent(String clientApplication) throws EntityManagementException {
		 List<DevelopmentEntity> appEntities = new ArrayList<DevelopmentEntity>();
		 
		if(clientApplication.equals(CLIENT_APP_REPORTER)) {
			DevelopmentEntity reportAdminComp = lifecycleManager.getEntityForName(
	                EntityType.COMPONENT
	              , CLIENT_APP_REPORTER_ADMIN
	              , LifecycleManager.MUST_EXIST);
			
			DevelopmentEntity reportUserComp = lifecycleManager.getEntityForName(
	                EntityType.COMPONENT
	              , CLIENT_APP_REPORTER_USER
	              , LifecycleManager.MUST_EXIST);
			
			appEntities.add(reportAdminComp);
			appEntities.add(reportUserComp);
		} else {
			
			DevelopmentEntity appEntity = lifecycleManager.getEntityForName(
	                EntityType.COMPONENT
	              , clientApplication
	              , LifecycleManager.MUST_EXIST);
			appEntities.add(appEntity);
		}
		return appEntities;
	}

    /**
     * Set the provided session as the current session for this web service call
     * 
     * @param secureSession
     * @param msgContext
     */
    private void setSessionForCall(ISecureSession secureSession, MessageContext msgContext) {
        msgContext.setProperty(AuthenticationHandlerConstants.SECURE_SESSION_PROPERTY_NAME, secureSession);
    }

    /**
     * Retrieve the Secure Session Manager
     * 
     * @return
     * @throws ServiceNotReadyFault
     */
    private ISecureSessionManager getSecureSessionManager() throws ServiceNotReadyFault {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();

        if (!componentManager.isComponentRegistered(ISecureSessionManager.COMPONENT_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (ISecureSessionManager) componentManager.getComponent(ISecureSessionManager.COMPONENT_NAME);
    }

    /**
     * Retrieve the Application user manager
     * 
     * @return the Application User Manager
     * @throws ServiceNotReadyFault
     */
    private IApplicationUserManager getApplicationUserManager() throws ServiceNotReadyFault {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        IApplicationUserManager appManager = null;

        if (!compMgr.isComponentRegistered(IApplicationUserManagerFactory.COMP_NAME)) {
            throw new ServiceNotReadyFault();
        }

        try {
            IApplicationUserManagerFactory appUserManagerFactory = compMgr.getComponent(ApplicationUserManagerFactoryImpl.class);
            appManager = appUserManagerFactory.getSingleton();
        } catch (RuntimeException e) {
            throw new ServiceNotReadyFault();
        }
        return appManager;
    }
}
