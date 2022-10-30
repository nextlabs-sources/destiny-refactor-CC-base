package com.bluejungle.pf.destiny.policymap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.hibernate.HibernateException;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.domain.IHasId;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.ICompositePredicate;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.expressions.Predicates.DefaultTransformer;
import com.bluejungle.framework.expressions.Predicates.ITransformer;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.pf.domain.destiny.common.ServerSpecManager;
import com.bluejungle.pf.destiny.lib.DictionaryHelper;
import com.bluejungle.pf.destiny.parser.IHasPQL;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectSpec;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;

/**
 * This is responsible for resolving the references in all the supplied policies and
 * generate mappings between the references and the corresponding policies.
 * This uses a lot of common code from MapBuilder which at some point need to
 * be refactored.
 *
 * @author ssen
 *
 */
public class PolicyReferenceResolver {
    private final StaticAttributeTracker attrTracker;
    private final ServerSpecManager specManager;
    private final DictionaryHelper dictHelper;
    private final IEnrollment[] domains;
    private final ReferenceResolver referenceResolver;
    private final IDictionary dictionary;
    private final IElementType contactType;
    private final IElementType hostType;
    private final IElementType userType;
    private final Date consistentTime;
    private static final IComponentManager componentManager = ComponentManagerFactory.getComponentManager();

    private static final Log LOG = LogFactory.getLog(PolicyReferenceResolver.class.getName());

    Pattern pNameFragmentPattern = null;

    public PolicyReferenceResolver(Collection<? extends IHasPQL> deployedEntities,
                                   String[] domainNames)
        throws DictionaryException {

        referenceResolver = new ReferenceResolver(deployedEntities);

        dictionary = componentManager.getComponent( Dictionary.COMP_INFO );

        if (domainNames == null) {
            domainNames = getAllEnrolledDomains().toArray(new String[0]);
        }
        
        domains = referenceResolver.initDomains(domainNames);

        specManager = componentManager.getComponent(ServerSpecManager.COMP_INFO);
        dictHelper = new DictionaryHelper(specManager, dictionary);

        consistentTime = dictionary.getLatestConsistentTime();

        contactType = dictionary.getType(ElementTypeEnumType.CONTACT.getName());
        hostType = dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
        userType = dictionary.getType(ElementTypeEnumType.USER.getName());

        attrTracker = componentManager.getComponent(StaticAttributeTracker.COMP_INFO);
        attrTracker.update();
    }

    private List<String> getAllEnrolledDomains() throws DictionaryException {
        List<String> enrolledDomains = new ArrayList<String>();
        Collection<IEnrollment> enrollments = dictionary.getEnrollments();
        for (IEnrollment thisEnrollment : enrollments) {
            enrolledDomains.add(thisEnrollment.getDomainName());
        }

        return enrolledDomains;
    }

