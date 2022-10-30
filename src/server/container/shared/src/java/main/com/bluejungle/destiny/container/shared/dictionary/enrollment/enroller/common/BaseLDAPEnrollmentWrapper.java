/*
 * Created on May 5, 2006
 *
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.ElementBaseIterator;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IDictionaryIterator;
import com.bluejungle.dictionary.IElementBase;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.dictionary.IEnrollmentSession;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMGroup;
import com.bluejungle.dictionary.Page;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.bluejungle.domain.enrollment.UserReservedFieldEnumType;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/common/BaseLDAPEnrollmentWrapper.java#1 $
 */
public abstract class BaseLDAPEnrollmentWrapper implements ILDAPEnrollmentWrapper, BasicLDAPEnrollmentProperties {

    private static final Log LOG = LogFactory.getLog(BaseLDAPEnrollmentWrapper.class.getName());

    protected final IEnrollment enrollment;
    protected final IDictionary dictionary;

    /*
     * Common attribute:
     */
    private final String objectGUIDAttributeName;

    private final boolean storeMissingAttributes;

    /*
     * User entry configuration:
     */
    private final boolean enrollUsers;
    private final String userIdentification;
    private final Map<IElementField, String> allAttributesForUsers;
    
    /*
     * Contact entry configuration:
     */
    private final boolean enrollContacts;
    private final String contactIdentification;
    private final Map<IElementField, String> allAttributesForContacts;

    /*
     * Computer entry configuration:
     */
    private final boolean enrollComputers;
    private final String computerIdentification;
    private final Map<IElementField, String> allAttributesForComputers;

    /*
     * Application entry configuration:
     */
    private final boolean enrollApplications;
    private final String applicationIdentification;
    private final Map<IElementField, String> allAttributesForApplications;

    /*
     * Group entry configuration:
     */
    private final boolean enrollGroups;
    private final String groupIdentification;
    private final String membershipAttributeName;
    
    private final String structureIdentification;
    
    private final String otherIdentification;

    /*
     * Element IDs for update enrollment 
     */
    protected final Set<Long> elementIDs; 
    
    protected final boolean isUpdate;
    
    protected final int databaseBatchSize;
    
    /*
     * Enrollment start time
     */
    protected final Date enrollmentStartTime;
    
    protected final boolean filterMailValue;
    

    /**
     * 
     * @param enrollment
     * @param dictionary
     * @throws DictionaryException
     * @deprecated please specific the database batch size
     */
    @Deprecated 
    public BaseLDAPEnrollmentWrapper(IEnrollment enrollment, IDictionary dictionary)
            throws DictionaryException {
        this(enrollment, dictionary, 512);
    }
    
    /**
     * Constructor
     * @throws DictionaryException 
     *
     */
    @SuppressWarnings("unchecked")
    public BaseLDAPEnrollmentWrapper(IEnrollment enrollment, IDictionary dictionary,
            int databaseBatchSize) throws DictionaryException {
        this.enrollment = enrollment;
        this.dictionary = dictionary;
        
        objectGUIDAttributeName = enrollment.getStrProperty(STATIC_ID_ATTRIBUTE);
        
        storeMissingAttributes = Boolean.valueOf(enrollment.getStrProperty(STORE_MISSING_ATTRIBUTES));

        // Initialize a map of external -> IElementField mappings:
        enrollUsers = Boolean.valueOf(enrollment.getStrProperty(ENROLL_USERS));
        userIdentification = enrollment.getStrProperty(USER_REQUIREMENTS);
        allAttributesForUsers =    enrollUsers
                        ? createElementFieldStringMap(ElementTypeEnumType.USER)
                        : Collections.EMPTY_MAP;
        
        enrollContacts = Boolean.valueOf(enrollment.getStrProperty(ENROLL_CONTACTS));        
        contactIdentification = enrollment.getStrProperty(CONTACT_REQUIREMENTS);
        allAttributesForContacts = enrollContacts
                        ? createElementFieldStringMap(ElementTypeEnumType.CONTACT)
                        : Collections.EMPTY_MAP;
        
        enrollComputers = Boolean.valueOf(enrollment.getStrProperty(ENROLL_COMPUTERS));
        computerIdentification = enrollment.getStrProperty(COMPUTER_REQUIREMENTS);
        allAttributesForComputers =    enrollComputers
                        ? createElementFieldStringMap(ElementTypeEnumType.COMPUTER)
                        : Collections.EMPTY_MAP;
        
        enrollApplications = Boolean.valueOf(enrollment.getStrProperty(ENROLL_APPLICATIONS));
        applicationIdentification = enrollment.getStrProperty(APPLICATION_REQUIREMENTS);
        allAttributesForApplications = enrollApplications
                        ? createElementFieldStringMap(ElementTypeEnumType.APPLICATION)
                        : Collections.EMPTY_MAP;
        
        String value;
        //make sure is backward compatible
        //backward 1: enroll.group is not in the version < OrionM1, and it is always true
        value = enrollment.getStrProperty(ENROLL_GROUPS);
        enrollGroups = value != null ? Boolean.valueOf(value) : true;
        groupIdentification = enrollment.getStrProperty(GROUP_REQUIREMENTS);
        membershipAttributeName = enrollment.getStrProperty(GROUP_ENUMERATION_ATTRIBUTE);
        
        //backward 2: structure.requirement is not in the version < OrionM1 and it matches everything
        value = enrollment.getStrProperty(STRUCTURAL_GROUP_REQUIREMNTS);
        structureIdentification = value != null ? value : "objectclass=*";
        
        otherIdentification = enrollment.getStrProperty(OTHER_REQUIREMNTS);
                        
        enrollmentStartTime = new Date();
        
        isUpdate = isEnrollmentExist();
        elementIDs = new HashSet<Long>();
        
        this.databaseBatchSize = databaseBatchSize;
        
        value = enrollment.getStrProperty(USER_SEARCHABLE_PREFIX + UserReservedFieldEnumType.MAIL.getName() + FORMATTER_SUFFIX);
        filterMailValue = value != null ? Boolean.valueOf(value) : true;
    }
    
