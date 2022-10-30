/*
 * Created on May 2, 2005 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.destiny.policymanager.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.bluejungle.destiny.policymanager.PolicyEnum;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.destiny.lib.LeafObject;
import com.bluejungle.pf.destiny.lib.LeafObjectType;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.PQLParser;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecManager;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.common.SpecReference;
import com.bluejungle.pf.domain.destiny.environment.RemoteAccessAttribute;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.obligation.DObligationManager;
import com.bluejungle.pf.domain.destiny.obligation.IDObligationManager;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.common.SpecType;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;

/**
 * @author dstarke
 * 
 */
public class PredicateHelpers {

    private static final IDSpecManager sm = (IDSpecManager) ComponentManagerFactory.getComponentManager().getComponent(IDSpecManager.COMP_INFO);
    private static IDObligationManager oblMgr = (IDObligationManager) ComponentManagerFactory.getComponentManager().getComponent(DObligationManager.COMP_INFO);

    public static void fillInUserComponent(IDSpec spec) {
        fillInStandardComponent(spec);
    }

    public static void fillInDesktopComponent(IDSpec spec) {
        fillInStandardComponent(spec);
    }

    public static void fillInPortalComponent(IDSpec spec) {
        fillInStandardComponent(spec);
    }

    public static void fillInResourceComponent(IDSpec spec) {
        fillInStandardComponent(spec);
    }

    public static void fillInApplicationComponent(IDSpec spec) {
        fillInStandardComponent(spec);
        /*
         * if (spec == null) { // TODO: report the error return; }
         * 
         * List<IPredicate> blankLineContents = new ArrayList<IPredicate>();
         * blankLineContents.add(PredicateConstants.FALSE);
         * blankLineContents.add(PredicateConstants.TRUE); CompositePredicate
         * initialCompositionLine = new CompositePredicate(BooleanOp.OR,
         * blankLineContents);
         * 
         * List<IPredicate> initialCompositionList = new ArrayList<IPredicate>();
         * initialCompositionList.add(initialCompositionLine);
         * initialCompositionList.add(PredicateConstants.TRUE);
         * CompositePredicate composition = new
         * CompositePredicate(BooleanOp.AND, initialCompositionList);
         * 
         * List<IPredicate> parts = new ArrayList<IPredicate>();
         * parts.add(composition); parts.add(PredicateConstants.TRUE);
         * 
         * CompositePredicate predicate = new CompositePredicate(BooleanOp.AND,
         * parts);
         * 
         * spec.setPredicate(predicate);
         */
    }

    public static void fillInActionComponent(IDSpec spec) {
        CompositePredicate predicate = new CompositePredicate(BooleanOp.OR, buildConstantArrayList(false));
        spec.setPredicate(predicate);
    }

    private static void fillInStandardComponent(IDSpec spec) {
        if (spec == null) {
            // TODO: report the error
            return;
        }

        List<IPredicate> blankLineContents = new ArrayList<IPredicate>();
        blankLineContents.add(PredicateConstants.FALSE);
        blankLineContents.add(PredicateConstants.TRUE);
        CompositePredicate initialCompositionLine = new CompositePredicate(BooleanOp.OR, blankLineContents);

        List<IPredicate> initialCompositionList = new ArrayList<IPredicate>();
        initialCompositionList.add(initialCompositionLine);
        initialCompositionList.add(PredicateConstants.TRUE);
        CompositePredicate composition = new CompositePredicate(BooleanOp.AND, initialCompositionList);

        List<IPredicate> propList = new ArrayList<IPredicate>();
        propList.add(PredicateConstants.TRUE);
        propList.add(PredicateConstants.TRUE);

        CompositePredicate properties = new CompositePredicate(BooleanOp.AND, propList);

        List<IPredicate> parts = new ArrayList<IPredicate>();
        parts.add(composition);
        parts.add(properties);

        CompositePredicate predicate = new CompositePredicate(BooleanOp.AND, parts);

        spec.setPredicate(predicate);
    }

