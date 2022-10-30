/*
 * Created on Jan 19, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.sharepoint.impl;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.rpc.ServiceException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.FileProvider;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.microsoft.schemas.sharepoint.soap.SiteDataSoap12Stub;
import com.microsoft.schemas.sharepoint.soap.GetListCollectionResponse;
import com.microsoft.schemas.sharepoint.soap.GetListCollection;
import com.microsoft.schemas.sharepoint.soap.SiteDataLocator;
import com.microsoft.schemas.sharepoint.soap.SiteDataSoapStub;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/sharepoint/impl/SharePointConnectionTester.java#1 $
 */

public class SharePointConnectionTester {

    // password decoder
    private static final ReversibleEncryptor CIPHER = new ReversibleEncryptor();
    private static final String SHAREPOINT_CLIENT_WSDD = "com/microsoft/schemas/sharepoint/soap/client-cfg.wsdd";
    private static final String SITEDATA_ENDPOINT = "/_vti_bin/SiteData.asmx";
    
    public static void testConnection(String portalUrl, String login, String password, String domain)
			throws EnrollmentValidationException {
        try {
			SiteDataSoap12Stub binding = getSiteDataSoap12Stub(portalUrl, login, password, domain);
			// Test operation
			GetListCollectionResponse value = binding.getListCollection(new GetListCollection());
			value.getVLists();
		} catch (Exception e) {
			throw new EnrollmentValidationException("Connection to Sharepoint server failed." + portalUrl, e);
		}
    }
    
    public static SiteDataSoap12Stub getSiteDataSoap12Stub(String portalURL, String login,
			String password, String domain) throws EnrollmentValidationException {
        EngineConfiguration config = new FileProvider(SHAREPOINT_CLIENT_WSDD);
        SiteDataLocator locator = new SiteDataLocator(config);
        String endpointAddress = fixHttpsPort(portalURL) + SITEDATA_ENDPOINT;
        locator.setSiteDataSoap12EndpointAddress(endpointAddress);
        try {
        	SiteDataSoap12Stub binding = (SiteDataSoap12Stub) locator.getSiteDataSoap12();
            binding.setUsername(domain + "\\" + login);
            String passwd = CIPHER.decrypt(password);
            binding.setPassword(passwd);
            
            // Time out after 5 minutes
            binding.setTimeout(600000);
            return binding;
        }
        catch (ServiceException jre) {
            throw new EnrollmentValidationException("Failed to conntect sharepoint server " + endpointAddress, jre);
        }
    }
    
    private static String fixHttpsPort(String urlStr){
    	//    	bug fix 5549
		//set the https default port to 443
		try {
			URL url = new URL(urlStr);
			if (url.getProtocol().equalsIgnoreCase("https") && url.getPort() == -1) {
				url = new URL(url.getProtocol(), url.getHost(), 443, url.getFile());
				return url.toExternalForm();
			}else{
				return urlStr;
			}
		} catch (MalformedURLException e) {
			return urlStr;
		}
    }
    
    public static SiteDataSoapStub getSiteDataSoapStub(String portalUrl, String login, String password, 
            String domain) throws EnrollmentValidationException {
        try {
            EngineConfiguration config = new FileProvider(SHAREPOINT_CLIENT_WSDD);
            SiteDataLocator locator = new SiteDataLocator(config);
            String endpointAddress = fixHttpsPort(portalUrl) + SITEDATA_ENDPOINT;
            locator.setSiteDataSoap12EndpointAddress(endpointAddress);
            SiteDataSoapStub binding = null;
            try {
                binding = (SiteDataSoapStub) locator.getSiteDataSoap();
                binding.setUsername(domain + "\\" + login);
                String passwd = CIPHER.decrypt(password);
                binding.setPassword(passwd);
            }
            catch (javax.xml.rpc.ServiceException jre) {
                throw new EnrollmentValidationException("Failed to conntect sharepoint server " + endpointAddress, jre);
            }
            
            // Time out after 5 minutes
            binding.setTimeout(600000);
            return binding;
        }
        catch ( Exception e ) {
            throw new EnrollmentValidationException("Failed to conntect sharepoint server", e);
        }
        
    }
}
