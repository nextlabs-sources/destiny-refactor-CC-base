/*
 * Created on Jul 18, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.DomainAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserAlreadyExistsException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ApplicationUserRepositoryAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUserDomain;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IInternalAccessGroup;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedDataTask;
import com.bluejungle.framework.datastore.hibernate.seed.ISeedUpdateTask;
import com.bluejungle.framework.datastore.hibernate.seed.SeedDataTaskException;
import com.bluejungle.framework.datastore.hibernate.seed.seedtasks.SeedDataTaskBase;
import com.bluejungle.version.IVersion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/HibernateApplicationUserRepositorSeedDataMgr.java#1 $
 */

public class HibernateApplicationUserRepositorSeedDataMgr extends SeedDataTaskBase implements ISeedUpdateTask {

    private static final String ADMINISTRATOR_PASSWORD = "AdministratorPassword";

    private static final String ALL_USERS_GROUP_TITLE = "All Policy Server Users";
    private static final String ALL_USERS_GROUP_DESCRIPTION = "A group containing all users.";
    private static final String ALL_USERS_GROUP_ID_PROPERTY_NAME = "ALL_USERS_GROUP_ID";

    private static final String DATA_SHARE_FILE_NAME = "shared_seed_data.properties";
    /**
     * @see com.bluejungle.destiny.tools.dbinit.seedtasks.SeedDataTaskBase#execute()
     */
    public void execute() throws SeedDataTaskException {
        HibernateApplicationUserRepository userRepository = new HibernateApplicationUserRepository();
        try {
            IApplicationUserDomain initialDomain = userRepository.createInitialDomain();

            Properties props = getConfiguration().get(ISeedDataTask.CONFIG_PROPS_CONFIG_PARAM);
            String administratorPassword = props.getProperty(ADMINISTRATOR_PASSWORD);
            userRepository.createAdministrator(administratorPassword);

            // Create seed application user groups
            IInternalAccessGroup allUsersGroup = initialDomain.createAccessGroup(ALL_USERS_GROUP_TITLE, ALL_USERS_GROUP_DESCRIPTION);

            // Store seed data groups ids in a file for use by pf seed data
            // task. NOT IDEAL. Need to find a way to share data amongst tasks
            // seedings different databases
            Properties seedAccessGroupIDs = new Properties();
            seedAccessGroupIDs.put(ALL_USERS_GROUP_ID_PROPERTY_NAME, allUsersGroup.getDestinyId().toString());
            File sharedSeedDataPropertyFile = new File(DATA_SHARE_FILE_NAME);
            FileOutputStream sharedSeedDataPropertyFileStream = new FileOutputStream(sharedSeedDataPropertyFile);
            seedAccessGroupIDs.store(sharedSeedDataPropertyFileStream, "Shared seed data.");            
        } catch (ApplicationUserRepositoryAccessException exception) {
            throw new SeedDataTaskException("Failed to create initial application user domain.", exception);
        } catch (DomainAlreadyExistsException exception) {
            throw new SeedDataTaskException("Failed to create initial application user domain.", exception);
        } catch (DomainNotFoundException exception) {
            throw new SeedDataTaskException("Failed to find domain for super user.", exception);
        } catch (UserAlreadyExistsException exception) {
            throw new SeedDataTaskException("Failed to create super user.  It already exists", exception);
        } catch (GroupAlreadyExistsException exception) {
            throw new SeedDataTaskException("Failed to create seed group.  It already exists", exception);
        } catch (IOException exception) {
            throw new SeedDataTaskException("Failed to share seed data.", exception);    
        }
    }
    
    @Override
    public void execute(IVersion fromV, IVersion toV) throws SeedDataTaskException {
        File sharedSeedDataPropertyFile = new File(DATA_SHARE_FILE_NAME);
        if (sharedSeedDataPropertyFile.exists()) {
            //do nothing if the file is already exist.
            return;
        }
        
        HibernateApplicationUserRepository userRepository = new HibernateApplicationUserRepository();
        try {
            IApplicationUserDomain defaultAdminDomain = userRepository.getApplicationUserDomain(userRepository.getDefaultAdminDomainName());

            IAccessGroup allUsersGroup = defaultAdminDomain.getAccessGroup(ALL_USERS_GROUP_TITLE);

            Properties seedAccessGroupIDs = new Properties();
            seedAccessGroupIDs.put(ALL_USERS_GROUP_ID_PROPERTY_NAME, allUsersGroup.getDestinyId().toString());
            FileOutputStream sharedSeedDataPropertyFileStream = new FileOutputStream(sharedSeedDataPropertyFile);
            seedAccessGroupIDs.store(sharedSeedDataPropertyFileStream, "Shared seed data.");            
        } catch (ApplicationUserRepositoryAccessException exception) {
            throw new SeedDataTaskException("Failed to create initial application user domain.", exception);
        } catch (DomainNotFoundException exception) {
            throw new SeedDataTaskException("Failed to find domain for super user.", exception);
        } catch (GroupNotFoundException exception) {
            throw new SeedDataTaskException("Failed to find a group.", exception);
        } catch (IOException exception) {
            throw new SeedDataTaskException("Failed to share seed data.", exception);    
        }
    }
    
}
