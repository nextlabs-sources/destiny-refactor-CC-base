package com.bluejungle.framework.datastore.hibernate.utils;

/*
 * All sources, binaries and HTML pages (C) copyright 2007 by NextLabs, Inc.
 * San Mateo CA, Ownership remains with NextLabs, Inc.
 * All rights reserved worldwide.
 *
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/framework/src/java/main/com/bluejungle/framework/datastore/hibernate/utils/IMassDMLFormatter.java#1 $
 */

/**
 * This interface defines the contract for formatters of Mass DML
 * that let you build DML statements based on the configuration
 * of the formatter (which is itself is based on the features
 * of the base RDBMS).  
 *
 * @author Sergey Kalinichenko
 */
public interface IMassDMLFormatter {

    /**
     * Given a target, a set of source fields, and an optional source,
     * builds an insertion SQL that either selects from the source, or
     * uses 'values'.
     * The implementation of this method will replace $<name>$ variable
     * expressions as follows (quotes are for clarity):
     * - In the target, "<name>," will be inserted for RDBMS that support
     *   sequences; the variable will be removed for RDBMS supporting identity
     *   columns.
     * - In the source fields, the selection expression will be inserted
     *   when sequences are supported. For RDBMS with identity columns, the
     *   reference will be removed.
     * - When the 'source' is empty, the method will insert a 'select'...'from'
     *   or 'values', depending on how the database deals with sequences and
     *   on the RDBMS support of identity columns.
     *
     * @param target A list of target fields with optional $<name>$ variables.
     * @param sourceFields A list of destination fields with optional $<name>$
     * variable references.
     * @param source This argument is optional. Passing null means that
     * the method must use the default source for the database, or use
     * 'values' if the RDBMS uses identity columns.
     * @return an insertion statement formatted according to the rules above.
     */
    String formatInsert( String target, String sourceFields, String source);

}
