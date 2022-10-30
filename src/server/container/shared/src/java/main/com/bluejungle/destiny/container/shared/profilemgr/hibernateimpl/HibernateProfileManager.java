/*
 * Created on Jan 7, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.AgentMgrQueryFieldType;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentDO;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentMgrQueryTermFactory;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentQueryResults;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.InvalidQuerySpecException;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentQuerySpecImpl;
import com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryField;
import com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryTerm;
import com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryTermSet;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryField;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryTerm;
import com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryTermSet;
import com.bluejungle.destiny.container.shared.profilemgr.IActivityJournalingSettingsManager;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.IBaseProfileDO;
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
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.datastore.hibernate.exceptions.UniqueConstraintViolationException;
import com.bluejungle.framework.datastore.hibernate.paging.HibernateLastResultBasedPagingHelper;
import com.bluejungle.framework.search.RelationalOp;
import com.bluejungle.framework.utils.TimeInterval;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.expression.Expression;

import org.apache.commons.logging.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// FIX ME - Verify the default profile is not manipulated in a way it shouldn't
// be (e.g. deleted)
/**
 * An implementation of the IProfileManager interface which uses Hibernate to
 * persist profiles.
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/profilemgr/HibernateProfileManager.java#1 $
 */
public class HibernateProfileManager implements IProfileManager, ILogEnabled, IInitializable, IManagerEnabled {

    /**
     * Key used to retrieve the default agent profile id from the seed data map
     */
    private static final String DEFAULT_AGENT_PROFILE_KEY_NAME = "DefaultAgentProfile";

    /**
     * Key used to retrieve the default desktop agent comm profile id from the
     * seed data map
     */
    private static final String DEFAULT_COMM_PROFILE_KEY_NAME_PREFIX = "DefaultCommProfile_";

    /**
     * Key used to retrieve the DABS location from the seed data config
     * properties
     */
    private static final String DABS_LOCATION_SEED_CONFIG_KEY_NAME = "DABSLocation";

    /**
     * Key used to retrieve the DDAC location from the seed data config
     * properties
     */
    private static final String DDAC_LOCATION_SEED_CONFIG_KEY_NAME = "DDACLocation";

    /**
     * Key used to retrieve the temper proofing password from the seed data
     * config properties
     */
    private static final String PASSWORD_SEED_CONFIG_KEY_NAME = "tamperProofingPassword";

    private static final String USER_PROFILE_DELETE_BY_ID_QUERY = "from UserProfileDO as user_profile where user_profile.id = ?";
    private static final String COMM_PROFILE_DELETE_BY_ID_QUERY = "from CommProfileDO as comm_profile where comm_profile.id = ?";
    private static final String AGENT_PROFILE_DELETE_BY_ID_QUERY = "from AgentProfileDO as agent_profile where agent_profile.id = ?";
    private static final String CUSTOM_JOURNALING_SETTINGS_NAME_DELIMETER = ":";

    private static final String[] COMM_PROFILE_NAME_AND_TYPE_PROPERTIES = { "name", "agentType" };
    private Long defaultAgentProfileID;
    private Map<IAgentType, Long> defaultCommProfileIds = new HashMap<IAgentType, Long>();

