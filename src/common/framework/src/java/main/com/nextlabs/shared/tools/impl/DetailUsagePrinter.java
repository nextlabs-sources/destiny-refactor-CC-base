package com.nextlabs.shared.tools.impl;

import java.util.Collection;
import java.util.List;

import com.bluejungle.framework.utils.ArrayUtils;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.StringFormatter;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * default detail usage print out.
 * @author hchan
 * @date Mar 27, 2007
 */
public class DetailUsagePrinter extends UsagePrinterBase {

	private final String indent;

	public DetailUsagePrinter() {
		this(ConsoleDisplayHelper.getScreenWidth(), ConsoleDisplayHelper.NEWLINE, OPTION_INDENTATION, true);
	}

	DetailUsagePrinter(int width, String lineBreaker, String indent, boolean isQuiet) {
		super(width, lineBreaker, isQuiet);
		this.indent = indent;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorVisitor#visit(com.nextlabs.shared.tools.IOptionDescriptor)
	 */
	public void visit(IOptionDescriptor<?> optionDescriptor) {
		StringBuilder output = new StringBuilder();

//		if (!optionDescriptor.isRequired()) {
//			output.append(OPTIONAL_OPEN);
//		}

		Collection<String> commonLineIndicators = optionDescriptor.getCommandLineIndicators();
		output.append(ArrayUtils.asString(CollectionUtils.toStringArray(commonLineIndicators), ", ", "-"));

		if (optionDescriptor.getNumPossibleValues() > 0) {
			String valueLabel = optionDescriptor.getValueLabel();
			if (valueLabel != null) {
				output.append(" ").append(VALUE_OPEN).append(valueLabel).append(VALUE_CLOSE);
			}
		}

//		if (!optionDescriptor.isRequired()) {
//			output.append(OPTIONAL_CLOSE);
//		}

		//remove the last ","
		boolean isOptionTooLong = output.length() >= indent.length();

		if (isOptionTooLong) {
			//the common line indicator is too long, put the description in next line
			output.append(super.lineBreaker);
		}

		String description = optionDescriptor.getDescription();
		description = StringFormatter.wrap(description, width - indent.length(), super.lineBreaker, indent);

		if (!isOptionTooLong) {
			if (description.length() > output.length()) {
				description = description.substring(output.length());
			}
		}
		output.append(description);
		getCache().add(output.toString());
		if (!isQuiet) {
			System.out.println(output);
		}
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IOptionDescriptorVisitor#visit(com.nextlabs.shared.tools.IComponentOptionDescriptor)
	 */
	public void visit(ICompoundOptionDescriptor compoundOptionDescriptor) {
		switch (compoundOptionDescriptor.getType()) {
		case SIMPLE:
			IOptionDescriptor<?> option = ((SimpleCompoundOptionDescriptor) compoundOptionDescriptor).getOption();
			if (option != null) {
				option.accept(this);
			}
			break;
		case UNIQUE:
		case MULTI:
		case SEQUENCE:
			List<ICompoundOptionDescriptor> list = compoundOptionDescriptor.getCompoundOptions();
			for (ICompoundOptionDescriptor descriptor : list) {
				descriptor.accept(this);
			}
			break;
		}
	}
}
