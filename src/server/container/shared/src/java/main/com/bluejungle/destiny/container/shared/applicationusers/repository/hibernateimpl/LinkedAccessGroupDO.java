/*
 * Created on May 12, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.applicationusers.repository.hibernateimpl;

import com.bluejungle.destiny.container.shared.applicationusers.core.DomainNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.core.GroupNotFoundException;
import com.bluejungle.destiny.container.shared.applicationusers.external.ExternalUserAccessException;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalDomainManager;
import com.bluejungle.destiny.container.shared.applicationusers.external.IExternalGroup;
import com.bluejungle.destiny.container.shared.applicationusers.external.IUserAccessProvider;
import com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;

/**
 * @author sgoldstein
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/applicationusers/repository/hibernateimpl/LinkedAccessGroupDO.java#1 $
 */

public class LinkedAccessGroupDO extends BaseAccessGroupDO implements ILinkedAccessGroup {

    private static final Log LOG = LogFactory.getLog(LinkedAccessGroupDO.class.getName());
    
    private static final String EMPTY_DESCRIPTION = "";

    private byte[] externalId;
    boolean isOrphaned = false;

    /**
     * Create an instance of LinkedAccessGroupDO. For hibernate use only
     */
    LinkedAccessGroupDO() {
        super();
    }

    /**
     * Create an instance of LinkedAccessGroupDO
     * 
     * @param externalId
     * @param title
     * @param accessDomain
     */
    LinkedAccessGroupDO(byte[] externalId, String title, AccessDomainDO accessDomain) {
        super(title, EMPTY_DESCRIPTION, accessDomain);
        this.externalId = externalId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup#getExternalId()
     */
    public byte[] getExternalId() {
        return this.externalId;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup#getExternalMembers()
     */
    public Collection getExternalMembers() throws ExternalUserAccessException {
        Collection membersToReturn = null;
        if (!isOrphanedInternal()) {
            try {
                IExternalGroup externalGroup = getExternalGroup();
                IUserAccessProvider externalAccessProvider = getExternalAccessProvider();
                membersToReturn = externalAccessProvider.getUsersInExternalGroup(externalGroup);
            } catch (GroupNotFoundException exception) {
                setIsOrphanedInteranl();
                membersToReturn = Collections.EMPTY_LIST;
            } catch (DomainNotFoundException exception) {
                throw new ExternalUserAccessException(exception);
            }
        }
        return membersToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup#getQualifiedExternalName()
     */
    public String getQualifiedExternalName() {
        String nameToReturn = null;
        try {
            nameToReturn = getExternalGroup().getQualifiedExternalName();
        } catch (GroupNotFoundException exception) {
            setIsOrphanedInteranl();
            StringBuffer errorMessage = new StringBuffer("Failed to retrieve qualified external name for group with id, ");
            errorMessage.append(this.getDestinyId());
            errorMessage.append(".  The external group was not found");
            LOG.error(errorMessage.toString(), exception);
        } catch (DomainNotFoundException exception) {
            StringBuffer errorMessage = new StringBuffer("Failed to retrieve qualified external name for group with id, ");
            errorMessage.append(this.getDestinyId());
            errorMessage.append(".  The external domain was not found.");
            LOG.error(errorMessage.toString(), exception);                  
        } catch (ExternalUserAccessException exception) {
            StringBuffer errorMessage = new StringBuffer("Failed to retrieve qualified external name for group with id, ");
            errorMessage.append(this.getDestinyId());
            errorMessage.append(".");
            LOG.error(errorMessage.toString(), exception);
        }
        
        // Return null if the group is not found.  This is what the open ldap implementation did.  Mimicing, although probably not ideal
        return nameToReturn;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.applicationusers.repository.ILinkedAccessGroup#isOrphaned()
     */
    public boolean isOrphaned() {
        if (!this.isOrphanedInternal()) {
            try {
                getExternalGroup();
            } catch (GroupNotFoundException exception) {
                setIsOrphanedInteranl();
            } catch (DomainNotFoundException exception) {
                StringBuffer errorMessage = new StringBuffer("Failed to determine orphaned state for group with id, ");
                errorMessage.append(this.getDestinyId());
                errorMessage.append(".  The external domain was not found.");
                LOG.error(errorMessage.toString(), exception);                  
            } catch (ExternalUserAccessException exception) {
                StringBuffer errorMessage = new StringBuffer("Failed to determine orphaned state for group with id, ");
                errorMessage.append(this.getDestinyId());
                errorMessage.append(".");
                LOG.error(errorMessage.toString(), exception);
            }
        }
        
        return this.isOrphanedInternal();
    }

    /**
     * Set the externalId. Required by Hibernate
     * 
     * @param externalId
     *            The externalId to set.
     */
    void setExternalId(byte[] externalId) {
        if (externalId == null) {
            throw new NullPointerException("externalId cannot be null.");
        }

        this.externalId = externalId;
    }

    private IExternalGroup getExternalGroup() throws DomainNotFoundException, ExternalUserAccessException, GroupNotFoundException {
        IUserAccessProvider externalAccessProvider = getExternalAccessProvider();
        return externalAccessProvider.getGroup(this.externalId);
    }

    private IUserAccessProvider getExternalAccessProvider() throws DomainNotFoundException {
        IExternalDomainManager externalDomainManager = HibernateApplicationUserRepository.getExternalDomainManager();
        IUserAccessProvider externalAccessProvider = externalDomainManager.getExternalDomain(this.getDomainName()).getUserAccessProvider();
        return externalAccessProvider;
    }
    
    private boolean isOrphanedInternal() {
        return this.isOrphaned;
    }
    
    private void setIsOrphanedInteranl() {
        this.isOrphaned = true;
    }
}
