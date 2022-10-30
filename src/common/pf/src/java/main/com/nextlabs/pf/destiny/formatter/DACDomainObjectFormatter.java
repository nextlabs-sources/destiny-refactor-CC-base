package com.nextlabs.pf.destiny.formatter;

/*
 * Created on Aug 15, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/nextlabs/pf/destiny/formatter/DACDomainObjectFormatter.java#1 $:
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IExpressionReference;
import com.bluejungle.framework.expressions.IExpressionVisitor;
import com.bluejungle.framework.expressions.IFunctionApplication;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.expressions.Predicates.DefaultTransformer;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.expressions.ValueType;
import com.bluejungle.framework.utils.StringUtils;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.obligation.LogObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;

/**
 * Formats policies as Microsoft DAC CARs (Central Access Rules)
 */
public class DACDomainObjectFormatter {
    // SDDL doesn't appear to have a good way to represent literal boolean values
    public static final String SDDL_TRUE = "1==1";
    public static final String SDDL_FALSE = "1==0";

    private final Map<String, String> acplADMappings;

    // A list of mappings from Nextlabs actions to SDDL permissions, sorted by length
    private final List<ActionPair> actionPairs;

    private DACCentralAccessRule car;

    public DACDomainObjectFormatter(Map<String, String> acplADMappings, Map<String, String> actionMappings) {
        this.acplADMappings = acplADMappings;

        actionPairs = new ArrayList<ActionPair>();

        for (Map.Entry<String, String> entry : actionMappings.entrySet()) {
            actionPairs.add(new ActionPair(entry.getKey(), entry.getValue()));
        }

        Collections.sort(actionPairs);
    }

    public DACCentralAccessRule getCAR() {
        return car;
    }

    public void formatPolicyMetadata(IDPolicy policy) {
        // Initialize some basics
        String[] policyPath = policy.getName().split("/");
        car = new DACCentralAccessRule();
        car.setName(policyPath[policyPath.length-1]);
        car.setDescription(policy.getDescription());
    }

    public void formatPolicy(IDPolicy policy) {
        formatPolicyMetadata(policy);

        if (policy.getMainEffect() != EffectType.ALLOW) {
            throw new IllegalArgumentException("Policy main effect was not Allow");
        }

        car.setRuleType(determineRuleType(policy));

        String action = formatAction(policy.getTarget().getActionPred());
        car.setAction(action);
        
        String resource = formatMappedPredicate(policy.getTarget().getFromResourcePred(), acplADMappings);
        car.setResourceCondition(resource);
        
        IPredicate toResource = policy.getTarget().getToResourcePred();
        if (toResource != null && toResource != PredicateConstants.TRUE) {
            throw new IllegalArgumentException("DAC policy " + policy.getName() + " with \"to\" resource: " + policy.getTarget());
        }

        IPredicate subjectAndCondition = policy.getTarget().getSubjectPred();

        if (policy.getConditions() != null && policy.getConditions() != PredicateConstants.TRUE) {
            subjectAndCondition = new CompositePredicate(BooleanOp.AND, subjectAndCondition, policy.getConditions());
        }

        formatSubject(subjectAndCondition, acplADMappings, car);
    }

    private DACCentralAccessRule.DACRuleType determineRuleType(IDPolicy policy) {
        IObligation[] obligations = policy.getObligationArray(EffectType.ALLOW);

        for (IObligation obligation : obligations) {
            if (obligation.getType().equals(LogObligation.OBLIGATION_NAME)) {
                return DACCentralAccessRule.DACRuleType.PROPOSED;
            }
        }

        return DACCentralAccessRule.DACRuleType.ACCESS;
    }

    private String formatAction(IPredicate actionPred) {
        final Collection<String> actions = new ArrayList<String>();
        
        // Get a list of all the actions
        actionPred.accept(new DefaultPredicateVisitor() {
            @Override
            public void visit(IPredicateReference ref) {
                throw new IllegalArgumentException("Can not format DAC policies with references: " + ref);
            }

            @Override
            public void visit(IPredicate pred) {
                actions.add(pred.toString());
            }
        }, IPredicateVisitor.PREORDER);

        return nextlabsActionsToDACAction(actions);
    }

    /**
     * Find the best match for the given set of ACPL actions and return the equivalent DAC action
     *
     * @param actions a collection of ACPL actions
     * @return the DAC action
     */
    private String nextlabsActionsToDACAction(Collection<String> actions) {
        for (ActionPair actionPair : actionPairs) {
            if (actionPair.matches(actions)) {
                return actionPair.getSddlAction();
            }
        }

        throw new IllegalArgumentException("Unable find DAC equivalent of " + StringUtils.join(actions, ", ") + " action");
    }


