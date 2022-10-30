package com.bluejungle.domain.log;

/*
 * All sources, binaries and HTML pages (C) copyright 2004-2017 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.utils.DynamicAttributes;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IMultivalue;

public class AttributeExternalizer {
    static Log log = LogFactory.getLog(AttributeExternalizer.class.getName());

    static Set<String> internalResAttributes;

    static {
        internalResAttributes = new HashSet<String>();

        internalResAttributes.add("access_date");
        internalResAttributes.add("created_date");
        internalResAttributes.add("isdirectory");
        internalResAttributes.add("size");
        internalResAttributes.add("created");
        internalResAttributes.add("modified");
        internalResAttributes.add("modified_by");
        internalResAttributes.add("filegroup");
        internalResAttributes.add("modified_date");
        internalResAttributes.add("owner");
        internalResAttributes.add("owner_ldap_group");
        internalResAttributes.add("name");
    }

    public static boolean keyShouldBeLogged(String key) {
        // keys that start with ce:: (except for ce::destinytype) and
        // a few specifically enumerated keys are "internal" and
        // should not be logged
        //
        // Unfortunately, SpecAttribute, where ce::destinytype is
        // defined, is in a jar that is compiled after this one, so we
        // can't reference it.
        return !((key.startsWith("ce::") && !key.equals("ce::destinytype")) || internalResAttributes.contains(key));
    }
    
    // Used by the V2 code
    public static void externalizeResourceAttributes(ObjectOutput out, DynamicAttributes attr) throws IOException {
        if (attr != null) {
            // We don't write everything - keys that start ce:: or are a known 'internal' key
            // should not be logged.
            int attrSize = 0;
            for (Map.Entry<String,IEvalValue> entry : attr.entrySet()) {
                String key = entry.getKey();
                if (keyShouldBeLogged(key)) {
                    attrSize++;
                }
            }

            // Can't just use attr.size()
            out.writeInt(attrSize);
            for (Map.Entry<String,IEvalValue> entry : attr.entrySet()) {
                String key = entry.getKey();
                if (keyShouldBeLogged(key)) {
                    out.writeUTF(key + ", " + entry.getValue().getValue());
                }
            }
        } else {
            out.writeInt(0);
        }

        return;
    }

    // Used by the V3 code
    public static DynamicAttributes readResourceAttributes(ObjectInput in) throws IOException {
        DynamicAttributes attr = null;

        int numAttrs = in.readInt();
        
        if (numAttrs >= 0) {
            attr = new DynamicAttributes();

            for (int i = 0; i < numAttrs; i++) {
                String key = in.readUTF();
                
                char flag = in.readChar();
                
                switch (flag) {
                    case 'S':
                        // Single value
                        String value = in.readUTF();
                        attr.put(key, value);
                        break;
                    case 'M':
                        // Multivalue
                        int numValues = in.readInt();
                        for (int j = 0; j < numValues; j++) {
                            String mvalue = in.readUTF();
                            attr.add(key, mvalue);
                        }
                        break;
                    default:
                        throw new IOException("Illegal flag in resource attributes. Got " + flag + ", but expecting S or M");
                }
            }
        }

        return attr;
    }

    public static void writeResourceAttributes(ObjectOutput out, DynamicAttributes attr) throws IOException {
        if (attr != null) {
            // We don't write everything - keys that start ce:: or are a known 'internal' key
            // should not be logged.
            int attrSize = 0;
            for (Map.Entry<String,IEvalValue> entry : attr.entrySet()) {
                String key = entry.getKey();
                if (keyShouldBeLogged(key)) {
                    attrSize++;
                }
            }

            // Can't just use attr.size()
            out.writeInt(attrSize);
            for (Map.Entry<String,IEvalValue> entry : attr.entrySet()) {
                String key = entry.getKey();
                if (keyShouldBeLogged(key)) {
                    Object value = entry.getValue().getValue();

                    out.writeUTF(key);

                    if (value instanceof String) {
                        out.writeChar('S');
                        writeResourceAttributeValue(out, (String)value);
                    } else if (value instanceof IMultivalue) {
                        out.writeChar('M');
                        IMultivalue mvalue = (IMultivalue)value;

                        out.writeInt(mvalue.size());
                        for (IEvalValue subvalue : (IMultivalue)value) {
                            writeResourceAttributeValue(out, (String)subvalue.getValue());
                        }
                    }
                }
            }
        } else {
            // We need a way to distinguish between a 0-sized set of attributes and a null
            out.writeInt(-1);
        }

        return;
    }

    static void writeResourceAttributeValue(ObjectOutput out, String value) throws IOException {
        // Remarkably, writeUTF will throw an exception if the string is > 64K bytes in size.
        // We don't actually need anywhere near that much, because we only store 4K of the value in
        // Determining the actual number of bytes of a string depends on the character encoding, so
        // rather than get cute, I'll truncate the value to 8K. This should be (a) small enough
        // that we can externalize it and (b) bigger than the largest amount we can store in the DB
        
        if (value.length() > 8192) {
            value = value.substring(0, 8192);
            
            if (log.isInfoEnabled()) {
                log.info("Long attribute value truncated to 8K. Value starts [" + value.substring(0, 100) + "...]");
            }
            
        }
        out.writeUTF(value);
    }
}
