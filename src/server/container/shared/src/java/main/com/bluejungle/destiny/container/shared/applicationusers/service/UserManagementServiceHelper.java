/*
 * Created on Sep 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.service;

import java.math.BigInteger;

import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IAccessGroup;
import com.bluejungle.destiny.container.shared.applicationusers.repository.IApplicationUser;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.services.management.types.UserDTO;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/service/UserManagementServiceHelper.java#1 $
 */

public final class UserManagementServiceHelper {

    private static IApplicationUserManager applicationUserManager;

    public static UserDTO convertUserToDTO(IApplicationUser applicationUser) throws ServiceNotReadyFault, UserNotFoundException, UserManagementAccessException {
        if (applicationUser == null) {
            throw new NullPointerException("applicationUser cannot be null.");
        }

        IApplicationUserManager applicationUserManager = getApplicationUserManager();
        
        long userId = applicationUser.getDestinyId().longValue();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(BigInteger.valueOf(userId));
        userDTO.setUniqueName(applicationUser.getUniqueName());
        userDTO.setUid(applicationUser.getLogin());
        userDTO.setType(SubjectType.USER.getName());
        userDTO.setName(applicationUser.getDisplayName());
        userDTO.setFirstName(applicationUser.getFirstName());
        userDTO.setLastName(applicationUser.getLastName());
        userDTO.setLocal(applicationUserManager.isLocallyAuthenticated(applicationUser));
        
        BigInteger primaryAccessGroupId = null;
        IAccessGroup primaryAccessGroup = applicationUserManager.getPrimaryAccessGroupForUser(userId);
        if (primaryAccessGroup != null) {
            primaryAccessGroupId = BigInteger.valueOf(primaryAccessGroup.getDestinyId().longValue());
        }
        userDTO.setPrimaryUserGroupId(primaryAccessGroupId);

        return userDTO;
    }

    /**
     * Retrieve the Application user manager
     * 
     * @return the Application User Manager
     * @throws ServiceNotReadyFault
     */
    private static IApplicationUserManager getApplicationUserManager() throws ServiceNotReadyFault {
        if (applicationUserManager == null) {
            IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
            if (!componentManager.isComponentRegistered(IApplicationUserManagerFactory.COMP_NAME)) {
                throw new ServiceNotReadyFault();
            }

            IApplicationUserManagerFactory appUserManagerFactory = (IApplicationUserManagerFactory) componentManager.getComponent(ApplicationUserManagerFactoryImpl.class);
            applicationUserManager = appUserManagerFactory.getSingleton();
        }

        return applicationUserManager;
    }
}
