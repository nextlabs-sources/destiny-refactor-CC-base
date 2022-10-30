package com.bluejungle.pf.destiny.lib;

import java.util.Comparator;
import java.util.Date;

import com.bluejungle.framework.utils.ObjectHelper;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;

/*
 * Created on Aug 31, 2009
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author Alan Morgan
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/DODDigest.java#1 $
 */

public class DODDigest {
    /** Represents the ID of the object. Can be null. */
    private Long id;

    /** Similar to the EntityType.  Made a string for ease of future expansion */
    private String type;

    /** Represents the name of the entity. Can be null. */
    private String name;

    /** Indicates that the descriptor should be accessible by the reader. */
    private boolean accessible;

    /** Represents the hibernate version of the corresponding entity. */
    private int version;

    /** Represents the destiny version of the corresponding entity. */
    private int destinyVersion;

    /** The name of the owner */
    private String ownerName;

    /** Represents unique id of the owner of the entity. Can be null. */
    private Long owner;
    
    /** The date the entity was last updated. */
    private Date lastUpdated;

    /** The date the entity was last modified. */
    private Date lastModified;

    /** The name of the last modifier of this entity */
    private String modifierName;

    /** The id of the last modifier of this entity */
    private Long modifier;
    
    /** The date the entity was submitted */
    private Date lastSubmitted;
    
    /** The name of the submitter */
    private String submitterName;
    
    /** The id of the submitter */
    private Long submitter;

    /** Development status */
    private DevelopmentStatus devStatus;

    /** Deployment status, etc of the entity */
    private DomainObjectUsage usageStatus;

    /** Used when the DOD is transmitted to the PolicyStudio only.  Indicates that the descriptor has dependencies */
    private boolean hasDependencies;

    /** Used when the DOD is transmitted to the PolicyStudio only.  Indicates that the entity is a sub-policy*/
    private boolean isSubPolicy;

    /** Caches the computed hashcode value.  -1 if not calculated yet */
    private int hashcode = -1;

