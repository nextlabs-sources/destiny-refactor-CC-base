package com.bluejungle.pf.engine.destiny;

/*
 * Created on Dec 2, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004-2007 by NextLabs, Inc., San Mateo CA, Ownership remains with NextLabs,
 * Inc, All rights reserved worldwide.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateReference;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.utils.MultipartKey;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.epicenter.evaluation.IEvaluationRequest;
import com.bluejungle.pf.domain.epicenter.misc.ITarget;
import com.bluejungle.pf.domain.epicenter.resource.IResource;

/**
 * @author sasha
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/EvaluationEngine.java#24 $
 */

public final class EvaluationEngine implements IEvaluationEngine {
    private final ITargetResolver resolver;
    private final Policy[] policies;
    private final IPredicate monitoredAppPredicate;
    private final IPredicate ignoredAppPredicate;
    private final IPredicate trustedAppPredicate;
    private final SubjectCache subjectCache;
    private static final String IGNORED_APPLICATIONS = "Ignored Applications";
    private static final String MONITORED_APPLICATIONS = "Monitored Applications";
    private static final String TRUSTED_APPLICATIONS = "Trusted Applications";
    private static final IEvalValue EMPTY_DIR = EvalValue.build(-1);
    private static final IEvalValue NON_EMPTY_DIR = EvalValue.build(1);
    private static final Log log = LogFactory.getLog(EvaluationEngine.class.getName());
    private static final PQLConverter pqlConverter = new PQLConverter();

    public EvaluationEngine(ITargetResolver resolver) {
        this.resolver = resolver;
        // cast for performance
        final IDPolicy[] ipolicies = resolver.getPolicies();
        policies = new Policy[ipolicies.length];
        System.arraycopy(ipolicies, 0, policies, 0, ipolicies.length);
        // Find the monitored and ignored app policies
        int monitoredAppPolicyIndex = -1;
        int ignoredAppPolicyIndex = -1;
        int trustedAppPolicyIndex = -1;
        for (int i = 0; i != policies.length; i++) {
            String policyName = policies[i].getName();
            if (MONITORED_APPLICATIONS.equals(policyName)) {
                monitoredAppPolicyIndex = i;
            } else if (IGNORED_APPLICATIONS.equals(policyName)) {
                ignoredAppPolicyIndex = i;
            } else if (TRUSTED_APPLICATIONS.equals(policyName)) {
                trustedAppPolicyIndex = i;
            }

            if ( monitoredAppPolicyIndex != -1 
                 && ignoredAppPolicyIndex != -1 
                 && trustedAppPolicyIndex != -1 ) {
                // all index has been found, I can stop now
                break;
            }
        }
        
        monitoredAppPredicate = getPredicate(policies, monitoredAppPolicyIndex);
        ignoredAppPredicate = getPredicate(policies, ignoredAppPolicyIndex);
        trustedAppPredicate = getPredicate(policies, trustedAppPolicyIndex);
        
        // Prepare the subject cache
        subjectCache = new SubjectCache(ipolicies.length);
    }
    
    public List<IDEffectType> evaluationDigest(final EvaluationRequest request) {
        final List<IDEffectType> results = new ArrayList<IDEffectType>(policies.length);
            
        for (int i = 0; i < policies.length; i++) {
            results.add(EffectType.DONT_CARE);
        }
        
        if (mustIgnoreResource(request)) {
            return results;
        }
        
        final BitSet applicables = getApplicablePolicies(request);
        
        final EvaluationContext context = new EvaluationContext(getSubjectKey(request), createPolicyExceptionMap(applicables, policies), policies, subjectCache);

        context.evaluate(request, getEvaluatablePolicies(applicables, policies));

        for (int i = 0; i < policies.length; i++) {
            if (context.getAllow(i)) {
                results.set(i, EffectType.ALLOW);
            } else if (context.getDeny(i)) {
                results.set(i, EffectType.DENY);
            }
        }

        return results;
    }

    public EvaluationResult evaluate(final EvaluationRequest request) {
        final IDPolicy[] additionalPolicies = pqlConverter.convert(request.getAdditionalPoliciesAsPQL());

        if (mustIgnoreResource(request)) {
            return new EvaluationResult(request, EvaluationResult.DONT_CARE);
        }

        final MultipartKey subjectKey = getSubjectKey(request);

        final IDPolicy[] combinedPolicies = combinePolicies(request, additionalPolicies);

        final BitSet applicables = getApplicablePolicies(request, combinedPolicies);

        final EvaluationContext context = new EvaluationContext(subjectKey, createPolicyExceptionMap(applicables, combinedPolicies), combinedPolicies, subjectCache);

        context.appendToLogString("Matching policies for " + request.getRequestId() + ":\n");
        if (applicables.isEmpty()) {
            context.appendToLogString("NO MATCHING POLICIES\n");
        }
           
        final IDEffectType effect = context.evaluate(request, getEvaluatablePolicies(applicables, combinedPolicies));

        final EvaluationResult res = new EvaluationResult(request, effect.getName());

        if (getLog().isInfoEnabled()) {
            getLog().info(context.getLogString());
        }

        if (request.getExecuteObligations()) {
            context.performObligations(request, res, effect);
        }

        return res;
    }


