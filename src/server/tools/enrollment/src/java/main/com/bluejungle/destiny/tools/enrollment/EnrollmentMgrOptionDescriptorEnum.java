/*
* Created on Aug 16, 2012
*
* All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
* San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
* worldwide.
*
* @author dwashburn
* @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/EnrollmentMgrOptionDescriptorEnum.java#1 $:
*/

package com.bluejungle.destiny.tools.enrollment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.bluejungle.framework.utils.CollectionUtils;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.OptionTrigger;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;
import com.nextlabs.shared.tools.impl.OptionTrigger.Condition;
import com.nextlabs.shared.tools.impl.OptionTrigger.ConditionEnum;

/**
 * TODO description
 *
 * @author hchan
 * @date Jul 5, 2007
 */
public class EnrollmentMgrOptionDescriptorEnum extends EnrollmentMgrSharedOptionDescriptorEnum {
    protected static final OptionId<Boolean> ENROLL_OPTIONS_ID          = OptionId.create("enroll", OptionValueType.ON_OFF);
    protected static final OptionId<Boolean> UPDATE_OPTIONS_ID          = OptionId.create("update", OptionValueType.ON_OFF);
    protected static final OptionId<Boolean> SYNC_OPTIONS_ID            = OptionId.create("sync",   OptionValueType.ON_OFF);
    protected static final OptionId<Boolean> DELETE_OPTIONS_ID          = OptionId.create("delete", OptionValueType.ON_OFF);    
    protected static final OptionId<Boolean> LIST_OPTIONS_ID            = OptionId.create("list",   OptionValueType.ON_OFF);
    protected static final OptionId<String>  TYPE_OPTIONS_ID            = OptionId.create("t",      OptionValueType.CUSTOM_LIST);
    protected static final OptionId<String>  DOMAIN_NAME_OPTIONS_ID     = OptionId.create("n",      OptionValueType.STRING);
    protected static final OptionId<File>    ROOT_FILE_OPTIONS_ID       = OptionId.create("a",      OptionValueType.EXIST_FILE);
    protected static final OptionId<File>    DEFINITION_FILE_OPTIONS_ID = OptionId.create("d",      OptionValueType.EXIST_FILE);
    protected static final OptionId<File>    FILTER_FILE_OPTIONS_ID     = OptionId.create("f",      OptionValueType.EXIST_FILE);
    protected static final OptionId<File>    DOMAINGROUP_FILE_OPTIONS_ID= OptionId.create("g",      OptionValueType.EXIST_FILE);
    
    
    static final Collection<OptionId<?>> ACTIONS;
    static{
        ACTIONS = new TreeSet<OptionId<?>>();
        ACTIONS.add(ENROLL_OPTIONS_ID);
        ACTIONS.add(UPDATE_OPTIONS_ID);
        ACTIONS.add(SYNC_OPTIONS_ID);
        ACTIONS.add(DELETE_OPTIONS_ID);
        ACTIONS.add(LIST_OPTIONS_ID);
    }
    /**
     *  - Unique
     - [Simple] # [h]
     - Sequence
         - Unique
             - Sequence
                 - Unique
                     - Sequence
                         - Unique
                             - [Simple] # [enroll]
                             - [Simple] # [update]
                         - Simple # t <Enrollment Type>
                         - [Simple] # [f] <filter file>
                         - Simple # a <ad_conndef>    //problem here
                         - Simple # d <definitionfile>
                         - Simple # g <domainGroupfile>
                     - Unique
                         - [Simple] # [sync]
                         - [Simple] # [delete]
                 - Simple # n <DomainName>
             - [Simple] # [list]
         - Simple # s <server>
         - Simple # p <port>
         - Simple # u <username>
         - [Simple] # [w] <password>
     */
    
    private final IOptionDescriptorTree treeRoot;

