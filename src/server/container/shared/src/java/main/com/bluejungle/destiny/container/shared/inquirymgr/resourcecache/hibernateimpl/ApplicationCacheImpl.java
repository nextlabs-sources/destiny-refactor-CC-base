/*
 * Created on Mar 23, 2005
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
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.datastore.hibernate.utils.IMassDMLFormatter;
import com.bluejungle.framework.utils.UnmodifiableDate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * This is the application cache implementation class. The application cache
 * manages cached application provided by the dictionary.
 *
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/inquirymgr/resourcecache/hibernateimpl/PolicyCacheImpl.java#1 $
 */

public class ApplicationCacheImpl extends BaseDictionaryResourceCacheImpl {

    private static final String NAME_INTERNAL_FIELD_NAME = "uniqueName";

    /**
     * SQL queries
     */
    private static final String INSERT_ELEMENT_TARGET = "CACHED_APPLICATION ($id$ original_id, name, active_from, active_to) ";
    private static final String INSERT_ELEMENT_SOURCE = "$"+CACHE_TABLE_SEQUENCE+"$?, ?, ?, ? ";
    private static final String CLOSE_ELEMENT_SQL = "update CACHED_APPLICATION set active_to=? WHERE original_id=? AND active_to=?";

    private IElementType elementType;
    private IElementField appNameField;

    /**
     * Saves the id and application name field from the dictionary
     *
     * @see IInitializable#init()
     */
    public void init() {
        super.init();
        try {
            this.elementType = getDictionary().getType(ElementTypeEnumType.APPLICATION.getName());
            //TODO - use enum here
            this.appNameField = this.elementType.getField(NAME_INTERNAL_FIELD_NAME);
        } catch (DictionaryException e) {
            getLog().error("unable to retrieve application id and application name field names in dictionary", e);
            throw new IllegalStateException("unable to retrieve application id and application name field names in dictionary");
        }
    }

    /**
     * Closes an existing application element
     *
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
     * Creates a new application element
     *
     * @see BaseDictionaryResourceCacheImpl#createNewElement(Connection, IElement, Date)
     */
    protected void createNewElement(Connection con, IElement elementToCreate, Date fromDate, IMassDMLFormatter formatter) throws SQLException {
        Long elementId = elementToCreate.getInternalKey();
        String elementName = getStringValue(elementToCreate, appNameField);

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
     * @see BaseDictionaryResourceCacheImpl#getElementType()
     */
    protected IElementType getElementType() {
        return this.elementType;
    }

    /**
     * @see BaseResourceCacheImpl#getResourceCacheType()
     */
    protected ResourceCacheType getResourceCacheType() {
        return ResourceCacheType.APPLICATION;
    }
}