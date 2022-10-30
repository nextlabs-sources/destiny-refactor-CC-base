package com.nextlabs.destiny.tools.keymanagement;

import static com.nextlabs.destiny.tools.keymanagement.KeyManagementMgrSharedOptionDescriptorEnum.FILE_OPTIONS_ID;
import static com.nextlabs.destiny.tools.keymanagement.KeyManagementMgrSharedOptionDescriptorEnum.FILE_PWD_OPTIONS_ID;
import static com.nextlabs.destiny.tools.keymanagement.KeyManagementMgrSharedOptionDescriptorEnum.FOLDER_OPTIONS_ID;
import static com.nextlabs.destiny.tools.keymanagement.KeyManagementMgrSharedOptionDescriptorEnum.KEY_HASH_OPTIONS_ID;
import static com.nextlabs.destiny.tools.keymanagement.KeyManagementMgrSharedOptionDescriptorEnum.KEY_LENGTH_OPTIONS_ID;
import static com.nextlabs.destiny.tools.keymanagement.KeyManagementMgrSharedOptionDescriptorEnum.KR_NAME_OPTIONS_ID;
import static com.nextlabs.destiny.tools.keymanagement.KeyManagementMgrSharedOptionDescriptorEnum.VERBOSE_OPTIONS_ID;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore.PasswordProtection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.DestroyFailedException;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Stub;

import com.bluejungle.destiny.appframework.appsecurity.axis.AuthenticationContext;
import com.bluejungle.destiny.appframework.appsecurity.axis.ISecureSessionVault;
import com.bluejungle.destiny.appframework.appsecurity.axis.SecureSessionVaultGateway;
import com.bluejungle.destiny.types.secure_session.v1.SecureSession;
import com.bluejungle.framework.utils.CodecHelper;
import com.nextlabs.destiny.services.keymanagement.KeyManagementIF;
import com.nextlabs.destiny.services.keymanagement.KeyManagementServiceLocator;
import com.nextlabs.destiny.services.keymanagement.types.KeyDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyIdDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingDTO;
import com.nextlabs.destiny.services.keymanagement.types.KeyRingWithKeysDTO;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.OptionId;
import com.nextlabs.shared.tools.SecureConsolePrompt;
import com.nextlabs.shared.tools.display.ConsoleDisplayHelper;
import com.nextlabs.shared.tools.impl.InteractiveQuestion;

public class KeyManagementMgr extends ConsoleApplicationBase{
    
    private static final int REQUEST_TIMEOUT = Integer.parseInt(
            System.getProperty("com.bluejungle.agent.sockettimeout", "1200000"));

    public static final String KM_TOOL_HOME = "KM_TOOL_HOME";
    
    public static final String KM_TOOL_KEYSTORE_PASSWORD = "KM_TOOL_KEYSTORE_PASSWORD";    

    protected static final String SOCKET_FACTORY_PROPERTY = "axis.socketSecureFactory";
    
    private static final String URL = "https://%s:%d/dkms/services/KeyManagementIFPort";

    private KeyManagementServiceLocator locator;
    
    protected KeyManagementServiceWrapper keyManagementWS;
    
    private final KeyManagementMgrSharedOptionDescriptorEnum ode;
    
    private boolean verboseMode = false;
    
    public static void main(String[] args){
        try{
            KeyManagementMgr mgr = new KeyManagementMgr();
            mgr.parseAndExecute(args);
        }catch(Exception e){
            e.printStackTrace();
        }    
    }
    
    protected KeyManagementMgr(){
        System.setProperty(SOCKET_FACTORY_PROPERTY, 
                KeyManagementMgrSocketFactory.class.getName());
        ode = new KeyManagementMgrSharedOptionDescriptorEnum();
        
        // must initialize all the optionid to map
        Action.values();
    }

