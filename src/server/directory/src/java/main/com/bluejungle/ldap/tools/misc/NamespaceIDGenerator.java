/*
 * Created on Apr 26, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.ldap.tools.misc;

import com.bluejungle.ldap.tools.ldifconverter.impl.ByteArrayToHexStringConverter;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/directory/tools/src/java/main/com/bluejungle/ldap/tools/misc/NamespaceIDGenerator.java#1 $
 */

public class NamespaceIDGenerator {

    public static final int MAX_NAMESPACE_CHARS = 64;
    private static final int MAX_CHARS = 128;

    /**
     * Main routine
     * 
     * @param args
     */
    public static void main(String[] args) {
        String namespace = args[0];
        String[] relativeIDs = null;

        // Means we have a count + ids:
        relativeIDs = new String[args.length - 1];
        System.arraycopy(args, 1, relativeIDs, 0, args.length - 1);
        System.out.println("----------------------------------------------------------------------------");
        System.out.println("Namespace: '" + namespace + "'");
        System.out.println("----------------------------------------------------------------------------");
        for (int i = 0; i < relativeIDs.length; i++) {
            try {
                String relativeID = relativeIDs[i];
                String newID = generateImportID(namespace, relativeID.getBytes());
                System.out.println("Relative ID ('" + relativeID + "') --> '" + newID + "'");
            } catch (RelativeIDException e) {
                System.out.println(e);
            }
        }
        System.out.println("----------------------------------------------------------------------------");
    }

    /**
     * Generates an internal import id for the given category and unique name
     * 
     * @param importCategory
     * @param uniqueNameWithinCategory
     * @return
     * @throws RelativeIDException
     */
    public static String generateInternalImportID(ImportCategoryEnumType importCategory, String uniqueNameWithinCategory) throws RelativeIDException {
        if (importCategory == null) {
            throw new NullPointerException("importCateogory is null");
        }
        if (uniqueNameWithinCategory == null) {
            throw new NullPointerException("uniqueNameWithinCategory is null");
        }
        if (uniqueNameWithinCategory.indexOf(ImportCategoryEnumType.NAMESPACE_COMPONENT_DELIMITER) >= 0) {
            throw new RelativeIDException("The import name provided: '" + uniqueNameWithinCategory + "' must not contain the '" + ImportCategoryEnumType.NAMESPACE_COMPONENT_DELIMITER + "' character.");
        }

        StringBuffer internalImportID = new StringBuffer(MAX_CHARS);
        internalImportID.append(importCategory.getName()).append(ImportCategoryEnumType.NAMESPACE_COMPONENT_DELIMITER).append(uniqueNameWithinCategory);
        String internalImportIDStr = internalImportID.toString();
        if (internalImportIDStr.length() > MAX_CHARS) {
            throw new RelativeIDException("The import name provided: '" + uniqueNameWithinCategory + "' was too long for category: '" + importCategory.getName() + "'");
        }
        return internalImportIDStr;
    }

    /**
     * Converts a relative-id into a hex string, and creates an 'id' string by
     * concatenating the padded namespace string with the hex string
     * 
     * @param namespace
     * @param relativeID
     * @return
     */
    public static String generateImportID(String namespace, byte[] relativeID) throws RelativeIDException {
        if (namespace.length() > MAX_NAMESPACE_CHARS) {
            throw new IllegalStateException("namespace: '" + namespace + "' is too long");
        }

        // Convert the relative ID to a hex string:
        String relativeIdAsHexStr = ByteArrayToHexStringConverter.convertToHexString(relativeID);

        // Create the final id string and make sure it's not too long:
        StringBuffer importIdStrBuf = new StringBuffer(MAX_CHARS);
        importIdStrBuf.append(namespace).append(ImportCategoryEnumType.NAMESPACE_COMPONENT_DELIMITER).append(relativeIdAsHexStr);
        String importIdStr = importIdStrBuf.toString();
        if (importIdStr.length() > MAX_CHARS) {
            throw new RelativeIDException("Relative ID '" + relativeID + "' is too long for import ID generation.");
        }

        return importIdStr;
    }
}