    protected boolean isEnrollmentExist() throws DictionaryException {
        boolean isEnrollmentExist = false;
        // check whether there is element in the enrollment, if so, set enrollment IDs for update enrollment
        IDictionaryIterator<?> itor = dictionary.query(dictionary.condition(enrollment), 
                enrollmentStartTime, null, new Page(0, 1));
        isEnrollmentExist |= isNotEmpty(itor);
        
        if (!isEnrollmentExist) {
            itor = dictionary.getEnumeratedGroups(dictionary.condition(enrollment), 
                    null, enrollmentStartTime, new Page(0, 1));
            isEnrollmentExist |= isNotEmpty(itor);
        }
        return isEnrollmentExist;
    }
    
    protected boolean isNotEmpty(IDictionaryIterator<?> iterator) throws DictionaryException{
        try {
            if (iterator.hasNext()) {
                return true;
            }
        } finally {
            iterator.close();
        }
        return false;
    }

    
    private Map<IElementField, String> createElementFieldStringMap(
            ElementTypeEnumType elementTypeEnumType) throws DictionaryException {
        Map<IElementField, String> map = new HashMap<IElementField, String>();
        IElementType elementType = dictionary.getType(elementTypeEnumType.getName());
        Collection<IElementField> fields = elementType.getFields();
        for (IElementField field : fields) {
            String externalName = enrollment.getExternalName(field);
            if (externalName != null) {
                map.put(field, externalName);
            } else {
                LOG.warn("No external field for " + elementTypeEnumType.getName() + " attribute: "
                        + field.getName());
            }
        }
        return map;
    }
    

    
    /**
     * save element ids for update enrollment
     */
    public int saveElementIDs(Collection<? extends IElementBase> elements) {
        if (isUpdate && (elements.size() > 0)) {
            for (IElementBase base : elements) {
                elementIDs.add(base.getInternalKey());
            }
            return elements.size();
        }
        return 0;
    }

    /**
     * Check element ids for enrollment update
     */
    public int removeElementIDs(IEnrollmentSession session) throws DictionaryException {
        if (!isUpdate || (elementIDs.size() == 0)) {
            return 0;
        }

        int size = 0;
        
        for (int pageStart = 0;; pageStart += databaseBatchSize) {
            IDictionaryIterator<IMElement> itor = 
                this.dictionary.query(this.dictionary.condition(this.enrollment)
                                      , this.dictionary.getLatestConsistentTime()
                                      , null
                                      , new Page(pageStart, databaseBatchSize));
            if (itor == null || !itor.hasNext()) {
                break;
            }
            size += removeElementsByIterator(itor, session);
        }
        
        for (int pageStart = 0;; pageStart += databaseBatchSize) {
            IDictionaryIterator<IMGroup> itor = dictionary.getEnumeratedGroups(
                    dictionary.condition(enrollment), null, dictionary.getLatestConsistentTime(), 
                    new Page(pageStart, databaseBatchSize));
            if (itor == null || !itor.hasNext()) {
                break;
            }
            size += removeElementsByIterator(itor, session);
        }
        
        for (int pageStart = 0;; pageStart += databaseBatchSize) {
            IDictionaryIterator<IMGroup> itor = dictionary.getStructuralGroups(
                    dictionary.condition(enrollment), null, dictionary.getLatestConsistentTime(), 
                    new Page(pageStart, databaseBatchSize));
            if (itor == null || !itor.hasNext()) {
                break;
            }
            size += removeElementsByIterator(itor, session);
        }
        
        return size;
    }
    
