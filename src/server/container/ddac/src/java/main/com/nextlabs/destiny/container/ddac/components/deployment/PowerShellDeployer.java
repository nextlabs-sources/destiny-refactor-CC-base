/*
 * Created on Oct 22, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/ddac/src/java/main/com/nextlabs/destiny/container/ddac/components/deployment/PowerShellDeployer.java#1 $:
 */

package com.nextlabs.destiny.container.ddac.components.deployment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.dcc.DCCResourceLocators;
import com.bluejungle.destiny.container.dcc.INamedResourceLocator;
import com.bluejungle.destiny.container.dcc.ServerRelativeFolders;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.utils.StringUtils;
import com.nextlabs.destiny.container.ddac.components.deployment.DDACDeploymentException;
import com.nextlabs.destiny.container.ddac.configuration.DDACActiveDirectoryConfiguration;
import com.nextlabs.pf.destiny.formatter.DACCentralAccessPolicy;
import com.nextlabs.pf.destiny.formatter.DACCentralAccessRule;
import com.nextlabs.pf.destiny.formatter.DACCentralAccessRule.DACUserClaims;
import com.nextlabs.pf.destiny.formatter.DACDomainObjectFormatter;

public class PowerShellDeployer implements IDACDeployer {
    private static final IComponentManager compMgr = ComponentManagerFactory.getComponentManager();
    private static final INamedResourceLocator serverResourceLocator = (INamedResourceLocator) compMgr.getComponent(DCCResourceLocators.SERVER_HOME_RESOURCE_LOCATOR);
    private static final String SCRIPTS_DIR = serverResourceLocator.getFullyQualifiedName(ServerRelativeFolders.SCRIPTS_FOLDER.getPath());
    private static final String RULE_SCRIPT = SCRIPTS_DIR + File.separator + "rule.ps1";
    private static final String POLICY_SCRIPT = SCRIPTS_DIR + File.separator + "policy.ps1";
    private static final String REMOVE_RULE_SCRIPT = SCRIPTS_DIR + File.separator + "remove_rule.ps1";
    private static final String REMOVE_POLICY_SCRIPT = SCRIPTS_DIR + File.separator + "remove_policy.ps1";

    private static final long RULE_WAIT_SECONDS = 30;

    private static final Log log = LogFactory.getLog(PowerShellDeployer.class.getName());
    
    public PowerShellDeployer() {
    }

    public String undeploy(DDACActiveDirectoryConfiguration adConfig, Collection<DACCentralAccessRule> rules, Collection<DACCentralAccessPolicy> policies) throws DDACDeploymentException {
        StringBuilder result = new StringBuilder();

        for (DACCentralAccessRule rule : rules) {
            String err = runRemoveRuleScript(adConfig, rule);
            addError(result, rule.getName(), err);
        }

        for (DACCentralAccessPolicy policy : policies) {
            String err = runRemovePolicyScript(adConfig, policy);
            addError(result, policy.getName(), err);
        }

        return result.toString();
    }

    public String deploy(DDACActiveDirectoryConfiguration adConfig, Collection<DACCentralAccessRule> rules, Collection<DACCentralAccessPolicy> policies) throws DDACDeploymentException {
        StringBuilder result = new StringBuilder();

        for (DACCentralAccessRule rule : rules) {
            String err = runRuleScript(adConfig, rule);
            addError(result, rule.getName(), err);
        }

        // If there is a new (as opposed to updated) rule, then we have to wait until
        // it gets added to AD before we can update the policies (or else a policy will
        // refer to a rule that doesn't exist). Unfortunately, there doesn't appear to
        // be a way to run PowerShell scripts sychronously. So, we wait.

        try {
            Thread.sleep(RULE_WAIT_SECONDS * 1000);
        } catch (InterruptedException e) {
        }

        for (DACCentralAccessPolicy policy : policies) {
            String err = runPolicyScript(adConfig, policy);
            addError(result, policy.getName(), err);
        }

        return result.toString();
    }

    private void addError(StringBuilder sb, String prefix, String err) {
        if (err != null && err.length() != 0) {
            sb.append(prefix);
            sb.append(": ");
            sb.append(err);
            sb.append("\n");
        }
    }

    /**
     * Build the arguments for the remove rule script, remove_rule.ps1, and then run it
     *
     * @param adConfig the active driectory config (used for user login and password)
     * @param rule the rule to remove
     */
    private String runRemoveRuleScript(DDACActiveDirectoryConfiguration adConfig, DACCentralAccessRule rule) throws DDACDeploymentException {
        ScriptArguments args = new ScriptArguments();

        args.add(rule.getServer());
        args.add(rule.getName());
        args.add(adConfig.getLogin());
        args.addHidden(adConfig.getPassword());

        return runScript(REMOVE_RULE_SCRIPT, args);
    }

