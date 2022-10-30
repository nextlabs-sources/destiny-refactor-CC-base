/*
 * Created on Sep 19, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/components/deployment/DDACDeploymentException.java#1 $:
 */

package com.nextlabs.destiny.container.ddac.components.deployment;

public class DDACDeploymentException extends Exception {
    public DDACDeploymentException(Throwable cause) {
        super (cause);
    }

    public DDACDeploymentException(String message) {
        super(message);
    }

    public DDACDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
