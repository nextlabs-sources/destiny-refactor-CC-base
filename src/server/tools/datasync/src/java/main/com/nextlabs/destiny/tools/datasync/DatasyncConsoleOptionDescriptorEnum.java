/*
 * Created on Jun 21, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.datasync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.bluejungle.framework.utils.ArrayUtils;
import com.nextlabs.destiny.container.dac.datasync.IDataSyncTask.SyncType;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/datasync/src/java/main/com/nextlabs/destiny/tools/datasync/DatasyncConsoleOptionDescriptorEnum.java#1 $
 */

public class DatasyncConsoleOptionDescriptorEnum implements IConsoleApplicationDescriptor{
    static final OptionId<SyncType> ACTION_OPTION_ID = new OptionId<SyncType>("action", OptionValueType.CUSTOM_LIST);
    
    static final OptionId<File> SERVER_CONFIGURATION_FOLDER_OPTION_ID = new OptionId<File>("serverConfigFolder", OptionValueType.EXIST_FOLDER);
    
    
    //don't expose the force option
//    static final OptionId<Boolean> FORCE_OPTION_ID =
//        OptionId.create("force", OptionValueType.ON_OFF);
    
    private final IOptionDescriptorTree tree;

    DatasyncConsoleOptionDescriptorEnum() throws InvalidOptionDescriptorException {
        SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
        
        String description;
        String valueLabel;
        description = "action";
        valueLabel = ArrayUtils.asString(SyncType.values(), "/");
        Option option = Option.createOption(ACTION_OPTION_ID, description, valueLabel);
        root.add(option);
        OptionValueType.CUSTOM_LIST.addCustomValue(ACTION_OPTION_ID, SyncType.values());
        
        File defaultValue = new File("C:\\Program Files\\NextLabs\\Policy Server\\server\\configuration\\");
        description = "policy server configuration folder";
        valueLabel = "path to configuration folder";
        boolean required = true;
        boolean valueRequired = true;
        List<String> cmdInds = new ArrayList<String>();
        cmdInds.add("sc");
        cmdInds.add(SERVER_CONFIGURATION_FOLDER_OPTION_ID.getName());
        option = new Option<File>(
                SERVER_CONFIGURATION_FOLDER_OPTION_ID,
                cmdInds,
                description,
                required,
                valueLabel,
                defaultValue,
                valueRequired,
                1);
        
        root.add(option);
        
//        description = "Each datasync manager has its own unique id. You can assign the id manually but you have to make sure it is unique.";
//        valueLabel = "data_manager_id";
//        root.add(Option.createOption(MANAGER_ID_OPTION_ID, description, valueLabel, null));

        
//        root.add(Option.createOnOffOption(FORCE_OPTION_ID, "Force action to execute! You should not do it if there is any other data sync manager is running."));
        
        tree = new OptionDescriptorTreeImpl(root);
    }

    public String getLongDescription() {
        return getShortDescription();
    }

    public String getName() {
        return "datasync";
    }

    public IOptionDescriptorTree getOptions() {
        return tree;
    }

    public String getShortDescription() {
        return "A tool for managing tables in the Activity Journal database. ";
    }

}
