/*
 * Created on Jun 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.dialect.Dialect;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IStartable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.HibernateUtils;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.shared.inquirymgr.ReportDataHolderManager;
import com.nextlabs.destiny.container.dac.datasync.Constants;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTask.SyncType;
import com.nextlabs.destiny.container.dac.datasync.sync.SyncTask;

/**
 * @author hchan
 * @version $Id:
 *          //depot/branch/Destiny_D_Reporter_Longevity/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/DataSyncManager.java#5 $
 */

public class DataSyncManager implements IDataSyncManager, IConfigurable,
        IInitializable, IStartable, ILogEnabled, IHasComponentInfo<DataSyncManager> {
    private static final int DEFAULT_WAIT_TIMEOUT = 1000 * 60 * 5;// 5 minutes
    private static final int DEFAULT_CHECK_INTERVAL = 1000 * 60 * 1;// 1 minutes
    
    private static final String CONTRADICT_ERR_MESSAGE = 
        "%1$s and %2$s can not be used at this same time in %3$s operation. "
        + "The system will only use %1$s";

    private static final ComponentInfo<DataSyncManager> COMP_INFO = 
        new ComponentInfo<DataSyncManager>(
            DataSyncManager.class.getName(), 
            DataSyncManager.class,
            IDataSyncManager.class, 
            LifestyleType.SINGLETON_TYPE);

    // configuration
    private Long syncTimeInterval;
    private Calendar syncTimeOfDay;
    private long syncTimeout;

    private Calendar indexRebuildTimeOfDay;
    private BitSet indexRebuildDayOfWeek;
    private BitSet indexRebuildDayOfMonth;
    private boolean indexRebuildAuto;
    private long indexRebuildTimeout;

    private Calendar archiveTimeOfDay;
    private BitSet archiveDayOfWeek;
    private BitSet archiveDayOfMonth;
    private int archiveDaysOfDataToKeep;
    private boolean archiveAuto;
    private long archiveTimeout;

    private IHibernateRepository dataSource;
    private ScheduledExecutorService executorService;

    
    
    private Dialect dialect;

    private Map<Runnable, ScheduledFuture<?>> futureTasks = 
        Collections.synchronizedMap(new HashMap<Runnable, ScheduledFuture<?>>());

    private Log log;
    private IConfiguration configuration;
    
    private int checkTimeout;
    private int checkInterval;
    
    private String myId;
    private ReportDataHolderManager propertyManger;

    public void init() {
        dataSource = configuration.get(DATASOURCE_CONFIG_PARAM);
        dialect = getDialect();
        
        
        //init sync options

        syncTimeInterval = configuration.get(SYNC_TIME_INTERVAL_PARAM);
        syncTimeOfDay = configuration.get(SYNC_TIME_OF_DAY_PARAM);
        if (syncTimeInterval != null && syncTimeOfDay != null) {
            getLog().warn(String.format(CONTRADICT_ERR_MESSAGE, "TimeInterval",
                    "TimeOfDay", "sync"));
            syncTimeOfDay = null;
        }
		syncTimeout = configuration.get(SYNC_TIMEOUT_PARAM);
		
		
		
		//init index rebulid options

		indexRebuildTimeOfDay = configuration.get(INDEXES_REBUILD_TIME_OF_DAY);
		indexRebuildDayOfWeek = configuration.get(INDEXES_REBUILD_DAY_OF_WEEK);
		indexRebuildDayOfMonth = configuration.get(INDEXES_REBUILD_DAY_OF_MONTH);
        if (indexRebuildDayOfWeek != null && indexRebuildDayOfMonth != null) {
            getLog().warn(String.format(CONTRADICT_ERR_MESSAGE, "DayOfWeek",
                    "DayOfMonth", "indexes rebuild"));
            indexRebuildDayOfMonth = null;
		} else if (indexRebuildDayOfWeek == null && indexRebuildDayOfMonth == null) {
			//no specific day is picked
			//will execute on everyday
			
			indexRebuildDayOfWeek = new BitSet(7);
			indexRebuildDayOfWeek.set(1, 7);
		}
        indexRebuildAuto = configuration.get(INDEXES_REBUILD_AUTO_REBUILD_INDEXES);
        indexRebuildTimeout = configuration.get(INDEXES_REBUILD_TIMEOUT_PARAM);

        
        
        //init achive options
        
        archiveTimeOfDay = configuration.get(ARCHIVE_TIME_OF_DAY);
        archiveDayOfWeek = configuration.get(ARCHIVE_DAY_OF_WEEK);
        archiveDayOfMonth = configuration.get(ARCHIVE_DAY_OF_MONTH);
        if (archiveDayOfWeek != null && archiveDayOfMonth != null) {
            getLog().warn(String.format(CONTRADICT_ERR_MESSAGE, "TimeInterval",
                    "TimeOfDay", "archive"));
            archiveDayOfMonth = null;
        } else if (archiveDayOfWeek == null && archiveDayOfMonth == null) {
			//no specific day is picked
			//will execute on everyday
			
        	archiveDayOfWeek = new BitSet(7);
        	archiveDayOfWeek.set(1, 7);
		}
		archiveDaysOfDataToKeep = configuration.get(ARCHIVE_DAYS_OF_DATA_TO_KEEP);
        archiveAuto = configuration.get(ARCHIVE_AUTO_ARCHIVE);
        archiveTimeout = configuration.get(ARCHIVE_TIMEOUT_PARAM);

        // the pool size must be 1 to avoid the overlap
        executorService = Executors.newScheduledThreadPool(1);
        
        String defaultId;
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            defaultId = localhost.getCanonicalHostName() + " - " + localhost.getHostAddress();
        } catch (UnknownHostException e) {
            defaultId = "unknown";
        }

        checkTimeout = configuration.get(WAIT_TIMEOUT_PARAM, DEFAULT_WAIT_TIMEOUT);
        checkInterval = configuration.get(CHECK_INTERVAL_PARAM, DEFAULT_CHECK_INTERVAL);
        
        myId = configuration.get(MANAGER_ID_PARAM, defaultId);
        
        getLog().trace("myId = " + myId);
        
        propertyManger = configuration.get(REPORT_DATA_HOLDER_MANAGER_PARAM);
    }
    
    public ComponentInfo<DataSyncManager> getComponentInfo() {
        return COMP_INFO;
    }
    
    public String getId() {
        return myId;
    }
    
    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    public void start() {
        register(new SyncTask());
        register(new Archiver());
        register(new IndexesRebuild());
    }

    public void stop() {
        executorService.shutdownNow();
    }
    
    protected void clear() throws HibernateException{
        propertyManger.remove(ACTIVE_MANAGER_KEY);
        propertyManger.remove(SYNC_ACTION_KEY);
        propertyManger.remove(SYNC_PROGRESS_KEY);
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    Dialect getDialect() throws IllegalArgumentException {
        return dataSource.getDialect();
    }

    public void register(final IDataSyncTask task)
            throws IllegalArgumentException {
        SyncType type = task.getType();

        switch (type) {
        case SYNC:
            scheduleSync(task);
            break;
        case INDEXES_REBUILD:
            if (indexRebuildAuto) {
                scheduleIndexRebuildTask(task);
            } else {
                getLog().info("Auto indexes rebuild is disabled.");
            }
            break;
        case ARCHIVE:
            if (archiveAuto) {
                scheduleArchiveTask(task);
            } else {
                getLog().info("Auto archive is disabled.");
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown sync type " + type);
        }
    }
    
    protected static final String ACTIVE_MANAGER_KEY  = "active.data.sync.manager";
    protected static final String SYNC_ACTION_KEY     = "active.data.sync.action";
    protected static final String SYNC_PROGRESS_KEY   = "active.data.sync.progress";
    
    /**
     * @return true if go, false otherwise.
     * @throws HibernateException
     */
    protected boolean trafficLight(long taskTimeout){
        long stopFromTime = System.currentTimeMillis();
        try {
            String currentManager = propertyManger.get(ACTIVE_MANAGER_KEY);
            getLog().trace(ACTIVE_MANAGER_KEY + " = " + currentManager);
            if (currentManager == null) {
                //green light
                propertyManger.add(ACTIVE_MANAGER_KEY, myId);
                return true;
            } else if (currentManager.equals(myId)) {
                //green light
                return true;
            }
            
            //yellow light now
            String lastAction = propertyManger.get(SYNC_ACTION_KEY);
            String lastProgress = propertyManger.get(SYNC_PROGRESS_KEY);

            getLog().trace("going to sleep, lastAction = " + lastAction + ", lastProgress = "
                            + lastProgress);
            long totalWaitTime;
            do{
                try {
                    wait(currentManager, lastAction, lastProgress);
                } catch (InterruptedException e) {
                    //get interrupted
                    //red light
                    return false;
                }
                
                currentManager = propertyManger.get(ACTIVE_MANAGER_KEY);
                getLog().trace(ACTIVE_MANAGER_KEY + " = " + currentManager);
                if (currentManager == null) {
                    //green light
                    propertyManger.add(ACTIVE_MANAGER_KEY, myId);
                    return true;
                } else if (currentManager.equals(myId)) {
                    //green light
                    return true;
                }
                
                //someone is holding the key
                
                //let see how it is going
                String action = propertyManger.get(SYNC_ACTION_KEY);
                String progress = propertyManger.get(SYNC_PROGRESS_KEY);
                
                getLog().trace("action = " + action + ", progress = "
                        + progress);
                
                totalWaitTime = System.currentTimeMillis() - stopFromTime;
                if (lastAction.equals(action) && lastProgress.equals(progress)) {
                    if (totalWaitTime > checkTimeout) {
                        //same actions and I wait too long, timeout!
                        if (propertyManger.saveOrUpdate(ACTIVE_MANAGER_KEY, myId)) {
                            getLog().info(
                                    "The previous manger '" + currentManager + "' is timeout.");
                            return true;
                        } else {
                            // someone do it before I do, wait again
                        }
                    } else {
                        // be patient
                    }
                } else {
                    lastAction = action;
                    lastProgress = progress;
                    stopFromTime = System.currentTimeMillis();
                }
            } while (totalWaitTime < taskTimeout);
            
            return false;
        } catch (HibernateException e) {
            getLog().error("Fail to read report internal table." + e);
            //red light
            return false;
        }
    }
    
    protected void wait(String currentManager, String currentAction, String currentProgress)
            throws InterruptedException {
        Thread.sleep(checkInterval);
    }
    
    //TODO implement formattable
    protected class DataSyncTaskUpdate implements IDataSyncTaskUpdate {
        protected int goodCount = 0;
        protected int badCount = 0;
        protected int total = 0;
        protected String prefix = "";
        
        public long getUpdateInterval() {
            return checkTimeout;
        }

        private void precheck() throws IllegalStateException {
            if (!alive()) {
                throw new IllegalStateException();
            }
        }

        private void postUpdate() throws IllegalStateException {
            try {
                if (!propertyManger.update(SYNC_PROGRESS_KEY, this.toString())) {
                    throw new IllegalStateException("Fail to update: " + SYNC_PROGRESS_KEY);
                }
            } catch (HibernateException e) {
                throw new IllegalStateException(e);
            }
        }
        
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
        
        public void addFail(int size) throws IllegalStateException {
            precheck();
            badCount += size;
            postUpdate();
        }

        public void addSuccess(int size) throws IllegalStateException {
            precheck();
            goodCount += size;
            if (getLog().isInfoEnabled()) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("Progress: ").append(goodCount).append('/').append(total);
                getLog().info(buffer.toString());
            }
            postUpdate();
        }

        public void setTotalSize(int size) throws IllegalStateException {
            precheck();
            this.total = size;
            postUpdate();
        }
        
        public void reset(){
            goodCount = 0;
            badCount = 0;
            total = 0;
            prefix = "";
        }

        public boolean alive() {
            try {
                String currentManager = propertyManger.get(ACTIVE_MANAGER_KEY);
                return currentManager != null && currentManager.equals(myId);
            } catch (HibernateException e) {
                getLog().error("fail to check the active manager.", e);
                return false;
            }
        }

        private static final String FORMAT = "%d/%d/%d";

        @Override
        public String toString() {
            return prefix + " " + String.format(FORMAT, total, goodCount, badCount);
        }

    }
    

    protected void scheduleSync(final IDataSyncTask task, long delay,
            final long taskTimeout, final IConfiguration config) {
        getLog().debug("schedule " + task.getType() + " task, delay = " + delay);
        Runnable r = new Runnable() {
            public void run() {
                try{
                    if (trafficLight(taskTimeout)) {
                        long startTime = System.currentTimeMillis();
                        getLog().debug("scheduled " + task.getType() + " task start.");
                        Session session = null;
                        try {
                            session = dataSource.getSession();
                            if (!propertyManger.saveOrUpdate(SYNC_ACTION_KEY, 
                                    task.getType().name())) {
                                getLog().error("fail to update: "+ SYNC_ACTION_KEY);
                                return;
                            }
                            if (!propertyManger.saveOrUpdate(SYNC_PROGRESS_KEY, "0")) {
                                getLog().error("fail to update: "+ SYNC_PROGRESS_KEY);
                                return;
                            }
                            
                            task.run(session, taskTimeout, config);
                        } catch (Throwable e) {
                            getLog().error("Error in " + task.getType() + " operation", e);
                        } finally {
                            getLog().debug("scheduled " + task.getType()
                                        + " task end. Total execution time = " 
                                        + (System.currentTimeMillis() - startTime));
                            futureTasks.remove(this);
                            
                            try {
                                propertyManger.remove(ACTIVE_MANAGER_KEY);
                            } catch (HibernateException e) {
                                getLog().error("fail to remote: "+ ACTIVE_MANAGER_KEY);
                            }
                            
                            HibernateUtils.closeSession(session, getLog());
                        }
                    } 
                    // no need to log, the trafficLight already log why.
                    //else {
                    //    getLog().
                    //}
                } catch (RuntimeException e) {
                    getLog().error("unexpected error", e);
                } catch (Error e) {
                    getLog().error("unexpected error", e);
                    throw e;
                } finally{
                    register(task);
                }
            }
        };
        futureTasks.put(r, executorService.schedule(r, delay,
                TimeUnit.MILLISECONDS));
    }
    
    protected HashMapConfiguration getCommonConfig(){
        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(IDataSyncTask.DIALECT_CONFIG_PARAMETER, dialect);
        config.setProperty(IDataSyncTask.TASK_UPDATE_PARAMETER, new DataSyncTaskUpdate());
        
        config.setProperty(Constants.USER_ATTRIBUTES_BLACKLIST_PROPERTY, 
        		configuration.get(Constants.USER_ATTRIBUTES_BLACKLIST_PROPERTY));
        
        config.setProperty(Constants.RESOURCE_ATTRIBUTES_BLACKLIST_PROPERTY, 
        		configuration.get(Constants.RESOURCE_ATTRIBUTES_BLACKLIST_PROPERTY));
        
        config.setProperty(Constants.POLICY_ATTRIBUTES_BLACKLIST_PROPERTY, 
        		configuration.get(Constants.POLICY_ATTRIBUTES_BLACKLIST_PROPERTY));
        
        config.setProperty(Constants.NUMBER_OF_EXTENDED_ATTRS_PROPERTY, 
        		configuration.get(Constants.NUMBER_OF_EXTENDED_ATTRS_PROPERTY));
        
        return config;
    }

    private void scheduleSync(final IDataSyncTask task) {
        long delay = syncTimeInterval != null 
                ? syncTimeInterval
                : calcuateTimeOfDayDelay(syncTimeOfDay);

        HashMapConfiguration config = getCommonConfig();
        scheduleSync(task, delay, syncTimeout, config);
    }

    private void scheduleIndexRebuildTask(final IDataSyncTask task) {
        long delay = indexRebuildDayOfWeek != null 
                ? calcuateTimeOfDayDelayWeek(indexRebuildTimeOfDay, indexRebuildDayOfWeek)
                : calcuateTimeOfDayDelayMonth(indexRebuildTimeOfDay, indexRebuildDayOfMonth);

        HashMapConfiguration config = getCommonConfig();
        scheduleSync(task, delay, indexRebuildTimeout, config);
    }

    private void scheduleArchiveTask(final IDataSyncTask task) {
        long delay = archiveDayOfWeek != null 
                ? calcuateTimeOfDayDelayWeek(archiveTimeOfDay, archiveDayOfWeek)
                : calcuateTimeOfDayDelayMonth(archiveTimeOfDay, archiveDayOfMonth);

        HashMapConfiguration config = getCommonConfig();
        config.setProperty(ARCHIVE_DAYS_OF_DATA_TO_KEEP,
                archiveDaysOfDataToKeep);
        scheduleSync(task, delay, archiveTimeout, config);
    }

    private long calcuateTimeOfDayDelay(Calendar timeOfDay) {
        return calcuateTimeOfDayDelay(timeOfDay, Calendar.getInstance());
    }

    long calcuateTimeOfDayDelay(Calendar timeOfDay, Calendar now) {
        Calendar c = (Calendar) timeOfDay.clone();
        c.set(now.get(Calendar.YEAR), 
              now.get(Calendar.MONTH), 
              now.get(Calendar.DATE));

        // add one day if the day is before now, which is yesterday
        if (c.before(now)) {
            c.add(Calendar.DATE, 1);
        }

        assert !c.before(now);
        return c.getTimeInMillis() - now.getTimeInMillis();
    }

    private long calcuateTimeOfDayDelayWeek(Calendar timeOfDay,
            BitSet daysOfWeek) {
        return calcuateTimeOfDayDelayWeek(timeOfDay, Calendar.getInstance(),
                daysOfWeek);
    }

    long calcuateTimeOfDayDelayWeek(Calendar timeOfDay, Calendar now,
            BitSet daysOfWeek) {
        Calendar c = (Calendar) timeOfDay.clone();
        c.set(now.get(Calendar.YEAR), 
              now.get(Calendar.MONTH), 
              now.get(Calendar.DATE));

        int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int startDay = currentDayOfWeek + (c.before(now) ? 1 : 0);

        Integer diffDay = null;
        for (int i = startDay; i <= 7; i++) {
            if (daysOfWeek.get(i - 1)) {
                diffDay = i - currentDayOfWeek;
                break;
            }
        }
        if (diffDay == null) {
            for (int i = 1; i < startDay; i++) {
                if (daysOfWeek.get(i - 1)) {
                    diffDay = i - currentDayOfWeek + 7;
                    break;
                }
            }
        }

        assert diffDay != null;
        c.add(Calendar.DATE, diffDay);

        assert !c.before(now);
        return c.getTimeInMillis() - now.getTimeInMillis();
    }

    private long calcuateTimeOfDayDelayMonth(Calendar timeOfDay,
            BitSet daysOfMonth) {
        return calcuateTimeOfDayDelayMonth(timeOfDay, Calendar.getInstance(),
                daysOfMonth);
    }

    long calcuateTimeOfDayDelayMonth(Calendar timeOfDay, Calendar now,
            BitSet daysOfMonth) {
        Calendar c = null;
        int currentDayOfMonth = now.get(Calendar.DAY_OF_MONTH);

        for (int i = currentDayOfMonth; i <= 31; i++) {
            if (daysOfMonth.get(i - 1)) {
                c = (Calendar) timeOfDay.clone();
                c.set(now.get(Calendar.YEAR), 
                      now.get(Calendar.MONTH), 
                      now.get(Calendar.DATE));
                c.set(Calendar.DAY_OF_MONTH, i);

                // the day is rollover to next month
                if (c.before(now)) {
                    c = null;
                } else if (c.get(Calendar.MONTH) > now.get(Calendar.MONTH)) {
                    // the month doesn't have that day, such as setting 31 on
                    // February
                    // however, no need to worry about roll over to next year
                    // since it won't happen
                    // because December always has 31st

                    c = (Calendar) timeOfDay.clone();
                    c.set(now.get(Calendar.YEAR), 
                          now.get(Calendar.MONTH), 
                          now.get(Calendar.DATE));
                    c.set(Calendar.DAY_OF_MONTH, c
                            .getActualMaximum(Calendar.DAY_OF_MONTH));
                    if (c.before(now)) {
                        c = null;
                        // today is the last day of the month so I haven't found
                        // one yet
                        continue;
                    }
                } else {
                    // we found a good day
                }
                break;
            }
        }
        if (c == null) {
            for (int i = 1; i <= currentDayOfMonth; i++) {
                if (daysOfMonth.get(i - 1)) {
                    c = (Calendar) timeOfDay.clone();
                    c.set(now.get(Calendar.YEAR), 
                          now.get(Calendar.MONTH), 
                          now.get(Calendar.DATE));
                    c.add(Calendar.MONTH, 1);
                    c.set(Calendar.DAY_OF_MONTH, i);
                    break;
                }
            }
        }

        assert !c.before(now);
        return c.getTimeInMillis() - now.getTimeInMillis();
    }
}
