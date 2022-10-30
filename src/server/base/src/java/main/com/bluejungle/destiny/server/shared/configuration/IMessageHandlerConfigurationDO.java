/*
 * Created on Jul 21, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.server.shared.configuration;

import java.util.Properties;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/base/src/java/main/com/bluejungle/destiny/server/shared/configuration/IMessageHandlerConfigurationDO.java#1 $
 */

public interface IMessageHandlerConfigurationDO {

    String getName();

    String getClassName();

    Properties getProperties();

}