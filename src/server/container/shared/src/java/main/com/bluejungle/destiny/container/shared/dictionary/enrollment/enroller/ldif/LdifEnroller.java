/*
 * Created on April 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl.LdifElementFactory;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl.LdifEnrollmentInitializer;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl.LdifEnrollmentWrapperImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.LDAPEnrollmentHelper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.SyncResultEnum;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.framework.utils.IPair;
import com.novell.ldap.LDAPAddRequest;
import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPLocalException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPModifyDNRequest;
import com.novell.ldap.LDAPModifyRequest;
import com.novell.ldap.LDAPSearchResult;
import com.novell.ldap.util.LDIFReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author atian 
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ldif/LdifEnroller.java#1 $
 */


/** 
 * LdifEnroller class is the main class for LDIF file enrollment 
 * It implements the IEnroller interface, 
 * and implements process() and sync() methods  
 */
public class LdifEnroller extends EnrollerBase {

    /** the fetch size for LDIF file batch read, it can be a configure parameter */
    private static final int DEFAULT_BATCH_SIZE = 1000;

    private static final Log LOG = LogFactory.getLog(LdifEnroller.class);

    private final int batchSize;
    
    private LdifEnrollmentWrapperImpl ldifEnrollment;
    private InputStream is;
    private LDIFReader ldifReader;
    private LdifElementFactory elementCreator;
    private boolean endOfFile;
    
    public LdifEnroller(int batchSize) {
        super();
        this.batchSize = batchSize;
    }
    
    public LdifEnroller() {
        this(DEFAULT_BATCH_SIZE);
    }
    
    public String getEnrollmentType() {
        return "LDIF";
    }

    @Override
    protected Log getLog() {
        return LOG;
    }

    /**
     * process method initializes ldif enrollment implementation 
     * a LDIF file reader and external name resolve will be created in ldif enrollment impl object
     *
     * @param enrollment is an enrollment object
     * @param properties is the LDIF enrollment configuration 
     * @param dictionary is a dictionary object 
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnroller#process()
     * @throws EnrollmentValidationException
     */
    public void process(IEnrollment enrollment, Map<String, String[]> properties,
			IDictionary dictionary) throws EnrollmentValidationException, DictionaryException {
    	super.process(enrollment, properties, dictionary);
		new LdifEnrollmentInitializer(enrollment, properties, dictionary).setup();
	}
    
    //read a inputstream in a serial order
    private class CombinedInputStream extends InputStream{
        private final InputStream[] streams;
        private int index = 0;
        
        private CombinedInputStream(InputStream ... streams){
            this.streams = streams;
        }

        @Override
        public int read() throws IOException {
            if( index >= streams.length){
                return -1;
            }
            
            int r; 
            r = streams[index].read();
            //try to read next one
            if(r == -1){
                index++;
                r = read();
            }
            return r;
        }

        /**
         * @throws IOException, only the last IOException
         */
        @Override
        public void close() throws IOException {
            IOException ioe = null;
            for (InputStream stream : streams) {
                try {
                    stream.close();
                } catch (IOException e) {
                    ioe = e;
                }
            }

            if (ioe != null) {
                throw ioe;
            }
        }
    }
    