    public boolean isApplicationIgnorable( final IDSubject app ) {
        final IEvaluationRequest request = new EvaluationRequest() {
            private static final long serialVersionUID = 1L;
            public IDSubject getApplication() {
                return app;
            }
        };
        if ( monitoredAppPredicate != null && ignoredAppPredicate == null ) {
            // Only the monitored applications list is present
            return !monitoredAppPredicate.match(request);
        } else if ( monitoredAppPredicate == null && ignoredAppPredicate != null ) {
            // Only the ignored applications list is present
            return ignoredAppPredicate.match(request);
        } else if ( monitoredAppPredicate != null && ignoredAppPredicate != null ) {
            // Both ignored and monitored lists are present
            return !monitoredAppPredicate.match(request) || ignoredAppPredicate.match(request);
        } else {
            // Both ignorable and monitored lists are empty
            return false;
        }
    }
    
    public boolean isApplicationTrusted(final IDSubject app) {
        final IEvaluationRequest request = new EvaluationRequest() {
            private static final long serialVersionUID = 1L;
            public IDSubject getApplication() {
                return app;
            }
        };
        
        // if nothing is defined, nothing is trusted.
        return trustedAppPredicate != null && trustedAppPredicate.match(request); 
    }
    

    /**
     * Combine the default policies from the bundle with any additional policies, returning the result
     *
     * @param additionalPolicies a (possibly null) array of Policy objects
     * @return an array of the default policies + additional policies
     */
    private IDPolicy[] combinePolicies(final EvaluationRequest request, final IDPolicy[] additionalPolicies) {
        if (additionalPolicies == null) {
            return policies;
        }

        if (request.getIgnoreBuiltinPolicies()) {
            return additionalPolicies;
        } else {
            final IDPolicy[] combined = Arrays.copyOf(policies, policies.length + additionalPolicies.length);

            System.arraycopy(additionalPolicies, 0, combined, policies.length, additionalPolicies.length);
            return combined;
        }
    }


    /**
     * From all the applicable policies, find the policies used as exceptions and return a map from their names
     * to the policy index
     *
     * @param applicables a bitset representing the applicable policies
     * @return a map from names to policies of all the policies used as exceptions to other policies
     */
    private static Map<String, Integer> createPolicyExceptionMap(final BitSet applicables, final IDPolicy[] policies)
    {
        final Map<String, Integer> exceptionMap = new HashMap<String, Integer>();
         
        for (int i = applicables.nextSetBit(0); i >= 0; i = applicables.nextSetBit(i+1)) {
            if (policies[i].hasAttribute(IDPolicy.EXCEPTION_ATTRIBUTE)) {
                exceptionMap.put(policies[i].getName(), i);
            }
        }

        return exceptionMap;
    }

    private static IPredicate getPredicate(final IDPolicy[] policies, final int index) {
        if ( index == -1 ) {
            return null;
        }

        final ITarget target = policies[index].getTarget();
        if ( target == null ) {
            return null;
        }
        final IPredicate monitoredPred = target.getSubjectPred();
        if ( monitoredPred == null ) {
            return null;
        }

        // Check if the monitored/ignored app policies have
        // constant "true" predicate (which means that they are empty).
        
        final AtomicBoolean isEmpty = new AtomicBoolean(true);

        monitoredPred.accept(new DefaultPredicateVisitor(){
            public void visit( IPredicateReference pred ) {
                isEmpty.set(false);
            }
            public void visit( IRelation pred ) {
                isEmpty.set(false);
            }
        }, IPredicateVisitor.PREORDER);

        if ( isEmpty.get() ) {
            return null;
        }

        return monitoredPred;
    }

