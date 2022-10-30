/*
 * Created on Feb 1, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.pf.domain.destiny.serviceprovider;
/**
 * @author nitoi
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/domain/destiny/serviceprovider/ExternalServiceProviderResponse.java#1 $
 * @description concrete class to implement response from external service provider
 */


public class ExternalServiceProviderResponse implements IExternalServiceProviderResponse {

	String formatString = null; // format string - e.g., "is" for integer, string
	Object[] data = null; // array of objects that represent data, e.g., String, integer
	
	// getter for format string
    public String getFormatString() {
    	return formatString;
    }
    
    // getter for data
    public Object[] getData() {
    	return data;
    }
    
    // setter for format string
    public void setFormatString(String inStr) {
    	formatString = inStr;
    }
    
    // setter for data
    public void setData(Object[] objs) {
    	data = objs;
    }
}
