/*
 * Created on Jun 9, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.dac.datasync;

import java.util.BitSet;
import java.util.Calendar;

import com.bluejungle.framework.comp.PropertyKey;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.nextlabs.destiny.container.shared.inquirymgr.ReportDataHolderManager;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/dac/src/java/main/com/nextlabs/destiny/container/dac/datasync/IDataSyncManager.java#1 $
 */

public interface IDataSyncManager {
    /**
     * Property that is in Long and represent time should be milliseconds
     */
    
    
    PropertyKey<IHibernateRepository> DATASOURCE_CONFIG_PARAM = new PropertyKey<IHibernateRepository>("DataSource");
    
    PropertyKey<Long>     SYNC_TIME_INTERVAL_PARAM = new PropertyKey<Long>(     "Sync TimeInterval");
    PropertyKey<Calendar> SYNC_TIME_OF_DAY_PARAM   = new PropertyKey<Calendar>( "Sync TimeOfDay");
    PropertyKey<Long>     SYNC_TIMEOUT_PARAM       = new PropertyKey<Long>(     "Sync Timeout");

    
    
    PropertyKey<Calendar> INDEXES_REBUILD_TIME_OF_DAY          = new PropertyKey<Calendar>( "IndexesRebuild TimeOfDay");
    PropertyKey<BitSet>   INDEXES_REBUILD_DAY_OF_WEEK          = new PropertyKey<BitSet>(   "IndexesRebuild DayOfWeek");
    PropertyKey<BitSet>   INDEXES_REBUILD_DAY_OF_MONTH         = new PropertyKey<BitSet>(   "IndexesRebuild DayOfMonth");
    PropertyKey<Boolean>  INDEXES_REBUILD_AUTO_REBUILD_INDEXES = new PropertyKey<Boolean>(  "IndexesRebuild AutoRebuildIndexes");
    PropertyKey<Long>     INDEXES_REBUILD_TIMEOUT_PARAM        = new PropertyKey<Long>(     "IndexesRebuild Timeout");
    
    
    
    PropertyKey<Calendar> ARCHIVE_TIME_OF_DAY          = new PropertyKey<Calendar>( "Archive TimeOfDay");
    PropertyKey<BitSet>   ARCHIVE_DAY_OF_WEEK          = new PropertyKey<BitSet>(   "Archive DayOfWeek");
    PropertyKey<BitSet>   ARCHIVE_DAY_OF_MONTH         = new PropertyKey<BitSet>(   "Archive DayOfMonth");
    PropertyKey<Integer>  ARCHIVE_DAYS_OF_DATA_TO_KEEP = new PropertyKey<Integer>(  "Archive DaysOfDataToKeep");
    PropertyKey<Boolean>  ARCHIVE_AUTO_ARCHIVE         = new PropertyKey<Boolean>(  "Archive AutoArchive");
    PropertyKey<Long>     ARCHIVE_TIMEOUT_PARAM        = new PropertyKey<Long>(     "Archive Timeout");
    
    /**
     * The time to determine the other people's sync is taking too long, in minutes   
     */
    PropertyKey<Integer> WAIT_TIMEOUT_PARAM = new PropertyKey<Integer>("wait timeout");
    
    PropertyKey<Integer> CHECK_INTERVAL_PARAM = new PropertyKey<Integer>("check interval");
    
    /**
     * the id of the manager. If the value is not value, id will be generated.
     */
    PropertyKey<String> MANAGER_ID_PARAM = new PropertyKey<String>("id");
    
    PropertyKey<ReportDataHolderManager> REPORT_DATA_HOLDER_MANAGER_PARAM = new PropertyKey<ReportDataHolderManager>("reportDataHolder");
    
    void register(IDataSyncTask task) throws IllegalArgumentException;
}
