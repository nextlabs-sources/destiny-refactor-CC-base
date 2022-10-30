package com.bluejungle.pf.destiny.policymap;

/*
 * Created on Jan 12, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2011 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 *
 * @author amorgan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/pf/src/java/main/com/bluejungle/pf/destiny/policymap/StaticAttributeTracker.java#1 $:
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.dictionary.Dictionary;
import com.bluejungle.dictionary.DictionaryException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.dictionary.IElementType;
import com.bluejungle.framework.comp.ComponentInfo;
import com.bluejungle.framework.comp.ComponentManagerFactory;
import com.bluejungle.framework.comp.LifestyleType;
import com.bluejungle.pf.domain.destiny.subject.IDSubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectAttribute;
import com.bluejungle.pf.domain.destiny.subject.SubjectType;
import com.bluejungle.pf.domain.epicenter.subject.ISubjectType;

/**
 * This class tracks what attributes are static by using the
 * dictionary. If an attribute says it is dynamic then it's
 * dynamic. If it's found in the dictionary or is one of the known
 * builtin attributes then it's static. If it doesn't say it's
 * dynamic, but isn't in the dictionary, we assume it's dynamic
 */

public class StaticAttributeTracker {
    public static final ComponentInfo<StaticAttributeTracker> COMP_INFO = 
    	new ComponentInfo<StaticAttributeTracker>(StaticAttributeTracker.class, LifestyleType.SINGLETON_TYPE);

    private final Log log = LogFactory.getLog(StaticAttributeTracker.class.getName());

    private IDictionary dictionary = ComponentManagerFactory.getComponentManager().getComponent(Dictionary.COMP_INFO);

    // All the known static (or static-ish. See below) attributes in the system. Set by update()
    private Set<String> staticAttributes = new HashSet<String>();

    // The Dictionary does some translating and special-casing of attribute names behind the secnes. user.name
    // is converted to user.principalName, for example. This means that user.name is treated like an enrolled
    // attribute, even though it doesn't appear directly in the dictionary. These cases are hard-coded in
    // various ways in DictionaryHelper.java, so we hard-code them here.
    private static final List<String> builtins = Arrays.asList(constructName(SubjectAttribute.USER_NAME),
                                                               constructName(SubjectAttribute.USER_LDAP_GROUP),
                                                               constructName(SubjectAttribute.USER_LDAP_GROUP_DISPLAY_NAME),
                                                               constructName(SubjectAttribute.USER_LDAP_GROUP_ID),
                                                               constructName(SubjectAttribute.USER_UID),
                                                               constructName(SubjectAttribute.USER_ID),
                                                               constructName(SubjectAttribute.HOST_NAME),
                                                               constructName(SubjectAttribute.HOST_LDAP_GROUP),
                                                               constructName(SubjectAttribute.HOST_LDAP_GROUP_DISPLAY_NAME),
                                                               constructName(SubjectAttribute.HOST_LDAP_GROUP_ID),
                                                               constructName(SubjectAttribute.HOST_UID),
                                                               constructName(SubjectAttribute.HOST_ID),
                                                               constructName(SubjectAttribute.APP_UID),
                                                               constructName(SubjectAttribute.APP_ID),
                                                               constructName(SubjectAttribute.CONTACT_ID));

    public StaticAttributeTracker() {
    }

    private static String constructName(String type, String attributeName) {
        return type.toLowerCase() + "-" + attributeName.toLowerCase();
    }

    private static String constructName(ISubjectType type, String attributeName) {
        return constructName(type.getName(), attributeName);
    }

    private static String constructName(IDSubjectAttribute attr) {
        return constructName(attr.getSubjectType(), attr.getName());
    }
                                                                             
    public boolean isDynamic(IDSubjectAttribute attr) {
        return !isStatic(attr);
    }

    public boolean isStatic(IDSubjectAttribute attr) {
        return !attr.isDynamic() && isStatic(attr.getSubjectType(), attr.getName());
    }

    synchronized private boolean isStatic(ISubjectType type, String attributeName) {
        synchronized (staticAttributes) {
            return staticAttributes.contains(constructName(type, attributeName));
        }
    }

    /**
     * Updates the internal table of static attributes. This consults the dictionary, so
     * it's not zero cost. Should be called before doing any operations that check if
     * an attribute is static or not
     *
     * There are some builtin values that should be considered static, even though they
     * don't appear in the database
     */
    public void update() {
        try {
            Set<String> newStaticAttributes = new HashSet<String>(builtins);
            
            Collection<IElementType> elementTypes = dictionary.getAllTypes();
            
            for (IElementType element : elementTypes) {
                Collection<IElementField> fields = element.getFields();
                for (IElementField field : fields) {
                    String name = constructName(element.getName(), field.getName());
                    
                    log.debug("Adding static attribute " + name);
                    newStaticAttributes.add(name);
                }
            }
            
            synchronized(staticAttributes) {
                staticAttributes = newStaticAttributes;
            }
        } catch (DictionaryException de) {
            log.error("Not updating static attribute map due to exception: " + de);
        }
    }
}
