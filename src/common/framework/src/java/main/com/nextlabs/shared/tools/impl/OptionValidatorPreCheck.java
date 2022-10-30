package com.nextlabs.shared.tools.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorVisitor;
import com.nextlabs.shared.tools.IParsedOptions;
import com.nextlabs.shared.tools.IUniqueChoiceOptionDescriptor;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.OptionValueType.OptionValueTypeList;

/**
 * This class should be called by OptionValidator
 * create OptionValidatorNode that marked which node is existed and required
 * create a list of existed option.
 * check if different command line indicators in the same optionId appeared more than once
 * check if the required value is existed if the option is existed.
 * check if the value is correct valueType
 * 
 * after this, all node should marked as (not)existed, and 
 *
 * @author hchan
 * @date Apr 11, 2007
 */
class OptionValidatorPreCheck implements IOptionDescriptorVisitor {
	private final OptionValidatorNode	root;
	private final CommandLineArguments	arguments;
	private OptionValidatorNode 		currentGlobalVNode;
	private List<String>				errorLog;
	private IParsedOptions				existedOptions;

	/**
	 * the arguments context will be removed if the data is parsed.
	 * after the visit, the arguments should only left with unparsed data.
	 * @param root
	 * @param arguments
	 */
	public OptionValidatorPreCheck(OptionValidatorNode root, CommandLineArguments arguments) {
		this.root = root;
		this.currentGlobalVNode = this.root;
		this.arguments = arguments;
		errorLog = new ArrayList<String>();
		existedOptions = new ParsedOptions();
	}

	public List<String> getErrorLog() {
		return errorLog;
	}
	
	public boolean hasError(){
		return errorLog.size() != 0;
	}

	public IParsedOptions getExistedOptions() {
		return existedOptions;
	}

	public void visit(ICompoundOptionDescriptor compoundOptionDescriptor) {
		if (errorLog.size() != 0) {
			return;
		}

		OptionValidatorNode currentVNode = root.getNode(compoundOptionDescriptor);
		currentGlobalVNode = currentVNode;
		switch (compoundOptionDescriptor.getType()) {
			case SIMPLE:
				IOptionDescriptor<?> option =
					((SimpleCompoundOptionDescriptor) compoundOptionDescriptor).getOption();
				if (option != null) {
					option.accept(this);
					if (errorLog.size() != 0) {
						return;
					} 
				}
			break;
			case UNIQUE: {
				IUniqueChoiceOptionDescriptor identifiedCompound =
					(IUniqueChoiceOptionDescriptor) compoundOptionDescriptor;
				List<ICompoundOptionDescriptor> compoundList = identifiedCompound.getCompoundOptions();
				for (ICompoundOptionDescriptor subCompound : compoundList) {
					subCompound.accept(this);
					if (errorLog.size() != 0) {
						return;
					}
					OptionValidatorNode vNode = root.getNode(subCompound);
					if (vNode.isExist()) {
						if (currentVNode.isSelected()) {
							errorLog.add(ErrorMessageGenerator.getReachMaxSelected(
									CollectionUtils.asString(
											currentVNode.getSelectedOptionNames(), ",", "-"), 1)
							);
						}else{
							try {
								currentVNode.addSelected(subCompound);
							} catch (ParseException e) {
								errorLog.add(e.getMessage());
							}
						}
					}
				}

				if (currentVNode.isSelected()) {
					currentVNode.setExist();
				}
			}
			break;
			case MULTI: {
				IMultiChoiceOptionDescriptor identifiedCompound = 
					(IMultiChoiceOptionDescriptor) compoundOptionDescriptor;
				List<ICompoundOptionDescriptor> compoundList = identifiedCompound.getCompoundOptions();
				for (ICompoundOptionDescriptor subCompound : compoundList) {
					subCompound.accept(this);
					if (errorLog.size() != 0) {
						return;
					}
					OptionValidatorNode vNode = root.getNode(subCompound);
					if (vNode.isExist()) {
						if (currentVNode.getNumSelected() >= identifiedCompound.getMaxinumNumChoice()) {
							errorLog.add(ErrorMessageGenerator.getReachMaxSelected(
									CollectionUtils.asString(
											currentVNode.getSelectedOptionNames(), ",", "-"), 
									identifiedCompound.getMaxinumNumChoice())
							);
						}else{
							try {
								currentVNode.addSelected(subCompound);
							} catch (ParseException e) {
								errorLog.add(e.getMessage());
							}
						}
					}
				}
				
				//if more than zero, the compound exists, we change the min/max range in different class
				if (currentVNode.isSelected()) {
					currentVNode.setExist();
				}
			}
			break;
			case SEQUENCE: {
				List<ICompoundOptionDescriptor> compoundList = 
					compoundOptionDescriptor.getCompoundOptions();
				for (ICompoundOptionDescriptor descriptor : compoundList) {
					descriptor.accept(this);
					if (errorLog.size() != 0) {
						return;
					}
					if (root.getNode(descriptor).isExist()) {
						currentVNode.setExist();
					}
				}
			}
			break;
		}
	}