    private static boolean isUserExpr(IExpression expr) {
        return (expr instanceof SubjectAttribute &&
                ((SubjectAttribute)expr).getName().equals("uid"));
    }
    
    private static boolean isStringExpression(IExpression expr) {
        return (expr instanceof Constant &&
                ((Constant)expr).getValue().getType() == ValueType.STRING);
    }

    private static String getStringFromExpression(IExpression expr) {
        if (isStringExpression(expr)) {
            return ((String)((Constant)expr).getValue().getValue());
        }

        throw new IllegalArgumentException("Expression " + expr + " was not a string");
    }

    private class UserTransformer extends DefaultTransformer {
        private String keepSid = null;
        
        public UserTransformer(String keepSid) {
            this.keepSid = keepSid;
        }
        
        @Override
        public IPredicate transformRelation(IRelation rel) {
            // Any user expressions for someone else should be eliminated
            if (isUserExpr(rel.getLHS())) {
                return keepThisExpression(rel.getRHS()) ? PredicateConstants.TRUE : PredicateConstants.FALSE;
            } else if (isUserExpr(rel.getRHS())) {
                return keepThisExpression(rel.getLHS()) ? PredicateConstants.TRUE : PredicateConstants.FALSE;
            }
            
            return rel;
            
        }
        
        private boolean keepThisExpression(IExpression expr) {
            return (isStringExpression(expr) &&
                    getStringFromExpression(expr).equals(keepSid));
        }
    }

    private void formatSubject(IPredicate subjectAndCondition, Map<String, String> acplADMappings, DACCentralAccessRule car) {
        final Set<String> uids = new HashSet<String>();

        subjectAndCondition.accept(new DefaultPredicateVisitor() {
            @Override
            public void visit(IRelation rel) {
                if (isUserExpr(rel.getLHS())) {
                    uids.add(getStringFromExpression(rel.getRHS()));
                } else if (isUserExpr(rel.getRHS())) {
                    uids.add(getStringFromExpression(rel.getLHS()));
                }
            }
        }, IPredicateVisitor.PREPOSTORDER);

        for (String uid : uids) {
            IPredicate transformedSubjectAndCondition = Predicates.transform(subjectAndCondition,
                                                                             new UserTransformer(uid));

            String claim = formatMappedPredicate(transformedSubjectAndCondition, acplADMappings);

            car.setUserClaims(uid, claim);
        }

        // Finally, do it for everyone
        IPredicate transformedSubjectAndCondition = Predicates.transform(subjectAndCondition,
                                                                         new UserTransformer(""));

        if (transformedSubjectAndCondition != PredicateConstants.FALSE) {
            String claim = formatMappedPredicate(transformedSubjectAndCondition, acplADMappings);
            car.setUserClaims(claim);
        }
    }

    private String formatMappedPredicate(IPredicate pred, final Map<String, String> mappings) {
        final StringBuilder sb = new StringBuilder();

        pred.accept(new IPredicateVisitor() {
            private final Stack<String> opStack = new Stack<String>();
            private final Stack<Boolean> separatorStack = new Stack<Boolean>();

            public void visit(IPredicateReference ref) {
                throw new IllegalArgumentException("Can not format DAC policies with references: " + ref);
            }
            
            public void visit(IPredicate pred) {
                if (pred == PredicateConstants.TRUE) {
                    sb.append(SDDL_TRUE);
                } else if (pred == PredicateConstants.FALSE) {
                    sb.append(SDDL_FALSE);
                } else {
                    throw new IllegalArgumentException("Can not format unknown predicates in DAC policies: " + pred);
                }
            }

            public void visit(ICompositePredicate cpred, boolean preorder) {
                visit(cpred, booleanOpToDACOp(cpred.getOp()), preorder);
            }

            public void visit(ICompositePredicate cpred, String opStr, boolean preorder) {
                if (preorder) {
                    addInfixOps();

                    if (cpred.predicateCount() == 1) {
                        sb.append(opStr);
                        sb.append(" (");
                    } else {
                        sb.append("(");
                    }

                    opStack.push(opStr);
                    separatorStack.push(true);
                } else {
                    sb.append(")");
                    opStack.pop();
                    separatorStack.pop();
                }
            }

            public void visit(IRelation rel) {
                addInfixOps();
                sb.append(formatSingleExpression(rel.getLHS(), mappings));
                sb.append(" ");
                sb.append(relationOpToDACOp(rel.getOp()));
                sb.append(" ");
                sb.append(formatSingleExpression(rel.getRHS(), mappings));
            }

            private void addInfixOps() {
                if ( separatorStack.isEmpty() ) {
                    return;
                }
                if (!separatorStack.peek()) {
                    // The two stacks grow and shrink at the same time:
                    assert !opStack.empty();
                    sb.append(" ");
                    sb.append(opStack.peek());
                    sb.append(" ");
                } else {
                    separatorStack.pop();
                    separatorStack.push(false);
                }
            }
        }, IPredicateVisitor.PREPOSTORDER);

        return sb.toString();
    }

