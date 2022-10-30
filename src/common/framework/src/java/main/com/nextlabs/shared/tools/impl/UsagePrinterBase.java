/**
 * 
 */
package com.nextlabs.shared.tools.impl;

import java.util.LinkedList;
import java.util.List;

import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorVisitor;
import com.nextlabs.shared.tools.IUsageRenderer;
import com.nextlabs.shared.tools.StringFormatter;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;

/**
 * Print usage of the command
 * 
 * @author hchan
 * @date Mar 29, 2007
 */
public abstract class UsagePrinterBase implements IUsageRenderer, IOptionDescriptorVisitor {

	private static final String NAME 			= "NAME";
	private static final String SYNOPSIS 		= "SYNOPSIS";
	private static final String OPTIONS 		= "OPTIONS";
	private static final String DESCRIPTION 	= "DESCRIPTION";
	
	protected static final String OPTION_INDENTATION = "       ";	//7 space
	
	protected static final String OPTIONAL_OPEN 	= "[";
	protected static final String OPTIONAL_CLOSE 	= "]";
	protected static final String UNIQUE_OPEN 		= "{";
	protected static final String UNIQUE_CLOSE 		= "}";
	protected static final String UNIQUE_DIVIDER 	= "|";
	protected static final String VALUE_OPEN 		= "<";
	protected static final String VALUE_CLOSE 		= ">";
	
	protected static final String SET_OPEN 			= "(";
	protected static final String SET_CLOSE 		= ")";
	
	protected final boolean isQuiet;
	protected final int width;
	protected final String lineBreaker;
	
	private List<String> cache;
	
	public UsagePrinterBase(int width, String lineBreaker, boolean isQuiet) {
		this.isQuiet = isQuiet;
		this.width = width;
		this.lineBreaker = lineBreaker;
		cache = new LinkedList<String>();
	}
	
	public List<String> getCache() {
		return cache;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IUsageRenderer#renderUsage(com.nextlabs.shared.tools.IConsoleApplicationDescriptor)
	 */
	public void renderUsage(IConsoleApplicationDescriptor descriptor) {
		ICompoundOptionDescriptor root = descriptor.getOptions().getRootOption();
		root.accept(this);
	}
	
	/**
	 * a standard format of the usage
	 * @param name		the command name of the application
	 * @param synopsis
	 * @param formattedOption
	 * @param description
	 */
	public static void print(String name, String synopsis, String formattedOptions,	String description) {
	    final int width = ConsoleDisplayHelper.getScreenWidth();
		System.out.println(NAME);
		System.out.println(StringFormatter.wrap(name, width, ConsoleDisplayHelper.NEWLINE));
		System.out.println(SYNOPSIS);
		System.out.println(StringFormatter.wrap(synopsis, width, ConsoleDisplayHelper.NEWLINE));
		System.out.println(OPTIONS);
		System.out.println(formattedOptions);
		System.out.println(DESCRIPTION);
		System.out.println(StringFormatter.wrapParagraph(description, width, ConsoleDisplayHelper.NEWLINE));
	}
	public static void print(String name, String synopsis, String formattedOptions) {
	    final int width = ConsoleDisplayHelper.getScreenWidth();
		System.out.println(NAME);
		System.out.println(StringFormatter.wrap(name, width, ConsoleDisplayHelper.NEWLINE));
		System.out.println(SYNOPSIS);
		System.out.println(StringFormatter.wrap(synopsis, width, ConsoleDisplayHelper.NEWLINE));
		System.out.println(OPTIONS);
		System.out.println(formattedOptions);
	}
}