    public ResolvedPolicyData resolvePolicySubjectData(boolean setUserToTrue, boolean setHostToTrue,
                                                       boolean setAppToTrue, String searchPoliciesContainingThis)
        throws PQLException, HibernateException, DictionaryException {

        referenceResolver.resolve(searchPoliciesContainingThis);

        // we do not care about from and to resources of any policy - only subjects
        for (IDPolicy parsedPolicy : referenceResolver.getParsedPolicies()) {
            parsedPolicy.getTarget().setFromResourcePred(PredicateConstants.TRUE);
            parsedPolicy.getTarget().setToResourcePred(PredicateConstants.TRUE);
        }

        ResolvedPolicyData resolvedPolicyData = null;

        // Resolve Users/Groups
        IPredicate allUsers = new CompositePredicate(BooleanOp.OR,
                                                     Arrays.asList(
                                                         dictionary.condition(contactType), dictionary.condition(userType)));

        SubjectResolver userResolver = new SubjectResolver(allUsers, SubjectType.USER);
        userResolver.resolveSubjects(setUserToTrue);

        //Resolve Hosts
        if (domains == null) {
            // no hosts
            getLog().info("No domains supplied - cannt resolve hosts");
        } else {
            IPredicate domainCondition;
            if (domains.length > 1) {
                List<IPredicate> conditions = new ArrayList<IPredicate>(domains.length);
                for ( int i = 0 ; i != domains.length ; i++ ) {
                    conditions.add(dictionary.condition(domains[i]));
                }
                domainCondition = new CompositePredicate(BooleanOp.OR, conditions);
            } else if (domains.length == 1) {
                domainCondition = dictionary.condition(domains[0]);
            } else {
                domainCondition = PredicateConstants.FALSE;
            }
            IPredicate allHosts = new CompositePredicate(
                BooleanOp.AND,  Arrays.asList(dictionary.condition(hostType),
                                              domainCondition ));
            SubjectResolver hostResolver =
                new SubjectResolver(allHosts,  SubjectType.HOST);
            hostResolver.resolveSubjects(setHostToTrue);
        }

        IElementType appType =
            dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
        SubjectResolver appResolver =
            new SubjectResolver(dictionary.condition(appType), SubjectType.APP);
        appResolver.resolveSubjects(setAppToTrue);

        if (getLog().isDebugEnabled()) {
            getLog().debug("Results after resolving the policy subject references:");
            for (IDPolicy thisPol : referenceResolver.getParsedPolicies()) {
                getLog().debug("Policy Name: " + thisPol.getName());
                getLog().debug(" Target subject predicate: " +
                               thisPol.getTarget().getSubjectPred());
            }
        }

        resolvedPolicyData = new ResolvedPolicyData(referenceResolver.getPolicyIds(), referenceResolver.getParsedPolicies());
        return resolvedPolicyData;
    }

    class SubjectResolver {
        SubjectType subjectType;
        long[] subjIds;
        IPredicate allSubjects;

        SubjectResolver(IPredicate allSubjects, SubjectType subjectType) {
            this.subjectType = subjectType;
            this.allSubjects = allSubjects;
        }

        void  resolveSubjects(boolean transformToTrue) throws DictionaryException  {
            List<Long> subjIdList = new ArrayList<Long>();
            IDictionaryIterator<Long> subjIter =
                dictionary.queryKeys(allSubjects, consistentTime);
            try {
                while (subjIter.hasNext()) {
                    subjIdList.add(subjIter.next());
                }
            } finally {
                subjIter.close();
            }

            subjIds = new long[subjIdList.size()];
            for ( int i = 0 ; i != subjIds.length ; i++ ) {
                subjIds[i] = (subjIdList.get(i)).longValue();
            }
            subjIdList = null;

            for (Map.Entry<Long,ReferenceResolver.IParsedEntity> entry : referenceResolver.getPolicyById().entrySet()) {
                IDPolicy policy = (IDPolicy)entry.getValue().getParsed();
                IPredicate subjectCondition = Predicates.transform(
                    policy.getTarget().getSubjectPred() ,
                new SubjectFilterTransformer(subjectType));
                if (subjectCondition == null) {
                    // This means that the subject type is not specified - so skip any further processing
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("The subject type: " + subjectType +
                                       " for policy: " + policy.getName() + " with predicate: " +
                                       policy.getTarget().getSubjectPred() + " has a NULL subject " +
                                       " condition. Skipping any further action");
                    }
                    continue;
                }

                // See if the policy should be added to all or to a subset of all subjects
                boolean tryAllSubjects = false;
                if (subjectCondition == PredicateConstants.TRUE) {
                    tryAllSubjects = true;
                } else if (subjectCondition instanceof IDSubjectSpec.SubjectSpecBuiltin) {
                    tryAllSubjects = (subjectCondition != IDSubjectSpec.EMPTY);
                } else if (policy.getMainEffect() == EffectType.ALLOW) {
                    tryAllSubjects = (policy.getOtherwiseEffect() == EffectType.DENY);
                }

                if (getLog().isDebugEnabled()) {
                    getLog().debug("Trying to transform the subject type: " + subjectType +
                                   " for policy: " + policy.getName() + " with predicate: " +
                                   policy.getTarget().getSubjectPred());
                }

                IPredicate transformedSubj = PredicateConstants.TRUE;
                if (!tryAllSubjects) {
                    transformedSubj = Predicates.transform(
                        policy.getTarget().getSubjectPred(),
                    new SubjectTransformer(allSubjects, subjectType, transformToTrue));
                }

                // Note that if tryAllSubjects is true, it means that it is applicable for all
                policy.getTarget().setSubjectPred(transformedSubj);

                if (getLog().isDebugEnabled()) {
                    getLog().debug("Transforming predicate: " +
                                   policy.getTarget().getSubjectPred() + " to: \n " + transformedSubj);
                }
            } // for
        } // resolveSubjects

