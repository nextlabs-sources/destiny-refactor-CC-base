/*
 * Created on Dec 9, 2004
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dcsf;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis.types.PositiveInteger;
import org.apache.axis.types.URI;

import com.bluejungle.destiny.services.management.ComponentServiceIF;
import com.bluejungle.destiny.services.management.types.ApplicationUserConfiguration;
import com.bluejungle.destiny.services.management.types.AuthenticationMode;
import com.bluejungle.destiny.services.management.types.AuthenticatorConfiguration;
import com.bluejungle.destiny.services.management.types.ComponentList;
import com.bluejungle.destiny.services.management.types.ComponentHeartbeatInfo;
import com.bluejungle.destiny.services.management.types.ComponentHeartbeatUpdate;
import com.bluejungle.destiny.services.management.types.CustomObligations;
import com.bluejungle.destiny.services.management.types.DCCConfiguration;
import com.bluejungle.destiny.services.management.types.DCCRegistrationInformation;
import com.bluejungle.destiny.services.management.types.DCCRegistrationStatus;
import com.bluejungle.destiny.services.management.types.DMSConfiguration;
import com.bluejungle.destiny.services.management.types.DMSRegistrationOutcome;
import com.bluejungle.destiny.services.management.types.ExternalDomainConfiguration;
import com.bluejungle.destiny.services.management.types.RepositoryConfigurationList;
import com.bluejungle.destiny.services.management.types.UserAccessConfiguration;
import com.bluejungle.destiny.services.management.types.UserRepositoryConfiguration;

/**
 * This class is designed to fail the first time, and then succeed the second
 * time for the component registration. For the event registration /
 * unregistration, this dummy class can fails or succeed on demand.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dcsf/com/bluejungle/destiny/container/dcsf/tests/MockComponentServiceStub.java#1 $:
 */

public class MockComponentServiceStub implements ComponentServiceIF {

    public static final String LOCATION = "http://location.com";
    public static final PositiveInteger HEARTBEAT = new PositiveInteger("10");
    public static final String CONF_NAME = "testName";
    private static Boolean succeedEvents = new Boolean(false);
    private static List eventRegistrationList = new ArrayList();
    private static List eventUnregistrationList = new ArrayList();
    private int counter = 1;

    /**
     * @param regInfo
     *            registration info
     * @return the registration info
     * @throws RemoteException
     */
    public DCCRegistrationStatus registerComponent(DCCRegistrationInformation regInfo) throws RemoteException {
        if (this.counter == 1) { //fail onces
            this.counter++;
            throw new RemoteException("Call failed for the test");
        }

        DCCRegistrationStatus status = new DCCRegistrationStatus();
        status.setResult(DMSRegistrationOutcome.Success);
        DCCConfiguration config = new DMSConfiguration();
        config.setHeartbeatRate(HEARTBEAT);
        status.setConfiguration(config);
        status.setCustomObligationConfiguration(new CustomObligations());
        ApplicationUserConfiguration appUserConfig = new ApplicationUserConfiguration();
        appUserConfig.setAuthenticationMode(AuthenticationMode.Hybrid);
        UserRepositoryConfiguration userRepositoryConfig = new UserRepositoryConfiguration();
        userRepositoryConfig.setProviderClassName("some.classname");
        appUserConfig.setUserRepositoryConfiguration(userRepositoryConfig);
        ExternalDomainConfiguration externalDomainConfig = new ExternalDomainConfiguration(); 
        AuthenticatorConfiguration authConfig = new AuthenticatorConfiguration();
        authConfig.setAuthenticatorClassName("some.classname");
        externalDomainConfig.setAuthenticatorConfiguration(authConfig);
        
        UserAccessConfiguration userAccessConfig = new UserAccessConfiguration();
        userAccessConfig.setUserAccessProviderClassName("some.classname");
        externalDomainConfig.setUserAccessConfiguration(userAccessConfig);
        
        appUserConfig.setExternalDomainConfiguration(externalDomainConfig);
        status.setApplicationUserConfiguration(appUserConfig);
        
        RepositoryConfigurationList repositoryList = new RepositoryConfigurationList();        
        status.setRepositories(repositoryList);
        
        return status;
    }

