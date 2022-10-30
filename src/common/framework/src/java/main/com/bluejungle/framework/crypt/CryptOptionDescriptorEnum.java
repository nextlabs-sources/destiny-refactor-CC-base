package com.bluejungle.framework.crypt;

import java.util.Set;
import java.util.TreeSet;

import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.SimpleCompoundOptionDescriptor;

/**
 * TODO description
 *
 * @author hchan
 * @date Jul 3, 2007
 */
class CryptOptionDescriptorEnum implements IConsoleApplicationDescriptor{
	static final OptionId<Boolean> IS_ESCAPED_STRING_OPTION_ID = OptionId.create("e", OptionValueType.ON_OFF); 
	
	
	private IOptionDescriptorTree options;
	
	CryptOptionDescriptorEnum() throws InvalidOptionDescriptorException{
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
		Set<String> commandLineIndicators = new TreeSet<String>();
		commandLineIndicators.add("w");
		commandLineIndicators.add("password");
		String description = "password";
		String valueLabel = "password to encrypt";
		boolean required = true;
		boolean  valueRequired = true;
		int numPossibleValues = 1;
		IOptionDescriptor<?> option = new Option<String>(
				PASSWORD_OPTION_ID, 
				commandLineIndicators,
				description,
				required,
				valueLabel,
				(String)null, // defaultValue, 
				valueRequired, 
				numPossibleValues);
		root.add(new SimpleCompoundOptionDescriptor(option));
		
		description = "true if the password contain escaped characters, by default is false";
		required = false;
		valueRequired = false;
		option = Option.createOnOffOption(IS_ESCAPED_STRING_OPTION_ID, description);
		root.add(new SimpleCompoundOptionDescriptor(option));
		
		options = new OptionDescriptorTreeImpl(root);
	}
	
	public String getLongDescription() {
		return "The encryptor class is used to encrypt a given password. This utility can be used by customers " +
		"when changing an encrypted password in a configuration file. The encrypted password will be " +
		"displayed in the standard output.";
	}
	public String getName() {
		return "mkpassword";
	}
	public IOptionDescriptorTree getOptions() {
		return options;
	}
	public String getShortDescription() {
		return "The encryptor class is used to encrypt a given password.";
	}
	
	
	
}
