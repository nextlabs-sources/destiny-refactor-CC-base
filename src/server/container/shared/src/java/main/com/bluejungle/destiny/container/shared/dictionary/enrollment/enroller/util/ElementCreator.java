/*
 * Created on April 24, 2006
 *
 * All sources, binaries and HTML pages (C) copyright 2006 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.hibernate.collection.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.IFormatter;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.EmailAddressFormatter;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryKey;
import com.bluejungle.dictionary.DictionaryPath;
import com.bluejungle.dictionary.ElementBaseIterator;
import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IElementVisitor;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.dictionary.IReferenceable;
import com.bluejungle.domain.enrollment.ApplicationReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ComputerReservedFieldEnumType;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;
import com.bluejungle.framework.datastore.hibernate.usertypes.StringArrayAsString;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.IPair;
import com.bluejungle.framework.utils.Pair;
import com.nextlabs.domain.enrollment.ContactReservedFieldEnumType;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;

/**
 * @author atian
 * @version $Id:
 */


/**
 * ElementCreator class is designed for creating Elements / Groups based on LDAP Entry
 *
 * 1. It contains Rfc Filter Evaluators for matching the expression given by enrollment
 *    configuration with a given LDAP entry object.
 *
 * 2. After evaluation, we can determine what is the element type of LDAP Entry
 *
 * 3. We will create the element/group object based on element type
 *
 */
public abstract class ElementCreator {
    public static final int STRING_COLUMN_LIMIT = 4000;
    
    protected static final IPair<SyncResultEnum, IMElement> IGNORE_ELEMENT = 
        new Pair<SyncResultEnum, IMElement>(SyncResultEnum.IGNORE_ENTRY, null);
    
    protected static final IPair<SyncResultEnum, IMElement> FAILED_ELEMENT = 
        new Pair<SyncResultEnum, IMElement>(SyncResultEnum.ERROR_ENTRY, null);
    
    protected static final IPair<SyncResultEnum, IElementBase> IGNORE_ELEMENT_BASE = 
        new Pair<SyncResultEnum, IElementBase>(SyncResultEnum.IGNORE_ENTRY, null);
    
    protected static final IPair<SyncResultEnum, IElementBase> FAILED_ELEMENT_BASE = 
        new Pair<SyncResultEnum, IElementBase>(SyncResultEnum.ERROR_ENTRY, null);
    
    private static final Log LOG = LogFactory.getLog(ElementCreator.class);
    
    private static final String DEFAULT_DISPLAY_NAME_ATTR = "CN";
    
    private static final int KEEP_LAST_N_WARNING_MESSAGES = 5;
    
    protected final IElementType USER_TYPE;
    protected final IElementType CONTACT_TYPE;
    protected final IElementType HOST_TYPE;
    protected final IElementType APP_TYPE;

    protected final ILDAPEnrollmentWrapper enrollmentWrapper;
    protected final IEnrollment enrollment;

    protected final RfcFilterEvaluator userTypeDetector;
    protected final RfcFilterEvaluator contactTypeDetector;
    protected final RfcFilterEvaluator hostTypeDetector;
    protected final RfcFilterEvaluator appTypeDetector;
    protected final RfcFilterEvaluator groupTypeDetector;
    protected final RfcFilterEvaluator structureTypeDetector;
    protected final RfcFilterEvaluator otherTypeDetector;

    private final String groupNamePrefix;
    
    private int warningCount;
    private Queue<String> lastNWarningMessages;
    
    private IFormatter<String[], String[]> mailFormatter;
    
