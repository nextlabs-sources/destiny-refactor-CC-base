/*
 * Created on May 3, 2005
 *
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.pf.tools;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;
import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.tools.LocationImporterCLI;

/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/tools/TestLocationImporter.java#1 $:
 */

public class TestLocationImporter extends PFTestWithDataSource {
	private static final String DEFAULT_SRC_ROOT_DIR = "c:\\work\\Destiny\\main";
	
    public final void testImportLocations() throws Exception {
        Map<String,String> defs = new HashMap<String,String>();
        defs.put( "intranet", "10.0.0.0/8" );
        defs.put( "vpn", "192.168.254.0/24" );
        defs.put( "dod", "6.0.0.0/8" );

        String srcRoot = System.getProperty("src.root.dir");
        if(srcRoot == null){
        	srcRoot = DEFAULT_SRC_ROOT_DIR;
        }
        String locationsFile = srcRoot + File.separator + "test_files" + File.separator + "com" + File.separator + "bluejungle" + File.separator + "pf" + File.separator + "tools" + File.separator + "locations.txt";

//        String[] args = {"-password", "123blue!", "-locations", locationsFile};
        String[] args = {
        		"-w", "123blue!", 
        		"-l", locationsFile,
        		"-u", "root",
        		"-s", "localhost",
        		"-p", "0"};
        
        new LocationImporterCLI().parseAndExecute(args);

        IComponentManager cm = ComponentManagerFactory.getComponentManager();
        ComponentInfo<IDestinySharedContextLocator> locatorInfo = 
            new ComponentInfo<IDestinySharedContextLocator>(
                IDestinySharedContextLocator.COMP_NAME, 
                MockSharedContextLocator.class, 
                IDestinySharedContextLocator.class, 
                LifestyleType.SINGLETON_TYPE);
        cm.registerComponent(locatorInfo, true);

        LifecycleManager lm = cm.getComponent(LifecycleManager.COMP_INFO);

        Collection<DeploymentEntity> locations = lm.getAllDeployedEntities(EntityType.LOCATION, new Date(Calendar.getInstance().getTimeInMillis() + 600000), DeploymentType.PRODUCTION);
        assertNotNull("locations should not be null", locations);
        assertEquals("there should be 3 locations", 3, locations.size());
        
        DeploymentEntity[] des = locations.toArray(new DeploymentEntity[] {});
        for (int i = 0; i < des.length; i++) {
            String pql = des[i].getPql();
            assertNotNull("pql should not be null", pql);
            DomainObjectBuilder dob = new DomainObjectBuilder(pql);
            Location location = dob.processLocation();
            String name = location.getName();
            String value = location.getValue();
            assertTrue("location " + name + " should exist", defs.containsKey( name ) );
            assertEquals("value must correspond to name", defs.get( name ), value);
        }
    }
}
