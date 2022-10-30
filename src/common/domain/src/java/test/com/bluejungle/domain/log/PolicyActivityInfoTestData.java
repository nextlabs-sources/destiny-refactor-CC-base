/*
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.domain.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.bluejungle.domain.action.ActionEnumType;
import com.bluejungle.domain.log.FromResourceInformation;
import com.bluejungle.domain.log.PolicyActivityInfo;
import com.bluejungle.domain.log.ResourceInformationTestData;
import com.bluejungle.domain.log.ToResourceInformation;
import com.bluejungle.domain.policydecision.PolicyDecisionEnumType;
import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.framework.utils.Pair;
import com.bluejungle.framework.utils.TestUtils;
import com.nextlabs.domain.log.PolicyActivityInfoV3;
import com.nextlabs.domain.log.PolicyActivityInfoV4;
import com.nextlabs.domain.log.PolicyActivityInfoV5;


/**
 * @author sasha
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/common/domain/src/java/test/com/bluejungle/domain/log/PolicyActivityInfoTestData.java#1 $
 *
 */
public class PolicyActivityInfoTestData {
    public static final PolicyActivityInfo generateRandom() {
        FromResourceInformation from = ResourceInformationTestData.generateRandomFrom();
        boolean hasTo = TestUtils.rand.nextBoolean();
        ToResourceInformation to = hasTo ? ResourceInformationTestData.generateRandomTo() : null;
        String userName = TestUtils.genRandomString(20);
        long userId = TestUtils.rand.nextLong();
        String hostName = TestUtils.genRandomString(32);
        String hostIP = TestUtils.getRandomIPAddress();
        long hostId = TestUtils.rand.nextLong();
        String appName = TestUtils.genRandomString(27);
        long appId = TestUtils.rand.nextLong();
        ActionEnumType action = ActionEnumType.getActionEnum(TestUtils.rand.nextInt(ActionEnumType.numElements()));
        PolicyDecisionEnumType decision = PolicyDecisionEnumType.getPolicyDecisionEnum(TestUtils.rand.nextInt(PolicyDecisionEnumType.numElements()));
        long decisionRequestId = TestUtils.rand.nextLong();
        long ts = TestUtils.rand.nextLong();
        int level = TestUtils.rand.nextInt(3);
        DynamicAttributes fromResAttrs = null;
        int nbAttr = TestUtils.rand.nextInt(4);
        if (nbAttr > 0) {
            fromResAttrs = new DynamicAttributes();
            for (int i = 0; i < nbAttr; i++){
                String key = "key" + i;

                if (i%2 == 0) {
                    String value = "value" + i;
                    fromResAttrs.put(key, value);
                } else {
                    for (int j = 0; j < 4; j++) {
                        String value = "value" + j;
                        fromResAttrs.add(key, value);
                    }
                }
            }
        }

        PolicyActivityInfo info = new PolicyActivityInfo(from,
                to,
                userName,
                userId,
                hostName,
                hostIP,
                hostId,
                appName,
                appId,
                action,
                decision,
                decisionRequestId,
                ts,
                level, 
                fromResAttrs);
        return info;
    }
    
    public static final PolicyActivityInfo[] generateRandom(int numEntries) {
        PolicyActivityInfo[] rv = new PolicyActivityInfo[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandom();
        }
        return rv;
    }

    public static final PolicyActivityInfoV2 generateRandomV2() {
        FromResourceInformation from = ResourceInformationTestData.generateRandomFrom();
        boolean hasTo = TestUtils.rand.nextBoolean();
        ToResourceInformation to = hasTo ? ResourceInformationTestData.generateRandomTo() : null;
        String userName = TestUtils.genRandomString(20);
        long userId = TestUtils.rand.nextLong();
        String hostName = TestUtils.genRandomString(32);
        String hostIP = TestUtils.getRandomIPAddress();
        long hostId = TestUtils.rand.nextLong();
        String appName = TestUtils.genRandomString(27);
        long appId = TestUtils.rand.nextLong();
        String action = TestUtils.genRandomString(6);
        PolicyDecisionEnumType decision = PolicyDecisionEnumType.getPolicyDecisionEnum(TestUtils.rand.nextInt(PolicyDecisionEnumType.numElements()));
        long decisionRequestId = TestUtils.rand.nextLong();
        long ts = TestUtils.rand.nextLong();
        int level = TestUtils.rand.nextInt(3);
        DynamicAttributes fromResAttrs = null;
        int nbAttr = TestUtils.rand.nextInt(4);
        if (nbAttr > 0) {
            fromResAttrs = new DynamicAttributes();
            for (int i = 0; i < nbAttr; i++){
                String key = "key" + i;

                if (i%2 == 0) {
                    String value = "value" + i;
                    fromResAttrs.put(key, value);
                } else {
                    for (int j = 0; j < 4; j++) {
                        String value = "value" + j;
                        fromResAttrs.add(key, value);
                    }
                }
            }
        }

        PolicyActivityInfoV2 info = new PolicyActivityInfoV2(from,
                                                             to,
                                                             userName,
                                                             userId,
                                                             hostName,
                                                             hostIP,
                                                             hostId,
                                                             appName,
                                                             appId,
                                                             action,
                                                             decision,
                                                             decisionRequestId,
                                                             ts,
                                                             level, 
                                                             fromResAttrs);
        return info;
    }
    
