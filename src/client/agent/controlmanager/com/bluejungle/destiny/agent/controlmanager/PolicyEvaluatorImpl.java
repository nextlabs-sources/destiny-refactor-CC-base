package com.bluejungle.destiny.agent.controlmanager;

/*
 * Created on May 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.agent.activityjournal.ActivityJournal;
import com.bluejungle.destiny.agent.activityjournal.LogIdGenerator;
import com.bluejungle.destiny.agent.ipc.OSType;
import com.bluejungle.destiny.agent.pdpapi.PDPSDK;
import com.bluejungle.destiny.agent.profile.ProfileManager;
import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.agenttype.AgentTypeEnumType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;
import com.bluejungle.framework.comp.IManagerEnabled;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IMultivalue;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.SerializationUtils;
import com.bluejungle.framework.utils.StringUtils;
import com.bluejungle.pf.domain.destiny.common.SpecAttribute;
import com.bluejungle.pf.domain.destiny.obligation.AgentCustomObligation;
import com.bluejungle.pf.domain.destiny.obligation.AgentDisplayObligation;
import com.bluejungle.pf.domain.destiny.obligation.AgentLogObligation;
import com.bluejungle.pf.domain.destiny.obligation.AgentNotifyObligation;
import com.bluejungle.pf.domain.destiny.obligation.DObligation;
import com.bluejungle.pf.domain.destiny.obligation.IDObligation;
import com.bluejungle.pf.domain.destiny.obligation.ILoggingLevel;
import com.bluejungle.pf.domain.destiny.resource.ResourceAttribute;
import com.bluejungle.pf.domain.destiny.subject.AgentSubject;
import com.bluejungle.pf.domain.destiny.subject.IDSubject;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectManager;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.evaluation.IEvaluationRequest;
import com.bluejungle.pf.domain.epicenter.resource.IMResource;
import com.bluejungle.pf.domain.epicenter.resource.IResource;
import com.bluejungle.pf.domain.epicenter.resource.Resource;
import com.bluejungle.pf.destiny.services.UNCUtil;
import com.bluejungle.pf.engine.destiny.ClientInformation;
import com.bluejungle.pf.engine.destiny.EvaluationRequest;
import com.bluejungle.pf.engine.destiny.EvaluationResult;
import com.bluejungle.pf.engine.destiny.IAgentPolicyContainer;
import com.bluejungle.pf.engine.destiny.IBundleVault;
import com.nextlabs.domain.log.PolicyActivityInfoV5;
import com.nextlabs.domain.log.PolicyActivityLogEntryV5;

/**
 * @author ihanen
 * @version $Id:
 *          //depot/personal/ihanen/main/Destiny/main/src/client/agent/controlmanager/com/bluejungle/destiny/agent/controlmanager/PolicyEvaluatorImpl.java#1 $
 */

public class PolicyEvaluatorImpl implements IPolicyEvaluator, ILogEnabled, IManagerEnabled, IInitializable, IDisposable, IConfigurable {

    /**
     * Sid of the system user
     */
    private static final String LOCAL_SYSTEM_USER = "S-1-5-18";

    /**
     * 127.0.0.1 in network byte order
     */
    private static final long LOCALHOST_BYTE_ADDRESS = 0x7f000001;

    /**
     * Caching parameters
     */
    private static final String UNRESOLVED_DNS_CACHE_SUFFIX = "UnresolvedDNSCache";

    /**
     * Public bin folder name for the control module
     */
    private static final String PUBLIC_BIN_FOLDER = "public_bin";

    /**
     * Icon file.  Not tamper proofed
     */
    private static final String ICON_FILE_NAME = "app-icon.ico";

    /**
     * Ignored file
     */
    private static final String IGNORED_RESOURCES_FILE = "config/folder.info";

    /**
     * Token for policy expression
     */
    private static final String MY_DOCUMENTS_TOKEN = "[mydocuments]";
    private static final String MY_DOCUMENTS_PATTERN = "\\[mydocuments\\]";
    private static final String MY_DESKTOP_TOKEN = "[mydesktop]";
    private static final String MY_DESKTOP_PATTERN = "\\[mydesktop\\]";
    private static final String NO_ATTACHMENT = "[no attachment]";

    /**
     * These are used to queue the custom obligation executions and running these
     * on a separate thread.
     */
    private BlockingQueue<IAsyncTask> asyncTaskQ;
    private AsyncTaskExecutor asyncTaskExecutor;
    private Thread asyncTaskExecutorThread;

    private static final String DONT_CARE_RESPONSE_ACCEPTABLE_KEY = "nextlabs.dont.care.acceptable";
    private static final String ERROR_RESPONSE_ACCEPTABLE_KEY = "nextlabs.error.result.acceptable";
    private boolean dontCareAcceptable = false;  // Can be overridden by the query
    private boolean errorAcceptable = false;  // Can be overridden by the query
    
    private class ResultParamArrayGen {
        private String evaluationResult = EvaluationResult.ALLOW;
        private List<String> delegatedObligations = new ArrayList<String>();
        int delegatedObligationCount = 0;

        ResultParamArrayGen() {
        }

        void addAttribute(String key, String value) {
            delegatedObligations.add(key);
            delegatedObligations.add(value);
        }

        void addOldStyleDelegatedObligation(String obligationName, String policyName, String param) {
            ++delegatedObligationCount;
            addAttribute("CE_ATTR_OBLIGATION_NAME:" + delegatedObligationCount, obligationName);
            addAttribute("CE_ATTR_OBLIGATION_POLICY:" + delegatedObligationCount, policyName);
            // This is what SPE is expecting
            addAttribute("CE_ATTR_OBLIGATION_VALUE:" + delegatedObligationCount, param);
            // But we also need these fields so that new enforcers don't freak out
            addAttribute("CE_ATTR_OBLIGATION_VALUE:" + delegatedObligationCount + ":1", param);
            addAttribute("CE_ATTR_OBLIGATION_NUMVALUES:" + delegatedObligationCount, "1");
        }

        void addDelegatedObligation(String obligationName, String policyName, List<String> optionalParams) {
            ++delegatedObligationCount;
            addAttribute("CE_ATTR_OBLIGATION_NAME:" + delegatedObligationCount, obligationName);
            addAttribute("CE_ATTR_OBLIGATION_POLICY:" + delegatedObligationCount, policyName);

            if (optionalParams != null) {
                int i = 0;
                for (String param : optionalParams) {
                    ++i;
                    addAttribute("CE_ATTR_OBLIGATION_VALUE:" + delegatedObligationCount + ":" + i, param);
                }

                addAttribute("CE_ATTR_OBLIGATION_NUMVALUES:" + delegatedObligationCount, Integer.toString(i));
            }
        }
        
        void setEvaluationResult(String res) {
            evaluationResult = res;
        }

        void buildReturnArray(List resultParamArray) {
            resultParamArray.clear();
            resultParamArray.add(evaluationResult);

            if (delegatedObligationCount > 0) {
                // These must be added at the beginning
                delegatedObligations.add(0, "CE_ATTR_OBLIGATION_COUNT");
                delegatedObligations.add(1, Integer.toString(delegatedObligationCount));
            }

            resultParamArray.add(delegatedObligations.toArray(new String[delegatedObligations.size()]));
        }
    }

    interface IDObligationExecutor {
        public void preamble(IDObligation obl, EvaluationResult res, ObligationContext obligationContext);
        public void execute(IDObligation obl, EvaluationResult res, ResultParamArrayGen returnParams, ObligationContext obligationContext);
    }
    
    private Map<String, IDObligationExecutor> obligationMap = null;

