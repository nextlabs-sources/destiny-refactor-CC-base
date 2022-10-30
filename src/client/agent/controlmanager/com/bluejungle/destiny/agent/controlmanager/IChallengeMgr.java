/*
 * Created on Nov 22, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

/**
 * This is the challenge manager interface.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/IChallengeMgr.java#1 $
 */

public interface IChallengeMgr {

    /**
     * Verifies if the answer to the challenge is correct.
     * 
     * @param answer
     *            candidate answer
     * @return true if the challenge was answered properly, false otherwise
     */
    public boolean answerChallenge(String answer);

    /**
     * Returns a new challenge to the caller
     * 
     * @return a new challenge string
     */
    public String getNewChallenge();

    /**
     * Returns true if the challenge manager is waiting for an answer
     * 
     * @return true if the challenge manager is waiting for an answer
     */
    public boolean hasPendingChallenge();
}
