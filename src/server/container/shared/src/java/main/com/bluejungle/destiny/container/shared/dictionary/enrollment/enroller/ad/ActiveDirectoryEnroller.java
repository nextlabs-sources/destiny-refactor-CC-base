/*
 * Created on Mar 27, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerBase;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DirSyncControl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.dirsync.DirSyncDispatcher;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ADEnrollmentInitializer;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ADEnrollmentWrapperImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.SyncResultEnum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.bluejungle.framework.utils.IPair;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;

/**
 * @author safdar
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/ActiveDirectoryEnroller.java#7 $
 */

public class ActiveDirectoryEnroller extends EnrollerBase {
	
	private static final int DEFAULT_BATCH_SIZE = 512;
    private static final Log LOG = LogFactory.getLog(ActiveDirectoryEnroller.class.getName());

    private final int batchSize;
    private ADEnrollmentWrapperImpl adEnrollment;
    private ActiveDirectoryElementFactory creator;

    public ActiveDirectoryEnroller() {
    	//use default batch size if the batch size is not specified
        this(DEFAULT_BATCH_SIZE);
    }
    
    public ActiveDirectoryEnroller(int batchSize) {
        super();
        this.batchSize = batchSize;
    }
    
    public String getEnrollmentType() {
        return "Active Directory";
    }

    @Override
    protected Log getLog() {
        return LOG;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnroller#process(com.bluejungle.dictionary.IEnrollment)
     */
    public void process(IEnrollment enrollment, Map<String, String[]> properties,
            IDictionary dictionary) throws EnrollmentValidationException, DictionaryException {
        super.process(enrollment, properties, dictionary);
        new ADEnrollmentInitializer(enrollment, properties, dictionary).setup();
    }
    
    @Override
    protected void preSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session) throws EnrollmentValidationException, DictionaryException {
        this.adEnrollment = new ADEnrollmentWrapperImpl(enrollment, dictionary);

        status = STATUS_PRE_SYNC;
        creator = new ActiveDirectoryElementFactory(adEnrollment, dictionary);
    }