    /**
     * ElementTypeEvaluator() constructor
     *   create element type identifiers in construction method
     * @param enrollmentWrapper the LDAPEnrollmentWrapper object
     * @param dictionary the dictionary object
     */
    public ElementCreator(ILDAPEnrollmentWrapper enrollmentWrapper, IDictionary dictionary)
            throws EnrollmentValidationException {
        warningCount = 0;
        lastNWarningMessages = new LinkedList<String>();
        
        this.enrollmentWrapper = enrollmentWrapper;
        this.enrollment = enrollmentWrapper.getEnrollment();

        userTypeDetector = enrollmentWrapper.enrollUsers() 
                ? new RfcFilterEvaluator(enrollmentWrapper.getUserIdentification()) 
                : RfcFilterEvaluator.ALWAYS_FALSE;

        contactTypeDetector = enrollmentWrapper.enrollContacts() 
                ? new RfcFilterEvaluator(enrollmentWrapper.getContactIdentification()) 
                : RfcFilterEvaluator.ALWAYS_FALSE;

        hostTypeDetector = enrollmentWrapper.enrollComputers() 
                ? new RfcFilterEvaluator(enrollmentWrapper.getComputerIdentification()) 
                : RfcFilterEvaluator.ALWAYS_FALSE;

        appTypeDetector = enrollmentWrapper.enrollApplications() 
                ? new RfcFilterEvaluator(enrollmentWrapper.getApplicationIdentification()) 
                : RfcFilterEvaluator.ALWAYS_FALSE;
                 
        groupTypeDetector = enrollmentWrapper.enrollGroups() 
                ? new RfcFilterEvaluator(enrollmentWrapper.getGroupIdentification()) 
                : RfcFilterEvaluator.ALWAYS_FALSE;
        groupNamePrefix = this.enrollment.getDomainName().toUpperCase() + ":Groups:";
                
        String expression = enrollmentWrapper.getStructureIdentification();
        structureTypeDetector = (expression != null && expression.trim().length() > 0)
                ? new RfcFilterEvaluator(expression) 
                : RfcFilterEvaluator.ALWAYS_FALSE;
                
        expression = enrollmentWrapper.getOtherIdentification();
        otherTypeDetector = (expression != null && expression.trim().length() > 0)
                ? new RfcFilterEvaluator(expression) 
                : RfcFilterEvaluator.ALWAYS_FALSE;
                
        mailFormatter = enrollmentWrapper.isFilterMailValue()
                ? new EmailAddressFormatter()
                : null;

        try {
            USER_TYPE = dictionary.getType(ElementTypeEnumType.USER.getName());
            CONTACT_TYPE = dictionary.getType(ElementTypeEnumType.CONTACT.getName());
            HOST_TYPE = dictionary.getType(ElementTypeEnumType.COMPUTER.getName());
            APP_TYPE = dictionary.getType(ElementTypeEnumType.APPLICATION.getName());
        } catch (DictionaryException de) {
            throw new EnrollmentValidationException("Can not find enrollment types from dictionary", de);
        }
    }
    
    protected void logWarn(String message){
        warningCount++;
        if(lastNWarningMessages.size() >= KEEP_LAST_N_WARNING_MESSAGES){
            lastNWarningMessages.poll();
        }
        lastNWarningMessages.add(message);
        LOG.warn(message);
        
    }

    /**
     * getElementType() uses the Rfc Filter to match the filter expression with
     * given LDAP Entry object, and will return the corresponding IEnrollmentType
     * object predefined in this class.
     *
     * If the entry can not match to any predefined type, null will be returned.
     *
     * @param  entry the LDAP entry object
     * @return IEnrollmentType a pre-defined IEnrollmentType object or null
     */
    protected IElementType getElementType(LDAPEntry entry) throws EnrollmentSyncException {
        if (userTypeDetector.evaluate(entry)) {
            return USER_TYPE;
        } else if (contactTypeDetector.evaluate(entry)) {
            return CONTACT_TYPE;
        } else if (hostTypeDetector.evaluate(entry)) {
            return HOST_TYPE;
        } else if (appTypeDetector.evaluate(entry)) {
            return APP_TYPE;
        } else {
            return null;
        }
    }

