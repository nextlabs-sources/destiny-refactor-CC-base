package com.bluejungle.pf.destiny.lib;

/*
 * All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author pkeni
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lib/DMSServiceImpl.java#1 $
 */

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.bluejungle.destiny.services.policy.types.Component;
import com.bluejungle.destiny.services.policy.types.DMSRoleData;
import com.bluejungle.destiny.services.policy.types.DMSUserData;
import com.bluejungle.destiny.services.policy.types.Role;
import com.bluejungle.destiny.services.policy.types.SubjectDTO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecManager;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.action.IAction;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;

/**
 * This class provides an implementation of the DMS service.
 */
@SuppressWarnings("deprecation")
public class DMSServiceImpl implements IDMSService, IManagerEnabled, IInitializable {

    public static final ComponentInfo<IDMSService> COMP_INFO = 
        new ComponentInfo<IDMSService>(
            DMSServiceImpl.class, 
            LifestyleType.SINGLETON_TYPE);
    
    private IComponentManager   manager;
    private IDSpecManager       specManager;
    private LifecycleManager    lifecycleManager;

    private static Map<String, Role> roleMap        = new HashMap<String, Role>();
    private static Map<Role, String> roleReverseMap = new HashMap<Role, String>();

    public static Map<EntityType, Component> entityMap        = new HashMap<EntityType, Component>();
    public static Map<Component, EntityType> entityReverseMap = new HashMap<Component, EntityType>();

    static {
        roleMap.put("System Administrator"  , Role.System_Administrator);
        roleMap.put("Policy Analyst"        , Role.Policy_Analyst);
        roleMap.put("Policy Administrator"  , Role.Policy_Administrator);
        roleMap.put("Business Analyst"      , Role.Business_Analyst);
        roleMap.put("Report Administrator", Role.Report_Administrator);

        roleReverseMap.put(Role.System_Administrator , "System Administrator");
        roleReverseMap.put(Role.Policy_Analyst       , "Policy Analyst");
        roleReverseMap.put(Role.Policy_Administrator , "Policy Administrator");
        roleReverseMap.put(Role.Business_Analyst     , "Business Analyst");
        roleReverseMap.put(Role.Report_Administrator     , "Report Administrator");

        entityMap.put(EntityType.POLICY, Component.Policy);
        entityMap.put(EntityType.PORTAL, Component.Portal);
        entityMap.put(EntityType.HOST, Component.Desktop);
        entityMap.put(EntityType.APPLICATION, Component.Application);
        entityMap.put(EntityType.LOCATION, Component.Location);
        entityMap.put(EntityType.ACTION, Component.Action);
        entityMap.put(EntityType.RESOURCE, Component.Resource);
        entityMap.put(EntityType.USER, Component.User);
        entityMap.put(EntityType.DEVICE, Component.Device);
        entityMap.put(EntityType.SAP, Component.SAP);
        entityMap.put(EntityType.ENOVIA, Component.Enovia);

        entityReverseMap.put(Component.Policy, EntityType.POLICY);
        entityReverseMap.put(Component.Portal, EntityType.PORTAL);
        entityReverseMap.put(Component.Desktop, EntityType.HOST);
        entityReverseMap.put(Component.Application, EntityType.APPLICATION);
        entityReverseMap.put(Component.Location, EntityType.LOCATION);
        entityReverseMap.put(Component.Action, EntityType.ACTION);
        entityReverseMap.put(Component.Resource, EntityType.RESOURCE);
        entityReverseMap.put(Component.User, EntityType.USER);
        entityReverseMap.put(Component.Device, EntityType.DEVICE);
        entityReverseMap.put(Component.SAP, EntityType.SAP);
        entityReverseMap.put(Component.Enovia, EntityType.ENOVIA);
    }

    /**
     * 
     */
    public DMSServiceImpl() {
        super();
    }

    @Override
    public Collection<?> getAllUsers() throws PolicyServiceException {
    	throw new PolicyServiceException("Should not reach here!");
    }
    
    private IDSubject convertToUserSubject(SubjectDTO subjDTO){
        return new Subject(
                subjDTO.getUid()
              , subjDTO.getUniqueName()
              , subjDTO.getName()
              , new Long(subjDTO.getId().longValue())
              , SubjectType.USER);
    }
    
    private DevelopmentEntity getEntityForName(String name) throws PolicyServiceException{
        DevelopmentEntity entity;
        try {
            entity = lifecycleManager.getEntityForName(EntityType.COMPONENT, name, LifecycleManager.MUST_EXIST);
        } catch (EntityManagementException eme) {
            throw new PolicyServiceException("Could not find " + name + " definition");
        }
        return entity;
    }
    
