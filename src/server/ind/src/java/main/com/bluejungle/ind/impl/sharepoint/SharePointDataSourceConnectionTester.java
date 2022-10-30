/*
 * Created on Jan 23, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl.sharepoint;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;

//import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.bluejungle.ind.INDException;
import com.microsoft.schemas.sharepoint.soap.GetListCollection;
import com.microsoft.schemas.sharepoint.soap.GetListCollectionResponse;
import com.microsoft.schemas.sharepoint.soap.SiteDataLocator;
import com.microsoft.schemas.sharepoint.soap.SiteDataSoap12Stub;
import com.microsoft.schemas.sharepoint.soap.WebsLocator;
import com.microsoft.schemas.sharepoint.soap.WebsSoap12Stub;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/impl/sharepoint/SharePointDataSourceConnectionTester.java#1 $
 */

public class SharePointDataSourceConnectionTester {

    // password decoder
    //private static final ReversibleEncryptor CIPHER = new ReversibleEncryptor();
    private static final String SHAREPOINT_CLIENT_WSDD = "com/microsoft/schemas/sharepoint/soap/client-cfg.wsdd";
    private static final String SITEDATA_ENDPOINT = "/_vti_bin/SiteData.asmx";
    private static final String GETWEB_ENDPOINT = "/_vti_bin/Webs.asmx";
    
    public static void testConnection(String portalURL, 
            String login, String password, String domain) throws INDException {
        try {
            SiteDataSoap12Stub binding = getSiteDataSoap12Stub(portalURL, login, password, domain);
            // Test operation
            GetListCollectionResponse value = null;
            value = binding.getListCollection(new GetListCollection());
            value.getVLists();         
        }
        catch ( Exception e ) {
            throw new INDException (e);
        }
    }
    
    public static SiteDataSoap12Stub getSiteDataSoap12Stub(String portalURL, 
            String login, String password, String domain) throws INDException {
        try {
            EngineConfiguration config = new FileProvider(SHAREPOINT_CLIENT_WSDD);
            SiteDataLocator locator = new SiteDataLocator(config);
            String endpointAddress = portalURL + SITEDATA_ENDPOINT;
            locator.setSiteDataSoap12EndpointAddress(endpointAddress);
            SiteDataSoap12Stub binding = null;
            try {
                binding = (SiteDataSoap12Stub) locator.getSiteDataSoap12();
                binding.setUsername(domain + "\\" + login);
                String passwd = password;
                binding.setPassword(passwd);
            }
            catch (javax.xml.rpc.ServiceException jre) {
                throw new INDException("Failed to conntect sharepoint server " + endpointAddress, jre);
            }
            
            // Time out after 5 minutes
            binding.setTimeout(300000);
            return binding;
        }
        catch ( Exception e ) {
            throw new INDException("Failed to conntect sharepoint server", e);
        }
        
    }
    
    public static WebsSoap12Stub getWebsSoap12Stub(String portalURL, 
            String login, String password, String domain) throws INDException {
        try {
            EngineConfiguration config = new FileProvider(SHAREPOINT_CLIENT_WSDD);
            WebsLocator locator = new WebsLocator(config);
            String endpointAddress = portalURL + GETWEB_ENDPOINT;
            locator.setWebsSoap12EndpointAddress(endpointAddress);
            WebsSoap12Stub binding = null;
            try {
                binding = (WebsSoap12Stub) locator.getWebsSoap12();
                binding.setUsername(domain + "\\" + login);
                String passwd = password;
                binding.setPassword(passwd);
            }
            catch (javax.xml.rpc.ServiceException jre) {
                throw new INDException("Failed to conntect sharepoint server " + endpointAddress, jre);
            }
            
            // Time out after 5 minutes
            binding.setTimeout(300000);
            return binding;
        }
        catch ( Exception e ) {
            throw new INDException("Failed to conntect sharepoint server", e);
        }
        
    }
}
