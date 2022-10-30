package com.bluejungle.destiny.agent.tools;

import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.MultiChoiceOptionDescriptor;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;

/**
 * TODO description
 *
 * @author hchan
 * @date Jul 3, 2007
 */
class DecryptBundleOptionDescriptorEnum implements IConsoleApplicationDescriptor {
    static final OptionId<String> BUNDLE_FILENAME_OPTION_ID = new OptionId<String>("b", OptionValueType.STRING);
    static final OptionId<String> OUTPUT_FILENAME_OPTION_ID = new OptionId<String>("f", OptionValueType.STRING);
    static final OptionId<String> AGENT_HOME_OPTION_ID = new OptionId<String>("e", OptionValueType.STRING);
    static final OptionId<String> PASSWORD_OPTION_ID = new OptionId<String>("p", OptionValueType.STRING);
    static final OptionId<String> ENCODING_OPTION_ID = new OptionId<String>("encoding", OptionValueType.STRING);
    
    
    private static final String DEFAULT_BUNDLE_FILE_NAME = "bundle.bin";
    private static final String DEFAULT_BUNDLE_OUT_FILE = "bundle.out";
    private static final String DEFAULT_AGENT_HOME = "c:\\Program Files\\Compliant Enterprise\\Windows Desktop Enforcer";

 
    private IOptionDescriptorTree options;
 
    DecryptBundleOptionDescriptorEnum() throws InvalidOptionDescriptorException{
        MultiChoiceOptionDescriptor root = new MultiChoiceOptionDescriptor(0, 3);

        String description = "The file path to the policy deployment bundle. Defaults to the standard agent bundle file";
        String valueLabel = "bundleFilename";
        IOptionDescriptor<?> option = Option.createOption(BUNDLE_FILENAME_OPTION_ID, 
                                                          description, valueLabel, DEFAULT_BUNDLE_FILE_NAME);
        root.add(option);
  
        description = "The file path to the text file that will contain the extracted bundle file contents";
        valueLabel = "outputFilename";
        option = Option.createOption(OUTPUT_FILENAME_OPTION_ID, 
                                     description, valueLabel, DEFAULT_BUNDLE_OUT_FILE); 
        root.add(option);
  
        description = "The agent installation directory";
        valueLabel = "agentHome";
        option = Option.createOption(AGENT_HOME_OPTION_ID, 
                                     description, valueLabel, DEFAULT_AGENT_HOME);  
        root.add(option);
  
        description = "password";
        valueLabel = "password";
        option = Option.createOption(PASSWORD_OPTION_ID, 
                                     description, valueLabel, null);  
        root.add(option);

        description = "encoding";
        valueLabel = "encoding";
        option = Option.createOption(ENCODING_OPTION_ID,
                                     description, valueLabel, null);
        root.add(option);
        
        UniqueChoiceOptionDescriptor superRoot = new UniqueChoiceOptionDescriptor();
        superRoot.add(Option.HELP_OPTION);
        superRoot.add(root);
        superRoot.setRequired(false);
  
        options = new OptionDescriptorTreeImpl(superRoot, false);
    }
 
    public String getLongDescription() {
        return getShortDescription();
    }

    public String getName() {
        return "decrypt";
    }

    public IOptionDescriptorTree getOptions() {
        return options;
    }

    public String getShortDescription() {
        return "Decrypt will extract the data from an encrypted policy deployment bundle file and " +
            "write the contents to an output file for debug purposes.";
    }
}