    private IDSpec getUserSpec() throws PolicyServiceException {
        DevelopmentEntity entity = getEntityForName("User");

        IDSpec parsedRole = getIDSpec(entity);
        return parsedRole;
    }

    private IDSpec getIDSpec(DevelopmentEntity entity)
            throws PolicyServiceException {
        IDSpec parsedRole;
        try {
            DomainObjectBuilder dob = new DomainObjectBuilder(entity.getPql());
            parsedRole = dob.processSpec();
        } catch (PQLException pe) {
            throw new PolicyServiceException("Could not parse User definition");
        }
        return parsedRole;
    }
    
    @Override
    public void addToUsers(SubjectDTO subjDTO) throws PolicyServiceException {
        IDSubject subj = convertToUserSubject(subjDTO);
        IDSpec parsedRole = getUserSpec();
        insertUser( subj, parsedRole );
    }

    @Override
    public void deleteFromUsers(SubjectDTO subjDTO) throws PolicyServiceException {
        IDSubject subj = convertToUserSubject(subjDTO);
        IDSpec parsedRole = getUserSpec();
        deleteUser( subj, parsedRole );
    }

    @Override
    public Collection<SubjectDTO> getAllRoles() throws PolicyServiceException {
        Collection<SubjectDTO> retCollection = new HashSet<SubjectDTO>();

        for (Role role : roleReverseMap.keySet()) {
            SubjectDTO dto = new SubjectDTO();
            String roleName = role.getValue().replace('_', ' '); 
            dto.setName(roleName);
            DevelopmentEntity entity = getEntityForName(roleName);
            dto.setId(BigInteger.valueOf(entity.getId().longValue()));
            dto.setUid(roleName);
            dto.setType(SubjectType.AGGREGATE.getName());
            
            retCollection.add(dto);
        }

        return retCollection;
    }

    @Override
    public DMSUserData getUserData(SubjectDTO subjDTO) throws PolicyServiceException {
        if (subjDTO == null){
            throw new IllegalArgumentException("Invalid null argument");
        }

        Collection<Role> retRoles = new HashSet<Role>();
        IDSubject subj = convertToUserSubject(subjDTO);
        EvaluationRequest req = new EvaluationRequest();
        req.setUser(subj);

        for (String name : roleMap.keySet()) {
            DevelopmentEntity entity = getEntityForName(name);

            IPredicate pred = getIDSpec(entity).getPredicate();
            if (pred.match(req)) {
                retRoles.add(roleMap.get(name));
            }
        }

        DMSUserData ret = new DMSUserData();
        ret.setRoles(retRoles.toArray(new Role[retRoles.size()]));
        return ret;
    }

    @Override
    public void setUserData(SubjectDTO subjDTO, DMSUserData data) throws PolicyServiceException {
        if (subjDTO == null || data == null) {
            throw new IllegalArgumentException("Invalid null argument");
        }

        try {
            Set<DevelopmentEntity> newEntities = new HashSet<DevelopmentEntity>();
            IDSubject subj = convertToUserSubject(subjDTO);
            
            for (String name : roleMap.keySet()) {
                DevelopmentEntity entity = getEntityForName(name);
                
                IDSpec parsedRole = getIDSpec(entity);
                
                boolean bMatched = false;

                if (data.getRoles() != null) {
                    for (Role role : data.getRoles()) {
                        String parsedRoleName = parsedRole.getName();
                        String inRoleName     = roleReverseMap.get(role);
                        
                        if (parsedRoleName.compareTo(inRoleName) == 0) {
                            bMatched = true;
                        }
                    }
                }
                    
                if (bMatched) {
                    insertUser( subj, parsedRole );
                } else {
                    deleteUser( subj, parsedRole );
                }
                
                DomainObjectFormatter dof = new DomainObjectFormatter();
                dof.formatDef(parsedRole);
                entity.setPql(dof.getPQL());
                newEntities.add(entity);
            }
            
            if (!newEntities.isEmpty()) {
                try {
                    lifecycleManager.saveEntities(newEntities, LifecycleManager.MUST_EXIST, null);
                } catch (EntityManagementException eme) {
                    throw new PolicyServiceException("Error saving user data");
                }
            }
        } catch ( PQLException pe ) {
            throw new PolicyServiceException( pe );
        }
    }

    @Override
    public DMSRoleData getRoleDataById(Long id) throws PolicyServiceException {
        if (id == null) {
            throw new IllegalArgumentException("Invalid null argument");
        }

        DevelopmentEntity entity;
        try {
            Collection<DevelopmentEntity> entities = lifecycleManager.getEntitiesForIDs(Collections.singleton(id));
            assert entities != null && entities.size() == 1;
            entity = entities.iterator().next();
        } catch (EntityManagementException eme) {
            throw new PolicyServiceException("Could not find  definition for ID: " + id);
        }
        SubjectDTO dto = new SubjectDTO();
        dto.setName(entity.getName()); 
        dto.setId(BigInteger.valueOf(entity.getId().longValue())); 
        dto.setUid(entity.getName()); 
        dto.setType(SubjectType.AGGREGATE.getName());
        return getRoleData(dto);
    }

