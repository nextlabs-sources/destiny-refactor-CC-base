/*
 * Created on May 10, 2016
 *
 * All sources, binaries and HTML pages (C) copyright 2016 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/openaz/src/java/main/com/nextlabs/openaz/pepapi/DiscretionaryPolicies.java#1 $:
 */

package com.nextlabs.openaz.pepapi;

import org.apache.openaz.xacml.api.Identifier;
import org.apache.openaz.xacml.std.IdentifierImpl;

import com.nextlabs.openaz.utils.Constants;

/**
 * Container class for discretionary policies (also known as ad-hoc policies or "policy on demand").
 *
 * The associated mapper is {@link com.nextlabs.openaz.pepapi.DiscretionaryPoliciesMapper}
 */
public final class DiscretionaryPolicies {
    public static final Identifier CATEGORY_ID = new IdentifierImpl(Constants.ID_NEXTLABS_ATTRIBUTE_CATEGORY, "pod");
    private String pql;
    private boolean ignorePDPPolicies;

    private DiscretionaryPolicies(String pql) {
        this(pql, false);
    }

    private DiscretionaryPolicies(String pql, boolean ignorePDPPolicies) {
        this.pql = pql;
        this.ignorePDPPolicies = ignorePDPPolicies;
    }

    /**
     * Creates a new DiscretionaryPolicies instance with the given pql
     * string. These will be evaluated along with the policies in the
     * bundle
     *
     * @param pql the policy or policies expressed as a PQL string
     * @return
     */
    public static DiscretionaryPolicies newInstance(String pql) {
        return new DiscretionaryPolicies(pql);
    }
    
    /**
     * Creates a new DiscretionaryPolicies instance with the given pql
     * string. These will be evaluated along with the policies in the
     *
     * @param pql the policy or policies expressed as a PQL string
     * @param ignorePDPPolicies if "true" then the policies in the bundle will be ignored and only
     * these policies will be evaluated. If "false" then the policies in the bundle will be evaluated
     * along with these policies
     * @return
     */
    public static DiscretionaryPolicies newInstance(String pql, boolean ignorePDPPolicies) {
        return new DiscretionaryPolicies(pql, ignorePDPPolicies);
    }

    /**
     * Get the PQL
     *
     * @return
     */
    public String getPql() {
        return pql;
    }

    /**
     * Get whether the policies in the bundle should be ignored in favor of these policies
     *
     * @return
     */
    public boolean getIgnorePDPPoliciesFlag() {
        return ignorePDPPolicies;
    }
}
