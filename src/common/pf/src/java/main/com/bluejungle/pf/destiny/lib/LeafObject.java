package com.bluejungle.pf.destiny.lib;

import java.text.Collator;

import com.bluejungle.framework.utils.ObjectHelper;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author sergey
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lib/LeafObject.java#1 $
 */


/**
 * Objects of this class represent leaf objects of the system,
 * such as users, hosts, applications, and resources.
 * Each object is defined by its type and a configurable collection
 * of attributes accessed by name.
 */
public class LeafObject implements Comparable<LeafObject> {

    private static Collator collator = Collator.getInstance();
    
    /** Represents the type of this leaf object. */
    private final LeafObjectType type;

    /** The value of the name attribute. */
    private String name;

    /** The value of the unique name attribute. */
    private String uniqueName;

    /** The value of the UID attribute. */
    private String uid;

    /** The value of the ID attribute. */
    private Long id;

    /** The value of the domain name. */
    private String domainName;

    /**
     * Creates a new <code>LeafObject</code> of the given type.
     * @param type the type of this <code>EntityObject</code>.
     */
    public LeafObject( LeafObjectType type ) {
        this.type = type;
    }

    /**
     * Returns the type of this leaf object.
     * @return the type of this leaf object.
     */
    public LeafObjectType getType() {
        return type;
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId( Long id ) {
        this.id = id;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName( String name ) {
        this.name = name;
    }

    /**
     * @return Returns the uid.
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param uid The uid to set.
     */
    public void setUid( String uid ) {
        this.uid = uid;
    }

    /**
     * @return Returns the uniqueName.
     */
    public String getUniqueName() {
        return uniqueName;
    }

    /**
     * @param uniqueName The uniqueName to set.
     */
    public void setUniqueName( String uniqueName ) {
        this.uniqueName = uniqueName;
    }

    /**
     * @param uid The domain name to set.
     */
    public void setDomainName( String domainName ) {
        this.domainName = domainName;
    }

    /**
     * @return Returns the domainName.
     */
    public String getDomainName() {
        return domainName;
    }

    public boolean equals( Object other ) {
        // Take care of nulls and objects of wrong type
        if ( ! (other instanceof LeafObject ) ) {
            return false;
        }
        LeafObject rhs = (LeafObject)other;
        
        return ObjectHelper.nullSafeEquals(type,       rhs.getType())
            && ObjectHelper.nullSafeEquals(name,       rhs.name)
            && ObjectHelper.nullSafeEquals(uid,        rhs.uid)
            && ObjectHelper.nullSafeEquals(uniqueName, rhs.uniqueName)
            && ObjectHelper.nullSafeEquals(id,         rhs.id)
            && ObjectHelper.nullSafeEquals(domainName, rhs.domainName)
        ;
    }

    public int hashCode() {
        return ObjectHelper.nullSafeHashCode(type, name, id, uid, uniqueName, domainName);
    }

    /**
     * In order to sort leaf object by name in PA, implements compareTo()
     * by comparing the name value of two LeafSubject objects
     */
    public int compareTo(LeafObject otherObject) throws ClassCastException {
        if ( this.name == null ) {
            throw new NullPointerException("Leaf Object name is null");
        }
        return collator.compare(this.name, otherObject.getName() );
    }
}
