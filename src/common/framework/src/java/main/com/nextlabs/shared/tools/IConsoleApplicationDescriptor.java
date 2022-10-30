package com.nextlabs.shared.tools;

/**
 * Console Application Descriptor interface
 * It describes the console.
 * 
 * @author hchan
 * @date Mar 27, 2007
 */
public interface IConsoleApplicationDescriptor {
	/**
	 * -h, OptionValueType.NONE
	 */
	OptionId<Boolean> HELP_OPTION_ID	= OptionId.create("h", OptionValueType.ON_OFF);
	
	/**
	 * -s, OptionValueType.STRING
	 */
	OptionId<String> HOST_OPTION_ID		= OptionId.create("s", OptionValueType.STRING);
	
	/**
	 * -u, OptionValueType.STRING
	 */
	OptionId<String> USER_ID_OPTION_ID	= OptionId.create("u", OptionValueType.STRING);
	
	/**
	 * -w, OptionValueType.STRING
	 */
	OptionId<String> PASSWORD_OPTION_ID	= OptionId.create("w", OptionValueType.STRING);
	
	/**
	 * -p, OptionValueType.INTEGER
	 */
	OptionId<Integer> PORT_OPTION_ID	= OptionId.create("p", OptionValueType.INTEGER);
	
	/**
	 * @return the command name of the application
	 */
	String getName();
	
	/**
	 * @return the short description of this application
	 */
	String getShortDescription();
	
	/**
	 * @return the full description of this application
	 */
	String getLongDescription();
	
	/**
	 * @return the option descriptor Tree
	 */
	IOptionDescriptorTree getOptions();
}
