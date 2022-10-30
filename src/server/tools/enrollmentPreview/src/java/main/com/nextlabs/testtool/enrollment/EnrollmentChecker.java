/*
 * Created on Dec 18, 2009
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2008 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.testtool.enrollment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.bluejungle.dictionary.ElementFieldType;
import com.bluejungle.dictionary.IElementField;
import com.bluejungle.framework.datastore.hibernate.usertypes.StringArrayAsString;
import com.nextlabs.testtool.enrollment.IEnrollmentPreviewGUI.TabbedPane;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollmentPreview/src/java/main/com/nextlabs/testtool/enrollment/EnrollmentChecker.java#1 $
 */

public class EnrollmentChecker {
    private final Method stringArrayAsString_toStringMethod;
    private final StringArrayAsString dummpyStringArrayAsString;
    
    //checking fields
    private final int dbStringLength;
    private final Charset databaseCharset;
    private final CharsetDecoder databaseCharsetDecoder;
    private final CharsetEncoder databaseCharsetEncoder;
    
    private final IEnrollmentPreviewGUI gui;
    private final Suggestions suggestions;
    
    EnrollmentChecker(int dbStringLength, String charsetName, IEnrollmentPreviewGUI gui,
            Suggestions suggestions) throws SecurityException, NoSuchMethodException {
        this.gui = gui;
        if (suggestions == null) {
            throw new NullPointerException("suggestions");
        }
        this.suggestions = suggestions;
        
        stringArrayAsString_toStringMethod =
            StringArrayAsString.class.getDeclaredMethod("toString", Object.class);
        stringArrayAsString_toStringMethod.setAccessible(true);

        dummpyStringArrayAsString = new StringArrayAsString();
        
        this.dbStringLength = dbStringLength;
        if (charsetName != null) {
            databaseCharset = Charset.forName(charsetName);
            databaseCharsetEncoder = databaseCharset.newEncoder();
            databaseCharsetDecoder = databaseCharset.newDecoder();
        } else {
            databaseCharset = null;
            databaseCharsetEncoder = null;
            databaseCharsetDecoder = null;
        }
    }
    
    public void checkField(String dn, IElementField field, Object value, Collection<String> warnings, 
            Set<MyTreeNode> warningNodes)
            throws IllegalAccessException, InvocationTargetException {
        ElementFieldType fieldType = field.getType();
        if (fieldType == ElementFieldType.CS_STRING 
                || fieldType == ElementFieldType.LONG_STRING
                || fieldType == ElementFieldType.STRING) {
            checkString(dn, field, (String) value, warnings, warningNodes);
        } else if (fieldType == ElementFieldType.STRING_ARRAY) {
            String strArrayValue = (String) stringArrayAsString_toStringMethod.invoke(
                    dummpyStringArrayAsString, value);
            checkString(dn, field, strArrayValue, warnings, warningNodes);
        } else {
            //TODO what can we check?
        }
    }
    
    private void checkString(String dn, IElementField field, String value, Collection<String> warnings, 
            Set<MyTreeNode> warningNodes) {
        int length = value.length();
        suggestions.markColumnLength(length);
        if (length > dbStringLength) {
            addWarning(WarningType.VALUE_TOO_LONG, field, dn, warningNodes);
            warnings.add(WarningType.VALUE_TOO_LONG.format(field.getName(), length, dbStringLength));
            //TODO log error
        }

        if (databaseCharset != null) {
            boolean equals;
            try{
                String dbString =databaseCharsetDecoder.decode(
                        databaseCharsetEncoder.encode(CharBuffer.wrap(value))).toString();
                equals = value.equals(dbString);
            } catch (CharacterCodingException e) {
                equals = false;
            }
            
            if (!equals) {
                addWarning(WarningType.CHARSER_NOT_SUPPORTED, field, dn, warningNodes);
                warnings.add(WarningType.CHARSER_NOT_SUPPORTED.format(field.getName()));
                //TODO log error
            }
        }
    }
    
    public void addWarning(WarningType warningType, IElementField field, String dn, Set<MyTreeNode> warningNodes){
        assert warningNodes != null;
        MyTreeNode node;
        node = gui.addNode(
                new String[]{ "Group by WARNING_TYPE", warningType.name(), field.getName(), dn}, 
                TabbedPane.WARNING);
        warningNodes.add(node);
        
        Map<IElementField, Integer> fields = suggestions.warntypeToFieldMap.get(warningType);
        if (fields == null) {
            fields = new HashMap<IElementField, Integer>();
            suggestions.warntypeToFieldMap.put(warningType, fields);
        }
        Integer fieldCount = fields.get(field);
        if (fieldCount == null) {
            fieldCount = 0;
        }
        fields.put(field, fieldCount + 1);
        
        node = gui.addNode(new String[]{ "Group by FIELDS", field.getName(), warningType.name(), dn}, 
                TabbedPane.WARNING);
        warningNodes.add(node);
        Map<WarningType, Integer> warnTypes = suggestions.fieldToWarntypeMap.get(field);
        if (warnTypes == null) {
            warnTypes = new HashMap<WarningType, Integer>();
            suggestions.fieldToWarntypeMap.put(field, warnTypes);
        }
        Integer warntypeCount = warnTypes.get(warningType);
        if (warntypeCount == null) {
            warntypeCount = 0;
        }
        warnTypes.put(warningType, warntypeCount + 1);
    }
}
