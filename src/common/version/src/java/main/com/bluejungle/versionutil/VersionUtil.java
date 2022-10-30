/*
 * Created on Aug 9, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.versionutil;

import org.apache.axis.types.NonNegativeInteger;

import com.bluejungle.version.types.Version;
import com.bluejungle.version.IVersion;
import com.bluejungle.version.VersionDefaultImpl;


/**
 * This class is used to convert the java IVersion object to a Version axis object 
 * and vice versa
 * 
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/version/src/java/main/com/bluejungle/versionutil/VersionUtil.java#1 $
 */

public class VersionUtil {

    /**
	 * Converts the WS Version object into a java IVersion
	 * @return an IVersion object
	 */
	public static IVersion convertWSVersionToIVersion(Version wsVersion){
	    IVersion javaIVersion = new VersionDefaultImpl(wsVersion.getMajor().intValue(), 
	            									   wsVersion.getMinor().intValue(), 
	            									   wsVersion.getMaintenance().intValue(), 
	            									   wsVersion.getPatch().intValue(), 
	            									   wsVersion.getBuild().intValue());
	    return javaIVersion;
	}

	/**
	 * Converts the java IVersion object into 
	 * @return a WS Version object
	 */
	public static Version convertIVersionToWSVersion(IVersion version){
	    NonNegativeInteger major = new NonNegativeInteger(String.valueOf(version.getMajor()));
	    NonNegativeInteger minor = new NonNegativeInteger(String.valueOf(version.getMinor()));
	    NonNegativeInteger maintenance = new NonNegativeInteger(String.valueOf(version.getMaintenance()));
	    NonNegativeInteger patch = new NonNegativeInteger(String.valueOf(version.getPatch()));
	    NonNegativeInteger build = new NonNegativeInteger(String.valueOf(version.getBuild()));
	    Version wsVersion = new Version(major, minor, maintenance, patch, build);
	    return wsVersion;
	}
}
