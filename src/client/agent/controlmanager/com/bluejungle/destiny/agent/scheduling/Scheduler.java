/*
 * Created on Dec 15, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.scheduling;

import java.util.TimerTask;

import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.framework.types.TimeUnits;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.framework.comp.ComponentImplBase;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.utils.TimeUnitsUtils;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/scheduling/Scheduler.java#1 $:
 */

public class Scheduler extends ComponentImplBase implements IConfigurable, IInitializable, IHasComponentInfo<Scheduler> {

    public static final String NAME = "Agent Scheduler";

    private static final ComponentInfo<Scheduler> COMP_INFO = new ComponentInfo<Scheduler>(
    		NAME, 
    		Scheduler.class, 
    		null, 
    		LifestyleType.SINGLETON_TYPE);

    private Timer agentScheduler = null;

    private TimerTask heartBeatTask = null;
    private TimerTask heartbeatRecordTask = null;
    private TimerTask uploadLogsTask = null;

    /**
     * 
     * Cancel timer if one already exists. Create new timer. Add heartbeat and
     * upload logs tasks to the timer.
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        getLog().info("Agent Scheduler Initialized.");

        if (this.agentScheduler != null) {
            this.agentScheduler.stop();
        }
        this.agentScheduler = new Timer();

        CommProfileDTO commProfile = getCommunicationProfile();

        if (commProfile == null) {
            getLog().fatal("Agent Scheduler Init failed. Communication Profile not available.");
            return;
        }

        setHeartBeatTimer(commProfile);
        setHeartbeatRecordTimer(commProfile);
        setUploadLogTimer(commProfile);

        Thread schedulerThread = new Thread(this.agentScheduler, "AgentScheduler");
        schedulerThread.start();
    }

    /**
     * sets the log upload timer task
     * 
     * @param commProfile
     *            communication profile
     */
    private void setUploadLogTimer(CommProfileDTO commProfile) {
        // add upload logs task
        this.uploadLogsTask = new UploadLogsTask();
        long unitMultiplier = TimeUnitsUtils.getMultiplier(commProfile.getLogFrequency().getTimeUnit());
        long uploadLogsDelay = commProfile.getLogFrequency().getTime().intValue() * unitMultiplier;
        this.agentScheduler.scheduleTask(this.uploadLogsTask, uploadLogsDelay);
    }

    /**
     * sets the heartbeat recorder timer task
     * 
     * @param commProfile
     *            communication profile
     */
    private void setHeartbeatRecordTimer(CommProfileDTO commProfile) {
        // add upload logs task
        this.heartbeatRecordTask = new HeartbeatRecordTask();
        long unitMultiplier = TimeUnitsUtils.getMultiplier(TimeUnits.minutes);
        long heartbeatRecordDelay = 30 * unitMultiplier;
        this.agentScheduler.scheduleTask(this.heartbeatRecordTask, heartbeatRecordDelay);
    }

    /**
     * sets the heartbeat timer task
     * 
     * @param commProfile
     *            communication profile
     */
    private void setHeartBeatTimer(CommProfileDTO commProfile) {
        //add heartbeat task
        this.heartBeatTask = new HeartbeatTask();
        long unitMultiplier = TimeUnitsUtils.getMultiplier(commProfile.getHeartBeatFrequency().getTimeUnit());
        long heartBeatDelay = commProfile.getHeartBeatFrequency().getTime().intValue() * unitMultiplier;
        this.agentScheduler.scheduleTask(this.heartBeatTask, heartBeatDelay);
    }

    /**
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * @see com.bluejungle.framework.comp.IStartable#start()
     */
    public void start() {
    }

    /**
     * stops the scheduler
     * 
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public void stop() {
        getLog().info("Agent Scheduler Stopped.");
        if (this.agentScheduler != null) {
            this.agentScheduler.stop();
        }
    }

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<Scheduler> getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * Gets the Communication Profile from the control module and returns it.
     * 
     * @return the communication profile.
     */
    private CommProfileDTO getCommunicationProfile() {
        IControlManager controlManager = (IControlManager) this.configuration.get(IControlManager.NAME);
        if (controlManager == null) {
            getLog().fatal("Agent Scheduler could not be initialized. Control Manager not available.");
            return null;
        }

        return controlManager.getCommunicationProfile();
    }

    /**
     * resets the heartbeat timer by canceling the task and re-adding it. This
     * is called if the heartbeat is sent because of a server push.
     */
    public void resetHeartBeatTimer() {
        this.heartBeatTask.cancel();
        CommProfileDTO commProfile = getCommunicationProfile();

        if (commProfile == null) {
            getLog().fatal("Reset heartbeat failed. Communication Profile not available.");
            return;
        }
        setHeartBeatTimer(commProfile);
    }

}
