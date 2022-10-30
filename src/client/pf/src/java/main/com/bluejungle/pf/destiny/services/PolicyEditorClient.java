package com.bluejungle.pf.destiny.services;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc. All rights reserved
 * worldwide.
 *
 * @author sergey
 *
 * @version $Id:
 *          //depot/main/Destiny/main/src/client/pf/src/java/main/com/bluejungle/pf/destiny/services/PolicyEditorClient.java#67 $
 */

import java.io.File;
import java.io.Serializable;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.rpc.ServiceException;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoggedInUser;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.ILoginMgr;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginInfoImpl;
import com.bluejungle.destiny.appframework.appsecurity.loginmgr.remote.RemoteLoginManager;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.policy.InvalidPasswordFault;
import com.bluejungle.destiny.services.policy.PolicyEditorIF;
import com.bluejungle.destiny.services.policy.PolicyEditorLocator;
import com.bluejungle.destiny.services.policy.types.CircularReferenceFault;
import com.bluejungle.destiny.services.policy.types.DeploymentRecordDTO;
import com.bluejungle.destiny.services.policy.types.DomainObjectEnum;
import com.bluejungle.destiny.services.policy.types.EntityDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceType;
import com.bluejungle.destiny.services.policy.types.LockRequestType;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.destiny.services.policy.types.RealmList;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNode;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNodeList;
import com.bluejungle.destiny.services.policy.types.StringList;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.framework.patterns.EnumBase;
import com.bluejungle.framework.utils.StringUtils;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lib.DTOUtils;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lifecycle.AttributeDescriptor;
import com.bluejungle.pf.destiny.lifecycle.AttributeType;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.ObligationDescriptor;
import com.bluejungle.pf.destiny.lifecycle.PolicyActionsDescriptor;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.action.ActionAttribute;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecManager;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.policy.PolicyFolder;
import com.bluejungle.pf.domain.destiny.resource.AgentResourceManager;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.AccessibleSubject;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.misc.IAccessControlled;
import com.bluejungle.pf.domain.epicenter.resource.IMResource;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.pf.engine.destiny.DefaultFileResourceHandler;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;

/**
 * This is a client-side library for working with the Policy Editor. This
 * library uses web services to connect to the server, and presents an easy
 * interface to its clients.
 */

public class PolicyEditorClient implements IPolicyEditorClient, IInitializable, IConfigurable{

    private Map<EntityType,List<AttributeDescriptor>> typeToAttributeList = new HashMap<EntityType,List<AttributeDescriptor>>();
    private Map<EntityType,List<AttributeDescriptor>> typeToCustomAttributeList = new HashMap<EntityType,List<AttributeDescriptor>>();

    /** Web service locator */
    private PolicyEditorLocator locator = new PolicyEditorLocator();

    private ILoggedInUser loggedInUser;
    private IDSubject who;

    /** entities the logged-in user is allowed to access */
    private Collection<EntityType> whoAllowedEntities;

    private LeafObject superUser;

    private BigInteger INVALID_ID = BigInteger.valueOf(-1);

    private IConfiguration configuration;
    
    private boolean loggedIn;
    
    private static final String SOCKET_FACTORY_PROPERTY = "axis.socketSecureFactory";
    
    private String location;
    private String username; 
    private String password;
    
    public static final ComponentInfo<PolicyEditorClient> COMP_INFO = new ComponentInfo<PolicyEditorClient>(
			"policyEditorClient", 
			PolicyEditorClient.class,
			IPolicyEditorClient.class, 
			LifestyleType.SINGLETON_TYPE);
    

    private static final Collection<String> BUILTIN_ROLE_NAMES = Arrays.asList(
        new String[] {
            "Policy Analyst"
        ,   "Policy Administrator"
        ,   "Business Analyst"
        ,   "ADMIN"
        }
    );

    private static final Collection<DAction> ALL_ACTIONS = Arrays.asList(
        new DAction[] {
            (DAction) DAction.WRITE
        ,   (DAction) DAction.ADMIN
        ,   (DAction) DAction.APPROVE
        ,   (DAction) DAction.DEPLOY
        ,   (DAction) DAction.READ
        ,   (DAction) DAction.RENAME
        ,   (DAction) DAction.DELETE
        }
    );

    private static final Collection<DAction> EDIT_ACTIONS = Arrays.asList(
        new DAction[] {
            (DAction) DAction.WRITE
        ,   (DAction) DAction.RENAME
        ,   (DAction) DAction.DELETE
        }
    );

    public void login() throws LoginException {
    	if(isLoggedIn()){
    		//do nothing and return;
    		return;
    	}
    	
        if (location == null) {
            throw new NullPointerException("location");
        }
        if (username == null) {
            throw new NullPointerException("username");
        }
        if (password == null) {
            throw new NullPointerException("password");
        }

        System.setProperty(SOCKET_FACTORY_PROPERTY, PolicyEditorSocketFactory.class.getName());

        locator.setPolicyEditorIFPortEndpointAddress(location + "/dps/services/PolicyEditorIFPort");

        // Verify that we can login; throw an exception if we cannot.
        HashMapConfiguration componentConfig = new HashMapConfiguration();
        componentConfig.setProperty(RemoteLoginManager.SECURE_SESSION_SERVICE_ENDPOINT_PROP_NAME, location + "/dps/services/SecureSessionService");
        ComponentInfo componentInfo = new ComponentInfo(ILoginMgr.COMP_NAME, RemoteLoginManager.class.getName(), ILoginMgr.class.getName(), LifestyleType.TRANSIENT_TYPE, componentConfig);
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        componentManager.registerComponent(componentInfo, true);
        RemoteLoginManager rlm = (RemoteLoginManager) componentManager.getComponent(componentInfo);

        if (rlm == null) {
            throw new NullPointerException("RemoteLoginManager");
        }
        
        LoginInfoImpl info = new LoginInfoImpl();
        info.setUserName(username);
        info.setPassword(password);

        //may throw LoginExcpetion when login
        this.loggedInUser = rlm.login(info);
        loggedIn = true;
        
        // Load roles for access control from the server
        try {
            Collection<? extends IHasId> builtInUsers = getEntitiesForNamesAndType(BUILTIN_ROLE_NAMES, EntityType.COMPONENT, true);
            IDSpecManager sm = (IDSpecManager) componentManager.getComponent(IDSpecManager.COMP_INFO);
            if (builtInUsers != null) {
                for (Iterator<? extends IHasId> iter = builtInUsers.iterator(); iter.hasNext(); sm.saveSpec((IDSpec) iter.next()))
                    ;
            }
        } catch (PolicyEditorException e) {
            // TODO: Downloading from the server failed - create empty roles.
        }

    }

    public IDSubject getLoggedInUser() throws PolicyEditorException {
        if (who == null) {
            Long whoPrincipalId = loggedInUser.getPrincipalId();
            IEvalValue whoAccessGroups = retrieveAccessGroupsForUser(whoPrincipalId);
            who = new AccessibleSubject(
                loggedInUser.getPrincipalName()
            ,   loggedInUser.getPrincipalName()
            ,   loggedInUser.getUsername()
            ,   whoPrincipalId
            ,   whoAccessGroups
            ,   SubjectType.USER);
        }
        return who;
    }

    private boolean isSuperUser() throws PolicyEditorException {
        return getSuperUser().getId().equals(getLoggedInUser().getId());
    }

    public boolean canChangePassword() throws PolicyEditorException {
        return loggedInUser.isPasswordModifiable();
    }

