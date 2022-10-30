/*
 * Created on Nov 4, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.impl;

import static com.nextlabs.shared.tools.impl.ErrorMessage.*;

import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/impl/ErrorMessageGenerator.java#1 $
 */

public class ErrorMessageGenerator {
	public static String getValueRequired(OptionId<?> optionId){
		return VALUE_REQUIRED.replace(KEY_OPTION_ID, optionId.getName());
	}
	
	public static String getInvalidValueType(OptionId<?> optionId, String value,
			OptionValueType<?> valueType) {
		return INVALID_VALUE_TYPE
				.replace(KEY_OPTION_ID, optionId.getName())
				.replace(KEY_VALUE, value)
				.replace(KEY_VALUE_TYPE, valueType.toString());
	}
	
	
	public static String getValueNotInList(OptionId<?> optionId, String value, String expected) {
		return VALUE_NOT_IN_LIST
				.replace(KEY_OPTION_ID, optionId.getName())
				.replace(KEY_VALUE, value)
				.replace(KEY_EXPECTED, expected);
	}
		
	public static String getUnknownArguments(String cmdIndicator){
		return UNKNOWN_ARGUMENTS.replace(KEY_CMD_INDICATOR, cmdIndicator);
	}

	public static String getReachMaxPossibleValues(OptionId<?> optionId, int selected, int max){
		return REACH_MAX_POSSIBLE_VALUES
				.replace(KEY_OPTION_ID, optionId.getName())
				.replace(KEY_VALUE, Integer.toString(selected))
				.replace(KEY_MAX, Integer.toString(max));
	}
	
	public static String getNoValues(OptionId<?> optionId){
		return NO_VALUES.replace(KEY_OPTION_ID, optionId.getName());
	}
	
	//create
	
	public static String getEmptyCommandlineIndicators(OptionId<?> optionId){
		return EMPTY_COMMANDLINE_INDICATORS.replace(KEY_OPTION_ID, optionId.getName());
	}
	
	public static String getCreateDuplicatedCommandIndicator(OptionId<?> optionId, String cmdIndicator) {
		return CREATE_DUPLICATED_COMMAND_INDICATOR
				.replace(KEY_OPTION_ID, optionId.getName())
				.replace(KEY_CMD_INDICATOR, cmdIndicator);
	}
	
	
	public static String getNegativePossibleValues(){
		return NEGATIVE_POSSIBLE_VALUES;
	}
	
	public static String getNullOptionId(){
		return NULL_OPTION_ID;
	}
	
	public static String getNullDescription(OptionId<?> optionId) {
		return NULL_DESCRIPTION.replace(KEY_OPTION_ID, optionId.getName());
	}

	public static String getDuplicatedOptionId(OptionId<?> optionId) {
		return DUPLICATED_OPTION_ID.replace(KEY_OPTION_ID, optionId.getName());
	}

	public static String getMissingValueLabel(OptionId<?> optionId) {
		return MISSING_VALUE_LABEL.replace(KEY_OPTION_ID, optionId.getName());
	}

	public static String getContradictRequiredZeroPossibleValues(OptionId<?> optionId) {
		return CONTRADICT_REQUIRED_ZERO_POSSIBLE_VALUES.replace(KEY_OPTION_ID, optionId.getName());
	}

	public static String getInvalidDefaultValueType(OptionId<?> optionId, Object value,
			OptionValueType<?> valueType) {
		return INVALID_DEFAULT_VALUE_TYPE
				.replace(KEY_OPTION_ID, optionId.getName())
				.replace(KEY_VALUE, value.toString())
				.replace(KEY_VALUE_TYPE, valueType.toString());
	}

	public static String getOptionRequired(OptionId<?> optionId) {
		return OPTION_REQUIRED.replace(KEY_OPTION_ID, optionId.getName());
	}

	//multi choice
	public static String getRequireSelectMin(String name, int min) {
		return REQUIRE_SELECT_MIN.replace(KEY_NAME, name)
				.replace(KEY_MIN, Integer.toString(min));
	}

	public static String getReachMaxSelected(String name, int max) {
		return REACH_MAX_SELECTED.replace(KEY_NAME, name)
				.replace(KEY_MAX, Integer.toString(max));
	}

	// the order is important
	public static String getSelectUnknown(String name, String list ) {
		return SELECT_UNKNOWN.replace(KEY_NAME, name)
				.replace(KEY_NAME, list);
	}
	
	public static String getUnknownOptionid(OptionId<?> optionId) {
		return UNKNOWN_OPTIONID.replace(KEY_OPTION_ID, optionId.getName());
	}

}