    /**
     * @param unregInfo
     *            unregistation info
     * @throws RemoteException
     */
    public void unregisterComponent(DCCRegistrationInformation unregInfo) throws RemoteException {
    }

    /**
     * Returns the counter.
     * 
     * @return the counter.
     */
    public int getCounter() {
        return this.counter;
    }

    /**
     * @return the list of registered components
     * @throws RemoteException
     */
    public ComponentList getComponents() throws RemoteException {
        return null;
    }

    /**
     * @return the list of registered components by type
     * @throws RemoteException
     */
    public ComponentList getComponentsByType(String type) throws RemoteException {
        return null;
    }
    
    /**
     * This function checks if the class is set to allow the registration or
     * not. If not, it fakes a web service exception.
     * 
     * @param eventName
     *            name of the event
     * @param callback
     *            callback URL for DCSF service
     * @throws RemoteException
     * @see com.bluejungle.destiny.services.management.ComponentServiceIF#registerEvent(java.lang.String,
     *      org.apache.axis.types.URI)
     */
    public void registerEvent(String eventName, URI callback) throws RemoteException {
        synchronized (MockComponentServiceStub.succeedEvents) {
            if (!succeedEvents.booleanValue()) {
                throw new RemoteException("Dummy web service error");
            }
        }

        //remember that the event registration request has succeeeded
        synchronized (eventRegistrationList) {
            eventRegistrationList.add(new RegisterEventRequest(eventName, callback));
        }
    }

    /**
     * This function checks if the class is set to allow the unregistration or
     * not. If not, it fakes a web service exception.
     * 
     * @param eventName
     *            name of the event
     * @param callback
     *            callback URL for DCSF service
     * @throws RemoteException
     * @see com.bluejungle.destiny.services.management.ComponentServiceIF#registerEvent(java.lang.String,
     *      org.apache.axis.types.URI)
     */
    public void unregisterEvent(String eventName, URI callback) throws RemoteException {
        synchronized (MockComponentServiceStub.succeedEvents) {
            if (!succeedEvents.booleanValue()) {
                throw new RemoteException("Dummy web service error");
            }
        }

        //remember that this event unregistration request has succeeded
        synchronized (eventUnregistrationList) {
            eventUnregistrationList.add(new RegisterEventRequest(eventName, callback));
        }
    }

    /**
     * Sets the succeedEvents
     * 
     * @param succeedEvents
     *            The succeedEvents to set.
     */
    public static void setSucceedEvents(Boolean succeedEvents) {
        synchronized (MockComponentServiceStub.succeedEvents) {
            MockComponentServiceStub.succeedEvents = succeedEvents;
        }
    }

    /**
     * Returns the event Registration List.
     * 
     * @return the event Registration List.
     */
    public static List getEventRegistrationList() {
        return eventRegistrationList;
    }

    /**
     * Returns the event unregistration List.
     * 
     * @return the event unregistration List.
     */
    public static List getEventUnregistrationList() {
        return eventUnregistrationList;
    }

    /**
     * Resets the dummy class statistics
     */
    public void reset() {
        synchronized (eventRegistrationList) {
            eventRegistrationList.clear();
        }

        synchronized (eventUnregistrationList) {
            eventUnregistrationList.clear();
        }
        counter = 0;
        synchronized (succeedEvents) {
            succeedEvents = new Boolean(false);
        }
    }

    /**
     * @param info
     * @return dummy update
     * @throws RemoteException
     *             if the hearbeat processing fails
     */
    public ComponentHeartbeatUpdate checkUpdates(ComponentHeartbeatInfo info) throws RemoteException {
        return null;
    }
}