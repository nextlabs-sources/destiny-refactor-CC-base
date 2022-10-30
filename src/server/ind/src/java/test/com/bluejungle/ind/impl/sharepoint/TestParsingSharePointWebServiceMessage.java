/*
 * Created on Feb 21, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl.sharepoint;

import junit.framework.TestCase;


/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/test/com/bluejungle/ind/impl/sharepoint/TestParsingSharePointWebServiceMessage.java#1 $
 */

public class TestParsingSharePointWebServiceMessage extends TestCase {

    private static final String SHAREPOINT_MESSAGE = 
"<SharePoint>"+
"<Webs>"+
"<SharePointServices.SiteData._sWebWithTime Url=\"http://sharepoint2007\" LastModified=\"2/21/2007 8:16:01 PM\" />"+
"<SharePointServices.SiteData._sWebWithTime Url=\"http://sharepoint2007/AAA\" LastModified=\"2/21/2007 8:16:01 PM\" />"+
"</Webs>"+
"<GroupsPerSite>"+
"<Groups>"+
"<Group ID=\"39\" Name=\"AAA Group\" Description=\"\" OwnerID=\"1\" OwnerIsUser=\"True\" />" +
"</Groups>"+
"</GroupsPerSite>"+
"<UsersOfGroups>"+
"<Users>"+
"<User ID=\"40\" Sid=\"S-1-5-21-830805687-550985140-3285839444-1310\" Name=\"Andy Han\" LoginName=\"TEST\\ahan\" Email=\"\" Notes=\"\" IsSiteAdmin=\"False\" IsDomainGroup=\"False\" />"+
"<User ID=\"1\" Sid=\"S-1-5-21-1787816742-3064201231-2476453273-500\" Name=\"SHAREPOINT2007\\Administrator\" LoginName=\"SHAREPOINT2007\\administrator\" Email=\"\" Notes=\"\" IsSiteAdmin=\"True\" IsDomainGroup=\"False\" />"+
"</Users>"+
"</UsersOfGroups>" +
"</SharePoint>";
    
    /**
     * Constructor
     * @param arg0
     */
    public TestParsingSharePointWebServiceMessage(String arg0) {
        super(arg0);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
