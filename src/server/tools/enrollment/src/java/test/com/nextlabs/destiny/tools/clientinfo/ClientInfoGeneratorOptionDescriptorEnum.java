/*
 * Created on Mar 29, 2008
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.tools.clientinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.bluejungle.framework.utils.ArrayUtils;
import com.nextlabs.shared.tools.ICompoundOptionDescriptor;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.OptionTrigger;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.OptionTrigger.Condition;
import com.nextlabs.shared.tools.impl.OptionTrigger.ConditionEnum;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/test/com/nextlabs/destiny/tools/clientinfo/ClientInfoGeneratorOptionDescriptorEnum.java#1 $
 */

public class ClientInfoGeneratorOptionDescriptorEnum implements IConsoleApplicationDescriptor {
	static final OptionId<String> CREATE_OPTION_ID 	= OptionId.create("create", OptionValueType.CUSTOM_LIST);
	
	static final String CREATE_USERS_VALUE = "USER";
	static final String CREATE_DOMAINS_VALUE = "DOMAIN";
	static final String CREATE_CLIENT_INFO_VALUE = "CLIENT-INFO";
	static final String[] CREATE_OPTION_VALUES = new String[] { 
		CREATE_USERS_VALUE,			
		CREATE_DOMAINS_VALUE, 
		CREATE_CLIENT_INFO_VALUE };
	
	static final OptionId<String> BASE_OPTION_ID 				= OptionId.create("base", 			OptionValueType.STRING);
	static final OptionId<String> FILTER_OPTION_ID 				= OptionId.create("filter", 		OptionValueType.STRING);
	static final OptionId<String> ATTRS_OPTION_ID 				= OptionId.create("attrs",			OptionValueType.STRING);
//	static final String SCOPE_OPTION_ID = "scope";
	
	static final OptionId<Integer> USER_NUM_MIN_OPTION_ID 		= OptionId.create("usersMin", 		OptionValueType.INTEGER);
	static final OptionId<Integer> USER_NUM_MAX_OPTION_ID 		= OptionId.create("usersMax", 		OptionValueType.INTEGER);
	static final OptionId<Integer> USER_NUM_AVERAGE_OPTION_ID 	= OptionId.create("usersAverage", 	OptionValueType.INTEGER);
	
	static final OptionId<Integer> DOMAIN_NUM_MIN_OPTION_ID 	= OptionId.create("domainsMin", 	OptionValueType.INTEGER);
	static final OptionId<Integer> DOMAIN_NUM_MAX_OPTION_ID 	= OptionId.create("domainsMax", 	OptionValueType.INTEGER);
	static final OptionId<Integer> DOMAIN_NUM_AVERAGE_OPTION_ID = OptionId.create("domainsAverage", OptionValueType.INTEGER);
	
	static final OptionId<Integer> CLIENT_NUM_OPTION_ID 		= OptionId.create("numOfClient", 	OptionValueType.INTEGER);
	
	static final OptionId<String> USER_FILE_OPTION_ID 			= OptionId.create("userFile", 		OptionValueType.STRING);
	static final OptionId<String> DOMAIN_FILE_OPTION_ID 		= OptionId.create("domainFile", 	OptionValueType.STRING);
	static final OptionId<String> OUTPUT_FILE_OPTION_ID 		= OptionId.create("outputFile", 	OptionValueType.STRING);
	
	static final OptionId<Float> DUPLICATE_DOMAIN_PERCENTAGE_OPTION_ID = OptionId.create("dupDomain", OptionValueType.FLOAT);
	
	
	private IOptionDescriptorTree treeRoot;
	
