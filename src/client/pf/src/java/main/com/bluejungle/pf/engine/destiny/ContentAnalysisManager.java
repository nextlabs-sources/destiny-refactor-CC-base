package com.bluejungle.pf.engine.destiny;

/*
 * Created on Nov 10, 2008
 *
 * All sources, binaries and HTML pages (C) copyright 2008 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/pf/src/java/main/com/bluejungle/pf/engine/destiny/ContentAnalysisManager.java#1 $
 */

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.ipc.IOSWrapper;
import com.bluejungle.destiny.agent.ipc.OSWrapper;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.expressions.DefaultPredicateVisitor;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IPredicateVisitor;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Predicates;
import com.bluejungle.framework.utils.StringUtils;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.policy.Policy;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.epicenter.evaluation.IEvaluationRequest;
import com.bluejungle.pf.domain.epicenter.resource.IMResource;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;

/**
 * This class provides the implementation of the content analysis, including parsing poilcies
 * to determine what analysis must be done, and updating the resource attributes.
 *
 * We assume that content analysis checks are made in PQL like so:
 *
 * resource.fso.content = "*REG:{name}>={count};*" or
 * resource.fso.content = "*KEY:{keywords}>={count};*"
 *
 * If the query REG:{name}>={count} matches then the string
 * "REG:{name}>={count};" (note the trailing ';') will appear in the
 * "content" attribute.  If it does not match then the string
 * "REG#:{name}>={count};" will appear.  If neither of these appear
 * then we have not checked for this content.  Note that there must be
 * a final, terminating ';' for this to work.
 *
 * This code is highly dependent on this format and any deviation from
 * this will likely result in NullPointerExceptions
 */

public class ContentAnalysisManager implements IContentAnalysisManager {
    private static final Log log = LogFactory.getLog(EvaluationEngine.class.getName());
    private static final IOSWrapper osWrapper;
    private IRegularExpressionInformationManager regularExpressionInfoManager;
    private Long processToken;
    private IMResource fromResource;
    private IMResource toResource;
    private int pid;
    private String userSid;
    private int level;

    class ContentAnalysisChecker extends DefaultPredicateVisitor {
        List<String> results = new ArrayList<String>();
        
        public List<String> getResults() {
            return results;
        }
        
        @Override
            public void visit(IRelation pred) {
            if (isContentAnalysisAttribute(pred.getLHS())) {
                extract(pred.getRHS());
            } else if (isContentAnalysisAttribute(pred.getRHS())) {
                extract(pred.getLHS());
            } 
        }
        
        private void extract(IExpression expr) {
            if (expr instanceof Constant) {
                results.add(((Constant)expr).getRepresentation());
            }
        }
    }

    class PartialEvaluator extends Predicates.DefaultTransformer {
        IEvaluationRequest req;

        PartialEvaluator(IEvaluationRequest req) {
            this.req = req;
        }

        // Evaluate everything except for content analysis relations
        @Override
            public IPredicate transformRelation(IRelation pred) {
            if (isContentAnalysisAttribute(pred.getLHS()) ||
                isContentAnalysisAttribute(pred.getRHS())) {
                return pred;
            } else {
                // Messy.  See EvaluationEngine.  We might not have a fromm
                // resource.  If that's the case then the only predicate we can
                // match is one that is always true.  That will evaluate correctly
                // with the null evaluation environment.
                if (req.getFromResource() == null) {
                    try {
                        if (pred.match(null)) {
                            return PredicateConstants.TRUE;
                        } else {
                            return PredicateConstants.FALSE;
                        }
                    } catch (Exception all) {
                        getLog().error("Misbehaving attribute class in empty predicate match");
                        return PredicateConstants.FALSE;
                    }
                }
                else {
                    if (pred.match(req)) {
                        return PredicateConstants.TRUE;
                    } else {
                        return PredicateConstants.FALSE;
                    }
                }
            }
        }
    }
    
    static {
        osWrapper = (IOSWrapper)ComponentManagerFactory.getComponentManager().getComponent( OSWrapper.class );
    }
    
    public static IContentAnalysisManager build(IRegularExpressionInformationManager regularExpressionInfoManager,
                                                IMResource fromResource,
                                                IMResource toResource,
                                                int pid,
                                                Long processToken,
                                                String userSid,
                                                int level)
    {
        if (fromResource == null) {
            return IContentAnalysisManager.DEFAULT;
        } else {
            return new ContentAnalysisManager(regularExpressionInfoManager, fromResource, toResource, pid, processToken, userSid, level);
        }
        
    }

