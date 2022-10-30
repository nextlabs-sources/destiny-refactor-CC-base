package com.nextlabs.shared.tools.impl;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorVisitor;
import com.nextlabs.shared.tools.IUsageRenderer;
import com.nextlabs.shared.tools.StringFormatter;

/**
 * print the structure of the options
 *
 * @author hchan
 * @date Apr 16, 2007
 */
public class OptionStructurePrinter implements IUsageRenderer, IOptionDescriptorVisitor {
	private int level;
	
	private enum TabStyle{MATCH_LAST_WORD, LIMITED_SPACE_8, LIMITED_SPACE_4};
	
	private final TabStyle tabStyle = TabStyle.LIMITED_SPACE_4;
	
	public static void print(IConsoleApplicationDescriptor descriptor){
		new OptionStructurePrinter().renderUsage(descriptor);
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IUsageRenderer#renderUsage(com.nextlabs.shared.tools.IConsoleApplicationDescriptor)
	 */
	public void renderUsage(IConsoleApplicationDescriptor descriptor) {
		ICompoundOptionDescriptor root = descriptor.getOptions().getRootOption();
		level = 0;
		root.accept(this);
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorVisitor#visit(com.nextlabs.shared.tools.ICompoundOptionDescriptor)
	 */
	public void visit(ICompoundOptionDescriptor compoundOptionDescriptor) {
		StringBuilder output = new StringBuilder();
		output.append(StringFormatter.repeat(' ', level));
		
		output.append(" - ");
		String compoundName;
		switch(compoundOptionDescriptor.getType()){
			case MULTI:	
				MultiChoiceOptionDescriptor multi = (MultiChoiceOptionDescriptor) compoundOptionDescriptor;
				compoundName = "Multi" + UsagePrinterBase.VALUE_OPEN + multi.getMininumNumChoice()
					+ "-" + multi.getMaxinumNumChoice() + UsagePrinterBase.VALUE_CLOSE;
			break;
			case UNIQUE:
				compoundName = "Unique";
				break;
			case SEQUENCE:
				compoundName = "Sequence";
				break;
			case SIMPLE:
				compoundName = "Simple";
				break;
			default:
				compoundName = "";
		}
		if (!compoundOptionDescriptor.isRequired()) {
			compoundName = UsagePrinterBase.OPTIONAL_OPEN + compoundName
					+ UsagePrinterBase.OPTIONAL_CLOSE;
		}
		output.append(compoundName);
		
		int oldLevel = level;
		switch (tabStyle) {
		case MATCH_LAST_WORD:
			level = output.length();
			break;
		case LIMITED_SPACE_8:
			level += 8;
			break;
		case LIMITED_SPACE_4:
			level += 4;
			break;
		}
		
		
		if(compoundOptionDescriptor.getType() == ICompoundOptionDescriptor.OptionDescriptorType.SIMPLE){
			IOptionDescriptor<?> option = ((SimpleCompoundOptionDescriptor)compoundOptionDescriptor).getOption();
			System.out.print(output);
			option.accept(this);
		}else{
			System.out.println(output);
			for(ICompoundOptionDescriptor children : compoundOptionDescriptor.getCompoundOptions()){
				children.accept(this);
			}
		}
		level = oldLevel;
		
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorVisitor#visit(com.nextlabs.shared.tools.IOptionDescriptor)
	 */
	public void visit(IOptionDescriptor<?> optionDescriptor) {
		StringBuilder output = new StringBuilder();

		output.append(" # ");
		String name = optionDescriptor.getName();
		if(!optionDescriptor.isRequired()){
			name = UsagePrinterBase.OPTIONAL_OPEN + name + UsagePrinterBase.OPTIONAL_CLOSE;
		}
		output.append(name);
		
		if (optionDescriptor.getNumPossibleValues() > 0) {
			output.append(" " + UsagePrinterBase.VALUE_OPEN + optionDescriptor.getValueLabel()
					+ UsagePrinterBase.VALUE_CLOSE);
		}
		System.out.println(output);
		
	}
}
