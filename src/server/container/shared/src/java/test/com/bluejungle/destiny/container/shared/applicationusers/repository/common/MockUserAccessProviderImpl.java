/*
 * Created on Sep 19, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.common;

import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.bluejungle.destiny.container.shared.applicationusers.core.GroupComparatorImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserComparatorImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalUserAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider;

/**
 * This class has only one group and one user in that group
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/applicationusers/repository/openldapimpl/MockUserAccessProviderImpl.java#2 $
 */

public class MockUserAccessProviderImpl implements IUserAccessProvider {

    public int nCallsToGetGroupById = 0;
    public int nCallsToGetGroupMembers = 0;

    private MockExternalGroupImpl singletonGroup;
    private MockExternalUserImpl singletonUserInGroup;

    /**
     * Constructor
     *  
     */
    public MockUserAccessProviderImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#initialize(java.lang.String,
     *      java.util.Properties)
     */
    public void initialize(String domainName, Properties properties) throws InvalidConfigurationException {
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getExternalUsers(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUserSearchSpec)
     */
    public SortedSet getExternalUsers(IUserSearchSpec[] searchSpec, int maxResultsToReturn) throws ExternalUserAccessException {
        SortedSet set = new TreeSet(new UserComparatorImpl());
        if (this.singletonUserInGroup != null) {
            set.add(this.singletonUserInGroup);
        }
        return set;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getUser(java.lang.String)
     */
    public IExternalUser getUser(String login) throws ExternalUserAccessException, UserNotFoundException {
        if ((this.singletonUserInGroup == null) || (!this.singletonUserInGroup.getLogin().equals(login))) {
            throw new UserNotFoundException();
        }
        return this.singletonUserInGroup;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getExternalGroups(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupSearchSpec)
     */
    public SortedSet getExternalGroups(IGroupSearchSpec[] searchSpec) throws ExternalUserAccessException {
        SortedSet set = new TreeSet(new GroupComparatorImpl());
        if (this.singletonGroup != null) {
            set.add(this.singletonGroup);
        }
        return set;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getGroup(byte[])
     */
    public IExternalGroup getGroup(byte[] externalId) throws ExternalUserAccessException, GroupNotFoundException {
        this.nCallsToGetGroupById++;
        if ((this.singletonGroup == null) || (!Arrays.equals(this.singletonGroup.getExternalId(), externalId))) {
            throw new GroupNotFoundException();
        }
        return this.singletonGroup;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getExternalGroupsContainingUser(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser)
     */
    public Set getExternalGroupsContainingUser(IExternalUser user) throws ExternalUserAccessException {
        SortedSet groups = new TreeSet(new GroupComparatorImpl());
        if ((this.singletonUserInGroup != null) && (this.singletonUserInGroup.equals(user))) {
            groups.add(this.singletonGroup);
        }
        return groups;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getUsersInExternalGroup(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup)
     */
    public Set getUsersInExternalGroup(IExternalGroup group) throws ExternalUserAccessException {
        this.nCallsToGetGroupMembers++;
        SortedSet users = new TreeSet(new UserComparatorImpl());
        if ((this.singletonGroup != null) && (this.singletonGroup.equals(group))) {
            users.add(this.singletonUserInGroup);
        }
        return users;
    }

    public void setSingletonGroup(MockExternalGroupImpl group) {
        this.singletonGroup = group;
    }

    public void setSingletonUserInGroup(MockExternalUserImpl user) {
        this.singletonUserInGroup = user;
    }
}