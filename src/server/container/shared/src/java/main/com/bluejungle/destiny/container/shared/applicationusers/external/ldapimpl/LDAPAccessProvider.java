/*
 * Created on Jul 6, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.external.ldapimpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.applicationusers.core.GroupComparatorImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.IUserSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.core.InitializationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.InvalidConfigurationException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserComparatorImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalUserAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser;
import com.bluejungle.destiny.container.shared.applicationusers.external.IGroupSearchSpec;
import com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider;
import com.bluejungle.destiny.container.shared.applicationusers.utils.ldap.LDAPQueryUtils;
import com.novell.ldap.LDAPBindHandler;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPReferralException;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.controls.LDAPSortControl;
import com.novell.ldap.controls.LDAPSortKey;
import com.novell.ldap.util.DN;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/external/ldapimpl/LDAPAccessProvider.java#1 $
 */

public class LDAPAccessProvider implements IUserAccessProvider {

    private static final Log LOG = LogFactory.getLog(LDAPAccessProvider.class);
    private static final int LDAP_SERVER_TIME_OUT = 300; // second
    private static final int LDAP_SEARCH_TIME_OUT = 300000; // milli-second

    private ILDAPAccessProviderConfiguration configuration;
    private LDAPConnection connection;
    private String domainName;
    private LDAPBindHandler bindHandlerForReferrals;

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#initialize(java.util.Properties)
     */
    public void initialize(String domainName, Properties properties) throws InvalidConfigurationException {
        if (domainName == null) {
            throw new NullPointerException("domain name is null");
        }
        if (properties == null) {
            throw new NullPointerException("properties is null");
        }

        this.domainName = domainName;
        this.configuration = PropertiesToConfigurationConverter.extractConfiguration(properties);
        this.bindHandlerForReferrals = new LDAPBindHandlerImpl(this.configuration.getLoginDN(), this.configuration.getLoginPwd());
        this.connection = new LDAPConnection();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getExternalUsers()
     */
    public SortedSet<IExternalUser> getExternalUsers(IUserSearchSpec[] searchSpecs, int maxResultsToReturn) throws ExternalUserAccessException {
        SortedSet<IExternalUser> allUsers = new TreeSet<IExternalUser>(new UserComparatorImpl());
        LDAPConnection connection = null;
        String lastNamesFilter = null;
        try {
            connection = getConnection();
            
            /* Fix for bug 2249, sorting on "SN" does not work well on AD when search filter is (sn=*) 
             * use sorting control only when user last name search spec is not empty   
             */
            if ( ( searchSpecs != null ) && ( searchSpecs[0] != null ) && 
                 ( ! searchSpecs[0].getLastNameStartsWith().equals("") ) ) {
                LDAPSortKey lastNameSortKey = new LDAPSortKey(this.configuration.getUserLastNameAttribute(), false);
                LDAPSortControl userSortControl = new LDAPSortControl(lastNameSortKey, false);
                LDAPSearchConstraints userSearchConstraints = connection.getSearchConstraints();
                userSearchConstraints.setControls(userSortControl);
                connection.setConstraints(userSearchConstraints);
            }

            // Combine the search specs into one query:
            if (searchSpecs != null) {
                Object[] searchValues = new Object[searchSpecs.length];
                for (int i = 0; i < searchSpecs.length; i++) {
                	String searchValue = searchSpecs[i].getLastNameStartsWith();
                	searchValues[i] = searchValue + "*";
                }
                lastNamesFilter = LDAPQueryUtils.generateCompoundSearchFilter(this.configuration.getUserLastNameAttribute(), searchValues);
            }
            lastNamesFilter = ((lastNamesFilter == null) || lastNamesFilter.equals("")) 
                    ? this.configuration.getUserSearchSpec() 
                    : "(&" + this.configuration.getUserSearchSpec() + lastNamesFilter + ")";

            // Execute query and ignore referral errors:
            LDAPSearchResults userResults = connection.search(
                    this.configuration.getRootDN()
                  , LDAPConnection.SCOPE_SUB
                  , lastNamesFilter
                  , new String[] { 
                        this.configuration.getUserFirstNameAttribute()
                      , this.configuration.getUserLastNameAttribute()
                      , this.configuration.getUserLoginAttribute() }
                  , false
                  , (LDAPSearchConstraints) null);
            boolean finishedIterating = false;
            while (!finishedIterating) {
                try {
                    // If maxResultsToReturn is <= 0, we don't enforce a bound.
                    // If > 0 we enforce that bound
                    int count = maxResultsToReturn <= 0 ? -1 : maxResultsToReturn;
                    while (userResults.hasMore() && (count != 0)) {
                        LDAPEntry userEntry = userResults.next();
                        IExternalUser user = new LDAPExternalUserImpl(this.domainName, userEntry, this.configuration);
                        allUsers.add(user);
                        count--;
                    }
                    finishedIterating = true;
                } catch (LDAPReferralException ignore) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unable to follow referral when retrieving external users. Ignoring and proceeding to next user.", ignore);
                    }
                } catch (InvalidEntryException ignore) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Encountered an entry that was invalid. Ignoring and proceeding to next user.", ignore);
                    }
                }
            }
        } catch (LDAPException e) {
            if (LDAPException.SIZE_LIMIT_EXCEEDED == e.getResultCode()) {
                // DO NOTHING - Gracefully exit for now
                // TODO: For Active Directory, we need a vendor-specific handler
                // to use the AD Paged Result Control.
                // (LDAP_PAGED_RESULT_OID_STRING)
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Sizelimit exceeded when querying for users from external domain: '" + this.domainName + "'. Returning the '" + allUsers.size() + "' entries that were gathered so far.", e);
                }
            } 
            else if (LDAPException.TIME_LIMIT_EXCEEDED == e.getResultCode()) {
                LOG.error("Timelimit exceeded when querying for users from external domain: '" + this.domainName + "filter: " + lastNamesFilter, e);
                throw new ExternalUserAccessException("Time limit exceeded during fetch users from external domain" + e);
            }
            else {
                throw new ExternalUserAccessException(e);
            }
        } finally {
            releaseConnection(connection);
        }
        return allUsers;
    }

    /**
     * Initializes the master connection instance. The pre-condition is that the
     * connection object already exists.
     */
    private void initializeConnection() throws LDAPException {
        // Create a connection:
        byte[] passwd = null;
        if (this.configuration.getLoginPwd() != null) {
            passwd = this.configuration.getLoginPwd().getBytes();
        }

        // Prepare security:
        if (this.configuration.isUsingSSL()) {
            LDAPSocketFactory sslSocketFactory = new LDAPJSSESecureSocketFactory();
            LDAPConnection.setSocketFactory(sslSocketFactory);
        }

        this.connection.connect(this.configuration.getServer(), this.configuration.getPort());
        this.connection.bind(LDAPConnection.LDAP_V3, this.configuration.getLoginDN(), passwd);
    }

    /**
     * Creates a connection
     * 
     * @return connection
     * @throws InitializationException
     */
    protected LDAPConnection getConnection() throws LDAPException {
        LDAPConnection newConnection = null;
        if (!this.connection.isConnected()) {
            initializeConnection();
        }
        newConnection = (LDAPConnection) this.connection.clone();

        // Enable referral handling:
        LDAPSearchConstraints searchConstraints = newConnection.getSearchConstraints();
        searchConstraints.setReferralFollowing(true);
        searchConstraints.setReferralHandler(this.bindHandlerForReferrals);
        searchConstraints.setMaxResults(0);
        searchConstraints.setTimeLimit(LDAP_SEARCH_TIME_OUT);
        searchConstraints.setServerTimeLimit(LDAP_SERVER_TIME_OUT);
        newConnection.setConstraints(searchConstraints);

        return newConnection;
    }

    /**
     * Releases the connection
     * 
     * @param connection
     */
    protected void releaseConnection(LDAPConnection connection) {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (LDAPException ignore) {
            }
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getUser(java.lang.String)
     */
    public IExternalUser getUser(String login) throws ExternalUserAccessException, UserNotFoundException {
        IExternalUser user = null;
        LDAPConnection connection = null;
        try {
            connection = getConnection();
            LDAPSearchResults userResults = connection.search(
                    this.configuration.getRootDN()
                  , LDAPConnection.SCOPE_SUB
                  , "(&" + this.configuration.getUserSearchSpec() + "(" + this.configuration.getUserLoginAttribute() + "=" + login + "))"
                  , new String[] {
                        this.configuration.getUserFirstNameAttribute()
                      , this.configuration.getUserLastNameAttribute()
                      , this.configuration.getUserLoginAttribute()
                      }
                  , false);
            if (!userResults.hasMore()) {
                throw new UserNotFoundException("User with login: '" + login + "' could not be found");
            }
            
            LDAPEntry userEntry = userResults.next();
            user = new LDAPExternalUserImpl(this.domainName, userEntry, this.configuration);
        } catch (LDAPException e) {
            throw new ExternalUserAccessException(e);
        } catch (InvalidEntryException e) {
            throw new ExternalUserAccessException("User with login: '" + login + "' exists but is not acceptable for import.", e);
        } finally {
            releaseConnection(connection);
        }
        return user;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getExternalGroups(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroupSearchSpec)
     */
    public SortedSet<IExternalGroup> getExternalGroups(IGroupSearchSpec[] searchSpecs) throws ExternalUserAccessException {
        if (searchSpecs == null) {
            throw new NullPointerException("search spec is null");
        }

        SortedSet<IExternalGroup> groups = new TreeSet<IExternalGroup>(new GroupComparatorImpl());
        LDAPConnection connection = null;
        try {
            connection = getConnection();

            // For each type of structural group, we issue a separate search:
            Collection<ILDAPStructuralGroupConfiguration> structuralGroupConfigs = this.configuration.getStructuralGroupConfigurations();
            for (ILDAPStructuralGroupConfiguration config : structuralGroupConfigs) {

                // Combine the search specs into one query:
                String groupTitlesFilter = null;
                if (searchSpecs != null) {
                    Object[] searchValues = new Object[searchSpecs.length];
                    for (int i = 0; i < searchSpecs.length; i++) {
                        searchValues[i] = searchSpecs[i].getTitleStartsWith() + "*";
                    }
                    groupTitlesFilter = LDAPQueryUtils.generateCompoundSearchFilter(config.getTitleAttribute(), searchValues);
                }
                groupTitlesFilter = ((groupTitlesFilter == null) || groupTitlesFilter.equals("")) 
                        ? config.getSearchSpec() 
                        : "(&" + config.getSearchSpec() + groupTitlesFilter + ")";

                // Execute query:
                LDAPSearchResults groupResults = connection.search(
                        this.configuration.getRootDN()
                      , LDAPConnection.SCOPE_SUB
                      , groupTitlesFilter
                      , null
                      , false
                      , (LDAPSearchConstraints) null);
                if (groupResults != null) {
                    boolean finishedIterating = false;
                    while (!finishedIterating) {
                        try {
                            while (groupResults.hasMore()) {
                                LDAPEntry groupEntry = groupResults.next();
                                IExternalGroup structuralGroup = new LDAPStructuralGroupImpl(groupEntry, this.domainName, this.configuration, config);
                                groups.add(structuralGroup);
                            }
                            finishedIterating = true;
                        } catch (LDAPReferralException ignore) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Unable to follow referral when retrieving external groups. Ignoring and proceeding to next group.", ignore);
                            }
                        } catch (InvalidEntryException ignore) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Encountered a group that was invalid. Ignoring and proceeding to next group.", ignore);
                            }
                        }
                    }
                }
            }

            // For each type of enumerated group, we issue a separate search:
            Collection<ILDAPEnumeratedGroupConfiguration> enumeratedGroupConfigs = this.configuration.getEnumeratedGroupConfigurations();
            for (ILDAPEnumeratedGroupConfiguration config : enumeratedGroupConfigs) {

                // Combine the search specs into one query:
                String groupTitlesFilter = null;
                if (searchSpecs != null) {
                    Object[] searchValues = new Object[searchSpecs.length];
                    for (int i = 0; i < searchSpecs.length; i++) {
                        searchValues[i] = searchSpecs[i].getTitleStartsWith() + "*";
                    }
                    groupTitlesFilter = LDAPQueryUtils.generateCompoundSearchFilter(config.getTitleAttribute(), searchValues);
                }
                groupTitlesFilter = ((groupTitlesFilter == null) || groupTitlesFilter.equals("")) 
                        ? config.getSearchSpec() 
                        : "(&" + config.getSearchSpec() + groupTitlesFilter + ")";

                // Execute query:
                LDAPSearchResults groupResults = connection.search(
                        this.configuration.getRootDN()
                      , LDAPConnection.SCOPE_SUB
                      , groupTitlesFilter
                      , null
                      , false
                      , (LDAPSearchConstraints) null);
                if (groupResults != null) {
                    boolean finishedIterating = false;
                    while (!finishedIterating) {
                        try {
                            while (groupResults.hasMore()) {
                                LDAPEntry groupEntry = groupResults.next();
                                IExternalGroup enumeratedGroup = new LDAPEnumeratedGroupImpl(groupEntry, this.domainName, this.configuration, config);
                                groups.add(enumeratedGroup);
                            }
                            finishedIterating = true;
                        } catch (LDAPReferralException ignore) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Unable to follow referral when retrieving external groups. Ignoring and proceeding to next group.", ignore);
                            }
                        } catch (InvalidEntryException ignore) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Encountered a group that was invalid. Ignoring and proceeding to next group.", ignore);
                            }
                        }
                    }
                }
            }

            // Sort the list of groups:
            //Collections.sort(groups);
        } catch (LDAPException e) {
            throw new ExternalUserAccessException(e);
        } finally {
            releaseConnection(connection);
        }
        return groups;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getExternalGroupsContainingUser(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalUser)
     */
    public Set<IExternalGroup> getExternalGroupsContainingUser(IExternalUser user) throws ExternalUserAccessException {
        if (user == null) {
            throw new NullPointerException("user is null");
        }

        Set<IExternalGroup> groups = new HashSet<IExternalGroup>();

        // First see if the containing structures count as "structural" groups:
        ILDAPExternalUser ldapUser = (ILDAPExternalUser) user;
        DN userDN = new DN(ldapUser.getDN());
        for (DN containerDN = userDN.getParent(); ((containerDN != null) && (containerDN.toString() != null)); containerDN = containerDN.getParent()) {
            try {
                IExternalGroup group = getExternalGroupWithDN(containerDN.toString());
                groups.add(group);
            } catch (GroupNotFoundException ignore) {
            } catch (ExternalUserAccessException ignore) {
            }
        }

        // Then check if any enumerated groups contain this user:
        groups.addAll(getEnumeratedGroupsContainingDN(ldapUser.getDN()));

        return groups;
    }

    /**
     * 
     * @param dn
     * @return
     * @throws ExternalUserAccessException
     */
    protected Set<ILDAPEnumeratedGroup> getEnumeratedGroupsContainingDN(String dn) throws ExternalUserAccessException {
        if (dn == null) {
            throw new NullPointerException("dn is null");
        }
        Set<ILDAPEnumeratedGroup> enumeratedGroups = new HashSet<ILDAPEnumeratedGroup>();

        // Retrieve any enumerated group that have this 'dn' as a member:
        LDAPConnection connection = null;
        try {
            connection = getConnection();
            getEnumeratedGroupsContainingDN(dn, connection, enumeratedGroups);
        } catch (LDAPException e) {
            throw new ExternalUserAccessException(e);
        } finally {
            releaseConnection(connection);
        }
        return enumeratedGroups;
    }

    private void getEnumeratedGroupsContainingDN(String dn, LDAPConnection conn, Set<ILDAPEnumeratedGroup> inputEnumeratedGroups)
        throws ExternalUserAccessException, LDAPException {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Recursive call: Obtaining Enumerated Group of DN: " + dn);
        }
        
        Collection<ILDAPEnumeratedGroupConfiguration> enumeratedGroupConfigs = this.configuration.getEnumeratedGroupConfigurations();
        for (ILDAPEnumeratedGroupConfiguration config : enumeratedGroupConfigs) {
            String membershipFilter = LDAPQueryUtils.generateStringSearchFilter(config.getMembershipAttribute(), dn);
            String enumeratedGroupFilter = "(&" + config.getSearchSpec() + membershipFilter + ")";
            LDAPSearchResults enumeratedGroupsResult = conn.search(
                    this.configuration.getRootDN()
                  , LDAPConnection.SCOPE_SUB
                  , enumeratedGroupFilter
                  , null
                  , false
                  , (LDAPSearchConstraints) null);
            if (enumeratedGroupsResult != null) {
                boolean finishedIterating = false;
                while (!finishedIterating) {
                    try {
                        while (enumeratedGroupsResult.hasMore()) {
                            LDAPEntry groupEntry = enumeratedGroupsResult.next();
                            ILDAPEnumeratedGroup group = new LDAPEnumeratedGroupImpl(
                                    groupEntry
                                  , this.domainName
                                  , this.configuration
                                  , config);

                            // Make sure we're not creating a cycle before recursively exploring upwards:
                            if (!inputEnumeratedGroups.contains(group)) {
                                inputEnumeratedGroups.add(group);
                                getEnumeratedGroupsContainingDN(
                                        group.getDistinguishedName()
                                      , conn
                                      , inputEnumeratedGroups);
                            }
                        }
                        finishedIterating = true;
                    } catch (LDAPReferralException ignore) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Unable to follow referral when retrieving external groups. Ignoring and proceeding to next group.", ignore);
                        }
                    } catch (InvalidEntryException ignore) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Encountered a group that was invalid. Ignoring and proceeding to next group.", ignore);
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     * Returns a map of dns to users for the array of dns provided. If a user
     * doesn't exist for a given dn, that dn is ignored. It is possible to tell
     * which dns didn't have corresponding users because those dns will not be
     * present in the returned map.
     * 
     * @param dn
     * @return map of dns to users
     * @throws ExternalUserAccessException
     * @throws UserNotFoundException
     */
    protected IExternalUser getExternalUserWithDN(String dn) throws ExternalUserAccessException, UserNotFoundException {
        if (dn == null) {
            throw new NullPointerException("dn is null");
        }

        IExternalUser userToReturn;
        LDAPConnection connection = null;
        try {
            connection = getConnection();
            LDAPSearchResults userResults = connection.search(
                    dn
                  , LDAPConnection.SCOPE_BASE
                  , this.configuration.getUserSearchSpec()
                  , new String[] { 
                        this.configuration.getUserFirstNameAttribute()
                      , this.configuration.getUserLastNameAttribute()
                      , this.configuration.getUserLoginAttribute()
                      }
                  , false);
            if ((userResults != null) && (userResults.hasMore())) {
                LDAPEntry userEntry = userResults.next();
                userToReturn = new LDAPExternalUserImpl(this.domainName, userEntry, this.configuration);
            } else {
                throw new UserNotFoundException("No user exists with dn: '" + dn + "'");
            }
        } catch (LDAPException e) {
            throw new ExternalUserAccessException(e);
        } catch (InvalidEntryException e) {
            throw new ExternalUserAccessException("User with dn: '" + dn + "' exists but is not acceptable for import.", e);
        } finally {
            releaseConnection(connection);
        }
        return userToReturn;
    }

    /**
     * Returns a group with the given dn, if one exists
     * 
     * @param dn
     * @return group
     * @throws ExternalUserAccessException
     * @throws GroupNotFoundException
     */
    protected IExternalGroup getExternalGroupWithDN(String dn) throws ExternalUserAccessException, GroupNotFoundException {
        if (dn == null) {
            throw new NullPointerException("dn is null");
        }

        IExternalGroup group = null;
        LDAPConnection connection = null;
        try {
            // Obtain and setup the connection with search controls:
            connection = getConnection();
            boolean bFound = false;

            // Retrieve any structural group with this dn, by searching with
            // base scope starting at 'dn':
            Collection<ILDAPStructuralGroupConfiguration> structuralGroupConfigs = this.configuration.getStructuralGroupConfigurations();
            for (ILDAPStructuralGroupConfiguration config : structuralGroupConfigs) {
                LDAPSearchResults structuralGroupsResult = connection.search(
                        dn
                      , LDAPConnection.SCOPE_BASE
                      , config.getSearchSpec()
                      , null
                      , false
                      , (LDAPSearchConstraints) null);
                if ((structuralGroupsResult != null) && (structuralGroupsResult.hasMore())) {
                    LDAPEntry groupEntry = structuralGroupsResult.next();
                    group = new LDAPStructuralGroupImpl(groupEntry, this.domainName, this.configuration, config);
                    bFound = true;
                }
            }

            // Retrieve any enumerated group with this dn, by searching with
            // base scope starting at 'dn':
            Collection<ILDAPEnumeratedGroupConfiguration> enumeratedGroupConfigs = this.configuration.getEnumeratedGroupConfigurations();
            for (ILDAPEnumeratedGroupConfiguration config : enumeratedGroupConfigs) {
                LDAPSearchResults enumeratedGroupsResult = connection.search(
                        dn
                      , LDAPConnection.SCOPE_BASE
                      , config.getSearchSpec()
                      , null
                      , false
                      , (LDAPSearchConstraints) null);
                if ((enumeratedGroupsResult != null) && (enumeratedGroupsResult.hasMore())) {
                    LDAPEntry groupEntry = enumeratedGroupsResult.next();
                    group = new LDAPEnumeratedGroupImpl(groupEntry, this.domainName, this.configuration, config);
                    bFound = true;
                }
            }

            if (bFound == false) {
                throw new GroupNotFoundException("No external group with dn: '" + dn + "' was found in external domain: '" + this.domainName + "'");
            }
        } catch (LDAPException e) {
            throw new ExternalUserAccessException(e);
        } catch (InvalidEntryException e) {
            throw new ExternalUserAccessException("Group with dn: '" + dn + "' exists but is not acceptable for import.", e);
        } finally {
            releaseConnection(connection);
        }
        return group;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getGroup(byte[])
     */
    public IExternalGroup getGroup(byte[] externalId) throws ExternalUserAccessException, GroupNotFoundException {
        if (externalId == null) {
            throw new NullPointerException("external id is null");
        }

        IExternalGroup group = null;
        LDAPConnection connection = null;
        try {
            // Obtain and setup the connection with search controls:
            connection = getConnection();
            String idSearchFilter = LDAPQueryUtils.generateBinarySearchFilter(this.configuration.getGloballyUniqueIdentifierAttribute(), externalId);

            boolean bFound = false;

            // Retrieve any structural group with this id:
            Collection<ILDAPStructuralGroupConfiguration> structuralGroupConfigs = this.configuration.getStructuralGroupConfigurations();
            for (ILDAPStructuralGroupConfiguration config : structuralGroupConfigs) {
                String structuralGroupFilter = "(&" + config.getSearchSpec() + idSearchFilter + ")";
                LDAPSearchResults structuralGroupsResult = connection.search(
                        this.configuration.getRootDN()
                      , LDAPConnection.SCOPE_SUB
                      , structuralGroupFilter
                      , null
                      , false
                      , (LDAPSearchConstraints) null);
                if ((structuralGroupsResult != null) && (structuralGroupsResult.hasMore())) {
                    LDAPEntry groupEntry = structuralGroupsResult.next();
                    group = new LDAPStructuralGroupImpl(groupEntry, this.domainName, this.configuration, config);
                    bFound = true;
                }
            }

            // Retrieve any enumerated group with this id:
            Collection<ILDAPEnumeratedGroupConfiguration> enumeratedGroupConfigs = this.configuration.getEnumeratedGroupConfigurations();
            for (ILDAPEnumeratedGroupConfiguration config : enumeratedGroupConfigs) {
                String enumeratedGroupFilter = "(&" + config.getSearchSpec() + idSearchFilter + ")";
                LDAPSearchResults enumeratedGroupsResult = connection.search(
                        this.configuration.getRootDN()
                      , LDAPConnection.SCOPE_SUB
                      , enumeratedGroupFilter
                      , null
                      , false
                      , (LDAPSearchConstraints) null);
                if ((enumeratedGroupsResult != null) && (enumeratedGroupsResult.hasMore())) {
                    LDAPEntry groupEntry = enumeratedGroupsResult.next();
                    group = new LDAPEnumeratedGroupImpl(groupEntry, this.domainName, this.configuration, config);
                    bFound = true;
                }
            }

            if (bFound == false) {
                throw new GroupNotFoundException("No external group with id: '" + externalId + "' was found in external domain: '" + this.domainName + "'");
            }
        } catch (LDAPException e) {
            throw new ExternalUserAccessException(e);
        } catch (InvalidEntryException e) {
            throw new ExternalUserAccessException("Group with externalID: '" + externalId + "' exists but is not acceptable for import.", e);
        } finally {
            releaseConnection(connection);
        }
        return group;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider#getUsersInExternalGroup(com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup)
     */
    public Set<IExternalUser> getUsersInExternalGroup(IExternalGroup group) throws ExternalUserAccessException {
        if (group == null) {
            throw new NullPointerException("group is null");
        }

        Set<IExternalUser> users;
        if (group instanceof ILDAPStructuralGroup) {
            users = getUsersInStructuralGroup((ILDAPStructuralGroup) group);
        } else {
            users = getUsersInEnumeratedGroup((ILDAPEnumeratedGroup) group);
        }
        return users;
    }

    /**
     * Returns the users in the given structural group, as well as the users in
     * any enumerated groups nested within the structural group
     * 
     * @param group
     * @return users
     * @throws ExternalUserAccessException
     */
    protected Set<IExternalUser> getUsersInStructuralGroup(ILDAPStructuralGroup group) throws ExternalUserAccessException {
        if (group == null) {
            throw new NullPointerException("group is null");
        }

        Set<IExternalUser> users = new HashSet<IExternalUser>();
        LDAPConnection connection = null;
        try {
            connection = getConnection();

            // First retrieve the users under the structural group:
            LDAPSearchResults usersUnderStructuralGroup = connection.search(
                    group.getDistinguishedName()
                  , LDAPConnection.SCOPE_SUB
                  , this.configuration.getUserSearchSpec()
                  , new String[] { 
                        this.configuration.getUserFirstNameAttribute()
                      , this.configuration.getUserLastNameAttribute()
                      , this.configuration.getUserLoginAttribute() 
                      }
                  , false
                  , (LDAPSearchConstraints) null);
            boolean finishedIterating = false;
            while (!finishedIterating) {
                try {
                    while (usersUnderStructuralGroup.hasMore()) {
                        LDAPEntry userEntry = usersUnderStructuralGroup.next();
                        IExternalUser user = new LDAPExternalUserImpl(this.domainName, userEntry, this.configuration);
                        users.add(user);
                    }
                    finishedIterating = true;
                } catch (LDAPReferralException ignore) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unable to follow referral when retrieving external users. Ignoring and proceeding to next user.", ignore);
                    }
                } catch (InvalidEntryException ignore) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Encountered an entry that was invalid. Ignoring and proceeding to next user.", ignore);
                    }
                }
            }

            // Then retrieve all enumerated groups under the structural group,
            // and get their members:
            Collection<ILDAPEnumeratedGroup> enumeratedGroups = getEnumeratedGroupsInStructuralGroup(group);
            for (ILDAPEnumeratedGroup enumGroup : enumeratedGroups) {
                users.addAll(getUsersInEnumeratedGroup(enumGroup));
            }
        } catch (LDAPException e) {
            throw new ExternalUserAccessException(e);
        } finally {
            releaseConnection(connection);
        }
        return users;
    }

    /**
     * Returns the enumerated groups under the given structural group
     * 
     * @param group
     * @return enumerated groups
     */
    protected Set<ILDAPEnumeratedGroup> getEnumeratedGroupsInStructuralGroup(ILDAPStructuralGroup group) throws ExternalUserAccessException {
        if (group == null) {
            throw new NullPointerException("group is null");
        }

        Set<ILDAPEnumeratedGroup> enumeratedGroups = new HashSet<ILDAPEnumeratedGroup>();

        // Retrieve any enumerated group that have this 'dn' as a member:
        LDAPConnection connection = null;
        try {
            connection = getConnection();

            Collection<ILDAPEnumeratedGroupConfiguration> enumeratedGroupConfigs = this.configuration.getEnumeratedGroupConfigurations();
            for (ILDAPEnumeratedGroupConfiguration config : enumeratedGroupConfigs) {
                String enumeratedGroupFilter = config.getSearchSpec();
                LDAPSearchResults enumeratedGroupsResult = connection.search(
                        group.getDistinguishedName()
                      , LDAPConnection.SCOPE_SUB
                      , enumeratedGroupFilter
                      , null
                      , false
                      , (LDAPSearchConstraints) null);
                if (enumeratedGroupsResult != null) {
                    boolean finishedIterating = false;
                    while (!finishedIterating) {
                        try {
                            while (enumeratedGroupsResult.hasMore()) {
                                LDAPEntry groupEntry = enumeratedGroupsResult.next();
                                ILDAPEnumeratedGroup enumGroup = new LDAPEnumeratedGroupImpl(groupEntry, this.domainName, this.configuration, config);
                                enumeratedGroups.add(enumGroup);
                            }
                            finishedIterating = true;
                        } catch (LDAPReferralException ignore) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Unable to follow referral when retrieving external groups. Ignoring and proceeding to next group.", ignore);
                            }
                        } catch (InvalidEntryException ignore) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Encountered a group that was invalid. Ignoring and proceeding to next group.", ignore);
                            }
                        }
                    }
                }
            }
        } catch (LDAPException e) {
            throw new ExternalUserAccessException(e);
        } finally {
            releaseConnection(connection);
        }

        return enumeratedGroups;
    }

    /**
     * Returns the users in the given enumerated group, and any enumerated
     * groups under it
     * 
     * @param group
     * @return
     */
    protected Set<IExternalUser> getUsersInEnumeratedGroup(ILDAPEnumeratedGroup group) throws ExternalUserAccessException {
        if (group == null) {
            throw new NullPointerException("group is null");
        }

        Set<IExternalUser> users = new HashSet<IExternalUser>();
        Set<ILDAPEnumeratedGroup> nestedGroups = new HashSet<ILDAPEnumeratedGroup>();

        String[] memberDNs = group.getMemberDNs();
        for (int i = 0; i < memberDNs.length; i++) {
            String dn = memberDNs[i];
            try {
                IExternalUser user = getExternalUserWithDN(dn);
                users.add(user);
            } catch (UserNotFoundException e) {
                try {
                    IExternalGroup nestedGroup = getExternalGroupWithDN(dn.toString());

                    // We only support enumerated groups being nested in an
                    // enumerated group. Structural group nesting is not
                    // supported due to performance impact/complexity:
                    if (nestedGroup instanceof ILDAPEnumeratedGroup) {
                        //Only explore this group if it hasn't already been
                        // explored:
                        if (!nestedGroups.contains(nestedGroup)) {
                            ILDAPEnumeratedGroup nestedEnumGroup = (ILDAPEnumeratedGroup) nestedGroup;
                            nestedGroups.add(nestedEnumGroup);
                            users.addAll(getUsersInEnumeratedGroup(nestedEnumGroup));
                        }
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Group with title: '" + nestedGroup.getTitle() + "' and dn: '" + nestedGroup.getDistinguishedName() + "' is a structural group nested as a member in the enumerated group :'" + group.getTitle() + "' with dn: '"
                                    + group.getDistinguishedName() + "'. Ignoring...");
                        }
                    }
                } catch (GroupNotFoundException ignore) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("The dn :'" + dn + "' in domain: '" + this.domainName + "' does not correspond to a recognized entry type. Proceeding to next member entry", ignore);
                    }
                }
            }
        }

        return users;
    }

    /**
     * set time limit to a LDAPConnection
     */
    public static void setDefaultTimeOut(LDAPConnection connection) {
        LDAPSearchConstraints constraint = connection.getSearchConstraints();    
        if ( constraint == null ) {
            //should never be here unless the setConstraints() set null constraint
            constraint = new LDAPSearchConstraints(
                    LDAP_SEARCH_TIME_OUT
                  , LDAP_SERVER_TIME_OUT
                  , LDAPSearchConstraints.DEREF_NEVER
                  , 0
                  , false
                  , 1
                  , null
                  ,10
            ); 
        }
        constraint.setServerTimeLimit(LDAP_SERVER_TIME_OUT);
        constraint.setTimeLimit(LDAP_SEARCH_TIME_OUT);
        connection.setConstraints(constraint);
    }

}
