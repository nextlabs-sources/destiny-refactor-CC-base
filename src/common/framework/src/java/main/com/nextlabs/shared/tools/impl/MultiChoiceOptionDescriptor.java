package com.nextlabs.shared.tools.impl;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor;

/**
 * @author hchan
 * @date Mar 30, 2007
 */
public class MultiChoiceOptionDescriptor extends CompoundOptionDescriptorBase implements IMultiChoiceOptionDescriptor {
	private final int mininumNumChoice;
	private final int maxinumNumChoice;

	public MultiChoiceOptionDescriptor(final int mininumNumChoice, final int maxinumNumChoice) {
		super();
		this.mininumNumChoice = mininumNumChoice;
		this.maxinumNumChoice = maxinumNumChoice;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ComponundOptionDescriptorBase#getType()
	 */
	public OptionDescriptorType getType() {
		return OptionDescriptorType.MULTI;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ICompoundOptionDescriptor#getName()
	 */
	public String getName() {
		StringBuilder output = new StringBuilder();
		for (ICompoundOptionDescriptor componentOption : getCompoundOptions()) {
			output.append(componentOption.getName()).append(",");
		}
		return output.toString();
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor#getMaxinumNumChoice()
	 */
	public int getMaxinumNumChoice() {
		return maxinumNumChoice;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor#getMininumNumChoice()
	 */
	public int getMininumNumChoice() {
		return mininumNumChoice;
	}

}