    /**
     * getElementKey() uses the element type to determine which of external
     * attribute name to use to extract attribute value from LDAP entry object.
     * and create new DictionaryKey object
     *
     * If the entry can not match to any predefined type, null will be returned.
     *
     * @param  entry the LDAP entry object
     * @param  type the enrollment type
     * @return DictionaryKey object or null
     */
    protected DictionaryKey getElementKey(LDAPEntry entry, IElementType type) {
        String staticIDAttrName = enrollmentWrapper.getObjectGUIDAttributeName();
        LDAPAttribute attr = entry.getAttribute(staticIDAttrName);
        if (attr == null) {
            //TODO_OJA
        	String dnValue = entry.getDN();
        	String msg = "Missing key=" + staticIDAttrName +
            		" for entry:" + dnValue;
            if (dnValue.length() > 0) {
            	byte[] dnValueBytes = null;
            	try {
					dnValueBytes = dnValue.getBytes("UTF-8");
				} catch (UnsupportedEncodingException e) {
	            	logWarn (msg + ". Unable to use DN as fallback ID.");
					return null;
				}
            	return new DictionaryKey( dnValueBytes );
            } else {
            	logWarn (msg);
            	return null;
            }
        }
        return new DictionaryKey( attr.getByteValue() );
    }
    
    /**
     * createContentElement() takes LDAP entry object as input
     *
     * 1. find the element type of input object
     * 2. find the key value of input object
     * 3. make new Elememt/Group based on element type and key
     *
     * If the entry can not match to any predefined type, null will be returned.
     *
     * @param  entry the LDAP entry object
     * @return IMElementBase object or null
     */
    public IPair<SyncResultEnum, ? extends IElementBase> createContentElement(LDAPEntry entry)
            throws EnrollmentSyncException {
        final IElementType type = getElementType(entry);
        final DictionaryKey key = getElementKey(entry, type);
        if (key == null) {
            // can't create element without objectguid
            return FAILED_ELEMENT_BASE;
        }
        
        final String dn = entry.getDN();
        final DictionaryPath path = LDAPEnrollmentHelper.getDictionaryPathFromDN(dn);
        final IPair<SyncResultEnum, ? extends IElementBase> saveElement;

        try {
            if (type != null) {
                saveElement = createElement(entry, type, key, dn, path);
            } else if (groupTypeDetector.evaluate(entry)) {
                saveElement = createGroupElement(entry, key, dn, path);
            } else if (structureTypeDetector.evaluate(entry)) {
                saveElement = createStructElement(entry, key, dn, path);
            } else if (otherTypeDetector.evaluate(entry)) {
                saveElement = createOtherElement(entry, key, dn, path);
            } else {
                saveElement = createUnknownElement(entry, key, dn, path);
            }
        } catch (DictionaryException e) {
            throw new EnrollmentSyncException(e, dn);
        }
        return saveElement;
    }
    
    protected IPair<SyncResultEnum, IMElement> createElement(
            LDAPEntry entry, 
            IElementType type,
            DictionaryKey key, 
            String dn, 
            DictionaryPath path)
            throws DictionaryException, EnrollmentSyncException {
        LOG.debug("Creating " + type.getName() + " " + dn);
        IMElement element = null;
        Boolean isChanged = false;
        if (enrollmentWrapper.isUpdate()) {
            IMElementBase elementBase = enrollment.getByKey(key, enrollmentWrapper.getEnrollmentStartTime());
            if(elementBase != null){
                if (elementBase.getType().equals(type)) {
                    element = (IMElement)elementBase;
                }else{
                    //the type is changed
                    isChanged = true;
                }
            }
        }

        SyncResultEnum syncResultEnum;
        //element doesn't exist
        if (element != null) {
            element.setPath(path);
            LOG.trace("Retrieved " + type.getName() + ": " + dn);
            //the result is not sure yet.
            syncResultEnum = null;
        } else {
            element = enrollment.makeNewElement(path, type, key);
            LOG.trace("Created " + type.getName() + ": " + dn);
            syncResultEnum = SyncResultEnum.NEW_ENTRY;
        }
        
        Boolean isTheElementChanged = setElementValues(element, type, entry);
        if (isTheElementChanged == null) {
            element = null;
        } else if (syncResultEnum == null) {
            syncResultEnum = (isChanged | isTheElementChanged)
                    ? SyncResultEnum.MODIFY_ENTRY 
                    : SyncResultEnum.UNMODIFY_ENTRY;
        }
        
        return element != null 
                ? new Pair<SyncResultEnum, IMElement>(syncResultEnum, element) 
                : FAILED_ELEMENT;
    }

