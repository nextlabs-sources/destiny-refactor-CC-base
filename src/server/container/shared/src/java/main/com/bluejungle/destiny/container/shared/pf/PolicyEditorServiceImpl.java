package com.bluejungle.destiny.container.shared.pf;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/pf/PolicyEditorServiceImpl.java#1 $
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO;
import com.bluejungle.destiny.container.shared.applicationusers.core.AccessGroupModificationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserManagementObserver;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl.SharePointEnrollmentProperties;
import com.bluejungle.destiny.container.shared.securesession.ISecureSession;
import com.bluejungle.destiny.container.shared.securesession.ISecureSessionManager;
import com.bluejungle.destiny.container.shared.securesession.SecureSessionPersistenceException;
import com.bluejungle.destiny.container.shared.securesession.hibernate.HibernateSecureSessionManager;
import com.bluejungle.destiny.container.shared.securesession.service.axis.AuthenticationHandlerConstants;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.server.shared.configuration.IDPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.services.policy.types.EnrollmentType;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNode;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNodeList;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.domain.enrollment.ApplicationReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.ind.IDataSource;
import com.bluejungle.ind.IDataSourceConnection;
import com.bluejungle.ind.IDataSourceManager;
import com.bluejungle.ind.IExternalResource;
import com.bluejungle.ind.INDException;
import com.bluejungle.ind.impl.DataSourceConnection;
import com.bluejungle.ind.impl.DataSourceManager;
import com.bluejungle.ind.impl.ExternalResource;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lib.AccessPolicyComponent;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lib.DictionaryHelper;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DTOUtils;
import com.bluejungle.pf.destiny.lib.IPolicyEditorService;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.AttributeType;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.parser.PQLHelper;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.common.SpecReference;
import com.bluejungle.pf.domain.destiny.deployment.AgentRequest;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.IPResource;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.AccessibleSubject;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.action.IAction;

/**
 * This class implements the contract of the policy editor.
 */
public class PolicyEditorServiceImpl implements IPolicyEditorService, IManagerEnabled, IInitializable, ILogEnabled {

    public static final ComponentInfo<IPolicyEditorService> COMP_INFO =
    new ComponentInfo<IPolicyEditorService>(
        IPolicyEditorService.class.getName()
    , PolicyEditorServiceImpl.class
    , IPolicyEditorService.class
    , LifestyleType.SINGLETON_TYPE
        );

    private static final int PREVIEW_LIMIT = 1000;

    private static final Set<IAction> EMPTY_ACTION_SET = Collections.emptySet();

    private static final String SECURE_SESSION_TYPES_PREFIX = "ss";
    
    private Log log;

    private IComponentManager manager;

    private LifecycleManager lm;

    private IApplicationUserManager appManager;

    private DictionaryHelper dictHelper;

    private ServerSpecManager specManager;

    private IDPSComponentConfigurationDO dpsConfig;

    private IDictionary dictionary;
    
    private IDataSourceManager indDataSourceManager;

    private ObjectLockManager lockMgr;

    /** A reference to the application user manager */
    private IApplicationUserManager appUserManager;

    private static final String DEFAULT_DEPLOYMENT_TIME = "DefaultDeploymentTime";

    /**
     * Helper class to track logged in user information
     * @author dwashburn
     *
     */
    public class ApplicationUser 
    {
        private String userName;
        private String principalName;
        private String destinyId;
        private String canChangePassword;
         
        public ApplicationUser(
            String userName,
        String principalName,
        String destinyId,
        String canChangePassword
            ) {
            this.userName = userName;
            this.principalName = principalName;
            this.destinyId = destinyId;
            this.canChangePassword = canChangePassword;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPrincipalName() {
            return principalName;
        }

        public void setPrincipalName(String principalName) {
            this.principalName = principalName;
        }

        public Long getDestinyId() 
            throws NumberFormatException
        { 
            Long retValue = new Long(destinyId);
            return retValue;
        }

        public void setDestinyId(String destinyId) {
            this.destinyId = destinyId;
        }

        public String getCanChangePassword() {
            return canChangePassword;
        }

        public void setCanChangePassword(String canChangePassword) {
            this.canChangePassword = canChangePassword;
        }
     
    }

    @Override
    public String getConfigValue( String name ) throws PolicyServiceException {
        if ( dpsConfig == null ) {
            return null;
        }
        if ( DEFAULT_DEPLOYMENT_TIME.equals( name ) ) {
            return dpsConfig.getDeploymentTime();
        }
        throw new PolicyServiceException("Unsupported configuration item: '"+name+"'" );
    }

    @Override
    public int getNumDeployedPolicies(Date asOf) throws PolicyServiceException {
        try {
            return lm.getAllDeployedEntitiesCount( EntityType.POLICY, asOf, DeploymentType.PRODUCTION, false);
        } catch (EntityManagementException e) {
            throw new PolicyServiceException("unable to count number of deployed policies", e);
        }
    }

    @Override
    public Date getLatestDeploymentTime(Date asOf) throws PolicyServiceException {
        try {
            return lm.getLatestDeploymentTime( asOf, DeploymentType.PRODUCTION );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException("unable to get latest deployment time", e);
        }
    }

