/*
 * Created on Jun 30, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external;

import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;

/**
 * This class provides APIS to iterate over external users.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/IExternalUserAccessProvider.java#1 $
 */

public interface IUserAccessProvider {

    /**
     * Initialize with configuration properties
     * 
     * @param properties
     */
    public void initialize(String domainName, Properties properties) throws InvalidConfigurationException;

    /**
     * Returns a set of external users <IExternalUser>in this domain
     * 
     * @return set of external users <IExternalUser>
     */
    public SortedSet<IExternalUser> getExternalUsers(IUserSearchSpec[] searchSpec, int maxResultsToReturn) throws ExternalUserAccessException;

    /**
     * Retrieves an external user entry given its login name
     * 
     * @param login
     * @return user
     * @throws ExternalUserAccessException
     */
    public IExternalUser getUser(String login) throws ExternalUserAccessException, UserNotFoundException;

    /**
     * Returns a set of available external groups <IExternalGroup>
     * satisfying the provided search spec
     * 
     * @param searchSpec
     * @return set of external groups <IExternalGroup>
     */
    public SortedSet<IExternalGroup> getExternalGroups(IGroupSearchSpec[] searchSpec) throws ExternalUserAccessException;

    /**
     * Retrieves an external group entry given its external id
     * 
     * @param externalId
     * @return group
     */
    public IExternalGroup getGroup(byte[] externalId) throws ExternalUserAccessException, GroupNotFoundException;

    /**
     * Returns a set of external groups <IExternalGroup>containing the
     * given user
     * 
     * @param user
     * @return set of external groups
     * @throws ExternalUserAccessException
     */
    public Set<IExternalGroup> getExternalGroupsContainingUser(IExternalUser user) throws ExternalUserAccessException;

    /**
     * Returns a set of external users <IExternalUser>who are members of
     * the given exernal group
     * 
     * @param group
     * @return set of external users
     * @throws ExternalUserAccessException
     */
    public Set<IExternalUser> getUsersInExternalGroup(IExternalGroup group) throws ExternalUserAccessException;
}