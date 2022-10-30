package com.bluejungle.destiny.tools.reporterdata;

import java.util.ArrayList;

import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentEntity;
import com.bluejungle.pf.destiny.lifecycle.DevelopmentStatus;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.lifecycle.PFTestWithDataSource;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.bluejungle.pf.utils.PQLTestUtils;

/*
 * Created on Jan 16, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
/**
 * @author ryoung
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/reporterData/src/java/main/com/bluejungle/destiny/tools/reporterdata/Deployment.java#1 $
 */

public class Deployment extends PFTestWithDataSource {

    /**
     * constructor.  Initializes the Directory->Destiny ID converter
     */
    public Deployment() throws Exception {
        super.setUp();
        cm.registerComponent(ServerSpecManager.COMP_INFO, true);
    }

    /**
     * Uses LifecycleManager to deploy a Development Entity
     * @param pql
     * @throws Exception
     */
    public void processPQL(String pql[]) throws Exception 
    {
        LifecycleManager lm = (LifecycleManager) cm.getComponent(LifecycleManager.COMP_INFO);
        final ArrayList des = new ArrayList();
        for (int i=0; i<pql.length; i++)
        {
            DomainObjectBuilder.OneObjectVisitor oov = new DomainObjectBuilder.OneObjectVisitor();
            DomainObjectBuilder.processInternalPQL( pql[i], oov );
            String name;
            EntityType type;
            if ( oov.getPolicy() != null ) {
                name = oov.getPolicy().getName();
                type = EntityType.POLICY;
            } else if ( oov.getSpec() != null ) {
                name = oov.getSpec().getName();
                type = EntityType.COMPONENT;
            } else {
                throw new IllegalArgumentException(pql[i]);
            }
            DevelopmentEntity de = lm.getEntityForName( type, name, LifecycleManager.MAKE_EMPTY);
            de.setPql(pql[i]);
            des.add(de);
            PQLTestUtils.setStatus(de, DevelopmentStatus.APPROVED);
        }
        lm.saveEntities(des, LifecycleManager.MUST_EXIST);
        long time = System.currentTimeMillis();
        lm.deployEntities(des, UnmodifiableDate.forTime(time+60*1000+500), DeploymentType.PRODUCTION, false);
    }
}
