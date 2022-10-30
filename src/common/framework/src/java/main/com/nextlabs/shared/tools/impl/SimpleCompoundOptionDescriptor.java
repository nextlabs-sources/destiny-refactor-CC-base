/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import java.util.Collections;
import java.util.List;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorVisitor;

/**
 * @author hchan
 * @date Mar 30, 2007
 */
public class SimpleCompoundOptionDescriptor implements ICompoundOptionDescriptor{
	private final IOptionDescriptor<?> option;
	private boolean required;
	
	public SimpleCompoundOptionDescriptor(IOptionDescriptor<?> option) {
		super();
		if(option != null){
			option.setParent(this);
			this.required = option.isRequired();
		}
		this.option = option;
		
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#getCompoundOptions()
	 */
	public List<ICompoundOptionDescriptor> getCompoundOptions() {
		//return empty list
		return Collections.emptyList();
	}

	public IOptionDescriptor<?> getOption() {
		return option;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#getType()
	 */
	public OptionDescriptorType getType() {
		return OptionDescriptorType.SIMPLE;
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#accept(com.nextlabs.shared.tools.IOptionDescriptorVisitor)
	 */
	public void accept(IOptionDescriptorVisitor visitor) {
		visitor.visit(this);
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#isRequired()
	 */
	public boolean isRequired() {
		return required;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#setRequired(boolean)
	 */
	public void setRequired(boolean required) {
		this.required = required;
		
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#getName()
	 */
	public String getName() {
		String output = option.getCommandLineIndicators().iterator().next();
		return output;
	}

	
}