    private IAgentManager agentManager;
    private IComponentManager compManager;
    private Log logger;

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#createUserProfile(java.lang.String)
     */
    public IUserProfileDO createUserProfile(IUserProfileData userProfileData) throws UniqueConstraintViolationException, DataSourceException {
        if (userProfileData == null) {
            throw new IllegalArgumentException("userProfileData cannot be null.");
        }

        if (userProfileExists(userProfileData.getName())) {
            throw new UniqueConstraintViolationException(new String[] { "name" });
        }

        UserProfileDO userProfileToCreate = new UserProfileDO(userProfileData);

        try {
            userProfileToCreate = (UserProfileDO) commitProfile(userProfileToCreate);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }

        return userProfileToCreate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#createCommProfile(java.lang.String,
     *      java.net.URI, int, int, int, int,
     *      com.bluejungle.destiny.container.dms.data.AgentPushParameters,
     *      boolean)
     */
    public ICommProfileDO createCommProfile(ICommProfileData commProfileData) throws UniqueConstraintViolationException, DataSourceException {
        if (commProfileData == null) {
            throw new IllegalArgumentException("commProfileData cannot be null.");
        }

        CommProfileDO commProfileToCreate = null;

        HibernateActivityJournalingSettingsManager activityJournalingSettingsManager = getActivityJournalingSettingsManager();

        Session hSession = null;
        Transaction transaction = null;
        try {
            hSession = getSession();
            transaction = hSession.beginTransaction();
            StringBuffer customJournalSettingsName = new StringBuffer(commProfileData.getAgentType().getId());
            customJournalSettingsName.append(CUSTOM_JOURNALING_SETTINGS_NAME_DELIMETER);
            customJournalSettingsName.append(commProfileData.getName());
            ICustomizableActivityJournalingSettings customJournalingSettings = activityJournalingSettingsManager.createActivityJournalingSettings(customJournalSettingsName.toString(), Collections.EMPTY_SET, hSession);
            commProfileToCreate = new CommProfileDO(commProfileData, customJournalingSettings);

            commProfileToCreate = (CommProfileDO) commitProfile(commProfileToCreate, hSession);
            transaction.commit();
        } catch (HibernateException exception) {
            HibernateUtils.rollbackTransation(transaction, getLog());
            if (exception instanceof JDBCException) {
                String sqlState = ((JDBCException) exception).getSQLState();
                /**
                 * FIX ME - When we move to Hibernate 3.0, change to catch
                 * ConstraintViolationException and let Hibernate handle the
                 * details
                 */
                if ((sqlState != null) && (sqlState.startsWith("23"))) {
                    /**
                     * It's a pretty safe assumption that this is due to the
                     * name/type constraint. The only other possibility is a
                     * database error
                     */
                    throw new UniqueConstraintViolationException(COMM_PROFILE_NAME_AND_TYPE_PROPERTIES, exception);
                }
            } else {
                throw new DataSourceException(exception);
            }
        } finally {
            HibernateUtils.closeSession(hSession, getLog());
        }

        return commProfileToCreate;
    }

    public void createDefaultProfileForUpgrade(final IAgentType agentType, final String locationStr, final Properties seedDataProperties) throws DataSourceException {
        final URI location;

        try {
            location = new URI(locationStr);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("locationStr " + locationStr + " is not a value URL");
        }

        if (location == null) {
            throw new NullPointerException("locationStr can not be null");
        }

        final String tamperProofingPassword = (String) seedDataProperties.get(PASSWORD_SEED_CONFIG_KEY_NAME);
        if (tamperProofingPassword == null) {
            throw new NullPointerException(PASSWORD_SEED_CONFIG_KEY_NAME + " needs to be specified for the seed data to be inserted");
        }

        Session seedDataSession = null;
        Transaction seedDataTransaction = null;
        try {
            seedDataSession = getSession();

            final CommProfileSeedDO seedDO = (CommProfileSeedDO) seedDataSession.createCriteria(CommProfileSeedDO.class).uniqueResult();

            if (seedDO == null) {
                throw new DataSourceException();
            }

            seedDataTransaction = seedDataSession.beginTransaction();

            final ICommProfileDO agentCommProfile = createCommProfile(new ICommProfileData() {
                public URI getDABSLocation() {
                    return location;
                }
                
                public TimeInterval getHeartBeatFrequency() {
                    return new TimeInterval(1, TimeInterval.TimeUnit.HOURS);
                }
                
                public int getLogExpiration() {
                    return 1;
                }
                
                public int getLogLimit() {
                    return 2;
                }
                
                public TimeInterval getLogFrequency() {
                    return new TimeInterval(30, TimeInterval.TimeUnit.SECONDS);
                }
                
                public boolean isPushEnabled() {
                    return true;
                }
                
                public int getDefaultPushPort() {
                    return 2000;
                }
                
                public String getName() {
                    return agentType.getTitle() + " Default Profile";
                }
                
                public IAgentType getAgentType() {
                    return agentType;
                }
                
                /**
                 * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getPassword()
                 */
                public String getPassword() {
                    return tamperProofingPassword;
                }
            });
            ((CommProfileDO) agentCommProfile).setDefault(true);
            this.updateCommProfile(agentCommProfile);

            // Remember the id of this record in the seed data table
            seedDO.getSeedItems().put(DEFAULT_COMM_PROFILE_KEY_NAME_PREFIX + agentType.getId(), agentCommProfile.getId());

            // Commit the seed data information
            Calendar now = Calendar.getInstance();
            seedDO.setLastUpdated(now);
            seedDataSession.save(seedDO);
            seedDataTransaction.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(seedDataTransaction, getLog());
        } finally {
            HibernateUtils.closeSession(seedDataSession, getLog());
        }

    }

