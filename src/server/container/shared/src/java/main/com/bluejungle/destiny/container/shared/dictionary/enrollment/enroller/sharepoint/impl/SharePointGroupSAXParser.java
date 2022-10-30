/*
 * Created on Jan 31, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.microsoft.schemas.sharepoint.soap.ArrayOfString;


/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/impl/SharePointGroupSAXParser.java#1 $
 */

public class SharePointGroupSAXParser extends DefaultHandler {
    static final String GROUP_TAG = "Group";
    
    //TODO move those tags to def
    static final String GROUP_ID_TAG = "ID";
    static final String GROUP_NAME_TAG = "Name";
    static final String DESCRIPTION_TAG = "Description";
    static final String GROUP_OWNERID_TAG = "OwnerID";
    static final String GROUP_OWNERISUSER_TAG = "OwnerIsUser";
    
    private ArrayOfString users = null;
    private int groupIndex = 0;
    private XMLReader usersParser = null;
    private SharePointElementCreator creator = null;
    
    public SharePointGroupSAXParser(ArrayOfString users, SharePointElementCreator creator) throws SAXException {
        this.users = users;
        this.creator = creator;
        
        try {
            // Create a JAXP SAXParserFactory and configure it
            SAXParserFactory spf = SAXParserFactory.newInstance();

            // Create a JAXP SAXParser
            SAXParser saxParser = spf.newSAXParser();

            // Get the encapsulated SAX XMLReader
            this.usersParser = saxParser.getXMLReader();
            this.usersParser.setContentHandler(new SharePointUserSAXParser(creator));
            this.usersParser.setErrorHandler(new MyErrorHandler(LogFactory.getLog(SharePointGroupSAXParser.class)));

        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        } 
    }
    
    // Parser calls this once at the beginning of a document
    public void startDocument() throws SAXException {
    }

    // Parser calls this for each element in a document
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {
        if ( qName.equals(GROUP_TAG)) {
            String id = atts.getValue(GROUP_ID_TAG);
            String name = atts.getValue(GROUP_NAME_TAG);
            String ownerID = atts.getValue(GROUP_OWNERID_TAG);
            String isOwnerUser = atts.getValue(GROUP_OWNERISUSER_TAG); 
            
            String usersForGroup = users.getString(groupIndex);
            try {
                creator.addGroup(id, name, ownerID, 
                                    Boolean.parseBoolean(isOwnerUser.toLowerCase()));
                this.usersParser.parse(new InputSource(new StringReader(usersForGroup)));               
                creator.saveGroupMembers();
            } catch (EnrollmentSyncException e) {
                throw new SAXException(e);
            } catch (IOException e) {
                throw new SAXException(e);
            }
            this.groupIndex ++;
        }
    }

    // Parser calls this once after parsing a document
    public void endDocument() throws SAXException {

    }

    // Error handler to report errors and warnings
    public static class MyErrorHandler implements ErrorHandler {
        /** Error handler output goes here */
        private final Log log;

        public MyErrorHandler(Log log) {
            this.log = log;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "URI=" + systemId +
                " Line=" + spe.getLineNumber() +
                ": " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            log.warn(getParseExceptionInfo(spe));
        }
        
        public void error(SAXParseException spe) throws SAXException {
            String message = getParseExceptionInfo(spe);
            log.error(message);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = getParseExceptionInfo(spe);
            log.fatal(message);
            throw new SAXException(message);
        }
    }
    
}
