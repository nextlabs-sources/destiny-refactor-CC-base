package com.bluejungle.pf.destiny.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.server.shared.configuration.IDABSComponentConfigurationDO;
import com.bluejungle.destiny.server.shared.configuration.ITrustedDomainsConfigurationDO;
import com.bluejungle.destiny.server.shared.registration.ServerComponentType;
import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.configuration.DestinyConfigurationStoreImpl;
import com.bluejungle.framework.configuration.IDestinyConfigurationStore;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.PredicateConstants;
import com.bluejungle.framework.expressions.Relation;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.MultipartKey;
import com.bluejungle.pf.destiny.lifecycle.DeploymentEntity;
import com.bluejungle.pf.destiny.lifecycle.DeploymentType;
import com.bluejungle.pf.destiny.lifecycle.LifecycleManager;
import com.bluejungle.pf.destiny.policymap.PolicyReferenceResolver;
import com.bluejungle.pf.destiny.policymap.ResolvedPolicyData;
import com.bluejungle.pf.domain.destiny.misc.EffectType;
import com.bluejungle.pf.domain.destiny.misc.IDEffectType;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.Subject;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.engine.destiny.EvaluationEngine;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;


/**
 * This encapsulates all policy search related methods. 
 *
 */
public class PolicySearchManager implements IInitializable, ILogEnabled  {

    private LifecycleManager lm;
    private final IDictionary dictionary;

    private Log log = 
        LogFactory.getLog(PolicySearchManager.class.getName());

    public static final ComponentInfo<PolicySearchManager> COMP_INFO = 
        new ComponentInfo<PolicySearchManager>(
                PolicySearchManager.class,
                LifestyleType.SINGLETON_TYPE);
    
    public PolicySearchManager() {
        IComponentManager manager = 
            ComponentManagerFactory.getComponentManager();
        lm = (LifecycleManager) manager.getComponent(LifecycleManager.COMP_INFO);
        dictionary = manager.getComponent(Dictionary.COMP_INFO );
    }

    /**
     * Find all policies that is applicable for the provided user and host. This search
     * can further be qualified by supplying a string that contains a fragment of policy
     * name. In this case a set of policies is created that contains all policies that have 
     * name containing the fragment.
     * 
     * @param userName unique name of the user.
     * @param hostName unique name of the host. 
     * @param policyNameFragment fragment of policy name. 
     * @throws PolicySearchException
     * @return a list of applicable policies - empty list if none found.
     */
    public List<Long> findApplicablePoliciesFor(
            String userName, String hostName, String policyNameFragment) 
            throws PolicySearchException   {
        if (userName == null && hostName == null) {
            throw new IllegalArgumentException (
            "User name and Host name cannot be null");
        }
        long begin = 0;
        List<Long> applicablePolicies = new ArrayList<Long>();
        String[] agentDomains = null;

        try {
            List<EvaluationRequest> requests = generateRequests(userName, hostName);
            if (requests.size() == 0) {
                if (getLog().isWarnEnabled()) {
                    getLog().warn("Could not find any enrolled user and host  with user name '" +
                            userName + "' and host name '" + hostName + "'. Search will " + 
                            " not yield any results.");
                 }
            }

            if (hostName != null) {
                String[] nameAndDomain = hostName.split("[.]", 2);
                if (nameAndDomain.length == 2) {
                    agentDomains = expandDomainList(nameAndDomain[1]);
                }
            }

            Collection <DeploymentEntity> allDeployedEntities = null;
            Date asOf = new Date();
            allDeployedEntities = 
                lm.getAllDeployedEntities(asOf, DeploymentType.PRODUCTION);

            // NOTE: REVISIT: For now all deployed entities are passed on to the resolver
            // which is responsible for filtering the policies to be considered if the client 
            // uses the policy name fragment. A more optimized approach would be to 
            // filter the policies here and pass the deployment entities for the policies and
            // the dependent components.
            PolicyReferenceResolver polRefResolver = new PolicyReferenceResolver(allDeployedEntities, agentDomains);
            boolean modifyUserPredictateToTrue = userName == null || userName.length() == 0;
            boolean modifyHostPredictateToTrue = hostName == null || hostName.length() == 0;
            boolean modifyAppPredicateToTrue = true; // always true

            if (getLog().isDebugEnabled()) {
                begin = System.currentTimeMillis();
            }
            ResolvedPolicyData resolvedPolicyData =  
                polRefResolver.resolvePolicySubjectData(
                        modifyUserPredictateToTrue, modifyHostPredictateToTrue,
                        modifyAppPredicateToTrue, policyNameFragment);

            if (getLog().isDebugEnabled()) {
                getLog().debug("Time taken to resolve policy data: " + 
                        (System.currentTimeMillis() - begin)  + " ms");
                begin = System.currentTimeMillis();
            }

            IDPolicy[] policies = resolvedPolicyData.getParsedPolicies();
            if (policies == null) {
                throw new PolicySearchException(
                        "Could not find any resolved policy that is deployed", null);
            }
            
            PolicySearchResolver searchResolver = new PolicySearchResolver(resolvedPolicyData);
            EvaluationEngine evalEngine = new EvaluationEngine(searchResolver);
              
            for (EvaluationRequest thisReq : requests) {
                List<IDEffectType> effects = evalEngine.evaluationDigest(thisReq);
                
                int i = 0;
                
                for (IDEffectType effect : effects) {
                    if (effect == EffectType.DONT_CARE) {
                        if (getLog().isInfoEnabled()) {
                            getLog().info(generateLogString(thisReq.getUser(), 
                                                            thisReq.getHost(),  policies[i].getName(),  "not applicable"));
                        }
                    } else {
                        applicablePolicies.add(policies[i].getId());
                        if (getLog().isInfoEnabled()) {
                            getLog().info(generateLogString(thisReq.getUser(), 
                                                            thisReq.getHost(),  policies[i].getName(),  " applicable"));
                        }
                    }
                    
                    i++;
                }
            }
            if (getLog().isDebugEnabled()) {
                getLog().debug("Time taken to evaluate: " + policies.length + " policies: " +
                        (System.currentTimeMillis() - begin) + " ms");
            }
        } catch (Exception ex) {
            if (ex instanceof PolicySearchException) throw (PolicySearchException) ex;
            if (getLog().isErrorEnabled()) {
                log.error(ex);
            }
            throw new PolicySearchException(
                    "Exception while searching for applicable policies for user:' "
                    + userName + "' and host: '" + hostName +
                    "' and policy name fragment: '" + policyNameFragment +"'", ex);
        }
        return applicablePolicies;
    }

