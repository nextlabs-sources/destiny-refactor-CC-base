/*
 * Created on Nov 19, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dms;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryField;
import com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryTerm;
import com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryTermSet;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryField;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryTerm;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryTermSet;
import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettings;
import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettingsManager;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.ICustomizableActivityJournalingSettings;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.IUserProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IUserProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.ProfileNotFoundException;
import com.bluejungle.destiny.container.shared.profilemgr.UserProfileQueryField;
import com.bluejungle.destiny.container.shared.profilemgr.UserProfileQueryTerm;
import com.bluejungle.destiny.container.shared.profilemgr.UserProfileQueryTermSet;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.AgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.CommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.UserProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.service.ProfileServiceHelper;
import com.bluejungle.destiny.framework.types.CommitFault;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.TimeIntervalDTO;
import com.bluejungle.destiny.framework.types.UniqueConstraintViolationFault;
import com.bluejungle.destiny.framework.types.UnknownEntryFault;
import com.bluejungle.destiny.services.management.AgentProfileDTOQuery;
import com.bluejungle.destiny.services.management.CommProfileDTOQuery;
import com.bluejungle.destiny.services.management.ProfileServiceIF;
import com.bluejungle.destiny.services.management.UserProfileDTOQuery;
import com.bluejungle.destiny.services.management.types.ActivityJournalingSettingsDTO;
import com.bluejungle.destiny.services.management.types.ActivityJournalingSettingsDTOList;
import com.bluejungle.destiny.services.management.types.ActivityJournalingSettingsInfo;
import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.AgentProfileDTOList;
import com.bluejungle.destiny.services.management.types.AgentProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.AgentProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.AgentProfileDTOQueryTermSet;
import com.bluejungle.destiny.services.management.types.AgentProfileInfo;
import com.bluejungle.destiny.services.management.types.BaseProfileInfo;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTOList;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.CommProfileDTOQueryTermSet;
import com.bluejungle.destiny.services.management.types.CommProfileInfo;
import com.bluejungle.destiny.services.management.types.UserProfileDTO;
import com.bluejungle.destiny.services.management.types.UserProfileDTOList;
import com.bluejungle.destiny.services.management.types.UserProfileDTOQueryField;
import com.bluejungle.destiny.services.management.types.UserProfileDTOQueryTerm;
import com.bluejungle.destiny.services.management.types.UserProfileDTOQueryTermSet;
import com.bluejungle.destiny.services.management.types.UserProfileInfo;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.domain.types.ActionTypeDTO;
import com.bluejungle.domain.types.ActionTypeDTOList;
import com.bluejungle.domain.types.AgentTypeDTO;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UniqueConstraintViolationException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UnknownEntryException;
import com.bluejungle.framework.utils.TimeInterval;

/**
 * Implementation of the DMS profile service
 * 
 * @author sgoldstein
 * @version :ID:$
 */
public class DMSProfileServiceImpl implements ProfileServiceIF {

    private static final Log LOG = LogFactory.getLog(DMSProfileServiceImpl.class.getName());
    
    /**
     * Create an instance of the DMS Profile Manager Service implementation
     */
    public DMSProfileServiceImpl() {
        super();
    }

    public AgentProfileDTO addAgentProfile(AgentProfileInfo agentProfileToAdd) throws RemoteException, CommitFault, ServiceNotReadyFault, UniqueConstraintViolationFault {
        AgentProfileDTO agentProfileDTOToReturn = null;

        IAgentProfileData agentProfileData = new AgentProfileDataImpl(agentProfileToAdd);
        IProfileManager profileManager = getProfileManager();

        try {
            IAgentProfileDO agentProfileCreated = profileManager.createAgentProfile(agentProfileData);
            agentProfileDTOToReturn = ProfileServiceHelper.getInstance().getAgentProfileDTO(agentProfileCreated);
        } catch (UniqueConstraintViolationException exception) {
            throw new UniqueConstraintViolationFault(exception.getConstrainingFields());
        } catch (DataSourceException exception) {
            // FIX ME. Can we provide more info here?
            throw new CommitFault();
        }

        return agentProfileDTOToReturn;
    }

