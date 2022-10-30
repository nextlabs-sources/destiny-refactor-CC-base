package com.bluejungle.destiny.agent.controlmanager.test;

/*
 * Created on Dec 21, 2004
 * All sources, binaries and HTML pages (C) Copyright 2007
 * by NextLabs Inc., San Mateo CA, Ownership remains with
 * NextLabs Inc, All rights reserved worldwide.
 */

import java.util.ArrayList;
import java.util.List;

import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.IHasComponentInfo;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;
import com.bluejungle.pf.engine.destiny.EvaluationResult;
import com.bluejungle.pf.engine.destiny.IEvaluationEngine;

/**
 * @author fuad
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/test/TestEvaluationEngine.java#1 $:
 */

public class TestEvaluationEngine implements IEvaluationEngine, IHasComponentInfo<TestEvaluationEngine> {
 
    public static final String EDIT = "EDIT";
    public static final String CREATENEW = "CREATENEW";
    public static final String COPY_TO_CLIPBOARD = "COPY_TO_CLIPBOARD";

    private static final ComponentInfo<TestEvaluationEngine> COMP_INFO = 
    	new ComponentInfo<TestEvaluationEngine>(
    		IEvaluationEngine.class.getName(), 
    		TestEvaluationEngine.class, 
    		IEvaluationEngine.class, 
    		LifestyleType.SINGLETON_TYPE);

    public ComponentInfo<TestEvaluationEngine> getComponentInfo() {
        return COMP_INFO;
    }

    /**
     * @see IEvaluationEngine#evaluate(EvaluationRequest request)
     */
    public EvaluationResult evaluate(EvaluationRequest request) {
        if (request.getAction().getName() != null && (""+request.getFromResource().getAttribute("name")).indexOf("abc.txt") != -1) {
            if (request.getAction().getName().equals(IDAction.EDIT_NAME) || request.getAction().getName().equals(IDAction.CREATE_NEW_NAME) || request.getAction().getName().equals(IDAction.COPY_PASTE_NAME)) {
                return new EvaluationResult(request, EvaluationResult.DENY);
            }
        }

        return new EvaluationResult(request, EvaluationResult.ALLOW);
    }

    /**
     * @see IEvaluationEngine#evaluationDigest(EvaluationRequest)
     */
    public List<IDEffectType> evaluationDigest(EvaluationRequest request) {
        return null;
    }

    /**
     * @see IEvaluationEngine#isApplicationIgnorable(IDSubject)
     */
    public boolean isApplicationIgnorable( IDSubject app ) {
        return false;
    }

}
