package com.bluejungle.destiny.tools.enrollment;

import java.util.Collection;
import java.util.TreeSet;

import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.IOptionDescriptor;
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
 * TODO description
 *
 * @author hchan
 * @date Jul 5, 2007
 */
public class PropertyMgrOptionDescriptorEnum extends EnrollmentMgrSharedOptionDescriptorEnum{
	static final OptionId<Boolean> ADD_OPTION_ID 	= OptionId.create("add", OptionValueType.ON_OFF);
	static final OptionId<Boolean> DELETE_OPTION_ID = OptionId.create("delete", OptionValueType.ON_OFF);
	static final OptionId<Boolean> LIST_OPTION_ID 	= OptionId.create("list", OptionValueType.ON_OFF);
	
	static final OptionId<String> TYPE_OPTION_ID 			= OptionId.create("t", OptionValueType.CUSTOM_LIST);
	static final OptionId<String> LOGICAL_NAME_OPTION_ID 	= OptionId.create("l", OptionValueType.STRING);
	static final OptionId<String> DISPLAY_NAME_OPTION_ID 	= OptionId.create("i", OptionValueType.STRING);
	static final OptionId<String> ENTITY_TYPE_OPTION_ID 	= OptionId.create("e", OptionValueType.CUSTOM_LIST);
	
	static final Collection<OptionId<?>> ACTIONS;
	static{
		ACTIONS = new TreeSet<OptionId<?>>();
		ACTIONS.add(ADD_OPTION_ID);
		ACTIONS.add(DELETE_OPTION_ID);
		ACTIONS.add(LIST_OPTION_ID);
	}

	private final IOptionDescriptorTree treeRoot;
	
	PropertyMgrOptionDescriptorEnum() throws InvalidOptionDescriptorException {
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();

		SequencedListOptionDescriptor addSeq = new SequencedListOptionDescriptor();

		String description = "add";
		String valueLabel = null;
		IOptionDescriptor<?> option = Option.createOnOffOption(ADD_OPTION_ID, description);
		addSeq.add(option);

		description = "type [" + CollectionUtils.asString(PropertyMgr.COLUMN_TYPES, ",") + "]";
		valueLabel = "type";
		option = Option.createOption(TYPE_OPTION_ID, description,  valueLabel);
		for(String type : PropertyMgr.COLUMN_TYPES){
			OptionValueType.CUSTOM_LIST.addCustomValue(TYPE_OPTION_ID, type);
		}
		addSeq.add(option);
		
		description = "display name";
		valueLabel = "displayName";
		option = Option.createOption(DISPLAY_NAME_OPTION_ID, description, valueLabel);
		addSeq.add(option);
		
		UniqueChoiceOptionDescriptor addDeleteUnique = new UniqueChoiceOptionDescriptor();
		addDeleteUnique.add(addSeq);
		
		description = "delete";
		option = Option.createOnOffOption(DELETE_OPTION_ID, description);
		addDeleteUnique.add(option);
		
		SequencedListOptionDescriptor addDeleteSeq = new SequencedListOptionDescriptor();
		addDeleteSeq.add(addDeleteUnique);
		
		description = "logical name";
		valueLabel = "logicalname";
		option = Option.createOption(LOGICAL_NAME_OPTION_ID, description, valueLabel);
		addDeleteSeq.add(option);
		
		description = "entity type [" + CollectionUtils.asString(PropertyMgr.ENTITY_TYPES, ",")
				+ "]";
		valueLabel = "entitytype";
		option = Option.createOption(ENTITY_TYPE_OPTION_ID, description, valueLabel);
		for(String type : PropertyMgr.ENTITY_TYPES){
			OptionValueType.CUSTOM_LIST.addCustomValue(ENTITY_TYPE_OPTION_ID, type);
		}
		addDeleteSeq.add(option);
		
		UniqueChoiceOptionDescriptor addDeleteListUnique = new UniqueChoiceOptionDescriptor();
		addDeleteListUnique.add(addDeleteSeq);

		description = "list";
		option = Option.createOnOffOption(LIST_OPTION_ID, description);
		addDeleteListUnique.add(option);
		
		root.add(addDeleteListUnique);
		
		addSharedOptions(root);
		
		treeRoot = new OptionDescriptorTreeImpl(root);
	}

	public String getLongDescription() {
		final String N = ConsoleDisplayHelper.NEWLINE;
		return  "-------------------------------------------------------------------------" + N
			+ " " + N
            + "PropertyMgr Usage:" + N
            + "      Property operations: [ -add | -delete | -list ]" + N
            + " " + N
            + " 1. Add property:" + N
            + "    propertyMgr -add -s server -p port -u username -w password" + N
            + "                -t type -l logicalname -i displayname -e entitytype" + N
            + " " + N
            + " 2. Delete property:" + N
            + "    propertyMgr -delete -s server -p port -u username -w password" + N
            + "                -l logicalname -e entitytype" + N
            + " " + N
            + " 3. List property:" + N
            + "    propertyMgr -list -s server -p port -u username -w password" + N
            + " " + N
            + "-------------------------------------------------------------------------" + N;
	}

	public String getName() {
		return "propertyMgr";
	}

	public IOptionDescriptorTree getOptions() {
		return treeRoot;
	}

	public String getShortDescription() {
		return "Property Manager";
	}
}
