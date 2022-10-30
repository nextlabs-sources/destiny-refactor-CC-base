package com.nextlabs.shared.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.StringUtils;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.impl.CommandLineArguments;
import com.nextlabs.shared.tools.impl.CommandLineImpl;
import com.nextlabs.shared.tools.impl.DetailUsagePrinter;
import com.nextlabs.shared.tools.impl.ErrorMessageGenerator;
import com.nextlabs.shared.tools.impl.OptionValidator;
import com.nextlabs.shared.tools.impl.SynopisUsagePrinter;
import com.nextlabs.shared.tools.impl.UsagePrinterBase;

/**
 * A base implementation of ConsoleApplication, all console application should extends from this calss
 * 
 * @author hchan
 * @date Mar 27, 2007
 */
public abstract class ConsoleApplicationBase{
	private static final boolean PRINT_HELP_IF_NO_ARGS = true;

	protected static boolean isDebug;
	static{
	    String str = System.getProperty("nextlabs.debug");
	    isDebug = StringUtils.stringToBoolean(str, false);   
	}
	
	protected boolean printHelpIfNoArgs() {
		return PRINT_HELP_IF_NO_ARGS;
	}
	
	/**
	 * create options, parse options, printout any error, then call the execute(ICommandLine)
	 * @param args arguments from command line
	 * @throws com.nextlabs.shared.tools.ParseException when parsing is failed.
	 */
	protected void parseAndExecute(String[] args) throws com.nextlabs.shared.tools.ParseException  {
		if (printHelpIfNoArgs() && args.length == 0) {
			printUsage();
			return;
		}
		
		OptionValidator optionValidator = new OptionValidator(new CommandLineArguments(args), true);
		optionValidator.renderUsage(getDescriptor());
		if (optionValidator.hasError()) {
			String exceptionMessage = CollectionUtils.asString(
					optionValidator.getErrorLog(), ConsoleDisplayHelper.NEWLINE);
			throw new ParseException(exceptionMessage);
		} else {
			CommandLineImpl commandLineImpl = new CommandLineImpl(optionValidator.getExistedOptions());
			
			if (commandLineImpl.isOptionExist(IConsoleApplicationDescriptor.HELP_OPTION_ID)) {
				printUsage();
			} else {
				execute(commandLineImpl);
			}
		}
	}

	/**
	 * print usage of the normal import tools
	 * need to make it configurable between the seed data import and normal import
	 */
	protected void printUsage() {
		String name = getDescriptor().getShortDescription();
		
		SynopisUsagePrinter sPrinter = new SynopisUsagePrinter();
		sPrinter.renderUsage(getDescriptor());
		
		String synopsis = sPrinter.getCache().get(0);
		
		DetailUsagePrinter detailPrinter = new DetailUsagePrinter();
		detailPrinter.renderUsage(getDescriptor());
		
		String formattedOptions = CollectionUtils.toString(detailPrinter.getCache());
		
		String description = getDescriptor().getLongDescription();
		
		UsagePrinterBase.print(name, synopsis, formattedOptions, description );
	}
	
	
	/**
	 * Perform all work requested by the end user running the tool
	 * @param commandLine
	 */
	protected abstract void execute(ICommandLine commandLine);

	/**
	 * @return a descriptor describing the tool
	 */
	protected abstract IConsoleApplicationDescriptor getDescriptor() ;
	
	/**
	 * get the option value, if the value is not in the found, return default value instead.
	 */
	protected <T> T getValue(ICommandLine commandLine, OptionId<T> optionId){
		IOptionDescriptor<T> option = optionId.getOption();
		if (option == null) {
			throw new InvalidOptionDescriptorException(ErrorMessageGenerator.getUnknownOptionid(optionId));
		}
		if(optionId.getValueType() == OptionValueType.ON_OFF && option.getNumPossibleValues() == 0){
			return (commandLine.isOptionExist(optionId))
					? (T)new Boolean(true)
					: option.getDefaultValue();
		}else{
			List<T> parsedValues = commandLine.getParsedValues(optionId);
			return (parsedValues == null)
					? option.getDefaultValue()
					: parsedValues.get(0);
		}
	}

	protected <T> List<T> getValues(ICommandLine commandLine, OptionId<T> optionId) {
		List<T> parsedValues = commandLine.getParsedValues(optionId);
		if (parsedValues != null) {
			return parsedValues;
		} else {
			if (optionId.getOption() == null) {
				throw new OptionDescriptorNotFoundException(optionId.getName());
			}
			T defaultValue = optionId.getOption().getDefaultValue();
			return Collections.singletonList(defaultValue);
		}
	}
	
	protected boolean isDebug(){
	    return isDebug;
	}
		
	protected static void printException(Throwable t){
		printException(null, t);
	}
	
	protected static void printException(String message, Throwable t){
		if (message != null) {
			message += ": " + t.getMessage();
		} else {
			message = t.getLocalizedMessage();
			if (message == null || message.length() == 0) {
				message = t.toString();
			}else{
				message = t.getClass().getName() + ": " + message;
			}
		}
		System.err.println(message);
		if (isDebug) {
            t.printStackTrace();
        }
	}
	
	protected void printDebugScreen(){
		printUsage();
		System.out.println("-------------");
		com.nextlabs.shared.tools.impl.OptionStructurePrinter.print(getDescriptor());
		System.exit(0);
	}
}
