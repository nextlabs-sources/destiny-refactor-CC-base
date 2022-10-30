package com.nextlabs.pf.destiny.formatter;

/*
 * Created on Jan 26, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/nextlabs/pf/destiny/formatter/XACMLDomainObjectFormatter.java#1 $:
 */

import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.Constant;
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
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.UnmodifiableDate;
import com.bluejungle.framework.utils.StringUtils;
import com.bluejungle.pf.destiny.lifecycle.EntityType;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.domain.destiny.common.IDSpec;
import com.bluejungle.pf.domain.destiny.common.IDSpecRef;
import com.bluejungle.pf.domain.destiny.environment.HeartbeatAttribute;
import com.bluejungle.pf.domain.destiny.environment.RemoteAccessAttribute;
import com.bluejungle.pf.domain.destiny.environment.TimeAttribute;
import com.bluejungle.pf.domain.destiny.exceptions.CombiningAlgorithm;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.obligation.CustomObligation;
import com.bluejungle.pf.domain.destiny.obligation.DisplayObligation;
import com.bluejungle.pf.domain.destiny.obligation.LogObligation;
import com.bluejungle.pf.domain.destiny.obligation.NotifyObligation;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.epicenter.exceptions.ICombiningAlgorithm;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyExceptions;
import com.bluejungle.pf.domain.epicenter.exceptions.IPolicyReference;
import com.bluejungle.pf.domain.epicenter.misc.IEffectType;
import com.bluejungle.pf.domain.epicenter.misc.IObligation;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;
import com.nextlabs.pf.destiny.formatter.RelationTypeIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Formats (some) domain objects as XACML.  See DomainObjectFormatter for the
 * traditional ACPL implementation.
 *
 * Converting from ACPL to XACML is not easy. Although the two formats
 * have a lot of concepts in common, they differ in the way they
 * express them. For many, perhaps most, policies we have to make a
 * choice between producing syntactically legal XACML and semantically
 * correct XACML.  We should always choose the former.
 *
 * === Targets and Rules
 *
 * Both ACPL and XAML have the concept of Targets and Rules, although
 * ACPL doesn't refer to them in that way. A Target is some predicate
 * that determines if the policy applies. A Rule is a condition that
 * specifies what result to return. Targets in ACPL are the Subject
 * (User, Host, Application), Action, and Resource predicates. The
 * Rule is the condition.
 *
 * That's fine, so far as it goes, but XACML has a restricted syntax
 * for Targets. A Rule can express any combination of boolean
 * operators. A Target must be ORs of ANDs. Nothing else is
 * acceptable. The ANDs can be single items, so (A OR B OR C) is a
 * perfectly valid Target, as is (A AND B) OR (C AND C) OR E, but (A
 * AND (B or (C AND D))) is not. ACPL has no such restrictions. This
 * is a problem if the ACPL has a Target that can not be expressed as
 * a XACML Target.
 *
 * To resolve this we determine what can legally go into the Target.
 * That will go and everything else will go into the Rule. This is
 * semantically incorrect, but syntactically legal, and it's the best
 * we can do.
 *
 * Predicates that can legally go into a Target are called "Flat
 * Predicates" in this code.
 *
 * === Types
 * 
 * XACML is fairly strongly typed. ACPL is not. ACPL compares both
 * strings and integers for equality using = (in fact, you can
 * actually compare strings to integers using this operator). ACPl has
 * different operators for this. We hard-code some knowledge about
 * which attributes typically have which types in
 * RelationTypeIdentifier and then try to do our best to guess what
 * operators should be used.
 * 
 * === Deny and Allow Only Policies
 *
 * XACML policies either ALLOW or DENY, but they don't do both. ACPL
 * is different. We have main effects and otherwise effects and the
 * otherwise effect will apply if the Target (i.e. Subject, etc)
 * matches but the Rule (i.e. Condition) does not. In other words,
 * Deny policies can Allow and Allow Only policies can Deny.
 *
 * To resolve this quirk, we convert Deny and Allow Only policies into
 * two policies - one of which handles the deny case and one of which
 * handles the allow case.
 *
 * === Policy Exceptions/Policy Sets
 *
 * ACPL has policy exceptions (sub-policies) and XACML has policy
 * sets. These are closely related, but there are differences. The
 * major one is that XACML policy sets are *not* policies (whereas
 * ACPL has policies. Some with exceptions and some that are
 * exceptions to other policies). XACML policies have both a Target
 * (do I apply) and a Rule (what do I do), whereas policy sets have
 * just a Target. This brings us back to the Target/Rule problem
 * mentioned earlier, with the additional wrinkle of not being able to
 * put excess stuff in the Rule, because there is no Rule available.
 *
 * One thought was to "push down" the excess stuff into the lower
 * policies (if you have stuff that would normally go into a Rule and
 * you are a Policy Set, add it to all your sub-policy's rules. Of
 * course, some of them may be Policy Sets as well and have their own
 * stuff to push down). This would likely produce huge and
 * hard-to-maintain XACML.
 * 
 * The approach we have taken is simple - anything that we can't put
 * into the Target will go into a comment. We don't expect that
 * NextLabs produced XACML will work as is (it's more of a template or
 * guideline), so asking the consumer of the XACML to do some editing
 * is not unreasonable.
 */

