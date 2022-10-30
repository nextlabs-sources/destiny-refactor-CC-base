package com.bluejungle.pf.destiny.lifecycle;

/* All sources, binaries and HTML pages (C) Copyright 2007 by Next Labs Inc,
 * San Mateo, CA.
 * Ownership remains with Next Labs Inc. All rights reserved worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/destiny/lifecycle/ObligationDescriptor.java#1 $
 */

/**
 */
public class ObligationDescriptor {
    /** Represents the display name of the obligation */
    private final String displayName;

    /** Represents the internalname of the obligation */
    private final String internalName;

    /** Represents the arguments of the obligation */
    private final ObligationArgument[] args;
    
    /**
     * Constructs the <code>ObligationDescriptor</code> with the specified
     * <code>displayName</code> and <code>pqlName</code>.
     * @param displayName the display name of the obligation.
     * @param internalName the internal name of the obligation.
     * @param args the arguments of the obligation.
     */
    public ObligationDescriptor(String displayName, String internalName, ObligationArgument[] args) {
        this.displayName = displayName;
        this.internalName = internalName;
        this.args = args;
    }

    /**
     * Accesses the display name of the obligation
     * @return the display name of the obligation
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Accesses the internal name of the obligation
     * @return the internal name of the obligation
     */
    public String getInternalName() {
        return internalName;
    }

    /**
     * Access the arguments of the obligation
     * @return the obligation arguments
     */
    public ObligationArgument[] getObligationArguments() {
        return args;
    }
}
