package com.nextlabs.shared.tools;

import java.util.Collection;

/**
 * @author hchan
 * @date Mar 27, 2007
 */
public interface IOptionDescriptor<T>{
	int UNLIMITED_NUM_POSSIBLE_VALUES = -329;

	OptionId<T> getOptionId();
	
	/**
	 * return option id, all the option id is unique.
	 * @return option id
	 */
	String getName();
	
	/**
	 * return command line indicators, all indicators is unique
	 * @return command line indicators
	 */
	Collection<String> getCommandLineIndicators();
	
	/**
	 * @see Require
	 * @return
	 */
	boolean isRequired();
	
	/**
	 * return true if this option requires a value.
	 * @return true if value required, false otherwise
	 */
	boolean isValueRequired();
	
	/**
	 * return the label of the value, required if <code>isValueRequired()</code> return true;
	 * @return
	 */
	String getValueLabel();
	
	/**
	 * description of this option
	 * @return
	 */
	String getDescription();
	
	/**
	 * The default value of this option
	 * @return
	 */
	T getDefaultValue();
	
	/**
	 * return the type of the value
	 * @return
	 */
	OptionValueType<T> getValueType();
	
	void accept(IOptionDescriptorVisitor vistor);
	
	/**
	 * number of possible option value
	 * @return
	 */
	int getNumPossibleValues();
	
	ICompoundOptionDescriptor getParent();

	void setParent(ICompoundOptionDescriptor parent);
}
