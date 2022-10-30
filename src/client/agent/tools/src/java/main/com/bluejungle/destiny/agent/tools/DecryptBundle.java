/*
 * Created on Dec 15, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.bluejungle.destiny.agent.controlmanager.AgentKeyManagerComponentBuilder;
import com.bluejungle.destiny.agent.controlmanager.IAgentKeyManagerComponentBuilder;
import com.bluejungle.destiny.agent.profile.IProfileManager;
import com.bluejungle.destiny.agent.profile.ProfileManager;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.securestore.ISecureStore;
import com.bluejungle.framework.securestore.SecureFileStore;
import com.bluejungle.framework.utils.CryptUtils;
import com.bluejungle.pf.destiny.lib.ClientInformationDTO;
import com.bluejungle.pf.destiny.lib.RegularExpressionDTO;
import com.bluejungle.pf.domain.destiny.deployment.InvalidBundleException;
import com.bluejungle.pf.engine.destiny.BundleVaultException;
import com.bluejungle.pf.engine.destiny.BundleVaultImpl;
import com.bluejungle.pf.engine.destiny.IBundleVault;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.SecureConsolePrompt;

/**
 * This tool is used to decrypt the bundle for debug purposes
 * 
 * @author sgoldstein
 * @version $Id:
 *          //depot/branch/Destiny_1.5.1/main/src/client/agent/tools/src/java/main/com/bluejungle/destiny/agent/tools/DecryptBundle.java#1 $
 */

public class DecryptBundle extends ConsoleApplicationBase {
    private static final String DIGEST_ALGORITM = "SHA1";
    private static final int MAX_NUMBER_AUTHENTICATION_ATTEMPTS = 3;

    private byte[] passwordHash;

    private final IConsoleApplicationDescriptor options;

    DecryptBundle() throws InvalidOptionDescriptorException {
        options = new DecryptBundleOptionDescriptorEnum();
    }
 
 
    @Override
    protected boolean printHelpIfNoArgs() {
        return false;
    }

    public static void main(String[] args) {
        try {
            for (String arg: args) {
                System.out.println("Arg: " + arg);
            }
            
            DecryptBundle decrypt = new DecryptBundle();
            decrypt.parseAndExecute(args);
        } catch (Exception e) {
            printException(e);
        }
    }

    private void extractBundle(String bundleFilename, String outputFilename, String enforcerHome, String encoding)
        throws Exception {
        System.out.println("Extracting bundle: " + bundleFilename);
        File bundleFile = new File(bundleFilename);
        if (!bundleFile.exists()) {
            throw new Exception("Failed - Bundle file not found.");
        }
        IComponentManager componentManager = ComponentManagerFactory.getComponentManager();

        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(AgentKeyManagerComponentBuilder.AGENT_HOME_DIRECTORY_PROPERTY_NAME,
                           enforcerHome);
        ComponentInfo<IAgentKeyManagerComponentBuilder> info = 
            new ComponentInfo<IAgentKeyManagerComponentBuilder>(
                IAgentKeyManagerComponentBuilder.COMPONENT_NAME,
                AgentKeyManagerComponentBuilder.class,
                IAgentKeyManagerComponentBuilder.class, 
                LifestyleType.SINGLETON_TYPE,
                config);
        IAgentKeyManagerComponentBuilder keyManagerBuilder = componentManager.getComponent(info);
        keyManagerBuilder.buildRegisteredKeyManager();

        config = new HashMapConfiguration();
        config.setProperty(BundleVaultImpl.BUNDLE_FILE_PROPERTY_NAME, bundleFilename);
        ComponentInfo<BundleVaultImpl> bundleVaultInfo = 
            new ComponentInfo<BundleVaultImpl>(
                IBundleVault.COMPONENT_NAME,
                BundleVaultImpl.class, 
                IBundleVault.class,
                LifestyleType.SINGLETON_TYPE,
                config);
        IBundleVault bundleVault = componentManager.getComponent(bundleVaultInfo);
  
        try {
            IBundleVault.BundleInfo bundleInfo = bundleVault.getBundleInfo();
            String[] uids = bundleInfo.getSubjects();
            if (uids != null && uids.length != 0) {
                System.out.print("The bundle has been prepared for these UIDs: ");
                boolean isFirst = true;
                for (String uid : uids) {
                    if (isFirst) {
                        System.out.print(", ");
                    } else {
                        isFirst = false;
                    }
                    System.out.print(uid);
                }
                System.out.println();
            }
            System.out.println("Bundle read.  Writing to: " + outputFilename);
            File outputFile = new File(outputFilename);
            outputFile.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(outputFile, false);
            PrintStream outputPrintStream = new PrintStream(outputStream, true, encoding);
            outputPrintStream.print(bundleInfo.getBundle().toString());

            System.out.println("Process Complete!");
        } catch (BundleVaultException exception) {
            throw new Exception ("Failed - Bundle file coult not be read.", exception);
        } catch (InvalidBundleException exception) {
            throw new Exception ("Failed - Bundle file not found.", exception);
        } catch (IOException exception) {
            throw new Exception ("Failed - Could not write output file.", exception);
        }
  
    }