    @Override
    public Collection<DMSRoleData> getAllRoleData() throws PolicyServiceException {
        Collection<SubjectDTO> allRoles = getAllRoles();
        Collection<DMSRoleData> retCollection = new HashSet<DMSRoleData>();

        for (SubjectDTO dto : allRoles) {
            retCollection.add(getRoleData(dto));
        }
        return retCollection;
    }

    @Override
    public DMSRoleData getRoleData(SubjectDTO role) throws PolicyServiceException {
        if (role == null) {
            throw new IllegalArgumentException("Invalid null argument");
        }

        DevelopmentEntity entity = getEntityForName(role.getUid());;

        IDSpec parsedRole = getIDSpec(entity);
        IAccessPolicy accessPolicy = parsedRole.getAccessPolicy();
        
        DMSRoleData roleData = new DMSRoleData();
        roleData.setName(role.getUid());
        
        readRoleAppData(role, roleData);

        Set<Component> allowedEntities = new HashSet<Component>();
        for (EntityType type : accessPolicy.getAllowedEntities()) {
            allowedEntities.add(entityMap.get(type));
        }
        if (!allowedEntities.isEmpty()) {
            roleData.setComponents(allowedEntities.toArray(new Component [allowedEntities.size()]));
        }

        return roleData;
    }
    
    @Override
    public void setRoleDataById(Long id, DMSRoleData data) throws PolicyServiceException {
        if (id == null){
            throw new IllegalArgumentException("Invalid null argument");
        }

        DevelopmentEntity entity;
        try {
            Collection<DevelopmentEntity> entities = lifecycleManager.getEntitiesForIDs(Collections.singleton(id));
            assert entities != null && entities.size() == 1;
            entity = entities.iterator().next();
        } catch (EntityManagementException eme) {
            throw new PolicyServiceException("Could not find definition for ID: " + id);
        }
        SubjectDTO dto = new SubjectDTO();
        dto.setName(entity.getName()); 
        dto.setId(BigInteger.valueOf(entity.getId().longValue())); 
        dto.setUid(entity.getName()); 
        dto.setType(SubjectType.AGGREGATE.getName());
        setRoleData(dto, data);
    }

    @Override
    public void setRoleData(SubjectDTO role, DMSRoleData roleData) throws PolicyServiceException {
        if (role == null) {
            throw new IllegalArgumentException("Invalid null argument");
        }

        try {
            DevelopmentEntity entity = getEntityForName(role.getUid());
            
            DomainObjectBuilder dob = new DomainObjectBuilder(entity.getPql());
            IDSpec parsedRole = dob.processSpec();
            
            IAccessPolicy accessPolicy = parsedRole.getAccessPolicy();
            
            writeRoleAppData(role, roleData);
            Collection<EntityType> entities = new HashSet<EntityType>();
            if (roleData.getComponents() != null) {
                for (Component component : roleData.getComponents()) {
                    entities.add(entityReverseMap.get(component));
                }
                
            }
            accessPolicy.setAllowedEntities(entities);
            
            DomainObjectFormatter dof = new DomainObjectFormatter();
            dof.formatDef(parsedRole);
            entity.setPql(dof.getPQL());
            dof.reset();
            dof.formatAccessPolicy(accessPolicy);
            entity.setApPql(dof.getPQL());
            
            lifecycleManager.saveEntity(entity, LifecycleManager.MUST_EXIST, null);
        } catch (EntityManagementException eme) {
            throw new PolicyServiceException( eme );
        } catch (PQLException pqlEx) {
            throw new PolicyServiceException( pqlEx );
        }
        
        return;
    }
    