    /**
     * @param itor
     * @param deletes
     * @param session
     * @return number of entries written
     * @throws DictionaryException
     */
    private int removeElementsByIterator(IDictionaryIterator<? extends IElementBase> itor, IEnrollmentSession session) throws DictionaryException {
        List<IElementBase> deletes = new ArrayList<IElementBase>(databaseBatchSize);
        
        boolean ignoreNormalize = itor instanceof ElementBaseIterator<?>;

        int size = 0;
        
        try {
            while (itor.hasNext()) {
                IElementBase element;
                if(ignoreNormalize){
                    element = ((ElementBaseIterator<? extends IElementBase>)itor).next(false);
                } else {
                    element = itor.next();
                }

                if (!elementIDs.remove(element.getInternalKey())) {
                    LOG.debug("Removing element id=" + element.getInternalKey());
                    deletes.add(element);
                    if (deletes.size() >= databaseBatchSize) {
                        deleteElements(session, deletes);
                        size+= deletes.size();
                        LOG.debug("Removed " + deletes.size() + " elements from enrollment");
                        deletes.clear();
                    }
                }
            }
        } finally {
            itor.close();
        }

        if (deletes.size() > 0) {
            deleteElements(session, deletes);
            size+= deletes.size();
            LOG.debug("Removed " + deletes.size() + " elements from enrollment");
            deletes.clear();
        }
        
        return size;
    }

    private void deleteElements(IEnrollmentSession session, List<? extends IElementBase> deletes)
            throws DictionaryException {
        session.beginTransaction();
        session.deleteElements(deletes);
        session.commit();
    }
    

    
    /**
     * check whether the enrollment is update
     */
    public boolean isUpdate() {
        return isUpdate;
    }
    
    /**
     * return enrollment start time
     */
    public Date getEnrollmentStartTime() {
        return enrollmentStartTime;
    }
    
    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getObjectGUIDAttributeName()
     */
    public String getObjectGUIDAttributeName() {
        return objectGUIDAttributeName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getUserIdentification()
     */
    public String getUserIdentification() {
        return userIdentification;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#getContactIdentification()
     */
    public String getContactIdentification() {
        return contactIdentification;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getComputerIdentification()
     */
    public String getComputerIdentification() {
        return computerIdentification;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getGroupIdentification()
     */
    public String getGroupIdentification() {
        return groupIdentification;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#storeMissingAttributes()
     */
    public boolean storeMissingAttributes() {
        return storeMissingAttributes;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#getApplicationIdentification()
     */
    public String getApplicationIdentification() {
        return applicationIdentification;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.IADEnrollmentWrapper#getMembershipAttributeName()
     */
    public String getMembershipAttributeName() {
        return membershipAttributeName;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#enrollApplications()
     */
    public boolean enrollApplications() {
        return enrollApplications;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#enrollComputers()
     */
    public boolean enrollComputers() {
        return enrollComputers;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#enrollUsers()
     */
    public boolean enrollUsers() {
        return enrollUsers;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#enrollContacts()
     */
    public boolean enrollContacts() {
        return enrollContacts;
    }
    
    public boolean enrollGroups() {
        return enrollGroups;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#getSearchableAttributesForApplications()
     */
    public Map<IElementField, String> getSearchableAttributesForApplications() {
        return allAttributesForApplications;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#getSearchableAttributesForComputers()
     */
    public Map<IElementField, String> getSearchableAttributesForComputers() {
        return allAttributesForComputers;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#getSearchableAttributesForUsers()
     */
    public Map<IElementField, String> getSearchableAttributesForUsers() {
        return allAttributesForUsers;
    }
    
    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#getSearchableAttributesForContacts()
     */
    public Map<IElementField, String> getSearchableAttributesForContacts() {
        return allAttributesForContacts;
    }

    /**
     * @see com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper#getWrappedEnrollment()
     */
    public IEnrollment getEnrollment() {
        return enrollment;
    }

    public String getStructureIdentification() {
        return structureIdentification;
    }

    public String getOtherIdentification() {
        return otherIdentification;
    }

    public boolean isFilterMailValue() {
        return filterMailValue;
    }
}
