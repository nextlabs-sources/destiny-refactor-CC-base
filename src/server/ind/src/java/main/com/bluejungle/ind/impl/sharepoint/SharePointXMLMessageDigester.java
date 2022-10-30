/*
 * Created on Feb 28, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ind.impl.sharepoint;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;

import com.bluejungle.ind.IExternalResource;
import com.bluejungle.ind.IExternalResourceTypes;
import com.bluejungle.ind.INDException;
import com.bluejungle.ind.impl.ExternalResource;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/ind/src/java/main/com/bluejungle/ind/impl/sharepoint/SharePointXMLMessageDigester.java#1 $
 */

public class SharePointXMLMessageDigester {

    private List<IExternalResource> result = null;
    
    /**
     * Sample message from sharepoint:
     *  
     *  <xml xmlns:rs='urn:schemas-microsoft-com:rowset'>
     *      <rs:data ItemCount="2">
     *          <z:row ows_ServerUrl='/Documents/IND Design.doc'
     *                 ows_EncodedAbsUrl='http://sharepoint2007/Documents/IND%20Design.doc'
     *                 ows_GUID='{E2B0C714-A514-4A53-A373-82A82F3372AF}'
     *                 ows_LinkFilename='IND Design.doc'/>
     *          <z:row .../>
     *          <z:row .../>
     *      </rs:data>
     *  </xml>
     *  
     * @param xmlStr
     * @return
     * @throws INDException
     */
    
    private static final String RESULT_DATA_ROW_PATH = "xml/rs:data/z:row";
    
    public List<IExternalResource> getListItems(String xmlStr) throws INDException {
        
        Digester digester = new Digester();
        digester.setValidating( false );
        digester.push(this);
        digester.addObjectCreate( RESULT_DATA_ROW_PATH, ExternalResource.class);

        //digester.addSetProperties( RESULT_DATA_ROW_PATH, "ows_EncodedAbsUrl", "URL");
        digester.addSetProperties( RESULT_DATA_ROW_PATH, "ows_LinkFilename", "name");
        digester.addSetProperties( RESULT_DATA_ROW_PATH, "ows_ServerUrl", "ID");
        
        digester.addSetNext( RESULT_DATA_ROW_PATH, "addResource");
        
        try{
            this.result = new ArrayList<IExternalResource>();
            digester.parse(new StringReader(xmlStr));
        }catch(Exception e){
            throw new INDException(e);
        }
        return result;
    }
    
    public void addResource(ExternalResource source){
        if ( source != null ) {
            source.setType(IExternalResourceTypes.DOCUMENT);
            this.result.add(source);
        }
    }
}