    private List<EvaluationRequest> generateRequests(
            String userName, String hostName) throws Exception {
        List<EvaluationRequest> evalRequests = new  ArrayList<EvaluationRequest>();

        Date now = new Date();
        
        // user name can resolve to multiple users and hence multiple eval requests
        if (userName != null) {
            String[] firstLastNames = null;
            try {
                firstLastNames = userName.split(" ", 2);
            } catch (PatternSyntaxException ex) {
                getLog().error("Error when trying to deduce first name and last name " + 
                        " from supplied userName string: " + userName + 
                ". The user name will be considered as unique name for search.");
            }
            if (firstLastNames != null && firstLastNames.length == 2) {
                IElementType userType = 
                    dictionary.getType(ElementTypeEnumType.USER.getName());
                IElementField firstNameRef = userType.getField(
                        UserReservedFieldEnumType.FIRST_NAME.getName());
                IElementField lastNameRef = userType.getField(
                        UserReservedFieldEnumType.LAST_NAME.getName());

                List<IPredicate> conditions = new ArrayList<IPredicate>();
                conditions.add(new Relation(RelationOp.EQUALS, firstNameRef, 
                        Constant.build(firstLastNames[0])));
                conditions.add(new Relation(RelationOp.EQUALS, lastNameRef, 
                        Constant.build(firstLastNames[1])));
                IPredicate searchPred = 
                    new CompositePredicate(BooleanOp.AND, conditions);

                IDictionaryIterator<IMElement> result = null;
                try {
                    result = dictionary.query(searchPred, now, null, null);
                    while (result.hasNext()) {
                        IMElement user = result.next();
                        evalRequests.add(createRequestWithUserSubject(user));
                    }
                } finally {
                    if (result != null) result.close();
                }
                
                if (evalRequests.size() == 0) {
                    // nothing was found - will not use user name in search
                    logNoUsersFound(userName);
                }
            }  else {
                IMElement user = dictionary.getElement(userName, now);
                if (user != null) {
                    evalRequests.add(createRequestWithUserSubject(user));
                } else {
                    logNoUsersFound(userName);
                }
            }
        }

        if (hostName != null) {
            IMElement host =  dictionary.getElement(hostName, now);
            if (host != null) {
                IDSubject subjHost = new Subject(host.getUniqueName(),
                        host.getUniqueName(),  host.getUniqueName(),
                        host.getInternalKey(), SubjectType.HOST);
                if (evalRequests.size() == 0) { // in case no users were found/supplied
                    evalRequests.add(new EvaluationRequest());
                }
                for (EvaluationRequest thisReq : evalRequests) {
                    thisReq.setHost(subjHost);
                }
            } else {
                if (getLog().isInfoEnabled()) { 
                    getLog().info("Could not obtain enrolled element for host: '" + 
                            hostName + "'. Search will not contain host.");
                }
            }
        }
        return  evalRequests;
    }

