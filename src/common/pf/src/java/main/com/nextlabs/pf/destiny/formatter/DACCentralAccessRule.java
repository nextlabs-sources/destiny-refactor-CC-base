/*
 * Created on Nov 16, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/pf/src/java/main/com/nextlabs/pf/destiny/formatter/DACCentralAccessRule.java#1 $:
 */

package com.nextlabs.pf.destiny.formatter;

import java.util.ArrayList;
import java.util.List;

public class DACCentralAccessRule {
    public static enum DACRuleType { ACCESS, PROPOSED };

    private String action;

    private List<DACUserClaims> userClaims;
    private String description;
    private String name;
    private String identity;
    private String resourceCondition;
    private String server;
    private DACRuleType ruleType;

    public DACCentralAccessRule() {
        userClaims = new ArrayList<DACUserClaims>();
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<DACUserClaims> getUserClaims() {
        return userClaims;
    }

    public void setUserClaims(String userId, String userClaims) {
        this.userClaims.add(new DACUserClaims(userId, userClaims));
    }

    public void setUserClaims(String userClaims) {
        this.userClaims.add(new DACUserClaims(userClaims));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static String getIdentity(String name, String identity) {
        return "CN=" + name + "," + identity;
    }

    public String getIdentity() {
        return "CN="+getName() + "," + identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getResourceCondition() {
        return resourceCondition;
    }

    public void setResourceCondition(String resourceCondition) {
        this.resourceCondition = resourceCondition;
    }
    
    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setRuleType(DACRuleType ruleType) {
        this.ruleType = ruleType;
    }

    public DACRuleType getRuleType() {
        return ruleType;
    }

    private static final String ALL_USERS = "AU";

    public static class DACUserClaims {
        private String userId;
        private String userClaimExpression;

        public DACUserClaims(String userClaimExpression) {
            this(ALL_USERS, userClaimExpression);
        }

        public DACUserClaims(String userId, String userClaimExpression) {
            this.userId = userId;
            this.userClaimExpression = userClaimExpression;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserClaims() {
            return userClaimExpression;
        }
    }
}
