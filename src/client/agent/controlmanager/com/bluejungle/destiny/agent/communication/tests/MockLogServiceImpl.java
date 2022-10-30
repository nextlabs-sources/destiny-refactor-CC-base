/*
 * Created on Dec 14, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.communication.tests;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;

import junit.framework.TestCase;

import org.apache.axis.client.Stub;

import com.bluejungle.destiny.agent.communication.CommunicationManager;
import com.bluejungle.destiny.agent.controlmanager.IControlManager;
import com.bluejungle.destiny.framework.types.ServiceNotReadyFault;
import com.bluejungle.destiny.framework.types.UnauthorizedCallerFault;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.LogUtils;
import com.bluejungle.domain.log.PolicyActivityLogEntry;
import com.bluejungle.domain.log.PolicyActivityLogEntryV2;
import com.bluejungle.domain.log.PolicyAssistantLogEntry;
import com.bluejungle.domain.log.TrackingLogEntry;
import com.bluejungle.domain.log.TrackingLogEntryV2;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;

import com.nextlabs.destiny.interfaces.log.v5.LogServiceIF;
import com.nextlabs.destiny.types.log.v5.LogStatus;
import com.nextlabs.domain.log.PolicyActivityLogEntryV3;
import com.nextlabs.domain.log.PolicyActivityLogEntryV4;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;
import com.nextlabs.domain.log.TrackingLogEntryV3;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class MockLogServiceImpl extends Stub implements LogServiceIF {

    public static final String FROM_FILE_NAME = "c:\\files\\my files\\your files\\our files\\bogus.txt";

    private static int NUMBER_OF_CALLS = 0;
    private static int NUMBER_OF_LOGS = 0;
    private String endpoint = null;

    /**
     * This function gets the encoded tracking log data and verifies that the
     * data that was sent is correct.
     * 
     * @see com.bluejungle.destiny.services.log.LogServiceIF#logTracking(java.lang.String)
     */
    public LogStatus logTracking(String data) throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        checkEndpoint();

        if (data != null) {
            try {
                ObjectInputStream ois = LogUtils.decodeData(data);
                int numEntries = ois.readInt();
                TrackingLogEntry[] entries = new TrackingLogEntry[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    TrackingLogEntry entry = new TrackingLogEntry();
                    entry.readExternal(ois);
                    entries[i] = entry;
                }
                for (int i = 0; i < numEntries; i++) {
                    TrackingLogEntry currentEntry = entries[i];
                    TestCase.assertEquals("Deserialized data should be correct", ActionEnumType.ACTION_MOVE, currentEntry.getAction());
                    TestCase.assertEquals("Deserialized data should be correct", 89, currentEntry.getApplicationId());
                    TestCase.assertEquals("Deserialized data should be correct", "notepad.exe", currentEntry.getApplicationName());
                    TestCase.assertNotNull("Deserialized data should be correct", currentEntry.getFromResourceInfo());
                    TestCase.assertEquals("Deserialized data should be correct", "c:\\files\\my files\\your files\\our files\\bogus.txt", currentEntry.getFromResourceInfo().getName());
                    TestCase.assertEquals("Deserialized data should be correct", "SOME-SID-LIKE-THING", currentEntry.getFromResourceInfo().getOwnerId());
                    TestCase.assertEquals("Deserialized data should be correct", 234, currentEntry.getFromResourceInfo().getSize());
                    TestCase.assertNull("Deserialized data should be correct", currentEntry.getToResourceInfo());
                }
                NUMBER_OF_LOGS+=numEntries;
            } catch (IOException e) {
                TestCase.fail("IOException thrown " + e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                TestCase.fail("ClassNotFoundException thrown " + e.getLocalizedMessage());
            }
        }
        
        NUMBER_OF_CALLS++;
        return LogStatus.Success;
    }

    /**
     * This function gets the encoded tracking log data and verifies that the
     * data that was sent is correct.
     * 
     * @see com.bluejungle.destiny.services.log.LogServiceIF#logTracking(java.lang.String)
     */
    public LogStatus logTrackingV2(String data) throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        checkEndpoint();

        if (data != null) {
            try {
                ObjectInputStream ois = LogUtils.decodeData(data);
                int numEntries = ois.readInt();
                TrackingLogEntryV2[] entries = new TrackingLogEntryV2[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    TrackingLogEntryV2 entry = new TrackingLogEntryV2();
                    entry.readExternal(ois);
                    entries[i] = entry;
                }
                for (int i = 0; i < numEntries; i++) {
                    TrackingLogEntryV2 currentEntry = entries[i];
                    TestCase.assertEquals("Deserialized data should be correct", ActionEnumType.ACTION_MOVE.getName(), currentEntry.getAction());
                    TestCase.assertEquals("Deserialized data should be correct", 89, currentEntry.getApplicationId());
                    TestCase.assertEquals("Deserialized data should be correct", "notepad.exe", currentEntry.getApplicationName());
                    TestCase.assertNotNull("Deserialized data should be correct", currentEntry.getFromResourceInfo());
                    TestCase.assertEquals("Deserialized data should be correct", "c:\\files\\my files\\your files\\our files\\bogus.txt", currentEntry.getFromResourceInfo().getName());
                    TestCase.assertEquals("Deserialized data should be correct", "SOME-SID-LIKE-THING", currentEntry.getFromResourceInfo().getOwnerId());
                    TestCase.assertEquals("Deserialized data should be correct", 234, currentEntry.getFromResourceInfo().getSize());
                    TestCase.assertNull("Deserialized data should be correct", currentEntry.getToResourceInfo());
                }
                NUMBER_OF_LOGS+=numEntries;
            } catch (IOException e) {
                TestCase.fail("IOException thrown " + e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                TestCase.fail("ClassNotFoundException thrown " + e.getLocalizedMessage());
            }
        }
        NUMBER_OF_CALLS++;
        return LogStatus.Success;
    }

    /**
     * This function gets the encoded tracking log data and verifies that the
     * data that was sent is correct.
     * 
     * @see com.bluejungle.destiny.services.log.LogServiceIF#logTrackingV3(java.lang.String)
     */
    public LogStatus logTrackingV3(String data) throws RemoteException, ServiceNotReadyFault, UnauthorizedCallerFault {
        checkEndpoint();

        if (data != null) {
            try {
                ObjectInputStream ois = LogUtils.decodeData(data);
                int numEntries = ois.readInt();
                TrackingLogEntryV3[] entries = new TrackingLogEntryV3[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    TrackingLogEntryV3 entry = new TrackingLogEntryV3();
                    entry.readExternal(ois);
                    entries[i] = entry;
                }
                for (int i = 0; i < numEntries; i++) {
                    TrackingLogEntryV3 currentEntry = entries[i];
                    TestCase.assertEquals("Deserialized data should be correct", ActionEnumType.ACTION_MOVE.getName(), currentEntry.getAction());
                    TestCase.assertEquals("Deserialized data should be correct", 89, currentEntry.getApplicationId());
                    TestCase.assertEquals("Deserialized data should be correct", "notepad.exe", currentEntry.getApplicationName());
                    TestCase.assertNotNull("Deserialized data should be correct", currentEntry.getFromResourceInfo());
                    TestCase.assertEquals("Deserialized data should be correct", "c:\\files\\my files\\your files\\our files\\bogus.txt", currentEntry.getFromResourceInfo().getName());
                    TestCase.assertEquals("Deserialized data should be correct", "SOME-SID-LIKE-THING", currentEntry.getFromResourceInfo().getOwnerId());
                    TestCase.assertEquals("Deserialized data should be correct", 234, currentEntry.getFromResourceInfo().getSize());
                    TestCase.assertNull("Deserialized data should be correct", currentEntry.getToResourceInfo());
                }
                NUMBER_OF_LOGS+=numEntries;
            } catch (IOException e) {
                TestCase.fail("IOException thrown " + e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                TestCase.fail("ClassNotFoundException thrown " + e.getLocalizedMessage());
            }
        }
        NUMBER_OF_CALLS++;
        return LogStatus.Success;
    }

    public LogStatus logPolicyAssistant(String data) throws RemoteException {
        checkEndpoint();

        if (data != null) {
            try {
                ObjectInputStream ois = LogUtils.decodeData(data);
                int numEntries = ois.readInt();
                PolicyAssistantLogEntry[] entries = new PolicyAssistantLogEntry[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    PolicyAssistantLogEntry entry = new PolicyAssistantLogEntry();
                    entry.readExternal(ois);
                    entries[i] = entry;
                }

                for (int i = 0; i < numEntries; i++) {
                    PolicyAssistantLogEntry currentEntry = entries[i];

                    TestCase.assertEquals("Deserialized data should be correct", "8675309", currentEntry.getLogIdentifier());
                    TestCase.assertEquals("Deserialized data should be correct", "Defooblifying Assistant", currentEntry.getAssistantName());
                    TestCase.assertEquals("Deserialized data should be correct", "-defoob -extra-foob", currentEntry.getAttrOne());
                    TestCase.assertEquals("Deserialized data should be correct", "Performs Defooblification On Fooblified Thingys", currentEntry.getAttrTwo());
                    TestCase.assertEquals("Deserialized data should be correct", "User defoobled correctly", currentEntry.getAttrThree());
                }
                NUMBER_OF_LOGS+=numEntries;
            } catch (IOException e) {
                TestCase.fail("IOException thrown " + e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                TestCase.fail("ClassNotFoundException thrown " + e.getLocalizedMessage());
            }
        }

        NUMBER_OF_CALLS++;
        return LogStatus.Success;
    }

    /**
     * @see com.bluejungle.destiny.services.log.LogServiceIF#log(com.bluejungle.destiny.services.log.types.LogInfo)
     */
    public LogStatus logPolicyActivity(String data) throws RemoteException {
        checkEndpoint();

        if (data != null) {
            try {
                ObjectInputStream ois = LogUtils.decodeData(data);
                int numEntries = ois.readInt();
                PolicyActivityLogEntry[] entries = new PolicyActivityLogEntry[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    PolicyActivityLogEntry entry = new PolicyActivityLogEntry();
                    entry.readExternal(ois);
                    entries[i] = entry;
                }
                for (int i = 0; i < numEntries; i++) {
                    PolicyActivityLogEntry currentEntry = entries[i];
                    TestCase.assertEquals("Deserialized data should be correct", 345, currentEntry.getPolicyId());
                    TestCase.assertEquals("Deserialized data should be correct", ActionEnumType.ACTION_MOVE, currentEntry.getAction());
                    TestCase.assertEquals("Deserialized data should be correct", 89, currentEntry.getApplicationId());
                    TestCase.assertEquals("Deserialized data should be correct", "notepad.exe", currentEntry.getApplicationName());
                    TestCase.assertEquals("Deserialized data should be correct", 456, currentEntry.getDecisionRequestId());
                    TestCase.assertNotNull("Deserialized data should be correct", currentEntry.getFromResourceInfo());
                    TestCase.assertEquals("Deserialized data should be correct", "c:\\files\\my files\\your files\\our files\\bogus.txt", currentEntry.getFromResourceInfo().getName());
                    TestCase.assertEquals("Deserialized data should be correct", "SOME-SID-LIKE-THING", currentEntry.getFromResourceInfo().getOwnerId());
                    TestCase.assertEquals("Deserialized data should be correct", 234, currentEntry.getFromResourceInfo().getSize());
                    TestCase.assertEquals("Deserialized data should be correct", PolicyDecisionEnumType.POLICY_DECISION_ALLOW, currentEntry.getPolicyDecision());
                    TestCase.assertNull("Deserialized data should be correct", currentEntry.getToResourceInfo());
                }
                NUMBER_OF_LOGS+=numEntries;
            } catch (IOException e) {
                TestCase.fail("IOException thrown " + e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                TestCase.fail("ClassNotFoundException thrown " + e.getLocalizedMessage());
            }
        }
        NUMBER_OF_CALLS++;
        return LogStatus.Success;
    }

    /**
     * @see com.bluejungle.destiny.services.log.LogServiceIF#log(com.bluejungle.destiny.services.log.types.LogInfo)
     */
    public LogStatus logPolicyActivityV2(String data) throws RemoteException {
        checkEndpoint();

        if (data != null) {
            try {
                ObjectInputStream ois = LogUtils.decodeData(data);
                int numEntries = ois.readInt();
                PolicyActivityLogEntryV2[] entries = new PolicyActivityLogEntryV2[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    PolicyActivityLogEntryV2 entry = new PolicyActivityLogEntryV2();
                    entry.readExternal(ois);
                    entries[i] = entry;
                }
                for (int i = 0; i < numEntries; i++) {
                    PolicyActivityLogEntryV2 currentEntry = entries[i];
                    TestCase.assertEquals("Deserialized data should be correct", 345, currentEntry.getPolicyId());
                    TestCase.assertEquals("Deserialized data should be correct", ActionEnumType.ACTION_MOVE.getName(), currentEntry.getAction());
                    TestCase.assertEquals("Deserialized data should be correct", 89, currentEntry.getApplicationId());
                    TestCase.assertEquals("Deserialized data should be correct", "notepad.exe", currentEntry.getApplicationName());
                    TestCase.assertEquals("Deserialized data should be correct", 456, currentEntry.getDecisionRequestId());
                    TestCase.assertNotNull("Deserialized data should be correct", currentEntry.getFromResourceInfo());
                    TestCase.assertEquals("Deserialized data should be correct", "c:\\files\\my files\\your files\\our files\\bogus.txt", currentEntry.getFromResourceInfo().getName());
                    TestCase.assertEquals("Deserialized data should be correct", "SOME-SID-LIKE-THING", currentEntry.getFromResourceInfo().getOwnerId());
                    TestCase.assertEquals("Deserialized data should be correct", 234, currentEntry.getFromResourceInfo().getSize());
                    TestCase.assertEquals("Deserialized data should be correct", PolicyDecisionEnumType.POLICY_DECISION_ALLOW, currentEntry.getPolicyDecision());
                    TestCase.assertNull("Deserialized data should be correct", currentEntry.getToResourceInfo());
                }
                NUMBER_OF_LOGS+=numEntries;
            } catch (IOException e) {
                TestCase.fail("IOException thrown " + e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                TestCase.fail("ClassNotFoundException thrown " + e.getLocalizedMessage());
            }
        }
        NUMBER_OF_CALLS++;
        return LogStatus.Success;
    }

    /**
     * @see com.bluejungle.destiny.services.log.LogServiceIF#log(com.bluejungle.destiny.services.log.types.LogInfo)
     */
    public LogStatus logPolicyActivityV3(String data) throws RemoteException {
        checkEndpoint();

        if (data != null) {
            try {
                ObjectInputStream ois = LogUtils.decodeData(data);
                int numEntries = ois.readInt();
                PolicyActivityLogEntryV3[] entries = new PolicyActivityLogEntryV3[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    PolicyActivityLogEntryV3 entry = new PolicyActivityLogEntryV3();
                    entry.readExternal(ois);
                    entries[i] = entry;
                }
                for (int i = 0; i < numEntries; i++) {
                    PolicyActivityLogEntryV3 currentEntry = entries[i];
                    TestCase.assertEquals("Deserialized data should be correct", 345, currentEntry.getPolicyId());
                    TestCase.assertEquals("Deserialized data should be correct", ActionEnumType.ACTION_MOVE.getName(), currentEntry.getAction());
                    TestCase.assertEquals("Deserialized data should be correct", 89, currentEntry.getApplicationId());
                    TestCase.assertEquals("Deserialized data should be correct", "notepad.exe", currentEntry.getApplicationName());
                    TestCase.assertEquals("Deserialized data should be correct", 456, currentEntry.getDecisionRequestId());
                    TestCase.assertNotNull("Deserialized data should be correct", currentEntry.getFromResourceInfo());
                    TestCase.assertEquals("Deserialized data should be correct", "c:\\files\\my files\\your files\\our files\\bogus.txt", currentEntry.getFromResourceInfo().getName());
                    TestCase.assertEquals("Deserialized data should be correct", "SOME-SID-LIKE-THING", currentEntry.getFromResourceInfo().getOwnerId());
                    TestCase.assertEquals("Deserialized data should be correct", 234, currentEntry.getFromResourceInfo().getSize());
                    TestCase.assertEquals("Deserialized data should be correct", PolicyDecisionEnumType.POLICY_DECISION_ALLOW, currentEntry.getPolicyDecision());
                    TestCase.assertNull("Deserialized data should be correct", currentEntry.getToResourceInfo());
                }
                NUMBER_OF_LOGS+=numEntries;
            } catch (IOException e) {
                TestCase.fail("IOException thrown " + e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                TestCase.fail("ClassNotFoundException thrown " + e.getLocalizedMessage());
            }
        }
        NUMBER_OF_CALLS++;
        return LogStatus.Success;
    }

    /**
     * @see com.bluejungle.destiny.services.log.LogServiceIF#log(com.bluejungle.destiny.services.log.types.LogInfo)
     */
    public LogStatus logPolicyActivityV5(String data) throws RemoteException {
        checkEndpoint();

        if (data != null) {
            try {
                ObjectInputStream ois = LogUtils.decodeData(data);
                int numEntries = ois.readInt();
                PolicyActivityLogEntryV5[] entries = new PolicyActivityLogEntryV5[numEntries];
                for (int i = 0; i < numEntries; i++) {
                    PolicyActivityLogEntryV5 entry = new PolicyActivityLogEntryV5();
                    entry.readExternal(ois);
                    entries[i] = entry;
                }
                for (int i = 0; i < numEntries; i++) {
                    PolicyActivityLogEntryV5 currentEntry = entries[i];
                    TestCase.assertEquals("Deserialized data should be correct", 345, currentEntry.getPolicyId());
                    TestCase.assertEquals("Deserialized data should be correct", ActionEnumType.ACTION_MOVE.getName(), currentEntry.getAction());
                    TestCase.assertEquals("Deserialized data should be correct", 89, currentEntry.getApplicationId());
                    TestCase.assertEquals("Deserialized data should be correct", "notepad.exe", currentEntry.getApplicationName());
                    TestCase.assertEquals("Deserialized data should be correct", 456, currentEntry.getDecisionRequestId());
                    TestCase.assertNotNull("Deserialized data should be correct", currentEntry.getFromResourceInfo());
                    TestCase.assertEquals("Deserialized data should be correct", "c:\\files\\my files\\your files\\our files\\bogus.txt", currentEntry.getFromResourceInfo().getName());
                    TestCase.assertEquals("Deserialized data should be correct", "SOME-SID-LIKE-THING", currentEntry.getFromResourceInfo().getOwnerId());
                    TestCase.assertEquals("Deserialized data should be correct", 234, currentEntry.getFromResourceInfo().getSize());
                    TestCase.assertEquals("Deserialized data should be correct", PolicyDecisionEnumType.POLICY_DECISION_ALLOW, currentEntry.getPolicyDecision());
                    TestCase.assertNull("Deserialized data should be correct", currentEntry.getToResourceInfo());
                }
                NUMBER_OF_LOGS+=numEntries;
            } catch (IOException e) {
                TestCase.fail("IOException thrown " + e.getLocalizedMessage());
            } catch (ClassNotFoundException e) {
                TestCase.fail("ClassNotFoundException thrown " + e.getLocalizedMessage());
            }
        }
        NUMBER_OF_CALLS++;
        return LogStatus.Success;
    }

    /**
     * checks that this service impl was created with the right endpoint
     */
    private void checkEndpoint() {
        IControlManager controlManager = (IControlManager) ComponentManagerFactory.getComponentManager().getComponent(IControlManager.NAME);
        if (controlManager == null) {
            TestCase.fail("Control Manager not available.");
        }
        TestCase.assertEquals("Unexpected Agent Service endpoint address", controlManager.getCommunicationProfile().getDABSLocation().toString() + CommunicationManager.LOG_SERVICE_SUFFIX, this.endpoint);
    }

    /**
     * Returns the numberOfCalls.
     * 
     * @return the numberOfCalls.
     */
    public int getNumberOfCalls() {
        return NUMBER_OF_CALLS;
    }

    /**
     * Returns the numberOfLogs.
     * 
     * @return the numberOfLogs.
     */
    public int getNumberOfLogs() {
        return NUMBER_OF_LOGS;
    }

    /**
     * Returns the endpoint.
     * 
     * @return the endpoint.
     */
    public String getEndpoint() {
        return this.endpoint;
    }
    
    public void reset () {
        NUMBER_OF_CALLS = 0;
        NUMBER_OF_LOGS = 0;
    }

    /**
     * Sets the endpoint
     * 
     * @param endpoint
     *            The endpoint to set.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

}