    protected void authenticate(ICommandLine commandLine) throws ServiceException, IOException {
        String server = getValue(commandLine,
                IConsoleApplicationDescriptor.HOST_OPTION_ID);
        Integer port = getValue(commandLine,
                KeyManagementMgrSharedOptionDescriptorEnum.PORT_OPTION_ID);
        String authUser = getValue(commandLine,
                KeyManagementMgrSharedOptionDescriptorEnum.USER_ID_OPTION_ID);
        String authPwd = getValue(commandLine,
                KeyManagementMgrSharedOptionDescriptorEnum.PASSWORD_OPTION_ID);

        if (authPwd == null) {
            SecureConsolePrompt securePrompt = new SecureConsolePrompt("Password: ");
            authPwd = new String(securePrompt.readConsoleSecure());
        }
        
        String keyStorePassword = getValue(commandLine,
        		KeyManagementMgrSharedOptionDescriptorEnum.KEYSTORE_PASSWORD_OPTION_ID);
        System.setProperty(KM_TOOL_KEYSTORE_PASSWORD, keyStorePassword);


        // Set authentication information in SOAP envelope header
        SecureSessionVaultGateway.setSecureSessionVault(new ISecureSessionVault(){
            private SecureSession secureSession;

            public void storeSecureSession(SecureSession secureSession) {
                this.secureSession = secureSession;
            }

            public SecureSession getSecureSession() {
                return secureSession;
            }

            public void clearSecureSession() {
                secureSession = null;
            }
        });
        AuthenticationContext authContext = AuthenticationContext.getCurrentContext();
        authContext.setUsername(authUser);
        authContext.setPassword(authPwd);

        String url = String.format(URL, server, port);
        System.out.println("Connecting to " + url);

        this.locator = new KeyManagementServiceLocator();
        this.locator.setKeyManagementIFPortEndpointAddress(url);
        KeyManagementIF keyManagementService = locator.getKeyManagementIFPort();
        ((Stub) keyManagementService).setTimeout(REQUEST_TIMEOUT);
        keyManagementWS = new KeyManagementServiceWrapper(keyManagementService);
    }
    
    void print(KeyRingDTO keyRing) {
        System.out.println("Name: " + keyRing.getName());
        System.out.println("CreateDate: " + new Date(keyRing.getCreateDate()));
        System.out.println("LastModifiedDate: " + new Date(keyRing.getLastModifiedDate()));
        KeyIdDTO[] keyIds = keyRing.getKeyIds();

        int numberOfKeys = keyRing.getKeyIds() != null ? keyRing.getKeyIds().length: 0;
        System.out.println("# of key: " + numberOfKeys);
        System.out.println();
        if(numberOfKeys >0 ){
            System.out.println("List of key ids");
            System.out.println(" index | | key hash");
            System.out.println("---------------------------------------");

            Arrays.sort(keyIds, new Comparator<KeyIdDTO>() {
                public int compare(KeyIdDTO k1, KeyIdDTO k2) {
                    return ((Long)k1.getTimestamp()).compareTo(k2.getTimestamp());
                }
            });

            for (int i = 0; i < numberOfKeys; i++) {
                System.out.println(String.format("%6d:  %s" , i , convertToString(keyIds[i])));
            }
        }
    }
    
    private static final class AbortException extends RuntimeException {
        
    }
    
    private static final Map<OptionId<?>, Action> OPTION_TO_ACTION = new HashMap<OptionId<?>, KeyManagementMgr.Action>();
    
    private enum Action {
        LIST_KEY_RING(KeyManagementMgrSharedOptionDescriptorEnum.LIST_KR_OPTIONS_ID) {
            @Override
            void run(ICommandLine commandLine, KeyManagementMgr mgr)
                    throws KeyManagementMgrException {
                System.out.println("Listing key ring names");
                Set<String> keyRingNames = mgr.keyManagementWS.getKeyRingNames();
                
                System.out.println("Found " + keyRingNames.size() + " key rings");
                
                int i = 0;
                for (String keyRingName : keyRingNames) {
                    System.out.println(String.format("  %4d: %s", ++i, keyRingName));
                }
            }
        },
        CREATE_KEY_RING(KeyManagementMgrSharedOptionDescriptorEnum.CREATE_KR_OPTIONS_ID) {
            @Override
            void run(ICommandLine commandLine, KeyManagementMgr mgr)
                    throws KeyManagementMgrException {
                String keyRingName = mgr.getValue(commandLine, KR_NAME_OPTIONS_ID);
                
                if (mgr.checkKeyRingExist(keyRingName)) {
                    System.out.println("Key ring '" + keyRingName + "' already exists.");
                    return;
                }
                
                System.out.println("Creating a key ring with name '" + keyRingName + "'");
                
                KeyRingDTO justCreated = mgr.keyManagementWS.createKeyRing(keyRingName);
                mgr.print(justCreated);
            }
        },
        DELETE_KEY_RING(KeyManagementMgrSharedOptionDescriptorEnum.DELETE_KR_OPTIONS_ID){
            @Override
            void run(ICommandLine commandLine, KeyManagementMgr mgr)
                    throws KeyManagementMgrException, IOException {
                String keyRingName = mgr.getValue(commandLine, KR_NAME_OPTIONS_ID);
                
                if (!mgr.checkKeyRingExist(keyRingName)) {
                    System.out.println("Key ring '" + keyRingName + "' does not exist.");
                    return;
                }
                
                System.out.println("Deleting a key ring with name '" + keyRingName + "'");
                
                String answer = InteractiveQuestion.prompt("Do you want to continue? (Y/N)");
                if (!answer.trim().equalsIgnoreCase("Y")) {
                    throw new AbortException();
                }
                
                mgr.keyManagementWS.deleteKeyRing(keyRingName);
            }
        },
        
