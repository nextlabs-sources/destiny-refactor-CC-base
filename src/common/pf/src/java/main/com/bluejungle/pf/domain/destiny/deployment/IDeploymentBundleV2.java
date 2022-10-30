package com.bluejungle.pf.domain.destiny.deployment;

/*
 * Created on May 7, 2009
 *
 * All sources, binaries and HTML pages (C) copyright 2009 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/domain/destiny/deployment/IDeploymentBundleV2.java#1 $
 */

import java.io.Serializable;
import java.util.BitSet;
import java.util.Map;

/**
 * IDeploymentBundleV2 represents a self-contained collection of policies along with all the supporting objects that are required to enforce those policies.
 * A bundle can be empty, or it can contain information.  If a bundle is empty, it means there are no updates available
 * for deployment.  An empty bundle contains nothing but a timestamp.
 */

public interface IDeploymentBundleV2 extends Serializable, IDeploymentBundle {
    /**
     * @return action name -> policy bitset
     * @throws IllegalStateException if the bundle is empty
     */
    Map<String,BitSet> getActionNameToPolicy();
}