    private IDObligationExecutor NotifyExecutor = new IDObligationExecutor() {

            public void preamble(IDObligation obl, EvaluationResult res, ObligationContext obligationContext) {
                if (res.getPAInfo().getLevel() == ILoggingLevel.LOG_LEVEL_USER) {
                    obligationContext.addDescription((AgentNotifyObligation)obl, "Email");
                }
            }

            public void execute(IDObligation obl, EvaluationResult res, ResultParamArrayGen returnParams, ObligationContext obligationContext) {
                AgentNotifyObligation realObl = (AgentNotifyObligation)obl;

                if (res.getPAInfo().getLevel() == ILoggingLevel.LOG_LEVEL_USER) {
                    String body = realObl.createBody(res.getPAInfo());
                    String subject = "Notification";

                    realObl.getExecutor().notify(null, realObl.getEmailAddresses(), subject, body);
                }
            }
        };

    private IDObligationExecutor DisplayExecutor = new IDObligationExecutor() {
        private boolean appNameMatches(String appName, String nameToMatch) {
            return appName.equals(nameToMatch) || appName.endsWith("/" + nameToMatch) || appName.endsWith("\\" + nameToMatch);
        }
        
        private boolean doDisplay(EvaluationResult res) {
            // Only do display notification for USER events
            if (res.getPAInfo().getLevel() != ILoggingLevel.LOG_LEVEL_USER) {
                return false;
            }

            String appName = res.getPAInfo().getApplicationName().toLowerCase();
            String resName = res.getPAInfo().getFromResourceInfo().getName();

            // If the action is OPEN by either explorer or acroread32info then we don't want
            // to do display (except when the file is http:)
            return (!(ActionEnumType.ACTION_OPEN.getName().equals(res.getPAInfo().getAction()) && appName != null &&
                      !resName.startsWith("http:") &&
                      (appNameMatches(appName, "explorer.exe") || appNameMatches(appName, "acrord32info.exe"))));
        }

        
        public void preamble(IDObligation obl, EvaluationResult res, ObligationContext obligationContext) {
            if (doDisplay(res)) {
                obligationContext.addDescription((AgentDisplayObligation)obl, "Display: " + ((AgentDisplayObligation)obl).getMessage());
            }
        }

        public void execute(IDObligation obl, final EvaluationResult res, ResultParamArrayGen returnParams, ObligationContext obligationContext) {
            final AgentDisplayObligation realObl = (AgentDisplayObligation)obl;
                
            if (doDisplay(res)) {
                asyncTaskQ.add(new IAsyncTask() {
                    public void execute() {
                        getControlManager().notifyUser(res.getEvaluationRequest().getLoggedInUser().getUid(),
                                                       Calendar.getInstance().getTime().toString(),
                                                       res.getPAInfo().getAction(),
                                                       res.getEffectName(),
                                                       res.getPAInfo().getFromResourceInfo().getName(),
                                                       realObl.getMessage());
                    }
                });

                returnParams.addOldStyleDelegatedObligation("CE::NOTIFY", realObl.getPolicy().getName(), realObl.getMessage());
            }
        }
    };

