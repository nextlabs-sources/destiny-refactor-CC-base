package com.nextlabs.shared.tools.impl;

import com.nextlabs.shared.tools.InvalidOptionDescriptorException;

/**
 * hack to let reset is avaliable
 *
 * @author hchan
 * @date Apr 10, 2007
 */
@SuppressWarnings("unchecked")
public class OptionMod extends Option{
	private OptionMod()
			throws InvalidOptionDescriptorException {
		super(null, null, null, false, null, null, false, 0);
	}

	public static void reset(){
		Option.reset();
		OptionTrigger.reset();
	}
}