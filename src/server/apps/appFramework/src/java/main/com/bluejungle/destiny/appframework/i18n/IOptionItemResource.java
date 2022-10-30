/*
 * Created on Mar 17, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.i18n;

/**
 * IOptionItemResource represent a single Option Item specified in a resource
 * bundle. An Option Item is general included in a list of other option items
 * which are display in the UI as a menu of options. Each option item has an
 * internationalized label to display to the user and a value to set on the
 * backing bean or UI component and the menu item is selected
 * 
 * @see com.bluejungle.destiny.appframework.i18n.OptionItemResourceListFactory
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/main/com/bluejungle/destiny/appframework/i18n/IOptionItemResource.java#1 $
 */

public interface IOptionItemResource {

    /**
     * Retrieve the internationalized resource for this Option Item.
     * 
     * @return the internationalized resource for this Option Item.
     */
    public String getResource();

    /**
     * Retrieve the value associated with this Option Item
     * 
     * @return the value associated with this Option Item
     */
    public Object getOptionValue();
}
