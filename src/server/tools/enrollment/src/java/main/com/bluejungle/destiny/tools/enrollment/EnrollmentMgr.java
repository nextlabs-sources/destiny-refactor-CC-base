/*
 * Created on Mar 1, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment;

import static com.bluejungle.destiny.tools.enrollment.EnrollmentMgrOptionDescriptorEnum.DEFINITION_FILE_OPTIONS_ID;
import static com.bluejungle.destiny.tools.enrollment.EnrollmentMgrOptionDescriptorEnum.DOMAIN_NAME_OPTIONS_ID;
import static com.bluejungle.destiny.tools.enrollment.EnrollmentMgrOptionDescriptorEnum.FILTER_FILE_OPTIONS_ID;
import static com.bluejungle.destiny.tools.enrollment.EnrollmentMgrOptionDescriptorEnum.DOMAINGROUP_FILE_OPTIONS_ID;
import static com.bluejungle.destiny.tools.enrollment.EnrollmentMgrOptionDescriptorEnum.ROOT_FILE_OPTIONS_ID;
import static com.bluejungle.destiny.tools.enrollment.EnrollmentMgrOptionDescriptorEnum.TYPE_OPTIONS_ID;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.axis.client.Stub;
import org.apache.axis.types.URI;

import com.bluejungle.destiny.appframework.appsecurity.axis.AuthenticationContext;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EntryNotFoundException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ActiveDirectoryEnrollmentProperties;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentStatus;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentType;
import com.bluejungle.destiny.services.enrollment.types.Profile;
import com.bluejungle.destiny.services.enrollment.types.Realm;
import com.bluejungle.destiny.services.enrollment.types.RealmList;
import com.bluejungle.destiny.tools.enrollment.filereader.ADConnFileReader;
import com.bluejungle.destiny.tools.enrollment.filereader.EnrollableSubdomain;
import com.bluejungle.destiny.tools.enrollment.filereader.EnrollmentDefinitionReader;
import com.bluejungle.destiny.tools.enrollment.filereader.DomainGroupConfigFileReader;
import com.bluejungle.destiny.tools.enrollment.filereader.PortalConnFileReader;
import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.bluejungle.destiny.tools.enrollment.filter.FilterFileReader;
import com.bluejungle.destiny.tools.enrollment.filter.SelectiveFilterGenerator;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.impl.OptionHelper;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/EnrollmentMgr.java#1 $
 */

public class EnrollmentMgr extends EnrollmentMgrShared {
    protected static final String DIRECTORY = 	"DIR";
    protected static final String PORTAL = 		"PORTAL";
    protected static final String LDIF = 			"LDIF";
    protected static final String DOMAINGROUP = 		"DOMAINGROUP";
    protected static final String UNKNOWN = 		"UNKNOWN";
    private static boolean verboseMode = false;
        
    protected static final Map<String, EnrollmentType> CLI_ENROLLMENT_TYPE;
    static {
        CLI_ENROLLMENT_TYPE = new HashMap<String, EnrollmentType>();
        CLI_ENROLLMENT_TYPE.put(DIRECTORY,         	EnrollmentType.DIRECTORY);
        CLI_ENROLLMENT_TYPE.put(PORTAL,         	EnrollmentType.PORTAL);
        CLI_ENROLLMENT_TYPE.put(LDIF,             	EnrollmentType.LDIF);
        CLI_ENROLLMENT_TYPE.put(DOMAINGROUP, 		EnrollmentType.DOMAINGROUP);
        CLI_ENROLLMENT_TYPE.put(UNKNOWN,         	EnrollmentType.UNKNOWN);
    }
    
    protected static final Map<String, EnrollmentType> ENROLLABLE_ENROLLMENT_TYPE;
    static {
        ENROLLABLE_ENROLLMENT_TYPE = new HashMap<String, EnrollmentType>();
        ENROLLABLE_ENROLLMENT_TYPE.put(DIRECTORY,     EnrollmentType.DIRECTORY);
        ENROLLABLE_ENROLLMENT_TYPE.put(DOMAINGROUP,   EnrollmentType.DOMAINGROUP);
        ENROLLABLE_ENROLLMENT_TYPE.put(PORTAL,        EnrollmentType.PORTAL);
        ENROLLABLE_ENROLLMENT_TYPE.put(LDIF,          EnrollmentType.LDIF);
    }
    
