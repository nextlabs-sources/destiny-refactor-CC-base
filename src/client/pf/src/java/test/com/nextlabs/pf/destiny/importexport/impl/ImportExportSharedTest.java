/*
 * Created on Sep 20, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.pf.destiny.importexport.impl;

import java.io.File;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.pf.destiny.services.IPolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorClient;
import com.bluejungle.pf.destiny.services.PolicyEditorClientMock;

import junit.framework.TestCase;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/test/com/nextlabs/pf/destiny/importexport/impl/ImportExportSharedTest.java#1 $
 */

public class ImportExportSharedTest  extends TestCase {
	//those files should be read only
	protected static final String POLICY_XML_1 		= "testPolicy.xml";
	protected static final String POLICY_XML_2 		= "testPolicy2.xml";
	protected static final String POLICY_XML_3 		= "hk-test.xml";
	protected static final String POLICY_XML_3_MOD 	= "hk-test-mod.xml";
	protected static final String POLICY_XML_4 		= "hk-test-2.xml";

	private static final String TEST_FOLDER = "c:\\";
	
	protected final File srcFilesFolder;
	
	protected final File testFolder;
	
	protected final IPolicyEditorClient client;

	protected ImportExportSharedTest() throws LoginException {
		client = createLoggedInClient();
		srcFilesFolder = getTestFileFolderFile();
		testFolder = new File(TEST_FOLDER);
	}


	@Override
	protected void setUp() throws Exception {
		super.setUp();
        PolicyEditorClientMock.prepareForTest((PolicyEditorClient)client);
	}

	protected File getTestFileFolderFile() {
		String basePath = System.getProperty("src.root.dir");
		if (basePath == null) {
			basePath = "c:\\work\\Destiny\\main";
		}
		basePath += File.separator + "test_files" + File.separator;
		String subPath = getClass().getPackage().getName();
		subPath = subPath.replaceAll("[.]", "\\" + File.separator);
		basePath += subPath;
		// Clean the directory for the test output and the differences
		return new File(basePath);
	}
	
	
	
	protected IPolicyEditorClient createLoggedInClient() throws LoginException {
		//create default client
        IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
        HashMapConfiguration pfClientConfig = new HashMapConfiguration();
		pfClientConfig.setProperty(PolicyEditorClient.LOCATION_CONFIG_PARAM, "https://localhost:8443");
		pfClientConfig.setProperty(PolicyEditorClient.USERNAME_CONFIG_PARAM, "Administrator");
		pfClientConfig.setProperty(PolicyEditorClient.PASSWORD_CONFIG_PARAM, "123blue!");
		
		PolicyEditorClient.COMP_INFO.overrideConfiguration(pfClientConfig);
		IPolicyEditorClient client = compMgr.getComponent(PolicyEditorClient.COMP_INFO);
		client.login();
		return client;
	}
}