	public ClientInfoGeneratorOptionDescriptorEnum() throws InvalidOptionDescriptorException {
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();
		
		String description;
		String valueLabel;
		Option<?> option;
		
		description = "create type";
		valueLabel = ArrayUtils.asString(CREATE_OPTION_VALUES, ", ");
		Option<String> createTypeOption = Option.createOption(CREATE_OPTION_ID, 
				description, valueLabel);
		
		createTypeOption.setLevel((byte) -110);
		for(String key : CREATE_OPTION_VALUES){
			OptionValueType.CUSTOM_LIST.addCustomValue(CREATE_OPTION_ID, key);
		}
		root.add(createTypeOption);
		
		
		ICompoundOptionDescriptor createUsersRoot = createUserOption();
		root.add(createUsersRoot);
		List<Condition> conditions = new ArrayList<Condition>();
		conditions.add(new Condition(createTypeOption, ConditionEnum.IF_FOUND,
				ClientInfoGeneratorOptionDescriptorEnum.CREATE_USERS_VALUE));
		OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_REQUIRED, createUsersRoot);

		ICompoundOptionDescriptor createClientInfosRoot = createClientInfoOption();
		root.add(createClientInfosRoot);
		conditions = new ArrayList<Condition>();
		conditions.add(new Condition(createTypeOption, ConditionEnum.IF_FOUND,
				ClientInfoGeneratorOptionDescriptorEnum.CREATE_CLIENT_INFO_VALUE));
		OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_REQUIRED, createClientInfosRoot);

		description = "output file";
		valueLabel = "output file";
		option = Option.createOption(OUTPUT_FILE_OPTION_ID, 
				description, valueLabel); 
		option.setLevel((byte) -29);
		root.add(option);
		
		treeRoot = new OptionDescriptorTreeImpl(root);
	}

	private ICompoundOptionDescriptor createUserOption() throws InvalidOptionDescriptorException {
		SequencedListOptionDescriptor createUsersRoot = new SequencedListOptionDescriptor();
		createUsersRoot.setRequired(false);
		
		Set<String> commandLineIndicators;
		String description;
		String valueLabel;
		boolean required;
		boolean valueRequired;
		int numPossibleValues;
		Option<?> option;
		
		commandLineIndicators = new TreeSet<String>();
		commandLineIndicators.add("host");
		commandLineIndicators.add(HOST_OPTION_ID.getName());
		description = "host name of the LDAP server to which you want to connect. " +
				"This value can also be a space-delimited list of hostnames or hostnames " +
				"and port numbers (using the syntax hostname:portnumber).";
		valueLabel = "host";
		required = true;
		valueRequired = true;
		numPossibleValues = 1;
		option = new Option<String>(
				HOST_OPTION_ID, 
				commandLineIndicators, 
				description,
				required, 
				valueLabel,
				null, // defaultValue, 
				valueRequired, 
				numPossibleValues);
		option.setLevel((byte) -99);
		createUsersRoot.add(option);

		commandLineIndicators = new TreeSet<String>();
		commandLineIndicators.add("port");
		commandLineIndicators.add(PORT_OPTION_ID.getName());
		description = "port number of the LDAP server to which you want to connect. "
				+ "This parameter is ignored for any host in the host  parameter which "
				+ "includes a colon and port number.";
		valueLabel = "host";
		required = false;
		valueRequired = true;
		numPossibleValues = 1;
		option = new Option<Integer>(
				PORT_OPTION_ID, 
				commandLineIndicators, 
				description,
				required, 
				valueLabel,
				new Integer(389), // defaultValue, 
				valueRequired, 
				numPossibleValues);
		option.setLevel((byte) -98);
		createUsersRoot.add(option);

		commandLineIndicators = new TreeSet<String>();
		commandLineIndicators.add("dn");
		commandLineIndicators.add(USER_ID_OPTION_ID.getName());
		description = "distinguished name used for authentication";
		valueLabel = "dn";
		required = true;
		valueRequired = true;
		numPossibleValues = 1;
		option = new Option<String>(
				USER_ID_OPTION_ID, 
				commandLineIndicators, 
				description,
				required, 
				valueLabel,
				null, // defaultValue, 
				valueRequired, 
				numPossibleValues);
		option.setLevel((byte) -97);
		createUsersRoot.add(option);

		commandLineIndicators = new TreeSet<String>();
		commandLineIndicators.add("passwd");
		commandLineIndicators.add(PASSWORD_OPTION_ID.getName());
		description = "password used for authentication";
		valueLabel = "passwd";
		required = true;
		valueRequired = true;
		numPossibleValues = 1;
		option = new Option<String>(
				PASSWORD_OPTION_ID, 
				commandLineIndicators, 
				description,
				required, 
				valueLabel,
				null, // defaultValue, 
				valueRequired, 
				numPossibleValues);
		option.setLevel((byte) -96);
		createUsersRoot.add(option);
		
		
		description = "search filter specifying the search criteria";
		valueLabel = "filter";
		required = false;
		valueRequired = true;
		option = Option.createOption(FILTER_OPTION_ID, description, valueLabel,	"(objectClass=User)");
		option.setLevel((byte) -79);
		createUsersRoot.add(option);
		
		description = "list of attributes that you want returned in the search results";
		valueLabel = "attrs";
		required = false;
		valueRequired = true;
		numPossibleValues = IOptionDescriptor.UNLIMITED_NUM_POSSIBLE_VALUES;
		option = new Option<String>(
				ATTRS_OPTION_ID,
				description,
				required,
				valueLabel,
				"userPrincipalName", // defaultValue
				valueRequired,
				numPossibleValues);
		option.setLevel((byte) -79);
		createUsersRoot.add(option);
		
		description = "the base distinguished name from which to search";
		valueLabel = "base";
		option = Option.createOption(BASE_OPTION_ID, description, valueLabel) ;
		option.setLevel((byte) -78);
		createUsersRoot.add(option);
		
//		description = "the scope of the entries to search";
//		valueLabel = "scope";
//		defaultValue = ClientInfoGenerator.SCOPE_SUB_VALUE;
//		optionValueType = OptionValueType.CUSTOM_LIST;
//		required = false;
//		valueRequired = true;
//		option = new Option(SCOPE_OPTION_ID, defaultValue, description, valueLabel,
//				optionValueType, required, valueRequired);
//		option.setLevel((byte) -77);
//		for(String key : ClientInfoGenerator.scopeToIntegerMap.keySet()){
//			OptionValueType.CUSTOM_LIST.addCustomValue(option, key);
//		}
//		createUsersRoot.add(option);
		
		return createUsersRoot;
	}
	
	private ICompoundOptionDescriptor createClientInfoOption() throws InvalidOptionDescriptorException {
		SequencedListOptionDescriptor createClientInfosRoot = new SequencedListOptionDescriptor();
		createClientInfosRoot.setRequired(false);
		
		String description;
		boolean required;
		String valueLabel;
		boolean valueRequired;
		Option<?> option;
		
		description = "Number of client";
		valueLabel = "client number";
		option = Option.createOption(CLIENT_NUM_OPTION_ID, 
				description,  valueLabel);
			
		option.setLevel((byte) -59);
		createClientInfosRoot.add(option);
		
		
		SequencedListOptionDescriptor usersRangeRoot = new SequencedListOptionDescriptor();
		usersRangeRoot.setRequired(false);
		
		description = "Minimum Number of users, default is 0";
		valueLabel = "min users";
		option = Option.createOption(USER_NUM_MIN_OPTION_ID, 
				description, valueLabel, 0);
		option.setLevel((byte) -59);
		usersRangeRoot.add(option);
		
		description = "Maximum Number of users, default is  50";
		valueLabel = "max users";
		option = Option.createOption(USER_NUM_MAX_OPTION_ID, 
				description, valueLabel, 50);
		option.setLevel((byte) -58);
		usersRangeRoot.add(option);
		
		description = "Average Number of users, default is 10";
		valueLabel = "ave users";
		option = Option.createOption(USER_NUM_AVERAGE_OPTION_ID, 
				description, valueLabel, 10);
		option.setLevel((byte) -57);
		usersRangeRoot.add(option);
		
		createClientInfosRoot.add(usersRangeRoot);
		
		
		SequencedListOptionDescriptor domainsRangeRoot = new SequencedListOptionDescriptor();
		domainsRangeRoot.setRequired(false);
		
		description = "Minimum Number of domains";
		valueLabel = "min domains";
		option = Option.createOption(DOMAIN_NUM_MIN_OPTION_ID, 
				description, valueLabel, 0);
		option.setLevel((byte) -49);
		domainsRangeRoot.add(option);
		
		description = "Maximum Number of domains";
		valueLabel = "max domains";
		option = Option.createOption(DOMAIN_NUM_MAX_OPTION_ID, 
				description, valueLabel, 10);
		option.setLevel((byte) -48);
		domainsRangeRoot.add(option);
		
		description = "Average Number of domains";
		valueLabel = "ave domaines";
		option = Option.createOption(DOMAIN_NUM_AVERAGE_OPTION_ID, 
				description, valueLabel, 5);
		option.setLevel((byte) -47);
		domainsRangeRoot.add(option);
		
		createClientInfosRoot.add(domainsRangeRoot);
		
		description = "domain names file";
		valueLabel = "domains file";
		option = Option.createOption(DOMAIN_FILE_OPTION_ID, 
				description, valueLabel);
		option.setLevel((byte) -79);
		createClientInfosRoot.add(option);
		
		description = "users file";
		valueLabel = "users file";
		option = Option.createOption(USER_FILE_OPTION_ID, 
				description, valueLabel);
		option.setLevel((byte) -76);
		createClientInfosRoot.add(option);
		
		description = "duplicate domain percentage";
		valueLabel = "duplicate domain percentage";
		required = false;
		valueRequired = true;
		option = Option.createOption(DUPLICATE_DOMAIN_PERCENTAGE_OPTION_ID, 
				description, valueLabel, 0F);
		option.setLevel((byte) -76);
		createClientInfosRoot.add(option);
		
		return createClientInfosRoot;
	}
	
	/**
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getLongDescription()
	 */
	public String getLongDescription() {
		//new line
		final String N = ConsoleDisplayHelper.NEWLINE;
		
		final String EMPTY_LINE = " " + N;
		
		//level 1
		final String L1 = "  ";
		
		//level 2
		final String L2 = "    ";
		return "This is an internal test tool for nextlabs. Don't file any bugs against this tool."+ N
			+ EMPTY_LINE
			+ "Known issues:  the average number is not working." + N
			+ EMPTY_LINE
			+ "Generate users file : " + N 
			+ L1 + getName() + N
			+ L2 + "-create USER " + N
			+ L2 + "-host cuba.test.bluejungle.com " + N
			+ L2 + "-dn test\\jimmy.carter " + N
			+ L2 + "-passwd jimmy.carter " + N
			+ L2 + "-base \"ou=users,ou=fixed,dc=test,dc=bluejungle,dc=com\"" + N
			+ L2 + "-outputFile users.file" + N
			+ EMPTY_LINE
			+ "Generate domains file : " + N 
			+ L1 + getName() + N
			+ L2 + "-create DOMAIN " + N
			+ L2 + "-outputFile domains.file" + N 
			+ EMPTY_LINE
			+ "Generate client-info file : " + N
			+ L1 + getName() + N
			+ L2 + "-create CLIENT-INFO " + N
			+ L2 + "-outputFile client-info.file " + N
			+ L2 + "-userFile users.file " + N
			+ L2 + "-domainFile domains.file " + N
			+ L2 + "-numOfClient 100"
		;
	}

	/**
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getName()
	 */
	public String getName() {
		return "clientInfoGenerator";
	}

	/**
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getOptions()
	 */
	public IOptionDescriptorTree getOptions() {
		return treeRoot;
	}

	/**
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getShortDescription()
	 */
	public String getShortDescription() {
		return "clientInfoGenerator";
	}

}
