package com.nextlabs.destiny.container.dcc;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.bluejungle.framework.configuration.DestinyRepository;

@RunWith(value = Parameterized.class)
public class BaseDCCComponentTestTest extends BaseDCCComponentTestCase{

    private static final Set<DestinyRepository> ALL_DESTINY_REPOSITORY;
    static {
        ALL_DESTINY_REPOSITORY = new HashSet<DestinyRepository>();
        ALL_DESTINY_REPOSITORY.add(DestinyRepository.ACTIVITY_REPOSITORY);
        ALL_DESTINY_REPOSITORY.add(DestinyRepository.DICTIONARY_REPOSITORY);
        ALL_DESTINY_REPOSITORY.add(DestinyRepository.MANAGEMENT_REPOSITORY);
        ALL_DESTINY_REPOSITORY.add(DestinyRepository.POLICY_FRAMEWORK_REPOSITORY);
    }
    
    private final String componentName;
    
    public BaseDCCComponentTestTest(String componentName) {
        this.componentName = componentName;
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] { 
                { "dabs" }
              , { "dac" }
              , { "dem" }
              , { "dms" }
              , { "dps" } 
        });
    }

    @Override
    protected String getComponentName() {
        return componentName;
    }

    @Override
    protected Set<DestinyRepository> getDataRepositories() {
        return ALL_DESTINY_REPOSITORY;
    }
    
    @Before
    public void clearComponentManager() {
        compMgr.shutdown();
        assertTrue(compMgr.isShutdown());
        BaseDCCComponentTestCase.staticInit();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void test() {
        for(DestinyRepository dp : ALL_DESTINY_REPOSITORY) {
            //make sure the component manager is clean
            assertFalse(compMgr.isComponentRegistered(dp.getName()));
        }
        initAll();
    }
}