    public static final PolicyActivityInfoV2[] generateRandomV2(int numEntries) {
        PolicyActivityInfoV2[] rv = new PolicyActivityInfoV2[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomV2();
        }
        return rv;
    }

    public static final PolicyActivityInfoV3 generateRandomV3() {
        FromResourceInformation from = ResourceInformationTestData.generateRandomFrom();
        boolean hasTo = TestUtils.rand.nextBoolean();
        ToResourceInformation to = hasTo ? ResourceInformationTestData.generateRandomTo() : null;
        String userName = TestUtils.genRandomString(20);
        long userId = TestUtils.rand.nextLong();
        String hostName = TestUtils.genRandomString(32);
        String hostIP = TestUtils.getRandomIPAddress();
        long hostId = TestUtils.rand.nextLong();
        String appName = TestUtils.genRandomString(27);
        long appId = TestUtils.rand.nextLong();
        String action = TestUtils.genRandomString(6);
        PolicyDecisionEnumType decision = PolicyDecisionEnumType.getPolicyDecisionEnum(TestUtils.rand.nextInt(PolicyDecisionEnumType.numElements()));
        long decisionRequestId = TestUtils.rand.nextLong();
        long ts = TestUtils.rand.nextLong();
        int level = TestUtils.rand.nextInt(3);
        DynamicAttributes fromResAttrs = null;
        int nbAttr = TestUtils.rand.nextInt(4);
        if (nbAttr > 0) {
            fromResAttrs = new DynamicAttributes();
            for (int i = 0; i < nbAttr; i++){
                String key = "key" + i;

                if (i%2 == 0) {
                    String value = "value" + i;
                    fromResAttrs.put(key, value);
                } else {
                    for (int j = 0; j < 4; j++) {
                        String value = "value" + j;
                        fromResAttrs.add(key, value);
                    }
                }
            }
        }

        PolicyActivityInfoV3 info = new PolicyActivityInfoV3(from,
                                                             to,
                                                             userName,
                                                             userId,
                                                             hostName,
                                                             hostIP,
                                                             hostId,
                                                             appName,
                                                             appId,
                                                             action,
                                                             decision,
                                                             decisionRequestId,
                                                             ts,
                                                             level, 
                                                             fromResAttrs);
        return info;
    }
    
    public static final PolicyActivityInfoV3[] generateRandomV3(int numEntries) {
        PolicyActivityInfoV3[] rv = new PolicyActivityInfoV3[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomV3();
        }
        return rv;
    }

