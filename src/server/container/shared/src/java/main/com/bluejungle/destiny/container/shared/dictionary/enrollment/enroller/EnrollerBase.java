/*
 * Created on Mar 6, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.SyncResultEnum;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IConfigurationSession;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/EnrollerBase.java#1 $
 */

public abstract class EnrollerBase implements IEnroller {
    
    protected static final String STATUS_STARTING                      = "starting";
	protected static final String STATUS_CREATE_ENROLLMENT_SESSION     = "create enrollment session";
	protected static final String STATUS_BEGIN_ENROLLMENT_TRANSACTION  = "begin enrollment transaction";
	protected static final String STATUS_PRE_SYNC                      = "pre sync";
	protected static final String STATUS_CONSTRUCT_ELEMENT_CREATOR     = "construct element creator";
	protected static final String STATUS_FETCHING_DATA                 = "fetching data";
	protected static final String STATUS_PROCESS_CONTENT_ENROLLMENT    = "processContentEnrollment";
	protected static final String STATUS_PROCESS_CHANGE_ENROLLMENT     = "processChangeEnrollment";
	protected static final String STATUS_REMOVE_ELEMENTS               = "remove elements";
	protected static final String STATUS_POST_PROCESS                  = "post process after add";
	protected static final String STATUS_DONE                          = "done";
	protected static final String STATUS_REPORT_READY                  = "ready to report";
	protected static final String STATUS_ALL_DONE                      = "all done";
	
	private static final int MESSAGE_MAX_LENGTH = 1024;
	    
	
	protected abstract Log getLog();
	
	protected String status;
	
	protected class SyncResult{
	    private static final int UNKNOWN = Integer.MIN_VALUE;
	    
	    public boolean success   = false;
	    public String message    = null;
	    public int newCount      = UNKNOWN;
	    public int changeCount   = UNKNOWN;
	    public int nochangeCount = UNKNOWN;
	    public int deleteCount   = UNKNOWN;
	    public int ignoreCount   = UNKNOWN;
	    public int failedCount   = UNKNOWN;
	    public int totalCount    = UNKNOWN;
        @Override
        public String toString() {
            final String N = ConsoleDisplayHelper.NEWLINE;
            StringBuilder s = new StringBuilder();
            s.append("Enrollment Status Report").append(N)
             .append("success:         ").append(success)                    .append(N)
             .append("message:         ").append(message)                    .append(N)
             .append("new items:       ").append(prettyNumber(newCount))     .append(N)
             .append("changed items:   ").append(prettyNumber(changeCount))  .append(N)
             .append("unchanged items: ").append(prettyNumber(nochangeCount)).append(N)
             .append("deleted items:   ").append(prettyNumber(deleteCount))  .append(N)
             .append("ignored items:   ").append(prettyNumber(ignoreCount))  .append(N)
             .append("failed items:    ").append(prettyNumber(failedCount))  .append(N)
             .append("total items:     ").append(prettyNumber(totalCount))   .append(N);
            return s.toString();
        }
        
        private String prettyNumber(int number){
            return number == UNKNOWN ? "unknown" : Integer.toString(number);
        }
	}
	
	protected final void checkNull( Object obj, String message ) throws NullPointerException{
        if ( obj == null ) {
            throw new NullPointerException(message + " is null");
        }
    }
	
    public void process(IEnrollment enrollment, Map<String, String[]> properties,
            IDictionary dictionary) throws EnrollmentValidationException, DictionaryException,
            NullPointerException {
        checkNull(enrollment, "enrollment");
        checkNull(dictionary, "dictionary");
        checkNull(properties, "properties");
    }

    public boolean sync(IEnrollment enrollment, IDictionary dictionary)
            throws EnrollmentValidationException, EnrollmentSyncException, DictionaryException,
            NullPointerException {
        checkNull(enrollment, "enrollment");
        checkNull(dictionary, "dictionary");
        //TODO_OJA
        getLog().info("The " + getEnrollmentType() + " enrollment of '" + enrollment.getDomainName() + "' is started.");
        SyncResult syncResult = new SyncResult();
        status = STATUS_STARTING;
        
        boolean isSuccess = false;
        synchronized (this) {
            IEnrollmentSession enrollmentSession = null;
            
            String message = null;
            status = STATUS_CREATE_ENROLLMENT_SESSION;
            try{
                enrollmentSession = enrollment.createSession();
                status = STATUS_BEGIN_ENROLLMENT_TRANSACTION;
                enrollmentSession.beginTransaction();
                
                status = STATUS_PRE_SYNC;
                preSync(enrollment, dictionary, enrollmentSession);

                internalSync(enrollment, dictionary, enrollmentSession, syncResult);
                
                isSuccess = syncResult.success;
                message = syncResult.message;
                if (isSuccess && message == null) {
                    message = getSuccessMessage();
                }
                
                //set status STATUS_REPORT_READY, must be the last line of try block
                status = STATUS_REPORT_READY;
            } catch (EnrollmentValidationException e) {
                message = toMessage(e);
                throw e;
            } catch (EnrollmentSyncException e) {
                message = toMessage(e);
                throw e;
            } catch (DictionaryException e) {
                message = toMessage(e);
                throw e;
            } catch (RuntimeException e) {
                // the getMessage may not be enough information.
                message = e.toString();
                if(message == null){
                    
                }
                throw e;
            } finally {
                if (getLog().isTraceEnabled()) {
                    getLog().trace("status = " + status);
                    getLog().trace(syncResult);
                } else if (getLog().isInfoEnabled()) {
                    if (STATUS_REPORT_READY.equals(status)) {
                        getLog().info(syncResult);
                    }
                }
                close(enrollmentSession, isSuccess, message, getLog());
            }
        }
        status = "post sync";
        postSync();
        status = STATUS_ALL_DONE;
        return isSuccess;
    }
    