    /**
     * @param seedDataProperties
     *            property bundle passing some dynamic value to the seed data.
     */
    void createDefaultProfiles(Properties seedDataProperties) throws DataSourceException {
        final String dabsStrLocation = (String) seedDataProperties.get(DABS_LOCATION_SEED_CONFIG_KEY_NAME);
        final URI dabsLocation;
        try {
            dabsLocation = new URI(dabsStrLocation);
        } catch (URISyntaxException e1) {
            throw new NullPointerException("DABS location needs to be valid for the seed data to be inserted");
        }
        if (dabsLocation == null) {
            throw new NullPointerException("DABS location needs to be specified for the seed data to be inserted");
        }

        final String tamperProofingPassword = (String) seedDataProperties.get(PASSWORD_SEED_CONFIG_KEY_NAME);
        if (tamperProofingPassword == null) {
            throw new NullPointerException(PASSWORD_SEED_CONFIG_KEY_NAME + " needs to be specified for the seed data to be inserted");
        }

        Session seedDataSession = null;
        Transaction seedDataTransaction = null;
        try {
            seedDataSession = getSession();
            seedDataTransaction = seedDataSession.beginTransaction();
            CommProfileSeedDO seedDO = new CommProfileSeedDO();

            IAgentProfileDO defaultAgentProfile = createAgentProfile(new IAgentProfileData() {

                /**
                 * @see com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData#isLogViewingEnabled()
                 */
                public boolean isLogViewingEnabled() {
                    return false;
                }

                /**
                 * @see com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData#isTrayIconEnabled()
                 */
                public boolean isTrayIconEnabled() {
                    return false;
                }

                /**
                 * @see com.bluejungle.destiny.container.shared.profilemgr.IBaseProfileData#getName()
                 */
                public String getName() {
                    return "Default_Profile";
                }
            });
            // Remember the id of this record in the seed data table
            seedDO.getSeedItems().put(DEFAULT_AGENT_PROFILE_KEY_NAME, defaultAgentProfile.getId());

            IAgentManager agentManager = getAgentManager();
            List<IAgentType> agentTypes = agentManager.getAgentTypes();
            for (final IAgentType nextAgentType : agentTypes) {
                ICommProfileDO nextCommProfile = createCommProfile(new ICommProfileData() {

                    public URI getDABSLocation() {
                        return dabsLocation;
                    }

                public TimeInterval getHeartBeatFrequency() {
                    return new TimeInterval(1, TimeInterval.TimeUnit.HOURS);
                }

                    public int getLogExpiration() {
                        return 1;
                    }

                    public int getLogLimit() {
                        return 2;
                    }

                    public TimeInterval getLogFrequency() {
                        return new TimeInterval(30, TimeInterval.TimeUnit.SECONDS);
                    }

                    public boolean isPushEnabled() {
                        return false;
                    }

                    public int getDefaultPushPort() {
                        return 2000;
                    }

                    public String getName() {
                        return nextAgentType.getTitle() + " Default Profile";
                    }

                    public IAgentType getAgentType() {
                        return nextAgentType;
                    }

                    /**
                     * @see com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData#getPassword()
                     */
                    public String getPassword() {
                        return tamperProofingPassword;
                    }
                });
                ((CommProfileDO) nextCommProfile).setDefault(true);
                this.updateCommProfile(nextCommProfile);

                // Remember the id of this record in the seed data table
                seedDO.getSeedItems().put(DEFAULT_COMM_PROFILE_KEY_NAME_PREFIX + nextAgentType.getId(), nextCommProfile.getId());
            }

            // Commit the seed data information
            Calendar now = Calendar.getInstance();
            seedDO.setLastUpdated(now);
            seedDataSession.save(seedDO);
            seedDataTransaction.commit();
        } catch (HibernateException e) {
            HibernateUtils.rollbackTransation(seedDataTransaction, getLog());
        } finally {
            HibernateUtils.closeSession(seedDataSession, getLog());
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#createAgentProfile(java.lang.String,
     *      boolean, boolean)
     */
    public IAgentProfileDO createAgentProfile(IAgentProfileData agentProfileData) throws UniqueConstraintViolationException, DataSourceException {
        if (agentProfileData == null) {
            throw new IllegalArgumentException("agentProfileData cannot be null.");
        }

        if (agentProfileExists(agentProfileData.getName())) {
            throw new UniqueConstraintViolationException(new String[] { "name" });
        }

        AgentProfileDO agentProfileToCreate = new AgentProfileDO(agentProfileData);

        try {
            agentProfileToCreate = (AgentProfileDO) commitProfile(agentProfileToCreate);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }

        return agentProfileToCreate;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#updateUserProfile(IUserProfileDO)
     */
    public void updateUserProfile(IUserProfileDO userProfileToUpdate) throws DataSourceException {
        if (userProfileToUpdate == null) {
            throw new IllegalArgumentException("userProfileToUpdate cannot be null");
        }

        try {
            // FIX ME - Remove cast through save method on object
            this.commitProfile((UserProfileDO) userProfileToUpdate);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#updateCommProfile(ICommProfileDO)
     */
    public void updateCommProfile(ICommProfileDO commProfileToUpdate) throws DataSourceException {
        if (commProfileToUpdate == null) {
            throw new IllegalArgumentException("commProfileToUpdate cannot be null");
        }
        try {
            this.commitProfile((CommProfileDO) commProfileToUpdate);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#updateAgentProfile(IAgentProfileDO)
     */
    public void updateAgentProfile(IAgentProfileDO agentProfileToUpdate) throws DataSourceException {
        if (agentProfileToUpdate == null) {
            throw new IllegalArgumentException("agentProfileToUpdate cannot be null");
        }

        try {
            this.commitProfile((AgentProfileDO) agentProfileToUpdate);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#getDefaultAgentProfile()
     */
    public IAgentProfileDO getDefaultAgentProfile() throws DataSourceException {
        if (this.defaultAgentProfileID == null) {
            throw new IllegalStateException("Default agent profile ID unknown");
        }

        IAgentProfileDO defaultProfile = null;
        try {
            defaultProfile = retrieveAgentProfileByID(this.defaultAgentProfileID.longValue());
        } catch (ProfileNotFoundException exception) {
            // This should only happen if the system is in an illegal state
            throw new IllegalStateException("Default profile not found: " + exception.getMessage());
        }

        return defaultProfile;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#getDefaultCommProfile()
     */
    public ICommProfileDO getDefaultCommProfile(IAgentType agentType) throws DataSourceException {
        if (!this.defaultCommProfileIds.containsKey(agentType)) {
            throw new IllegalArgumentException("Unknown Agent Type: " + agentType.getId());
        }

        Long defaultCommProfileID = this.defaultCommProfileIds.get(agentType);

        ICommProfileDO defaultProfile = null;
        try {
            defaultProfile = retrieveCommProfileByID(defaultCommProfileID.longValue());
        } catch (ProfileNotFoundException exception) {
            // This should only happen if the system is in an illegal state
            throw new IllegalStateException("Default profile not found: " + exception.getMessage());
        }

        return defaultProfile;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#retrieveUserProfileByName(java.lang.String)
     */
    public IUserProfileDO retrieveUserProfileByName(String userProfileName) throws DataSourceException, ProfileNotFoundException {
        if (userProfileName == null) {
            throw new IllegalArgumentException("userProfileName cannot be null.");
        }

        IUserProfileDO userProfileToReturn = retrieveUserProfileByUniqueField(UserProfileQueryField.NAME, userProfileName);
        if (userProfileToReturn == null) {
            throw new ProfileNotFoundException("user", "name", userProfileName);
        }

        return userProfileToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#retrieveCommProfileByName(java.lang.String)
     */
    public ICommProfileDO retrieveCommProfileByName(String commProfileName) throws DataSourceException, ProfileNotFoundException {
        if (commProfileName == null) {
            throw new IllegalArgumentException("commProfileName cannot be null.");
        }

        ICommProfileDO commProfileToReturn = retrieveCommProfileByUniqueField(CommProfileQueryField.NAME, commProfileName);
        if (commProfileToReturn == null) {
            throw new ProfileNotFoundException("communication", "name", commProfileName);
        }

        return commProfileToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#retrieveAgentProfileByName(java.lang.String)
     */
    public IAgentProfileDO retrieveAgentProfileByName(String agentProfileName) throws DataSourceException, ProfileNotFoundException {
        if (agentProfileName == null) {
            throw new IllegalArgumentException("agentProfileName cannot be null.");
        }

        IAgentProfileDO agentProfileToReturn = retrieveAgentProfileByUniqueField(AgentProfileQueryField.NAME, agentProfileName);
        if (agentProfileToReturn == null) {
            throw new ProfileNotFoundException("agent", "name", agentProfileName);
        }

        return agentProfileToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#retrieveUserProfileByID(long)
     */
    public IUserProfileDO retrieveUserProfileByID(long id) throws DataSourceException, ProfileNotFoundException {
        IUserProfileDO userProfileToReturn = retrieveUserProfileByUniqueField(UserProfileQueryField.ID, new Long(id));

        if (userProfileToReturn == null) {
            throw new ProfileNotFoundException("user", "id", new Long(id).toString());
        }

        return userProfileToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#retrieveCommProfileByID(long)
     */
    public ICommProfileDO retrieveCommProfileByID(long id) throws DataSourceException, ProfileNotFoundException {
        ICommProfileDO commProfileToReturn = retrieveCommProfileByUniqueField(CommProfileQueryField.ID, new Long(id));
        if (commProfileToReturn == null) {
            throw new ProfileNotFoundException("communication", "ID", new Long(id).toString());
        }

        return commProfileToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#retrieveAgentProfileByID(long)
     */
    public IAgentProfileDO retrieveAgentProfileByID(long id) throws DataSourceException, ProfileNotFoundException {
        IAgentProfileDO agentProfileToReturn = retrieveAgentProfileByUniqueField(AgentProfileQueryField.ID, new Long(id));

        if (agentProfileToReturn == null) {
            throw new ProfileNotFoundException("agent", "id", new Long(id).toString());
        }

        return agentProfileToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#retrieveUserProfiles(com.bluejungle.destiny.container.shared.profilemgr.UserProfileQueryTermSet,
     *      IUserProfileDO, int,
     *      com.bluejungle.destiny.container.shared.profilemgr.UserProfileQueryField)
     */
    public List<UserProfileDO> retrieveUserProfiles(UserProfileQueryTermSet userProfileQueryTerms, IUserProfileDO lastResult, int fetchSize, UserProfileQueryField sortProperty) throws DataSourceException {
        if (userProfileQueryTerms == null) {
            throw new IllegalArgumentException("userProfileQueryTerms cannot be null");
        }

        Map<String, Object> fieldsToSearchOn = new HashMap<String, Object>();
        Iterator queryTermsIterator = userProfileQueryTerms.queryTerms();
        while (queryTermsIterator.hasNext()) {
            UserProfileQueryTerm nextTerm = (UserProfileQueryTerm) queryTermsIterator.next();
            fieldsToSearchOn.put(nextTerm.getQueryField().getFieldName(), nextTerm.getValue());
        }

        String sortField = (sortProperty == null) ? null : sortProperty.getFieldName();

        try {
            return retrieveProfiles(UserProfileDO.class, fieldsToSearchOn, lastResult, fetchSize, sortField);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#retrieveAgentProfiles(com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryTermSet,
     *      IAgentProfileDO, int,
     *      com.bluejungle.destiny.container.shared.profilemgr.AgentProfileQueryField)
     */
    public List<? extends IAgentProfileDO> retrieveAgentProfiles(AgentProfileQueryTermSet agentProfileQueryTerms, IAgentProfileDO lastResult, int fetchSize, AgentProfileQueryField sortProperty) throws DataSourceException {
        if (agentProfileQueryTerms == null) {
            throw new IllegalArgumentException("agentProfileQueryTerms cannot be null");
        }

        Map<String, Object> fieldsToSearchOn = new HashMap<String, Object>();
        Iterator queryTermsIterator = agentProfileQueryTerms.queryTerms();
        while (queryTermsIterator.hasNext()) {
            AgentProfileQueryTerm nextTerm = (AgentProfileQueryTerm) queryTermsIterator.next();
            fieldsToSearchOn.put(nextTerm.getQueryField().getFieldName(), nextTerm.getValue());
        }

        String sortField = (sortProperty == null) ? null : sortProperty.getFieldName();

        try {
            return retrieveProfiles(AgentProfileDO.class, fieldsToSearchOn, lastResult, fetchSize, sortField);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#retrieveCommProfiles(com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryTermSet,
     *      ICommProfileDO, int,
     *      com.bluejungle.destiny.container.shared.profilemgr.CommProfileQueryField)
     */
    public List<? extends ICommProfileDO>  retrieveCommProfiles(CommProfileQueryTermSet commProfileQueryTerms, ICommProfileDO lastResult, int fetchSize, CommProfileQueryField sortProperty) throws DataSourceException {
        if (commProfileQueryTerms == null) {
            throw new IllegalArgumentException("commProfileQueryTerms cannot be null");
        }

        Map<String, Object> fieldsToSearchOn = new HashMap<String, Object>();
        Iterator queryTermsIterator = commProfileQueryTerms.queryTerms();
        while (queryTermsIterator.hasNext()) {
            CommProfileQueryTerm nextTerm = (CommProfileQueryTerm) queryTermsIterator.next();
            fieldsToSearchOn.put(nextTerm.getQueryField().getFieldName(), nextTerm.getValue());
        }

        String sortField = (sortProperty == null) ? null : sortProperty.getFieldName();

        try {
            return retrieveProfiles(CommProfileDO.class, fieldsToSearchOn, lastResult, fetchSize, sortField);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#deleteUserProfile(Long)
     */
    public void deleteUserProfile(Long id) throws DataSourceException {
        try {
            deleteProfileById(id, USER_PROFILE_DELETE_BY_ID_QUERY);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#deleteCommProfile(Long)
     */
    public void deleteCommProfile(Long id) throws DataSourceException {
        if (id == null) {
            throw new NullPointerException("id cannot be null.");
        }

        try {
            IAgentManager agentManager = getAgentManager();
            IAgentMgrQueryTermFactory queryTermFactory = agentManager.getAgentMgrQueryTermFactory();
            AgentQuerySpecImpl agentsWithProfileId = new AgentQuerySpecImpl();
            agentsWithProfileId.addSearchSpecTerm(queryTermFactory.getConcreteQueryTerm(AgentMgrQueryFieldType.COMM_PROFILE_ID, RelationalOp.EQUALS, id));
            IAgentQueryResults agentsWithProfileToDelete;
            try {
                agentsWithProfileToDelete = agentManager.getAgents(agentsWithProfileId);
            } catch (InvalidQuerySpecException e) {
                throw new DataSourceException(e);
            }
            IAgentDO[] agentsWithProfileToDeleteArray = agentsWithProfileToDelete.getAgents();
            Long defaultProfileIdToSet = null;
            Set<Long> idsOfAgentsToUpdate = new HashSet<Long>();
            for (int i = 0; i < agentsWithProfileToDeleteArray.length; i++) {
                if (i == 0) {
                    defaultProfileIdToSet = this.getDefaultCommProfile(agentsWithProfileToDeleteArray[i].getType()).getId();
                }
                idsOfAgentsToUpdate.add(agentsWithProfileToDeleteArray[i].getId());
            }
            if (!idsOfAgentsToUpdate.isEmpty()) {
                agentManager.setCommProfileForAgents(idsOfAgentsToUpdate, defaultProfileIdToSet);
            }

            deleteProfileById(id, COMM_PROFILE_DELETE_BY_ID_QUERY);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        } catch (PersistenceException exception) {
            throw new DataSourceException(exception);
        } catch (ProfileNotFoundException exception) {
            throw new DataSourceException(exception);
        }
    }

    /**
     * @see com.bluejungle.destiny.container.shared.profilemgr.IProfileManager#deleteAgentProfile(Long)
     */
    public void deleteAgentProfile(Long id) throws DataSourceException {
        try {
            deleteProfileById(id, AGENT_PROFILE_DELETE_BY_ID_QUERY);
        } catch (HibernateException exception) {
            throw new DataSourceException(exception);
        }
    }

    /**
     * Delete a profile with the specified name and query string
     * 
     * @param id
     *            the id of the profile to delete
     * @param deleteQuery
     *            the Hibernate query used to delete the profile
     * @throws HibernateException
     *             if an error occurs while accessing the database.
     */
    private void deleteProfileById(Long id, String deleteQuery) throws HibernateException {
        Session session = getSession();

        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.delete(deleteQuery, id, Hibernate.LONG);
            transaction.commit();
        } catch (HibernateException exception) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (HibernateException rollbackException) {
                    getLog().error("Failed to rollback transaction while attempting to delete a profile instance", exception);
                }
            }

            throw exception;
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (HibernateException exception) {
                    getLog().error("Failed to close Hibernate session following profile delete", exception);
                }
            }
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log logger) {
        this.logger = logger;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.logger;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.compManager = manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.compManager;
    }

    /**
     * Determine if a User Profile with the provided name already exists
     * 
     * @param name
     *            the name of the user profile to test
     * @return true if the user profile already exists; false otherwise
     * @throws DataSourceException
     */
    private boolean userProfileExists(String userProfileName) throws DataSourceException {
        boolean valueToReturn = false;

        IUserProfileDO userProfileToReturn = retrieveUserProfileByUniqueField(UserProfileQueryField.NAME, userProfileName);
        if (userProfileToReturn != null) {
            valueToReturn = true;
        }

        return valueToReturn;
    }

    /**
     * Determine if a Agent Profile with the provided name already exists
     * 
     * @param name
     *            the name of the agent profile to test
     * @return true if the agent profile already exists; false otherwise
     * @throws DataSourceException
     */
    private boolean agentProfileExists(String agentProfileName) throws DataSourceException {
        boolean valueToReturn = false;

        IAgentProfileDO agentProfileToReturn = retrieveAgentProfileByUniqueField(AgentProfileQueryField.NAME, agentProfileName);
        if (agentProfileToReturn != null) {
            valueToReturn = true;
        }

        return valueToReturn;
    }

    /**
     * Query for profiles
     * 
     * @param profileClass
     *            the class of the profile (UserProfileDO, AgentProfileDO,
     *            CommProfileDO)
     * @param searchFields
     *            the fields to search on. These must match bean properties from
     *            the class
     * @param lastResult
     *            the last result from the previous page retrieved, if
     *            retrieving results in chunks
     * @param fetchSize
     *            the number of results to retrieve
     * @param sortProperty
     *            the field on which the results should be sorted. This must
     *            match a bean property from the class
     * @return The results of the query
     * @throws HibernateException
     *             if an error occurs while accessing the database
     */
    private <T> List<T> retrieveProfiles(Class<T> profileClass, Map<String, Object> searchFields, IBaseProfileDO lastResult, int fetchSize, String sortProperty) throws HibernateException {
        // Add Query Terms
        Session session = getSession();
        Criteria queryCriteria = session.createCriteria(profileClass);
        queryCriteria.add(Expression.allEq(searchFields));

        HibernateLastResultBasedPagingHelper.addPagingCriteria(lastResult, fetchSize, sortProperty, queryCriteria);

        try {
            return queryCriteria.list();
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (HibernateException exception) {
                    getLog().error("Failed to close Hibernate session following profile search", exception);
                }
            }
        }
    }

    /**
     * Commit a profile to the database
     * 
     * @param profileToCommit
     * @return the profile which was persisted
     * @throws HibernateException
     *             is an error occured while storing the profile
     */
    private IBaseProfileDO commitProfile(BaseProfileDO profileToCommit) throws HibernateException {
        profileToCommit.setModifiedDate();
        Session session = getSession();

        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            profileToCommit = commitProfile(profileToCommit, session);
            transaction.commit();
        } catch (HibernateException exception) {
            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch (HibernateException rollbackException) {
                    getLog().error("Failed to rollback transaction while attempting to persist a profile instance", exception);
                }
            }

            throw exception;
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (HibernateException exception) {
                    getLog().error("Failed to close Hibernate session after attempting to persist a profile instance", exception);
                }
            }
        }
        return profileToCommit;
    }

    /**
     * @param profileToCommit
     * @param session
     * @return
     * @throws HibernateException
     */
    private BaseProfileDO commitProfile(BaseProfileDO profileToCommit, Session session) throws HibernateException {
        return (BaseProfileDO) session.saveOrUpdateCopy(profileToCommit);
    }

    /**
     * Retrieve a UserProfileDO by a unique field. If it was not found, null is
     * returned Due to the fact that this method returns null, it should never
     * be made public. If such a method is required, the behavior must be
     * changed
     * 
     * @param field
     *            the name of the unique field
     * @param value
     *            the value the field must match
     * @return the profile matching the provided criterion or null if it was not
     *         found
     * @throws DataSourceException
     *             if an error occurs while performing the query
     */
    private IUserProfileDO retrieveUserProfileByUniqueField(UserProfileQueryField field, Object value) throws DataSourceException {
        UserProfileQueryTerm queryTerm = new UserProfileQueryTerm(field, value);
        UserProfileQueryTermSet queryTermsSet = new UserProfileQueryTermSet();
        queryTermsSet.addQueryTerm(queryTerm);
        List<? extends IUserProfileDO> userProfilesRetrieved = retrieveUserProfiles(queryTermsSet, null, 1, null);

        return userProfilesRetrieved.isEmpty() ? null : userProfilesRetrieved.get(0);
    }

    /**
     * Retrieve a CommProfileDO by a unique field. If it was not found, null is
     * returned Due to the fact that this method returns null, it should never
     * be made public. If such a method is required, the behavior must be
     * changed
     * 
     * @param field
     *            the name of the unique field
     * @param value
     *            the value the field must match
     * @return the profile matching the provided criterion or null if it was not
     *         found
     * @throws DataSourceException
     *             if an error occurs while performing the query
     */
    private ICommProfileDO retrieveCommProfileByUniqueField(CommProfileQueryField field, Object value) throws DataSourceException {
        CommProfileQueryTerm queryTerm = new CommProfileQueryTerm(field, value);
        CommProfileQueryTermSet queryTermsSet = new CommProfileQueryTermSet();
        queryTermsSet.addQueryTerm(queryTerm);
        List<? extends ICommProfileDO> commProfilesRetrieved = retrieveCommProfiles(queryTermsSet, null, 1, null);

        return commProfilesRetrieved.isEmpty() ? null : commProfilesRetrieved.get(0);
    }

    /**
     * Retrieve a AgentProfileDO by a unique field. If it was not found, null is
     * returned Due to the fact that this method returns null, it should never
     * be made public. If such a method is required, the behavior must be
     * changed
     * 
     * @param field
     *            the name of the unique field
     * @param value
     *            the value the field must match
     * @return the profile matching the provided criterion or null if it was not
     *         found
     * @throws DataSourceException
     *             if an error occurs while performing the query
     */
    private IAgentProfileDO retrieveAgentProfileByUniqueField(AgentProfileQueryField field, Object value) throws DataSourceException {
        AgentProfileQueryTerm queryTerm = new AgentProfileQueryTerm(field, value);
        AgentProfileQueryTermSet queryTermsSet = new AgentProfileQueryTermSet();
        queryTermsSet.addQueryTerm(queryTerm);
        List<? extends IAgentProfileDO> agentProfilesRetrieved = retrieveAgentProfiles(queryTermsSet, null, 1, null);

        return agentProfilesRetrieved.isEmpty() ? null : agentProfilesRetrieved.get(0);
    }

    /**
     * @return
     */
    private HibernateActivityJournalingSettingsManager getActivityJournalingSettingsManager() {
        return (HibernateActivityJournalingSettingsManager) getManager().getComponent(IActivityJournalingSettingsManager.COMP_NAME);
    }

    /**
     * Retrieve a Hibernate Session
     * 
     * @return a Hibernate Session
     */
    private Session getSession() throws HibernateException {
        IHibernateRepository dataSource = this.getDataSource();
        return dataSource.getSession();
    }

    /**
     * Returns a data source object that can be used to create sessions.
     * 
     * @return IHibernateDataSource Data source object
     */
    private IHibernateRepository getDataSource() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        IHibernateRepository dataSource = (IHibernateRepository) componentManager.getComponent(DestinyRepository.MANAGEMENT_REPOSITORY.getName());

        if (dataSource == null) {
            throw new IllegalStateException("Required datasource not initialized for ProfileManager.");
        }

        return dataSource;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        /*
         * Intialize the activity journal settings manager.
         */
        ComponentInfo activityJournlingSettingsMgrCompInfo = new ComponentInfo(IActivityJournalingSettingsManager.COMP_NAME, HibernateActivityJournalingSettingsManager.class.getName(), IActivityJournalingSettingsManager.class.getName(),
                                                                               LifestyleType.SINGLETON_TYPE);
        getManager().registerComponent(activityJournlingSettingsMgrCompInfo, true);

        // Loads the list of ids for the default profiles
        Session s = null;
        try {
            s = getSession();
            CommProfileSeedDO seedDataInfo = (CommProfileSeedDO) s.createCriteria(CommProfileSeedDO.class).uniqueResult();
            if (seedDataInfo != null) {
                IAgentManager agentManager = getAgentManager();
                Map<String, Long> seedItems = seedDataInfo.getSeedItems();
                for (Map.Entry<String, Long> nextSeedDataItem : seedItems.entrySet()) {
                    String nextSeedDataItemKey = nextSeedDataItem.getKey();
                    if (nextSeedDataItemKey.startsWith(DEFAULT_COMM_PROFILE_KEY_NAME_PREFIX)) {
                        String agentTypeId = nextSeedDataItemKey.substring(DEFAULT_COMM_PROFILE_KEY_NAME_PREFIX.length());
                        IAgentType nextAgentType = agentManager.getAgentType(agentTypeId);
                        this.defaultCommProfileIds.put(nextAgentType, nextSeedDataItem.getValue());
                    } else if (nextSeedDataItemKey.equals(DEFAULT_AGENT_PROFILE_KEY_NAME)) {
                        this.defaultAgentProfileID = nextSeedDataItem.getValue();
                    }
                }
            }
        } catch (HibernateException e) {
            getLog().fatal("Unable to load seed data records for profiles. Seed data may not have been inserted properly", e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtils.closeSession(s, getLog());
        }
    }

    private IAgentManager getAgentManager() {
        if (this.agentManager == null) {
            if (!this.compManager.isComponentRegistered(IAgentManager.COMP_NAME)) {
                throw new IllegalArgumentException("Agnet Manager has not been registered");
            }

            this.agentManager = (IAgentManager) this.compManager.getComponent(IAgentManager.COMP_NAME);
        }

        return this.agentManager;
    }
}
