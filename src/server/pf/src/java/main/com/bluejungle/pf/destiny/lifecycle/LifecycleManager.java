package com.bluejungle.pf.destiny.lifecycle;

/*
 * All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA. Ownership remains with Blue Jungle Inc.
 * All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/LifecycleManager.java#1 $
 */

import java.io.Serializable;
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
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.ObjectDeletedException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.destiny.server.shared.configuration.IDPSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.DefaultInterceptor;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.criteria.CaseInsensitiveLike;
import com.bluejungle.framework.datastore.hibernate.usertypes.DateToLongUserType;
import com.bluejungle.framework.datastore.hibernate.usertypes.EnumUserType;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IFunctionApplication;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionReference;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.SetUtils;
import com.bluejungle.framework.utils.StringUtils;
import com.bluejungle.framework.utils.TimeRelation;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.lib.DODDigest;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IHasPQL;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.common.AccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.SpecBase;
import com.bluejungle.pf.domain.destiny.common.SpecReference;
import com.bluejungle.pf.domain.destiny.common.ValidationVisitor;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyExceptions;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.misc.IAccessControlled;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;

/**
 * This class is a singleton. It manages all development and deployment entities using the hibernate as its back-end set of APIs.
 */
public class LifecycleManager implements IInitializable, IConfigurable, IManagerEnabled {
    
    private static final Log AUDIT_MODIFY_LOG = LogFactory.getLog("com.nextlabs.audit.LifecycleManager.modify");
    
    /**
     * This interface is used to make instances of new entities.
     */
    public interface NewEntityMaker {
        /**
         * Given a name and a type, returns a PQL string
         * representing an empty entity.
         * @param id the ID of the entity. This parameter is used when
         * a new entity replaces an old one.
         * @param name the name of the new entity.
         * @param type the type of the new entity.
         * @return PQL for an entity with the specified name and type.
         */
        String makeNewEntity( Long id, String name, EntityType type ) throws EntityManagementException;
    }

    /**
     * This <code>NewEntityMaker</code> creates empty entities
     * with no owner.
     */
    public static NewEntityMaker MAKE_EMPTY = new NewEntityMaker() {
        public String makeNewEntity( Long id, String name, EntityType type) throws EntityManagementException {
            StringBuffer res = new StringBuffer( 128 );
            res.append( "ID " );
            res.append( id );
            res.append( " STATUS NEW " );
            res.append( type.emptyPql( name ) );
            return res.toString();
        }
    };

    /**
     * This <code>NewEntityMaker</code> reports an error when called.
     */
    public static NewEntityMaker MUST_EXIST = new NewEntityMaker() {
        public String makeNewEntity( Long id, String name, EntityType type) throws EntityManagementException {
            throw new EntityManagementException( "Entity " + name + " of type " + type + " does not exist." );
        }
    };

    /**
     * This <code>NewEntityMaker</code> reports an error when called
     * to creare a new item, but returns a new entity when it detects the
     * undelete operation (i.e. ID is not null).
     */
    public static NewEntityMaker ONLY_UNDELETE = new NewEntityMaker() {
        public String makeNewEntity( Long id, String name, EntityType type) throws EntityManagementException {
            if ( id == null ) {
                throw new EntityManagementException( "Entity " + name + " of type " + type + " does not exist." );
            }
            StringBuffer res = new StringBuffer( 128 );
            res.append( "ID " );
            res.append( id );
            res.append( " STATUS NEW " );
            res.append( type.emptyPql( name ) );
            return res.toString();
        }
    };

    /**
     * This interface defines the contract for converting
     * <code>DevelopmentEntity</code> objects to
     * <code>DomainObjectDescriptor</code> objects. 
     */
    public interface DescriptorMaker {
        /**
         * Builds a <code>DomainObjectDescriptor</code> based on the given
         * <code>DevelopmentEntity</code>.
         * @param dev the <code>DevelopmentEntity</code>.
         * @return the <code>DomainObjectDescriptor</code>.
         */
        public DomainObjectDescriptor convert( DevelopmentEntity dev );
    }

    /**
     * This interface defines the contract for converting
     * <code>DevelopmentEntity</code> objects to <code>DODDigest</code> objects. 
     */
    public interface DigestMaker {
        public void computeObjectUsage(List<Long> entityIds);
        public DODDigest convert(DevelopmentEntity dev);
    }

    /**
     * A converter that takes fields of the <code>DevelopmentEntity</code>
     * and builds a <code>DomainObjectDescriptor</code> without further analysis.
     */
    public static DescriptorMaker DIRECT_CONVERTER = new DescriptorMaker() {
        /**
         * Builds a <code>DomainObjectDescriptor</code> using the fields
         * of the given <code>DevelopmentEntity</code>.
         * @param dev the <code>DevelopmentEntity</code>.
         * @return the <code>DomainObjectDescriptor</code>.
         */
        public DomainObjectDescriptor convert( DevelopmentEntity dev ) {
            if ( dev == null ) {
                return null;
            }
            DomainObjectDescriptor dod = new DomainObjectDescriptor(
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
            ,   false);
            return dod;
        }
    };

    public static final ComponentInfo<LifecycleManager> COMP_INFO = 
        new ComponentInfo<LifecycleManager>(
                LifecycleManager.class,
                LifestyleType.SINGLETON_TYPE);

    /**
     * Constructor for use by Component Manager
     */
    public LifecycleManager() {
    }

    /** Logging */
    private static final Log log = LogFactory.getLog(LifecycleManager.class.getName());

    /** Hibernate Session Factory. */
    private IHibernateRepository hds;

    /** Configuration * */
    private IConfiguration config;

    /** The component manager */
    private IComponentManager manager;

    /** Names of formal arguments for case-insensitive searching. */
    private static final String[] formalNames = { "?", "?", "?", "?", "?" };

    /** safety window for deployment (or cancellation) in the future.  If not
        far enough out in the future by at least `window' minutes, the action
        is denied.
    */
    private int window = 1;
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    public List<EntityType> convertStringsToEntityTypes(Collection<String> types) {
        // We could use a Set here, but we are dealing with small amounts of data
        // so the optimization probably isn't work it.
        List<EntityType> entityTypes = new ArrayList<EntityType>();

        for (String type : types) {
            EntityType et = EntityType.COMPONENT;
            if(type.equals("POLICY") || type.equals("FOLDER")) {
                et = EntityType.forName(type);
            }

            if (!entityTypes.contains(et)) {
                entityTypes.add(et);
            }
        }

        return entityTypes;
    }

    /**
     * Filter a list of development entities based on a requested list
     * of named types.  This is complicated by the fact that the named
     * type ACTION is actually an EntityType.COMPONENT.  POLICY, by
     * contrast, is EntityType.POLICY.  We can identify the action component
     * by the fact that its name starts "ACTION".  
     *
     * @param devEntities a <code>Collection</code> of <code>DevelopmentEntities</code>
     * @param types a <code>Collection</code> of names of types of entities
     * @param ignoreHidden skip entities marked "hidden"
     * @return a <code>Collection</code> of just those entities that match those types
     */
    private Collection<DevelopmentEntity> filterDevEntitiesByType(Collection<DevelopmentEntity> devEntities, Collection<String> types, boolean ignoreHidden) {
        List <DevelopmentEntity> filtered = new ArrayList<DevelopmentEntity>();
        boolean containsPolicy = types.contains("POLICY");
        boolean containsFolder = types.contains("FOLDER");

        for (DevelopmentEntity devEntity: devEntities) {
            boolean add = false;

            if (ignoreHidden && devEntity.isHidden()) {
                continue;
            }

            if ((devEntity.getType() == EntityType.POLICY && containsPolicy) ||
                (devEntity.getType() == EntityType.FOLDER && containsFolder)) {
                add = true;
            } else {
                String name = devEntity.getName();
                for (String type : types) {
                    if (name.startsWith(type)) {
                        add = true;
                        break;
                    }
                }
            }

            if (add) {
                filtered.add(devEntity);
            }
        }

        return filtered;
    }
 
    public List<DODDigest> getDigests(Collection<String> types, DigestMaker maker) throws EntityManagementException {
        if (types == null) {
            throw new NullPointerException("type");
        }

        if (types.size() == 0) {
            return new ArrayList<DODDigest>();
        }

        return convertDevEntitiesToDigests(filterDevEntitiesByType(getAllEntitiesOfType(convertStringsToEntityTypes(types)), types, true) , maker);
    }

    /**
     * Given a name template, finds a <code>Collection</code> of
     * entity names for the given type where entity name case-insensitively
     * matches the name template.
     *
     * @param type the desired entity type.
     * @param nameTemplate the name template (may have % to indicate wildcards.)
     * @param includeDeleted indicates whether or not the deleted items should be included.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code> objects
     * with names matching the given template.
     * @throws EntityManagementException on errors accessing the data store.
     */
    public Collection<DomainObjectDescriptor> getEntityDescriptors(EntityType type,
            String nameTemplate, boolean includeDeleted) throws EntityManagementException {
        return getEntityDescriptors( Arrays.asList( new EntityType[] {type} ), nameTemplate, DIRECT_CONVERTER, includeDeleted );
    }

    /**
     * Given a name template, finds a <code>Collection</code> of
     * entity names for the given types where entity name case-insensitively
     * matches the name template.
     *
     * @param types a <code>Collection</code> of the desired entity types.
     * @param nameTemplate the name template (may have % to indicate wildcards.)
     * @param maker Maker of <code>DomainObjectDescriptor</code> objects.
     * @param includeDeleted indicates whether or not the deleted items should be included.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code> objects
     * with names matching the given template.
     * @throws EntityManagementException on errors accessing the data store.
     */
    public Collection<DomainObjectDescriptor> getEntityDescriptors(Collection<EntityType> types,
            String nameTemplate, DescriptorMaker maker, boolean includeDeleted)
            throws EntityManagementException {
        if (types == null) {
            throw new NullPointerException("type");
        }
        if (nameTemplate == null) {
            throw new NullPointerException("nameTemplate");
        }
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            
            CaseInsensitiveLike cond = new CaseInsensitiveLike("<unused>", nameTemplate);

            Query q = hs.createQuery(
                "from DevelopmentEntity d "
            +   "where "
            +   cond.getCondition("d.name", formalNames, "lower")
            +   " and d.type in (:types) and d.status != :del "
            +   "order by d.name asc" );

            q.setParameterList( "types", types, UserTypeForEntityType.TYPE);
            q.setParameter("del", includeDeleted ?
                DevelopmentStatus.ILLEGAL
            :   DevelopmentStatus.DELETED
            ,   UserTypeForDevelopmentStatus.TYPE
            );
            String[] vals = cond.getBindStrings();
            for (int i = 0; i != vals.length; i++) {
                q.setString(i, vals[i]);
            }
            return convertDevEntitiesToDescriptors(q.list(), maker);
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }
    
    private void lazyCommitAndClose(Transaction tx, Session hs, Throwable orginalException)
            throws EntityManagementException {
        HibernateException ignorableException = null;
        //call commit first before close session
        if (tx != null) {
            try {
                tx.commit();
            } catch (HibernateException e) {
                //ignore if there is already an exception
                if (ignorableException == null && orginalException == null) {
                    ignorableException = e;
                }
            }
        }

        if (hs != null) {
            try {
                hds.closeCurrentSession();
            } catch (HibernateException e) {
                //ignore if there is already an exception
                if (ignorableException == null && orginalException == null) {
                    ignorableException = e;
                }
            }
        }

        if (ignorableException != null) {
            throw new EntityManagementException(ignorableException);
        }
    }

