package com.nextlabs.shared.tools;

import java.util.List;

/**
 * CommandLine interface
 * 
 * @author hchan
 * @date Mar 27, 2007
 * 
 */
public interface ICommandLine{	
	/**
	 * 
	 * @param optionId	
	 * @return an array of parsed values that is corresponding to  <code>optionId</code>
	 */
	<T> List<T> getParsedValues(OptionId<T> optionId);
	
	/**
	 * 
	 * @param optionId
	 * @return true if the optionId exists in the command line input.
	 */
	boolean isOptionExist(OptionId<?> optionId);
}
