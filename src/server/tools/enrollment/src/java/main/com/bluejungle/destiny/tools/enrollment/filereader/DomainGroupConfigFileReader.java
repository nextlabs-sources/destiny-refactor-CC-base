/*
* Created on Aug 21, 2012
*
* All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
* San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
* worldwide.
*
* @author dwashburn
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filereader/DomainGroupConfigFileReader.java#1 $:
*/

/**
 * 
 */
package com.bluejungle.destiny.tools.enrollment.filereader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.mdom.impl.DomainGroupEnrollmentProperties;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.bluejungle.destiny.tools.enrollment.util.ValidationHelper;

/**
 * @author dwashburn
 *
 */
public class DomainGroupConfigFileReader extends BaseFileReader {

	protected Vector<EnrollableSubdomain> subdomains;
	
	/**
	 * @return the subdomains
	 */
	public Vector<EnrollableSubdomain> getSubdomains() {
		return subdomains;
	}
	
	/**
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws FileFormatException
	 */
	public DomainGroupConfigFileReader(File file) throws FileNotFoundException,
			IOException, FileFormatException {
		super(file);
	}

	@Override
	protected String getFileDescription() {
		return "The Multi-Domain configuration file";
	}

	@Override
	protected void validate(Properties properties) throws FileFormatException {
		// validate that all global properties, if specified, point to readable files that exist.
		
        String syncInterval = properties.getProperty(DomainGroupEnrollmentProperties.PULL_INTERVAL);
        if (syncInterval != null) {
            try {
                long interval = ValidationHelper.validateSyncInterval(syncInterval);
                if (interval > 0) {
                    String startTime = properties.getProperty(DomainGroupEnrollmentProperties.START_TIME);
                    String timeFormat = properties.getProperty(DomainGroupEnrollmentProperties.TIME_FORMAT);
                    ValidationHelper.validateSyncStartTime(startTime,timeFormat);
                }
            } catch (IllegalArgumentException e) {
                throw new FileFormatException(e.getMessage());
            }
        }
        
        String defaultDefFile = isReadableFileProperty(properties, DomainGroupEnrollmentProperties.DEFINITION_FILENAME_PROPERTY);
        String defaultFiltFile = isReadableFileProperty(properties, DomainGroupEnrollmentProperties.FILTER_FILENAME_PROPERTY);
        
        int subDomainCount = 0;
        if (subdomains == null) {
        	subdomains = new Vector<EnrollableSubdomain>(); 
        }
        while (true) {
        	String subdomainPropPrefix = DomainGroupEnrollmentProperties.SUBDOMAIN_PREFIX + subDomainCount;
        	String thisSubdomainTypeProp = subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_TYPE;
        	String thisSubdomainType = properties.getProperty(thisSubdomainTypeProp);
        	if (thisSubdomainType == null) 
        		break;     	 
        	thisSubdomainType = thisSubdomainType.trim();
        	validateSubdomainType(thisSubdomainType, thisSubdomainTypeProp);
        	
        	String thisSubdomainNameProp = subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_NAME;
        	String thisSubdomainName = properties.getProperty(thisSubdomainNameProp);
        	if (thisSubdomainName == null) 
        		break;
        	thisSubdomainName=thisSubdomainName.trim();
        	
        	// If sub domain connection, definition, or filter file properties are specified, they need to be readable files.
        	String connFile = isReadableFileProperty(properties, 
        		subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_CONNECTION_FILENAME_PROPERTY); 
        	if ((connFile == null) && (thisSubdomainType.compareTo("LDIF") != 0)) {
        		// LDIF enrollments don't have connection files.
        		throw new FileFormatException("Required subdomain property: " 
        										+ subdomainPropPrefix 
        										+ DomainGroupEnrollmentProperties.SUBDOMAIN_CONNECTION_FILENAME_PROPERTY 
        										+ " for sub-domain type: " + thisSubdomainType
        										+ " not found.");
        	}
        	String defFile = isReadableFileProperty(properties, 
        		subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_DEFINITION_FILENAME_PROPERTY);   
        	if (defFile == null)
        		defFile = defaultDefFile;
        	String filtFile = isReadableFileProperty(properties, 
        		subdomainPropPrefix + DomainGroupEnrollmentProperties.SUBDOMAIN_FILTER_FILENAME_PROPERTY);
        	if (filtFile == null)
        		filtFile = defaultFiltFile;
        	EnrollableSubdomain subDomain = new EnrollableSubdomain(thisSubdomainName, thisSubdomainType, connFile, defFile, filtFile);
        	subdomains.add(subDomain);
        	subDomainCount++;      	
        }
        
        if (subDomainCount == 0) {
        	throw new FileFormatException("DomainGroup configuration must specify subdomains.");
        }

	}

	/**
	 * @param thisSubdomainType
	 * @param prop
	 * @throws FileFormatException 
	 */
	private void validateSubdomainType(String thisSubdomainType, String prop) 
			throws FileFormatException {
		if (thisSubdomainType.compareTo("DIR")    == 0) return;
		if (thisSubdomainType.compareTo("PORTAL") == 0) return;
		if (thisSubdomainType.compareTo("LDIF")   == 0) return;
		if (thisSubdomainType.compareTo("TEXT")   == 0) return;
		
		throw new FileFormatException("Property: " + prop + 
			" with value: " + thisSubdomainType + 
			", is not a valid subdomain type. Valid values: DIR|PORTAL|LDIF|TEXT");
		
	}

	/**
	 * @param properties
	 * @throws FileFormatException
	 */
	private String isReadableFileProperty(Properties properties, String propName)
			throws FileFormatException {
		String propValueFileName = properties.getProperty(propName);
		if (propValueFileName != null) {
			File f = new File(propValueFileName);
			if (!f.exists()) {
				throw new FileFormatException("Property: " + propName + 
						" with value: " + propValueFileName + ", specifies a file that does not exist.");
			} else {
				if (!(f.isFile() && f.canRead())) {
					throw new FileFormatException("Property: " + propName + 
							" with value: " + propValueFileName + ", specifies a file that is not a readable file.");
				}
			}
		}
		return propValueFileName;
	}

	@Override
	protected List<EnrollmentProperty> convert(Properties properties) {
		return super.convert(properties);
	}

}