    private void logNoUsersFound(String userName) {
        if (getLog().isInfoEnabled()) { 
            getLog().info("Could not obtain enrolled element for user: '" + 
                    userName + "'. Search will not contain user.");
        }
    }

    private EvaluationRequest createRequestWithUserSubject(IMElement user) {

        IDSubject subjUser = new Subject(user.getUniqueName(),
                user.getUniqueName(),  user.getUniqueName(),
                user.getInternalKey(), SubjectType.USER);
        EvaluationRequest request = new EvaluationRequest();
        request.setUser(subjUser);

        return request;
    }

    private String generateLogString(
            IDSubject user, IDSubject host, String policyName, String variable) {
        String userData = "";
        if (user != null) {
            StringBuilder sb = new StringBuilder(user.getUniqueName());
            userData = sb.append("' with ID: '").append(user.getId()).toString();
        }
        String hostData = "";
        if (host != null) {
            StringBuilder sb = new StringBuilder(host.getUniqueName());
            hostData = sb.append("' with ID: '").append(host.getId()).toString();
        }
        StringBuilder sb = new StringBuilder("This policy: '").append(policyName);
        sb.append("' is ").append(variable).append(" for user: '").append(userData);
        sb.append("' and host: '").append(hostData).append("'.");
        
        return sb.toString();
    }

    public void init() {
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        if (log != null)
            this.log = log;
    }

    /**
     * Given an agent domain, returns an array of all domains to which this agent
     * has access according to the trust relations set up for the agent's domain.
     *
     * @param agentDomain the domain of the agent.
     * @return an array of all trusted domains for the agent (including its own),
     * or an empty array if the agent's domain is null or empty.
     */
    private String[] expandDomainList(String agentDomain) {
        if (agentDomain == null || agentDomain.length() == 0) {
            getLog().warn("Agent domain is null - using an empty domain list.");
            return new String[0];
        }
        SortedSet<String> res = new TreeSet<String>();
        res.add(agentDomain);
        try {
            IComponentManager manager = 
                ComponentManagerFactory.getComponentManager();
            IDestinyConfigurationStore confStore = (IDestinyConfigurationStore) 
            manager.getComponent(DestinyConfigurationStoreImpl.COMP_INFO);
            IDABSComponentConfigurationDO dabsConfig =
                (IDABSComponentConfigurationDO) 
                confStore.retrieveComponentConfiguration(
                        ServerComponentType.DABS.getName());
            if (dabsConfig != null) {
                ITrustedDomainsConfigurationDO tdConfig = 
                    dabsConfig.getTrustedDomainsConfiguration();
                if (tdConfig != null) {
                    String[] trustedDomains = tdConfig.getTrustedDomains();
                    if (trustedDomains != null) {
                        for (int i = 0; i != trustedDomains.length; i++) {
                            if (trustedDomains[i] != null) {
                                String[] domains = trustedDomains[i].split(",");
                                for (int j = 0; j != domains.length; j++) {
                                    domains[j] = domains[j].trim();
                                }
                                if (Arrays.asList(domains).contains(agentDomain)) {
                                    getLog().info("Domain '" + agentDomain + 
                                            "' is in the list of mutual trust: " + trustedDomains[i]);
                                    for (int j = 0; j != domains.length; j++) {
                                        res.add(domains[j]);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    getLog().warn("Unable to get trusted domain configuration.");
                }
            } else {
                getLog().warn("Unable to get DABS configuration - trusted domains will not be configured.");
            }
        } catch (Exception ignored) {
            getLog().warn("Exception getting a list of trusted domains", ignored);
        }
        if ( getLog().isInfoEnabled()) {
            StringBuffer msg = new StringBuffer("Effective list of domains: ");
            boolean first = true;
            for (String domain : res) {
                if (!first) {
                    msg.append(", ");
                } else {
                    first = false;
                }
                msg.append(domain);
                if (agentDomain.equals(domain)) {
                    msg.append("(REQUESTED)");
                }
            }
            getLog().info(msg);
        }
        return res.toArray(new String[res.size()]);
    }
    
    public  List<String> getAllEnrolledNamesByType(ElementTypeEnumType type)
    throws Exception {
        if (type == null) 
            throw new IllegalArgumentException("Required parameter is missing");
        IDictionaryIterator<IMElement> result = null;
        List<String> allItemNames = new ArrayList<String>();
        
        try {
            IElementType  elementType = dictionary.getType(type.getName());
            IPredicate allHosts = new CompositePredicate(
                    BooleanOp.AND,  Arrays.asList(dictionary.condition(elementType), 
                            PredicateConstants.TRUE ));
            result = dictionary.query(allHosts, new Date(), null, null);
            
            while (result.hasNext()) {
                String itemName = result.next().getUniqueName();
                if (itemName != null)
                    allItemNames.add(itemName);
            }
        } finally {
            if (result != null) result.close();
        }
        return allItemNames;
    }
}
