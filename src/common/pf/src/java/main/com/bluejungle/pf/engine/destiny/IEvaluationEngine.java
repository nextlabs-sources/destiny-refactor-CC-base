package com.bluejungle.pf.engine.destiny;

/*
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 *
 * @author sasha, sergey
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/bluejungle/pf/engine/destiny/IEvaluationEngine.java#1 $
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;

/**
 * This interface defines the contract of the evaluation engine.
 */
public interface IEvaluationEngine {

    /**
     * This constant is used as a sentinel object when the real engine
     * has not finished initializing.
     */
    final IEvaluationEngine SKIP_EVERYTHING = new IEvaluationEngine() {
        public EvaluationResult evaluate( EvaluationRequest request ) {
            return new EvaluationResult(request, EvaluationResult.DONT_CARE);
        }
        
        public List<IDEffectType> evaluationDigest(EvaluationRequest request) {
            // We have no policies, so return an empty list
            return Collections.emptyList();
        }

        public boolean isApplicationIgnorable( IDSubject app ) {
            return false;
        }
    };

    /**
     * Evaluates all policies deployed on this agent to determine whether or not
     * the specified action should be allowed.
     *
     * @param request Evaluation request
     * @return result of the evaluation
     */
    EvaluationResult evaluate(EvaluationRequest request);

    /**
     * Evaluates all policies deployed on this agent and returns allow/deny results for each
     * one
     *
     * @param request Evaluation request
     * @return result of each policy evaluation
     */
    List<IDEffectType> evaluationDigest(EvaluationRequest request);

    /**
     * Evaluates a named application component against the given application.
     * @param  app an IDSubject representing the application.
     * @return true if the specified component exists and its predicate evaluates to true;
     * false otherwise.
     */
    boolean isApplicationIgnorable( IDSubject app );
}
