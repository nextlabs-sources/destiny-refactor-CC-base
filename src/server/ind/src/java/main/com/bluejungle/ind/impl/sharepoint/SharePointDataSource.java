/*
 * Created on Feb 20, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl.sharepoint;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.axis.message.MessageElement;

import com.bluejungle.ind.IDataSourceConnection;
import com.bluejungle.ind.IDataSourceType;
import com.bluejungle.ind.IExternalResource;
import com.bluejungle.ind.IExternalResourceTypes;
import com.bluejungle.ind.INDException;
import com.bluejungle.ind.impl.DataSource;
import com.bluejungle.ind.impl.ExternalResource;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.microsoft.schemas.sharepoint.soap.ArrayOf_sList;
import com.microsoft.schemas.sharepoint.soap.GetListCollection;
import com.microsoft.schemas.sharepoint.soap.GetListCollectionResponse;
import com.microsoft.schemas.sharepoint.soap.GetListItems;
import com.microsoft.schemas.sharepoint.soap.GetListItemsResponse;
import com.microsoft.schemas.sharepoint.soap.GetWebCollection;
import com.microsoft.schemas.sharepoint.soap.GetWebCollectionResponse;
import com.microsoft.schemas.sharepoint.soap.GetWebCollectionResponseGetWebCollectionResult;
import com.microsoft.schemas.sharepoint.soap.SiteDataSoap12Stub;
import com.microsoft.schemas.sharepoint.soap.WebsSoap12Stub;
import com.microsoft.schemas.sharepoint.soap._sList;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/impl/sharepoint/SharePointDataSource.java#1 $
 */

public class SharePointDataSource extends DataSource {
      
    /**
     * Constructor
     * @param name
     * @param type
     */
    public SharePointDataSource(String name, IDataSourceType type) {
        super(name, type);
    }

    public SharePointDataSource() {
        super();
    }
    
    /**
     * Set the connection info
     * @see com.bluejungle.ind.impl.DataSource#setConnection(com.bluejungle.ind.IDataSourceConnection)
     */
    @Override
    public void setConnection(IDataSourceConnection connection) throws INDException {
        super.setConnection(connection);
    }
    
    /**
     * Get all sub Sites by given parentSite URL
     * @param parentSite
     * @return List of all subsites and lists under parent site 
     * @throws INDException
     */
    public List<IExternalResource> getSitesList(String parentSite) throws INDException {
        List<IExternalResource> result = new ArrayList<IExternalResource>();
        try {
            WebsSoap12Stub soapStub = SharePointDataSourceConnectionTester.getWebsSoap12Stub(
                    parentSite, connection.getUserName(),
                    connection.getPassword(), connection.getDomainName() );
            GetWebCollectionResponse value = soapStub.getWebCollection(new GetWebCollection());
            GetWebCollectionResponseGetWebCollectionResult webs = value.getGetWebCollectionResult();
            if ( webs != null ) {
                MessageElement[] elements = webs.get_any();
                for( int i=0; i< elements.length; i++ ) {
                    Iterator itor = elements[i].getChildElements();
                    while( itor.hasNext() ) {
                        MessageElement msg = (MessageElement) itor.next();
                        String title = msg.getAttribute("Title");
                        String url = msg.getAttribute("Url");
                        result.add(new ExternalResource(null, url, url, 
                                title, IExternalResourceTypes.SITE, true));
                    }
                }
            }
        }
        catch ( RemoteException e ) {
            throw new INDException(e);
        }
        return result;
    }
    
    /**
     * Get all lists under parent site URL
     * @param parentSite URL
     * @return all lists under parent site
     * @throws INDException
     */
    public List<IExternalResource> getLists(String parentSite) throws INDException {
        List<IExternalResource> result = new ArrayList<IExternalResource>();
        try {
            GetListCollectionResponse value = null;

            SiteDataSoap12Stub soapStub = SharePointDataSourceConnectionTester.getSiteDataSoap12Stub(
                        parentSite, connection.getUserName(),
                        connection.getPassword(), connection.getDomainName() ); 
           
            value = soapStub.getListCollection(new GetListCollection());
            ArrayOf_sList lists = value.getVLists();
            if ( lists != null ) {
                _sList[] listsArray = lists.get_sList();
                for(int i=0; i<listsArray.length; i++) {
                    _sList list = listsArray[i];
                    String baseType = list.getBaseType(); 
                    boolean hasChildren = false;
                    if ( baseType.equals("DocumentLibrary") ) {
                        hasChildren = true;
                    }
                    String url = list.getDefaultViewUrl();
                    url = norm(url);
                    result.add(new ExternalResource(null, list.getInternalName(),  
                        connection.getURL()+url, list.getTitle(), 
                        IExternalResourceTypes.LIST, hasChildren));
                }
            }
            return result;
        }
        catch ( RemoteException e ) {
            throw new INDException(e);
        }
    }
    
