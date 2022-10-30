/*
 * Created on Feb 2, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.ISharePointEnrollmentWrapper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryKey;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.dictionary.IReferenceable;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.RelationOp;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/impl/SharePointElementCreator.java#1 $
 */

public class SharePointElementCreator {

    private static final Log LOG = LogFactory.getLog(SharePointElementCreator.class);
    
    private final ISharePointEnrollmentWrapper enrollmentWrapper;
    private final IElementField sidField;
    private final Collection<IElementBase> groups; 
    private final Map<String, IElementBase> members; 
    private final String groupPrefix;
    private final IEnrollment enrollment;
    
    private Collection<String> enrollmentNameCache = null;
    private IMGroup currentGroup = null;
    private boolean isExistingGroup = true;
    
    public SharePointElementCreator(ISharePointEnrollmentWrapper enrollmentWrapper) throws DictionaryException {
        this.enrollmentWrapper = enrollmentWrapper;
        this.groups = new ArrayList<IElementBase>();
        this.currentGroup = null;
        this.members = new HashMap<String, IElementBase>();
        this.groupPrefix = enrollmentWrapper.getDomainName().toUpperCase() + ":Groups:";
        this.enrollment = this.enrollmentWrapper.getEnrollment();

        IElementType userType =
                enrollment.getDictionary().getType(ElementTypeEnumType.USER.getName());
        sidField = userType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName());
    }
    
    public void addGroup(String id, String name, String ownerID, boolean isOwnerUser)
            throws EnrollmentSyncException {
        DictionaryKey key = new DictionaryKey((name + id).getBytes());
        this.members.clear();

        IMGroup group = null;

        if (this.enrollmentWrapper.isUpdate()) {
            try {
                group = enrollment.getGroup(key, enrollmentWrapper.getEnrollmentStartTime());
            } catch (DictionaryException e) {
                throw new EnrollmentSyncException(e, "id=" + id + ", name=" + name);
            }
        }

        if (group == null) {
            DictionaryPath path = new DictionaryPath( new String[]{ enrollmentWrapper.getDomainName(), "groups", name});
            group = enrollment.makeNewEnumeratedGroup(path, key);
            LOG.debug("Created group " + name + ", id = " + id);
            isExistingGroup = false;
        }else{
            LOG.debug("Retrieved group " + name + ", id = " + id);
            isExistingGroup = true;
        }

        if (group != null) {
            currentGroup = group;
            currentGroup.setExternalKey(key);
            currentGroup.setDisplayName(name);
            currentGroup.setName(name);
            currentGroup.setUniqueName(this.groupPrefix + name);
            groups.add(currentGroup);
        }
    }
    
    public void addMemberToCurrentGroup(String id, String name, String SID, boolean isDomainGroup)
            throws EnrollmentSyncException {
        if (currentGroup == null) {
            throw new EnrollmentSyncException("Can not add memeber to null group", name);
        }
        
        IMElementBase element = null;
        try {
            if (!isDomainGroup) {
                // the group from ad
                IDictionaryIterator<IMElement> itor = null;
                try{
                    //TODO Don't save the members. They are normalized
                    itor = this.enrollment.getDictionary().query(
                            sidField.buildRelation(RelationOp.EQUALS,Constant.build(SID)),  
                            enrollmentWrapper.getEnrollmentStartTime(), null, null);
                    if (itor.hasNext()) {
                        element = itor.next();
                    }
                } finally{
                    if (itor != null) {
                        itor.close();
                    }
                }
            } else {
                // the group from sharepoint
                String[] result = name.split("\\\\");
                if ( ( result != null ) && ( result.length > 1 ) ) {
                    String domainName = result[0].toLowerCase();
                    String domainNameLong = getMatchingDomainNameByShortName(domainName);
                    if ( domainNameLong != null ) {
                        String uniqueName = domainNameLong + ":Groups:" + result[1];  
                        element = this.enrollment.getDictionary().getGroup(uniqueName, enrollmentWrapper.getEnrollmentStartTime());
                    }
                }
            }
        } catch (DictionaryException e) {
            throw new EnrollmentSyncException(e, "id=" + id + ", name=" + name);
        }
        
        
        if (element != null) {
            members.put(SID, element);
        } else {
            //TODO ask Oja
            if(isDomainGroup){
                LOG.warn("The domian user/group '" + name + "' under group '" + name +"' is not enrolled. The entry will be ignored");
            }else{
                LOG.warn("The user/group '" + name + "' under group '" + name +"' is not enrolled. The entry will be ignored");
            }
        }
    }
    
    /**
     * saveGroupMembers() set the group members
     *
     * If group exists, remove the members first
     * 
     */
    public void saveGroupMembers() throws EnrollmentSyncException {

        // if this is update, remove all direct children of the group
        try {
            if (isExistingGroup && this.enrollmentWrapper.isUpdate()) {
                removeAllDirectGroupMembers(currentGroup);
            }
        } catch (DictionaryException e) {
            throw new EnrollmentSyncException("Failed to remove group members", e, currentGroup.getName());
        }

        try {
            for (IElementBase element : members.values()) {
                LOG.debug("add child " + element + " to " + currentGroup);
                currentGroup.addChild(element);
            }
        } catch (DictionaryException e) {
            throw new EnrollmentSyncException("Failed to save group members", e, currentGroup.getName());
        }
    }

    /**
     * Remove direct elements and group members of a group for updating existing group
     * @param group
     * @throws DictionaryException
     */
    private void removeAllDirectGroupMembers(IMGroup group) throws DictionaryException {
        removeAllChild(group, group.getDirectChildElements());
        removeAllChild(group, group.getDirectChildGroups());
    }
    
    private void removeAllChild(IMGroup group, IDictionaryIterator<? extends IReferenceable> itor)
            throws DictionaryException {
        if (itor != null) {
            try {
                while (itor.hasNext()) {
                    group.removeChild(itor.next());
                }
            } finally {
                itor.close();
            }
        }
    }
    
    /**
     * case insensitive, always return UPPERCASE
     * @param shortName
     * @return
     * @throws DictionaryException
     */
    private String getMatchingDomainNameByShortName(String shortName) throws DictionaryException {
        if (enrollmentNameCache == null) {
            enrollmentNameCache = new HashSet<String>();
            Collection<IEnrollment> enrollments = this.enrollment.getDictionary().getEnrollments();
            for (IEnrollment e : enrollments) {
                enrollmentNameCache.add(e.getDomainName().toUpperCase());
            }
        }
        shortName = shortName.toUpperCase();
        for (String domainName : enrollmentNameCache) {
            if (domainName.equals(shortName) || domainName.startsWith(shortName + ".")) {
                return domainName;
            }
        }
        LOG.debug("can't find any domain that match '" + shortName + "'");
        return null;
    }
    
    public Collection<IElementBase> getGroups() {
        return groups;
    }
    
    public Collection<IElementBase> getMembers() {
        return members.values();
    }
}
