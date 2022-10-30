package com.nextlabs.shared.tools;

import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;

/**
 * @author hchan
 * @date Mar 28, 2007
 */
abstract class EntityImortExportOptionDescriptorEnum implements IConsoleApplicationDescriptor {
	static final OptionId<String> FILE_OPTION = OptionId.create("F", OptionValueType.STRING);
	
//	public static final String	LIST_SUBSET_OPTION	= "l";
//	public static final String	LIST_ALL_OPTION		= "v";
	
	private OptionDescriptorTreeImpl options;
	
	EntityImortExportOptionDescriptorEnum() throws InvalidOptionDescriptorException {
		options = new OptionDescriptorTreeImpl(createRoot());
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getOptions()
	 */
	public IOptionDescriptorTree getOptions() {
		return options;
	}
	
	protected abstract ICompoundOptionDescriptor createRoot() throws InvalidOptionDescriptorException;
}
