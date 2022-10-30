package com.nextlabs.pdpevalservice.filters.transform;

import com.bluejungle.destiny.agent.pdpapi.IPDPApplication;

import com.bluejungle.destiny.agent.pdpapi.IPDPHost;
import com.bluejungle.destiny.agent.pdpapi.IPDPNamedAttributes;
import com.bluejungle.destiny.agent.pdpapi.IPDPResource;
import com.bluejungle.destiny.agent.pdpapi.IPDPSDKCallback;
import com.bluejungle.destiny.agent.pdpapi.IPDPUser;

public class EvalRequest {
	
	private IPDPResource[] resourceArr = null;
	
	private String action = null;
	
	private IPDPUser user = null;

	private IPDPApplication application = null;
	
	private IPDPHost host = null;
	
	private boolean performObligations = true;
	
	private IPDPNamedAttributes[] additionalData = null;
	
	//TODO: Value is hardcoded here. Check if there is a better way to handle this.
	private int noiseLevel = 3;//PDPSDK.NOISE_LEVEL_USER_ACTION;
	
	//TODO: Value is hardcoded here. Check if there is a better way to handle this.
	private int timeoutInMins = -1;//PDPSDK.WAIT_FOREVER;
	
	private IPDPSDKCallback callBack = IPDPSDKCallback.NONE;
	
	public IPDPResource[] getResourceArr() {
		return resourceArr;
	}

	public void setResourceArr(IPDPResource[] resourceArr) {
		this.resourceArr = resourceArr;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public IPDPUser getUser() {
		return user;
	}

	public void setUser(IPDPUser user) {
		this.user = user;
	}

	public IPDPApplication getApplication() {
		return application;
	}

	public void setApplication(IPDPApplication application) {
		this.application = application;
	}

	public IPDPHost getHost() {
		return host;
	}

	public void setHost(IPDPHost host) {
		this.host = host;
	}

	public boolean isPerformObligations() {
		return performObligations;
	}

	public void setPerformObligations(boolean performObligations) {
		this.performObligations = performObligations;
	}

	public IPDPNamedAttributes[] getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(IPDPNamedAttributes[] additionalData) {
		this.additionalData = additionalData;
	}

	public int getNoiseLevel() {
		return noiseLevel;
	}

	public void setNoiseLevel(int noiseLevel) {
		this.noiseLevel = noiseLevel;
	}

	public int getTimeoutInMins() {
		return timeoutInMins;
	}

	public void setTimeoutInMins(int timeoutInMins) {
		this.timeoutInMins = timeoutInMins;
	}

	public IPDPSDKCallback getCallBack() {
		return callBack;
	}

	public void setCallBack(IPDPSDKCallback callBack) {
		this.callBack = callBack;
	}
	
}