    private ContentAnalysisManager(IRegularExpressionInformationManager regularExpressionInfoManager,
                                   IMResource fromResource,
                                   IMResource toResource,
                                   int pid,
                                   Long processToken,
                                   String userSid,
                                   int level) {
        this.regularExpressionInfoManager = regularExpressionInfoManager;
        this.fromResource = fromResource;
        this.toResource = toResource;
        this.pid = pid;
        this.processToken = processToken;
        this.userSid = userSid;
        this.level = level;
    }

    private boolean isContentAnalysisAttribute(IExpression expr) {
        if (expr instanceof ResourceAttribute) {
            return ((ResourceAttribute)expr).getName().equals(SpecAttribute.CONTENT_NAME);
        } else {
            return false;
        }
    }

    private boolean policyHasContentAnalysis(IDPolicy policy) {
        ContentAnalysisChecker checker = new ContentAnalysisChecker();
        IPredicate pred = policy.getTarget().getFromResourcePred();

        if (pred != null) {
            pred.accept(checker, IPredicateVisitor.PREORDER);
        }

        if (checker.getResults().size() > 0) {
            return true;
        } else {
            pred = policy.getTarget().getToResourcePred();

            if (pred != null) {
                pred.accept(checker, IPredicateVisitor.PREORDER);
            }
        }

        return (checker.getResults().size() > 0);
    }

    private List<Integer> getPoliciesWithContentAnalysis(IDPolicy[] policies, BitSet applicables) {
        List<Integer> policiesWithContentAnalysis = new ArrayList<Integer>();

        for (int i = applicables.nextSetBit(0); i >= 0; i = applicables.nextSetBit(i+1)) {
            IDPolicy policy = policies[i];

            if (policyHasContentAnalysis(policy)) {
                policiesWithContentAnalysis.add(i);
            }
        }

        return policiesWithContentAnalysis;
    }

    private IPredicate processPartialEvaluation(IPredicate pred, Set<String> contentAnalysisStrings, IMResource resource) {
        // The result of partial evaluation might have collapsed the
        // entire predicate down to TRUE or FALSE or it might have
        // left some content analysis predicates around.
        if (pred == null) {
            // Everything matches a predicate that doesn't exist
            pred = PredicateConstants.TRUE;
        } else if (resource == null) {
            // If the resource doesn't exist then the only thing we can match is straight TRUE
            if (pred != PredicateConstants.TRUE) {
                pred = PredicateConstants.FALSE;
            }
        } else {
            // We have a resource and a predicate.  Let's check for content analysis strings
            ContentAnalysisChecker checker = new ContentAnalysisChecker();
            pred.accept(checker, IPredicateVisitor.PREORDER);
            
            filterContentAnalysis(checker.getResults(), contentAnalysisStrings, resource);
        }

        return pred;
    }

    private void performContentAnalysisForSingleResource(Long reqId, IMResource resource, Set<String> contentAnalysisStrings) {
        // If either the resource or a manageable file system name doesn't exist then there is nothing
        // for us to do
        if (resource == null || resource == IEvalValue.EMPTY) {
            getLog().debug("Request " + reqId + " has non-existant resource.  Returning\n");
            return;
        }

        if (contentAnalysisStrings.size() == 0) {
            getLog().debug("Request " + reqId + " has no content analysis strings.  Returning\n");
            return;
        }

        IEvalValue nativeResNameValue = resource.getAttribute(SpecAttribute.NATIVE_RESOURCE_NAME);

        if (nativeResNameValue == null || nativeResNameValue == IEvalValue.NULL) {
            getLog().debug("Unable to find resource name for request " + reqId + ".  Returning\n");
            return;
        }

        String nativeResName = (String)nativeResNameValue.getValue();
        
        // Content analysis relies on ifilters and ifilters use the file extension to work their magic.  tmp files could
        // be anything and unless they actually turn out to be text files (unlikely) we will not be able to analyze them.
        // Skip.
        if (nativeResName.toLowerCase().endsWith(".tmp")) {
            getLog().info("Skipping content analysis for request " + reqId + ".  File " + nativeResName + " is a tmp file");
            return;
        }

        if (getLog().isTraceEnabled()) {
            StringBuilder sb = new StringBuilder("Filtered content analysis specifications for ");
            sb.append(nativeResName + ":\n");
            for (String s : contentAnalysisStrings) {
                sb.append(s + "\n");
            }
            getLog().trace(sb.toString());
        }

        // Each string is broken up into four pieces and put into an array
        String[] contentAnalysisInput = buildContentAnalysisArray(contentAnalysisStrings);

        if (getLog().isTraceEnabled()) {
            StringBuilder sb = new StringBuilder("Formatted for JNI:\n");
            for (int i = 0; i != contentAnalysisInput.length; i++) {
                if (i % 4 == 0) {
                    sb.append("--------\n");
                }
                sb.append(contentAnalysisInput[i] + "\n");
            }
            sb.append("--------\n");
            getLog().trace(sb.toString());
        }

        // So the response should be 1/4 of the length of the input
        long startTime = System.nanoTime();
        int[] response = osWrapper.getContentAnalysisAttributes(nativeResName, pid, processToken, userSid, level, contentAnalysisInput);
        long endTime = System.nanoTime();

        if (response.length != contentAnalysisInput.length / 4) {
            // Either an error or the file does not exist.  Most likely the latter
            getLog().warn("Request " + reqId + " content analysis for " + nativeResName + " failed (file might not exist).");
            return;
        }
        else if (getLog().isTraceEnabled()) {
            StringBuilder sb = new StringBuilder("Request " + reqId + " results for " + nativeResName + ":\n");
            for (int i = 0; i != response.length; i++) {
                sb.append(response[i] + " ");
            }
            sb.append("\nAnalysis time: " + (endTime - startTime)/1000000 + "ms\n");
            getLog().trace(sb.toString());
        }

        String updatedContent = convertResultsToString(contentAnalysisInput, response);

        IEvalValue originalContent = resource.getAttribute(SpecAttribute.CONTENT_NAME);
        
        if (originalContent == null || originalContent == IEvalValue.NULL) {
            resource.setAttribute(SpecAttribute.CONTENT_NAME, EvalValue.build(updatedContent));
        } else {
            resource.setAttribute(SpecAttribute.CONTENT_NAME, EvalValue.build((String)originalContent.getValue() + ";" + updatedContent));
        }
    }