    /*
     * Enrollment List Attribute
     */
    private static final Map<String, String> ENROLLMENT_PROPERTY_TO_DISPLAY;
    static {
        ENROLLMENT_PROPERTY_TO_DISPLAY = new HashMap<String, String>();
        ENROLLMENT_PROPERTY_TO_DISPLAY.put("server", 			 "server    ");
        ENROLLMENT_PROPERTY_TO_DISPLAY.put("port",   			 "port      ");
        ENROLLMENT_PROPERTY_TO_DISPLAY.put("login",  			 "login     ");
        ENROLLMENT_PROPERTY_TO_DISPLAY.put("roots",  			 "roots     ");
        ENROLLMENT_PROPERTY_TO_DISPLAY.put("filter", 			 "filter    ");
        ENROLLMENT_PROPERTY_TO_DISPLAY.put("ldif.filename",		 "ldif.filename ");    
        ENROLLMENT_PROPERTY_TO_DISPLAY.put("scheduledsyncinterv","SyncIntrvl");    
    }
    
    private final IConsoleApplicationDescriptor descriptor;

    protected EnrollmentMgr() throws InvalidOptionDescriptorException{
        this(new EnrollmentMgrOptionDescriptorEnum());
    }
    
    protected EnrollmentMgr(IConsoleApplicationDescriptor descriptor) throws InvalidOptionDescriptorException{
        super();
        this.descriptor = descriptor;
    }
    
    public static void main(String[] args){
        try{
            EnrollmentMgr mgr = new EnrollmentMgr();
            mgr.parseAndExecute(args);
        }catch(Exception e){
            System.err.println(e.getMessage());
        }    
    }
    
    @Override
    protected void handleException(Exception e) {
        if (verboseMode) {
            e.printStackTrace();
        } else {
            super.handleException(e);
        }
    }
    
    protected void printExceptionEnrollManager(String message, Throwable t){
        super.printExceptionEnrollManager(message, t);
        if (verboseMode) {
            System.err.println("");
            t.printStackTrace();
        }
    }
    
    protected void exec(ICommandLine commandLine)
            throws EnrollmentMgrException, FileFormatException, URI.MalformedURIException,
            IOException, Exception {
        System.out.println(WELCOME);
        
        String type         	= getValue(commandLine, TYPE_OPTIONS_ID);
        String domainName   	= getValue(commandLine, DOMAIN_NAME_OPTIONS_ID);
        File connFile       	= getValue(commandLine, ROOT_FILE_OPTIONS_ID);
        File definitionFile 	= getValue(commandLine, DEFINITION_FILE_OPTIONS_ID);
        File domainGroupConfigFile= getValue(commandLine, DOMAINGROUP_FILE_OPTIONS_ID);
        File filterFile 		= getValue(commandLine, FILTER_FILE_OPTIONS_ID);
        
        if (type != null && getEnrollmentType(type) == EnrollmentType.DOMAINGROUP) {
        	if ((connFile != null) || (definitionFile != null) || (filterFile != null)) {   			
        		System.err.println("DomainGroup enrollment can not specify -f, -d, or -a options.");
        		System.exit(1);
        	}
        }
        
        if (commandLine.isOptionExist( EnrollmentMgrOptionDescriptorEnum.VERBOSE_OPTIONS_ID)) {
            verboseMode = true;
            System.out.println ("Verbose mode ON");
        }
        
        authenticate(commandLine);

        final OptionId<?> selectedAction = OptionHelper.findSelectedOption(
                EnrollmentMgrOptionDescriptorEnum.ACTIONS, commandLine);
        
        // Execute the command:
		if (commandLine.isOptionExist(EnrollmentMgrOptionDescriptorEnum.ENROLL_OPTIONS_ID)) {
			enroll(getEnrollmentType(type), domainName, connFile, definitionFile, 
					filterFile, domainGroupConfigFile);
		} else if (commandLine.isOptionExist(EnrollmentMgrOptionDescriptorEnum.UPDATE_OPTIONS_ID)) {
			update(getEnrollmentType(type), domainName, connFile, definitionFile, 
					filterFile, domainGroupConfigFile);
		} else if (commandLine.isOptionExist(EnrollmentMgrOptionDescriptorEnum.DELETE_OPTIONS_ID)) {
			delete(domainName);
		} else if (commandLine.isOptionExist(EnrollmentMgrOptionDescriptorEnum.SYNC_OPTIONS_ID)) {
			sync(domainName);
		} else if (commandLine.isOptionExist(EnrollmentMgrOptionDescriptorEnum.LIST_OPTIONS_ID)) {
			list();
		} else {
			throw new ParseException("unknown action");
		}

		if (commandLine.isOptionExist(EnrollmentMgrOptionDescriptorEnum.SYNC_OPTIONS_ID)) {
			System.out.println("\nEnrollment sync action result: " + getRealmStatus(domainName));
		} else {
			System.out.println("\nEnrollment " + selectedAction
					+ " action done!");
		}
        
        AuthenticationContext.clearCurrentContext();

        if (commandLine.isOptionExist( EnrollmentMgrOptionDescriptorEnum.ENROLL_OPTIONS_ID)) {
            System.out.println("Please proceed with sync action.");
        }
    }
    
