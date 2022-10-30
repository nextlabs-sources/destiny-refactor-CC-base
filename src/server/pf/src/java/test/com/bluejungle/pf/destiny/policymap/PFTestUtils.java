package com.bluejungle.pf.destiny.policymap;

/*
 * Created on Sep 11, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by extLabs Inc.,
 * Redwood City CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IAttribute;
import com.bluejungle.framework.expressions.IExpression;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.IRelation;
import com.bluejungle.pf.destiny.formatter.DomainObjectFormatter;
import com.bluejungle.pf.destiny.parser.DefaultPQLVisitor;
import com.bluejungle.pf.destiny.parser.DomainObjectBuilder;
import com.bluejungle.pf.destiny.parser.DomainObjectDescriptor;
import com.bluejungle.pf.destiny.parser.PQLException;
import com.bluejungle.pf.domain.destiny.action.DAction;
import com.bluejungle.pf.domain.destiny.action.IDAction;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundle;
import com.bluejungle.pf.domain.destiny.deployment.IDeploymentBundleV2;
import com.bluejungle.pf.domain.destiny.policy.IDPolicy;
import com.bluejungle.pf.domain.destiny.subject.Location;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;

/**
 * @author sgoldstein, sergey
 * @version $Id:
 *          //depot/branch/Destiny_1.5.1/main/src/server/pf/src/java/test/com/bluejungle/pf/destiny/policymap/PFTestUtils.java#1 $
 */

public class PFTestUtils {

    private static final Pattern VAR_PATTERN =
        Pattern.compile("[$]([0-9a-zA-Z_+-:@ ]+)[$]");

