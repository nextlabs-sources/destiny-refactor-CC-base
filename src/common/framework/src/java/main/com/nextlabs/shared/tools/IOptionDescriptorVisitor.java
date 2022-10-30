package com.nextlabs.shared.tools;

/**
 * @author hchan
 * @date Mar 27, 2007
 */
public interface IOptionDescriptorVisitor {
	void visit(IOptionDescriptor<?> optionDescriptor);
	
	void visit(ICompoundOptionDescriptor compoundOptionDescriptor);
}