    /**
     * Given a name template, finds a <code>Collection</code> of entity IDs
     * for the given type where entity name case-insensitively matches
     * the name template.
     *
     * @param type the desired entity type.
     * @param nameTemplate the name template (may have % to indicate wildcards.)
     * @return a <code>Collection</code> of <code>Long</code> objects
     * with IDs of entities whose names match the given template.
     * @throws EntityManagementException on errors accessing the data store.
     */
    public Collection<Long> getEntityIDs( Collection<EntityType> types, String nameTemplate ) throws EntityManagementException {
        if (types == null) {
            throw new NullPointerException("types");
        }
        if (nameTemplate == null) {
            throw new NullPointerException("nameTemplate");
        }
        if ( types.isEmpty() ) {
            return Collections.emptyList();
        }
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            CaseInsensitiveLike cond = new CaseInsensitiveLike("<unused>", nameTemplate);

            Query q = hs.createQuery(
                "select d.id from DevelopmentEntity d "
            +   "where "
            +   cond.getCondition("d.name", formalNames, "lower")
            +   " and d.type in (:types) and d.status != :del" );

            q.setParameterList("types", types, UserTypeForEntityType.TYPE);
            q.setParameter("del", DevelopmentStatus.DELETED, UserTypeForDevelopmentStatus.TYPE);
            String[] vals = cond.getBindStrings();
            for (int i = 0; i != vals.length; i++) {
                q.setString(i, vals[i]);
            }
            return q.list();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Saves or updates a single entity.
     *
     * @param entity the entity to be saved or updated.
     * @param maker Provides an algorithm for making empty entities.
     * @throws EntityManagementException on errors accessing the datastore.
     */
    public void saveEntity( DevelopmentEntity entity, NewEntityMaker maker, Long modifier) throws EntityManagementException {
        if (entity == null) {
            return;
        }
        saveEntities(
                Arrays.asList(new DevelopmentEntity[] { entity })
              , maker
              , modifier
        );
    }
    
    private static final Pattern PQL_STATUS_PATTERN = Pattern.compile("ID \\d+ STATUS (\\p{Upper}+) ");
    
    private static String removeStatusInPql(String pql) {
        if (pql == null) {
            return null;
        }
        Matcher m = PQL_STATUS_PATTERN.matcher(pql);
        if (m.find()) {
            return pql.substring(0, m.start(1)) + pql.substring(m.end(1));
        }
        return pql;
    }
    
    private static boolean isSamePqlWithoutStatus(String pql1, String pql2) {
        return isSameString(removeStatusInPql(pql1), removeStatusInPql(pql2));
    }
    
    private static boolean isSameString(String s1, String s2){
        if (s1 != null) {
            return s1.equals(s2);
        } else {
            return s2 == null;
        }
    }
    
    private String appendLinePrefix(String string, String prefix) {
        assert string != null;
        prefix = prefix + "> ";
        return prefix + string.replace(LINE_SEPARATOR, LINE_SEPARATOR + prefix);
    }

    /**
     * Saves or updates a <code>Collection</code> of <code>DevelopmentEntity</code> objects in the persistent store.
     *
     * @param developmentEntities a <code>Collection</code> of <code>DevelopmentEntity</code> objects.
     * @param maker Provides an algorithm for making empty entities.
     * @throws EntityManagementException when the save operation fails.
     */
    public void saveEntities(Collection<DevelopmentEntity> developmentEntities, NewEntityMaker maker, Long modifier) throws EntityManagementException {
        final StringBuilder debuggingString = new StringBuilder();

        // Check the arguments
        if (developmentEntities == null) {
            throw new NullPointerException("developmentEntities");
        }
        if (developmentEntities.isEmpty()) {
            return;
        }
        Transaction tx = null;
        try {
            Session hs = hds.getCountedSession(ENTITY_SAVE_INTERCEPTOR);
            tx = hs.beginTransaction();
            Collection<DevelopmentEntity> clearOnRollback = new LinkedList<DevelopmentEntity>();
            try {
                // Replace references by name with references by IDs:
                final List<SpecReference> allReferences = new LinkedList<SpecReference>();
                final Map<String,IDescriptorFormatter>[] formatterByName = new Map[EntityType.getElementCount()];
                final Map<String,DevelopmentEntity>[] entityByName = new Map[EntityType.getElementCount()];
                final Set<String>[] refNames = new Set[EntityType.getElementCount()];
                final Set<String> knownNames = new HashSet<String>();

                for (int i = 0; i != entityByName.length; i++) {
                    entityByName[i] = new HashMap<String, DevelopmentEntity>();
                    formatterByName[i] = new HashMap<String, IDescriptorFormatter>();
                    refNames[i] = new HashSet<String>();
                }

                // Collect the names of referenced entities,
                // stores the references themselves for the later update,
                // and stores the domain objects that correspond to
                // all of the the processed entities.
                List<DevelopmentEntity> toSave = new LinkedList<DevelopmentEntity>(developmentEntities);

                // While renaming objects, the code below pays attention to
                // names of entities already in the database to avoid duplicates.
                try {
                    for (final DevelopmentEntity dev : developmentEntities) {
                        if (dev == null) {
                            throw new NullPointerException("Element of developmentEntities");
                        }
                        if (dev.getId() == null) {
                            clearOnRollback.add( dev );
                        }
                        class PreSaveVisitor extends DefaultPredicateVisitor implements IPQLVisitor {
                            public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                                if (policy == null) {
                                    return;
                                }
                                debuggingString.append("Policy name: ");
                                debuggingString.append(descr.getName());
                                debuggingString.append("\n");

                                // Policy exceptions live under their parent policies, not folders, so
                                // don't look for referenced folder name
                                if (!policy.hasAttribute(IDPolicy.EXCEPTION_ATTRIBUTE)) {
                                    refNames[EntityType.FOLDER.getType()].addAll(toPathElements( descr.getName() ) );
                                }

                                ITarget target = policy.getTarget();
                                if (target == null) {
                                    debuggingString.append("Target is null, exiting\n");
                                    return;
                                }
                                visitComponent(DomainObjectDescriptor.EMPTY, target.getActionPred());
                                visitComponent(DomainObjectDescriptor.EMPTY, target.getFromResourcePred());
                                visitComponent(DomainObjectDescriptor.EMPTY, target.getToResourcePred());
                                visitComponent(DomainObjectDescriptor.EMPTY, target.getSubjectPred());
                                visitComponent(DomainObjectDescriptor.EMPTY, target.getToSubjectPred());
                                if (descr.getName() != null) {
                                    debuggingString.append("Creating formatter\n");
                                    formatterByName[EntityType.POLICY.getType()].put(
                                        descr.getName(), formatterForPolicy( policy )
                                    );
                                }
                            }
                            public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                                if (pred == null) {
                                    return;
                                }
                                pred.accept( this, IPredicateVisitor.PREORDER );
                                if (descr.getName() != null) {
                                    formatterByName[EntityType.COMPONENT.getType()].put(
                                        descr.getName(), formatterForPred( pred )
                                    );
                                }
                            }
                            public void visitLocation(DomainObjectDescriptor descr, Location location) {
                                if (descr.getName() != null) {
                                    formatterByName[EntityType.LOCATION.getType()].put(
                                        descr.getName()
                                    ,   formatterForLocation( 
                                            new Location(
                                                descr.getId()
                                            ,   descr.getName()
                                            ,   location.getValue()
                                            )
                                        )
                                    );
                                }
                            }
                            public void visitFolder( DomainObjectDescriptor descr ) {
                                if (descr.getName() != null) {
                                    refNames[EntityType.FOLDER.getType()].addAll(toPathElements(descr.getName()));
                                    if (!dev.getName().equals(descr.getName())) {
                                        // Renaming policy folders is not supported
                                        throw new UnsupportedOperationException("Renaming policy folders is not supported.");
                                    }
                                    formatterByName[EntityType.FOLDER.getType()].put(
                                        descr.getName(), formatterForPolicyFolder()
                                    );
                                }
                            }
                            public void visit(IPredicateReference pred) {
                                if (pred instanceof SpecReference) {
                                    addReference((SpecReference)pred);
                                }
                            }

                            public void visit(IRelation pred) {
                                // either side may be an expression reference
                                IExpression lhs = pred.getLHS();
                                if (lhs instanceof SpecReference) {
                                    addReference((SpecReference) lhs);
                                }
                                IExpression rhs = pred.getRHS();
                                if (rhs instanceof SpecReference) {
                                    addReference((SpecReference) rhs);
                                }
                            }

                            private void addReference(SpecReference ref) {
                                if ( ref.isReferenceByName() ) {
                                    String theName = ref.getReferencedName();
                                    refNames[EntityType.COMPONENT.getType()].add( theName );
                                    allReferences.add( ref );
                                }
                            }

                            public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                                // Although a top-level pql object, this will never be encountered here.
                                assert(false);
                            }
                        };
                        DomainObjectBuilder.processInternalPQL( dev.getPql(), new PreSaveVisitor() );
                        knownNames.add( dev.getName() );
                    }
                } catch (PQLException pqlEx) {
                    // TODO: This should be a new kind of exception (PQL error)
                    throw new EntityManagementException(pqlEx);
                }
                mapByName( developmentEntities, entityByName );
                // Get the entities referenced by the original collection of entities.
                // Note that when an entity does not exist, a new one is created.
                for ( int i = 0 ; i != entityByName.length ; i++ ) {
                    Set<String> unknownNames = SetUtils.minus(refNames[i], knownNames);
                    Collection<DevelopmentEntity> unknownRefs = getEntitiesForNames(EntityType.forType(i), unknownNames, maker);
                    // Collect all newly created entities. In case of rollback, we'll need to clear out their IDs
                    for (DevelopmentEntity dev : unknownRefs) {
                        assert dev != null;
                        if (dev.getId() == null) {
                            clearOnRollback.add(dev);
                        }
                    }
                    mapByName( unknownRefs, entityByName );
                    final Map<String,IDescriptorFormatter> mapForThisType = formatterByName[i];
                    for (DevelopmentEntity dev : unknownRefs) {
                        if (dev.getStatus() != DevelopmentStatus.NEW) {
                            continue;
                        }
                        try {
                            DomainObjectBuilder.processInternalPQL( dev.getPql(), new DefaultPQLVisitor() {
                                public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                                    if (policy != null && descr.getName() != null) {
                                        mapForThisType.put(descr.getName(), formatterForPolicy(policy));
                                    }
                                }
                                public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                                    if (pred != null && descr.getName() != null) {
                                        mapForThisType.put(
                                            descr.getName(), formatterForPred(pred)
                                        );
                                    }
                                }
                                public void visitFolder(DomainObjectDescriptor descr) {
                                    if (descr.getName() != null) {
                                        mapForThisType.put(
                                            descr.getName(), formatterForPolicyFolder()
                                        );
                                    }
                                }
                                public void visitLocation(DomainObjectDescriptor descr, Location location) {
                                    // TODO: We can ignore it for now because locations cannot be referenced
                                }
                            });
                        } catch (PQLException e) {
                            // This will never happen because we are parsing
                            // the empty PQL
                            assert false : "Error parsing an empty PQL";
                        }
                    }
                    toSave.addAll( unknownRefs );
                }
                
                ListIterator<SpecReference> referenceIterator = allReferences.listIterator();
                while(referenceIterator.hasNext()){
                    SpecReference ref = referenceIterator.next();
//                for (SpecReference ref : allReferences) {
                    assert ref.isReferenceByName(); // We checked this before adding ref to the collection.
                    DevelopmentEntity dev = entityByName[EntityType.COMPONENT.getType()].get(ref.getReferencedName());
                    Long id = dev.getId();
                    if (id != null) {
                        ref.setReferencedID(id);
                        referenceIterator.remove();
                    }
                }
                
                DomainObjectFormatter dof = new DomainObjectFormatter();

                // Pre-save the entities to ensure that
                // all of them have a valid ID.
                ListIterator<DevelopmentEntity> iterator = toSave.listIterator();
                while(iterator.hasNext()){
                    DevelopmentEntity dev = iterator.next();
                    if (dev.getId() != null) {
                        //existing entry
                        
                        // update the modifier if the pql(except status) is changed or deleted
                        // update the submitter if it is DRAFT/EMPTY to APPROVED

                        DevelopmentEntity existing = (DevelopmentEntity)hs.get(DevelopmentEntity.class, dev.getId());
                        try {
                            String existingPQL = existing.getPql(); 
                            if (isSameString(existingPQL, dev.getPql())) {
                                //exactly same pql
                                if (log.isInfoEnabled()) {
                                    System.getProperty("line.separator");
                                    log.info("unnecessary save on " + dev.getId() + LINE_SEPARATOR 
                                            + appendLinePrefix(existingPQL, "_"));
                                }
                                iterator.remove();
                                continue;
                            }
                            
                            //set the previous modifier first
                            dev.setModifier(existing.getModifier());
                            dev.setLastModified(existing.getLastModified());
                            
                            
                            // this is going to be ugly
                            
                            // the toSave pql may contains a reference by name
                            // but the saved pql is referenced by id
                            // we can't compare the pql directly
                            // so we need to solve the id first if possible.
                            
                            IDescriptorFormatter formatter = (IDescriptorFormatter)formatterByName[dev.getType().getType()].get(dev.getName());
                            if ( formatter != null ) {
                                dof.reset();
                                formatter.format(
                                    dof, 
                                    new DomainObjectDescriptor(
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
                                    ,   false)
                                );

                                dev.setPql(dof.getPQL());
                            }
                            
                            if (!isSamePqlWithoutStatus(existingPQL, dev.getPql())
                                    || (dev.getStatus() == DevelopmentStatus.DELETED && existing.getStatus() != DevelopmentStatus.DELETED)
                            ) {
                                dev.setModifier(modifier);
                                dev.setLastModified(new Date());
                                
                                if (AUDIT_MODIFY_LOG.isInfoEnabled()) {
                                    log.info("A policy is modified. Name = \"" + dev.getName() 
                                            + "\" modifier = " + modifier 
                                            + LINE_SEPARATOR
                                            + appendLinePrefix(dev.getPql(), "O")
                                            + LINE_SEPARATOR
                                            + appendLinePrefix(dev.getPql(), "N")
                                    ); 
                                }
                            }
                            
                            if (dev.getStatus() == DevelopmentStatus.APPROVED 
                                    || dev.getStatus() == DevelopmentStatus.OBSOLETE
                                    || dev.getStatus() == DevelopmentStatus.DELETED) {
                                dev.setSubmitter(existing.getSubmitter());
                                dev.setSubmittedTime(existing.getSubmittedTime());
                            } else {
                                dev.setSubmitter(null);
                                dev.setSubmittedTime(null);
                            }
                            
                            // from DRAFT/EMPTY to APPROVED
                            // from ANYTHING but not OBSOLETE to OBSOLETE
                            if (  (existing.getStatus() == DevelopmentStatus.DRAFT 
                                     || existing.getStatus() == DevelopmentStatus.EMPTY)
                                   && dev.getStatus() == DevelopmentStatus.APPROVED
                               || (existing.getStatus() != DevelopmentStatus.OBSOLETE
                                   && dev.getStatus() == DevelopmentStatus.OBSOLETE)
                            ) {
                                dev.setSubmitter(modifier);
                                dev.setSubmittedTime(new Date());
                            }
                            
                        } finally {
                            hs.evict(existing);
                        }
                        
                    } else {
                        //a new entity should not have submitter
                        dev.setModifier(modifier);
                        dev.setLastModified(new Date());
                        
                        if (AUDIT_MODIFY_LOG.isInfoEnabled()) {
                            log.info("A new policy is created. Name = \"" + dev.getName() 
                                    + "\" modifier = " + modifier 
                                    + LINE_SEPARATOR
                                    + appendLinePrefix(dev.getPql(), "N")
                            ); 
                        }
                    }
                    dev.setStatus(updateStatusOnSave(dev.getStatus()));
                    hs.saveOrUpdate(dev);
                }

                // Go through the domain objects and replace
                // references by name with references by ID.
                for ( SpecReference ref : allReferences ) {
                    assert ref.isReferenceByName(); // We checked this before adding ref to the collection.
                    DevelopmentEntity dev = entityByName[EntityType.COMPONENT.getType()].get(ref.getReferencedName());
                    Long id = dev.getId();
                    // The code above ensures that all referenced entities have IDs
                    assert id != null;

                    debuggingString.append("Converting named reference ");
                    debuggingString.append(ref.getReferencedName());
                    debuggingString.append(" to id ");
                    debuggingString.append(id);
                    debuggingString.append("\n");

                    ref.setReferencedID(id);
                }

                // Go through all entities, replace their PQL with
                // that obtained by formatting the domain objects, and
                // save the entities with the resulting changes to PQL.
                // At the same time, replace the status of "NEW" with "EMPTY".
                
                for (DevelopmentEntity dev : toSave ) {
                    IDescriptorFormatter formatter = (IDescriptorFormatter)formatterByName[dev.getType().getType()].get(dev.getName());
                    if ( formatter != null ) {
                        dof.reset();
                        formatter.format(
                            dof, 
                            new DomainObjectDescriptor(
                                dev.getId()
                            ,   dev.getName()
                            ,   dev.getOwner()
                            ,   dev.getAccessPolicy()
                            ,   dev.getType()
                            ,   dev.getDescription()
                            ,   updateStatusOnSave( dev.getStatus() )
                            ,   dev.getVersion()
                            ,   dev.getLastUpdated()
                            ,   dev.getCreated()
                            ,   dev.getLastModified()
                            ,   dev.getModifier() 
                            ,   dev.getSubmittedTime()
                            ,   dev.getSubmitter()
                            ,   dev.isHidden()
                            ,   true
                            ,   false)
                        );

                        String pql = dof.getPQL();

                        // These statements should all be gone after the transformation
                        if (pql.contains("GROUP=")) {
                            log.error("Transformation of " + dev.getName() + " didn't remove named group references from PQL: " + pql);
                            log.error("Debug info: " + debuggingString.toString());
                        }
                        dev.setPql(pql);
                    } else {
                        if (dev.getPql().contains("GROUP=")) {
                            // We have an error in that sometimes references by name aren't changed to references by ID.  We don't know why.
                            // These logging messages are designed to tell us why
                            log.error("Entity " + dev.getName() + " didn't have a formatter and has named group references in PQL. " +
                            		"This is probably wrong.  PQL: " + dev.getPql());
                            log.error("Debug info: " + debuggingString.toString());
                        }
                    }
                    hs.saveOrUpdate(dev);
                }
                // See if the insertion created duplicates
                @SuppressWarnings("unchecked")
                Collection<String> duplicates = hs.createQuery(
                    "select d.name from DevelopmentEntity as d "
                +   "where d.status != :del "
                +   "group by d.name, d.type "
                +   "having count(d.id) > 1"
                ).setParameter( "del", DevelopmentStatus.DELETED, UserTypeForDevelopmentStatus.TYPE ).list();

                if ( !duplicates.isEmpty() ) {
                    throw new DuplicateEntityException( duplicates );
                }
                // Finally, commit the transaction
                tx.commit();
                
 
            } catch (Exception e) {
                log.error("Error when saving entities; " + e.getMessage());
                if ( tx != null ) {
                    tx.rollback();
                }
                for ( DevelopmentEntity dev : clearOnRollback ) {
                    log.error("Rolling back unknown ref " + dev.getName());
                    dev.setId( null );
                }
                throw e;
            } finally {
                hds.closeCurrentSession();
            }
        } catch ( Exception e ) {
            log.error("Error when saving entities; " + e.getMessage());
            log.error("Collected debug info: " + debuggingString.toString());
            if ( e instanceof EntityManagementException ) {
                throw (EntityManagementException)e;
            } else {
                throw new EntityManagementException( e );
            }
        }
    }

    /**
     * Replaces the NEW status with EMPTY on save. 
     * @param status the status to check.
     * @return the status after the replacement.
     */
    private static DevelopmentStatus updateStatusOnSave( DevelopmentStatus status ) {
        return status == DevelopmentStatus.NEW ? DevelopmentStatus.EMPTY : status;
    }

    /**
     * Get a <code>Collection</code> of <code>DomainObjectDescriptor</code> objects
     * for a collection of names. If an entity never existed, an exception is thrown.
     *
     * @param type the type of the entity to retrieve.
     * @param nameCollection a <code>Collection</code> of <code>String</code>
     * objects represetning names of entities to retrieve.
     * @param maker Maker of <code>DomainObjectDescriptor</code> objects.
     * @return a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects with names matching the names from the specified <code>Collection</code>.
     * @throws EntityManagementException on errors accessing the data store,
     * or when a name represents an entity that never existed.
     */
    public Collection<DomainObjectDescriptor> getDescriptorsForNames(
        EntityType type
    ,   Collection<String> nameCollection
    ,   DescriptorMaker maker
    ) throws EntityManagementException {
        return  convertDevEntitiesToDescriptors(
            getEntitiesForNames(
                type
            ,   nameCollection
            ,   ONLY_UNDELETE
            )
        ,   maker
        );
    }

    /**
     * Finds an existing development entity for the given name, or creates a new one.
     *
     * @param type the type of the development entity.
     * @param name the name of the development entity.
     * @param maker Provides an algorithm for making empty entities.
     * @param when the asof date for the query.
     * @return an existing or a newly created development entity.
     * @throws EntityManagementException on errors accessing the data store.
     */
    public DevelopmentEntity getEntityForName(EntityType type, String name, NewEntityMaker maker) throws EntityManagementException {
        if (name == null) {
            throw new NullPointerException("name");
        }

        Collection<DevelopmentEntity> ent = getEntitiesForNames( type, Arrays.asList(new String[] { name }), maker );

        assert ent != null && ent.size() == 1; // getEntitiesForNames returns exactly as many results as there are items in the list

        return ent.iterator().next();
    }

    /**
     * Get a <code>Collection</code> of <code>DevelopmentEntity</code> objects
     * for a collection of names. If an entity does not exist, a new one
     * is created for each missing name.
     *
     * @param type the type of the entity to retrieve.
     * @param nameCollection a <code>Collection</code> of <code>String</code> objects
     * representing names of entities to retrieve.
     * @param maker Provides an algorithm for making empty entities.
     * @return a <code>Collection</code> of <code>DevelopmentEntity</code>
     * objects with names matching the names from the specified <code>Collection</code>.
     * @throws EntityManagementException on errors accessing the data store.
     */
    public Collection<DevelopmentEntity> getEntitiesForNames(
        EntityType type
    ,   Collection<String> nameCollection
    ,   NewEntityMaker maker
    ) throws EntityManagementException {
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (nameCollection == null) {
            throw new NullPointerException("nameCollection");
        }
        if ( maker == null ) {
            maker = MAKE_EMPTY;
        }

        // Deal with empty collections of names without going to the database
        // (this also avoids syntax error in HQL when the IN list is empty.
        if (nameCollection.isEmpty()) {
            return Collections.emptyList();
        }
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            
            // Ensure that the names are unique.
            Set<String> nameSet = new HashSet<String>(nameCollection);
            String[] names = nameSet.toArray(new String[nameSet.size()]);
            // Sort names in ascending order, in the same way the HQL query sorts its results
            Arrays.sort( names, String.CASE_INSENSITIVE_ORDER );

            Query q = hs.createQuery(
                "from DevelopmentEntity as dev "
            +   "where"
            +   "  dev.name in (:namelist) and"
            +   "  dev.type = :type and"
            +   "  dev.status != :del "
            +   "order by"
            +   "  upper(dev.name) asc");

            q.setParameter( "type", type, UserTypeForEntityType.TYPE );
            q.setParameter( "del", DevelopmentStatus.DELETED, UserTypeForDevelopmentStatus.TYPE );
            q.setParameterList( "namelist", names );
            List<DevelopmentEntity> res = new LinkedList<DevelopmentEntity>();
            Iterator<DevelopmentEntity> rs = q.iterate();
            int i = 0;
            // This loop goes through names and results in a coordinated fashion.
            // Since both collections are sorted, there is no nested loop
            // (the loop inside does not count because it advances the same
            // loop variable as the outer loop).
            while (rs.hasNext() || i < names.length) {
                DevelopmentEntity dev = null;
                if (rs.hasNext()) {
                    dev = rs.next();
                }
                while (i != names.length && (dev == null || !dev.getName().equalsIgnoreCase(names[i]))) {
                    res.add( new DevelopmentEntity( maker.makeNewEntity( null, names[i++], type ), type ) );
                }
                if (i == names.length && dev != null) {
                    // This means that either
                    // (1) the results that came from hibernate were not sorted in ascending order, or
                    // (2) there were more results than the query has asked for, for example because
                    //     of rows with duplicate names in the database.
                    throw new EntityManagementException("Persistence layer returned unexpected results in getEntitiesForNames.");
                }
                if (dev != null) {
                    res.add(dev);
                }
                i++;
            }
            // This algorithm should return one result per unique name in the request:
            assert res.size() == names.length;
            return res;
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } catch ( PQLException pe ) {
            originalException = pe;
            throw new EntityManagementException( pe );
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Retrieves entities matching IDs from the given <code>Collection</code> of
     * <code>DomainObjectDescriptor</code> objects.
     *
     * @param ids a <code>Collection</code> of <code>DomainObjectDescriptor</code>
     * objects.
     * @return a <code>Collection</code> of <code>DevelopmentEntity</code>
     * objects for the given set of <code>DomainObjectDescriptor</code> objects.
     * @throws EntityManagementException on errors accessing the data store.
     */
    public Collection<DevelopmentEntity> getEntitiesForDescriptors(Collection<DomainObjectDescriptor> descriptors) throws EntityManagementException {
        if ( descriptors == null ) {
            throw new NullPointerException("descriptors");
        }
        if ( descriptors.isEmpty() ) {
            return Collections.emptyList();
        }
        Long[] ids = new Long[descriptors.size()];
        int i = 0;
        for ( DomainObjectDescriptor dod : descriptors ) {
            ids[i++] = dod.getId();
        }
        return getEntitiesForIDs(Arrays.asList( ids ));
    }

    /**
     * Retrieves entities matching IDs from the given list.
     *
     * @param ids a <code>Collection</code> of <code>Long</code> objects
     * representing IDs of the desired <code>DevelopmentEntity</code> objects.
     * @return a <code>Collection</code> of <code>DevelopmentEntity</code>
     * objects for the given set of IDs.
     * @throws EntityManagementException on errors accessing the data store.
     */
    public Collection<DevelopmentEntity> getEntitiesForIDs(Collection<Long> ids) throws EntityManagementException {
        if (ids == null) {
            throw new NullPointerException("ids");
        }
        // Deal with empty collections of IDs without going to the database
        // (this also avoids syntax error in HQL when the IN list is empty.
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
    	SplitHQL sp = createIDListQuery(ids, "dev.id");
    	try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Query query = hs.createQuery(
            		"from DevelopmentEntity as dev where ("+ sp.getQuery() +") order by dev.id asc"     	
            );
            for(IdListsWithName idlist : sp.getList()) {
            	query.setParameterList(idlist.getName(), idlist.getList());
            }
            return query.list();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Retrieves entities matching IDs from the given list,
     * and returns their descriptors.
     *
     * @param ids a <code>Collection</code> of <code>Long</code> objects
     * representing IDs of the desired <code>DevelopmentEntity</code> objects.
     * @param maker Maker of <code>DomainObjectDescriptor</code> objects.
     * @return a <code>List</code> of <code>DomainObjectDescriptor</code>
     * objects for the given set of IDs.
     * @throws EntityManagementException on errors accessing the data store.
     */
    public List<DomainObjectDescriptor> getEntityDescriptorsForIDs(Collection<Long> ids, DescriptorMaker maker) throws EntityManagementException {
        return convertDevEntitiesToDescriptors( getEntitiesForIDs( ids), maker );
    }

    /**
     * Retrieves entities matching IDs from the given list,
     * and returns their digests.
     *
     * @param ids a <code>Collection</code> of <code>Long</code> objects
     * representing IDs of the desired <code>DevelopmentEntity</code> objects.
     * @param maker Maker of <code>DODDigest</code> objects.
     * @return a <code>List</code> of <code>DODDigest</code>
     * objects for the given set of IDs.
     * @throws EntityManagementException on errors accessing the data store.
     */
    public List<DODDigest> getEntityDigestsForIDs(Collection<Long> ids, DigestMaker maker) throws EntityManagementException {
        return convertDevEntitiesToDigests( getEntitiesForIDs( ids), maker );
    }

    /**
     * Given an <code>EntityType</code> object returns all entities of that type.
     * @param type an <code>EntityType</code> specifying the desired type of entities to return.
     * @return a <code>Collection</code> of all <code>DevelopmentEntity</code> objects of the specified type.
     * @throws EntityManagementException when the operation fails.
     */
    public Collection<DevelopmentEntity> getAllEntitiesOfType( EntityType type ) throws EntityManagementException {
        return getAllEntitiesOfType(Collections.singletonList(type));
    }

    /**
     * Given a <code>Collection</code> of <code>EntityType</code> objects,
     * returns all entities of the specified types.
     * @param types a <code>Collection</code> of <code>EntityType</code>
     * objects specifying the desired types of entities to return.
     * @return a <code>Collection</code> of all <code>DevelopmentEntity</code>
     * objects of the specified types.
     * @throws EntityManagementException when the operation fails.
     */
    public Collection<DevelopmentEntity> getAllEntitiesOfType( Collection<EntityType> types ) throws EntityManagementException {
        if (types == null) {
            throw new NullPointerException("types");
        }
        if ( types.isEmpty() ) {
            return Collections.emptyList();
        }
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Query q = hs.createQuery(
                "from DevelopmentEntity d where d.type in (:types) and d.status != :del"
            );
            q.setParameterList( "types", types, UserTypeForEntityType.TYPE );
            q.setParameter( "del", DevelopmentStatus.DELETED, UserTypeForDevelopmentStatus.TYPE );
            return q.list();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Given a <code>DevelopmentStatus</code> object,
     * returns all entities with the specified status.
     * @param status the <code>DevelopmentStatus</code>.
     * @param maker Maker of <code>DomainObjectDescriptor</code> objects.
     * @return a <code>Collection</code> of all <code>DevelopmentEntity</code>
     * objects with the specified status.
     * @throws EntityManagementException when the operation fails.
     */
    public Collection<DomainObjectDescriptor> getAllEntitiesForStatus(DevelopmentStatus status,
            DescriptorMaker maker) throws EntityManagementException {
        if (status == null) {
            throw new NullPointerException("status");
        }
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Query q = hs.createQuery(
                "from DevelopmentEntity d where d.status = :status"
            );
            q.setParameter( "status", status, UserTypeForDevelopmentStatus.TYPE );
            return convertDevEntitiesToDescriptors( q.list(), maker );
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }
    
    /**
     * Returns the deployed entity with the given name, for the specified asOf date.
     *
     * @param type desired entity type.
     * @param name name of the deployment entity at the time of deployment.
     * @param asOf the asOf date for the query.
     * @param deploymentType the type of deployment (production or test).
     * @return the deployed entity with the given name, for the specified
     * asOf date, or null if there is no deployed record matching the name
     * and the asOf date.
     * @throws EntityManagementException when the operation fails.
     */
    public DeploymentEntity getDeployedEntityForName(
        EntityType type
    ,   String name
    ,   Date asOf
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        Collection<DeploymentEntity> res = getDeployedEntitiesForNames(type, Arrays.asList(new String[] { name }), asOf, deploymentType);
        assert res != null; // getDeployedEntitiesForNames must never return null results.
        assert res.size() <= 1; // There can be no more than one deployed entity at a time.
        if (res.size() == 1) {
            return res.iterator().next();
        } else {
            return null; // The entity does not have an active deployment on the specified day
        }
    }

    /**
     * Returns a <code>Collection</code> of <code>DeploymentEntity</code>
     * objects with the given names, for the specified asOf date.
     *
     * @param type desired entity type.
     * @param nameCollection names of the deployment entities at the time of deployment.
     * @param asOf the asOf date for the query.
     * @param deploymentType the type of deployment (production or test).
     * @return a <code>Collection</code> of <code>DeploymentEntity</code>
     * objects with the given names, for the specified asOf date.
     * The collection may have fewer elements than names in cases when
     * not all entities have deployment records at the specified asOf time.
     * @throws EntityManagementException when the operation fails.
     */
    public Collection<DeploymentEntity> getDeployedEntitiesForNames(
            EntityType type
    , Collection<String> nameCollection
    , Date asOf
    , DeploymentType deploymentType
    ) throws EntityManagementException {
        // Check arguments for nulls
        if (type == null) {
            throw new NullPointerException("type");
        }
        if (nameCollection == null) {
            throw new NullPointerException("nameCollection");
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        if (deploymentType == null) {
            throw new NullPointerException("deploymentType");
        }
        if (nameCollection.isEmpty()) {
            return Collections.emptyList();
        }
        // Build a session and execute the request
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Query q = hs.createQuery(
                "from DeploymentEntity d "
            +   "where d.name in (:names)"
            +   "  and d.overrideCount=0"
            +   "  and d.timeRelation.activeTo > :asOf"
            +   "  and d.timeRelation.activeFrom <= :asOf"
            +   "  and d.pql is not null"
            +   "  and d.developmentEntity.type = :type"
            +   "  and d.deploymentRecord.deploymentType = :depType"
            );
            q.setParameterList( "names", nameCollection );
            q.setParameter( "asOf", asOf, DateToLongUserType.TYPE );
            q.setParameter( "type", type, UserTypeForEntityType.TYPE );
            q.setParameter( "depType", deploymentType, UserTypeForDeploymentType.TYPE );
            return q.list();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }
    
    private class NotAnApplicationException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
    
    public Collection<DevelopmentEntity> getAllApplicatinResourceComponents () throws EntityManagementException {
        // Build a session and execute the request
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Query q = hs.createQuery(
                "from DevelopmentEntity d"
            +   " where d.hidden = 'Y'"
            +   " and d.pqlStr is not null"
            +   " and d.type = :type"
            +   " and d.status = :status"
            );
            q.setParameter( "type", EntityType.COMPONENT, UserTypeForEntityType.TYPE );
            q.setParameter( "status", DevelopmentStatus.APPROVED, UserTypeForDevelopmentStatus.TYPE );
            
            Collection<DevelopmentEntity> entities = q.list();
            
            Collection<DevelopmentEntity> filtered = new LinkedList<DevelopmentEntity>();
            
            for (DevelopmentEntity e : entities) {
                if (e.getName().contains("//")) {
                    continue;
                }
                try {
                    // the pql must match very specific format in order to 
                    // be a application resource component
                    DomainObjectBuilder.processInternalPQL(e.getPql(), new IPQLVisitor() {
                        @Override
                        public void visitPolicy(
                                DomainObjectDescriptor descriptor,
                                IDPolicy policy) {
                            throw new NotAnApplicationException();
                        }

                        @Override
                        public void visitFolder(
                                DomainObjectDescriptor descriptor) {
                            throw new NotAnApplicationException();
                        }

                        @Override
                        public void visitComponent(
                                DomainObjectDescriptor descriptor,
                                IPredicate spec) {
                            spec.accept(new IPredicateVisitor(){

                                @Override
                                public void visit(ICompositePredicate pred,
                                        boolean preorder) {
                                    throw new NotAnApplicationException();
                                }

                                @Override
                                public void visit(IPredicateReference pred) {
                                    throw new NotAnApplicationException();
                                }

                                @Override
                                public void visit(IRelation pred) {
                                    if( pred.getLHS() != SubjectAttribute.APP_NAME 
                                        || pred.getOp() != RelationOp.EQUALS
                                        || !(pred.getRHS() instanceof Constant)){
                                        throw new NotAnApplicationException();
                                    }
                                }

                                @Override
                                public void visit(IPredicate pred) {
                                    throw new NotAnApplicationException();
                                }
                                
                            }, IPredicateVisitor.PREORDER);
                        }

                        @Override
                        public void visitAccessPolicy(
                                DomainObjectDescriptor descriptor,
                                IAccessPolicy accessPolicy) {
                            throw new NotAnApplicationException();
                        }

                        @Override
                        public void visitLocation(
                                DomainObjectDescriptor descriptor,
                                Location location) {
                            throw new NotAnApplicationException();
                        }
                    });
                    
                    filtered.add(e);
                } catch (NotAnApplicationException e1) {
                    //ignore this exception
                }
            }
            return filtered;
        } catch (PQLException e) {
            originalException = e;
            throw new EntityManagementException(e);
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Returns the date of the last deployment activity
     * prior to (asOf), inclusive.
     * @param asOf the latest time to check for deployment, inclusive.
     * @param deploymentType the desired deployment type.
     * @return  the date of the last deployment activity
     * prior to (asOf), inclusive.
     * @throws EntityManagementException when the operation cannot complete.
     */
    public Date getLatestDeploymentTime( Date asOf, DeploymentType deploymentType) throws EntityManagementException {
        return getLatestDeploymentTime( UnmodifiableDate.START_OF_TIME, asOf, deploymentType );
    }

    /**
     * Returns the date of the last deployment activity in the interval
     * from (since), exclusive, to (asOf), inclusive.
     * @param since the lower end of the interval, exclusive.
     * @param asOf the higher end of the interval, inclusive.
     * @param deploymentType the desired deployment type.
     * @return the date of the last deployment activity in the interval
     * from (since), exclusive, to (asOf), inclusive.
     * @throws EntityManagementException when the operation cannot complete.
     */
    public Date getLatestDeploymentTime( Date since, Date asOf, DeploymentType deploymentType ) throws EntityManagementException {
        // Check arguments for nulls
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        if (deploymentType == null) {
            throw new NullPointerException("deploymentType");
        }
        if ( since == null ) {
            since = UnmodifiableDate.START_OF_TIME;
        }
        // Check for degenerate intervals.
        if ( asOf.before(since) ) {
            return UnmodifiableDate.START_OF_TIME;
        }
        
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        // Run the aggregated query
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            String queryString =
                "select max(d.timeRelation.activeFrom) "
            +   "from DeploymentEntity d "
            +   "where d.deploymentRecord.deploymentType = :dt"
            +   "  and d.timeRelation.activeFrom <= :asof";
            if ( since != UnmodifiableDate.START_OF_TIME ) {
                queryString += "  and d.timeRelation.activeFrom > :since";
            }
            Query q = hs.createQuery( queryString );
            q.setParameter( "dt", deploymentType, UserTypeForDeploymentType.TYPE );
            q.setParameter( "asof", asOf, DateToLongUserType.TYPE );
            if ( since != UnmodifiableDate.START_OF_TIME ) {
                q.setParameter( "since", since, DateToLongUserType.TYPE );
            }
            Collection tmp = q.list();
            if ( tmp != null && !tmp.isEmpty() ) {
                Object val = tmp.iterator().next();
                if ( val != null && val instanceof Date ) {
                    return UnmodifiableDate.forDate( (Date)val );
                } else {
                    return UnmodifiableDate.START_OF_TIME;
                }
            } else {
                return UnmodifiableDate.START_OF_TIME;
            }
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    public List<DeploymentEntity> getAllDeployedEntities(
        EntityType type
    ,   Date asOf
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        return getAllDeployedEntities( Arrays.asList( new EntityType[] { type } ), UnmodifiableDate.START_OF_TIME, asOf, deploymentType );
    }

    public List<DeploymentEntity> getAllDeployedEntities(
        EntityType type
    ,   Date since
    ,   Date asOf
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        return getAllDeployedEntities( Arrays.asList( new EntityType[] { type } ), since, asOf, deploymentType );
    }

    public List<DeploymentEntity> getAllDeployedEntities(
        Collection<EntityType> types
    ,   Date asOf
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        return getAllDeployedEntities( types, UnmodifiableDate.START_OF_TIME, asOf, deploymentType );
    }

    public List<DeploymentEntity> getAllDeployedEntities(
        Date asOf
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        return getAllDeployedEntities( UnmodifiableDate.START_OF_TIME, asOf, deploymentType );
    }

    public List<DeploymentEntity> getAllDeployedEntities(
        Date since
    ,   Date asOf
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        return getAllDeployedEntities( Arrays.asList(new EntityType[0]), since, asOf, deploymentType );
    }
    
    public List<DeploymentEntity> getAllDeployedEntities(
            Collection<EntityType> types
    ,   Date since
    ,   Date asOf
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        // Check arguments for nulls
        if (since == null) {
            throw new NullPointerException("since");
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        if (deploymentType == null) {
            throw new NullPointerException("deploymentType");
        }
        // Build a session and execute the request
        
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            String qs = "from DeploymentEntity d "
            +   "where d.overrideCount=0"
            +   "  and d.pql is not null"
            +   "  and d.timeRelation.activeTo > :asOf"
            +   "  and d.timeRelation.activeFrom <= :asOf"
            +   "  and d.timeRelation.activeFrom > :since"
            +   "  and d.deploymentRecord.deploymentType = :depType";

            if ( types != null && !types.isEmpty() ) {
                qs += "  and d.developmentEntity.type in (:types)";
            }

            qs += " order by d.developmentEntity.id asc";

            Query q = hs.createQuery( qs );
            q.setParameter( "asOf", asOf, DateToLongUserType.TYPE );
            q.setParameter( "since", since, DateToLongUserType.TYPE );
            q.setParameter( "depType", deploymentType, UserTypeForDeploymentType.TYPE );
            if ( types != null && !types.isEmpty() ) {
                q.setParameterList( "types", types, UserTypeForEntityType.TYPE );
            }

            return q.list();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    public int getAllDeployedEntitiesCount(
        EntityType type
    ,   Date asOf
    ,   DeploymentType deploymentType 
    ,   boolean includeHidden
    ) throws EntityManagementException {
        return getAllDeployedEntitiesCount( Arrays.asList( new EntityType[] { type } ), 
                UnmodifiableDate.START_OF_TIME, asOf, deploymentType, includeHidden );
    }

    public int getAllDeployedEntitiesCount(
        Date asOf
    ,   DeploymentType deploymentType
    ,   boolean includeHidden
    ) throws EntityManagementException {
        return getAllDeployedEntitiesCount( UnmodifiableDate.START_OF_TIME, asOf, deploymentType, includeHidden );
    }

    public int getAllDeployedEntitiesCount(
        Date since
    ,   Date asOf 
    ,   DeploymentType deploymentType
    ,   boolean includeHidden
    ) throws EntityManagementException {
        return getAllDeployedEntitiesCount( Arrays.asList(new EntityType[0]), since, asOf, deploymentType, includeHidden );
    }
    
    public int getAllDeployedEntitiesCount(
        Collection<EntityType> types 
    ,   Date since
    ,   Date asOf 
    ,   DeploymentType deploymentType 
    ,   boolean includeHidden
    ) throws EntityManagementException {
        // Check arguments for nulls
        if (since == null) {
            throw new NullPointerException("since");
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        if (deploymentType == null) {
            throw new NullPointerException("deploymentType");
        }
        // Build a session and execute the request
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            String qs = "select count(*) from DeploymentEntity d "
            +   "where d.overrideCount=0"
            +   "  and d.pql is not null"
            +   "  and d.timeRelation.activeTo > :asOf"
            +   "  and d.timeRelation.activeFrom <= :asOf"
            +   "  and d.timeRelation.activeFrom > :since"
            +   "  and d.deploymentRecord.deploymentType = :depType";
            
            if ( false == includeHidden ){
                qs += "  and d.hidden = 'N'";
            }

            if ( types != null && !types.isEmpty() ) {
                qs += "  and d.developmentEntity.type in (:types)";
            }

            Query q = hs.createQuery( qs );
            q.setParameter( "asOf", asOf, DateToLongUserType.TYPE );
            q.setParameter( "since", since, DateToLongUserType.TYPE );
            q.setParameter( "depType", deploymentType, UserTypeForDeploymentType.TYPE );
            if ( types != null && !types.isEmpty() ) {
                q.setParameterList( "types", types, UserTypeForEntityType.TYPE );
            }

            Integer res = (Integer) q.uniqueResult();
            return res.intValue();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }
    
    /**
     * Returns the deployed entity for the specific development entity and an asOf date.
     *
     * @param developmentEntity The development entity for which to get the deployment entity.
     * @param asOf the asOf date for the query.
     * @param deploymentType the type of deployment (production or test).
     * @return the deployed entity with the given name, for the specified asOf
     * date, or null if there is no deployed record matching the name and
     * the asOf date.
     * @throws EntityManagementException when the operation fails.
     */
    public DeploymentEntity getDeployedEntityForEntity(
        DevelopmentEntity developmentEntity
    ,   Date asOf 
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        Collection<DeploymentEntity> res = getDeployedEntitiesForEntities(
                Arrays.asList(new DevelopmentEntity[] { developmentEntity }), asOf, deploymentType);
        assert res != null; // getDeployedEntitiesForEntities must never return null results.
        assert res.size() <= 1; // There can be no more than one deployed entity at a time
        if (res.size() == 1) {
            return res.iterator().next();
        } else {
            return null; // The entity does not have an active deployment on the specified day
        }
    }

    /**
     * Returns a <code>List</code> of <code>DeploymentEntity</code> objects of a <code>Collection</code> of
     * given development entity Ids
     *
     * @param entityIds A <code>Collection</code> of <code>Long</code> ids for development entities
     * @return a <code>List</code> of <code>DeploymentEntity</code> objects of a <code>Collection</code> of
     * given Ids
     * @throws EntityManagementException when the operation fails
     */
    public List<DeploymentEntity> getDeploymentEntitiesForIDs(
        Collection<Long> entityIds 
    ,   Date asOf
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        if (entityIds == null) {
            throw new NullPointerException("entityIds");
        }
        if (entityIds.isEmpty()) {
            return Collections.emptyList();
        }
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Query q = hs.createQuery( "from DeploymentEntity d "
            +   "where d.overrideCount=0"
            +   "  and d.pql is not null"
            +   "  and d.timeRelation.activeTo > :asOf"
            +   "  and d.timeRelation.activeFrom <= :asOf"
            +   "  and d.developmentEntity.id in (:ids)"
            +   "  and d.deploymentRecord.deploymentType = :depType"
            +   "  order by d.developmentEntity.id asc"
            );
            q.setParameter( "asOf", asOf, DateToLongUserType.TYPE );
            q.setParameterList( "ids", entityIds );
            q.setParameter( "depType", deploymentType, UserTypeForDeploymentType.TYPE );
            return q.list();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Returns a <code>Collection</code> of <code>DeploymentEntity</code>
     * objects for a <code>Collection</code> of given
     * <code>DevelopmentEntity</code> objects, for the specified asOf date.
     *
     * @param type desired entity type.
     * @param developmentEntities A <code>Collection</code>
     * of <code>DevelopmentEntity</code> objects.
     * @param asOf the asOf date for the query.
     * @param deploymentType the type of deployment (production or test).
     * @return a <code>List</code> of <code>DeploymentEntity</code>
     * objects for the specified development entities and the specified
     * asOf date. The collection may have fewer elements than names in cases
     * when not all entities have deployment records at the specified asOf time.
     * @throws EntityManagementException when the operation fails.
     */
    public List<DeploymentEntity> getDeployedEntitiesForEntities(
        Collection<DevelopmentEntity> developmentEntities 
    ,   Date asOf
    ,   DeploymentType deploymentType
    ) throws EntityManagementException {
        if (developmentEntities == null) {
            throw new NullPointerException("developmentEntities");
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        if (deploymentType == null) {
            throw new NullPointerException("deploymentType");
        }
        if (developmentEntities.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<Long>(developmentEntities.size());
        for (DevelopmentEntity dev : developmentEntities ) {
            ids.add(dev.getId());
        }
        return getDeploymentEntitiesForIDs(ids, asOf, deploymentType);
    }

    /**
     * Deploys a <code>Collection</code> of approved <code>DevelopmentEntity</code> objects.
     *
     * @param developmentEntities a <code>Collection</code> of <code>DevelopmentEntity</code> objects to deploy.
     * @param asOf the asOf date for deployment (must be at least one minute in the future).
     * @param deploymentType the type of deployment (production or test).
     * @param when the current date.
     * @param hidden indicates if the deployment should be marked hidden.
     * @return the <code>DeploymentRecord</code> for the action (null if there was no action).
     * @throws EntityManagementException when the operation fails
     * because one of the following has happened:
     * (1) not all entites are eligible for deployment,
     * (2) a data store exception prevents the operation from completion, or
     * (3) the asOf date is not sufficiently in the future.
     */
    DeploymentRecord deployEntities(
        Collection<DevelopmentEntity> developmentEntities 
    ,   Date asOf
    ,   DeploymentType deploymentType
    ,   Date when
    ,   boolean hidden
    ,   Long deployer
    ) throws EntityManagementException {
        return deployOrUndeploy(developmentEntities, asOf, deploymentType, when, hidden, deployer, new DeploymentEntityMaker() {
            public DeploymentEntity makeDeploymentEntity(DevelopmentEntity dev, DeploymentRecord rec, Date asOf) {
                return dev.makeDeploymentEntity(rec, asOf);
            }
            public DeploymentActionType getActivityType() {
                return DeploymentActionType.DEPLOY;
            }
            public boolean checkStatus(DevelopmentStatus status) {
                return status == DevelopmentStatus.APPROVED;
            }
        });
    }

    /**
     * Deploys a <code>Collection</code> of approved <code>DevelopmentEntity</code> objects.
     *
     * @param developmentEntities a <code>Collection</code> of <code>DevelopmentEntity</code> objects to deploy.
     * @param asOf the asOf date for deployment (must be at least one minute in the future).
     * @param deploymentType the type of deployment (production or test).
     * @param hidden indicates if the deployment should be marked hidden.
     * @return the <code>DeploymentRecord</code> for the action (null if there was no action).
     * @throws EntityManagementException when the operation fails
     * because one of the following has happened:
     * (1) not all entites are eligible for deployment,
     * (2) a data store exception prevents the operation from completion, or
     * (3) the asOf date is not sufficiently in the future.
     */
    public DeploymentRecord deployEntities(
        Collection<DevelopmentEntity> developmentEntities 
    ,   Date asOf
    ,   DeploymentType deploymentType
    ,   boolean hidden
    ,   Long deployer
    ) throws EntityManagementException {
        return deployEntities(developmentEntities, asOf, deploymentType, new Date(), hidden, deployer);
    }

    /**
     * Undeploys entites.
     *
     * @param developmentEntities a <code>Collection</code>
     * of <code>DeploymentEntity</code> objects to be undeploed.
     * @param asOf the date at which to end the deployment.
     * @param deploymentType the type of deployment (production or test).
     * @param hidden indicates if the deployment should be marked hidden.
     * @return the <code>DeploymentRecord</code> for the action (null if there was no action).
     * @throws EntityManagementException when the undeployment of one
     * or more entities cannot be scheduled because, for example, it is too late.
     */
    public DeploymentRecord undeployEntities(
        Collection<DevelopmentEntity> developmentEntities 
    ,   Date asOf
    ,   DeploymentType deploymentType
    ,   boolean hidden
    ,   Long deployer
    ) throws EntityManagementException {
        return undeployEntities(developmentEntities, asOf, deploymentType, new Date(), hidden, deployer);
    }

    /**
     * This method performs the work undeployEntities, but it takes
     * an extra argument for the current time. This extra argument is useful
     * for unit testing, which is why the method is package-private.
     *
     * @param developmentEntities A <code>Collection</code>
     * of <code>DeploymentEntity</code> objects to be undeployed.
     * @param asOf the date at which to end the deployment.
     * @param deploymentType the type of deployment (production or test).
     * @param now the current time, or an alternative time for testing.
     * @param hidden indicates if the deployment should be marked hidden.
     * @return the <code>DeploymentRecord</code> for the action (null if there was no action).
     * @throws EntityManagementException when the undeployment of one
     * or more entities cannot be scheduled because, for example, it is too late.
     */
    DeploymentRecord undeployEntities(
        Collection<DevelopmentEntity> developmentEntities 
    ,   Date asOf
    ,   DeploymentType deploymentType 
    ,   Date now
    ,   boolean hidden
    ,   Long deployer
    ) throws EntityManagementException {
        return deployOrUndeploy(developmentEntities, asOf, deploymentType, now, hidden, deployer, new DeploymentEntityMaker() {

            public DeploymentEntity makeDeploymentEntity(DevelopmentEntity dev, DeploymentRecord rec, Date asOf) {
                return dev.makeTombstone(rec, asOf);
            }

            public DeploymentActionType getActivityType() {
                return DeploymentActionType.UNDEPLOY;
            }

            public boolean checkStatus(DevelopmentStatus status) {
                return status == DevelopmentStatus.OBSOLETE;
            }
        });
    }

    /**
     * Deploys or undeploys a <code>Collection</code>
     * of <code>DevelopmentEntity</code> objects.
     * This is a package-private method for unit testing.
     *
     * @param developmentEntities a <code>Collection</code>
     * of <code>DevelopmentEntity</code> objects to deploy or undeploy.
     * @param asOf the asOf date for deployment (must be at least one minute in the future).
     * @param deploymentType the type of deployment (production or test).
     * @param now the current time (used for time-independent testing).
     * @param hidden indicates if the deployment should be marked hidden.
     * @param maker an object that makes deployment entities from development entities.
     * @return the <code>DeploymentRecord</code> for the action (null if there was no action).
     * @throws EntityManagementException when the operation fails because
     * one of the following has happened:
     * (1) not all entites are eligible for deployment,
     * (2) a data store exception prevents the operation from completion, or
     * (3) the asOf date is not sufficiently in the future.
     */
    private DeploymentRecord deployOrUndeploy(
        Collection<DevelopmentEntity> developmentEntities
    ,   Date asOf
    ,   DeploymentType deploymentType
    ,   Date now
    ,   boolean hidden
    ,   Long deployer
    ,   DeploymentEntityMaker maker
    ) throws EntityManagementException {
        // Check arguments for nulls
        if (developmentEntities == null) {
            throw new NullPointerException("developmentEntities");
        }
        // Do not bother checking anything else if there is nothing to deploy
        if (developmentEntities.isEmpty()) {
            return null;
        }
        if (asOf == null) {
            throw new NullPointerException("asOf");
        }
        if (deploymentType == null) {
            throw new NullPointerException("deploymentType");
        }
        saveEntities( developmentEntities, MUST_EXIST, deployer);
        // Verify that
        // (1) all entities are of the right type, and
        // (2) that they all are in the correct state.
        for ( DevelopmentEntity dev : developmentEntities ) {
            if ( !maker.checkStatus( dev.getStatus() ) ) {
                String message = "Entity '" + dev.getName() + "' must be ";
                if ( maker.getActivityType() == DeploymentActionType.DEPLOY ) {
                    message +=  "approved for deployment.";
                } else {
                    message +=  "obsolete for undeployment.";
                }
                throw new EntityManagementException( message );
            }
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MINUTE, window);
        Date earliestTimeToDeploy = cal.getTime();
        if ( earliestTimeToDeploy.after(asOf) ) {
            asOf = earliestTimeToDeploy;
        }

        try {
            Session hs = hds.getCountedSession(ENTITY_SAVE_INTERCEPTOR);
            Transaction tx = hs.beginTransaction();
            try {
                saveEntities( developmentEntities, MUST_EXIST, deployer);
                // Prepare a list of IDs and a corresponding formal IN list string:
                List<Long> ids = new LinkedList<Long>();
                for ( DevelopmentEntity dev : developmentEntities ) {
                    // All entities either came from hibernate or have been saved.
                    // Therefore, all IDs must be set to a valid value of type Long.
                    assert dev.getId() != null;
                    ids.add(dev.getId());
                }

                // Check for missing and circular references.
                // If we try to deploy entities, all their dependencies
                // must also be deployed as of the time of deployment.
                // TODO:

                // If we try to undeploy entities, there should be no
                // deployed references to them as of the time of undeployment.
                // TODO:

                // Create a record of deployment
                DeploymentRecord deploymentRecord = new DeploymentRecord( maker.getActivityType(), deploymentType, asOf, now, hidden, deployer);
                hs.save(deploymentRecord);

                // Get deployment entities for the given development entities
                // that are active at the asOf time (tombstones are OK here)
                Query q = hs.createQuery(
                    "from DeploymentEntity dp "
                +   "where dp.developmentEntity.id in (:idlist) and"
                +   "      dp.timeRelation.activeFrom <= :asOf and"
                +   "      :asOf < dp.timeRelation.activeTo and"
                +   "      dp.deploymentRecord.deploymentType = :dt")
                .setParameterList("idlist", ids)
                .setParameter("dt", deploymentType, UserTypeForDeploymentType.TYPE)
                .setParameter("asOf", asOf, DateToLongUserType.TYPE );

                Collection<DeploymentEntity> active = q.list();
                List<DeploymentEntity> copies = new LinkedList<DeploymentEntity>();
                // Close the asOf dates for all active or shadowed entities,
                // update their records in the RDBMS, and prepare a list of
                // copies of temporary "shadowed records for cases when the deployment
                // is cancelled later. These records will ultimately be purged unless they are
                // "resurrected" by a subsequent deployment cancellation.
                for (DeploymentEntity dep : active) {
                    TimeRelation tr = dep.getTimeRelation();
                    assert tr != null; // All objects that come from hibernate must have this field set
                    dep.setTimeRelation(new TimeRelation(tr.getActiveFrom(), asOf));
                    DeploymentEntity copy = dep.copy();
                    copy.setTimeRelation(new TimeRelation(asOf, tr.getActiveTo()));
                    copy.addOverride();
                    copy.setDeploymentRecord(deploymentRecord);
                    copies.add(copy);
                    hs.update(dep);
                }

                // Get deployment entities for the given development entities
                // that become active on after the asOf date
                q = hs.createQuery(
                    "from DeploymentEntity dp "
                +   "where dp.developmentEntity.id in (:idlist) and "
                +   "      dp.timeRelation.activeFrom >= :asOf and "
                +   "      dp.deploymentRecord.deploymentType = :dt")
                .setParameterList("idlist", ids)
                .setParameter("dt", deploymentType, UserTypeForDeploymentType.TYPE)
                .setParameter( "asOf", asOf, DateToLongUserType.TYPE );
                Collection<DeploymentEntity> overriden = q.list();
                // Set override count for all deployment entities
                for (DeploymentEntity dep : overriden) {
                    dep.addOverride();
                    hs.update(dep);
                }

                // Save the prepared copies after updating the shadowing counts.
                for (DeploymentEntity copy : copies) {
                    hs.save(copy);
                }

                // Purge the overriden records from the database
                purge(hs, now);

                // For each development entity to be deployed, make a
                // deployment entity, with its timeRelation set appropriately;
                // Save the resulting deployment entities
                
                for (DevelopmentEntity dev : developmentEntities) {
                    DeploymentEntity dep = maker.makeDeploymentEntity(dev, deploymentRecord, asOf);
                    dep.setDeploymentRecord(deploymentRecord);
                    hs.save(dep);
                }

                // Finally, we are ready to commit the transaction
                tx.commit();
                return deploymentRecord;
            } catch (HibernateException he) {
                if ( tx != null ) {
                    tx.rollback();
                }
                throw he;
            } finally {
                hds.closeCurrentSession();
            }
        } catch (HibernateException he) {
            throw new EntityManagementException(he);
        }
    }

    /**
     * Cancels deployment of entites scheduled for deployment in the future.
     *
     * @param record the <code>DeploymentRecord</code>to be unscheduled for deployment.
     * @throws EntityManagementException when the deployment of one
     * or more entities cannot be cancelled because, for example, it is too late.
     */
    public void cancelDeploymentOfEntities( DeploymentRecord record ) throws EntityManagementException {
        cancelDeploymentOfEntities(record, new Date());
    }

    /**
     * This method performs the work cancelDeploymentOfEntities, but it takes
     * an extra argument for the current time. This extra argument is useful
     * for unit testing, which is why the method is package-private.
     *
     * @param record the <code>DeploymentRecord</code>to be unscheduled for deployment.
     * @param now the current time, or an alternative time for testing.
     * @throws EntityManagementException when the deployment of one
     * or more entities cannot be unscheduled because, for example, it is too late.
     */
    void cancelDeploymentOfEntities(DeploymentRecord record, Date now) throws EntityManagementException {
        if (record == null) {
            throw new NullPointerException("record");
        }
        if (record.getWhenCancelled() != null) {
            throw new IllegalArgumentException("Attempting to cancel a deployment that has already been cancelled.");
        }
        Collection<DeploymentEntity> deployedEntities = this.getDeployedEntitiesForDeploymentRecord( record );
        assert deployedEntities != null;
        if (deployedEntities.isEmpty()) {
            return;
        }
        // Go through dates of deployment of all
        // deployed entities, and find the earliest one:
        Date earliestCancelRequest = earliestActivationTime(deployedEntities);
        // Add one minute to the current time
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.MINUTE, window);
        Date latestLegalTimeToCancel = cal.getTime();
        // Check if it is still OK to deploy
        if ( latestLegalTimeToCancel.after( earliestCancelRequest ) ) {
            throw new EntityManagementException(
                "Cannot cancel deployment because some or all entities have deployment time "
            +   "in the past or within " + 60 * window + " seconds of the current time.");
        }
        Collection ids = deploymentEntitiesToIds(deployedEntities);
        // Proceed with cancelling the deployment
        try {
            Session hs = hds.getCountedSession(ENTITY_SAVE_INTERCEPTOR);
            Transaction tx = null;
            try {
                tx = hs.beginTransaction();
                // Get all entities that may have been shadowed by
                // one of the entities from the list
                Query q = hs.createQuery(
                    "select a "
                +   "from DeploymentEntity as a, DeploymentEntity as b "
                +   "where b.id in (:idlist) and"
                +   "      a.developmentEntity.id=b.developmentEntity.id and"
                +   "      a.deploymentRecord.deploymentType = b.deploymentRecord.deploymentType and"
                +   "      a.overrideCount > 0 and"
                +   "      a.originalVersion < b.originalVersion and"
                +   "      b.timeRelation.activeFrom <= a.timeRelation.activeFrom");

                q.setParameterList("idlist", ids);
                // Go through these entities and decrement their override counts
                List<DeploymentEntity> shadowed = q.list();
                for ( DeploymentEntity dep : shadowed ) {
                    dep.removeOverride();
                    hs.update(dep);
                }

                // Now delete the cancelled entities
                for ( DeploymentEntity dep : deployedEntities )
                    hs.delete(dep);

                // Purge the overriden records from the database
                purge(hs, now);

                try {
                    // Update the record to reflect cancellation
                    record = (DeploymentRecord)hs.load( DeploymentRecord.class, record.getId() );
                    record.cancel( now );
                    hs.update( record );
                } catch (ObjectDeletedException ode ) {
                    // The record may have been "shadowed"
                    // at the time users attempted to cancel.
                    // This is OK.
                }

                // Finally, we are ready to commit the transaction
                tx.commit();
            } catch (HibernateException he) {
                if ( tx != null ) {
                    tx.rollback();
                }
                throw he;
            } finally {
                hds.closeCurrentSession();
            }
        } catch (HibernateException he) {
            throw new EntityManagementException(he);
        }
    }

    /**
     * Returns the deployment history between <code>from</code> and <code>to</code>.
     *
     * @param from a <code>Date</code> or null. If null, it is replaced with the beginning of time constant.
     * @param to a <code>Date</code> or null. If null, it is replaced with the end of time constant.
     * @return a <code>Collection</code> of <code>DeploymentRecord</code> objects representing
     * deployment history between <code>from</code> and <code>to</code>.
     * @throws EntityManagementException when the operation fails.
     */
    public Collection<DeploymentRecord> getDeploymentHistory(Date from, Date to) throws EntityManagementException {
        if (from == null) {
            from = UnmodifiableDate.START_OF_TIME;
        }
        if (to == null) {
            to = UnmodifiableDate.END_OF_TIME;
        }
        
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Collection<Object[]> records = hs
                .createQuery(
                    "select dr, count(dp)"
                +   " from DeploymentEntity dp"
                +   " right outer join dp.deploymentRecord dr"
                +   " where dr.asOf between :from and :to"
                +      " and (dp.overrideCount is null or dp.overrideCount = 0)"
                +   " group by dr,"
                +   " dr.deploymentActionType, dr.deploymentType, dr.asOf, dr.whenRequested, dr.whenCancelled, dr.hidden, dr.deployer"
                +   " order by dr.asOf" )
                .setParameter( "from", from, DateToLongUserType.TYPE )
                .setParameter( "to", to, DateToLongUserType.TYPE )
                .list();
            List<DeploymentRecord> res = new ArrayList<DeploymentRecord>( records.size() );
            for ( Object[] vals : records ) {
                DeploymentRecord rec = (DeploymentRecord)vals[0];
                assert vals[1] instanceof Integer;
                rec.setNumberOfDeployedEntities( ((Integer)vals[1]).intValue() );
                res.add((DeploymentRecord)vals[0]);
            }
            return res;
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Given a development entity and a deployment type, returns a collection
     * of dates on which changes to the entity took place.
     *
     * @param developmentEntity the entity for which to get the deployment history.
     * @param deploymentType the type of deployment (production or test).
     * @return a <code>Collection</code> of <code>TimeRelation</code>
     * objects representing intervals on which changes to the entity took place.
     * @throws EntityManagementException when the operation cannot complete
     * because of an error in the datastore.
     */
    public Collection<DeploymentHistory> getDeploymentHistoryForEntityId(
            Long developmentEntityId,
            DeploymentType deploymentType) 
            throws EntityManagementException {
        if (developmentEntityId == null) {
            // Newly created entities do not have deployment history
            return Collections.emptyList();
        }
        if (deploymentType == null) {
            throw new NullPointerException("deploymentType");
        }
        
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Query q = hs.createQuery(
                "select dp.timeRelation, dp.lastModified, dp.modifier, dp.submittedTime" 
            +   ", dp.submitter, dp.deploymentRecord.whenRequested, dp.deploymentRecord.deployer "
            +   "from DeploymentEntity dp "
            +   "where dp.developmentEntity.id = :id and"
            +   "  dp.deploymentRecord.deploymentType = :dt and"
            +   "  dp.overrideCount = 0 and"
            +   "  dp.deploymentRecord.whenCancelled is null "
            +   "order by dp.timeRelation.activeFrom");

            assert developmentEntityId != null; // We've checked this above
            q.setLong("id", developmentEntityId.longValue());
            q.setParameter("dt", deploymentType, UserTypeForDeploymentType.TYPE);
            List<Object[]> qr = q.list();
            // qr should return an empty list if no result.
            if (qr == null) {
                // should not be here
                return null;
            }
            Collection<DeploymentHistory> histories = new ArrayList<DeploymentHistory>(qr.size());
            
            for(Object[] qro : qr) {
                histories.add(new DeploymentHistory(
                        (TimeRelation)qro[0] // TimeRelation timeRelation
                      , (Date)qro[1] // Date lastModified
                      , (Long)qro[2] // Long modifier
                      , (Date)qro[3] // Date submitTime
                      , (Long)qro[4] // Long submitter
                      , (Date)qro[5] // Date deployTime
                      , (Long)qro[6] // Long deployer
                ));
            }
            return histories;
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Returns a <code>Collection</code> of <code>DeploymentEntity</code>
     * objects for a given deployment record.
     * @param record the deployment record.
     * @return a <code>Collection</code> of <code>DeploymentEntity</code>
     * objects for a given deployment record.
     * @throws EntityManagementException EntityManagementException when
     * the operation cannot complete because of an error in the datastore.
     */
    public Collection<DeploymentEntity> getDeployedEntitiesForDeploymentRecord( DeploymentRecord record ) throws EntityManagementException {
        if ( record == null ) {
            throw new NullPointerException("record");
        }
        if ( record.getId() == null ) {
            throw new IllegalArgumentException("record must have an ID");
        }
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            return hs
                .createQuery( "from DeploymentEntity d where d.deploymentRecord.id = :id and d.overrideCount = 0" )
                .setParameter( "id", record.getId() )
                .list();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Returns the number of entities in approved or obsolete state that
     * are either not deployed at the asOf, or are newer than the version
     * deployed at the asOf date.  We check for obsolete objects because,
     * unintuitively, these objects must be deployed to finalize the
     * deactivation
     * @param asOf the asOf date for the query.
     * @param types a <code>Collection</code> of entity types to check.
     * @return the number of entities of any of the given types
     * that are either in approved state or obsolete state that are either
     * not deployed at the asOf date, or are newer than the version deployed
     * at the asOf date.
     *
     * @throws EntityManagementException EntityManagementException when
     * the operation cannot complete because of an error in the datastore.
     */
    public int getCountOfEntitiesReadyForDeployment( Date asOf, Collection<EntityType> types ) throws EntityManagementException {
        List<DevelopmentStatus> status = Arrays.asList(new DevelopmentStatus[] {
            DevelopmentStatus.APPROVED
        ,   DevelopmentStatus.OBSOLETE
        } );
        
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Number res = (Number)hs.createQuery(
                   "select count(*) "
                +  "from DevelopmentEntity dev "
                +  "left join dev.allDeploymentEntities dep "
                +  "where "
                +  "dev.status in (:status) "
                +  "and dev.type in (:types) "
                +  "and dev.hidden = :notHidden "
                +  "and (dep is null or ("
                +  "        dev.version != dep.originalVersion"
                +  "    and dep.overrideCount = 0"
                +  "    and dep.timeRelation.activeFrom <= :asof"
                +  "    and dep.timeRelation.activeTo > :asof ) )" )
                     .setParameter( "asof", asOf, DateToLongUserType.TYPE )
                     .setParameterList( "status", status, UserTypeForDevelopmentStatus.TYPE )
                     .setParameter( "notHidden", Boolean.FALSE, Hibernate.YES_NO )
                     .setParameterList( "types", types, UserTypeForEntityType.TYPE)
                .uniqueResult();

            return res != null ? res.intValue() : 0;
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    private interface IDResolutionQuery {
        Query makeQuery(Session hs, Collection<Long> ids) throws HibernateException ;
    }
    
    private class IdListsWithName {
    	private String name;
    	private List<Long> ids;
    	public IdListsWithName(String listName, List<Long> idsList) {
    		name = listName;
    		ids = idsList;
    	}
        public String getName ( ) { 
            return name; 
        }    
        public void setName (String listName) { 
            name = listName;            
        } 
        public List<Long> getList ( ) { 
            return ids; 
        } 
        public void setList (List<Long> idsList) { 
        	ids = idsList; 
        } 
    }
    
    private class SplitHQL {
    	private String hqlQuery;
    	private List<IdListsWithName> subLists;
    	public SplitHQL(String idQuery) {
    		hqlQuery = idQuery;
    		subLists = new ArrayList<IdListsWithName> ();
    	}
        public String getQuery ( ) { 
            return hqlQuery; 
        }     
        public void setQuery (String idQuery) { 
        	hqlQuery = idQuery;            
        } 
        public List<IdListsWithName> getList ( ) { 
            return subLists; 
        } 
        public void setList (List<IdListsWithName> list) { 
        	subLists = list; 
        } 
    }

    /*
     * Some databases have a limit to the number of expressions that can appear in a list. Oracle is limited to 1000.
     * To work around this limitation we will check to see if we have more than 1000 expressions and, if so, split them
     * up into multiple clauses. We also build the appropriate HQL string to handle this.
     *
     * Note: it is possible in the future that Hibernate will handle this for us, but that is not the case now
     */

	private SplitHQL createIDListQuery(Collection<Long> ids, String hqlIdName) {
		//ORACLE-01795: maximum number of expressions in a list is 1000
		final int DATALIMIT = 1000;
		StringBuilder IDListQuery = new StringBuilder();
		List <Long> originalList = new ArrayList <Long> (ids);		
		int startIdx = 0, smallListsIdx = 0;
		final List <ArrayList<Long>> smallLists = new ArrayList<ArrayList<Long>>();
		while (startIdx <= originalList.size() -1) {
			int endIdx = startIdx + DATALIMIT -1;
			if (endIdx > originalList.size() -1) {
				endIdx = originalList.size() -1;
			}
			endIdx = endIdx +1;
			smallLists.add( new ArrayList<Long> (originalList.subList(startIdx, endIdx)));
			if(smallLists.size()!=1){
				IDListQuery.append(" OR ");
			}
			IDListQuery.append(hqlIdName+ " in (:idlist"+smallListsIdx+")");
			startIdx = endIdx;
			smallListsIdx ++;
		}
		SplitHQL split = new SplitHQL(IDListQuery.toString()); 
		for(int i=0; i<smallLists.size(); i++){
			IdListsWithName idName = new IdListsWithName("idlist"+i, smallLists.get(i));
			split.getList().add(idName);
		}
    	return split;
    }

    public void resolveIds( Collection<? extends IHasPQL> entities ) throws EntityManagementException {
        resolveIds( entities, new IDResolutionQuery() {       	
            public Query makeQuery(Session hs, Collection<Long> ids) throws HibernateException {
            	SplitHQL sp = createIDListQuery(ids, "d.id");
                assert ids != null && !ids.isEmpty();
                Query query = hs.createQuery(
                	"select d.id, d.name from DevelopmentEntity d where ("+ sp.getQuery() +") and d.type=:type"       	
                )
                .setParameter("type", EntityType.COMPONENT, UserTypeForEntityType.TYPE);
                for(IdListsWithName idlist : sp.getList()) {
                	query.setParameterList(idlist.getName(), idlist.getList());
                }               
                return query;
            }
        });
    }

    public void resolveIds(Collection<? extends IHasPQL> entities, final Date asOf,
            final DeploymentType deploymentType) throws EntityManagementException {
        resolveIds( entities, new IDResolutionQuery() {
            public Query makeQuery(Session hs, Collection<Long> ids) throws HibernateException {
            	SplitHQL sp = createIDListQuery(ids, "dp.developmentEntity.id");
                assert ids != null && !ids.isEmpty();
                Query query = hs.createQuery(
                    "select dp.developmentEntity.id, dp.name "
                +   "from DeploymentEntity dp "
                +   "where ("+ sp.getQuery() + ")"
                +   "  and dp.timeRelation.activeFrom <= :asOf "
                +   "  and :asOf < dp.timeRelation.activeTo"
                +   "  and dp.developmentEntity.type = :type"
                +   "  and dp.deploymentRecord.deploymentType = :dt and dp.overrideCount = 0")   
                .setParameter( "asOf", asOf, DateToLongUserType.TYPE )
                .setParameter("type", EntityType.COMPONENT, UserTypeForEntityType.TYPE)
                .setParameter( "dt", deploymentType, UserTypeForDeploymentType.TYPE );
                for(IdListsWithName idlist : sp.getList()) {
                	query.setParameterList(idlist.getName(), idlist.getList());
                }
                return query;
            }
        });
    }

    private void resolveIds( Collection<? extends IHasPQL> entities, IDResolutionQuery queryMaker ) throws EntityManagementException {
        if ( entities == null ) {
            throw new NullPointerException("entities");
        }
        assert queryMaker != null;
        final Map<String,Object> parsed = new HashMap<String,Object>();
        final List<Long> ids = new LinkedList<Long>();
        final List<SpecReference> allRefs = new LinkedList<SpecReference>();

        class IdFinderVisitor extends DefaultPredicateVisitor implements IPQLVisitor, IExpressionVisitor {
            private final String pql;
            public IdFinderVisitor( String pql ) {
                this.pql = pql;
            }
            public void visitPolicy( DomainObjectDescriptor descr, IDPolicy policy) {
                if ( policy == null ) {
                    return;
                }
                ITarget target = policy.getTarget();
                if ( target == null ) {
                    return;
                }
                visitComponent( DomainObjectDescriptor.EMPTY, target.getActionPred() );
                visitComponent( DomainObjectDescriptor.EMPTY, target.getFromResourcePred() );
                visitComponent( DomainObjectDescriptor.EMPTY, target.getToResourcePred() );
                visitComponent( DomainObjectDescriptor.EMPTY, target.getSubjectPred() );
                visitComponent( DomainObjectDescriptor.EMPTY, target.getToSubjectPred() );
                parsed.put( pql, policy );
                if ( descr.getOwner() != null ) {
                    policy.setOwner( new Subject (
                            descr.getOwner().toString (), 
                            descr.getOwner().toString (), 
                            descr.getOwner().toString (), 
                            descr.getOwner (), 
                            SubjectType.USER));
                }
                if ( descr.getAccessPolicy() != null ) {
                    policy.setAccessPolicy( (AccessPolicy)descr.getAccessPolicy() );
                }
            }
            public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                if ( pred == null ) {
                    return;
                }
                pred.accept( this, IPredicateVisitor.PREORDER );
                // when ID is null, we know this is a surrogate spec from the policy target
                if ( descr.getId() != null ) {
                    IAccessControlled ac;
                    parsed.put( pql, ac = new SpecBase(
                        null
                    ,   SpecType.ILLEGAL
                    ,   descr.getId()
                    ,   descr.getName()
                    ,   descr.getDescription()
                    ,   descr.getStatus()
                    ,   pred
                    ,   descr.isHidden() ) );
                    if ( descr.getOwner() != null ) {
                        ac.setOwner( new Subject (
                                descr.getOwner().toString (), 
                                descr.getOwner().toString (), 
                                descr.getOwner().toString (), 
                                descr.getOwner (), 
                                SubjectType.USER) );
                    }
                    if ( descr.getAccessPolicy() != null ) {
                        ac.setAccessPolicy( (AccessPolicy)descr.getAccessPolicy() );
                    }
                }
            }
            public void visit(IPredicateReference pred) {
                if ( pred instanceof SpecReference ) {
                    addSpecReference( (SpecReference)pred );
                }
            }
            public void visit(IRelation pred ) {
                pred.getLHS().acceptVisitor( this, IExpressionVisitor.PREORDER );
                pred.getRHS().acceptVisitor( this, IExpressionVisitor.PREORDER );
            }
            public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
                // TODO: Add support for location referencing other locations here
            }
            public void visitFolder(DomainObjectDescriptor descriptor) {
                // Policy Folders are ignored in this context
            }
            public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                // Access policy is not relevant in this context
            }
            public void visit( IAttribute attribute ) {
                // Do nothing
            }
            public void visit( Constant constant ) {
                // Do nothing
            }
            public void visit( IFunctionApplication func) {
                // Do nothing
            }
            public void visit( IExpressionReference ref ) {
                if ( ref instanceof SpecReference ) {
                    addSpecReference( (SpecReference)ref );
                }
            }
            public void visit( IExpression expression ) {
                // Do nothing
            }
            private void addSpecReference( SpecReference ref ) {
                if ( !ref.isReferenceByName() ) {
                    ids.add( ref.getReferencedID() );
                    allRefs.add( ref );
                }
            }
        };

        try {
            // Parse everything
            for ( IHasPQL hasPql : entities ) {
                String pql = hasPql.getPql();
                DomainObjectBuilder.processInternalPQL(pql, new IdFinderVisitor(pql));
            }
            // There are no references by ID in the PQL - we should return right away.
            if ( ids.isEmpty() ) {
                return;
            }

            // Get IDs for all references
            final Map<Long,Object[]> idToNameType = new HashMap<Long,Object[]>();


            Session hs = null;
            Transaction tx = null;
            Throwable originalException = null;
            try {
                hs = hds.getCountedSession();
                tx = hs.beginTransaction();
                Collection<Object[]> qr = queryMaker.makeQuery(hs,ids).list();
                for ( Object[] v : qr ) {
                    // Queries must return two objects - a Long and a String
                    assert v != null
                        && v.length == 2
                        && v[0] instanceof Long
                        && v[1] instanceof String;
                    idToNameType.put( (Long)v[0], v );
                }
            } catch (HibernateException he) {
                originalException = he;
                throw new EntityManagementException(he);
            } finally {
                lazyCommitAndClose(tx, hs, originalException);
            }
            

            // Go through the references and resolve IDs into names
            for ( SpecReference ref : allRefs ) {
                assert !ref.isReferenceByName(); // We checked this beffore adding ref to the collection.
                Object[] v = (Object[])idToNameType.get( ref.getReferencedID() );
                if ( v == null ) {
                    throw new EntityManagementException("Unable to resolve ID "+ref.getReferencedID());
                }
                ref.setReferencedName((String)v[1]);
            }
            // Go through original entities, and update their PQL
            DomainObjectFormatter dof = new DomainObjectFormatter();
            for ( IHasPQL ent : entities ) {
                Object parsedEntity = parsed.get( ent.getPql() );
                if ( parsedEntity != null ) {
                    dof.reset();
                    if ( parsedEntity instanceof IDPolicy ) {
                        IDPolicy p = (IDPolicy)parsedEntity;
                        dof.formatPolicyDef(
                            new DomainObjectDescriptor(
                                p.getId()
                            ,   p.getName()
                            ,   (p.getOwner() == null) ? null : p.getOwner().getId ()
                            ,   p.getAccessPolicy()
                            ,   EntityType.POLICY
                            ,   p.getDescription()
                            ,   p.getStatus()
                            )
                        ,   p
                        );
                    } else if ( parsedEntity instanceof IDSpec ) {
                        IDSpec s = (IDSpec)parsedEntity;
                        dof.formatDef(
                            new DomainObjectDescriptor(
                                s.getId()
                            ,   s.getName()
                            ,   (s.getOwner() == null)? null : s.getOwner().getId ()
                            ,   s.getAccessPolicy()
                            ,   EntityType.COMPONENT
                            ,   s.getDescription()
                            ,   s.getStatus() )
                        ,   s.getPredicate()
                        );
                    } else {
                        assert false; // We did not put anything else into this map
                    }
                    ((IHasChangeablePQL)ent).setPql( dof.getPQL() );
                }
            }
        } catch ( PQLException e ) {
            throw new EntityManagementException(e);
        }
    }

    /**
     * Returns the earliest "active from" of a collecton of deployed entities.
     * This method is internal, therefore all argument checks are replaced
     * with assertions.
     *
     * @param deployedEntities a <code>Collection</code>
     * of <code>DeploymentEntity</code> objects.
     * @return the earliest "active from" of a collecton of deployed entities.
     */
    private static Date earliestActivationTime(Collection<DeploymentEntity> deployedEntities) {
        assert deployedEntities != null;
        assert !deployedEntities.isEmpty();
        Date res = UnmodifiableDate.END_OF_TIME;
        for ( DeploymentEntity dep : deployedEntities ) {
            if (res.after(dep.getActiveFrom())) {
                res = dep.getActiveFrom();
            }
        }
        return res;
    }

    /**
     * Removes inactive records that can no longer be re-activated.
     *
     * @param hs the hibernate session on which to perform the deletion.
     * @param now the time as of which to perform the deletion.
     * @throws HibernateException when there is an error performing the operation.
     */
    private static void purge(Session hs, Date now) throws HibernateException {
        // Purge the overriden records that became active in the past
        // because they can no longer be unscheduled for deployment:
        // Collect IDs of deployment records that may need to be deleted
        Set<Long> deploymentRecordsToDelete = new HashSet<Long>();
        List<DeploymentEntity> overriden = hs.find(
            "from DeploymentEntity dp where dp.overrideCount > 0 and dp.timeRelation.activeFrom <= ?"
        ,   now
        ,   DateToLongUserType.TYPE );
        for ( DeploymentEntity dep : overriden ) {
            deploymentRecordsToDelete.add( dep.getDeploymentRecord().getId() );
            hs.delete( dep );
        }
        if ( !deploymentRecordsToDelete.isEmpty() ) {
            // Purge deployment records that correspond to
            // completely overriden deployment sets
            Query delQuery = hs.createQuery(
                "from DeploymentRecord as dr where dr.id in (:ids) AND not exists "
            +   "(from DeploymentEntity as dp where dp.deploymentRecord = dr)"
            );
            delQuery.setParameterList( "ids", deploymentRecordsToDelete );
            List<DeploymentRecord> drToDelete = delQuery.list();
            for ( DeploymentRecord dr : drToDelete ) {
                hs.delete( dr );
            }
        }
    }

    /**
     * For a collection of <code>DevelopmentEntity</code> objects,
     * populates a map from the entity name to the entity. When multiple
     * entities compete for the same name, the last one wins.
     * @param devEntities the entities to be added to the map.
     * @param map the <code>Map[]</code> to which to add the entities.
     * The actual map is picked based on the type of the entity.
     *
     * All calls to this method are internal, so the argument checks
     * are replaced with Java assertions.
     */
    private static void mapByName( Collection<DevelopmentEntity> devEntities, Map<String,DevelopmentEntity>[] map ) {
        assert devEntities != null;
        assert map != null;
        for ( DevelopmentEntity dev : devEntities ) {
            map[dev.getType().getType()].put( dev.getName(), dev );
        }
    }

    /**
     * this method is package visibility for use by unit tests
     * @return Hibernate session factory used by this lifecycle manager
     */
    SessionFactory getSessionFactory() {
        return hds;
    }

    /**
     * A hibernate interceptor that automatically updates the lastUpdated field
     * of the Developmententity objects, and converts a collection of
     * the <code>DevelopmentEntity</code> objects to a <code>Collection</code>
     * of Long IDs of the corresponding objects.
     */
    private static final Interceptor ENTITY_SAVE_INTERCEPTOR = new DefaultInterceptor() {

        public boolean onFlushDirty(
            Object obj
        ,   Serializable id
        ,   Object[] state
        ,   Object[] prevState
        ,   String[] names
        ,   Type[] types
        ) throws CallbackException {
            if (obj instanceof DevelopmentEntity) {
                updateLastUpdated((DevelopmentEntity) obj, state, names);
                return true;
            } else {
                return false;
            }
        }

        public boolean onSave(Object obj, Serializable id, Object[] state, String[] names, Type[] types) throws CallbackException {
            if (obj instanceof DevelopmentEntity) {
                updateLastUpdated((DevelopmentEntity) obj, state, names);
                return true;
            } else {
                return false;
            }
        }

        private void updateLastUpdated(DevelopmentEntity obj, Object[] state, String[] names) {
            // Set the last updated field
            DevelopmentEntity dev = (DevelopmentEntity) obj;
            Date now = new Date();
            dev.setLastUpdated(now);
            
            if (dev.getCreated() == null) {
                dev.setCreated(now);
            }
            if (posLastUpdated == -1) {
                for (int i = 0; i != names.length ; i++) {
                    if ("lastUpdated".equals(names[i])) {
                        posLastUpdated = i;
                    }
                    if ("created".equals(names[i])) {
                        posCreated = i;
                    }
                }
            }
            // Fix the state to avoid an extra trip to DB from hibernate
            if (posLastUpdated != -1) {
                state[posLastUpdated] = now;
            }
            if (posCreated != -1) {
                state[posCreated] = dev.getCreated();
            }
        }

        private int posLastUpdated = -1;
        private int posCreated = -1;
    };

    /**
     * Takes a collection of DeploymentEntity objects, and returns a collection of their IDs.
     *
     * @param deploymentEntities a collection of DeploymentEntity objects.
     * @return a collection of IDs of deploymentEntities.
     */
    private static Collection<Long> deploymentEntitiesToIds(Collection<DeploymentEntity> deploymentEntities) {
        Set<Long> res = new HashSet<Long>();
        for ( DeploymentEntity dep : deploymentEntities ) {
            res.add(dep.getId());
        }
        return Collections.unmodifiableSet(res);
    }

    /**
     * This private interface parameterizes the Deploy/Undeploy method.
     * Since the only difference between deploying and undeploying is
     * what's actually gets deployed (a copy or a "tombstone"), passing
     * an appropriate anonymous implementation makes the same
     * algorithm perform both actions.
     */
    private interface DeploymentEntityMaker {
        DeploymentEntity makeDeploymentEntity(DevelopmentEntity dev, DeploymentRecord rec, Date asOf);

        DeploymentActionType getActivityType();

        boolean checkStatus(DevelopmentStatus status);
    }

    /**
     * Splits a <code>String</code> containing a path to a policy
     * in a policy folder into a collection of path elements to the
     * policy. For example, the path
     * <code>"Equities$Trading$Enforcement$NoResearchViewing"</code>
     * is split into a <code>List</code> of these <code>Strings</code>:
     * <code>"Equities"</code>, <code>"Equities$Trading"</code>,
     * and <code>"Equities$Trading$Enforcement"</code>.
     * When the path to a policy at the root is passed in, an empty
     * <code>List</code> is returned.
     *
     * This method has package visibility for testing.
     *
     * @param path A path to a policy or a policy folder.
     * @return A <code>Collection</code> of path elements.
     */
    static List<String> toPathElements( String path ) {
        if ( path == null ) {
            throw new NullPointerException(path);
        }
        if ( path.length() == 0 ) {
            throw new IllegalArgumentException("path");
        }

        List<String> res = new ArrayList<String>(StringUtils.count(path, PQLParser.SEPARATOR));
        for ( int pos = path.indexOf( PQLParser.SEPARATOR ) ; pos != -1 ; pos = path.indexOf( PQLParser.SEPARATOR, pos+1 ) ) {
            res.add( path.substring( 0, pos ) );
        }
        return res;
    }

    /**
     * Classes implementing this interface construct
     * dependencies from pairs (from,to). Dependencies can
     * be either forward or reverse.
     */
    private interface DependencyBuilder {
        /**
         * Adds a direct reference from one ID to another.
         * @param dg A <code>Map</code> representing the dependency graph
         * as a collection of an adjacency lists.
         * @param fromId the from ID of the reference.
         * @param toId the to ID of the reference.
         */
        void addReference( Map<Long,Set<Long>> dg, Long fromId, Long toId );
    }

    /**
     * Classes implementing this interface extract IDs from
     * <code>DevelopmentEntity</code> or <code>DeploymentEntity</code> objects.
     */
    private interface IdExtractor {
        Long extractId( Object obj );
    }

    /**
     * Given an initial <code>Collection</code> of IDs and a <code>Collection</code>
     * of candidate entities (either <code>DevelopmentEntity</code> or
     * <code>deploymentEntity</code> objects), returns a <code>Collection</code>
     * of their dependencies, direct or indirect, with one-away references obtained
     * through the passed-in dependency builder object.
     *
     * @param initial a <code>Collection</code> of IDs of the initial frontier of the BFS.
     * @param candidates a <code>Collection</code> of all candidate entities.
     * @param dependencyBuilder a <code>DependencyBuilder</code> object for adding
     *    references to the dependency graph. Either a direct or an indirect reference
     *    can be added.
     * @param idExtractor an <code>IdExtractor</code> object for extracting
     *    ID from the candidate objects.
     * @param onlyDirect indicates whether the full closure of the reference graph
     *    is to be returned, or only the one-away references should be extracted.
     * @return a <code>Collection</code> of IDs resulted from BFS expansion of the
     *    initial set of entities. None of the IDs from the <code>initial</code>
     *    <code>Collection</code> is included in the returned <code>Collection</code>.
     * @throws PolicyServiceCircularRefException when the dependency chain contains circular references.
     * @throws PQLException when any of the candidates has invalid PQL.
     */
    public Collection<Long> discoverBFS(Collection<Long> initial,
            Collection<? extends IHasPQL> candidates, DependencyBuilder dependencyBuilder,
            IdExtractor idExtractor, boolean onlyDirect) throws PQLException,
            CircularReferenceException {
        /**
         * Reference finder visitor class walks policies and predicates,
         * finding and storing all direct references.
         */
        class ReferenceFinderVisitor extends DefaultPredicateVisitor implements IPQLVisitor, IExpressionVisitor {
            private Set<Long> refs = new HashSet<Long>();
            private ValidationVisitor validationVisitor = new ValidationVisitor();
            private Map<String, DevelopmentEntity> devEntityByName = new HashMap<String, DevelopmentEntity>();
            private PQLException internalPQLException = null; 

            public ReferenceFinderVisitor() {
                super();

                try {
                    for (DevelopmentEntity dev : getAllEntitiesOfType(EntityType.POLICY)) {
                        devEntityByName.put(dev.getName(), dev);
                    }
                } catch (EntityManagementException e) {
                    log.error("Unable to get entities of type POLICY from db", e);
                }
            }

            public void reset() {
                refs = new HashSet<Long>();
            }

            public Set<Long> getRefs() {
                return Collections.unmodifiableSet( refs );
            }

            public PQLException getException() {
                return internalPQLException;
            }

            public void visitPolicy(DomainObjectDescriptor descr, IDPolicy policy) {
                if ( policy == null ) {
                    return;
                }
                ITarget target = policy.getTarget();
                if ( target == null ) {
                    return;
                }
                visitComponent( DomainObjectDescriptor.EMPTY, target.getActionPred() );
                visitComponent( DomainObjectDescriptor.EMPTY, target.getFromResourcePred() );
                visitComponent( DomainObjectDescriptor.EMPTY, target.getToResourcePred() );
                visitComponent( DomainObjectDescriptor.EMPTY, target.getSubjectPred() );
                visitComponent( DomainObjectDescriptor.EMPTY, target.getToSubjectPred() );
                visitExceptions ( DomainObjectDescriptor.EMPTY, policy.getPolicyExceptions() );
            }
            public void visitExceptions ( DomainObjectDescriptor descr, IPolicyExceptions exceptions ) {
                for (IPolicyReference ref : exceptions.getPolicies()) {
                    if (ref == null) {
                        throw new NullPointerException("Null reference inside policy exceptions for DomainObjectDescriptor " + descr.getName());
                    }

                    DevelopmentEntity devEntity = devEntityByName.get(ref.getReferencedName());

                    if (devEntity == null) {
                        log.warn("Unable to find devEntity for policy reference " + ref.getReferencedName());
                        continue;
                    }

                    refs.add(devEntity.getId());
                }
            }
            public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                checkPredicate( pred );
            }
            private void checkPredicate( IPredicate pred ) {
                if ( pred == null ) {
                    return;
                }
                validationVisitor.reset();
                pred.accept( validationVisitor, IPredicateVisitor.PREPOSTORDER );
                pred.accept( this, IPredicateVisitor.PREORDER );
            }
            public void visit(IPredicateReference pred) {
                if ( pred instanceof SpecReference ) {
                    processReference((SpecReference)pred);
                }
            }
            public void visit( IRelation rel ) {
                rel.getLHS().acceptVisitor( this, IExpressionVisitor.PREORDER );
                rel.getRHS().acceptVisitor( this, IExpressionVisitor.PREORDER );
            }
            public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
                // TODO: Add code for finding references to other locations here
            }
            public void visitFolder(DomainObjectDescriptor descriptor) {
                // Policy Folders are ignored in this context
            }
            public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                // Not relevant in this context
            }
            public void visit( IAttribute attribute ) {
                // Ignored
            }
            public void visit( Constant constant ) {
                // Ignored
            }
            public void visit( IFunctionApplication func) {
                // Do nothing
            }
            public void visit( IExpressionReference expr ) {
                if ( expr instanceof SpecReference ) {
                    processReference((SpecReference)expr);
                }
            }
            public void visit( IExpression expression ) {
                // Ignored
            }
            private void processReference( SpecReference ref ) {
                if ( !ref.isReferenceByName() ) {
                    refs.add( ref.getReferencedID() );
                }
            }
        }

        // We know all of this is true because this method is called internally.
        assert initial != null;
        assert candidates != null;
        assert dependencyBuilder != null;
        assert idExtractor != null;

        // Build a dependency graph
        Map<Long,Set<Long>> dg = new HashMap<Long,Set<Long>>();
        ReferenceFinderVisitor finder = new ReferenceFinderVisitor();
        for ( IHasPQL obj : candidates ) {
            Long id = idExtractor.extractId( obj );
            String pql = obj.getPql();
            finder.reset();
            try {
                DomainObjectBuilder.processInternalPQL( pql, finder );
            } catch ( ValidationVisitor.CircularReferenceException e ) {
                // We found a self-referencing predicate. This is an unlikely case,
                // but preventing it goes long way toward avoiding infinite loops.
                List refs;
                try {
                    refs = getEntityDescriptorsForIDs(
                        Arrays.asList( new Long[] { id } )
                    ,   DIRECT_CONVERTER
                    );
                } catch ( EntityManagementException eme ) {
                    refs = Arrays.asList( new DomainObjectDescriptor[0] );
                }
                throw new CircularReferenceException( refs );
            }

            if (finder.getException() != null) {
                throw finder.getException();
            }

            for ( Long refId : finder.getRefs() ) {
                dependencyBuilder.addReference(dg, id, refId);
            }
        }

        // Go through the graph in BFS order, using the items in the
        // initial collection as the initial frontier:
        Set<Long> discovered = new HashSet<Long>( initial );
        Set<Long> resIds = new HashSet<Long>();
        LinkedList<Long> q = new LinkedList<Long>( initial );
        while ( !q.isEmpty() ) {
            Long toExplore = (Long)q.removeFirst();
            if ( dg.containsKey( toExplore ) ) {
                Set<Long> edges = dg.get( toExplore );
                if ( edges == null ) {
                    continue;
                }
                for ( Long edge : edges ) {
                    if ( !discovered.contains( edge ) ) {
                        discovered.add( edge );
                        resIds.add( edge );
                        if ( !onlyDirect ) {
                            q.addLast( edge );
                        }
                    }
                }
            }
        }

        checkCircularRefs( dg, initial, new LinkedList<Long>(), new HashSet<Long>() );

        return resIds;
    }

    /**
     * Given a <code>Collection</code> of ids, returns its subset
     * such that all items included in the result have been deployed
     * at least once.
     * @param ids The initial <code>Collection</code> of ids.
     * @return A subset of the initial collection such that all
     * items in the result have been deployed at least once.
     */
    public Collection<Long> checkHasDeployments( Collection ids, DeploymentType type ) throws EntityManagementException {
        if ( ids == null ) {
            throw new NullPointerException("ids");
        }
        if ( ids.isEmpty() ) {
            return Collections.emptyList();
        }
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            return hs.createQuery(
                "select distinct d.developmentEntity.id "
            +   "from DeploymentEntity d "
            +   "where d.developmentEntity.id in (:ids)"
            +   "  and d.overrideCount=0"
            +   "  and d.deploymentRecord.deploymentType = :depType"
            )
            .setParameterList( "ids", ids )
            .setParameter( "depType", type, UserTypeForDeploymentType.TYPE )
            .list();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Given a <code>Collection</code> of ids, returns its subset
     * such that all items included in the result have deployments
     * scheduled in the future.
     * @param ids The initial <code>Collection</code> of ids.
     * @return A subset of the initial collection such that all
     * items in the result have deployments in the future.
     */
    public Collection<Long> checkHasFutureDeployments( Collection<Long> ids, DeploymentType type ) throws EntityManagementException {
        if ( ids == null ) {
            throw new NullPointerException("ids");
        }
        if ( ids.isEmpty() ) {
            return Collections.emptyList();
        }
        
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            return hs.createQuery(
                "select distinct d.developmentEntity.id "
            +   "from DeploymentEntity d "
            +   "where d.developmentEntity.id in (:ids)"
            +   "  and d.overrideCount=0"
            +   "  and d.timeRelation.activeFrom >= :now"
            +   "  and d.deploymentRecord.deploymentType = :depType"
            )
            .setParameterList( "ids", ids )
            .setParameter( "now", new Date(), DateToLongUserType.TYPE )
            .setParameter( "depType", type, UserTypeForDeploymentType.TYPE )
            .list();
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Given a <code>Collection</code> of ids, returns its subset
     * such that all items included in the result are referenced
     * from at least one entity.
     * @param ids The initial <code>Collection</code> of ids.
     * @return A subset of the initial collection such that all
     * items in the result are referenced.
     */
    public Collection<Long> checkHasReferences( Collection<Long> ids ) throws EntityManagementException {
        if ( ids == null ) {
            throw new NullPointerException("ids");
        }
        if ( ids.isEmpty() ) {
            return Collections.emptyList();
        }
        
        Session hs = null;
        Transaction tx = null;
        Throwable originalException = null;
        
        final Set<Long> idSet = new HashSet<Long>( ids );
        try {
            hs = hds.getCountedSession();
            tx = hs.beginTransaction();
            Collection<EntityType> types = hs.createQuery(
                "select distinct d.type from DevelopmentEntity d where d.id in (:ids)"
            )
            .setParameterList( "ids", ids )
            .list();
            Set<EntityType> typesForQuery = new HashSet<EntityType>();
            for ( EntityType type : types ) {
                if ( type != EntityType.POLICY
                  && type != EntityType.FOLDER
                  && !typesForQuery.contains( type ) ) {
                    typesForQuery.add( type );
                    typesForQuery.add( EntityType.POLICY );
                }
            }
            if ( typesForQuery.isEmpty() ) {
                return Collections.emptyList();
            }
            Collection<DevelopmentEntity> candidates =
                hs.createQuery("from DevelopmentEntity d where d.type in (:types) and d.status != :deleted")
                .setParameterList("types", typesForQuery, UserTypeForEntityType.TYPE )
                .setParameter("deleted", DevelopmentStatus.DELETED, UserTypeForDevelopmentStatus.TYPE)
                .list();
            final Set<Long> res = new HashSet<Long>();
            class IdFinderVisitor extends DefaultPredicateVisitor implements IPQLVisitor, IExpressionVisitor {
                public void visitPolicy( DomainObjectDescriptor descr, IDPolicy policy) {
                    if ( policy == null ) {
                        return;
                    }
                    ITarget target = policy.getTarget();
                    if ( target == null ) {
                        return;
                    }
                    visitComponent( DomainObjectDescriptor.EMPTY, target.getActionPred() );
                    visitComponent( DomainObjectDescriptor.EMPTY, target.getFromResourcePred() );
                    visitComponent( DomainObjectDescriptor.EMPTY, target.getToResourcePred() );
                    visitComponent( DomainObjectDescriptor.EMPTY, target.getSubjectPred() );
                    visitComponent( DomainObjectDescriptor.EMPTY, target.getToSubjectPred() );
                }
                public void visitComponent(DomainObjectDescriptor descr, IPredicate pred) {
                    if ( pred == null ) {
                        return;
                    }
                    pred.accept( this, IPredicateVisitor.PREORDER );
                }
                public void visit(IPredicateReference pred) {
                    if ( pred instanceof SpecReference ) {
                        addSpecReference( (SpecReference)pred );
                    }
                }
                public void visit(IRelation pred ) {
                    pred.getLHS().acceptVisitor( this, IExpressionVisitor.PREORDER );
                    pred.getRHS().acceptVisitor( this, IExpressionVisitor.PREORDER );
                }
                public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
                    // TODO: Add support for location referencing other locations here
                }
                public void visitFolder(DomainObjectDescriptor descriptor) {
                    // Policy Folders are ignored in this context
                }
                public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                    // Access policy is not relevant in this context
                }
                public void visit( IAttribute attribute ) {
                    // Do nothing
                }
                public void visit( Constant constant ) {
                    // Do nothing
                }
                public void visit( IFunctionApplication func) {
                    // Do nothing
                }
                public void visit( IExpressionReference ref ) {
                    if ( ref instanceof SpecReference ) {
                        addSpecReference( (SpecReference)ref );
                    }
                }
                public void visit( IExpression expression ) {
                    // Do nothing
                }
                private void addSpecReference( SpecReference ref ) {
                    if ( !ref.isReferenceByName() ) {
                        if ( idSet.contains( ref.getReferencedID() ) ) {
                            res.add( ref.getReferencedID() );
                        }
                    }
                }
            };
            IPQLVisitor visitor = new IdFinderVisitor();
            for ( DevelopmentEntity dev : candidates ) {
                DomainObjectBuilder.processInternalPQL( dev.getPql(), visitor );
            }
            return res;
        } catch (HibernateException he) {
            originalException = he;
            throw new EntityManagementException(he);
        } catch (PQLException e) {
            originalException = e;
            throw new EntityManagementException(e);
        } finally {
            lazyCommitAndClose(tx, hs, originalException);
        }
    }

    /**
     * Traverses the dependency graph depth-first, looking for circular references.
     * @param dg a <code>Map</code> of <code>Long</code> to <code>Set</code>s of
     * <code>Long</code>s, representing the dependency graph.
     * @param nodes a <code>Collection</code> of nodes to explore DFS.
     * @param black a <code>List</code> of nodes currently on the stack.
     * @param white a <code>Set</code> of nodes that we have processed.
     * @throws PolicyServiceCircularRefException when a circular reference is found.
     */
    private void checkCircularRefs(Map<Long, Set<Long>> dg, Collection<Long> nodes,
            List<Long> black, Set<Long> white) throws CircularReferenceException {
        if (nodes == null) {
            return;
        }
        for (Long node : nodes) {
            if (white.contains(node)) {
                continue;
            }
            if (black.contains(node)) {
                List refs;
                try {
                    refs = getEntityDescriptorsForIDs(black, DIRECT_CONVERTER);
                } catch (EntityManagementException eme) {
                    refs = Arrays.asList(new Object[0]);
                }
                throw new CircularReferenceException(refs);
            }
            black.add(node);
            checkCircularRefs(dg, dg.get(node), black, white);
            black.remove(node);
            white.add(node);
        }
    }

    /**
     * Converts a <code>Collection</code> of <code>DevelopmentEntity</code>
     * objects to a <code>List</code> of <code>DomainObjectDescriptor</code>
     * objects.
     * @param entities a <code>Collection</code> of <code>DevelopmentEntity</code>
     * objects to convert.
     * @param maker Maker of <code>DomainObjectDescriptor</code> objects.
     * @return a <code>List</code> of <code>DomainObjectDescriptor</code> objects.
     */
    private static List<DomainObjectDescriptor> convertDevEntitiesToDescriptors( Collection<DevelopmentEntity> entities, DescriptorMaker maker ) {
        if ( entities == null ) {
            return null;
        }
        if ( maker == null ) {
            throw new NullPointerException("maker");
        }
        List<DomainObjectDescriptor> res = new ArrayList<DomainObjectDescriptor>(entities.size());
        for ( DevelopmentEntity dev : entities ) {
            res.add( maker.convert( dev ) );
        }
        return res;
    }

    /**
     * Converts a <code>Collection</code> of <code>DevelopmentEntity</code>
     * objects to a <code>List</code> of <code>DODDigest</code> objects.
     * @param entities a <code>Collection</code> of <code>DevelopmentEntity</code>
     * objects to convert.
     * @param maker Maker of <code>DODDigest</code> objects.
     * @return a <code>List</code> of <code>DODDigest</code> objects.
     */
    public static List<DODDigest> convertDevEntitiesToDigests(Collection<DevelopmentEntity> entities, DigestMaker maker) {
        if (entities == null) {
            return null;
        }

        if (maker == null) {
            throw new NullPointerException("maker");
        }
        List<DODDigest> res = new ArrayList<DODDigest>(entities.size());

        List<Long> entityIds = new ArrayList<Long>(entities.size());
        for (DevelopmentEntity entity : entities) {
            entityIds.add(entity.getId());
        }

        maker.computeObjectUsage(entityIds);
        
        for (DevelopmentEntity dev : entities) {
            res.add(maker.convert(dev));
        }
        return res;
    }

    /**
     * Defines a contract for re-formatting objects with a new descriptor.
     */
    private interface IDescriptorFormatter {
        /**
         * Re-formats an object together with a new descriptor.
         * @param dof The formatter to use for the formatting operation.
         * @param dod the <code>DomainObjectDescriptor</code> to impose
         * on top of the given object.
         */
        public void format( DomainObjectFormatter dof, DomainObjectDescriptor dod );
    }
    
    /**
     * Creates a wrapper for formatting PQL from <code>IDPolicy</code> objects.
     * @param policy the <code>IDPolicy</code> to format.
     * @return an <code>IDescriptorFormatter</code> object for the policy.
     */
    private static IDescriptorFormatter formatterForPolicy( final IDPolicy policy ) {
        return new IDescriptorFormatter() {
            public void format( DomainObjectFormatter dof, DomainObjectDescriptor dod ) {
                dof.formatPolicyDef( dod, policy );
            }
        };
    }

    /**
     * Creates a wrapper for formatting PQL from <code>PolicyFolder</code> objects.
     * @return an <code>IDescriptorFormatter</code> object for the folder.
     */
    private static IDescriptorFormatter formatterForPolicyFolder() {
        return new IDescriptorFormatter() {
            public void format( DomainObjectFormatter dof, DomainObjectDescriptor dod ) {
                dof.formatFolder( dod );
            }
        };
    }

    /**
     * Creates a wrapper for formatting PQL from <code>IPredicate</code> objects.
     * @param pred the <code>IPredicate</code> to format.
     * @return an <code>IDescriptorFormatter</code> object for the spec.
     */
    private static IDescriptorFormatter formatterForPred( final IPredicate pred ) {
        return new IDescriptorFormatter() {
            public void format( DomainObjectFormatter dof, DomainObjectDescriptor dod ) {
                dof.formatDef( dod, pred);
            }
        };
    }

    /**
     * Creates a wrapper for formatting PQL from <code>Location</code> objects.
     * @param folder the <code>Location</code> to format.
     * @return an <code>IDescriptorFormatter</code> object for the location.
     */
    private static IDescriptorFormatter formatterForLocation( final Location location ) {
        return new IDescriptorFormatter() {
            public void format( DomainObjectFormatter dof, DomainObjectDescriptor dod ) {
                dof.formatLocation( dod, location );
            }
        };
    }

    /**
     * Constructs direct references from-->to.
     */
    public static final DependencyBuilder DIRECT_DEPENDENCY_BUILDER = new DependencyBuilder() {
        public void addReference(Map<Long,Set<Long>> dg, Long fromId, Long toId) {
            if ( !dg.containsKey( fromId ) ) {
                dg.put( fromId, new HashSet<Long>() );
            }
            dg.get(fromId).add( toId );
        }
    };

    /**
     * Constructs reverse references to-->from.
     */
    public static final DependencyBuilder REVERSE_DEPENDENCY_BUILDER = new DependencyBuilder() {
        public void addReference(Map<Long,Set<Long>> dg, Long fromId, Long toId) {
            if ( !dg.containsKey( toId ) ) {
                dg.put( toId, new HashSet<Long>() );
            }
            dg.get(toId).add(fromId);
        }
    };

    /**
     * Extracts IDs from <code>DevelopmentEntity</code> objects.
     */
    public static final IdExtractor DEVELOPMENT_ID_EXTRACTOR = new IdExtractor() {
        public Long extractId( Object obj ) {
            return ((DevelopmentEntity)obj).getId();
        }
    };

    /**
     * Extracts IDs from <code>DeploymentEntity</code> objects.
     */
    public static final IdExtractor DEPLOYMENT_ID_EXTRACTOR = new IdExtractor() {
        public Long extractId( Object obj ) {
            DevelopmentEntity dev = ((DeploymentEntity)obj).getDevelopmentEntity();
            return dev != null ? dev.getId() : null;
        }
    };

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {

        try {
            final IDestinyConfigurationStore confStore = (IDestinyConfigurationStore) getManager().getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
            IDPSComponentConfigurationDO dpsConfig = ((IDPSComponentConfigurationDO) (confStore.retrieveComponentConfiguration(ServerComponentType.DPS.getName())));
            if (dpsConfig != null) {
                window = dpsConfig.getLifecycleManagerGraceWindow();
            }
        }
        catch (Exception e) {
            // if we can't get shared context locator, assume running standalone
        }
        hds = (IHibernateRepository) manager.getComponent( DestinyRepository.POLICY_FRAMEWORK_REPOSITORY.getName() );
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.config = config;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return config;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
    }

    /**
     * A user type for persisting instances of <code>EntityType</code> in Hibernate.
     */
    public static class UserTypeForEntityType extends EnumUserType<EntityType> {
        public UserTypeForEntityType() {
            super( new EntityType[] {
                EntityType.POLICY
            ,   EntityType.FOLDER
            ,   EntityType.LOCATION
            ,   EntityType.COMPONENT
            ,   EntityType.HOST
            ,   EntityType.USER
            ,   EntityType.APPLICATION
            ,   EntityType.ACTION
            ,   EntityType.RESOURCE
            }
            ,   new String[] {
                "PO"
            ,   "FL"
            ,   "LC"
            ,   "CO"
            ,   "HO"
            ,   "US"
            ,   "AP"
            ,   "AC"
            ,   "RS"
            }
            ,   EntityType.class
            );
        }

        public static final Type TYPE = makeHibernateType( UserTypeForEntityType.class );
    }

    /**
     * A user type for persisting instances of <code>DeploymentActionType</code> in Hibernate
     */
    public static class UserTypeForDeploymentActionType extends EnumUserType<DeploymentActionType> {
        public UserTypeForDeploymentActionType() {
            super( new DeploymentActionType[] {
                DeploymentActionType.DEPLOY
            ,   DeploymentActionType.UNDEPLOY
            }
            ,   new String[] {
                "DE"
            ,   "UN"
            }
            ,   DeploymentActionType.class
            );
        }

        public static final Type TYPE = makeHibernateType( UserTypeForDeploymentActionType.class );
    }

    /**
     * A user type for persisting instances of <code>DeploymentType</code> in Hibernate
     */
    public static class UserTypeForDeploymentType extends EnumUserType<DeploymentType> {
        public UserTypeForDeploymentType() {
            super( new DeploymentType[] { DeploymentType.PRODUCTION, DeploymentType.TESTING }
            ,   new String[]            { "PR",                      "TS"  }
            ,   DeploymentType.class
            );
        }
        public static final Type TYPE = makeHibernateType( UserTypeForDeploymentType.class );
    }

    /**
     * A user type for persisting instances of <code>DevelopmentStatus</code> in Hibernate
     */
    public static class UserTypeForDevelopmentStatus extends EnumUserType<DevelopmentStatus> {
        public UserTypeForDevelopmentStatus() {
            super( new DevelopmentStatus[] {
                DevelopmentStatus.EMPTY
            ,   DevelopmentStatus.DRAFT
            ,   DevelopmentStatus.APPROVED
            ,   DevelopmentStatus.OBSOLETE
            ,   DevelopmentStatus.DELETED
            ,   DevelopmentStatus.ILLEGAL
            }
            ,   new String[] {
                "EM"
            ,   "DR"
            ,   "AP"
            ,   "OB"
            ,   "DE"
            ,   "##"
            }
            ,   DevelopmentStatus.class
            );

        }

        public static final Type TYPE = makeHibernateType();

        private static Type makeHibernateType() {
            try {
                return Hibernate.custom( UserTypeForDevelopmentStatus.class );
            } catch ( Exception ex ) {
                return null;
            }
        }
    }

    private static final Type makeHibernateType( Class cl ) {
        try {
            return Hibernate.custom( cl );
        } catch ( HibernateException ex ) {
            return null;
        }
    }
}