    private static final Map<BooleanOp, String> booleanOpMap = new HashMap<BooleanOp, String>();

    static {
        booleanOpMap.put(BooleanOp.AND, "&&");
        booleanOpMap.put(BooleanOp.OR, "||");
        booleanOpMap.put(BooleanOp.NOT, "!");
    }

    private static String booleanOpToDACOp(BooleanOp op) {
        String dacOp = booleanOpMap.get(op);

        if (dacOp == null) {
            throw new IllegalArgumentException("Operator " + op + " can not be expressed in DAC rule");
        }

        return dacOp;
    }

    private static final Map<RelationOp, String> relationOpMap = new HashMap<RelationOp, String>();

    static {
        relationOpMap.put(RelationOp.EQUALS, "==");
        relationOpMap.put(RelationOp.NOT_EQUALS, "!=");
        relationOpMap.put(RelationOp.GREATER_THAN,">");
        relationOpMap.put(RelationOp.LESS_THAN, "<");
        relationOpMap.put(RelationOp.GREATER_THAN_EQUALS, ">=");
        relationOpMap.put(RelationOp.LESS_THAN_EQUALS, "<=");
        relationOpMap.put(RelationOp.INCLUDES, "Contains");
    }

    private static String relationOpToDACOp(RelationOp op) {
        String dacOp = relationOpMap.get(op);

        if (dacOp == null) {
            throw new IllegalArgumentException("Operator " + op + " can not be expressed in DAC rule");
        }

        return dacOp;
    }

    private String formatSingleExpression(final IExpression expr, final Map<String, String> mappings) {
        final StringBuilder sb = new StringBuilder();

        if (expr == null) {
            return "";
        }

        expr.acceptVisitor(new IExpressionVisitor() {
            public void visit(IAttribute attr) {
                String attributeName = getAttributeName(attr);
                String dacAttribute = mappings.get(attributeName);

                if (dacAttribute == null) {
                    throw new IllegalArgumentException("No mapping for attribute " + attributeName);
                }
                sb.append(dacAttribute);
            }

            public void visit(Constant constant) {
                if (constant.getValue().getType() == ValueType.STRING) {
                    sb.append(quote(constant.getRepresentation()));
                } else {
                    sb.append(constant.getRepresentation());
                }
            }

            public void visit(IFunctionApplication func) {
                throw new IllegalArgumentException("Function application not supported in DAC policies: " + func);
            }

            public void visit(IExpression e) {
                throw new IllegalArgumentException("Unexpected expression: " + e + " in expression " + expr);
            }

            public void visit(IExpressionReference ref) {
                throw new IllegalArgumentException("References not supported in DAC policies: " + ref);
            }

        }, IExpressionVisitor.PREORDER);

        return sb.toString();
    }

    /**
     * Return dotted name of attr (e.g. resource.fso.name)
     * @param attr the attribute
     * @return the string representation of the name
     */
    private String getAttributeName(IAttribute attr) {
        String typeName = attr.getObjectTypeName();
        String subTypeName = attr.getObjectSubTypeName();

        String res = typeName;
        if (subTypeName != null && !subTypeName.equals(typeName)) {
            res += ".";
            res += subTypeName;
        }

        res+=".";
        res+= attr.getName();

        return res.replace("\"", "");
    }

    private static String quote(String s) {
        return "\"" + s + "\"";
    }

    private static class ActionPair implements Comparable<ActionPair> {
        private List<String> nextlabsActions;
        private String sddlAction;

        public ActionPair(String nextlabsActions, String sddlAction) {
            if (nextlabsActions == null) {
                throw new NullPointerException("nextlabsActions can not be null");
            }

            if (sddlAction == null) {
                throw new NullPointerException("sddlAction can not be null");
            }

            String[] actions = nextlabsActions.split(", *");
            this.nextlabsActions = Arrays.asList(actions);

            this.sddlAction = sddlAction;
        }

        /**
         * Determine if a list of actions is a good match for this ActionPair. In order
         * to be a good match, actions must contain every member of nextlabsActions (the
         * reverse is not true)
         */
        public boolean matches(Collection<String> actions) {
            Set<String> incoming = new HashSet(actions);

            for (String nextlabAction : nextlabsActions) {
                if (!incoming.contains(nextlabAction)) {
                    return false;
                }
            }

            return true;
        }

        public List<String> getNextlabsActions() {
            return nextlabsActions;
        }

        public String getSddlAction() {
            return sddlAction;
        }

        // Sort in descending order of number of actions
        public int compareTo(ActionPair o) {
            return o.getNextlabsActions().size() - nextlabsActions.size();
        }
    }
}
