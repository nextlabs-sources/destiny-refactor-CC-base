/*
 * Created on Jan 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr;

import java.util.List;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UniqueConstraintViolationException;

/**
 * The IProfileManager provides management services for Agent, User, and
 * Communication profiles.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/IProfileManager.java#3 $
 */
public interface IProfileManager {

    /**
     * <code>COMP_NAME</code> is the name of the Profile Manager component.
     */
    public static final String COMP_NAME = "profileMgr";

    /**
     * Create a User Profile with the provided name
     * 
     * @param userProfileData
     *            the data used to create the UserProfile Domain Object
     * @return The UserProfileDO created
     * @throws UniqueConstraintViolationException
     *             if a user profile with the specified name already exists
     * @throws DataSourceException
     *             if an error occurs while persisting the profile
     *  
     */
    public IUserProfileDO createUserProfile(IUserProfileData userProfileData) throws UniqueConstraintViolationException, DataSourceException;

    /**
     * Create an Agent Profile
     * 
     * @param agentProfileData
     *            the data used to create the AgentProfile Domain Object
     * @return The AgentProfileDO created
     * @throws DataSourceException
     *             if an error occurs while persisting the profile
     */
    public IAgentProfileDO createAgentProfile(IAgentProfileData agentProfileData) throws UniqueConstraintViolationException, DataSourceException;

    /**
     * Create a Communication Profile
     * 
     * FIX ME - Verify purpose of parameters for javadoc accuracy
     * 
     * 
     * @param commProfileData
     *            the data used to create the CommProfile Domain Object
     * @return The CommProfileDO created
     * @throws DataSourceException
     *             is an error occurs while persisting the profile
     */
    public ICommProfileDO createCommProfile(ICommProfileData commProfileData) throws UniqueConstraintViolationException, DataSourceException;

    /**
     * Update the specific agent profile.
     * 
     * @param agentProfileToUpdate
     *            the agent profile to update
     * @throws DataSourceException
     *             if an error occurs while persisting the profile
     */
    public void updateAgentProfile(IAgentProfileDO agentProfileToUpdate) throws DataSourceException;

    /**
     * Update the specific communication profile.
     * 
     * @param commProfileToUpdate
     *            the communication profile to update
     * @throws DataSourceException
     *             if an error occurs while persisting the profile
     */
    public void updateCommProfile(ICommProfileDO comProfileToUpdate) throws DataSourceException;

    /**
     * Update the specific user profile.
     * 
     * @param userProfileToUpdate
     *            the userprofile to update
     * @throws DataSourceException
     *             if an error occurs while persisting the profile
     */
    public void updateUserProfile(IUserProfileDO userProfileToUpdate) throws DataSourceException;

    /**
     * Retrieve the default agent profile
     * 
     * @return the default agent profile
     * @throws DataSourceException
     *             if an error occurs while reading the default agent profile
     */
    public IAgentProfileDO getDefaultAgentProfile() throws DataSourceException;

    /**
     * Retrieve the default comm profile
     * 
     * @return the default comm profile
     * @param agentType the type of agent to associated with the comm profile
     * @throws DataSourceException
     *             if an error occurs while reading the default comm profile
     */
    public ICommProfileDO getDefaultCommProfile(IAgentType agentType) throws DataSourceException;

    // For future use
    /**
     * Retrieve the default user profile
     * 
     * @return the default user profile
     * @throws DataSourceException
     *             if an error occurs while reading the default user profile
     */
    //public UserProfileDO getDefaultUserProfile() throws DataSourceException;
    /**
     * Retrieve a User Profile by name
     * 
     * @param userProfileName
     *            the name of the user profile to retrieve
     * @return The UserProfileDO with the associated name
     * @throws DataSourceException
     *             if an error occurs while attempting to read the user profile
     *             from the database
     * @throws ProfileNotFoundException
     *             if a user profile with the specified name could not be found
     */
    public IUserProfileDO retrieveUserProfileByName(String userProfileName) throws DataSourceException, ProfileNotFoundException;

    /**
     * Retrieve a Communication Profile by name
     * 
     * @param commProfileName
     *            the name of the comm profile to retrieve
     * @return The CommProfileDO with the associated name
     * @throws DataSourceException
     *             if an error occurs while attempting to read the comm profile
     *             from the database
     * @throws ProfileNotFoundException
     *             if a comm profile with the specified name could not be found
     */
    public ICommProfileDO retrieveCommProfileByName(String commProfileName) throws DataSourceException, ProfileNotFoundException;

