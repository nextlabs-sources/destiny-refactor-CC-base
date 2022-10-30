/*
 * Created on Dec 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;
import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.bluejungle.dictionary.IDictionary;
import com.bluejungle.dictionary.IEnrollment;
import com.bluejungle.framework.comp.IConfiguration;
import com.bluejungle.framework.comp.PropertyKey;


/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/IPreviewer.java#1 $
 */

public interface IPreviewer {
    PropertyKey<IEnrollment>           ENROLLMENT_KEY            = new PropertyKey<IEnrollment>(          "ENROLLMENT");
    PropertyKey<Map<String, String[]>> ENROLLMENT_PROPERTIES_KEY = new PropertyKey<Map<String, String[]>>("ENROLLMENT_PROPERTIES");
    PropertyKey<IDictionary>           DICTIONARY_KEY            = new PropertyKey<IDictionary>(          "DICTIONARY");
    PropertyKey<IEnrollmentPreviewGUI> GUI_KEY                   = new PropertyKey<IEnrollmentPreviewGUI>("GUI");
//    PropertyKey<Suggestions>           SUGGESTIONS_KEY           = new PropertyKey<Suggestions>(          "SUGGESTIONS");
    PropertyKey<EnrollmentChecker>     VALUE_CHECKER_KEY         = new PropertyKey<EnrollmentChecker>(    "VALUE_CHECKER");
    
    
    
    
    public List<EnrollmentProperty> createRealmProperties(File connFile, File defFile,
            File filterFile, File selectiveFolder) throws IOException, FileFormatException;
    
    //similar to EnrollerBase.preSync
    void init(IConfiguration configuration) throws Exception;
    
    Suggestions getSuggestions();
    
    //similar to EnrollerBase.internalSync
    void start() throws Exception;
    
    int getEntryCount();
}
