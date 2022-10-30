/*
 * Created on Feb 22, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollerCreationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentCheckException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentThreadException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentController;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnroller;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BasicLDAPEnrollmentProperties;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.DomainGroupEnroller;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentType;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IUpdateRecord;
import com.nextlabs.framework.messaging.IMessageHandler;
import com.nextlabs.framework.messaging.MessagingException;
import com.nextlabs.framework.messaging.impl.StringMessage;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author safdar
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/controller/defaultimpl/EnrollmentController.java#1 $
 */

public class EnrollmentController implements IEnrollmentController {
    private static final Log LOG = LogFactory.getLog(EnrollmentController.class);
    private static final int CORE_ENROLLMENT_THREAD_POOL = 1;
    
    /*
     * Private:
     */
    private final IEnrollerFactory enrollerFactory;
    private final IDictionary dictionary;
    private final IMessageHandler messageHandler;
    private final ExecutorService enrollmentTreadPool;
    private final ConcurrentMap<String, EnrollmentSyncTimerTask> enrollmentTasks;
    private final Timer timer;
    
    private void nullCheck(Object o, String name) throws NullPointerException {
        if (o == null) {
            throw new NullPointerException(name + " is null");
        }
    }

    public EnrollmentController(IDictionary dictionary, IEnrollerFactory enrollerFactory,
                                IMessageHandler messageHandler)
        throws DictionaryException {
        super();
        nullCheck(dictionary, "dictionary");
        nullCheck(enrollerFactory, "enroller factory");
        this.enrollerFactory = enrollerFactory;
        this.dictionary = dictionary;
        this.messageHandler = messageHandler;
        this.enrollmentTreadPool = Executors.newFixedThreadPool(CORE_ENROLLMENT_THREAD_POOL);
        this.enrollmentTasks = new ConcurrentHashMap<String, EnrollmentSyncTimerTask>();
        this.timer = new Timer("EnrollmentControllerTimer", true);
        
        // Read the enrollments and synch them:
        Collection<IEnrollment> enrollments = this.dictionary.getEnrollments();

        for (IEnrollment enrollment : enrollments) {
            addEnrollmentThread(enrollment);
        }
    }
    
    private class EnrollmentSyncTimerTask extends TimerTask{
        private final EnrollmentTask innerTask;

        private Future<Boolean> result;
    
        private boolean isTriggerBySystem;
    
        private EnrollmentSyncTimerTask(EnrollmentTask task) {
            super();
            this.innerTask = task;
            result = null;
        }
    
        public EnrollmentSyncTimerTask(IEnrollment enrollment, long startTime, long interval) {
            this(new EnrollmentTask(enrollment));
            long delay = getInitialDelay(startTime, interval);

            LOG.info("The next sync will be after " + ConsoleDisplayHelper.formatTime(delay) + ". And happen every " + ConsoleDisplayHelper.formatTime(interval));
            timer.scheduleAtFixedRate(this, delay, interval);

            updateEnrollmentSyncTime(enrollment, delay);
            isTriggerBySystem = true;
        }
    
        public EnrollmentSyncTimerTask(IEnrollment enrollment) {
            this(new EnrollmentTask(enrollment));
            timer.schedule(this, 0);
            isTriggerBySystem = false;
        }

        @Override
        public void run() {
            try {
                result = enrollmentTreadPool.submit(innerTask);
                try {
                    boolean isSuccess = result.get();
                } catch (InterruptedException e) {
                    LOG.error("The enrollment task is interrupted.", e);
                } catch (ExecutionException e) {
                    if (isTriggerBySystem) {
                        LOG.error("", e);
                    }
                    
                    if (innerTask.enrollment.getIsRecurring()) {
                        try {
                            email(innerTask.enrollment, e.getCause());
                        } catch (MessagingException e1) {
                            LOG.error("Fail to send message.", e1);
                        }
                    }
                }
            } catch (RuntimeException e) {
                LOG.error("Unexpected error while running the enrollment.", e);
            }
        }

        @Override
        public boolean cancel() {
            super.cancel();
            return result != null ? result.cancel(false) : true;
        }
    }
    
