package com.nextlabs.destiny.tools.keymanagement;

import java.io.File;

import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.IOptionDescriptorTree;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.OptionValueType;
import com.nextlabs.shared.tools.OptionValueType.OptionValueTypePrimitive;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.impl.Option;
import com.nextlabs.shared.tools.impl.OptionDescriptorTreeImpl;
import com.nextlabs.shared.tools.impl.OptionTrigger;
import com.nextlabs.shared.tools.impl.SequencedListOptionDescriptor;
import com.nextlabs.shared.tools.impl.UniqueChoiceOptionDescriptor;
import com.nextlabs.shared.tools.impl.OptionTrigger.ConditionEnum;

public class KeyManagementMgrSharedOptionDescriptorEnum implements IConsoleApplicationDescriptor{
    
    private static final OptionValueType<String> UPPERCASE_STRING = new OptionValueTypePrimitive<String>("STRING"){
        public boolean isValid(Object object) {
            return true;
        }

        public String getValue(OptionId<String> optionId, String value) {
            return value != null ? value.toUpperCase() : null;
        }
    };
    
    static final OptionId<String> KEYSTORE_PASSWORD_OPTION_ID = OptionId.create("z", OptionValueType.STRING);
    
    static final OptionId<Boolean> VERBOSE_OPTIONS_ID  = OptionId.create("v", OptionValueType.ON_OFF);
    
    static final OptionId<Boolean> LIST_KR_OPTIONS_ID      = OptionId.create("listKeyRing",   OptionValueType.ON_OFF);
    static final OptionId<Boolean> CREATE_KR_OPTIONS_ID    = OptionId.create("createKeyRing", OptionValueType.ON_OFF);
    static final OptionId<Boolean> DELETE_KR_OPTIONS_ID    = OptionId.create("deleteKeyRing", OptionValueType.ON_OFF);
    static final OptionId<Boolean> LIST_KEY_OPTIONS_ID     = OptionId.create("listKey",       OptionValueType.ON_OFF);
    static final OptionId<Boolean> GENERATE_KEY_OPTIONS_ID = OptionId.create("generateKey",   OptionValueType.ON_OFF);
    static final OptionId<Boolean> DELETE_KEY_OPTIONS_ID   = OptionId.create("deleteKey",     OptionValueType.ON_OFF);
    static final OptionId<Boolean> EXPORT_OPTIONS_ID       = OptionId.create("export",        OptionValueType.ON_OFF);
    static final OptionId<Boolean> EXPORT_ALL_OPTIONS_ID   = OptionId.create("exportAll",     OptionValueType.ON_OFF);
    static final OptionId<Boolean> IMPORT_OPTIONS_ID       = OptionId.create("import",        OptionValueType.ON_OFF);
    
    static final OptionId<String>  KR_NAME_OPTIONS_ID     = OptionId.create("keyRingName",  UPPERCASE_STRING);
    static final OptionId<String>  KEY_HASH_OPTIONS_ID    = OptionId.create("keyHash",      OptionValueType.STRING);
    static final OptionId<Integer> KEY_LENGTH_OPTIONS_ID  = OptionId.create("keyLength",    OptionValueType.INTEGER);
    static final OptionId<File>    FILE_OPTIONS_ID        = OptionId.create("file",         OptionValueType.FILE);
    static final OptionId<File>    FOLDER_OPTIONS_ID      = OptionId.create("folder",       OptionValueType.FOLDER);
    static final OptionId<String>  FILE_PWD_OPTIONS_ID    = OptionId.create("filePassword", OptionValueType.STRING);
    
    static final OptionId<String>  QUIET_OPTIONS_ID       = OptionId.create("quiet", OptionValueType.STRING);
    
    private final IOptionDescriptorTree treeRoot;

