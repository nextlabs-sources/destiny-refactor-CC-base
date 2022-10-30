/*
 * Created on Oct 22, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/components/deployment/IDACDeployer.java#1 $:
 */

package com.nextlabs.destiny.container.ddac.components.deployment;

import java.util.Collection;

import com.nextlabs.destiny.container.ddac.components.deployment.DDACDeploymentException;
import com.nextlabs.destiny.container.ddac.configuration.DDACActiveDirectoryConfiguration;
import com.nextlabs.pf.destiny.formatter.DACCentralAccessPolicy;
import com.nextlabs.pf.destiny.formatter.DACCentralAccessRule;

public interface IDACDeployer {
    public String undeploy(DDACActiveDirectoryConfiguration adConfig, Collection<DACCentralAccessRule> rules, Collection<DACCentralAccessPolicy> policies) throws DDACDeploymentException;
    public String deploy(DDACActiveDirectoryConfiguration adConfig, Collection<DACCentralAccessRule> rules, Collection<DACCentralAccessPolicy> policies) throws DDACDeploymentException;
}
