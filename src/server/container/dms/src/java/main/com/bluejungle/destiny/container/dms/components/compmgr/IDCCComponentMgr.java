/*
 * Created on Oct 25, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *  
 */
package com.bluejungle.destiny.container.dms.components.compmgr;

import java.util.List;

import com.bluejungle.destiny.container.dcc.DCCComponentEnumType;
import com.bluejungle.destiny.container.dms.data.ComponentDO;
import com.bluejungle.destiny.server.shared.registration.IComponentHeartbeatInfo;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;

/**
 * CRUD library interface for the Component Manager
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/IDCCComponentMgr.java#8 $
 */
public interface IDCCComponentMgr extends IInitializable, ILogEnabled, IDisposable {

    public static final String COMP_NAME = "regComponent";

    /**
     * Register a DCC component
     * 
     * @param component
     *            DCC component to be added to the component repository
     * @throws DataSourceException
     */
    public void enableComponent(ComponentDO component) throws DataSourceException;

    /**
     * Unregister a DCC component
     * 
     * @param component
     *            DCC component to be disabled in the component repository
     * @throws DataSourceException
     */
    public void disableComponent(ComponentDO component) throws DataSourceException;

    /**
     * Registers a component heartbeat and returns any updates for the
     * component.
     * 
     * @param info
     * @throws DataSourceException
     */
    public void confirmActive(IComponentHeartbeatInfo info) throws DataSourceException;

    /**
     * Returns a unique list of DCSF locations. This API allows to locate all
     * the DCSF modules within the DCC unit
     * 
     * @return a set of string
     * @throws DataSourceException
     */
    public List<IDCCComponentDO> getDCSFs() throws DataSourceException;

    /**
     * Returns the component with the given name.
     * 
     * @param name
     * @return DCC Component
     * @throws DataSourceException
     */
    public ComponentDO getComponentByName(String name) throws DataSourceException;

    /**
     * Returns the list of all registered components
     * 
     * @return a list of all DCC component instances
     * @throws DataSourceException
     *             if a persistence error ocurrs
     */
    public List<IDCCComponentDO> getComponents() throws DataSourceException;

    /**
     * Returns a list of component by type
     * 
     * @param type
     *            type of the DCC component
     * @return a list of matching DCC component instances.
     * @throws DataSourceException
     *             if the query fails
     */
    public List<IDCCComponentDO> getComponentByType(DCCComponentEnumType type) throws DataSourceException;

    /**
     * Returns the dcsf component with the given callback URL.
     * 
     * @param callback
     * @return DCSF Component
     * @throws DataSourceException
     */
    public ComponentDO getDCSFByCallbackURL(String url) throws DataSourceException;

    /**
     * Cleans the database tables associated with component registration.
     * Currently the only use for this is with JUnit testing.
     * 
     * @throws DataSourceException
     */
    public void clearAll() throws DataSourceException;
}