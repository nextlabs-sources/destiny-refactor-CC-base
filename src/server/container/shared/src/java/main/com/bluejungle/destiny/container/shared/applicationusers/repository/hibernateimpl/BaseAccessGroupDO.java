/*
 * Created on May 10, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2006 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author atian
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/AccessGroupDO.java#1 $
 */

public abstract class BaseAccessGroupDO implements IAccessGroup {
    private static final String EMPTY_ACCESS_CONTROL_DATA = "access_control ";
    /*static {
        // SDG - 4/24/06  Removing due to a build depedency problem.  Rather than fixing it, going with the safer solution of hard coding
        EMPTY_ACCESS_CONTROL_DATA = new AccessPolicyComponent().toPQL();
    }*/
    
    /** the ID of group */ 
    private Long id;

    /** the title of group */
    private String title;

    /** the description of group */
    private String description;

    /** the members of group */
    private Set<BaseApplicationUserDO> members = new HashSet<BaseApplicationUserDO>();

    /** the default access control pql string * */
    private String applicableAccessControl = EMPTY_ACCESS_CONTROL_DATA;

    /** The domain in which this group is contained * */
    private AccessDomainDO accessDomain;

//    private Set usersWithPrimaryAccessGroup;
    
    /**
     * Create an instance of BaseAccessGroupDO. For Hibernate Use Only!
     */
    BaseAccessGroupDO() {
    }

    /**
     * Create an instance of BaseAccessGroupDO
     * 
     * @param title
     * @param description
     * @param accessDomain
     */
    BaseAccessGroupDO(String title, String description, AccessDomainDO accessDomain) {
        if (title == null) {
            throw new NullPointerException("title cannot be null.");
        }

        if (description == null) {
            throw new NullPointerException("description cannot be null.");
        }

        if (accessDomain == null) {
            throw new NullPointerException("accessDomain cannot be null.");
        }

        this.title = title;
        this.description = description;
        this.accessDomain = accessDomain;
    }

    /**
     * access id attribute of group DO
     * 
     * @return the id of group
     */
    public Long getId() {
        return this.id;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup#getDestinyId()
     */
    public Long getDestinyId() {
        return this.id;
    }

    /**
     * access title attribute of Group DO
     * 
     * @return the title of group
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * set title attribute of Group DO
     * 
     * @param the
     *            title of group
     */
    public void setTitle(String title) {
        if (title == null) {
//          Workaround for Oracle empty string to null issue
            //throw new NullPointerException("title cannot be null.");
            title = "";
        }

        this.title = title;
    }

    /**
     * access description attribute of Group DO
     * 
     * @return the description of group
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * set description attribute of Group DO
     * 
     * @param the
     *            description of group
     */
    public void setDescription(String description) {
        if (description == null) {
            // Workaround for Oracle empty string to null issue
//            throw new NullPointerException("description cannot be null.");
            description = "";
        }

        this.description = description;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.core.IDomainEntity#getDomainName()
     */
    public String getDomainName() {
        return this.accessDomain.getName();
    }

    /**
     * Retrieve the applicationAccessControl.
     * 
     * @return the applicationAccessControl.
     */
    public String getApplicableAccessControl() {
        return this.applicableAccessControl;
    }

    /**
     * Set the applicationAccessControl
     * 
     * @param applicationAccessControl
     *            The applicationAccessControl to set.
     */
    public void setApplicableAccessControl(String applicationAccessControl) {
        if (applicationAccessControl == null) {
            throw new NullPointerException("applicationAccessControl cannot be null.");
        }

        this.applicableAccessControl = applicationAccessControl;
    }
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object otherGroup) {
        boolean valueToReturn = false;
        if ((otherGroup != null) && (otherGroup instanceof BaseAccessGroupDO)) {
            // Hibernate recommends not using ID, but I feel that it's the best
            // way to test equality. The reasons they provide are valid, but the
            // trade off of using the fields for equality is not worth it
            valueToReturn = this.getId().equals(((BaseAccessGroupDO) otherGroup).getId());
        }
        return valueToReturn;
    }
    
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.getId().hashCode();
    }

    /**
     * add member to a Group
     * 
     * @param the
     *            member of group
     */
    void addMember(BaseApplicationUserDO user) {
        if (user == null) {
            throw new NullPointerException("user cannot be null.");
        }

        this.members.add(user);
    }

    /**
     * Add a collection of members to this group
     * 
     * @param users
     *            the members to add
     */
    void addMembers(Collection<BaseApplicationUserDO> users) {
        if (users == null) {
            throw new NullPointerException("users cannot be null.");
        }
        this.members.addAll(users);
    }

    /**
     * delete member from a Group
     * 
     * @param the
     *            member of group
     */
    void deleteMember(BaseApplicationUserDO user) {
        if (user == null) {
            throw new NullPointerException("user cannot be null.");
        }

        this.members.remove(user);
    }

    /**
     * delete members from a Group
     * 
     * @param the
     *            members of group to delete
     */
    void deleteMembers(Collection<BaseApplicationUserDO> membersToDelete) {
        if (membersToDelete == null) {
            throw new NullPointerException("membersToDelete cannot be null.");
        }
        this.members.removeAll(membersToDelete);
    }

    /**
     * set id attribute of group DO. Required for Hibernate
     * 
     * @param the
     *            id of group
     */
    void setId(Long id) {
        if (id == null) {
            throw new NullPointerException("id cannot be null.");
        }

        this.id = id;
    }

    /**
     * access members of Group DO
     * 
     * @return the members of group
     */
    Set<BaseApplicationUserDO> getMembers() {
        return this.members;
    }
    
    /**
     * set members of Group DO. Required for Hibernate
     * 
     * @param the
     *            members of group
     */
    void setMembers(Set<BaseApplicationUserDO> members) {
        if (members == null) {
            throw new NullPointerException("members cannot be null.");
        }

        this.members = members;
    }

    /**
     * Retrieve the accessDomain. Required for Hibernate
     * 
     * @return the accessDomain.
     */
    AccessDomainDO getAccessDomain() {
        return this.accessDomain;
    }

    /**
     * Set the accessDomain. Required for Hibernate
     * 
     * @param accessDomain
     *            The accessDomain to set.
     */
    void setAccessDomain(AccessDomainDO accessDomain) {
        if (accessDomain == null) {
            throw new NullPointerException("accessDomain cannot be null.");
        }

        if (accessDomain == null) {
            throw new NullPointerException("accessDomain cannot be null.");
        }

        this.accessDomain = accessDomain;
    }
}