    private EnrollmentType getEnrollmentType(String type) throws ParseException{
        EnrollmentType enrollmentType = CLI_ENROLLMENT_TYPE.get(type);
        if (enrollmentType == null) {
            throw new ParseException("invalid enrollment type " + type);
        }
        return enrollmentType;
    }
    
  
    @Override
    protected IConsoleApplicationDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Prepares the enrollment for the provided source.
     * 
     * @param enrollmentType
     * @param name
     * @param connFile
     * @param defFile
     * @param filterFile
     * @param domainGroupConfigFile
     * @throws IOException if error on reading the filter or definition file
     * @throws FileFormatException if the filter or definition is not correct format.
     * @throws EnrollmentMgrException 
     */
    protected void enroll(EnrollmentType enrollmentType, String name, File connFile, File defFile,
            File filterFile, File domainGroupConfigFile) throws EnrollmentMgrException, IOException, FileFormatException {
        Realm newRealm = new Realm();
        newRealm.setId(java.math.BigInteger.ZERO);

        newRealm.setName(name);

        newRealm.setType(enrollmentType);

        // Prepare the profile properties:
        List<EnrollmentProperty> properties = new ArrayList<EnrollmentProperty>();

        // Figure out the root DN:
        if (enrollmentType == EnrollmentType.DIRECTORY) {         
            // Parse the root file:
            ADConnFileReader ad_conndefReader = new ADConnFileReader(connFile);
            properties.addAll(ad_conndefReader.getProperties());
            parseFilterFile(filterFile, properties);           
        }
        
        Vector<EnrollableSubdomain> subdomains = null;
        if (enrollmentType == EnrollmentType.DOMAINGROUP) {
            subdomains = parseDomainGroupConfigFile(domainGroupConfigFile, properties);       	
        }

        // Figure out the root DN:
        if (enrollmentType == EnrollmentType.PORTAL) {
            // Parse the root file:
            PortalConnFileReader portalConnFileReader = new PortalConnFileReader(connFile);
            properties.addAll(portalConnFileReader.getProperties());
        }

        if (enrollmentType != EnrollmentType.DOMAINGROUP) {
        	parseDefinitionFile(defFile, properties);
        }

        normalizeProperties(properties);

        // Create a profile with all the gathered properties:
        Profile profile = new Profile();
        EnrollmentProperty[] propArr = properties.toArray(new EnrollmentProperty[] {});
        profile.setProperties(propArr);
        newRealm.setProfile(profile);
        // Now create the enrollment on the server:
        enrollmentWS.createRealm(newRealm);
        
        // After the multi-domain is enrolled, enroll any sub-domains that are not already enrolled.
        if (enrollmentType == EnrollmentType.DOMAINGROUP) {
	        handleSubRealms("enroll", name, subdomains);
        }
     }

