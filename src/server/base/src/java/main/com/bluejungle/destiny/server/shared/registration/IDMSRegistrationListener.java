/*
 * Created on Oct 25, 2004
 */
package com.bluejungle.destiny.server.shared.registration;

/**
 * @author ihanen This interface is used as a callback once the DMS registration
 *         is done. Every DCC component should implement this interface so that
 *         they can be notified once their DMS registration is complete (or
 *         failed)
 */
public interface IDMSRegistrationListener {

    /**
     * Called when the DMS registration is complete
     * 
     * @param status
     *            answers returns by the management server about the
     *            registration.
     *  
     */
    public void onDMSRegistration(IDCCRegistrationStatus status);
}