    protected KeyManagementMgrSharedOptionDescriptorEnum() throws InvalidOptionDescriptorException {
        Option<?> option;
        String description, valueLabel;
        
        SequencedListOptionDescriptor root = new SequencedListOptionDescriptor();

        description = "server hostname, default is localhost";
        valueLabel = "server";
        option = Option.createOption(HOST_OPTION_ID, description, valueLabel, "localhost");
        root.add(option);        
        
        description = "server port, default is 8443";
        valueLabel = "port";
        option = Option.createOption(PORT_OPTION_ID, description, valueLabel, 8443);
        root.add(option);
        
        description = "username";
        valueLabel = "username";
        option = Option.createOption(USER_ID_OPTION_ID, description, valueLabel);
        root.add(option);
        
        description = "password, if not specific in the command line. It will prompt and ask you";
        valueLabel = "password";
        option = Option.createOption(PASSWORD_OPTION_ID, description, valueLabel, null);
        root.add(option);

        description = "keystore password";
        valueLabel = "keystorePassword";
        option = Option.createOption(KEYSTORE_PASSWORD_OPTION_ID, description, valueLabel, "password");
        root.add(option);
        
        description = "verbose mode";
        option = Option.createOnOffOption(VERBOSE_OPTIONS_ID, description);
        root.add(option);
        
        UniqueChoiceOptionDescriptor operationRoot = new UniqueChoiceOptionDescriptor();
        description = "list all key ring names";
        option = Option.createOnOffOption(LIST_KR_OPTIONS_ID, description);
        operationRoot.add(option);
        
        description = "create a key ring";
        option = Option.createOnOffOption(CREATE_KR_OPTIONS_ID, description);
        operationRoot.add(option);
        
        description = "delete a key ring";
        option = Option.createOnOffOption(DELETE_KR_OPTIONS_ID, description);
        operationRoot.add(option);
        
        description = "list all key under a key ring";
        option = Option.createOnOffOption(LIST_KEY_OPTIONS_ID, description);
        operationRoot.add(option);
        
        description = "create a key";
        option = Option.createOnOffOption(GENERATE_KEY_OPTIONS_ID, description);
        operationRoot.add(option);
        
        description = "delete a key";
        option = Option.createOnOffOption(DELETE_KEY_OPTIONS_ID, description);
        operationRoot.add(option);
        
        description = "import a key ring from a file";
        option = Option.createOnOffOption(IMPORT_OPTIONS_ID, description);
        operationRoot.add(option);
        
        description = "export a key ring to a password protected file";
        option = Option.createOnOffOption(EXPORT_OPTIONS_ID, description);
        operationRoot.add(option);
        
        description = "export all the key rings to a folder. One keyring to one file. All the key rings are password protected";
        option = Option.createOnOffOption(EXPORT_ALL_OPTIONS_ID, description);
        operationRoot.add(option);
        
        root.add(operationRoot);
        
        description = "key ring name, the name will automatically convert to uppercase.";
        valueLabel = "keyRingName";
        option = Option.createOption(KR_NAME_OPTIONS_ID, valueLabel, description, null);
        root.add(option);
        
        description = "key hash in base64 format The input is case insensitive.";
        valueLabel = "keyHash";
        option = Option.createOption(KEY_HASH_OPTIONS_ID, valueLabel, description, null);
        root.add(option);
        
        description = "The key length in bytes. The default is 16";
        valueLabel = "keyLength";
        option = Option.createOption(KEY_LENGTH_OPTIONS_ID, valueLabel, description, 16);
        root.add(option);
        
        description = "A file location for import/export";
        valueLabel = "file";
        option = Option.createOption(FILE_OPTIONS_ID, valueLabel, description, null);
        root.add(option);
        
        description = "A folder location for export";
        valueLabel = "folder";
        option = Option.createOption(FOLDER_OPTIONS_ID, valueLabel, description, null);
        root.add(option);
        
        description = "A password for the import/export file. Optional";
        valueLabel = "filePassword";
        option = Option.createOption(FILE_PWD_OPTIONS_ID, valueLabel, description, null);
        root.add(option);
        
        OptionId[] requireKeyRingName = new OptionId[] {
                CREATE_KR_OPTIONS_ID
              , DELETE_KR_OPTIONS_ID
              , LIST_KEY_OPTIONS_ID
              , GENERATE_KEY_OPTIONS_ID
              , DELETE_KEY_OPTIONS_ID
              , EXPORT_OPTIONS_ID
        };
        
        for(OptionId optionId : requireKeyRingName) {
            OptionTrigger.add(
                    ConditionEnum.IF_FOUND
                  , optionId
                  , OptionTrigger.Action.MARK_AS_REQUIRED
                  , KR_NAME_OPTIONS_ID
            );
        }
        
        OptionTrigger.add(
                ConditionEnum.IF_FOUND
              , EXPORT_OPTIONS_ID
              , OptionTrigger.Action.MARK_AS_REQUIRED
              , FILE_OPTIONS_ID
        );
        
        OptionTrigger.add(
                ConditionEnum.IF_FOUND
              , IMPORT_OPTIONS_ID
              , OptionTrigger.Action.MARK_AS_REQUIRED
              , FILE_OPTIONS_ID
        );
        
        OptionTrigger.add(
                ConditionEnum.IF_FOUND
              , EXPORT_ALL_OPTIONS_ID
              , OptionTrigger.Action.MARK_AS_REQUIRED
              , FOLDER_OPTIONS_ID
        );
        
        treeRoot = new OptionDescriptorTreeImpl(root);
    }
    
