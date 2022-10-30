package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/DevelopmentEntity.java#1 $
 */

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.utils.ObjectHelper;
import com.bluejungle.framework.utils.TernaryType;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.IPQLVisitor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.common.IAccessPolicy;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.epicenter.policy.IPolicy;

public class DevelopmentEntity extends AbstractEntity implements IHasChangeablePQL {

    /** The unique Id of the owner of the entity. */
    private Long owner;

    /** PQL which defines access policy on this entity. */
    private String apPql;

    /** The type if this entity. */
    private EntityType type;

    /** Development status of the entity (draft, approved, etc.) */
    private DevelopmentStatus status = DevelopmentStatus.NEW;
    
    /** The date this entity was created. */
    private Date created;
    
    /** The date this entity was most recently updated. */
    private Date lastUpdated;
    
    /** Flag indicating if this entity has dependencies */
    private boolean hasDependencies;

    /** Flag indicating if this entity is a sub-policy */
    private boolean isSubPolicy;

    /**
     * Private default constructor for hibernate's use.
     */
    DevelopmentEntity() {
    }
    
    /**
     * Creates a new development entity defined by the specified PQL.
     * @param pql the PQL content of the development entity.
     */
    public DevelopmentEntity(String pql) throws PQLException {
        super();
        hasDependencies = false;
        isSubPolicy = false;
        setPql(pql);
    }
    
    /**
     * Creates a new development entity defined by the specified PQL,
     * with the specified type.
     * @param pql the PQL content of the development entity.
     * @param type The desired type of this entity.
     */
    public DevelopmentEntity(String pql, EntityType type) throws PQLException {
        this(pql);
        this.type = type;
    }
    
    /**
     * Returns the owner of the entity.
     * @return the owner of the entity.
     */
    public Long getOwner() {
        return owner;
    }

    /**
     * Changes the owner of the entity.
     * @param owner the new owner of the entity.
     */
    void setOwner(Long owner) {
        this.owner = owner;
    }

    /**
     * Getter for Hibernate
     * @return the apPql of the entity.
     */
    public String getApPql () {
        return apPql;
    }

    /**
     * Setter for Hibernate
     * @param apPql the new apPql of the entity.
     */
    public void setApPql( String apPql ) {
        this.apPql = apPql;
    }

    /**
     * Returns the access policy pql for the entity.
     * @return the access policy pql for the entity.
     */
    public String getAccessPolicyPQL() {
        return apPql;
    }

