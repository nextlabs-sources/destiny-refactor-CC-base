package com.nextlabs.shared.tools.impl;

/**
 * All error message
 * You need to replace the keyword(s) in each message to something meaningful.
 * All keyword starts with "KEY_"
 *
 * @author hchan
 * @date Apr 12, 2007
 */
interface ErrorMessage {
	// keywords
	String	KEY_OPTION_ID		= "%OPTION_ID%";
	String	KEY_CMD_INDICATOR	= "%CMD_INDICATOR%";
	String	KEY_VALUE_TYPE		= "%VALUE_TYPE%";
	String	KEY_VALUE			= "%VALUE%";
	String	KEY_NAME			= "%NAME%";
	String	KEY_MIN				= "%MIN%";
	String	KEY_MAX				= "%MAX%";
	String	KEY_EXPECTED		= "%EXPECTED%";
	
	
	//parsing
	//remove on next release
	String	DUPLICATED_COMMAND_INDICATOR_IN_ARGUMENTS = "Different command Indicator with same parameter -"	+ KEY_OPTION_ID + " occured.";

	//if the value required and missed in the arguments, the GNUParser is catching it. The error message is different.
	String VALUE_REQUIRED = "The required value for the -"+KEY_OPTION_ID+" parameter is not found."; 
	
	String INVALID_VALUE_TYPE	= "The value "+KEY_VALUE+" is an incorrect data type. The -"+KEY_OPTION_ID+" requires a "+KEY_VALUE_TYPE+" value.";
	String VALUE_NOT_IN_LIST	= "The option you have selected -"+KEY_OPTION_ID+", " +KEY_VALUE+", is not valid. Please select a value from the following: ("+KEY_EXPECTED+")";
	
	String UNKNOWN_ARGUMENTS = "unknown args " + KEY_CMD_INDICATOR;
	
	String REACH_MAX_POSSIBLE_VALUES =  "The -"+KEY_OPTION_ID + " parameter has reached its maximum value of " + KEY_MAX +". You have " +KEY_VALUE + " values.";
	String NO_VALUES = "The -"+KEY_OPTION_ID + " parameter cannot have any values.";
	
	//create
	String EMPTY_COMMANDLINE_INDICATORS = "The -"+KEY_OPTION_ID+" parameter cannot have any empty commandLine Indicators.";
	String CREATE_DUPLICATED_COMMAND_INDICATOR = "commandLineIndicator \"" + KEY_CMD_INDICATOR+ "\" of the -" + KEY_OPTION_ID + " parameter already exists.";
	
	String NEGATIVE_POSSIBLE_VALUES = "The number of possible values cannot be negative.";
	String NULL_OPTION_ID = "The optionId is null or incomplete.";
	String NULL_DESCRIPTION = "You must provide a description for the -"+KEY_OPTION_ID+" parameter.";
	String DUPLICATED_OPTION_ID = "The -" + KEY_OPTION_ID + " parameter has already been defined.";
	String MISSING_VALUE_LABEL = "The -"+KEY_OPTION_ID+" parameter requires both a value and a value label.";
	
	String CONTRADICT_REQUIRED_ZERO_POSSIBLE_VALUES = "The -"+KEY_OPTION_ID+" parameter has required zero numPossibleValues. Change to !requried or set >=1 numPossibleValues.";
	
	String INVALID_DEFAULT_VALUE_TYPE = "The value of the -"+KEY_OPTION_ID+" parameter," +KEY_VALUE+", is the wrong data type. Please supply a "+KEY_VALUE_TYPE +" value.";
	
	String OPTION_REQUIRED = "The -"+KEY_OPTION_ID+" parameter is required.";
	
	
	//multi choice
	String REQUIRE_SELECT_MIN = "You must select at least "+ KEY_MIN + " compound(s) from the group " + KEY_NAME + ".";
	String REACH_MAX_SELECTED =  KEY_NAME + " cannot be used together. You can only select a maximum of " + KEY_MAX + " group(s)";
	String SELECT_UNKNOWN = "The option you have selected, ("+ KEY_NAME +"), is not valid for the " + KEY_NAME + " property.";

	//runtime
	String UNKNOWN_OPTIONID = "Unknown optionId: " + KEY_OPTION_ID;
	
}
