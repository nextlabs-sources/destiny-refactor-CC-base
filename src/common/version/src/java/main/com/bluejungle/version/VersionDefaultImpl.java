/*
 * Created on Aug 7, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.version;

import java.io.Serializable;
import java.util.IllegalFormatException;

/**
 * @author rlin
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/version/src/java/main/com/bluejungle/version/VersionDefaultImpl.java#1 $
 */

public class VersionDefaultImpl implements IVersion, Serializable {
	private static final long serialVersionUID = 1L;
	
    private int major = 0;
    private int minor = 0;
    private int maintenance = 0;
    private int patch = 0;
    private int build = 0;
    
    /**
     * 
     * Constructor
     * @param major
     * @param minor
     * @param maintenance
     * @param patch
     * @param build
     */
    public VersionDefaultImpl(int major, int minor, int maintenance, int patch, int build){
        setMajor(major);
        setMinor(minor);
        setMaintenance(maintenance);
        setPatch(patch);
        setBuild(build);
    }
    
    public static IVersion getValue(String str) throws IllegalFormatException {
		String[] numbers = str.split("\\.");

		int minor = 0, maintenance = 0, patch = 0, build = 0;
		int major = Integer.parseInt(numbers[0]);

		if (numbers.length > 1) {
			minor = Integer.parseInt(numbers[1]);
		}
		
		if (numbers.length > 2) {
			maintenance = Integer.parseInt(numbers[2]);
		}

		if (numbers.length > 3) {
			patch = Integer.parseInt(numbers[3]);
		}

		if (numbers.length > 4) {
			build = Integer.parseInt(numbers[4]);
		}

		return new VersionDefaultImpl(major, minor, maintenance, patch, build);
	}
    
    @Override
	public String toString() {
		return major + "." + minor + "." + maintenance + "." + patch + "." + build;
	}
    
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(IVersion o) {
		return this.major == o.getMajor() ?
				this.minor == o.getMinor() ?
				this.maintenance == o.getMaintenance() ?		
				this.patch == o.getPatch() ? 
					this.build - o.getBuild() 
					: this.patch - o.getPatch()
					: this.maintenance - o.getMaintenance()
					: this.minor - o.getMinor()
					: this.major - o.getMajor();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final VersionDefaultImpl other = (VersionDefaultImpl) obj;
		return this.compareTo(other) == 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + build;
		result = prime * result + maintenance;
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + patch;
		return result;
	}

	/**
     * @see com.bluejungle.version.IVersion#getMajor()
     */
    public int getMajor() {
        return this.major;
    }

    /**
     * @see com.bluejungle.version.IVersion#getMinor()
     */
    public int getMinor() {
        return this.minor;
    }

    /**
     * @see com.bluejungle.version.IVersion#getMaintenance()
     */
    public int getMaintenance() {
        return this.maintenance;
    }

    /**
     * @see com.bluejungle.version.IVersion#getPatch()
     */
    public int getPatch() {
        return this.patch;
    }

    /**
     * @see com.bluejungle.version.IVersion#getBuild()
     */
    public int getBuild() {
        return this.build;
    }

    /**
     * @see com.bluejungle.version.IVersion#setMajor()
     */
    public void setMajor(int major) {
        if (major < 0){
            this.major = 0;
        } else {
            this.major = major;
        }
    }

    /**
     * @see com.bluejungle.version.IVersion#setMinor()
     */
    public void setMinor(int minor) {
        if (minor < 0){
            this.minor = 0;
        } else {
            this.minor = minor;
        }
    }

    /**
     * @see com.bluejungle.version.IVersion#setMaintenance()
     */
    public void setMaintenance(int maintenance) {
        if (maintenance < 0){
            this.maintenance = 0;
        } else {
            this.maintenance = maintenance;
        }
    }

    /**
     * @see com.bluejungle.version.IVersion#setPatch()
     */
    public void setPatch(int patch) {
        if (patch < 0){
            this.patch = 0;
        } else {
            this.patch = patch;
        }
    }

    /**
     * @see com.bluejungle.version.IVersion#setBuild()
     */
    public void setBuild(int build) {
        if (build < 0){
            this.build = 0;
        } else {
            this.build = build;
        }
    }
}
