package com.bluejungle.pf.tools;

import com.bluejungle.framework.utils.ArrayUtils;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.OptionTrigger;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.OptionTrigger.ConditionEnum;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;

/**
 * Location Importer Option Descriptor Enum
 *
 * @author hchan
 * @date Jul 5, 2007
 */
class LocationImporterOptionDescriptorEnum implements IConsoleApplicationDescriptor {
	static final OptionId<Boolean> VERBOSE_OPTION_ID = OptionId.create("v", OptionValueType.ON_OFF);
	static final OptionId<String> LOCATIONS_OPTION_ID = OptionId.create("l", OptionValueType.STRING);
	
	static final OptionId<String> INSTANCE_OPTION_ID = OptionId.create("i", OptionValueType.STRING);
	static final OptionId<String> DATABASE_OPTION_ID = OptionId.create("d", OptionValueType.CUSTOM_LIST);
	
	static final OptionId<String> JDBC_URL_OPTION_ID = OptionId.create("jdbcUrl", OptionValueType.STRING);
	
	private IOptionDescriptorTree options;
	
	/**
	 - Unique
     - [Simple] # [h]
     - Sequence
         - Simple # l <locations_filename>
         - [Simple] # [v]
         - Simple # u <username>
         - [Simple] # [w] <password>
         - Simple # s <host>
         - Simple # p <portnum>
         - [Simple] # [d] <[postgres,oracle,sqlserver,db2]>
         - [Simple] # [i] <instance>
	  */
	
	LocationImporterOptionDescriptorEnum() throws InvalidOptionDescriptorException {
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();

		String description = "locations";
		String valueLabel = "locations_filename";
		IOptionDescriptor<?> option = Option.createOption(LOCATIONS_OPTION_ID, description, valueLabel);
		root.add(option);

		description = "verbose";
		valueLabel = null;
		option = Option.createOnOffOption(VERBOSE_OPTION_ID, description);
		root.add(option);

		description = "user";
		valueLabel = "username";
		option = Option.createOption(USER_ID_OPTION_ID, description, valueLabel);
		root.add(option);

		description = "password";
		valueLabel = "password";
		option = Option.createOption(PASSWORD_OPTION_ID, description, valueLabel, null);
		root.add(option);
		
		String[] databaseKeys = new String[]{
                DeploymentToolsBase.POSTGRES_ARGUMENT_VALUE,
                DeploymentToolsBase.ORACLE_ARGUMENT_VALUE,
                DeploymentToolsBase.SQLSERVER_ARGUMENT_VALUE,
                DeploymentToolsBase.DB2_ARGUMENT_VALUE,
        }; 
        
        description = "Database type, default is " + DeploymentToolsBase.POSTGRES_ARGUMENT_VALUE;
        valueLabel = "[" + ArrayUtils.asString(databaseKeys, ",") + "]";;
        Option<String> databaseTypeOption = Option.createOption(DATABASE_OPTION_ID, 
                description, valueLabel, DeploymentToolsBase.POSTGRES_ARGUMENT_VALUE);
        for(String  databaseKey : databaseKeys){
            OptionValueType.CUSTOM_LIST.addCustomValue(DATABASE_OPTION_ID, databaseKey);
        }
        root.add(databaseTypeOption);
		
		
        
		UniqueChoiceOptionDescriptor choiceRoot = new UniqueChoiceOptionDescriptor();
		
		SequencedListOptionDescriptor seperateFieldRoot = new SequencedListOptionDescriptor();
		
		description = "host";
		valueLabel = "host";
		option = Option.createOption(HOST_OPTION_ID, description, valueLabel);
		seperateFieldRoot.add(option);

		description = "Port";
		valueLabel = "portnum";
		option = Option.createOption(PORT_OPTION_ID, description, valueLabel);
		seperateFieldRoot.add(option);

		description = "instance. This is only for some database such as Oracle and Microsoft sql server.";
		valueLabel = "instance";
		Option<String> instanceOption = Option.createOption(INSTANCE_OPTION_ID, 
				description, valueLabel, null);
		seperateFieldRoot.add(instanceOption);
		
		//FIXME we can do better here by looking jdbc driver url to determine if the instance is required.
		//  this may require DeploymentToolsBase class to use 
		//  com.bluejungle.destiny.tools.dbinit.hibernateMod.dialect.DbURLFormat
		String[] databasesRequireInstance = new String[]{
				DeploymentToolsBase.ORACLE_ARGUMENT_VALUE,
				DeploymentToolsBase.SQLSERVER_ARGUMENT_VALUE,
				DeploymentToolsBase.DB2_ARGUMENT_VALUE,
		}; 
		
		
		choiceRoot.add(seperateFieldRoot);
		
		description = "jdbc url";
        valueLabel = "jdbcUrl";
        option = Option.createOption(JDBC_URL_OPTION_ID, description, valueLabel);
        choiceRoot.add(option);
        
        root.add(choiceRoot);
		
		for(String databaseRequireInstance : databasesRequireInstance){
			OptionTrigger.add(
			        ConditionEnum.IF_FOUND, 
			        databaseTypeOption,
					databaseRequireInstance, 
					OptionTrigger.Action.MARK_AS_REQUIRED, 
					instanceOption.getParent());
		}
		options = new OptionDescriptorTreeImpl(root);
	}
	
	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getLongDescription()
	 */
	public String getLongDescription() {
		return getShortDescription();
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getName()
	 */
	public String getName() {
		return "importLocations";
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getOptions()
	 */
	public IOptionDescriptorTree getOptions() {
		return options;
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getShortDescription()
	 */
	public String getShortDescription() {
		return "Very simple tool to import locations.";
	}

}
