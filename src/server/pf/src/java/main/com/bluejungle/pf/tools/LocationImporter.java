/*
 * Created on May 2, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved worldwide.
 */
package com.bluejungle.pf.tools;

import java.io.BufferedReader;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.EntityManagementException;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.subject.Location;

/**
 * Very simple tool to import locations.  A lot of hardcoded values.
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/tools/LocationImporter.java#1 $:
 */

public class LocationImporter extends DeploymentToolsBase {

    void importLocations(BufferedReader in, boolean verbose) throws PQLException, Exception {
        // Read in the file, put the data into the defs Map:
        Map<String,String> defs = new HashMap<String,String>();
        
        String line;
        int i = 0;
        while ((line = in.readLine()) != null) {
            i++;
            line = line.trim();
            if (line.startsWith("#")) {
                continue;
            }
            if (line.length() == 0) {
                if (verbose) {
                    System.out.println("Blank line " + i + " Skipping");
                }
                continue;
            }
            String[] values = line.split("\\s");
            if (values.length != 2
             || values[0] == null
             || values[0].length() == 0
             || values[1] == null
             || values[1].length() == 0 ) {
                System.err.println("Invalid line " + i + " : " + line + "\nSkipping");
                continue;
            }
            if ( defs.containsKey( values[0] ) ) {
                System.err.println("Location '"+values[0]+"' is redefined.\nSkipping");
                continue;
            }
            String pql =
                "id null status approved creator \"0\" "
            +   "ACCESS_POLICY "
            +   "ACCESS_CONTROL "
            +   "PBAC FOR * ON ADMIN BY PRINCIPAL.USER.NAME = RESOURCE.DSO.OWNER DO ALLOW "
            +   "ALLOWED_ENTITIES "
            +   "HIDDEN location " + values[0] + " = " + values[1];
            DomainObjectBuilder dob = new DomainObjectBuilder(pql);
            Location location;
            try {
                location = dob.processLocation();
            } catch (Exception e) {
                System.err.println("Invalid line " + i + " : " + line + "\n" + e + "\n Skipping");
                continue;
            }
            if (location == null) {
                System.err.println("Invalid line " + i + " : " + line + "\n Skipping");
                continue;
            }
            if (verbose) {
                System.out.println("Importing location " + values[0] + " = " + values[1]);
            }
            defs.put( values[0], pql );
        }
        in.close();
        deployLocations( defs );
        
        if (verbose) {
            System.out.println("Successfully imported " + defs.size() + " locations.");
        }
    }

    /**
     * @param locations a <code>Map</code> of location names to PQL defs.
     * @throws EntityManagementException when the operation cannot complete.
     */
    private void deployLocations(Map<String,String> locations) throws EntityManagementException {
        LifecycleManager lm = (LifecycleManager) cm.getComponent(LifecycleManager.COMP_INFO);
        Collection<DevelopmentEntity> devs = lm.getEntitiesForNames( EntityType.LOCATION, locations.keySet(), LifecycleManager.MAKE_EMPTY );
        for ( DevelopmentEntity dev : devs ) {
            try {
                dev.setPql( locations.get( dev.getName() ) );
            } catch ( PQLException pqlEx ) {
                // This should not hapen
                assert false : "The PQL has been parsed before.";
            }
        }
        lm.deployEntities( devs, UnmodifiableDate.forTime(Calendar.getInstance().getTimeInMillis() + 60001), DeploymentType.PRODUCTION, true, null);
    }

}
