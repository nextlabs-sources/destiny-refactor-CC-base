/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import java.util.List;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorVisitor;
import com.nextlabs.shared.tools.IUsageRenderer;
import com.nextlabs.shared.tools.OptionId;

/**
 * TODO description
 *
 * @author hchan
 * @date Apr 4, 2007
 */
public class OptionDesciptorSearcher  implements IUsageRenderer, IOptionDescriptorVisitor {
	private final OptionId<?> optionId;
	private final String optionName;
	
	private IOptionDescriptor<?> searchResult;
	
	public OptionDesciptorSearcher(String optionName){
		optionId = null;
		this.optionName = optionName;
	}
	
	public OptionDesciptorSearcher(OptionId<?> optionId){
		this.optionId = optionId;
		optionName = null;
	}
	
	public boolean isFound(){
		return searchResult != null;
	}
	
	/**
	 * @return the searchResult
	 */
	public IOptionDescriptor<?> getSearchResult() {
		return searchResult;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IUsageRenderer#renderUsage(com.nextlabs.shared.tools.IConsoleApplicationDescriptor)
	 */
	public void renderUsage(IConsoleApplicationDescriptor descriptor) {
		ICompoundOptionDescriptor root = descriptor.getOptions().getRootOption();
		root.accept(this);
		
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorVisitor#visit(com.nextlabs.shared.tools.ICompoundOptionDescriptor)
	 */
	public void visit(ICompoundOptionDescriptor compoundOptionDescriptor) {
		if(isFound()){
			return;
		}
		switch (compoundOptionDescriptor.getType()) {
		case SIMPLE:
			IOptionDescriptor<?> option = ((SimpleCompoundOptionDescriptor) compoundOptionDescriptor).getOption();
			if (option != null) {
				option.accept(this);
			}
			break;
		default:
			List<ICompoundOptionDescriptor> list = compoundOptionDescriptor.getCompoundOptions();
			for (ICompoundOptionDescriptor descriptor : list) {
				descriptor.accept(this);
			}
			break;
		}
		
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorVisitor#visit(com.nextlabs.shared.tools.IOptionDescriptor)
	 */
	public void visit(IOptionDescriptor<?> optionDescriptor) {
		if(isFound()){
			return;
		}
		if (optionName != null && optionDescriptor.getName().equals(optionName)) {
			searchResult = optionDescriptor;
		} else if (optionId != null && optionDescriptor.getOptionId().equals(optionId)) {
			searchResult = optionDescriptor;
		}
	}
}
