/*
 * Created on June 24, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */

package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ldif.ILDIFEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.ElementCreator;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.LDAPEnrollmentHelper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.ActiveDirectorySIDConverter;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.ElementBaseIterator;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.dictionary.IReferenceable;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.expressions.BooleanOp;
import com.bluejungle.framework.expressions.CompositePredicate;
import com.bluejungle.framework.expressions.Constant;
import com.bluejungle.framework.expressions.IPredicate;
import com.bluejungle.framework.expressions.RelationOp;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPModifyRequest;
import com.novell.ldap.LDAPAttribute;


/**
 * LdifElementFactory create element using LDAPMessage from LDIF file  
 * By the nature of LDIF change API, we can not use DictionaryKey to retrieve element
 * We have to use DN to get element from dictionary
 * 
 * @author atian
 *
 */

public class LdifElementFactory extends ElementCreator {
    
    private static final Log LOG = LogFactory.getLog(LdifElementFactory.class);
    

    private static final String SID_PREFIX = "S-1";
    
    private final Map<IElementType, Map<String, Set<IElementField>>> lookupMap;
    
    private final IElementField memberField;
    
    private final boolean lookupAllEnrollment;

    /**
     * Constructor
     * @param enrollmentWrapper
     * @param dictionary
     * @throws EnrollmentValidationException
     */
    public LdifElementFactory (ILDIFEnrollmentWrapper enrollmentWrapper, IDictionary dictionary ) 
            throws EnrollmentValidationException {
        super(enrollmentWrapper, dictionary);
        lookupMap = new HashMap<IElementType, Map<String,Set<IElementField>>>();
        
        lookupMap.put(USER_TYPE, reverse(enrollmentWrapper.getSearchableAttributesForUsers()));
        lookupMap.put(APP_TYPE, reverse(enrollmentWrapper.getSearchableAttributesForApplications()));
        lookupMap.put(HOST_TYPE, reverse(enrollmentWrapper.getSearchableAttributesForComputers()));
        
        lookupAllEnrollment = enrollmentWrapper.isGroupMemberFromAllEnrollment();
        
        IElementType userType;
        try {
            userType = enrollment.getDictionary().getType(ElementTypeEnumType.USER.getName());
        } catch (DictionaryException e) {
            throw new EnrollmentValidationException(e);
        }
        
        if(lookupAllEnrollment){
            String memberKey = enrollmentWrapper.getMemberAttributeKey();
            if (memberKey == null) {
                throw new EnrollmentValidationException("Since you are looking up all enrollment, you must specific '"
                        + LdifEnrollmentProperties.MEMBER_ATTRIBUTE_KEY
                        + "'.");
            }
            
            memberField = userType.getField(memberKey);
            if (memberField == null) {
                throw new EnrollmentValidationException("Please check '"
                      + LdifEnrollmentProperties.MEMBER_ATTRIBUTE_KEY
                      + "' is defined correctly.");
            }
        } else {
            memberField = null;
        }
    }
    
    private Map<String, Set<IElementField>> reverse(Map<IElementField, String> map) {
        Map<String, Set<IElementField>> reverseMap = new HashMap<String, Set<IElementField>>();
        for (Map.Entry<IElementField, String> e : map.entrySet()) {
            Set<IElementField> values = reverseMap.get(e.getValue());
            if(values == null){
                values = new HashSet<IElementField>();
                reverseMap.put(e.getValue(), values);
            }
            values.add(e.getKey());
        }
        return reverseMap;
    }

    /**
     * get SID from LDAPAttribute 
     * converted by Active Directory SID converter tool
     */
    public Object getSidValue(LDAPAttribute ldapAttr) {
        String sid = ldapAttr.getStringValue();
    
        if (sid == null) {
            return null;
        } else if (sid.startsWith(SID_PREFIX)) {
            return sid;
        } else { // Base64 encoding of byte string
            return ActiveDirectorySIDConverter.sidToString(ldapAttr.getByteValue());
        }
    }
    
    private String getSidValue(String stringValue, byte[] byteValue) {
        if ( stringValue == null ) {
            return null;
        }else if (stringValue.startsWith(SID_PREFIX)) {
            return stringValue;
        } else { // Base64 encoding of byte string
            return ActiveDirectorySIDConverter.sidToString(byteValue);
        }
    }

    /**
     * getElementByDN() method fetches element from dictionary by given DN
     * @param DN
     * @return element
     * @throws DictionaryException
     */
    public IMElementBase getElementByDN(String DN) throws DictionaryException {
        DictionaryPath path = LDAPEnrollmentHelper.getDictionaryPathFromDN(DN);
        List<IReferenceable> list = enrollment.getProvisionalReferences(Collections.singletonList(path));
        if(list == null || list.isEmpty()){
            return null;
        }
        
        assert list.size() == 1;
        assert list.get(0) instanceof IMElementBase;
        
        return (IMElementBase) list.get(0);
    }
    
