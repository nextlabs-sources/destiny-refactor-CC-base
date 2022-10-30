/*
 * Created on April 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.DistinguishedName;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.framework.utils.ArrayUtils;

/**
 * @author atian 
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/util/LDAPEnrollmentHelper.java#1 $
 */


/**
 * LDAPEnrollmentHelper class is designed for creating Elements / Groups based on LDAP Entry
 *
 * 1. It contains Rfc Filter Evaluators for matching the expression given by enrollment 
 *    configuration with a given LDAP entry object.
 *
 * 2. After evaluation, we can determine what is the element type of LDAP Entry  
 * 
 * 3. We will create the element/group object based on element type
 *
 */
public class LDAPEnrollmentHelper {
	private static final char EQUALE = '=';

	private static final char LDAP_PATH_SEPERATOR = ',';

	public static DictionaryPath getDictionaryPathFromDN(String dn) {
		String[] paths = DistinguishedName.splitPath(dn);

		//reverse the order
		ArrayUtils.reverse(paths);

		return new DictionaryPath(paths);
	}
	
	/**
	 * get the rdn value 
	 * @param dn
	 * @param domainGroupName
	 * @return if the rdn contains EQUALE, return the value only
	 *         if the rdn doesn't contain EQUALE, return rdn
	 */
    public static String getNameFromDN(String dn, String domainGroupName) {
		String[] path = DistinguishedName.splitPath(dn);
		int index = path[0].indexOf(EQUALE);

		return domainGroupName + (index > 0 ? path[0].substring(index + 1) : path[0]);
	}

    public static String getDNfromDictionaryPath( DictionaryPath dicPath ) {
        String [] path = dicPath.getPath();
        int len = path.length ;
        if ( len > 1 ) {
            StringBuffer buffer = new StringBuffer(path[len-1]);
            for( int i = len-2; i >= 0 ; i-- ) {
                buffer.append( LDAP_PATH_SEPERATOR + path[i] );
            } 
            return buffer.toString(); 
        }
        return null;
    }
}
