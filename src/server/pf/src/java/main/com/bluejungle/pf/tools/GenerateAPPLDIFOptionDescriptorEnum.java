package com.bluejungle.pf.tools;

import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;

/**
 * Option Descriptor Enum for GenAPPLDIF
 *
 * @author hchan
 * @date Jul 3, 2007
 */
class GenerateAPPLDIFOptionDescriptorEnum implements IConsoleApplicationDescriptor{
	static final OptionId<String> EXENAME_OPTION_ID = new OptionId<String>("e",  OptionValueType.STRING);
	static final OptionId<String> APPNAME_OPTION_ID = new OptionId<String>("n",  OptionValueType.STRING);
	
	private IOptionDescriptorTree options;
	
	public GenerateAPPLDIFOptionDescriptorEnum() throws InvalidOptionDescriptorException {
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
		
		String description = "Application executable pathname";
		String valueLabel = "application_executable_pathname";
		Option<?> option = Option.createOption(EXENAME_OPTION_ID, description, valueLabel);
		root.add(option);

		description = "Application name";
		valueLabel = "application_name";
		option = Option.createOption(APPNAME_OPTION_ID, description, valueLabel);
		root.add(option);
		
		options = new OptionDescriptorTreeImpl(root);
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getLongDescription()
	 */
	public String getLongDescription() {
		return "Generate Application LDIF\n\nExample: GenerateAPPLDIF -e c:\\Program Files\\Microsoft Office\\OFFICE11\\WINWORD.EXE -n \"Microsoft Word\"";
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getName()
	 */
	public String getName() {
		return "genappldif";
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getOptions()
	 */
	public IOptionDescriptorTree getOptions() {
		return options;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getShortDescription()
	 */
	public String getShortDescription() {
		return "Generate Application LDIF";
	}
	

}