    protected EnrollmentMgrOptionDescriptorEnum() throws InvalidOptionDescriptorException {
        Option<?> option;
        
        SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();

        SequencedListOptionDescriptor enrollUpdateSeq = new SequencedListOptionDescriptor();

        UniqueChoiceOptionDescriptor enrollUpdateUnique = new UniqueChoiceOptionDescriptor();

        String description = "enroll";
        option = Option.createOnOffOption(ENROLL_OPTIONS_ID, 
                description);
        option.setLevel((byte)-50);
        enrollUpdateUnique.add(option);

        description = "update";
        option= Option.createOnOffOption(UPDATE_OPTIONS_ID, 
                description);
        option.setLevel((byte)-50);
        enrollUpdateUnique.add(option);
        enrollUpdateSeq.add(enrollUpdateUnique);

        Set<String> keys = EnrollmentMgr.ENROLLABLE_ENROLLMENT_TYPE.keySet();
        description = "type, [" + CollectionUtils.asString(keys, ",") + "]";
        String valueLabel = "Enrollment Type";
        Option<String> typeOption = Option.createOption(TYPE_OPTIONS_ID, 
                description, valueLabel);
        typeOption.setLevel((byte)-30);
        for(String key : keys){
            if (!EnrollmentMgr.UNKNOWN.equals(key)) {
                OptionValueType.CUSTOM_LIST.addCustomValue(TYPE_OPTIONS_ID, key);
            }
        }
        enrollUpdateSeq.add(typeOption);

       
        
        description = "connectionfile";
        valueLabel = "connection file";
        option = Option.createOption(ROOT_FILE_OPTIONS_ID, 
                description, valueLabel);
        enrollUpdateSeq.add(option);
        
        description = "definitionfile";
        valueLabel = "definition file";
        option = Option.createOption(DEFINITION_FILE_OPTIONS_ID, description, valueLabel);
        enrollUpdateSeq.add(option);
        
        description = "filterfile";
        valueLabel = "filter file";
        option = Option.createOption(FILTER_FILE_OPTIONS_ID, description, valueLabel, null);
        enrollUpdateSeq.add(option);
        
        UniqueChoiceOptionDescriptor syncDeleteUnique = new UniqueChoiceOptionDescriptor();
        
        description = "domaingroupfile";
        valueLabel = "domaingroup file";
        option = Option.createOption(DOMAINGROUP_FILE_OPTIONS_ID, description, valueLabel, null);
        enrollUpdateSeq.add(option);

        description = "sync";
        option = Option.createOnOffOption(SYNC_OPTIONS_ID, description);
        option.setLevel((byte)-50);
        syncDeleteUnique.add(option);

        description = "delete";
        option = Option.createOnOffOption(DELETE_OPTIONS_ID, description);
        option.setLevel((byte)-50);
        syncDeleteUnique.add(option);

        SequencedListOptionDescriptor updateEnrollSyncDeleteSeq = new SequencedListOptionDescriptor();
        UniqueChoiceOptionDescriptor updateEnrollSyncDeleteUnique = new UniqueChoiceOptionDescriptor();
        updateEnrollSyncDeleteUnique.add(enrollUpdateSeq);
        updateEnrollSyncDeleteUnique.add(syncDeleteUnique);
        updateEnrollSyncDeleteSeq.add(updateEnrollSyncDeleteUnique);

        UniqueChoiceOptionDescriptor updateEnrollSyncDeleteListUnique = new UniqueChoiceOptionDescriptor();
        updateEnrollSyncDeleteListUnique.add(updateEnrollSyncDeleteSeq);
        
        description = "list";
        Option<Boolean> listOption = Option.createOnOffOption(LIST_OPTIONS_ID, description);
        listOption.setLevel((byte)-50);
        updateEnrollSyncDeleteListUnique.add(listOption);
        
        root.add(updateEnrollSyncDeleteListUnique);
        
        description = "domain name";
        valueLabel = "DomainName";
        Option<String> domainNameOption = Option.createOption(DOMAIN_NAME_OPTIONS_ID, 
                description, valueLabel);
        root.add(domainNameOption);
        
        addSharedOptions(root);
        
        // -enroll -t ldif -a is optional
        List<Condition> conditions = new ArrayList<Condition>();
        conditions.add(new Condition(ENROLL_OPTIONS_ID, ConditionEnum.IF_FOUND));
        conditions.add(new Condition(typeOption, ConditionEnum.IF_FOUND, EnrollmentMgr.LDIF));
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_NOT_REQUIRED, ROOT_FILE_OPTIONS_ID);
        