    @Override
    protected void internalSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session, SyncResult syncResult) throws EnrollmentSyncException {
        // It is the first time enrollment when cookie is null
        final boolean firstTime = (adEnrollment.getCookie() == null);

        final DirSyncControl control;
        if (!firstTime && adEnrollment.isDirSyncEnabled()) {
            // skip dir sync control during first time content enrollment
            control = new DirSyncControl();
            control.setControlValue(adEnrollment.getCookie());
        } else {
            control = null;
        }

        LOG.trace("Filter: " + adEnrollment.getFilter());

        status = STATUS_FETCHING_DATA;
        DirSyncDispatcher dispatcher = new DirSyncDispatcher(
                control, 
                adEnrollment.getServer(), 
                adEnrollment.getPort(), 
                adEnrollment.getLogin(),
                adEnrollment.getPassword(), 
                adEnrollment.getSubtreesToEnroll(),
                adEnrollment.getAllAttributesToRetrieve(), 
                adEnrollment.getFilter(),
                adEnrollment.isPagingEnabled(), 
                adEnrollment.getSecureTransportMode(),
                adEnrollment.getAlwaysTrustAD(),
                batchSize);
        try {
            dispatcher.pull();
        } catch (RetrievalFailedException e) {
            throw new EnrollmentSyncException(e, e.getEntry());
        }

        if (firstTime) {
            status = STATUS_PROCESS_CONTENT_ENROLLMENT;
            processContentEnrollment(dispatcher, creator, session, syncResult);
        } else {
            status = STATUS_PROCESS_CHANGE_ENROLLMENT;
            processChangeEnrollment(dispatcher, creator, session, syncResult);
        }
        
        status = STATUS_REMOVE_ELEMENTS;
        if (adEnrollment.isUpdate()) {
            try {
                syncResult.deleteCount = adEnrollment.removeElementIDs(session);
            } catch (DictionaryException e) {
                throw new EnrollmentSyncException(e);
            }
        }
        
        status = STATUS_POST_PROCESS;
        // Save new cookie only if commit() is successful
        if (adEnrollment.isDirSyncEnabled()) {
            status = "persist dirSync cookie";
            DirSyncControl dirSynccontrol;
            try {
                dirSynccontrol = dispatcher.processDirSyncControl();
            } catch (RetrievalFailedException e) {
                throw new EnrollmentSyncException(e, e.getEntry());
            }
            
            if (dirSynccontrol != null) {
                LOG.info("saving dir sync cookie");
                IConfigurationSession configurationSession = null;
                try {
                    configurationSession = dictionary.createSession();
                    configurationSession.beginTransaction();
                    
                    // a hack to work around net.sf.hibernate.NonUniqueObjectException
                    // it happens on  configurationSession.saveEnrollment(enrollment);
                    IEnrollment e = dictionary.getEnrollment(adEnrollment.getDomainName());
                    e.setBinProperty(ActiveDirectoryEnrollmentProperties.COOKIE, dirSynccontrol.getValue());
                    configurationSession.saveEnrollment(e);
                    configurationSession.commit();
                } catch (DictionaryException e) {
                    rollback(configurationSession, LOG);
                    throw new EnrollmentSyncException(e);
                } finally {
                    close(configurationSession, LOG);
                }
            }
        }
       
        status = STATUS_DONE;
        syncResult.success = true;
        String warningMessage = creator.getWarningMessage();
        if(warningMessage != null){
            syncResult.message = warningMessage;
        }
    }

    /**
     * processContentEnrollment method handles the content enrollment
     * 
     * @param dispatcher
     * @param creator
     * @param enrollmentSession
     * @return
     * @throws EnrollmentFailedException
     * @throws DictionaryException
     * @throws RetrievalFailedException
     */
    private void processContentEnrollment(
	    		DirSyncDispatcher dispatcher,
				ActiveDirectoryElementFactory creator, 
				IEnrollmentSession enrollmentSession,
				SyncResult syncResult
	) throws EnrollmentSyncException {
    	String currentEntry = null;
    	syncResult.newCount = 0;
    	syncResult.changeCount = 0;
    	syncResult.nochangeCount = 0;
    	syncResult.deleteCount = 0;
    	syncResult.ignoreCount = 0;
    	syncResult.failedCount = 0;
    	
		Collection<IElementBase> elements = new ArrayList<IElementBase>(batchSize);
        int counter = 1;
        
        try {
			while (dispatcher.hasMore()) {
			    LDAPEntry entry = dispatcher.next();
			    
			    currentEntry = entry.getDN();
			    LOG.trace("got AD entry " + currentEntry);
			    
			    IPair<SyncResultEnum, ? extends IElementBase> r = creator.createContentElement(entry);
			    
			    if (updateSyncResult(r.first(), r.second(), elements, null, syncResult)) {
                    counter++;
                }
			    
			    // Commit every COMMIT_SIZE records:
			    if (counter % batchSize == 0) {
			    	saveElementsAndUpdateId(enrollmentSession, elements);
			    	elements.clear();
			    }
			    threadCheck(currentEntry);
			}
			// Save remaining elements
			if (!elements.isEmpty()) {
				saveElementsAndUpdateId(enrollmentSession, elements);
				elements.clear();
			}
        } catch (RetrievalFailedException e) {
            String failedEntry = e.getEntry();
            throw failedEntry != null
                    ? new EnrollmentSyncException(e, failedEntry)
                    : new EnrollmentSyncException(e, currentEntry, false);
        } catch (DictionaryException e) {
            throw new EnrollmentSyncException(e, currentEntry);
        } catch (NoSuchElementException e) {
            throw new EnrollmentSyncException(e, currentEntry, false);
    	} catch (RuntimeException e) {
            throw new EnrollmentSyncException(e, currentEntry);
        }
    }

	private void saveElementsAndUpdateId(IEnrollmentSession enrollmentSession,
			Collection<? extends IElementBase> elementsToSave)
			throws DictionaryException {
		enrollmentSession.beginTransaction();
		enrollmentSession.saveElements(elementsToSave);
		enrollmentSession.commit();

		if (adEnrollment.isUpdate()) {
			adEnrollment.saveElementIDs(elementsToSave);
		}
	}

    /**
     * processChangeEnrollment method handles the content enrollment
     * 
     * Notice there is some similarity between processChangeEnrollment() and
     * processContentEnrollment() methods. For performance reasons, we
     * separated content enroll and change enroll into two methods.
     * 
     * There are 4 types of changes can be made in external ActiveDirectory 1.
     * Modify attribute When attribute is modified in external directory,
     * LDAPEntry contains following info: Entry DN: LDAPAttribute: {type='sn',
     * value='Harrison BBB'} modify attribute and new value LDAPAttribute:
     * {type='objectGUID', value='?>e+#{?J??(? ?b'}
     * 
     * 2. Add new entry When new entry is created, it contains all new attribute
     * and new objectGUID
     * 
     * 3. Delete an existing entry When an entry is deleted, the LDPAEntry
     * contains following info: Entry DN of deleted object LDAPAttribute:
     * {type='objectGUID', value='?>e+#{?J??(? ?b'} Attr :isDelete: {true}
     * 
     * 4. Modify DN a. A node can be moved in to enrollment scope fetch the
     * whole subtree and add all entries in subtree into dictionary b. A node
     * can be moved out from enrollment scope delete the node/group from
     * dictionary
     * 
     * @param dispatcher
     * @param creator
     * @param enrollmentSession
     * @return
     * @throws EnrollmentFailedException
     * @throws DictionaryException
     * @throws RetrievalFailedException
     */
    private void processChangeEnrollment(
	    		DirSyncDispatcher dispatcher,
				ActiveDirectoryElementFactory creator, 
				IEnrollmentSession enrollmentSession,
				SyncResult syncResult)
		throws EnrollmentSyncException 
	{    	
    	syncResult.newCount = 0;
    	syncResult.changeCount = 0;
    	syncResult.nochangeCount = 0;
    	syncResult.deleteCount = 0;
    	syncResult.ignoreCount = 0;
    	syncResult.failedCount = 0;
    	
//		Map<DictionaryPath, IElementBase> elements = new HashMap<DictionaryPath, IElementBase>();
        Collection<IElementBase> elements = new ArrayList<IElementBase>(batchSize);
		
		Collection<IElementBase> elements2Delete = new ArrayList<IElementBase>();
        IElementBase element = null;
        int counter = 1;
        
        String currentParsedDn = null;
        try {
			while (dispatcher.hasMore()) {
			    LDAPEntry entry = dispatcher.next();
			    if (isDeleted(entry)) {
			        // process deleted entry
			        if (!entry.getDN().equals("") && !inEnrollmentScope(getParentPreDelete(entry))) {
			            LOG.trace(" got out of scope deleted AD entry " + entry.getDN());
			        }
			        if ((element = creator.createDeleteElement(entry)) != null) {
			            elements2Delete.add(element);
			            counter++;
			        }
			    } else if (!inEnrollmentScope(entry.getDN())) {
			    	// not a deleted entry --- isDeleted(entry) == FALSE

			        // Check whether the entry is in the enrollment scope by DN name
			        LOG.trace(" got out of scope changed AD entry " + entry.getDN());
			        // if the entry is NOT in the scope, Check whether the entry is moved
			        // it could be the entry moved out, in that case, delete the entry
			        if (isMoved(entry)) {
			            if ((element = creator.createDeleteElement(entry)) != null) {
			                elements2Delete.add(element);
			                counter++;
			            }
			        } 
			        // else, // do nothing if entry is not moved, truly out of scope entry 
			    } else { 
			    	// the entry is IN the enrollment scope

			    	LOG.trace(" got changed AD entry " + entry.getDN());

			        // Create change element by createChangeElement() method
			        // if the entry does not exist in dictionary, the return value is null
			        element = creator.createChangeElement(entry);
			        if (element != null) {
			            // if the entry exists, modify the exist element
			            elements.add(element);
			            counter++;
			        } else { 
			        	// element == null, which means the entry does not exist in dictionary
			            LOG.trace("found new entry to add: " + entry.getDN());
			            // check whether the entry is moved
			            if (isMoved(entry)) { // if moved into the scope
			                // fetch the whole subtree, add all entries in subtree into dictionary
							DirSyncDispatcher subTreeDispatcher = new DirSyncDispatcher(
									null,
									adEnrollment.getServer(), 
									adEnrollment.getPort(),
									adEnrollment.getLogin(), 
									adEnrollment.getPassword(),
									new String[] { entry.getDN() }, 
									adEnrollment.getAllAttributesToRetrieve(), 
									adEnrollment.getFilter(), 
									adEnrollment.isPagingEnabled(),
									adEnrollment.getSecureTransportMode(),
									adEnrollment.getAlwaysTrustAD(),
									batchSize);
							subTreeDispatcher.pull();
							while (subTreeDispatcher.hasMore()) {
			                    LDAPEntry entryInSubTree = subTreeDispatcher.next();
			                    
			                    IPair<SyncResultEnum, ? extends IElementBase> r = creator.createContentElement(entryInSubTree);
			                    
			                    if (updateSyncResult(r.first(), r.second(), elements, null, syncResult)) {
			                        counter++;
			                    }
			                }
			            } else { 
			            	// if not moved in, it will be added as a new entry
			                IPair<SyncResultEnum, ? extends IElementBase> r = creator.createContentElement(entry);
			                if (updateSyncResult(r.first(), r.second(), elements, null, syncResult)) {
                                counter++;
                            }
			            }
			        }
			    } 

			    // Commit every COMMIT_SIZE records:
			    if ((counter % batchSize) == 0) {
			        saveAndDeleteElements(enrollmentSession, elements, elements2Delete);
			        elements.clear();
			        elements2Delete.clear();
			    }
			} // while loop through each changed entry from external directory

			// Save remaining elements
			if (!elements.isEmpty() || !elements2Delete.isEmpty()) {
				saveAndDeleteElements(enrollmentSession, elements, elements2Delete);
			}
		} catch (RetrievalFailedException e) {
		    //TODO, the lastParedDn is not set
		    //currentParsedDn is the last dn
		    String failedEntry = e.getEntry();
            throw failedEntry != null
                    ? new EnrollmentSyncException(e, failedEntry)
                    : new EnrollmentSyncException(e, currentParsedDn, false);
		} catch (DictionaryException e) {
		  //TODO, the lastParedDn is not set
			throw new EnrollmentSyncException(e, currentParsedDn);
		}
        LOG.info("Saving Active Directory change elements total: " + (counter - 1));
    }

	private void saveAndDeleteElements(IEnrollmentSession enrollmentSession,
			Collection<? extends IElementBase> elementsTosave, Collection<? extends IElementBase> elementsToDelete)
			throws DictionaryException {
		enrollmentSession.beginTransaction();
		enrollmentSession.saveElements(elementsTosave);
		enrollmentSession.deleteElements(elementsToDelete);
		enrollmentSession.commit();
	}

    /**
     * Detect whether the input entry is deleted
     * 
     * @param entry:
     *            A LDAP entry object
     * @return boolean value: whether entry has been deleted
     */
    private boolean isDeleted(LDAPEntry entry) {
        LDAPAttribute isDeletedAttr = entry.getAttribute(adEnrollment.getIsDeletedAttributeName());
        if ((isDeletedAttr != null) && isDeletedAttr.getStringValue().equalsIgnoreCase("TRUE")) {
            return true;
        }
        return false;
    }

    /**
     * get Last known parent attribute value from a deleted LDAP entry
     * 
     * @param entry
     * @return the attribute value of lastKnownParent
     */
    private String getParentPreDelete(LDAPEntry entry) {
        LDAPAttribute parentAttr = entry.getAttribute(adEnrollment.getLastKnownParentAttributeName());
        if (parentAttr != null) {
            return parentAttr.getStringValue();
        }
        return null;
    }

    /**
     * detect the given dn string is in one of the enrollment subtrees
     * 
     * @param dn
     * @return true if the dn locates under one of subtrees
     */
    private boolean inEnrollmentScope(String dn) {
        if (dn != null) {
            String[] subTreesToEnroll = adEnrollment.getSubtreesToEnroll();
            for (int i = 0; i < subTreesToEnroll.length; i++) {
                if (dn.toLowerCase().endsWith(subTreesToEnroll[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Detect whether the entry is moved since last enrollment When entry is
     * moved in Active Directory, the LDAP entry always have the following
     * attributes: Entry changed:
     * 'CN=hosts,CN=Computers,DC=qa,DC=bluejungle,DC=com LDAPAttribute:
     * {type='instanceType', value='4'} LDAPAttribute: {type='name',
     * value='hosts'} LDAPAttribute: {type='objectGUID',
     * value='/w?%5HH??v?;?8'} LDAPAttribute: {type='parentGUID',
     * value='??}?mO?2?3V??'}
     * 
     * @param entry
     * @return boolean true if entry is moved
     */
    private boolean isMoved(LDAPEntry entry) {
        LDAPAttribute parentGUIDAttr = entry.getAttribute(adEnrollment.getParentGUIDAttributeName());
        if ((parentGUIDAttr != null) && (entry.getAttributeSet().size() == 4)) {
            return true;
        }
        return false;
    }
}
