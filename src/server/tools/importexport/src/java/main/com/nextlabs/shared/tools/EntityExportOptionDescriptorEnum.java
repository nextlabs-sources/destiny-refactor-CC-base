package com.nextlabs.shared.tools;

import java.util.Collection;
import java.util.TreeSet;

import com.nextlabs.shared.tools.impl.CompoundOptionDescriptorBase;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;

/**
 * @author hchan
 * @date Mar 30, 2007
 */
class EntityExportOptionDescriptorEnum extends EntityImortExportOptionDescriptorEnum {	
	static final OptionId<Boolean>	LISR_ALL_OPTION				= OptionId.create("l", OptionValueType.ON_OFF);
	
	static final OptionId<Boolean>	OVERWRITE_OPTION			= OptionId.create("f", OptionValueType.ON_OFF);
	static final OptionId<Boolean>	ABORT_OPTION				= OptionId.create("x", OptionValueType.ON_OFF);
	
	static final OptionId<Boolean>	INCLUDE_ALL_OPTION			= OptionId.create("a", OptionValueType.ON_OFF);
	static final OptionId<String>	INCLUDE_DIR_OPTION			= OptionId.create("d", OptionValueType.STRING);
	static final OptionId<String>	INCLUDE_DIR_R_OPTION		= OptionId.create("r", OptionValueType.STRING);
	static final OptionId<String>	INCLUDE_PATH_NAME_OPTION	= OptionId.create("P", OptionValueType.STRING);
	
	static final Collection<OptionId<Boolean>> OVERWRITES_OPTION_IDS;
	static{
		OVERWRITES_OPTION_IDS = new TreeSet<OptionId<Boolean>>();
		OVERWRITES_OPTION_IDS.add(OVERWRITE_OPTION);
		OVERWRITES_OPTION_IDS.add(ABORT_OPTION);
	};
	