        LIST_KEY(KeyManagementMgrSharedOptionDescriptorEnum.LIST_KEY_OPTIONS_ID) {
            @Override
            void run(ICommandLine commandLine, KeyManagementMgr mgr)
                    throws KeyManagementMgrException {
                String keyRingName = mgr.getValue(commandLine, KR_NAME_OPTIONS_ID);
                
                System.out.println("Listing a key ring with name '" + keyRingName + "'");
                
                KeyRingDTO keyRing = mgr.keyManagementWS.getKeyRing(keyRingName);
                mgr.print(keyRing);
            }
        },
        GENERATE_KEY(KeyManagementMgrSharedOptionDescriptorEnum.GENERATE_KEY_OPTIONS_ID) {
            @Override
            void run(ICommandLine commandLine, KeyManagementMgr mgr)
                    throws KeyManagementMgrException {
                String keyRingName = mgr.getValue(commandLine, KR_NAME_OPTIONS_ID);
                
                int keyLength = mgr.getValue(commandLine, KEY_LENGTH_OPTIONS_ID);
                
                System.out.println("Generating a " + keyLength
                        + " byte key in key ring '" + keyRingName + "'");
                
                KeyIdDTO keyId = mgr.keyManagementWS.generateKey(keyRingName, keyLength * 8);
                System.out.println("Key Hash: " + mgr.convertToString(keyId));
            }
        },
        DELETE_KEY(KeyManagementMgrSharedOptionDescriptorEnum.DELETE_KEY_OPTIONS_ID) {
            @Override
            void run(ICommandLine commandLine, KeyManagementMgr mgr)
                    throws KeyManagementMgrException, IOException {
                String keyRingName = mgr.getValue(commandLine, KR_NAME_OPTIONS_ID);
                
                String keyHash16 = mgr.getValue(commandLine, KEY_HASH_OPTIONS_ID);
                
                KeyIdDTO keyId;
                
                if(keyHash16 == null){
                    KeyRingDTO keyRing = mgr.keyManagementWS.getKeyRing(keyRingName);
                    mgr.print(keyRing);
                    KeyIdDTO[] keyIds = keyRing.getKeyIds();
                    
                    if(keyIds == null || keyIds.length == 0){
                        System.out.println("No key ring on the server.");
                        return;
                    }else {
                        
                        String answer = InteractiveQuestion.prompt(
                                "Key index to delete? (0-" + (keyIds.length - 1) + ")");
                        
                        
                        int index = Integer.parseInt(answer);

                        if (index < 0 || index >= keyIds.length) {
                            System.out.println("Invalid index: " + index);
                            throw new AbortException();
                        }
                        
                        keyId = keyIds[index];
                        keyHash16 = mgr.convertToString(keyId);
                    }
                } else {
                    keyId = mgr.convertToKeyIdDTO(keyHash16);
                    
                    mgr.keyManagementWS.getKey(keyRingName, keyId);
                }
                
                System.out.println("Deleting a key with key hash " + keyHash16
                        + " in key ring '" + keyRingName + "'");
                
                String answer = InteractiveQuestion.prompt("Do you want to continue? (Y/N)");
                if (!answer.trim().equalsIgnoreCase("Y")) {
                    throw new AbortException();
                }
                
                
                mgr.keyManagementWS.deleteKey(keyRingName, keyId);
            }
        },
        EXPORT (KeyManagementMgrSharedOptionDescriptorEnum.EXPORT_OPTIONS_ID) {

            @Override
            void run(ICommandLine commandLine, KeyManagementMgr mgr)
                    throws KeyManagementMgrException, IOException {
                String keyRingName = mgr.getValue(commandLine, KR_NAME_OPTIONS_ID);
                
                File outputFile = mgr.getValue(commandLine, FILE_OPTIONS_ID);
                
                System.out.println("Exporting key ring '" + keyRingName + "' to file '" + outputFile.getAbsolutePath() + "'" );
                
                if (outputFile.exists()) {
                    String answer = InteractiveQuestion.prompt("The output file already exists. Do you want to overwrite? (Y/N)");
                    if (!answer.trim().equalsIgnoreCase("Y")) {
                        throw new AbortException();
                    }
                }
                
                KeyRingWithKeysDTO keyRing = mgr.keyManagementWS.getKeyRingWithKeys(keyRingName);
                
                String keyRingPassword = mgr.getValue(commandLine, FILE_PWD_OPTIONS_ID);
                if (keyRingPassword == null) {
                    SecureConsolePrompt securePrompt = new SecureConsolePrompt("Key ring Password: ");
                    keyRingPassword = new String(securePrompt.readConsoleSecure());
                }
                
                PasswordProtection password = new PasswordProtection(keyRingPassword.toCharArray());
                try {
                    KeyRingFileHelper.save(keyRing, password, outputFile);
                } finally {
                    try {
                        password.destroy();
                    } catch (DestroyFailedException e) {
                        // ignore
                    }
                }
            }
        },
        EXPORT_ALL (KeyManagementMgrSharedOptionDescriptorEnum.EXPORT_ALL_OPTIONS_ID) {

            @Override
            void run(ICommandLine commandLine, KeyManagementMgr mgr)
                    throws KeyManagementMgrException, IOException {
                File outputFolder = mgr.getValue(commandLine, FOLDER_OPTIONS_ID);
                
                System.out.println("Exporting all key rings to folder '" + outputFolder.getAbsolutePath() + "'" );
                
                if(!outputFolder.exists()){
                    if (!outputFolder.mkdirs()) {
                        System.err.println("Can't create the output folder '" + outputFolder.getAbsolutePath() + "'" );
                    }
                }
                
                List<KeyRingWithKeysDTO> keyrings = mgr.keyManagementWS.getAllKeyRingsWithKeys();
                
                if (keyrings.isEmpty()) {
                    System.out.println("There is no key ring on the server.");
                    return;
                }
                
                String keyRingPassword = mgr.getValue(commandLine, FILE_PWD_OPTIONS_ID);
                if (keyRingPassword == null) {
                    SecureConsolePrompt securePrompt = new SecureConsolePrompt("Key ring Password: ");
                    keyRingPassword = new String(securePrompt.readConsoleSecure());
                }
                
                
                
                for(KeyRingWithKeysDTO keyRing : keyrings) {
                    File outputFile = new File(outputFolder, keyRing.getName() + ".keyring");
                    
                    if (outputFile.exists()) {
                        String answer = InteractiveQuestion.prompt("The output file '" + outputFile.getAbsolutePath() + "'already exists. Do you want to overwrite? (Y/N)");
                        if (!answer.trim().equalsIgnoreCase("Y")) {
                            System.out.println("KeyRing '" + keyRing.getName() + "' is skipped.");
                            continue;
                        }
                    }
                    
                    PasswordProtection password = new PasswordProtection(keyRingPassword.toCharArray());
                    try {
                        KeyRingFileHelper.save(keyRing, password, outputFile);
                    } finally {
                        try {
                            password.destroy();
                        } catch (DestroyFailedException e) {
                            // ignore
                        }
                    }
                    
                    System.out.println("KeyRing '" + keyRing.getName() + "' is exported to a file '" + outputFile.getAbsolutePath() + "'");
                }                
            }
        },
        IMPORT (KeyManagementMgrSharedOptionDescriptorEnum.IMPORT_OPTIONS_ID) {

            @Override
            void run(ICommandLine commandLine, KeyManagementMgr mgr)
                    throws KeyManagementMgrException, IOException {
                String keyRingName = mgr.getValue(commandLine, KR_NAME_OPTIONS_ID);
                
                File inputFile = mgr.getValue(commandLine, FILE_OPTIONS_ID);
                
                if(keyRingName != null) {
                    System.out.println("Importing file '" + inputFile.getAbsolutePath() + "' to key ring '" + keyRingName + "'" );
                } else {
                    System.out.println("Importing file '" + inputFile.getAbsolutePath() + "'" );
                }
                
                
                
                String keyRingPassword = mgr.getValue(commandLine, FILE_PWD_OPTIONS_ID);
                if (keyRingPassword == null) {
                    SecureConsolePrompt securePrompt = new SecureConsolePrompt("Key ring Password: ");
                    keyRingPassword = new String(securePrompt.readConsoleSecure());
                }
                
                
                
                PasswordProtection password = new PasswordProtection(keyRingPassword.toCharArray());
                KeyRingWithKeysDTO keyRing;
                try {
                    keyRing = KeyRingFileHelper.load(inputFile, password);
                } finally {
                    try {
                        password.destroy();
                    } catch (DestroyFailedException e) {
                        // ignore
                    }
                }
                
                if (keyRingName == null) {
                    keyRingName = keyRing.getName();
                    System.out.println("Importing file to key ring '" + keyRingName + "'" );
                }
                
                if (mgr.checkKeyRingExist(keyRingName)) {
                    String answer = InteractiveQuestion.prompt(
                            "The key ring '" + keyRingName + "' already exists. Do you want to overwrite? (Y/N)");
                    if (!answer.trim().equalsIgnoreCase("Y")) {
                        throw new AbortException();
                    }
                    
                    mgr.keyManagementWS.deleteKeyRing(keyRingName);
                }
                
                System.out.println("Importing " + keyRing.getKeys().length + " keys to key ring '" + keyRingName + "'");
                
                mgr.keyManagementWS.createKeyRing(keyRingName);
                
                //TODO create a batch operation
                for (KeyDTO keyDTO : keyRing.getKeys()) {
                    mgr.keyManagementWS.setKey(keyRingName, keyDTO);
                }
            }
        },
        ;
        
