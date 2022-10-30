package com.bluejungle.pf.utils;

import java.util.Collection;
import java.util.Date;

import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.tools.DeploymentToolsBase;

/**
 * Exports PQL from the database in a format amicable to re-import.
 * 
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/test/com/bluejungle/pf/utils/PQLExporter.java#1 $
 * 
 */

public class PQLExporter extends DeploymentToolsBase {

    public static final String USAGE = "PQLExporter -host hostname -password password [-port portnum]";
    public static final String HOST_ARGUMENT_NAME = "host";

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            PQLExporter exporter = new PQLExporter();
            exporter.setupDatasource(args);

            System.exit(exporter.exportPQL());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    private int exportPQL() throws Exception {
        Date now = new Date();
        Collection<DeploymentEntity> entities = lm.getAllDeployedEntities(now, DeploymentType.PRODUCTION);
        lm.resolveIds(entities, now, DeploymentType.PRODUCTION);
        for (DeploymentEntity entity : entities) {
            System.out.println(entity.getPql());
        }
        return 0;
    }

}
