/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorVisitor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.OptionValueType.OptionValueNoValueLabel;
import com.nextlabs.shared.tools.OptionValueType.OptionValueTypeFile;
import com.nextlabs.shared.tools.OptionValueType.OptionValueTypeList;

/**
 * @author hchan
 * @date Mar 28, 2007
 */
public class Option<T> implements IOptionDescriptor<T> {
	private final Collection<String>	commandLineIndicators;
	private final T						defaultValue;
	private final String				description;
	private final OptionId<T>			optionId;
	private final String				valueLabel;
	private final boolean				required;
	private final boolean				valueRequired;
	private final int					numPossibleValues;
	
	//this if the option importance level, lower is more important
	private byte level = 0;
	private ICompoundOptionDescriptor parent;
	
	private static Set<String> allCommandLineIndicator = new TreeSet<String>();
	private static Set<OptionId<?>> allOptionid = new TreeSet<OptionId<?>>();
	
	public static final Option<Boolean> HELP_OPTION ;
	static{
		try {
			HELP_OPTION = Option.createOnOffOption(
					IConsoleApplicationDescriptor.HELP_OPTION_ID,
					"Display help page");
		} catch (InvalidOptionDescriptorException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	//full feature constructor
	public Option(OptionId<T> optionId, 
			Collection<String> commandLineIndicators, 
			String description, 
			boolean required, 
			String valueLabel,
			T defaultValue, 
			boolean valueRequired,
			int numPossibleValues
			) throws InvalidOptionDescriptorException{
		super();
		this.commandLineIndicators = commandLineIndicators;
		this.defaultValue = 		defaultValue;
		this.description = 			description;
		this.optionId = 			optionId;
		this.valueLabel = 			valueLabel;
		this.required = 			required;
		this.valueRequired = 		valueRequired;
		this.numPossibleValues = 	numPossibleValues;
		
		
		//if the valueType is a user-defined list such as custom list.
		// the default value may not in the list now
		// the check will fail.
		// so the default value will add to the user define list automatically.
		
		OptionValueType<T> valueType = optionId != null ? optionId.getValueType() : null;
		if(valueType != null && valueType instanceof OptionValueTypeList && defaultValue != null){
			((OptionValueTypeList)valueType).addCustomValue(optionId, defaultValue);
		}
		
		check();
		
		if (optionId != null)
			optionId.setOption(this);
	}
	
	public Option(OptionId<T> optionId, 
			String description,
			boolean required, 
			String valueLabel,
			T defaultValue,
			boolean valueRequired,
			int numPossibleValues
			) throws InvalidOptionDescriptorException {
		this(optionId,
				singleton(optionId.getName()), 
				description,
				required,
				valueLabel,
				defaultValue, 
				valueRequired, 
				numPossibleValues);
	}
	
	/**
	 * create an option that is not required
	 * if the option exists, it will have exactly one value
	 * 
	 */
	public static <T> Option<T> createOption(
			OptionId<T> optionId, 
			String description, 
			String valueLabel, 
			T defaultValue
			) throws InvalidOptionDescriptorException {
		return new Option<T>(
				optionId, 		// OptionId<T> optionId, 
				description, 	// String description,
				false,			// boolean required, 
				valueLabel,		// String valueLabel,
				defaultValue , 	// T defaultValue,
				true,			// boolean valueRequired
				1);				// numPossibleValues
	}
	
	/**
	 * create an option that is required and has exactly one value
	 * 
	 */
	public static <T> Option<T> createOption(
			OptionId<T> optionId, 
			String description, 
			String valueLabel
			) throws InvalidOptionDescriptorException {
		return new Option<T>(
				optionId, 		// OptionId<T> optionId, 
				description, 	// String description,
				true,			// boolean required, 
				valueLabel,		// String valueLabel,
				null, 		// T defaultValue,
				true,			// boolean valueRequired
				1);				// numPossibleValues
	}
	
	/**
	 * create an option that is for OptionValueType.ON_OFF
	 * it doesn't take values
	 * And the option is not required.
	 * The default value is false.
	 * 
	 * @param optionId
	 * @param description
	 * @return
	 * @throws InvalidOptionDescriptorException
	 */
	public static Option<Boolean> createOnOffOption(
			OptionId<Boolean> optionId, 
			String description
			) throws InvalidOptionDescriptorException {
		return new Option<Boolean>(
				optionId, 		// OptionId<T> optionId, 
				description, 	// String description,
				false,			// boolean required, 
				null,			// String valueLabel,
				false, 			// T defaultValue,
				false,			// boolean valueRequired
				0);				// numPossibleValues
	}
	
	
	
	/**
     * Returns an mutable set containing only the specified object.
     * This is different than Collections.singleton which the return is immutable
     *
     * @param o the sole object to be stored in the returned set.
     * @return an mutable set containing only the specified object.
     */
	private static Set<String> singleton(String o){
		Set<String> returnSet = new TreeSet<String>();
		returnSet.add(o);
		return returnSet;
	}
	
	private void check() throws InvalidOptionDescriptorException{
		final OptionValueType<T> valueType;
		
		//optionId is the most important thing, check it first
		if (optionId == null 
				|| optionId.getName() == null
				|| (valueType = optionId.getValueType()) == null) {
			throw new InvalidOptionDescriptorException(ErrorMessageGenerator.getNullOptionId());
		}
		
		if (allOptionid.contains(optionId)) {
			throw new InvalidOptionDescriptorException(ErrorMessageGenerator
					.getDuplicatedOptionId(optionId));
		}

		if (commandLineIndicators == null || commandLineIndicators.size() == 0) {
			throw new InvalidOptionDescriptorException(ErrorMessageGenerator
					.getEmptyCommandlineIndicators(optionId));
		}
		
		for (String commandLineIndicator : commandLineIndicators) {
			if (allCommandLineIndicator.contains(commandLineIndicator)) {
				throw new InvalidOptionDescriptorException(ErrorMessageGenerator
						.getCreateDuplicatedCommandIndicator(optionId, commandLineIndicator));
			}
		}
		
		if (description == null) {
			throw new InvalidOptionDescriptorException(ErrorMessageGenerator
					.getNullDescription(optionId));
		}

		if (numPossibleValues < 0 && numPossibleValues != UNLIMITED_NUM_POSSIBLE_VALUES) {
			throw new InvalidOptionDescriptorException(ErrorMessageGenerator
					.getNegativePossibleValues());
		}

		if(valueType instanceof OptionValueNoValueLabel){
			if (valueType == OptionValueType.ON_OFF) {
				if ( numPossibleValues != 0) {
					throw new InvalidOptionDescriptorException("OptionValueType.ON_OFF can't have any values");
				}
			}
			//the other type don't even check
		} else {
			if (numPossibleValues != 0 && (valueLabel == null || valueLabel.length() == 0)) {
				throw new InvalidOptionDescriptorException(ErrorMessageGenerator
						.getMissingValueLabel(optionId));
			}
		}

		if (valueRequired && numPossibleValues == 0) {
			throw new InvalidOptionDescriptorException(ErrorMessageGenerator
					.getContradictRequiredZeroPossibleValues(optionId));
		}
		
		if (valueType == null) {
			throw new NullPointerException("Value Type is null");
		} else if (defaultValue != null) {
			if (valueType instanceof OptionValueTypeFile) {
				//TODO check the default value for those type
			} else {
				if (!valueType.isValid(optionId, defaultValue)) {
					throw new InvalidOptionDescriptorException(ErrorMessageGenerator
							.getInvalidDefaultValueType(optionId, defaultValue, valueType));
				}
			}
		}
		
		//if everything alright
		for(String commandLineIndicator : commandLineIndicators ){
			allCommandLineIndicator.add(commandLineIndicator);
		}
		allOptionid.add(optionId);
	}
	
	/**
	 * reset the history of commandLineIndicator and optionId.
	 * This should only be called in unit test and no one else.
	 */
	static void reset(){
		allCommandLineIndicator.clear();
		allOptionid.clear();
	}
	
	/**
	 * return true if the value match the valueType
	 * @param obj
	 * @return
	 */
	boolean checkValueType(Object obj){
		return optionId.getValueType().isValid(optionId, obj);
	}
	
	public void accept(IOptionDescriptorVisitor vistor) {
		vistor.visit(this);
	}

	public Collection<String> getCommandLineIndicators() {
		return commandLineIndicators;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public String getDescription() {
		return description;
	}
	
	public OptionId<T> getOptionId() {
		return optionId;
	}

	public String getName() {
		return optionId.getName();
	}

	public String getValueLabel() {
		return valueLabel;
	}

	public OptionValueType<T> getValueType() {
		return optionId.getValueType();
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isValueRequired() {
		return valueRequired;
	}

	public int getNumPossibleValues() {
		return numPossibleValues;
	}

	public final ICompoundOptionDescriptor getParent() {
		return this.parent;
	}

	public final void setParent(ICompoundOptionDescriptor parent) {
		this.parent = parent;
	}

	/**
	 * 
	 * @return priority, lower is more important
	 */
	public byte getLevel() {
		return this.level;
	}

	/**
	 * 
	 * @param priority lower is more important
	 */
	public void setLevel(byte priority) {
		this.level = priority;
	}
}
