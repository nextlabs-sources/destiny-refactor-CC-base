/*
 * Created on Dec 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import java.util.List;
import java.util.Map;

import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.PropertyKey;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/BasePreviewer.java#1 $
 */

public abstract class BasePreviewer implements IPreviewer{
    protected IEnrollment enrollment;
    protected Map<String, String[]> properties;
    protected IDictionary dictionary;
    protected IEnrollmentPreviewGUI gui;
    protected EnrollmentChecker valueChecker;
    
    
    protected volatile int entryCount;
    
    public BasePreviewer(){
        entryCount = 0;
    }
    
    public int getEntryCount() {
        return entryCount;
    }
    
    protected void normalize(List<EnrollmentProperty> properties){
     // normalizeProperties
        for (EnrollmentProperty property : properties) {
            String key = property.getKey();
            property.setKey(key.trim().toLowerCase());
            String[] values = property.getValue();
            if ((values == null) || (values.length == 0)) {
                throw new IllegalArgumentException("Invalid value for " + key);
            }
            
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    property.setValue(i, values[i].trim());
                }
            }
        }
    }

    public void init(IConfiguration configuration) throws Exception {
        enrollment =   getMustProperty(configuration, ENROLLMENT_KEY);
        properties =   getMustProperty(configuration, ENROLLMENT_PROPERTIES_KEY);
        dictionary =   getMustProperty(configuration, DICTIONARY_KEY);
        gui =          getMustProperty(configuration, GUI_KEY);
        valueChecker = getMustProperty(configuration, VALUE_CHECKER_KEY);
    }
    
    protected <T> T getMustProperty(IConfiguration config, PropertyKey<T> key) {
        T value = config.get(key);
        if (value == null) {
            throw new NullPointerException(key.toString());
        }
        return value;
    }
    
}
