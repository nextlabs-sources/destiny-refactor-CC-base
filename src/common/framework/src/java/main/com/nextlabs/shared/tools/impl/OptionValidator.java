/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IMultiChoiceOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorVisitor;
import com.nextlabs.shared.tools.IParsedOptions;
import com.nextlabs.shared.tools.IUniqueChoiceOptionDescriptor;
import com.nextlabs.shared.tools.IUsageRenderer;
import com.nextlabs.shared.tools.OptionId;

/**
 * Validate the parsed options match OptionDescriptor
 * 
 * @author hchan
 * @date Mar 28, 2007
 */
public class OptionValidator implements IUsageRenderer, IOptionDescriptorVisitor {
	private CommandLineArguments	arguments;
	private List<String>			errorLog;
	private boolean					stopAtUnknown;
	private IParsedOptions			existedOptions;
	private OptionValidatorNode		validatorRoot;

	/**
	 * @param commandLine
	 * @param stopAtUnknown	validation will stop if there is unknown arguments
	 */
	public OptionValidator(CommandLineArguments arguments, boolean stopAtUnknown) {
		super();
		this.arguments = arguments;
		this.stopAtUnknown = stopAtUnknown;
		errorLog = new ArrayList<String>();
	}

	public List<String> getErrorLog() {
		return errorLog;
	}

	public boolean hasError() {
		return errorLog.size() != 0;
	}

	/**
	 * @return all known parsed options
	 */
	public IParsedOptions getExistedOptions() {
		return existedOptions;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IUsageRenderer#renderUsage(com.nextlabs.shared.tools.IConsoleApplicationDescriptor)
	 */
	public void renderUsage(IConsoleApplicationDescriptor descriptor) {
		errorLog.clear();
		//run pre-check
		ICompoundOptionDescriptor root = descriptor.getOptions().getRootOption();
		validatorRoot = new OptionValidatorNode(root);
		CommandLineArguments argumentsClone = (CommandLineArguments)this.arguments.clone();
		OptionValidatorPreCheck preCheck = new OptionValidatorPreCheck(validatorRoot, argumentsClone);
		root.accept(preCheck);
		this.existedOptions = preCheck.getExistedOptions();
		this.errorLog = preCheck.getErrorLog();
		if (errorLog.size() > 0) {
			return;
		}
		
		Set<String> leftover = argumentsClone.getAllIndicators();

		if(leftover.size() > 0 && stopAtUnknown){
			errorLog.add(ErrorMessageGenerator.getUnknownArguments(CollectionUtils.asString(
					leftover, ",")));
		}else{
			OptionTrigger.markAll(validatorRoot, existedOptions);
			root.accept(this);
		}
	}

	public void visit(ICompoundOptionDescriptor compoundOptionDescriptor) {
		OptionValidatorNode currentVNode = validatorRoot.getNode(compoundOptionDescriptor);
		
		if(!currentVNode.isExist() && !currentVNode.isRequired() ){
			currentVNode.setAllChildrenRequired(false);
		}
		
		switch (compoundOptionDescriptor.getType()) {
		case SIMPLE:
			if (currentVNode.isRequired() && !currentVNode.isExist()) {
				OptionId<?> optionId = ((SimpleCompoundOptionDescriptor)compoundOptionDescriptor).getOption().getOptionId();
				errorLog.add(ErrorMessageGenerator.getOptionRequired(optionId));
			}
			break;
		case UNIQUE:
			IUniqueChoiceOptionDescriptor uniqueComponund = (IUniqueChoiceOptionDescriptor) compoundOptionDescriptor;
			List<ICompoundOptionDescriptor> uniqueList = uniqueComponund.getCompoundOptions();
			for (ICompoundOptionDescriptor subDescriptor : uniqueList) {
				OptionValidatorNode vNode = validatorRoot.getNode(subDescriptor);
				
				Collection<ICompoundOptionDescriptor> selectedCompounts = currentVNode.getSelectedOptions();
				
				ICompoundOptionDescriptor selectedCompount =
						selectedCompounts.size() == 1 
						? selectedCompounts.iterator().next() 
						: null;
				
				if (selectedCompount == subDescriptor) {
					vNode.setRequired(currentVNode.isRequired() && vNode.isRequired());
				} else {
					vNode.setAllChildrenRequired(false);
				}
				subDescriptor.accept(this);
			}
			
			if (currentVNode.isRequired() && !currentVNode.isExist()) {
				errorLog.add(ErrorMessageGenerator
						.getRequireSelectMin(currentVNode.getName(), 1));
			}
			break;
		case MULTI:
			IMultiChoiceOptionDescriptor multiComponund = (IMultiChoiceOptionDescriptor) compoundOptionDescriptor;
			List<ICompoundOptionDescriptor> multiList = multiComponund.getCompoundOptions();
			for (ICompoundOptionDescriptor subDescriptor : multiList) {
				OptionValidatorNode vNode = validatorRoot.getNode(subDescriptor);
				if (currentVNode.getSelectedOptions().contains(subDescriptor)) {
					vNode.setRequired(currentVNode.isRequired() && vNode.isRequired());
				} else {
					vNode.setAllChildrenRequired(false);
				}
				subDescriptor.accept(this);
			}

			if (currentVNode.getNumSelected() < multiComponund.getMininumNumChoice()
					&& currentVNode.isRequired()) {
				errorLog.add(ErrorMessageGenerator.getRequireSelectMin(currentVNode.getName(),
						multiComponund.getMininumNumChoice()));
				return;
			}
			break;
		case SEQUENCE:
			List<ICompoundOptionDescriptor> sequenceList = compoundOptionDescriptor.getCompoundOptions();
			for (ICompoundOptionDescriptor subDescriptor : sequenceList) {
				subDescriptor.accept(this);
			}
			break;
		}
	}

	public void visit( IOptionDescriptor<?> optionDescriptor) {
		//do nothing
	}
}
