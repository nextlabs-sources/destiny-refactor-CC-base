/*
 * Created on Feb 10, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bluejungle.destiny.server.shared.configuration.IConnectionPoolConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.IRepositoryConfigurationDO;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dms/com/bluejungle/destiny/container/dms/components/configmgr/filebasedimpl/DataSourceConfigurationList.java#1 $
 */

public class RepositoryConfigurationList {

    /*
     * List of data sources
     */
    private List<RepositoryConfigurationDO> repositoryList;
    private List<ConnectionPoolConfigurationDO> connectionPoolList;

    /**
     * Constructor
     *  
     */
    public RepositoryConfigurationList() {
        super();
        this.repositoryList = new ArrayList<RepositoryConfigurationDO>();
        this.connectionPoolList = new ArrayList<ConnectionPoolConfigurationDO>();
    }

    /**
     * Returns an array of the data sources
     * 
     * @return array of datasources
     */
    public RepositoryConfigurationDO[] getRepositoriesAsArray() {
        RepositoryConfigurationDO[] array = null;
        if (this.repositoryList != null) {
            array = this.repositoryList.toArray(new RepositoryConfigurationDO[] {});
        }
        return array;
    }

    /**
     * Returns a set of the data sources
     * 
     * @return set of data sources
     */
    public Set<? extends IRepositoryConfigurationDO> getRepositoriesAsSet() {
        Set<RepositoryConfigurationDO> set = null;
        if (this.repositoryList != null) {
            set = new HashSet<RepositoryConfigurationDO>();
            set.addAll(this.repositoryList);
        }
        return set;
    }

    /**
     * Adds a repository to the list of data sources
     * 
     * @param config
     */
    public void addRepository(RepositoryConfigurationDO config) {
        this.repositoryList.add(config);
    }

    /**
     * Adds a connection pool to the list of connection pools
     * 
     * @param config
     */
    public void addConnectionPool(ConnectionPoolConfigurationDO config) {
        this.connectionPoolList.add(config);
    }

    /**
     * Returns an array of the connection pools
     * 
     * @return array of connection pools
     */
    public ConnectionPoolConfigurationDO[] getConnectionPoolsAsArray() {
        ConnectionPoolConfigurationDO[] array = null;
        if (this.connectionPoolList != null) {
			array = this.connectionPoolList.toArray(new ConnectionPoolConfigurationDO[] {});
		}
        return array;
    }

    /**
     * Returns a set of the connection pools
     * 
     * @return set of connection pools
     */
    public Set<? extends IConnectionPoolConfigurationDO> getConnectionPoolsAsSet() {
        Set<ConnectionPoolConfigurationDO> set = null;
        if (this.connectionPoolList != null) {
            set = new HashSet<ConnectionPoolConfigurationDO>();
            set.addAll(this.connectionPoolList);
        }
        return set;
    }
}