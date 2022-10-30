/*
 * Created on Jun 21, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.datasync;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.hibernate.HibernateException;

import com.bluejungle.framework.comp.HashMapConfiguration;
import com.nextlabs.destiny.container.dac.datasync.Archiver;
import com.nextlabs.destiny.container.dac.datasync.DataSyncManager;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncManager;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTask;
import com.nextlabs.destiny.container.dac.datasync.IndexesRebuild;
import com.nextlabs.destiny.container.dac.datasync.sync.SyncTask;
import com.nextlabs.shared.tools.StringFormatter;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.display.ProgressBar;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/datasync/src/java/main/com/nextlabs/destiny/tools/datasync/DataSyncManagerConsole.java#1 $
 */

public class DataSyncManagerConsole extends DataSyncManager{
    private static final String TRAFFIC_FORMAT = "%tT: The other one '%s' is doing '%s - %s'. I have to wait.";
    
    private final Timer timer;
   
    private DataSyncTaskUpdateConsole taskUpdate;
    private ProgressBar display;
    
    public DataSyncManagerConsole() {
        super();
        timer = new Timer();
    }
    
    void sync(){
        taskUpdate = new DataSyncTaskUpdateConsole();
        display = new Display();
        
        IDataSyncTask task = new SyncTask();
        long timeout = getConfiguration().get(IDataSyncManager.SYNC_TIMEOUT_PARAM);
        super.scheduleSync(task, 0, timeout, getCommonConfig());
    }
    
    void indexRebuild(){
        taskUpdate = new DataSyncTaskUpdateIndexesRebildConsole();
        display = new IndexesRebuildDisplay();
        
        IDataSyncTask task = new IndexesRebuild();
        long timeout = getConfiguration().get(IDataSyncManager.INDEXES_REBUILD_TIMEOUT_PARAM);
        super.scheduleSync(task, 0, timeout, getCommonConfig());
    }
    
    void archive() {
        taskUpdate = new DataSyncTaskUpdateConsole();
        display = new Display();
        
        IDataSyncTask task = new Archiver();
        long timeout = getConfiguration().get(IDataSyncManager.ARCHIVE_TIMEOUT_PARAM);
        HashMapConfiguration config = getCommonConfig();
        config.setProperty(ARCHIVE_DAYS_OF_DATA_TO_KEEP, 
        getConfiguration().get(IDataSyncManager.ARCHIVE_DAYS_OF_DATA_TO_KEEP));
        super.scheduleSync(task, 0, timeout, config);
    }
    
    @Override
    protected HashMapConfiguration getCommonConfig() {
        HashMapConfiguration config = super.getCommonConfig();
        config.setProperty(IDataSyncTask.TASK_UPDATE_PARAMETER, taskUpdate);
        return config;
    }
    
    
    @Override
    protected boolean trafficLight(long taskTimeout) {
        boolean result = super.trafficLight(taskTimeout);
        timer.schedule(new ProgressBarTimeTask(), 0, 5000);
        return result;
    }

    @Override
    protected void wait(String currentManager, String currentAction, String currentProgress)
            throws InterruptedException {
        System.out.println(String.format(TRAFFIC_FORMAT, new Date(), currentManager, currentAction,
                currentProgress));
        super.wait(currentManager, currentAction, currentProgress);
    }

    @Override
    public void register(IDataSyncTask task){
        //this method will be called after the task is done.
        
        super.stop();
        timer.cancel();
        
        System.out.println();
        System.out.println("done");
    }

    @Override
    public void start() {
        //don't call super!
       
    }
    
    private class ProgressBarTimeTask extends TimerTask{
        @Override
        public void run() {
            ConsoleDisplayHelper.redraw(display);
        }
    }
    
    protected void clear() throws HibernateException{
        super.clear();
    }
    
    private class DataSyncTaskUpdateConsole extends DataSyncTaskUpdate {
        int getGoodCount() {
            return goodCount;
        }
        int getBadCount() {
            return badCount;
        }
        int getTotal() {
            return total;
        }
        
