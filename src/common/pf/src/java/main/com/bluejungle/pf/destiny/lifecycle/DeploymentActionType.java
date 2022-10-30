package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2005 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 *
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/DeploymentActionType.java#1 $
 */

import com.bluejungle.framework.patterns.EnumBase;

/**
 * Describes the type of deployment action that was taken -
 * a deployment, a cancellation, or an undeployment.
 */
public class DeploymentActionType extends EnumBase {

    /**
     * Represents the deployment action.
     */
    public static final DeploymentActionType DEPLOY = new DeploymentActionType( "DEPLOY" ) {};

    /**
     * Represents the undeployment action.
     */
    public static final DeploymentActionType UNDEPLOY = new DeploymentActionType( "UNDEPLOY" ) {};

    /**
     * The constructor is private to prevent unwanted instanciations from the outside.
     * @param name is passed through to the constructor of the superclass.
     */
    private DeploymentActionType( String name ) {
        super( name, DeploymentActionType.class );
    }

    /**
     * Returns the <code>DeploymentActionType</code> with the given name.
     * @param name the name of the desired <code>DeploymentActionType</code>.
     * @return the <code>DeploymentActionType</code> with the given name.
     */
    public static DeploymentActionType forName( String name ) {
        return getElement( name, DeploymentActionType.class );
    }

    /**
     * Returns true when the <code>DeploymentActionType</code>
     * with a given name exists; false otherwise.
     * @param name the name of the desired <code>DeploymentActionType</code>.
     * @return true when the <code>DeploymentActionType</code>
     * with a given name exists; false otherwise.
     */
    public static boolean exists( String name ) {
        return existsElement( name, DeploymentActionType.class );
    }

}