    private IDObligationExecutor CustomExecutor = new IDObligationExecutor() {
        private String wrap(String s) {
            return "\"" + s + "\"";
        }
            
        private String trimResourceName(String name) {
            if (name.startsWith("file:///")) {
                return wrap(name.substring(8));
            } else if (name.startsWith("file:")) {
                    return wrap(name.substring(5));
                }
                return wrap(name);
            }

            public String convertArg(String argument, AgentCustomObligation obl, EvaluationResult res, ObligationContext obligationContext) {
                if (argument == null || argument.equals("") || argument.charAt(0) != '$') {
                    return argument;
                }

                // Use a hash table for matching?  Probably takes longer to set up than we
                // are spending here
                if (argument.equals("$CETimestamp")) {
                    return Long.toString(res.getEvaluationRequest().getTimestamp());
                } else if (argument.equals("$CEUser")) {
                    return wrap(res.getEvaluationRequest().getUserName());
                } else if (argument.equals("$CEComputer")) {
                    return wrap(res.getPAInfo().getHostName());
                } else if (argument.equals("$CEApplication")) {
                    return wrap((String)(res.getEvaluationRequest().getApplication().getAttribute("name").getValue()));
                } else if (argument.equals("$CEAction")) {
                    return wrap(res.getEvaluationRequest().getAction().getName());
                } else if (argument.equals("$CEPolicy")) {
                return wrap(obl.getPolicy().getName());
                } else if (argument.equals("$CEIPAddress")) {
                    return res.getEvaluationRequest().getHostIPAddress();
                } else if (argument.equals("$CESource")) {
                    return trimResourceName((String)res.getEvaluationRequest().getFromResource().getIdentifier());
                } else if (argument.equals("$CERecipient") ||
                           argument.equals("$CERecipients")) {
                    String[] recipients = res.getPAInfo().getAttributesMap().get("RF").getStrings("Sent to");
                    if (recipients.length == 0) {
                        return "NONE";
                    } else {
                        return StringUtils.join(recipients, ";");
                    }
                } else if (argument.equals("$CEDestination")) {
                    if (res.getEvaluationRequest().getToResource() == null) {
                        return "NONE";
                    } else {
                        return trimResourceName((String)res.getEvaluationRequest().getToResource().getIdentifier());
                    }
                } else if (argument.equals("$CEClients") ||
                           argument.equals("$CEClientIds")) {
                    EvaluationRequest req = res.getEvaluationRequest();
                    ClientInformation[] clients = req.getClientInformationManager().getClientsForUser(req.getUser().getUid());

                    if (clients.length == 0) {
                        return "";
                    } else {
                        Arrays.sort(clients, new Comparator<ClientInformation>() {
                            private final Collator collator = Collator.getInstance();
                            public int compare (ClientInformation clientA, ClientInformation clientB) {
                                return collator.compare(clientA.getShortName(), clientB.getShortName());
                            }
                        });
                
                        String[] clientDetails = new String[clients.length];
                        
                        boolean getShortName = argument.equals("$CEClients");

                        for (int i = 0; i < clients.length; i++) {
                            if (getShortName) {
                                clientDetails[i] = clients[i].getShortName();
                            } else {
                                clientDetails[i] = clients[i].getIdentifier();
                            }
                        }

                        return StringUtils.join(clientDetails, ";");
                    }
                } else if (argument.equals("$CEMatchedRecipients")) {
                List<IDSubject> matchedRecipients = res.getEvaluationRequest().getSentToMatchesForPolicy(obl.getPolicy().getName());

                    if (matchedRecipients == null) {
                        return "";
                    } else {
                        String[] matches = new String[matchedRecipients.size()];
                        int i = 0;
                        for (IDSubject recip : matchedRecipients) {
                            matches[i++] = recip.getUniqueName();
                        }

                        return StringUtils.join(matches, ";");
                    }
                } else if (argument.startsWith("$CEClientName.")) {  // $CEClientName.blahblah
                    EvaluationRequest req = res.getEvaluationRequest();
                    String key = argument.substring("$CEClientName.".length());
                    Object idAttribute = req.getFromResource().getAttribute(key).getValue();
                    if (idAttribute != null) {
                        Set<String> clientIdSet = new HashSet<String>();
                        if (idAttribute instanceof String) {
                            clientIdSet.add((String)idAttribute);
                        } else if (idAttribute instanceof IMultivalue) {
                            for (IEvalValue element : (IMultivalue)idAttribute) {
                                Object elementValue = element.getValue();
                                if (elementValue instanceof String) {
                                    clientIdSet.add((String)elementValue);
                                }
                            }
                        }
                        List<String> clients = new ArrayList<String>();
                        int unknownCount = 0;
                        for (String clientId : clientIdSet) {
                            ClientInformation clientInfo = req.getClientInformationManager().getClient(clientId);
                            if (clientInfo != null) {
                                clients.add(clientInfo.getLongName());
                            } else {
                                unknownCount++;
                            }
                        }
                        Collections.sort(clients, Collator.getInstance());
                        if (unknownCount != 0) {
                            clients.add("Unknown client ("+unknownCount+")");
                        }
                        if (!clients.isEmpty()) {
                            return StringUtils.join(clients, ";");
                        } else {
                            return "No client";
                        }
                    } else {
                        return "No client";
                    }
                } else if (argument.equals("$CESerializedLog")) {
                PolicyActivityLogEntryV5 entry = new PolicyActivityLogEntryV5(res.getPAInfo(), obl.getPolicy().getId().longValue(), obligationContext.getLogUid(obl.getPolicy().getName()));

                    String ret = SerializationUtils.wrapExternalizable(entry);
                    
                    if (ret == null) {
                        ret = "";
                    }

                    return ret;
                } else if (argument.equals("$CELogUid")) {
                return Long.toString(obligationContext.getLogUid(obl.getPolicy().getName()));
                } else {
                    // Assume the argument is someting like $ctx.attr and get that value from the dynamic attributes
                    String[] attrIdentifier = argument.substring(1).split("\\.");

                    if (attrIdentifier.length != 2) {
                        return argument;
                    }

                    String dimension = attrIdentifier[0];
                    String attr = attrIdentifier[1];

                    IEvaluationRequest req = res.getEvaluationRequest();

                    IEvalValue v = EvalValue.NULL;

                    if (dimension.equals("environment")) {
                        v = req.getEnvironment().get(attr);
                    } else if (dimension.equals("from")) {
                        IResource from = req.getFromResource();
                        v = from.getAttribute(attr);
                    } else if (dimension.equals("to")) {
                        IResource to = req.getToResource();
                        if (to != null) {
                            v = to.getAttribute(attr);
                        }
                    } else if (dimension.equals("user")) {
                        IDSubject user = req.getUser();
                        v = user.getAttribute(attr);
                    } else {
                        return argument;
                    }

                    if(v != null && v.getValue() instanceof String) {
                    	return (String) v.getValue();
                    }
                }
                
                // No match?  Return what we were given
                return argument;
            }

            // Expand strings "$CE..." in obligation arguments
            // invocationString was added to arguments to support old obligation argument format 
            public List<String> convertArgs(List <? extends Object> args, String invocationString, AgentCustomObligation obl, EvaluationResult res, ObligationContext obligationcontext) {
                List<String> newArgs = new ArrayList<String>();

                // If the obligation is one of five obligations that expect old format, 
            	// (value, value, ...) instead of (label, value, label, value ...)
            	// Skip labels by ignoring every other argument
            	// The definition of the obligation names was taken from Enforcers_Tuscany/platforms/win32/modules/msoPEP/stdafx.h
                boolean skipLabel = false;
                if (invocationString.equals("PREPEND_BODY") ||
                		invocationString.equals("APEND_BODY") ||
                		invocationString.equals("PREPEND_SUBJECT") ||
                		invocationString.equals("TAG_MESSAGE") ||
                		invocationString.equals("STORE_ATTACHMENTS"))
                	skipLabel = true;
                int i = 0;
                for (Object o : args) {
                	i++;
                	if (skipLabel == true && (i % 2 != 0)) {
                		// skip this argument
                		continue;
                	}
                	newArgs.add(convertArg((String)o, obl, res, obligationcontext));
                }

                return newArgs;
            }

            private String joinToString(List<? extends Object> args) {
                StringBuilder buffer = new StringBuilder("");
                for (Object obj : args) {
                    buffer.append(" ");
                    buffer.append(obj);
                }

                return buffer.toString();
            }

            public void preamble(IDObligation obl, EvaluationResult res, ObligationContext obligationContext) {
                AgentCustomObligation realObl = (AgentCustomObligation)obl;
                String name = realObl.getCustomObligationName();
                CustomObligationsContainer obligationsContainer = controlManager.getCustomObligationsContainer();

                obligationContext.addDescription(realObl, obligationsContainer.getInvocationString(name) + " " + joinToString(realObl.getCustomObligationArgs()));
            }

            public void execute(IDObligation obl, final EvaluationResult res, ResultParamArrayGen returnParams, ObligationContext obligationContext) {
                AgentCustomObligation realObl = (AgentCustomObligation)obl;

                final String name = realObl.getCustomObligationName();

                final CustomObligationsContainer obligationsContainer = controlManager.getCustomObligationsContainer();

                if (obligationsContainer.obligationExists(name)) {
                    String runLocation = obligationsContainer.getRunLocation(name);
                    final String invocationString = obligationsContainer.getInvocationString(name);
                    // invocationString was added to convertArgs() arguments to support old obligation argument format 
                    final List<String> args = convertArgs(realObl.getCustomObligationArgs(), invocationString, realObl, res, obligationContext);

                    if (runLocation.equals(CustomObligationsContainer.PDP_LOCATION)) {
                        asyncTaskQ.add(new IAsyncTask() {
                            @Override
                            public void execute() {
                                // Executable obligation
                                String cmd = invocationString + " " + joinToString(args);;
                                
                                try {
                                    String runBy = obligationsContainer.getRunBy(name);
                                    
                                    getLog().trace("Custom Obligation: " + cmd);
                                    
                                    if (runBy == null || runBy.equals("System")) {
                                        Runtime.getRuntime().exec(cmd);
                                    } else {
                                        getControlManager().executeCustomObligations(res.getEvaluationRequest().getLoggedInUser().getUid(), cmd);
                                    }
                                } catch (IOException e) {
                                    //....
                                }
                                
                            }
                        });          
                    } else {
                        // If it's not PDP, assume PEP (crossing fingers)
                        // Delegated obligation
                        returnParams.addDelegatedObligation(invocationString, realObl.getPolicy().getName(), args);
                    }
                } else {
                    // Likewise, if we've never heard of the obligation, assume it's a PEP obligation and send it off
                    getLog().warn("Unable to find obligation information for " + name + ". Sending to PEP");

                    // We have to use the given name as the invocation string, because we have nothing else
                    String invocationString = name;
                    final List<String> args = convertArgs(realObl.getCustomObligationArgs(), invocationString, realObl, res, obligationContext);
                    returnParams.addDelegatedObligation(invocationString, realObl.getPolicy().getName(), args);
                }
                return;
            }
        };

    private IDObligationExecutor LogExecutor = new IDObligationExecutor() {
        public void preamble(IDObligation obl, EvaluationResult res, ObligationContext obligationContext) {
            obligationContext.addDescription((AgentLogObligation)obl, "LOG");
        }
        
        public void execute(final IDObligation obl, final EvaluationResult res, ResultParamArrayGen returnParams, final ObligationContext obligationContext) {
            
            asyncTaskQ.add(new IAsyncTask() {
                @Override
                public void execute() {
                    AgentLogObligation actualObl = (AgentLogObligation) obl;
                    
                    DynamicAttributes oblDesc = obligationContext.getDescription(actualObl);
                    
                    PolicyActivityInfoV5 paInfo = res.getPAInfo();
                    
                    if (oblDesc != null) {
                        paInfo = new PolicyActivityInfoV5(paInfo);
                        paInfo.addAttributes(PolicyActivityInfoV5.FROM_RESOURCE_ATTRIBUTES_TAG, oblDesc);
                    }
                    
                    PolicyActivityLogEntryV5 entry = new PolicyActivityLogEntryV5(paInfo,
                                                                                  actualObl.getPolicy().getId().longValue(),
                                                                                  actualObl.getPolicy().getTags(),
                                                                                  obligationContext.getLogUid(actualObl.getPolicy().getName()));
                    
                    actualObl.getExecutor().logActivity(entry);
                }
                
            });
        }
    };

    /**
     * All tasks that need to be run asynchronously should implement this interface and
     * should be added to the queue asyncTaskQ 
     */
    interface IAsyncTask {
        void execute();
    }
        
    /**
     * This is responsible for running all the async tasks by pulling them out of the 
     * queue. It blocks till the next task is available on the queue.
     */
    private class AsyncTaskExecutor implements Runnable {
        private final BlockingQueue<IAsyncTask> queue;

        public AsyncTaskExecutor(BlockingQueue<IAsyncTask> queue) {
            super();
            this.queue = queue;
        }

