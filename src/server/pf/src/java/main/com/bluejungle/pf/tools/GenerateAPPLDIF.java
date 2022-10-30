/*
 * Created Jul 11, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.pf.tools;

import java.io.File;

import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.ldap.tools.misc.ImportCategoryEnumType;
import com.bluejungle.ldap.tools.misc.NamespaceIDGenerator;
import com.bluejungle.ldap.tools.misc.RelativeIDException;
import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;

/**
 * @author pkeni
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/tools/GenerateAPPLDIF.java#1 $
 */

public class GenerateAPPLDIF extends ConsoleApplicationBase{
	private static final int ERROR_STATUS = -1;
	private static final int SUCCESS_STATUS = 0;
	private static final int INPUT_ERROR_STATUS = 1;
	
    private IConsoleApplicationDescriptor optionRoot;
    
    private String errorMessage;
    
    private GenerateAPPLDIF() throws InvalidOptionDescriptorException{
    	optionRoot = new GenerateAPPLDIFOptionDescriptorEnum();
    }

    public static void main (String [] args) {
    	try {
    		GenerateAPPLDIF generateAPPLDIF = new GenerateAPPLDIF();
    		generateAPPLDIF.parseAndExecute(args);
		} catch (Exception e) {
			printException(e);
			System.exit(INPUT_ERROR_STATUS);
		}
    }
    
    private int createLDIF (String exeName, String appName){
        IComponentManager manager = ComponentManagerFactory.getComponentManager();
        IOSWrapper osWrapper = (IOSWrapper) manager.getComponent(OSWrapper.class);
        
        if (exeName.length () == 0) {
        	errorMessage = "Invalid executable name\n";
            return ERROR_STATUS;
        }
        if (appName.length () == 0) {
        	errorMessage = "Invalid application name\n";
            return ERROR_STATUS;
        }
        
        String fingerPrint = osWrapper.getAppInfo (exeName);
        if (fingerPrint.length () == 0) {
        	errorMessage = "Error getting application info\n";
            return ERROR_STATUS;
        }
        String uniqueGlobalIdentifier = null;

        try {
            uniqueGlobalIdentifier = NamespaceIDGenerator.generateInternalImportID(ImportCategoryEnumType.APPLICATIONS, appName);
        } catch (RelativeIDException e) {
        	errorMessage = "Unique id generation failed with RelativeIDException for " + appName + "\n" + e.getMessage() + "\n";
            return ERROR_STATUS;
        }
        
        String uniqueSystemIdentifier = (exeName.lastIndexOf (File.separatorChar) != -1) 
        	? exeName.substring (exeName.lastIndexOf (File.separatorChar) + 1)
        	: exeName;

        System.out.println("dn: cn=" + appName + ", o=Applications,dc=DestinyData,dc=Destiny,dc=com");
        System.out.println("uniqueGlobalIdentifier: " + uniqueGlobalIdentifier);
        System.out.println("fullyQualifiedName: " + appName);
        System.out.println("objectClass: top");
        System.out.println("objectClass: DestinyObject");
        System.out.println("objectClass: Application");
        System.out.println("uniqueSystemIdentifier: " + uniqueSystemIdentifier.toLowerCase());
        System.out.println("cn: " + appName);
        System.out.println("applicationFingerPrint: " + fingerPrint);
        System.out.println("");

        return SUCCESS_STATUS;
    }

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ConsoleApplicationBase#execute(com.nextlabs.shared.tools.ICommandLine)
	 */
	@Override
	protected void execute(ICommandLine commandLine) {
		int status;
		
		String exeName = getValue(commandLine, GenerateAPPLDIFOptionDescriptorEnum.EXENAME_OPTION_ID);
		String appName = getValue(commandLine, GenerateAPPLDIFOptionDescriptorEnum.APPNAME_OPTION_ID);
		
		status=createLDIF(exeName, appName);
		if(status == ERROR_STATUS){
			System.err.println(errorMessage);
		}
		
		System.exit(status);
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ConsoleApplicationBase#getDescriptor()
	 */
	@Override
	protected IConsoleApplicationDescriptor getDescriptor() {
		return optionRoot;
	}
}