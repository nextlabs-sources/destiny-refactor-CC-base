package com.nextlabs.shared.tools.impl;

import java.util.List;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionDescriptorNotFoundException;

/**
 * TODO description
 *
 * @author hchan
 * @date Apr 4, 2007
 */
public class OptionDescriptorTreeImpl implements IOptionDescriptorTree {
	private final ICompoundOptionDescriptor root;

	static ICompoundOptionDescriptor addHelpToRoot(ICompoundOptionDescriptor root)
			throws InvalidOptionDescriptorException {
		UniqueChoiceOptionDescriptor superRoot = new UniqueChoiceOptionDescriptor();
		superRoot.add(Option.HELP_OPTION);
		superRoot.add(root);
		return superRoot;
	}
	
	public OptionDescriptorTreeImpl(ICompoundOptionDescriptor root)
			throws InvalidOptionDescriptorException {
		this(root, true);
	}
	
	public OptionDescriptorTreeImpl(ICompoundOptionDescriptor root, boolean addHelp)
		throws InvalidOptionDescriptorException {
		this.root = addHelp ? addHelpToRoot(root) : root;
	}

	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorTree#getRootOption()
	 */
	public ICompoundOptionDescriptor getRootOption() {
		return root;
	}
	
	public static ICompoundOptionDescriptor getRealRootOption(ICompoundOptionDescriptor root){
		if (root.getType() == ICompoundOptionDescriptor.OptionDescriptorType.UNIQUE) {
			List<ICompoundOptionDescriptor> compoundOptions = root.getCompoundOptions();
			if (compoundOptions.size() == 2) {
				for (ICompoundOptionDescriptor compoundOption : compoundOptions) {
					if (compoundOption.getType() != ICompoundOptionDescriptor.OptionDescriptorType.SIMPLE
						|| ((SimpleCompoundOptionDescriptor)compoundOption).getOption() != Option.HELP_OPTION) {
							root = compoundOption;
					}
				}
			}
		}
		return root;
	}

	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorTree#getOption(java.lang.String)
	 */
	public IOptionDescriptor<?> getOption(String optionId) {
		OptionDesciptorSearcher optionDesciptorSearcher = new OptionDesciptorSearcher(optionId);
		optionDesciptorSearcher.visit(root);
		if (!optionDesciptorSearcher.isFound()) {
			throw new OptionDescriptorNotFoundException(optionId);
		} else {
			return optionDesciptorSearcher.getSearchResult();
		}
	}
}
