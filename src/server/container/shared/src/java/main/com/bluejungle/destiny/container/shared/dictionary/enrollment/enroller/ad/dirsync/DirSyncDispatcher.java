/*
 * Created on May 17, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.RetrievalFailedException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ADUtils;
import com.bluejungle.framework.crypt.ReversibleEncryptor;
import com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.ad.ADConnectionHelper;
import com.nextlabs.destiny.container.shared.dictionary.enrollment.enroller.ad.ADConnectionHelper.ConnectionType;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPControl;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPExtendedOperation;
import com.novell.ldap.LDAPExtendedResponse;
import com.novell.ldap.LDAPJSSESecureSocketFactory;
import com.novell.ldap.LDAPJSSEStartTLSFactory;
import com.novell.ldap.LDAPSearchConstraints;
import com.novell.ldap.LDAPSearchResults;
import com.novell.ldap.LDAPSocketFactory;
import com.novell.ldap.extensions.PartitionEntryCountRequest;
import com.novell.ldap.extensions.PartitionEntryCountResponse;

/**
 * @author safdar
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/dirsync/DirSyncDispatcher.java#2 $
 */

public class DirSyncDispatcher implements Closeable{
    private static final Log LOG = LogFactory.getLog(DirSyncDispatcher.class);
	
    //  password decoder
    private static final ReversibleEncryptor CIPHER = new ReversibleEncryptor();
	
    private DirSyncControl dirSyncControl;
    private final DeletedControl deletedControl;
    private final String server;
    private final int port;
    private final String login;
    private final String password;
    private final String secureTransactionMode;
    private final boolean alwaysTrustAD;
    private final String[] roots;
    private final String filter;
    private final String[] attributesToRetrieve;
    private final boolean isPagingEnabled;
    private final int batchSize;
    
    private int currentRootIndex;
    private List<LDAPEntry> searchResult;
    private Iterator<LDAPEntry> searchResultItor;
    private LDAPConnection connection;
    private boolean hasMoreDataToPull;
    private SimplePagedResultResponseControl pageResultControl;

    /**
     * Constructor
     *  
     */
    public DirSyncDispatcher(DirSyncControl control, 
                             String server, 
                             int port, 
                             String login, 
                             String password, 
                             String roots[], 
                             String[] attributesToRetrieve, 
                             String filter,
                             boolean isPagingEnabled,
                             String secureTransactionMode,
                             boolean alwaysTrustAD,
                             int batchSize) {
        super();
        this.dirSyncControl 		= control;
        this.server 			= server;
        this.port 			= port;
        this.login 			= login;
        this.password 			= password;
        this.secureTransactionMode	= secureTransactionMode;
        this.alwaysTrustAD              = alwaysTrustAD;
        this.roots 			= roots;
        this.attributesToRetrieve 	= attributesToRetrieve;
        this.filter 			= filter;
        this.isPagingEnabled 		= isPagingEnabled;
        this.batchSize 			= batchSize;
        
        this.deletedControl 		= new DeletedControl();
        this.pageResultControl 		= null;
        
        LOG.trace("attributesToRetrieve: " + Arrays.toString(attributesToRetrieve));
    }

    /**
     * @return
     */
    public void pull() throws RetrievalFailedException {
        hasMoreDataToPull = false;
        currentRootIndex = 0;
        doPull();
    }

    public boolean hasMore() throws RetrievalFailedException {
        if (searchResultItor.hasNext()) {
            LOG.trace("Something is still in the buffer.");
            return true;
        }
        
        if (hasMoreDataToPull) {
            LOG.trace("more data to pull");
            doPull();
            return true;
        }

        LOG.trace("No more");
        return false;
    }
    
    public LDAPEntry next() throws NoSuchElementException {
        if (!searchResultItor.hasNext()) {
            throw new NoSuchElementException("No data to pull from LDAP server");
        }
        return searchResultItor.next();
    }

