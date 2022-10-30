/*
 * Created on Mar 27, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.datastore.hibernate.utils.IMassDMLFormatter;
import com.bluejungle.framework.utils.UnmodifiableDate;

/**
 * This is the user and user group resource cache implementation. The user and
 * user group information comes from the policy framework / DDIF and is stored
 * in a couple of data object with a many to many relationship (many users can
 * belong to many user groups).
 *
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/UserAndGroupsCacheImpl.java#1 $
 */

public class UserAndGroupsCacheImpl extends BaseDictionaryResourceCacheImpl {

    private static final String INSERT_ELEMENT_TARGET = "CACHED_USER ($id$original_id, display_name, first_name, last_name, sid, active_from, active_to) ";
    private static final String INSERT_ELEMENT_SOURCE = "$"+CACHE_TABLE_SEQUENCE+"$?, ?, ?, ?, ?, ?, ? ";
    private static final String CLOSE_ELEMENT_SQL = "update CACHED_USER set active_to=? WHERE original_id=? AND active_to=?";

    private static final String INSERT_GROUP_TARGET = "CACHED_USERGROUP ($id$original_id, name, display_name, active_from, active_to, enroll_type) ";
    private static final String INSERT_GROUP_SOURCE = "$"+CACHE_TABLE_SEQUENCE+"$?, ?, ?, ?, ?, ? ";
    private static final String CLOSE_GROUP_SQL = "update CACHED_USERGROUP set active_to=? WHERE original_id=? AND active_to=?";

    private static final String INSERT_MEMBER_TARGET = "CACHED_USERGROUP_MEMBER ($id$groupid, userid, active_from, active_to) ";
    private static final String INSERT_MEMBER_SOURCE = "$"+CACHE_TABLE_SEQUENCE+"$?, ?, ?, ? ";
    private static final String CLOSE_GROUP_MEMBER_SQL = "update CACHED_USERGROUP_MEMBER set active_to=? WHERE groupid=? AND userid=? AND active_to=?";
   
    private IElementType elementType;
    private IElementField displayNameField;
    private IElementField firstNameField;
    private IElementField lastNameField;
    private IElementField sidNameField;


    /**
     * @see BaseDictionaryResourceCacheImpl#getElementType()
     */
    protected IElementType getElementType() {
        return elementType;
    }

    /**
     * @see BaseResourceCacheImpl#getResourceCacheType()
     */
    protected ResourceCacheType getResourceCacheType() {
        return ResourceCacheType.USER;
    }
    
    @Override
    protected boolean isSupportingGroups() {
        return true;
    }

    /**
     * @see IInitializable#init()
     */
    public void init() {
        super.init();
        try {
            elementType      = getDictionary().getType(ElementTypeEnumType.USER.getName());
            displayNameField = elementType.getField(UserReservedFieldEnumType.PRINCIPAL_NAME.getName());
            firstNameField   = elementType.getField(UserReservedFieldEnumType.FIRST_NAME.getName());
            lastNameField    = elementType.getField(UserReservedFieldEnumType.LAST_NAME.getName());
            sidNameField     = elementType.getField(UserReservedFieldEnumType.WINDOWS_SID.getName());
        } catch (DictionaryException exception) {
            getLog().error("unable to retrieve known user fields from dictionary", exception);
        }
    }

    /**
     * @see BaseDictionaryResourceCacheImpl#closeElement(Connection, IElement, Date)
     */
    protected void closeElement(Connection con, IElement elementToClose, Date closeDate) throws SQLException {
        Long elementId = elementToClose.getInternalKey();
        PreparedStatement ps = con.prepareStatement(CLOSE_ELEMENT_SQL);
        ps.setLong(1, closeDate.getTime());
        ps.setLong(2, elementId.longValue());
        ps.setLong(3, UnmodifiableDate.END_OF_TIME.getTime());
        ps.execute();
        ps.close();
    }

