/*
 * Created on Dec 16, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.agent.profile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;

import com.bluejungle.destiny.services.management.types.AgentProfileDTO;
import com.bluejungle.destiny.services.management.types.CommProfileDTO;
import com.bluejungle.framework.comp.ComponentImplBase;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * The Profile Manager’s main functions are: 1. Upon agent startup, load the
 * profile files which are kept on disk as xml files in memory 2. provide an API
 * for other components to access the in-memory-kept profile data 3. Upon
 * receiving them from the Control Manager, save the updated profiles as xml
 * files on disk
 * 
 * @author fuad
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */
public class ProfileManager extends ComponentImplBase implements IProfileManager {

    public static final PropertyKey<String> BASE_DIR_PROPERTY_NAME = new PropertyKey<String>("baseDirProperty");
    private static final String DEFAULT_BASE_DIR = ".";

    public static final String PROFILE_DIR = "config";
    private static final String COMMUNICATION_PROFILE_FILE = "commprofile.xml";
    private static final String AGENT_PROFILE_FILE = "agentprofile.xml";
    private static final String CASTOR_MAPPING = "mapping.xml";

    private CommProfileDTO communicationProfile = null;
    private AgentProfileDTO agentProfile = null;
    private String baseDir;

    /**
     * Returns the agentProfile.
     * 
     * @return the agentProfile.
     */
    public synchronized AgentProfileDTO getAgentProfile() {
        return this.agentProfile;
    }

    /**
     * Sets the agentProfile and saves it to disk
     * 
     * @param agentProfile
     *            The agentProfile to set.
     */
    public synchronized void setAgentProfile(AgentProfileDTO agentProfile) {
        this.agentProfile = agentProfile;
        // Create a File to marshal to
        FileWriter writer = null;
        String filename = this.baseDir + File.separator + PROFILE_DIR + File.separator + AGENT_PROFILE_FILE;

        // Load Castor Mapping
        Mapping mapping = new Mapping();

        try {
            mapping.loadMapping(new InputSource(this.baseDir + File.separator + PROFILE_DIR + File.separator + CASTOR_MAPPING));
            writer = new FileWriter(filename);
            Marshaller marshaller = new Marshaller(writer);
            marshaller.setMapping(mapping);
            marshaller.marshal(agentProfile);
        } catch (IOException e) {
            getLog().error("Could not create file: " + filename, e);
        } catch (MarshalException e1) {
            getLog().error("Unable to save Agent profile", e1);
        } catch (ValidationException e1) {
            getLog().error("Unable to save Agent profile", e1);
        } catch (MappingException e) {
            getLog().error("Unable to save Agent profile", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e2) {
                    getLog().error("Unable to close Profile Writer", e2);
                }
            }
        }

    }

    /**
     * Returns the communicationProfile.
     * 
     * @return the communicationProfile.
     */
    public synchronized CommProfileDTO getCommunicationProfile() {
        return this.communicationProfile;
    }

    /**
     * Sets the communicationProfile and saves it to disk
     * 
     * @param communicationProfile
     *            The communicationProfile to set.
     */
    public synchronized void setCommunicationProfile(CommProfileDTO communicationProfile) {
        this.communicationProfile = communicationProfile;

        // Create a File to marshal to
        FileWriter writer = null;
        String filename = this.baseDir + File.separator + PROFILE_DIR + File.separator + COMMUNICATION_PROFILE_FILE;

        // Load Castor Mapping
        Mapping mapping = new Mapping();

        try {
            mapping.loadMapping(new InputSource(this.baseDir + File.separator + PROFILE_DIR + File.separator + CASTOR_MAPPING));
            writer = new FileWriter(filename);
            Marshaller marshaller = new Marshaller(writer);
            marshaller.setMapping(mapping);
            marshaller.marshal(communicationProfile);
        } catch (IOException e) {
            getLog().error("Could not create file: " + filename, e);
        } catch (MarshalException e1) {
            getLog().error("Unable to save Agent profile", e1);
        } catch (ValidationException e1) {
            getLog().error("Unable to save Agent profile", e1);
        } catch (MappingException e) {
            getLog().error("Unable to save Agent profile", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e2) {
                    getLog().error("Unable to Close Profile Writer", e2);
                }
            }
        }

    }

    /**
     * Read profiles into memory
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        IConfiguration configuration = getConfiguration();
        if (configuration != null) {
            this.baseDir = configuration.get(BASE_DIR_PROPERTY_NAME, DEFAULT_BASE_DIR);
        } else {
            this.baseDir = DEFAULT_BASE_DIR;
        }

        // Create a new Unmarshaller
        Unmarshaller communicationProfileUnmarshaller = new Unmarshaller(CommProfileDTO.class);
        Unmarshaller agentProfileUnmarshaller = new Unmarshaller(AgentProfileDTO.class);
        // Load Mapping
        Mapping mapping = new Mapping();
        try {
            mapping.loadMapping(new InputSource(this.baseDir + File.separator + PROFILE_DIR + File.separator + CASTOR_MAPPING));
            communicationProfileUnmarshaller.setMapping(mapping);
            agentProfileUnmarshaller.setMapping(mapping);
        } catch (IOException e3) {
            getLog().error("Profile Init failed. Castor mapping file could not be loaded.", e3);
        } catch (MappingException e3) {
            getLog().error("Profile Init failed. Castor mapping file could not be loaded.", e3);
        }

        FileReader reader = null;

        try {
            reader = new FileReader(this.baseDir + File.separator + PROFILE_DIR + File.separator + AGENT_PROFILE_FILE);
            this.agentProfile = (AgentProfileDTO) agentProfileUnmarshaller.unmarshal(reader);
            reader = new FileReader(this.baseDir + File.separator + PROFILE_DIR + File.separator + COMMUNICATION_PROFILE_FILE);
            this.communicationProfile = (CommProfileDTO) communicationProfileUnmarshaller.unmarshal(reader);
        } catch (FileNotFoundException e) {
            getLog().error("Profile Init failed. File not found.", e);
        } catch (MarshalException e1) {
            getLog().error("Profile Init failed. Invalid file.", e1);
        } catch (ValidationException e1) {
            getLog().error("Profile Init failed. Invalid file.", e1);
        }
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
     * @see com.bluejungle.framework.comp.IStartable#stop()
     */
    public void stop() {
    }
}