    protected IPair<SyncResultEnum, IMGroup> createGroupElement(
            LDAPEntry entry,
            DictionaryKey key, 
            String dn,
            DictionaryPath path) throws DictionaryException, EnrollmentSyncException {
        LOG.debug("Creating an enumerated group " + dn);
        IMGroup group = null;
        boolean isChanged = false;
        if (enrollmentWrapper.isUpdate()) {
            IMElementBase elementBase = enrollment.getByKey(key, enrollmentWrapper.getEnrollmentStartTime());
            if (elementBase != null) {
                if (elementBase.getType().equals(IElementType.ENUM_GROUP_TYPE)) {
                    group = (IMGroup) elementBase;
                }else{
                    //the type is changed
                    isChanged = true;
                }
            }
        }
        SyncResultEnum syncResultEnum;
        final boolean isExistingGroup = group != null;
        if (isExistingGroup) {
            group.setPath(path);
            LOG.trace("Retrieved Enum Group: " + dn);
            syncResultEnum = null;
        } else {
            group = enrollment.makeNewEnumeratedGroup(path, key);
            LOG.trace("Created Enum Group: " + dn);
            syncResultEnum = SyncResultEnum.NEW_ENTRY;
        }
        
        String name = LDAPEnrollmentHelper.getNameFromDN(dn, groupNamePrefix);
        isChanged |= group.setUniqueName(name);

        //TODO make this configurable
        LDAPAttribute cn = entry.getAttribute(DEFAULT_DISPLAY_NAME_ATTR);
        String displayName = cn != null ? cn.getStringValue() : name;
        isChanged |= group.setDisplayName(displayName);
        
        isChanged |= setGroupMembers(group, entry, isExistingGroup);
        if (syncResultEnum == null) {
            syncResultEnum = isChanged 
                    ? SyncResultEnum.MODIFY_ENTRY 
                    : SyncResultEnum.UNMODIFY_ENTRY;
        }
        return new Pair<SyncResultEnum, IMGroup>(syncResultEnum, group);
    }
    
    protected IPair<SyncResultEnum, IMGroup> createStructElement(
            LDAPEntry entry,
            DictionaryKey key, 
            String dn,
            DictionaryPath path) throws DictionaryException {
        LOG.debug("Creating an structural group " + dn);
        IMGroup structuralGroup = null;
        boolean isChanged = false;
        if (enrollmentWrapper.isUpdate()) {
            IMElementBase elementBase = enrollment.getByKey(key, enrollmentWrapper.getEnrollmentStartTime());
            if (elementBase != null) {
                if (elementBase.getType().equals(IElementType.STRUCT_GROUP_TYPE)) {
                    structuralGroup = (IMGroup) elementBase;
                } else {
                    // the type is changed
                    isChanged = true;
                }
            }
        }
        
        SyncResultEnum syncResultEnum;
        if (structuralGroup != null) {
            structuralGroup.setPath(path);
            LOG.trace("Retrieved Structural Group: " + dn);
            syncResultEnum = null;
        } else {
            structuralGroup = enrollment.makeNewStructuralGroup(path, key);
            LOG.trace("Created Structural Group: " + dn);
            syncResultEnum = SyncResultEnum.NEW_ENTRY;
        }
        
        isChanged |= structuralGroup.setDisplayName(LDAPEnrollmentHelper.getNameFromDN(dn, ""));
        isChanged |= structuralGroup.setUniqueName(LDAPEnrollmentHelper.getNameFromDN(dn,
                groupNamePrefix));
        
        if (syncResultEnum == null) {
            syncResultEnum = isChanged 
                    ? SyncResultEnum.MODIFY_ENTRY 
                    : SyncResultEnum.UNMODIFY_ENTRY;
        }
        
        return new Pair<SyncResultEnum, IMGroup>(syncResultEnum, structuralGroup);
    }
    