    /* (non-Javadoc)
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    @Override
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    @Override
    public IComponentManager getManager() {
        return manager;
    }

    /* (non-Javadoc)
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    @Override
    public void init() {
        specManager      = manager.getComponent(IDSpecManager.COMP_INFO);
        lifecycleManager = manager.getComponent(LifecycleManager.COMP_INFO);
    }

    public static boolean userExistsInRelation(IDSubject user, IRelation relation) {
        IExpression specAttribute;
        IExpression constant;
        
        if (relation.getLHS() instanceof SpecAttribute) {
            specAttribute = relation.getLHS();
            constant = relation.getRHS();
        } else {
            specAttribute = relation.getRHS();
            constant = relation.getLHS();
        }
        
        SpecAttribute attr = (SpecAttribute)specAttribute;
        String val         = (String) ((Constant) constant).getValue().getValue();
        
        if (attr != SubjectAttribute.USER_NAME) {
            return false;
        }
        return val.equalsIgnoreCase(user.getName());
    }

    public static void insertUser( IDSubject subject, IDSpec spec ) {
        IPredicate predicate = spec.getPredicate();
        if (!Predicates.find( predicate, new AccessPolicy.SubjectDetector( subject ) ) ) {
            IPredicate pred = SubjectAttribute.USER_NAME.buildRelation( RelationOp.EQUALS, subject.getUniqueName() );
            if (predicate instanceof CompositePredicate) {
                ((CompositePredicate) predicate).addPredicate(pred);
            } else {
                spec.setPredicate(new CompositePredicate(BooleanOp.OR, Arrays.asList( new IPredicate[] { predicate, pred } ) ) );
            }
        }
    }

    public static void deleteUser( IDSubject subject, IDSpec spec ) {
        IPredicate transformed = Predicates.transform(
            spec.getPredicate()
        ,   new AccessPolicy.SubjectDeleter(subject)
        ) ;
        spec.setPredicate(transformed != null ? transformed : PredicateConstants.FALSE);
    }

    private void readRoleAppData(SubjectDTO role, DMSRoleData data) throws PolicyServiceException {
        Set<String> allowedApps  = new HashSet<String>();
        
        DevelopmentEntity entity = getEntityForName(role.getName());
        IDSpec roleSpec = new SpecBase( 
                null            // IDSpecManager manager
              , SpecType.USER   // SpecType specType
              , entity.getId () // Long id
              , role.getName()  // String name
              , null            // String description
              , DevelopmentStatus.EMPTY // DevelopmentStatus status
              , null            // IPredicate pred
              , false           // boolean hidden
        );
        
        Collection<DevelopmentEntity> apps;
        try {
            apps = lifecycleManager.getAllApplicatinResourceComponents();
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }
        
        String[] allAppsNames = new String[apps.size()];
        
        int i = 0;
        for (DevelopmentEntity app : apps) {
            String appName = app.getName();
            allAppsNames[i++] = appName;
            
            IDSpec appSpec = getIDSpec(app);

            AccessPolicy accessPolicy;
            try {
                DomainObjectBuilder dob = new DomainObjectBuilder(app.getApPql());
                accessPolicy = (AccessPolicy) dob.processAccessPolicy();
            } catch (PQLException pe) {
                throw new PolicyServiceException("Could not parse " + appName + " definition");
            }

            appSpec.setAccessPolicy(accessPolicy);
            appSpec.setOwner(new Subject(
                    app.getOwner().toString()
                  , app.getOwner().toString()
                  , app.getOwner().toString()
                  , entity.getOwner()
                  , SubjectType.USER)
            );
            if (appSpec.checkRoleAccess (roleSpec, DAction.getAction(IDAction.READ_NAME))) {
                allowedApps.add(appName);
            }
        }

        data.setAllApps(allAppsNames);
        data.setAllowApps(allowedApps.toArray(new String[allowedApps.size()]));
    }

    private void writeRoleAppData(SubjectDTO role, DMSRoleData data) throws PolicyServiceException {
        String[] allowedApps = data.getAllowApps();
        Set<String> allowedAppsSet = new HashSet<String>();
        if (allowedApps != null) {
            Collections.addAll(allowedAppsSet, allowedApps);
        }
        
        Set<DevelopmentEntity> setApps = new HashSet<DevelopmentEntity>();
        final Set<IAction> actionSet = new HashSet<IAction>();
        actionSet.add( DAction.getAction(IDAction.READ_NAME) );
        
        // check if the role exist.
        getEntityForName(role.getName());

        IPredicate roleSpec = specManager.getSpecReference(role.getName());
        
        Collection<DevelopmentEntity> apps;
        try {
            apps = lifecycleManager.getAllApplicatinResourceComponents();
        } catch (EntityManagementException e) {
            throw new PolicyServiceException(e);
        }

        for (DevelopmentEntity app : apps) {
            String appName = app.getName();

            IDSpec appSpec = getIDSpec(app);
            
            boolean allowed = allowedAppsSet.contains(appName);
            
            try {
                appSpec.getAccessPolicy().setActionsForRole(roleSpec, (allowed ? actionSet : null));
                DomainObjectFormatter dof = new DomainObjectFormatter();
                dof.formatDef(appSpec);
                app.setPql(dof.getPQL());
                setApps.add(app);
            } catch (PQLException pe) {
                throw new PolicyServiceException("Could not set actions for role " + role.getName() + " on resource " + appName);
            }
        }
        
        try {
            lifecycleManager.saveEntities(setApps, LifecycleManager.MUST_EXIST, null);
        } catch (EntityManagementException eme) {
            throw new PolicyServiceException("Could not save entities");
        }
    }

}
