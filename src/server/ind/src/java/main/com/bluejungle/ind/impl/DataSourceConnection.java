/*
 * Created on Jan 23, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl;

import com.bluejungle.ind.IDataSourceConnection;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/impl/DataSourceConnection.java#1 $
 */

public class DataSourceConnection implements IDataSourceConnection {

    protected String serverName;
    protected int port;
    protected String userName;
    protected String password;
    protected String domain;
    protected String url;
    
    /**
     * Constructor
     */
    public DataSourceConnection(String server, int port, String userName, String password) {
        this.serverName = server;
        this.port = port;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Constructor
     */
    public DataSourceConnection(String server, int port, String userName, 
            String password, String domain) {
        this.serverName = server;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.domain = domain;
    }
    
    /**
     * Constructor
     */
    public DataSourceConnection(String url, String userName, 
            String password, String domain) {
        this.url = url;
        this.userName = userName;
        this.password = password;
        this.domain = domain;
    }
    
    /**
     * @see com.bluejungle.ind.IDataSourceConnection#getDomainName()
     */
    public String getDomainName() {
        return domain;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceConnection#getPassword()
     */
    public String getPassword() {
        return password;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceConnection#getPortNumber()
     */
    public int getPortNumber() {
        return port;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceConnection#getServerName()
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceConnection#getURL()
     */
    public String getURL() {
        return url;
    }
    
    /**
     * @see com.bluejungle.ind.IDataSourceConnection#setURL(java.lang.String)
     */
    public void setURL(String url) {
        this.url = url;
    }
    
    /**
     * @see com.bluejungle.ind.IDataSourceConnection#getUserName()
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceConnection#setDomainName(java.lang.String)
     */
    public void setDomainName(String name) {
        this.domain = name;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceConnection#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceConnection#setPortNumber(int)
     */
    public void setPortNumber(int port) {
        this.port = port;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceConnection#setServerName(java.lang.String)
     */
    public void setServerName(String servername) {
        this.serverName = servername;
    }

    /**
     * @see com.bluejungle.ind.IDataSourceConnection#setUserName(java.lang.String)
     */
    public void setUserName(String name) {
        this.userName = name;
    }

}
