/*
 * Created on Dec 3, 2007
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2007 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.dbinit.hibernate;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/dbinit/src/java/main/com/bluejungle/destiny/tools/dbinit/hibernate/ResultSetKey.java#1 $
 */

public interface ResultSetKey {
	String COLUMN_NAME 		= "COLUMN_NAME";
	String TYPE_NAME 		= "TYPE_NAME";
	String DATA_TYPE 		= "DATA_TYPE";
	String COLUMN_SIZE 		= "COLUMN_SIZE";
	String DECIMAL_DIGITS 	= "DECIMAL_DIGITS";
	String IS_NULLABLE 		= "IS_NULLABLE";
	
	String FK_NAME 			= "FK_NAME";
	String FKCOLUMN_NAME 	= "FKCOLUMN_NAME";
	String PKCOLUMN_NAME 	= "PKCOLUMN_NAME";
	String PKTABLE_NAME 	= "PKTABLE_NAME";
	String PK_NAME 			= "PK_NAME";
	String INDEX_NAME 		= "INDEX_NAME";
	
	String TABLE_NAME 		= "TABLE_NAME";
	
	String TABLE_CAT		= "TABLE_CAT";
	
	String TABLE_SCHEM 		= "TABLE_SCHEM";
	
	String KEY_SEQ 			= "KEY_SEQ";
	
}
