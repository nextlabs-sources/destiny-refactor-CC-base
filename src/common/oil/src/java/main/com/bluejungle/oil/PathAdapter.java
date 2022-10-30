/*
 * Created on Oct 9,2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.oil;

import java.io.Serializable;

public class PathAdapter implements Serializable{
	private OSType ostype = new OSType();
	private String seperator;
	private String regexSeperator;

	public PathAdapter(){
		switch(ostype.getOSType())
		{
		case OSType.OS_TYPE_WINDOWS:
			seperator = "\\";
			regexSeperator = "\\\\";
			break;
		case OSType.OS_TYPE_LINUX:
			seperator = "/";
			regexSeperator = "/";
			break;
		}
	}
	

	public String getPathSeperator()
	{
		return seperator;
	}
	public String getRegexSeperator()
	{
		return regexSeperator;
	}
	
	public boolean isLinuxPhysicalPath(String inputpath)
	{//may NOT a good having this method 
		return ((inputpath.charAt(0)=='/') && (inputpath.charAt(1)!='/'));
	}	
	
	public boolean isPhysicalAddress(String inputpath)
	{
		boolean ret = false;
		switch(ostype.getOSType())
		{
		case OSType.OS_TYPE_WINDOWS:
			if(inputpath.charAt(1)==':')
				ret = true;
			break;
		case OSType.OS_TYPE_LINUX:
			if(inputpath.charAt(0)=='/' && inputpath.charAt(1)!='/')
				ret = true;
			break;
		}
		return ret;
	}
}