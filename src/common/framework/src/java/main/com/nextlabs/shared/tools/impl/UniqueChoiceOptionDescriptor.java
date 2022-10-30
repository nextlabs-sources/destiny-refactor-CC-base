/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import com.nextlabs.shared.tools.IUniqueChoiceOptionDescriptor;

/**
 * @author hchan
 * @date Mar 27, 2007
 */
public class UniqueChoiceOptionDescriptor extends MultiChoiceOptionDescriptor implements IUniqueChoiceOptionDescriptor {

	public UniqueChoiceOptionDescriptor() {
		super(0, 1);
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.impl.MultiChoiceOptionDescriptor#getType()
	 */
	@Override
	public OptionDescriptorType getType() {
		return OptionDescriptorType.UNIQUE;
	}
	
}