	//visit(IOptionDescriptor) needs to return a boolean to tell if the option exists
	public void visit(IOptionDescriptor<?> optionDescriptor) {
		OptionId<?> optionId = optionDescriptor.getOptionId();
		List<String> optionValues = new LinkedList<String>();
		boolean isCommandLineIndicatorFound = false;
		
		for (String commonLineIndicator : optionDescriptor.getCommandLineIndicators()) {
			String[] values;
			if ((values = arguments.popValues(commonLineIndicator)) != null) {
				isCommandLineIndicatorFound = true;
				Collections.addAll(optionValues, values);
			}
		}

		if (isCommandLineIndicatorFound) {
			if (optionValues == null || optionValues.size() == 0) {
				//no value from the command line
				
				if(optionDescriptor.isValueRequired()){
					errorLog.add(ErrorMessageGenerator.getValueRequired(optionId));
					return;
				}
				//}else{ goto no_problem_found }
			}else{
				if(optionDescriptor.getNumPossibleValues() == 0 ){
					errorLog.add(ErrorMessageGenerator.getNoValues(optionId));
					return;
				} else if(optionValues.size() > optionDescriptor.getNumPossibleValues()){
					errorLog.add(ErrorMessageGenerator.getReachMaxPossibleValues(optionId,
							optionValues.size(), optionDescriptor.getNumPossibleValues()));
					return;
				}
				
				//check each value is valid
				OptionValueType type = optionDescriptor.getValueType();
				for (String optionValue : optionValues) {
					if (!type.isValid(optionId, optionValue)) {
						if (type instanceof OptionValueTypeList) {
                            Object[] possibleValues = ((OptionValueTypeList) type).getCustomList(optionId).toArray();
                            boolean willSort = true;
                            for (Object possibleValue : possibleValues) {
                                if (!(possibleValue instanceof Comparable)) {
                                    willSort = false;
                                    break;
                                }
                            }
                            if (willSort) {
                                Arrays.sort(possibleValues);
                            }
							String expected = ArrayUtils.asString(possibleValues, "/", "");
							errorLog.add(ErrorMessageGenerator.getValueNotInList(optionId,
									optionValue, expected));
						} else {
							errorLog.add(ErrorMessageGenerator.getInvalidValueType(optionId,
									optionValue, optionDescriptor.getValueType()));
						}		
					}
				}
				//check all values before return the error(s)
				if(errorLog.size() != 0 ){
					return;
				}
				
			}
			
			//reach here means no problem is found
			existedOptions.put(optionDescriptor, optionValues.toArray(new String[optionValues
					.size()]));
			currentGlobalVNode.setExist(optionId);
		} else {
			//default is already false
			//currentGlobalVNode.setExist(false);
		}
	}
}
