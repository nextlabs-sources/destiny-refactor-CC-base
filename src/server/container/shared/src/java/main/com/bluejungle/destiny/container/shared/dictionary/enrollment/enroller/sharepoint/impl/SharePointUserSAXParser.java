/*
 * Created on Jan 31, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/impl/SharePointUserSAXParser.java#1 $
 */

public class SharePointUserSAXParser extends DefaultHandler {
    static final String USER_TAG = "User";
    
    //TODO move those tags to def
    static final String USER_ID_TAG = "ID";
    static final String USER_SID_TAG = "Sid";
    static final String USER_NAME_TAG = "Name";
    static final String LOGIN_NAME_TAG = "LoginName";
    static final String EMAIL_TAG = "Email";
    static final String NOTES_TAG = "Notes";
    static final String IS_SITE_ADMIN_TAG = "IsSiteAdmin";
    static final String IS_DOMAIN_GROUP_TAG = "IsDomainGroup";
    
    private SharePointElementCreator creator = null;
    
    public SharePointUserSAXParser(SharePointElementCreator creator) {
        this.creator = creator;
    }
    
    // Parser calls this once at the beginning of a document
    public void startDocument() throws SAXException {
        
    }

    // Parser calls this for each element in a document
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        if (USER_TAG.equals(qName)) {
            String userID = atts.getValue(USER_ID_TAG);
            String userName = atts.getValue(USER_NAME_TAG);
            String SID = atts.getValue(USER_SID_TAG);
            boolean isDomainGroup = Boolean.parseBoolean(atts.getValue(IS_DOMAIN_GROUP_TAG)); 
            
            try {
                creator.addMemberToCurrentGroup(userID, userName, SID, isDomainGroup);
            } catch (EnrollmentSyncException e) {
                throw new SAXException(e);
            }
        }
    }

    // Parser calls this once after parsing a document
    public void endDocument() throws SAXException {
    }
}
