/*
 * Created on Dec 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.bluejungle.dictionary.IElementField;
import com.bluejungle.framework.utils.CollectionUtils;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/Suggestions.java#1 $
 */

public class Suggestions {
    protected static final String N = "\n";
    
    final Map<WarningType, Map<IElementField, Integer>> warntypeToFieldMap;
    final Map<IElementField, Map<WarningType, Integer>> fieldToWarntypeMap;
    
    int maxDatabaseColumnLength = 0;
    
    private final List<String> messages;
    
    
    public Suggestions() {
        messages = new LinkedList<String>();
        
        warntypeToFieldMap = new HashMap<WarningType, Map<IElementField, Integer>>();
        fieldToWarntypeMap = new HashMap<IElementField, Map<WarningType, Integer>>();
    }
    
    void markColumnLength(int length){
        if(length > maxDatabaseColumnLength){
            maxDatabaseColumnLength = length; 
        }
    }
    
    protected List<String> getMessages(){
        // new line
        List<String> allSuggestions = new LinkedList<String>();
        {
            Map<IElementField, Integer> fields = warntypeToFieldMap.get(WarningType.VALUE_TOO_LONG);
            if (fields != null && fields.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(fields.size());
                sb.append(fields.size() == 1 ? " field has " : " fields have ");
                sb.append("a value longer than the enrollment constraint.").append(N)
                  .append("Suggest one of following actions:").append(N)
                  .append("1. Increase the database column length to ").append(maxDatabaseColumnLength)
                  .append(". And change enrollment length checking.").append(N)
                  .append("2. Not to enrollment following fields by removing them from .def file: ").append(N)
                  .append("   ").append(CollectionUtils.asString(fields.keySet(), ", ")).append(N);
                
                allSuggestions.add(sb.toString());
            }
        }
        
        {
            Map<IElementField, Integer> fields = warntypeToFieldMap.get(WarningType.MISSING_FIELD);
            if (fields != null && fields.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(fields.size());
                sb.append(fields.size() == 1 ? " field has " : " fields have ");
                sb.append("a missing value.").append(N)
                  .append("Suggest one of following actions:").append(N)
                  .append("1. Not to enrollment following fields by removing them from .def file: ").append(N)
                  .append("   ").append(CollectionUtils.asString(fields.keySet(), ", ")).append(N);
                
                allSuggestions.add(sb.toString());
            }
        }
        
        {
            Map<IElementField, Integer> fields = warntypeToFieldMap.get(WarningType.CHARSER_NOT_SUPPORTED);
            if (fields != null && fields.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(fields.size());
                sb.append(fields.size() == 1 ? " field has " : " fields have ");
                sb.append("a value that contains non-support character.").append(N)
                  .append("Suggest one of following actions:").append(N)
                  .append("1. Change the database charset.").append(N)
                  .append("2. Not to enrollment following fields by removing them from .def file: ").append(N)
                  .append("   ").append(CollectionUtils.asString(fields.keySet(), ", ")).append(N);
                
                allSuggestions.add(sb.toString());
            }
        }
        
        if (!allSuggestions.isEmpty()) {
            allSuggestions.add("Please update the config.properties to match the new setting and rerun this tool.");
        }
        
        allSuggestions.addAll(messages);
        return allSuggestions;
    }
}
