package com.nextlabs.shared.tools;

/**
 * throw this exception if user try to request an OptionDescritpor but could not be found
 *
 * @author hchan
 * @date Apr 4, 2007
 */
public class OptionDescriptorNotFoundException extends RuntimeException {
	public OptionDescriptorNotFoundException(String optionId) {
		super("OptionId \"" + optionId + "\" is not found");
	}
}