    protected IPair<SyncResultEnum, IElementBase> createOtherElement(
            LDAPEntry entry,
            DictionaryKey key, 
            String dn,
            DictionaryPath path) {
        LOG.debug("Ignore entry: " + dn);
        return IGNORE_ELEMENT_BASE;
    }
    
    protected IPair<SyncResultEnum, IElementBase> createUnknownElement(
            LDAPEntry entry,
            DictionaryKey key, 
            String dn,
            DictionaryPath path) {
        //TODO_OJA
        logWarn("Could not determine the TYPE of " + dn);
        return FAILED_ELEMENT_BASE;
    }

    /**
     * setElementValues() takes LDAP entry object as input
     *
     * 1. loop through all attributes of a LDAP entry
     * 2. lookup the attribute name in external fields
     * 3. if the attribute name exists in external fields, set the value
     *
     * If the entry can not match to any predefined type, EnrollmentSyncException will happen
     *
     * @param  element the element to be set value
     * @param  type the element type
     * @param  entry the LDAP entry object
     * @throws EnrollmentSyncException if the element type or field type is unknown 
     * @return true if the element is changed, false otherwise. Null if the value contains a problem. The whole entry should be skipped
     */
    protected Boolean setElementValues(IMElement element, IElementType type, LDAPEntry entry)
            throws EnrollmentSyncException {
        final Map<IElementField, String> externalField;
        final IElementField uniqueNameField;
        final IElementField displayNameField;
        final IElementField sidField;
        
        if (type == USER_TYPE) {
            externalField = enrollmentWrapper.getSearchableAttributesForUsers();
            uniqueNameField = USER_TYPE.getField(UserReservedFieldEnumType.PRINCIPAL_NAME.getName());
            displayNameField = USER_TYPE.getField(UserReservedFieldEnumType.DISPLAY_NAME.getName());
            sidField = USER_TYPE.getField(UserReservedFieldEnumType.WINDOWS_SID.getName());
        } else if (type == CONTACT_TYPE) {
            externalField = enrollmentWrapper.getSearchableAttributesForContacts();
            uniqueNameField = CONTACT_TYPE.getField(ContactReservedFieldEnumType.PRINCIPAL_NAME.getName());
            displayNameField = CONTACT_TYPE.getField(ContactReservedFieldEnumType.DISPLAY_NAME.getName());
            sidField = null;
        } else if (type == HOST_TYPE) {
            externalField = enrollmentWrapper.getSearchableAttributesForComputers();
            uniqueNameField = HOST_TYPE.getField(ComputerReservedFieldEnumType.DNS_NAME.getName());
            displayNameField = null;
            sidField = HOST_TYPE.getField(ComputerReservedFieldEnumType.WINDOWS_SID.getName());
        } else if (type == APP_TYPE) {
            externalField = enrollmentWrapper.getSearchableAttributesForApplications();
            uniqueNameField = APP_TYPE.getField(ApplicationReservedFieldEnumType.UNIQUE_NAME.getName());
            displayNameField = APP_TYPE.getField(ApplicationReservedFieldEnumType.DISPLAY_NAME.getName());
            sidField = null;
        } else {
            throw new EnrollmentSyncException("Invalid element type: " + type, entry.getDN());
        }
        
        return setElementFieldsValues(element, entry, externalField, uniqueNameField, displayNameField, sidField);
    }
    
    private String getStringValue(LDAPEntry entry, String attrKey){
        LDAPAttribute attr= entry.getAttribute( attrKey );
        String value = null;
        if(attr != null){
            value = attr.getStringValue();
        }
        return value;
    }

