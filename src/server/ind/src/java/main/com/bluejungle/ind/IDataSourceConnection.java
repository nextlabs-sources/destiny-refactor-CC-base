/*
 * Created on Jan 23, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/IDataSourceConnection.java#1 $
 */

public interface IDataSourceConnection {

    public String getServerName();
    public void setServerName(String servername);
    
    public int getPortNumber();
    public void setPortNumber(int port);
    
    public String getURL();
    public void setURL(String url);
    
    public String getUserName();
    public void setUserName(String name);
    
    public String getPassword();
    public void setPassword(String password);
    
    public String getDomainName();
    public void setDomainName(String name);
    
}