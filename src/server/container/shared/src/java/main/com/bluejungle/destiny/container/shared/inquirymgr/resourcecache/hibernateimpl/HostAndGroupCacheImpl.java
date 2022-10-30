/*
 * Created on Mar 28, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.inquirymgr.resourcecache.hibernateimpl;

import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IElement;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.datastore.hibernate.utils.IMassDMLFormatter;
import com.bluejungle.framework.utils.UnmodifiableDate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * This is the host cache implementation class. The host cache manages cached
 * host provided by the policy framework.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/HostAndGroupCacheImpl.java#3 $
 */

public class HostAndGroupCacheImpl extends BaseDictionaryResourceCacheImpl {

    private static final String INSERT_ELEMENT_TARGET = "CACHED_HOST ($id$original_id, name, active_from, active_to) ";
    private static final String INSERT_ELEMENT_SOURCE = "$"+CACHE_TABLE_SEQUENCE+"$?, ?, ?, ? ";
    private static final String CLOSE_ELEMENT_SQL = "update CACHED_HOST set active_to=? WHERE original_id=? AND active_to=?";

    private static final String INSERT_GROUP_TARGET = "CACHED_HOSTGROUP ($id$original_id, name, active_from, active_to) ";
    private static final String INSERT_GROUP_SOURCE = "$"+CACHE_TABLE_SEQUENCE+"$?, ?, ?, ? ";
    private static final String CLOSE_GROUP_SQL = "update CACHED_HOSTGROUP set active_to=? WHERE original_id=? AND active_to=?";

    private static final String INSERT_GROUP_MEMBER_SQL = "insert into CACHED_HOST_HOSTGROUP (hostgroup_id, host_id, active_from, active_to) VALUES (?, ?, ?, ?)";
    private static final String CLOSE_GROUP_MEMBER_SQL = "update CACHED_HOST_HOSTGROUP set active_to=? WHERE hostgroup_id=? AND host_id=? AND active_to=?";
    
    private static final String HOST_NAME_INTERNAL_FIELD_NAME = "dnsName";
    
    private IElementType elementType;
    private IElementField hostNameField;

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        try {
            this.elementType = getDictionary().getType(ElementTypeEnumType.COMPUTER.getName());
            // TODO - Used enums in server dictionary.  These should be moved to common dictionary?
            this.hostNameField = this.elementType.getField(HOST_NAME_INTERNAL_FIELD_NAME);
        } catch (DictionaryException exception) {
            getLog().error("unable to retrieve host id and host dns name field names in dictionary", exception);
        }
    } 

    /**
     * @see BaseDictionaryResourceCacheImpl#getElementType()
     */
    protected IElementType getElementType() {
        return this.elementType;
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

    protected void createNewElement(Connection con, IElement elementToCreate, Date fromDate, IMassDMLFormatter formatter) throws SQLException {
        Long elementId = elementToCreate.getInternalKey();
        String elementName = getStringValue(elementToCreate, hostNameField);

        String insertSql = formatter.formatInsert(
            INSERT_ELEMENT_TARGET
        ,   INSERT_ELEMENT_SOURCE
        ,   null
        );
        PreparedStatement ps = con.prepareStatement(insertSql);
        
        ps.setLong(1, elementId.longValue());
        ps.setString(2, elementName);
        ps.setLong(3, fromDate.getTime());
        ps.setLong(4, UnmodifiableDate.END_OF_TIME.getTime());
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
        String groupName = elementToCreate.getName();
        
        String insertSql = formatter.formatInsert(
            INSERT_GROUP_TARGET
        ,   INSERT_GROUP_SOURCE
        ,   null
        );
        PreparedStatement ps = con.prepareStatement(insertSql);

        ps.setLong(1, groupId.longValue());
        ps.setString(2, groupName);
        ps.setLong(3, fromDate.getTime());
        ps.setLong(4, UnmodifiableDate.END_OF_TIME.getTime());
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
    protected void createNewGroupMember(Connection con, IMGroup parentGroup, Long memberKey, Date fromDate) throws SQLException {
        Long groupId = parentGroup.getInternalKey();
        
        PreparedStatement ps = con.prepareStatement(INSERT_GROUP_MEMBER_SQL);

        ps.setLong(1, groupId.longValue());
        ps.setLong(2, memberKey.longValue());
        ps.setLong(3, fromDate.getTime());
        ps.setLong(4, UnmodifiableDate.END_OF_TIME.getTime());
        ps.execute();
        ps.close();
    }

    /**
     * @see BaseResourceCacheImpl#getResourceCacheType()
     */
    protected ResourceCacheType getResourceCacheType() {
        return ResourceCacheType.HOST;
    }

    
    /**
     * @see BaseDictionaryResourceCacheImpl#isSupportingGroups()
     */
    protected boolean isSupportingGroups() {
        return false; // For now, we don't support groups for hosts.  Though, most of the host group caching code is implemented above and NOT tested
    }

}