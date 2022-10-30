/*
 * Created on Feb 17, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.dem;

import com.bluejungle.destiny.container.dcc.BaseDCCComponentImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.IEnrollmentManager;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.controller.defaultimpl.EnrollmentManagerImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.EnrollerFactoryImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IEnrollerFactory;
import com.bluejungle.destiny.server.shared.configuration.IDEMComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.nextlabs.destiny.container.shared.utils.DCCComponentHelper;
import com.nextlabs.framework.messaging.IMessageHandler;
import com.nextlabs.framework.messaging.IMessageHandlerManager;
import com.nextlabs.framework.messaging.handlers.EmailMessageHandler;
import com.nextlabs.framework.messaging.impl.MessageHandlerManagerImpl;

/**
 * This is the main DEM component implementation class.
 * 
 * @author ihanen
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/container/dem/src/java/main/com/bluejungle/destiny/container/dem/DEMComponentImpl.java#1 $
 */

public class DEMComponentImpl extends BaseDCCComponentImpl {

    /**
     * @see com.bluejungle.destiny.server.shared.registration.IRegisteredDCCComponent#getComponentType()
     */
    public ServerComponentType getComponentType() {
        return ServerComponentType.DEM;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        super.init();
        
        // Initialize the security components
        DCCComponentHelper.initSecurityComponents(getManager(), getLog());
        
        final IDestinyConfigurationStore configMgr = getManager().getComponent(
						DestinyConfigurationStoreImpl.COMP_INFO);
        
		final IDEMComponentConfigurationDO demCfg = (IDEMComponentConfigurationDO) configMgr
						.retrieveComponentConfiguration(getComponentType().getName());
		
        IComponentManager compMgr = getManager();
        compMgr.registerComponent(ServerSpecManager.COMP_INFO, true);
        
        // Initialize the dictionary component:
        HashMapConfiguration enrollmentMgrConfig = new HashMapConfiguration();
        
        IDictionary dictionary = getManager().getComponent(Dictionary.COMP_INFO);
        enrollmentMgrConfig.setProperty(IEnrollmentManager.DICTIONARY, dictionary);
        
        IEnrollerFactory enrollerFactory = new EnrollerFactoryImpl();
        enrollmentMgrConfig.setProperty(IEnrollmentManager.ENROLLER_FACTORY, enrollerFactory);
        
        IMessageHandlerManager messageHandlerManager =
            compMgr.getComponent(MessageHandlerManagerImpl.class);
        IMessageHandler messageHandler = messageHandlerManager.getMessageHandler(
                EmailMessageHandler.DEFAULT_HANDLER_NAME);
        enrollmentMgrConfig.setProperty(IEnrollmentManager.MESSAGE_HANDLER, messageHandler);
        compMgr.getComponent(EnrollmentManagerImpl.class, enrollmentMgrConfig);


        HashMapConfiguration config = new HashMapConfiguration();
        config.setProperty(ReporterCacheRefreshThread.REPORTER_CACHE_THREAD_REFRESH_PROP_NAME, 
        		 demCfg.getReporterCacheRefreshRate());
        compMgr.getComponent(ReporterCacheRefreshThread.COMP_INFO, config);
    }

}