    public CommProfileDTO addCommProfile(CommProfileInfo commProfileToAdd) throws RemoteException, CommitFault, ServiceNotReadyFault, UniqueConstraintViolationFault {
        CommProfileDTO commProfileDTOToReturn = null;

        // If a journaling settings name is specified, try to retrieve it first
        IActivityJournalingSettings assignedSetting = null;
        String assignedJournalingSettingsName = commProfileToAdd.getAssignedActivityJournalingName();
        if (assignedJournalingSettingsName != null) {
            IActivityJournalingSettingsManager settingsManager = getActivityJournalingSettingsManager();
            IAgentManager agentManager = getAgentManager();
            IAgentType agentType = agentManager.getAgentType(commProfileToAdd.getAgentType().getValue());
            try {
                assignedSetting = settingsManager.getActivityJournalingSettings(assignedJournalingSettingsName, agentType);
            } catch (UnknownEntryException exception) {
                throw new UnknownEntryFault();
            } catch (DataSourceException exception) {
                // FIX ME. Can we provide more info here?
                throw new CommitFault();
            }
        }

        IProfileManager profileManager = getProfileManager();
        try {
            ICommProfileData commProfileData = new CommProfileDataImpl(commProfileToAdd);
            ICommProfileDO commProfileCreated = profileManager.createCommProfile(commProfileData);

            // Assign a journaling setting if one was specified
            Boolean isCustomJournalingSettingsAssigned = commProfileToAdd.getCustomActivityJournalingSettingsAssigned();
            if ((isCustomJournalingSettingsAssigned != null) && (isCustomJournalingSettingsAssigned.equals(Boolean.TRUE))) {
                assignedSetting = commProfileCreated.getCurrentJournalingSettings();
            }

            commProfileCreated.setCurrentJournalingSettings(assignedSetting);

            // Insert custom profile settings
            ActivityJournalingSettingsInfo customJournalingSettingsInfo = commProfileToAdd.getCustomActivityJournalingSettings();
            if (customJournalingSettingsInfo != null) {
                ActionTypeDTOList loggedActions = customJournalingSettingsInfo.getLoggedActivities();
                updateCustomJournalingSettings(commProfileCreated, loggedActions);
            }

            profileManager.updateCommProfile(commProfileCreated);

            commProfileDTOToReturn = ProfileServiceHelper.getInstance().getCommProfileDTO(commProfileCreated);
        } catch (UniqueConstraintViolationException exception) {
            throw new UniqueConstraintViolationFault(exception.getConstrainingFields());
        } catch (DataSourceException exception) {
            // FIX ME. Can we provide more info here?
            throw new CommitFault();
        }

        return commProfileDTOToReturn;
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#addUserProfile(com.bluejungle.destiny.services.management.types.UserProfileInfo)
     */
    public UserProfileDTO addUserProfile(UserProfileInfo userProfileToAdd) throws RemoteException, CommitFault, ServiceNotReadyFault, UniqueConstraintViolationFault {
        UserProfileDTO userProfileDTOToReturn = null;

        IUserProfileData userProfileData = new UserProfileDataImpl(userProfileToAdd);

        IProfileManager profileManager = getProfileManager();
        try {
            IUserProfileDO userProfileCreated = profileManager.createUserProfile(userProfileData);
            userProfileDTOToReturn = ProfileServiceHelper.getInstance().getUserProfileDTO(userProfileCreated);
        } catch (UniqueConstraintViolationException exception) {
            throw new UniqueConstraintViolationFault(exception.getConstrainingFields());
        } catch (DataSourceException exception) {
            // FIX ME. Can we provide more info here?
            throw new CommitFault();
        }

        return userProfileDTOToReturn;
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#updateAgentProfile(com.bluejungle.destiny.services.management.types.AgentProfileDTO)
     */
    public void updateAgentProfile(AgentProfileDTO profileInfoToUpdate) throws RemoteException, UnknownEntryFault, ServiceNotReadyFault {
        long agentProfileID = profileInfoToUpdate.getId();

        IProfileManager profileManager = getProfileManager();
        try {
            IAgentProfileDO agentProfileToUpdate = profileManager.retrieveAgentProfileByID(agentProfileID);
            agentProfileToUpdate.setName(profileInfoToUpdate.getName().toString());
            agentProfileToUpdate.setLogViewingEnabled(profileInfoToUpdate.isLogViewingEnabled());
            agentProfileToUpdate.setTrayIconEnabled(profileInfoToUpdate.isTrayIconEnabled());
            profileManager.updateAgentProfile(agentProfileToUpdate);
        } catch (ProfileNotFoundException exception) {
            throw new UnknownEntryFault();
        } catch (DataSourceException exception) {
            throw new CommitFault();
        }
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#updateCommProfile(com.bluejungle.destiny.services.management.types.CommProfileDTO)
     */
    public void updateCommProfile(CommProfileDTO profileInfoToUpdate) throws RemoteException, UnknownEntryFault, ServiceNotReadyFault {
        long commProfileID = profileInfoToUpdate.getId();

        IActivityJournalingSettings assignedSetting = null;
        ActivityJournalingSettingsDTO assignedJournalingSettings = profileInfoToUpdate.getCurrentActivityJournalingSettings();
        if (assignedJournalingSettings != null) {
            IActivityJournalingSettingsManager settingsManager = getActivityJournalingSettingsManager();
            try {
                IAgentManager agentManager = getAgentManager();
                IAgentType agentType = agentManager.getAgentType(profileInfoToUpdate.getAgentType().getValue());
                assignedSetting = settingsManager.getActivityJournalingSettings(assignedJournalingSettings.getName(), agentType);
            } catch (UnknownEntryException exception) {
                throw new UnknownEntryFault();
            } catch (DataSourceException exception) {
                // FIX ME. Can we provide more info here?
                throw new CommitFault();
            }
        }

        IProfileManager profileManager = getProfileManager();
        try {
            ICommProfileDO commProfileToUpdate = profileManager.retrieveCommProfileByID(commProfileID);
            commProfileToUpdate.setName(profileInfoToUpdate.getName().toString());
            try {
                commProfileToUpdate.setDABSLocation(new URI(profileInfoToUpdate.getDABSLocation().toString()));
            } catch (URISyntaxException exception) {
                // Shouldn't happen
                LOG.error("Failed to translate axis URI to java URI", exception);
            }
            TimeIntervalDTO heartBeatFrequency = profileInfoToUpdate.getHeartBeatFrequency();
            commProfileToUpdate.setHeartBeatFrequency(getTimeIntervalDO(heartBeatFrequency));
            commProfileToUpdate.setLogLimit(profileInfoToUpdate.getLogLimit().intValue());
            TimeIntervalDTO logFrequency = profileInfoToUpdate.getLogFrequency();
            commProfileToUpdate.setLogFrequency(getTimeIntervalDO(logFrequency));
            commProfileToUpdate.setDefaultPushPort(profileInfoToUpdate.getDefaultPushPort().intValue());
            commProfileToUpdate.setPushEnabled(profileInfoToUpdate.isPushEnabled());

            String password = profileInfoToUpdate.getPassword();
            if (password != null) {
                commProfileToUpdate.setPassword(password);
            }

            commProfileToUpdate.setCurrentJournalingSettings(assignedSetting);

            profileManager.updateCommProfile(commProfileToUpdate);

            ActivityJournalingSettingsDTO customSettingsDTO = profileInfoToUpdate.getCustomActivityJournalingSettings();
            ActionTypeDTOList loggedActionsList = customSettingsDTO.getLoggedActivities();
            updateCustomJournalingSettings(commProfileToUpdate, loggedActionsList);
        } catch (ProfileNotFoundException exception) {
            throw new UnknownEntryFault();
        } catch (DataSourceException exception) {
            throw new CommitFault();
        }
    }

    /**
     * @param commProfileToUpdate
     * @param loggedActionsList
     * @throws DataSourceException
     */
    private void updateCustomJournalingSettings(ICommProfileDO commProfileToUpdate, ActionTypeDTOList loggedActionsList) throws DataSourceException {
        ActionTypeDTO[] loggedActionsArray = loggedActionsList.getAction();
        Set loggedActions = new HashSet();
        if (loggedActionsArray != null) {
            for (int i = 0; i < loggedActionsArray.length; i++) {
                ActionTypeDTO nextActionDTO = loggedActionsArray[i];
                ActionEnumType nextAction = ActionEnumType.getActionEnum(nextActionDTO.getValue());
                loggedActions.add(nextAction);
            }
        }

        ICustomizableActivityJournalingSettings customSettings = commProfileToUpdate.getCustomJournalingSettings();
        customSettings.setLoggedActions(loggedActions);
        customSettings.save();
    }

    /**
     * Translate a TimeInterval DTO to a TimeInterval DO
     * 
     * @param timeInterval
     * @return
     */
    private TimeInterval getTimeIntervalDO(TimeIntervalDTO timeInterval) {
        int time = timeInterval.getTime().intValue();
        String timeUnit = timeInterval.getTimeUnit().toString();
        TimeInterval.TimeUnit translatedTimeUnit = TimeInterval.TimeUnit.getTimeUnit(timeUnit);
        return new TimeInterval(time, translatedTimeUnit);
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#updateUserProfile(com.bluejungle.destiny.services.management.types.UserProfileDTO)
     */
    public void updateUserProfile(UserProfileDTO userProfileDTOToUpdate) throws RemoteException, UnknownEntryFault, ServiceNotReadyFault {
        long userProfileID = userProfileDTOToUpdate.getId();

        IProfileManager profileManager = getProfileManager();
        try {
            IUserProfileDO userProfileToUpdate = profileManager.retrieveUserProfileByID(userProfileID);
            userProfileToUpdate.setName(userProfileDTOToUpdate.getName().toString());
            profileManager.updateUserProfile(userProfileToUpdate);
        } catch (ProfileNotFoundException exception) {
            throw new UnknownEntryFault();
        } catch (DataSourceException exception) {
            throw new CommitFault();
        }
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#getAgentProfiles(com.bluejungle.destiny.services.management.AgentProfileInfoQueryParams)
     */
    public AgentProfileDTOList getAgentProfiles(AgentProfileDTOQuery agentProfileQuery) throws RemoteException {
        AgentProfileQueryTermSet agentProfileQueryTermSet = buildAgentProfileQueryTermSet(agentProfileQuery);
        IAgentProfileDO fromResult = buildAgentProfileLastResult(agentProfileQuery);
        AgentProfileQueryField sortSpec = buildAgentProfileSortSpec(agentProfileQuery);

        IProfileManager profileManager = getProfileManager();
        try {
            List<? extends IAgentProfileDO> profilesRetrieved = profileManager.retrieveAgentProfiles(agentProfileQueryTermSet, fromResult, agentProfileQuery.getFetchSize(), sortSpec);

            return buildAgentProfileDTOList(profilesRetrieved);
        } catch (DataSourceException exception) {
            throw new CommitFault();
        }
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#getCommProfiles(com.bluejungle.destiny.services.management.CommProfileDTOQueryParams)
     */
    public CommProfileDTOList getCommProfiles(CommProfileDTOQuery commProfileDTOQuery) throws RemoteException, ServiceNotReadyFault {
        CommProfileQueryTermSet commProfileQueryTermSet = buildCommProfileQueryTermSet(commProfileDTOQuery);
        ICommProfileDO fromResult = buildCommProfileLastResult(commProfileDTOQuery);
        CommProfileQueryField sortSpec = buildCommProfileSortSpec(commProfileDTOQuery);

        IProfileManager profileManager = getProfileManager();

        try {
            List<? extends ICommProfileDO> profilesRetrieved = profileManager.retrieveCommProfiles(commProfileQueryTermSet, fromResult, commProfileDTOQuery.getFetchSize(), sortSpec);

            return buildCommProfileDTOList(profilesRetrieved);
        } catch (DataSourceException exception) {
            throw new CommitFault();
        }
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#getUserProfiles(com.bluejungle.destiny.services.management.UserProfileInfoQueryParams)
     */
    public UserProfileDTOList getUserProfiles(UserProfileDTOQuery userProfileQuery) throws RemoteException, ServiceNotReadyFault {
        UserProfileQueryTermSet userProfileQueryTermSet = buildUserProfileQueryTermSet(userProfileQuery);
        IUserProfileDO fromResult = buildUserProfileLastResult(userProfileQuery);
        UserProfileQueryField sortSpec = buildUserProfileSortSpec(userProfileQuery);

        IProfileManager profileManager = getProfileManager();

        try {
            List<? extends IUserProfileDO> profilesRetrieved = profileManager.retrieveUserProfiles(userProfileQueryTermSet, fromResult, userProfileQuery.getFetchSize(), sortSpec);

            return buildUserProfileDTOList(profilesRetrieved);
        } catch (DataSourceException exception) {
            throw new CommitFault();
        }
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#removeAgentProfile(long)
     */
    public void removeAgentProfile(long id) throws RemoteException, UnknownEntryFault, ServiceNotReadyFault {
        IProfileManager profileManager = getProfileManager();
        try {
            profileManager.deleteAgentProfile(new Long(id));
        } catch (DataSourceException exception) {
            // FIX ME. Can we provide more info here?
            throw new CommitFault();
        }
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#removeCommProfile(long)
     */
    public void removeCommProfile(long id) throws RemoteException, UnknownEntryFault, ServiceNotReadyFault {
        IProfileManager profileManager = getProfileManager();
        try {
            profileManager.deleteCommProfile(new Long(id));
        } catch (DataSourceException exception) {
            // FIX ME. Can we provide more info here?
            throw new CommitFault();
        }
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#removeUserProfile(long)
     */
    public void removeUserProfile(long id) throws RemoteException, UnknownEntryFault, ServiceNotReadyFault {
        IProfileManager profileManager = getProfileManager();
        try {
            profileManager.deleteUserProfile(new Long(id));
        } catch (DataSourceException exception) {
            // FIX ME. Can we provide more info here?
            throw new CommitFault();
        }
    }

    /**
     * @see com.bluejungle.destiny.services.management.ProfileServiceIF#getActivityJournalingSettings()
     */
    public ActivityJournalingSettingsDTOList getActivityJournalingSettings(String agentTypeId) throws RemoteException, ServiceNotReadyFault {
        IAgentManager agentManager = getAgentManager();
        IAgentType agentType = agentManager.getAgentType(agentTypeId);
        
        IActivityJournalingSettingsManager settingsManager = getActivityJournalingSettingsManager();

        try {
            List<IActivityJournalingSettings> settingsRetrieved = settingsManager.getActivityJournalSettings(agentType);
            return buildActivityJournalingSettingsDTOList(settingsRetrieved);
        } catch (DataSourceException exception) {
            throw new CommitFault();
        }
    }

    /**
     * Build an Agent Profile DTO List from the provided list of AgentProfileDO
     * instances
     * 
     * @param profilesToReturn
     * @param profilesRetrieved
     */
    private AgentProfileDTOList buildAgentProfileDTOList(List<? extends IAgentProfileDO> profilesRetrieved) {
        /**
         * Unfortunately, at this point, we either need to extend
         * services.types.AgentProfileDTOList or iterate through the list in
         * order to return the correct type I've chosen to iterate through the
         * list
         */
        ProfileServiceHelper serviceHelper = ProfileServiceHelper.getInstance();

        ArrayList<AgentProfileDTO> dtoResults = new ArrayList<AgentProfileDTO>();

        for (IAgentProfileDO profile : profilesRetrieved)
        {
            dtoResults.add(serviceHelper.getAgentProfileDTO(profile));
        }

        AgentProfileDTOList profilesToReturn = new AgentProfileDTOList();
        profilesToReturn.setAgentProfileDTO(dtoResults.toArray(new AgentProfileDTO[dtoResults.size()]));

        return profilesToReturn;
    }

    /**
     * Build an Comm Profile DTO List from the provided list of CommProfileDO
     * instances
     * 
     * @param profilesToReturn
     * @param profilesRetrieved
     */
    private CommProfileDTOList buildCommProfileDTOList(List<? extends ICommProfileDO> profilesRetrieved) {
        /**
         * Unfortunately, at this point, we either need to extend
         * services.types.CommProfileDTOList or iterate through the list in
         * order to return the correct type I've chosen to iterate through the
         * list
         */
        ProfileServiceHelper serviceHelper = ProfileServiceHelper.getInstance();

        ArrayList<CommProfileDTO> dtoResults = new ArrayList<CommProfileDTO>();

        for (ICommProfileDO profile : profilesRetrieved)
        {
            dtoResults.add(serviceHelper.getCommProfileDTO(profile));
        }

        CommProfileDTOList profilesToReturn = new CommProfileDTOList();
        profilesToReturn.setCommProfileDTO(dtoResults.toArray(new CommProfileDTO[dtoResults.size()]));

        return profilesToReturn;
    }

    /**
     * Build an User Profile DTO List from the provided list of UserProfileDO
     * instances
     * 
     * @param profilesToReturn
     * @param profilesRetrieved
     */
    private UserProfileDTOList buildUserProfileDTOList(List<? extends IUserProfileDO> profilesRetrieved) {
        /**
         * Unfortunately, at this point, we either need to extend
         * services.types.UserProfileDTOList or iterate through the list in
         * order to return the correct type I've chosen to iterate through the
         * list
         */
        ProfileServiceHelper serviceHelper = ProfileServiceHelper.getInstance();

        ArrayList<UserProfileDTO> dtoResults = new ArrayList<UserProfileDTO>();

        for (IUserProfileDO profile : profilesRetrieved)
        {
            dtoResults.add(serviceHelper.getUserProfileDTO(profile));
        }

        UserProfileDTOList profilesToReturn = new UserProfileDTOList();
        profilesToReturn.setUserProfileDTO(dtoResults.toArray(new UserProfileDTO[dtoResults.size()]));

        return profilesToReturn;
    }

    /**
     * Build an Activy Journaling Settings DTO List from the provided list of
     * IActivityJournalingSettings instances
     * 
     * @param settingToRetrieve
     * @return the created list
     */
    private ActivityJournalingSettingsDTOList buildActivityJournalingSettingsDTOList(List<IActivityJournalingSettings> settingsRetrieved) {
        /**
         * Unfortunately, at this point, we either need to extend
         * services.types.ActivityJournalingSettingsDTOList or iterate through
         * the list in order to return the correct type. I've chosen to iterate
         * through the list
         */
        ProfileServiceHelper serviceHelper = ProfileServiceHelper.getInstance();

        ArrayList<ActivityJournalingSettingsDTO> dtoResults = new ArrayList<ActivityJournalingSettingsDTO>();

        for (IActivityJournalingSettings setting : settingsRetrieved)
        {
            dtoResults.add(serviceHelper.getActivityJournalingSettingsDTO(setting));
        }

        ActivityJournalingSettingsDTOList settingsToReturn = new ActivityJournalingSettingsDTOList();
        settingsToReturn.setActivityJournalingSettings(dtoResults.toArray(new ActivityJournalingSettingsDTO[dtoResults.size()]));

        return settingsToReturn;

    }

    /**
     * Build an Agent Profile Sort spec using the provided AgentProfileDTOQuery
     * 
     * @param agentProfileQuery
     * @return an Agent Profile sort spec based on the provided
     *         AgentProfileDTOQuery
     */
    private AgentProfileQueryField buildAgentProfileSortSpec(AgentProfileDTOQuery agentProfileQuery) {
        AgentProfileQueryField sortSpec = null;
        AgentProfileDTOQueryField sortSpecInfo = agentProfileQuery.getSortField();
        if (sortSpecInfo != null) {
            sortSpec = new AgentProfileQueryField(sortSpecInfo.toString());
        }
        return sortSpec;
    }

    /**
     * Build an Comm Profile Sort spec using the provided CommProfileDTOQuery
     * 
     * @param commProfileQuery
     * @return a Comm Profile sort spec based on the provided
     *         CommProfileDTOQuery
     */
    private CommProfileQueryField buildCommProfileSortSpec(CommProfileDTOQuery commProfileQuery) {
        CommProfileQueryField sortSpec = null;
        CommProfileDTOQueryField sortSpecInfo = commProfileQuery.getSortField();
        if (sortSpecInfo != null) {
            sortSpec = new CommProfileQueryField(sortSpecInfo.toString());
        }
        return sortSpec;
    }

    /**
     * Build an User Profile Sort spec using the provided User ProfileDTOQuery
     * 
     * @param userProfileQuery
     * @return a User Profile sort spec based on the provided
     *         UserProfileDTOQuery
     */
    private UserProfileQueryField buildUserProfileSortSpec(UserProfileDTOQuery userProfileQuery) {
        UserProfileQueryField sortSpec = null;
        UserProfileDTOQueryField sortSpecInfo = userProfileQuery.getSortField();
        if (sortSpecInfo != null) {
            sortSpec = new UserProfileQueryField(sortSpecInfo.toString());
        }
        return sortSpec;
    }

    /**
     * Build the last result from a previous query based on the provided
     * AgentProfileDTOQuery instance
     * 
     * @param agentProfileQuery
     * @return
     */
    private IAgentProfileDO buildAgentProfileLastResult(AgentProfileDTOQuery agentProfileQuery) {
        IAgentProfileDO fromResult = null;
        AgentProfileDTO fromDTOResult = agentProfileQuery.getFromResult();
        if (fromDTOResult != null) {
            /**
             * FIX ME - We need to create an AgentProfileDO instance outside of
             * the profile manager. This is not ideal The way to solve this
             * would be to make AgentProfileDO an interface, allowing an
             * implementation to be created which is not persistent. This would
             * go against general design decisions previously made and there
             * isn't time to do this.
             */
            long fromDTOID = fromDTOResult.getId();
            Calendar fromDTOCreatedDate = fromDTOResult.getCreatedDate();
            Calendar fromDTOModifiedDate = fromDTOResult.getModifiedDate();
            String fromDTOResultName = fromDTOResult.getName().toString();
            boolean fromIsDefaultProfile = fromDTOResult.isDefaultProfile();
            boolean fromDTOResultLogViewingEnabled = fromDTOResult.isLogViewingEnabled();
            boolean fromDTOResultTrayIconEnabled = fromDTOResult.isTrayIconEnabled();

            // FIX ME - Should not have access to this!!!
            fromResult = new AgentProfileDO(new Long(fromDTOID), fromDTOResultName, fromIsDefaultProfile, fromDTOResultLogViewingEnabled, fromDTOResultTrayIconEnabled, fromDTOCreatedDate, fromDTOModifiedDate);
        }
        return fromResult;
    }

    /**
     * Build the last result from a previous query based on the provided
     * CommProfileDTOQuery instance
     * 
     * @param commProfileQuery
     * @return
     * @throws ServiceNotReadyFault 
     */
    private ICommProfileDO buildCommProfileLastResult(CommProfileDTOQuery commProfileQuery) throws ServiceNotReadyFault {
        ICommProfileDO fromResult = null;
        CommProfileDTO fromDTOResult = commProfileQuery.getFromResult();
        if (fromDTOResult != null) {
            /**
             * FIX ME - We need to create an CommProfileDO instance outside of
             * the profile manager.
             */
            long fromDTOID = fromDTOResult.getId();
            Calendar fromDTOCreatedDate = fromDTOResult.getCreatedDate();
            Calendar fromDTOModifiedDate = fromDTOResult.getModifiedDate();
            String fromDTOResultName = fromDTOResult.getName().toString();
            URI fromDTOResultDABSLocation = null;
            try {
                fromDTOResultDABSLocation = new URI(fromDTOResult.getDABSLocation().toString());
            } catch (URISyntaxException exception) {
                // Shouldn't happen
                LOG.warn("Failed to convert DABSLocation Axis URI to Java URI", exception);
            }

            String fromDTOAgentTypeString = fromDTOResult.getAgentType().toString();
            IAgentType fromDTOAgentType = this.getAgentManager().getAgentType(fromDTOAgentTypeString);

            com.bluejungle.framework.utils.TimeInterval fromDTOHeartBeatFrequency = getTimeIntervalDO(fromDTOResult.getHeartBeatFrequency());
            int fromDTOLogLimit = fromDTOResult.getLogLimit().intValue();
            com.bluejungle.framework.utils.TimeInterval fromDTOLogFrequency = getTimeIntervalDO(fromDTOResult.getLogFrequency());
            int fromDTODefaultPushPort = fromDTOResult.getDefaultPushPort().intValue();
            boolean fromDTOPushEnabled = fromDTOResult.isPushEnabled();
            boolean fromIsDefaultProfile = fromDTOResult.isDefaultProfile();
            byte[] fromDTOPasswordHash = fromDTOResult.getPasswordHash();

            // FIX ME -!!!!!!!!!!!!!!!!!!!!!!!! shouldn't have access to the DO
            // implementation
            fromResult = new CommProfileDO(new Long(fromDTOID), fromDTOResultName, fromIsDefaultProfile, fromDTOResultDABSLocation, fromDTOAgentType, fromDTOHeartBeatFrequency, fromDTOLogLimit, fromDTOLogFrequency, fromDTOPushEnabled,
                    fromDTODefaultPushPort, fromDTOPasswordHash, fromDTOCreatedDate, fromDTOModifiedDate);
        }
        return fromResult;
    }

    /**
     * Build the last result from a previous query based on the provided
     * UserProfileDTOQuery instance
     * 
     * @param userProfileQuery
     * @return
     */
    private IUserProfileDO buildUserProfileLastResult(UserProfileDTOQuery userProfileQuery) {
        IUserProfileDO fromResult = null;
        UserProfileDTO fromDTOResult = userProfileQuery.getFromResult();
        if (fromDTOResult != null) {
            /**
             * FIX ME - We need to create an UserProfileDO instance outside of
             * the profile manager. This is not ideal The way to solve this
             * would be to make UserProfileDO an interface, allowing an
             * implementation to be created which is not persistent. This would
             * go against general design decisions previously made and there
             * isn't time to do this.
             */
            long fromDTOID = fromDTOResult.getId();
            Calendar fromDTOCreatedDate = fromDTOResult.getCreatedDate();
            Calendar fromDTOModifiedDate = fromDTOResult.getModifiedDate();
            String fromDTOResultName = fromDTOResult.getName().toString();
            boolean fromIsDefaultProfile = fromDTOResult.isDefaultProfile();

            // FIX ME --------- Should haven't acces to this!!!!
            fromResult = new UserProfileDO(new Long(fromDTOID), fromDTOResultName, fromIsDefaultProfile, fromDTOCreatedDate, fromDTOModifiedDate);
        }
        return fromResult;
    }

    /**
     * Build an AgentProfileQueryTermSet based on the provided
     * AgentProfileDTOQuery
     * 
     * @param agentProfileQuery
     * @return
     */
    private AgentProfileQueryTermSet buildAgentProfileQueryTermSet(AgentProfileDTOQuery agentProfileQuery) {
        AgentProfileQueryTermSet agentProfileQueryTermSet = new AgentProfileQueryTermSet();

        AgentProfileDTOQueryTermSet agentProfileDTOQueryTermList = agentProfileQuery.getAgentProfileDTOQueryTermSet();
        if (agentProfileDTOQueryTermList != null) {
            AgentProfileDTOQueryTerm[] agentProfileQueryTerms = agentProfileDTOQueryTermList.getAgentProfileDTOQueryTerm();
            if (agentProfileQueryTerms != null) {
                for (int i = 0; i < agentProfileQueryTerms.length; i++) {
                    AgentProfileQueryField queryField = new AgentProfileQueryField(agentProfileQueryTerms[i].getAgentProfileDTOQueryField().toString());
                    Object queryFieldValue = agentProfileQueryTerms[i].getValue();
                    AgentProfileQueryTerm nextQueryTerm = new AgentProfileQueryTerm(queryField, queryFieldValue);
                    agentProfileQueryTermSet.addQueryTerm(nextQueryTerm);
                }
            }
        }
        return agentProfileQueryTermSet;
    }

    /**
     * Build an CommProfileQueryTermSet based on the provided
     * CommProfileDTOQuery
     * 
     * @param commProfileQuery
     * @return
     * @throws ServiceNotReadyFault 
     */
    private CommProfileQueryTermSet buildCommProfileQueryTermSet(CommProfileDTOQuery commProfileQuery) throws ServiceNotReadyFault {
        CommProfileQueryTermSet commProfileQueryTermSet = new CommProfileQueryTermSet();

        CommProfileDTOQueryTermSet commProfileDTOQueryTermList = commProfileQuery.getCommProfileDTOQueryTermSet();
        if (commProfileDTOQueryTermList != null) {
            CommProfileDTOQueryTerm[] commProfileQueryTerms = commProfileDTOQueryTermList.getCommProfileDTOQueryTerm();
            if (commProfileQueryTerms != null) {
                for (int i = 0; i < commProfileQueryTerms.length; i++) {
                    CommProfileQueryTerm nextQueryTerm = buildCommProfileQueryTerm(commProfileQueryTerms[i]);
                    commProfileQueryTermSet.addQueryTerm(nextQueryTerm);
                }
            }
        }
        return commProfileQueryTermSet;
    }

    /**
     * Build a CommProfileQueryTerm based on the provided
     * CommProfileDTOQueryTerm
     * 
     * @param inputQueryTerm
     * @return
     * @throws ServiceNotReadyFault 
     */
    private CommProfileQueryTerm buildCommProfileQueryTerm(CommProfileDTOQueryTerm inputQueryTerm) throws ServiceNotReadyFault {
        CommProfileQueryField queryField = new CommProfileQueryField(inputQueryTerm.getCommProfileDTOQueryField().toString());
        Object queryFieldValue = inputQueryTerm.getValue();

        // This is a bit of a hack, but I can't think of a good solution which
        // is not very time consuming
        if (queryFieldValue instanceof AgentTypeDTO) {
            AgentTypeDTO agentTypeValue = (AgentTypeDTO) queryFieldValue;
            queryFieldValue = getAgentManager().getAgentType(agentTypeValue.getValue());
        }
        CommProfileQueryTerm nextQueryTerm = new CommProfileQueryTerm(queryField, queryFieldValue);
        return nextQueryTerm;
    }

    /**
     * Build an UserProfileQueryTermSet based on the provided
     * UserProfileDTOQuery
     * 
     * @param userProfileQuery
     * @return
     */
    private UserProfileQueryTermSet buildUserProfileQueryTermSet(UserProfileDTOQuery userProfileQuery) {
        UserProfileQueryTermSet userProfileQueryTermSet = new UserProfileQueryTermSet();

        UserProfileDTOQueryTermSet userProfileDTOQueryTermList = userProfileQuery.getUserProfileDTOQueryTermSet();
        if (userProfileDTOQueryTermList != null) {
            UserProfileDTOQueryTerm[] userProfileQueryTerms = userProfileDTOQueryTermList.getUserProfileDTOQueryTerm();
            if (userProfileQueryTerms != null) {
                for (int i = 0; i < userProfileQueryTerms.length; i++) {
                    UserProfileQueryField queryField = new UserProfileQueryField(userProfileQueryTerms[i].getUserProfileDTOQueryField().toString());
                    Object queryFieldValue = userProfileQueryTerms[i].getValue();
                    UserProfileQueryTerm nextQueryTerm = new UserProfileQueryTerm(queryField, queryFieldValue);
                    userProfileQueryTermSet.addQueryTerm(nextQueryTerm);
                }
            }
        }
        return userProfileQueryTermSet;
    }

    /**
     * Retrieve the Profile Manager component
     * 
     * @return the Profile Manager component
     * @throws ServiceNotReadyFault
     *             if the Profile Manager is not available
     */
    private IProfileManager getProfileManager() throws ServiceNotReadyFault {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        if (!componentManager.isComponentRegistered(IProfileManager.COMP_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (IProfileManager) componentManager.getComponent(IProfileManager.COMP_NAME);
    }

    /**
     * Retrieve the Agent Manager component
     * 
     * @return the Profile Manager component
     * @throws ServiceNotReadyFault
     *             if the Agent Manager is not available
     */
    private IAgentManager getAgentManager() throws ServiceNotReadyFault {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        if (!componentManager.isComponentRegistered(IAgentManager.COMP_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (IAgentManager) componentManager.getComponent(IAgentManager.COMP_NAME);
    }
    
    /**
     * @return
     */
    private IActivityJournalingSettingsManager getActivityJournalingSettingsManager() throws ServiceNotReadyFault {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        if (!componentManager.isComponentRegistered(IActivityJournalingSettingsManager.COMP_NAME)) {
            throw new ServiceNotReadyFault();
        }

        return (IActivityJournalingSettingsManager) componentManager.getComponent(IActivityJournalingSettingsManager.COMP_NAME);
    }

    private class BaseProfileDataImpl {

        private BaseProfileInfo baseProfileInfo;

        private BaseProfileDataImpl(BaseProfileInfo baseProfileInfo) {
            if (baseProfileInfo == null) {
                throw new IllegalArgumentException("baseProfileInfo cannot be null");
            }

            this.baseProfileInfo = baseProfileInfo;
        }

        /**
         * Retrieve the name of the profile to create
         * 
         * @return the name of the profile to create
         */
        public String getName() {
            return this.baseProfileInfo.getName().toString();
        }
    }

    private class UserProfileDataImpl extends BaseProfileDataImpl implements IUserProfileData {

        /**
         * Constructor
         * 
         * @param userProfileInfo
         */
        public UserProfileDataImpl(UserProfileInfo userProfileInfo) {
            super(userProfileInfo);
        }
    }

    private class AgentProfileDataImpl extends BaseProfileDataImpl implements IAgentProfileData {

        private AgentProfileInfo agentProfileInfo;

        /**
         * Constructor
         * 
         * @param agentProfileInfo
         */
        public AgentProfileDataImpl(AgentProfileInfo agentProfileInfo) {
            super(agentProfileInfo);

            if (agentProfileInfo == null) {
                throw new IllegalArgumentException("agentProfileInfo cannot be null");
            }

            this.agentProfileInfo = agentProfileInfo;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData#isLogViewingEnabled()
         */
        public boolean isLogViewingEnabled() {
            return this.agentProfileInfo.isLogViewingEnabled();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData#isTrayIconEnabled()
         */
        public boolean isTrayIconEnabled() {
            return this.agentProfileInfo.isTrayIconEnabled();
        }
    }

    private class CommProfileDataImpl extends BaseProfileDataImpl implements ICommProfileData {

        private CommProfileInfo commProfileInfo;
        private IAgentType agentType;

        /**
         * Constructor
         * 
         * @param commProfileInfo
         * @throws ServiceNotReadyFault 
         */
        public CommProfileDataImpl(CommProfileInfo commProfileInfo) throws ServiceNotReadyFault {
            super(commProfileInfo);

            if (commProfileInfo == null) {
                throw new IllegalArgumentException("commProfileInfo cannot be null");
            }

            this.commProfileInfo = commProfileInfo;
            this.agentType = DMSProfileServiceImpl.this.getAgentManager().getAgentType(this.commProfileInfo.getAgentType().toString());
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getDABSLocation()
         */
        public URI getDABSLocation() {
            URI uriToReturn = null;

            try {
                uriToReturn = new URI(commProfileInfo.getDABSLocation().toString());
            } catch (URISyntaxException exception) {
                // This should never happen
                throw new IllegalArgumentException("Invalid DABS Location URI: " + commProfileInfo.getDABSLocation().toString());
            }

            return uriToReturn;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getHeartBeatFrequency()
         */
        public com.bluejungle.framework.utils.TimeInterval getHeartBeatFrequency() {
            return getTimeIntervalDO(this.commProfileInfo.getHeartBeatFrequency());
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getLogLimit()
         */
        public int getLogLimit() {
            return this.commProfileInfo.getLogLimit().intValue();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getLogFrequency()
         */
        public com.bluejungle.framework.utils.TimeInterval getLogFrequency() {
            return getTimeIntervalDO(this.commProfileInfo.getLogFrequency());
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getDefaultPushPort()
         */
        public int getDefaultPushPort() {
            return this.commProfileInfo.getDefaultPushPort().intValue();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#isPushEnabled()
         */
        public boolean isPushEnabled() {
            return this.commProfileInfo.getPushEnabled().booleanValue();
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getAgentType()
         */
        public IAgentType getAgentType() {
            return this.agentType;
        }

        /**
         * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getPassword()
         */
        public String getPassword() {
            return this.commProfileInfo.getPassword();
        }
    }
}
