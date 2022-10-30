package com.nextlabs.shared.tools;

/**
 * The very top root of IOptionDescriptor
 * 
 * @author hchan
 * @date Mar 27, 2007
 */
public interface IOptionDescriptorTree {
	ICompoundOptionDescriptor getRootOption();

	/**
	 * @param optionId
	 * @return IOptionDescriptor with a specific optionId in the tree
	 */
	IOptionDescriptor<?> getOption(String optionId);
}