	/**
	 * Helper method to either enroll or update a subdomain of a Multi-domain realm.
	 * @param name
	 * @param subdomains
	 * @throws FileFormatException
	 * @throws EnrollmentMgrException
	 * @throws IOException
	 */
	private void handleSubRealms(String operation, String name,
			Vector<EnrollableSubdomain> subdomains) 
	throws FileFormatException, IOException 
	{
		for (EnrollableSubdomain es : subdomains) {
			String subdomainName = es.getName();
			EnrollmentType subdomainType = null;
			File subConnFile = es.getConnectionFile();	
			File subDefFile  = es.getDefinitionFile();
			File subFiltFile = es.getFilterFile();
			try {
				subdomainType = getEnrollmentType(es.getDomainType());
			} 
			catch (ParseException e1) {
				throw new FileFormatException("DomainGroup sub-domain:" + es.getName() + " has a type that is not valid.", e1);
			}
			
			if (doesUniqueRealmExist(subdomainName)) {
				System.out.println("DomainGroup " + operation + " found subdomain: " + subdomainName + " already exists.");
				System.out.println("DomainGroup updating subdomain: " + es.getName() + "\nType: " + subdomainType);
				if (es.getConnectionFile()!= null)
					System.out.println("ConnectionFile: " + es.getConnectionFile());
				if (es.getDefinitionFile()!= null)
					System.out.println("DefinitionFile: " + es.getDefinitionFile());
				if (es.getFilterFile()!= null)
					System.out.println("FilterFile: " + es.getFilterFile());
				try {
					update(subdomainType, es.getName(), subConnFile, subDefFile, subFiltFile, null);
				} catch (EnrollmentMgrException e) {
					System.out.println("DomainGroup update of subdomain: " + es.getName() + " got Exception: " + e.getMessage());
				}
			} else {
				System.out.println("DomainGroup " + operation + " found subdomain: " + subdomainName + " did not exist and needs to be enrolled.");
				System.out.println("DomainGroup enrolling subdomain: " + es.getName() + "\nType: " + subdomainType);
				if (es.getConnectionFile()!= null)
					System.out.println("ConnectionFile: " + es.getConnectionFile());
				if (es.getDefinitionFile()!= null)
					System.out.println("DefinitionFile: " + es.getDefinitionFile());
				if (es.getFilterFile() != null)
					System.out.println("FilterFile: " + es.getFilterFile());
				try {
					enroll(subdomainType, es.getName(), subConnFile, subDefFile, subFiltFile, null);
				} catch (EnrollmentMgrException e) {
					System.out.println("DomainGroup enroll of subdomain: " + es.getName() + " got Exception: " + e.getMessage());
				}
			}
			System.out.println();
		}
	}

	/**
	 * @param domainGroupConfigFile
	 * @param properties
	 */
	private Vector<EnrollableSubdomain> parseDomainGroupConfigFile(File domainGroupConfigFile,
			List<EnrollmentProperty> properties) 
					throws IOException, FileFormatException 
	{
		if (domainGroupConfigFile == null) 
			return new Vector<EnrollableSubdomain>();
		
		DomainGroupConfigFileReader dg_configReader = new DomainGroupConfigFileReader(domainGroupConfigFile);
		properties.addAll(dg_configReader.getProperties());
		Vector<EnrollableSubdomain> subdomains = dg_configReader.getSubdomains();
		return subdomains;
		
	}

	protected void parseFilterFile(File filterFile, List<EnrollmentProperty> properties) 
					throws IOException, FileFormatException 
	{		
		if (filterFile == null) 
			return;
		
		// Parse the filter file:
		FilterFileReader filterFileReader = new FilterFileReader(filterFile);
		SelectiveFilterGenerator filterGenerator = new SelectiveFilterGenerator(
				filterFileReader.getSelectedUsers(),
				filterFileReader.getSelectedHosts(),
				filterFileReader.getSelectedGroups());
		String filter = filterGenerator.getSelectiveFilter();
		if (filter != null && filter.length() > 4000) {
			throw new FileFormatException("Filter property exceeds maximum allowed size of 4000 characters.");
		}
		
		properties.add(new EnrollmentProperty(
				ActiveDirectoryEnrollmentProperties.FILTER,
				new String[] { filter }));
	}
    
