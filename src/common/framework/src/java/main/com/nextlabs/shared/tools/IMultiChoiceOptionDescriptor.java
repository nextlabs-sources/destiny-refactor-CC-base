package com.nextlabs.shared.tools;

/**
 * Interface of option descriptor that users can select multi choice
 * 
 * @author hchan
 * @date Mar 30, 2007
 */
public interface IMultiChoiceOptionDescriptor extends ICompoundOptionDescriptor {
	/**
	 * @return minimum number can be choice
	 */
	int getMininumNumChoice();
	
	/**
	 * @return maximum number can be choice
	 */
	int getMaxinumNumChoice();
	
}