public class XACMLDomainObjectFormatter {
    private static final String URN_1_0 = "urn:oasis:names:tc:xacml:1.0:";
    private static final String URN_3_0 = "urn:oasis:names:tc:xacml:3.0:";
    

    private StringBuilder out = new StringBuilder();

    private final FlatPredicateDetector flatDetector = new FlatPredicateDetector();

    private final IPredicateVisitor actionVisitor = new FlatPredicateVisitor(actionPredicateFormatter);

    /*
     * Not all predicates are formatted the same way. To and from resources have slightly different category
     * information. The target and condition have different "and" and "or" signifiers.
     */
    enum EntityContext {
        ALTERNATE_PREDICATE,   // destination resource and send-to subject
        CONDITION_PREDICATE,
        AND_PREDICATE,
        OR_PREDICATE
    }


    private static final EnumSet<EntityContext> DEFAULT_CONTEXT = EnumSet.noneOf(EntityContext.class);

    private final IPredicateVisitor genericVisitor =
        new PredicateVisitor(genericPredicateFormatter, EnumSet.of(EntityContext.CONDITION_PREDICATE)); 

    private final IPredicateVisitor altGenericVisitor =
        new PredicateVisitor(genericPredicateFormatter, EnumSet.of(EntityContext.ALTERNATE_PREDICATE)); 

    // This leaves everything alone, except it converts monstrosities like (TRUE AND (FALSE or TRUE)) to TRUE
    private final Predicates.DefaultTransformer defaultTrans = new Predicates.DefaultTransformer();

    public final static String NEXTLABS_URN = "urn:nextlabs:";

    /**
     * Default constructor
     */
    public XACMLDomainObjectFormatter() {
    }

    public String getXACML() {
        String res = out.toString();
        out.setLength(0);
        return res;
    }

    public void formatDef(IHasId obj) {
        formatDef(Collections.singletonList(obj));
    }

    private final Map<String, IDSpec> specs = new HashMap<String, IDSpec>();
    private final Map<String, IDPolicy> policyExceptions = new HashMap<String, IDPolicy>();

    public void formatDef(Collection<? extends IHasId> objects) {
        List<IDPolicy> policies = new ArrayList<IDPolicy>();

        // First, we need the components that will be used by the policies
        for (IHasId id : objects) {
            if (id instanceof IDSpec) {
                IDSpec spec = (IDSpec)id;
                specs.put(spec.getName(), spec);
            } else if (id instanceof IDPolicy) {
                IDPolicy p = (IDPolicy)id;

                if (isException(p)) {
                    policyExceptions.put(p.getName(), p);
                } else {
                    policies.add(p);
                }
            } else {
                // ??
            }
        }
        iput("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        iput("<PolicySet xmlns=\"", URN_1_0, "policy\"\n");
        iput("           PolicySetId=\"Exported NextLabs Policies\"\n");
        iput("           PolicyCombiningAlgId=\"", URN_1_0, "policy-combining-algorithm:deny-overrides\">\n");
        inc();

        for (IDPolicy policy : policies) {
            formatPolicy(policy);
        }
        dec();
        iput("<PolicySet>\n");

    }

    private void formatPolicy(IDPolicy policy) {
        formatPolicyDef ( new DomainObjectDescriptor( policy.getId(),
                                                      policy.getName(),
                                                      policy.getOwner() == null ? null : policy.getOwner().getId(),
                                                      policy.getAccessPolicy(),
                                                      EntityType.POLICY,
                                                      policy.getDescription(),
                                                      policy.getStatus(),
                                                      -1,
                                                      UnmodifiableDate.START_OF_TIME,
                                                      UnmodifiableDate.START_OF_TIME,
                                                      null,
                                                      null,
                                                      null,
                                                      null,
                                                      policy.isHidden(),
                                                      true,
                                                      false),
                          policy);
    }

