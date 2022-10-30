package com.nextlabs.shared.tools.impl;

import java.util.List;

import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IParsedOptions;
import com.nextlabs.shared.tools.OptionId;

/**
 * @author hchan
 * @date Mar 29, 2007
 */
public class CommandLineImpl implements ICommandLine {
	private final IParsedOptions map;

	public CommandLineImpl(IParsedOptions parsedOptions){
		this.map = parsedOptions;
	}
	
	public <T> List<T> getParsedValues(OptionId<T> optionId){
		return map.get(optionId);
	}
	
	public boolean isOptionExist(OptionId<?> optionId){
		return map.contains(optionId);
	}
}
