package com.nextlabs.shared.tools;

import java.util.List;

/**
 * @author hchan
 * @date Mar 27, 2007
 * 
 * An object that hold the parsed option, it has key and value.
 * The key must be String. And the value could be any object.
 */
public interface IParsedOptions{
	<T> List<T> get(OptionId<T> optionId);
	<T> List<T> get(IOptionDescriptor<T> optionId);
	
	<T> boolean contains(OptionId<T> optionId);
	
	<T> void put(IOptionDescriptor<T> option, String[] values);
}