    protected void parseDefinitionFile(File definitionFile,
            List<EnrollmentProperty> properties) throws IOException, FileFormatException {
		if (definitionFile == null) 
			return;
		
        // Parse the enrollment definition:
        EnrollmentDefinitionReader defReader = new EnrollmentDefinitionReader(definitionFile);

        properties.addAll(defReader.getProperties());
    }
    
    protected Realm getTheOnlyRealm(String name) 
    				throws EnrollmentMgrException
    {
        RealmList existingRealms = this.enrollmentWS.getRealms(name);
        if (existingRealms == null || existingRealms.getRealms() == null
                || existingRealms.getRealms().length == 0) {
            // No realm exists with the given name
            throw new EnrollmentMgrException(new EntryNotFoundException("enrollment", name).getMessage());
        } else if (existingRealms.getRealms().length != 1) {
            // We should only retrieve one realm:
            throw new EnrollmentMgrException("More than one realm exist with same name, '" + name + "'. Please contact Administrator.");
        }

        return existingRealms.getRealms()[0];
    }

	protected boolean doesUniqueRealmExist(String name) 
	{
		RealmList existingRealms;
		try {
			existingRealms = this.enrollmentWS.getRealms(name);
		} catch (EnrollmentMgrException e) {
			return false;
		}
		
		if (existingRealms == null || existingRealms.getRealms() == null
				|| existingRealms.getRealms().length == 0) {
			// No realm exists with the given name
			return false;
		} else if (existingRealms.getRealms().length != 1) {
			// We should only retrieve one realm:
			return false;
		}

		return true;
	}

    /**
     * Update enrollment
     * @param enrollmentType
     * @param name
     * @param connFile
     * @param defFile
     * @param filterFile
     * @param domainGroupConfigFile
     * @throws FileFormatException 
     * @throws IOException 
     * @throws EnrollmentMgrException 
     */
    private void update(EnrollmentType enrollmentType, String name, File connFile, File defFile,
            File filterFile, File domainGroupConfigFile) throws IOException, FileFormatException, EnrollmentMgrException {
        Realm existingRealm = getTheOnlyRealm(name);

        boolean updateMade = false;
        
        // Prepare the profile properties:
        Profile existingProfile = existingRealm.getProfile();

        Map<String, String[]> propertyMap = new HashMap<String, String[]>();
        if (existingProfile != null) {
            EnrollmentProperty[] existingProperties = existingProfile.getProperties();
            if (existingProperties != null) {
                for (EnrollmentProperty property : existingProperties) {
                	String theKey = property.getKey();
                	if ((enrollmentType == EnrollmentType.DOMAINGROUP) &&
                		 (theKey.startsWith("subdomain.")))
                			continue;
                	
                    propertyMap.put(property.getKey(), property.getValue());
                }
            }
        }

        if (connFile != null && connFile.length() > 0) {
            List<EnrollmentProperty> properties = null;
            if (enrollmentType == EnrollmentType.DIRECTORY) {
                ADConnFileReader ad_conndefReader = new ADConnFileReader(connFile);
                properties = ad_conndefReader.getProperties();
            } else if (enrollmentType == EnrollmentType.PORTAL) {
                PortalConnFileReader portal_conndefReader = new PortalConnFileReader(connFile);
                properties = portal_conndefReader.getProperties();
            }
            for (EnrollmentProperty prop : properties) {
                propertyMap.put(prop.getKey(), prop.getValue());
            }
            updateMade = true;
        }

        if (filterFile != null && filterFile.length() > 0) {
            // Parse the filter file:
            FilterFileReader filterFileReader = new FilterFileReader(filterFile);
            SelectiveFilterGenerator filterGenerator = new SelectiveFilterGenerator(
                    filterFileReader.getSelectedUsers(), filterFileReader.getSelectedHosts(),
                    filterFileReader.getSelectedGroups());
            String filter = filterGenerator.getSelectiveFilter();
            propertyMap.put(ActiveDirectoryEnrollmentProperties.FILTER, new String[] { filter });
            updateMade = true;
        }

        if (defFile != null && defFile.length() > 0) {
            EnrollmentDefinitionReader defReader = new EnrollmentDefinitionReader(
                    defFile);
            List<EnrollmentProperty> properties = defReader.getProperties();
            for (EnrollmentProperty prop :  properties) {
                propertyMap.put(prop.getKey(), prop.getValue());
            }
            updateMade = true;
        }

        Vector<EnrollableSubdomain> subdomains = null;
        if (domainGroupConfigFile != null && domainGroupConfigFile.length() > 0) {
        	DomainGroupConfigFileReader dg_configReader = new DomainGroupConfigFileReader(domainGroupConfigFile);
            List<EnrollmentProperty> properties = dg_configReader.getProperties();
    		subdomains = dg_configReader.getSubdomains();
            for (EnrollmentProperty prop :  properties) {
                propertyMap.put(prop.getKey(), prop.getValue());
            }      	
        	updateMade = true;
        }

        // Commit these updates:
        if (updateMade) {
            if (existingProfile == null) {
                existingProfile = new Profile();
            }
            existingProfile.setProperties(mapToPropertiesArray(propertyMap));
            existingRealm.setProfile(existingProfile);
            this.enrollmentWS.updateRealm(existingRealm);
            if (enrollmentType == EnrollmentType.DOMAINGROUP) {
            	handleSubRealms("update", name, subdomains);
            }
        } else {
            System.out.println("No update made");
        }

    }



