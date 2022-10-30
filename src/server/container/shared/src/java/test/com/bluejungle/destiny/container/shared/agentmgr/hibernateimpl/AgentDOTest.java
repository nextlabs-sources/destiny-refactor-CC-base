/*
 * Created on Feb 3, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl;

import com.bluejungle.destiny.container.shared.agentmgr.IAgentManager;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentStartupConfiguration;
import com.bluejungle.destiny.container.shared.agentmgr.IAgentType;
import com.bluejungle.destiny.container.shared.agentmgr.PersistenceException;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO;
import com.bluejungle.destiny.container.shared.profilemgr.ICommProfileData;
import com.bluejungle.destiny.container.shared.profilemgr.IProfileManager;
import com.bluejungle.destiny.container.shared.profilemgr.hibernateimpl.HibernateProfileManager;
import com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.datastore.hibernate.exceptions.DataSourceException;
import com.bluejungle.framework.utils.TimeInterval;
import com.bluejungle.version.IVersion;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/test/com/bluejungle/destiny/container/shared/agentmgr/hibernateimpl/AgentDOTest.java#1 $
 */

public class AgentDOTest extends BaseContainerSharedTestCase {

    private static final String TEST_HOST_NAME = "foo.nextlabs.com";

    private IAgentManager agentManager;

    private AgentDO agentToTest;
    private IProfileManager profileManager;

