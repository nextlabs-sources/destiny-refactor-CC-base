package com.nextlabs.destiny.container.dkms;

import static org.junit.Assert.assertNotNull;

import java.util.Set;

import com.bluejungle.destiny.server.shared.configuration.IGenericComponentConfigurationDO;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyRepository;
import com.bluejungle.framework.datastore.hibernate.IHibernateRepository;
import com.bluejungle.framework.sharedcontext.DestinySharedContextLocatorImpl;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.nextlabs.destiny.container.dcc.BaseDCCComponentTestCase;

public abstract class DKMSComponentTestBase extends BaseDCCComponentTestCase{
    
    @Override
    protected String getComponentName() {
        return DKMSConstants.COMPONENT_TYPE_NAME;
    }

    @Override
    protected Set<DestinyRepository> getDataRepositories() {
        // I don't have any pre-defined destiny repository.
        return null;
    }

    protected IHibernateRepository initDataSource() {
        IGenericComponentConfigurationDO cConfig = (IGenericComponentConfigurationDO)readConfigurationFile().getDCCConfiguration(DKMSConstants.COMPONENT_TYPE);
        assertNotNull(cConfig);
        
        IHibernateRepository repository = KeyManagementRepository.create(compMgr, cConfig.getProperties());
        assertNotNull(repository);
        
        return repository;
    }
    
}

