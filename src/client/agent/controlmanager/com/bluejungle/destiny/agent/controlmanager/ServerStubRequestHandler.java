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

import com.bluejungle.destiny.agent.controlmanager.ServerStubRequestTaskImpl;
import com.bluejungle.destiny.agent.controlmanager.ServiceRequestTaskImpl;
import com.bluejungle.destiny.agent.pdpapi.IPDPEnforcement;
import com.bluejungle.destiny.agent.pdpapi.PDPEnforcement;
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
import com.bluejungle.pf.domain.destiny.serviceprovider.IServiceProviderManager;
import com.bluejungle.pf.domain.destiny.serviceprovider.ServiceProviderManager;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.engine.destiny.EvaluationResult;
import com.bluejungle.pf.engine.destiny.IAgentPolicyContainer;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/personal/ihanen/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/CMKernelRequestHandler.java#1 $
 */

public class ServerStubRequestHandler implements IWorker, IInitializable, IManagerEnabled, ILogEnabled {
    private static final int DENY = 0;
    private static final int ALLOW = 1;
    private static final int DONTCARE = 2;
    private static final int ERROR = 3;

    private IServiceProviderManager serviceProviderManager;
    private IPolicyEvaluator policyEvaluator;
    private Log log;
    private IComponentManager manager;

    /**
     * @see com.bluejungle.framework.threading.IWorker#doWork(com.bluejungle.framework.threading.ITask)
     */
    public void doWork(ITask task) {
        if (task instanceof ServerStubRequestTaskImpl) {
            final ServerStubRequestTaskImpl kernelTask = (ServerStubRequestTaskImpl) task;
            final Object[] inputs = kernelTask.getInputParams();
            
            //Call queryDecisionEngine to get the results
            final List<String> results = new ArrayList<String>(2);
            boolean evaluationSuccess = policyEvaluator.queryDecisionEngine(inputs, results);
            int resultCode = ALLOW;
            if (evaluationSuccess) {
                //Read the evaluation result
                if (EvaluationResult.DENY.equals(results.get(0))) {
                    resultCode = DENY;
                } else if (EvaluationResult.DONT_CARE.equals(results.get(0))) {
                    resultCode = DONTCARE;
                } else if(EvaluationResult.ERROR.equals(results.get(0))) {
                	resultCode = ERROR;
                }
                
            }
            
            if(results.size() > 1) {   
                //Read resulted obligations at index 1
                Object tmpObject = results.get(1);
                if (tmpObject != null) {
                    String obligations [] = (String []) tmpObject;
                    kernelTask.getResponseHandler().respond(kernelTask, resultCode, obligations);
                } else {
                    String attrs[]={};
                    kernelTask.getResponseHandler().respond(kernelTask, resultCode, attrs);
                }
            } else {
                String attrs[]={};
                kernelTask.getResponseHandler().respond(kernelTask, resultCode, attrs);
                // kernelTask.getOSWrapper().SendPolicyResponse(kernelTask.getHandle(), kernelTask.getRequestNumber(), resultCode, attrs);        
            }
        } else if (task instanceof PDPRequestTaskImpl) {
            final PDPRequestTaskImpl pdpTask= (PDPRequestTaskImpl)task;
            final List<String> results = new ArrayList<String>(2);

            boolean evaluationSuccess = policyEvaluator.queryDecisionEngine(pdpTask.getArguments(),
                                                                            pdpTask.getProcessToken(),
                                                                            pdpTask.getLoggingLevel(),
                                                                            pdpTask.getObligationsFlag(),
                                                                            results);

            String result = EvaluationResult.ALLOW;
            if (evaluationSuccess) { 
                result = results.get(0);
            }

            String obligations[] = {};

            if (results.size() > 1 && results.get(1) != null) {
                obligations = (String[])((Object)results.get(1));
            }
            
            IPDPEnforcement enforcementResult = new PDPEnforcement(result, obligations);
            pdpTask.getCallback().callback(enforcementResult);
        } else if (task instanceof ServiceRequestTaskImpl) {
            final ServiceRequestTaskImpl serviceTask = (ServiceRequestTaskImpl)task;

            final Object inputs[] = serviceTask.getInputParams();
            String reqId = (String)inputs[0];
            String command = (String)inputs[1];

            ArrayList<Object> inputArgs = new ArrayList<Object>();
            ArrayList<Object> outputArgs = new ArrayList<Object>();
        
            if (command.equals("SERVICEINVOKE")) {
                // Add everything else
                for (int i = 2; i < inputs.length; i++) {
                    inputArgs.add(inputs[i]);
                }
                
                serviceProviderManager.invoke(inputArgs, outputArgs);
            }

            serviceTask.getResponseHandler().respond(serviceTask, reqId, outputArgs.toArray(new Object[outputArgs.size()]));
        }
    }

    /**
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see com.bluejungle.framework.comp.IManagerEnabled#getManager()
     */
    public IComponentManager getManager() {
        return manager;
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
        policyEvaluator = getManager().getComponent(evaluatorCompInfo);

        serviceProviderManager = (IServiceProviderManager) getManager().getComponent(ServiceProviderManager.COMP_INFO);
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
