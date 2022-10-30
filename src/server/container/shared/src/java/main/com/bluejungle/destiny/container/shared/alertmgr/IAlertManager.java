package com.bluejungle.destiny.container.shared.alertmgr;

import java.util.Set;
import java.util.List;

public interface IAlertManager {
	public abstract IAlertDefinition defineAlert(String namespace, String title, String messageTemplate, String alertHandlerClassName);
	public abstract IAlertDefinition retrieveAlertDefinition(Long id);
	public abstract void deleteAlertDefinition(Long id);
	public abstract void fireAlert(Long alertDefinitionId, Object[] alertData);
	public abstract void subscribe(String ownerId, Long alertDefinitionId, Set messengerConfigs);
	public abstract IAlertSubscription retrieveSubscription(String ownerId, Long alertDefinintoinID);
	public abstract List retrieveSubscriptions(String ownerId, String namespace);
}
 