    private void extractClients(String enforcerHome) throws IOException {
        File base = new File(enforcerHome);
        File inFile = new File(base, "clientinfo.bin");
        File outFile = new File(base, "clientinfo.txt");
        if (!inFile.exists() || !inFile.canRead()) {
            System.out.println("Client information is not available.");
            return;
        }
        if (outFile.exists() && !outFile.canWrite()) {
            System.out.println("Unable to create an output file for client information.");
            return;
        }
        ISecureStore<ClientInformationDTO> store = new SecureFileStore<ClientInformationDTO>(inFile, "bundleKey");
        ClientInformationDTO dto = store.read();
        FileWriter out = new FileWriter(outFile);
        out.append(dto.toString());
        out.flush();
        out.close();
    }

    private void extractRegularExpression(String enforcerHome) throws IOException {
        File base = new File(enforcerHome);
        File inFile = new File(base, "regexp.bin");
        File outFile = new File(base, "regexp.txt");
        if (!inFile.exists() || !inFile.canRead()) {
            System.out.println("Regular expression information is not available.");
            return;
        }
        if (outFile.exists() && !outFile.canWrite()) {
            System.out.println("Unable to create an output file for regular expression information.");
            return;
        }
        ISecureStore<RegularExpressionDTO> store = new SecureFileStore<RegularExpressionDTO>(inFile, "bundleKey");
        RegularExpressionDTO dto = store.read();
        FileWriter out = new FileWriter(outFile);
        out.append(dto.toString());
        out.flush();
        out.close();
    }

    /**
     */
    private final boolean authenticateUser() throws Exception {        
        boolean valueToReturn = false;
        
        try {
            for (int i = 0; ((i < MAX_NUMBER_AUTHENTICATION_ATTEMPTS) && (!valueToReturn)); i++) {
                SecureConsolePrompt securePrompt = new SecureConsolePrompt("Password: ");
                char[] password = securePrompt.readConsoleSecure();
                valueToReturn = isPasswordMatch(new String(password));
                if (!valueToReturn) {
                    throw new Exception("Passowrd Incorrect");
                }
            }
        } catch (NoSuchAlgorithmException exception) {
            throw new Exception("Failed - Could not validate password.", exception);
        } catch (IOException exception) {
            throw new Exception("Failed - Could not read password.", exception);
        }

        return valueToReturn;
    }
    
    private final boolean isPasswordMatch(String userInput) throws NoSuchAlgorithmException {
        byte[] passwordToVerify = CryptUtils.digest(userInput, DIGEST_ALGORITM, 0);
        return Arrays.equals(this.passwordHash, passwordToVerify);
    }

    @Override
    protected void execute(ICommandLine commandLine) {
        System.out.println("\nStarting Decrypt...\n");
        try {
            String bundleFilename = getValue(commandLine,
                                             DecryptBundleOptionDescriptorEnum.BUNDLE_FILENAME_OPTION_ID);
            String outputFilename = getValue(commandLine,
                                             DecryptBundleOptionDescriptorEnum.OUTPUT_FILENAME_OPTION_ID);
            String agentHome = getValue(commandLine,
                                        DecryptBundleOptionDescriptorEnum.AGENT_HOME_OPTION_ID);
            String password = getValue(commandLine, 
                                       DecryptBundleOptionDescriptorEnum.PASSWORD_OPTION_ID);
            String encoding = getValue(commandLine,
                                       DecryptBundleOptionDescriptorEnum.ENCODING_OPTION_ID);
            
            // check if agent home exists
            File agentHomeFile = new File(agentHome);
            if (!agentHomeFile.exists()) {
                File temp = new File("temp");
                File parent = temp.getCanonicalFile().getParentFile();
                if (parent != null && parent.isDirectory()) {
                    agentHome = parent.getAbsolutePath();
                }
            }
            if (!new File(agentHome).exists()) {
                System.err.println("Agent home \"" + agentHome +"\" doesn't exist.");
                return;
            }

            HashMapConfiguration configuration = new HashMapConfiguration();
            configuration.setProperty(ProfileManager.BASE_DIR_PROPERTY_NAME, agentHome);
            IComponentManager componentManager = ComponentManagerFactory.getComponentManager();
            ComponentInfo<IProfileManager> info = 
                new ComponentInfo<IProfileManager>(
                    IProfileManager.NAME, 
                    ProfileManager.class, 
                    IProfileManager.class, 
                    LifestyleType.SINGLETON_TYPE,
                    configuration);
            IProfileManager profileManager = componentManager.getComponent(info);
            this.passwordHash = profileManager.getCommunicationProfile().getPasswordHash();

            boolean isAuthenticated = false;
            if (password != null) {
                isAuthenticated = isPasswordMatch(password);
            } else {
                isAuthenticated = authenticateUser();
            }
   
            if (isAuthenticated) {
                extractBundle(bundleFilename, outputFilename, agentHome, encoding == null ? "UTF16" : encoding);
                extractClients(agentHome);
                extractRegularExpression(agentHome);
            } else {
                System.out.println("User not authenticated.  Exiting.");
            }  
        } catch (Exception e) {
            printException(e);
        }
    }

    @Override
    protected IConsoleApplicationDescriptor getDescriptor() {
        return options;
    }
}
