package com.bluejungle.destiny.container.shared.alertmgr;

public interface IAlertDefinition {
	public abstract Long getId();
	public abstract String getNamespace();
	public abstract String getTitle();
	public abstract void setTitle(String titleToSet);
	public abstract String getMessageTemplate();
	public abstract void setMessageTemplate(String messageTemplate);
	public abstract IAlertHandler getAlertHandler();
	public abstract void save();
	public abstract void delete();
}
 