    protected void doPull() throws RetrievalFailedException {
        LOG.trace("start pulling on " + currentRootIndex);
        try {
            connection = acquireConnection();
            // Setup the dirsync control:
            LDAPSearchConstraints constraints = new LDAPSearchConstraints();
            String searchRoot = roots[currentRootIndex];
            if (dirSyncControl != null) { 
            	// set the dirsync control
            	constraints.setControls(deletedControl);
            	constraints.setControls(dirSyncControl);
            	constraints.setMaxResults(0);
            	// search from the AD Domain
            	searchRoot = ADUtils.constructDomainRootDN(ADUtils.extractADDomainFromRootDN(
                                                               roots[currentRootIndex]));
            } else if (isPagingEnabled) {
                byte[] cookie = pageResultControl != null
                                ? pageResultControl.getCookie()
                                : null;
                LOG.trace("paging is used. cookie = " + Arrays.toString(cookie));
                constraints.setControls(new SimplePagedResultControl(batchSize, cookie));
                constraints.setMaxResults(batchSize);
            } else {
                constraints.setMaxResults(0);
            }
            // Make the search with no size limit and time limit
            constraints.setTimeLimit(0);
            constraints.setServerTimeLimit(0);           
            constraints.setReferralFollowing(true);
            
            // If the attrs is null, it means everything.
            // We are doing ok in Microsoft Active Directory.
            // However, in openDS, virtual attributes are not returning
            // I must specific when attribute I want to return
            LDAPSearchResults results = connection.search(
                searchRoot,                   //base 
                LDAPConnection.SCOPE_SUB,     //scope
                filter,                       //filter
                attributesToRetrieve,         //attrs
                false,                        //typesOnly
                constraints);                 //cons
            setSearchResultInBuffer(results);
            processResponseControl(results);
            
        } catch (LDAPException e) {
            throw new RetrievalFailedException(e);
        } catch (RuntimeException e) {
            throw new RetrievalFailedException("Failed to retrive entries from directory", e);
        } 
    }
    
    private void processResponseControl(LDAPSearchResults results) {   
        LOG.trace("start processResponseControl");
    	LDAPControl[] responseControls = results.getResponseControls(); 
    	if (responseControls != null) {
            for (LDAPControl control : responseControls) {
                if (control instanceof SimplePagedResultResponseControl) {
                    pageResultControl = (SimplePagedResultResponseControl) control;
                    hasMoreDataToPull = pageResultControl.hasMorePages();
                    LOG.trace("found SimplePagedResultResponseControl, hasMoreDataToPull = " + hasMoreDataToPull);
                } else if (control instanceof DirSyncResponseControl) {
                    DirSyncResponseControl dirSyncResponseControl =
                        (DirSyncResponseControl) control;
                    dirSyncControl.updateCookie(dirSyncResponseControl.getCookie());
                    hasMoreDataToPull = dirSyncResponseControl.isMoreDataAvailable();
                    LOG.trace("found DirSyncResponseControl, hasMoreDataToPull = " + hasMoreDataToPull); 
                    break;
                }
            }
        }
        //increase the root index, and set has more to pull when there are more roots to pull
        //do this only when change tracking is disabled.
        if ((dirSyncControl == null) && !hasMoreDataToPull) {
            currentRootIndex++;
            if (currentRootIndex < this.roots.length) {
                hasMoreDataToPull = true;
                pageResultControl = null;
				
                LOG.trace("next root, root = " + this.roots[currentRootIndex]);
            }
        }
    }

    public DirSyncControl processDirSyncControl() throws RetrievalFailedException {
    	String searchRoot = null;
        try {           	
            //first time enrollment, search again with dir sync control, just to get the initial cookie
            if ( dirSyncControl == null ) { 
                connection = acquireConnection();
                // search from the AD Domain
                String returnedAtts[] = { "dn" };
                searchRoot = ADUtils.constructDomainRootDN(ADUtils.extractADDomainFromRootDN(this.roots[0]));
                boolean hasMoreData = true; 
                int count = 0;
                while (hasMoreData) {
                    // Setup the dirsync control:
                    LDAPSearchConstraints constraints = new LDAPSearchConstraints();
                    if ( dirSyncControl == null ) {
                    	dirSyncControl = new DirSyncControl();
                    }
                    constraints.setControls(dirSyncControl); 
                    constraints.setReferralFollowing(true);
                    LDAPSearchResults results =	connection.search(
                        searchRoot, 
                        LDAPConnection.SCOPE_SUB, 
                        this.filter,
                        returnedAtts, 
                        true, 
                        constraints);
                    // the results is a stream, in order to get response controls, we have to loop through all entries
                    while (results.hasMore()) {
                        LDAPEntry entry = results.next();
                        LOG.info(entry.getDN()); 
                    }
                    LOG.info("processed dir sync control batch " + count); 
                    count++;
                    hasMoreData = processDirSyncResponse(results);
                }
                LOG.info("dir sync control processed"); 
            }
        } catch (LDAPException e) {
            throw new RetrievalFailedException("Permission issue: Failed to search AD from root "
                                               + searchRoot, e);
        } catch (RuntimeException e) {
            throw new RetrievalFailedException("Failed to retrive entries from directory", e);
        }
    
        return dirSyncControl;
    }