    private void email(IEnrollment enrollment, Throwable t) throws MessagingException{
        if(messageHandler==null){
            return;
        }
        String subject; 
        if (t != null) {
            subject = String.format("Error Notification: Problem with Automatic Enrollment Sync for \"%s\"",
                                    enrollment.getDomainName());
        }else{
            subject = String.format("Successful Notification: Automatic Enrollment Sync for \"%s\" is completed.",
                                    enrollment.getDomainName());
        }

        //needs double line break in plain text
        //http://support.microsoft.com/kb/287816
        String N = "\r\n\r\n";
        StringWriter stringWriter = new StringWriter();
        
        stringWriter.append("Control Center has encountered a problem during its automatic " 
                            + "enrollment sync procedure. A summary is displayed below; for more details, "
                            + "please check Policy Server log files under " 
                            + "[Policy Server Installation Folder]/server/logs.").append(N); 
        
        stringWriter.append("Enrollment name: ").append(enrollment.getDomainName()).append(N);
        
        String enrollmentTypeName;
        try {
            IEnroller enroller = this.enrollerFactory.getEnroller(enrollment);
            enrollmentTypeName = enroller.getEnrollmentType();
        } catch (EnrollerCreationException e1) {
            enrollmentTypeName = enrollment.getType();
            
        }
        stringWriter.append("Enrollment type: ").append(enrollmentTypeName).append(N);

        //don't show the update record because it was not updated.
        if(!( t instanceof EnrollmentCheckException)){
            try {
                IUpdateRecord record = enrollment.getStatus();
                if (record != null) {
                    stringWriter.append("Start time: ").append(record.getStartTime().toString()).append(N);
                    stringWriter.append("End time: ").append(record.getEndTime().toString()).append(N);
                    stringWriter.append("Error Message: ").append(record.getErrorMessage()).append(N);
                    
                }
            } catch (DictionaryException e) {
                stringWriter.append("Can not get enrollment records. ").append(N);
                stringWriter.append(e.toString()).append(N);
            }
        }
        
        if (t != null) {
            stringWriter.append(N);
            stringWriter.append("Detail exception: ").append(N);
            t.printStackTrace(new PrintWriter(stringWriter));
        }
        
        // StringWriter.close() does nothing
        //stringWriter.close();

        email(subject, stringWriter.toString());
    }
    
    private void email(String subject, String message) throws MessagingException{
        if(messageHandler==null){
            return;
        }

        messageHandler.sendMessage(new StringMessage(subject, message), null);
    }
    
    private void updateEnrollmentSyncTime(IEnrollment enrollment, long delay){
        if(delay > 0){
            Calendar nextSyncTime = Calendar.getInstance();
            
            while(delay >= Integer.MAX_VALUE){
                nextSyncTime.add(Calendar.MILLISECOND, Integer.MAX_VALUE);
                delay -= Integer.MAX_VALUE;
            }
            nextSyncTime.add(Calendar.MILLISECOND, (int)delay);
            enrollment.setNextSyncTime(nextSyncTime);
        }else{
            enrollment.setNextSyncTime(null);
        }
    }
    
    private long getInitialDelay(long startTime, long pullInterval) {
        if(pullInterval <= 0){
            return 0;
        }
        
        long delay;
        long currentTime = System.currentTimeMillis();
        long offset = (currentTime - startTime) % pullInterval;
        if (currentTime - startTime >= 0) {
            delay = pullInterval - offset;
        } else { // wait till enrollment start time 
            delay = startTime - currentTime;
        }
        return delay;
    }
    
    /**
     * Add enrollment only if the enrollment is recurring and active, otherwise ignore and return
     */
    private void addEnrollmentThread(IEnrollment enrollment) {
        // if the enrollment is not recurring, ignore it and return
        if (enrollment.getIsRecurring() && enrollment.getIsActive()) {
            long startTime = enrollment.getNumProperty(BasicLDAPEnrollmentProperties.START_TIME);
            // get interval in minutes, convert to milli-second
            long interval =enrollment.getNumProperty(
                BasicLDAPEnrollmentProperties.PULL_INTERVAL) * 60 * 1000;

            // sanity check
            if (interval <= 0) {
                LOG.info("Interval is <= 0 for enrollment " + enrollment.getDomainName() + ", but enrollment is marked as recurring. Please update enrollment to resolve this.");
                return;
            }
            EnrollmentSyncTimerTask task = new EnrollmentSyncTimerTask(enrollment, startTime, interval);
            enrollmentTasks.put(enrollment.getDomainName(), task);
        }
    } 
    
    public void deleteEnrollmentThread(IEnrollment enrollment) throws EnrollmentThreadException {
        TimerTask t = enrollmentTasks.remove(enrollment.getDomainName());
        if (t != null) {
            //TODO, check result
            t.cancel();
        }
        
        enrollment.setNextSyncTime(null);
    }
    
    /**
     * @throws EnrollerCreationException 
     * @throws EnrollmentValidationException 
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentController#process(com.bluejungle.dictionary.IEnrollment, java.util.Map)
     */
    public void process(IEnrollment enrollment, Map<String, String[]> properties)
        throws EnrollmentValidationException, EnrollerCreationException,
               EnrollmentThreadException, DictionaryException {
        IEnroller enroller = this.enrollerFactory.getEnroller(enrollment);
        enroller.process(enrollment, properties, dictionary);
                    
        deleteEnrollmentThread(enrollment);
        addEnrollmentThread(enrollment);
    }
    
