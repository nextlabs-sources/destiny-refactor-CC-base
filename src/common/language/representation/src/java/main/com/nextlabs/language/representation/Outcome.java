package com.nextlabs.language.representation;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/language/representation/src/java/main/com/nextlabs/language/representation/Outcome.java#1 $
 */

/**
 * This is the enumeration of policy outcomes.
 *
 * @author Sergey Kalinichenko
 */
public enum Outcome {

    /**
     * This outcome specifies that the result of this policy applied to
     * the set of of specific context values is allow.
     */
    ALLOW(1)

    /**
     * This outcome specifies that the result of this policy applied to
     * the set of of specific context values is deny.
     */
,   DENY(2)

    /**
     * This outcome specifies that this policy should behave as inapplicable
     * for the set of of specific context values.
     */
,   NOTHING(0);

    /**
     * A constant for accessing the number of outcome types.
     */
    public static final int SIZE = 3;

    /**
     * The ordinal of this outcome.
     */
    private final int ordinal;

    /**
     * A for-ordinal array of outcomes.
     */
    private static final Outcome[] forOrdinal = new Outcome[SIZE];

    static {
        forOrdinal[ALLOW.getOrdinal()] = ALLOW;
        forOrdinal[DENY.getOrdinal()] = DENY;
        forOrdinal[NOTHING.getOrdinal()] = NOTHING;
    }

    /**
     * Constructs an outcome with the given ordinal.
     *
     * @param ordinal
     */
    private Outcome(int ordinal) {
        this.ordinal = ordinal;
    }

    /**
     * Returns the ordinal of this outcome.
     *
     * @return the ordinal of this outcome.
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * Returns the outcome for the given ordinal.
     *
     * @param ordinal the ordinal for which to get the outcome.
     * @return the Outcome for the specified ordinal.
     */
    public static Outcome forOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= SIZE) {
            throw new IllegalArgumentException("ordinal");
        }
        return forOrdinal[ordinal];
    }

}