    public void formatPolicyDef(DomainObjectDescriptor descr, IDPolicy p) {
        // Create "pseudo policies" to deal with deny/allow-only cases
        List<IDPolicy> helpers = PseudoPolicyHelper.createPseudoPolicies(p);

        if (helpers != null) {
            iput("<PolicySet xmlns=\"", URN_1_0, "policy\"\n");
            iput("           PolicySetId=\"", NEXTLABS_URN, descr.getName(), "\"\n");

            if (p.getMainEffect() == EffectType.ALLOW) {
                iput("           PolicyCombiningAlgId=\"", URN_1_0, "policy-combining-algorithm:permit-overrides\">\n");
            } else {
                iput("           PolicyCombiningAlgId=\"", URN_1_0, "policy-combining-algorithm:deny-overrides\">\n");
            }
            inc();
        }

        formatSinglePolicy(descr, p);

        if (helpers != null) {
            for (IDPolicy helper : helpers) {
                formatSinglePolicy(descr, helper);
            }
            dec();
            iput("</PolicySet>\n");
        }
    }

    private void formatSinglePolicy(DomainObjectDescriptor descr, IDPolicy p) {
        if (hasExceptions(p)) {
            formatPolicyWithExceptions(descr, p);
        } else {
            formatPolicyWithoutExceptions(descr, p);
        }
    }

    private void formatPolicyWithExceptions(DomainObjectDescriptor descr, IDPolicy p) {
        iput("<PolicySet xmlns=\"", URN_1_0, "policy\"\n");
        iput("           PolicySetId=\"", NEXTLABS_URN, descr.getName(), "\"\n");
        
        if (p.getPolicyExceptions().getCombiningAlgorithm() == CombiningAlgorithm.ALLOW_OVERRIDES) {
            iput("           PolicyCombiningAlgId=\"", URN_1_0, "policy-combining-algorithm:permit-overrides\">\n");
        } else {
            iput("           PolicyCombiningAlgId=\"", URN_1_0, "policy-combining-algorithm:deny-overrides\">\n");
        }

        inc();
        List<UnplacedPredicate> extraPredicates = formatPolicyBodyTarget(p);

        iput("<!--\n");
        formatPolicyBodyRule(descr.getDescription(), p, extraPredicates, CommentsFlag.SKIP_COMMENTS);
        iput("-->\n");

        formatPolicyBodyAdvice(p);
        
        for (IPolicyReference ref : p.getPolicyExceptions().getPolicies()) {
            IDPolicy subPolicy = policyExceptions.get(ref.getReferencedName());

            if (subPolicy == null) {
                iput("<!-- Unable to find information for sub-policy ", ref.getReferencedName(), " -->");
            } else {
                formatPolicy(subPolicy);
            }
        }

        dec();

        iput("</PolicySet>\n");
    }

    private void formatPolicyWithoutExceptions(DomainObjectDescriptor descr, IDPolicy p) {
        iput("<Policy\n");
        iput("    xmlns=\"", URN_3_0, "core:schema:wd-17\"\n");
        iput("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        iput("    xsi:schemaLocation=\"", URN_3_0, "core:schema:wd-17\n");
        iput("                        http://docs.oasis-open.org/xacml/3.0/xacml-core-v3-schema-wd-17.xsd\"\n");
        iput("    PolicyId=\"", NEXTLABS_URN, p.getName(), "\"\n");
        iput("    Version=\"1.0\"\n");
        iput("    RuleCombiningAlgId=\"identifier:rule-combining-algorithm:deny-overrides\">\n");
        
        inc();
        formatPolicyBody(descr.getDescription(), p);
        dec();
        
        iput("</Policy>\n");
    }

    private class UnplacedPredicate {
        String name;
        IPredicate predicate;

        public UnplacedPredicate(String name, IPredicate predicate) {
            this.name = name;
            this.predicate = predicate;
        }
    }

    /*
     * XACML policies are divided up into targets and rules
     * logic and expressions. Targets are much more limited.
     *
     */
    private List<UnplacedPredicate> formatPolicyBodyTarget(IDPolicy p) {
        ITarget t = p.getTarget();

        iput("<Target>\n");
        inc();

        IPredicate action = t.getActionPred();
        assert action != null;

        action = Predicates.transform(action, defaultTrans);

        if (action != PredicateConstants.TRUE) {
            iput("<!-- start ACTION -->\n");
            iput("<AnyOf>\n");
            inc();
            action.accept(actionVisitor, IPredicateVisitor.PREPOSTORDER);
            dec();
            iput("</AnyOf>\n");
            iput("<!-- end ACTION -->\n");
        }

        // Any predicates that would normally go into the target, but can't, go here
        List<UnplacedPredicate> extraPredicates = new ArrayList<UnplacedPredicate>();

        IPredicate from = t.getFromResourcePred();
        assert from != null;
        from = Predicates.transform(from, defaultTrans);

        // Attempt to put the from resource into the Target. If it won't fit then we'll add it to the Rule
        if (!formatFlatPredicate(from, "FROM RESOURCE")) {
            extraPredicates.add(new UnplacedPredicate("FROM RESOURCE", from));
        }
        

        IPredicate subj = t.getSubjectPred();
        assert subj != null;
        subj = Predicates.transform(subj, defaultTrans);

        // Attempt to put the subject into the Target. If it won't fit then we'll add it to the Rule
        if (!formatFlatPredicate(subj, "SUBJECT")) {
            extraPredicates.add(new UnplacedPredicate("SUBJECT", subj));
        }

        dec();
        iput("</Target>\n");

        return extraPredicates;
    }

