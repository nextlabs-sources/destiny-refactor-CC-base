/*
 * Created on Mar 16, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.appframework.i18n;

import com.bluejungle.destiny.appframework.CommonConstants;

/**
 * Contains constants for common Option Item Resource List factories
 * 
 * @see com.bluejungle.destiny.appframework.i18n.OptionItemResourceListFactory
 * @author sgoldstein
 * @version $Id: //depot/Destiny/D_Nimbus/pcv/Nimbus_8.1_bugfixes/main/src/server/apps/appFramework/src/java/main/com/bluejungle/destiny/appframework/i18n/CommonOptionItemResourceLists.java#1 $
 */

public interface CommonOptionItemResourceLists {

    public static final OptionItemResourceListFactory MAX_UI_ELEMENT_LIST_SIZE_OPTIONS = new OptionItemResourceListFactory(CommonConstants.COMMON_BUNDLE_NAME, "max_ui_element_list_size_option.",
            new OptionItemResourceListFactory.IOptionItemResourceValueTypeConverter() {

                /**
                 * @see com.bluejungle.destiny.appframework.i18n.OptionItemResourceListFactory.IOptionItemResourceValueTypeConverter#convert(java.lang.String)
                 */
                public Object convert(String keyToConvert) {
                    return Integer.valueOf(keyToConvert);
                }
            });
}
