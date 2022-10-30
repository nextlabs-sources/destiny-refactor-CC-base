package com.bluejungle.pf.destiny.services;

import com.bluejungle.destiny.appframework.appsecurity.loginmgr.LoginException;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;

/**
 * @author hchan
 * @date Mar 22, 2007
 */
public class PushDeploymentClient{

	public static void main(String[] args) {
		if(args.length != 3){
			//display usage
			System.out.println("invalid arguments, usage = <location> <username> <password>");
		}else{
			String location = args[0];
			String username = args[1];
			String password = args[2];
			try {				
				IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
				HashMapConfiguration pfClientConfig = new HashMapConfiguration();
				pfClientConfig.setProperty(PolicyEditorClient.LOCATION_CONFIG_PARAM, location);
				pfClientConfig.setProperty(PolicyEditorClient.USERNAME_CONFIG_PARAM, username);
				pfClientConfig.setProperty(PolicyEditorClient.PASSWORD_CONFIG_PARAM, password);
				
				PolicyEditorClient.COMP_INFO.overrideConfiguration(pfClientConfig);
				ComponentInfo<PolicyEditorClient> compInfo = PolicyEditorClient.COMP_INFO;
				IPolicyEditorClient client = compMgr.getComponent(compInfo);
				client.login();
				if(client.isLoggedIn()){
					client.executePush();
					System.out.println("push request is successful.");
				}else{
					System.out.println("Login failed.");
				}
			} catch (LoginException e) {
				System.out.println("unable to login.");
				e.printStackTrace();
			} catch (PolicyEditorException e) {
				System.out.println("unable to push.");
				e.printStackTrace();
			}
		}
	}
}
