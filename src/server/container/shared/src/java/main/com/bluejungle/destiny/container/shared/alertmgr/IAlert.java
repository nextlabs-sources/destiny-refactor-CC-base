package com.bluejungle.destiny.container.shared.alertmgr;

public interface IAlert {
 
	public abstract IAlertDefinition getAlertDefinition();
	public abstract Object[] getAlertData();
}
 