    /**
     * This method replaces all occurrences of $[a-z]$ with the IDs
     * of the corresponding elements looked up in the dictionary.
     *
     * @param input the string containing optional references to variables.
     * @param dictionary the dictionary from which to look up elements.
     * @return the input with variables replaced with the corresponding IDs.
     * @throws DictionaryException if the oepration cannot be performed.
     */
    public static String replaceDictionaryVariables(
        String input
    ,   IDictionary dictionary
    ) throws DictionaryException {
        Matcher matcher = VAR_PATTERN.matcher(input);
        StringBuffer res = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(
                res
            ,   getInternalKey(matcher.group(1), dictionary).toString()
            );
        }
        matcher.appendTail(res);
        return res.toString();
    }


    /**
     * Finds an ID of a dictionary element with the specified unique name.
     *
     * @param uniqueName the unique name to look up in the dictionary.
     * @param dictionary the dictionary from which to look up the unique name.
     * @return the ID of the element identified by its unique name.
     * @throws DictionaryException if the oepration cannot be performed.
     */
    public static Long getInternalKey(
        String uniqueName
    ,   IDictionary dictionary
    ) throws DictionaryException {
        IElementBase element = dictionary.getByUniqueName(
            uniqueName
        ,   dictionary.getLatestConsistentTime()
        );
        if (element == null) {
            throw new IllegalArgumentException(
                "'"+uniqueName+"' is not a name of a dictionary element."
            );
        }
        return element.getInternalKey();
    }

    /**
     * This method is for viewing the content of a bundle.
     * 
     * @param bundle the bundle to view.
     * @param raw indicates that the IDs should not be replaced with their sequence numbers. 
     * @return a string representing the content of the bundle.
     * @throws PQLException if the bundle is invalid.
     */
    public static String toString(IDeploymentBundle bundle, final boolean raw) throws PQLException {
        StringBuilder out = new StringBuilder("Deployment Bundle:\n");
        if (!bundle.isEmpty()) {
            final Map<Object,Object> subjIdCache = new HashMap<Object,Object>();
            final DomainObjectFormatter dof = new DomainObjectFormatter() {

                /**
                 * @see DomainObjectFormatter#formatRef(IPredicate)
                 */
                public void formatRelation(StringBuilder out, IRelation rel) {
                    IExpression lhs = rel.getLHS();
                    if (lhs == SubjectAttribute.USER_ID || lhs == SubjectAttribute.HOST_ID || lhs == SubjectAttribute.APP_ID) {
                        super.formatAttribute(out, (IAttribute) lhs);
                        out.append(rel.getOp());
                        out.append(cached(extractKey(rel.getRHS()), subjIdCache, raw));
                        return;
                    }
                    IExpression rhs = rel.getRHS();
                    if (rhs == SubjectAttribute.USER_ID || rhs == SubjectAttribute.HOST_ID || rhs == SubjectAttribute.APP_ID) {
                        out.append(cached(extractKey(rel.getLHS()), subjIdCache, raw));
                        out.append(rel.getOp());
                        super.formatAttribute(out, (IAttribute) rhs);
                        return;
                    }
                    super.formatRelation(out, rel);
                }

                Object extractKey(IExpression expr) {
                    if (expr instanceof Constant) {
                        Object val = ((Constant) expr).getValue().getValue();
                        if (val instanceof BigInteger) {
                            return new Long(((BigInteger) val).longValue());
                        } else {
                            return val;
                        }
                    } else {
                        return expr;
                    }
                }
            };

            // Sort all keys
            Collection<IDeploymentBundle.ISubjectKeyMapping> subjectKeyMappings =
                bundle.getSubjectKeyMappings();
            List<IDeploymentBundle.ISubjectKeyMapping> sortedSubjectKeyMappings =
                new ArrayList<IDeploymentBundle.ISubjectKeyMapping>();
            sortedSubjectKeyMappings.addAll(subjectKeyMappings);
            Collections.sort(
                sortedSubjectKeyMappings
            ,   new Comparator<IDeploymentBundle.ISubjectKeyMapping>() {
                    public int compare(
                        IDeploymentBundle.ISubjectKeyMapping lhs
                    ,   IDeploymentBundle.ISubjectKeyMapping rhs) {
                        String lid = lhs.getUid();
                        String rid = rhs.getUid();
                        return nullSafeCompare(lid, rid);
                    }
                }
            );

            // Pre-cache subject keys
            for (IDeploymentBundle.ISubjectKeyMapping mapping : sortedSubjectKeyMappings) {
                cached(mapping.getId(), subjIdCache, raw);
            }

            // Format all policies and locations
            out.append("Deployment entities:\n");
            DomainObjectBuilder.processInternalPQL(
                bundle.getDeploymentEntities()
            ,   new DefaultPQLVisitor() {
                    public void visitLocation(DomainObjectDescriptor descriptor, Location location) {
                        dof.formatLocation(descriptor, location);
                    }
                    public void visitPolicy(DomainObjectDescriptor descriptor, IDPolicy policy) {
                        dof.formatPolicyDef(descriptor, policy);
                    }
                }
            );
            out.append(dof.getPQL());
            out.append("\nPolicies that apply to all users: ");
            out.append(bundle.getPoliciesForAllUsers());
            out.append("\nPolicies that apply to all hosts: ");
            out.append(bundle.getPoliciesForAllHosts());
            out.append("\nPolicies that apply to all applications: ");
            out.append(bundle.getPoliciesForAllApps());
            out.append("\nSubject-Policy Mappings:\n");

            Map<Long,String> subjectSortKeys = new HashMap<Long,String>();
            for (IDeploymentBundle.ISubjectKeyMapping subjectKeyMapping : sortedSubjectKeyMappings) {
                if (!subjectSortKeys.containsKey(subjectKeyMapping.getId())) {
                    subjectSortKeys.put(subjectKeyMapping.getId(), subjectKeyMapping.getUid());
                }
            }

            toString(out, bundle.getSubjectToPolicy(), subjIdCache, subjectSortKeys, raw);

            out.append("Action-Policy Mappings:\n");

            // We probably should arrange for both old and new style bundles to be tested
            if (bundle instanceof IDeploymentBundleV2) {
                IDeploymentBundleV2 bundleV2 = (IDeploymentBundleV2)bundle;
                List<String> sortedActionNameList = new ArrayList<String>();
                for (IDAction action : DAction.allActions()) {
                    sortedActionNameList.add(action.getName());
                }
                Collections.sort(sortedActionNameList);
                toString(out, bundleV2.getActionNameToPolicy(), sortedActionNameList);
            } else {
                Map<Long,String> actionSortKeys = new HashMap<Long, String>();
                for (Object actionObj : DAction.allActions()) {
                    IDAction action = (IDAction)actionObj;
                    actionSortKeys.put(action.getId(), action.getName());
                }

                toString(out, bundle.getActionToPolicy(), new HashMap<Object,Object>(), actionSortKeys, raw);
            }

            out.append("Subject-Group Mappings:\n");
            toString(out, bundle.getSubjectToGroup(), subjIdCache, subjectSortKeys, raw);

            out.append("Subject keys:\n");
            for (IDeploymentBundle.ISubjectKeyMapping subjectKeyMapping : sortedSubjectKeyMappings) {
                out.append(subjectKeyMapping.getUid());
                out.append(" (");
                out.append(subjectKeyMapping.getSubjectType());
                out.append('-');
                out.append(subjectKeyMapping.getUidType());
                out.append(") -> ");
                out.append(cached(subjectKeyMapping.getId(), subjIdCache, raw));
                out.append('\n');
            }
        } else {
            out.append("<EMPTY>");
        }
        return out.toString();
    }

    private static void toString(
        StringBuilder out
    ,   Map<Long,BitSet> idMappings
    ,   Map<Object,Object> idCache
    ,   final Map<Long,String> sortKeys
    ,   boolean raw) {
        SortedMap<Long,BitSet> sortedIdMap = new TreeMap<Long,BitSet>(new Comparator<Long>() {
            public int compare(Long lhs, Long rhs) {
                String lid = sortKeys.get(lhs);
                String rid = sortKeys.get(rhs);
                return nullSafeCompare(lid, rid);
            }
        });

        sortedIdMap.putAll(idMappings);

        toStringImpl(out, sortedIdMap, idCache, raw);
    }

    /**
     * @param out
     * @param sortedIdMap
     * @param subjIdCache
     */
    private static void toStringImpl(
        StringBuilder out
    ,   SortedMap<Long,BitSet> sortedIdMap
    ,   Map<Object,Object> subjIdCache
    ,   boolean raw) {
        for (Map.Entry<Long,BitSet> nextMapping : sortedIdMap.entrySet()) {
            out.append(cached(nextMapping.getKey(), subjIdCache, raw));
            out.append(" -> ");
            out.append(nextMapping.getValue());
            out.append('\n');
        }
    }
    
    /**
     * @param out
     * @param actionNameMapping from bundle
     * @param actionsNames sorted list of action names
     */
    private static void toString(
        StringBuilder out
    ,   Map<String, BitSet> actionNameMapping
    ,   List<String> actionNames) {
        for (String name: actionNames) {
            out.append(name);
            out.append(" -> ");
            out.append(actionNameMapping.get(name));
            out.append('\n');
        }
    }

    private static Object cached(Object key, Map<Object,Object> cache, boolean raw) {
        if (raw) {
            return key;
        }
        if (cache.containsKey(key)) {
            return cache.get(key);
        } else {
            Integer res = cache.size();
            cache.put(key, res);
            return res;
        }
    }

    private static int nullSafeCompare(String a, String b) {
        if (a == null || b == null) {
            if (a == null) {
                return b == null ? 0 : 1;
            } else {
                return -1;
            }
        } else {
            return a.toLowerCase().compareTo(b.toLowerCase());
        }
    }

}