    /**
     * Build the arguments for the rule script, rule.ps1, and then run it
     *
     * @param adConfig the active directory config (used for user login and password)
     * @param rule the rule to process
     */  
    private String runRuleScript(DDACActiveDirectoryConfiguration adConfig, DACCentralAccessRule rule) throws DDACDeploymentException {
        ScriptArguments args = new ScriptArguments();
    
        args.add(rule.getServer());
        args.add(rule.getName());
        args.add(rule.getIdentity());

        String description = rule.getDescription();

        if (description == null || description.equals("")) {
            description = "Rule generated from ACPL";
        }

        args.add(description);

        Collection<DACUserClaims> claims = rule.getUserClaims();

        StringBuilder claimString = new StringBuilder();

        for (DACUserClaims claim : claims) {
            claimString.append(convertClaim(rule, claim));
        }
         
        args.add(claimString.toString());

        args.add("(" + rule.getResourceCondition() + ")");
        args.add(adConfig.getLogin());
        args.addHidden(adConfig.getPassword());  // Don't display this in the log

        if (rule.getRuleType() == DACCentralAccessRule.DACRuleType.ACCESS) {
            args.add("current");
        } else {
            args.add("proposed");
        }

        return runScript(RULE_SCRIPT, args);
    }

    /**
     * Build the arguments for the remove policy script, remove_policy.ps1, and then run it
     *
     * @param adConfig
     * @param policy the policy to remove
     */
    private String runRemovePolicyScript(DDACActiveDirectoryConfiguration adConfig, DACCentralAccessPolicy policy) throws DDACDeploymentException {
        ScriptArguments args = new ScriptArguments();

        args.add(policy.getName());
        args.add(policy.getServer());
        args.add(adConfig.getLogin());
        args.addHidden(adConfig.getPassword());

        return runScript(REMOVE_POLICY_SCRIPT, args);
    }

    /**
     * Build the arguments for the policy script, policy.ps1, and then run it
     *
     * @param adConfig the active directory config (used for user login and password)
     * @param policy the policy to process
     */  
    private String runPolicyScript(DDACActiveDirectoryConfiguration adConfig, DACCentralAccessPolicy policy) throws DDACDeploymentException {
        ScriptArguments args = new ScriptArguments();

        args.add(policy.getName());
        args.add(policy.getServer());

        StringBuilder members = new StringBuilder();

        boolean first = true;
        for (String car : policy.getCARs()) {
            if (!first) {
                members.append(",");
            }
            first = false;
            members.append(car);
        }

        args.add(members.toString());
        args.add(adConfig.getLogin());
        args.addHidden(adConfig.getPassword());

        return runScript(POLICY_SCRIPT, args);
    }

    /**
     * Run the named script with the specified arguments. There is no easy way to determine when the script has finished executing
     *
     * @param scriptName the absolute path name of the script
     * @param args the arguments
     */  
    private String runScript(String scriptName, ScriptArguments args) throws DDACDeploymentException {
        StringBuilder powerShellCmd = new StringBuilder("powershell -F ");
        StringBuilder loggedCmd = new StringBuilder("powershell -F ");

        powerShellCmd.append(quote(scriptName));
        loggedCmd.append(quote(scriptName));

        for (SecretString arg : args) {
            powerShellCmd.append(" ");
            powerShellCmd.append(quoteAndEscape(arg.toString()));

            loggedCmd.append(" ");
            if (arg.isHidden()) {
                loggedCmd.append("XXXXXXXX");
            } else {
                loggedCmd.append(quoteAndEscape(arg.toString()));
            }
        }

        log.debug(loggedCmd.toString());

        String errorOut = "";

        try {
            Process proc = Runtime.getRuntime().exec(powerShellCmd.toString());
            InputStream err = proc.getErrorStream();
            proc.getOutputStream().close();
            errorOut = streamToString(err);
        } catch (IOException e) {
            log.error("Error when executing powershell: " + loggedCmd.toString());
            throw new DDACDeploymentException("Error when executing pwoershell: " + loggedCmd.toString(), e);
        }

        return errorOut;
    }

    private static String streamToString(InputStream in) throws IOException {
        StringBuilder res = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            res.append(line);
            res.append("\n");
        }
        br.close();
        return res.toString();
    }

    /**
     * Wrap the string in " "
     */
    private static String quote(String s) {
        return "\"" + s + "\"";
    }

    /**
     * Quote, and put a \ before each "
     */
    private static String quoteAndEscape(String s) {
        return quote(s.replaceAll("\"", "\\\\\""));
    }
            
    private String convertClaim(DACCentralAccessRule rule, DACUserClaims claim) {
        if (claim.getUserClaims().equals(DACDomainObjectFormatter.SDDL_TRUE)) {
            return "(A;;" + rule.getAction() + ";;;" + claim.getUserId() + ")";
        } else {
            return "(XA;;" + rule.getAction() + ";;;" + claim.getUserId() + ";(" + claim.getUserClaims() + "))";
        }
    }

    private class ScriptArguments implements Iterable<SecretString> {
        private List<SecretString> args;

        public ScriptArguments() {
            args = new ArrayList<SecretString>();
        }

        public void add(String arg) {
            args.add(new SecretString(arg));
        }

        public void addHidden(String arg) {
            args.add(new SecretString(arg, true));
        }
        
        public Iterator<SecretString> iterator() {
            return args.iterator();
        }
    }

    private class SecretString {
        private boolean hide;
        private String str;

        public SecretString(String str) {
            this(str, false);
        }

        public SecretString(String str, boolean hide) {
            this.hide = hide;
            this.str = str;
        }

        public boolean isHidden() {
            return hide;
        }

        public String toString() {
            return str;
        }
    }



}
