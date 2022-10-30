/*
 * Created on Mar 22, 2010
 *
 * All sources, binaries and HTML pages (C) copyright 2010 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.agent.controlmanager;

public interface ITrustedProcessManager
{
    /**
     * @param processID a process id
     * @return true if the process id is of a trusted process, false otherwise
     */
    public boolean isTrustedProcess(int processID);

    /**
     * Adds the given process id to the set of "trusted" processes
     * @param processID a process id
     */
    public void addTrustedProcess(int processID);

    /**
     * Adds the given process id to the set of permanent "trusted" processes.
     * Unlike processIDs added by addTrustedProcess, these will never time out
     * and are valid until the PC restarts
     * @param processID a process id
     */
    public void addPermanentTrustedProcess(int processID);
}
