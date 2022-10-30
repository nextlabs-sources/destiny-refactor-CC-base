/*
 * Created on Dec 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment.ad;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.ad.impl.ADEnrollmentWrapperImpl;
import com.bluejungle.destiny.container.shared.dictionary.enrollment.enroller.common.BasicLDAPEnrollmentProperties;
import com.bluejungle.framework.utils.CollectionUtils;
import com.bluejungle.framework.utils.Formatter;
import com.nextlabs.testtool.enrollment.Suggestions;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/ad/ADSuggestions.java#1 $
 */

public class ADSuggestions extends Suggestions{
    ADEnrollmentWrapperImpl adEnrollment;
    
    final Set<String> okObjectClasses;
    final Set<String> failObjectClasses;
    
    ADSuggestions() {
        super();
        okObjectClasses = new HashSet<String>();
        failObjectClasses = new HashSet<String>();
    }
    
    @Override
    protected List<String> getMessages() {
        List<String> allSuggestions = super.getMessages();
        
        if( !failObjectClasses.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("You have some entries that can't be recongized.").append(N)
              .append("Suggest one of following actions:").append(N)
              .append("1. If you want to enroll those objects, modify \"*.requirement\" in .def file.").append(N);
            
            Set<String> diff = new HashSet<String>(failObjectClasses);
            diff.removeAll(okObjectClasses);
            if (!diff.isEmpty()) {
                String appendFilter = CollectionUtils.asString(diff, "", "", new Formatter<String>() {
                    public String toString(String t) {
                        return "(!(objectClass=" + t + "))";
                    }
                });
                
                sb.append("2. If you don't want to enroll those objects, you have two options.").append(N)
                  .append("   a. Change the \"filter\" in .conn file. Replace the value with \"");
                if (adEnrollment.getFilter() != null) {
                    sb.append("(&(").append(adEnrollment.getFilter()).append(")")
                      .append(appendFilter).append(")");
                } else {
                    sb.append("(&").append(appendFilter).append(")");
                }
                sb.append("\".").append(N)
                  .append("   b. Change \"").append(BasicLDAPEnrollmentProperties.OTHER_REQUIREMNTS)
                  .append("\" in .def file. Replace the value with \"");
                
                if (adEnrollment.getOtherIdentification() != null) {
                      sb.append("(&(").append(adEnrollment.getOtherIdentification()).append(")")
                    .append(appendFilter).append(")");
                } else {
                      sb.append("(&").append(appendFilter).append(")");
                }
                  
                sb.append("\"").append(N)
                  .append("   ").append(N)
                ;
            }
            allSuggestions.add(sb.toString());
        }
        
        return allSuggestions;
    }
    
    
    

}