    /**
     * @see IContentAnalysisManager.performContentAnalysis()
     *
     * We don't return the results of CA (if any).  Instead, we modify the
     * resources in req to include the results of the content analysis
     * pass.  The evaluation step will take these and make a final
     * determination.
     *
     * Content analysis is expensive, so we only do it if we have to.
     * If we determine that either the source or destination won't
     * match a policy (based on all its other attributes, name, etc)
     * then we don't do content analysis.  There is a catch, however.
     *
     * The evaluation will make "auxiliary maps".  If it determines
     * that resource A doesn't match its appropriate part of policy B
     * then it will note that fact and not evaluate policy B when it
     * next sees resource A.  If, however, we short circuit the CA
     * step then the evaluation could conclude that resource A doesn't
     * match (its part of) policy B when what really happened is we
     * didn't evaluate enough of resource A to make a determination.
     *
     * We return a BitSet of the skipped policies (or null if none
     * were skipped) so that (a) evaluation doesn't try to evaluate
     * them and (b) it won't make incorrect assumptions about
     * resource->policy applicability.
     *
     */

    public BitSet performContentAnalysis(IEvaluationRequest req, IDPolicy[] policies, BitSet applicables) {
        if (fromResource == null) {
            return null;
        }

        IEvalValue fromNativeResNameValue = fromResource.getAttribute(SpecAttribute.NATIVE_RESOURCE_NAME);

        if (fromNativeResNameValue == null || fromNativeResNameValue == IEvalValue.NULL) {
            getLog().trace("No from resource found.  Ignoring");
            return null;
        }

        // applicables isn't the last word.  We partially evaluate the
        // resource predicates to see if they match up to content
        // analysis and then perform the necessary content analysis

        // First, get the policies that actually have content analysis
        List<Integer> policiesWithContentAnalysis = getPoliciesWithContentAnalysis(policies, applicables);

        // No policies => no content analysis
        if (policiesWithContentAnalysis.size() == 0) {
            return null;
        }

        // Then evaluate the policy as far as we can, leaving the content analysis
        // relations alone
        
        // The final collection of strings
        Set<String> fromContentAnalysisStrings = new HashSet<String>();
        Set<String> toContentAnalysisStrings = new HashSet<String>();

        BitSet skippedContentPolicies = new BitSet(policies.length);

        for (Integer policyOrdinal : policiesWithContentAnalysis) {
            IDPolicy policy = policies[policyOrdinal];

            // If the 'to' predicate evaluates to FALSE (which is not
            // the same as not being there) then the policy won't
            // match.  If the policy won't match then we don't care
            // about any content analysis stuff that might be in the
            // 'from' resource, so we don't even bother computing it.
            if (processPartialEvaluation(Predicates.transform(policy.getTarget().getToResourcePred(),
                                                              new PartialEvaluator(EvaluationRequest.createToRequest(req))),
                                         toContentAnalysisStrings,
                                         toResource) == PredicateConstants.FALSE) {
                getLog().trace("Destination resource didn't match in policy " + policy.getName());
                skippedContentPolicies.set(policyOrdinal);
            } else {
                processPartialEvaluation(Predicates.transform(policy.getTarget().getFromResourcePred(),
                                                              new PartialEvaluator(req)),
                                         fromContentAnalysisStrings,
                                         fromResource);
            }

        }

        // Whew
        if (fromContentAnalysisStrings.size() > 0) {
            performContentAnalysisForSingleResource(req.getRequestId(), fromResource, fromContentAnalysisStrings); 
        }

        if (toContentAnalysisStrings.size() > 0) {
            performContentAnalysisForSingleResource(req.getRequestId(), toResource, toContentAnalysisStrings);
        }

        return skippedContentPolicies;
    }

