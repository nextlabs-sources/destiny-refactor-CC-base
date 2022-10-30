/*
 * Created on Dec 16, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.profile.tests;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedShort;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;

import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.agent.profile.ProfileManager;
import com.bluejungle.destiny.framework.types.TimeUnits;
import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.domain.types.ActionTypeDTO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class ProfileReadTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Constructor for ProfileReadTest.
     * 
     * @param name
     */
    public ProfileReadTest(String name) {
        super(name);
    }

    public void testProfileRead() throws MalformedURLException, IOException, MappingException, FileNotFoundException, MarshalException, ValidationException {
        System.out.println("Start Profile Read test...");
        ComponentInfo<ProfileManager> info = new ComponentInfo<ProfileManager>(
        		ProfileManager.class.getName(), 
        		ProfileManager.class, 
        		IProfileManager.class, 
        		LifestyleType.SINGLETON_TYPE);
        IProfileManager profileManager = (IProfileManager) ComponentManagerFactory.getComponentManager().getComponent(info);
        profileManager.init();
        AgentProfileDTO agentProfile = profileManager.getAgentProfile();
        assertEquals("Invalid Created Date", new GregorianCalendar(2004, 3, 25, 12, 40).getTime(), agentProfile.getCreatedDate().getTime());
        assertEquals("Invalid Modified Date", new GregorianCalendar(2004, 3, 25, 12, 41).getTime(), agentProfile.getModifiedDate().getTime());
        assertTrue("Invalid Hook all Proc property", agentProfile.isHookAllProc());
        assertEquals("Invalid Name", agentProfile.getName(), "foo");
        assertEquals("Invalid App count", agentProfile.getHookedApplications().getApplication().length, 5);
        assertEquals("Invalid App1", agentProfile.getHookedApplications().getApplication(0).getName(), "winword");
        assertEquals("Invalid App2", agentProfile.getHookedApplications().getApplication(1).getName(), "notepad");
        assertEquals("Invalid App3", agentProfile.getHookedApplications().getApplication(2).getName(), "excel");
        assertEquals("Invalid App4", agentProfile.getHookedApplications().getApplication(3).getName(), "outlook");
        assertEquals("Invalid App5", agentProfile.getHookedApplications().getApplication(4).getName(), "iexplore");
        CommProfileDTO communicationProfile = profileManager.getCommunicationProfile();
        assertEquals("Invalid Created Date", communicationProfile.getCreatedDate().getTime(), new GregorianCalendar(2004, 3, 25, 12, 40).getTime());
        assertEquals("Invalid Modified Date", communicationProfile.getModifiedDate().getTime(), new GregorianCalendar(2004, 3, 25, 12, 41).getTime());
        assertEquals("Invalid DABS Location", communicationProfile.getDABSLocation(), new URI("http://localhost:8081/dabs"));
        assertEquals("Invalid password hash", communicationProfile.getPasswordHash()[0], 0);
        assertEquals("Invalid password hash", communicationProfile.getPasswordHash()[1], 1);
        assertEquals("Invalid heartbeat rate", new UnsignedShort("10000"), communicationProfile.getHeartBeatFrequency().getTime());
        assertEquals("Invalid heartbeat unit", TimeUnits.seconds, communicationProfile.getHeartBeatFrequency().getTimeUnit());
        assertEquals("Invalid log rate", new UnsignedShort("2000"), communicationProfile.getLogFrequency().getTime());
        assertEquals("Invalid log limit", new UnsignedShort("3000"), communicationProfile.getLogLimit());
        assertEquals("Invalid default port", new UnsignedShort(9080), communicationProfile.getDefaultPushPort());
        assertEquals("Invalid push enabled", true, communicationProfile.isPushEnabled());
        assertEquals("Invalid action", ActionTypeDTO.EMBED, communicationProfile.getCurrentActivityJournalingSettings().getLoggedActivities().getAction(0));
        assertEquals("Invalid action", ActionTypeDTO.EMAIL, communicationProfile.getCurrentActivityJournalingSettings().getLoggedActivities().getAction(1));

        //Castor test
        Unmarshaller unmarshaller = new Unmarshaller(ArrayList.class);
        //Load Mapping
        Mapping mapping = new Mapping();
        mapping.loadMapping("config/mapping.xml");
        unmarshaller.setMapping(mapping);

        FileReader reader = new FileReader("castortest.xml");
        ArrayList array = null;
        if (reader != null) {
            array = (ArrayList) unmarshaller.unmarshal(reader);
        }

        assertEquals("Invalid ArrayList size", 2, array.size());
        assertEquals("Invalid communication profile", communicationProfile, array.get(0));
        assertEquals("Invalid agent profile", agentProfile, array.get(1));

        System.out.println("End Read test.");
    }

}