    private static final int     SECOND                     = 1000;
    private static final int     SHORT_SLEEP_PERIOD         = 4 * SECOND;
    private static final int     MAX_SHORT_SLEEP_PERIOD     = 120 * SECOND;
    private static final float     SPEED_DOWN_FACTOR         = 1.386F; // (ln 4)
    private static final int     LONG_SLEEP_PERIOD         = 30 * SECOND;
    private static final int     SPECIAL_CHAR_COUNT         = 5;
    private static final char     SPECIAL_SYM             = ':';
    private static final char     NORMAL_SYM                 = '.';
    private static final int     MAX_UNKNOWN_REALM_RETRY = 10;
    
    /**
     * Sync enrollment
     * @param name
     * @throws RemoteException
     */
    protected void sync(String name) throws EnrollmentMgrException{
        Realm realm = getTheOnlyRealm(name);
        EnrollmentType realmType = null;
        if (realm != null) {
        	realmType = realm.getType();
        }
        
        EnrollmentServiceThread enrollmentServiceThread = new EnrollmentServiceThread(
                enrollmentWS, realm, Thread.currentThread());
        Thread syncThread = new Thread(enrollmentServiceThread);
        
        syncThread.start();
        
        try {
            //syncThread still running, it may be dead right after if there is an exception
            String status = UNKNOWN_REALM_STATUS;
            int timeCounter = 1;
            int retry =0;
            int sleeptime = SHORT_SLEEP_PERIOD;
            do {
                String newStatus = getRealmStatus(name);
                boolean enrolling = newStatus.equalsIgnoreCase("enrolling");
                if (newStatus.equalsIgnoreCase(status)) {
                    System.out.print((timeCounter % SPECIAL_CHAR_COUNT) == 0
                            ? SPECIAL_SYM
                            : NORMAL_SYM);
                    timeCounter++;
                } else {
                    //status changed.
                    status = newStatus;
                    if(enrolling){
                        System.out.print("\n" + status + " ");
                    }
                    
                    //reset the counters
                    timeCounter = 1;
                    retry = 0;
                    sleeptime = SHORT_SLEEP_PERIOD;
                }
                
                //first check if the status is enrolling
                //  there may be a case the status is not ready.
                
                //if status is not enrolling
                if(realmType != EnrollmentType.DOMAINGROUP && !enrolling){                    
                    retry ++;
                    
                    //is not keep going, don't even brother to sleep
                    if (retry > MAX_UNKNOWN_REALM_RETRY) {
                        System.out.println("realm \"" + name + "\" status is unknown. Sync may not be completed.");
                        break;
                    }
                    sleeptime = Math.min((int)(sleeptime * SPEED_DOWN_FACTOR), MAX_SHORT_SLEEP_PERIOD);
                }                
            
                //if enrolling, sleep longer
                if (realmType == EnrollmentType.DOMAINGROUP) {
                	// DomainGroup will show completed status right away, It may take a while, so use long sleep time
                	Thread.sleep(LONG_SLEEP_PERIOD);
                } else {
                	Thread.sleep(enrolling
                        ? LONG_SLEEP_PERIOD
                        : sleeptime);
                }
            } while (syncThread.isAlive());
            
        } catch (InterruptedException e1) {
            //ignore
            //syncThread is done;
            //yield, hope the sync can complete by itself.
            Thread.yield();
        }finally{
            System.out.println();
        }
        
        EnrollmentMgrException e = enrollmentServiceThread.getException();
        if (e != null) {
            throw e;
        }
    }
    
