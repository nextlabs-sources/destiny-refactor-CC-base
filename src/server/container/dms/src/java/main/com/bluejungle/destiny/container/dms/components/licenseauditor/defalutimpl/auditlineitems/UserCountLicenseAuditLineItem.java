/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.auditlineitems;

import java.util.Collection;
import java.util.Properties;

import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.ILicenseAuditLineItem;
import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.InvalidLicenseLineItemException;
import com.bluejungle.destiny.container.shared.applicationusers.core.ApplicationUserManagerFactoryImpl;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManager;
import com.bluejungle.destiny.container.shared.applicationusers.core.IApplicationUserManagerFactory;
import com.bluejungle.destiny.container.shared.applicationusers.core.UserManagementAccessException;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;

/**
 * Verifies that the system user count is valid for the installed license
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/licenseauditor/defalutimpl/auditlineitems/UserCountLicenseAuditLineItem.java#1 $
 */

public class UserCountLicenseAuditLineItem implements ILicenseAuditLineItem {

    private static final String DESCRIPTION = "User Count License Property Validation";
    private static final String USER_COUNT_LICENSE_PROPERTY_NAME = "usercount";

    private IApplicationUserManager applicationUserManager;

    /**
     * @see com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.ILicenseAuditLineItem#getDescription()
     */
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * @see com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.ILicenseAuditLineItem#verify(java.util.Properties)
     */
    public void verify(Properties licenseProperties) throws InvalidLicenseLineItemException {
        String userCountPropertyValue = licenseProperties.getProperty(USER_COUNT_LICENSE_PROPERTY_NAME);
        if (userCountPropertyValue == null) {
            throw new InvalidLicenseLineItemException("Required property, " + USER_COUNT_LICENSE_PROPERTY_NAME + ", not found.", this);
        }

        if (!"-1".equals(userCountPropertyValue)) {
            try {
                Integer userCount = getUserCount();
                int licensePropertyUserCount = Integer.parseInt(userCountPropertyValue);
                if (userCount.intValue() > licensePropertyUserCount) {
                    StringBuffer exceptionMessage = new StringBuffer("Current user count, ");
                    exceptionMessage.append(userCount.intValue());
                    exceptionMessage.append(", larger than allowed by license.");

                    throw new InvalidLicenseLineItemException(exceptionMessage.toString(), this);
                }
            } catch (UserManagementAccessException exception) {
                throw new InvalidLicenseLineItemException("Failed to retrieve application user count.  Could not validate license.", this);
            } catch (NumberFormatException exception) {
                throw new InvalidLicenseLineItemException("License user count property not a valid number.", this);
            }
        }
    }

    /**
     * Retrieve the current application user count
     * 
     * @return the current application user count
     * @throws UserManagementAccessException 
     */
    private Integer getUserCount() throws UserManagementAccessException {
        IApplicationUserManager userManager = getApplicationUserManager();
        Collection applicationUsers = userManager.getApplicationUsers(null, 0);
        return new Integer(applicationUsers.size());
    }

    /**
     * Retrieve the Application user manager
     * 
     * @return the Application User Manager
     */
    private IApplicationUserManager getApplicationUserManager() {
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
        if (this.applicationUserManager == null) {
            if (!componentManager.isComponentRegistered(IApplicationUserManagerFactory.COMP_NAME)) {
                throw new IllegalStateException("Application user manager could not be found");
            }

            IApplicationUserManagerFactory appUserManagerFactory = (IApplicationUserManagerFactory) componentManager.getComponent(ApplicationUserManagerFactoryImpl.class);
            this.applicationUserManager = appUserManagerFactory.getSingleton();
        }

        return this.applicationUserManager;
    }
}
