/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IParsedOptions;
import com.nextlabs.shared.tools.OptionId;

/**
 * @author hchan
 * @date Mar 29, 2007
 */
public class ParsedOptions implements IParsedOptions {
	Map<OptionId<?>, List<?>> idToValuseMap = new HashMap<OptionId<?>, List<?>>();
	Map<IOptionDescriptor<?>, List<?>> optionToValuesMap = new HashMap<IOptionDescriptor<?>, List<?>>();
	
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -5813893338776376408L;
	
	public ParsedOptions() {
		super();
	}
	
	public <T> void put(IOptionDescriptor<T> option, String[] values) {
		List<T> list = new ArrayList<T>();
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				list.add(option.getValueType().getValue(option.getOptionId(), values[i]));
			}
		}

		idToValuseMap.put(option.getOptionId(), list);
		optionToValuesMap.put(option, list);
	}
	
	public <T> boolean contains(IOptionDescriptor<T> option) {
		return optionToValuesMap.containsKey(option);
	}

	public <T> boolean contains(OptionId<T> optionId) {
		return idToValuseMap.containsKey(optionId);
	}

	public <T> List<T> get(IOptionDescriptor<T> option) {
		return (List<T>)optionToValuesMap.get(option);
	}

	public <T> List<T> get(OptionId<T> optionId) {
		return (List<T>)idToValuseMap.get(optionId);
	}

}