    private String toMessage(Throwable t) {
        String message = t.getMessage();
        if (message == null) {
            message = t.toString();
        }
        return message;
    }
    
    /**
     * the sync is going to start but haven't start yet. That's why you can't 
     * throw EnrollmentSyncExcpetion.
     * @param enrollment
     * @param dictionary
     * @param session
     * @throws EnrollmentValidationException
     * @throws DictionaryException
     */
    protected void preSync(IEnrollment enrollment, IDictionary dictionary,
            IEnrollmentSession session) throws EnrollmentValidationException, DictionaryException {
        //do nothing
    }
    
    /**
     * you can't throw DictionaryException or EnrollmentValidationException here, the sync is 
     * already started. You should know the name of last entry.
     * @param enrollment
     * @param dictionary
     * @param session
     * @param syncResult
     * @throws EnrollmentSyncException
     */
    protected abstract void internalSync(
            IEnrollment enrollment, 
            IDictionary dictionary,
            IEnrollmentSession session, 
            SyncResult syncResult
    ) throws EnrollmentSyncException;
    
    protected void threadCheck(String lastEntry) throws EnrollmentSyncException {
        if (Thread.interrupted()) {
            throw new EnrollmentSyncException("The thread is interrupted.", lastEntry);
        }
    }
    
    protected String getSuccessMessage() {
        return "The " + getEnrollmentType() + " enrollment succeeded.";
    }
    
    /**
     * you should not throw any exception in this method.
     */
    protected void postSync(){
        //do nothing
    }
    
    /**
     * 
     * @param resultEnum
     * @param element
     * @param saves
     * @param deletes
     * @param syncResult
     * @return true there is a change.
     */
    protected boolean updateSyncResult(SyncResultEnum resultEnum, IElementBase element,
            Collection<IElementBase> saves, Collection<IElementBase> deletes, SyncResult syncResult) {
     switch(resultEnum){
        case NEW_ENTRY:
            syncResult.newCount++;
            saves.add(element);
            return true;
        case MODIFY_ENTRY:
            syncResult.changeCount++;
            saves.add(element);
            return true;
        case UNMODIFY_ENTRY:
            syncResult.nochangeCount++;
            saves.add(element);
            return true;
        case DELETE_ENTRY:
            syncResult.deleteCount++;
            deletes.add(element);
            return true;
        case IGNORE_ENTRY:
            syncResult.ignoreCount++;
            return false;
        case ERROR_ENTRY:
            syncResult.failedCount++;
            return false;
        default:
            throw new IllegalArgumentException(resultEnum.name());
        }
    }
    
    protected void close(IEnrollmentSession enrollmentSession, boolean success, String message,
            Log log) {
        if (enrollmentSession == null) {
            return;
        }
        
        if (!success) {
            try {
                enrollmentSession.rollback();
            } catch (DictionaryException e) {
                log.error("Failed to rollback failed enrollment", e);
            }
        }

        //the LDAP exception may contains invalid UTF8 chars that database doesn't like
        if (message != null) {
            message = message.replaceAll("\\p{Cntrl}", "");
        
            if (message.length() > MESSAGE_MAX_LENGTH) {
                message = message.substring(0, MESSAGE_MAX_LENGTH);
            }
        }
        
        try {
            enrollmentSession.close(success, message);
        } catch (DictionaryException e) {
            log.error("Failed to close enrollment session", e);
        }
    }
    
    protected void rollback(IConfigurationSession session, Log log) {
    	if(session != null){
    		try {
				session.rollback();
			} catch (DictionaryException e) {
				log.error("Failed to rollack dictionary session",e);
			}
    	}
	}
    
    protected void close(IConfigurationSession session, Log log) {
    	if (session != null) {
			try {
				session.close();
			} catch (DictionaryException e) {
				log.error("Failed to close dictionary session",e);
			}
		}
	}
}
