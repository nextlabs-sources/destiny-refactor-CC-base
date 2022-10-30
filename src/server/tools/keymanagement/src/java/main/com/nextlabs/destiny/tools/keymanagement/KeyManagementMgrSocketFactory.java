package com.nextlabs.destiny.tools.keymanagement;

import java.util.Hashtable;

import com.bluejungle.destiny.tools.common.SecureClientSocketFactory;

public class KeyManagementMgrSocketFactory extends SecureClientSocketFactory {

    private static final String KEYSTORE_NAME = "/security/keymanagement-keystore.jks";

    /**
     * Constructor
     * 
     * @param params
     */
    public KeyManagementMgrSocketFactory(Hashtable params) {
        super(params);
    }

    /**
     * @see com.bluejungle.destiny.tools.common.SecureClientSocketFactory#getKeystoreName()
     */
    public String getKeystoreName() {
        String toolHome = System.getProperty(KeyManagementMgr.KM_TOOL_HOME, "");
        return toolHome + KEYSTORE_NAME;
    }
    
	/* (non-Javadoc)
	 * @see com.bluejungle.destiny.tools.common.SecureClientSocketFactory#getPassword()
	 */
	@Override
	public String getPassword() {
		return System.getProperty(KeyManagementMgr.KM_TOOL_KEYSTORE_PASSWORD, 
				super.getPassword()); // Get default password from superclass by default
	}    
}

