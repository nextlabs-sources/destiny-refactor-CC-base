/*
 * Created on Nov 18, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.scheduling;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimerTask;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class Timer implements Runnable {

    List<TaskInfo> taskList = new ArrayList<TaskInfo>();
    private boolean isRunning;

    private class TaskInfo {

        TimerTask task;
        long delay;
        long lastExecutionTime;
        long nextExecutionTime;

        /**
         * Returns the delay.
         * 
         * @return the delay.
         */
        public final long getDelay() {
            return delay;
        }

        /**
         * Sets the delay
         * 
         * @param delay
         *            The delay to set.
         */
        public final void setDelay(long delay) {
            this.delay = delay;
        }

        /**
         * Returns the lastExecutionTime.
         * 
         * @return the lastExecutionTime.
         */
        public final long getLastExecutionTime() {
            return lastExecutionTime;
        }

        /**
         * Sets the lastExecutionTime
         * 
         * @param lastExecutionTime
         *            The lastExecutionTime to set.
         */
        public final void setLastExecutionTime(long lastExecutionTime) {
            this.lastExecutionTime = lastExecutionTime;
            nextExecutionTime = lastExecutionTime + delay;
        }

        /**
         * Returns the nextExecutionTime.
         * 
         * @return the nextExecutionTime.
         */
        public final long getNextExecutionTime() {
            return nextExecutionTime;
        }

        /**
         * Sets the nextExecutionTime
         * 
         * @param nextExecutionTime
         *            The nextExecutionTime to set.
         */
        public final void setNextExecutionTime(long nextExecutionTime) {
            this.nextExecutionTime = nextExecutionTime;
        }

        /**
         * Returns the task.
         * 
         * @return the task.
         */
        public final TimerTask getTask() {
            return task;
        }

        /**
         * Sets the task
         * 
         * @param task
         *            The task to set.
         */
        public final void setTask(TimerTask task) {
            this.task = task;
        }
    }

    /**
     * Constructor
     */
    public Timer() {
        isRunning = true;
    }

    public synchronized void stop() {
        isRunning = false;
        notify();
    }

    /**
     * schedule a task to be run every 'delay' seconds.
     * 
     * @param task
     *            task to run
     * @param delay
     *            delay in milliseconds between runs
     */
    public synchronized void scheduleTask(TimerTask task, long delay) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setTask(task);
        taskInfo.setDelay(delay);
        taskInfo.setLastExecutionTime(Calendar.getInstance().getTimeInMillis());
        taskInfo.setNextExecutionTime(taskInfo.getLastExecutionTime() + delay);
        taskList.add(taskInfo);
        notify();
    }

    public void run() {
        while (isRunning) {
            boolean runNow = false;
            long sleepTime = Long.MAX_VALUE;
            long now = Calendar.getInstance().getTimeInMillis();

            synchronized (this) {
                for (TaskInfo taskInfo : taskList) {
                    long delay = 0;
                    if (taskInfo.getLastExecutionTime() <= now) {
                        delay = taskInfo.getLastExecutionTime() + taskInfo.getDelay() - now;
                    } else {
                        // this means the machine time has changed. we will
                        // run tasks just to be safe
                        delay = 0;
                    }

                    if (delay <= 0) {
                        runNow = true;
                        break;
                    } else if (delay < sleepTime) {
                        sleepTime = delay;
                    }
                }
            }

            if (!runNow) {
                try {
                    synchronized (this) {
                        wait(sleepTime);
                    }
                } catch (InterruptedException e) {

                }
            }
            if (isRunning) {
                runTasks();
            }
        }
    }

    /**
     * run all tasks that are due to be run
     */
    private synchronized void runTasks() {
        long now = Calendar.getInstance().getTimeInMillis();

        for (TaskInfo taskInfo : taskList) {
            if (taskInfo.getNextExecutionTime() <= now || taskInfo.getLastExecutionTime() > now) {
                taskInfo.getTask().run();
                taskInfo.setLastExecutionTime(now);
            }
        }
    }

}