	EntityExportOptionDescriptorEnum() throws InvalidOptionDescriptorException {
		super();
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ImortExportOptionDescriptorEnum#getLongDescription()
	 */
	public String getLongDescription() {
		return "The Export feature is a command line interface that allows a user (administrator) "
				+ "to save the policies and components from a server into a file. The CLI first "
				+ "connects to the server, using an Administrator's credentials, then performs "
				+ "one or more actions.";
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ImortExportOptionDescriptorEnum#getName()
	 */
	public String getName() {
		return "export";
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ImortExportOptionDescriptorEnum#getShortDescription()
	 */
	public String getShortDescription() {
		return "Export policies into a file.";
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ImortExportOptionDescriptorEnum#createCommonRoot()
	 */
	@Override
	protected ICompoundOptionDescriptor createRoot() throws InvalidOptionDescriptorException {
//		MultiChoiceOptionDescriptor superRoot = new MultiChoiceOptionDescriptor(1,2);
//
//		String description = "Supersedes any other arugements, and display the synopsis";
//		String  valueLabel = null;
//		boolean required = false;
//		boolean valueRequired = false;
//		IOptionDescriptor option = new Option(HELP_OPTION_ID, description, valueLabel, required);
//		superRoot.add(option);
		
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();

		String description = "This is the host that servers the policies";
		String valueLabel = "host";
		IOptionDescriptor<?> option = Option.createOption(HOST_OPTION_ID, description,
				valueLabel);
		root.add(option);

		description = "The administrator's user id";
		valueLabel = "username";
		option = Option.createOption(USER_ID_OPTION_ID, description, valueLabel);
		root.add(option);

		description = "The administrator's password";
		valueLabel = "password";
		option = Option.createOption(PASSWORD_OPTION_ID, description,  valueLabel, null);
		root.add(option);

		int defaultValue = 8443;
		description = "Port, 8443 by default";
		valueLabel = "port";
		option = Option.createOption(PORT_OPTION_ID, description, valueLabel,
						defaultValue);
		root.add(option);

		
//		description = "List the names of folders, policies and components specified by the target";
//		valueLabel = null;
//		required = false;
//		valueRequired = false;
//		option = new Option(LIST_ALL_OPTION, description, valueLabel, required, valueRequired);
//		UniqueChoiceOptionDescriptor uniqueComponundLvl1 = new UniqueChoiceOptionDescriptor();
//		uniqueComponundLvl1.add(option);
//
//		description = "List the names and all destails of the folders, policies and components specified by "
//				+ "the target. Supersedes -v. Lists: path/name, version, author.";
//		valueLabel = null;
//		required = false;
//		valueRequired = false;
//		option = new Option(LIST_SUBSET_OPTION, description, valueLabel, required, valueRequired);
//		uniqueComponundLvl1.add(option);
//
//		root.add(uniqueComponundLvl1);

		//FIXME try not to guess the root index/type
		
		createOptionalArguments(root);
		createSpecifyingTheTargets(root);
		return root;
	}

	private void createOptionalArguments(CompoundOptionDescriptorBase root)
			throws InvalidOptionDescriptorException {
		UniqueChoiceOptionDescriptor unique = new UniqueChoiceOptionDescriptor();
		String description = "List the policy names and folders.";
		boolean defaultValue = false;
		Option<?> option = Option.createOnOffOption(LISR_ALL_OPTION, description);
		unique.add(option);

		description = "filename";
		String valueLabel = "filename";
		option = Option.createOption(FILE_OPTION, description, valueLabel, null);
		unique.add(option);
		root.add(unique);

		UniqueChoiceOptionDescriptor overwriteUniqueChoice = new UniqueChoiceOptionDescriptor();
		overwriteUniqueChoice.setRequired(false);
		description = "Force file overwrite; backup existing file before overwrite. "
				+ "Flags -f and -x are mutually exclusive";
		defaultValue = false;
		option = Option.createOnOffOption(OVERWRITE_OPTION, description);
		overwriteUniqueChoice.add(option);

		description = "Cancel if file exists. Flags -f and -x are mutually exclusive";
		defaultValue = false;
		option = Option.createOnOffOption(ABORT_OPTION, description);
		overwriteUniqueChoice.add(option);
		root.add(overwriteUniqueChoice);
	}

	private void createSpecifyingTheTargets(CompoundOptionDescriptorBase root)
			throws InvalidOptionDescriptorException {
		SequencedListOptionDescriptor listDescriptor = new SequencedListOptionDescriptor();
		listDescriptor.setRequired(false);

		String description = "Include all folders, policies and associated components. Supersedes "
				+ "any other target specification.";
		String valueLabel = null;
		boolean valueRequired = false;
		Option<?> option = Option.createOnOffOption(INCLUDE_ALL_OPTION, description);
		listDescriptor.add(option);

		String defaultValue = null;
		description = "Include directory specified by <name>, exporting all policies and their dependent components. "
				+ "May be used more than once, and in conjunction with any other target specifier. ";
		valueLabel = "name";
		boolean required = false;
		valueRequired = true;
		int numPossibleValues = Integer.MAX_VALUE;
		option = new Option<String>(INCLUDE_DIR_OPTION, description, required, valueLabel, defaultValue,
						valueRequired, numPossibleValues);
		listDescriptor.add(option);

		defaultValue = null;
		description = "Include directory specified by <name>, as wellas its subdirectories, exporting all policies and "
				+ "their dependent components. May be used more than once, and in conjunction with any other target "
				+ "specifier.";
		valueLabel = "name";
		required = false;
		valueRequired = true;
		option = new Option<String>(INCLUDE_DIR_R_OPTION, description, required, valueLabel, defaultValue,
				valueRequired, numPossibleValues);
		listDescriptor.add(option);

		defaultValue = null;
		description = "Include the policy specified by path/name, including all of its dependent components. The path is "
				+ "the folder hierarchyof this policy. May be used more than once, and inconjunction with any other target "
				+ "specifier.";
		valueLabel = "path/name";
		required = false;
		valueRequired = true;
		option = new Option<String>(INCLUDE_PATH_NAME_OPTION, description, required, valueLabel, defaultValue,
				valueRequired, numPossibleValues);
		listDescriptor.add(option);

		root.add(listDescriptor);
//
//		description = "Include the component specified by the qualified name. The qualified name is made of the component "
//				+ "TYPE and the name. TYPE may be one of the following: USER, COMPUTER, APPLICATION, ACTION, FILE, PORTAL.";
//		valueLabel = "[TYPE]:<name>";
//		required = false;
//		valueRequired = true;
//		option = new Option("c", description, valueLabel, required, valueRequired);
//		root.add(option);
//
//		description = "Shallow Export – do not wire components to actual system system environment";
//		valueLabel = null;
//		required = false;
//		valueRequired = false;
//		option = new Option("n", description, valueLabel, required, valueRequired);
//		root.add(option);
	}
}