        final OptionId<?> optionId;
        
        Action(OptionId<?> optionId){
            this.optionId = optionId;
            OPTION_TO_ACTION.put(optionId, this);
        }
        
        abstract void run(ICommandLine commandLine, KeyManagementMgr mgr) throws Exception;

    }
    
    /**
     * 
     * @param name
     * @return true if exist
     * @throws KeyManagementMgrException 
     */
    private boolean checkKeyRingExist(String name) throws KeyManagementMgrException {
        Set<String> names = keyManagementWS.getKeyRingNames();
        return names.contains(name);
    }
    
    private KeyIdDTO convertToKeyIdDTO(String string) {
        // the last 8 characters are timestamp
        
        String keyIdHash16 = string.substring(0, string.length() - 8);
        String timestamp16 = string.substring(string.length() - 8);
        
        return new KeyIdDTO(
                CodecHelper.base16Decode(keyIdHash16)
              , Integer.parseInt(timestamp16, 16) * 1000L);
    }
    
    private String convertToString(KeyIdDTO keyId) {
        String timestampStr = Integer.toHexString((int) (keyId.getTimestamp() / 1000));

        // pad with zero
        for (int i = timestampStr.length(); i < 8; i++) {
            timestampStr = "0" + timestampStr;
        }

        return CodecHelper.base16Encode(keyId.getHash()) + timestampStr;
    }
    