        public String toString() {
            StringBuilder sb = new StringBuilder("SUBJECT Type:").append(subjectType);
            sb.append("\n SUBJECT IDs: ");
            if (subjIds != null) {
                for (int i = 0; i < subjIds.length; i++) {
                    sb.append(subjIds[i]).append(",");
                }
            } else {
                sb.append("NULL");
            }
            return sb.toString();
        }
    } // subjectResolver

    /**
     *  This transformer removes attributes referencing types  other than subjType, and
     *  ignores all dynamic attributes. Special care is taken for applications since
     *  attributes such as name etc are always dynamic. The logic here should be
     *  re-visited since we should not have to add the special case of application type here.
     *
     */
    class SubjectFilterTransformer extends Predicates.DefaultTransformer {
        SubjectType subjType;

        SubjectFilterTransformer(SubjectType subjType) {
            this.subjType = subjType;
        }

        /**
         * @see DefaultTransformer#transformRelation(IRelation)
         */
        public IPredicate transformRelation(IRelation rel) {
            if (isStaticAttributeOfThisType(rel.getLHS()) || isStaticAttributeOfThisType(rel.getRHS())) {
                return null;
            } else {
                return rel;
            }
        }

        /**
         * This method returns true if the relation needs to be removed:
         * Relations on attributes of unrelated types and
         * relations on dynamic attributes need to be removed.
         * @param attr the <code>IExpression</code> to check.
         * @return true if the attribute is to be removed, false otherwise.
         */
        private boolean isStaticAttributeOfThisType(IExpression attr) {
            if (attr instanceof IDSubjectAttribute) {
                IDSubjectAttribute subjAttr = (IDSubjectAttribute)attr;
                // Relations on attributes of unrelated types and
                // relations on dynamic attributes need to be removed.
                if (subjAttr.getSubjectType() != subjType) {
                    return true;
                } else if (!subjAttr.getSubjectType().equals(SubjectType.APP)
                           && attrTracker.isDynamic(subjAttr))   {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    class SubjectTransformer extends Predicates.DefaultTransformer {
        private IPredicate singleSubjectPredicate = null;
        private Predicates.IDetector wrongSubjectDetector = null;
        private SubjectFilterTransformer keepOnlyStaticAttributesOfThisType;
        IPredicate allSubjects;
        SubjectType subjType;
        boolean transformToTrue;

        //private final Map<IPredicate,IPredicate> cache =  new HashMap<IPredicate,IPredicate>();

        SubjectTransformer(IPredicate allSubjects, SubjectType subjType,
                           boolean transformToTrue) {
            this.allSubjects = allSubjects;
            wrongSubjectDetector = new WrongSubjectDetector(subjType);
            keepOnlyStaticAttributesOfThisType =
                new SubjectFilterTransformer(subjType);
            this.subjType  = subjType;
            this.transformToTrue = transformToTrue;
        }

        public void transformCompositeStart(ICompositePredicate pred) {
            if (singleSubjectPredicate != null) {
                return;
            }
            if (!Predicates.find(pred, wrongSubjectDetector)) {
                singleSubjectPredicate = pred;
            }
        }

        /**
         * @see DefaultTransformer#transformCompositeEnd(ICompositePredicate, IPredicate)
         */
        public IPredicate transformCompositeEnd(ICompositePredicate orig, IPredicate converted) {
            IPredicate res;
            if (singleSubjectPredicate == orig) {
                singleSubjectPredicate = null;
                res = flattenSubjectPredicate(converted);
            } else {
                res = converted;
            }
            return res;
        }


        /**
         * Converts the given predicate to a synthetic group predicate.
         * @param pred the predicate to convert.
         * @return the resulting synthetic group.
         */
        private IPredicate flattenSubjectPredicate(IPredicate pred)  {
            IPredicate res = null;
            if (transformToTrue) {
                res = PredicateConstants.TRUE;
            } else {
                IPredicate queryPred =
                    Predicates.transform(pred, keepOnlyStaticAttributesOfThisType);
                if (queryPred == null) {
                    queryPred = PredicateConstants.TRUE;
                }
                IDictionaryIterator<Long> subjIter = null;
                try {
                    subjIter = dictionary.queryKeys( new CompositePredicate(BooleanOp.AND,
                                                                            Arrays.asList(allSubjects, dictHelper.toDictionaryPredicate(
                                                                                              queryPred, null, subjType))), consistentTime);
                    Collection<IPredicate> preds = new ArrayList<IPredicate>();
                    while (subjIter.hasNext()) {
                        long thisSubjId =  subjIter.next().longValue();
                        IExpression lhs = subjType == SubjectType.USER ?
                                          SubjectAttribute.USER_ID :  SubjectAttribute.HOST_ID;

                        IRelation rel = new Relation(
                            RelationOp.EQUALS, lhs, Constant.build(thisSubjId));

                        preds.add(rel);
                    }
                    res = new CompositePredicate(BooleanOp.OR, preds);
                } catch (DictionaryException e) {
                    getLog().error("Error building subject groups: "+e.getMessage());
                } finally {
                    if (subjIter != null) {
                        try {
                            subjIter.close();
                        } catch (Exception ex) {
                            if (getLog().isErrorEnabled()) {
                                getLog().error("Exception while trying to close subject iterator",ex);
                            }
                        }
                    }
                }
            }
            return res;
        }
    } // class SubjectTransformer

    class WrongSubjectDetector  extends Predicates.DefaultDetector {
        SubjectType subjType;

        WrongSubjectDetector(SubjectType subjType) {
            this.subjType = subjType;
        }

        // This detector determines whether or not a predicate
        // contains references to attributes of unrelated types.
        public boolean checkRelation(IRelation rel) {
            if (rel.getLHS() instanceof IAttribute || rel.getRHS() instanceof IAttribute) {
                // An expression contains attributes
                if (rel.getLHS() instanceof IDSubjectAttribute || rel.getRHS() instanceof IDSubjectAttribute) {
                    // An expression contains a subject attribute
                    if (rel.getLHS() instanceof IDSubjectAttribute && checkSubjectAttribute((IDSubjectAttribute)rel.getLHS())) {
                        return true;
                    }
                    if (rel.getRHS() instanceof IDSubjectAttribute && checkSubjectAttribute((IDSubjectAttribute)rel.getRHS())) {
                        return true;
                    }
                } else {
                    // An expression contains a non-subject attribute
                    return true;
                }
            }
            return false;
        }
        boolean checkSubjectAttribute(IDSubjectAttribute attr) {
            return attr.getSubjectType() != subjType;
        }
    } // class WrongSubjectDetector

    Log getLog() {
        return LOG;
    }

    private void setPolicyNameFragmentPattern(Pattern pNamePattern) {
        pNameFragmentPattern = pNamePattern;
    }

    private Pattern getPolicyNameFragmentPattern() {
        return pNameFragmentPattern;
    }
}
