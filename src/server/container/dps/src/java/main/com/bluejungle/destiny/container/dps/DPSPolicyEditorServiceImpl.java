package com.bluejungle.destiny.container.dps;

/**
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *
 * @author sergey
 */

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.bluejungle.destiny.container.policyDeployMgr.IPolicyDeployMgr;
import com.bluejungle.destiny.container.policyDeployMgr.PolicyDeployMgrImpl;
import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQuerySpec;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTerm;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentPolicyAssemblyStatus;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidQuerySpecException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentQuerySpecImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.AuthenticationFailedException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAuthenticationDomain;
import com.bluejungle.destiny.container.shared.pf.PolicyEditorServiceImpl;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequest;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.IDeploymentRequestMgr;
import com.bluejungle.destiny.container.shared.policydeploymentmgr.hibernateimpl.DeploymentRequestMgrImpl;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationArgumentDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ICustomObligationsConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IDPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.destiny.services.policy.InvalidPasswordFault;
import com.bluejungle.destiny.services.policy.PolicyEditorIF;
import com.bluejungle.destiny.services.policy.types.Access;
import com.bluejungle.destiny.services.policy.types.AccessList;
import com.bluejungle.destiny.services.policy.types.AgentStatusDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.AgentStatusList;
import com.bluejungle.destiny.services.policy.types.AttributeDescriptorList;
import com.bluejungle.destiny.services.policy.types.CircularReferenceFault;
import com.bluejungle.destiny.services.policy.types.Component;
import com.bluejungle.destiny.services.policy.types.ComponentList;
import com.bluejungle.destiny.services.policy.types.DMSUserData;
import com.bluejungle.destiny.services.policy.types.DeploymentHistoryList;
import com.bluejungle.destiny.services.policy.types.DeploymentRecordDTO;
import com.bluejungle.destiny.services.policy.types.DeploymentRecordList;
import com.bluejungle.destiny.services.policy.types.DomainObjectEnum;
import com.bluejungle.destiny.services.policy.types.DomainObjectEnumList;
import com.bluejungle.destiny.services.policy.types.DomainObjectStateEnum;
import com.bluejungle.destiny.services.policy.types.DomainObjectUsageListDTO;
import com.bluejungle.destiny.services.policy.types.EntityDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.EntityDescriptorList;
import com.bluejungle.destiny.services.policy.types.EntityDigestList;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceConnectionInfo;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceFault;
import com.bluejungle.destiny.services.policy.types.LeafObjectDTO;
import com.bluejungle.destiny.services.policy.types.LeafObjectList;
import com.bluejungle.destiny.services.policy.types.LeafObjectSearchSpecDTO;
import com.bluejungle.destiny.services.policy.types.ListOfIds;
import com.bluejungle.destiny.services.policy.types.LockRequestType;
import com.bluejungle.destiny.services.policy.types.ObligationArgumentDTO;
import com.bluejungle.destiny.services.policy.types.ObligationDescriptorDTO;
import com.bluejungle.destiny.services.policy.types.ObligationDescriptorList;
import com.bluejungle.destiny.services.policy.types.ObligationValueDTO;
import com.bluejungle.destiny.services.policy.types.PolicyActionsDescriptorList;
import com.bluejungle.destiny.services.policy.types.PolicyEditorRoles;
import com.bluejungle.destiny.services.policy.types.PolicyServiceFault;
import com.bluejungle.destiny.services.policy.types.Realm;
import com.bluejungle.destiny.services.policy.types.RealmList;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNode;
import com.bluejungle.destiny.services.policy.types.ResourceTreeNodeList;
import com.bluejungle.destiny.services.policy.types.Role;
import com.bluejungle.destiny.services.policy.types.StringList;
import com.bluejungle.destiny.services.policy.types.SubjectDTO;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.search.RelationalOp;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lib.AgentStatusDescriptor;
import com.bluejungle.pf.destiny.lib.DMSServiceImpl;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.lib.DTOUtils;
import com.bluejungle.pf.destiny.lib.DomainObjectUsage;
import com.bluejungle.pf.destiny.lib.IDMSService;
import com.bluejungle.pf.destiny.lib.IPolicyEditorService;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lib.PolicyServiceException;
import com.bluejungle.pf.destiny.lib.SharedUtils;
import com.bluejungle.pf.destiny.lifecycle.CircularReferenceException;
import com.bluejungle.pf.destiny.lifecycle.DeploymentHistory;
import com.bluejungle.pf.destiny.lifecycle.DeploymentRecord;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.deployment.AgentAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.common.ISpec;

/**
 * This class implements the DPS Policy Editor Web Service. It uses the Policy
 * Editor Service to edit policies and groups, and to the directory service to
 * obtain leaf objects.
 *
 * @author sergey
 */
public class DPSPolicyEditorServiceImpl implements PolicyEditorIF {

    /** getAttribute method needs the manager. */
    private IComponentManager manager;

    /** Implementation of the policy editor service. */
    private IPolicyEditorService service;

    /** Implementation of the user role service. */
    private IDMSService userRoleService;

    /** A reference to the AgentManager. */
    private IAgentManager agentManager;

    /** A reference to the application user manager */
    private IApplicationUserManager appUserManager;

    private IDPSComponentConfigurationDO dpsConfig;

    private IDestinyConfigurationStore confStore;

    private PolicyActionsDescriptorList policyActionsList;