    /**
     * Sync the enrollment 
     * @param enrollment
     * @throws EnrollmentFailedException 
     */
    public void sync(IEnrollment enrollment) throws EnrollmentValidationException,
                                                    EnrollmentSyncException, EnrollmentThreadException, DictionaryException {
        EnrollmentSyncTimerTask task;
        synchronized (enrollmentTasks) {
            task = enrollmentTasks.get(enrollment.getDomainName());
            if (task != null) {
                if (enrollment.getIsRecurring()) {
    
                    if( task.result != null && !task.result.isDone() ){
                        throw new EnrollmentThreadException("Enrollment is running");
                    }
    
                    task.cancel();
                } else {
                    throw new EnrollmentThreadException("Enrollment is running");
                }
            }

            task = new EnrollmentSyncTimerTask(enrollment);
            enrollmentTasks.put(enrollment.getDomainName(), task);
            
            try {
                while (task.result == null) {
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                throw new EnrollmentThreadException(e);
            }
        }

        try {
            if (task.result != null) {
                task.result.get(); // wait for task to finish
            }
        } catch (CancellationException e) {
            throw new EnrollmentThreadException(e);
        } catch (InterruptedException e) {
            throw new EnrollmentThreadException(e);
        } catch(ExecutionException e){
            // check if enrollTask encountered an exception (e.g. failed to sync because of invalid user information)
            Throwable enrollTaskException = e.getCause();
            if (enrollTaskException != null) {
                if (enrollTaskException instanceof EnrollmentValidationException) {
                    throw (EnrollmentValidationException) enrollTaskException;
                } else if (enrollTaskException instanceof EnrollmentThreadException) {
                    throw (EnrollmentThreadException) enrollTaskException;
                } else if (enrollTaskException instanceof EnrollmentSyncException) {
                    throw (EnrollmentSyncException) enrollTaskException;
                } else if (enrollTaskException instanceof DictionaryException) {
                    throw (DictionaryException) enrollTaskException;
                } else {
                    //TODO
                    throw new RuntimeException(e);
                }
            }
        } finally{
            if (!enrollment.getIsRecurring()) {
                deleteEnrollmentThread(enrollment);
            }
            addEnrollmentThread(enrollment);
        }
    }
    
    private class EnrollmentTask implements Callable<Boolean> {
        private final IEnrollment enrollment;
    
        EnrollmentTask(IEnrollment enrollment) {
            this.enrollment = enrollment;
        }
    
        public Boolean call() throws 
            EnrollerCreationException,        
            EnrollmentCheckException, 
            EnrollmentValidationException,
            EnrollmentThreadException,
            DictionaryException, 
            EnrollmentSyncException
        {
            try {
                LOG.info( enrollment.getDomainName() + " Enrollment started..... ");
            
                // Block sync operation if there is a failed enrollment that is still active 
                // (active_to is bigger than current time) in the database.
                // Code around latestConsistantTime in Dictionary.java seems to have a philosophy that
                // "When one enrollment is broken, database is corrupt, so everything after that is broken, too."
                // To follow this philosophy, we should not let sync operation in such a state.
                // Special case: we allow sync of broken enrollment because it may be necessary to fix it. 
                // Additional Special case: we allow sync if the Dictionary error happened on a DomainGroup, since 
                // We want the sync of subdomains of the domaingroup to proceed to hopefully correct the error.
                if (dictionary.isThereFailedEnrollment(enrollment.getDomainName())) {
                    String enrollmentTypeStr = enrollment.getType();
                    int firstChar = enrollmentTypeStr.lastIndexOf('.')+1;
                    if ( firstChar > 0 ) {
                        enrollmentTypeStr = enrollmentTypeStr.substring( firstChar );
                    }
            
                    String dgEnrollerTypeStr = DomainGroupEnroller.class.getSimpleName();
                    if (!enrollmentTypeStr.equals(dgEnrollerTypeStr)) {
                        throw new EnrollmentCheckException("Database contains previously failed enrollment. " +
                                                           "Fix it before proceeding.");
                    }
                }
                IEnroller enroller = enrollerFactory.getEnroller(enrollment);
                if (Thread.currentThread().isInterrupted()) {
                    throw new EnrollmentThreadException("EnrollmentTask is interrupted.");
                }
                return enroller.sync(enrollment, dictionary);
            } finally {
                LOG.info( enrollment.getDomainName() + " Enrollment finished!");
            
                long startTime = enrollment.getNumProperty(BasicLDAPEnrollmentProperties.START_TIME);
                // get interval in minutes, convert to milli-second
                long interval = enrollment.getNumProperty(
                    BasicLDAPEnrollmentProperties.PULL_INTERVAL) * 60 * 1000;
                long delay = getInitialDelay(startTime, interval);
                updateEnrollmentSyncTime(enrollment, delay);
            }
        }
    }
}

    