    protected boolean setGroupMembers(IMGroup group, LDAPEntry entry, boolean isExistingGroup)
            throws EnrollmentSyncException {
        if (memberField == null) {
            // using old implementation
            return super.setGroupMembers(group, entry, isExistingGroup);
        }
        
        
        boolean isChanged = false;
        
        final String membershipAttributeName = enrollmentWrapper.getMembershipAttributeName();
        final LDAPAttribute attr = entry.getAttribute(membershipAttributeName);
        
        /**
         * 1. get the membership from ad
         * 2. get all membership from dictionary
         * 3. covert them to ProvisionalReferences
         * 4. if the ad_membership exists in dictionary, do nothing. Otherwise, add it.
         * 5. after checking all ad_membership, remove all remaining dictionary_relationship.
         */
        
        String errorMessage = "setGroupMembers";
        
        try {
            Queue<IMElementBase> memberElements = new LinkedList<IMElementBase>();
            if (attr != null && attr.size() > 0) {
                final boolean isMemberFieldSid = UserReservedFieldEnumType.WINDOWS_SID.getName()
                        .equals(memberField.getName());
                
                final IDictionary dictionary = this.enrollment.getDictionary();
                
                String[] attrStringValues = attr.getStringValueArray();
                byte[][] attrByteValues = isMemberFieldSid ? attr.getByteValueArray() : null;
                
                for (int i = 0, size = attr.size(); i < size; i++) {
                    
                    String memberValue = isMemberFieldSid ? getSidValue(attrStringValues[i], attrByteValues[i]) : attrStringValues[i];
                    
                    IMElementBase element = null;
                    ElementBaseIterator<IMElement> itor = null;
                    try{
                        IPredicate condition = memberField.buildRelation(RelationOp.EQUALS,Constant.build(memberValue));
                        if (!lookupAllEnrollment) {
                            Collection<IPredicate> conditionPredicates = new ArrayList<IPredicate>(2);
                            conditionPredicates.add(dictionary.condition(enrollment));
                            conditionPredicates.add(condition);
                            condition = new CompositePredicate(BooleanOp.AND, conditionPredicates);
                        }
                        
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Looking member '" + memberValue + "' in group " + group.getUniqueName() + ". Condition = " + condition);
                        }
                        
                        //TODO Don't save the members, the members are normalized
                        itor = (ElementBaseIterator<IMElement>)dictionary.query(
                                condition
                              , enrollmentWrapper.getEnrollmentStartTime()
                              , null
                              , null
                        );
                        
                        if (itor.hasNext()) {
                            element = itor.next(false);
                            memberElements.add(element);
                        } else {
                            logWarn("Unable to find group member '" + memberValue + "' in group " + group.getUniqueName());
                        }
                    } finally{
                        if (itor != null) {
                            itor.close();
                        }
                    }
                }
            }
            
            if (!memberElements.isEmpty()) {
                Map<DictionaryPath, IReferenceable> existingChildren;
                if (enrollmentWrapper.isUpdate() && isExistingGroup) {
                    errorMessage = "Unable to acquire the child elements of group, " + group.getUniqueName() + ".";
                    existingChildren = getAllChildren(group);
                } else {
                    existingChildren = Collections.EMPTY_MAP;
                }
                
                //TODO can we tell what the reference is?
                errorMessage = "An error in provisional reference has been detected.";
                final boolean isTraceEnabled = LOG.isTraceEnabled();

                for(IMElementBase memberElement : memberElements) {
                    if (existingChildren.remove(memberElement.getPath()) == null) {
                        group.addChild(memberElement);
                        isChanged = true;
                    }
                    if (isTraceEnabled) {
                        LOG.trace("Adding member " + memberElement.getPath().toString() + " to "
                                + group.getPath().toString());
                    }
                }
                
                for (IReferenceable removePath : existingChildren.values()) {
                    errorMessage = "Unable to remove group members, " 
                        + (removePath != null ? removePath.getPath() : "<no_dictionary_path>");
                    group.removeChild(removePath);
                    isChanged = true;
                }
            } else {
                errorMessage = "Failed to remove group members";
                if (enrollmentWrapper.isUpdate() && isExistingGroup) {
                    removeAllDirectGroupMembers(group);
                }
                if(attr != null){
                    logWarn("A group member attribute is empty: '" + membershipAttributeName
                            + "' in '" + entry.getDN() + "'. Please supply a value.");
                } else {
                    LOG.trace("A group member attribute is missing: '" + membershipAttributeName
                            + "' in '" + entry.getDN() + "'." );
                }
            }
        } catch (DictionaryException e) {
            throw new EnrollmentSyncException(errorMessage, e, entry.getDN());
        }
        return isChanged;
    }
    
    
    /**
     * Create dictionary element from LDAP modify request
     * @param msg the LDAPModifyRequest
     * @return element created
     * @throws DictionaryException
     * @throws EnrollmentFailedException
     */
    public IMElementBase createModifyElement(LDAPModifyRequest msg) throws DictionaryException, EnrollmentSyncException {
        String DN = msg.getDN();
        IMElementBase mod = getElementByDN(DN);
        if ((mod != null) && (mod instanceof IMElement)) {

            IElementType type = ((IMElement) mod).getType();
            Map<String, Set<IElementField>> searchableAttributes = lookupMap.get(type);
            if (searchableAttributes == null) {
                throw new EnrollmentSyncException("Invalid element type", DN);
            }

            LDAPModification[] mods = msg.getModifications();
            for (LDAPModification m : mods) {
                if ((m != null) && (m.getAttribute() != null)) {
                    Set<IElementField> fields =
                            searchableAttributes.get(m.getAttribute().getName());
                    if (m.getOp() == LDAPModification.DELETE) { // if delete op, set null value
                        for (IElementField field : fields) {
                            ((IMElement) mod).setValue(field, null);
                        }
                    } else { //if ADD and REPLACE op, set new value (NOTE: dictionary only supports single value) 
                        for (IElementField field : fields) {
                            Object fieldValue = getFieldValue(field, m.getAttribute(), DN);
                            ((IMElement) mod).setValue(field, fieldValue);
                        }
                    }
                }
            }
        }    
        return mod;
    }
}
