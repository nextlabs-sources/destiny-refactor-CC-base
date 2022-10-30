/*
 * Created on Dec 16, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.profile.tests;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.apache.axis.types.Token;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedShort;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;

import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.agent.profile.ProfileManager;
import com.bluejungle.destiny.framework.types.TimeIntervalDTO;
import com.bluejungle.destiny.framework.types.TimeUnits;
import com.bluejungle.destiny.services.management.types.ActivityJournalingSettingsDTO;
import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.ApplicationList;
import com.bluejungle.destiny.services.management.types.ApplicationProcess;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.domain.types.ActionTypeDTO;
import com.bluejungle.domain.types.ActionTypeDTOList;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;

/**
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class ProfileWriteTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Constructor for ProfileTest.
     * 
     * @param name
     */
    public ProfileWriteTest(String name) {
        super(name);
    }

    public void testProfile() throws MalformedURLException, IOException, MappingException, FileNotFoundException, MarshalException, ValidationException {
        System.out.println("Start Profile Write test...");

        ComponentInfo info = new ComponentInfo(ProfileManager.class.getName(), ProfileManager.class.getName(), IProfileManager.class.getName(), LifestyleType.SINGLETON_TYPE);
        IProfileManager profileManager = (IProfileManager) ComponentManagerFactory.getComponentManager().getComponent(info);

        AgentProfileDTO agentProfile = new AgentProfileDTO();
        agentProfile.setCreatedDate(new GregorianCalendar(2004, 3, 25, 12, 40));
        agentProfile.setModifiedDate(new GregorianCalendar(2004, 3, 25, 12, 41));
        agentProfile.setHookAllProc(true);
        agentProfile.setName(new Token("foo"));
        ApplicationList appList = new ApplicationList();
        ApplicationProcess[] appProcessArray = new ApplicationProcess[5];
        appProcessArray[0] = new ApplicationProcess();
        appProcessArray[0].setName("winword");
        appProcessArray[1] = new ApplicationProcess();
        appProcessArray[1].setName("notepad");
        appProcessArray[2] = new ApplicationProcess();
        appProcessArray[2].setName("excel");
        appProcessArray[3] = new ApplicationProcess();
        appProcessArray[3].setName("outlook");
        appProcessArray[4] = new ApplicationProcess();
        appProcessArray[4].setName("iexplore");
        appList.setApplication(appProcessArray);
        agentProfile.setHookedApplications(appList);

        profileManager.setAgentProfile(agentProfile);

        CommProfileDTO communicationProfile = new CommProfileDTO();
        communicationProfile.setCreatedDate(new GregorianCalendar(2004, 3, 25, 12, 40));
        communicationProfile.setModifiedDate(new GregorianCalendar(2004, 3, 25, 12, 41));
        communicationProfile.setName(new Token("comm"));
        communicationProfile.setDABSLocation(new URI("http://localhost:8081/dabs"));
        communicationProfile.setPasswordHash(new byte[] { 0, 1, 2, 3, 4 });

        TimeIntervalDTO heartBeatFrequency = new TimeIntervalDTO(new UnsignedShort("10000"), TimeUnits.seconds);
        communicationProfile.setHeartBeatFrequency(heartBeatFrequency);
        TimeIntervalDTO logFrequency = new TimeIntervalDTO(new UnsignedShort("2000"), TimeUnits.seconds);

        //logFrequency.
        communicationProfile.setLogFrequency(logFrequency);
        communicationProfile.setLogLimit(new UnsignedShort("3000"));
        communicationProfile.setPushEnabled(true);
        communicationProfile.setDefaultPushPort(new UnsignedShort(9080));

        ActivityJournalingSettingsDTO settings = new ActivityJournalingSettingsDTO();
        ActionTypeDTOList actionList = new ActionTypeDTOList();
        actionList.setAction(new ActionTypeDTO[] { ActionTypeDTO.EMBED, ActionTypeDTO.EMAIL });

        settings.setLoggedActivities(actionList);
        communicationProfile.setCurrentActivityJournalingSettings(settings);
        profileManager.setCommunicationProfile(communicationProfile);

        //Castor collection testing...
        FileWriter writer = null;
        String filename = "castortest.xml";

        Mapping mapping = new Mapping();
        mapping.loadMapping("config/mapping.xml");

        writer = new FileWriter(filename);
        Marshaller marshaller = new Marshaller(writer);
        marshaller.setMapping(mapping);
        ArrayList array = new ArrayList();
        array.add(communicationProfile);
        array.add(agentProfile);
        marshaller.marshal(array);

        System.out.println("End Write test.");
    }
}
