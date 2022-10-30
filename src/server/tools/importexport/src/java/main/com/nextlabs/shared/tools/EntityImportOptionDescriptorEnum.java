package com.nextlabs.shared.tools;

import java.util.Collection;
import java.util.TreeSet;

import com.nextlabs.shared.tools.impl.MultiChoiceOptionDescriptor;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;

/**
 * @author hchan
 * @date Mar 30, 2007
 */
class EntityImportOptionDescriptorEnum extends EntityImortExportOptionDescriptorEnum {
	static final OptionId<Boolean>	SHALLOW_IMPORT_OPTION		= OptionId.create("i", OptionValueType.ON_OFF);
	static final OptionId<Boolean>	LIST_ALL_OPTION				= OptionId.create("l", OptionValueType.ON_OFF);
	static final OptionId<Boolean>	CONFLICT_OVERWRITE_OPTION	= OptionId.create("o", OptionValueType.ON_OFF);
	static final OptionId<Boolean>	CONFLICT_RENAME_NEW_OPTION	= OptionId.create("r", OptionValueType.ON_OFF);
	static final OptionId<Boolean>	CONFLICT_KEEP_OPTION		= OptionId.create("d", OptionValueType.ON_OFF);
	static final OptionId<Boolean>	CONFLICT_CANCEL_OPTION		= OptionId.create("x", OptionValueType.ON_OFF);
	static final OptionId<Boolean>	STATUS_OPTION				= OptionId.create("A", OptionValueType.ON_OFF);
	
	static final Collection<OptionId<Boolean>> CONFLICT_SOLUTION_OPTION_IDS;
	static{
		CONFLICT_SOLUTION_OPTION_IDS = new TreeSet<OptionId<Boolean>>();
		CONFLICT_SOLUTION_OPTION_IDS.add(CONFLICT_OVERWRITE_OPTION);
		CONFLICT_SOLUTION_OPTION_IDS.add(CONFLICT_RENAME_NEW_OPTION);
		CONFLICT_SOLUTION_OPTION_IDS.add(CONFLICT_KEEP_OPTION);
	};
	
	EntityImportOptionDescriptorEnum() throws InvalidOptionDescriptorException {
		super();
	}
	
		/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ImortExportOptionDescriptorEnum#getLongDescription()
	 */
	public String getLongDescription() {
		return "The Import feature is a command line interface that allows a user (administrator) to load "
				+ "policies and components to a server from a file. The CLI connects to the server, using "
				+ "an Administrator's credentials, then performs one or more of actions.";
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ImortExportOptionDescriptorEnum#getName()
	 */
	public String getName() {
		return "import";
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ImortExportOptionDescriptorEnum#getShortDescription()
	 */
	//	return "Import policies into a server.";
	public String getShortDescription() {
		return "Create seed data for a NextLabs enforcement product (e.g., Entitlement Manager for SAP)";
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ImortExportOptionDescriptorEnum#createCommonRoot()
	 */
	protected ICompoundOptionDescriptor createRoot() throws InvalidOptionDescriptorException {		
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
		
		UniqueChoiceOptionDescriptor uniqueLvl2 = new UniqueChoiceOptionDescriptor();
		root.add(uniqueLvl2);
		
		SequencedListOptionDescriptor sequencedLvl3 = new SequencedListOptionDescriptor();
		String description = "Name or URL of the host where Policy Management Server is running";
		String valueLabel = "server";
		IOptionDescriptor option = Option.createOption(HOST_OPTION_ID, description, 
				valueLabel);
		sequencedLvl3.add(option);

		description = "User name of a Control Center user with administrator privileges";
		valueLabel = "username";
		option = Option.createOption(USER_ID_OPTION_ID, description, valueLabel);
		sequencedLvl3.add(option);

		description = "Password for the Control Center administrative user specified by -u";
		valueLabel = "password";
		option = Option.createOption(PASSWORD_OPTION_ID, description, valueLabel, null);
		sequencedLvl3.add(option);
		
		description = "Port of the Control Center host, which is 8443 unless it was explicitly changed";
		valueLabel = "port";
		option = Option.createOption(PORT_OPTION_ID, description, valueLabel, 8443);
		sequencedLvl3.add(option);
		
		
		MultiChoiceOptionDescriptor multiComponundLvl4 = new MultiChoiceOptionDescriptor(0, 2);

		UniqueChoiceOptionDescriptor conflictSolutionComponund = new UniqueChoiceOptionDescriptor();
		description = "Solve all conflicts by overwriting existing data on the server. ("
				+ "Mutually exclusive w/ -u, -d)";
		option = Option.createOnOffOption(CONFLICT_OVERWRITE_OPTION, description);
		conflictSolutionComponund.add(option);

		description = "[Default behavior] Solve all conflicts by adding new data. This will autogenerate "
				+ "new names. (mutually exclusive w/ -o, -d)";
		option = Option.createOnOffOption(CONFLICT_RENAME_NEW_OPTION, description);
		conflictSolutionComponund.add(option);

		description = "Solve all conflicts by keeping existing objects. (mutually exclusive with -u, -o)";
		option = Option.createOnOffOption(CONFLICT_KEEP_OPTION, description);
		conflictSolutionComponund.add(option);
		multiComponundLvl4.add(conflictSolutionComponund);

		description = "Cancel on conflict. Supersedes -o, -u, -d.";
		option = Option.createOnOffOption(CONFLICT_CANCEL_OPTION, description);
		multiComponundLvl4.add(option);
		sequencedLvl3.add(multiComponundLvl4);

		description = "Shallow import - remove components definition (other than name), from the imported data.";
		option = Option.createOnOffOption(SHALLOW_IMPORT_OPTION, description);
		sequencedLvl3.add(option);
		
		description = "Indicated -APPROVED- lifecycle status of seed data components";
		option = Option.createOnOffOption(STATUS_OPTION, description);
		sequencedLvl3.add(option);

		uniqueLvl2.add(sequencedLvl3);
		
		description = "List the names of folders, policies and components stored inside the file.";
		option = Option.createOnOffOption(LIST_ALL_OPTION, description);
		uniqueLvl2.add(option);

		description = "Name of the seed data file for a NextLabs enforcement product (e.g., Entitlement Manager for SAP)";
		valueLabel = "filename";
		option = Option.createOption(FILE_OPTION, description, valueLabel);			
		root.add(option);
		return root;
	}

}
