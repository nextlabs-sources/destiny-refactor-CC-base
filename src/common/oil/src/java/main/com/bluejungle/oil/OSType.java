/*
 * Created on Oct 9,2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.oil;

import java.io.Serializable;

public class OSType  implements Serializable{
	public static final int OS_TYPE_WINDOWS = 1;
	public static final int OS_TYPE_LINUX = 2;
	
	private int osType;
	
	public OSType() {
		if(isWindows())
		{
			osType = OS_TYPE_WINDOWS;
			return;
		}
		if(isLinux())
		{
			osType = OS_TYPE_LINUX;
			return;
		}
	}
	
	public int getOSType(){
		return osType;
	}
	
	private boolean isWindows(){
		String os = System.getProperty("os.name");
		return os.toLowerCase().startsWith("windows");
	}
	
	private boolean isLinux() {
		String os = System.getProperty("os.name");
		return os.equals("Linux");
       }
}