    /**
     * @throws java.lang.Exception
     */
    public void setUp() throws Exception {
        super.setUp();

        // Initialize the necessary manager objects:
        // Initialize the component manager:
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();

        // Initialize the agent manager:
        ComponentInfo agentMgrCompInfo = new ComponentInfo(IAgentManager.COMP_NAME, AgentManager.class.getName(), IAgentManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.agentManager = (AgentManager) compMgr.getComponent(agentMgrCompInfo);

        // Initialize the profile manager:
        ComponentInfo profileMgrCompInfo = new ComponentInfo(IProfileManager.COMP_NAME, HibernateProfileManager.class.getName(), IProfileManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        this.profileManager = (IProfileManager) compMgr.getComponent(profileMgrCompInfo);

        IAgentType desktopAgentType = this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName());

        // Agent (A, Desktop)
        MockAgentRegistrationData mockRegistrationInfo = new MockAgentRegistrationData(TEST_HOST_NAME, desktopAgentType);
        IAgentStartupConfiguration agentConfiguration = this.agentManager.registerAgent(mockRegistrationInfo);
        Long agentId = agentConfiguration.getId();
        this.agentToTest = (AgentDO) this.agentManager.getAgentById(agentId);
    }

    /**
     * @throws java.lang.Exception
     */
    public void tearDown() throws Exception {
        this.agentManager.unregisterAgent(this.agentToTest.getId());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#hashCode()}.
     */

    public void testHashCode() {
        // Just call it to see if it doesn't explode
        this.agentToTest.hashCode();
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getId()}.
     */

    public void testGetSetId() {
        assertNotNull("Ensure ID is not null", this.agentToTest.getId());

        Long originalId = this.agentToTest.getId();
        Long idToSet = new Long(3);
        this.agentToTest.setId(idToSet);
        assertEquals("testGetSetId - ", idToSet, this.agentToTest.getId());
        // Replace old id to allow tear down to function propertly
        this.agentToTest.setId(originalId);

        try {
            this.agentToTest.setId(null);
            fail("Should throw NPE for null ID");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getType()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setType(com.bluejungle.destiny.container.shared.agentmgr.IAgentType)}.
     */

    public void testGetSetType() {
        assertEquals("testGetSetType - Ensure initial agent type is as expected", this.agentManager.getAgentType(AgentTypeEnumType.DESKTOP.getName()), this.agentToTest.getType());
        IAgentType typeToSet = this.agentManager.getAgentType(AgentTypeEnumType.FILE_SERVER.getName());
        this.agentToTest.setType(typeToSet);
        assertEquals("testGetSetType - Ensure type set is retrieved", typeToSet, this.agentToTest.getType());

        try {
            this.agentToTest.setType(null);
            fail("Should throw NPE for null type");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getHost()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setHost(java.lang.String)}.
     */

    public void testGetSetHost() {
        assertEquals("testGetSetHost - Ensure host value as initially expected", TEST_HOST_NAME, this.agentToTest.getHost());

        String hostToSet = "HEHE!.nextlabs.com";
        this.agentToTest.setHost(hostToSet);
        assertEquals("testGetSetHost - Ensure host set as expected", hostToSet, this.agentToTest.getHost());

        try {
            this.agentToTest.setHost(null);
            fail("Should throw NPE for null Host");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getPushPort()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setPushPort(java.lang.Integer)}.
     */

    public void testGetSetPushPort() {
        try {
            this.agentToTest.getPushPort();
            fail("Should initially throw IllegalStateException when push port is not set");
        } catch (IllegalStateException exception) {

        }

        Integer portToSet = new Integer(92834837);
        this.agentToTest.setPushPort(portToSet);
        assertEquals("testGetSetPushPort - Ensure push port set as expected", portToSet, this.agentToTest.getPushPort());

        try {
            this.agentToTest.setPushPort(null);
            fail("Should throw NPE for null push port");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getLastHeartbeat()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setLastHeartbeat(java.util.Calendar)}.
     */

    public void testGetSetLastHeartbeat() {
        assertNull("testGetSetLastHeartbeat - Ensure last heartbeat is initially null", this.agentToTest.getLastHeartbeat());
        Calendar lastHeartBeat = Calendar.getInstance();
        this.agentToTest.setLastHeartbeat(lastHeartBeat);
        assertEquals("testGetSetLastHeartbeat - Ensure last heart beat set as expected", lastHeartBeat, this.agentToTest.getLastHeartbeat());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#isOnline()}.
     */

    public void testIsOnline() {
        assertFalse("testIsOnline - Ensure agent is initially offline", this.agentToTest.isOnline());

        // Now set last heartbeat
        Calendar heartbeat = Calendar.getInstance();
        this.agentToTest.setLastHeartbeat(heartbeat);
        assertTrue("testIsOnline - Ensure agent is now online", this.agentToTest.isOnline());

        heartbeat.add(Calendar.DATE, -2);
        assertFalse("testIsOnline - Ensure agent is now offline again", this.agentToTest.isOnline());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getAgentProfile()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setAgentProfile(com.bluejungle.destiny.container.shared.profilemgr.IAgentProfileDO)}.
     * 
     * @throws DataSourceException
     */

    public void testGetSetAgentProfile() throws DataSourceException {
        IAgentProfileDO defaultProfile = this.profileManager.getDefaultAgentProfile();
        assertEquals("testGetSetAgentProfile - Ensure initial agent profile is default", defaultProfile, this.agentToTest.getAgentProfile());

        IAgentProfileData agentProfileData = new IAgentProfileData() {

            public boolean isLogViewingEnabled() {
                // TODO Auto-generated method stub
                return false;
            }

            public boolean isTrayIconEnabled() {
                // TODO Auto-generated method stub
                return false;
            }

            public String getName() {
                return "foo";
            }

        };
        IAgentProfileDO newAgentProfile = this.profileManager.createAgentProfile(agentProfileData);

        try {
            this.agentToTest.setAgentProfile(newAgentProfile);
            assertEquals("testGetSetAgentProfile - Ensure agent profile set as expected.", newAgentProfile, this.agentToTest.getAgentProfile());

            try {
                this.agentToTest.setAgentProfile(null);
                fail("Should throw NPE for null agent profile");
            } catch (NullPointerException exception) {

            }
        } finally {
            this.profileManager.deleteAgentProfile(newAgentProfile.getId());
        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getCommProfile()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setCommProfile(com.bluejungle.destiny.container.shared.profilemgr.ICommProfileDO)}.
     * 
     * @throws DataSourceException
     */

    public void testGetSetCommProfile() throws DataSourceException {
        final IAgentType agentType = this.agentToTest.getType();
        ICommProfileDO defaultProfile = this.profileManager.getDefaultCommProfile(agentType);
        assertEquals("testGetSetAgentProfile - Ensure initial comm profile is default", defaultProfile, this.agentToTest.getCommProfile());

        ICommProfileData commProfileData = new ICommProfileData() {

            public IAgentType getAgentType() {
                return agentType;
            }

            public URI getDABSLocation() {
                URI dabsLocation = null;
                try {
                    dabsLocation = new URI("http://foo.com");
                } catch (URISyntaxException exception) {

                }
                return dabsLocation;
            }

            public int getDefaultPushPort() {
                return 0;
            }

            public TimeInterval getHeartBeatFrequency() {
                return new TimeInterval(0);
            }

            public TimeInterval getLogFrequency() {
                return new TimeInterval(0);
            }

            public int getLogLimit() {
                return 0;
            }

            public String getPassword() {
                return "unsecure";
            }

            public boolean isPushEnabled() {
                return false;
            }

            public String getName() {
                return "foo";
            }
        };
        ICommProfileDO newProfile = this.profileManager.createCommProfile(commProfileData);

        try {
            this.agentToTest.setCommProfile(newProfile);
            assertEquals("testGetSetAgentProfile - Ensure comm profile set as expected.", newProfile, this.agentToTest.getCommProfile());

            try {
                this.agentToTest.setCommProfile(null);
                fail("Should throw NPE for null comm profile");
            } catch (NullPointerException exception) {

            }
        } finally {
            this.profileManager.deleteCommProfile(newProfile.getId());
        }

    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getIsPushReady()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setIsPushReady(boolean)}.
     */

    public void testSetGetIsPushReady() {
        assertFalse("testSetGetIsPushReady - Ensure agent is intially not push ready", this.agentToTest.getIsPushReady());
        this.agentToTest.setIsPushReady(true);
        assertTrue("testSetGetIsPushReady - Ensure agent is push ready", this.agentToTest.getIsPushReady());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getPolicyAssemblyStatus()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setPolicyAssemblyStatus(com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentPolicyAssemblyStatus)}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#resetPolicyAssemblyStatus()}.
     */

    public void testGetSetResetPolicyAssemblyStatus() {
        assertNull("testGetSetResetPolicyAssemblyStatus - Ensure PolicyAssembly status initially not null.", this.agentToTest.getPolicyAssemblyStatus());

        AgentPolicyAssemblyStatus statusToSet = new AgentPolicyAssemblyStatus();
        this.agentToTest.setPolicyAssemblyStatus(statusToSet);
        assertEquals("testGetSetResetPolicyAssemblyStatus - ", statusToSet, this.agentToTest.getPolicyAssemblyStatus());

        this.agentToTest.resetPolicyAssemblyStatus();
        assertNotNull("testGetSetResetPolicyAssemblyStatus - Ensure PolicyAssembly status not null after reset.", this.agentToTest.getPolicyAssemblyStatus());
        assertNotSame("testGetSetResetPolicyAssemblyStatus - Ensure PolicyAssembly status not equal to previous status after reset.", statusToSet, this.agentToTest.getPolicyAssemblyStatus());

        try {
            this.agentToTest.setPolicyAssemblyStatus(null);
            fail("Should throw NPE for null PolicyAssembly status");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getProfileStatus()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setProfileStatus(com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentProfileStatus)}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#resetProfileStatus()}.
     */

    public void testGetSetResetProfileStatus() {
        assertNull("testGetSetResetProfileStatus - Ensure profile status initially not null.", this.agentToTest.getProfileStatus());

        AgentProfileStatus statusToSet = new AgentProfileStatus();
        this.agentToTest.setProfileStatus(statusToSet);
        assertEquals("testGetSetResetProfileStatus - ", statusToSet, this.agentToTest.getProfileStatus());

        this.agentToTest.resetProfileStatus();
        assertNotNull("testGetSetResetProfileStatus - Ensure profile status not null after reset.", this.agentToTest.getProfileStatus());
        assertNotSame("testGetSetResetProfileStatus - Ensure profile status not equal to previous status after reset.", statusToSet, this.agentToTest.getProfileStatus());

        try {
            this.agentToTest.setProfileStatus(null);
            fail("Should throw NPE for null profile status");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getRegistrations()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#addNewRegistration()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setRegistrations(java.util.Set)}.
     */
    public void testGetSetAddNewRegistrations() {
        assertNotNull("testGetSetAddNewRegistrations - Ensure registrations are not initially null", this.agentToTest.getRegistrations());
        Set mySet = new HashSet();
        this.agentToTest.setRegistrations(mySet);
        assertEquals("testGetSetAddNewRegistrations - Ensure registration set as expected", mySet, this.agentToTest.getRegistrations());

        this.agentToTest.addNewRegistration();
        assertTrue("testGetSetAddNewRegistrations - Ensure set of registrations is now larger", mySet.size() == 1);

        try {
            this.agentToTest.setRegistrations(null);
            fail("Should throw NPE for null registrations");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setRegistered(boolean)}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#isRegistered()}.
     */
    public void testIsSetRegistered() {
        assertTrue("testIsSetRegistered - Ensure agent is initially registered", this.agentToTest.isRegistered());
        this.agentToTest.setRegistered(false);
        assertFalse("testIsSetRegistered - Ensure agent now not registered", this.agentToTest.isRegistered());
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#getVersion()}.
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#setVersion(com.bluejungle.version.IVersion)}.
     */
    public void testGetSetVersion() {
        assertNotNull("testGetSetVersion - Ensure version is not null initially", this.agentToTest.getVersion());

        IVersion versionToSet = new IVersion() {

            /**
             * @see com.bluejungle.version.IVersion#getBuild()
             */
            public int getBuild() {
                // TODO Auto-generated method stub
                return 0;
            }

            /**
             * @see com.bluejungle.version.IVersion#getMaintenance()
             */
            public int getMaintenance() {
                // TODO Auto-generated method stub
                return 0;
            }

            /**
             * @see com.bluejungle.version.IVersion#getMajor()
             */
            public int getMajor() {
                // TODO Auto-generated method stub
                return 0;
            }

            /**
             * @see com.bluejungle.version.IVersion#getMinor()
             */
            public int getMinor() {
                // TODO Auto-generated method stub
                return 0;
            }

            /**
             * @see com.bluejungle.version.IVersion#getPatch()
             */
            public int getPatch() {
                // TODO Auto-generated method stub
                return 0;
            }

            /**
             * @see com.bluejungle.version.IVersion#setBuild(int)
             */
            public void setBuild(int build) {
                // TODO Auto-generated method stub

            }

            /**
             * @see com.bluejungle.version.IVersion#setMaintenance(int)
             */
            public void setMaintenance(int maintenance) {
                // TODO Auto-generated method stub

            }

            /**
             * @see com.bluejungle.version.IVersion#setMajor(int)
             */
            public void setMajor(int major) {
                // TODO Auto-generated method stub

            }

            /**
             * @see com.bluejungle.version.IVersion#setMinor(int)
             */
            public void setMinor(int minor) {
                // TODO Auto-generated method stub

            }

            /**
             * @see com.bluejungle.version.IVersion#setPatch(int)
             */
            public void setPatch(int patch) {
                // TODO Auto-generated method stub

            }
            
            public int compareTo(IVersion o){
            	return 0;
            }

        };
        this.agentToTest.setVersion(versionToSet);
        assertEquals("testGetSetVersion - Ensure Version set as expected", versionToSet, this.agentToTest.getVersion());

        try {
            this.agentToTest.setVersion(null);
            fail("Should throw NPE for null version");
        } catch (NullPointerException exception) {

        }
    }

    /**
     * Test method for
     * {@link com.bluejungle.destiny.container.shared.agentmgr.hibernateimpl.AgentDO#equals(java.lang.Object)}.
     * 
     * @throws PersistenceException
     */
    public void testEqualsObject() throws PersistenceException {
        assertTrue("testEqualsObject - Ensure Identity", this.agentToTest.equals(this.agentToTest));
        AgentDO anotherInstance = (AgentDO) this.agentManager.getAgentById(this.agentToTest.getId());
        assertTrue("testEqualsObject - Ensure Another Instance works as expected", this.agentToTest.equals(anotherInstance));

        assertFalse("testEqualsObject - Ensure another Agent DO works as expected", this.agentToTest.equals(new AgentDO()));
        assertFalse("testEqualsObject - Ensure null works as expected", this.agentToTest.equals(null));
        assertFalse("testEqualsObject - Ensure differnt class type works as expected", this.agentToTest.equals(new Integer(0)));
    }

    /**
     * @see com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase#getComponentName()
     */
    @Override
    protected String getComponentName() {
        // TODO Auto-generated method stub
        return super.getComponentName();
    }

    /**
     * @see com.bluejungle.destiny.container.shared.test.BaseContainerSharedTestCase#getDataRepositories()
     */
    @Override
    protected Set getDataRepositories() {
        // TODO Auto-generated method stub
        return super.getDataRepositories();
    }

}