    private void formatPolicyBodyRule(String description, IDPolicy p, List<UnplacedPredicate> extraPredicates) {
        formatPolicyBodyRule(description, p, extraPredicates, CommentsFlag.SHOW_COMMENTS);
    }


    enum CommentsFlag { SKIP_COMMENTS, SHOW_COMMENTS };

    private void formatPolicyBodyRule(String description, IDPolicy p, List<UnplacedPredicate> extraPredicates, CommentsFlag comments) {
        boolean showComments = (comments == CommentsFlag.SHOW_COMMENTS);

        iput("<Rule Effect=\"" + translateEffect(p.getMainEffect()) + "\" RuleId=\"" + StringUtils.escape(p.getName()) + "\">\n");

        if (description != null && description.length() != 0) {
            inc();
            iput("<Description>" + StringUtils.escape(description) + "</Description>\n");
            dec();
        }

        inc();
        iput("<Condition FunctionId=\"", URN_1_0, "function:and\">\n");
        inc();
        
        for (UnplacedPredicate ch : extraPredicates) {
            if (ch.predicate != PredicateConstants.TRUE) {
                if (showComments) iput("<!-- start ", ch.name, " -->\n");
                ch.predicate.accept(genericVisitor, IPredicateVisitor.PREPOSTORDER);
                if (showComments) iput("<!-- end ", ch.name, " -->\n");
            }
        }

        ITarget t = p.getTarget();

        IPredicate to = t.getToResourcePred();
        if (to != null) {
            to = Predicates.transform(to, defaultTrans);
            if (to != PredicateConstants.TRUE) {
                if (showComments) iput("<!-- start TO RESOURCE -->\n");
                to.accept(altGenericVisitor, IPredicateVisitor.PREPOSTORDER);
                if (showComments) iput("<!-- end TO RESOURCE-->\n");
            }
        }
        
        IPredicate ts = t.getToSubjectPred();
        if (ts != null) {
            ts = Predicates.transform(ts, defaultTrans);
            if (ts != PredicateConstants.TRUE) {
                if (showComments) iput("<!-- start TO SUBJECT -->\n");
                ts.accept(altGenericVisitor, IPredicateVisitor.PREPOSTORDER);
                if (showComments) iput("<!-- end TO SUBJECT -->\n");
            }
        }
        
        IPredicate conditions = p.getConditions();
        
        if (conditions != null) {
            conditions = Predicates.transform(conditions, defaultTrans);
            if (conditions != PredicateConstants.TRUE) {
                if (showComments) iput("<!-- start WHERE -->\n");
                conditions.accept(genericVisitor, IPredicateVisitor.PREPOSTORDER);
                if (showComments) iput("<!-- end WHERE -->\n");
            }
        }

        dec();
        iput("</Condition>\n");
        dec();
        iput("</Rule>\n");
    }

    private void formatPolicyBodyAdvice(IDPolicy p) {
        iput("<AssociatedAdvice>\n");
        inc();
        for (IEffectType effectType : EffectType.elements()) {
            formatObligations(effectType, p.getObligationArray(effectType));
        }
        dec();
        iput("</AssociatedAdvice>\n");
    }

    private void formatPolicyBody(String description, IDPolicy p) {
        List<UnplacedPredicate> extraPredicates = formatPolicyBodyTarget(p);

        formatPolicyBodyRule(description, p, extraPredicates);

        formatPolicyBodyAdvice(p);
    }

    private boolean formatFlatPredicate(IPredicate pred, String name) {
        flatDetector.reset();
        pred.accept(flatDetector, IPredicateVisitor.PREPOSTORDER);
        if (flatDetector.isFlat()) {
            if (pred != PredicateConstants.TRUE) {
                BooleanOp op = flatDetector.getOp();
                
                iput("<!-- start ", name, " -->\n");
                iput("<AnyOf>\n");
                inc();
                
                if (op == BooleanOp.AND) {
                    iput("<AllOf>\n");
                    inc();
                }
                pred.accept(new FlatPredicateVisitor(genericPredicateFormatter, createOpContext(flatDetector.getOp())), IPredicateVisitor.PREPOSTORDER);
                
                if (op == BooleanOp.AND) {
                    dec();
                    iput("</AllOf>\n");
                }
                
                dec();
                iput("</AnyOf>\n");
                iput("<!-- end ", name, " -->\n");
            }
            
        }
        return flatDetector.isFlat();
    }
        