    public static void fillInPolicy(IDPolicy policy, String type) {
        ITarget target = policy.getTarget();

        CompositePredicate userPred = new CompositePredicate(BooleanOp.AND, buildConstantArrayList(true));
        CompositePredicate hostPred = new CompositePredicate(BooleanOp.AND, buildConstantArrayList(true));
        CompositePredicate appPred = new CompositePredicate(BooleanOp.AND, buildConstantArrayList(true));

        List<IPredicate> subjectParts = new ArrayList<IPredicate>();
        subjectParts.add(userPred);
        subjectParts.add(hostPred);
        subjectParts.add(appPred);
        CompositePredicate subjectPred = new CompositePredicate(BooleanOp.AND, subjectParts);

        target.setSubjectPred(subjectPred);

        CompositePredicate fromPred = new CompositePredicate(BooleanOp.AND, buildConstantArrayList(true));
        target.setFromResourcePred(fromPred);

        CompositePredicate toPred = new CompositePredicate(BooleanOp.AND, buildConstantArrayList(true));

        List<IPredicate> dateParts = new ArrayList<IPredicate>();
        dateParts.add(PredicateConstants.TRUE);
        dateParts.add(PredicateConstants.TRUE);
        dateParts.add(PredicateConstants.TRUE);

        policy.setAttribute("Usage", true);

        // always log deny enforcements
        IObligation newObligation = oblMgr.createLogObligation();
        policy.addObligation(newObligation, EffectType.DENY);

        // TODO: fix drag-over composition controls to open automatically, then
        // remove the following
        // temporary code to pre-open user, document, and action composition
        // controls
        CompositePredicate member = new CompositePredicate(BooleanOp.OR, new ArrayList<IPredicate>());
        PredicateHelpers.rebalanceDomainObject(member, BooleanOp.OR);
        PredicateHelpers.addPredicate(userPred, member);

        member = new CompositePredicate(BooleanOp.OR, new ArrayList<IPredicate>());
        PredicateHelpers.rebalanceDomainObject(member, BooleanOp.OR);
        PredicateHelpers.addPredicate(fromPred, member);

        CompositePredicate actPred = new CompositePredicate(BooleanOp.AND, buildConstantArrayList(true));
        member = new CompositePredicate(BooleanOp.OR, new ArrayList<IPredicate>());
        PredicateHelpers.rebalanceDomainObject(member, BooleanOp.OR);
        PredicateHelpers.addPredicate(actPred, member);
        if (type.equals(PolicyEnum.COMMUNICATION_POLICY.toString())) {
            policy.setAttribute(PolicyEnum.COMMUNICATION_POLICY.name(), true);

            target.setToSubjectPred(toPred);
        } else if (type.equals(PolicyEnum.DOCUMENT_POLICY.toString())) {
            policy.setAttribute(PolicyEnum.DOCUMENT_POLICY.name(), true);

            target.setToResourcePred(toPred);
        }
        target.setActionPred(actPred);
    }

    private static List<IPredicate> buildConstantArrayList(boolean isAnd) {
        List<IPredicate> ret = new ArrayList<IPredicate>();
        ret.add(PredicateConstants.TRUE);
        if (isAnd) {
            ret.add(PredicateConstants.TRUE);
        } else {
            ret.add(PredicateConstants.FALSE);
        }
        return ret;
    }

    // ------------------------
    // Object Balancing Helpers
    // ------------------------

    /**
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

    /**
     * Removes a predicate in a domain object at a given location. Ignores TRUE
     * and FALSE constants when calculating the remove index.
     * 
     * @param domainObject
     * @param index
     * @return
     */
    public static IPredicate removePredicateAt(CompositePredicate domainObject, int index) {
        IPredicate ret = null;
        int realPredicateIndex = 0;
        for (int i = 0; i < domainObject.predicateCount(); i++) {
            if (!(domainObject.predicateAt(i) instanceof PredicateConstants)) {
                if (index == realPredicateIndex) {
                    ret = domainObject.removePredicate(i);
                    break;
                }
                realPredicateIndex++;
            }
        }
        rebalanceDomainObject(domainObject, domainObject.getOp());
        return ret;
    }

    /**
     * Adds a predicate to a domain object and automatically manages any TRUE or
     * FALSE constants that may have been inserted to preserve the structure of
     * the composite.
     * 
     * @param domainObject
     * @param predicate
     */
    public static void addPredicate(CompositePredicate domainObject, IPredicate predicate) {
        domainObject.addPredicate(predicate);
        rebalanceDomainObject(domainObject, domainObject.getOp());
    }

    /**
     * Inserts a predicate in a domain object at a given location. Ignores TRUE
     * and FALSE constants when calculating the insertion index.
     * 
     * @param domainObject
     * @param toInsert
     * @param pos
     */
    public static void insertPredicateAt(CompositePredicate domainObject, IPredicate toInsert, int pos) {
        int realPredicateIndex = 0;
        boolean inserted = false;
        for (int i = 0; i < domainObject.predicateCount(); i++) {
            if (!(domainObject.predicateAt(i) instanceof PredicateConstants)) {
                if (pos == realPredicateIndex) {
                    domainObject.insertElement(toInsert, i);
                    inserted = true;
                    break;
                }
                realPredicateIndex++;
            }
        }
        if (!inserted) {
            domainObject.insertElement(toInsert, domainObject.predicateCount());
        }
        rebalanceDomainObject(domainObject, domainObject.getOp());
    }

    /**
     * 
     * @param domainObject
     * @param index
     * @return the predicate at the given index. Ignores TRUE and FALSE
     *         constants in calculating the index of a sub-predicate.
     */
    public static IPredicate getPredicateAt(CompositePredicate domainObject, int index) {
        int i = 0;
        int realPredicateIndex = 0;
        while (i < domainObject.predicateCount() && realPredicateIndex <= index) {
            if (!(domainObject.predicateAt(i) instanceof PredicateConstants)) {
                if (realPredicateIndex == index) {
                    return domainObject.predicateAt(i);
                }
                realPredicateIndex++;
            }
            i++;
        }
        return null;
    }

