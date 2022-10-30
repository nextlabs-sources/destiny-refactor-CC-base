/*
 * Created on Oct 8, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.shared.tools.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

//import com.bluejungle.destiny.tools.enrollment.EnrollmentMgrOptionDescriptorEnum;
import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorVisitor;
import com.nextlabs.shared.tools.IUniqueChoiceOptionDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionDescriptorNotFoundException;
import com.nextlabs.shared.tools.ParseException;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/nextlabs/shared/tools/impl/InteractiveMenu.java#1 $
 */

public class InteractiveMenu {
	private static final String STRING_FORMAT_MENU_ITEM = "%1$d. %2$s%n";
	
	private final IConsoleApplicationDescriptor consoleDecriptor;
	private ICompoundOptionDescriptor root;
	private OptionValidatorNode validatorRoot;

	private InputFromInteractiveMenu inputs;

	public static void main(String[] args) throws InvalidOptionDescriptorException, IOException {
//		IConsoleApplicationDescriptor consoleDecriptor = new EnrollmentMgrOptionDescriptorEnum();
//		InteractiveMenu interactiveMenu = new InteractiveMenu(consoleDecriptor);
//		interactiveMenu.start();
	}

	public InteractiveMenu(final IConsoleApplicationDescriptor consoleDecriptor) {
		this.consoleDecriptor = consoleDecriptor;
		root = OptionDescriptorTreeImpl.getRealRootOption(consoleDecriptor.getOptions().getRootOption());
		validatorRoot = new OptionValidatorNode(root);
		validatorRoot.setAllChildrenRequired(true);
		inputs = new InputFromInteractiveMenu();
	}

	public void start() throws IOException {
		System.out.println("Welcome to " + consoleDecriptor.getName());
		execute(root);
		System.out.println("done");
	}
	
	private void execute(ICompoundOptionDescriptor compound) throws IOException {
		List<IOptionDescriptor> allPossible;
		do {
			int numOfRequiredOption = 0;
			//get all possible option
			allPossible = getAllPossible(compound);

			//only display the the highest priority one
			//TODO maybe display all but sorted with the priority
			allPossible = filterOnlyMostImportant(allPossible);

			//build the menu and put the number in front;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < allPossible.size(); i++) {
				String name;
				if (allPossible.get(i).isRequired()) {
					name =  allPossible.get(i).getName();
					numOfRequiredOption++;
				} else {
					name =  UsagePrinterBase.OPTIONAL_OPEN + allPossible.get(i).getName() + UsagePrinterBase.OPTIONAL_CLOSE;
				}
				sb.append(String.format(STRING_FORMAT_MENU_ITEM, i, name));
			}
			
			
			sb.append(ConsoleDisplayHelper.NEWLINE);

			IOptionDescriptor option;
			if (allPossible.size() == 1) {
				option = allPossible.iterator().next();
				System.out.println("Auto selected option: " + option.getName());
			} else {
				//prompt the option
				String selectOption = InteractiveQuestion.prompt(sb.toString());
				if (selectOption == null || selectOption.trim().length() == 0) {
					if (numOfRequiredOption == 0) {
						break;
					} else {
						continue;
					}
				}

				try {
					option = consoleDecriptor.getOptions().getOption(selectOption);
				} catch (OptionDescriptorNotFoundException e) {
					System.err.println("Option \"" + selectOption + "\" is unknown.");
					continue;
				}
			}
			
//			System.out.println(option.getDescription());
			System.out.println("  " + option.getDescription());
			if(option.getValueLabel() != null){
				System.out.println("  Value Label: " + option.getValueLabel());
			}

			if (option.getNumPossibleValues() > 0) {
				List<Object> savedInputValues = new ArrayList<Object>();
				String inputValue;
				if (option.getDefaultValue() != null) {
					inputValue = InteractiveQuestion.prompt("Please input values, enter to use default value: "
									+ option.getDefaultValue());
					if (inputValue.trim().length() == 0) {
						savedInputValues.add(option.getDefaultValue());
					}
				} else {
					inputValue = InteractiveQuestion.prompt("Please input values, enter to skip");
				}

				while ((inputValue == null || inputValue.trim().length() == 0) && option.isRequired()
						&& option.isValueRequired()) {
					inputValue = InteractiveQuestion.prompt("Value is required.");
				}

				while (inputValue.trim().length() != 0) {
					savedInputValues.add(inputValue);
					inputValue = InteractiveQuestion
							.prompt("Please input next values, enter without input to next");
				}

				addInput(option.getName(), savedInputValues);
			} else {
				addInput(option.getName(), null);
			}
			//select the option
			select(option);
		} while (allPossible.size() > 0);

		StringBuilder sb = new StringBuilder();
		for (String key : inputs.keySet()) {
			sb.append(key);
			sb.append(" - ");
			if (inputs.get(key) != null) {
				for (Object value : inputs.get(key)) {
					sb.append(value);
					sb.append(", ");
				}

			}
			sb.append(ConsoleDisplayHelper.NEWLINE);
		}