    /**
     * setElementFieldsValues() takes LDAP entry object as input
     *
     * 1. loop through all attributes of a LDAP entry
     * 2. lookup the attribute name in external fields
     * 3. if the attribute name exists in external fields, set the value
     *
     * If the entry can not match to any predefined type, null will be returned.
     *
     * @param  element the element to be set value
     * @param  entry the LDAP entry object
     * @param  externalFields the fields where the values are going to be set
     * @param uniqueNameField the unique name field, can't be null
     * @param sidField the sid field, could be null
     * @throws EnrollmentSyncException 
     * @return true if the element is changed, false otherwise. Null if the value can't be set
     */
    protected Boolean setElementFieldsValues(IMElement element, 
            LDAPEntry entry,
            Map<IElementField, String> externalFields, 
            IElementField uniqueNameField,
            IElementField displayNameField,
            IElementField sidField
    ) throws EnrollmentSyncException {
        assert uniqueNameField != null;
        
        boolean changed = false;

        SortedSet<String> missingAttributes = new TreeSet<String>();
        
        String displayNameAttrKey = externalFields.get(displayNameField);
        if (displayNameAttrKey == null) {
            displayNameAttrKey = DEFAULT_DISPLAY_NAME_ATTR;
        }
        
        String displayName = getStringValue(entry, displayNameAttrKey);
        if(displayName == null){
            missingAttributes.add(displayNameAttrKey);
        }
        changed |= element.setDisplayName(displayName);
        
        
        String uniqueNameAttrKey = externalFields.get(uniqueNameField);
        String uniqueName = getStringValue(entry, uniqueNameAttrKey);
        if(uniqueName == null){
            missingAttributes.add(uniqueNameAttrKey);
        }
        changed |= element.setUniqueName(uniqueName);

        
        for( Map.Entry<IElementField, String> attrEntry : externalFields.entrySet() ) {
            String attrName = attrEntry.getValue();
            LDAPAttribute attr = entry.getAttribute( attrName );

            if (attr == null && !enrollmentWrapper.storeMissingAttributes()) {
                missingAttributes.add(attrName);
                continue;
            }

            IElementField field = attrEntry.getKey();
            Object fieldValue;
            if ((sidField != null) && (field.getName().equals(sidField.getName()))) {
                fieldValue = getSidValue(attr);
            } else {
                fieldValue = getFieldValue(field, attr, entry.getDN());
                if (fieldValue == null && attr != null && attr.getByteValue() != null) {
                    // the attribute has something but it can't be set. 
                    // The whole entry will be skipped
                    LOG.trace("Attribute: " + attrName + ", DN=" + entry.getDN() + ", has data but cannot be set.  Skipping entire entry. Data=" + Arrays.toString(attr.getByteValue()));
                    return null;  // null has meaning here.  FindBugs doesn't like it though.  Ignore FindBugs issue.
                }
            }
            changed |= element.setValue( field, fieldValue );
            LOG.trace("set element value " + field.getName() + "=" + (attr == null ? "<EMPTY>" : attr.getStringValue()) );
        }
        
        if (!missingAttributes.isEmpty()) {
            LOG.info("A missing value has been detected: " + CollectionUtils.asString(missingAttributes, ", ") + " in '" + entry.getDN() + "'.");
        }
        
        return changed;
    }
    
    /**
     * set SID related to different enrollment types
     *
     * This method should be overridden based on the need of enroller
     */
    protected abstract Object getSidValue(LDAPAttribute entry);
    
