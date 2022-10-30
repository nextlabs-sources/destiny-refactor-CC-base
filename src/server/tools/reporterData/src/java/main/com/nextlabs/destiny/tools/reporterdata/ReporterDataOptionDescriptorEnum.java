/*
 * Created on Oct 3, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.reporterdata;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;


import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/nextlabs/destiny/tools/reporterdata/ReporterDataOptionDescriptorEnum.java#1 $
 */

public class ReporterDataOptionDescriptorEnum implements IConsoleApplicationDescriptor {
	private IOptionDescriptorTree options;
	static final OptionId<File> SERVER_CONFIGURATION_FOLDER_OPTION_ID = OptionId.create("serverConfigFolder", OptionValueType.EXIST_FOLDER);

	static final OptionId<File> PERFORMANCE_OPTION_ID 	= OptionId.create("perfConfigFile",	OptionValueType.EXIST_FILE);
	static final OptionId<File> CSV_CONFIG_OPTION_ID 	= OptionId.create("csvConfig",		OptionValueType.EXIST_FILE);
	static final OptionId<File> CSV_DATA_OPTION_ID 		= OptionId.create("csvData",		OptionValueType.EXIST_FILE);
	
	@Deprecated
	static final OptionId<Boolean> REGULAR_OPTION_ID 	= OptionId.create("regularData",	OptionValueType.ON_OFF);
	
	@Deprecated
	static final OptionId<File> UNKNOWN_INPUT_OPTION_ID = OptionId.create("input",			OptionValueType.EXIST_FILE);
	
	@Deprecated
	static final OptionId<File> DASHBOARD_DATA_OPTION_ID = OptionId.create("dashboardData",	OptionValueType.EXIST_FILE);
	
	
	public ReporterDataOptionDescriptorEnum() throws InvalidOptionDescriptorException {
		File defaultValue;
		String description;
		String valueLabel;
		boolean required;
		boolean valueRequired;
		Option<?> option;
		Collection<String> cmdInds;
		
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
		
		defaultValue = new File("C:\\Program Files\\NextLabs\\Policy Server\\server\\configuration\\");
		description = "policy server configuration folder";
		valueLabel = "path to configuration folder";
		required = true;
		valueRequired = true;
		cmdInds = new LinkedList<String>();
		cmdInds.add("sc");
		cmdInds.add(SERVER_CONFIGURATION_FOLDER_OPTION_ID.getName());
		option = new Option<File>(
				SERVER_CONFIGURATION_FOLDER_OPTION_ID, 
				cmdInds, 
				description,
				required,
				valueLabel, 
				defaultValue, 
				valueRequired, 
				1);
		root.add(option);
		
		UniqueChoiceOptionDescriptor actionGroup = new UniqueChoiceOptionDescriptor();
		
		description = "Deprecated! insert regular report data.";
		option = Option.createOnOffOption(REGULAR_OPTION_ID, description);
		actionGroup.add(option);
		
		description = "Deprecated! insert dashboard data";
		valueLabel = "dashboard data file";
		option = Option.createOption(DASHBOARD_DATA_OPTION_ID, description, valueLabel);
		actionGroup.add(option);
		
		description = "Deprecated! input file?";
		valueLabel = "input file?";
		required = false;
		option = Option.createOption(UNKNOWN_INPUT_OPTION_ID, description, valueLabel, null);
		actionGroup.add(option);
		
		defaultValue = new File("perfConfig.properties"); //useless to set since the value is required
		description = "performance test configuration file.";
		valueLabel = "perfConfig.properties";
		required = true;
		valueRequired = true;
		cmdInds = new LinkedList<String>();
		cmdInds.add("pp");
		cmdInds.add(PERFORMANCE_OPTION_ID.getName());
		option = new Option<File>(
				PERFORMANCE_OPTION_ID, 
				cmdInds, 
				description,
				required,
				valueLabel, 
				defaultValue, 
				valueRequired, 
				1);
		actionGroup.add(option);
		
		SequencedListOptionDescriptor csvGroup = new SequencedListOptionDescriptor();
		
		defaultValue = null; //useless to set since the value is required
		description = "The input csv file.";
		valueLabel = "csv file";
		required = true;
		valueRequired = true;
		cmdInds = new LinkedList<String>();
		cmdInds.add("cd");
		cmdInds.add(CSV_DATA_OPTION_ID.getName());
		option = new Option<File>(
				CSV_DATA_OPTION_ID, 
				cmdInds, 
				description,
				required,
				valueLabel, 
				defaultValue, 
				valueRequired, 
				1);
		csvGroup.add(option);
		
		defaultValue = null; //useless to set since the value is required
		description = "The configuration to specifiy how to input the data.";
		valueLabel = "csv configuraiton";
		required = true;
		valueRequired = true;
		cmdInds = new LinkedList<String>();
		cmdInds.add("cc");
		cmdInds.add(CSV_CONFIG_OPTION_ID.getName());
		option = new Option<File>(
				CSV_CONFIG_OPTION_ID, 
				cmdInds, 
				description,
				required,
				valueLabel, 
				defaultValue, 
				valueRequired, 
				1);
		
		csvGroup.add(option);
		
		actionGroup.add(csvGroup);
		
		root.add(actionGroup);
		
		options = new OptionDescriptorTreeImpl(root);
		
	}

	public String getLongDescription() {
	    final String N = ConsoleDisplayHelper.NEWLINE;
	    StringBuilder sb = new StringBuilder();
	    
	    sb.append("This tool is to insert random data or fixed data into Reporter. ")
	      .append("It is mainly for test and demo purpose. ")
	      .append("You should have done enrollment and deployed some policies before running this tool. ")
	      .append("The size of the enrollment should be equal or greater than the value specifed in the configuration file. ")
	      .append("\n\nExample:").append(N)
	      .append("  Insert random data: ").append(N)
	      .append("    ").append(getName())
          .append(" -sc C:\\configuration\\ -pp perfConfig.properties\n").append(N)
          .append("  Insert fixed data from csv: ").append(N)
          .append("    ").append(getName())
          .append(" -sc C:\\configuration\\ -cc csvConfig.properties -cd input.csv\n").append(N);
	    
		String version = null;
		try {
			version = this.getClass().getPackage().getImplementationVersion();
		} catch (Exception ignore) {
		}
		
		if (version == null) {
		    version = "unknown";
        }
		
		sb.append("Version: " + version);
		
		return sb.toString();
	}

	public String getName() {
		return "reporterData";
	}

	public IOptionDescriptorTree getOptions() {
		return options;
	}

	public String getShortDescription() {
		return getName() + ", a tool to insert data into database for Reporter.";
	}
}