    public static final PolicyActivityInfoV4 generateRandomV4() {
        FromResourceInformation from = ResourceInformationTestData.generateRandomFrom();
        boolean hasTo = TestUtils.rand.nextBoolean();
        ToResourceInformation to = hasTo ? ResourceInformationTestData.generateRandomTo() : null;
        String userName = TestUtils.genRandomString(20);
        long userId = TestUtils.rand.nextLong();
        String hostName = TestUtils.genRandomString(32);
        String hostIP = TestUtils.getRandomIPAddress();
        long hostId = TestUtils.rand.nextLong();
        String appName = TestUtils.genRandomString(27);
        long appId = TestUtils.rand.nextLong();
        String action = TestUtils.genRandomString(6);
        PolicyDecisionEnumType decision = PolicyDecisionEnumType.getPolicyDecisionEnum(TestUtils.rand.nextInt(PolicyDecisionEnumType.numElements()));
        long decisionRequestId = TestUtils.rand.nextLong();
        long ts = TestUtils.rand.nextLong();
        int level = TestUtils.rand.nextInt(3);
        DynamicAttributes fromResAttrs = null;
        int nbAttr = TestUtils.rand.nextInt(4);
        if (nbAttr > 0) {
            fromResAttrs = new DynamicAttributes();
            for (int i = 0; i < nbAttr; i++){
                String key = "key" + i;

                if (i%2 == 0) {
                    String value = "value" + i;
                    fromResAttrs.put(key, value);
                } else {
                    for (int j = 0; j < 4; j++) {
                        String value = "value" + j;
                        fromResAttrs.add(key, value);
                    }
                }
            }
        }

        List<IPair<String, String>> evaluationAnnotations = new ArrayList<IPair<String, String>>();
        nbAttr = TestUtils.rand.nextInt(4);
        if (nbAttr > 0) {
            for (int i = 0; i < nbAttr; i++) {
                evaluationAnnotations.add(new Pair("key" + i, "value"+i));
                
            }
        }

        PolicyActivityInfoV4 info = new PolicyActivityInfoV4(from,
                                                             to,
                                                             userName,
                                                             userId,
                                                             hostName,
                                                             hostIP,
                                                             hostId,
                                                             appName,
                                                             appId,
                                                             action,
                                                             decision,
                                                             decisionRequestId,
                                                             ts,
                                                             level, 
                                                             fromResAttrs,
                                                             evaluationAnnotations);
        return info;
    }
    
    public static final PolicyActivityInfoV4[] generateRandomV4(int numEntries) {
        PolicyActivityInfoV4[] rv = new PolicyActivityInfoV4[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomV4();
        }
        return rv;
    }
    
    public static final PolicyActivityInfoV5 generateRandomV5() {
        FromResourceInformation from = ResourceInformationTestData.generateRandomFrom();
        boolean hasTo = TestUtils.rand.nextBoolean();
        ToResourceInformation to = hasTo ? ResourceInformationTestData.generateRandomTo() : null;
        String userName = TestUtils.genRandomString(20);
        long userId = TestUtils.rand.nextLong();
        String hostName = TestUtils.genRandomString(32);
        String hostIP = TestUtils.getRandomIPAddress();
        long hostId = TestUtils.rand.nextLong();
        String appName = TestUtils.genRandomString(27);
        long appId = TestUtils.rand.nextLong();
        String action = TestUtils.genRandomString(6);
        PolicyDecisionEnumType decision = PolicyDecisionEnumType.getPolicyDecisionEnum(TestUtils.rand.nextInt(PolicyDecisionEnumType.numElements()));
        long decisionRequestId = TestUtils.rand.nextLong();
        long ts = TestUtils.rand.nextLong();
        int level = TestUtils.rand.nextInt(3);
        Map<String,DynamicAttributes> attributesMap = null;
        int nbAttr = TestUtils.rand.nextInt(4);
        if (nbAttr > 0) {
        	attributesMap = new HashMap<String,DynamicAttributes>();
            for (int i = 0; i < nbAttr; i++){
                String key = "key" + i;

                if (i%2 == 0) {
                	DynamicAttributes value = new DynamicAttributes();
                	value.add("value" + i, "value" + i);
                    attributesMap.put(key, value);
                } else {
                    for (int j = 0; j < 4; j++) {
                    	DynamicAttributes value =new DynamicAttributes();
                    	value.add("value" + j, "value" + j);
                        attributesMap.put(key, value);
                    }
                }
            }
        }

        List<IPair<String, String>> evaluationAnnotations = new ArrayList<IPair<String, String>>();
        nbAttr = TestUtils.rand.nextInt(4);
        if (nbAttr > 0) {
            for (int i = 0; i < nbAttr; i++) {
                evaluationAnnotations.add(new Pair("key" + i, "value"+i));
                
            }
        }

        PolicyActivityInfoV5 info = new PolicyActivityInfoV5(from,
                                                             to,
                                                             userName,
                                                             userId,
                                                             hostName,
                                                             hostIP,
                                                             hostId,
                                                             appName,
                                                             appId,
                                                             action,
                                                             decision,
                                                             decisionRequestId,
                                                             ts,
                                                             level, 
                                                             attributesMap,
                                                             evaluationAnnotations);
        return info;
    }
    
    public static final PolicyActivityInfoV5[] generateRandomV5(int numEntries) {
        PolicyActivityInfoV5[] rv = new PolicyActivityInfoV5[numEntries];
        for (int i = 0; i < numEntries; i++) {
            rv[i] = generateRandomV5();
        }
        return rv;
    }
}
