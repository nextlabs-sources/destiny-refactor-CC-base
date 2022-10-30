/*
 * Created on Sep 10, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.pf.engine.destiny;

import java.io.File;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.destiny.services.policy.types.ExternalDataSourceType;
import com.bluejungle.framework.comp.ComponentManagerFactory;

public class DefaultFileResourceHandler {

    public static final String MY_DOCUMENTS_VAR_NAME = "[mydocuments]";

    public static final String MY_DESKTOP_VAR_NAME = "[mydesktop]";

    private static final String myDocuments;

    private static final String desktop;

    static {
        IOSWrapper osWrapper = (IOSWrapper) ComponentManagerFactory.getComponentManager().getComponent(OSWrapper.class);
        myDocuments = osWrapper.getMyDocumentsFolder().replace( File.separatorChar, '/' ).toLowerCase();
        desktop = osWrapper.getMyDesktopFolder().replace( File.separatorChar, '/' ).toLowerCase();
    }

    private static String getNativeString(String canonicalString) {
        int myDocPos = canonicalString.lastIndexOf( MY_DOCUMENTS_VAR_NAME );
        if ( myDocPos != -1 ) {
            String tail = canonicalString.substring(myDocPos+MY_DOCUMENTS_VAR_NAME.length());
            canonicalString= canonicalString.substring(0, myDocPos) + myDocuments;
            if ( ( tail.length() > 0 ) && ( tail.charAt(0) != '/' ) && ( tail.charAt(0) != '\\' ) ) {
                canonicalString+= File.separatorChar;
            }
            canonicalString+= tail;
        }
        int myDesktopPos = canonicalString.lastIndexOf( MY_DESKTOP_VAR_NAME );
        if ( myDesktopPos != -1 ) {
            String tail = canonicalString.substring(myDesktopPos+MY_DESKTOP_VAR_NAME.length());
            canonicalString = canonicalString.substring(0, myDesktopPos) + desktop;
            if ( ( tail.length() > 0 ) && ( tail.charAt(0) != '/' ) && ( tail.charAt(0) != '\\' ) ) {
                canonicalString += File.separatorChar;
            }
            canonicalString += tail;
        }
        return canonicalString;
    }

    public static String getNativeName (String canonicalName, char separator) {
        if ( canonicalName == null ) {
            return null;
        }
        String nativeString = getNativeString(canonicalName);
        return nativeString.replaceAll("^file:(///)?", "").replace ('/', separator);
    }

    public static String getNativeName (String canonicalName) {
        return getNativeName (canonicalName, File.separatorChar);
    }

    public static String getCanonicalName (String nativeName) {
        return getCanonicalName (nativeName, File.separatorChar);
    }

    public static String getCanonicalName (String nativeName, char separator) {

        String ret = nativeName.replace(File.separatorChar, '/').toLowerCase();

        if ( ret.startsWith( desktop ) ) {
            String fileName = ret.substring( desktop.length() );
            if ( ( fileName.length() > 0 ) && ( fileName.charAt(0) != '/' ) ) {
                ret = MY_DESKTOP_VAR_NAME + '/' + fileName;
            }
            else {
                ret = MY_DESKTOP_VAR_NAME + fileName;
            }
        }
        else if ( ret.startsWith( myDocuments ) ) {
            String fileName = ret.substring( myDocuments.length() );
            if ( ( fileName.length() > 0 ) && ( fileName.charAt(0) != '/' ) ) {
                ret = MY_DOCUMENTS_VAR_NAME + '/' + fileName;
            }
            else {
                ret = MY_DOCUMENTS_VAR_NAME + fileName;
            }
        }

        if ( !ret.startsWith ("//")) {
            ret = "///" + ret;
        }

        return "file:"+ret;
		
    }


    /**
     * get resource name: converting native name to blue jungle resource name
     *  We will use resource name to match with Predicate
     * @param nativeName
     *      The native name from File.
     */
    public static String getResourceName( String nativeName ) {
        String name = nativeName.replace(File.separatorChar,'/');
        if ( name.startsWith("//") ) { // this is a remote path
            return "file:" + name;
        }
        return "file:///"+ name; // this is a local path
    }

    /**
     * Normalize URL into internal format
     * @param url
     * @param type
     * @return
     */
    public static final String SHAREPOINT_URL_PREFIX = "SharePoint://";

    public static String getNormalizedResourceURL(String url, ExternalDataSourceType type) {

        if ( url == null ) {
            return url;
        }
        if ( type == ExternalDataSourceType.SHAREPOINT ) {
            int index = url.indexOf("://");
            if ( index > 0 ) {
                url = url.substring(index+3);
                url = url.replaceAll("//", "/");

                // remove all the .aspx in the string, for example
                // "SharePoint://sharepoint2007/Alpha/Lists/Alpha list/default view.aspx" will return
                // "SharePoint://sharepoint2007/Alpha/Lists/Alpha list/default view";
                // "SharePoint://ahuang-server01/Banking/**/Lists/Calendar/calendar.aspx/**" will return
                // "SharePoint://ahuang-server01/Banking/**/Lists/Calendar/calendar/**";
                // "SharePoint://sharepoint2007/Alpha/default.aspx" will return
                // "SharePoint://sharepoint2007/Alpha/default";
                // "SharePoint://sharepoint2007/Alpha/default.aspx123" will return
                // "SharePoint://sharepoint2007/Alpha/default.aspx123";
                // "SharePoint://sharepoint2007/Alpha/defaultaspx" will return
                // "SharePoint://sharepoint2007/Alpha/defaultaspx";
                // "SharePoint://sharepoint2007/Alpha/default.aspx$" will return
                // "SharePoint://sharepoint2007/Alpha/default.aspx$";
                url = url.replaceAll("[.]aspx($|/)", "$1");
                return SHAREPOINT_URL_PREFIX + url;
            }
        }
        return url;
    }

}