        @Override
        public void setPrefix(String prefix) {
            super.setPrefix(prefix);
            System.out.println();
            System.out.println();
            System.out.println(prefix + " starts");
        }
        
        private void superSetPrefix(String prefix){
            super.setPrefix(prefix);
        }
        
        @Override
        public void addFail(int size) throws IllegalStateException {
            super.addFail(size);
            display.update((float) (goodCount + badCount) / total);
        }

        @Override
        public void addSuccess(int size) throws IllegalStateException {
            super.addSuccess(size);
            display.update((float) (goodCount + badCount) / total);
        }

        @Override
        public void setTotalSize(int size) throws IllegalStateException {
            super.setTotalSize(size);
            display.update(0F);
        }
        
        @Override
        public void reset() {
            super.reset();
            display.start();
            display.update(0F);
        }
    }
    
    private class Display extends ProgressBar {
        private static final String GOOD_FORMAT = "%d/%d   Time left: %s";
        private static final String BAD_FORMAT =  "%d(%d)/%d   Time left: %s";
        
        public Display() {
            //those numbers have no meaning
            super(10, 20, 10);
        }
        
        public int getLength() {
            return ConsoleDisplayHelper.getScreenWidth();
        }
        
        @Override
        public void update(float percent) {
            if(percent > 1F){
                percent = 1F;
            }else if (percent < 0F){
                percent = 0F;
            }
            super.update(percent);
        }

        public String getOutput() {
            String str;
            
            if(taskUpdate.getBadCount() > 0 ){
                str = String.format(BAD_FORMAT, 
                            taskUpdate.getGoodCount(), 
                            taskUpdate.getBadCount(), 
                            taskUpdate.getTotal(), 
                            ConsoleDisplayHelper.formatTime(getOverallEstiimateTimeLeft())
                );
            }else{
                str = String.format(GOOD_FORMAT, 
                        taskUpdate.getGoodCount(), 
                        taskUpdate.getTotal(), 
                        ConsoleDisplayHelper.formatTime(getOverallEstiimateTimeLeft())
                );
            }
            
            return StringFormatter.fitLength(str, ConsoleDisplayHelper.getScreenWidth());
            
        }
        public boolean isUpdateable() {
            return true;
        }
    }
    
    

    private class DataSyncTaskUpdateIndexesRebildConsole extends DataSyncTaskUpdateConsole {
        private int indexesCount = 0;
        private String lastTableName = null;

        @Override
        public void setPrefix(String prefix) {
            super.superSetPrefix(prefix);
            indexesCount++;

            int i = prefix.indexOf(" - ");
            String tableName = i != -1 ? prefix.substring(0, i) : prefix;

            if(lastTableName == null || !lastTableName.equals(tableName)){
                System.out.println("");
                System.out.println("");
                System.out.println("rebuilding indexes on table '" + tableName + "'");
                lastTableName = tableName; 
            }
        }

        @Override
        public void reset() {
            super.reset();
            indexesCount = 0;
        }

        @Override
        public int getGoodCount() {
            return indexesCount;
        }
    }
    
    private class IndexesRebuildDisplay extends ProgressBar {
        private static final String FORMAT = "%d/%d   Elapsed Time: %s";
        
        public IndexesRebuildDisplay() {
            //those numbers have no meaning
            super(10, 20, 10);
        }
        
        public int getLength() {
            return ConsoleDisplayHelper.getScreenWidth();
        }
        
        @Override
        public void update(float percent) {
            if(percent > 1F){
                percent = 1F;
            }else if (percent < 0F){
                percent = 0F;
            }
            super.update(percent);
        }
        
        public String getOutput() {
            return StringFormatter.fitLength(
                    String.format(FORMAT, 
                            taskUpdate.getGoodCount(), 
                            taskUpdate.getTotal(), 
                            ConsoleDisplayHelper.formatTime(getElapsedTime())
                    ), ConsoleDisplayHelper.getScreenWidth());
        }

        public boolean isUpdateable() {
            return true;
        }
        
    }
    
    
}
