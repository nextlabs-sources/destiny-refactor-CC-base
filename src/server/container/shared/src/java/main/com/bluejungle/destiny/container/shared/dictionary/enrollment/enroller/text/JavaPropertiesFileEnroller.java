/*
 * Created on Mar 13, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by Blue Jungle
 * Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights
 * reserved worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;

/**
 * @author atian
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/text/JavaPropertiesFileEnroller.java#1 $
 */

public class JavaPropertiesFileEnroller extends EnrollerBase {

    /** the fetch size for LDIF file batch read, it can be a configure parameter */
    private static final int DEFAULT_BATCH_SIZE = 512;

    /* Logger */
    private static final Log LOG = LogFactory.getLog(JavaPropertiesFileEnroller.class);
    
    private JavaPropertiesEnrollmentWrapperImpl wrapper;
    
    public String getEnrollmentType() {
        return "Text File";
    }

    @Override
    protected Log getLog() {
        return LOG;
    }

    public void process(IEnrollment enrollment, Map<String, String[]> properties, IDictionary dictionary)
            throws EnrollmentValidationException, DictionaryException {
        super.process(enrollment, properties, dictionary);
        new JavaPropertiesEnrollmentInitalizer(enrollment, properties, dictionary).setup();
    }
    
    @Override
    protected void preSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session) throws EnrollmentValidationException, DictionaryException {
        wrapper = new JavaPropertiesEnrollmentWrapperImpl(enrollment, dictionary, DEFAULT_BATCH_SIZE);
    }

    @Override
    protected void internalSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session, SyncResult syncResult) throws EnrollmentSyncException {
        Map<DictionaryPath, IElementBase> elements = new HashMap<DictionaryPath, IElementBase>(DEFAULT_BATCH_SIZE);
        Collection<IElementBase> deletes = new ArrayList<IElementBase>();

        int counter = 0;
        int fetched = 0;
        
        String lastEntry = null;
        try {
            status = STATUS_FETCHING_DATA;
            while ((fetched = wrapper.fetchElements(elements, deletes)) > 0) {
                counter += fetched;
                
                session.beginTransaction();
                Collection<IElementBase> elementsValues = elements.values();
                // just get the first one, a collection doesn't have order.
                DictionaryPath dictPath = elementsValues.iterator().next().getPath();
                if(dictPath != null){
                    lastEntry = dictPath.toString();
                }
                session.saveElements(elements.values());
                if (!deletes.isEmpty()) {
                    status = STATUS_REMOVE_ELEMENTS;
                    dictPath = deletes.iterator().next().getPath();
                    if(dictPath != null){
                        lastEntry = dictPath.toString();
                    }
                    session.deleteElements(deletes);
                }
                
                session.commit();
                if (wrapper.isUpdate()) {
                    status = STATUS_PROCESS_CONTENT_ENROLLMENT;
                    elementsValues = elements.values();
                    if (!elementsValues.isEmpty()) {
                        // just get the first one, a collection doesn't have order.
                        dictPath = elementsValues.iterator().next().getPath();
                        if (dictPath != null) {
                            lastEntry = dictPath.toString();
                        }
                    }
                    wrapper.saveElementIDs(elements.values());
                }
                elements.clear();
                deletes.clear();
                
                threadCheck(lastEntry);
                
                status = STATUS_FETCHING_DATA;
            }
            if (wrapper.isUpdate()) {
                status = STATUS_REMOVE_ELEMENTS;
                wrapper.removeElementIDs(session);
            }
        } catch (DictionaryException e) {
            throw new EnrollmentSyncException(e, lastEntry);
        }
        
        syncResult.success = true;
        syncResult.message = null;
    }
}
