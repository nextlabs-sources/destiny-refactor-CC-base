package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2004 by Blue Jungle Inc,
 * Redwood City, CA.
 * Ownership remains with Blue Jungle Inc. All rights reserved worldwide.
 * 
 * @author Sergey Kalinichenko
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/DeploymentType.java#1 $
 */

import com.bluejungle.framework.patterns.EnumBase;

/**
 * This type-safe enumeration represents the type of
 * an entity deployment (production or testing).
 */
public abstract class DeploymentType extends EnumBase {

    public static final DeploymentType PRODUCTION = new DeploymentType( "PRODUCTION" ) {};

    public static final DeploymentType TESTING = new DeploymentType( "TESTING" ) {};

    /**
     * The constructor is private to prevent unwanted instanciations from the outside.
     * @param name is passed through to the constructor of the superclass.
     */
    private DeploymentType( String name ) {
        super( name, DeploymentType.class );
    }

    /**
     * Returns the <code>DeploymentType</code> with the given name.
     * @param name the name of the desired <code>DeploymentType</code>.
     * @return the <code>DeploymentType</code> with the given name.
     */
    public static DeploymentType forName( String name ) {
        return getElement( name, DeploymentType.class );
    }

}