        public void run() {
            try {
                if (getLog().isTraceEnabled()) {
                    getLog().trace("Starting the Async Task Executor thread");
                }
                while (true) { 
                    executeTask(queue.take());
                 }
              } catch (InterruptedException ex) { 
                  if (getLog().isErrorEnabled()) {
                      getLog().error("The async task executor thread was interrupted and will die", ex);
                  }
              }
        }

        void executeTask(IAsyncTask task) {
            task.execute();
        }
    } // class AsyncTaskExecutor

    
    /* On creation... */
    {
        obligationMap = new HashMap<String, IDObligationExecutor>();
        obligationMap.put(AgentCustomObligation.OBLIGATION_NAME, CustomExecutor);
        obligationMap.put(AgentDisplayObligation.OBLIGATION_NAME, DisplayExecutor);
        obligationMap.put(AgentLogObligation.OBLIGATION_NAME, LogExecutor);
        obligationMap.put(AgentNotifyObligation.OBLIGATION_NAME, NotifyExecutor);
    }
    
    private Log log;
    private IConfiguration configuration;
    private IControlManager controlManager;
    private IComponentManager manager;

    /**
     * Subject manager
     */
    private IDSubjectManager subjectManager;

    /**
     * Policy container
     */
    private IAgentPolicyContainer policyContainer;

    // caching of reverse DNS lookups
    private Cache dnsCache;

    // temporary storage for names that we haven't resolved but are unwilling
    // to declare permanently unresolveable
    private static final int MAX_DNS_ATTEMPTS=3;
    private Map<String, Integer> undecidedDnsCache;
    
    // names that we can't resolve
    private Cache unresolvedDnsCache;

    // my type
    private AgentTypeEnum agentType;
    
    /**
     * Predicates to ignore or track file resources
     */
    private CompositePredicate ignoredResourceCondition = null;
    
    /**
     * @param fileName
     * @return canonicalized version of file name
     */
    private String canonicalizeFileName(String fileName, String resourceType) {
        if (fileName == null || fileName.length() == 0 ||
            (resourceType != null && !resourceType.equals("fso"))) {
            return fileName;
        }

        String ret = fileName.replace('\\', '/');

        // There has to be a better way to do this.  We need the concept of an empty resource
        if (ret.equals("c:/no_attachment.ice")) {
            return NO_ATTACHMENT;
        }

        if (ret.startsWith("//")) {
            return ("file:" + ret);
        } else {
            return ("file:///" + ret);
        }
    }

    /**
     * @param nativeResources
     * @return
     */
    private List<String> canonicalizeResources(List<String> nativeResources, String resourceType) {
        if (nativeResources == null) {
            return null;
        }
        List<String> res = new ArrayList<String>();
        for (String name : nativeResources) {
            res.add(canonicalizeFileName(name.toLowerCase(), resourceType));
        }
        return res;
    }