    @Override
    public String getLongDescription() {
        final String N = ConsoleDisplayHelper.NEWLINE;
        final String NN = "\n" + N;
        final String I = "     "; // indent
        return "Example usage:" + N + N
             + " List all the key rings" + N
             + I + " -" + LIST_KR_OPTIONS_ID.getName() + NN
             + " Create a key ring with name 'MY_KEY_RING'." + N
             + I + " -" + CREATE_KR_OPTIONS_ID.getName() + " -" + KR_NAME_OPTIONS_ID + " MY_KEY_RING" + NN
             + " Delete a key ring with name 'MY_KEY_RING'." + N
             + I + " -" + DELETE_KR_OPTIONS_ID.getName() + " -" + KR_NAME_OPTIONS_ID + " MY_KEY_RING" + NN
             + " List keys in a key ring 'MY_KEY_RING'." + N
             + I + " -" + LIST_KEY_OPTIONS_ID.getName() + " -" + KR_NAME_OPTIONS_ID + " MY_KEY_RING" + NN
             + " Generate a key in a key ring 'MY_KEY_RING'." + N
             + I + " -" + GENERATE_KEY_OPTIONS_ID.getName() + " -" + KR_NAME_OPTIONS_ID + " MY_KEY_RING" + NN
             + " Generate a 16 bytes key in a key ring 'MY_KEY_RING'." + N
             + I + " -" + GENERATE_KEY_OPTIONS_ID.getName() + " -" + KR_NAME_OPTIONS_ID + " MY_KEY_RING -" + KEY_LENGTH_OPTIONS_ID + " 16" + NN
             + " Delete a key in a key ring 'MY_KEY_RING' with key hash is 12AB34CD." + N
             + I + " -" + DELETE_KEY_OPTIONS_ID.getName() + " -" + KR_NAME_OPTIONS_ID + " MY_KEY_RING -" + KEY_HASH_OPTIONS_ID + " 12AB34CD" + NN
             + " Export a key ring to a file 'kr.dat'. Password will be prompted." + N
             + I + " -" + EXPORT_OPTIONS_ID.getName() + " -" + KR_NAME_OPTIONS_ID + " MY_KEY_RING -" + FILE_OPTIONS_ID.getName() + " kr.dat" + NN
             + " Export a key ring to a file 'kr.dat' with password 'monkey'." + N
             + I + " -" + EXPORT_OPTIONS_ID.getName() + " -" + KR_NAME_OPTIONS_ID + " MY_KEY_RING -" + FILE_OPTIONS_ID.getName() + " kr.dat -" + FILE_PWD_OPTIONS_ID + " monkey" + NN
             + " Export all key ring into a folder 'keyrings'. Password will be prompted." + N
             + I + " -" + EXPORT_ALL_OPTIONS_ID.getName() + " -" + FOLDER_OPTIONS_ID.getName() + " keyrings" + NN
             + " Import a key ring from file 'kr.dat'. The keyring name will be read from the file." + N
             + I + " -" + IMPORT_OPTIONS_ID.getName() + " -" + FILE_OPTIONS_ID + " kr.dat" + NN
             + " Import a key ring from file 'kr.dat' to key ring 'NEW_KEY_RING'." + N
             + I + " -" + IMPORT_OPTIONS_ID.getName() + " -" + FILE_OPTIONS_ID + " kr.dat" + " -" + KR_NAME_OPTIONS_ID + " NEW_KEY_RING"
        ;
        
    }

    @Override
    public String getName() {
        return "keymanagement";
    }

    @Override
    public IOptionDescriptorTree getOptions() {
        return treeRoot;
    }

    @Override
    public String getShortDescription() {
        return "Key Management Manager";
    }
}

