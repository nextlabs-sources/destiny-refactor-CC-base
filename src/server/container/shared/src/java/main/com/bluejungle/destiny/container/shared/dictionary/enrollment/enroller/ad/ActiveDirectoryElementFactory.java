package com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad;

import java.util.Date;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentSyncException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.common.EnrollmentValidationException;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.ILDAPEnrollmentWrapper;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.ActiveDirectorySIDConverter;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.ElementCreator;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.util.LDAPEnrollmentHelper;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.DictionaryKey;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.dictionary.IMElement;
import com.bluejungle.dictionary.IMElementBase;
import com.bluejungle.domain.enrollment.ElementTypeEnumType;
import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPEntry;

/**
 * @author atian
 * @version $Id:
 *          //depot/personal/safdar/branches/inc-sync/main/src/server/container/shared/src/java/main/com/bluejungle/destiny/container/shared/dictionary/enrollment/enroller/ad/ActiveDirectoryElementFactory.java#1 $
 */

/**
 * ActiveDirectoryElementFactory class is designed for creating Elements /
 * Groups based on LDAP Entry
 * 
 * 1. It contains Rfc Filter Evaluators for matching the expression given by
 * enrollment configuration with a given LDAP entry object.
 * 
 * 2. After evaluation, we can determine what is the element type of LDAP Entry
 * 
 * 3. We will create the element/group object based on element type
 * 
 */

public class ActiveDirectoryElementFactory extends ElementCreator {

    /**
     * Construct Element Factory for Active Directory
     * 
     * @param enrollmentWrapper
     * @param dictionary
     * @throws EnrollmentValidationException
     */
    public ActiveDirectoryElementFactory(ILDAPEnrollmentWrapper enrollmentWrapper,
            IDictionary dictionary) throws EnrollmentValidationException {
        super(enrollmentWrapper, dictionary);
    }

    /**
     * get SID from LDAPAttribute 
     * converted by Active Directory SID converter tool
     */

    public Object getSidValue(LDAPAttribute ldapAttr) {
        byte[]  sid = ldapAttr.getByteValue();
        return ActiveDirectorySIDConverter.sidToString(sid);
    }

    /**
     * Create a change element from a LDAPEntry There are 4 types of changes can
     * be made in exteranl ActiveDirectory 1. Modify attribute When attribute is
     * modified in external directory, LDAPEntry contains following info: Entry
     * DN: LDAPAttribute: {type='sn', value='Harrison BBB'} modify attribute and
     * new value LDAPAttribute: {type='objectGUID', value='?>e+#{?J??(? ?b'}
     * 
     * 2. Add new entry When new entry is created, it contains all new attribute
     * and new objectGUID
     * 
     * 3. Delete an existing entry When an entry is deleted, the LDPAEntry
     * contains following info: Entry DN of deleted object LDAPAttribute:
     * {type='objectGUID', value='?>e+#{?J??(? ?b'} Attr :isDelete: {true}
     * 
     * 4. Modify DN When a tree leaf node is moved, When a tree internal node is
     * moved
     * 
     * @param entry
     *            LDAPEntry
     * @return IMElementBase which can be USER/GROUP/HOST/APP type of element
     *         object
     */
    public IMElementBase createChangeElement(LDAPEntry entry) throws EnrollmentSyncException {
        //TODO rework
        LDAPAttribute objectGUIDAttr = entry.getAttribute(super.enrollmentWrapper.getObjectGUIDAttributeName());
        if (objectGUIDAttr == null) {
            return null;
        }

        byte[] key = objectGUIDAttr.getByteValue();
        if (key == null) {
            // The entry does not exist in dictionary, return null
            return null;
        }
        
        // Check whether entry exists in dictionary
        // If it exists, create change element/group
        try {
            Date now = new Date();
            IMElementBase element = super.enrollment.getByKey(new DictionaryKey(key), now);
            if (element == null) {
                return null;
            }
            if ( element instanceof IMElement ) {
                String typeName = ((IMElement)element).getType().getName();
                IElementType type = null;
                if (typeName.equalsIgnoreCase(ElementTypeEnumType.USER.getName())) {
                    type = USER_TYPE;
                } else if (typeName.equalsIgnoreCase(ElementTypeEnumType.COMPUTER.getName())) {
                    type = HOST_TYPE;
                } else {
                    throw new EnrollmentSyncException("invalid element type for " + typeName,
                            entry.getDN());
                }
                setElementValues( (IMElement)element, type, entry);
                
            } else { // group
                
            }
            element.setPath( LDAPEnrollmentHelper.getDictionaryPathFromDN(entry.getDN()) );
            return element;
        } catch (DictionaryException e) {
            return null;
        }
    }

    /**
     * Create a deleted element
     * 
     * @param entry
     * @return
     */
    public IMElementBase createDeleteElement(LDAPEntry entry) throws EnrollmentSyncException {
        byte[] key = null;
        LDAPAttribute objectGUIDAttr = entry.getAttribute(super.enrollmentWrapper.getObjectGUIDAttributeName());
        if (objectGUIDAttr != null) {
            key = objectGUIDAttr.getByteValue();
        }
        if (key != null) {
            try {
                // check whether the key is an element
                Date now = new Date();
                IMElementBase element = super.enrollment.getByKey(new DictionaryKey(key), now);
                if (element != null) {
                    return element;
                }
            } catch (DictionaryException e) {
                throw new EnrollmentSyncException(e, entry.getDN());
            }
        }
        return null;
    }

}
