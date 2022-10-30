/*
 * Created on May 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.agent.controlmanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.bluejungle.destiny.agent.ipc.IKernelRequestTask;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.HashMapConfiguration;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.threading.ITask;
import com.bluejungle.framework.threading.IWorker;
import com.bluejungle.pf.domain.destiny.resource.AgentResourceManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.engine.destiny.EvaluationResult;
import com.bluejungle.pf.engine.destiny.IAgentPolicyContainer;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/personal/ihanen/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/CMKernelRequestHandler.java#1 $
 */

public class CMKernelRequestHandler implements IWorker, IInitializable, IManagerEnabled, ILogEnabled {

    private static final int ALLOW = 2;
    private static final int DENY = 1;

    private IPolicyEvaluator policyEvaluator;
    private Log log;
    private IComponentManager manager;

    /**
     * @see com.bluejungle.framework.threading.IWorker#doWork(com.bluejungle.framework.threading.ITask)
     */
    public void doWork(ITask task) {
        final IKernelRequestTask kernelTask = (IKernelRequestTask) task;
        final String[] inputs = kernelTask.getInputParams();

        long inTime = System.currentTimeMillis();

        /*
         * int resultCode = ALLOW; if (fileName != null &&
         * fileName.endsWith("nodelete.txt")) { if (action != null &&
         * "DELETE".equals(action)) { resultCode = DENY; } }
         */
        final List<String> results = new ArrayList<String>(3);

        Object[] newStyleArray = new Object[14];

        // This is absolutely silly.  The input from user mode does not have 1st field (methodname)
        // which stripped out by the RequestHandlerBase while the input from the kernel mode
        // was untouched.  And everywhere else is expected the 1st field is stripped.  This
        // is just bad programming, but we have to live with it...
        ArrayList<String> shiftedArray = new ArrayList<String>();
        for (int i = 1; i < inputs.length; i++) {
            shiftedArray.add (inputs[i]);
        }
        
        APIConverter.convertOldToNew(newStyleArray, shiftedArray);

        boolean evaluationSuccess = this.policyEvaluator.queryDecisionEngine(newStyleArray, results);
        int resultCode = ALLOW;
        if (evaluationSuccess) {
            //Read the evaluation result
            if (EvaluationResult.DENY.equals(results.get(0))) {
                resultCode = DENY;
            }
        }

        if (System.currentTimeMillis() - inTime > 5 * 1000) {
            getLog().error("queryDecisionEngine took over 5 seconds");
        }

        kernelTask.getOSWrapper().setKernelPolicyResponse(kernelTask.getHandle(), kernelTask.getRequestNumber(), resultCode, 2);
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return this.manager;
    }

    /**
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        //Initializes the policy evaluator
        HashMapConfiguration policyEvalConfig = new HashMapConfiguration();
        policyEvalConfig.setProperty(IPolicyEvaluator.AGENT_POLICY_CONTAINER_CONFIG_PARAM, getManager().getComponent(IAgentPolicyContainer.COMP_INFO));
        policyEvalConfig.setProperty(IPolicyEvaluator.CONTROL_MANAGER_CONFIG_PARAM, (IControlManager)getManager().getComponent(IControlManager.NAME));
        policyEvalConfig.setProperty(IPolicyEvaluator.RESOURCE_MANAGER_CONFIG_PARAM, getManager().getComponent(AgentResourceManager.COMP_INFO));
        policyEvalConfig.setProperty(IPolicyEvaluator.SUBJECT_MANAGER_CONFIG_PARAM, getManager().getComponent(IDSubjectManager.COMP_INFO));
        ComponentInfo<PolicyEvaluatorImpl> evaluatorCompInfo = new ComponentInfo<PolicyEvaluatorImpl>(
        		"policyEvaluator", 
        		PolicyEvaluatorImpl.class, 
        		IPolicyEvaluator.class, 
        		LifestyleType.SINGLETON_TYPE, 
        		policyEvalConfig);
        this.policyEvaluator = getManager().getComponent(evaluatorCompInfo);
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#setManager(com.bluejungle.framework.comp.IComponentManager)
     */
    public void setManager(IComponentManager manager) {
        this.manager = manager;
    }
}