    protected static final String UNKNOWN_REALM_STATUS = "UNKNOWN_REALM_STATUS";
    
    protected String getRealmStatus(String name) throws EnrollmentMgrException {
        RealmList existingRealms = enrollmentWS.getRealms(name);
        Realm[] realms = existingRealms.getRealms();
        if (realms.length == 0) {
            return "realm \"" + name + "\" not found";
        }
        Realm realm = realms[0];
        if(realm == null){
            return UNKNOWN_REALM_STATUS;
        }
        EnrollmentStatus enrollmentStatus = realms[0].getStatus();
        if(enrollmentStatus == null){
            return UNKNOWN_REALM_STATUS;
        }
        String status = enrollmentStatus.getStatus();
        if(status == null){
            return UNKNOWN_REALM_STATUS;
        }
        return status;
    }
    
    protected EnrollmentStatus getEnrollmentStatus(String name) throws EnrollmentMgrException {
        RealmList existingRealms = enrollmentWS.getRealms(name);
        Realm[] realms = existingRealms.getRealms();
        if (realms.length == 0) {
            return null;
        }
        return realms[0].getStatus();
    }
    
    private class EnrollmentServiceThread implements Runnable {
        private final Realm newRealm;
        private final EnrollmentServiceWrapper enrollmentIFForSync;
        private final Thread callerThread;

        private EnrollmentMgrException exception;

        public EnrollmentServiceThread(EnrollmentServiceWrapper enrollmentIFForSync, Realm newRealm, Thread callerThread) {
            this.enrollmentIFForSync = enrollmentIFForSync;
            this.newRealm = newRealm;
            this.callerThread = callerThread;
        }

        public void run() {
            try {
                ((Stub) enrollmentIFForSync.getEnrollmentIF()).setTimeout(0);
                enrollmentIFForSync.enrollRealm(newRealm);
            } catch (EnrollmentMgrException e) {
                exception = e;
            }
            callerThread.interrupt();
        }

        //may return null if no exception is found
        public EnrollmentMgrException getException() {
            return this.exception;
        }
    }

    /**
     * Delete enrollment
     * @param name
     * @throws RemoteException
     */
    protected void delete(String name) throws EnrollmentMgrException {
        Realm realm = getTheOnlyRealm(name);
        this.enrollmentWS.deleteRealm(realm);
    }

    /**
     * List Action
     * @throws RemoteException
     * @throws EnrollmentMgrException 
     */
    protected void list() throws EnrollmentMgrException {
        System.out.println("\nFetching enrollment list from server....\n");
        RealmList existingRealms = enrollmentWS.getRealms(null);
        if (existingRealms != null) {
            Realm[] realms = existingRealms.getRealms();
            realms = realmsFilter(realms);
            if (realms == null || realms.length == 0) {
                System.out.println("No existing enrollments.");
                return;
            }
            for (Realm realm : realms) {
                printRealm(realm);
            }
        }
    }
    
