/*
 * Created on Nov 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.nextlabs.random.RandomString;

/**
 * This is the challenge manager implementation class.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/ChallengeMgrImpl.java#1 $
 */

public class ChallengeMgrImpl implements IChallengeMgr, IHasComponentInfo<ChallengeMgrImpl>, IInitializable, ILogEnabled, IManagerEnabled {

    /**
     * Constants for the random string generation
     */
    private static final int MIN_ASCII = 63;
    private static final int MAX_ASCII = 126;
    private static final int CHALLENGE_SIZE = 30;

    private static final Object LOCK_OBJECT = new Object();
    private static String expectedAnswer = "";

    private IOSWrapper osWrapper;

    /**
     * Component manager information
     */
    public static final ComponentInfo<ChallengeMgrImpl> COMP_INFO = 
    	new ComponentInfo<ChallengeMgrImpl>(
    		"ChallengeMgr", 
    		ChallengeMgrImpl.class, 
    		LifestyleType.SINGLETON_TYPE);

    private IComponentManager compMgr;
    private Log log;

    /**
     * Constructor
     */
    public ChallengeMgrImpl() {
        super();
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IChallengeMgr#answerChallenge(java.lang.String)
     */
    public boolean answerChallenge(String answer) {
        final boolean result;
        synchronized (LOCK_OBJECT) {
            result = expectedAnswer.equals(answer);
            setExpectedAnswer("");
        }
        return result;
    }

    /**
     * Calculates the expected answer to the challenge.
     * 
     * @param challenge
     *            challenge string
     * @return the expected answer to the challenge.
     */
    protected String calculateChallengeAnswer(String challenge) {
        return getOSWrapper().hashChallenge(challenge);
    }

    /**
     * @see com.bluejungle.framework.comp.IHasComponentInfo#getComponentInfo()
     */
    public ComponentInfo<ChallengeMgrImpl> getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.compMgr;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IChallengeMgr#getNewChallenge()
     */
    public String getNewChallenge() {
        final String newChallenge;
        synchronized (LOCK_OBJECT) {
            newChallenge = RandomString.getRandomString(CHALLENGE_SIZE, CHALLENGE_SIZE, MIN_ASCII, MAX_ASCII);
            setExpectedAnswer(calculateChallengeAnswer(newChallenge));
        }
        return newChallenge;

    }

    /**
     * Returns the OS wrapper object
     * 
     * @return the OS wrapper object
     */
    protected IOSWrapper getOSWrapper() {
        return this.osWrapper;
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IChallengeMgr#hasPendingChallenge()
     */
    public boolean hasPendingChallenge() {
        final boolean result;
        synchronized (LOCK_OBJECT) {
            result = (expectedAnswer.length() > 0);
        }
        return result;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        this.osWrapper = (IOSWrapper) getManager().getComponent(OSWrapper.class);
    }

    /**
     * Sets the expected answer to the challenge
     * 
     * @param newExpectedAnswer
     *            expected answer to set
     */
    protected void setExpectedAnswer(String newExpectedAnswer) {
        expectedAnswer = newExpectedAnswer;
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log newLog) {
        this.log = newLog;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager newMgr) {
        this.compMgr = newMgr;
    }

}
