/*
 * Created on Jan 18, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint;

import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl.SharePointConnectionTester;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl.SharePointElementCreator;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl.SharePointEnrollmentInitalizer;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl.SharePointEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl.SharePointEnrollmentWrapperImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl.SharePointGroupSAXParser;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.microsoft.schemas.sharepoint.soap.ArrayOfString;
import com.microsoft.schemas.sharepoint.soap.GetSite;
import com.microsoft.schemas.sharepoint.soap.GetSiteResponse;
import com.microsoft.schemas.sharepoint.soap.SiteDataSoap12Stub;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/SharePointEnroller.java#1 $
 */

public class SharePointEnroller extends EnrollerBase {
    
    private static final Log LOG = LogFactory.getLog(SharePointEnroller.class);

    private SharePointEnrollmentWrapperImpl wrapper;
    private XMLReader xmlReader;
    
    public String getEnrollmentType() {
        return "Sharepoint";
    }

    @Override
    protected Log getLog() {
        return LOG;
    }
    
    public void process(IEnrollment enrollment, Map<String, String[]> properties,
            IDictionary dictionary) throws EnrollmentValidationException, DictionaryException {
        super.process(enrollment, properties, dictionary);
        new SharePointEnrollmentInitalizer(enrollment, properties, dictionary).setup();
    }
    
    @Override
    protected void preSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session) throws EnrollmentValidationException, DictionaryException {
        
        wrapper = new SharePointEnrollmentWrapperImpl(enrollment, dictionary);
        
        // Create a JAXP SAXParserFactory and configure it
        SAXParserFactory spf = SAXParserFactory.newInstance();
            
        try {
            // Create a JAXP SAXParser
            SAXParser saxParser = spf.newSAXParser();

            // Get the encapsulated SAX XMLReader
            xmlReader = saxParser.getXMLReader();
        } catch (ParserConfigurationException e) {
            throw new EnrollmentValidationException(e);
        } catch (SAXException e) {
            throw new EnrollmentValidationException(e);
        }
        
        xmlReader.setErrorHandler(new SharePointGroupSAXParser.MyErrorHandler(LOG));
    }

    @Override
    protected void internalSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session, SyncResult syncResult) throws EnrollmentSyncException {
        syncResult.newCount = 0;
        syncResult.deleteCount = 0;

        syncResult.changeCount = 0;
        syncResult.nochangeCount = 0;
        syncResult.ignoreCount = 0;
        syncResult.failedCount = 0;
        syncResult.totalCount  = 0;

        
        String login     = enrollment.getStrProperty(SharePointEnrollmentProperties.LOGIN_PROPERTY);
        String password  = enrollment.getStrProperty(SharePointEnrollmentProperties.PASSWORD_PROPERTY);
        String domain    = enrollment.getStrProperty(SharePointEnrollmentProperties.DOMAIN_PROPERTY);
        String[] portals = enrollment.getStrArrayProperty(SharePointEnrollmentProperties.PORTALS_PROPERTY);
        
        String lastEntry = null;
        try {
            for (String portal : portals) {
                LOG.debug("parsing portal: " + portal);
                lastEntry = "portal=" + portal;
                
                SharePointElementCreator creator = getSharePointElementCreator(wrapper);
                
                SiteDataSoap12Stub binding = 
                    SharePointConnectionTester.getSiteDataSoap12Stub(portal, login, password, domain);
                // Test operation
                GetSiteResponse value = binding.getSite(new GetSite());
                String groups = value.getStrGroups();
                ArrayOfString users = value.getVGroups();
                if ( groups != null ) {
                    lastEntry = "groups=" + groups;
                    xmlReader.setContentHandler(new SharePointGroupSAXParser(users, creator));
                    xmlReader.parse(new InputSource(new StringReader(groups)));
                    
                    Collection<IElementBase> groupElements = creator.getGroups();
                    
                    session.beginTransaction();
                    session.saveElements(groupElements);
                    session.commit();
                    
                    wrapper.saveElementIDs(groupElements);
                    
                    syncResult.newCount += groupElements.size();
                }
            }
            LOG.debug("All portal is done. Removing DNE entries.");
            int deletedCount = wrapper.removeElementIDs(session);
            syncResult.deleteCount += deletedCount;
        } catch (DictionaryException e){
            throw new EnrollmentSyncException(e, lastEntry);
        } catch (EnrollmentValidationException e){
            throw new EnrollmentSyncException(e, lastEntry);
        } catch (RemoteException e) {
            throw new EnrollmentSyncException(e, lastEntry);
        } catch (SAXException e) {
            throw new EnrollmentSyncException(e, lastEntry);
        } catch (IOException e) {
            throw new EnrollmentSyncException(e, lastEntry);
        }
        syncResult.success = true;
    }
    
    // override this to provide different implementation
    protected SharePointElementCreator getSharePointElementCreator(
            ISharePointEnrollmentWrapper wrapper) throws DictionaryException {
        return new SharePointElementCreator(wrapper);
    }
}