    private boolean processDirSyncResponse(LDAPSearchResults results) {
        // Save the dirsync cookie at this stage:
        if ( (results != null) && (results.getResponseControls() != null) ) {
            LDAPControl[] responseControls = results.getResponseControls();
            DirSyncResponseControl dirSyncResponseControl = null;
            for (int j = 0; j < responseControls.length; j++) {
                if (responseControls[j] instanceof DirSyncResponseControl) {
                    dirSyncResponseControl = (DirSyncResponseControl) responseControls[j];
                    if (dirSyncResponseControl != null) { 
                     	break;
                    }
                }
            }
            if ((dirSyncResponseControl != null) && (dirSyncResponseControl.getCookie() != null)) {
                this.dirSyncControl.updateCookie(dirSyncResponseControl.getCookie());
                return dirSyncResponseControl.isMoreDataAvailable();
            } else {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Creates a temporary LDAP connection
     * 
     * @return
     * @throws ConnectivityException
     */
    protected synchronized LDAPConnection acquireConnection() throws LDAPException {
        ConnectionType connType = ConnectionType.UNENCRYPTED;

        if (secureTransactionMode != null) {
            if ("SSL".compareToIgnoreCase(secureTransactionMode) == 0) {
                connType = ConnectionType.SSL;
            } else if ("TLS".compareToIgnoreCase(secureTransactionMode) == 0) {
                connType = ConnectionType.TLS;
            }
        }
        
        if (connection == null) {
            try {
                connection = ADConnectionHelper.createConnection(connType, alwaysTrustAD);
            } catch (IOException e) {
                throw new RuntimeException("Unable to create connection of type " + secureTransactionMode);
            }
        }
        
        if (!connection.isConnected()) {
            connection.connect(server, port);
            if (connType == ConnectionType.TLS) {
            	connection.startTLS();
            }
            connection.bind(LDAPConnection.LDAP_V3, login, CIPHER.decrypt(password).getBytes());
        }
        
        if (connType == ConnectionType.TLS && !connection.isTLS()) {
            throw new RuntimeException("TLS secure transport did not initialize.");
        }
        return connection;
    }

    protected void finalize() {
        releaseConnection(this.connection);
    }

    /**
     * Releases the temporary connection
     * 
     * @param connection
     */
    protected void releaseConnection(LDAPConnection connection) {
        boolean useTLS = ((secureTransactionMode != null) && (secureTransactionMode.compareToIgnoreCase("TLS") == 0));
        if (connection != null) {
            try {
            	if (useTLS) {
                    connection.stopTLS();
            	}
                connection.disconnect();
            } catch (LDAPException ignore) {
            }
        }
    }

    /**
     * Returns the dir synch control
     * 
     * @return
     */
    public DirSyncControl getDirSyncControl() {
        return this.dirSyncControl;
    }
    
    /** If the attribute value exceeds the max, they will have "range" suffix
     * http://msdn.microsoft.com/en-us/library/aa367017.aspx
     * 
     * EXPIRED RFC DRAFT which looks like where Microsoft got the idea.
     * http://www.hut.fi/cc/docs/kerberos/draft-kashi-incremental-00.txt
     */ 
    private static final Pattern ATTRIBUTE_VALUE_RANGE_PATTERN =
        Pattern.compile("(.*);range=(\\d+)\\-(\\d+|\\*)(.*)", Pattern.CASE_INSENSITIVE);
    
    /**
     * the first string is "<Attribute name>;", with the semi-colon
     * the second integer is the start range
     * the third string is either empty string or <Attribute name>
     */
    private static final String ATTRIBUTE_VALUE_RANGE_TEMPLATE = "%s;range=%d-*%s";
    
    private void setSearchResultInBuffer(LDAPSearchResults result) throws RetrievalFailedException {
        LOG.trace("start setSearchResultInBuffer");
        this.searchResult = new ArrayList<LDAPEntry>(this.batchSize);
        
        String entryDn = null;
        try{
            while (result.hasMore()) {
            	LDAPEntry entry = result.next();
            	entryDn = entry.getDN();
            	
            	LOG.trace("going to add " + entryDn + ". Already got " + searchResult.size() + " entries.");
            	
            	LDAPAttributeSet orginalAttributes = entry.getAttributeSet();
            	LDAPAttributeSet modifiedAttributes = new LDAPAttributeSet();
            	Iterator<LDAPAttribute> attrIterator = orginalAttributes.iterator();
                
            	//check each attribute name if they contain range
                while(attrIterator.hasNext()){
                    LDAPAttribute attr = attrIterator.next();

                    Matcher matcher = ATTRIBUTE_VALUE_RANGE_PATTERN.matcher(attr.getName());
                    if(matcher.matches()){
                        final String attributeName = matcher.group(1) + matcher.group(4);
                        // the attribute value has a range
                		
                        List<String> attrValues = new ArrayList<String>();
                        Collections.addAll(attrValues, attr.getStringValueArray());
                		
                        do{
                            // start from the end range plus one
                            int startRange = Integer.parseInt(matcher.group(3)) + 1;
                            String newAttr = String.format(ATTRIBUTE_VALUE_RANGE_TEMPLATE, 
                                                           matcher.group(1), startRange, matcher.group(4));
                            LDAPSearchResults r = connection.search(
                                entry.getDN(), 				// String base
                                LDAPConnection.SCOPE_BASE, 	// int scope
                                null, 						// String filter
                                new String[]{newAttr},		// String[] attrs
                                false);						// boolean typesOnly
    	            		
                            LDAPEntry subEntry = r.next();
                            Iterator<LDAPAttribute> subIterator = subEntry.getAttributeSet().iterator();
                            LDAPAttribute subAttr = subIterator.next();
                            Collections.addAll(attrValues, subAttr.getStringValueArray());
                            matcher = ATTRIBUTE_VALUE_RANGE_PATTERN.matcher(subAttr.getName());
                            if (!matcher.matches()) {
                                LOG.warn("The attribute \"" + subAttr.getName()
                                         + "\"doesn't match the pattern");
                                break;
                            }
                            // the last one is end with *, we will stop when we see the end.
                			
                        }while( !matcher.group(3).equals("*") );
                		
                        LDAPAttribute existing = modifiedAttributes.getAttribute(attributeName);
                        if(existing == null ){
                            // add
                        } else if (existing.size() == 0) {
                            // replace
                            modifiedAttributes.remove(attributeName);
                        } else {
                            //merge
                            modifiedAttributes.remove(attributeName);
                            Collections.addAll(attrValues, existing.getStringValueArray());
                        }

                        //the range is removed from the attribute's name
                        modifiedAttributes.add(new LDAPAttribute(attributeName, attrValues.toArray(new String[attrValues.size()])));
                    }else{
                        //an attribute without range
                        LDAPAttribute existing = modifiedAttributes.getAttribute(attr.getName());
                        if(existing == null ){
                            // add
                            modifiedAttributes.add(attr);
                        } else if (existing.size() == 0) {
                            // replace
                            modifiedAttributes.remove(attr.getName());
                            modifiedAttributes.add(attr);
                        } else {
                            //merge
                            String[] attrValues = attr.getStringValueArray();
                            for(String attrValue : attrValues) {
                                existing.addValue(attrValue);
                            }
                        }
                    }
                }
                
                //construct a new ldap entry since the attribute is changed.
                searchResult.add(new LDAPEntry(entry.getDN(), modifiedAttributes));
            }
        } catch (LDAPException e){
            throw new RetrievalFailedException(e, entryDn);
        }
        LOG.trace("Pulled " + searchResult.size() + " entries.");
        this.searchResultItor = this.searchResult.iterator();
    }
    
    /**
     * not always supported
     * @return
     */
    public int getEntryCount() {
        try {
            connection = acquireConnection();
        } catch (LDAPException e) {
            return -1;
        }
        int count = 0;
        try {
            for (String root : roots) {
                LDAPExtendedOperation request = new PartitionEntryCountRequest(root);
                LDAPExtendedResponse response = connection.extendedOperation(request);
                if ((response.getResultCode() == LDAPException.SUCCESS)
                    && (response instanceof PartitionEntryCountResponse)) {
                    count += ((PartitionEntryCountResponse) response).getCount();
                }
            }
        } catch (LDAPException e) {
            count = -1;
        }
        return count;
    }

    public void close() throws IOException {
        releaseConnection(connection);
    }
}