    /**
     * Constructor
     */
    public DPSPolicyEditorServiceImpl() {
        super();
        manager = ComponentManagerFactory.getComponentManager();
        service = manager.getComponent(PolicyEditorServiceImpl.COMP_INFO);
        userRoleService = manager.getComponent(DMSServiceImpl.COMP_INFO);

        ComponentInfo<IAgentManager> agentMgrCompInfo = 
            new ComponentInfo<IAgentManager>(
                IAgentManager.COMP_NAME
              , AgentManager.class
              , IAgentManager.class
              , LifestyleType.SINGLETON_TYPE
        );
        agentManager = manager.getComponent(agentMgrCompInfo);

        // Initialize the Profile Manager:
        /*
         * -sergey- This code is borrowed directly from the
         * DABSComponentImpl.java. I am including the comment above it as is - I
         * am sure it applies to DPS too. :sergey: FIXME: - We shouldn't be
         * directly accessing the IProfileManager implementation here (i.e.
         * HibernateProfileManager.class.getName()). Though, I can't think of
         * the negative consequences at the moment, so I'm going to do it for
         * consistency
         *
         * Ideally, we should have a declaritive model, in which the required
         * components and their implementions are specified in a config file+
         */
        ComponentInfo<IProfileManager> profileMgrCompInfo = 
            new ComponentInfo<IProfileManager>(
                IProfileManager.COMP_NAME, 
                HibernateProfileManager.class, 
                IProfileManager.class, 
                LifestyleType.SINGLETON_TYPE
        );
        manager.getComponent(profileMgrCompInfo);

        IApplicationUserManagerFactory appUserMgrFactory = manager.getComponent(ApplicationUserManagerFactoryImpl.class);
        appUserManager = appUserMgrFactory.getSingleton();

        confStore = manager.getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
        dpsConfig = (IDPSComponentConfigurationDO) confStore.retrieveComponentConfiguration(ServerComponentType.DPS.getName());

        // HACK HACK.  This should come from dpsConfig
        policyActionsList = SharedUtils.getPolicyActionsDescriptorList(confStore); 
    }