    /**
     * Returns the access policy pql for the entity.
     * @return the access policy pql for the entity.
     */
    public IAccessPolicy getAccessPolicy() {
        if (apPql == null) return null;
        DomainObjectBuilder dob = new DomainObjectBuilder(apPql);
        try {
            return dob.processAccessPolicy();
        } catch (PQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Changes the access policy pql for the entity.
     * This method is for Hibernate's use
     * @param apPql the new access policy pql for the entity.
     */
    void setAccessPolicyPQL( String apPql ) {
        this.apPql = apPql;
    }

    /**
     * Changes the PQL content of the entity,
     * and synchronizes the fields to the data in the PQL.
     * @param pql the new PQL content of the entity.
     */
    public void setPql( String pql ) throws PQLException {
        if ( pql == null ) {
            throw new NullPointerException("pql");
        }
        this.pql = pql;

        final AtomicBoolean hasReferences = new AtomicBoolean(false);

        DomainObjectBuilder.processInternalPQL( pql,
            new IPQLVisitor() {
                public void visitPolicy(DomainObjectDescriptor descriptor, IDPolicy policy) {
                    isSubPolicy = policy.hasAttribute(IPolicy.EXCEPTION_ATTRIBUTE);

                    if (policy.getPolicyExceptions().getPolicies().size() > 0 ||
                        foundReferences(policy.getTarget().getActionPred(),
                                        policy.getTarget().getFromResourcePred(),
                                        policy.getTarget().getToResourcePred(),
                                        policy.getTarget().getSubjectPred(),
                                        policy.getTarget().getToSubjectPred())) {
                        hasReferences.set(true);
                    }
                    
                    syncToDescriptor( descriptor );
                }
                public void visitFolder(DomainObjectDescriptor descriptor) {
                    syncToDescriptor( descriptor );
                }
                public void visitComponent(DomainObjectDescriptor descriptor, IPredicate pred) {
                    if (foundReferences(pred)) {
                        hasReferences.set(true);
                    }
                    syncToDescriptor( descriptor );
                }
                public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
                    syncToDescriptor( descriptor );
                }
                public void visitAccessPolicy(DomainObjectDescriptor descriptor, IAccessPolicy accessPolicy) {
                    syncToDescriptor( descriptor );
                }

                private boolean foundReferences(IPredicate ... preds) {
                    for (IPredicate pred : preds) {
                        if (foundReferences(pred)) {
                            return true;
                        }
                    }
                    return false;
                }

                private boolean foundReferences(IPredicate pred) {
                    if (pred == null) {
                        return false;
                    }

                    final AtomicBoolean hasReferences = new AtomicBoolean(false);

                    pred.accept(new DefaultPredicateVisitor() {
                        @Override
                        public void visit(IPredicateReference ref) {
                            hasReferences.set(true);
                        }
                    }, IPredicateVisitor.POSTORDER);

                    return hasReferences.get();
                }

                /**
                 * This method copies descriptor's fields into the dev
                 */
                private void syncToDescriptor( DomainObjectDescriptor descr ) {
                    assert descr != null;
                    name = descr.getName();
                    if (type == null) {
                        type = descr.getType();
                    }
                    description = descr.getDescription();
                    hidden = descr.isHidden();
                    if ( descr.getOwner() != null )  {
                        owner = descr.getOwner();
                    }
                    if ( descr.getStatus() != null ) {
                        status = descr.getStatus();
                    }
                    IAccessPolicy ap = descr.getAccessPolicy();
                    if ( ap != null ) {
                        dof.reset();
                        dof.formatAccessPolicy( ap );
                        apPql = dof.getPQL();
                    }
                }
                private final DomainObjectFormatter dof = new DomainObjectFormatter();
            }
        );

        this.hasDependencies = hasReferences.get();
    }

    /**
     * Returns the PQL content of the entity.
     * This package-private method is for use by Hibernate.
     * @return the PQL content of the entity.
     */
    String getPqlStr() {
        return pql;
    }

    /**
     * Changes the PQL content of the entity
     * without synchronizing the other fields.
     * This package-private method is for use by Hibernate.
     * @param pql the new PQL content of the entity.
     */
    void setPqlStr( String pql ) {
        if ( pql == null ) {
            throw new NullPointerException("pql");
        }
        this.pql = pql;
    }

    /**
     * Gets the date this entity was created.
     * @return the date this entity was created.
     */
    public Date getCreated() {
        return created;
    }
    
    /**
     * Changes the date this entity was created.
     * This method is package-scoped because it is intended for use
     * only by the LifecycleManager.
     *
     * @param when the date this entity was last updated.
     */
    void setCreated( Date when ) {
        created = UnmodifiableDate.forDate( when );
    }

    /**
     * Gets the type of this entity.
     * @return the type of this entity.
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Changes the type of this entity.
     * This is an internal method for hibernate's use.
     * @param type the new type of this entity.
     */
    void setType(EntityType type) {
        if ( type == null ) {
            throw new NullPointerException("type");
        }
        this.type = type;
    }

    /**
     * Gets whether or not this entity has dependencies. This is not settable, but is
     * determined from the pql
     */
    public boolean hasDependencies() {
        return hasDependencies;
    }

    /**
     * Hibernate-ified name for accessing hasDependencies
     */
    public TernaryType isHasDependencies() {
        return hasDependencies ? TernaryType.TRUE : TernaryType.FALSE;
    }

    /**
     * Only to be used by hibernate
     */
    public void setHasDependencies(TernaryType hasDependencies) {
        // Assume it has dependencies unless we know it doesn't
        this.hasDependencies = (hasDependencies != TernaryType.FALSE);
    }

    /**
     * Gets whether or not this entity has is a sub-policy. This is not settable, but is
     * determined from the pql
     */
    public boolean isASubPolicy() {
        return isSubPolicy;
    }

    /**
     * Used by hibernate
     */
    public TernaryType isSubPolicy() {
        return isSubPolicy ? TernaryType.TRUE : TernaryType.FALSE;
    }

    /**
     * Only to be used by hibernate
     */
    public void setSubPolicy(TernaryType isSubPolicy) {
        // It's a sub-policy only if we know it is
        this.isSubPolicy = (isSubPolicy == TernaryType.TRUE);
    }

    /**
     * Builds a new deployment entity for this development entity.
     * @param rec the <code>deploymentRecord</code> for which to deploy this entity.
     * @param asOf the date as of which the deployment entity is to be active.
     * @return a new deployment entity for this development entity.
     */
    public DeploymentEntity makeDeploymentEntity(DeploymentRecord rec, Date asOf) {
        String savedPql = getPqlWithoutAccessPolicy(pql);
        return new DeploymentEntity(
                name          // String name
              , description   // String description
              , savedPql      // String pql
              , rec           // DeploymentRecord deploymentRecord
              , this          // DevelopmentEntity devEntity
              , version       // int originalVersion
              , asOf          // Date asOf
              , hidden        // boolean hidden
              , lastModified  // Date lastModified
              , modifier      // Long lastModifier
              , submittedTime // Date submittedTime
              , submitter     // Long submitter
        );
    }

    /**
     * Builds a new deployment entity for this development entity.
     * @param rec the <code>deploymentRecord</code> for which to deploy this entity.
     * @param asOf the date as of which the deployment entity is to be active.
     * @return a new deployment entity for this development entity.
     */
    public DeploymentEntity makeTombstone(DeploymentRecord rec, Date asOf) {
        return new DeploymentEntity(
                name          // String name
              , description   // String description
              , rec           // DeploymentRecord deploymentRecord
              , this          // DevelopmentEntity devEntity
              , version       // int originalVersion
              , asOf          // Date asOf
              , hidden        // boolean hidden
              , lastModified  // Date lastModified
              , modifier      // Long lastModifier
              , submittedTime // Date submittedTime
              , submitter     // Long submitter
        );
    }


    /**
     * Obtains the ID of this entity.
     * @return the ID of this entity.
     */
    public Long getId() {
        return super.getId();
    }

    /**
     * Implements the Object.hashCode().
     */
    public int hashCode() {
        return ObjectHelper.nullSafeHashCode(
                id
              , owner
              , apPql
              , name
              , type
              , status
              , lastUpdated
              , version
              , pql
              , lastModified
              , modifier
              , submittedTime
              , submitter
        );
    }

    /**
     * Compares this object to another one.
     * @param otehr the object to be compared to this one.
     */
    public boolean equals( Object other ) {
        if (!(other instanceof DevelopmentEntity)) {
            return false;
        }
        // A convenient shortcut:
        if (other == this) {
            return true;
        }
        // The real check:
        DevelopmentEntity o = (DevelopmentEntity)other;
        return ObjectHelper.nullSafeEquals(id,            o.id)
            && ObjectHelper.nullSafeEquals(owner,         o.owner)
            && ObjectHelper.nullSafeEquals(apPql,         o.apPql)
            && ObjectHelper.nullSafeEquals(name,          o.name)
            && ObjectHelper.nullSafeEquals(type,          o.type)
            && ObjectHelper.nullSafeEquals(status,        o.status)
            && ObjectHelper.nullSafeEquals(lastUpdated,   o.lastUpdated)
            &&                             version     == o.version
            && ObjectHelper.nullSafeEquals(pql,           o.pql)
            && ObjectHelper.nullSafeEquals(lastModified,  o.lastModified)
            && ObjectHelper.nullSafeEquals(modifier,      o.modifier)
            && ObjectHelper.nullSafeEquals(submittedTime, o.submittedTime)
            && ObjectHelper.nullSafeEquals(submitter,     o.submitter)
        ;
    }

    /**
     * Returns the development status of this entity.
     * @return the development status of this entity.
     */
    public DevelopmentStatus getStatus() {
        return status;
    }

    /**
     * Sets the new development status for this entity.
     * @param status the new development status for this entity.
     */
    void setStatus(DevelopmentStatus status) {
        if (status == null) {
            throw new NullPointerException("status");
        }
        this.status = status;
    }

    /**
     * Gets the version of this entity.
     * @return the version of this entity.
     */
    public int getVersion() {
        return super.getVersion();
    }
    
    public Date getLastUpdated() {
        return lastUpdated;
    }

    void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Synchronizes hidden fields of this entity with fields of a saved entity.
     * @param saved a saved <code>DevelopmentEntity</code> with which to synchronize.
     */
    public void synchronizeWithSaved( DevelopmentEntity saved ) {
        //not very good 
        this.id            = saved.id;
        this.version       = saved.version;
        this.lastUpdated   = saved.lastUpdated;
        this.lastModified  = saved.lastModified;
        this.modifier      = saved.modifier;
        this.submittedTime = saved.submittedTime;
        this.submitter     = saved.submitter;
        this.created       = saved.created;
    }

    /**
     * The AllDeploymentEntities property is defined in the mapping
     * to enable joining.
     * This package-private method is for Hibernate.
     * @return allDeploymentRecords
     */
    Collection<DeploymentEntity> getAllDeploymentEntities() {
        return allDeploymentRecords;
    }

    /**
     * The AllDeploymentEntities property is defined in the mapping
     * to enable joining. Hibernate will push a lazy collection into it.
     * @param allRecords the <code>Collection</code> the Hibernate is
     * pushing into the entity. This value is stored and returned back
     * to Hibernate to avoid unintended deletions.
     */
    void setAllDeploymentEntities( Collection<DeploymentEntity> allRecords ) {
        allDeploymentRecords = allRecords;
    }

    private Collection<DeploymentEntity> allDeploymentRecords;

    private static String getPqlWithoutAccessPolicy(String pql) {

        DomainObjectBuilder dob = new DomainObjectBuilder(pql);
        DomainObjectBuilder.OneObjectVisitor v = new DomainObjectBuilder.OneObjectVisitor();

        try {
            dob.processInternalPQL(v);
        } catch (PQLException pe) {
            throw new AssertionError("Unexpected invalid PQL in DevelopmentEntity");
        }

        Object domainObject = null;

        if (v.getPolicy() != null) {
            IDPolicy policy = v.getPolicy();
            policy.setAccessPolicy(null);
            domainObject = policy;
        } else if (v.getSpec() != null) {
            IDSpec spec = v.getSpec();
            spec.setAccessPolicy(null);
            domainObject = spec;
        } else if (v.getLocation() != null) {
            return pql;
        } else {
            throw new AssertionError("Unexpected invalid PQL in DevelopmentEntity");
        }
        assert domainObject != null;

        DomainObjectFormatter dof = new DomainObjectFormatter();
        dof.formatDef(domainObject);
        return dof.getPQL();
    }

}