    /**
     * Given a name template, returns a <code>Collection</code> of
     * entity names.
     * @param nameTemplate template (with wildcards) of an object name.
     * @param entityType type of the entity.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>s.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    @Override
    public Collection<DomainObjectDescriptor> getDescriptorsForNameAndType(
        String nameTemplate
    , Collection<EntityType> entityTypes
        ) throws PolicyServiceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
     
        try {
            return lm.getEntityDescriptors( entityTypes, 
                                            nameTemplate, 
                                            new AccessCheckingConverter( loggedInUser.getUserName(), 
                                                                         loggedInUser.getDestinyId() ), 
                                            false );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DomainObjectDescriptor> getDescriptorsForNamesAndType(
        Collection<String> names
    , EntityType entityType
        ) throws PolicyServiceException {
        try {
            return lm.getDescriptorsForNames( entityType, names, LifecycleManager.DIRECT_CONVERTER );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of names, returns
     * a <code>Collection</code> of <code>String</code> objects
     * with PQL representing domain objects.
     * @param names a <code>Collection</code> of <code>String</code> objects representing names of entities.
     * @param entityType type of the entity.
     * @return a <code>Collection</code> of <code>String</code> objects with PQL representing domain objects.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    @Override
    public Collection<String> getEntitiesForNamesAndType(
        Collection<String> names
    , EntityType entityType
        ) throws PolicyServiceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        Long authUser =loggedInUser.getDestinyId();
        try {
            Collection<DevelopmentEntity> entities = lm.getEntitiesForNames(
                entityType
            , names
            , new MakerOfEntitiesWithAccess(authUser, getDefaultAPPqlForUser(authUser))
                );
            lm.resolveIds( entities );
            return PQLHelper.extractPQL( entities );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
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
    
    /**
     * Determine is a Secure Session is present in the soap header and if it is
     * valid
     * 
     * @param msgContext
     * @return
     * @throws ServiceNotReadyFault
     * @throws SOAPException
     */
    private ISecureSession getSecureSessionFromMsgContext(MessageContext msgContext)
        throws ServiceNotReadyFault, SOAPException, AxisFault {

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
                            addSecureSessionHeader(msgContext, secureSession);
                            return  secureSession;
                        }
                    } catch (SecureSessionPersistenceException exception) {
                        getLog().error("Error while retrieving secure session.  User request may be denied.", exception);
                    }
                }
            }

            // Detach Secure Session Header Element
            secureSessionElement.detachNode();
        }

        return null;
    }  

    private ApplicationUser getLoggedInUserFromSecureSession()
        throws PolicyServiceException 
    {
        MessageContext msgContext = MessageContext.getCurrentContext();    
        ISecureSession secureSession = null;
        try {
            secureSession = getSecureSessionFromMsgContext(msgContext);
        } catch (ServiceNotReadyFault e1) { 
            // The format of the message thrown is important as the string "not get current loggedInUser"
            // is used in the client to detect this condition happened, to inform the user they need to log in again.
            throw new PolicyServiceException("Could not get current loggedInUser. Unable to get secure session from context, service not ready.", e1);
        } catch (AxisFault e1) {
            throw new PolicyServiceException("Could not get current loggedInUser. Unable to get secure session from context, axis fault.", e1);
        } catch (SOAPException e1) {
            throw new PolicyServiceException("Could not get current loggedInUser. Unable to get secure session from context, soap exception.", e1);
        }
  
        if (secureSession == null)
            throw new PolicyServiceException("Could not get current loggedInUser.");
  
        String sessionUserName = (String) secureSession.getProperty(ISecureSession.USERNAME_PROP_NAME);
        String sessionPrincipalName = (String) secureSession.getProperty(ISecureSession.PRINCIPALNAME_PROP_NAME);
        String sessionId = (String) secureSession.getProperty(ISecureSession.ID_PROP_NAME);
        String sessionCanChangePassword = (String) secureSession.getProperty(ISecureSession.CAN_CHANGE_PASSWORD);

        // Note that this user is still active
        noteActiveUser(sessionPrincipalName);

        return new ApplicationUser(sessionUserName, 
                                   sessionPrincipalName, 
                                   sessionId, 
                                   sessionCanChangePassword);

    }

    @Override
    public List<DODDigest> getDODDigests(Collection<String> types) throws PolicyServiceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
     
        try {
            return lm.getDigests(types, new AccessCheckingDigestConverter(loggedInUser.getUserName(), loggedInUser.getDestinyId()));
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }


    /**
     * This takes the user id of the initiator to add the submitter information if required.
     * The submitter id is stored in the DevelopmentEntity schema. This is updated
     * only if the status of the saved entity is DRAFT but the to-be-saved status is
     * APPROVED.
     * 
     *  Note that although the submitter information is relevant to policy objects, here
     *  we are adding this information to all dependent components. The submitter
     *  information for non-policy components may not be valid since it can change
     *  if a different policy with a different submitter also includes that component. 
     *  The submitter information for policy components must always be valid.
     *  
     * @param pql
     * @param loggedOnUserId
     * @return list of DevelopmentEntity to save
     * @throws PolicyServiceException
     */
    private List<DevelopmentEntity> getActualEntities(
        String pql, final Long loggedOnUserId) throws PolicyServiceException {
        try {
            final List<DevelopmentEntity> entities = new LinkedList<DevelopmentEntity>();
            final Map<Long,DevelopmentEntity> ids = new HashMap<Long,DevelopmentEntity>();
            
            DomainObjectBuilder.processInternalPQL( pql, new IPQLVisitor() {
                private DomainObjectFormatter formatter = new DomainObjectFormatter();
                public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                    formatter.reset();
                    formatter.formatPolicyDef( descr, policy );
                    addDevEntity( descr );
                }
                public void visitFolder(DomainObjectDescriptor descr) {
                    formatter.reset();
                    formatter.formatFolder( descr );
                    addDevEntity( descr );
                }
                public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                    formatter.reset();
                    formatter.formatDef( descr, pred );
                    addDevEntity( descr );
                }
                public void visitLocation(DomainObjectDescriptor descr, Location location) {
                    formatter.reset();
                    formatter.formatLocation( descr, location );
                    addDevEntity( descr );
                }
                public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                    // Should not get top level access policy in this context
                    assert(false);
                }
                private void addDevEntity( DomainObjectDescriptor descr ) {
                    try {
                        DevelopmentEntity dev = new DevelopmentEntity(formatter.getPQL());
                        entities.add( dev );
                        if ( descr.getId() != null ) {
                            ids.put( descr.getId(), dev );
                        }
                    } catch ( PQLException pqlEx ) {
                        log.error( "Unable to save entity: " + pqlEx.getMessage() );
                    }
                }
            });

            // Since the development entities traveled to the client,
            // their versions may be stale. The step below updates them:
            Collection<DevelopmentEntity> existing = lm.getEntitiesForIDs( ids.keySet() );
            for ( DevelopmentEntity dev : existing ) {
                Object toSave = ids.get( dev.getId() );
                assert toSave != null; // The ID must exist in the map
                ((DevelopmentEntity) toSave).synchronizeWithSaved(dev);
            }
            return entities;
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        } catch (PQLException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public List<DODDigest> saveEntitiesDigest(String pql) throws PolicyServiceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        Long authUser =loggedInUser.getDestinyId();
        try {
            List<DevelopmentEntity> entities = getActualEntities(pql, authUser);

            lm.saveEntities(
                entities
            , new MakerOfEntitiesWithAccess(authUser, getDefaultAPPqlForUser(authUser)) 
            , authUser  
                );
            return LifecycleManager.convertDevEntitiesToDigests(entities, new DefaultDigestConverter());
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public List<DomainObjectDescriptor> saveEntities(String pql) throws PolicyServiceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        Long authUser =loggedInUser.getDestinyId();
        try {
            // Go through the entities and get the "real" ones when IDs exist
            List<DevelopmentEntity> entities = getActualEntities(pql, authUser);

            // Save the entities and get their IDs
            lm.saveEntities(
                entities
            , new MakerOfEntitiesWithAccess(authUser, getDefaultAPPqlForUser(authUser))
            , authUser  
                );
            List<DomainObjectDescriptor> res = new ArrayList<DomainObjectDescriptor>(entities.size());
            for ( DevelopmentEntity dev : entities ) {
                assert dev.getId() != null; // We have just saved all of them - all IDs must have been set.
                res.add( makeDescriptor( dev ) );
            }
            return res;
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    private String getDefaultAPPqlForUser (Long authUserId) throws PolicyServiceException {
        if(authUserId==null) {
            throw new NullPointerException("auth user id is null");
        }
        String defaultApPQL = null;
        IApplicationUserManager appManager = getApplicationUserManager();

        try {
            IAccessGroup primaryAccessGroup = appManager.getPrimaryAccessGroupForUser(authUserId.longValue());
            if(primaryAccessGroup!=null) {
                defaultApPQL = primaryAccessGroup.getApplicableAccessControl();
            } else {
                if(getLog().isDebugEnabled()) {
                    getLog().debug("No primary access group found on user: '" + authUserId.longValue() + "'. Using default access control.");
                }
                defaultApPQL = AccessPolicy.getDefaultAccessPolicyStr( authUserId );
            }
        } catch (UserNotFoundException e) {
            defaultApPQL = AccessPolicy.getDefaultAccessPolicyStr( authUserId );
            if(getLog().isWarnEnabled()) {
                getLog().warn("Unable to retrieve user with id: '" + authUserId.longValue() + "'. Using default access control.");
            }
        } catch (UserManagementAccessException e) {
            throw new PolicyServiceException(e);
        }
        return defaultApPQL;
    }

    private IApplicationUserManager getApplicationUserManager() {
        if(this.appManager==null) {
            IApplicationUserManagerFactory appManagerFactory = (IApplicationUserManagerFactory) this.manager
                                                               .getComponent(IApplicationUserManagerFactory.COMP_NAME);
            this.appManager = appManagerFactory.getSingleton();
        }
        return this.appManager;
    }

    @Override
    public Collection<String> getEntitiesForIds(Collection<Long> ids) throws PolicyServiceException {
        try {
            Collection<DevelopmentEntity> entities = lm.getEntitiesForIDs( ids );
            lm.resolveIds( entities );
            return PQLHelper.extractPQL( entities );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DomainObjectDescriptor> getReferringObjectsForGroup(
        String nameTemplate
    , EntityType entityType
    , EntityType referringType
    , boolean onlyDirect
        ) throws PolicyServiceException, CircularReferenceException {
        // Web services layer should have done this check for us.
        assert nameTemplate != null && entityType != null;

        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        Long did =loggedInUser.getDestinyId();
        String username = loggedInUser.getUserName();
     
        try {
            // Get the referenced entities
            Collection<Long> referenced = lm.getEntityIDs( Arrays.asList( new EntityType[] {entityType} ), nameTemplate );
            if ( referenced.isEmpty() ) {
                return Collections.emptyList();
            }
            // Get the candidate referencing entities
            List<EntityType> referringTypes = new LinkedList<EntityType>();
            if ( referringType != null ) {
                referringTypes.add( referringType );
                if (referringType != EntityType.POLICY) {
                    referringTypes.add(EntityType.COMPONENT);
                }
            } else {
                referringTypes.add( EntityType.POLICY );
                referringTypes.add(EntityType.COMPONENT);
            }
            Collection<DevelopmentEntity> candidates = lm.getAllEntitiesOfType( referringTypes );
            // Run the parameterized BFS algorithm
            Collection<Long> resIds = lm.discoverBFS(
                referenced
            ,   candidates
            ,   LifecycleManager.REVERSE_DEPENDENCY_BUILDER
            ,   LifecycleManager.DEVELOPMENT_ID_EXTRACTOR
            ,   onlyDirect
                );
            // Make descriptors for the IDs found by the BFS.
            return lm.getEntityDescriptorsForIDs( resIds, new AccessCheckingConverter( username, did ) );
        } catch ( EntityManagementException e) {
            throw new PolicyServiceException(e);
        } catch (PQLException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DomainObjectDescriptor> getReferringObjectsAsOf(
        Collection<DomainObjectDescriptor> descriptors
    , Date asOf
    , boolean onlyDirect
        ) throws PolicyServiceException, CircularReferenceException {
        // Web service checks arguments - we use asserts instead of throwing exceptions
        assert descriptors != null && asOf != null;
        if ( descriptors.isEmpty() ) {
            return Collections.emptyList();
        }
        try {
            // Prepare Collections of IDs and types
            Set<Long> referenced = new HashSet<Long>();
            Set<EntityType> types = new HashSet<EntityType>();
            types.add( EntityType.POLICY );
            for ( DomainObjectDescriptor descr : descriptors ) {
                EntityType type = descr.getType();
                if (type != EntityType.COMPONENT &&
                    type != EntityType.ACTION &&
                    type != EntityType.APPLICATION &&
                    type != EntityType.HOST &&
                    type != EntityType.PORTAL &&
                    type != EntityType.RESOURCE &&
                    type != EntityType.USER ) {
                    throw new IllegalArgumentException("Unsupported type in getReferringObjectsAsOf: "+type);
                }
                referenced.add( descr.getId() );
                types.add( type );
            }
            // Get the candidate referencing entities
            Collection<DeploymentEntity> candidates = lm.getAllDeployedEntities( types, asOf, DeploymentType.PRODUCTION );
            // Run the parameterized BFS algorithm
            Collection<Long> resIds = lm.discoverBFS(
                referenced
            ,   candidates
            ,   LifecycleManager.REVERSE_DEPENDENCY_BUILDER
            ,   LifecycleManager.DEPLOYMENT_ID_EXTRACTOR
            ,   onlyDirect
                );
            // Make descriptors for the IDs found by the BFS.
            return lm.getEntityDescriptorsForIDs( resIds, LifecycleManager.DIRECT_CONVERTER );
        } catch ( EntityManagementException e) {
            throw new PolicyServiceException(e);
        } catch (PQLException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DODDigest> getEntityDigestsForIDs(Collection<Long> ids) throws PolicyServiceException {
        try {
            return lm.getEntityDigestsForIDs(ids, new DefaultDigestConverter() );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }


    @Override
    public Collection<DomainObjectDescriptor> getEntityDescriptorsForIDs(
        Collection<Long> ids
        ) throws PolicyServiceException {
        try {
            return lm.getEntityDescriptorsForIDs(ids, LifecycleManager.DIRECT_CONVERTER);
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }


    @Override
    public Collection<DomainObjectDescriptor> getDescriptorsForState(
        DevelopmentStatus status
        ) throws PolicyServiceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        Long did =loggedInUser.getDestinyId();
        String userName = loggedInUser.getUserName();
        try {
            return lm.getAllEntitiesForStatus( status, new AccessCheckingConverter( userName, did ) );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DomainObjectDescriptor> getDeployedObjectDescriptors(
        Collection<Long> ids
    , Date asOf
        ) throws PolicyServiceException {
        try {
            List<DeploymentEntity> deps = lm.getDeploymentEntitiesForIDs( ids, asOf, DeploymentType.PRODUCTION );
            assert deps != null;
            DomainObjectDescriptor[] res = new DomainObjectDescriptor[ deps.size() ];
            int i = 0;
            for ( DeploymentEntity dep : deps ) {
                res[i++] = makeDescriptor(dep);
            }
            return Arrays.asList( res );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<String> getDeployedObjects(
        Collection<Long> ids
    , Date asOf
        ) throws PolicyServiceException {
        try {
            Collection<DeploymentEntity> entities = lm.getDeploymentEntitiesForIDs( ids, asOf, DeploymentType.PRODUCTION );
            lm.resolveIds( entities, asOf, DeploymentType.PRODUCTION );
            return PQLHelper.extractPQL( entities );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DomainObjectDescriptor> getDependenciesAsOf(
        Collection<DomainObjectDescriptor> descriptors
    , Date asOf
        ) throws CircularReferenceException, PolicyServiceException {
        assert descriptors != null;
        assert asOf != null;
        try {
            // Find the types of potential references and IDs of the initial BFS set.
            Set<EntityType> referringTypes = new HashSet<EntityType>();
            List<Long> initial = new LinkedList<Long>();
            List<Long> policyIDs = new LinkedList<Long>();
            for ( DomainObjectDescriptor dod : descriptors ) {
                initial.add( dod.getId() );
                initial.add( dod.getId() );
                EntityType dodType = dod.getType();
                if (dodType == EntityType.POLICY) {
                    policyIDs.add(dod.getId());
                } else {
                    referringTypes.add(dodType);
                    referringTypes.add(EntityType.COMPONENT);
                }
            }
            if ( !policyIDs.isEmpty() ) {
                // Policies can reference anything except policies
                referringTypes.add( EntityType.LOCATION );
                referringTypes.add( EntityType.COMPONENT );
                referringTypes.add( EntityType.ACTION );
                referringTypes.add( EntityType.APPLICATION );
                referringTypes.add( EntityType.HOST );
                referringTypes.add( EntityType.PORTAL );
                referringTypes.add( EntityType.RESOURCE );
                referringTypes.add( EntityType.USER );
            }
            Collection<DeploymentEntity> candidates = lm.getAllDeployedEntities(referringTypes, asOf, DeploymentType.PRODUCTION);
            candidates.addAll(lm.getDeploymentEntitiesForIDs( policyIDs, asOf, DeploymentType.PRODUCTION));
            // Run the parameterized BFS algorithm
            Collection<Long> resIds = lm.discoverBFS(
                initial
            ,   candidates
            ,   LifecycleManager.DIRECT_DEPENDENCY_BUILDER
            ,   LifecycleManager.DEPLOYMENT_ID_EXTRACTOR
            ,   false
                );
            // Make descriptors for the IDs found by the BFS.
            return lm.getEntityDescriptorsForIDs( resIds, LifecycleManager.DIRECT_CONVERTER );
        } catch ( EntityManagementException e) {
            throw new PolicyServiceException(e);
        } catch (PQLException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DomainObjectDescriptor> descriptorsForDeploymentRecord(
        DeploymentRecord record
        ) throws PolicyServiceException {
        try {
            Collection<DeploymentEntity> deps = lm.getDeployedEntitiesForDeploymentRecord( record );
            DomainObjectDescriptor[] res = new DomainObjectDescriptor[ deps.size() ];
            int i = 0;
            for ( DeploymentEntity dep : deps ) {
                res[i++] = makeDescriptor(dep);
            }
            return Arrays.asList( res );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DeploymentHistory> getDeploymentHistory(DomainObjectDescriptor descr) throws PolicyServiceException {
        return getDeploymentHistory(descr.getId());
    }

    public Collection<DeploymentHistory> getDeploymentHistory(Long id) throws PolicyServiceException {
        try {
            return lm.getDeploymentHistoryForEntityId(
                id
            ,   DeploymentType.PRODUCTION
                );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public DeploymentRecord scheduleDeployment(
        Collection<DomainObjectDescriptor> descrs
    , Date when
        ) throws PolicyServiceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        try {
            Collection<DevelopmentEntity> entities = lm.getEntitiesForDescriptors( descrs );
            return lm.deployEntities( entities, when, DeploymentType.PRODUCTION, false, loggedInUser.getDestinyId());
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public DeploymentRecord scheduleUndeployment(
        Collection<DomainObjectDescriptor> descrs
    , Date when
        ) throws PolicyServiceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        try {
            Collection<DevelopmentEntity> entities = lm.getEntitiesForDescriptors( descrs );
            return lm.undeployEntities( entities, when, DeploymentType.PRODUCTION, false, loggedInUser.getDestinyId());
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public void cancelScheduledDeployment(DeploymentRecord record) throws PolicyServiceException {
        try {
            lm.cancelDeploymentOfEntities( record );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DeploymentRecord> getDeploymentRecords(Date from, Date to) throws PolicyServiceException {
        try {
            return lm.getDeploymentHistory( from, to );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<AttributeDescriptor> getAttributeDescriptors(EntityType type) throws PolicyServiceException {
        Collection<? extends IElementField> dictionaryAttributes;
        Set<String> skip = new HashSet<String>();
        SubjectType subjType;
        List<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>();
        try {
            if ( type == EntityType.APPLICATION ) {
                IElementType appType = dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
                dictionaryAttributes = appType.getFields();
                skip.add(ApplicationReservedFieldEnumType.SYSTEM_REFERENCE.getName());
                skip.add(ApplicationReservedFieldEnumType.APP_FINGER_PRINT.getName());
                skip.add(ApplicationReservedFieldEnumType.UNIQUE_NAME.getName());
                attributes.add(new AttributeDescriptor("Executable Name", AttributeType.STRING, SubjectAttribute.APP_NAME));
                attributes.add(new AttributeDescriptor("URL", AttributeType.STRING, SubjectAttribute.APP_URL));
                attributes.add(new AttributeDescriptor("Tool", AttributeType.STRING, SubjectAttribute.APP_TOOL));
                subjType = SubjectType.APP;
            } else if ( type == EntityType.USER ) {
                IElementType userType = dictionary.getType(ElementTypeEnumType.USER.getName());
                dictionaryAttributes = userType.getFields();
                skip.add(UserReservedFieldEnumType.WINDOWS_SID.getName());
                skip.add(UserReservedFieldEnumType.UNIX_ID.getName());
                skip.add(UserReservedFieldEnumType.MAIL.getName());
                attributes.add(new AttributeDescriptor("Email Address"
                                                       , AttributeType.STRING, SubjectAttribute.USER_EMAIL));
                attributes.add(new AttributeDescriptor("Email Domain"
                                                       , AttributeType.STRING, SubjectAttribute.USER_EMAIL_DOMAIN));
                attributes.add(new AttributeDescriptor("Client Identifier"
                                                       , AttributeType.STRING, false, SubjectAttribute.USER_CLIENT_ID));
                attributes.add(new AttributeDescriptor("Recipient Client Count"
                                                       , AttributeType.LONG, SubjectAttribute.SENT_TO_CLIENT_COUNT));
                subjType = SubjectType.USER;
            } else if (  type == EntityType.HOST ) {
                IElementType hostType = dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
                dictionaryAttributes = hostType.getFields();
                skip.add(ComputerReservedFieldEnumType.WINDOWS_SID.getName());
                skip.add(ComputerReservedFieldEnumType.UNIX_ID.getName());
                attributes.add(new AttributeDescriptor("Network Address", AttributeType.STRING, SubjectAttribute.INET_ADDRESS));
                attributes.add(new AttributeDescriptor("Site", AttributeType.STRING, SubjectAttribute.LOCATION));
                subjType = SubjectType.HOST;
            } else {
                throw new IllegalArgumentException("Unexpected argument type: "+type );
            }
        } catch (DictionaryException e) {
            throw new PolicyServiceException("failed to get element fields", e);
        }
        for( IElementField attribute : dictionaryAttributes ) {
            if ( !skip.contains(attribute.getName())) {
                attributes.add(
                    new AttributeDescriptor(
                        attribute.getLabel()
                    ,   getAttributeTypeFromDictionaryField(attribute)
                    ,   SubjectAttribute.forNameAndType(attribute.getName(), subjType)
                        )
                    );
            }
        }
        if (type == EntityType.USER || type == EntityType.HOST) {
            attributes.add(
                new AttributeDescriptor(
                    "Group"
                ,   AttributeType.STRING
                ,   SubjectAttribute.forNameAndType("ldapgroup", subjType)
                    )
                );
        }
        return attributes;
    }

    /**
     * Converts enumeration objects of type ElementFieldType to type AttributeType
     *
     * @param type
     * @return converted enumeration
     */
    private AttributeType getAttributeTypeFromDictionaryField(IElementField field) {
        ElementFieldType type = field.getType();
        if (ElementFieldType.STRING == type 
            || ElementFieldType.CS_STRING == type
            || ElementFieldType.STRING_ARRAY == type
            || ElementFieldType.LONG_STRING == type) {
            return AttributeType.STRING;
        } else if (ElementFieldType.DATE == type) {
            return AttributeType.DATE;
        } else if (ElementFieldType.NUMBER == type) {
            return AttributeType.LONG;
        } else {
            throw new IllegalArgumentException("Unknown attribute type: "+type);
        }
    }

    @Override
    public Collection<LeafObject> getLeafObjectsForIds(
        long[] elementIds
    , long[] userGroupIds
    , long[] hostGroupIds
        ) throws PolicyServiceException {
        try {
            return dictHelper.getLeafObjectsForIds(elementIds, userGroupIds, hostGroupIds);
        } catch (DictionaryException e) {
            throw new PolicyServiceException("Unable to query elements by IDs", e);
        }
    }

    @Override
    public Collection<LeafObject> queryLeafObjects(
        IPredicate searchPredicate
    , String namespaceId
    , LeafObjectType type
    , int limit
        ) throws PolicyServiceException {
        List<LeafObject> res;

        try {
            if (namespaceId != null) {
                IEnrollment enrollment = this.dictionary.getEnrollment(namespaceId);
                IPredicate namespacePredicate = this.dictionary.condition(enrollment);
                searchPredicate = new CompositePredicate(BooleanOp.AND, searchPredicate);
                ((CompositePredicate)searchPredicate).addPredicate(namespacePredicate);
            }
            if (type == LeafObjectType.USER) {
                res = dictHelper.getMatchingSubjects(searchPredicate, LeafObjectType.USER, null, limit);
            } else if (type == LeafObjectType.CONTACT) {
                res = dictHelper.getMatchingContacts(searchPredicate, limit);
            } else if (type == LeafObjectType.HOST) {
                res = dictHelper.getMatchingSubjects(searchPredicate, LeafObjectType.HOST, null, limit);
            } else if (type == LeafObjectType.APPLICATION) {
                res = dictHelper.getMatchingSubjects(searchPredicate, LeafObjectType.APPLICATION, null, limit);
            } else if (type == LeafObjectType.USER_GROUP || type == LeafObjectType.HOST_GROUP) {
                res = dictHelper.getMatchingGroups(searchPredicate, type, limit);
                // sort the groups if there is more than 1 group returned
                if ( res.size() > 1 ) {
                    Collections.sort(res);
                }
            } else if (type == LeafObjectType.APPUSER) {
                Long appUserId = getOneUserId(searchPredicate);
                
                Set<IApplicationUser> appUsers;
                if(appUserId != null){
                    try {
                        appUsers = Collections.singleton(appUserManager.getApplicationUser(appUserId));
                    } catch (UserNotFoundException e) {
                        appUsers = Collections.EMPTY_SET;
                    }
                }else {
                    appUsers = appUserManager.getApplicationUsers(
                        makeUserSearchSpecs(searchPredicate), limit
                        );
                }
                
                res = new ArrayList<LeafObject>(appUsers.size());
                for (IApplicationUser user : appUsers) {
                    LeafObject leaf = new LeafObject(LeafObjectType.APPUSER);
                    leaf.setName(user.getDisplayName());
                    leaf.setUid(user.getDestinyId().toString());
                    leaf.setId(user.getDestinyId());
                    leaf.setUniqueName(user.getUniqueName());
                    leaf.setDomainName(user.getDomainName());
                    res.add(leaf);
                }
            } else if (type == LeafObjectType.ACCESSGROUP) {
                Set<IAccessGroup> appGroups = appUserManager.getAccessGroups(
                    makeGroupSearchSpecs(searchPredicate), limit
                    );
                res = new ArrayList<LeafObject>(appGroups.size());
                for (IAccessGroup group : appGroups) {
                    LeafObject leaf = new LeafObject(LeafObjectType.ACCESSGROUP);
                    leaf.setName(group.getTitle());
                    leaf.setUniqueName(group.getTitle());
                    leaf.setUid(group.getDestinyId().toString());
                    leaf.setId(group.getDestinyId());
                    leaf.setDomainName(group.getDomainName());
                    res.add(leaf);
                }
            } else {
                throw new IllegalArgumentException("type:"+type);
            }
        } catch ( DictionaryException e ) {
            throw new PolicyServiceException ("Failed to get objects from dictionary", e);
        } catch ( EntityManagementException e ) {
            throw new PolicyServiceException ("Failed to get objects from dictionary", e);
        } catch ( PQLException e ) {
            throw new PolicyServiceException ("Failed to get objects from dictionary", e);
        } catch ( CircularReferenceException e ) {
            throw new PolicyServiceException ("Failed to get objects from dictionary", e);
        } catch ( UserManagementAccessException e ) {
            throw new PolicyServiceException ("Failed to get application users/groups", e);
        }
        return res;
    }
    
    /**
     * return a user id in the predicate if the predicate only contains userid and only one 
     * @return
     */
    private Long getOneUserId(IPredicate pred) {
        if (pred == null) {
            throw new NullPointerException("pred");
        }
        final AtomicReference<Long> userId = new AtomicReference<Long>();
        DefaultPredicateVisitor visitor = new DefaultPredicateVisitor() {
            @Override
            public void visit(IRelation rel) {
                IExpression lhs = rel.getLHS();
                IExpression rhs = rel.getRHS();
                if (lhs == SubjectAttribute.APPUSER_ID && rhs instanceof Constant && userId.get() == null) {
                    userId.set((Long) ((Constant) rhs).getValue().getValue());
                } else {
                    throw new IllegalArgumentException();
                }
            }
        };
        try {
            pred.accept(visitor, IPredicateVisitor.POSTORDER);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return userId.get();
    }

    /**
     * Given a predicate, makes a corresponding <code>IUserSearchSpec[]</code>.
     * @param pred the predicate to convert to a collection of search specs.
     * @return a <code>IUserSearchSpec[]</code> corresponding to the predicate.
     */
    private static IUserSearchSpec[] makeUserSearchSpecs(IPredicate pred) {
        if (pred == null) {
            throw new NullPointerException("pred");
        }
        SearchSpecBuilder<IUserSearchSpec> ssb = new SearchSpecBuilder<IUserSearchSpec>() {
            @Override
            protected IUserSearchSpec makeSpec(IAttribute attr, Constant constant) {
                assert attr != null;
                assert constant != null;
                if (attr != SubjectAttribute.USER_NAME) {
                    throw new IllegalArgumentException("Unsupported search attribute: "+attr);
                }
                final String val = constant.getRepresentation().replace('*', '%');
                return new IUserSearchSpec() {
                    @Override
                    public String getLastNameStartsWith() {
                        return val;
                    }
                };
            }
        };
        pred.accept(ssb, IPredicateVisitor.POSTORDER);
        List<IUserSearchSpec> res = ssb.getResult();
        return (IUserSearchSpec[])res.toArray(new IUserSearchSpec[res.size()]);
    }

    /**
     * Given a predicate, makes a corresponding <code>IGroupSearchSpec[]</code>.
     * @param pred the predicate to convert to a collection of search specs.
     * @return a <code>IGroupSearchSpec[]</code> corresponding to the predicate.
     */
    private static IGroupSearchSpec[] makeGroupSearchSpecs(IPredicate pred) {
        if (pred == null) {
            throw new NullPointerException("pred");
        }
        SearchSpecBuilder<IGroupSearchSpec> ssb = new SearchSpecBuilder<IGroupSearchSpec>() {
            @Override
            protected IGroupSearchSpec makeSpec(IAttribute attr, Constant constant) {
                assert attr != null;
                assert constant != null;
                if (attr != SubjectAttribute.USER_LDAP_GROUP) {
                    throw new IllegalArgumentException("Unsupported search attribute: "+attr);
                }
                final String val = constant.getRepresentation().replace('*', '%');
                return new IGroupSearchSpec() {
                    @Override
                    public String getTitleStartsWith() {
                        return val;
                    }
                };
            }
        };
        pred.accept(ssb, IPredicateVisitor.POSTORDER);
        List<IGroupSearchSpec> res = ssb.getResult();
        return (IGroupSearchSpec[])res.toArray(new IGroupSearchSpec[res.size()]);
    }

    @Override
    public Collection<LeafObject> getMatchingActions(
        DomainObjectDescriptor descr
        ) throws PolicyServiceException, CircularReferenceException {
        if ( descr == null ) {
            throw new NullPointerException("descriptor");
        }
        final Map<Long,IHasId> byId;
        try {
            byId = specManager.getDependentsMapById( descr );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        } catch (PQLException e) {
            throw new PolicyServiceException(e);
        }
        final Set<LeafObject> res = new HashSet<LeafObject>();

        // Get the predicate
        IPredicate pred = ((SpecBase)byId.get(descr.getId())).getPredicate();

        pred.accept( new DefaultPredicateVisitor() {
            public void visit(IPredicateReference pred) {
                SpecReference ref = (SpecReference)pred;
                if ( ref.isReferenceByName() ) {
                    throw new IllegalArgumentException("reference by name is unexpected");
                }
                SpecBase spec = (SpecBase)byId.get( ref.getReferencedID() );
                spec.getPredicate().accept( this, PREORDER );
            }
            public void visit(IPredicate pred) {
                if ( pred instanceof DAction ) {
                    LeafObject leaf = new LeafObject( LeafObjectType.ACTION );
                    leaf.setName(((DAction)pred).getName());
                    res.add( leaf );
                } else {
                    throw new IllegalArgumentException("Found a leaf action of an unexpected type");
                }
            }
        }, IPredicateVisitor.PREORDER );

        return res;
    }

    @Override
    public String getFullyResolvedEntity(DomainObjectDescriptor descr) throws PolicyServiceException, CircularReferenceException {
        if ( descr == null ) {
            throw new NullPointerException("descriptor");
        }
        // Resolve all the dependencies of this descriptor
        Collection<DomainObjectDescriptor> deps;
        try {
            deps = specManager.getDependencies(
                Arrays.asList(new DomainObjectDescriptor[] { descr })
            , LifecycleManager.DIRECT_CONVERTER);
        } catch ( EntityManagementException e ) {
            throw new PolicyServiceException( e );
        } catch ( PQLException e ) {
            throw new PolicyServiceException( e );
        } catch ( CircularReferenceException e ) {
            throw new PolicyServiceException( e );
        }
        // Get all related objects
        Set<Long> ids = new HashSet<Long>(DomainObjectDescriptor.extractIds(deps));
        ids.add( descr.getId() );
        Collection<IHasId> all;
        try {
            all = DTOUtils.makeCollectionOfSpecs(
                DTOUtils.mergeStrings(
                    PQLHelper.extractPQL(
                        lm.getEntitiesForIDs( ids )
                        )
                    )
                );
        } catch (PQLException e) {
            throw new PolicyServiceException( e );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException( e );
        }

        // Build a map of IDs to specs
        final Map<Long,IHasId> byId = new HashMap<Long,IHasId>();
        for ( IHasId spec : all ) {
            byId.put( spec.getId(), spec );
        }

        // Get the predicate
        IPredicate pred = ((SpecBase)byId.get(descr.getId())).getPredicate();

        class RefResolutionVisitor implements IPredicateVisitor {
            final Stack<IPredicate> stack = new Stack<IPredicate>();
            
            @Override
            public void visit(ICompositePredicate pred, boolean preorder) {
                // reverse the top predicateCount() elements of the stack
                LinkedList<IPredicate> tmp = new LinkedList<IPredicate>();
                assert stack.size() >= pred.predicateCount();
                for ( int i = 0 ; i != pred.predicateCount() ; i++ ) {
                    tmp.addFirst( stack.pop() );
                }
                if ( tmp.isEmpty() ) {
                    throw new NoSuchElementException("Detected a composite predicate with zero elements.");
                }
                stack.push( new CompositePredicate( pred.getOp(), tmp ) );
            }
            
            @Override
            public void visit(IPredicateReference pred) {
                SpecReference ref = (SpecReference)pred;
                if ( ref.isReferenceByName() ) {
                    throw new IllegalArgumentException("reference by name is unexpected");
                }
                SpecBase spec = (SpecBase)byId.get( ref.getReferencedID() );
                spec.getPredicate().accept( this, POSTORDER );
            }
            @Override
            public void visit(IRelation pred) {
                IExpression lhs = pred.getLHS();
                IExpression rhs = pred.getRHS();
                if ( lhs == ResourceAttribute.OWNER_GROUP && (rhs instanceof SpecReference ) ) {
                    stack.push( resolveGroup( (SpecReference)rhs, byId, pred.getOp() ) );
                } else if ( rhs == ResourceAttribute.OWNER_GROUP  && (lhs instanceof SpecReference ) ) {
                    stack.push( resolveGroup( (SpecReference)lhs, byId, pred.getOp() ) );
                } else if ( lhs == ResourceAttribute.OWNER_LDAP_GROUP && (rhs instanceof Constant ) ) {
                    stack.push( resolveLDAPGroup(((Constant)rhs).getRepresentation(), pred.getOp()));
                } else if ( rhs == ResourceAttribute.OWNER_LDAP_GROUP  && (lhs instanceof Constant ) ) {
                    stack.push( resolveLDAPGroup(((Constant)lhs).getRepresentation(), pred.getOp()));
                } else if ( lhs == ResourceAttribute.OWNER && (rhs instanceof Constant ) ) {
                    stack.push( resolveOwner(((Constant)rhs).getRepresentation(), pred.getOp()));
                } else if ( rhs == ResourceAttribute.OWNER  && (lhs instanceof Constant ) ) {
                    stack.push( resolveOwner(((Constant)lhs).getRepresentation(), pred.getOp()));
                } else {
                    stack.push( pred );
                }
            }
            
            @Override
            public void visit(IPredicate pred) {
                stack.push( pred );
            }
            public IPredicate getResult() {
                // Visiting a non-empty predicate should produce one result
                assert stack.size() == 1;
                return (IPredicate)stack.pop();
            }
            private IPredicate resolveGroup( SpecReference ref, Map<Long,IHasId> byId, RelationOp op ) {
                Collection<LeafObject> users;
                try {
                    users = dictHelper.getMatchingSubjects(ref, LeafObjectType.USER, byId, PREVIEW_LIMIT);
                } catch ( EntityManagementException e ) {
                    return PredicateConstants.FALSE;
                } catch ( PQLException e ) {
                    return PredicateConstants.FALSE;
                } catch ( CircularReferenceException e ) {
                    return PredicateConstants.FALSE;
                } catch ( DictionaryException e ) {
                    return PredicateConstants.FALSE;
                }
                return ownerInEquivalentPredicate( users, op );
            }
            private IPredicate resolveLDAPGroup( String groupName, RelationOp op ) {
                Collection<LeafObject> users;
                try {
                    users = dictHelper.getMatchingSubjects(
                        new Relation(
                            RelationOp.EQUALS
                        ,   SubjectAttribute.USER_LDAP_GROUP
                        ,   Constant.build( groupName )
                            )
                    ,   LeafObjectType.USER
                    ,   null
                    ,   PREVIEW_LIMIT
                        );
                } catch ( EntityManagementException e ) {
                    return PredicateConstants.FALSE;
                } catch ( PQLException e ) {
                    return PredicateConstants.FALSE;
                } catch ( CircularReferenceException e ) {
                    return PredicateConstants.FALSE;
                } catch ( DictionaryException e ) {
                    return PredicateConstants.FALSE;
                }
                return ownerInEquivalentPredicate( users, op );
            }
            private IPredicate resolveOwner( String ownerUserName, RelationOp op ) {
                Collection<LeafObject> users;
                try {
                    users = dictHelper.getMatchingSubjects(
                        new Relation(
                            RelationOp.EQUALS
                        ,   SubjectAttribute.USER_NAME
                        ,   Constant.build( ownerUserName )
                            )
                    ,   LeafObjectType.USER
                    ,   null
                    ,   PREVIEW_LIMIT
                        );
                } catch ( EntityManagementException e ) {
                    return PredicateConstants.FALSE;
                } catch ( PQLException e ) {
                    return PredicateConstants.FALSE;
                } catch ( CircularReferenceException e ) {
                    return PredicateConstants.FALSE;
                } catch ( DictionaryException e ) {
                    return PredicateConstants.FALSE;
                }
                return ownerInEquivalentPredicate( users, op );
            }
            private IPredicate ownerInEquivalentPredicate( Collection<LeafObject> users, RelationOp op ) {
                List<IPredicate> preds = new ArrayList<IPredicate>( users.size() );
                for ( LeafObject user : users ) {
                    preds.add(
                        new Relation(
                            RelationOp.EQUALS
                        ,   ResourceAttribute.OWNER
                        ,   Constant.build( user.getUid() )
                            )
                        );
                }
                IPredicate res;
                if ( preds.size() == 0 ) {
                    res = PredicateConstants.FALSE;
                } else if ( preds.size() == 1 ) {
                    res = (IPredicate)preds.iterator().next();
                } else {
                    res = new CompositePredicate( BooleanOp.OR, preds );
                }
                if ( op == RelationOp.EQUALS ) {
                    return res;
                } else if ( op == RelationOp.NOT_EQUALS ) {
                    return new CompositePredicate( BooleanOp.NOT, res );
                } else {
                    throw new IllegalArgumentException("Unexpected operator: "+op.getName());
                }
            }
        }

        RefResolutionVisitor rrv = new RefResolutionVisitor();
        pred.accept( rrv, IPredicateVisitor.POSTORDER );


        // Format the resulting predicate, and return the PQL.
        DomainObjectFormatter dof = new DomainObjectFormatter();
        dof.formatDef( descr, rrv.getResult() );

        return dof.getPQL();
    }

    @Override
    public String getLockHolder( Object key ) {
        return lockMgr.getLockHolder(key);
    }

    @Override
    public String acquireLock(Object key, boolean force)  throws PolicyServiceException {
        return lockMgr.acquireLock(key, force);
    }

    @Override
    public String releaseLock(Object key) throws PolicyServiceException {
        return lockMgr.releaseLock(key);
    }

    /**
     * Update the user's "last activity" record, so that we can determine if they have timed out or not.
     * This is called every time getLoggedInUserFromSecureSession() is called
     */
    private void noteActiveUser(String user) {
        lockMgr.updateUserActivity(user);
    }

    private class ObjectLockManager {
        private Map<Object, String> locks = new HashMap<Object, String>();
        private Map<String, Long> lastActivityByUser = new HashMap<String, Long>();
        private Long timeoutMs;

        public ObjectLockManager() {
            try {
                timeoutMs = getSecureSessionManager().getDefaultTimeout();
            } catch (ServiceNotReadyFault e) {
                timeoutMs = HibernateSecureSessionManager.DEFAULT_SESSION_TIMEOUT;
                getLog().warn("Unable to get secure session manager. Assuming default timeout is " + timeoutMs);
            }
        }

        public synchronized void updateUserActivity(String user) {
            lastActivityByUser.put(user, System.currentTimeMillis());
        }

        private synchronized boolean userHasTimedOut(String user) {
            Long lastActivity = lastActivityByUser.get(user);

            if (lastActivity == null) {
                return true;
            }

            long now = System.currentTimeMillis();

            if (now - lastActivity > timeoutMs) {
                lastActivityByUser.remove(user);
                return true;
            }

            return false;
        }
        
        public synchronized String acquireLock(Object key, boolean force) throws PolicyServiceException {
            String requestor = getLoggedInUserFromSecureSession().getPrincipalName();

            if ( key == null ) {
                throw new NullPointerException("key");
            }

            if ( requestor == null ) {
                throw new NullPointerException("requestor");
            }

            String owner = locks.get( key );
            if ( owner == null || force || userHasTimedOut(owner)) {
                locks.put( key, owner = requestor );
            }
            return owner;
        }

        public synchronized String releaseLock(Object key) throws PolicyServiceException {
            String holder = getLoggedInUserFromSecureSession().getPrincipalName();

            if ( key == null ) {
                throw new NullPointerException("key");
            }
            if ( holder == null ) {
                throw new NullPointerException("holder");
            }

            String owner = (String)locks.get( key );
            if ( owner != null && owner.equals( holder ) ) {
                locks.remove( key );
            }
            return owner;
        }

        public synchronized String getLockHolder(Object key) {
            if ( key == null ) {
                throw new NullPointerException("key");
            }
            return locks.get( key );
        }
    }

    @Override
    public Collection<DomainObjectDescriptor> ensureOperationIsAllowed(
        Collection<DomainObjectDescriptor> descriptors
    , IAction action
        ) throws PolicyServiceException {
        // Create a temporary subject from this did
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        Long did =loggedInUser.getDestinyId();
        String username = loggedInUser.getUserName();
        List<Long> usersGroups = new ArrayList<Long>();
        try {
            Set<IAccessGroup> accessGroups = appUserManager.getAccessGroupsContainingUser(did.longValue());
            for ( IAccessGroup group : accessGroups ) {
                usersGroups.add( group.getDestinyId() );
            }
        } catch (UserNotFoundException unfe) {
            getLog().error("Failed to locate user with id, " + did + ".  Assuming no group membership.");
        } catch ( UserManagementAccessException umae ) {
            getLog().error("Failed to locate groups for user with id, " + did + ".  Assuming no group membership.");
        }

        // Must be an AccessibleSubject, because we are doing PBAC against it
        IDSubject who = new AccessibleSubject(
            username
        , username
        , username
        , did
        , EvalValue.build(Multivalue.create(usersGroups))
        , SubjectType.USER
            );

        assert descriptors != null;
        if ( descriptors.isEmpty() ) {
            return Collections.emptyList();
        }
        if ( !(action instanceof DAction) ) {
            throw new IllegalArgumentException( "action" );
        }
        List<Long> ids = new ArrayList<Long>( descriptors.size() );
        Map<Long,DomainObjectDescriptor> idToDescr = new HashMap<Long,DomainObjectDescriptor>();
        for ( DomainObjectDescriptor descr : descriptors ) {
            Long id = descr.getId();
            ids.add( id );
            idToDescr.put( id, descr );
        }
        List<DomainObjectDescriptor> res = new LinkedList<DomainObjectDescriptor>();
        try {
            Collection<DevelopmentEntity> entities = lm.getEntitiesForIDs( ids );
            for ( DevelopmentEntity dev : entities ) {
                IAccessPolicy ap = dev.getAccessPolicy();
                if ( ap == null || ap.checkAccess(
                         makeSyntheticOwner(dev.getOwner())
                     ,   who
                     ,   (DAction)action ) ) {
                    res.add( idToDescr.get( dev.getId() ) );
                } else if ( action == DAction.READ && who.getId().equals( dev.getOwner() ) ) {
                    res.add( idToDescr.get( dev.getId() ) );
                }
            }
        } catch ( EntityManagementException e ) {
            throw new PolicyServiceException( e );
        }
        return res;
    }

    @Override
    public Collection<? extends IAction> allowedActions(
        Collection<DomainObjectDescriptor> descriptors
        ) throws PolicyServiceException {
        assert descriptors != null;
        if ( descriptors.isEmpty() ) {
            return Collections.emptyList();
        }
        final DAction[] allActions = new DAction[] {
            (DAction)DAction.WRITE,
            (DAction)DAction.ADMIN,
            (DAction)DAction.APPROVE,
            (DAction)DAction.DEPLOY,
            (DAction)DAction.READ,
            (DAction)DAction.RENAME,
            (DAction)DAction.DELETE
        };
        
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        Long did =loggedInUser.getDestinyId();
        String username = loggedInUser.getUserName();
     
        final IDSubject who;
		try {
			who = makeAccessibleSubject(username, did);
		} catch (UserNotFoundException e) {
			throw new PolicyServiceException(e);
		} catch (UserManagementAccessException e) {
			throw new PolicyServiceException(e);
		}
        
        List<Long> ids = new ArrayList<Long>();
        for (DomainObjectDescriptor dod : descriptors ) {
            ids.add(dod.getId());
        }
        Collection<String> entities = getEntitiesForDescriptors( descriptors );
        final int[] counts = new int[allActions.length];
        for ( String pql : entities ) {
            DomainObjectBuilder dob = new DomainObjectBuilder(pql);
            try {
                dob.processInternalPQL(new IPQLVisitor() {
                    public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                        throw new IllegalArgumentException("allowedActions:visitAccessPolicy");
                    }
                    public void visitComponent(DomainObjectDescriptor descriptor, IPredicate spec) {
                        processAccess(descriptor.getAccessPolicy(), descriptor.getOwner());
                    }
                    public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
                        processAccess(descriptor.getAccessPolicy(), descriptor.getOwner());
                    }
                    public void visitPolicy(DomainObjectDescriptor descriptor, IDPolicy policy) {
                        processAccess(descriptor.getAccessPolicy(), descriptor.getOwner());
                    }
                    public void visitFolder(DomainObjectDescriptor descriptor) {
                        processAccess(descriptor.getAccessPolicy(), descriptor.getOwner());
                    }
                    void processAccess(IAccessPolicy accessPolicy, Long ownerId) {
                        for ( int i = 0 ; i != allActions.length ; i++ ) {
                            if (accessPolicy.checkAccess(makeSyntheticOwner(ownerId), who, allActions[i])) {
                                counts[i]++;
                            }
                        }
                    }
                });
            } catch (PQLException ignored) {
                return Collections.emptyList();
            }
        }
        List<IAction> res = new LinkedList<IAction>();
        for (int i = 0 ; i != allActions.length ; i++ ) {
            if (counts[i] == entities.size()) {
                res.add(allActions[i]);
            }
        }
        return res;
    }

    @Override
    public Collection<AgentStatusDescriptor> addCountsToAgentDescriptors(
        Collection<AgentStatusDescriptor> agents
        ) throws PolicyServiceException {
        // Group all agents by their timestamp, and build a predicate to query hosts by name
        Map<Date,List<AgentStatusDescriptor>> byTimestamp = new HashMap<Date,List<AgentStatusDescriptor>>();
        for ( AgentStatusDescriptor agent : agents ) {
            Date ts = agent.getLastUpdated();
            if ( ts != null ) {
                if ( byTimestamp.containsKey( ts ) ) {
                    byTimestamp.get(ts).add( agent );
                } else {
                    List<AgentStatusDescriptor> tmp = new LinkedList<AgentStatusDescriptor>();
                    tmp.add( agent );
                    byTimestamp.put( ts, tmp );
                }
            }
        }
        try {
            DomainObjectBuilder.OneObjectVisitor oov = new DomainObjectBuilder.OneObjectVisitor();
            // For each timestamp, do an as-of query,
            // then process all agents that have the given timestamp.
            for ( Date ts : byTimestamp.keySet() ) {
                List<AgentStatusDescriptor> tsAgents = byTimestamp.get(ts);
                List<DeploymentEntity> atTheTime = lm.getAllDeployedEntities( ts, DeploymentType.PRODUCTION );
                int smartPolicyCount = 0;
                int componentCount = 0;
                for ( DeploymentEntity dep : atTheTime ) {
                    if ( dep.isHidden() == true ) continue;
                    if ( dep.getType() == EntityType.POLICY ) {
                        DomainObjectBuilder.processInternalPQL( dep.getPql(), oov );
                        IDPolicy policy = oov.getPolicy();
                        IPredicate dt = policy.getDeploymentTarget();
                        if ( dt != null ) {
                            for ( AgentStatusDescriptor agent : tsAgents ) {
                                AgentRequest agentRequest = new AgentRequest(agent.getId(), agent.getAgentType());
                                if ( dt.match( agentRequest ) ) {
                                    agent.addNumPolicies( 1 );
                                }
                            }
                        } else {
                            smartPolicyCount++;
                        }
                    } else if ( dep.getType() != EntityType.LOCATION ) {
                        componentCount++;
                    }
                }
                for ( AgentStatusDescriptor agent : tsAgents ) {
                    agent.addNumPolicies( smartPolicyCount );
                    agent.setNumComponents( componentCount );
                }
            }
        } catch (EntityManagementException e) {
            throw new PolicyServiceException( e );
        } catch (PQLException e) {
            throw new PolicyServiceException( e );
        }
        return agents;
    }

    @Override
    public Collection<AgentStatusDescriptor> getAgentsForDeployedObject(
        Collection<AgentStatusDescriptor> agents
    , DomainObjectDescriptor obj
    , Date asOf
        ) throws PolicyServiceException {
        List<AgentStatusDescriptor> res = new ArrayList<AgentStatusDescriptor>(agents.size());
        IPredicate dt;
        Date from;
        Date to;
        try {
            Collection<DeploymentEntity> deps = lm.getDeploymentEntitiesForIDs(
                Arrays.asList(new Long[] { obj.getId() })
            , asOf
            , DeploymentType.PRODUCTION
                );
            if (deps.isEmpty()) {
                throw new IllegalArgumentException("Object for the specified descriptor does not exist.");
            }
            DeploymentEntity dep = (DeploymentEntity)deps.iterator().next();
            if ( obj.getType() == EntityType.POLICY ) {
                DomainObjectBuilder.OneObjectVisitor oov = new DomainObjectBuilder.OneObjectVisitor();
                DomainObjectBuilder.processInternalPQL( dep.getPql(), oov );
                IDPolicy policy = oov.getPolicy();
                if (policy == null) {
                    throw new AssertionError("Internal error: an object defined as policy in the data repository does not contain a valid policy.");
                }
                dt = policy.getDeploymentTarget();
            } else {
                dt = null;
            }
            from = dep.getActiveFrom();
            to = dep.getActiveTo();
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        } catch (PQLException e) {
            throw new PolicyServiceException(e);
        }
        for (AgentStatusDescriptor agent : agents) {
            if (agent.getLastUpdated() == null) {
                // This agent has never been updated
                continue;
            }
            AgentRequest agentRequest = new AgentRequest(agent.getId(), agent.getAgentType());
            if (dt != null && !dt.match(agentRequest)) {
                // This agent does not belong to the manual deployment set
                continue;
            }
            if (from != null && agent.getLastUpdated().before(from)) {
                // This agent has a timestamp before the activation time of the policy.
                continue;
            }
            if (to != null && !agent.getLastUpdated().before(to)) {
                // This agent has a timestamp on or after the deactivation time of the policy.
                continue;
            }
            res.add( agent );
        }
        return res;
    }

    @Override
    public Collection<DomainObjectDescriptor> deploymentStatusForAgent(AgentStatusDescriptor agent) throws PolicyServiceException {
        if ( agent == null ) {
            throw new NullPointerException("agent");
        }
        Date timeStamp = agent.getLastUpdated();

        // When there is nothing deployed on an agent, the timestamp is null.
        // We check this before proceeding with the rest of the logic:
        if ( timeStamp == null ) {
            return Collections.emptyList();
        }

        assert lm != null;
        List<DomainObjectDescriptor> res = new LinkedList<DomainObjectDescriptor>();
        try {
            List<DeploymentEntity> all = lm.getAllDeployedEntities( timeStamp, DeploymentType.PRODUCTION );
            DomainObjectBuilder.OneObjectVisitor oov = new DomainObjectBuilder.OneObjectVisitor();
            AgentRequest agentRequest = new AgentRequest(agent.getId(), agent.getAgentType());
            for ( DeploymentEntity dep : all ) {
                if (dep.isHidden() == true) {
                    continue;
                }
                if (dep.getType() == EntityType.POLICY) {
                    DomainObjectBuilder.processInternalPQL(dep.getPql(), oov);
                    IDPolicy policy = oov.getPolicy();
                    IPredicate dt = policy.getDeploymentTarget();
                    if (dt == null || dt.match(agentRequest)) {
                        res.add(makeDescriptor(dep));
                    }
                } else {
                    res.add(makeDescriptor(dep));
                }
            }
            return res;
        } catch (EntityManagementException e) {
            throw new PolicyServiceException( e );
        } catch (PQLException e) {
            throw new PolicyServiceException( e );
        }
    }

    @Override
    public boolean hasObjectsToDeploy() throws PolicyServiceException {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime( UnmodifiableDate.END_OF_TIME );
            cal.add( Calendar.MILLISECOND, -1 );
            return lm.getCountOfEntitiesReadyForDeployment(
                cal.getTime()
            ,   Arrays.asList( new EntityType[] {
                EntityType.ACTION
                ,   EntityType.APPLICATION
                ,   EntityType.HOST
                ,   EntityType.COMPONENT
                ,   EntityType.LOCATION
                ,   EntityType.POLICY
                ,   EntityType.PORTAL
                ,   EntityType.RESOURCE
                ,   EntityType.USER
            } ) ) != 0;
        } catch (EntityManagementException exception) {
            throw new PolicyServiceException(exception);
        }
    }

    /**
     * This method is for running client tests in a distributed environment.
     * This is a better alternative than running the same commands
     * in the client tests code, because it prevents unexpected use
     * of server-side libraries on the client.
     *
     */
    @Override
    public void prepareForTests() throws PolicyServiceException {
        try {
            preparePf();
            prepareAgentDb();
        } catch ( HibernateException he ) {
            throw new PolicyServiceException(he);
        }
    }

    /**
     * Prepares the PF database.
     *
     * Unlike other methods, the "prepare" methods talk
     * directly to Hibernate.
     *
     * @throws HibernateException in response to a DB-related error.
     */
    private void preparePf() throws HibernateException {
        IHibernateRepository hds = (IHibernateRepository) manager.getComponent( 
            DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName() );
        Session hs = hds.openSession();
        try {
            Transaction tx = hs.beginTransaction();
            hs.delete( "from DeploymentEntity" );
            hs.delete( "from DevelopmentEntity where hidden != 'Y'" );
            hs.delete( "from DeploymentRecord");
            tx.commit();
        } finally {
            hs.close();
        }
    }

    /**
     * Seeds ten agents of each kind into the management database.
     *
     * Unlike other methods, the "prepare" methods talk
     * directly to Hibernate.
     *
     * @throws HibernateException in response to a DB-related error.
     */
    private void prepareAgentDb() throws HibernateException {
        IHibernateRepository hds = (IHibernateRepository) manager.getComponent( 
            DestinyRepository.MANAGEMENT_REPOSITORY.getName());
        Session s = hds.openSession();
        try {
            Transaction t = s.beginTransaction();
            s.delete("from AgentDO");
            IAgentType[] agentTypes = new IAgentType[] {
            };
            //TODO dead code
            for ( IAgentType agentType : agentTypes ) {
                AgentDO agent = new AgentDO();
                agent.setType(agentType);
                s.save(agent);
            }
            t.commit();
        } finally {
            HibernateUtils.closeSession(s, null);
        }
    }

    @Override
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    @Override
    public IComponentManager getManager() {
        return manager;
    }

    @Override
    public void init() {
        lm = (LifecycleManager) manager.getComponent(LifecycleManager.COMP_INFO);

        manager.getComponent(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName());

        manager.registerComponent(ServerSpecManager.COMP_INFO, true);
        specManager = (ServerSpecManager) manager.getComponent(ServerSpecManager.COMP_INFO);

        manager.registerComponent(Dictionary.COMP_INFO, true);
        dictionary = manager.getComponent(Dictionary.COMP_INFO);

        indDataSourceManager = (IDataSourceManager) manager.getComponent(DataSourceManager.COMP_INFO);
        
        dictHelper = new DictionaryHelper(specManager, dictionary);

        getApplicationUserManager().addListener (new UserManagementObserverImpl ());

        final IDestinyConfigurationStore confStore = getManager().getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
        dpsConfig = ((IDPSComponentConfigurationDO) (confStore.retrieveComponentConfiguration(ServerComponentType.DPS.getName())));

        IApplicationUserManagerFactory appUserMgrFactory = manager.getComponent(ApplicationUserManagerFactoryImpl.class);
        appUserManager = appUserMgrFactory.getSingleton();

        lockMgr = new ObjectLockManager();
    }

    @Override
    public void setLog(Log log) {
        this.log = log;
    }

    @Override
    public Log getLog() {
        return log;
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, returns a <code>Collection</code> of corresponding entities.
     * @param descriptors a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects.
     * @return a <code>Collection</code> of corresponding entities.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    private Collection<String> getEntitiesForDescriptors(
        Collection<DomainObjectDescriptor> descriptors
        ) throws PolicyServiceException {
        try {
            List<Long> ids = new ArrayList<Long>( descriptors.size() );
            for ( DomainObjectDescriptor descriptor : descriptors ) {
                ids.add(descriptor.getId());
            }
            Collection<DevelopmentEntity> entities = lm.getEntitiesForIDs( ids );
            lm.resolveIds( entities );
            return PQLHelper.extractPQL( entities );
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    private static DomainObjectDescriptor makeDescriptor( DeploymentEntity dep ) {
        return new DomainObjectDescriptor(
            dep.getDevelopmentEntity().getId()
        ,   dep.getName()
        ,   dep.getDevelopmentEntity().getOwner()
        ,   dep.getDevelopmentEntity().getAccessPolicy()
        ,   dep.getDevelopmentEntity().getType()
        ,   dep.getDescription()
        ,   DevelopmentStatus.APPROVED
        ,   dep.getOriginalVersion()
        ,   dep.getDevelopmentEntity().getLastUpdated()
        ,   dep.getDevelopmentEntity().getCreated()
        ,   dep.getDevelopmentEntity().getLastModified()
        ,   dep.getDevelopmentEntity().getModifier()
        ,   dep.getDevelopmentEntity().getSubmittedTime()
        ,   dep.getDevelopmentEntity().getSubmitter()
        ,   false // Deployed entities are always visible
        ,   true  // Deployed entities are always accessible
        ,   false );
    }

    private static DomainObjectDescriptor makeDescriptor( DevelopmentEntity dev ) {
        return new DomainObjectDescriptor(
            dev.getId()
        ,   dev.getName()
        ,   dev.getOwner()
        ,   dev.getAccessPolicy()
        ,   dev.getType()
        ,   dev.getDescription()
        ,   dev.getStatus()
        ,   dev.getVersion()
        ,   dev.getLastUpdated()
        ,   dev.getCreated()
        ,   dev.getLastModified()
        ,   dev.getModifier()
        ,   dev.getSubmittedTime()
        ,   dev.getSubmitter()
        ,   dev.isHidden()
        ,   true
        ,   false );
    }

    /**
     * Change listener for appuser or accessgroup delete actions.  Goes through policy/component
     * objects and cleans out references to the appuser/accessgroups
     */
    private class UserManagementObserverImpl implements IUserManagementObserver {

        public long USER_DELETED = 1;
        public long GROUP_DELETED = 2;

        /**
         * Constructor.
         *
         */
        private UserManagementObserverImpl() {
        }

        @Override
        public void onUserDelete (long appUserId) {
            deleteIdReference (appUserId, USER_DELETED);
        }
        
        @Override
        public void onGroupDelete (long accessGroupId) {
            deleteIdReference (accessGroupId, GROUP_DELETED);
        }

        private void deleteIdReference (final long longId, final long which) {
            final Long id = new Long (longId);

            try {
                Collection<DevelopmentEntity> allEntities = lm.getAllEntitiesOfType(EntityType.elements());
                Collection<DevelopmentEntity> newEntities = new HashSet<DevelopmentEntity>();

                for ( final DevelopmentEntity dev : allEntities ) {
                    String apPql = dev.getApPql ();
                    String pql = dev.getPql ();

                    if (apPql != null && apPql.length () != 0) {
                        DomainObjectBuilder dob = new DomainObjectBuilder (apPql);
                        IAccessPolicy ap = dob.processAccessPolicy ();
                        if (which == USER_DELETED) {
                            ap.setActionsForUser(id, EMPTY_ACTION_SET);
                        } else {
                            ap.setActionsForGroup(id, EMPTY_ACTION_SET);
                        }
                        DomainObjectFormatter dof = new DomainObjectFormatter ();
                        dof.formatAccessPolicy (ap);
                        dev.setApPql (dof.getPQL ());
                    }

                    if (pql != null && pql.length () != 0) {
                        DomainObjectBuilder.processInternalPQL (pql, new IPQLVisitor () {
                            private IDSpec spec;

                            @Override
                            public void visitPolicy (DomainObjectDescriptor descr, IDPolicy policy) {
                                IAccessPolicy ap = descr.getAccessPolicy ();

                                try {
                                    if (which == USER_DELETED) {
                                        ap.setActionsForUser(id, EMPTY_ACTION_SET);
                                    } else {
                                        ap.setActionsForGroup(id, EMPTY_ACTION_SET);
                                    }
                                    policy.setAccessPolicy ((AccessPolicy) ap);
                                    DomainObjectFormatter dof = new DomainObjectFormatter ();
                                    dof.formatDef (policy);
                                    dev.setPql (dof.getPQL ());
                                }
                                catch (PQLException e) {
                                    log.error ("Error deleting object with destiny id " + longId + " from store");
                                }
                            }
                            
                            @Override
                            public void visitFolder(DomainObjectDescriptor descriptor) {
                                // Policy Folders are not relevant
                            }
                            
                            @Override
                            public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                                IAccessPolicy ap = descr.getAccessPolicy();

                                try {
                                    if (which == USER_DELETED) {
                                        ap.setActionsForUser(id, EMPTY_ACTION_SET);
                                    } else {
                                        ap.setActionsForGroup(id, EMPTY_ACTION_SET);
                                    }
                                    
                                    DomainObjectDescriptor descrWithNewAccessPolicy = 
                                        new DomainObjectDescriptor(
                                            descr.getId()
                                        ,   descr.getName()
                                        ,   descr.getOwner() 
                                        ,   ap 
                                        ,   descr.getType()
                                        ,   descr.getDescription() 
                                        ,   descr.getStatus()
                                        ,   descr.getVersion()
                                        ,   descr.getLastUpdated()
                                        ,   descr.getWhenCreated() 
                                        ,   descr.getLastModified()
                                        ,   descr.getModifier()
                                        ,   descr.getLastSubmitted()
                                        ,   descr.getSubmitter()
                                        ,   descr.isHidden() 
                                        ,   descr.isAccessible()
                                        ,   false);
                                    
                                    DomainObjectFormatter dof = new DomainObjectFormatter();
                                    dof.formatDef(descrWithNewAccessPolicy, pred);
                                    dev.setPql(dof.getPQL());
                                } catch (PQLException e) {
                                    log.error("Error deleting object with destiny id " + longId + " from store");
                                }
                            }
                                
                            @Override
                            public void visitAccessPolicy(DomainObjectDescriptor descr, IAccessPolicy accessPolicy) {
                                // Access Policies are not relevant
                            }
                            
                            @Override
                            public void visitLocation(DomainObjectDescriptor descr, Location location) {
                                // Locations are not relevant
                            }

                        });
                    }
                    newEntities.add (dev);
                }
                //                for (DevelopmentEntity e : newEntities) {
                //                    e.setModifier(0L);
                //                }
                lm.saveEntities (newEntities, new MakerOfEntitiesWithAccess (
                                     new Long(0)
                                 , getDefaultAPPqlForUser(new Long(0)))
                                 , null);
            } catch (EntityManagementException e) {
                log.error ("Error deleting object with destiny id " + longId + " from store");
            } catch (PQLException e) {
                log.error ("Error deleting object with destiny id " + longId + " from store");
            } catch (PolicyServiceException pse) {
                log.error ("Error deleting object with destiny id " + longId + " from store");
            }

            try {
                Set<IAccessGroup> accessGroups = getApplicationUserManager().getAccessGroups(null, 0);
                for ( IAccessGroup group : accessGroups ) {
                    String ap = group.getApplicableAccessControl();
                    AccessPolicyComponent apc = new AccessPolicyComponent (ap);

                    if (which == USER_DELETED) {
                        apc.setActionsForUser(id, EMPTY_ACTION_SET);
                    } else {
                        apc.setActionsForGroup(id, EMPTY_ACTION_SET);
                    }
                    getApplicationUserManager().setDefaultAccessControlAssignmentForGroup(
                        group.getDestinyId().longValue()
                    , apc.toPQL()
                        );
                }
            } catch (UserManagementAccessException e) {
                log.error ("User management exception while changing accessgroup default pql for deletion of object id " + longId);
            } catch (PQLException pe) {
                log.error ("PQL Exception in changing accessgroup default pql for deletion of object id " + longId);
            } catch (AccessGroupModificationFailedException agmfe) {
                log.error ("Error updating accessgroup default pql for deletion of object id " + longId);
            } catch (GroupNotFoundException gnfe) {
                log.error ("Internal error: "+gnfe.getMessage());
            }
        }
    }

    @Override
    public Collection<LeafObject> getMatchingSubjects(
        DomainObjectDescriptor descr
        ) throws PolicyServiceException, CircularReferenceException {
        try {
            if ( descr == null ) {
                throw new NullPointerException("descriptor");
            }
            final Map<Long,IHasId> byId = specManager.getDependentsMapById( descr );
            
            // Get the predicate
            IPredicate pred = ((SpecBase)byId.get(descr.getId())).getPredicate();
            try {
                LeafObjectType type = getLeafObjectTypeFromComponentName(descr.getName());
                return dictHelper.getMatchingSubjects( pred, type, byId, -1);
            } catch (IllegalArgumentException e) {
                throw new DictionaryException(e);
            }
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        } catch (PQLException e) {
            throw new PolicyServiceException(e);
        } catch (CircularReferenceException e) {
            throw new PolicyServiceException(e);
        } catch ( DictionaryException e ) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DomainObjectDescriptor> getDependencies(
        Collection<DomainObjectDescriptor> descriptors
    , boolean directOnly
        ) throws PolicyServiceException, CircularReferenceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        Long did =loggedInUser.getDestinyId();
        String userName = loggedInUser.getUserName();
        try {
            return specManager.getDependencies(descriptors, directOnly, new AccessCheckingConverter( userName, did ) );
        } catch (PQLException e) {
            throw new PolicyServiceException(e);
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Collection<DODDigest> getDependenciesDigest(
        Collection<DODDigest> digests
    , boolean directOnly
        ) throws PolicyServiceException, CircularReferenceException {
        ApplicationUser loggedInUser = getLoggedInUserFromSecureSession();
        Long did =loggedInUser.getDestinyId();
        String userName = loggedInUser.getUserName();
        try {
            return specManager.getDependenciesDigest(digests, directOnly, new AccessCheckingDigestConverter( userName, did ) );
        } catch (PQLException e) {
            throw new PolicyServiceException(e);
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
    }

    /**
     * Inspects PQL to determine if there are references to other components
     * @param pql the pql
     * @return true if there are references, false if not
     */
    private boolean hasReferences(String pql) {
        final boolean foundReference[] = {false};
            
        final Predicates.IDetector checkForReference = new Predicates.DefaultDetector() {
            @Override
            public boolean checkReference(IPredicateReference p) {
                return true;
            }};
        
        try {
            DomainObjectBuilder.processInternalPQL( pql, 
                                                    new DefaultPQLVisitor() {
                                                        @Override
                                                        public void visitComponent(DomainObjectDescriptor dod, IPredicate spec) {
                                                            if (Predicates.find(spec, checkForReference)) {
                                                                foundReference[0] = true;
                                                            }
                                                        }
                                                        @Override
                                                        public void visitPolicy(DomainObjectDescriptor dod, IDPolicy pol) {
                                                            if (Predicates.find(pol.getTarget().getActionPred(), checkForReference) ||
                                                                Predicates.find(pol.getTarget().getFromResourcePred(), checkForReference) ||
                                                                Predicates.find(pol.getTarget().getToResourcePred(), checkForReference) ||
                                                                Predicates.find(pol.getTarget().getSubjectPred(), checkForReference) ||
                                                                Predicates.find(pol.getTarget().getToSubjectPred(), checkForReference) ||
                                                                Predicates.find(pol.getConditions(), checkForReference)) {
                                                                foundReference[0] = true;
                                                            }
                                                        }
                                                    });
        } catch ( PQLException e ) {
            // Nothing.  We are processing it to find out if it has any references.  We'll assume not.
        }
        
        return foundReference[0];
    }


    private static class MakerOfEntitiesWithAccess implements LifecycleManager.NewEntityMaker {
        private final Long owner;
        private final String acPql;
        public MakerOfEntitiesWithAccess( Long owner, String acPql ) {
            if ( owner == null ) {
                throw new NullPointerException( "owner" );
            }
            if ( acPql == null ) {
                throw new NullPointerException( "acPql" );
            }
            this.owner = owner;
            // FIXME -= Hack alert =-
            // The reason the code below removes access_control is that
            // sometimes the string is included, and some times it is not,
            // depending on the source of the default. This hack deals with
            // the problem by removing an extra access_control. The real fix
            // should ensure that all sources of the access control PQL
            // follow the same basic structure.
            this.acPql = acPql.replaceFirst("access_control", "").replaceFirst("ACCESS_CONTROL", "");
        }
        
        @Override
        public String makeNewEntity(Long id, String name, EntityType type) throws EntityManagementException {
            StringBuffer res = new StringBuffer( 128 );
            res.append( "ID " );
            res.append( id );
            res.append( " STATUS NEW CREATOR \"" );
            res.append( owner.toString () );
            res.append( "\" " );
            res.append( "ACCESS_POLICY ACCESS_CONTROL\n");
            res.append( acPql );
            res.append( "\n" );
            res.append( "ALLOWED_ENTITIES\n");
            res.append( type.emptyPql( name ) );
            return res.toString();
        }
    }

    // Used by getOwnerNamesMap.  We just need a big number
    static private int maxPossibleOwners = 10000;

    private class DefaultDigestConverter implements LifecycleManager.DigestMaker {
        private Map<Long, String> appUserIdNameMap;
        private Map<Long, Long> idVersionMap;
        private Map<Long, DomainObjectUsage> idUsageMap = new HashMap<Long, DomainObjectUsage>();

        public DefaultDigestConverter() {
            try {
                appUserIdNameMap = getAppUserNamesMap();
            } catch (PolicyServiceException e) {
                log.error("exception in setUniqueOwnerIds " + e);
            }
        }
        
        @Override
        public DODDigest convert(DevelopmentEntity dev) {
            return convert(dev, true);
        }
        
        @Override
        public void computeObjectUsage(List<Long> entityIds) {
            int idx = 0;
            try {
                for (DomainObjectUsage dou : getUsageListById(entityIds)) {
                    idUsageMap.put(entityIds.get(idx++), dou);
                }
            } catch (PolicyServiceException e) {
                log.error("exception in computeObjectUsage " + e);
            }
        }

        private DomainObjectUsage getUsage(Long id) {
            return idUsageMap.get(id);
        }

        private int getDestinyVersion(Long id) {
            Collection<DeploymentHistory> hist = null;
            try {
                hist = getDeploymentHistory(id);
            } catch (PolicyServiceException pse) {
                // Ignore it, we return 0 below
            }

            return (hist == null) ? 0 : hist.size();
        }
        
        protected DODDigest convert(DevelopmentEntity dev, boolean isAccessible) {
            try {
                return new DODDigest(
                    dev.getId()
                , getNamedType(dev.getType(), dev.getName())
                , dev.getName()
                , isAccessible
                , dev.hasDependencies()
                , dev.isASubPolicy()
                , dev.getVersion()
                , getDestinyVersion(dev.getId())
                , dev.getOwner()
                , getAppUserName(dev.getOwner())
                , dev.getLastUpdated()
                , dev.getLastModified()
                , dev.getModifier()
                , getAppUserName(dev.getModifier())
                , dev.getSubmittedTime()
                , dev.getSubmitter()
                , getAppUserName(dev.getSubmitter())
                , dev.getStatus()
                , getUsage(dev.getId())
                    );
            } catch (PolicyServiceException e) {
                //FIXME log exception!
                // Uh oh
                return null;
            }

        }

        private String getNamedType(EntityType type, String name) {
            if (type == EntityType.POLICY || type == EntityType.FOLDER) {
                return type.getName();
            }

            // Distinguish between the various COMPONENT types by name
            int idx = name.indexOf('/');
            if (idx == -1) {
                // Shouldn't happen
                return EntityType.COMPONENT.getName();
            } else {
                return name.substring(0, idx);
            }
        }

        private String getAppUserName(Long did) throws PolicyServiceException {
            if (did == null) {
                return null;
            }
            String cachedName = appUserIdNameMap.get(did);
            if (cachedName == null) {
                cachedName = "Unknown";
                appUserIdNameMap.put(did, cachedName);
            }
            return cachedName;
        }

        // We really should cache this (Policy Studio does (or did)).  This is a list of all the application users.
        private Map<Long, String> getAppUserNamesMap() throws PolicyServiceException {
            Collection<LeafObject> results = 
                queryLeafObjects(
                    SubjectAttribute.USER_NAME.buildRelation(
                        RelationOp.EQUALS, Constant.build(""))
                , null
                , LeafObjectType.APPUSER
                , maxPossibleOwners);
            
            Map<Long, String> mapping = new HashMap<Long, String>();

            for (LeafObject lobj : results) {
                mapping.put(lobj.getId(), lobj.getName());
            }

            try {
                // Add the super-user
                IApplicationUser superUser = getApplicationUserManager().getSuperUser();
                mapping.put(superUser.getDestinyId(), superUser.getDisplayName());
            } catch(UserManagementAccessException umae) {
                getLog().error("Failed to locate superuser");
            }
            
            return mapping;
        }
    }

    private class AccessCheckingDigestConverter extends DefaultDigestConverter {
        private IDSubject who;
        private Long did;
        private boolean isSuperuser;
        private Map<String, IAccessPolicy> apPqlMap = new HashMap<String, IAccessPolicy>();

        public AccessCheckingDigestConverter(String userName, Long did) {
            super();

            IApplicationUserManager aum = getApplicationUserManager();
            this.did = did;
            try {
                isSuperuser = aum.getSuperUser().getDestinyId().equals( did );
                if ( !isSuperuser && did != null ) {
                    who = makeAccessibleSubject(userName, did);
                }
            } catch ( UserNotFoundException unfe ) {
                getLog().error("Failed to locate user with id, " + did + ".  Access to entity will be disallowed");
            } catch ( UserManagementAccessException umae ) {
                getLog().error("Failed to locate groups for user with id, " + did + ".  Access to entity will be disallowed");
            }

        }

        @Override
        public DODDigest convert(DevelopmentEntity dev) {
            IAccessPolicy ap = getAccessPolicy(dev);
            boolean isAccessible = 
                isSuperuser 
                || ( ap == null ) 
                || (
                    (who != null)  
                && (ap.checkAccess(makeSyntheticOwner(dev.getOwner()), who, (DAction)DAction.READ ))
                    );
            return convert(dev, isAccessible);
        }

        private IAccessPolicy getAccessPolicy(DevelopmentEntity dev) {
            if (dev == null) {
                return null;
            }
            String apPql = dev.getApPql();
            IAccessPolicy accessPolicy = apPqlMap.get(apPql);
            if (accessPolicy == null) {
                accessPolicy = dev.getAccessPolicy();
                apPqlMap.put(apPql, accessPolicy);
            }
            return accessPolicy;
        }
    }

    /**
     * Converts <code>DevelopmentEntity</code> objects to
     * <code>DomainObjectdescriptor</code> objects, paying attention to
     * the access control of each converted entity.
     */
    private class AccessCheckingConverter implements LifecycleManager.DescriptorMaker {
        private IDSubject who;
        private boolean isSuperuser;
        public AccessCheckingConverter( String userName, Long did ) {
            IApplicationUserManager aum = getApplicationUserManager();
            try {
                isSuperuser = aum.getSuperUser().getDestinyId().equals( did );
                if ( !isSuperuser && did != null ) {
                    who = makeAccessibleSubject(userName, did);
                }
            } catch ( UserNotFoundException unfe ) {
                getLog().error("Failed to locate user with id, " + did + ".  Access to entity will be disallowed");
            } catch ( UserManagementAccessException umae ) {
                getLog().error("Failed to locate groups for user with id, " + did + ".  Access to entity will be disallowed");
            }
        }
        
        @Override
        public DomainObjectDescriptor convert( DevelopmentEntity dev ) {
            if ( dev == null ) {
                return null;
            }
            IPResource owner = makeSyntheticOwner(dev.getOwner());
            IAccessPolicy ap = dev.getAccessPolicy();

            return new DomainObjectDescriptor(
                dev.getId()
            ,   dev.getName()
            ,   dev.getOwner()
            ,   ap
            ,   dev.getType()
            ,   dev.getDescription()
            ,   dev.getStatus()
            ,   dev.getVersion()
            ,   dev.getLastUpdated()
            ,   dev.getCreated()
            ,   dev.getLastModified()
            ,   dev.getModifier()
            ,   dev.getSubmittedTime()
            ,   dev.getSubmitter()
            ,   dev.isHidden()
            ,   isSuperuser
            || ( ap == null )
            || ((who != null)  &&  (ap.checkAccess(owner, who, (DAction)DAction.READ )))
            ,   hasReferences(dev.getPql()));
        }
    }

    @Override
    public List<DomainObjectUsage> getUsageList( List<DomainObjectDescriptor> descriptors ) throws PolicyServiceException {
        List<Long> ids = new ArrayList<Long>( descriptors.size() );
        for ( DomainObjectDescriptor dod : descriptors ) {
            ids.add(dod.getId());
        }

        return getUsageListById(ids);
    }

    @Override
    public List<DomainObjectUsage> getUsageListById( List<Long> ids ) throws PolicyServiceException {
        try {
            Collection<DeploymentEntity> deployedAsOfNow = lm.getDeploymentEntitiesForIDs(
                ids
            ,   new Date()
            ,   DeploymentType.PRODUCTION
                );
            Map<Long,Long> idToDeployedVersionNow = new HashMap<Long,Long>();
            for ( DeploymentEntity dep : deployedAsOfNow ) {
                idToDeployedVersionNow.put( dep.getDevelopmentEntity().getId(), (long)dep.getOriginalVersion() );
            }
            Collection<DeploymentEntity> deployedAsOfEOT = lm.getDeploymentEntitiesForIDs(
                ids
            ,   new Date( UnmodifiableDate.END_OF_TIME.getTime() - 1 )
            ,   DeploymentType.PRODUCTION
                );
            Map<Long,Long> idToDeployedVersionEOT = new HashMap<Long,Long>();
            for ( DeploymentEntity dep : deployedAsOfEOT ) {
                idToDeployedVersionEOT.put( dep.getDevelopmentEntity().getId(), (long)dep.getOriginalVersion());
            }
            Set<Long> withReferences = new HashSet<Long>( lm.checkHasReferences( ids ) );
            Set<Long> withDeployments = new HashSet<Long>( lm.checkHasDeployments( ids, DeploymentType.PRODUCTION ) );
            Set<Long> withFutureDeployments = new HashSet<Long>( lm.checkHasFutureDeployments( ids, DeploymentType.PRODUCTION ) );
            List<DomainObjectUsage> res = new ArrayList<DomainObjectUsage>( ids.size() );
            for ( Long id : ids ) {
                res.add(
                    new DomainObjectUsage(
                        withReferences.contains( id )
                    ,   withDeployments.contains( id )
                    ,   withFutureDeployments.contains( id )
                    ,   (Long)idToDeployedVersionNow.get( id )
                    ,   (Long)idToDeployedVersionEOT.get( id )
                        )
                    );
            }
            return res;
        } catch (EntityManagementException e ) {
            throw new PolicyServiceException(e);
        }
    }

    @Override
    public Set<String> getDictionaryEnrollmentNames() throws PolicyServiceException {
        Set<String> res = new HashSet<String>();
        try {
            Collection<? extends IEnrollment> enrollments = this.dictionary.getEnrollments();
            for ( IEnrollment nextEnrollment : enrollments ) {
                res.add(nextEnrollment.getDomainName());
            }
        } catch (DictionaryException exception) {
            throw new PolicyServiceException("Failed to retrieve dictionary enrollments.", exception);
        }
        return res;
    }

    @Override
    public Set<Realm> getDictionaryEnrollmentRealms() throws PolicyServiceException {
        Set<Realm> res = new HashSet<Realm>();
        try {
            Collection<? extends IEnrollment> enrollments = this.dictionary.getEnrollments();
            for ( IEnrollment nextEnrollment : enrollments ) {
                res.add( makeRealm(nextEnrollment));
            }
        } catch (DictionaryException exception) {
            throw new PolicyServiceException("Failed to retrieve dictionary enrollments.", exception);
        }
        return res;
    }

    private Realm makeRealm(IEnrollment enrollment) throws DictionaryException {
        Realm res = new Realm();
        res.setName(enrollment.getDomainName());        
        try {            
            String enrollmentType = enrollment.getType();
            EnrollmentType type = EnrollmentType.fromString(enrollmentType);
            res.setType(type);
        }
        catch (IllegalArgumentException e) {
            // unknown enrollment type
            res.setType(EnrollmentType.fromString("com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.unknown.UnknownEnroller")); 
        }
        catch (Exception e) {
            log.error("can not make realm, reason: " + e);
            throw new DictionaryException("Unknow realm type " + enrollment.getType());
        }  
        return res;
    }
    
    /**
     * Get Portal URL list
     */
    @Override
    public String[] getPortalURLList() throws PolicyServiceException {
        List < String > result = new ArrayList<String>();
        try {
            Collection<? extends IEnrollment> enrollments = this.dictionary.getEnrollments();
            for ( IEnrollment enrollment : enrollments ) {
                String className = EnrollmentTypeEnumType.PORTAL.getClassName();
                if ( enrollment.getType().equals(className)) {
                    String[] properties = enrollment.getStrArrayProperty(SharePointEnrollmentProperties.PORTALS_PROPERTY);
                    for( int i=0; i<properties.length; i++ ) {
                        result.add(properties[i]);
                    }
                }
            }
            return result.toArray(new String[]{});
        }
        catch ( DictionaryException e ) {
            log.error("Can not get portal URLs: " + e);
            throw new PolicyServiceException("Can not get portal URLs " + e);
        }
    }
    
    /**
     * Create External Data Source 
     */
    @Override
    public int createExternalDataSource(ExternalDataSourceConnectionInfo info) throws PolicyServiceException {
        try {
            IDataSource source = indDataSourceManager.createDataSource(info.getType().getValue());
            IDataSourceConnection connection = new DataSourceConnection(
                info.getURL(), info.getUserName(), info.getPassWord(), info.getDomain() );
            source.setConnection(connection);
            return indDataSourceManager.addDataSource(source);
        }
        catch (INDException e) {
            throw new PolicyServiceException(e);
        }
    }
    
    /**
     * Get Resource Tree Node
     */
    @Override
    public ResourceTreeNode getResourceTreeNodeChildren(int resourceID, ResourceTreeNode node) throws PolicyServiceException {
        if ( node == null ) {
            throw new PolicyServiceException("Resource Tree Node is null");
        }
        
        IDataSource source = indDataSourceManager.getDataSoruceByID(resourceID);
        if ( source == null ) {
            throw new PolicyServiceException("Invalid data source ID " + resourceID);
        }
        
        IExternalResource resource = makeExternalResourceFromResourceTreeNode(node);
        
        try {
            source.getResourceTreeNodeChildren(resource);
        }
        catch(INDException e) {
            throw new PolicyServiceException(e);
        }
        
        node = makeResourceTreeNodeFromExternalResource(resource);
        
        return node;
    }
    
    
    /**
     * Get Portal Resource Preview
     */
    @Override
    public ResourceTreeNodeList getPortalResourcePreview(
        int resourceID
    , DomainObjectDescriptor descriptor
        ) throws PolicyServiceException {
        
        IDataSource source = indDataSourceManager.getDataSoruceByID(resourceID);
        if ( source == null ) {
            throw new PolicyServiceException("Invalid data source ID " + resourceID);
        }
        
        try {
            List<IExternalResource> result = source.getResourcePreview(descriptor);
            if ( result != null ) {
                List<ResourceTreeNode> listOfnodes = new ArrayList<ResourceTreeNode>();              
                for(IExternalResource resource: result) {
                    listOfnodes.add(makeResourceTreeNodeFromExternalResource(resource));
                }
                return new ResourceTreeNodeList(listOfnodes.toArray(new ResourceTreeNode[]{}));
            }
        }
        catch(Exception e) {
            throw new PolicyServiceException(e);
        }
        
        return null;
    }
    
    /**
     * Converting ResourceTreeNode to ExternalResource
     * @param node ResourceTreeNode
     * @return ExternalResource
     */
    public static IExternalResource makeExternalResourceFromResourceTreeNode(ResourceTreeNode node) {
        IExternalResource resource = new ExternalResource();
        resource.setType(node.getType());
        resource.setName(node.getName());
        resource.setID(node.getId());
        resource.setURL(node.getUrl());
        resource.setHasChildren(node.isHasChildren());
        ResourceTreeNode[] children = node.getChildren();
        if ( children == null ) {
            return resource;
        }
        List<IExternalResource> listOfChildren = new ArrayList<IExternalResource>();
        for(int i=0; i<children.length; i++) {
            listOfChildren.add(makeExternalResourceFromResourceTreeNode(children[i]));
        }
        resource.setChildren(listOfChildren.toArray(new IExternalResource[]{}));
        return resource;
    }
    
    /**
     * Converting ExternalResource to {@link ResourceTreeNode}
     * @param resource IExternalResource
     * @return node: ResourceTreeNode
     */
    public static ResourceTreeNode makeResourceTreeNodeFromExternalResource(IExternalResource resource) {
        ResourceTreeNode node = new ResourceTreeNode();
        node.setType(resource.getType());
        node.setName(resource.getName());
        node.setId(resource.getID());
        node.setUrl(resource.getURL());
        node.setHasChildren(resource.hasChildren());
        IExternalResource[] children = resource.getChildren();
        if ( children == null ) {
            return node;
        }
        List<ResourceTreeNode> listOfChildren = new ArrayList<ResourceTreeNode>();
        for(int i=0; i<children.length; i++) {
            listOfChildren.add(makeResourceTreeNodeFromExternalResource(children[i]));
        }
        node.setChildren(listOfChildren.toArray(new ResourceTreeNode[]{}));
        return node;
    }
    
    private static abstract class SearchSpecBuilder<T> implements IPredicateVisitor {

        private List<T> res = new ArrayList<T>(4);

        public List<T> getResult() {
            return res;
        }

        @Override
        public void visit(ICompositePredicate pred, boolean preorder) {
            if (pred.getOp() != BooleanOp.OR) {
                throw new IllegalArgumentException("Only OR-red specs are supported");
            }
        }
        
        @Override
        public void visit(IPredicate pred) {
            throw new IllegalArgumentException("Unknown predicate: "+pred);
        }
        
        @Override
        public void visit(IPredicateReference pred) {
            throw new IllegalArgumentException(
                "Predicate references are not supported in application user manager searches."
                );
        }
        
        @Override
        public void visit(IRelation rel) {
            IExpression lhs = rel.getLHS();
            IExpression rhs = rel.getRHS();
            if (lhs instanceof IAttribute && rhs instanceof Constant) {
                res.add(makeSpec((IAttribute)lhs, (Constant)rhs));
            } else if (lhs instanceof Constant && rhs instanceof IAttribute) {
                res.add(makeSpec((IAttribute)rhs, (Constant)lhs));
            } else {
                throw new IllegalArgumentException("Unexpected relation: "+rel);
            }
        }

        protected abstract T makeSpec(IAttribute attr, Constant constant);

    }

    /* Given a did, compute a user subject appropriate for doing accessibility calculations */
    private IDSubject makeAccessibleSubject(String userName, Long did) throws UserNotFoundException, UserManagementAccessException {
        Set<IAccessGroup> accessGroups = appUserManager.getAccessGroupsContainingUser(did.longValue());
        List<Long> ids = new ArrayList<Long>();
        for ( IAccessGroup group : accessGroups ) {
            ids.add( group.getDestinyId() );
        }
        return new AccessibleSubject(userName,
                                     userName,
                                     userName,
                                     did,
                                     EvalValue.build(Multivalue.create(ids)),
                                     SubjectType.USER);
    }
    private static IPResource makeSyntheticOwner(Long ownerId) {
        final IDSubject owner = new Subject("<ignored>", "<ignored>", "<ignored>", ownerId, SubjectType.USER);
        return new IPResource() {
            @Override
            public IDSubject getOwner() {
                return owner;
            }
        };
    }
    
    private static LeafObjectType getLeafObjectTypeFromComponentName(String name) {        
        if ( name != null ) {
            int loc = name.indexOf(PQLParser.SEPARATOR);
            if (  loc > 0 ) {
                String typeName = name.substring(0, loc).toUpperCase();
                return LeafObjectType.forName(typeName);
            }
        }
        return null;
    }
}