    /**
     * Given a name of a configuration item, returns its value or null if the
     * item is not available.
     *
     * @param name
     *            the name of the configuration item to return.
     * @return the name of the configuration item or null if it is not
     *         available.
     * @throws PolicyServiceFault
     *             when it is impossible to contact the service.
     */
    @Override
    public String getConfigValue(String name) throws PolicyServiceFault {
        assert service != null;
        try {
            return service.getConfigValue(name);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public int getNumDeployedPolicies() throws PolicyServiceFault {
        assert service != null;
        try {
            return service.getNumDeployedPolicies(new Date());
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public Calendar getLatestDeploymentTime() throws PolicyServiceFault {
        assert service != null;
        try {
            Calendar res = Calendar.getInstance();
            res.setTime(service.getLatestDeploymentTime(new Date()));
            return res;
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public String getEntitiesForNamesAndType(
            StringList nameList
          , DomainObjectEnum domainObjectType
    ) throws PolicyServiceFault {
        assert service != null;
        Collection<String> strings;
        try {
            strings = service.getEntitiesForNamesAndType(
                    DTOUtils.makeCollectionOfStrings(nameList)
                  , DTOUtils.makeEntityType(domainObjectType)
            );
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.mergeStrings(strings);
    }

    @Override
    public EntityDescriptorList getDescriptorsForNameAndType(
            String nameTemplate
          , DomainObjectEnumList domainObjectTypes
    ) throws PolicyServiceFault {
        assert service != null;
        Collection<DomainObjectDescriptor> descriptors;
        try {
            descriptors = service.getDescriptorsForNameAndType(
                    nameTemplate
                  , DTOUtils.makeEntityTypeCollection(domainObjectTypes)
            );
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDescriptorList(descriptors);
    }

    @Override
    public String getEntitiesForDescriptors(
            EntityDescriptorList descriptorList
    ) throws PolicyServiceFault {
        assert service != null;
        Collection<String> strings;
        try {
            Collection<Long> ids = DomainObjectDescriptor.extractIds(
                    DTOUtils.makeListOfDescriptors(descriptorList));
            strings = service.getEntitiesForIds(ids);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.mergeStrings(strings);

    }

    @Override
    public EntityDigestList getDODDigests(
            StringList typeList
    ) throws PolicyServiceFault {
        assert service != null;
        Collection<DODDigest> dods;
        try {
            dods = service.getDODDigests(
                    DTOUtils.makeCollectionOfStrings(typeList)
            );
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDigestList(dods);
    }

    @Override
    public EntityDigestList getDODDigestsByIds(
            ListOfIds elementIds
    ) throws PolicyServiceFault {
        assert service != null;
        Collection<DODDigest> dods;
        try {
            dods = service.getEntityDigestsForIDs(
                    DTOUtils.makeListFromListOfIds(elementIds)
            );
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDigestList(dods);
    }
    
    @Override
    public EntityDescriptorList getDescriptorsByIds(
            ListOfIds elementIds
    ) throws PolicyServiceFault {
        assert service != null;
        Collection<DomainObjectDescriptor> descriptors;
        try {
            descriptors = service.getEntityDescriptorsForIDs(
                    DTOUtils.makeListFromListOfIds(elementIds)
            );
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDescriptorList(descriptors);
    }

    @Override
    public EntityDigestList saveEntitiesDigest(
            String pql
    ) throws PolicyServiceFault {
        assert service != null;
        Collection<DODDigest> dods;
        try {
            dods = service.saveEntitiesDigest(pql);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDigestList(dods);
    }


    @Override
    public EntityDescriptorList saveEntities(
            String pql
    ) throws PolicyServiceFault {
        assert service != null;
        try {
            Collection<DomainObjectDescriptor> descriptors = service.saveEntities(pql);
            return DTOUtils.makeEntityDescriptorList(descriptors);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public EntityDescriptorList getReferringObjectsForGroup(
            String nameTemplate
          , DomainObjectEnum domainObjectType
          , DomainObjectEnum referringType
          , boolean onlyDirect
    ) throws PolicyServiceFault, CircularReferenceFault {
        Collection<DomainObjectDescriptor> descriptors;
        try {
            descriptors = service.getReferringObjectsForGroup(
                    nameTemplate
                  , DTOUtils.makeEntityType(domainObjectType)
                  , DTOUtils.makeEntityType(referringType)
                  , onlyDirect
            );
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        } catch (CircularReferenceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDescriptorList(descriptors);
    }

    @Override
    public EntityDescriptorList getReferringObjectsAsOf(
            EntityDescriptorList descriptors
          , Calendar asOf
          , boolean onlyDirect
    ) throws CircularReferenceFault, PolicyServiceFault {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        Collection<DomainObjectDescriptor> dods;
        try {
            dods = service.getReferringObjectsAsOf(
                    DTOUtils.makeListOfDescriptors(descriptors)
                  , UnmodifiableDate.forTime(asOf.getTimeInMillis())
                  , onlyDirect
            );
        } catch (CircularReferenceException e) {
            throw makeFaultForException(e);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDescriptorList(dods);
    }

    @Override
    public EntityDescriptorList getDependencies(
            EntityDescriptorList descriptors
          , boolean directOnly
    ) throws CircularReferenceFault, PolicyServiceFault {
        Collection<DomainObjectDescriptor> dods;
        try {
            dods = service.getDependencies(
                    DTOUtils.makeListOfDescriptors(descriptors)
                  , directOnly
            );
        } catch (CircularReferenceException e) {
            throw makeFaultForException(e);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDescriptorList(dods);
    }

    @Override
    public EntityDigestList getDependenciesDigest(
            EntityDigestList digests
          , boolean directOnly
    ) throws CircularReferenceFault, PolicyServiceFault {
        Collection<DODDigest> doddigests;
        try {
            doddigests = service.getDependenciesDigest(
                    DTOUtils.makeListOfDigests(digests)
                  , directOnly
            );
        } catch (CircularReferenceException e) {
            throw makeFaultForException(e);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDigestList(doddigests);
    }

    @Override
    public String objectLock(
            BigInteger id
          , LockRequestType type
    ) throws PolicyServiceFault {
        if (id == null) {
            throw new NullPointerException("id");
        }
        Long ID = new Long(id.longValue());
        if (type.equals(LockRequestType.QUERY)) {
            return service.getLockHolder(ID);
        } else if (type.equals(LockRequestType.LOCK)) {
            try {
				return service.acquireLock(ID, false);
            } catch (PolicyServiceException e) {
                throw makeFaultForException(e);
            }
        } else if (type.equals(LockRequestType.FORCE)) {
            try {
				return service.acquireLock(ID, true);
            } catch (PolicyServiceException e) {
                throw makeFaultForException(e);
            }
        } else if (type.equals(LockRequestType.UNLOCK)) {
            try {
				return service.releaseLock(ID);
            } catch (PolicyServiceException e) {
                throw makeFaultForException(e);
            }
        } else {
            throw new IllegalArgumentException("type");
        }
    }

    @Override
    public EntityDescriptorList getDescriptorsForState(
            DomainObjectStateEnum objectState
    ) throws PolicyServiceFault {
        if (objectState == null) {
            throw new NullPointerException("objectState");
        }
        Collection<DomainObjectDescriptor> descriptors;
        try {
            descriptors = service.getDescriptorsForState(
                    DTOUtils.makeDevelopmentStatus(objectState)
            );
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDescriptorList(descriptors);

    }

    @Override
    public AttributeDescriptorList getSubjectAttributes(
            DomainObjectEnum domainObjectType
    ) throws PolicyServiceFault {
        try { 
            return SharedUtils.getSubjectAttributes(
                    DTOUtils.makeEntityType(domainObjectType), dpsConfig, service);
        } catch (Exception e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public AttributeDescriptorList getResourceAttributes(
            String subtypeName
    ) throws PolicyServiceFault {
        try { 
            return SharedUtils.getResourceAttributes(subtypeName, dpsConfig);
        } catch (Exception e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public AttributeDescriptorList getCustomResourceAttributes(
            String subtypeName
    ) throws PolicyServiceFault {
        try {
            return SharedUtils.getCustomResourceAttributes(subtypeName, dpsConfig);
        } catch (Exception ex) {
            throw makeFaultForException(ex);
        }
    }
    
    @Override
    public LeafObjectList getLeafObjectsByIds(
            ListOfIds elementIds
          , ListOfIds userGroupIds
          , ListOfIds hostGroupIds
    ) throws RemoteException, PolicyServiceFault {
        try {
            return DTOUtils.makeLeafObjectList(
                service.getLeafObjectsForIds(
                    DTOUtils.makeArrayOfIds(elementIds)
                ,   DTOUtils.makeArrayOfIds(userGroupIds)
                ,   DTOUtils.makeArrayOfIds(hostGroupIds)
                )
            );
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    private class PredicateToAgentSearchConverter extends DefaultPredicateVisitor {
        private boolean isSearchById = false;
        private final AgentQuerySpecImpl searchSpec = new AgentQuerySpecImpl();
        private final Stack<IAgentMgrQueryTerm> stack = new Stack<IAgentMgrQueryTerm>();
        private final IAgentMgrQueryTermFactory qtf = agentManager.getAgentMgrQueryTermFactory();

        @Override
        public void visit(ICompositePredicate pred, boolean preorder) {
            if (pred.getOp() == BooleanOp.AND) {
                for ( int i = 0 ; !stack.isEmpty() && i != pred.predicateCount() ; i++ ) {
                    searchSpec.addSearchSpecTerm(stack.pop());
                }
            } else if (pred.getOp() == BooleanOp.OR) {
                Set<IAgentMgrQueryTerm> orTerms = new HashSet<IAgentMgrQueryTerm>();
                for ( int i = 0 ; !stack.isEmpty() && i != pred.predicateCount() ; i++ ) {
                    orTerms.add(stack.pop());
                }
                stack.push(qtf.getORCompositeQueryTerm(orTerms));
            }
        }
        
        @Override
        public void visit(IRelation pred) {
            IExpression attrExpr = pred.getLHS();
            IExpression constExpr = pred.getRHS();
            // Watch out for switched LHS and RHS
            if (constExpr instanceof IAttribute) {
                IExpression tmp = attrExpr;
                attrExpr = constExpr;
                constExpr = tmp;
            }
            if (constExpr instanceof Constant && attrExpr instanceof IAttribute) {
                Constant constVal = (Constant)constExpr;
                String val = constVal.getRepresentation();
                IAttribute attr = (IAttribute)attrExpr;
                if (attr == SubjectAttribute.HOST_NAME) {
                    RelationalOp op = RelationalOp.EQUALS;
                    if (val.endsWith("*")) {
                        op = RelationalOp.LIKE;
                        val = val.replaceAll("[*]", "%");
                    }
                    stack.push(
                        qtf.getConcreteQueryTerm(
                            AgentMgrQueryFieldType.HOST
                        ,   op
                        ,   val
                        )
                    );
                } else if (attr == AgentAttribute.ID) {
                    isSearchById = true;
                    try {
                        Long idVal = Long.parseLong(val);
                        stack.push(
                            qtf.getConcreteQueryTerm(
                                AgentMgrQueryFieldType.ID
                            ,   RelationalOp.EQUALS
                            ,   idVal
                            )
                        );
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
        }

        public IAgentMgrQuerySpec getQuery(AgentTypeEnumType agentType, int maxResults) {
            if (stack.size() == 1) {
                searchSpec.addSearchSpecTerm(stack.pop());
                if (!isSearchById) {
                    searchSpec.addSearchSpecTerm(
                        qtf.getConcreteQueryTerm(
                            AgentMgrQueryFieldType.TYPE
                        ,   RelationalOp.EQUALS
                        ,   agentManager.getAgentType(agentType.getName())
                        )
                    );
                }
                searchSpec.setLimit(maxResults);
            }
            return searchSpec;
        }

    }

    @Override
    public LeafObjectList runLeafObjectQuery(
            LeafObjectSearchSpecDTO spec
    ) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        try {
            Collection<IHasId> specs;
            try {
                specs = DTOUtils.makeCollectionOfSpecs(spec.getSpec());
            } catch (PQLException pe) {
                throw new PolicyServiceFault(pe.getClass().getName(), pe.getMessage());
            }
            if (specs == null || specs.isEmpty()) {
                throw new PolicyServiceFault("IAE", "Invalid or empty predicate in the request.");
            }

            // Only the first predicate is used (there should never be more than one).
            IPredicate pred = ((ISpec)(specs.iterator().next())).getPredicate();
            LeafObjectType leafType = DTOUtils.makeLeafObjectType(spec.getType());
            if (leafType == null) {
                throw new PolicyServiceFault("NPE", "Leaf type is null.");
            }

            AgentTypeEnumType agentType = leafType.getAgentType();
            Collection<LeafObject> agents;
            
            if (agentType == null) {
                agents = service.queryLeafObjects(
                        pred
                    ,   spec.getNamespaceId()
                    ,   leafType
                    ,   spec.getMaxResults().intValue()
                );
            } else {
                // Get the list of agents of the specified type satisfying the condition from the predicate.

                // Convert the predicate to agent query search
                PredicateToAgentSearchConverter converter = new PredicateToAgentSearchConverter();
                pred.accept(converter, IPredicateVisitor.POSTORDER);

                // Use the query spec to get the agents
                IAgentMgrQuerySpec agentSpec = converter.getQuery(agentType, spec.getMaxResults().intValue());
                IAgentDO[] agentData = agentManager.getAgents(agentSpec).getAgents();

                // Convert the results to a list of leaf objects
                agents = new ArrayList<LeafObject>();
                for (IAgentDO agent : agentData) {
                    AgentTypeEnumType type = AgentTypeEnumType.getAgentType(agent.getType().getId());
                    LeafObject agentLeaf = new LeafObject(LeafObjectType.forAgentType(type));
                    agentLeaf.setId(agent.getId());
                    agentLeaf.setDomainName("<UNKNOWN>");
                    agentLeaf.setName(agent.getHost());
                    agentLeaf.setUid(""+agent.getId());
                    agentLeaf.setUniqueName(agent.getHost()+":"+agent.getId());
                    agents.add(agentLeaf);
                }
            }
            return DTOUtils.makeLeafObjectList(agents);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        } catch (InvalidQuerySpecException e) {
            throw makeFaultForException(e);
        } catch (PersistenceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public LeafObjectList getMatchingSubjects(
            EntityDescriptorDTO dto
    ) throws PolicyServiceFault, CircularReferenceFault {
        try {
            Collection<LeafObject> leafVals = service.getMatchingSubjects(DTOUtils.makeDomainObjectDescriptor(dto));
            return DTOUtils.makeLeafObjectList(leafVals);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        } catch (CircularReferenceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public LeafObjectDTO getSuperUser(
    ) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        IApplicationUserManager appUserManager = getApplicationUserManager();

        LeafObjectDTO userToReturn = null;

        try {
            IApplicationUser superUser = appUserManager.getSuperUser();
            LeafObject superUserLeaf = new LeafObject(LeafObjectType.APPUSER);
            superUserLeaf.setName(superUser.getDisplayName());
            superUserLeaf.setUid(superUser.getDestinyId().toString());
            superUserLeaf.setId(superUser.getDestinyId());
            superUserLeaf.setUniqueName(superUser.getUniqueName());
            userToReturn = DTOUtils.makeLeafObjectDTO(superUserLeaf);
        } catch (UserManagementAccessException exception) {
            throw new PolicyServiceFault(exception.getClass().getName(), "Failed to retrieve super user");
        }

        return userToReturn;
    }

    @Override
    public LeafObjectList getMatchingActions(
            EntityDescriptorDTO dto
    ) throws PolicyServiceFault, CircularReferenceFault {
        try {
            Collection<LeafObject> leafVals = service.getMatchingActions(
                    DTOUtils.makeDomainObjectDescriptor(dto)
            );
            return DTOUtils.makeLeafObjectList(leafVals);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        } catch (CircularReferenceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public String getFullyResolvedEntity(
            EntityDescriptorDTO dto
    ) throws PolicyServiceFault, CircularReferenceFault {
        try {
            DomainObjectDescriptor descr = DTOUtils.makeDomainObjectDescriptor(dto);
            return service.getFullyResolvedEntity(descr);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        } catch (CircularReferenceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public EntityDescriptorList getDeployedObjectDescriptors(
            EntityDescriptorList descriptors
          , Calendar asOf
    ) throws PolicyServiceFault {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        try {
            Collection<DomainObjectDescriptor> dods = service.getDeployedObjectDescriptors(
                    DomainObjectDescriptor.extractIds(DTOUtils.makeListOfDescriptors(descriptors))
                  , UnmodifiableDate.forTime(asOf.getTimeInMillis())
            );
            return DTOUtils.makeEntityDescriptorList(dods);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public String getDeployedObjects(
            EntityDescriptorList descriptors
          , Calendar asOf
    ) throws PolicyServiceFault {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        try {
            Collection<String> strings = service.getDeployedObjects(
                    DomainObjectDescriptor.extractIds(DTOUtils.makeListOfDescriptors(descriptors))
                  , UnmodifiableDate.forTime(asOf.getTimeInMillis())
            );
            return DTOUtils.mergeStrings(strings);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public EntityDescriptorList getDependenciesAsOf(
            EntityDescriptorList descriptors
          , Calendar asOf
    ) throws CircularReferenceFault, PolicyServiceFault {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        try {
            Collection<DomainObjectDescriptor> dods = service.getDependenciesAsOf(
                    DTOUtils.makeListOfDescriptors(descriptors)
                  , UnmodifiableDate.forTime(asOf.getTimeInMillis())
            );
            return DTOUtils.makeEntityDescriptorList(dods);
        } catch (CircularReferenceException e) {
            throw makeFaultForException(e);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public EntityDescriptorList descriptorsForDeploymentRecord(
            DeploymentRecordDTO record
    ) throws PolicyServiceFault {
        if (record == null) {
            throw new NullPointerException("record");
        }
        try {
            Collection<DomainObjectDescriptor> descriptors = service.descriptorsForDeploymentRecord(
                    DTOUtils.makeDeploymentRecord(record
            ));
            return DTOUtils.makeEntityDescriptorList(descriptors);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public DeploymentHistoryList getDeploymentHistory(
            EntityDescriptorDTO descriptor
    ) throws PolicyServiceFault {
        if (descriptor == null) {
            throw new NullPointerException("descriptor");
        }
        try {
            Collection<DeploymentHistory> trs = service.getDeploymentHistory(
                    DTOUtils.makeDomainObjectDescriptor(descriptor)
            );
            return DTOUtils.makeDeploymentHistoryList(trs);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public DeploymentRecordDTO scheduleDeployment(
            EntityDescriptorList descriptors
          , Calendar theDate
    ) throws PolicyServiceFault {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (theDate == null) {
            throw new NullPointerException("theDate");
        }
        try {
        	DeploymentRecordDTO deploymentRecordDTO = DTOUtils.makeDeploymentRecordDTO(
    	            service.scheduleDeployment(
                            DTOUtils.makeListOfDescriptors(descriptors)
                          , UnmodifiableDate.forTime(theDate.getTimeInMillis())));
            return deploymentRecordDTO;
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public DeploymentRecordDTO scheduleUndeployment(
            EntityDescriptorList descriptors
          , Calendar theDate
    ) throws PolicyServiceFault {
        if (descriptors == null) {
            throw new NullPointerException("descriptors");
        }
        if (theDate == null) {
            throw new NullPointerException("theDate");
        }
        try {
            DeploymentRecordDTO deploymentRecordDTO = DTOUtils.makeDeploymentRecordDTO(
                    service.scheduleUndeployment(
                            DTOUtils.makeListOfDescriptors(descriptors)
                          , UnmodifiableDate.forTime(theDate.getTimeInMillis())));
            return deploymentRecordDTO;
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public void cancelScheduledDeployment(DeploymentRecordDTO record) throws PolicyServiceFault {
        if (record == null) {
            throw new NullPointerException("record");
        }
        try {
            service.cancelScheduledDeployment(DTOUtils.makeDeploymentRecord(record));
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public DeploymentRecordList getDeploymentRecords(
            long from
          , long to
    ) throws PolicyServiceFault {
        try {
            Collection<DeploymentRecord> record = service.getDeploymentRecords(
                    UnmodifiableDate.forTime(from)
                  , UnmodifiableDate.forTime(to)
            );
            return DTOUtils.makeDeploymentRecordList(record);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public DomainObjectUsageListDTO getUsageList(
            EntityDescriptorList descriptorListReq
    ) throws RemoteException, ServiceNotReadyFault {
        try {
            List<DomainObjectUsage> objects = service.getUsageList(
                    DTOUtils.makeListOfDescriptors(descriptorListReq)
            );
            return DTOUtils.makeUsageListDTO(objects);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public EntityDescriptorList ensureOperationIsAllowed(
            EntityDescriptorList descriptors
          , Access desiredAccess
    ) throws PolicyServiceFault {
        try {
            Collection<DomainObjectDescriptor> dods = service.ensureOperationIsAllowed(
                    DTOUtils.makeListOfDescriptors(descriptors)
                  , DTOUtils.makeAction(desiredAccess)
            );
            return DTOUtils.makeEntityDescriptorList(dods);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public AccessList allowedActions(
            EntityDescriptorList descriptors
    ) throws PolicyServiceFault {
        try {
            Collection<? extends IAction> actions = service.allowedActions(
                    DTOUtils.makeListOfDescriptors(descriptors)
            );
            return DTOUtils.makeAccessList(actions);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    @Override
    public AgentStatusList getAgentList(
    ) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        Collection<AgentStatusDescriptor> asd;
        try {
            asd = service.addCountsToAgentDescriptors(getAgentListImpl());
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeAgentStatusList(asd);

    }

    @Override
    public AgentStatusList getAgentsForDeployedObject(
            EntityDescriptorDTO descriptor
          , Calendar theDate
    ) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        Collection<AgentStatusDescriptor> asd;
        try {
            asd = service.getAgentsForDeployedObject(
                    getAgentListImpl()
                  , DTOUtils.makeDomainObjectDescriptor(descriptor)
                  , theDate.getTime()
            );
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeAgentStatusList(asd);
    }

    @Override
    public EntityDescriptorList deploymentStatusForAgent(
            AgentStatusDescriptorDTO descriptor
    ) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        AgentStatusDescriptor agent = DTOUtils.makeAgentStatusDescriptor(descriptor);
        Collection<DomainObjectDescriptor> descriptors;
        try {
            descriptors = service.deploymentStatusForAgent(agent);
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
        return DTOUtils.makeEntityDescriptorList(descriptors);
    }

    private Collection<AgentStatusDescriptor> getAgentListImpl() throws PolicyServiceFault {
        IAgentDO[] agents = getAllRegisteredAgentDOs();

        List<AgentStatusDescriptor> res = new ArrayList<AgentStatusDescriptor>();
        for (int i = 0; i != agents.length; i++) {
            Date bundleTimestamp = null;

            IAgentPolicyAssemblyStatus stat = agents[i].getPolicyAssemblyStatus();
            if (stat != null) {
                Calendar cal = stat.getLastAcknowledgedDeploymentBundleTimestamp();
                if (cal != null) {
                    bundleTimestamp = cal.getTime();
                }
            }

            int numPolicies = 0;
            int numComponents = 0;
            res.add(new AgentStatusDescriptor(
                agents[i].getId()
            ,   agents[i].getHost()
            ,   AgentTypeEnumType.getAgentType(agents[i].getType().getId())
            ,   bundleTimestamp == null ? UnmodifiableDate.START_OF_TIME : bundleTimestamp
            ,   numPolicies
            ,   numComponents
            ));
        }
        return res;
    }

    private IAgentDO[] getAllRegisteredAgentDOs() throws PolicyServiceFault {
        AgentQuerySpecImpl searchSpec = new AgentQuerySpecImpl();
        searchSpec.addSearchSpecTerm(
                agentManager.getAgentMgrQueryTermFactory().getConcreteQueryTerm(
                        AgentMgrQueryFieldType.REGISTERED
                      , RelationalOp.EQUALS
                      , Boolean.TRUE
                )
        );
        searchSpec.setLimit(0);
        try {
            return agentManager.getAgents(searchSpec).getAgents();
        } catch (PersistenceException e) {
            throw makeFaultForException(e);
        } catch (InvalidQuerySpecException e) {
            throw makeFaultForException(e);
        }
    }

    /**
     * Prepares the system for running unit tests.
     *
     * @throws PolicyServiceFault
     *             when the system cannot be prepared to run the unit tests.
     */
    @Override
    public void prepareForTests() throws PolicyServiceFault {
        try {
            service.prepareForTests();
        } catch (PolicyServiceException e) {
            throw makeFaultForException(e);
        }
    }

    /**
     * Creates an instance of <code>PolicyServiceFault</code> wrapping a given
     * <code>Exception</code>.
     *
     * @param ex
     *            the <code>Exception</code> object to be wrapped.
     * @return an instance of <code>PolicyServiceFault</code> wrapping a given
     *         <code>Exception</code>.
     */
    private static PolicyServiceFault makeFaultForException(Exception ex) {
        assert ex != null; // This method is called internally; ex must not be null.
        PolicyServiceFault res = new PolicyServiceFault();
        res.setCauseType(ex.getClass().toString());
        res.setCauseMessage(ex.getMessage());
        return res;
    }

    /**
     * Creates an instance of <code>PolicyServiceFault</code> wrapping a given
     * <code>Exception</code>.
     *
     * @param ex
     *            the <code>Exception</code> object to be wrapped.
     * @return an instance of <code>PolicyServiceFault</code> wrapping a given
     *         <code>Exception</code>.
     */
    private static CircularReferenceFault makeFaultForException(CircularReferenceException ex) {
        assert ex != null; // This method is called internally; ex must not be null.
        return new CircularReferenceFault(DTOUtils.makeEntityDescriptorList(ex.getChainOfReferences()));
    }

    /**
     * Retrieve the Application user manager
     *
     * @return the Application User Manager
     * @throws ServiceNotReadyFault
     */
    private IApplicationUserManager getApplicationUserManager() throws ServiceNotReadyFault {
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        if (appUserManager == null) {
            if (!compMgr.isComponentRegistered(IApplicationUserManagerFactory.COMP_NAME)) {
                throw new ServiceNotReadyFault();
            }

            try {
                IApplicationUserManagerFactory appUserManagerFactory = compMgr.getComponent(ApplicationUserManagerFactoryImpl.class);
                appUserManager = appUserManagerFactory.getSingleton();
            } catch (RuntimeException e) {
                throw new ServiceNotReadyFault();
            }
        }
        return appUserManager;
    }

    @Override
    public void changePassword(
            String uniqueName
          , String oldPassword
          , String newPassword
    ) throws RemoteException, InvalidPasswordFault {
        try {
            IApplicationUser appUser = appUserManager.getApplicationUser(uniqueName);
            IAuthenticationDomain authDomain = appUserManager.getAuthenticationDomain(appUser.getDomainName());
            authDomain.authenticateUser(appUser.getLogin(), oldPassword);
            appUserManager.updateApplicationUser(appUser, newPassword);
        } catch (AuthenticationFailedException e) {
            throw new InvalidPasswordFault();
        } catch (UserNotFoundException e) {
            throw new RemoteException("error changing user password for user " + uniqueName, e);
        } catch (UserManagementAccessException e) {
            throw new RemoteException("error changing user password for user " + uniqueName, e);
        } catch (DomainNotFoundException e) {
            throw new RemoteException("error changing user password for user " + uniqueName, e);
        }

    }

    @Override
    public ObligationDescriptorList getObligationDescriptors(
    ) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        if (confStore == null) {
            return null;
        }
        ICustomObligationsConfigurationDO obligations = confStore.retrieveCustomObligationsConfig();
        return new ObligationDescriptorList(convertCustomObligations(obligations.getCustomObligations()));
    }

    private ObligationDescriptorDTO[] convertCustomObligations(ICustomObligationConfigurationDO[] obls) {
        ObligationDescriptorDTO[] newObligations = new ObligationDescriptorDTO[obls.length];

        for (int i = 0; i < obls.length; i++) {
            newObligations[i] = new ObligationDescriptorDTO(
                    obls[i].getDisplayName()
                  , obls[i].getInvocationString()
                  , convertCustomObligationArguments(obls[i].getArguments())
            );
        }

        return newObligations;
    }

    private ObligationArgumentDTO[] convertCustomObligationArguments(ICustomObligationArgumentDO[] args) {
        ObligationArgumentDTO[] newArguments = new ObligationArgumentDTO[args.length];

        for (int i = 0; i < args.length; i++) {
            newArguments[i] = new ObligationArgumentDTO();

            newArguments[i].setDisplayName(args[i].getName());
            newArguments[i].setUserEditable(args[i].isArgumentUserEditable());
            newArguments[i].setHidden(args[i].isArgumentHidden());

            String defaultValue = args[i].getDefaultValue();
            String[] values = args[i].getValues();

            ObligationValueDTO[] newValues = new ObligationValueDTO[values.length];
            for (int j = 0; j < values.length; j++) {
                newValues[j] = new ObligationValueDTO(values[j], false);
                if (values[j].equals(defaultValue)) {
                    newValues[j].set_default(true);
                }
            }

            newArguments[i].setValues(newValues);
        }

        return newArguments;
    }


    @Override
    public PolicyActionsDescriptorList getPolicyActionsDescriptors(
    ) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        return policyActionsList;
    }

    @Override
    public LeafObjectList getAccessGroupsForUser(
            BigInteger id
    ) throws RemoteException {
        Set<IAccessGroup> accessGroup;
        try {
            accessGroup = appUserManager.getAccessGroupsContainingUser(id.longValue());
        } catch (UserManagementAccessException umae) {
            throw new RemoteException("could not get access groups for user with id " + id);
        } catch (UserNotFoundException unfe) {
            throw new RemoteException("No such user with id " + id);
        }
        Collection<LeafObject> leafVals = makeLeafObjectsFromAccessGroups(accessGroup);
        return DTOUtils.makeLeafObjectList(leafVals);
    }

    @Override
    public ComponentList getAllowedEntitiesForUser(
            BigInteger id
    ) throws RemoteException {
        IApplicationUserManager aum = getApplicationUserManager();
        Collection<Component> components;
        try {
            if (aum.getSuperUser().getDestinyId().longValue() == id.longValue()) {
                return DTOUtils.makeComponentList(DMSServiceImpl.entityReverseMap.keySet());
            }

            IApplicationUser appUser = appUserManager.getApplicationUser(id.longValue());
            SubjectDTO userDto = new SubjectDTO();

            userDto.setName(appUser.getUniqueName());
            userDto.setId(BigInteger.valueOf(appUser.getDestinyId().longValue()));
            userDto.setUid(appUser.getUniqueName());
            userDto.setUniqueName(appUser.getUniqueName());
            userDto.setType(SubjectType.APPUSER.getName());

            DMSUserData userData = userRoleService.getUserData(userDto);
            Role[] roles = userData.getRoles();
            components = new HashSet<Component>();

            for (Role role : roles) {
                SubjectDTO roleDto = new SubjectDTO();
                roleDto.setName(role.getValue().replace('_', ' '));
                roleDto.setId(BigInteger.valueOf(-1));
                roleDto.setUid(role.getValue().replace('_', ' '));
                roleDto.setType(SubjectType.AGGREGATE.getName());
                Component[] comps = userRoleService.getRoleData(roleDto).getComponents();
                if(comps != null){
                    Collections.addAll(components, comps);
                }
            }
        } catch (UserManagementAccessException umae) {
            throw new RemoteException("could not get access groups for user with id " + id);
        } catch (UserNotFoundException unfe) {
            throw new RemoteException("No such user with id " + id);
        } catch (PolicyServiceException pse) {
            throw new RemoteException("No such user with id " + id);
        }
        
        return DTOUtils.makeComponentList(components);
    }

    @Override
    public void updateComputersWithAgents(
    ) throws RemoteException {
        HostsWithAgentsTransferTask transferTask = manager.getComponent(HostsWithAgentsTransferTask.class);

        try {
            transferTask.run();
        } catch (PersistenceException exception) {
            throw new RemoteException("Failed to update computers.  Could not load agents", exception);
        } catch (EntityManagementException exception) {
            throw new RemoteException("Failed to update computers.", exception);
        } catch (PQLException exception) {
            throw new RemoteException("Failed to update computers.  Could not load create desktop component", exception);
        }
    }

    @Override
    public boolean hasObjectsToDeploy(
    ) throws PolicyServiceFault {
        try {
            return service.hasObjectsToDeploy();
        } catch (PolicyServiceException exception) {
            throw makeFaultForException(exception);
        }
    }

    @Override
    public StringList getDictionaryEnrollmentNames( 
            PolicyEditorRoles role 
    ) throws RemoteException, ServiceNotReadyFault {
        Set<String> enrollmentNames;
        try {
            enrollmentNames = service.getDictionaryEnrollmentNames();
        } catch (PolicyServiceException exception) {
            throw makeFaultForException(exception);
        }
        
        String[] enrollmentNamesInArray = enrollmentNames.toArray(new String[enrollmentNames.size()]);
        return new StringList(enrollmentNamesInArray);

    }

    @Override
    public RealmList getDictionaryEnrollmentRealms(
            PolicyEditorRoles role
    ) throws RemoteException, ServiceNotReadyFault {
        Set<Realm> enrollmentRealms;
        try {
            enrollmentRealms = service.getDictionaryEnrollmentRealms();
        } catch (PolicyServiceException exception) {
            throw makeFaultForException(exception);
        }
        Realm[] enrollmentRealmsInArray = enrollmentRealms.toArray(new Realm[enrollmentRealms.size()]);
        return new RealmList(enrollmentRealmsInArray);
    }

    private Set<LeafObject> makeLeafObjectsFromAccessGroups(Set<IAccessGroup> accessGroups) {
        Set<LeafObject> res = new HashSet<LeafObject>();
        if (accessGroups != null) {
            for ( IAccessGroup group : accessGroups ) {
                LeafObject leaf = new LeafObject(LeafObjectType.ACCESSGROUP);
                leaf.setName(group.getTitle());
                leaf.setUniqueName(group.getTitle());
                leaf.setUid(group.getDestinyId().toString());
                leaf.setId(group.getDestinyId());
                res.add(leaf);
            }
        }
        return res;
    }

    /**
     * Create external data source
     * @param info
     * @throws RemoteException
     * @throws ExternalDataSourceFault
     */
    @Override
    public StringList getPortalURLList(
    ) throws RemoteException, ExternalDataSourceFault {
        String[] urls;
        try {
            urls = service.getPortalURLList();
        } catch (PolicyServiceException exception) {
            throw makeFaultForException(exception);
        }
        StringList urlsToReturn = urls != null ? new StringList(urls) : null;
        return urlsToReturn;
    }

    /**
     * Create external data source
     * @param info
     * @throws RemoteException
     * @throws ExternalDataSourceFault
     */
    @Override
    public int createExternalDataSource(
            ExternalDataSourceConnectionInfo info
    ) throws RemoteException, ExternalDataSourceFault {
        try {
            return service.createExternalDataSource(info);
        } catch (Exception exception) {
            throw makeFaultForException(exception);
        }
    }

    /**
     * Fetch the children of given resource tree node
     *
     * if the input node.value is null, fetch the root node
     * else fetch all children and set node.value with children updated
     *
     */
    @Override
    public ResourceTreeNodeList getResourceTreeNodeChildren(
            int resourceID
          , ResourceTreeNode node
    ) throws RemoteException, ExternalDataSourceFault {
        if ( node == null ) {
            throw new RemoteException("Invalid ResourceTreeNode: null");
        }
        try {
            ResourceTreeNodeList list = new ResourceTreeNodeList();
            node = service.getResourceTreeNodeChildren(resourceID, node);
            list.setChildren(node.getChildren());
            return list;
        } catch (Exception exception) {
            throw makeFaultForException(exception);
        }

    }

    /**
     * Get resource preview from a given EntityDescriptorDTO
     */
    @Override
    public ResourceTreeNodeList getMatchingPortalResource(
            int sourceID
          , EntityDescriptorDTO descriptor
    ) throws RemoteException, ServiceNotReadyFault, PolicyServiceFault {
        try {
            return service.getPortalResourcePreview(sourceID, DTOUtils.makeDomainObjectDescriptor(descriptor));
        } catch (Exception exception) {
            throw makeFaultForException(exception);
        }
    }

    @Override
    public void executePush(
    ) throws PolicyServiceFault{
    	schedulePush(Calendar.getInstance());
    }

    @Override
    public void schedulePush(
            Calendar pushTime
    ) throws PolicyServiceFault{
    	try {
            //create a deploymentRequest
            IHibernateRepository mgmtDataSource = (IHibernateRepository) manager.getComponent(
                    DestinyRepository.MANAGEMENT_REPOSITORY.getName());

            HashMapConfiguration config = new HashMapConfiguration();
            config.setProperty(IDeploymentRequestMgr.MGMT_DATA_SOURCE_CONFIG_PARAM, mgmtDataSource);
            ComponentInfo<IDeploymentRequestMgr> reqMgrCompInfo = 
                new ComponentInfo<IDeploymentRequestMgr>("deploymentRequestMgr",
                    DeploymentRequestMgrImpl.class,
                    IDeploymentRequestMgr.class,
                    LifestyleType.TRANSIENT_TYPE,
                    config);
            IDeploymentRequestMgr deploymentRequestMgr = manager.getComponent(reqMgrCompInfo);

            config = new HashMapConfiguration();
            config.setProperty(IPolicyDeployMgr.MGMT_DATA_SOURCE_CONFIG_PARAM, mgmtDataSource);

            ComponentInfo<IPolicyDeployMgr> deployMgrCompInfo = 
                new ComponentInfo<IPolicyDeployMgr>(
                    IPolicyDeployMgr.COMP_NAME,
                    PolicyDeployMgrImpl.class,
                    IPolicyDeployMgr.class,
                    LifestyleType.TRANSIENT_TYPE,
                    config);
            IPolicyDeployMgr policyDeployMgr = manager.getComponent(deployMgrCompInfo);

            IDeploymentRequest deploymentRequest = deploymentRequestMgr.createDeploymentRequest(pushTime);

            Long deployRequestId = deploymentRequest.getId();

            IAgentDO[] agents = getAllRegisteredAgentDOs();
            for (IAgentDO agent : agents) {
                deploymentRequest.addTargetHost(agent.getHost());
            }
            deploymentRequestMgr.saveDeploymentRequest(deploymentRequest);
            policyDeployMgr.executeDeploymentRequest(deployRequestId);
        } catch (Exception exception) {
            throw makeFaultForException(exception);
        }
    }

}
