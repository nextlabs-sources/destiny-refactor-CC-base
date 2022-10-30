package com.nextlabs.pf.destiny.importexport.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lib.AccessPolicyComponent;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectSearchSpec;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorException;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.SpecReference;
import com.bluejungle.pf.domain.destiny.exceptions.PolicyReference;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.PolicyObject;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute.ExternalSubjectAttribute;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.misc.IAccessControlled;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;
import com.nextlabs.pf.destiny.importexport.ExportEntity;
import com.nextlabs.pf.destiny.importexport.ExportFile;
import com.nextlabs.pf.destiny.importexport.IComponentVerifier;
import com.nextlabs.pf.destiny.importexport.IImportConflict;
import com.nextlabs.pf.destiny.importexport.IImportState;
import com.nextlabs.pf.destiny.importexport.IImporter;
import com.nextlabs.pf.destiny.importexport.ImportException;
import com.nextlabs.pf.destiny.importexport.ImportFileParser;
import com.nextlabs.pf.destiny.importexport.ImportOverwriteManager;
import com.nextlabs.pf.destiny.importexport.PredicateHelpers;
import com.nextlabs.pf.destiny.importexport.IImportState.Shallow;
import com.nextlabs.pf.destiny.importexport.IImportState.RetainStatus;

public class Importer implements IImporter {
    private static final String BLUE_JUNGLE_DUMMY_OBJECT = "BlueJungleDummyObject";
    private static final String ALL_USERS_GROUP_TITLE = "All Policy Server Users";
    private static final Log LOG = LogFactory.getLog(Importer.class);
    
    private static final Set<SubjectAttribute> ID_BASED_ATTRIBUTES = new HashSet<SubjectAttribute>();
    static{
        ID_BASED_ATTRIBUTES.add(SubjectAttribute.APP_ID);
        ID_BASED_ATTRIBUTES.add(SubjectAttribute.HOST_ID);
        ID_BASED_ATTRIBUTES.add(SubjectAttribute.HOST_LDAP_GROUP_ID);
        ID_BASED_ATTRIBUTES.add(SubjectAttribute.USER_ID);
        ID_BASED_ATTRIBUTES.add(SubjectAttribute.USER_LDAP_GROUP_ID);
    }
    
    //    used to signal a dictionary error discovered while parsing PQL
    static final Long DICTIOANRY_ERROR_SIGN = new Long(-1);
    
    private final ExportFile importData;
    private final HashMap<NameAndType, String> renameMapping;
    private final TreeMap<Long, Long> userIdMapping;
    
    private IImportState importState;
    private IPolicyEditorClient client;
    private Long currentUserId;
    private RetainStatus retainStatus;
    
    /**
     * This method processes the import file and setup the rename suffix for conflicted policy
     * Constructor
     * @param importSource
     * @throws ImportException
     */
    public Importer(File importSource, Shallow failRecovery) throws ImportException {
        this(importSource, failRecovery, IImportState.RetainStatus.SET_TO_DRAFT);
    }
    /**
     * This method processes the import file and setup the rename suffix for conflicted policy
     * Constructor
     * @param importSource
     * @param failRecover
     * @param 
     * @throws ImportException
     */
    public Importer(File importSource, Shallow failRecovery, RetainStatus retainStatus) throws ImportException {
        this.retainStatus = retainStatus;
        importState = new ImportState(failRecovery);
        //parse the import file
        importData = ImportFileParser.parseFile(importSource);

        Collection<ExportEntity> entities = getEntities();
        if (entities != null) {
            SortedSet<ExportEntity> duplicated = checkNameUniqueness(entities);
            if (!duplicated.isEmpty()) {
                StringBuilder sb = new StringBuilder("The following entries have duplicated name. " +
                		"You can't import entries with same name and same type.\n");
                for(ExportEntity d : duplicated){
                    sb.append(d.getName()).append("\n");
                }
                throw new ImportException(sb.toString());
            }
        }
        renameMapping = new HashMap<NameAndType, String>();
        userIdMapping = new TreeMap<Long, Long>();
    }
    