    /*
     * Construct a list of the content analysis expressions for which
     * we do not already know the answer.  The resource's content
     * attribute contains the content analysis results.
     *
     * If the string "REG:ccn>=5" appears in the attribute value
     * then there is a match.  If "REG#:ccn>=5" appears then isn't
     * a match.  If neither appear then the content check has not
     * been made and we should search.
     */
    private void filterContentAnalysis(List<String> expressions, Set<String> results, IMResource resource) {
        assert resource != null;

        IEvalValue content = resource.getAttribute(SpecAttribute.CONTENT_NAME);
        if (content == null || content == IEvalValue.NULL) {
            results.addAll(expressions);
            return;
        }

        String strContent = (String)content.getValue();
        for (String e : expressions) {
            if (!StringUtils.isMatch(e, strContent) &&
                !StringUtils.isMatch(invertContentAnalysisExpression(e), strContent)) {
                results.add(e);
            }
        }
    }
    
    private String invertContentAnalysisExpression(String expression) {
        if (expression.startsWith("*REG:")) {
            return "*REG#:" + expression.substring(5);
        } else if (expression.startsWith("*KEY:")) {
            return "*KEY#:" + expression.substring(5);
        } else {
            throw new IllegalArgumentException("Expecting either KEY: or REG: in " + expression);
        }
    }

    private String[] buildContentAnalysisArray(Set<String> expressions) {
        List<String> results = new ArrayList<String>();
        
        for (String expr : expressions) {
            splitContentAnalysis(expr, results);
        }

        return results.toArray(new String[results.size()]);
    }

    /**
     * Split a content analysis expression into four parts.  Currently
     * we assume that the operator is >=
     *
     * *REG:ccn>=5;*   =>  REG:ccn
     *                     <regexp associated with ccn>
     *                     >=
     *                     5
     *
     * *KEY:a b>=5;*   =>  KEY:
     *                     a b
     *                     >=
     *                     5
     */
    private void splitContentAnalysis(String contentAnalysisExpression, List<String> accumulate) {
        
        if (contentAnalysisExpression == null) {
            throw new NullPointerException("contentAnalysisExpression is null");
        }
    
        int index = contentAnalysisExpression.lastIndexOf(">=");
        
        if (index < 6 || index+3 >= contentAnalysisExpression.length()-1 ) {
            throw new IllegalArgumentException("Unable to parse " + contentAnalysisExpression);
        }
        
        // Skip leading *
        String defn = contentAnalysisExpression.substring(1, index);

        boolean skip = false;

        if (defn.startsWith("REG:")) {
            String regexp = regularExpressionInfoManager.getRegularExpressionByName(defn.substring(4));

            // If we can't find a regular expression definition then we should skip this whole clause
            if (regexp != null) {
                accumulate.add(defn);
                accumulate.add(regexp);
            } else {
                skip = true;
            }
        } else {
            accumulate.add("KEY:");
            accumulate.add(defn.substring(4));
        }

        if (!skip) {
            accumulate.add(">=");
            accumulate.add(contentAnalysisExpression.substring(index+2, contentAnalysisExpression.length()-2));
        }
    }

    private String convertResultsToString(String[] contentAnalysisInput, int[] contentAnalysisResults) {
        String[] results = new String[contentAnalysisResults.length];

        for (int i = 0; i != contentAnalysisResults.length; i++) {
            if (contentAnalysisResults[i] != 0) {
                results[i] = makeMatch(contentAnalysisInput[i*4], contentAnalysisInput[i*4+1], contentAnalysisInput[i*4+2], contentAnalysisInput[i*4+3]);
            } else {
                results[i] = makeFailedMatch(contentAnalysisInput[i*4], contentAnalysisInput[i*4+1], contentAnalysisInput[i*4+2], contentAnalysisInput[i*4+3]);
            }
        }

        return StringUtils.join(results, ";") + ";";
    }

    private String makeMatch(String desc, String re, String op, String count) {
        if (desc.startsWith("REG:")) {
            return desc + op + count;
        } else {
            return desc + re + op + count;
        }
    }

    private String makeFailedMatch(String desc, String re, String op, String count) {
        if (desc.startsWith("REG:")) {
            return "REG#:" + desc.substring(4) + op + count;
        } else {
            return "KEY#:" + re + op + count;
        }
    }

    private Log getLog() {
        return log;
    }
}

