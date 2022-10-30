/*
 * Created on Feb 26, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl.sharepoint;

import com.bluejungle.ind.IDataSource;
import com.bluejungle.ind.IDataSourceConnection;
import com.bluejungle.ind.IExternalResource;
import com.bluejungle.ind.impl.DataSourceConnection;
import com.bluejungle.ind.impl.DataSourceFactory;
import com.bluejungle.ind.impl.DataSourceManager;
import com.bluejungle.ind.impl.DataSourceType;
import com.bluejungle.ind.impl.ExternalResource;

import junit.framework.TestCase;


/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/test/com/bluejungle/ind/impl/sharepoint/TestSharePointDataSource.java#1 $
 */

public class TestSharePointDataSource extends TestCase {
    
    private SharePointDataSource dataSource = null;
    private DataSourceManager indDataSourceManager = new DataSourceManager();
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        indDataSourceManager.init();
        IDataSource dataSource = indDataSourceManager.createDataSource(DataSourceFactory.SHAREPOINT);
        IDataSourceConnection connection = new DataSourceConnection(
                "http://sharepoint2007", "Administrator", "123blue!", "sharepoint2007" );
        dataSource.setConnection(connection);
        indDataSourceManager.addDataSource(dataSource);
        
    }
       
   
    public void testGetRootSites() throws Exception {
        ExternalResource node = new ExternalResource();
        dataSource.getResourceTreeNodeChildren(node);
        assertNotNull(node.getChildren());
        assertTrue(node.getChildren().length > 1);
        IExternalResource[] children = node.getChildren();
        for(int i=0; i<children.length; i++) {
            IExternalResource child = children[i];
            System.out.println("URL:" + child.getURL() + " name: " + child.getName() + " type: " + child.getType());
        }
    }
   
    public void testGetAAASites() throws Exception {
        ExternalResource node = new ExternalResource();
        node.setID("http://sharepoint2007/AAA");
        node.setType("SITE");
        dataSource.getResourceTreeNodeChildren(node);
        assertNotNull(node.getChildren());
        assertTrue(node.getChildren().length > 1);
        IExternalResource[] children = node.getChildren();
        for(int i=0; i<children.length; i++) {
            IExternalResource child = children[i];
            System.out.println("URL:" + child.getURL() + " name: " + child.getName() + " type: " + child.getType());
        }
    }
    
    public void testGetList() throws Exception {
        ExternalResource node = new ExternalResource();
        node.setID("http://sharepoint2007/AAA");
        node.setType("SITE");
        dataSource.getResourceTreeNodeChildren(node);
        assertNotNull(node.getChildren());
        assertTrue(node.getChildren().length > 1);
        IExternalResource[] children = node.getChildren();
        for(int i=0; i<children.length; i++) {
            IExternalResource child = children[i];
            System.out.println("URL:" + child.getURL() + " name: " + child.getName() + " type: " + child.getType());
        }
    }
    
    public void testGetListItems() throws Exception {
        ExternalResource node = new ExternalResource();
        node.setID("{19FC0768-8AEF-4ABC-94E9-C6BC7B9A2AE4}");
        node.setType("LIST");
        dataSource.getResourceTreeNodeChildren(node);
        assertNotNull(node.getChildren());
        assertTrue(node.getChildren().length > 1);
        IExternalResource[] children = node.getChildren();
        for(int i=0; i<children.length; i++) {
            IExternalResource child = children[i];
            System.out.println("URL:" + child.getURL() + " name: " + child.getName() + " type: " + child.getType());
        }
    }
    
    public void testGetDocumentLibItems() throws Exception {
        ExternalResource node = new ExternalResource();
        node.setID("{49881AE3-4C77-4892-A678-EE4D813C84E4}");
        node.setType("LIST");
        dataSource.getResourceTreeNodeChildren(node);
        assertNotNull(node.getChildren());
        assertTrue(node.getChildren().length > 1);
        IExternalResource[] children = node.getChildren();
        for(int i=0; i<children.length; i++) {
            IExternalResource child = children[i];
            System.out.println("ID:" + child.getID() + " name: " + child.getName() + " type: " + child.getType());
        }
    }
}