    private void formatObligations(IEffectType effectType, IObligation[] obligations) {
        if (obligations == null || obligations.length == 0) {
            return;
        }

        for (IObligation obligation : obligations) {
            if (obligation instanceof CustomObligation) {
                CustomObligation custob = (CustomObligation)obligation;
                
                formatObligationExpressionHead(custob.getCustomObligationName(), effectType);

                int i = 0;
                inc();
                for (Object o : custob.getCustomObligationArgs()) {
                    formatObligationAttributeAssignment("arg" + (++i), 
                                                        RelationTypeIdentifier.XACML_STRING_TYPE,
                                                        o.toString());
                }
                dec();
                formatObligationExpressionTail();
            } else if (obligation instanceof DisplayObligation) {
                DisplayObligation dobl = (DisplayObligation)obligation;

                formatObligationExpressionHead(dobl.getType(), effectType);
                inc();
                formatObligationAttributeAssignment("message",
                                                    RelationTypeIdentifier.XACML_STRING_TYPE,
                                                    StringUtils.escape(dobl.getMessage()));
                dec();
                formatObligationExpressionTail();
            } else if (obligation instanceof LogObligation) {
                formatObligationExpressionHead(((LogObligation)obligation).getType(), effectType);
                formatObligationExpressionTail();
                continue;
            } else if (obligation instanceof NotifyObligation) {    
                NotifyObligation nobl = (NotifyObligation)obligation;

                formatObligationExpressionHead(nobl.getType(), effectType);
                inc();
                formatObligationAttributeAssignment("recipients",
                                                    RelationTypeIdentifier.XACML_STRING_TYPE,
                                                    nobl.getEmailAddresses());
                formatObligationAttributeAssignment("message",
                                                    RelationTypeIdentifier.XACML_STRING_TYPE,
                                                    StringUtils.escape(nobl.getBody()));
                dec();
                formatObligationExpressionTail();
            } else { 
                formatObligationExpressionHead(obligation.getType(), effectType);
                formatObligationExpressionTail();
            }
        }
    }

    private static void formatAttribute(final StringBuilder out, int depth, RelationTypeIdentifier.IRelationTypeInfo relTypeInfo, IAttribute attr, EnumSet<EntityContext> ec) {
        iputb(out, depth, "<AttributeDesignator MustBePresent=\"true\"\n");
        iputb(out, depth, "                     Category=\"" + getAttributeCategory(attr, ec) + "\"\n");
        iputb(out, depth, "                     AttributeId=\"" + getAttributeName(attr) + "\"\n");
        iputb(out, depth, "                     DataType=\"" + relTypeInfo.getDataType() + "\"/>\n");
    }

    private static void formatConstant(final StringBuilder out, int depth, RelationTypeIdentifier.IRelationTypeInfo relTypeInfo, String constantValue) {
        // Strip quotes from front and back. This is necessary (?) because some constants (like date) are wrapped in quotes. This is
        // probably not what we want for strings with quotes, but those are rare.
        
        // TODO - Take an IEvalValue instead and try to figure out whether we should strip quotes or not based on type
        if (constantValue.startsWith("\"") && constantValue.endsWith("\"")) {
            constantValue = constantValue.substring(1, constantValue.length()-1);
        }
        iputb(out, depth, "<AttributeValue DataType=\"", relTypeInfo.getDataType(), "\">", constantValue, "</AttributeValue>\n");
    }

    private static void formatFunction(final StringBuilder out, int depth, RelationTypeIdentifier.IRelationTypeInfo relTypeInfo, IFunctionApplication func, EnumSet<EntityContext> ec) {
        iputb(out, depth, "<Apply FunctionId=\"", NEXTLABS_URN, "external-function:", func.getServiceName(), ":", func.getFunctionName(), "\">\n");

        // Format all the pieces
        for (IExpression expr : func.getArguments()) {
            // Build a fake relation so that we can deduce the type of this expression
            IRelation fakerel = new Relation(RelationOp.EQUALS, expr, expr);
            formatSingleExpression(out, depth+1, RelationTypeIdentifier.getMatchInformation(fakerel), expr, ec);
        }

        iputb(out, depth, "</Apply>\n");
    }