    /**
     * Returns the agent type
     * 
     * @return the agent type
     */
    protected AgentTypeEnum getAgentType() {
        return this.agentType;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the control manager
     * 
     * @return the control manager
     */
    protected IControlManager getControlManager() {
        return controlManager;
    }

    /**
     * @param fileNameList
     *            filename with variable
     * @return ArrayList containing all possible variations of the name
     */
    private List<String> getEquivalentFileNames(String fileName, boolean resolveHostNames) {
        if (fileName == null || fileName.length() == 0) {
            return null;
        }

        List<String> fileNameList = null;

        FolderTrie sharedFolderTrie = controlManager.getSharedFolderTrie();
        if (sharedFolderTrie != null) {
            if (this.agentType == AgentTypeEnum.DESKTOP) {
                List<String> names = new ArrayList<String>();
                if (resolveHostNames) {
                    names.addAll(UNCUtil.getEquivalentHostNames(fileName));
                }
                else {
                    names.add(fileName);
                }

                for (String name : names) {
                    List<String> nameList = sharedFolderTrie.getFolderList(name);
                    if (fileNameList == null) {
                        fileNameList = nameList;
                    } else {
                        fileNameList.addAll(nameList);
                    }
                }
            } else {
                fileNameList = sharedFolderTrie.getFolderList(fileName);
            }
        } else {
            fileNameList = new ArrayList<String>();
            if (this.agentType == AgentTypeEnum.DESKTOP && resolveHostNames) {
                fileNameList.addAll(UNCUtil.getEquivalentHostNames(fileName));
            } else {
                fileNameList.add(fileName);
            }
        }
        
        return fileNameList;
    }

    synchronized private void removeFromUndecided(String hostArg) {
        undecidedDnsCache.remove(hostArg);
    }

    synchronized private boolean exceededMaxAttempts(String hostArg) {
        Integer count = undecidedDnsCache.get(hostArg);
        boolean exceededCount = false;

        if (count == null) {
            undecidedDnsCache.put(hostArg, 1);
        } else if (count >= MAX_DNS_ATTEMPTS) {
            removeFromUndecided(hostArg);
            exceededCount = true;
        } else {
            undecidedDnsCache.put(hostArg, count+1);
        }

        return exceededCount;
    }

    /**
     * Returns some host information
     * 
     * @param hostArg
     *            host argument (as a string for both hostname or IP address)
     * @param hostName
     *            name of the host
     * @return host information
     * @throws UnknownHostException
     */
    private HostInfo getHostInfo(String hostArg) {
        HostInfo hostInfo;
        boolean isHostName = false;
        // Technically the ip address should fit in an int, but it can come
        // in either signed or unsigned (in which case we might need a long)
        long ip = 0;

        try {
            ip = Long.parseLong(hostArg);
        } catch (NumberFormatException e) {
            isHostName = true;
        }

        if (isHostName) {
            try {
                Element elem = getCachedDNSResult(hostArg);
                if (elem != null) {
                    hostInfo = (HostInfo)elem.getValue();
                } else {
                    boolean resolvedHostName = true;
                    InetAddress addr = InetAddress.getByName(hostArg);
                    String hostName  = addr.getCanonicalHostName().toLowerCase();
                    String hostIP    = addr.getHostAddress();
                    if (hostIP.equals (hostName)) {
                        resolvedHostName = false;
                    }
                    hostInfo = new HostInfo(hostName, hostIP);
                    elem = new Element(hostArg, hostInfo);
                    cacheDNSResult(elem, hostArg, resolvedHostName);
                }
            } catch (UnknownHostException e) {
                getLog().error("Invalid host name/identifier " + hostArg, e);
                hostInfo = new HostInfo (hostArg, "");
            }
        } else {
            byte[] hostAddress = new byte[4];
            for (int i = 24, j = 0; i >= 0; i -= 8, j++) {
                hostAddress[j] = new Long((ip >> i) & 0xff).byteValue();
            }
            if ((LOCALHOST_BYTE_ADDRESS == ip) || (ip == 0)) {
                hostInfo = new HostInfo(getControlManager().getHostName(), getControlManager().getHostIP());
            } else {
                Long longAddr = new Long(ip);
                try {
                    Element elem = getCachedDNSResult(longAddr);
                    if (elem != null) {
                        hostInfo = (HostInfo) elem.getValue();
                    } else {
                        InetAddress addr = InetAddress.getByAddress(hostAddress);
                        String hostName = addr.getCanonicalHostName().toLowerCase();
                        String hostIP = addr.getHostAddress();
                        boolean resolvedHostName = true;
                        if (hostIP.equals(hostName)) {
                            resolvedHostName = false;
                        }
                        hostInfo = new HostInfo(hostName, hostIP);
                        elem = new Element(longAddr, hostInfo);
                        cacheDNSResult(elem, hostArg, resolvedHostName);
                    }
                } catch (UnknownHostException e) {
                    getLog().error("Invalid host IP address", e);
                    hostInfo = new HostInfo("", "");
                }
            }
        }
        return hostInfo;
    }

    /*
     * We have two caches and a waiting list.  The resolved cache is
     * for names that we have found.  The unresolved cache is for
     * names that we've looked for a few times and not found, so we
     * won't bother looking for them in the future.  There is also an
     * undecided list, where we have the items that we have failed to
     * find, along with a count of how many times we have failed to
     * find them.
     *
     * TODO -
     * This is likely overengineered.  We could probably do this with
     * one cache.  We could also utilize this in the UNCUtil function
     * that looks up the host name.
     */

    /**
     * Look up element in the resolved and then unresolved caches
     * @param key the key (either a Long or a String)
     * @return the element from the cache (or null if error/it doesn't exist)
     */
    private Element getCachedDNSResult(Serializable key) {
        if (dnsCache == null || key == null) {
            return null;
        }

        try {
            Element element = dnsCache.get(key);
            if (element == null) {
                element = unresolvedDnsCache.get(key);
            }
            return element;
        } catch (CacheException ce) {
            getLog().warn("Unable to use dns cache (internal error).  Continuing as if element was not found.");
            return null;
        }
    }

    /**
     * Cache result of dns lookup.  If the entry was found it is cached in the resolved cache.
     * If it wasn't then we will see if it's bee unresolved too many times (the undecided list)
     * and, if so, move it into the unresolved cache.
     * @param elem the element to cache (includes both key and value)
     * @param hostArg the host identification (name or ip address)
     * @param wasResolved was the host information successfully resolved or not
     */
    private void cacheDNSResult(Element elem, String hostArg, boolean wasResolved) {
        if (dnsCache == null || unresolvedDnsCache == null) {
            return;
        }

        if (wasResolved) {
            dnsCache.put(elem);
            removeFromUndecided(hostArg);
        } else if (exceededMaxAttempts(hostArg)) {
            getLog().info("Unable to find " + hostArg + " after multiple attempts.  Putting in unresolved cache\n");
            unresolvedDnsCache.put(elem);
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
     * Returns the policy container
     * 
     * @return the policy container
     */
    protected IAgentPolicyContainer getPolicyContainer() {
        return policyContainer;
    }

    /**
     * Returns the subject manager
     * 
     * @return the subject manager
     */
    protected IDSubjectManager getSubjectManager() {
        return subjectManager;
    }

    /**
     * @see IInitializable#init()
     */
    public void init() {
        controlManager = getConfiguration().get(CONTROL_MANAGER_CONFIG_PARAM);
        if (getControlManager() == null) {
            throw new NullPointerException("Control manager needs to be passed to the configuration");
        }

        this.subjectManager = getManager().getComponent(IDSubjectManager.COMP_INFO);
        if (getSubjectManager() == null) {
            throw new NullPointerException("Unable to get IDSubjectManager component");
        }

        this.policyContainer = getConfiguration().get(AGENT_POLICY_CONTAINER_CONFIG_PARAM);
        if (getPolicyContainer() == null) {
            throw new NullPointerException("Unable to get IAgentPolicyContainer component");
        }

        String allowDontCareForAllQueries = System.getProperty(DONT_CARE_RESPONSE_ACCEPTABLE_KEY);

        if (allowDontCareForAllQueries != null) {
            dontCareAcceptable = Boolean.parseBoolean(allowDontCareForAllQueries);
        }
        
        String allowErrorForAllQueries = System.getProperty(ERROR_RESPONSE_ACCEPTABLE_KEY);
        if (allowErrorForAllQueries != null) {
            errorAcceptable = Boolean.parseBoolean(allowErrorForAllQueries);
        }
        
        IBundleVault bundleVault = (IBundleVault) this.getManager().getComponent(IBundleVault.COMPONENT_NAME);

        //      setup EHCache
        CacheManager cacheManager;
        try {
            cacheManager = CacheManager.create();
        } catch (CacheException e) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Unable to create reverse DNS cache, proceeding in uncached mode.", e);
            }
            // no big deal, no caching
            return;
        }

        //This will get called a few times but should be ok.
        UNCUtil.setLocalHost(controlManager.getHostName());
        this.agentType = AgentTypeEnum.getAgentTypeEnum(getControlManager().getAgentType().getValue());

        this.dnsCache = cacheManager.getCache(CMRequestHandler.class.getName());
        if (this.dnsCache == null) {
            int resolvedDNSCacheTimeout = getControlManager().getResolvedDNSCacheTimeout();
            this.dnsCache = new Cache(CMRequestHandler.class.getName(), 20000, false, false, resolvedDNSCacheTimeout, 0);
            try {
                cacheManager.addCache(this.dnsCache);
            } catch (CacheException e1) {
                if (getLog().isWarnEnabled()) {
                    getLog().warn("unable to create reverse DNS cache, proceeding in uncached mode.", e1);
                }
                this.dnsCache = null;
            }
        }

        this.undecidedDnsCache = new HashMap<String, Integer>();

        this.unresolvedDnsCache = cacheManager.getCache(CMRequestHandler.class.getName() + UNRESOLVED_DNS_CACHE_SUFFIX);
        if (this.unresolvedDnsCache == null) {
            // XXX fix the hard-coded values
            this.unresolvedDnsCache = new Cache(CMRequestHandler.class.getName() + UNRESOLVED_DNS_CACHE_SUFFIX, 20000, false, false, 10 * 60, 0);
            try {
                cacheManager.addCache(this.unresolvedDnsCache);
            } catch (CacheException e1) {
                if (getLog().isWarnEnabled()) {
                    getLog().warn("unable to create (unresolved) reverse DNS cache, proceeding in uncached mode.", e1);
                }
                this.unresolvedDnsCache = null;
            }
        }

        final List<IPredicate> conditions = new ArrayList<IPredicate>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(IGNORED_RESOURCES_FILE));
            String resource = null;
            while ((resource = br.readLine()) != null) {
                conditions.add(ResourceAttribute.NAME.buildRelation(RelationOp.EQUALS, resource));
            }
            if (conditions.size() > 0) {
                this.ignoredResourceCondition = new CompositePredicate(BooleanOp.OR, conditions);
            }
        } catch (FileNotFoundException e2) {
            this.log.info("Resource.info file not found.");
        } catch (IOException e) {
            this.log.error("Resource.info file read error.", e);
        }
        
