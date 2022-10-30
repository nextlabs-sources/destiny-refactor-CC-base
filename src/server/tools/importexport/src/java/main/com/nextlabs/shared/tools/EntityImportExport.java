package com.nextlabs.shared.tools;

import java.io.IOException;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorClient;

/**
 * share common methods between import and export tools
 * @author hchan
 * @date Apr 9, 2007
 */
abstract class EntityImportExport extends ConsoleApplicationBase {
	//initalize this field in the constructor
	protected IConsoleApplicationDescriptor consoleApplicationDescriptor;
	
	//unknown error status
	protected static final int STATUS_ERR_UNKNOWN_EXCEPTION_THROWN = -99;

	//a prefix of host connection string
	private static final String PROTOCOL = "https://";
	
	EntityImportExport() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.nextlabs.shared.tools.ConsoleApplicationBase#execute(com.nextlabs.shared.tools.ICommandLine)
	 */
	@Override
	protected void execute(ICommandLine commandLine) {
		int returnCode;
		try {
			returnCode = doAction(commandLine);
		} catch (Exception e) {
			printException(e);
			returnCode = STATUS_ERR_UNKNOWN_EXCEPTION_THROWN;
		}
		System.out.println(returnCode + ":\t" + getReturnMessage(returnCode));
	}
	
	protected abstract int doAction(ICommandLine commandLIne) throws Exception;
	
	/**
	 * create PolicyEditClient by given arguements from ICommandLine
	 * @param commandLine
	 * @return	
	 * @throws IOException 
	 */
	protected IPolicyEditorClient createPolicyEditorClient(ICommandLine commandLine)
			throws IOException {
		String hostUrl = getValue(commandLine, EntityExportOptionDescriptorEnum.HOST_OPTION_ID);
		hostUrl = hostUrl.toLowerCase();

		if (!hostUrl.startsWith(PROTOCOL)) {
			hostUrl = PROTOCOL + hostUrl;
		}

		Integer port = getValue(commandLine,
				EntityExportOptionDescriptorEnum.PORT_OPTION_ID);
		hostUrl += ":" + port;

		String username = getValue(commandLine,
				EntityExportOptionDescriptorEnum.USER_ID_OPTION_ID);
		String password = getValue(commandLine,
				EntityExportOptionDescriptorEnum.PASSWORD_OPTION_ID);

		if (password == null) {
			SecureConsolePrompt securePrompt = new SecureConsolePrompt("Password: ");
			password = new String(securePrompt.readConsoleSecure());
		}

		IPolicyEditorClient client = createPolicyEditorClient(hostUrl, username, password);

		return client;
	}
	
	/**
	 * create Policy Editor Client
	 * @param hostUrl	host to connect
	 * @param username
	 * @param password
	 * @return
	 */
	protected IPolicyEditorClient createPolicyEditorClient(String hostUrl, String username,
			String password) {
		IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
		HashMapConfiguration pfClientConfig = new HashMapConfiguration();
		pfClientConfig.setProperty(PolicyEditorClient.LOCATION_CONFIG_PARAM, hostUrl);
		pfClientConfig.setProperty(PolicyEditorClient.USERNAME_CONFIG_PARAM, username);
		pfClientConfig.setProperty(PolicyEditorClient.PASSWORD_CONFIG_PARAM, password);

		PolicyEditorClient.COMP_INFO.overrideConfiguration(pfClientConfig);
		IPolicyEditorClient client = compMgr.getComponent(PolicyEditorClient.COMP_INFO);
		return client;
	}

	@Override
	protected IConsoleApplicationDescriptor getDescriptor() {
		return consoleApplicationDescriptor;
	}

	/**
	 * retrieve a meanful string by different status
	 * @param returnStatus	must be one of those static final STAUS_*
	 * @return	a string that related to the returnStatus
	 */
	protected abstract String getReturnMessage(int returnStatus);
}
