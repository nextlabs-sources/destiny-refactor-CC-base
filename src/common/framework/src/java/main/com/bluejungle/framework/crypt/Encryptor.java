/*
 * Created on Sep 27, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.crypt;

import com.nextlabs.shared.tools.ConsoleApplicationBase;
import com.nextlabs.shared.tools.ICommandLine;
import com.nextlabs.shared.tools.IConsoleApplicationDescriptor;
import com.nextlabs.shared.tools.InvalidOptionDescriptorException;
import com.nextlabs.shared.tools.SecureConsolePrompt;

/**
 * The encryptor class is used to encrypt a given password. This utility can be
 * used by customers when changing an encrypted password in a configuration
 * file. The encrypted password will be displayed in the standard output.
 * 
 * @author ihanen
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/framework/src/java/main/com/bluejungle/framework/crypt/Encryptor.java#1 $
 */

public class Encryptor extends ConsoleApplicationBase{

    /**
     * Status return codes
     */
    private static final int STATUS_ERROR = 1;
    private static final int STATUS_SUCCESS = 0;

    private IConsoleApplicationDescriptor optionRoot;
    
    
    private static final char ESCAPED_CHAR = '^';
    
    //the order does matter, the ESCAPED_CHAR has to be at the end 
    private static final char[] REQUIRE_ESCAPED_CHARS = new char[]{'&','<','>','(',')','@','|',ESCAPED_CHAR};
    
    
    protected Encryptor() throws InvalidOptionDescriptorException{
//    	PRINT_HELP_IF_NO_ARGS = false;
    	optionRoot = new CryptOptionDescriptorEnum();
    }
    
    /**
     * Main function
     */
    public static void main(String[] args) {
    	try {
    		Encryptor encryptor = new Encryptor();
			encryptor.parseAndExecute(args);
			System.exit(STATUS_SUCCESS);
		} catch (Exception e) {
			printException(e);
			System.exit(STATUS_ERROR);
		}
        
    }

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ConsoleApplicationBase#execute(com.nextlabs.shared.tools.ICommandLine)
	 */
	@Override
	protected void execute(ICommandLine commandLine) {
		try {
			String passwordToEncrypt = getValue(commandLine, CryptOptionDescriptorEnum.PASSWORD_OPTION_ID);
			
			if(passwordToEncrypt==null){
				SecureConsolePrompt securePrompt = new SecureConsolePrompt("Password: ");
                char[] password = securePrompt.readConsoleSecure();
                passwordToEncrypt = new String(password);
			}else{
				if (getValue(commandLine, CryptOptionDescriptorEnum.IS_ESCAPED_STRING_OPTION_ID)) {
					for(char c : REQUIRE_ESCAPED_CHARS){
						passwordToEncrypt = passwordToEncrypt.replace(Character.toString(ESCAPED_CHAR)
								+ Character.toString(c), Character.toString(c));
					}
				}
			}
			ReversibleEncryptor encryptor = new ReversibleEncryptor();
	        System.out.println(encryptor.encrypt(passwordToEncrypt));
		} catch (Exception e) {
			printException(e);
			System.exit(STATUS_ERROR);
		}
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ConsoleApplicationBase#getDescriptor()
	 */
	@Override
	protected IConsoleApplicationDescriptor getDescriptor() {
		return optionRoot;
	}
}