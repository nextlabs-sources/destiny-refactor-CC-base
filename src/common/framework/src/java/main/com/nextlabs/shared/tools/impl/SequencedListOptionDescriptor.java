/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import com.nextlabs.shared.tools.ISequencedListOptionDescriptor;

/**
 * @author hchan
 * @date Mar 27, 2007
 */
public class SequencedListOptionDescriptor extends MultiChoiceOptionDescriptor implements ISequencedListOptionDescriptor {
	
	public SequencedListOptionDescriptor() {
		super(0, Integer.MAX_VALUE);
	}

	public OptionDescriptorType getType() {
		return OptionDescriptorType.SEQUENCE;
	}
}