    /**
     * @see BaseDictionaryResourceCacheImpl#createNewElement(Connection, IElement, Date)
     */
    protected void createNewElement(Connection con, IElement elementToCreate, Date fromDate, IMassDMLFormatter formatter) throws SQLException {
        Long elementId = elementToCreate.getInternalKey();
        String elementDisplayName = getStringValue(elementToCreate, displayNameField);
        String elementFirstName = getStringValue(elementToCreate, firstNameField);
        String elementLastName = getStringValue(elementToCreate, lastNameField);
        String elementSid = getStringValue(elementToCreate, sidNameField);

        String insertSql = formatter.formatInsert(
            INSERT_ELEMENT_TARGET
        ,   INSERT_ELEMENT_SOURCE
        ,   null
        );
        PreparedStatement ps = con.prepareStatement(insertSql);

        ps.setLong(1, elementId.longValue());
        ps.setString(2, elementDisplayName);
        ps.setString(3, elementFirstName);
        ps.setString(4, elementLastName);
        ps.setString(5, elementSid);
        ps.setLong(6, fromDate.getTime());
        ps.setLong(7, UnmodifiableDate.END_OF_TIME.getTime());
        ps.execute();
        ps.close();
    }

    /**
     * @see BaseDictionaryResourceCacheImpl#closeGroup(Connection, IMGroup, Date)
     */
    protected void closeGroup(Connection con, IMGroup elementToClose, Date closeDate) throws SQLException {
        Long groupId = elementToClose.getInternalKey();
        PreparedStatement ps = con.prepareStatement(CLOSE_GROUP_SQL);
        ps.setLong(1, closeDate.getTime());
        ps.setLong(2, groupId.longValue());
        ps.setLong(3, UnmodifiableDate.END_OF_TIME.getTime());
        ps.execute();
        ps.close();
    }

    /**
     * @see BaseDictionaryResourceCacheImpl#createNewGroup(Connection, IMGroup, Date, String)
     */
    protected void createNewGroup(Connection con, IMGroup elementToCreate, Date fromDate, IMassDMLFormatter formatter) throws SQLException {
        Long groupId = elementToCreate.getInternalKey();
        String groupName = elementToCreate.getUniqueName();
        String displayName = elementToCreate.getDisplayName();
        IEnrollment enrollment = elementToCreate.getEnrollment();
        String enrollmentType = enrollment.getType();

        String insertSql = formatter.formatInsert(
            INSERT_GROUP_TARGET
        ,   INSERT_GROUP_SOURCE
        ,   null
        );
        PreparedStatement ps = con.prepareStatement(insertSql);

        ps.setLong(1, groupId.longValue());
        ps.setString(2, groupName);
        ps.setString(3, displayName);
        ps.setLong(4, fromDate.getTime());
        ps.setLong(5, UnmodifiableDate.END_OF_TIME.getTime());
        ps.setString(6, enrollmentType);
        ps.execute();
        ps.close();
    }
    
    /**
     * @throws SQLException 
     * @see BaseDictionaryResourceCacheImpl#closeGroupMember(Connection, IMGroup, Long, Date)
     */
    protected void closeGroupMember(Connection con, IMGroup parentGroup, Long memberKey, Date closeDate) throws SQLException {
        Long groupId = parentGroup.getInternalKey();        
        PreparedStatement ps = con.prepareStatement(CLOSE_GROUP_MEMBER_SQL);
        ps.setLong(1, closeDate.getTime());
        ps.setLong(2, groupId.longValue());
        ps.setLong(3, memberKey.longValue());
        ps.setLong(4, UnmodifiableDate.END_OF_TIME.getTime());
        ps.execute();
        ps.close();
    }

    /**
     * @throws SQLException 
     * @see BaseDictionaryResourceCacheImpl#createNewGroupMember(Connection, IMGroup, Long, Date)
     */
    protected void createNewGroupMember(Connection con, IMassDMLFormatter formatter, IMGroup parentGroup, Long memberKey, Date fromDate) throws SQLException {
        Long groupId = parentGroup.getInternalKey();

        String insertSql = formatter.formatInsert(
            INSERT_MEMBER_TARGET
        ,   INSERT_MEMBER_SOURCE
        ,   null
        );
        PreparedStatement ps = con.prepareStatement(insertSql);
        
        ps.setLong(1, groupId.longValue());
        ps.setLong(2, memberKey.longValue());
        ps.setLong(3, fromDate.getTime());
        ps.setLong(4, UnmodifiableDate.END_OF_TIME.getTime());
        ps.execute();
        ps.close();
    }
}