    @Override
    protected void preSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session) throws EnrollmentValidationException, DictionaryException {
        ldifEnrollment = new LdifEnrollmentWrapperImpl(enrollment, dictionary);
        String ldifFileName = ldifEnrollment.getLdifFileName();

        File file = new File(ldifFileName);
        if (!file.exists()) {
            //TODO_OJA
            throw new EnrollmentValidationException("The LDIF file, "
                    + file.getAbsolutePath() + ", does not exist.");
        }
        if (!file.isFile()) {
            //TODO_OJA
            throw new EnrollmentValidationException("The LDIF file, "
                    + file.getAbsolutePath() + ", is not a file.");
        }
        if (!file.canRead()) {
            //TODO_OJA
            throw new EnrollmentValidationException("The LDIF file, "
                    + file.getAbsolutePath() + ", can not be read.");
        }
        
        
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(ldifFileName);
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = bufReader.readLine()) != null
                    && (line.length() == 0 || line.startsWith("#"))) {
            }
            
            if ((line != null) && line.startsWith("version:")) {
                is = new FileInputStream(ldifFileName);
            } else {
                is = new CombinedInputStream(new ByteArrayInputStream("version:1\n".getBytes()),
                        new FileInputStream(ldifFileName));
            }
        } catch (FileNotFoundException e) {
            throw new EnrollmentValidationException("Ldif file does not exist:" + ldifFileName);
        } catch (IOException e) {
            throw new EnrollmentValidationException("Can't read: " + ldifFileName, e);
        } finally{
            if(fis != null){
                try {
                    fis.close();
                } catch (Exception e) {
                    LOG.warn("Failed to close file " + ldifFileName, e);
                }
            }
        }
        
        try {
            ldifReader = new LDIFReader(is);
        } catch (LDAPLocalException e) {
            throw new EnrollmentValidationException(e);
        } catch (IOException e) {
            throw new EnrollmentValidationException(e);
        }
        
        // initialize LDIF enrollment
        // setup Element Creator 
        elementCreator = new LdifElementFactory(ldifEnrollment, dictionary);
    }

    /**
     * sync method fetches a batch of LDAP entries from LDIF file and save the batch 
     * to dictionary
     * It starts a transaction at the beginning and either commit if there is no error
     * or rollback the transaction if any error happens
     *
     * Note: for performance reason, we may change this method to commit more frequently
     * @param enrollment is an enrollment object
     * @param dictionary is a dictionary object 
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnroller#sync(com.bluejungle.dictionary.IEnrollment, com.bluejungle.dictionary.IDictionary)
     */
    @Override
    protected void internalSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session, SyncResult syncResult) throws EnrollmentSyncException {

        Collection<IElementBase> elements = new ArrayList<IElementBase>(batchSize);
        Collection<IElementBase> deletes = new ArrayList<IElementBase>();

        endOfFile = false;
        syncResult.totalCount = 0;
        syncResult.newCount = 0;
        syncResult.changeCount = 0;
        syncResult.nochangeCount = 0;
        syncResult.deleteCount = 0;
        syncResult.ignoreCount = 0;
        syncResult.failedCount = 0;
        int fetched = 0;
        
        String lastEntry = null;
        try {
            status = STATUS_FETCHING_DATA;
            while (fetchElements(elements, deletes, syncResult)) {
                syncResult.totalCount += fetched;

                session.beginTransaction();
                
                String t;
                if ((t = getLastEntry(elements)) != null) {
                    lastEntry = t;
                }
                session.saveElements(elements);
                if (!deletes.isEmpty()) {
                    if ((t = getLastEntry(deletes)) != null) {
                        lastEntry = t;
                    }
                    session.deleteElements(deletes);
                }
                session.commit();
                if (ldifEnrollment.isUpdate()) {
                    status = STATUS_PROCESS_CONTENT_ENROLLMENT;
                    if ((t = getLastEntry(elements)) != null) {
                        lastEntry = t;
                    }
                    ldifEnrollment.saveElementIDs(elements);
                }
                
                elements.clear();
                deletes.clear();
                
                threadCheck(lastEntry);
                status = STATUS_FETCHING_DATA;
            }
            
            status = STATUS_REMOVE_ELEMENTS;
            if (ldifEnrollment.isUpdate()) {
                try {
                    ldifEnrollment.removeElementIDs(session);
                } catch (DictionaryException e) {
                    throw new EnrollmentSyncException(e);
                }
            }
        } catch (DictionaryException e) {
            throw new EnrollmentSyncException(e, lastEntry);
        }
        
        syncResult.success = true;
    }
    
    // just get the first one element, a collection doesn't have order.
    protected String getLastEntry(Collection<IElementBase> elements){
        if (!elements.isEmpty()) {
            IElementBase element = elements.iterator().next();
            if (element != null) {
                DictionaryPath dictPath = element.getPath();
                if (dictPath != null) {
                    return dictPath.toString();
                }
            }
        }
        return null;
    }
    
    /**
     * fetchElements() reads LDIF file, fetch the entries from LDIF file and convert them to elements
     * @param elements
     * @param deletes
     * @return size of elements fetched, including the elements to delete
     * @throws EnrollmentFailedException
     */
    public boolean fetchElements(Collection<IElementBase> elements,
            Collection<IElementBase> deletes, SyncResult syncResult) throws EnrollmentSyncException {
        return ldifReader.isRequest()
                ? fetchChangeElements(elements, deletes, syncResult)
                : fetchContentElements(elements, syncResult);
    }
    
    /**
     * fetchContentElements() fetches element from a content LDIF file
     * @param saves collection of elements fetched
     * @return size of elements fetched
     * @throws LDAPException
     * @throws IOException
     * @throws EnrollmentFailedException
     */
    private boolean fetchContentElements(Collection<IElementBase> saves, SyncResult syncResult)
            throws EnrollmentSyncException {
        LDAPMessage msg = null;
        String lastParsedDn = null;
        
        int counter = 0;
        try {
            while ((!endOfFile) && ((msg = ldifReader.readMessage()) != null)
                    && (counter < batchSize)) {
                LDAPEntry entry = ((LDAPSearchResult) msg).getEntry();
                lastParsedDn = entry.getDN();
                
                LOG.trace(" Got LDIF entry:" + lastParsedDn);
                IPair<SyncResultEnum, ? extends IElementBase> r = elementCreator.createContentElement(entry);
                if (updateSyncResult(r.first(), r.second(), saves, null, syncResult)) {
                    counter++;
                }
            }
        } catch (IOException e) {
            throw new EnrollmentSyncException(e, lastParsedDn, false);
        } catch (LDAPException e) {
            throw new EnrollmentSyncException(e, lastParsedDn, false);
        }
        
        if (msg == null) {
            endOfFile = true;
        }
        return counter > 0;
    }
    
    /**
     * fetchChangeElements() fetches elements from change LDIF file
     * 
     * @param elements
     * @param deletes
     * @return
     * @throws LDAPException
     * @throws IOException
     * @throws EnrollmentFailedException
     */
    private boolean fetchChangeElements(Collection<IElementBase> saves,
            Collection<IElementBase> deletes, SyncResult syncResult) throws EnrollmentSyncException {
        LDAPMessage msg = null;
        LDAPEntry entry;
        int counter = 0;
        String lastDn = null;
        try {
            while ( (!endOfFile) && ( ( msg = ldifReader.readMessage() ) != null ) 
                    && (counter < batchSize ) ) {
                
                if (msg instanceof LDAPAddRequest) { // LDAP Add
                    // This is an add entity change 
                    entry = ((LDAPAddRequest) msg).getEntry();
                    lastDn = entry.getDN();
                    LOG.trace("Got LDIF add entry:" + lastDn );
                    IPair<SyncResultEnum, ? extends IElementBase> r = elementCreator.createContentElement(entry);
                    if (updateSyncResult(r.first(), r.second(), saves, null, syncResult)) {
                        counter++;
                    }
                } else if (msg instanceof LDAPDeleteRequest) { // LDAP delete
                    // This is a delete entity change 
                    lastDn = ((LDAPDeleteRequest) msg).getDN();
                    LOG.trace("Got LDIF delete entry:" + lastDn );
                    IMElementBase delete = elementCreator.getElementByDN(lastDn);
                    if (delete != null && updateSyncResult(SyncResultEnum.DELETE_ENTRY, delete, 
                            null, deletes, syncResult)) {
                        counter++;
                    }else{
                        LOG.info("can not build LDIF delete element:" + lastDn);
                        updateSyncResult(SyncResultEnum.ERROR_ENTRY, null, null, null,
                                syncResult);
                    }
                } else if (msg instanceof LDAPModifyDNRequest) { // LDAP modDN
                    // This is a modifiy DN change 
                    lastDn = ((LDAPModifyDNRequest)msg).getDN();
                    LOG.trace("Got LDIF mod DN entry:" + lastDn );
                    IMElementBase element = elementCreator.getElementByDN(lastDn);
                    if (element != null) {
                        String newRDN = ((LDAPModifyDNRequest)msg).getNewRDN();
                        element.setPath( LDAPEnrollmentHelper.getDictionaryPathFromDN(newRDN) );
                        
                        updateSyncResult(SyncResultEnum.MODIFY_ENTRY, element, 
                                saves, null, syncResult);
                        counter ++;
                    } else {
                        LOG.info("can not build LDIF modDN element:" + lastDn);
                        updateSyncResult(SyncResultEnum.ERROR_ENTRY, null, null, null,
                                syncResult);
                    }
                } else if (msg instanceof LDAPModifyRequest) { // LDAP modify
                    lastDn = ((LDAPModifyRequest)msg).getDN();
                    LOG.trace("Got LDIF modify entry:" + lastDn );
                    IMElementBase element = elementCreator.createModifyElement( (LDAPModifyRequest)msg );
                    if (element != null) {
                        updateSyncResult(SyncResultEnum.MODIFY_ENTRY, element, 
                                saves, null, syncResult);
                        counter ++;
                    } else {
                        LOG.info(" can not build LDIF modify element:" + lastDn );
                        updateSyncResult(SyncResultEnum.ERROR_ENTRY, null, null, null,
                                syncResult);
                        
                    }
                } else {
                    throw new EnrollmentSyncException("un-recoginizable LDAP change: " + msg.toString(), lastDn);
                }
                
            } // end of while loop
        } catch (DictionaryException e) {
            throw new EnrollmentSyncException(e, lastDn);
        } catch (IOException e) {
            throw new EnrollmentSyncException(e, lastDn, false);
        } catch (LDAPException e) {
            throw new EnrollmentSyncException(e, lastDn, false);
        }
        if (msg == null) {
            endOfFile = true;
        }       
        return counter > 0;
    }

    @Override
    protected void postSync() {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                getLog().warn("failed to cleanup inputstream" + e);
            }
        }
    }
    
    
}