    /**
     * Normalize the URL, trim /AllItems and /Forms/AllItems 
     * @param url
     * @return
     */
    public String norm(String url) {
        int index = url.indexOf("/AllItems.aspx");
        if ( index > 0 ) {
            int index2 = url.indexOf("/Forms/AllItems.aspx");
            if ( index2 > 0 ) {
                return url.substring(0, index2);
            }
            return url.substring(0, index);
        }
        return url;
    }
    
    /**
     * Get All list items 
     * @param id
     * @return
     * @throws INDException
     */
    public List<IExternalResource> getListItems(String id) throws INDException {
        List<IExternalResource> result = null;
        try {
            SiteDataSoap12Stub soapStub = SharePointDataSourceConnectionTester.getSiteDataSoap12Stub(
                    connection.getURL(), connection.getUserName(),
                    connection.getPassword(), connection.getDomainName() ); 
            
            GetListItemsResponse value = null;
            //String viewFields = "<ViewFields><FieldRef Name=\"ows_LinkFilename\"/>"+
            //                     "<FieldRef Name=\"ows_EncodedAbsUrl\"/></ViewFields>";
            // SharePoint views Fields is not working   Feb 28 2007
            
            value = soapStub.getListItems(
                    new GetListItems(id,null,null,null));
            String listItems = value.getGetListItemsResult();
            if ( listItems != null ) {
                SharePointXMLMessageDigester digester = new SharePointXMLMessageDigester();
                result = digester.getListItems(listItems);
                for ( IExternalResource node: result) {
                    node.setURL(connection.getURL() + node.getID());
                }
            }
            return result;
        }
        catch ( RemoteException e ) {
            throw new INDException(e);
        }
    }
    
    /**
     * Get all children of a given external resource node
     * @see com.bluejungle.ind.impl.DataSource#getResourceTreeNodeChildren(com.bluejungle.ind.IExternalResource)
     */
    public void getResourceTreeNodeChildren(IExternalResource node) throws INDException {
        
        // if the ID of node is not set, we are assuming it is root node
        if ( node.getID() == null ) {
            node.setID(super.connection.getURL());
            node.setType(IExternalResourceTypes.PORTAL);
        }       
        if ( node.getType().equals(IExternalResourceTypes.PORTAL) ) {
        	// before getting sites, convert host name in URL into FQDN
            List<IExternalResource> children = getSitesList(convertHostNameURLIntoFQDN(node.getID())); 
            children.addAll(getLists(connection.getURL()));
            node.setChildren(children.toArray(new IExternalResource[]{}) );
        }
        else if ( node.getType().equals(IExternalResourceTypes.SITE) ) {
        	// before getting sites, convert host name in URL into FQDN
        	// warning: this code path has not been tested after convertHostNameURLIntoFQDN() is introduced.  it should be tested.
            List<IExternalResource> children = getSitesList(convertHostNameURLIntoFQDN(node.getURL()));  
            children.addAll(getLists(node.getURL()));
            node.setChildren( children.toArray(new IExternalResource[]{}) );
        }
        else if ( node.getType().equals(IExternalResourceTypes.LIST) ) {
        	// warning: this code path has not been tested after convertHostNameURLIntoFQDN() is introduced.  it should be tested.
            List<IExternalResource> children = getListItems(convertHostNameURLIntoFQDN(node.getID()));  // before getting lists, convert host name in URL into FQDN
            node.setChildren(children.toArray(new IExternalResource[]{}));
        }
        else {
            // ignore other types, will not display in lookup
        }
    }
    
    @Override
    public boolean testConnection() throws INDException {
        String portalURL = connection.getURL();
        String login = connection.getUserName();
        String password = connection.getPassword();
        String domain = connection.getDomainName();
        
        SharePointDataSourceConnectionTester.testConnection(portalURL, login, password, domain);
        
        return true;
    }
    
    @Override
    public List<IExternalResource> getResourcePreview(DomainObjectDescriptor descriptor) throws INDException {
        throw new INDException("SharePoint resource preview is not implemented");
    }
 
    /**
     * Take a URL, and convert the host name in it into FQDN through reverse DNS lookup.
     * For example, http://hostname/site will become http://hostname.domain.com/site
     * 
     * @param urlString URL whose host name will be converted
     * @return URL with host name converted into FQDN
     * @throws INDException
     */
    private String convertHostNameURLIntoFQDN(String urlString)
    throws INDException
    {
    	URL url = null;
    	try {
    		url = new URL(urlString);
    	} catch(MalformedURLException e) {
    		throw new INDException("URL " + urlString + " is malformed", e);
    	}
    	String hostname = url.getHost();
    	String fqdn = null;
    	try
    	{
    		InetAddress addr = InetAddress.getByName(hostname);
    		fqdn = addr.getCanonicalHostName().toLowerCase();
    	}
    	catch(UnknownHostException e)
    	{
    		throw new INDException((new StringBuilder()).append("Host name ").append(hostname).append(" cannot be converted into Fully Qualified Domain Name").toString(), e);
    	}
    	return urlString.replaceFirst(hostname, fqdn);
    }   
}