    private static void formatApplyRelation(final StringBuilder out, int depth, final IRelation rel, EnumSet<EntityContext> ec) {
        RelationTypeIdentifier.IRelationTypeInfo relTypeInfo = RelationTypeIdentifier.getMatchInformation(rel);

        if (relTypeInfo.applyNot()) {
            iputb(out, depth, "<Apply FunctionId=\"", RelationTypeIdentifier.NOT_FUNCTION_ID, "\">\n");
            depth++;
        }
        
        iputb(out, depth, "<Apply FunctionId=\"", relTypeInfo.getMatchFunction(), "\">\n");
        
        formatSingleExpression(out, depth+1, relTypeInfo, rel.getLHS(), ec);
        formatSingleExpression(out, depth+1, relTypeInfo, rel.getRHS(), ec);

        iputb(out, depth, "</Apply>\n");
        
        if (relTypeInfo.applyNot()) {
            depth--;
            iputb(out, depth, "</Apply>\n");
        }
    }

    private static void formatMatchRelation(final StringBuilder out, int depth, final IRelation rel, EnumSet<EntityContext> ec) {
        RelationTypeIdentifier.IRelationTypeInfo relTypeInfo = RelationTypeIdentifier.getMatchInformation(rel);

        if (ec.contains(EntityContext.OR_PREDICATE)) {
            iputb(out, depth++, "<AllOf>\n");
        }

        iputb(out, depth, "<Match MatchId=\"", relTypeInfo.getMatchFunction(), "\">\n");
        
        formatSingleExpression(out, depth+1, relTypeInfo, rel.getLHS(), ec);
        formatSingleExpression(out, depth+1, relTypeInfo, rel.getRHS(), ec);

        iputb(out, depth, "</Match>\n");

        if (ec.contains(EntityContext.OR_PREDICATE)) {
            iputb(out, --depth, "</AllOf>\n");
        }
    }

    private static void formatSingleExpression(final StringBuilder out, final int depth, final RelationTypeIdentifier.IRelationTypeInfo relTypeInfo, IExpression expr, final EnumSet<EntityContext> ec) {

        if (expr == null) {
            formatConstant(out, depth, relTypeInfo, "UNKNOWN");
            return;
        }

        IExpressionVisitor ev = new IExpressionVisitor() {
            public void visit(IAttribute attr) {
                formatAttribute(out, depth, relTypeInfo, attr, ec);
            }

            public void visit(Constant constant) {
                formatConstant(out, depth, relTypeInfo, constant.getRepresentation());
            }

            public void visit(IFunctionApplication func) {
                formatFunction(out, depth, relTypeInfo, func, ec);
            }

            public void visit(IExpression expression) {
                formatConstant(out, depth, relTypeInfo, "UNEXPECTED EXPRESSION");
            }

            public void visit(IExpressionReference ref) {
                formatConstant(out, depth, relTypeInfo, "EXPRESSION REFERENCE");
            }

        };
        expr.acceptVisitor(ev, IExpressionVisitor.PREORDER);
    }

    private static final IPredicateFormatter genericPredicateFormatter = new IPredicateFormatter() {
        public void formatPredicate(final StringBuilder out, int depth, IPredicate pred) {
            if (pred == PredicateConstants.TRUE) {
                iputb(out, depth, "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#boolean\">true</AttributeValue>\n");
            } else if (pred == PredicateConstants.FALSE) {
                iputb(out, depth, "<AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#boolean\">false</AttributeValue>\n");
            } else {
                // Not much to do here - we're accepting something
                // we don't know, so using its toString looks like
                // our best bet. Subclasses may have a better idea, so they
                // can provide more meaningful printing here.
                iputb(out, depth, "<!-- UNCLASSIFIABLE PREDICATE: ", pred, " -->\n");
            }
        }
    };

    /*
     * All actions in policies are joined by OR. We make use of this fact when formatting them
     */
    private static final IPredicateFormatter actionPredicateFormatter = new IPredicateFormatter() {
        public void formatPredicate(final StringBuilder out, int depth, IPredicate action) {
            iputb(out, depth, "<AllOf>\n");
            
            iputb(out, depth+1, "<Match MatchId=\"" + RelationTypeIdentifier.STRING_EQ.getMatchFunction() + "\">\n");

            formatConstant(out, depth+2, RelationTypeIdentifier.STRING_EQ, action.toString());

            iputb(out, depth+2, "<AttributeDesignator MustBePresent=\"true\"\n");
            iputb(out, depth+2, "                     Category=\"", URN_3_0, "attribute-category:action\"\n");
            iputb(out, depth+2, "                     AttributeId=\"", URN_1_0, "action:action-id\"\n");
            iputb(out, depth+2, "                     DataType=\"" + RelationTypeIdentifier.STRING_EQ.getDataType() + "\"/>\n");

            iputb(out, depth+1, "</Match>\n");
            iputb(out, depth, "</AllOf>\n");
        }
    };


    /* A "flat predicate" is one that has operators of only one type (all AND or all OR) and can thus be "flattened out" into
     * a single A op B op C op ... regardless of the original structure. ACPL has a very flexible structure, but XACML, in the
     * Target, is more restrictive. Flat predicates can, relatively easily, be placed into Targets (although this requires some
     * help from the caller).
     *
     * The only acceptable operators are AND and OR. NOT is not allowed.
     */

