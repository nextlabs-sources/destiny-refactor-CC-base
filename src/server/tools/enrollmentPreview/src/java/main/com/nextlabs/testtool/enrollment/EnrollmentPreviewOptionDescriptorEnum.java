/*
 * Created on Feb 10, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import java.io.File;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.EnrollmentTypeEnumType;
import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/EnrollmentPreviewOptionDescriptorEnum.java#1 $
 */

public class EnrollmentPreviewOptionDescriptorEnum implements IConsoleApplicationDescriptor{
    static final OptionId<File> CONFIG_FILE_OPTIONS_ID = 
            new OptionId<File>("config", OptionValueType.EXIST_FILE);
    static final OptionId<Boolean> NO_GUI_OPTIONS_ID = 
            new OptionId<Boolean>("logonly", OptionValueType.BOOLEAN);
	static final OptionId<File> ROOT_FILE_OPTIONS_ID = 
	        new OptionId<File>("a", OptionValueType.EXIST_FILE);
	static final OptionId<File> DEFINITION_FILE_OPTIONS_ID = 
	        new OptionId<File>("d", OptionValueType.EXIST_FILE);
	static final OptionId<String> ENROLLMENT_TYPE_OPTION_ID = 
	        new OptionId<String>("t", OptionValueType.CUSTOM_LIST);
	static final OptionId<File> FILTER_FILE_OPTIONS_ID = 
	        new OptionId<File>("f", OptionValueType.FILE);
	static final OptionId<File> SELECTIVE_FILTER_FILE_OPTIONS_ID =
	        new OptionId<File>("sf", OptionValueType.FILE);
		
	private IOptionDescriptorTree treeRoot;
	
	EnrollmentPreviewOptionDescriptorEnum() throws InvalidOptionDescriptorException{
		SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();

		String valueLabel;
		String description;
		
		description = "configuration, defined checking values";
        valueLabel = "config.properties";
        root.add(Option.createOption(CONFIG_FILE_OPTIONS_ID, description, valueLabel, new File("config.properties")));
        
        root.add(Option.createOnOffOption(NO_GUI_OPTIONS_ID, 
                "This will lower the memory requirement but some features such as GUI and suggestions."));
		
		description = "ad_conndef";
		valueLabel = "ad_conndef";
		root.add(Option.createOption(ROOT_FILE_OPTIONS_ID, description, valueLabel));
		
		description = "definitionfile";
		valueLabel = "definitionfile";
		root.add(Option.createOption(DEFINITION_FILE_OPTIONS_ID, description, valueLabel));
		
		description = "enrollment type";
		valueLabel = CollectionUtils.asString(EnrollmentTypeEnumType.getElements(), "/");
		root.add(Option.createOption(ENROLLMENT_TYPE_OPTION_ID, description, valueLabel));
        for (EnrollmentTypeEnumType enumType : EnrollmentTypeEnumType.getElements()) {
            OptionValueType.CUSTOM_LIST.addCustomValue(ENROLLMENT_TYPE_OPTION_ID, enumType.getName());
        }
		
		    
		
		description = "filterfile";
		valueLabel = "filter file";
		root.add(Option.createOption(FILTER_FILE_OPTIONS_ID, description, valueLabel, null));
		
		description = "selectivefilterfolder";
		valueLabel = "selective filter file folder, usually it is the enrollmgr folder";
		root.add(Option.createOption(SELECTIVE_FILTER_FILE_OPTIONS_ID, 
						description,
						valueLabel,
						new File("C:\\Program File\\Nextlabs\\Policy Server\\tools\\enrollment\\")
		));
		
		treeRoot = new OptionDescriptorTreeImpl(root);
	}
	
	public String getLongDescription() {
		return "This tool can preivew the enrollment base on the conn and def files. "
		    + "It will show how each entry is classifed. If the entry is misclassifed, you can fix it before the enrollment. "
    		+ "It will also show all warning during enrollment sync."
	        + "Currently only Active Directory enrollment is support. "
		
		;
	}

	public String getName() {
		return "EnrollmentPreview";
	}

	public IOptionDescriptorTree getOptions() {
		return treeRoot;
	}

	public String getShortDescription() {
		return "A preview of enrollment configuration.";
	}

}
