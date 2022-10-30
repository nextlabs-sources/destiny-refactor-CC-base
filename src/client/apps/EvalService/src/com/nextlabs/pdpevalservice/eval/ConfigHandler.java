package com.nextlabs.pdpevalservice.eval;

import java.util.HashMap;

import javax.servlet.ServletContext;

public class ConfigHandler {
	
	public static final String RMIPORT = "RMIPORT";
	
	public static final String RMIREMOTEHOSTNAME = "RMIREMOTEHOSTNAME";
	
	private HashMap<String, String> paramMap = new HashMap<String, String>();  
	
	private static ConfigHandler handler = new ConfigHandler();
	
	private ConfigHandler(){		
	}
	
	public static ConfigHandler getInstance(){
		return handler;
	}

	public void processParams(ServletContext servletContext) {
		if(servletContext==null){
			return;
		}
		setRMIPortNumber(servletContext);
		setRemoteHostName(servletContext);
		
	}
	
	public String getParamValue(String paramName){
		return paramMap.get(paramName);
	}

	private void setRemoteHostName(ServletContext servletContext) {
		String remoteHostName = servletContext.getInitParameter(RMIREMOTEHOSTNAME);
		if(remoteHostName!=null && remoteHostName.length()>0){
			paramMap.put(RMIREMOTEHOSTNAME, remoteHostName);
		}
	}

	private void setRMIPortNumber(ServletContext servletContext) {
		String rmiPortNum = servletContext.getInitParameter(RMIPORT);
		if(rmiPortNum!=null && rmiPortNum.length()>0){
			paramMap.put(RMIPORT, rmiPortNum);
		}		
	}

}
