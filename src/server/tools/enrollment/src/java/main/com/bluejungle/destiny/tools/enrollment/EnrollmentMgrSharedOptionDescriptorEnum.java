/*
 * Created on Aug 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment;

import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.CompoundOptionDescriptorBase;
import com.nextlabs.shared.tools.impl.Option;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/EnrollmentMgrSharedOptionDescriptorEnum.java#1 $
 */

public abstract class EnrollmentMgrSharedOptionDescriptorEnum implements IConsoleApplicationDescriptor{
    static final OptionId<Boolean> VERBOSE_OPTIONS_ID  = OptionId.create("v", OptionValueType.ON_OFF);
    
    static final OptionId<String> KEYSTORE_PASSWORD_OPTION_ID = OptionId.create("z", OptionValueType.STRING);
    
    protected void addSharedOptions(CompoundOptionDescriptorBase root){
        String description, valueLabel;
        Option<?> option;
        
        description = "server hostname, default is localhost";
        valueLabel = "server";
        option = Option.createOption(HOST_OPTION_ID, description, valueLabel, "localhost");
        root.add(option);        
        
        description = "server port, default is 8443";
        valueLabel = "port";
        option = Option.createOption(PORT_OPTION_ID, description, valueLabel, 8443);
        root.add(option);
        
        description = "username";
        valueLabel = "username";
        option = Option.createOption(USER_ID_OPTION_ID, description, valueLabel);
        root.add(option);
        
        description = "password, if not specific in the command line. It will prompt and ask you";
        valueLabel = "password";
        option = Option.createOption(PASSWORD_OPTION_ID, description, valueLabel, null);
        root.add(option);
        
        description = "keystore password";
        valueLabel = "keystorePassword";
        option = Option.createOption(KEYSTORE_PASSWORD_OPTION_ID, description, valueLabel, "password");
        root.add(option);
        
        description = "verbose mode";
        option = Option.createOnOffOption(VERBOSE_OPTIONS_ID, description);
        root.add(option);
    }
}