    protected Realm[] realmsFilter(Realm[] realms) {
        if(realms == null){
            return null;
        }
        List<Realm> realmList = new ArrayList<Realm>(realms.length);

        final Collection<EnrollmentType> knownEnrollmenTypes = CLI_ENROLLMENT_TYPE.values();
        for (Realm realm : realms) {
            if (knownEnrollmenTypes.contains(realm.getType())) {
                realmList.add(realm);
            }
        }
        return realmList.toArray(new Realm[]{});
    }
    
    
    /**
     * Prints out the realm
     * 
     * @param realmToPrint
     */
    protected void printRealm(Realm realmToPrint) {
        StringBuffer text = new StringBuffer();
        text.append("Domain: '" + realmToPrint.getName() + "' Type: " + realmToPrint.getType() + "\n");

        // Prepare the profile properties:
        Profile existingProfile = realmToPrint.getProfile();

        if (realmToPrint.getType() == EnrollmentType.DOMAINGROUP) {
        	text.append("   Subdomains configured:\n");
            if (existingProfile != null) {
                EnrollmentProperty[] existingProperties = existingProfile.getProperties();
                if (existingProperties != null) {
                    for (EnrollmentProperty property : existingProperties) {
                    	String theKey = property.getKey();
                    	String display = null;
                    	if (theKey.startsWith("subdomain.") && (theKey.endsWith(".name")))
                    		display = theKey.substring("subdomain.".length());
                        if (display != null) {
                            text.append("      ");
                            String[] values = property.getValue();
                            for (int j = 0; j < values.length; j++) {
                                text.append(property.getValue()[j]);
                                if (j < (values.length - 1)) {
                                    text.append(" \\\n                ");
                                }
                            }
                            text.append("\n");
                        }
                    }
                }
            }       	
        }
    
        if (existingProfile != null) {
            EnrollmentProperty[] existingProperties = existingProfile.getProperties();
            if (existingProperties != null) {
                for (EnrollmentProperty property : existingProperties) {
                    String display = ENROLLMENT_PROPERTY_TO_DISPLAY.get(property.getKey());
                    if (display != null) {
                        text.append("   ");
                        text.append(display);
                        text.append(" : ");
                        String[] values = property.getValue();
                        for (int j = 0; j < values.length; j++) {
                            text.append(property.getValue()[j]);
                            if (j < (values.length - 1)) {
                                text.append(" \\\n                ");
                            }
                        }
                        text.append("\n");
                    }
                }
            }
        }
        
        // Enrollment status
        EnrollmentStatus status = realmToPrint.getStatus();
        if (status == null || ((status.getStartTime() == null) && (status.getEndTime() == null))) {
            text.append("   never synced\n");
        } else {
            text.append("   start time : " + status.getStartTime());
            if (!(status.getEndTime().indexOf("9999") > 0)) {
                text.append("\n   end time   : " + status.getEndTime());
            }
            text.append("\n   status     : " + status.getStatus());
            if (status.getErrorMessage() != null) {
                text.append("\n   message    : " + status.getErrorMessage());
            }
            text.append("\n");
        }

        System.out.println(text.toString());

    }
    
    private void normalizeProperty(EnrollmentProperty property){
        String key = property.getKey();
        property.setKey(key.trim().toLowerCase());
        String[] values = property.getValue();
        if ((values == null) || (values.length == 0)) {
            throw new IllegalArgumentException("Invalid value for " + key);
        } 
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                property.setValue(i, values[i].trim());
            }
        }
    }
    
    private void normalizeProperties(List<EnrollmentProperty> properties) {
        for (EnrollmentProperty property : properties) {
            normalizeProperty(property);
        }
    }
    
    /**
     * Converting a property hash map to property array 
     * @param map
     * @return
     */
    private EnrollmentProperty[] mapToPropertiesArray(Map<String, String[]> map) {
        if (map == null) {
            throw new NullPointerException("map is null");
        }

        Set<String> keys = map.keySet();
        EnrollmentProperty[] props = new EnrollmentProperty[keys.size()];
        int i = 0;
        for (Iterator<String> iter = keys.iterator(); iter.hasNext(); i++) {
            String key = iter.next();
            String[] values = map.get(key);
            props[i] = new EnrollmentProperty(key, values);
            normalizeProperty(props[i]);
        }
        return props;
    }
}