    /**
     * Retrieve a Agent Profile by name
     * 
     * @param agentProfileName
     *            the name of the agent profile to retrieve
     * @return The AgentProfileDO with the associated name
     * @throws DataSourceException
     *             if an error occurs while attempting to read the agent profile
     *             from the database
     * @throws ProfileNotFoundException
     *             if a agent profile with the specified name could not be found
     */
    public IAgentProfileDO retrieveAgentProfileByName(String agentProfileName) throws DataSourceException, ProfileNotFoundException;

    /**
     * Retrieve the user profile by id
     * 
     * @param id
     *            the ID of the user profile to retrieve
     * @return The UserProfileDO with the associated id
     * @throws DataSourceException
     *             if an error occurs while attempting to read the user profile
     *             from the database
     * @throws ProfileNotFoundException
     *             if a user profile with the specified id could not be found
     */
    public IUserProfileDO retrieveUserProfileByID(long id) throws DataSourceException, ProfileNotFoundException;

    /**
     * Retrieve the communication profile by id
     * 
     * @param id
     *            the ID of the comm profile to retrieve
     * @return The CommProfileDO with the associated id
     * @throws DataSourceException
     *             if an error occurs while attempting to read the comm profile
     *             from the database
     * @throws ProfileNotFoundException
     *             if a comm profile with the specified id could not be found
     */
    public ICommProfileDO retrieveCommProfileByID(long id) throws DataSourceException, ProfileNotFoundException;

    /**
     * Retrieve the agent profile by id
     * 
     * @param id
     *            the ID of the agent profile to retrieve
     * @return The AgentProfileDO with the associated id
     * @throws DataSourceException
     *             if an error occurs while attempting to read the agent profile
     *             from the database
     * @throws ProfileNotFoundException
     *             if a agent profile with the specified id could not be found
     */
    public IAgentProfileDO retrieveAgentProfileByID(long id) throws DataSourceException, ProfileNotFoundException;

    /**
     * Retrieve a list of AgentProfiles which match the provided query
     * 
     * @param agentProfileQueryTerms
     *            the query terms of the query
     * @param lastResult
     *            when retrieving a list of results a page at a time, the last
     *            result of the previous page retrieved
     * @param fetchSize
     *            the number of results to retrieve
     * @param sortProperty
     *            the property by which to sort the results
     * @return a list of AgentProfileDO's which match the provided query
     * @throws DataSourceException
     *             if an error occurs while querying the database
     */
    public List<? extends IAgentProfileDO> retrieveAgentProfiles(AgentProfileQueryTermSet agentProfileQueryTerms, IAgentProfileDO lastResult, int fetchSize, AgentProfileQueryField sortProperty) throws DataSourceException;

    /**
     * Retrieve a list of CommProfiles which match the provided query
     * 
     * @param commProfileQueryTerms
     *            the query terms of the query
     * @param lastResult
     *            when retrieving a list of results a page at a time, the last
     *            result of the previous page retrieved
     * @param fetchSize
     *            the number of results to retrieve
     * @param sortProperty
     *            the property by which to sort the results
     * @return a list of CommProfileDO's which match the provided query
     * @throws DataSourceException
     *             if an error occurs while querying the database
     */
    public List<? extends ICommProfileDO> retrieveCommProfiles(CommProfileQueryTermSet commProfileQueryTerms, ICommProfileDO lastResult, int fetchSize, CommProfileQueryField sortProperty) throws DataSourceException;

    /**
     * Retrieve a list of UserProfiles which match the provided query
     * 
     * @param userProfileQueryTerms
     *            the query terms of the query
     * @param lastResult
     *            when retrieving a list of results a page at a time, the last
     *            result of the previous page retrieved
     * @param fetchSize
     *            the number of results to retrieve
     * @param sortProperty
     *            the property by which to sort the results
     * @return a list of UserProfileDO's which match the provided query
     * @throws DataSourceException
     *             if an error occurs while querying the database
     */
    public List<? extends IUserProfileDO> retrieveUserProfiles(UserProfileQueryTermSet userProfileQueryTerms, IUserProfileDO lastResult, int fetchSize, UserProfileQueryField sortProperty) throws DataSourceException;

    /**
     * Delete the agent profile with the specified name
     * 
     * @param id
     *            the id of the agent profile to delete
     * @throws DataSourceException
     *             if a persistence error occurs while deleting the profile
     */
    public void deleteAgentProfile(Long id) throws DataSourceException;

    /**
     * Delete the comm profile with the specified name
     * 
     * @param id
     *            the id of the comm profile to delete
     * @throws DataSourceException
     *             if a persistence error occurs while deleting the profile
     */
    public void deleteCommProfile(Long id) throws DataSourceException;

    /**
     * Delete the userprofile with the specified name
     * 
     * @param id
     *            the id of the user profile to delete
     * @throws DataSourceException
     *             if a persistence error occurs while deleting the profile
     */
    public void deleteUserProfile(Long id) throws DataSourceException;
}