    private class FlatPredicateDetector extends DefaultPredicateVisitor {
        // Remember the relation op we have seen
        BooleanOp seen = null;
        boolean flat = true;

        public void reset() {
            seen = null;
            flat = true;
        }

        @Override
        public void visit(ICompositePredicate pred, boolean preorder) {
            if (seen == null) {
                seen = pred.getOp();
            }

            if (pred.getOp() == BooleanOp.NOT) {
                flat = false;
            }

            flat &= (pred.getOp() == seen);
        }

        public boolean isFlat() {
            return flat;
        }

        public BooleanOp getOp() {
            // If we haven't seen anything at all then we have a flat structure and can pick the operator
            if (seen == null) {
                return BooleanOp.AND;
            }
            
            return seen;
        }
    }

    private interface IPredicateFormatter {
        void formatPredicate(final StringBuilder out, int depth, IPredicate pred);
    }


    private class FlatPredicateVisitor extends PredicateVisitor {
        private boolean seenComposite = false;

        public FlatPredicateVisitor(IPredicateFormatter predicateFormatter) {
            super(predicateFormatter, DEFAULT_CONTEXT);
        }

        public FlatPredicateVisitor(IPredicateFormatter predicateFormatter, EnumSet<EntityContext> ec) {
            super(predicateFormatter, ec);
        }

        // We are only interested in the leaf predicates
        public void visit(ICompositePredicate pred, boolean preorder) {
        }

        /**
         * Formats a relation spec.
         *
         * @param spec the relation spec to format.
         */
        public void visit(IRelation rel) {
            formatMatchRelation(out, getDepth(), rel, ec);
        }

    }

    private class PredicateVisitor implements IPredicateVisitor {
        private final IPredicateFormatter predicateFormatter;
        protected final EnumSet<EntityContext> ec;

        public PredicateVisitor(IPredicateFormatter predicateFormatter) {
            this(predicateFormatter, DEFAULT_CONTEXT);
        }

        public PredicateVisitor(IPredicateFormatter predicateFormatter, EnumSet<EntityContext> ec) {
            this.predicateFormatter = predicateFormatter;
            this.ec = ec;
        }

        /**
         * Formats a composite predicate.
         *
         * @param pred the composite predicate to format.
         * @param preorder true when it's a pre-order visit call, false otherwise.
         */
        public void visit(ICompositePredicate pred, boolean preorder) {
            visit(pred, pred.getOp(), preorder);
        }

        private void visit(ICompositePredicate pred, BooleanOp op, boolean preorder ) {
            if (preorder) {
                if (op == BooleanOp.OR) {
                    iput("<Apply FunctionId=\"", URN_1_0, "function:or\">\n");
                } else if (op == BooleanOp.AND) {
                    iput("<Apply FunctionId=\"", URN_1_0, "function:and\">\n");
                } else if (op == BooleanOp.NOT) {
                    iput("<Apply FunctionId=\"", URN_1_0, "function:not\">\n");
                }
                inc();
            } else {
                dec();
                iput("</Apply>\n");
            }
        }

        /**
         * Formats a spec reference.
         *
         * @param pred the predicate reference to format.
         */
        public void visit(IPredicateReference predRef) {
            // If this predicate was passed in then format it.  If not, print an error message
            // and skip it
            String name = ((IDSpecRef)predRef).getReferencedName();

            IDSpec referencedSpec = specs.get(name);
            if (referencedSpec == null) {
                put("<!-- Can't find referenced spec " + name + " -->\n");
                return;
            } 

            IPredicate pred = Predicates.transform(referencedSpec.getPredicate(), defaultTrans);

            pred.accept(this, IPredicateVisitor.PREPOSTORDER);
        }

        /**
         * Formats a relation spec.
         *
         * @param spec the relation spec to format.
         */
        public void visit(IRelation rel) {
            formatApplyRelation(out, getDepth(), rel, ec);
        }

        /**
         * Formats a generic predicate.
         *
         * @param pred the generic predicate to format.
         */
        public void visit(IPredicate pred) {
            predicateFormatter.formatPredicate(out, getDepth(), pred);
        }

    };



    private void formatObligationExpressionHead(String name, IEffectType effectType) {
        iput("<AdviceExpression AdviceId=\"", NEXTLABS_URN, name, "\" AppliesTo=\"", translateEffect(effectType), "\">\n");
    }

    private void formatObligationExpressionTail() {
        iput("</AdviceExpression>\n");
    }