        // -update -t ldif -a is optional
        conditions = new ArrayList<Condition>();
        conditions.add(new Condition(UPDATE_OPTIONS_ID, ConditionEnum.IF_FOUND));
        conditions.add(new Condition(typeOption, ConditionEnum.IF_FOUND, EnrollmentMgr.LDIF));
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_NOT_REQUIRED, ROOT_FILE_OPTIONS_ID);
        
        // -t DOMAINGROUP -g domainGroupConfigFile -g is required
        conditions = new ArrayList<Condition>();
        conditions.add(new Condition(ENROLL_OPTIONS_ID, ConditionEnum.IF_FOUND));
        conditions.add(new Condition(typeOption, ConditionEnum.IF_FOUND, EnrollmentMgr.DOMAINGROUP));
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_REQUIRED, DOMAINGROUP_FILE_OPTIONS_ID);        
        
        conditions = new ArrayList<Condition>();
        conditions.add(new Condition(UPDATE_OPTIONS_ID, ConditionEnum.IF_FOUND));
        conditions.add(new Condition(typeOption, ConditionEnum.IF_FOUND, EnrollmentMgr.DOMAINGROUP));
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_REQUIRED, DOMAINGROUP_FILE_OPTIONS_ID);
        
        // -enroll -t DOMAINGROUP -f filterfile, -a connection ala root file, and -d def file are not required
        conditions = new ArrayList<Condition>();
        conditions.add(new Condition(ENROLL_OPTIONS_ID, ConditionEnum.IF_FOUND));
        conditions.add(new Condition(typeOption, ConditionEnum.IF_FOUND, EnrollmentMgr.DOMAINGROUP));
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_NOT_REQUIRED, FILTER_FILE_OPTIONS_ID); 
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_NOT_REQUIRED, ROOT_FILE_OPTIONS_ID);
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_NOT_REQUIRED, DEFINITION_FILE_OPTIONS_ID);  
        
        // -update -t DOMAINGROUP -f filterfile, -a connection ala root file, and -d def file are not required
        conditions = new ArrayList<Condition>();
        conditions.add(new Condition(UPDATE_OPTIONS_ID, ConditionEnum.IF_FOUND));
        conditions.add(new Condition(typeOption, ConditionEnum.IF_FOUND, EnrollmentMgr.DOMAINGROUP));
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_NOT_REQUIRED, FILTER_FILE_OPTIONS_ID);
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_NOT_REQUIRED, ROOT_FILE_OPTIONS_ID);
        OptionTrigger.add(conditions, OptionTrigger.Action.MARK_AS_NOT_REQUIRED, DEFINITION_FILE_OPTIONS_ID);
        
        OptionTrigger.add(
                ConditionEnum.IF_FOUND, 
                LIST_OPTIONS_ID, 
                OptionTrigger.Action.MARK_AS_NOT_REQUIRED,
                DOMAIN_NAME_OPTIONS_ID);
        
        treeRoot = new OptionDescriptorTreeImpl(root);
    }
    
    protected EnrollmentMgrOptionDescriptorEnum(IOptionDescriptorTree treeRoot) {
        this.treeRoot = treeRoot;
    }
    
    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getLongDescription()
     */
    public String getLongDescription() {
        final String N = ConsoleDisplayHelper.NEWLINE;
        return "-------------------------------------------------------------------------" + N 
            + "\n"
            + "EnrollmentMgr Usage Example:" + N
            + "\n" 
            + " 1. Create Directory enrollment:" + N
            + "     enrollmgr -u username -enroll -t DIR -n DomainName " + N +
            "          -a file.conn -d file.def" + N 
            + "\n"
            + " 2. Update Directory enrollment:" + N
            + "    enrollmgr -u username -update -t DIR -n DomainName" + N
            + "        -a new_file.conn -d new_file.def" + N
            + "\n"
            + " 3. Sync enrollment:" + N
            + "    enrollmgr -u username -sync -n DomainName" + N 
            + "\n"
            + " 4. Delete enrollment:" + N
            + "    enrollmgr -u username -delete -n DomainName" + N 
            + "\n"
            + " 5. List enrollments:" + N
            + "    enrollmgr -u username -list" + N 
            + "\n"
            + "-------------------------------------------------------------------------" + N;
    }

    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getName()
     */
    public String getName() {
        return "enrollmgr";
    }

    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getOptions()
     */
    public IOptionDescriptorTree getOptions() {
        return treeRoot;
    }

    /* (non-Javadoc)
     * @see com.nextlabs.shared.tools.IConsoleApplicationDescriptor#getShortDescription()
     */
    public String getShortDescription() {
        return "Enrollment Manager";
    }

}