    /**
     * setGroupMembers() set the group members
     *
     * 1. find the member DNs of input LDAP entry object
     * 2. create dictionary path from member DNS and get Provisional Reference from enrollment
     * 3. add new element / groups to group
     * 4. remove all entry that is not definied in AD
     *
     * @param  group group element
     * @param  entry LDAP entry
     * @param  isExistingGroup
     * @throws EnrollmentSyncException
     * @return true if there is any membership changes. 
     */
    protected boolean setGroupMembers(IMGroup group, LDAPEntry entry, boolean isExistingGroup)
            throws EnrollmentSyncException {

        boolean isChanged = false;
        
        //oracle has limit on 1000 object in the query. 800 is not optimized but a large safe number.
        final int MAX_PATH_NUMBER = 800;
        
        final String membershipAttributeName = enrollmentWrapper.getMembershipAttributeName();
        final LDAPAttribute attr = entry.getAttribute(membershipAttributeName);

        /**
         * 1. get the memebership from ad
         * 2. get all membership from dictionary
         * 3. covert them to ProvisionalReferences
         * 4. if the ad_membership exists in dictionary, do nothing. Otherwise, add it.
         * 5. after checking all ad_membership, remove all remaining dictionary_relationship.
         */
        
        String errorMessage = "setGroupMembers";
        
        try {
            Queue<DictionaryPath> dictionaryPaths = new LinkedList<DictionaryPath>();
            if (attr != null) {
                @SuppressWarnings("unchecked")
                Enumeration<String> allAttrValues = attr.getStringValues();
                
                String memberDN;
                // put all the member in the local variable first. 
                while (allAttrValues.hasMoreElements()) {
                    memberDN = allAttrValues.nextElement();
                    DictionaryPath path = LDAPEnrollmentHelper.getDictionaryPathFromDN(memberDN);
                    dictionaryPaths.add(path);
                }
            }
            
            if (!dictionaryPaths.isEmpty()) {
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
                while (!dictionaryPaths.isEmpty()) {
                    Collection<DictionaryPath> pathsPart = new LinkedList<DictionaryPath>();
                    //transfer at most MAX_PATH_NUMBER from paths to pathsPart
                    for (int i = 0; i < MAX_PATH_NUMBER & !dictionaryPaths.isEmpty(); i++) {
                        pathsPart.add(dictionaryPaths.poll());
                    }
                    
                    List<IReferenceable> memberElements = enrollment.getProvisionalReferences(pathsPart);
                    for (IReferenceable element : memberElements) {
                        if (existingChildren.remove(element.getPath()) == null) {
                            group.addChild(element);
                            isChanged = true;
                        }
                        if (isTraceEnabled) {
                            LOG.trace("Adding member " + element.getPath().toString() + " to "
                                    + group.getPath().toString());
                        }
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
     * get all the direct elements, groups and references. 
     * @param group
     * @return
     * @throws DictionaryException
     */
    protected Map<DictionaryPath, IReferenceable> getAllChildren(IMGroup group)
            throws DictionaryException {
        Map<DictionaryPath, IReferenceable> existingChildrens =
                new HashMap<DictionaryPath, IReferenceable>();
        
        IDictionaryIterator<IMElement> childIterator = group.getDirectChildElements();
        extractDictionaryPaths(childIterator, existingChildrens);
        
        IDictionaryIterator<IMGroup> groupIterator = group.getDirectChildGroups();
        extractDictionaryPaths(groupIterator, existingChildrens);
        
        IDictionaryIterator<DictionaryPath> refIterator = null;
        try {
            refIterator = group.getAllReferenceMemebers();
            while (refIterator.hasNext()) {
                final DictionaryPath c = refIterator.next();
                existingChildrens.put(c, new IReferenceable() {
                    public DictionaryPath getPath() {
                        return c;
                    }
                    
                    public void accept(IElementVisitor visitor) {
                        visitor.visitProvisionalReference(this);
                    }
                });
            }
        } finally {
            if (refIterator != null) {
                try {
                    refIterator.close();
                } catch (DictionaryException e) {
                    logWarn("Unable to close dictionary iterator, " + e);
                }
            }
        }
        
        return existingChildrens;
    }
    
    /**
     * loop through each entry in <code>itor</code>.
     * get the dictionary path and put it into <code>existingChildrens</code>
     * @param itor
     * @param existingChildrens
     * @throws DictionaryException
     */
    private void extractDictionaryPaths(IDictionaryIterator<? extends IElementBase> itor,
            Map<DictionaryPath, IReferenceable> existingChildrens) throws DictionaryException {
        
        boolean ignoreNormalize = itor instanceof ElementBaseIterator<?>;
        try {
            while (itor.hasNext()) {
                IElementBase c;
                if(ignoreNormalize){
                    c = ((ElementBaseIterator<? extends IElementBase>)itor).next(false);
                }else{
                    c = itor.next();
                }
                existingChildrens.put(c.getPath(), c);
            }
        } finally {
            if (itor != null) {
                try {
                    itor.close();
                } catch (DictionaryException e) {
                    logWarn("Unable to close dictionary iterator, " + e);
                }
            }
        }
    }

    /**
     * get Element Field value by a giving string
     *
     * @param field
     * @param value
     * @param dn is only used when display error messages.
     * @return null if the attribute is invalid. Such as too long or invalid encoding
     * @throws EnrollmentSyncException if the field type is unknown.
     */
    protected Object getFieldValue(IElementField field, LDAPAttribute attr, String dn)
            throws EnrollmentSyncException {
        final Object fieldValue;
        final ElementFieldType fieldType = field.getType();
        if (attr == null) 
        	return null;
        
        if (fieldType == ElementFieldType.STRING_ARRAY) {
            String[] valueArray = attr.getStringValueArray();
            if (field.getName().equals(UserReservedFieldEnumType.MAIL.getName())
                    && mailFormatter != null) {
                valueArray = mailFormatter.format(valueArray);
            }
            String value = StringArrayAsString.toString(valueArray);
            if (!checkString(value, attr, dn)) {
                return null;
            }
            fieldValue = valueArray;
        } else if (fieldType == ElementFieldType.STRING) {
        	String valueStr = attr.getStringValue();
        	if (valueStr == null)
        		return null;
            String value = valueStr.toLowerCase();
            if (!checkString(value, attr, dn)) {
                return null;
            }
            fieldValue = value;
        } else if (fieldType == ElementFieldType.CS_STRING) {
            String value = attr.getStringValue();
            if (!checkString(value, attr, dn)) {
                return null;
            }
            fieldValue = value;
        } else if (fieldType == ElementFieldType.NUMBER) {
            fieldValue = new Long(attr.getStringValue());
        } else if (fieldType == ElementFieldType.DATE) {
            Date date = null;
            try {
                date = DateFormat.getDateInstance().parse(attr.getStringValue());
            } catch (java.text.ParseException e) {
                logWarn("Can not parse date string : " + attr.getStringValue() + " on dn: " + dn);
            }
            fieldValue = date;
        } else if (fieldType == ElementFieldType.LONG_STRING) {
            fieldValue = attr.getStringValue().toLowerCase();
        } else {
            throw new EnrollmentSyncException("Undefined field type: " + fieldType.getName(), dn);
        }
        return fieldValue;
    }
    
    /**
     * 
     * @param s the string value
     * @param attr the LDAP attribute of the entry
     * @param dn the dn of the entry
     * @return true if the string is ok, can be set to database. False otherwise.
     */
    protected boolean checkString(String s, LDAPAttribute attr, String dn) {
        boolean isOk = true;
        if (s.length() > STRING_COLUMN_LIMIT) {
            logWarn(String.format("For this DN '%s', the length of the '%s' attribute, %d, " +
                    "exceeds the maximum limit of %d. This entry will be skipped.", 
                    dn, (attr!=null) ? attr.getName() : "unknown", s.length(), STRING_COLUMN_LIMIT));
            isOk = false;
        }
        
        //more checking
        
        return isOk;
    }

    /**
     * Remove direct elements and group members of a group for a existing group
     * @param group
     * @throws DictionaryException
     */
    protected void removeAllDirectGroupMembers(IMGroup group) throws DictionaryException {
        IDictionaryIterator<IMElement> itor = group.getDirectChildElements();
        
        boolean ignoreNormalize = itor instanceof ElementBaseIterator<?>;
        try {
            while( itor.hasNext() ) {
                IElementBase c;
                if(ignoreNormalize){
                    c = ((ElementBaseIterator<? extends IElementBase>)itor).next(false);
                }else{
                    c = itor.next();
                }
                group.removeChild(c);
            }
        } finally {
            itor.close();
        }
        IDictionaryIterator<IMGroup> itor2 = group.getDirectChildGroups();
        try {
            while( itor2.hasNext() ) {
                group.removeChild(itor2.next());
            }
        } finally {
            itor2.close();
        }
    }
    
    public String getWarningMessage() {
        if (warningCount == 0) {
            return null;
        } else if (warningCount == 1) {
            //just one message, display it directly
            return lastNWarningMessages.peek();
        } else {
            //more than one message, that's too much. Check the server log
            return String.format("There are %d warnings. Please check the server log for details.",
                    warningCount);
        }
    }

}