		System.out.println(sb);
	}

	private class InputFromInteractiveMenu extends TreeMap<String, List<Object>> {
		public InputFromInteractiveMenu() {
			super();
		}
	}

	private void addInput(String optionid, List<Object> inputValues) {
		List<Object> savedInputValues = inputs.get(optionid);
		if (savedInputValues == null) {
			inputs.put(optionid, inputValues);
		} else {
			savedInputValues.addAll(inputValues);
			inputs.put(optionid, savedInputValues);
		}
	}

	private List<IOptionDescriptor> filterOnlyMostImportant(
			Collection<IOptionDescriptor> allPossibles) {
		byte highestPriority = Byte.MAX_VALUE;
		for (IOptionDescriptor possible : allPossibles) {
			Option option = (Option) possible;
			highestPriority = (byte) Math.min(highestPriority, option.getLevel());
		}

		List<IOptionDescriptor> highestOptions = new ArrayList<IOptionDescriptor>();

		for (IOptionDescriptor possible : allPossibles) {
			Option option = (Option) possible;
			if (option.getLevel() == highestPriority) {
				highestOptions.add(possible);
			}
		}

		return highestOptions;
	}

	private void select(IOptionDescriptor option) {
		validatorRoot.getNode(option).setExist();
		validatorRoot.getNode(option).setRequired(false);
		new MarkOptionExist().visit(root);
	}

//	private List<IOptionDescriptor> getAllPossible() {
//		OptionPossibleSearcher optionPossibleSearcher = new OptionPossibleSearcher();
//		optionPossibleSearcher.visit(root);
//		return optionPossibleSearcher.possible;
//	}
	
	private List<IOptionDescriptor> getAllPossible(ICompoundOptionDescriptor option) {
		OptionPossibleSearcher optionPossibleSearcher = new OptionPossibleSearcher();
		optionPossibleSearcher.visit(option);
		return optionPossibleSearcher.possible;

	}

	private class MarkOptionExist implements IOptionDescriptorVisitor {
		List<IOptionDescriptor> possible = new ArrayList<IOptionDescriptor>();

		public void visit(ICompoundOptionDescriptor compoundOptionDescriptor) {
			OptionValidatorNode currentVNode = validatorRoot.getNode(compoundOptionDescriptor);

			if (!currentVNode.isRequired()) {
				return;
			}

			switch (compoundOptionDescriptor.getType()) {
			case SIMPLE:
				break;
			case UNIQUE: {
				IUniqueChoiceOptionDescriptor identifiedCompound = (IUniqueChoiceOptionDescriptor) compoundOptionDescriptor;
				if (currentVNode.isSelected()) {
					return;
				}
				for (ICompoundOptionDescriptor subCompound : identifiedCompound.getCompoundOptions()) {
					visit(subCompound);
					OptionValidatorNode vNode = validatorRoot.getNode(subCompound);
					if (vNode.isExist()) {
						try {
							currentVNode.addSelected(subCompound);
						} catch (ParseException e) {
							throw new RuntimeException(e);
						}
						currentVNode.setExist();
						break;
					}
				}
				if (currentVNode.isSelected()) {
					for (ICompoundOptionDescriptor subCompound : identifiedCompound.getCompoundOptions()) {
						Collection<ICompoundOptionDescriptor> selected = currentVNode.getSelectedOptions();
						
						if (selected.size() == 1 && selected.iterator().next() != subCompound) {
							OptionValidatorNode vNode = validatorRoot.getNode(subCompound);
							vNode.setAllChildrenRequired(false);
						}
					}
				}
			}
				break;
			case MULTI: {
				IMultiChoiceOptionDescriptor identifiedCompound = (IMultiChoiceOptionDescriptor) compoundOptionDescriptor;
				List<ICompoundOptionDescriptor> compoundList = identifiedCompound.getCompoundOptions();
				for (ICompoundOptionDescriptor subCompound : compoundList) {
					visit(subCompound);
					OptionValidatorNode vNode = validatorRoot.getNode(subCompound);
					if (vNode.isExist()) {
						try {
							currentVNode.addSelected(subCompound);
						} catch (ParseException e) {
							throw new RuntimeException(e);
						}
					}
				}
				if (currentVNode.getNumSelected() > 0) {
					currentVNode.setExist();
					currentVNode.setAllChildrenRequired(false);
				}
			}
				break;
			case SEQUENCE:
				boolean allNotRequired = true;
				for (ICompoundOptionDescriptor subCompound : compoundOptionDescriptor.getCompoundOptions()) {
					visit(subCompound);
					OptionValidatorNode vNode = validatorRoot.getNode(subCompound);
					if (vNode.isExist()) {
						currentVNode.setExist();
					}
					if (vNode.isRequired()) {
						allNotRequired = false;
					}
				}
				if (allNotRequired) {
					currentVNode.setRequired(false);
				}
				break;
			}
		}

		public void visit(IOptionDescriptor optionDescriptor) {
			//do nothing
		}

		public List<IOptionDescriptor> getPossible() {
			return this.possible;
		}
	}

	private class OptionPossibleSearcher implements IOptionDescriptorVisitor {
		List<IOptionDescriptor> possible = new ArrayList<IOptionDescriptor>();

		public void visit(ICompoundOptionDescriptor compoundOptionDescriptor) {
			OptionValidatorNode currentVNode = validatorRoot.getNode(compoundOptionDescriptor);

			if (!currentVNode.isRequired()) {
				return;
			}
			switch (compoundOptionDescriptor.getType()) {
			case SIMPLE:
				possible.add(((SimpleCompoundOptionDescriptor) currentVNode.getCurrentNode()).getOption());
				break;
			case UNIQUE:
			case MULTI:
			case SEQUENCE:
				for (ICompoundOptionDescriptor c : compoundOptionDescriptor.getCompoundOptions()) {
					visit(c);
				}
				break;
			}
		}

		public void visit(IOptionDescriptor optionDescriptor) {
			//do nothing
		}
	}
}
