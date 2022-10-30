package com.bluejungle.destiny.container.shared.alertmgr;

import java.util.Set;

public interface IAlertSubscription {
	public abstract Long getId();
	public abstract String getOwnerId();
	public abstract IAlertDefinition getAlertDefition();
	public abstract Set getMessegeHandlerConfigs();
	public abstract void setMessageHandlerConfigs(Set messageHandlerConfigs);
	public abstract void save();
	public abstract void delete();
}
 
