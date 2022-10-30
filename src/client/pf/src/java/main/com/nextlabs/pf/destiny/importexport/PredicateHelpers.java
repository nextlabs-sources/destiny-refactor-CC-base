package com.nextlabs.pf.destiny.importexport;

import java.util.Iterator;

import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;

/**
 * This class copy com.bluejungle.destiny.policymanager.ui.PredicateHelpers
 * TODO move PredicateHelpers to a shared jar, so everybody can use it 
 * @date May 4, 2007
 */
public class PredicateHelpers {
	/* 
	 * Adds or removes padding constants from the domain object to ensure that
     * composite predicates remain valid and continue to be evaluated properly
     * when they contain too few real predicates.
     */
    public static void rebalanceDomainObject(CompositePredicate domainObject, BooleanOp type) {
        if (type == BooleanOp.AND) {
            rebalanceAndDomainObject(domainObject);
        } else if (type == BooleanOp.OR) {
            rebalanceOrDomainObject(domainObject);
        }
    }

    /**
     * Adds or removes "padding" constants from the domain object The composite
     * must contain at least two predicates Therefore, it is padded with extra
     * TRUE predicates
     */
    private static void rebalanceAndDomainObject(CompositePredicate domainObject) {
        // count the real predicates
        Iterator iter = domainObject.predicates().iterator();
        int count = 0;
        int trueCount = 0;
        while (iter.hasNext()) {
            IPredicate pred = (IPredicate) iter.next();
            if (pred != PredicateConstants.TRUE) {
                count++;
            } else {
                trueCount++;
            }
        }

        int requiredTrues = 2 - count;
        if (requiredTrues < 0) {
            requiredTrues = 0;
        }

        if (trueCount == requiredTrues) {
            return;
        }

        if (trueCount > requiredTrues) {
            // remove extra TRUES
            int numToRemove = trueCount - requiredTrues;
            for (int i = 0; i < domainObject.predicateCount(); i++) {
                if ((numToRemove > 0) && (domainObject.predicateAt(i) == PredicateConstants.TRUE)) {
                    domainObject.removePredicate(i);
                    numToRemove--;
                }
            }
        } else if (trueCount < requiredTrues) {
            // add additional TRUES
            int numToAdd = requiredTrues - trueCount;
            for (int i = 0; i < numToAdd; i++) {
                domainObject.addPredicate(PredicateConstants.TRUE);
            }
        }
    }

    /**
     * Adds or removes "padding" predicate constants to ensure that a predicate
     * is valid A blank predicate should be padded with a TRUE and a FALSE
     * constant A predicate with one real entry should be padded with a FALSE
     * constant A predicate with two or more entries should not be padded.
     */
    private static void rebalanceOrDomainObject(CompositePredicate domainObject) {
        if (domainObject.predicateCount() == 0) {
            // create the blank structure
            domainObject.addPredicate(PredicateConstants.FALSE);
            domainObject.addPredicate(PredicateConstants.TRUE);
            return;
        }
        if (domainObject.predicateCount() == 1) {
            // if the only predicate is a constant, add the counterpart
            IPredicate predicate = domainObject.predicateAt(0);
            if (predicate == PredicateConstants.FALSE) {
                domainObject.addPredicate(PredicateConstants.TRUE);
            } else if (predicate == PredicateConstants.TRUE) {
                domainObject.addPredicate(PredicateConstants.FALSE);
            } else {
                // otherwise, balance out the list with a FALSE predicate
                domainObject.addPredicate(PredicateConstants.FALSE);
            }
            return;
        }
        // at this point, we have 2 or more predicates
        // count the "real" predicates and what constants we have
        Iterator iter = domainObject.predicates().iterator();
        int count = 0;
        boolean foundTrue = false;
        boolean foundFalse = false;
        while (iter.hasNext()) {
            IPredicate pred = (IPredicate) iter.next();
            if (pred instanceof PredicateConstants) {
                if (pred == PredicateConstants.TRUE) {
                    foundTrue = true;
                }
                if (pred == PredicateConstants.FALSE) {
                    foundFalse = true;
                }
            } else {
                count++;
            }
        }
        // if the number of real predicates is 0, make sure there is a TRUE and
        // a FALSE predicate
        if (count == 0) {
            if (foundTrue && foundFalse) {
                // structure is ok
                return;
            }
            // strip out everything and build a blank structure;
            // this really shouldn't ever happen- but just in case we're going
            // to assimilate the structure
            for (int i = 0; i < domainObject.predicateCount(); i++) {
                domainObject.removePredicate(i);
            }
            domainObject.addPredicate(PredicateConstants.FALSE);
            domainObject.addPredicate(PredicateConstants.TRUE);
            return;
        }
        // if the number of real predicates is 1, remove all TRUE constants,
        // make sure there is a FALSE constant
        if (count == 1) {
            if (foundTrue) {
                // remove any TRUE constants
                for (int i = 0; i < domainObject.predicateCount(); i++) {
                    if (domainObject.predicateAt(i) == PredicateConstants.TRUE) {
                        domainObject.removePredicate(i);
                    }
                }
                if (!foundFalse) {
                    // add the required FALSE constant
                    domainObject.addPredicate(PredicateConstants.FALSE);
                }
            }
            return;
        }
        // if the number of real predicates is 2 or more, remove all constants
        for (int i = domainObject.predicateCount() - 1; i >= 0; i--) {
            if (domainObject.predicateAt(i) instanceof PredicateConstants) {
                domainObject.removePredicate(i);
            }
        }
    }
}
