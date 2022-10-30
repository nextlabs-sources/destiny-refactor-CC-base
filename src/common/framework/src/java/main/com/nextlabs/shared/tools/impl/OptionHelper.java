/*
 * Created on Aug 9, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/impl/OptionHelper.java#1 $
 */

public class OptionHelper {
	
	/**
	 * return the first selected IOptionDescriptor in the list.
	 * @param optionIds
	 * @param commandLine
	 * @return
	 */
	public static OptionId<?> findSelectedOption(Collection<OptionId<?>> optionIds,
			ICommandLine commandLine) {
		for(OptionId<?> option : optionIds){
			if(commandLine.isOptionExist(option)){
				return option;
			}
		}
		return null; 
	}
	
	public static Collection<OptionId<Boolean>> findTrueOption(
			Collection<OptionId<Boolean>> optionIds, ICommandLine commandLine) {
		Collection<OptionId<Boolean>> trueOptions = new LinkedList<OptionId<Boolean>>();
		for (OptionId<Boolean> optionId : optionIds) {
			IOptionDescriptor<Boolean> option = optionId.getOption();
			if (optionId.getValueType() == OptionValueType.ON_OFF) {
				if (commandLine.isOptionExist(optionId) || option.getDefaultValue()) {
					trueOptions.add(optionId);
				}
			}else{
				List<Boolean> parsedValues = commandLine.getParsedValues(optionId);
				if (parsedValues != null) {
					if (parsedValues.get(0)) {
						trueOptions.add(optionId);
					}
				} else if (option.getDefaultValue()) {
					trueOptions.add(optionId);
				}
			}
		}
		return trueOptions; 
	}
	
}