    /**
     * Determine if this resource is one that we always ignore. Most of this logic should be
     * in the PEP, actually. The only remaining special case is empty directories or directories
     * where we are doing the OPEN action.
     *
     * @param request the evaluation request
     * @return whether or not we should ignore this resource
     */
    private static boolean mustIgnoreResource(final EvaluationRequest request) {
        final IResource resource = request.getFromResource();
            
        if (resource == null) {
            return false;
        }

        final IEvalValue directoryType = resource.getAttribute(ResourceAttribute.IS_DIRECTORY.getName());

        if (NON_EMPTY_DIR.equals(directoryType)) {
            // Ignore "open" operations on non-empty folders
            return (request.getAction() == DAction.OPEN);
        } else if (EMPTY_DIR.equals(directoryType)) {
            return true;
        }

        return false;
    }

    /**
     * Constructs a multipart key, that consists of the user, host, and application.
     * This will be used for caching evaluation results
     *
     * @param request the evaluation request
     * @return the key
     */
    private static MultipartKey getSubjectKey(final EvaluationRequest request) {
        final IDSubject user = request.getUser();
        final IDSubject host = request.getHost();
        final IDSubject app = request.getApplication();

        final List<Long> keys = new ArrayList<Long>();

        boolean isCacheable = true;

        if (user != null && user.getId() != null) {
            isCacheable = isCacheable && user.isCacheable();
            keys.add(user.getId());
        }

        if (host != null && host.getId() != null) {
            isCacheable = isCacheable && host.isCacheable();
            keys.add(host.getId());
        }

        if (app != null && app.getId() != null) {
            isCacheable = isCacheable && app.isCacheable();
            keys.add(app.getId());
        }

        final MultipartKey subjectKey = new MultipartKey(keys);
        subjectKey.setCacheable(isCacheable);

        return subjectKey;
    }

    /**
     * Get a bitset denoting the policies that we need to evaluate for this request
     *
     * @param request the evaluation request
     * @return the applicable policies
     */
    private BitSet getApplicablePolicies(final EvaluationRequest request) {
        return getApplicablePolicies(request, resolver.getPolicies());
    }

    /**
     * Get a bitset denoting the policies that we need to evaluate for this request
     *
     * @param request the evaluation request
     * @param additional policies (assumed to be applicable)
     * @return the applicable policies
     */
    private BitSet getApplicablePolicies(final EvaluationRequest request, final IDPolicy[] combinedPolicies) {
        BitSet applicables = null;

        int appendNewPoliciesAt = 0;
            
        if (request.getIgnoreBuiltinPolicies()) {
            // The only policies that count are the ones passed in
            applicables = new BitSet(combinedPolicies.length);

        } else {
            applicables = resolver.getApplicablePolicies(request);

            // Make sure that all the extra policies (if any) are marked applicable
            appendNewPoliciesAt = resolver.getPolicies().length;
        }
        
        checkApplicabilityOfAddedPolicies(request, combinedPolicies, applicables, appendNewPoliciesAt);
                
        final BitSet skippedPolicies = request.getContentAnalysisManager().performContentAnalysis(request, combinedPolicies, applicables);

        if (skippedPolicies != null) {
            applicables.andNot(skippedPolicies);
        }

        return applicables;
    }

    /**
     * We can't assume the new policies are automatically
     * applicable. When evaluating policies we check everything except
     * the action. IOW, we assume that policies that don't match the
     * action have already been excluded. We need to remove those
     * here.
     *
     * @param request the request
     * @param combinedPolicies all the policies (bundle + user supplied)
     * @param applicables the bitset for applicable polices. We modify this
     * @param index the position in combinedPolicies (and the bitset) where the new policies start (after all the bundle policies)
     */
    private static void checkApplicabilityOfAddedPolicies(final EvaluationRequest request, final IDPolicy[] combinedPolicies, final BitSet applicables, final int index) {
        for (int i = index ; i < combinedPolicies.length; i++) {
            IDPolicy p = combinedPolicies[i];

            applicables.set(i, p.getTarget().getActionPred().match(request));
        }
    }
            
    /**
     * From all the applicable policies, get the evaluatable ones. Some policies are "top-level" and others just
     * exist as  exceptions to other policies. We want the ones that are not exceptions
     *
     * @param applicables a bitset representing the applicable policies
     * @return a list of evaluatable policies and their corresponding indexes
     */
    private static BitSet getEvaluatablePolicies(final BitSet applicables, final IDPolicy[] policies) {
        final BitSet evaluatables = new BitSet(policies.length);
        
        for (int i = applicables.nextSetBit(0); i >= 0; i = applicables.nextSetBit(i+1)) {
            if (!policies[i].hasAttribute(IDPolicy.EXCEPTION_ATTRIBUTE)) {
                evaluatables.set(i);
            }
        }

        return evaluatables;
    }

    private Log getLog() {
        return log;
    }
}
