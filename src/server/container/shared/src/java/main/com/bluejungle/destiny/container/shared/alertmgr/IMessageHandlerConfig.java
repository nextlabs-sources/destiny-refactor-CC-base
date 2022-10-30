package com.bluejungle.destiny.container.shared.alertmgr;

import com.nextlabs.framework.messaging.IMessageHandlerInstructions;

public interface IMessageHandlerConfig {
	public abstract String getMessageHandlerName();
	public abstract IMessageHandlerInstructions getMessageHandlerInstructions();
}
 