    SortedSet<ExportEntity> checkNameUniqueness(Collection<ExportEntity> entities) {
        SortedSet<ExportEntity> duplicated = new TreeSet<ExportEntity>(new Comparator<ExportEntity>(){
            public int compare(ExportEntity o1, ExportEntity o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        Map<String, Map<String, ExportEntity>> map = new HashMap<String, Map<String, ExportEntity>>();
        for (ExportEntity e : entities) {
            String type = e.getType();
            String name = e.getName();
            if(name.endsWith(" ")){
                LOG.warn("The " + type + ",  " + name + ", has a trailing space in the name. " +
                		"The name is trimmed auto.");
            }
            
            Map<String, ExportEntity> names = map.get(type);
            if (names == null) {
                names = new HashMap<String, ExportEntity>();
                map.put(type, names);
            }
            
            name = name.trim();
            
            ExportEntity existing = names.get(name);
            if(existing != null){
                duplicated.add(existing);
                duplicated.add(e);
            }else{
                names.put(name, e);
            }
        }
        
        return duplicated;
    }
    
    /**
     *returns the a collection of entities for the user to selectively import.
     * @return entitiesFromFile, containing the entities from the 
     *  import file in ExportEntity format
     * @throws ImportException
     */
    public Collection<ExportEntity> getEntities() {
        return importData.getExportEntities();
    }
    
    HashMap<NameAndType, String> getRenameMapping(){
        return renameMapping;
    }

    /**
     * This method takes in a Collection of ExportEntities to
     * be imported, and processes them for conflicts.
     * @param selected, the entities from the file selected for import.
     * @return the IImportState for conflict resolution.
     * @throws ImportException 
     */
    //user is not allowed to unselect a component that is depended upon.
    public IImportState doImport(Collection<ExportEntity> selected) throws ImportException {
        //don't create the PolicyEditorClient on the method initializeImport, 
        //because the user may only want to view the file
        //and viewing the import file should not require PolicyEditorClient
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        client = (IPolicyEditorClient) compMgr.getComponent(PolicyEditorClient.COMP_INFO.getName());
        
        try {
            currentUserId = client.getLoggedInUser().getId();
        } catch (PolicyEditorException e) {
            throw new ImportException("unable to get logged in user id", e);
        }
        
        IComponentVerifier verifier = new ComponentVerifier(client);
        /*
         Collection<User> users = new ArrayList<User>();
         users = importData.getUsers();
         */
        //this will have to recurse down into users,
        //but this only has to recurse one layer deep because
        //  referred existing components should already
        //  have their dependencies straight, and referred
        //  components from the import file are checked anyway.        
        importState = verifier.verifyComponents(importState, selected, importData, userIdMapping);
                
        return importState;
    }

    /**
     * This method commits the import after all conflicts are handled.
     * It will carry out the user's instructions for conflicts,
     * parse the PQL with the rename mapping, and call saveEntities
     * on the entities that will be committed.
     * @throws ImportException 
     */
    public void commitImport() throws ImportException {
        solveAllImportConflicts(importState);
        solveAllUnconflicted(importState);

        //create domain objects for final results
        Collection<IHasId> committed = getCommitData(importState.getUnconflicted());
        
        //add all the "should be deleted policies" here to save together
        committed.addAll(importState.getShouldBeDeletedPolicies());
                
        if (importState.getLeafErrors() == ImportState.Shallow.FULL) {
            Collection<IHasId> errors = getCommitData(importState.getErrors());
            committed.addAll(errors);
        }

        try {
            LOG.debug("Committing entities.");
            client.saveEntities(committed);
        } catch (PolicyEditorException e) {
            throw new ImportException(e);
        }
    }

    private IHasId processEntity(String pql, String type) throws ImportException {
        IHasId obj = null;

        try {
            DomainObjectBuilder builder = new DomainObjectBuilder(pql);

            if (type.equals(EntityType.POLICY.getName())) {
                obj = builder.processPolicy();
            } else if (type.equals(EntityType.COMPONENT.getName())) {
                obj = builder.processSpec();
            } else {
                throw new ImportException("Unknown entity type: " + type);
            }
            
            if (obj == null) {
                throw new ImportException("Could not create policy/component. Badly formed PQL? : " + pql);
            }
        } catch (PQLException e) {
            throw new ImportException(e);
        }

        return obj;
    }

    /**
     * Generate domain objects from the export entities. If an object is replacing an existing object, it
     * will take the ACL of that object (i.e. changing an object doesn't change its ACL).
     */
    Collection<IHasId> getCommitData(Collection<ExportEntity> exportEntities) throws ImportException {
        Collection<IHasId> committed = new ArrayList<IHasId>();
        for (ExportEntity currentEntity : exportEntities) {

            // If there was a pre-existing object, grab its ACL
            IAccessPolicy accessPolicy = null;

            if (currentEntity.getOriginalPql() != null) {
                IHasId originalObject = processEntity(currentEntity.getOriginalPql(), currentEntity.getType());

                if (originalObject instanceof IAccessControlled) {
                    accessPolicy = ((IAccessControlled)originalObject).getAccessPolicy();
                }
            }

            IHasId obj = processEntity(currentEntity.getPql(), currentEntity.getType());

            if (accessPolicy != null && obj instanceof IAccessControlled) {
                ((IAccessControlled)obj).setAccessPolicy((AccessPolicy)accessPolicy);
            }
            committed.add(obj);

        }
        return committed;
    }

    void solveAllUnconflicted(IImportState importState) throws ImportException {
        final IAccessPolicy defaultAcl;
        try {
            defaultAcl = createACL();
        } catch (PolicyEditorException e) {
            throw new ImportException("unable to create acl", e);
        }
        
        Iterator<ExportEntity> unconfIter = importState.getUnconflicted().iterator();
        while (unconfIter.hasNext()) {
            final ExportEntity parseEnt = unconfIter.next();
            try {
                //build a dummy object with currently logged-in user
                //so we can steal its AcPQL
                //    TODO: possibly change the name of shallowly imported policies and components
                
                switch (importState.getLeafErrors()) {
                case SHALLOW:
                    shallowImport(parseEnt, defaultAcl);
                    break;
                case FULL:
                    if (nonShallowImport(parseEnt, defaultAcl)) {
                        unconfIter.remove();
                        importState.addError(parseEnt);
                    }
                    break;
                default:
                    throw new ImportException("Unknown ImportState. " + importState.getLeafErrors());
                }                
            } catch (PQLException e) {
                throw new ImportException(e);
            } catch (PolicyEditorException e) {
                throw new ImportException(e);
            }
        }
    }

    void solveAllImportConflicts(IImportState importState) throws ImportException {
        Collection<IImportConflict> conflicts = importState.getConflicts();
        ImportOverwriteManager importMgr = new ImportOverwriteManager();
        for (IImportConflict currentConflict : conflicts) {
            switch (importState.getKeepWhich()) {
                case ASK_USER:
                    //filing resolutions into local importState
                    switch (currentConflict.getResolution().getType()) {
                        case KEEP_NEW:
                            GetIDVersionFromPQL visitor = new GetIDVersionFromPQL();
                            try {
                                DomainObjectBuilder.processInternalPQL(currentConflict.versionFromServer(), visitor);
                            } catch (PQLException e) {
                                throw new ImportException(e);
                            }
                            //check if user has "modify" access for  all the import policies/ components
                            if(importMgr.objectCanBeModified(currentConflict.getName(), currentConflict.getType())){
                            	// Group all the import policies into "top" or "sub" policy group, 
                            	// we will do the policy comparison between import policies and original policy later. 
                                if(currentConflict.getType().equalsIgnoreCase(EntityType.POLICY.getName())){
                                	importMgr.groupingPolicies(currentConflict.getName());
                                }
                            }else{
                            	throw new ImportException("Import Overwrite cannot be completed. You do not have modify permission on object " + currentConflict.getName());
                            }
  
                            //when overwriting the database, we need to keep the database id and 
                            //possibly version too
                            importState.addUnconflicted(
                                    new ExportEntity(
                                            currentConflict.getName(), 
                                            currentConflict.getType(), 
                                            currentConflict.versionFromServer(), 
                                            currentConflict.versionFromFile(), 
                                            visitor.getId(), 
                                            visitor.getVersion()));
                            break;
                        case KEEP_OLD:
                            //do nothing, abandon this conflict's data.
                            break;
                        case RENAME_NEW:
                            //autogenerated name
                            String newName = currentConflict.getResolution().getNewName();
                            //store rename in mapping, store version from file into importState
                            //note that this generated name can be changed in the future, and the 
                            //mapping will still work properly.
                            getRenameMapping().put( new NameAndType(
                                    currentConflict.getName(), 
                                    currentConflict.getType()),
                                    newName);
                            importState.addUnconflicted( new ExportEntity(
                                    currentConflict.getName(), 
                                    currentConflict.getType(), 
                                    currentConflict.versionFromFile()));
                            break;
                        default:
                            throw new ImportException("Unknown conflict resolution " 
                                    + currentConflict.getResolution());
                    }
                    break;
                case ALWAYS_NEW:
                    importState.addUnconflicted(new ExportEntity(
                            currentConflict.getName(), 
                            currentConflict.getType(),
                            currentConflict.versionFromFile()));
                    break;
                case ALWAYS_OLD:
                    //do nothing, abandon this conflict's data.
                    break;
                default:
                    throw new ImportException("Unknown importState.getKeepWhich " + importState.getKeepWhich());
            }
        }
        // if the import policy needs to change the original structure, we need to check if 
    	// the original policy is in a deletable state, and if the import user has the "delete" 
    	// access for the original policy.

        importMgr.deleteUnreferencedSubPolicies(importState);

    }
    
    private class Bool{
        boolean value;
    }

    /**
     * @param unconfIter
     * @param parseEnt
     * @return true if contain errors
     * @throws PQLException
     * @throws PolicyEditorException
     */
    boolean nonShallowImport(final ExportEntity parseEnt, final IAccessPolicy defaultAcl)
            throws PQLException, PolicyEditorException {
        final DomainObjectFormatter dof = new DomainObjectFormatter();
        
        final Bool hasError = new Bool();
        hasError.value = false;
        //PQL massaging, changing leafObject IDs and AcPQL
        DomainObjectBuilder.processInternalPQL(parseEnt.getPql(), new IPQLVisitor() {
            public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                if (policy != null) {
                    ITarget target = policy.getTarget();
                    if (target != null) {
                        if (target.getActionPred() != null) {
                            renameComponent(descr, target.getActionPred());
                        }
                        if (target.getFromResourcePred() != null) {
                            renameComponent(descr, target.getFromResourcePred());
                        }
                        if (target.getToResourcePred() != null) {
                            renameComponent(descr, target.getToResourcePred());
                        }
                        if (target.getSubjectPred() != null) {
                            renameComponent(descr, target.getSubjectPred());
                        }
                    }
                    List<IPolicyReference> references = policy.getPolicyExceptions().getPolicies();
                    final List<IPolicyReference> renamedReferences = new ArrayList<IPolicyReference>();
                    for (IPolicyReference reference : references) {
                        renamedReferences.add(new PolicyReference(getNewPolicyReferenceName(reference.getReferencedName())));
                    }
                    policy.getPolicyExceptions().setPolicies(renamedReferences);
                }
                dof.formatPolicyDef(update(descr), policy);
            }

            public void visitFolder(DomainObjectDescriptor descr) {
                dof.formatFolder(update(descr));
            }

            public void renameComponent(DomainObjectDescriptor descr, IPredicate spec) {
                spec.accept(new DefaultPredicateVisitor() {
                    public void visit(IPredicateReference pred) {
                        renameReference(pred);
                    }
                }, IPredicateVisitor.POSTORDER);
            }

            public String getNewPolicyReferenceName(String name) {
                String renameTo = getRenameMapping().get(new NameAndType(name, EntityType.POLICY.getName()));

                return renameTo == null ? name : renameTo;
            }

            public void visitComponent(DomainObjectDescriptor descr, IPredicate spec) {
                //to parse/replace users, edit here.
                spec.accept(new DefaultPredicateVisitor() {
                    public void visit(IRelation pred) {
                        // This method does not pay attention to the operator
                        // because it looks only for ID-based attributes where
                        // the operator is either == or !=
                        addIds(pred.getLHS(), pred.getRHS(), pred);
                        addIds(pred.getRHS(), pred.getLHS(), pred);
                    }

                    // This method assumes that the attribute is on the left
                    // and the constant is on the right. We try calling this method
                    // both ways, so this approach works even when the attribute
                    // and the constant are swapped.
                    private void addIds(IExpression lhs, IExpression rhs, IRelation pred) {
                        if (rhs instanceof Constant) {
                            Object val = rhs.evaluate(null).getValue();
                            if (val instanceof Long && ID_BASED_ATTRIBUTES.contains(lhs)) {
                                if (userIdMapping.containsKey(val)) { 
                                    if ((userIdMapping.get(val)).longValue() == DICTIOANRY_ERROR_SIGN.longValue()) {
                                        hasError.value = true;
                                    }
                                    ((Relation) pred).setRHS(Constant
                                            .build(( userIdMapping.get(val)).longValue()));
                                }
                            }
                        }
                    }
                    
                    public void visit(IPredicateReference pred) {
                        renameReference(pred);
                    }
                }, IPredicateVisitor.POSTORDER);
                dof.formatDef(update(descr), spec);
            }

            public void visitResource(DomainObjectDescriptor descr, IPredicate spec) {
                dof.formatDef(update(descr), spec);
            }

            public void visitAction(DomainObjectDescriptor descr, IPredicate spec) {
                dof.formatDef(update(descr), spec);
            }

            public void visitLocation(DomainObjectDescriptor descr, Location location) {
                dof.formatLocation(update(descr), location);
            }

            public void visitAccessPolicy(DomainObjectDescriptor descr, IAccessPolicy accessPolicy) {
                dof.formatAccessPolicy(accessPolicy);
            }

            public DomainObjectDescriptor update(DomainObjectDescriptor descr) {
                //                             use database id and version when there is one. 
                Long id = parseEnt.getDatabaseId() != null ? parseEnt.getDatabaseId() : null;

                int version = (parseEnt.getDatabaseVersion() != 0) 
                        ? parseEnt.getDatabaseVersion() 
                        : descr.getVersion();

                NameAndType nat = new NameAndType(descr.getName(), descr.getType().getName());
                
                
                String name = getRenameMapping().containsKey(nat) 
                        ? (String) getRenameMapping().get(nat) 
                        : descr.getName();

                Date date = getRenameMapping().containsKey(nat)
                        ? UnmodifiableDate.forDate(new Date())
                        : UnmodifiableDate.START_OF_TIME;
                
                DevelopmentStatus status = retainStatus == IImportState.RetainStatus.RETAIN ? descr.getStatus() : DevelopmentStatus.DRAFT;

                return new DomainObjectDescriptor(
                                    id //change id to null so that saveEntities creates a new database entry  
                                ,   name 
                                ,   currentUserId 
                                ,   defaultAcl 
                                ,   descr.getType() 
                                ,   descr.getDescription()
                                ,   status
                                ,   version 
                                ,   date
                                ,   descr.getWhenCreated()
                                ,   date
                                ,   currentUserId
                                ,   null
                                ,   null
                                ,   descr.isHidden() 
                                ,   true
                                ,   false );
            }
        });
        parseEnt.setPql(dof.getPQL());
        dof.reset();
        
        return hasError.value;
    }

    
    void shallowImport(final ExportEntity parseEnt, final IAccessPolicy defaultAcl) throws PQLException,
            PolicyEditorException {
        final DomainObjectFormatter dof = new DomainObjectFormatter();

        DomainObjectBuilder.processInternalPQL(parseEnt.getPql(), new IPQLVisitor() {
            public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                if (policy != null) {
                    ITarget target = policy.getTarget();
                    if (target != null) {
                        if(target.getActionPred() != null ){
                            renameComponent(descr, target.getActionPred());
                        }
                        if(target.getFromResourcePred() != null ){
                            renameComponent(descr, target.getFromResourcePred());
                        }
                        if(target.getToResourcePred() != null ){
                            renameComponent(descr, target.getToResourcePred());
                        }
                        if(target.getSubjectPred() != null ){
                            renameComponent(descr, target.getSubjectPred());
                        }
//                        renameComponent(descr, target.getToSubjectPred());
                    }
                }
                dof.formatPolicyDef(update(descr), policy);
            }

            public void visitFolder(DomainObjectDescriptor descr) {
                dof.formatFolder(update(descr));
            }

            public void renameComponent(DomainObjectDescriptor descr, IPredicate spec) {
                spec.accept(new DefaultPredicateVisitor() {
                    public void visit(IPredicateReference pred) {
                        renameReference(pred);
                    }
                }, IPredicateVisitor.POSTORDER);
            }

            public void visitComponent(DomainObjectDescriptor descr, IPredicate spec) {
                //to parse/replace users, edit here.
                String componentName = descr.getName();
                LOG.debug("componentName = " + componentName);
                componentName = componentName.substring(0, componentName.indexOf('/'));
                if (componentName.equalsIgnoreCase(EntityType.USER.getName()) 
                        || componentName.equalsIgnoreCase(EntityType.HOST.getName())
                        || componentName.equalsIgnoreCase(EntityType.APPLICATION.getName())) {
                    
                    //replace all external reference to nothing.
                    spec.accept(new DefaultPredicateVisitor() {
                        @Override
                        public void visit(ICompositePredicate compositePredicate, boolean preorder) {
                            //there are external references 
                            List<IPredicate> removeRefs = new ArrayList<IPredicate>();
                            for (IPredicate predicate : compositePredicate.predicates()) {
                                if (predicate instanceof IRelation) {
                                    IExpression lhsExp = ((IRelation)predicate).getLHS();
                                    if(lhsExp instanceof SubjectAttribute 
                                            && !(lhsExp instanceof ExternalSubjectAttribute) ){
                                        removeRefs.add(predicate);
                                    }
                                }
                            }

                            for (IPredicate removeRef : removeRefs) {
                                LOG.debug("removeRef = " + removeRef);
                                ((CompositePredicate)compositePredicate).removePredicate(removeRef);
                            }

                            if (!removeRefs.isEmpty()) {
                                //no need to rebalance if nothing has been removed
                                PredicateHelpers.rebalanceDomainObject(((CompositePredicate)compositePredicate),
                                        compositePredicate.getOp());
                            }
                        }
                        
                        public void visit(IPredicateReference pred) {
                            renameReference(pred);
                        }
                    }, IPredicateVisitor.POSTORDER);
                    
                    
//                    //TODO remove all empty box
//                    spec.accept(new DefaultPredicateVisitor() {
//                        boolean firstTime = true;
//                        
//                        @Override
//                        public void visit(ICompositePredicate compositePredicate, boolean preorder) {
//                            if(firstTime){
//                                IPredicate firstPredicate = compositePredicate.predicateAt(0);
//                                if(firstPredicate instanceof CompositePredicate){
//                                    CompositePredicate firstCompositePredicate = (CompositePredicate)firstPredicate;
//                                    int predicateCount = firstCompositePredicate.predicateCount();
//                                    if(predicateCount >= 2){
//                                        //TODO
//                                    }
//                                }
//                                firstTime = false;
//                            }
//                        }
//                    }, IPredicateVisitor.PREORDER);
                }
                dof.formatDef(update(descr), spec);
            }

            public void visitResource(DomainObjectDescriptor descr, IPredicate spec) {
                dof.formatDef(update(descr), spec);
            }

            public void visitAction(DomainObjectDescriptor descr, IPredicate spec) {
                dof.formatDef(update(descr), spec);
            }

            public void visitLocation(DomainObjectDescriptor descr, Location location) {
                dof.formatLocation(update(descr), location);
            }

            public void visitAccessPolicy(DomainObjectDescriptor descr, IAccessPolicy accessPolicy) {
                dof.formatAccessPolicy(accessPolicy);
            }

            public DomainObjectDescriptor update(DomainObjectDescriptor descr) {
                // use database id and version when there is one. 
                Long id = (parseEnt.getDatabaseId() != null) 
                        ? parseEnt.getDatabaseId() 
                        : null;

                int version = (parseEnt.getDatabaseVersion() != 0) 
                        ? parseEnt.getDatabaseVersion() 
                        : descr.getVersion();

                NameAndType nat = new NameAndType(descr.getName(), descr.getType().getName());
                String name = getRenameMapping().containsKey(nat) 
                        ? (String) getRenameMapping().get(nat) 
                        : descr.getName();
                  
                
                DevelopmentStatus status = retainStatus == IImportState.RetainStatus.RETAIN ? descr.getStatus() : DevelopmentStatus.DRAFT;

                return new DomainObjectDescriptor(
                                    id 
                                ,   name 
                                ,   currentUserId
                                ,   defaultAcl 
                                ,   descr.getType()
                                ,   descr.getDescription() 
                                ,   status
                                ,   version
                                ,   UnmodifiableDate.forDate(new Date())
                                ,   descr.getWhenCreated() 
                                ,   UnmodifiableDate.forDate(new Date())
                                ,   currentUserId
                                ,   null
                                ,   null
                                ,   descr.isHidden() 
                                ,   true
                                ,   false );
                                
            }
        });
        parseEnt.setPql(dof.getPQL());
        dof.reset();
    }

    private IAccessPolicy createACL() throws PolicyEditorException {
    	Collection<? extends IHasId> list = client.getEntitiesForNamesAndType(
    			Collections.singleton(BLUE_JUNGLE_DUMMY_OBJECT), EntityType.POLICY, true);
    	return ((IAccessControlled) list.iterator().next()).getAccessPolicy();
    }
    
    void renameReference(IPredicateReference pred) {
        if (pred instanceof SpecReference) {
            SpecReference ref = ((SpecReference) pred);
            String name = ref.getReferencedName();
            String renameTo = getRenameMapping().get(new NameAndType(name, EntityType.COMPONENT.getName()));
            if (renameTo != null) {
                ref.setReferencedName(renameTo);
            }
        }
    }

    class NameAndType {
        String name;
        String type;

        //constructor
        NameAndType(String name, String type) {
            this.name = name;
            this.type = type;
        }

        //override for mapping
        public boolean equals(Object other) {
            if (other instanceof NameAndType) {
                NameAndType identifiedOther = (NameAndType)other;
                return (name.equals(identifiedOther.name)) && (type.equalsIgnoreCase(identifiedOther.type));
            } else {
                return false;
            }
        }

        //override for mapping
        public int hashCode() {
            return (name.hashCode() + type.hashCode());
        }
    }

    private class GetIDVersionFromPQL implements IPQLVisitor {
        private Long id;
        private int version;

        public final Long getId() {
            return id;
        }

        public final int getVersion() {
            return version;
        }

        public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
            setIdVersion(descriptor);
        }

        public void visitComponent(DomainObjectDescriptor descriptor, IPredicate spec) {
            setIdVersion(descriptor);
        }

        public void visitFolder(DomainObjectDescriptor descriptor) {
            setIdVersion(descriptor);
        }

        public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
            setIdVersion(descriptor);
        }

        public void visitPolicy(DomainObjectDescriptor descriptor, IDPolicy policy) {
            setIdVersion(descriptor);
        }

        private void setIdVersion(DomainObjectDescriptor descriptor) {
            if (id == null) {
                id = descriptor.getId();
                version = descriptor.getVersion();
            }
        }
    }
}