    public DODDigest(
            Long id
          , String type
          , String name
          , boolean accessible
          , boolean hasDependencies
          , boolean isSubPolicy
          , int version
          , int destinyVersion
          , Long owner
          , String ownerName
          , Date lastUpdated
          , Date lastModified
          , Long modifier
          , String modifierName
          , Date lastSubmitted
          , Long submitter
          , String submitterName
          , DevelopmentStatus devStatus
          , DomainObjectUsage usageStatus
    ) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.accessible = accessible;
        this.hasDependencies = hasDependencies;
        this.isSubPolicy = isSubPolicy;
        this.version = version;
        this.destinyVersion = destinyVersion;
        this.owner = owner;
        this.ownerName = ownerName;
        this.lastUpdated = lastUpdated;
        this.lastModified = lastModified;
        this.modifier = modifier;
        this.modifierName = modifierName;
        this.lastSubmitted = lastSubmitted;
        this.submitter = submitter;
        this.submitterName = submitterName;
        this.devStatus = devStatus;
        this.usageStatus = usageStatus;
    }

    /**
     * Gets the id of the object
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the type of the object
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the name of the object
     */
    public String getName() {
        return name;
    }

    /**
     * Determines if the entity is accessible
     */
    public boolean isAccessible() {
        return accessible;
    }

    /**
     * Determines if the entity has dependencies
     */
    public boolean hasDependencies() {
        return hasDependencies;
    }

    /**
     * Determines if the entity is a subpolicy dependencies
     */
    public boolean isSubPolicy() {
        return isSubPolicy;
    }

    /**
     * Returns the id of the owner of the entity
     */
    public Long getOwnerId() {
        return owner;
    }

    /**
     * Returns the id of the owner of the entity
     */
    public String getOwnerName() {
        return ownerName;
    }
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    /**
     * Returns the most recent change date of the entity
     */
    public Date getLastModified() {
        return lastModified;
    }
    
    public String getModifierName() {
        return modifierName;
    }

    public Long getModifier() {
        return modifier;
    }
    
    public Date getLastSubmitted() {
        return lastSubmitted;
    }
    
    public String getSubmitterName() {
        return submitterName;
    }

    public Long getSubmitter() {
        return submitter;
    }

    /**
     * Returns the development status of the current version
     */
    public DevelopmentStatus getDevStatus() {
        return devStatus;
    }

    /**
     * Returns the usage status of the current version
     */
    public DomainObjectUsage getUsageStatus() {
        return usageStatus;
    }

    /**
     * Returns the version number
     */
    public int getVersion() {
        return version;
    }

    /**
     * Returns the destiny version number
     */
    public int getDestinyVersion() {
        return destinyVersion;
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg) {
        if (!(arg instanceof DODDigest)) {
            return false;
        }
        DODDigest digest = (DODDigest) arg;
        
        if ((getId() != null) && (digest.getId() != null)) {
            return getId().equals(digest.getId());
        }
        
        //why not access the field directly, will someone extend this class?
        
        return ObjectHelper.nullSafeEquals(getName(),          digest.getName())
            && ObjectHelper.nullSafeEquals(getType(),          digest.getType())
            && ObjectHelper.nullSafeEquals(getOwnerId(),       digest.getOwnerId())
            && ObjectHelper.nullSafeEquals(getOwnerName(),     digest.getOwnerName())
            && ObjectHelper.nullSafeEquals(getDevStatus(),     digest.getDevStatus())
            && (                           getVersion()     == digest.getVersion())
            && ObjectHelper.nullSafeEquals(getLastUpdated(),   digest.getLastUpdated())
            && ObjectHelper.nullSafeEquals(getLastModified(),  digest.getLastModified())
            && ObjectHelper.nullSafeEquals(getModifier(),      digest.getModifier())
            && ObjectHelper.nullSafeEquals(getModifierName(),  digest.getModifierName())
            && ObjectHelper.nullSafeEquals(getLastSubmitted(), digest.getLastSubmitted())
            && ObjectHelper.nullSafeEquals(getSubmitter(),     digest.getSubmitter())
            && ObjectHelper.nullSafeEquals(getSubmitterName(), digest.getSubmitterName())
        ;
    }
    
    public int hashCode() {
        if (hashcode != -1) {
            return hashcode;
        }

        if (getId() != null) {
            hashcode = getId().hashCode();
        } else {
            
            //why not access the field directly, will someone extend this class?
            
            hashcode = ObjectHelper.nullSafeHashCode(
                    getType()
                  , getName()
                  , isAccessible()
                  , hasDependencies()
                  , getVersion()
                  , getOwnerId()
                  , getOwnerName()
                  , getUsageStatus()
                  , getDevStatus()
                  , getLastUpdated()
                  , getLastModified()
                  , getModifier()
                  , getModifierName()
                  , getLastSubmitted()
                  , getSubmitter()
                  , getSubmitterName()
            );
        }

        return hashcode;
    }

    /**
     * Provides a <code>Comparator</code> for sorting descriptors.
     * Descriptors are sorted on their ordinal first (higher ordinals
     * are sorted ahead of lower ones to ensure that folders are sorted
     * higher than their content, i.e. policies). When the ordinals are
     * identical, names are compared case-insensitively.
     */
    static public Comparator<DODDigest> CASE_INSENSITIVE_COMPARATOR = new Comparator<DODDigest>() {
        public int compare(DODDigest lhs, DODDigest rhs) {
            String lhst = lhs.getType();
            String rhst = rhs.getType();
            if ( lhst == null || rhst == null ) {
                throw new NullPointerException("DODDigest.getType()");
            }
            if ( !lhst.equals(rhst) ) {
                return lhst.compareToIgnoreCase(rhst);
            } else {
                return lhs.getName().compareToIgnoreCase( rhs.getName() );
            }
        }
    };

}
