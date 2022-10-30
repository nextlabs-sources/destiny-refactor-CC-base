/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import java.util.Collection;
import java.util.List;

import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * @author hchan
 * @date Mar 29, 2007
 */
public class SynopisUsagePrinter extends UsagePrinterBase {
	private StringBuffer singleLine = new StringBuffer();
	
//	private char currentComponundParentType	= ICompoundOptionDescriptor.UNKNOWN;
	
	private boolean dontAddBracket = false;
	
	private ICompoundOptionDescriptor root;

	public SynopisUsagePrinter() {
		this(ConsoleDisplayHelper.getScreenWidth(), ConsoleDisplayHelper.NEWLINE, true);
	}

	SynopisUsagePrinter(int width, String lineBreaker, boolean isQuiet) {
		super(width, lineBreaker, isQuiet);
	}

	@Override
	public void renderUsage(IConsoleApplicationDescriptor consoleApplicationDescritpor) {
		root = OptionDescriptorTreeImpl.getRealRootOption(consoleApplicationDescritpor.getOptions().getRootOption());
		
		singleLine.append(consoleApplicationDescritpor.getName()).append(" ");
		if (root.getType() == ICompoundOptionDescriptor.OptionDescriptorType.UNIQUE
				|| root.getType() == ICompoundOptionDescriptor.OptionDescriptorType.MULTI) {
			dontAddBracket = true;
		} else {
			dontAddBracket = false;
		}
		root.accept(this);
		getCache().add(singleLine.toString());
	}
	

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorVisitor#visit(com.nextlabs.shared.tools.IOptionDescriptor)
	 */
	public void visit(IOptionDescriptor<?> optionDescriptor) {
		StringBuilder output = new StringBuilder();

		Collection<String> commonLineIndicators = optionDescriptor.getCommandLineIndicators();
		output.append(ArrayUtils.asString(CollectionUtils.toStringArray(commonLineIndicators), ",", "-"));

		if (optionDescriptor.getNumPossibleValues() > 0) {
			String valueLabel = optionDescriptor.getValueLabel();
			if (valueLabel != null) {
				output.append(" ").append(VALUE_OPEN).append(valueLabel).append(VALUE_CLOSE);
			}
		}

		singleLine.append(output);
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorVisitor#visit(com.nextlabs.shared.tools.IComponentOptionDescriptor)
	 */
	public void visit(ICompoundOptionDescriptor compoundOptionDescriptor) {
		if (!compoundOptionDescriptor.isRequired()){
			if(!dontAddBracket){
				singleLine.append(OPTIONAL_OPEN);
			}
		}

		switch (compoundOptionDescriptor.getType()) {
		case SIMPLE:
			IOptionDescriptor<?> option = ((SimpleCompoundOptionDescriptor) compoundOptionDescriptor).getOption();
			if (option != null) {
				option.accept(this);
			}
			break;
		case MULTI:
		case UNIQUE: {
			List<ICompoundOptionDescriptor> uniquelist = compoundOptionDescriptor
					.getCompoundOptions();

			if (compoundOptionDescriptor != root) {
				singleLine.append(UNIQUE_OPEN);
			}

			boolean previousValue = dontAddBracket;
			dontAddBracket = true;
			int length = uniquelist.size();
			for (int i = 0; i < length; i++) {
				uniquelist.get(i).accept(this);
				if (i != length - 1) {
					singleLine.append(" " + UNIQUE_DIVIDER).append(" ");
				}
			}

			if (compoundOptionDescriptor != root) {
				singleLine.append(UNIQUE_CLOSE).append("");
			}
			dontAddBracket = previousValue;
			break;
		}
		case SEQUENCE: {
			List<ICompoundOptionDescriptor> sequenceList = compoundOptionDescriptor
					.getCompoundOptions();
			
			boolean previousValue = dontAddBracket;
			dontAddBracket = false;
			int length = sequenceList.size();
			for (int i = 0; i < length; i++) {
				sequenceList.get(i).accept(this);
				if (i != length - 1) {
					singleLine.append(" ");
				}
			}
		
			dontAddBracket = previousValue;
			break;
		}
		
		//end switch
		}

		if (!compoundOptionDescriptor.isRequired()) {
			if(!dontAddBracket){
				singleLine.append(OPTIONAL_CLOSE).append("");
			}
		}
	}
}