    private void formatObligationAttributeAssignment(String id, String type, String value) {
        iput("<AttributeAssignmentExpression AttributeId=\"", id, "\">\n");
        inc();
        iput("<AttributeValue DataType=\"" + type + "\">" + value + "</AttributeValue>\n");
        dec();
        iput("</AttributeAssignmentExpression>\n");
    }



    private static final Map<String, String> typeCategoryMap = new HashMap<String, String>();
    private static final Map<String, String> altTypeCategoryMap = new HashMap<String, String>();

    static {
        // The particular attributes we are picking here don't matter (APP_URL would work as well as APP_NAME).  We just want
        // to know the object type name (e.g. "application") for each diffenent kind of attribute
        typeCategoryMap.put(SubjectAttribute.APP_NAME.getObjectTypeName(), NEXTLABS_URN +"subject-category:application-subject");
        typeCategoryMap.put(SubjectAttribute.USER_NAME.getObjectTypeName(), URN_1_0 + "subject-category:access-subject");
        typeCategoryMap.put(SubjectAttribute.HOST_NAME.getObjectTypeName(), NEXTLABS_URN + "subject-category:host-subject");

        // Destination resource is in the "alt map"
        typeCategoryMap.put(ResourceAttribute.NAME.getObjectTypeName(), URN_3_0 + "attribute-category:resource");

        typeCategoryMap.put(TimeAttribute.IDENTITY.getObjectTypeName(), URN_3_0 + "attribute-category:environment");
        typeCategoryMap.put(HeartbeatAttribute.TIME_SINCE_LAST_HEARTBEAT.getObjectTypeName(), URN_3_0 + "attribute-category:environment");
        typeCategoryMap.put(RemoteAccessAttribute.REMOTE_ACCESS.getObjectTypeName(), URN_3_0 + "attribute-category:environment");

        altTypeCategoryMap.put(ResourceAttribute.NAME.getObjectTypeName(), NEXTLABS_URN + "attribute-category:destination-resource");
        altTypeCategoryMap.put(SubjectAttribute.USER_NAME.getObjectTypeName(), URN_1_0 + "subject-category:recipient-subject");
    }


    private static String getAttributeCategory(IAttribute attr, EnumSet<EntityContext> ec) {
        String objType = attr.getObjectTypeName();

        String category = null;

        if (ec.contains(EntityContext.ALTERNATE_PREDICATE)) {
            category = altTypeCategoryMap.get(objType);

            if (category != null) {
                return category;
            }
        }

        category = typeCategoryMap.get(objType);

        if (category != null) {
            return category;
        }

        return "unknown-category";
    }

    private static String getAttributeName(IAttribute attr) {
        String name = attr.getName();
        String typeName = attr.getObjectTypeName();

        // Handle the few special cases where the XACML name isn't the same as the attribute name
        if (name.equals("name")) {
            if (typeName.equals(SubjectAttribute.USER_NAME.getObjectTypeName())) {
                return URN_1_0 + "subject:subject-id";
            } else if (typeName.equals(ResourceAttribute.NAME.getObjectTypeName())) {
                return URN_1_0 + "resource:resource-id";
            }
        }
        
        return name;
    }


    private boolean hasExceptions(IDPolicy p) {
        return (p.getPolicyExceptions().getPolicies().size() > 0);
    }

    private boolean isException(IDPolicy p) {
        return p.hasAttribute(IDPolicy.EXCEPTION_ATTRIBUTE);
    }

    private static final String PERMIT = "Permit";
    private static final String DENY = "Deny";

    private String translateEffect(IEffectType et) {
        if (et == EffectType.ALLOW) {
            return PERMIT;
        } else {
            return DENY;
        }
    }

    private static EnumSet<EntityContext> createOpContext(BooleanOp op) {
        if (op == BooleanOp.OR) {
            return EnumSet.of(EntityContext.OR_PREDICATE);
        } else {
            return EnumSet.of(EntityContext.AND_PREDICATE);
        }
    }

    // =============== PRIVATE UTILITY METHODS ===================
    private void put(Object ... o) {
        for (Object obj : o) {
            out.append(obj);
        }
    }

    private void iput(Object ... o) {
        indent();
        put(o);
    }

    private static void put(StringBuilder out, Object ... o) {
        for (Object obj : o ) {
            out.append(obj);
        }
    }

    private static void iputb(StringBuilder out, int indentDepth, Object ... o) {
        indent(out, indentDepth);
        put(out, o);
    }

    private int id = 0;
    private static int spi = 2;

    private void inc() {
        id++;
    }

    private void dec() {
        id--;
    }

    private int getDepth() {
        return id;
    }

    private void indent() {
        indent(out, id);
    }

    private static void indent(final StringBuilder out, int indentDepth) {
        for (int i = 0; i < indentDepth * spi ; i++) {
            out.append(' ');
        }
    }
}