    protected void exec(ICommandLine commandLine) throws Exception {
        if (commandLine.isOptionExist(VERBOSE_OPTIONS_ID)) {
            verboseMode = true;
            System.out.println ("Verbose mode ON");
        }
        
        
        authenticate(commandLine);
        
        
        Action action = null;
        for (OptionId id : OPTION_TO_ACTION.keySet()) {
            if (commandLine.isOptionExist(id)) {
                action = OPTION_TO_ACTION.get(id);
                break;
            }
        }
        if (action == null) {
            throw new KeyManagementMgrException("unknown action");
        }
        
        try {
            action.run(commandLine, this);
            System.out.println("Done.");
        } catch (AbortException e) {
            System.out.println("Abort.");
        }
        
        AuthenticationContext.clearCurrentContext();
    }
    
    @Override
    protected void execute(ICommandLine commandLine) {
        try {
            exec(commandLine);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    protected void handleException(Exception e){
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("KeyManagement error: ");
        errorMessage.append(e.getMessage() != null ? e.getMessage() : e.toString());
        if (e.getCause() != null) {
            errorMessage.append(ConsoleDisplayHelper.NEWLINE).append(e.getCause());
        }
        System.err.println(errorMessage);
        
        if (verboseMode) {
            e.printStackTrace();
            if(e.getCause() != null && e.getCause() != e){
                e.getCause().printStackTrace();
            }
        }
    }
    
    
    @Override
    protected IConsoleApplicationDescriptor getDescriptor() {
        return ode;
    }
}

