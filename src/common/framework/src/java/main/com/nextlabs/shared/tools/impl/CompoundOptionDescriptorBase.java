/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import java.util.ArrayList;
import java.util.List;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorVisitor;

/**
 * @author hchan
 * @date Mar 28, 2007
 */
public abstract class CompoundOptionDescriptorBase implements ICompoundOptionDescriptor {
	private List<ICompoundOptionDescriptor> compoundOptions = new ArrayList<ICompoundOptionDescriptor>();
	
	//default all compound is 
	private boolean required = true;
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#accept(com.nextlabs.shared.tools.IOptionDescriptorVisitor)
	 */
	public void accept(IOptionDescriptorVisitor visitor) {
		visitor.visit(this);
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#getCompoundOptions()
	 */
	public List<ICompoundOptionDescriptor> getCompoundOptions() {
		return compoundOptions;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#isRequired()
	 */
	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required){
		this.required = required;
	}
	
	public void add(ICompoundOptionDescriptor compoundOptionDescriptor){
		compoundOptions.add(compoundOptionDescriptor);
	}
	
	public void add(IOptionDescriptor<?> optionDescriptor){
		SimpleCompoundOptionDescriptor simpleCompoundOptionDescriptor = new SimpleCompoundOptionDescriptor(optionDescriptor);
		simpleCompoundOptionDescriptor.setRequired(optionDescriptor.isRequired());
		compoundOptions.add(simpleCompoundOptionDescriptor);
	}
	
	public abstract String getName();
}