    /**
     * @see IPolicyEditorClient#isLoggedIn()
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * IPolicyEditorClient#getConfigValue(String)
     */
    public String getConfigValue(String name) throws PolicyEditorException {
        try {
            return port().getConfigValue(name);
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Returns the latest deployment time.
     * @return the latest deployment time.
     */
    public Calendar getLatestDeploymentTime() throws PolicyEditorException {
        try {
            return port().getLatestDeploymentTime();
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Returns the number of deployed policies.
     *
     * @return the number of deployed policies.
     */
    public int getNumDeployedPolicies() throws PolicyEditorException {
        try {
            return port().getNumDeployedPolicies();
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given an <code>EntityType</code>, returns a <code>Collection</code>
     * of <code>DomainObjectDescriptor</code> objects representing the
     * corresponding entities.
     *
     * @param nameTemplate a <code>String</code> with wildcards '%' and '.'
     * defining a template for the names of desired objects.
     * @param type Type of the entity.
     * @param includeHidden hidden entities are included only when this flag is set.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects representing the corresponding entities.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<DomainObjectDescriptor> getDescriptorsForNameAndType(String nameTemplate, EntityType type, boolean includeHidden) throws PolicyEditorException {
        try {
            Collection<DomainObjectDescriptor> res = DTOUtils.makeListOfDescriptors(
                port().getDescriptorsForNameAndType(
                    nameTemplate
                ,   DTOUtils.makeDomainObjectEnumList(
                        Arrays.asList(new EntityType[] { adjustDeprecatedType(type) })
                    )
                )
            );
            return (includeHidden) ? res : filter(res, REMOVE_HIDDEN_DESCRIPTORS);
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>EntityType</code> objects,
     * returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects representing the
     * corresponding entities.
     *
     * @param nameTemplate a <code>String</code> with wildcards '%' and '.'
     * defining a template for the names of desired objects.
     * @param types a <code>Collection</code> of <code>EntityType</code> objects.
     * @param includeHidden hidden entities are included only when this flag is set.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects representing the corresponding entities.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<DomainObjectDescriptor> getDescriptorsForNameAndTypes(String nameTemplate, Collection<EntityType> types, boolean includeHidden) throws PolicyEditorException {
        try {
            List<EntityType> adjustedTypes = new ArrayList<EntityType>();
            for (EntityType t : types) {
                adjustedTypes.add(adjustDeprecatedType(t));
            }
            Collection<DomainObjectDescriptor> res = DTOUtils.makeListOfDescriptors(
                port().getDescriptorsForNameAndType(
                    nameTemplate
                ,   DTOUtils.makeDomainObjectEnumList(adjustedTypes)
                )
            );
            return (includeHidden) ? res : filter(res, REMOVE_HIDDEN_DESCRIPTORS);
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given an <code>EntityType</code>, returns a <code>Collection</code>
     * of policy objects with names matching the template and the corresponding
     * type.
     *
     * @param nameTemplate a <code>String</code> with wildcards '%' and '.'
     * defining a template for the names of desired objects.
     * @param type Type of the entity.
     * @param includeHidden hidden entities are included only when this flag is set.
     * @return a <code>Collection</code> of policy objects representing names
     * of the corresponding entities.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<? extends IHasId> getEntitiesForNamesAndType(Collection<String> names, EntityType type, boolean includeHidden) throws PolicyEditorException {
        try {
            Collection<IHasId> res = DTOUtils.makeCollectionOfSpecs(
                port().getEntitiesForNamesAndType(
                    DTOUtils.makeStringList(names)
                ,   DTOUtils.makeDomainObjectEnum(adjustDeprecatedType(type))
                )
            );
            return (includeHidden) ? res : filter(res, REMOVE_HIDDEN_ENTITIES);
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        } catch (PQLException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>s,
     * returns a <code>Collection</code> of domain objects.
     *
     * @param descriptors a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects representing names of entities.
     * @return a <code>Collection</code> of domain objects.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<? extends IHasId> getEntitiesForDescriptors(Collection<DomainObjectDescriptor> descriptors) throws PolicyEditorException {
        if (descriptors == null) {
            throw new NullPointerException("names");
        }
        if (descriptors.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return DTOUtils.makeCollectionOfSpecs(port().getEntitiesForDescriptors(DTOUtils.makeEntityDescriptorList(descriptors)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        } catch (PQLException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>Strings</code> representing names
     * of entitye types, return a <code>List</code> of correspodning
     * <code>DODDigest</code>s, a lightweight domain object
     *
     * @param types, the names of the entity types (e.g. PORTAL, FOLDER)
     * @return a <code>List</code> of lightweight domain objects
     * @throws PolicyEditorException when the operation can not complete
     */
    public List<DODDigest> getDODDigests(Collection<String> types) throws PolicyEditorException {
        if (types == null) {
            throw new NullPointerException("types");
        }
        if (types.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return DTOUtils.makeListOfDigests(port().getDODDigests(DTOUtils.makeStringList(types)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>List</code> of ids of domain objects (presumably acquired from
     * <code>getDODDigests</code>) return a <code>List</code> of the actual
     * <code>DODDigest</code>s.
     *
     * @param ids, a <code>List</code> of ids of domain objects
     * @return a <code>List</code> of DODDigest
     * @throws PolicyEditorException when the operation can not complete
     */
    public List<DODDigest> getDODDigestsByIds(List<Long> ids) throws PolicyEditorException {
        if (ids == null) {
            throw new NullPointerException("ids");
        }
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return DTOUtils.makeListOfDigests(port().getDODDigestsByIds(DTOUtils.makeListOfIds(ids)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>List</code> of ids of domain objects (presumably acquired from
     * <code>getDODDigests</code>) return a <code>List</code> of the actual
     * <code>DomainObjectDescriptor</code>s.
     *
     * @param ids, a <code>List</code> of ids of domain objects
     * @return a <code>List</code> of DomainObjectDescriptor
     * @throws PolicyEditorException when the operation can not complete
     */
    public List<DomainObjectDescriptor> getDescriptorsByIds(List<Long> ids) throws PolicyEditorException {
        if (ids == null) {
            throw new NullPointerException("ids");
        }
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return DTOUtils.makeListOfDescriptors(port().getDescriptorsByIds(DTOUtils.makeListOfIds(ids)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Saves the given <code>Collection</code> of domain objects.
     *
     * @param entities the <code>Collection</code> of domain objects to be saved
     * @return a <code>List</code> of domain object digets corresponding to the saved
     * objects
     * @throws PolicyEditorException when the operation cannot complete
     */
    public List<DODDigest> saveEntitiesDigest(Collection<? extends IHasId> entities) throws PolicyEditorException {
        if (entities == null) {
            throw new NullPointerException("entities");
        }
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<DODDigest> digests = DTOUtils.makeListOfDigests(port().saveEntitiesDigest(DTOUtils.makePql(entities)));
            Iterator<DODDigest> digestIter = digests.iterator();
            Iterator<? extends IHasId> enIter = entities.iterator();
            while (digestIter.hasNext() && enIter.hasNext()) {
                DODDigest digest = (DODDigest) digestIter.next();
                Object ent = enIter.next();
                if (ent instanceof SpecBase) {
                    ((SpecBase) ent).setId(digest.getId());
                } else if (ent instanceof Policy) {
                    ((Policy) ent).setId(digest.getId());
                } else if (ent instanceof PolicyFolder) {
                    ((PolicyFolder) ent).setId(digest.getId());
                }
            }
            assert !(digestIter.hasNext() || enIter.hasNext());
            return digests;
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }
    
    /**
     * Saves the given <code>Collection</code> of domain objects.
     *
     * @param entities the <code>Collection</code> of domain objects to be saved.
     * @return a <code>List</code> of descriptors corresponding to the saved
     * objects from <code>entities</code>.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public List<DomainObjectDescriptor> saveEntities(Collection<? extends IHasId> entities) throws PolicyEditorException {
        if (entities == null) {
            throw new NullPointerException("entities");
        }
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<DomainObjectDescriptor> descriptors = DTOUtils.makeListOfDescriptors(port().saveEntities(DTOUtils.makePql(entities)));
            Iterator<DomainObjectDescriptor> descrIter = descriptors.iterator();
            Iterator<? extends IHasId> enIter = entities.iterator();
            while (descrIter.hasNext() && enIter.hasNext()) {
                DomainObjectDescriptor descr = (DomainObjectDescriptor) descrIter.next();
                Object ent = enIter.next();
                if (ent instanceof SpecBase) {
                    ((SpecBase) ent).setId(descr.getId());
                } else if (ent instanceof Policy) {
                    ((Policy) ent).setId(descr.getId());
                } else if (ent instanceof PolicyFolder) {
                    ((PolicyFolder) ent).setId(descr.getId());
                }
            }
            assert !(descrIter.hasNext() || enIter.hasNext());
            return descriptors;
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * @see IPolicyEditorClient#getReferringObjectsForGroup(String, EntityType, EntityType, boolean, boolean)
     */
    public Collection<DomainObjectDescriptor> getReferringObjectsForGroup(String nameTemplate, EntityType type, EntityType referringType, boolean onlyDirect, boolean includeHidden) throws PolicyEditorException {
        try {
            if (nameTemplate == null) {
                throw new NullPointerException("nameTemplate");
            }
            if (type == null) {
                throw new NullPointerException("type");
            }
            // Referring type may be null...
            DomainObjectEnum refType = (referringType != null) ? DTOUtils.makeDomainObjectEnum(adjustDeprecatedType(referringType)) : null;
            List<DomainObjectDescriptor> res = DTOUtils.makeListOfDescriptors(
                port().getReferringObjectsForGroup(
                    nameTemplate
                ,   DTOUtils.makeDomainObjectEnum(adjustDeprecatedType(type))
                ,   refType
                ,   onlyDirect
                )
            );
            return (includeHidden) ? res : filter(res, REMOVE_HIDDEN_DESCRIPTORS);
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an asOf <code>Date</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects referring to the objects in the initial <code>Collection</code>.
     *
     * @param initial a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects for which to find the dependencies.
     * @param asOf the asOf <code>Date</code> for the query.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects referring to the objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException when the operation cannot complete.
     * @throws CircularReferenceException
     */
    public Collection<DomainObjectDescriptor> getReferringObjectsAsOf(Collection<DomainObjectDescriptor> initial, Date asOf, boolean onlyDirect) throws PolicyEditorException, CircularReferenceException {
        try {
            if (initial == null) {
                throw new NullPointerException("initial");
            }
            if (asOf == null) {
                throw new NullPointerException("asOf");
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(asOf);
            return DTOUtils.makeListOfDescriptors(port().getReferringObjectsAsOf(DTOUtils.makeEntityDescriptorList(initial), cal, onlyDirect));
        } catch (CircularReferenceFault e) {
            throw new CircularReferenceException(DTOUtils.makeListOfDescriptors(e.getElement()));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>DODDigest</code>
     * objects, returns a <code>Collection</code> of
     * <code>DODDigest</code> objects referred to by objects in
     * the initial <code>Collection</code>.
     *
     * @param initial a <code>Collection</code> of <code>DODDigest</code>
     * objects for which to find the dependencies.
     * @param directOnly only return directDependencies
     * @return a <code>Collection</code> of <code>DODDigest</code>
     * objects referred to by objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<DODDigest> getDependenciesDigest(Collection<DODDigest> initial, boolean directOnly) throws CircularReferenceException, PolicyEditorException {
        try {
            if (initial == null) {
                throw new NullPointerException("initial");
            }
            return DTOUtils.makeListOfDigests(port().getDependenciesDigest(DTOUtils.makeEntityDigestList(initial), directOnly));
        } catch (CircularReferenceFault e) {
            throw new CircularReferenceException(DTOUtils.makeListOfDescriptors(e.getElement()));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>DODDigest</code>
     * objects, returns a <code>Collection</code> of
     * <code>DODDigest</code> objects referred to by objects in
     * the initial <code>Collection</code>.
     *
     * @param initial a <code>Collection</code> of <code>DODDigest</code>
     * objects for which to find the dependencies.
     * @return a <code>Collection</code> of <code>DODDigest</code>
     * objects referred to by objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<DODDigest> getDependenciesDigest(Collection<DODDigest> initial) throws CircularReferenceException, PolicyEditorException {
        return getDependenciesDigest(initial, false);
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects referred to by objects in
     * the initial <code>Collection</code>.
     *
     * @param initial a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects for which to find the dependencies.
     * @param directOnly only return direct dependencies
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects referred to by objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<DomainObjectDescriptor> getDependencies(Collection<DomainObjectDescriptor> initial, boolean directOnly) throws CircularReferenceException, PolicyEditorException {
        try {
            if (initial == null) {
                throw new NullPointerException("initial");
            }
            return DTOUtils.makeListOfDescriptors(port().getDependencies(DTOUtils.makeEntityDescriptorList(initial), directOnly));
        } catch (CircularReferenceFault e) {
            throw new CircularReferenceException(DTOUtils.makeListOfDescriptors(e.getElement()));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects referred to by objects in
     * the initial <code>Collection</code>.
     *
     * @param initial a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects for which to find the dependencies.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects referred to by objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<DomainObjectDescriptor> getDependencies(Collection<DomainObjectDescriptor> initial) throws CircularReferenceException, PolicyEditorException {
        return getDependencies(initial, false);
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an asOf <code>Date</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects referred to by objects in the initial <code>Collection</code>.
     *
     * @param initial a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects for which to find the dependencies.
     * @param asOf the asOf <code>Date</code> for the query.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects referred to by objects in the initial <code>Collection</code>.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<DomainObjectDescriptor> getDependenciesAsOf(Collection<DomainObjectDescriptor> initial, Date asOf) throws CircularReferenceException, PolicyEditorException {
        try {
            if (initial == null) {
                throw new NullPointerException("initial");
            }
            if (asOf == null) {
                throw new NullPointerException("asOf");
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(asOf);
            return DTOUtils.makeListOfDescriptors(port().getDependenciesAsOf(DTOUtils.makeEntityDescriptorList(initial), cal));
        } catch (CircularReferenceFault e) {
            throw new CircularReferenceException(DTOUtils.makeListOfDescriptors(e.getElement()));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given an entity status, returns a <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects in the specified state.
     *
     * @param status The development status of the object (empty, draft, approved, etc.)
     * @param includeHidden hidden entities are included only when this flag is set.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects in the specified state.
     * @throws PolicyServiceException when the operation cannot be completed.
     */
    public Collection<DomainObjectDescriptor> getDescriptorsForState(DevelopmentStatus status, boolean includeHidden) throws PolicyEditorException {
        try {
            if (status == null) {
                throw new NullPointerException("status");
            }
            Collection<DomainObjectDescriptor> res = DTOUtils.makeListOfDescriptors(port().getDescriptorsForState(DTOUtils.makeDomainObjectStateEnum(status)));
            return (includeHidden) ? res : filter(res, REMOVE_HIDDEN_DESCRIPTORS);
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }
    
    /**
     * Return all custom obligations
     *
     * @return a <code>Collection</code> of <code>ObligationDescriptor</code>s
     * @throws PolicyServiceException when the operation cannot be completed.
     */

    public Collection<ObligationDescriptor> getObligationDescriptors() throws PolicyEditorException {
        try {
            return DTOUtils.makeObligationDescriptorCollection(port().getObligationDescriptors());
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    public Collection<PolicyActionsDescriptor> getAllPolicyActions() throws PolicyEditorException {
        try {
            return DTOUtils.makePolicyActionsDescriptorCollection(port().getPolicyActionsDescriptors());
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given an <code>EntityType</code>, returns all custom attributes applicable to it.
     *
     * @param type the required <code>EntityType</code>.
     * @return a <code>Collection</code> of <code>String</code> objects
     * representing the names of the custom attributes for the given type.
     * Note that <code>getAttributesForType</code> will include these attributes
     * plus the default ones.
     * @throws PolicyServiceException when the operation cannot be completed.
     */
    public synchronized Collection<AttributeDescriptor> getCustomAttributesForType(EntityType type) throws PolicyEditorException {
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (type != EntityType.RESOURCE && type != EntityType.PORTAL) {
            throw new IllegalArgumentException("type");
        }
        List<AttributeDescriptor> res = typeToCustomAttributeList.get(type);
        if (res == null) {
            try {
                if (type == EntityType.RESOURCE) {
                    res = DTOUtils.makeAttributeDescriptorCollection(
                        port().getCustomResourceAttributes(ResourceAttribute.FILE_SYSTEM_SUBTYPE)
                    );
                } else if ( type == EntityType.PORTAL ) {
                    res = DTOUtils.makeAttributeDescriptorCollection(
                        port().getCustomResourceAttributes(ResourceAttribute.PORTAL_SUBTYPE)
                    );
                } else {
                    throw new PolicyEditorException("Unexpected type: "+type.getName());
                }
                typeToCustomAttributeList.put(type, res);
            } catch (RemoteException e) {
                throw new PolicyEditorException(e);
            } catch (ServiceException e) {
                throw new PolicyEditorException(e);
            }
        }
        return res;
    }

    /**
     * Given an <code>EntityType</code>, returns all attributes applicable to it.
     *
     * @param type the required <code>EntityType</code>.
     * @return a <code>Collection</code> of <code>String</code> objects
     * representing the names of the attributes for the given type.
     * @throws PolicyServiceException when the operation cannot be completed.
     */
    public synchronized Collection<AttributeDescriptor> getAttributesForType(EntityType type) throws PolicyEditorException {
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (type == EntityType.POLICY || type == EntityType.FOLDER || type == EntityType.LOCATION || type == EntityType.ILLEGAL) {
            throw new IllegalArgumentException("type");
        }
        List<AttributeDescriptor> res = typeToAttributeList.get(type);
        if (res == null) {
            try {
                if ( type == EntityType.USER || type == EntityType.APPLICATION || type == EntityType.HOST ) {
                    res = DTOUtils.makeAttributeDescriptorCollection(
                        port().getSubjectAttributes(DTOUtils.makeDomainObjectEnum(type))
                    );
                } else if (type == EntityType.ACTION) {
                    res = Arrays.asList(
                        new AttributeDescriptor[] {
                            new AttributeDescriptor(ActionAttribute.NAME.getName(), AttributeType.STRING, ActionAttribute.NAME)
                        }
                    );
                } else if (type == EntityType.RESOURCE) {
                    res = DTOUtils.makeAttributeDescriptorCollection(
                        port().getResourceAttributes(ResourceAttribute.FILE_SYSTEM_SUBTYPE)
                    );
                } else if ( type == EntityType.PORTAL ) {
                    res = DTOUtils.makeAttributeDescriptorCollection(
                        port().getResourceAttributes(ResourceAttribute.PORTAL_SUBTYPE)
                    );
                } else {
                    throw new PolicyEditorException("Unexpected type: "+type.getName());
                }
                typeToAttributeList.put(type, res);
            } catch (RemoteException e) {
                throw new PolicyEditorException(e);
            } catch (ServiceException e) {
                throw new PolicyEditorException(e);
            }
        }
        return res;
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an "as of" <code>Date</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects describing the deployed objects at the time specified by the asOf
     * <code>Date</code>.
     *
     * @param descriptors a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects for which we need to obtain the deployed objects.
     * @param asOf the "as of" <code>Date</code> for which the deployed objects
     * are to be examined.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects describing the deployed objects at the time specified by asOf.
     * @throws PolicyEditorException when the operation cannot be completed.
     */
    public Collection<DomainObjectDescriptor> getDeployedObjectDescriptors(Collection<DomainObjectDescriptor> descriptors, Date asOf) throws PolicyEditorException {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (descriptors.isEmpty()) {
            return Collections.emptyList();
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        Calendar asofCalendar = Calendar.getInstance();
        asofCalendar.setTime(asOf);
        try {
            return DTOUtils.makeListOfDescriptors(port().getDeployedObjectDescriptors(DTOUtils.makeEntityDescriptorList(descriptors), asofCalendar));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects and an "as of" <code>Date</code>, returns a
     * <code>Collection</code> of domain objects representing the state of the
     * deployed objects at the time specified by the asOf <code>Date</code>.
     *
     * @param descriptors a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects for which we need to obtain the deployed objects.
     * @param asOf the "as of" <code>Date</code> for which the deployed objects
     * are to be examined.
     * @return returns a <code>Collection</code> of domain objects
     * representing the state of the deployed objects at the time specified by asOf.
     * @throws PolicyServiceException when the operation cannot be completed.
     */
    public Collection<? extends IHasId> getDeployedObjects(Collection<DomainObjectDescriptor> descriptors, Date asOf) throws PolicyEditorException {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (descriptors.isEmpty()) {
            return Collections.emptyList();
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        Calendar asofCalendar = Calendar.getInstance();
        asofCalendar.setTime(asOf);
        try {
            return DTOUtils.makeCollectionOfSpecs(port().getDeployedObjects(DTOUtils.makeEntityDescriptorList(descriptors), asofCalendar));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        } catch (PQLException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>DeploymentRecord</code>, returns a
     * <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects that were deployed or undeployed in a deployment action described
     * by the record.
     *
     * @param record the <code>DeploymentRecord</code> the deployed or undeployed
     * objects for which we are querying.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects that were deployed or undeployed in a deployment action described by the record.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    public Collection<DomainObjectDescriptor> getDescriptorsForDeploymentRecord(DeploymentRecord record) throws PolicyEditorException {
        if (record == null) {
            throw new NullPointerException("record");
        }
        try {
            return DTOUtils.makeListOfDescriptors(port().descriptorsForDeploymentRecord(DTOUtils.makeDeploymentRecordDTO(record)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>DomainObjectDescriptor</code>, returns a
     * <code>Collection</code> of <code>TimeRelation</code> objects
     * representing the from and the to dates of deployment of the given object.
     *
     * @param descriptor a <code>DomainObjectDescriptor</code> of the object
     * for which we need the deployment history.
     * @return a <code>Collection</code> of <code>TimeRelation</code>
     * objects representing the from and the to dates of deployment of the given object.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    public Collection<DeploymentHistory> getDeploymentHistory(DomainObjectDescriptor descriptor) throws PolicyEditorException {
        if (descriptor == null) {
            throw new NullPointerException("descriptor");
        }
        try {
            return DTOUtils.makeCollectionOfDeploymentHistory(port().getDeploymentHistory(DTOUtils.makeEntityDescriptorDTO(descriptor)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, schedules a deployment of all objects in the
     * <code>Collection</code> at the time specified by the "when" parameter.
     *
     * @param descriptors a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects to be deployed.
     * @param when the date when the deployment is to happen.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    public DeploymentRecord scheduleDeployment(Collection<DomainObjectDescriptor> descriptors, Date when) throws PolicyEditorException {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (when == null) {
            throw new NullPointerException("when");
        }
        try {
            Calendar whenCalendar = Calendar.getInstance();
            whenCalendar.setTime(when);
            DeploymentRecordDTO scheduleDeploymentDTO = port().scheduleDeployment(
					DTOUtils.makeEntityDescriptorList(descriptors), whenCalendar);
			DeploymentRecord deploymentRecord = DTOUtils
					.makeDeploymentRecord(scheduleDeploymentDTO);
			return deploymentRecord;
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects, schedules an undeployment of all objects in the
     * <code>Collection</code> at the time specified by the "when" parameter.
     *
     * @param descriptors a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects to be undeployed.
     * @param when the date when the undeployment is to happen.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    public DeploymentRecord scheduleUndeployment(Collection<DomainObjectDescriptor> descriptors, Date when) throws PolicyEditorException {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (when == null) {
            throw new NullPointerException("when");
        }
        try {
            Calendar whenCalendar = Calendar.getInstance();
            whenCalendar.setTime(when);
            return DTOUtils.makeDeploymentRecord(
            		port().scheduleUndeployment(DTOUtils.makeEntityDescriptorList(descriptors), whenCalendar));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Cancels a deployment action described by a <code>DeploymentRecod</code>.
     *
     * @param record the record describing the action to be canceled.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    public void cancelScheduledDeployment(DeploymentRecord record) throws PolicyEditorException {
        try {
            port().cancelScheduledDeployment(DTOUtils.makeDeploymentRecordDTO(record));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Given a from and a to dates, returns a <code>Collection</code> of
     * <code>DeploymentRecord</code> objects representing all deployment and
     * undeployment actions that happened in the interval [from, to], inclusive.
     *
     * @param from the from <code>Date</code>.
     * @param to the to <code>Date</code>.
     * @return a <code>Collection</code> of <code>DeploymentRecord</code>
     * objects representing all deployment and undeployment actions that
     * happened in the interval [from, to], inclusive.
     * @throws PolicyServiceException when the operation cannot complete.
     */
    public Collection<DeploymentRecord> getDeploymentRecords(Date from, Date to) throws PolicyEditorException {
        try {
            if (from == null) {
                from = UnmodifiableDate.START_OF_TIME;
            }
            if (to == null) {
                to = UnmodifiableDate.END_OF_TIME;
            }
            return DTOUtils.makeCollectionOfDeploymentRecords(port().getDeploymentRecords(from.getTime(), to.getTime()));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * 
     * @see IPolicyEditorClient#hasLock(IHasId)
     */
    public boolean hasLock(IHasId object) throws PolicyEditorException {
        if (object == null) {
            throw new NullPointerException("object");
        }
        boolean valueToReturn = false;
        try {
            String lockOwner = port().objectLock(BigInteger.valueOf(object.getId().longValue()), LockRequestType.QUERY);
            if (this.getLoggedInUser().getUid().equals(lockOwner)) {
                valueToReturn = true;
            }
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
        
        return valueToReturn;
    }

    /**
     * 
     * @see IPolicyEditorClient#acquireLock(IHasId, boolean)
     */
    public void acquireLock(IHasId object, boolean force) throws PolicyEditorException {
        if (object == null) {
            throw new NullPointerException("object");
        }

        String requestor = this.getLoggedInUser().getUid();
        
        try {
            String newOwner = port().objectLock(BigInteger.valueOf(object.getId().longValue()), force ? LockRequestType.FORCE : LockRequestType.LOCK);
            if (!newOwner.equalsIgnoreCase(requestor)) {
                throw new PolicyEditorException("Lock currently owned by user with uid, " + newOwner);
            }
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * 
     * @see IPolicyEditorClient#releaseLock(IHasId)
     */
    public void releaseLock(IHasId object) throws PolicyEditorException {
        if (object == null) {
            throw new NullPointerException("object");
        }
   
        try {
            port().objectLock(BigInteger.valueOf(object.getId().longValue()), LockRequestType.UNLOCK);
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * @see IPolicyEditorClient#runLeafObjectQuery(LeafObjectSearchSpec)
     */
    public List<LeafObject> runLeafObjectQuery(LeafObjectSearchSpec searchSpec) throws PolicyEditorException {
        if (searchSpec == null) {
            throw new NullPointerException("searchSpec cannot be null.");
        }
        try {
            return DTOUtils.makeListOfLeafObjects(
                port().runLeafObjectQuery(
                    DTOUtils.searchSpecToDTO(searchSpec)
                )
            );
        } catch (RemoteException exception) {
            throw new PolicyEditorException(exception);
        } catch (ServiceException exception) {
            throw new PolicyEditorException(exception);
        }
    }

    /**
     * @see IPolicyEditorClient#getLeafObjectsForIds(long[], long[], long[])
     */
    public List<LeafObject> getLeafObjectsForIds( long[] elementIds, long[] userGroupIds, long[] hostGroupIds ) throws PolicyEditorException {
        if (elementIds == null || userGroupIds == null || hostGroupIds == null) {
            throw new NullPointerException("id array is null");
        }
        if (elementIds.length == 0 && userGroupIds.length == 0 && hostGroupIds.length == 0) {
            return Collections.emptyList();
        }
        try {
            return DTOUtils.makeListOfLeafObjects(
                port().getLeafObjectsByIds(
                    DTOUtils.makeListOfIds(elementIds)
                ,   DTOUtils.makeListOfIds(userGroupIds)
                ,   DTOUtils.makeListOfIds(hostGroupIds)
                )
            );
        } catch (RemoteException exception) {
            throw new PolicyEditorException(exception);
        } catch (ServiceException exception) {
            throw new PolicyEditorException(exception);
        }
    }

    /**
     * Given a descriptor of a subject group, returns a <code>Collection</code>
     * of <code>LeafObject</code>s belonging to that group.
     *
     * @param descr a descriptor of a subject group.
     * @return a <code>Collection</code> of <code>LeafObject</code>s
     * belonging to the specified group.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<LeafObject> getMatchingSubjects(DomainObjectDescriptor descr) throws PolicyEditorException {
        if (descr == null) {
            throw new NullPointerException("descr");
        }
        try {
            return DTOUtils.makeListOfLeafObjects(port().getMatchingSubjects(DTOUtils.makeEntityDescriptorDTO(descr)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * @see IPolicyEditorClient#getSuperUser()
     */
    public LeafObject getSuperUser() throws PolicyEditorException {
        if (superUser == null) {
            superUser = retrieveSuperUser();
        }
        return superUser;
    }

    /**
     * Given a descriptor of an action group, returns a <code>Collection</code>
     * of <code>IDAction</code>s referenced by that group.
     *
     * @param descr a descriptor of an action group.
     * @return a <code>Collection</code> of <code>IDAction</code>s referenced by that group.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public Collection<? extends IAction> getMatchingActions(DomainObjectDescriptor descr) throws PolicyEditorException {
        if (descr == null) {
            throw new NullPointerException("descr");
        }
        try {
            Collection<LeafObject> actList = DTOUtils.makeListOfLeafObjects(port().getMatchingActions(DTOUtils.makeEntityDescriptorDTO(descr)));
            List<IAction> res = new ArrayList<IAction>(actList.size());
            for (LeafObject leaf : actList ) {
                res.add(DAction.getAction(leaf.getName()));
            }
            return res;
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * Returns a <code>ResourcePreview</code> object for the given resource
     * descriptor.
     *
     * @param descr a resource descriptor for which to obtain a preview object.
     * @return a <code>ResourcePreview</code> object for the given resource descriptor.
     * @throws PolicyEditorException when the operation cannot complete.
     */
    public ResourcePreview getResourcePreview(DomainObjectDescriptor descr) throws PolicyEditorException {
        if (descr == null) {
            throw new NullPointerException("descr");
        }
        final IPredicate resourcePred;
        try {
            String pql = port().getFullyResolvedEntity(DTOUtils.makeEntityDescriptorDTO(descr));

            Collection<IHasId> tmp = DTOUtils.makeCollectionOfSpecs(pql);

            if (tmp == null) {
                throw new NullPointerException("getFullyResolvedEntity() returned null");
            }
            if (tmp.size() != 1) {
                throw new IllegalArgumentException("getFullyResolvedEntity() returned " + tmp.size() + " entities instead of returning 1.");
            }
            SpecBase resourceSpec = (SpecBase) (tmp.iterator().next());

            resourcePred = resourceSpec.getPredicate();
            if (resourcePred == null) {
                throw new NullPointerException("resource predicate");
            }
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (PQLException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }

        // Walk the predicate and build a set of restrictions on the root

        final Stack<Set<String>> stackOfSets = new Stack<Set<String>>();
        final Set<ResourceAttribute> neededAttrs = new HashSet<ResourceAttribute>();
        neededAttrs.add(ResourceAttribute.IS_DIRECTORY);

        resourcePred.accept(new IPredicateVisitor() {

            public void visit(ICompositePredicate pred, boolean preorder) {
                int predicateCount = pred.predicateCount();
                assert stackOfSets.size() >= predicateCount;
                if (pred.getOp() == BooleanOp.AND) {
                    Set<String> res = new HashSet<String>();
                    Set<String> toRemove = new HashSet<String>();
                    boolean foundSpecificLocalRoot = false;
                    for (int i = 0; i != predicateCount; i++) {
                        Set<String> vals = stackOfSets.pop();
                        if (vals instanceof SetInversion) {
                            toRemove.addAll(vals);
                        } else {
                            for (String pattern : vals ) {
                                foundSpecificLocalRoot |= 
                                    pattern.startsWith(DefaultFileResourceHandler.MY_DESKTOP_VAR_NAME)
                                ||  pattern.startsWith(DefaultFileResourceHandler.MY_DOCUMENTS_VAR_NAME)
                                || (pattern.length() > 1 && pattern.charAt(1) == ':' && pattern.charAt(0) != '*');
                                res.add(pattern);
                            }
                        }
                    }
                    res.removeAll(toRemove);
                    if (foundSpecificLocalRoot) {
                        // We found a specific local root - remove all generic
                        // local roots
                        Set<String> tmpSet = new HashSet<String>();
                        for (String pattern : res ) {
                            if (!pattern.startsWith("?") && !pattern.startsWith("!") && !pattern.startsWith("*:")) {
                                tmpSet.add(pattern);
                            }
                        }
                        res = tmpSet;
                    }
                    stackOfSets.push(res);
                } else if (pred.getOp() == BooleanOp.OR) {
                    Set<String> res = new HashSet<String>();
                    boolean foundInversion = false;
                    boolean foundGenericLocalRoot = false;
                    for (int i = 0; i != predicateCount; i++) {
                        Set<String> vals = stackOfSets.pop();
                        foundInversion |= (vals instanceof SetInversion);
                        if (!foundInversion) {
                            for (String pattern : vals) {
                                foundGenericLocalRoot |= pattern.startsWith("?") || pattern.startsWith("!") || pattern.startsWith("*");
                                res.add(pattern);
                            }
                        }
                    }
                    if (foundGenericLocalRoot) {
                        // We found a generic local root - remove all specific
                        // local roots
                        Set<String> tmpSet = new HashSet<String>();
                        for (String pattern : res) {
                            if ((!pattern.startsWith(DefaultFileResourceHandler.MY_DESKTOP_VAR_NAME))
                            &&  (!pattern.startsWith(DefaultFileResourceHandler.MY_DOCUMENTS_VAR_NAME))
                            &&  (pattern.length() < 2 || pattern.charAt(0) == '!' || pattern.charAt(0) == '?' || pattern.charAt(0) == '*')) {
                                tmpSet.add(pattern);
                            }
                        }
                        res = tmpSet;
                    }
                    stackOfSets.push(foundInversion ? new SetInversion<String>() : res);
                } else if (pred.getOp() == BooleanOp.NOT) {
                    Set<String> operand = stackOfSets.pop();
                    stackOfSets.push(SetInversion.make(operand));
                } else {
                    throw new IllegalArgumentException("Found an unexpected boolean operator: " + pred.getOp());
                }
            }

            public void visit(IPredicateReference pred) {
                throw new IllegalArgumentException("Found a predicate reference in a fully expanded predicate.");
            }

            public void visit(IRelation pred) {
                IExpression lhs = pred.getLHS();
                IExpression rhs = pred.getRHS();
                if (lhs instanceof ResourceAttribute || rhs instanceof ResourceAttribute) {
                    ResourceAttribute resAttr;
                    Constant constVal;
                    if (lhs instanceof ResourceAttribute && rhs instanceof Constant) {
                        resAttr = (ResourceAttribute) lhs;
                        constVal = (Constant) rhs;
                    } else if (rhs instanceof ResourceAttribute && lhs instanceof Constant) {
                        resAttr = (ResourceAttribute) rhs;
                        constVal = (Constant) lhs;
                    } else {
                        throw new IllegalArgumentException("The opposite side of a resource attribute must be a constant.");
                    }
                    neededAttrs.add(resAttr);
                    // See if we can get a restriction for the initial part of
                    // the name
                    // (type and directory attributes cannot do that; neither
                    // can the
                    // "straight" name attribute.)
                    if (resAttr == ResourceAttribute.NAME) {
                        if (pred.getOp() == RelationOp.EQUALS) {
                            IEvalValue val = constVal.evaluate(null);
                            if (val.getType() == ValueType.STRING) {

                                String rep = constVal.getRepresentation();
                                String str = DefaultFileResourceHandler.getNativeName(val.getValue().toString());

                                if (str.startsWith("*") || (rep.indexOf('/') == -1 && rep.indexOf('\\') == -1)) {
                                    stackOfSets.push(new SetInversion<String>());
                                } else {
                                    Constant newConstVal = Constant.build(DefaultFileResourceHandler.getResourceName(str));
                                    assert pred instanceof Relation;
                                    if (lhs instanceof ResourceAttribute && rhs instanceof Constant) {
                                        ((Relation) pred).setRHS(newConstVal);
                                    } else {
                                        ((Relation) pred).setLHS(newConstVal);
                                    }
                                    stackOfSets.push(new HashSet<String>(Arrays.asList(new String[] { str })));
                                }
                            } else {
                                throw new IllegalArgumentException("The opposite side of a resource attribute must be a String.");
                            }
                        } else {
                            stackOfSets.push(new SetInversion<String>());
                        }
                    } else {
                        stackOfSets.push(new SetInversion<String>());
                    }
                } else {
                    if (lhs instanceof Constant && rhs instanceof Constant) {
                        if (pred.match(null)) {
                            stackOfSets.push(new SetInversion<String>());
                        } else {
                            stackOfSets.push(new HashSet<String>());
                        }
                    } else {
                        throw new IllegalArgumentException("Found a non-resource attribute in a resource predicate.");
                    }
                }
            }

            public void visit(IPredicate pred) {
                if (pred == PredicateConstants.TRUE) {
                    stackOfSets.push(new SetInversion<String>());
                } else if (pred == PredicateConstants.FALSE) {
                    stackOfSets.push(new HashSet<String>());
                } else {
                    throw new IllegalArgumentException("Found an unexpected resource predicate.");
                }
            }
        }, IPredicateVisitor.POSTORDER);

        assert stackOfSets.size() == 1;
        Set<String> rootSet = stackOfSets.pop();
        final Set<String> roots;
        final boolean mustTryAllRoots;
        final boolean mustUseLocalRoots;

        if (rootSet instanceof SetInversion) {
            // An attempt to get a restriction was not successful
            roots = Collections.emptySet();
            mustTryAllRoots = true;
            mustUseLocalRoots = true;
        } else {
            boolean foundLocalRoot = false;
            String[] rootStr = (String[]) rootSet.toArray(new String[rootSet.size()]);
            Set<String> rootCandidates = new HashSet<String>();
            // combine the strings
            for (int i = 0; i != rootStr.length; i++) {
                if (rootStr[i].startsWith("!") || rootStr[i].startsWith("?") || rootStr[i].startsWith("*")) {
                    // Generic local root
                    foundLocalRoot = true;
                } else {
                    // A specific local root or a share point
                    boolean canBeSubsumed = false;
                    for (int j = 0; !canBeSubsumed && j != rootStr.length; j++) {
                        if (i != j && !rootStr[j].startsWith("!") && !rootStr[j].startsWith("?") && !rootStr[j].startsWith("*")) {
                            canBeSubsumed = StringUtils.isMatch(rootStr[j], rootStr[i]);
                            if (canBeSubsumed) {
                                // See if the two patterns are equivalent -
                                // check the reverse relation
                                if (StringUtils.isMatch(rootStr[i], rootStr[j])) {
                                    // The strings are equivalent - the one with
                                    // the lower index wins
                                    canBeSubsumed = (i > j);
                                }
                            }
                        }
                    }
                    if (!canBeSubsumed) {
                        rootCandidates.add(rootStr[i]);
                    }
                }
            }

            mustUseLocalRoots = foundLocalRoot;

            boolean foundEmptyRoot = false;
            Set<String> prunedRoots = new HashSet<String>();

            for (String candidate : rootCandidates) {
                assert !candidate.startsWith("!") && !candidate.startsWith("?") && !candidate.startsWith("*"); // generic local roots don't
                // make the list
                int pos0 = candidate.indexOf("!");
                if (pos0 == -1) {
                    pos0 = candidate.length() - 1;
                }
                int pos1 = candidate.indexOf("?");
                if (pos1 == -1) {
                    pos1 = candidate.length() - 1;
                }
                int pos2 = candidate.indexOf("*");
                if (pos2 == -1) {
                    pos2 = candidate.length() - 1;
                }
                int pos = Math.min(pos0, Math.min(pos1, pos2));
                // Convert "\\host\a*\**" to "\\host"
                while (pos != 0 && candidate.charAt(pos - 1) != File.separatorChar) {
                    pos--;
                }
                candidate = candidate.substring(0, pos);
                if (candidate.length() >= 2 && !"\\\\".equals(candidate)) {
                    prunedRoots.add(candidate);
                } else {
                    foundEmptyRoot = true;
                }
            }

            // If one of the roots was empty, it was of the form "\\xyz*" or
            // "\\*"
            // In this case we must try all roots.
            mustTryAllRoots = foundEmptyRoot;

            if (mustTryAllRoots) {
                roots = Collections.emptySet();
            } else {
                roots = Collections.unmodifiableSet(prunedRoots);
            }
        }

        // Prepare the right resource manager
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        manager.registerComponent(AgentResourceManager.COMP_INFO, true);
        final AgentResourceManager rm = (AgentResourceManager) ComponentManagerFactory.getComponentManager().getComponent(AgentResourceManager.COMP_INFO);

        return new ResourcePreview() {

            private final IEvalValue CONST_MINUS_ONE = EvalValue.build(-1);

            public Collection<String> getRoots() {
                return roots;
            }

            public boolean tryAllNetworkRoots() {
                return mustTryAllRoots;
            }

            public boolean tryAllLocalRoots() {
                return mustUseLocalRoots;
            }

            public boolean isProbableRoot(File dir) {
                final String dirName = DefaultFileResourceHandler.getResourceName(dir.getAbsolutePath());
                if (dirName == null) {
                    return false;
                }
                TernaryEvaluator eval = new TernaryEvaluator(dirName.toLowerCase());
                resourcePred.accept(eval, IPredicateVisitor.POSTORDER);
                return eval.getResult() != Ternary.FALSE;
            }

            // From java.io.FilenameFilter
            public boolean accept(File dir, String name) {
                return accept(new File(dir, name));
            }

            // From java.io.FileFilter
            public boolean accept(File pathname) {
                String resourceName = DefaultFileResourceHandler.getResourceName(pathname.getAbsolutePath());
                // TODO (sergey) This should be coming from a different resource manager
                IResource res = rm.getResource( resourceName, null, AgentTypeEnumType.DESKTOP).getResource();
                if (CONST_MINUS_ONE.equals(res.getAttribute(ResourceAttribute.IS_DIRECTORY.getName()))) {
                    return false;
                }
                return resourcePred.match(new EvaluationRequest(res));
            }
        };
    }

    private static class Ternary extends EnumBase {

        private static final long serialVersionUID = 1L;

        private Ternary(String name) {
            super(name);
        }

        static public Ternary TRUE = new Ternary("<true>") {
            private static final long serialVersionUID = 1L;
        };
        static public Ternary FALSE = new Ternary("<false>") {
            private static final long serialVersionUID = 1L;
        };
        static public Ternary UNKNOWN = new Ternary("<unknown>") {
            private static final long serialVersionUID = 1L;
        };

        public static Ternary and(Ternary a, Ternary b) {
            if (a == FALSE || b == FALSE) {
                return FALSE;
            }
            if (a == UNKNOWN || b == UNKNOWN) {
                return UNKNOWN;
            }
            return TRUE;
        }

        public static Ternary or(Ternary a, Ternary b) {
            if (a == TRUE || b == TRUE) {
                return TRUE;
            }
            if (a == UNKNOWN || b == UNKNOWN) {
                return UNKNOWN;
            }
            return FALSE;
        }

        public static Ternary not(Ternary a) {
            if (a == FALSE) {
                return TRUE;
            } else if (a == TRUE) {
                return FALSE;
            } else {
                return UNKNOWN;
            }
        }
    }

    private static class TernaryEvaluator implements IPredicateVisitor {

        private final Stack<Ternary> stack = new Stack<Ternary>();
        private final EvaluationRequest posRequest, negRequest;

        public TernaryEvaluator(final String name) {
            posRequest = new EvaluationRequest(new IResource() {
                private static final long serialVersionUID = 1L;
                private final String namePattern = name+"/|";
                private final IEvalValue namePatternValue = EvalValue.build(namePattern);
                public IEvalValue getAttribute(String name) {
                    if ("name".equals(name)) {
                        return namePatternValue;
                    } else {
                        return EvalValue.NULL;
                    }
                }
                public Serializable getIdentifier() {
                    return namePattern;
                }
                public boolean hasAttribute(String name) {
                    return true;
                }
                public Set<Map.Entry<String, IEvalValue>> getEntrySet() {
                    throw new UnsupportedOperationException();
                }
                public IMResource clone() {
                    throw new UnsupportedOperationException();
                }
            });
            negRequest = new EvaluationRequest(new IResource() {
                private static final long serialVersionUID = 1L;
                private final String namePattern = name+"/";
                private final IEvalValue namePatternValue = EvalValue.build(namePattern);
                public IEvalValue getAttribute(String name) {
                    if ("name".equals(name)) {
                        return namePatternValue;
                    } else {
                        return EvalValue.NULL;
                    }
                }
                public Serializable getIdentifier() {
                    return namePattern;
                }
                public boolean hasAttribute(String name) {
                    return true;
                }
                public Set<Map.Entry<String, IEvalValue>> getEntrySet() {
                    throw new UnsupportedOperationException();
                }
                public IMResource clone() {
                    throw new UnsupportedOperationException();
                }
            });
        }

        public Ternary getResult() {
            if (stack.size() == 1) {
                return (Ternary) stack.peek();
            }
            throw new IllegalStateException("Stack is empty in TernaryEvaluator.getResult()");
        }

        /**
         * @see IPredicateVisitor#visit(ICompositePredicate, boolean)
         */
        public void visit(ICompositePredicate pred, boolean preorder) {
            int predicateCount = pred.predicateCount();
            assert stack.size() >= predicateCount;
            Ternary res;
            if (pred.getOp() == BooleanOp.AND) {
                if (predicateCount != 0) {
                    res = (Ternary) stack.pop();
                    for (int i = 1; i != predicateCount; i++) {
                        res = Ternary.and(res, (Ternary) stack.pop());
                    }
                } else {
                    res = Ternary.UNKNOWN;
                }
            } else if (pred.getOp() == BooleanOp.OR) {
                if (predicateCount != 0) {
                    res = (Ternary) stack.pop();
                    for (int i = 1; i != predicateCount; i++) {
                        res = Ternary.or(res, (Ternary) stack.pop());
                    }
                } else {
                    res = Ternary.UNKNOWN;
                }
            } else if (pred.getOp() == BooleanOp.NOT) {
                res = Ternary.not((Ternary) stack.pop());
            } else {
                throw new IllegalArgumentException("Found an unexpected boolean operator: " + pred.getOp());
            }
            stack.push(res);
        }

        /**
         * @see IPredicateVisitor#visit(IPredicateReference)
         */
        public void visit(IPredicateReference pred) {
            throw new IllegalArgumentException("Found a predicate reference in a fully expanded predicate.");
        }

        /**
         * @see IPredicateVisitor#visit(IRelation)
         */
        public void visit(IRelation pred) {
            IExpression lhs = pred.getLHS();
            IExpression rhs = pred.getRHS();
            EvaluationRequest req = (pred.getOp() == RelationOp.NOT_EQUALS ) ? negRequest : posRequest;
            if (lhs instanceof ResourceAttribute || rhs instanceof ResourceAttribute) {
                ResourceAttribute resAttr;
                if (lhs instanceof ResourceAttribute && rhs instanceof Constant) {
                    resAttr = (ResourceAttribute) lhs;
                } else if (rhs instanceof ResourceAttribute && lhs instanceof Constant) {
                    resAttr = (ResourceAttribute) rhs;
                } else {
                    throw new IllegalArgumentException("The opposite side of a resource attribute must be a constant.");
                }
                if (resAttr == ResourceAttribute.NAME) {
                    stack.push(((Relation) pred).match(req) ? Ternary.UNKNOWN : Ternary.FALSE);
                } else {
                    stack.push(Ternary.UNKNOWN);
                }
            } else {
                if (lhs instanceof Constant && rhs instanceof Constant) {
                    stack.push(((Relation) pred).match(req) ? Ternary.TRUE : Ternary.FALSE);
                } else {
                    throw new IllegalArgumentException("Found a non-resource attribute in a resource predicate.");
                }
            }
        }

        /**
         * @see IPredicateVisitor#visit(IPredicate)
         */
        public void visit(IPredicate pred) {
            if (pred == PredicateConstants.TRUE) {
                stack.push(Ternary.TRUE);
            } else if (pred == PredicateConstants.FALSE) {
                stack.push(Ternary.FALSE);
            } else {
                stack.push(Ternary.UNKNOWN);
            }
        }
    }

    /**
     * @see IPolicyEditorClient#ensureOperationIsAllowed(Collection,IAction)
     */
    public Collection<DomainObjectDescriptor> ensureOperationIsAllowed(Collection<DomainObjectDescriptor> descriptors, IAction action) throws PolicyEditorException {
        if (isSuperUser()) {
            return descriptors;
        }
        try {
            return DTOUtils.makeListOfDescriptors(port().ensureOperationIsAllowed(DTOUtils.makeEntityDescriptorList(descriptors), DTOUtils.makeAccess(action)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * @see IPolicyEditorClient#allowedActions(Collection)
     */
    public Collection<? extends IAction> allowedActions(Collection<DomainObjectDescriptor> descriptors) throws PolicyEditorException {
        if (isSuperUser()) {
            return ALL_ACTIONS;
        }
        try {
            return DTOUtils.makeCollectionOfActions(port().allowedActions(DTOUtils.makeEntityDescriptorList(descriptors)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    private boolean checkComponentPrefix(Collection<EntityType> types, String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        int pos = name.indexOf(PQLParser.SEPARATOR);
        if (pos != -1) {
            String prefix = name.substring(0,pos).toUpperCase();

            if (prefix.equals("OBJECT")) {
                prefix = "SAP";
            }

            if (!EntityType.existsElement(prefix)) {
                prefix = EntityType.COMPONENT.getName();
            }

            EntityType type = EntityType.forName(prefix);
            if (type != EntityType.PORTAL) {
                return types.contains(type);
            } else {
                return types.contains(EntityType.RESOURCE);
            }
        } else {
            return true;
        }
    }

    /**
     * @see IPolicyEditorClient#allowedActions(IHasId)
     */
    public Collection<? extends IAction> allowedActions(IHasId object) throws PolicyEditorException {
        if (isSuperUser()) {
            return ALL_ACTIONS;
        }
        IDSubject currentUser = getLoggedInUser();
        Collection<EntityType> allowedEntitiesForUser = getAllowedEntitiesForCurrentUser();
        boolean policiesAreAllowed = allowedEntitiesForUser.contains(EntityType.POLICY);
        List<IAction> res = new ArrayList<IAction>(ALL_ACTIONS.size());
        if (object instanceof IDPolicy || object instanceof PolicyFolder) {
            IAccessControlled p = (IAccessControlled) object;
            for (DAction action : ALL_ACTIONS ) {
                if (p.checkAccess(currentUser, action)) {
                    if (policiesAreAllowed || !EDIT_ACTIONS.contains(action)) {
                        res.add(action);
                    }
                }
            }
        } else if (object instanceof IDSpec) {
            IDSpec spec = (IDSpec) object;
            for (DAction action : ALL_ACTIONS ) {
                if (spec.checkAccess(currentUser, action)) {
                    if (checkComponentPrefix(allowedEntitiesForUser, spec.getName()) || !EDIT_ACTIONS.contains(action)) {
                        res.add(action);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("allowedActions:object");
        }
        return res;
    }

    /**
     * @see IPolicyEditorClient#allowedEntities(Long)
     */
    public Collection<EntityType> allowedEntities() throws PolicyEditorException {
        if (isSuperUser()) {
            return EntityType.elements();
        }
        return getAllowedEntitiesForCurrentUser();
    }

    /**
     * @see IPolicyEditorClient#getAgentList()
     */
    public Collection<AgentStatusDescriptor> getAgentList() throws PolicyEditorException {
        try {
            return DTOUtils.makeCollectionOfAgentStatusDescriptors(port().getAgentList());
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * @see IPolicyEditorClient#getAgentsForDeployedObject(DomainObjectDescriptor, Date)
     */
    public Collection<AgentStatusDescriptor> getAgentsForDeployedObject(DomainObjectDescriptor obj, Date asOf) throws PolicyEditorException {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(asOf);
            return DTOUtils.makeCollectionOfAgentStatusDescriptors(port().getAgentsForDeployedObject(DTOUtils.makeEntityDescriptorDTO(obj), cal));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    /**
     * @see IPolicyEditorClient#deploymentStatusForAgent(AgentStatusDescriptor)
     */
    public Collection<DomainObjectDescriptor> deploymentStatusForAgent(AgentStatusDescriptor agent) throws PolicyEditorException {
        try {
            return DTOUtils.makeListOfDescriptors(port().deploymentStatusForAgent(DTOUtils.makeAgentStatusDescriptorDTO(agent)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    public List<DomainObjectUsage> getUsageList(List<DomainObjectDescriptor> descriptors) throws PolicyEditorException {
        try {
            return DTOUtils.makeUsageList(port().getUsageList(DTOUtils.makeEntityDescriptorList(descriptors)));
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    
    /**
     * @see IPolicyEditorClient#getDictionaryEnrollmentNames()
     */
    public Set<String> getDictionaryEnrollmentNames(PolicyEditorRoles role) throws PolicyEditorException {
        Set<String> namesToReturn = new HashSet<String>();
        
        try {
            StringList enrollmentNames =  port().getDictionaryEnrollmentNames(role);
            String[] namesAsArray = enrollmentNames.getElement();
            if (namesAsArray == null) {
                namesAsArray = new String[0];
            }
            for (int i = 0; i < namesAsArray.length; i++) {
                namesToReturn.add(namesAsArray[i]);
            }
            
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
        
        return namesToReturn;
    }

    /**
     * @see IPolicyEditorClient#getDictionaryEnrollmentRealms()
     */
    public Set<Realm> getDictionaryEnrollmentRealms(PolicyEditorRoles role) throws PolicyEditorException {
        Set<Realm> realmsToReturn = new HashSet<Realm>();
        
        try {
            RealmList enrollmentRealms =  port().getDictionaryEnrollmentRealms(role);
            Realm[] realmsAsArray = enrollmentRealms.getRealms();
            if (realmsAsArray == null) {
                realmsAsArray = new Realm[0];
            }
            for (int i = 0; i < realmsAsArray.length; i++) {
                realmsToReturn.add(realmsAsArray[i]);
            }
            
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
        
        return realmsToReturn;
    }

    private static class SetInversion<T> extends HashSet<T> {

        private static final long serialVersionUID = 1L;

        private SetInversion() {
        }

        private SetInversion(Collection<T> c) {
            super(c);
        }

        public static <T> Set<T> make(Set<T> c) {
            if (c instanceof SetInversion) {
                return new HashSet<T>(c);
            } else {
                return new SetInversion<T>(c);
            }
        }

    }

    /**
     * This package-private method is for unit testing. It clears the database
     * when the user name and the password match the ones used in our testing
     * database.
     *
     * @throws PolicyServiceException
     *             when the operation cannot complete.
     */
    void prepareForTests() throws PolicyEditorException {
        try {
            port().prepareForTests();
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    private PolicyEditorIF port() throws ServiceException {
        // We are here, so the setConfiguration could not have failed.
        // The setConfiguration ensured that the locator is not null:
        assert locator != null;

        return locator.getPolicyEditorIFPort();
    }

    private interface Filter<T> {

        boolean accept(T obj);
    }

    private static <T> Collection<T> filter(Collection<T> in, Filter<T> filter) {
        if (in == null) {
            return null;
        }
        List<T> res = new ArrayList<T>(in.size());
        for (T element : in) {
            if (filter == null || filter.accept(element)) {
                res.add(element);
            }
        }
        return res;
    }

    private static Filter<DomainObjectDescriptor> REMOVE_HIDDEN_DESCRIPTORS = new Filter<DomainObjectDescriptor>() {
        public boolean accept(DomainObjectDescriptor descr) {
            return !descr.isHidden();
        }
    };

    private static Filter<IHasId> REMOVE_HIDDEN_ENTITIES = new Filter<IHasId>() {
        public boolean accept(IHasId obj) {
            if (obj instanceof IDPolicy) {
                return !((IDPolicy) obj).isHidden();
            } else if (obj instanceof IDSpec) {
                return !((IDSpec) obj).isHidden();
            } else {
                return true;
            }
        }
    };

    /**
     * @see IPolicyEditorClient#changePassword(String, String)
     */
    public void changePassword(String oldPassword, String newPassword) throws PolicyEditorException, InvalidPasswordException {
        try {
            port().changePassword(getLoggedInUser().getUniqueName(), oldPassword, newPassword);
        } catch (InvalidPasswordFault e) {
            throw new InvalidPasswordException();
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    private IEvalValue retrieveAccessGroupsForUser(Long id) throws PolicyEditorException {
        try {
            List<LeafObject> leaves = DTOUtils.makeListOfLeafObjects(port().getAccessGroupsForUser(BigInteger.valueOf(id.longValue())));
            List<Long> ids = new ArrayList<Long>(leaves.size());
            for (LeafObject leaf : leaves) {
                if (leaf.getId() != null) {
                    ids.add(leaf.getId());
                }
            }
            return EvalValue.build(Multivalue.create(ids));
        } catch (RemoteException re) {
            throw new PolicyEditorException(re);
        } catch (ServiceException se) {
            throw new PolicyEditorException(se);
        }
    }

    /**
     * This method retrieves all entities that the user may touch.
     */
    private Collection<EntityType> getAllowedEntitiesForCurrentUser() throws PolicyEditorException {
        if (whoAllowedEntities == null) {
            IDSubject currentUser = getLoggedInUser();
            Long id = currentUser.getId();
            BigInteger idValue;
            if (id != null) {
                idValue = BigInteger.valueOf(id.longValue());
            } else {
                idValue = INVALID_ID;
            }
            try {
                whoAllowedEntities = DTOUtils.makeCollectionOfEntityTypes(
                    port().getAllowedEntitiesForUser(idValue)
                );
            } catch (RemoteException re) {
                throw new PolicyEditorException(re);
            } catch (ServiceException se) {
                throw new PolicyEditorException(se);
            }
        }
        return whoAllowedEntities;
    }

    /**
     * @see IPolicyEditorClient#updateComputersWithAgents()
     */
    public void updateComputersWithAgents() throws PolicyEditorException {
        try {
            port().updateComputersWithAgents();
        } catch (RemoteException re) {
            throw new PolicyEditorException(re);
        } catch (ServiceException se) {
            throw new PolicyEditorException(se);
        }
    }

    /**
     * Retrieve the super user from the back end store
     *
     * @return the super user
     * @throws PolicyEditorException
     */
    private LeafObject retrieveSuperUser() throws PolicyEditorException {
        LeafObject objectToReturn = null;
        try {
            objectToReturn = DTOUtils.makeLeafObject(port().getSuperUser());
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
        return objectToReturn;
    }

    /**
     * @see IPolicyEditorClient#hasObjectsToDeploy()
     */
    public boolean hasObjectsToDeploy() throws PolicyEditorException {
        try {
            return port().hasObjectsToDeploy();
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }

    private static EntityType adjustDeprecatedType(EntityType type) {
        if (type == EntityType.ACTION
        ||  type == EntityType.APPLICATION
        ||  type == EntityType.HOST
        ||  type == EntityType.RESOURCE
        ||  type == EntityType.USER
        ||  type == EntityType.PORTAL) {
            return EntityType.COMPONENT;
        } else {
            return type;
        }
    }

    /**
     * @see IPolicyEditorClient#createExternalDataSource(ExternalDataSourceConnectionInfo)
     */
    public int createExternalDataSource(ExternalDataSourceConnectionInfo externalDataSourceConnectionInfo) throws PolicyEditorException {
        try {
            return port().createExternalDataSource(externalDataSourceConnectionInfo);            
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }
    
    /**
     * @see IPolicyEditorClient#getPortalURLList()
     */
    public List<String> getPortalURLList() throws PolicyEditorException {
        List<String> result = new ArrayList<String>();
        try {
            StringList stringList = port().getPortalURLList();
            String[] portals = stringList.getElement();
            if (portals != null) {
                for (int i = 0; i < portals.length; i++) {
                    result.add(portals[i]);
                }
            }
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
        return result;
    }
    
    /**
     * @see IPolicyEditorClient#getResourceTreeNodeChildren(int, ResourceTreeNode)
     */
    public ResourceTreeNode getResourceTreeNodeChildren(int sourceID, ResourceTreeNode resourceTreeNode) throws PolicyEditorException {
        if ( resourceTreeNode == null ) {
            throw new PolicyEditorException("Resource Tree Node should not be null");
        }
        try {
            ResourceTreeNodeList list = port().getResourceTreeNodeChildren(sourceID, resourceTreeNode);
            resourceTreeNode.setChildren(list.getChildren());
            return resourceTreeNode;
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }
    
    /**
     * Normalize URL into internal format
     * @param url
     * @param type
     * @return normalized URL
     */
    public String getNormalizedResourceURL(String url, ExternalDataSourceType type) {    
        return DefaultFileResourceHandler.getNormalizedResourceURL(url, type);
    }
    
    /**
     * @see IPolicyEditorClient#getMatchingPortalResource(int, EntityDescriptorDTO)
     */
    public ResourceTreeNodeList getMatchingPortalResource(int sourceID, EntityDescriptorDTO descriptor) throws PolicyEditorException {
        try {
            ResourceTreeNodeList list = port().getMatchingPortalResource(sourceID, descriptor);
            return list;
        } catch (RemoteException e) {
            throw new PolicyEditorException(e);
        } catch (ServiceException e) {
            throw new PolicyEditorException(e);
        }
    }
    
	public void executePush() throws PolicyEditorException {
		try {
			port().executePush();
		} catch (ServiceNotReadyFault e) {
			 throw new PolicyEditorException(e);
		} catch (RemoteException e) {
			 throw new PolicyEditorException(e);
		} catch (ServiceException e) {
			 throw new PolicyEditorException(e);
		}
	}
	
	public void executePush(Date scheduleTime) throws PolicyEditorException {
		try {
			Calendar whenCalendar = Calendar.getInstance();
			whenCalendar.setTime(scheduleTime);
			port().schedulePush(whenCalendar);
		} catch (ServiceNotReadyFault e) {
			throw new PolicyEditorException(e);
		} catch (RemoteException e) {
			throw new PolicyEditorException(e);
		} catch (ServiceException e) {
			throw new PolicyEditorException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
	 */
	public IConfiguration getConfiguration() {
		return configuration;
	}

	/* (non-Javadoc)
	 * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
	 */
	public void setConfiguration(IConfiguration configuration) {
		this.configuration = configuration;
		init();
		
	}

	/* (non-Javadoc)
	 * @see com.bluejungle.framework.comp.IInitializable#init()
	 */
	public void init() {
		location = (String) getConfiguration().get(IPolicyEditorClient.LOCATION_CONFIG_PARAM);
		username = (String) getConfiguration().get(IPolicyEditorClient.USERNAME_CONFIG_PARAM);
		password = (String) getConfiguration().get(IPolicyEditorClient.PASSWORD_CONFIG_PARAM);
	}
}