    /**
     * 
     * @param domainObject
     * @return the number of predicates that are not the constants TRUE or FALSE
     */
    public static int getRealPredicateCount(CompositePredicate domainObject) {
        Iterator iter = domainObject.predicates().iterator();
        int count = 0;
        while (iter.hasNext()) {
            IPredicate pred = (IPredicate) iter.next();
            if (!(pred instanceof PredicateConstants)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 
     * @param domainObject
     * @param member
     * @return the index of the member in the domain object when TRUE and FALSE
     *         predicates are ignored
     */
    public static int getRealIndexOfObject(CompositePredicate domainObject, IPredicate member) {
        int i = 0;
        int realPredicateIndex = 0;
        while (i < domainObject.predicateCount()) {
            IPredicate obj = domainObject.predicateAt(i);
            if (!(obj instanceof PredicateConstants)) {
                if (obj == member) {
                    return realPredicateIndex;
                }
                realPredicateIndex++;
            }
            i++;
        }
        return -1;
    }

    /**
     * 
     * @param domainObject
     * @return true if the given predicate is a NOT predicate, false otherwise
     */
    public static boolean isNegationPredicate(CompositePredicate domainObject) {
        return domainObject.getOp() == BooleanOp.NOT;
    }

    /**
     * 
     * @param domainObject
     * @return a predicate representing the negation of the given predicate
     */
    public static IPredicate getNegationOfPredicate(CompositePredicate domainObject) {
        if (isNegationPredicate(domainObject)) {
            return domainObject.predicateAt(0);
        } else {
            CompositePredicate ret = new CompositePredicate(BooleanOp.NOT, domainObject);
            return ret;
        }
    }

    /**
     * Remove any references to object of the specified name and type from the
     * domain object
     * 
     * @param domainObject
     * @param name
     * @param entityType
     */
    public static void removeReferences(IHasId domainObject, String name, EntityType entityType) {
        if (domainObject instanceof IDPolicy) {
            ITarget target = ((IDPolicy) domainObject).getTarget();
            if (entityType == EntityType.ACTION) {
                removeReferences(null, target.getActionPred(), name, entityType);
            } else if (entityType == EntityType.APPLICATION || entityType == EntityType.USER || entityType == EntityType.HOST) {
                removeReferences(null, target.getSubjectPred(), name, entityType);
                removeReferences(null, target.getToSubjectPred(), name, entityType);
            } else if (entityType == EntityType.RESOURCE) {
                removeReferences(null, target.getFromResourcePred(), name, entityType);
                removeReferences(null, target.getToResourcePred(), name, entityType);
            }
        } else if (domainObject instanceof IDSpec) {
            IPredicate pred = ((IDSpec) domainObject).getPredicate();
            removeReferences(null, pred, name, entityType);
        }
    }

    private static void removeReferences(CompositePredicate parent, IPredicate pred, String name, EntityType type) {
        if (pred instanceof CompositePredicate) {
            CompositePredicate comp = (CompositePredicate) pred;
            for (int i = comp.predicateCount() - 1; i >= 0; i--) {
                IPredicate subPred = comp.predicateAt(i);
                removeReferences(comp, subPred, name, type);
            }
        } else if (pred instanceof IDSpecRef) {
            IDSpecRef ref = (IDSpecRef) pred;
            if (name.equals(ref.getReferencedName())) {
                if (parent != null) {
                    parent.removePredicate(pred);
                    rebalanceDomainObject(parent, parent.getOp());
                }
            }
        }
    }

    /**
     * 
     * @param actionPred
     * @return a set representing the actions contained in the given composite
     *         predicate
     */
    public static Set<IDAction> getActionSet(CompositePredicate actionPred) {
        Set<IDAction> ret = new HashSet<IDAction>();
        Iterator iter = actionPred.predicates().iterator();
        while (iter.hasNext()) {
            IPredicate pred = (IPredicate) iter.next();
            if (pred instanceof IDAction) {
                ret.add((IDAction) pred);
            }
        }
        return ret;
    }

    /**
     * Update a composite predicate to represent matching a given set of actions
     * 
     * @param pred
     * @param actionSet
     */
    public static void updateActionSet(CompositePredicate pred, Set<IDAction> actionSet) {
        Set<IDAction> oldSet = getActionSet(pred);

        // find out what needs to be added
        List<IDAction> toAdd = new ArrayList<IDAction>();
        Iterator<IDAction> addIter = actionSet.iterator();
        while (addIter.hasNext()) {
            IDAction obj = addIter.next();
            if (!oldSet.contains(obj)) {
                toAdd.add(obj);
            }
        }
        // add new things
        Iterator<IDAction> toAddIter = toAdd.iterator();
        while (toAddIter.hasNext()) {
            Object obj = toAddIter.next();
            pred.addPredicate((IPredicate) obj);
        }
        // remove extra stuff
        oldSet.removeAll(actionSet);
        Iterator<IDAction> iter = oldSet.iterator();
        while (iter.hasNext()) {
            pred.removePredicate((IPredicate) iter.next());
        }
        rebalanceDomainObject(pred, BooleanOp.OR);
    }

    // ------------------------
    // Time Condition Helpers
    // ------------------------

    private static boolean isStartTime(IPredicate condition) {
        if (condition instanceof Relation) {
            Relation relation = (Relation) condition;
            RelationOp op = relation.getOp();
            if ((relation.getLHS() == TimeAttribute.IDENTITY) && (op == RelationOp.GREATER_THAN || op == RelationOp.GREATER_THAN_EQUALS)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEndTime(IPredicate condition) {
        if (condition instanceof Relation) {
            Relation relation = (Relation) condition;
            RelationOp op = relation.getOp();
            if ((relation.getLHS() == TimeAttribute.IDENTITY) && (op == RelationOp.LESS_THAN || op == RelationOp.LESS_THAN_EQUALS)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDailyFromTime(IPredicate condition) {
        if (condition instanceof Relation) {
            Relation relation = (Relation) condition;
            RelationOp op = relation.getOp();
            if ((relation.getLHS() == TimeAttribute.TIME) && (op == RelationOp.GREATER_THAN || op == RelationOp.GREATER_THAN_EQUALS)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDailyToTime(IPredicate condition) {
        if (condition instanceof Relation) {
            Relation relation = (Relation) condition;
            RelationOp op = relation.getOp();
            if ((relation.getLHS() == TimeAttribute.TIME) && (op == RelationOp.LESS_THAN || op == RelationOp.LESS_THAN_EQUALS)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWeekDaysPredicate(IPredicate condition) {
        // week day predicates are Relations on WEEKDAY attributes
        // or OR composite predicates
        // containing only relations on WEEKDAY attributes
        if (condition instanceof Relation) {
            Relation relation = (Relation) condition;
            if (relation.getLHS() == TimeAttribute.WEEKDAY) {
                return true;
            }
        }
        if (condition instanceof CompositePredicate) {
            CompositePredicate comp = (CompositePredicate) condition;
            BooleanOp op = comp.getOp();
            if (op != BooleanOp.OR) {
                return false;
            }
            Iterator iter = comp.predicates().iterator();
            while (iter.hasNext()) {
                IPredicate pred = (IPredicate) iter.next();
                if (!(pred instanceof Relation)) {
                    return false;
                }
                IExpression exp = ((Relation) pred).getLHS();
                if (exp != TimeAttribute.WEEKDAY) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static boolean isDayOfMonthRelation(IPredicate condition) {
        if (condition instanceof Relation) {
            Relation relation = (Relation) condition;
            if (relation.getLHS() == TimeAttribute.DATE) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDOWIMRelation(IPredicate condition) {
        if (condition instanceof Relation) {
            Relation relation = (Relation) condition;
            if (relation.getLHS() == TimeAttribute.DOWIM) {
                return true;
            }
        }
        return false;
    }

    /**
     * This is a helper class that implements the logic for finding a specific
     * time predicate in the conditions list.
     * 
     * @param condition
     *            the condition to search
     * @param tester
     *            contains a function used to test whether a particular
     *            predicate matches
     * @return the predicate matching the test
     */
    private static IPredicate getTime(IPredicate condition, TimeTester tester) {
        if (condition == null) {
            return null;
        }
        if (tester.test(condition)) {
            return condition;
        }
        if (condition instanceof CompositePredicate) {
            CompositePredicate comp = (CompositePredicate) condition;
            Iterator iter = comp.predicates().iterator();
            while (iter.hasNext()) {
                IPredicate pred = (IPredicate) iter.next();
                IPredicate time = getTime(pred, tester);
                if (time != null) {
                    return time;
                }
            }
        }
        return null;
    }

    /**
     * @param condition
     * @return the predicate representing the enforcement start time
     */
    public static IPredicate getStartTime(IPredicate condition) {
        return getTime(condition, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isStartTime(condition);
            }
        });
    }

    /**
     * 
     * @param condition
     * @return the predicate representing the enforcement end time
     */
    public static IPredicate getEndTime(IPredicate condition) {
        return getTime(condition, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isEndTime(condition);
            }
        });
    }

    /**
     * 
     * @param condition
     * @return the predicate representing the start time for daily enforcement
     */
    public static IPredicate getDailyFromTime(IPredicate condition) {
        return getTime(condition, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isDailyFromTime(condition);
            }
        });
    }

    /**
     * 
     * @param condition
     * @return the predicate representing the end time for daily enforcement
     */
    public static IPredicate getDailyToTime(IPredicate condition) {
        return getTime(condition, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isDailyToTime(condition);
            }
        });
    }

    /**
     * 
     * @param condition
     * @return the predicate representing the day of the week conditions
     */
    public static IPredicate getWeekDayPredicate(IPredicate condition) {
        return getTime(condition, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isWeekDaysPredicate(condition);
            }
        });
    }

    /**
     * 
     * @param condition
     * @return the predicate representing the day of the month conditions
     */
    public static IPredicate getDayOfMonthPredicate(IPredicate condition) {
        return getTime(condition, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isDayOfMonthRelation(condition);
            }
        });
    }

    /**
     * 
     * @param condition
     * @return the predicate representing the day of the week in the month
     *         conditions
     */
    public static IPredicate getDOWIMPredicate(IPredicate condition) {
        return getTime(condition, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isDOWIMRelation(condition);
            }
        });
    }

    public static IPredicate getFullDOWIMInfoPredicates(IPredicate condition) {
        IPredicate dowim = getDOWIMPredicate(condition);
        IPredicate wdp = getWeekDayPredicate(condition);
        if (dowim == null || wdp == null) {
            return null;
        }
        List<IPredicate> list = new ArrayList<IPredicate>();
        list.add(dowim);
        list.add(wdp);
        return new CompositePredicate(BooleanOp.AND, list);
    }

    /**
     * Add or set the start time/date for a policy
     * 
     * @param policy
     * @param time
     */
    public static void setStartTime(IDPolicy policy, IExpression time) {
        IPredicate condition = policy.getConditions();
        Relation relation = (Relation) getStartTime(condition);
        if (relation != null) {
            relation.setRHS(time);
        } else {
            relation = new Relation(RelationOp.GREATER_THAN_EQUALS, TimeAttribute.IDENTITY, time);
            addPredicateToConditions(policy, relation);
        }
    }

    public static void setConnectionType(IDPolicy policy, IExpression type) {
        IPredicate condition = policy.getConditions();
        Relation relation = (Relation) getConnectionType(condition);
        if (relation != null) {
            relation.setRHS(type);
        } else {
            relation = new Relation(RelationOp.EQUALS, RemoteAccessAttribute.REMOTE_ACCESS, type);
            addPredicateToConditions(policy, relation);
        }
    }

    public static void setConnectionSite(IDPolicy policy, IExpression type) {
        IPredicate condition = policy.getConditions();
        Relation relation = (Relation) getConnectionSite(condition);
        if (relation != null) {
            relation.setRHS(type);
        } else {
            relation = new Relation(RelationOp.EQUALS, RemoteAccessAttribute.REMOTE_ADDRESS, type);
            addPredicateToConditions(policy, relation);
        }
    }

    public static IPredicate getConnectionType(IPredicate condition) {
        return getType(condition, new TypeTester() {

            public boolean test(IPredicate condition) {
                return isConnectionType(condition);
            }
        });
    }

    public static IPredicate getConnectionSite(IPredicate condition) {
        return getType(condition, new TypeTester() {

            public boolean test(IPredicate condition) {
                return isConnectionSite(condition);
            }
        });
    }

    public static void removeConnectionType(IDPolicy policy) {
        removeType(policy, new TypeTester() {

            public boolean test(IPredicate condition) {
                return isConnectionType(condition);
            }
        });
    }

    public static void removeConnectionSite(IDPolicy policy) {
        removeSite(policy, new TypeTester() {

            public boolean test(IPredicate condition) {
                return isConnectionSite(condition);
            }
        });
    }

    private static void removeType(IDPolicy policy, TypeTester tester) {
        IPredicate condition = policy.getConditions();
        if (condition == null) {
            return;
        }
        if (tester.test(condition)) {
            policy.setConditions(null);
            return;
        }
        if (condition instanceof CompositePredicate) {
            removeType((CompositePredicate) condition, policy, tester);
        }
    }

    private static void removeSite(IDPolicy policy, TypeTester tester) {
        IPredicate condition = policy.getConditions();
        if (condition == null) {
            return;
        }
        if (tester.test(condition)) {
            policy.setConditions(null);
            return;
        }
        if (condition instanceof CompositePredicate) {
            removeSite((CompositePredicate) condition, policy, tester);
        }
    }

    private static void removeType(CompositePredicate pred, CompositePredicate parent, TypeTester tester) {
        List<Integer> removeIndices = new ArrayList<Integer>();
        for (int i = 0; i < pred.predicateCount(); i++) {
            IPredicate child = pred.predicateAt(i);
            if (tester.test(child)) {
                removeIndices.add(new Integer(i));
                continue;
            }
            if (child instanceof CompositePredicate) {
                removeType((CompositePredicate) child, pred, tester);
            }
        }
        // remove predicates in reverse order to make sure we remove the right
        // ones
        for (int j = removeIndices.size() - 1; j >= 0; j--) {
            pred.removePredicate(((Integer) removeIndices.get(j)).intValue());
        }
        if (pred.predicateCount() == 1) {
            // unwrap single conditions
            parent.removePredicate(pred);
            parent.addPredicate(pred.predicateAt(0));
        } else if (pred.predicateCount() == 0) {
            parent.removePredicate(pred);
        }
    }

    private static void removeSite(CompositePredicate pred, CompositePredicate parent, TypeTester tester) {
        List<Integer> removeIndices = new ArrayList<Integer>();
        for (int i = 0; i < pred.predicateCount(); i++) {
            IPredicate child = pred.predicateAt(i);
            if (tester.test(child)) {
                removeIndices.add(new Integer(i));
                continue;
            }
            if (child instanceof CompositePredicate) {
                removeSite((CompositePredicate) child, pred, tester);
            }
        }
        // remove predicates in reverse order to make sure we remove the right
        // ones
        for (int j = removeIndices.size() - 1; j >= 0; j--) {
            pred.removePredicate(((Integer) removeIndices.get(j)).intValue());
        }
        if (pred.predicateCount() == 1) {
            // unwrap single conditions
            parent.removePredicate(pred);
            parent.addPredicate(pred.predicateAt(0));
        } else if (pred.predicateCount() == 0) {
            parent.removePredicate(pred);
        }
    }

    private static void removeType(CompositePredicate pred, IDPolicy parent, TypeTester tester) {
        List<Integer> removeIndices = new ArrayList<Integer>();
        for (int i = 0; i < pred.predicateCount(); i++) {
            IPredicate child = pred.predicateAt(i);
            if (tester.test(child)) {
                removeIndices.add(new Integer(i));
                continue;
            }
            if (child instanceof CompositePredicate) {
                removeType((CompositePredicate) child, pred, tester);
            }
        }
        // remove predicates in reverse order to make sure we remove the right
        // ones
        for (int j = removeIndices.size() - 1; j >= 0; j--) {
            pred.removePredicate(((Integer) removeIndices.get(j)).intValue());
        }
        if (pred.predicateCount() == 1) {
            // unwrap single conditions
            parent.setConditions(pred.predicateAt(0));
        } else if (pred.predicateCount() == 0) {
            parent.setConditions(null);
        }
    }

    private static void removeSite(CompositePredicate pred, IDPolicy parent, TypeTester tester) {
        List<Integer> removeIndices = new ArrayList<Integer>();
        for (int i = 0; i < pred.predicateCount(); i++) {
            IPredicate child = pred.predicateAt(i);
            if (tester.test(child)) {
                removeIndices.add(new Integer(i));
                continue;
            }
            if (child instanceof CompositePredicate) {
                removeType((CompositePredicate) child, pred, tester);
            }
        }
        // remove predicates in reverse order to make sure we remove the right
        // ones
        for (int j = removeIndices.size() - 1; j >= 0; j--) {
            pred.removePredicate(((Integer) removeIndices.get(j)).intValue());
        }
        if (pred.predicateCount() == 1) {
            // unwrap single conditions
            parent.setConditions(pred.predicateAt(0));
        } else if (pred.predicateCount() == 0) {
            parent.setConditions(null);
        }
    }

    private static boolean isConnectionType(IPredicate condition) {
        if (condition instanceof Relation) {
            Relation relation = (Relation) condition;
            RelationOp op = relation.getOp();
            if ((relation.getLHS() == RemoteAccessAttribute.REMOTE_ACCESS) && (op == RelationOp.EQUALS)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isConnectionSite(IPredicate condition) {
        if (condition instanceof Relation) {
            Relation relation = (Relation) condition;
            RelationOp op = relation.getOp();
            if ((relation.getLHS() == RemoteAccessAttribute.REMOTE_ADDRESS) && (op == RelationOp.EQUALS)) {
                return true;
            }
        }
        return false;
    }

    private static abstract class TypeTester {

        public abstract boolean test(IPredicate condition);
    }

    private static IPredicate getType(IPredicate condition, TypeTester tester) {
        if (condition == null) {
            return null;
        }
        if (tester.test(condition)) {
            return condition;
        }
        if (condition instanceof CompositePredicate) {
            CompositePredicate comp = (CompositePredicate) condition;
            Iterator iter = comp.predicates().iterator();
            while (iter.hasNext()) {
                IPredicate pred = (IPredicate) iter.next();
                IPredicate time = getType(pred, tester);
                if (time != null) {
                    return time;
                }
            }
        }
        return null;
    }

    /**
     * Add or set the end time/date for a policy
     * 
     * @param policy
     * @param time
     */
    public static void setEndTime(IDPolicy policy, IExpression time) {
        IPredicate condition = policy.getConditions();
        Relation relation = (Relation) getEndTime(condition);
        if (relation != null) {
            relation.setRHS(time);
        } else {
            relation = new Relation(RelationOp.LESS_THAN_EQUALS, TimeAttribute.IDENTITY, time);
            addPredicateToConditions(policy, relation);
        }
    }

    /**
     * Add or set the beginning time for daily enforcement in a policy
     * 
     * @param policy
     * @param time
     */
    public static void setDailyToTime(IDPolicy policy, IExpression time) {
        IPredicate condition = policy.getConditions();
        Relation relation = (Relation) getDailyToTime(condition);
        if (relation != null) {
            relation.setRHS(time);
        } else {
            relation = new Relation(RelationOp.LESS_THAN_EQUALS, TimeAttribute.TIME, time);
            addPredicateToConditions(policy, relation);
        }
    }

    /**
     * Add or set the end time for daily enforcement in a policy
     * 
     * @param policy
     * @param time
     */
    public static void setDailyFromTime(IDPolicy policy, IExpression time) {
        IPredicate condition = policy.getConditions();
        Relation relation = (Relation) getDailyFromTime(condition);
        if (relation != null) {
            relation.setRHS(time);
        } else {
            relation = new Relation(RelationOp.GREATER_THAN_EQUALS, TimeAttribute.TIME, time);
            addPredicateToConditions(policy, relation);
        }
    }

    /**
     * Add or set the predicate representing the enforcement days of the week in
     * a policy
     * 
     * @param policy
     * @param newWeekdays
     */
    public static void setWeekdayPredicate(IDPolicy policy, IPredicate newWeekdays) {
        removeWeekdayPredicate(policy);
        addPredicateToConditions(policy, newWeekdays);
    }

    public static void addPredicateToConditions(IDPolicy policy, IPredicate predicate) {
        IPredicate condition = policy.getConditions();
        if (condition == null) {
            policy.setConditions(predicate);
            return;
        }
        if (condition instanceof CompositePredicate && ((CompositePredicate) condition).getOp() == BooleanOp.AND) {
            ((CompositePredicate) condition).addPredicate(predicate);
        } else {
            // create a new composite predicate in the policy
            composeNewPredicate(policy, predicate);
        }
    }

    public static void addWeekdayExpressionToConditions(IDPolicy policy, String name) {
        IPredicate pred = getWeekDayPredicate(policy.getConditions());
        Relation rel = new Relation(RelationOp.EQUALS, TimeAttribute.WEEKDAY, TimeAttribute.WEEKDAY.build(name));
        if (pred == null) {
            setWeekdayPredicate(policy, rel);
        } else if (pred instanceof Relation) {
            // if only one, and it is a new one, we'll wrap it in a new
            // composite
            IExpression exp = ((Relation) pred).getRHS();
            if (!exp.evaluate(null).equals(rel.getRHS().evaluate(null))) {
                List<IPredicate> list = new ArrayList<IPredicate>();
                list.add(pred);
                list.add(rel);
                CompositePredicate newPredicate = new CompositePredicate(BooleanOp.OR, list);
                PredicateHelpers.setWeekdayPredicate(policy, newPredicate);
            }
        } else if (pred instanceof CompositePredicate) {
            // if already a composite, just add this one to the list
            ((CompositePredicate) pred).addPredicate(rel);
        }
    }

    public static void removeWeekdayExpressionFromConditions(IDPolicy policy, String name) {
        IPredicate pred = getWeekDayPredicate(policy.getConditions());
        IExpression expToRemove = TimeAttribute.WEEKDAY.build(name);
        if (pred instanceof CompositePredicate) {
            CompositePredicate comp = (CompositePredicate) pred;
            Iterator iter = comp.predicates().iterator();
            while (iter.hasNext()) {
                Relation rel = (Relation) iter.next();
                IExpression exp = rel.getRHS();
                if (exp.evaluate(null).equals(expToRemove.evaluate(null))) {
                    comp.removePredicate(rel);
                    break;
                }
            }
            if (comp.predicateCount() == 1) {
                // unrwap the single predicates
                PredicateHelpers.setWeekdayPredicate(policy, comp.predicateAt(0));
            }
        }
    }

    /**
     * Helper function that composes a new predicate with the existing policy
     * conditions
     * 
     * @param policy
     * @param newPredicate
     */
    private static void composeNewPredicate(IDPolicy policy, IPredicate newPredicate) {
        List<IPredicate> conditionParts = new ArrayList<IPredicate>();
        conditionParts.add(policy.getConditions());
        conditionParts.add(newPredicate);
        CompositePredicate pred = new CompositePredicate(BooleanOp.AND, conditionParts);
        policy.setConditions(pred);
    }

    /**
     * Helper class representing a test function
     * 
     */
    private static abstract class TimeTester {

        public abstract boolean test(IPredicate condition);
    }

    /**
     * Remove a predicate from the conditions of a policy. The predicate to
     * remove is identified by the TimeTester that is passed in.
     * 
     * @param policy
     * @param tester
     */
    private static void removeTime(IDPolicy policy, TimeTester tester) {
        IPredicate condition = policy.getConditions();
        if (condition == null) {
            return;
        }
        if (tester.test(condition)) {
            policy.setConditions(null);
            return;
        }
        if (condition instanceof CompositePredicate) {
            removeTime((CompositePredicate) condition, policy, tester);
        }
    }

    /**
     * Removes a predicate from the composite predicate representing the
     * conditions of a policy. The predicate to remove is identified by the
     * TimeTester that is passed in. If the predicate was in a composite
     * predicate, this method will automatically unwrap single predicates or
     * remove empty predicates resulting from the removal.
     * 
     * @param pred
     * @param parent
     * @param tester
     */
    private static void removeTime(CompositePredicate pred, IDPolicy parent, TimeTester tester) {
        List<Integer> removeIndices = new ArrayList<Integer>();
        for (int i = 0; i < pred.predicateCount(); i++) {
            IPredicate child = pred.predicateAt(i);
            if (tester.test(child)) {
                removeIndices.add(new Integer(i));
                continue;
            }
            if (child instanceof CompositePredicate) {
                removeTime((CompositePredicate) child, pred, tester);
            }
        }
        // remove predicates in reverse order to make sure we remove the right
        // ones
        for (int j = removeIndices.size() - 1; j >= 0; j--) {
            pred.removePredicate(((Integer) removeIndices.get(j)).intValue());
        }
        if (pred.predicateCount() == 1) {
            // unwrap single conditions
            parent.setConditions(pred.predicateAt(0));
        } else if (pred.predicateCount() == 0) {
            parent.setConditions(null);
        }
    }

    /**
     * Removes a predicate from a composite predicate. If the predicate was in a
     * composite predicate, this method will automatically unwrap single
     * predicates or remove empty predicates resulting from the removal. The
     * predicate to remove is identified by the TimeTester passed in.
     * 
     * @param pred
     * @param parent
     * @param tester
     */
    private static void removeTime(CompositePredicate pred, CompositePredicate parent, TimeTester tester) {
        List<Integer> removeIndices = new ArrayList<Integer>();
        for (int i = 0; i < pred.predicateCount(); i++) {
            IPredicate child = pred.predicateAt(i);
            if (tester.test(child)) {
                removeIndices.add(new Integer(i));
                continue;
            }
            if (child instanceof CompositePredicate) {
                removeTime((CompositePredicate) child, pred, tester);
            }
        }
        // remove predicates in reverse order to make sure we remove the right
        // ones
        for (int j = removeIndices.size() - 1; j >= 0; j--) {
            pred.removePredicate(((Integer) removeIndices.get(j)).intValue());
        }
        if (pred.predicateCount() == 1) {
            // unwrap single conditions
            parent.removePredicate(pred);
            parent.addPredicate(pred.predicateAt(0));
        } else if (pred.predicateCount() == 0) {
            parent.removePredicate(pred);
        }
    }

    /**
     * Removes the start date condition from the policy
     * 
     * @param policy
     */
    public static void removeStartTime(IDPolicy policy) {
        removeTime(policy, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isStartTime(condition);
            }
        });

    }

    /**
     * Remove the end date condition from the policy
     * 
     * @param policy
     */
    public static void removeEndTime(IDPolicy policy) {
        removeTime(policy, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isEndTime(condition);
            }
        });
    }

    /**
     * Remove the beginning time for daily enforcement from the policy
     * 
     * @param policy
     */
    public static void removeDailyFromTime(IDPolicy policy) {
        removeTime(policy, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isDailyFromTime(condition);
            }
        });
    }

    /**
     * Remove the end time for daily enforcement from the policy
     * 
     * @param policy
     */
    public static void removeDailyToTime(IDPolicy policy) {
        removeTime(policy, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isDailyToTime(condition);
            }
        });
    }

    /**
     * Remove the predicate representing the enforcement days of the week from
     * the policy
     * 
     * @param policy
     */
    public static void removeWeekdayPredicate(IDPolicy policy) {
        removeTime(policy, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isWeekDaysPredicate(condition);
            }
        });
    }

    public static void removeDayOfMonthPredicate(IDPolicy policy) {
        removeTime(policy, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isDayOfMonthRelation(condition);
            }
        });
    }

    public static void removeDOWIMPredicate(IDPolicy policy) {
        removeTime(policy, new TimeTester() {

            public boolean test(IPredicate condition) {
                return isDOWIMRelation(condition);
            }
        });
    }

    // ------------------------
    // Reference Creation Helpers
    // ------------------------

    /**
     * Constructs a reference to a policy component suitable for use in a policy
     * or component definition.
     */
    public static IPredicate getComponentReference(String name, SpecType specType) {
        IDSpecRef classRef = (IDSpecRef) sm.getSpecReference(name);
        return classRef;
    }

    public static IPredicate getResourceReference(String name) {
        return ResourceAttribute.NAME.buildRelation(RelationOp.EQUALS, name);
    }

    public static IPredicate getLeafReference(LeafObject leaf) {
        if (leaf.getType() == LeafObjectType.USER) {
            return SubjectAttribute.USER_ID.buildRelation(RelationOp.EQUALS, "" + leaf.getId());
        }
        if (leaf.getType() == LeafObjectType.CONTACT) {
            return SubjectAttribute.CONTACT_ID.buildRelation(RelationOp.EQUALS, "" + leaf.getId());
        }
        if (leaf.getType() == LeafObjectType.USER_GROUP) {
            Relation rel = new Relation(RelationOp.EQUALS, SubjectAttribute.USER_LDAP_GROUP_ID, SubjectAttribute.USER_LDAP_GROUP_ID.build("" + leaf.getId()));
            return rel;
        }
        if (leaf.getType() == LeafObjectType.APPUSER) {
            return SubjectAttribute.APPUSER_ID.buildRelation(RelationOp.EQUALS, "" + leaf.getId());
        }
        if (leaf.getType() == LeafObjectType.ACCESSGROUP) {
            Relation rel = new Relation(RelationOp.EQUALS, SubjectAttribute.APPUSER_ACCESSGROUP_ID, SubjectAttribute.APPUSER_ACCESSGROUP_ID.build("" + leaf.getId()));
            return rel;
        }
        if (leaf.getType() == LeafObjectType.HOST) {
            Relation rel = new Relation(RelationOp.EQUALS, SubjectAttribute.HOST_ID, SubjectAttribute.HOST_ID.build("" + leaf.getId()));
            return rel;
        }
        if (leaf.getType() == LeafObjectType.HOST_GROUP) {
            Relation rel = new Relation(RelationOp.EQUALS, SubjectAttribute.HOST_LDAP_GROUP_ID, SubjectAttribute.HOST_LDAP_GROUP_ID.build("" + leaf.getId()));
            return rel;
        }
        if (leaf.getType() == LeafObjectType.APPLICATION) {
            Relation rel = new Relation(RelationOp.EQUALS, SubjectAttribute.APP_ID, SubjectAttribute.APP_ID.build("" + leaf.getId()));
            return rel;
        }
        return null;
    }

    /**
     * Converts an EntityType to a SpecType.
     * 
     * @param type
     * @return a SpecType for the given EntityType, or null if the type cannot
     *         be converted.
     */
    public static SpecType getSpecType(EntityType type) {
        if (type == EntityType.ACTION) {
            return SpecType.ACTION;
        }
        if (type == EntityType.APPLICATION) {
            return SpecType.APPLICATION;
        }
        if (type == EntityType.HOST) {
            return SpecType.HOST;
        }
        if (type == EntityType.RESOURCE) {
            return SpecType.RESOURCE;
        }
        if (type == EntityType.USER) {
            return SpecType.USER;
        }
        return null;
    }

    /**
     * 
     * Returns the type of the specified predicate
     * 
     * @param predicate
     * @return the type of the predicate
     */
    public static SpecType getPredicateType(IPredicate predicate) {
        // FIXME (sergey) This requires some hardcodinf
        if (predicate instanceof SpecReference) {
            SpecReference ref = (SpecReference) predicate;
            if (ref.isReferenceByName()) {
                String name = ref.getReferencedName();
                int pos = name.indexOf(PQLParser.SEPARATOR);
                if (pos != -1) {
                    try {
                        return SpecType.forName(name.substring(0, pos).toLowerCase());
                    } catch (Exception ignored) {
                        // FIXME (sergey) This should not happen
                    }
                }
            }
        } else if (predicate instanceof IRelation) {
            IExpression exp = ((IRelation) predicate).getLHS();
            return getExpressionType(exp);
        }
        return null;
    }

    public static SpecType getExpressionType(IExpression exp) {
        SpecType specType = null;
        if (exp instanceof SpecAttribute) {
            SpecAttribute sa = (SpecAttribute) exp;
            specType = sa.getSpecType();
            if (specType == SpecType.RESOURCE) {
                // Resources have subtypes
                String subtype = sa.getObjectSubTypeName();
                if (ResourceAttribute.PORTAL_SUBTYPE.equals(subtype)) {
                    specType = SpecType.PORTAL;
                }
            }
        }
        return specType;
    }
}