        // This sets up the mechanism for running the custom obligation tasks on
        //  a separate thread
        asyncTaskQ = new LinkedBlockingQueue<IAsyncTask>();
        asyncTaskExecutor = new AsyncTaskExecutor(asyncTaskQ);
        asyncTaskExecutorThread = new Thread(asyncTaskExecutor, "PolicyEvaluatorImpl.AsyncTaskExecutor");
        asyncTaskExecutorThread.start();
    }

    public void dispose() {
        asyncTaskExecutorThread.interrupt();
    }

    /**
     * Convert input parameters to a string (to be printed later)
     * 
     * @param paramArray
     *            Parameter Array from C++ callback
     * @return void
     */
    private String inputParamsToString(Long sequenceID, Map<String, DynamicAttributes> context, Long processToken, int loggingLevel, boolean ignoreObligations) {
        StringBuilder buffer = new StringBuilder("Request " + sequenceID + " input params\n");
        Set<String> keys = context.keySet();

        for (String name : keys) {
            buffer.append("  " + name + "\n");

            Set<String> dynKeys = context.get(name).keySet();

            for (String key : dynKeys) {
                String[] vals = context.get(name).getStrings(key);

                buffer.append("\t" + key + ":");
                for (String v : vals) {
                    buffer.append(" " + v);
                }
                buffer.append("\n");
            }
        }
        
        buffer.append("  Ignore obligation = " + Boolean.toString(ignoreObligations) + "\n");
        buffer.append("  Process Token = " + processToken + "\n");
        buffer.append("  LogLevel = " + loggingLevel + "\n");
        return buffer.toString();
    }

    private int decodeLoggingLevel(Object[] paramArray)
    {
        int loggingLevel = 0;
        String loggingVal = (String)paramArray[LOG_LEVEL_INDEX];

        try {
            loggingLevel = Integer.decode(loggingVal).intValue();
        } catch (NumberFormatException e) {
            //Error in converting - defaulting to LOG_LEVEL_USER
            getLog().error("Error converting log level - value was " + loggingVal);
        } finally {
            //0 is invalid value - defaults to LOG_LEVEL_USER
            if (loggingLevel == 0) {
                loggingLevel = ILoggingLevel.LOG_LEVEL_USER;
            }
        }

        return loggingLevel;
    }

    private Long decodeProcessToken(Object[] paramArray) {
        String processVal = (String)paramArray[PROCESS_TOKEN_INDEX];
        Long processToken = null;

        try {
            processToken = Long.decode(processVal);
        } catch (NumberFormatException e) {
            getLog().error("Error converting process token - value was " + processVal);
        }

        return processToken;
    }

    private DynamicAttributes decodeAttributes(Object[] attrArray) {
        DynamicAttributes attrs = new DynamicAttributes();

        for (int i = 0; i < attrArray.length; i+=2) {
            String key = (String)attrArray[i];
            try {
                String value = (String)attrArray[i+1];
                attrs.add(key.toLowerCase(), value);
            } catch (ArrayIndexOutOfBoundsException e) {
                getLog().error("Found key " + key + ", but no value");
            }
        }

        return attrs;
    }

    private String decodeResolvedFromFileName(DynamicAttributes fromContext, String fromFileName) {
        String resolvedFromFileName = fromContext.getString(SpecAttribute.RESOLVEDNAME_ATTR_NAME);

        if (resolvedFromFileName == null || resolvedFromFileName.length() == 0) {
            resolvedFromFileName = fromFileName;
        }

        return (OSType.getSystemOS() == OSType.OS_LINUX) ? resolvedFromFileName : resolvedFromFileName.toLowerCase();
    }

    private boolean hostNameResolutionRequested(DynamicAttributes attrs) {
        // Anything other than an explicit "no" is a "yes"
        return (!("no".equals(attrs.getString(SpecAttribute.GET_EQUIVALENT_HOST_NAMES))));
    }

    // TODO - We shouldn't need separate code from from and to resources
    private List<String> buildEquivalentToNamesList(DynamicAttributes toContext, String toFileName) {
        List<String> toFileNameList = null;

        boolean performResolveNames = hostNameResolutionRequested(toContext);

        if (AgentTypeEnum.DESKTOP.equals(getAgentType())) {
            toFileNameList = getEquivalentFileNames(toFileName, performResolveNames);
            if (toFileNameList != null) {
                String resolvedToFileName = toContext.getString(SpecAttribute.RESOLVEDNAME_ATTR_NAME);

                if (resolvedToFileName == null || resolvedToFileName.length() == 0) {
                    resolvedToFileName = toFileName;
                } else if (!toFileName.equals(resolvedToFileName)) {
                    /* A distinct resolved file name, should be added to the filelist for evaulation */
                    toFileNameList.addAll(getEquivalentFileNames (resolvedToFileName, performResolveNames));
                }
                String localToFileName = null;
                if (performResolveNames) {
                localToFileName = UNCUtil.resolveLocalPath(resolvedToFileName);
                }
                if (localToFileName != null) {
                    toFileNameList.add(localToFileName);
                }
            }
        } else {
            // At least fill out one element for the File server agent
            toFileNameList = getEquivalentFileNames(toFileName, performResolveNames);
        }

        return toFileNameList;
    }

    private void expandResourceURL(DynamicAttributes resourceContext) {
        if (hostNameResolutionRequested(resourceContext)) {
            // This should only appear once, but there is no way to ensure that.  Hence getStrings().
            String[] resourceURLs = resourceContext.getStrings("url");
            
            if (resourceURLs.length > 0) {
                List<String> equivalentURLs = new ArrayList<String>();
                for (String url : resourceURLs) {
                    equivalentURLs.addAll(UNCUtil.getEquivalentHostNames(url));
                }
                
                resourceContext.put("url", equivalentURLs);
            }
        }
    }

    private Map<String, DynamicAttributes> decodeAllDynamicArgs(Object[] paramArray) {
        Map<String, DynamicAttributes> ret = new HashMap<String, DynamicAttributes>();
        Object[] dimensions = (Object[])paramArray[DIMENSION_DEFS_INDEX];

        for (int i = 0; i < dimensions.length; i++) {
            ret.put((String)dimensions[i], decodeAttributes((Object[])paramArray[i+DIMENSION_DEFS_INDEX+1]));
        }

        return ret;
    }
 
    private void addFileListToAttr(List<String> fileNameList, DynamicAttributes context, String resourceDestinyType)
    {
        if (context != DynamicAttributes.EMPTY && fileNameList != null) {
            if (resourceDestinyType.equals("fso")) {
                fileNameList = canonicalizeResources(fileNameList, resourceDestinyType);
            }
            context.put(SpecAttribute.NAME_ATTR_NAME, fileNameList);
        }
    }

    private boolean accessDeniedByPEP(DynamicAttributes context) {
        if (context == null) {
            return false;
        }

        String decision = context.getString("tamper-resistance-decision");

        if (decision == null || !decision.equals("deny")) {
            return false;
        }

        return true;
    }

    private static DynamicAttributes getEnvironmentContext(Map<String, DynamicAttributes> context) {
        DynamicAttributes environmentContext = context.get("environment");
        
        if (environmentContext == null) {
            environmentContext = new DynamicAttributes();
            context.put("environment", environmentContext);
        }
        
        return environmentContext;
    }

    private void addHeartbeatInfo(DynamicAttributes environmentContext) {
        environmentContext.add("time_since_last_successful_heartbeat", String.valueOf(getControlManager().getTimeSinceLastSuccessfulHeartbeat()));
    }

    private boolean isDontCareAcceptable(DynamicAttributes environmentContext) {
        // If the request has a preference, use it, otherwise use the global setting
        String requestDontCareFlag = environmentContext.getString("dont-care-acceptable");

        if (requestDontCareFlag == null) {
            return dontCareAcceptable;
        } else {
            return !requestDontCareFlag.equals("no");
        }
    }
    
    private boolean isErrorAcceptable(DynamicAttributes environmentContext) {
        // If the request has a preference, use it, otherwise use the global setting
        String requestErrorFlag = environmentContext.getString("error-result-acceptable");

        if (requestErrorFlag == null) {
            return errorAcceptable;
        } else {
            return !requestErrorFlag.equals("no");
        }
    }

    private void addCacheHint(DynamicAttributes resourceContext, ResultParamArrayGen returnParams, boolean computeValue) {
        String cacheHint = resourceContext.getString(SpecAttribute.REQUEST_CACHE_HINT);
        
        if (cacheHint != null && cacheHint.equals("yes")) {
            long cacheHintValue = 0;
            if (computeValue) {
                cacheHintValue = getControlManager().getSDKCacheHintValue() ;
                long timeToNextHeartbeat = getControlManager().getSecondsUntilNextHeartbeat();
                
                if (cacheHintValue > timeToNextHeartbeat) {
                    cacheHintValue = timeToNextHeartbeat;
                }
            }
            returnParams.addAttribute("CE_CACHE_HINT", Long.toString(cacheHintValue));
        }
    }

    private long generateRequestID() {
        return System.nanoTime();
    }

    public boolean queryDecisionEngine(Object[] paramArray, List resultParamArray) {
        Map<String, DynamicAttributes> context = decodeAllDynamicArgs(paramArray);
        
        Long processToken = decodeProcessToken(paramArray);
        int loggingLevel = decodeLoggingLevel(paramArray);
        boolean ignoreObligations = Boolean.valueOf(((String) paramArray[IGNORE_OBLIGATIONS_INDEX])).booleanValue();

        return queryDecisionEngine(context, processToken, loggingLevel, ignoreObligations, resultParamArray);
    }

    /**
     * @see com.bluejungle.destiny.agent.controlmanager.IPolicyEvaluator#queryDecisionEngine(java.util.ArrayList,
     *      java.util.ArrayList)
     */
    public boolean queryDecisionEngine(Map<String, DynamicAttributes> context, Long processToken, int loggingLevel, boolean ignoreObligations, List resultParamArray) {
        ResultParamArrayGen returnParams = new ResultParamArrayGen();

        long enterTime = System.nanoTime();
        String inputParamsAsString = "";

        ProcessTokenWrapper processTokenWrapper = ProcessTokenWrapper.NONE;

        try {
            DynamicAttributes environmentContext = getEnvironmentContext(context);
            errorAcceptable = isErrorAcceptable(environmentContext);

            addHeartbeatInfo(environmentContext);
            
            long requestID = generateRequestID();
            environmentContext.put("request_id", EvalValue.build(Long.toString(requestID)));
            
            inputParamsAsString = getLog().isInfoEnabled() ? inputParamsToString(requestID, context, processToken, loggingLevel, ignoreObligations) : "";
            
            String actionName = context.get("action").getString("name");
            if (actionName.equals("NEW_EMAIL") ||
                actionName.equals("FORWARD") ||
                actionName.equals("REPLY")) {
                actionName = "EMAIL";
                context.get("action").put("name", actionName);
            }


            DynamicAttributes appContext = context.get("application");
            String appName = null;
            if (appContext != null) {
                appName = appContext.getString("name");
                
                // Mark the process as trusted unless this is a RUN action.  RUN actions come from the procdetect
                // driver and we'll get that even if there are no other enforcers on the system
                String pid = appContext.getString("pid");
                if (pid != null && !actionName.equals("RUN")) {
                    try {
                        getControlManager().getTrustedProcessManager().addTrustedProcess(Integer.parseInt(pid));
                    } catch (NumberFormatException e) {
                    }
                }
            }

            if (appName == null) {
                appName = "";
            }

            // Special query
            if (actionName.equals(PDPSDK.MONITOR_APP_ACTION)) {
                String effect = EvaluationResult.ALLOW;

                if (policyContainer.isApplicationIgnorable(appName)) {
                    effect = EvaluationResult.DENY;
                }

                returnParams.setEvaluationResult(effect);
                returnParams.buildReturnArray(resultParamArray);
                getLog().info(inputParamsAsString + "  Result: " + effect);
                return true;
            }

            DynamicAttributes userContext = context.get("user");
            String userId = userContext.getString("id");
            String userName = userContext.getString("name");

            if (userName == null || userName.equals("")) {
                if (userId == null || userId.equals("")) {
                    // Problem - no user information at all
                    userId = "S-1-0-0";
                    userContext.put("id", userId);

                    userName = "Nobody";
                } else {
                    userName = UserNameCache.getUserName(userId);
                }
                userContext.put("name", userName);
            }

            processTokenWrapper = ProcessTokenMap.substituteToken(userId, appName, processToken);

            // do not enforce policies for local system user.
            if (LOCAL_SYSTEM_USER.equals(userId)) {
                returnParams.buildReturnArray(resultParamArray);
                return true;
            }

            // FIXME This is a hack to get the integration going
            if (context.get("action") != null) {
                String tmp = context.get("action").getString("name");
                if ("NEW_EMAIL".equals(tmp)
                  || "FORWARD".equals(tmp)
                  || "REPLY".equals(tmp)) {
                    actionName = "EMAIL";
                    context.get("action").put("name", actionName);
                }
            }

            if ("CLOSE".equals(actionName)) {
                returnParams.buildReturnArray(resultParamArray);
                return true;
            }

            DynamicAttributes fromContext = context.get("from");
            String fromResourceDestinyType = fromContext.getString(SpecAttribute.DESTINYTYPE_ATTR_NAME);

            // Fix host name in "url", if attribute exists
            expandResourceURL(fromContext);

            String fromFileName = fromContext.getString(SpecAttribute.ID_ATTR_NAME);

            if (OSType.getSystemOS() == OSType.OS_LINUX) {
                fromContext.put(SpecAttribute.ORIG_ATTR_NAME, canonicalizeFileName(fromFileName, fromResourceDestinyType));
            } else {
                fromFileName = fromFileName.toLowerCase();
            }

            DynamicAttributes hostContext = context.get("host");
            String hostIP = hostContext.getString("inet_address");
            String hostName = hostContext.getString("name");

            if (hostName == null || hostIP == null) {
                if (getControlManager().resolveHostInformation()) {
                    getLog().debug("Resolving host name info");
                    HostInfo hostInfo = getHostInfo(hostIP);
                    
                    hostName = hostInfo.getHostName();
                    hostContext.put("name", hostName);
                    
                    hostIP = hostInfo.getHostIP();
                    hostContext.put("inet_address", hostIP);
                } else {
                    getLog().debug("Not resolving host name info");
                    long ip = 0;
                    try {
                        ip = Long.parseLong(hostIP);
                    } catch (NumberFormatException e) {
                    }
                    if (ip == 0) {
                        ip = LOCALHOST_BYTE_ADDRESS;
                    }
                    String dottedIP = HostInfo.toIP(ip);
                    hostContext.put("inet_address", dottedIP);
                    hostContext.put("name", dottedIP);
                }
            }


            DynamicAttributes toContext = context.get("to");

            if (toContext == null) {
                toContext = DynamicAttributes.EMPTY;
            }

            // Fix host name in "url", if attribute exists
            expandResourceURL(toContext);

            String resolvedFromFileName = decodeResolvedFromFileName(fromContext, fromFileName);

            String localFromFileName = null;
            if (AgentTypeEnum.DESKTOP.equals(getAgentType())) {
                localFromFileName = UNCUtil.resolveLocalPath(resolvedFromFileName);
            }

            String nameToUse;
            if (localFromFileName != null) {
                nameToUse = localFromFileName;
            } else if (resolvedFromFileName.charAt(0) != '[') {
                nameToUse = resolvedFromFileName;
            } else {
                nameToUse = fromFileName;
            }

            nameToUse = nameToUse.toLowerCase();

            String toResourceDestinyType = toContext.getString(SpecAttribute.DESTINYTYPE_ATTR_NAME);

            String canonicalizedFromFileName = canonicalizeFileName(nameToUse, fromResourceDestinyType);

            // A couple of names indicate that we aren't actually looking at a real file.  We should mark it
            // accordingly.
            if (canonicalizedFromFileName.endsWith("*") || canonicalizedFromFileName.equals(NO_ATTACHMENT)) {
                fromContext.put(SpecAttribute.FSCHECK_ATTR_NAME, "no");
                // If the file doesn't exist then it really shouldn't have a modified date.  It shouldn't have
                // a modified date either, but that's never passed in by the enforcer, so I'm going to ignore that
                fromContext.remove("modified_date");
            }

            boolean resourceAccessDeniedByPEP = accessDeniedByPEP(fromContext);

            if (!resourceAccessDeniedByPEP && this.ignoredResourceCondition != null) {
                IMResource resource = new Resource(canonicalizedFromFileName);
                resource.setAttribute(SpecAttribute.NAME_ATTR_NAME, canonicalizedFromFileName);
                EvaluationRequest request = new EvaluationRequest(resource);
                
                if (this.ignoredResourceCondition.match(request)) {
                    getLog().debug("Action ignored for: " + fromFileName);
                    returnParams.buildReturnArray(resultParamArray);
                    return true;
                }
            }

            final String toFileName = toContext.getString(SpecAttribute.ID_ATTR_NAME);

            String effect = null;
            ActionEnumType action = ActionEnumType.getActionEnum(actionName);
            boolean isTracked = false;

            if (resourceAccessDeniedByPEP) {
                isTracked = true;
                effect = EvaluationResult.DENY;
            } else {
                if (loggingLevel == ILoggingLevel.LOG_LEVEL_SYSTEM) {
                    effect = EvaluationResult.ALLOW;
                }
                isTracked = getControlManager().isTrackedAction(action);
            }

            List<String> fromFileNameList = getEquivalentFileNames(fromFileName, hostNameResolutionRequested(fromContext));
            if (!fromFileName.equals(resolvedFromFileName)) {
                fromFileNameList.addAll(getEquivalentFileNames(resolvedFromFileName, hostNameResolutionRequested(fromContext)));
            }
            if (localFromFileName != null) {
                fromFileNameList.add(localFromFileName);
            }

            List<String> toFileNameList = buildEquivalentToNamesList(toContext, toFileName);

            long querySetupTime = 0;
            long obligationExecutionTime = 0;

            //skip if we already know the effect
            if (effect == null) {
                if (action.equals(ActionEnumType.ACTION_STOP_AGENT)) {
                    effect = EvaluationResult.DENY;
                    getLog().error("Agent Termination Attempted");
                } else {
                    addFileListToAttr(fromFileNameList, fromContext, fromResourceDestinyType);

                    if (toFileNameList != null) {
                        addFileListToAttr(toFileNameList, toContext, toResourceDestinyType);
                    } else {
                        toContext = DynamicAttributes.EMPTY;
                    }

                    querySetupTime = (System.nanoTime() - enterTime)/1000000;

                    //Do the real policy evaluation
                    EvaluationResult result = getPolicyContainer().evaluate(
                        requestID
                    ,   AgentTypeEnumType.getAgentType(getAgentType().getName())
                    ,   processTokenWrapper.getToken()
                    ,   context
                    ,   System.currentTimeMillis()
                    ,   loggingLevel
                    ,   !ignoreObligations
                    );
                    effect = result.getEffectName();

                    long obligationStart = System.nanoTime();

                    List<IDObligation> obligations = result.getObligations();

                    if (result.getPAInfo() != null) {
                        IEvalValue url = context.get("application") != null ? context.get("application").get("url") : null;

                        if (url != null) {
                            result.getPAInfo().addAttribute("RF", "URL", url);
                        }

                        DynamicAttributes sendTo = context.get("sendto");
                        if (sendTo != null) {
                            IEvalValue val = sendTo.get("email");
                            
                            if (val != null) {
                            	result.getPAInfo().addAttribute("RF", "Sent to", val);
                            }
                        }
                    }

                    // Cache hint only goes in if there are no obligations and no tracking activity logs
                    addCacheHint(fromContext, returnParams, (ignoreObligations || obligations == null || obligations.size() == 0) && !isTracked);

                    if (!ignoreObligations && obligations != null) {
                        executeObligations(obligations, result, returnParams);
                    }
                    
                    obligationExecutionTime = (System.nanoTime() - obligationStart)/1000000;
                }
            } else {
                // Skipped evaluation, so no obligations.  However, we might still have tracking logs
                addCacheHint(fromContext, returnParams, !isTracked);
            }

            // Only return "don't care" if the PEP says that it understand it
            if (effect.equals(EvaluationResult.DONT_CARE) && !isDontCareAcceptable(environmentContext)) {
                effect = EvaluationResult.ALLOW;
            }

            returnParams.setEvaluationResult(effect);
            returnParams.buildReturnArray(resultParamArray);

            if (!ignoreObligations && isTracked) {
                IDSubject user = getSubjectManager().getSubject(userId, SubjectType.USER);
                Long userSubjectId = user != null ? user.getId() : IDSubject.UNKNOWN_ID;
                IDSubject host = getSubjectManager().getSubject(hostName, SubjectType.HOST);
                Long hostSubjectId = host != null ? host.getId() : IDSubject.UNKNOWN_ID;
                IDSubject application = getSubjectManager().getSubject(appName, SubjectType.APP);
                Long appSubjectId = application != null ? application.getId() : IDSubject.UNKNOWN_ID;

                getControlManager().logTrackingActivity(action, userSubjectId, userName, hostSubjectId, hostName, hostIP, appSubjectId, appName, canonicalizedFromFileName, fromContext, canonicalizeFileName(toFileName, toResourceDestinyType), null, loggingLevel);
            }

            if (getLog().isInfoEnabled()) {
                long elapsedTime = (System.nanoTime() - enterTime)/1000000;
                StringBuilder sb = new StringBuilder(inputParamsAsString);
                sb.append("  Result: Effect = ");
                sb.append(effect);
                sb.append(" (total:");
                sb.append(elapsedTime);
                sb.append("ms, setup:");
                sb.append(querySetupTime);
                sb.append("ms, obligations:");
                sb.append(obligationExecutionTime);
                sb.append("ms)\n");

                if (resultParamArray.get(1) != null) {
                    sb.append("  Obligations:\n");
                    for (String s: (String[])resultParamArray.get(1)) {
                        sb.append("    " + s + "\n");
                    }
                }

                sb.append("  From file list: " + fromFileNameList);
                sb.append("\n  To filename list: " + toFileNameList);

                getLog().info(sb.toString());
            }
        } catch (Throwable e) {
            getLog().error(inputParamsAsString + "  Result: Exception thrown\n", e);
            
            // Only return "error" if the PEP says that it understand it otherwise 'allow' so the application
            // doesn't hang. We have to be careful here. Shouldn't add any code
            // that throws exceptions that should be caught explicitly
			if (errorAcceptable) {
				returnParams.setEvaluationResult(EvaluationResult.ERROR);
			}

            returnParams.buildReturnArray(resultParamArray);
        } finally {
            processTokenWrapper.closeProcessToken();
        }

        return true;
    }

    /**
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
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

    public void executeObligationPreamble(IDObligation obl, EvaluationResult res, ResultParamArrayGen returnParams, ObligationContext obligationContext) {
        IDObligationExecutor oblExec = obligationMap.get(obl.getType());

        if (oblExec == null) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Can't find obligation type " + obl.getType() + " in map.");
            }
        } else {
            oblExec.preamble(obl, res, obligationContext);
        }
    }
    

    public void executeObligation(IDObligation obl, EvaluationResult res, ResultParamArrayGen returnParams, ObligationContext obligationContext) {
        IDObligationExecutor oblExec = obligationMap.get(obl.getType());

        if (oblExec == null) {
            if (getLog().isWarnEnabled()) {
                getLog().warn("Can't find obligation type " + obl.getType() + " in map.");
            }
        } else {
            oblExec.execute(obl, res, returnParams, obligationContext);
        }
    }
    
    public void executeObligations(List<IDObligation> obligations, EvaluationResult res, ResultParamArrayGen returnParams) {
        ObligationContext obligationContext = new ObligationContext();

        for (IDObligation obl : obligations) {
            executeObligationPreamble(obl, res, returnParams, obligationContext);
        }
        for (IDObligation obl : obligations) {
            executeObligation(obl, res, returnParams, obligationContext);
        }
    }
}


class ObligationContext {
    private Map<String, DynamicAttributes> obligationDescriptionByPolicy;
    private Map<String, Long> logUidByPolicy;

    public ObligationContext() {
        obligationDescriptionByPolicy = new HashMap<String, DynamicAttributes>();
        logUidByPolicy = new HashMap<String, Long>();
    }

    public Long getLogUid(String policyName) {
        Long uid = logUidByPolicy.get(policyName);

        if (uid == null) {
            ComponentInfo info = new ComponentInfo(LogIdGenerator.NAME, LogIdGenerator.NAME, LogIdGenerator.NAME, LifestyleType.SINGLETON_TYPE);
            LogIdGenerator logIdGenerator = (LogIdGenerator)ComponentManagerFactory.getComponentManager().getComponent(info);
            uid = logIdGenerator.getNextId();
            logUidByPolicy.put(policyName, uid);
        }

        return uid;
    }

    private String getPolicyName(DObligation obl) {
        return obl.getPolicy().getName();
    }

    public void addDescription(DObligation obl, String description) {
        String policyName = getPolicyName(obl);
        DynamicAttributes policyObligations = obligationDescriptionByPolicy.get(policyName);
        if (policyObligations == null) {
            policyObligations = new DynamicAttributes();
            obligationDescriptionByPolicy.put(policyName, policyObligations);
        }

        policyObligations.add("Obligation", description);
    }

    public DynamicAttributes getDescription(DObligation obl) {
        return obligationDescriptionByPolicy.get(getPolicyName(obl));
    }
}
