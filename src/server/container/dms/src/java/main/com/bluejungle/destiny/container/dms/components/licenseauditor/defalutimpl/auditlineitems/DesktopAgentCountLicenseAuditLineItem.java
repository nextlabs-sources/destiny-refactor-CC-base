/*
 * Created on May 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.auditlineitems;

import java.util.Properties;

import com.bluejungle.destiny.container.dms.components.licenseauditor.defalutimpl.InvalidLicenseLineItemException;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStatistics;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;

/**
 * Validates Desktop Enforcer count license property
 * 
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dms/src/java/main/com/bluejungle/destiny/container/dms/components/licenseauditor/defalutimpl/auditlineitems/DesktopAgentCountLicenseAuditLineItem.java#1 $
 */

public class DesktopAgentCountLicenseAuditLineItem extends BaseAgentCountLicenseAuditLineItem {

    private static final String DESCRIPTION = "Desktop Agent Count Property Validation";
    private static final String DESKTOP_AGENT_COUNT_LICENSE_PROPERTY_NAME = "windows_desktop_enforcer";

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
        String desktopAgentCountPropertyValue = licenseProperties.getProperty(DESKTOP_AGENT_COUNT_LICENSE_PROPERTY_NAME);
        if (desktopAgentCountPropertyValue == null) {
            throw new InvalidLicenseLineItemException("Required property, " + DESKTOP_AGENT_COUNT_LICENSE_PROPERTY_NAME + ", not found.", this);
        }

        if (!"-1".equals(desktopAgentCountPropertyValue)) {
            try {
                long desktopAgentCount = getAgentCount(AgentTypeEnumType.DESKTOP.getName());
                if (desktopAgentCount > Long.parseLong(desktopAgentCountPropertyValue)) {
                    StringBuffer exceptionMessage = new StringBuffer("Current desktop agent count, ");
                    exceptionMessage.append(desktopAgentCount);
                    exceptionMessage.append(", larger than allowed by license.");

                    throw new InvalidLicenseLineItemException(exceptionMessage.toString(), this);
                }
            } catch (PersistenceException exception) {
                throw new InvalidLicenseLineItemException("Failed to read agent statistics.  Could not validate license.", this);
            } catch (NumberFormatException exception) {
                throw new InvalidLicenseLineItemException("License desktop enforcer count property not a valid number.", this);
            }
        }
    }

}