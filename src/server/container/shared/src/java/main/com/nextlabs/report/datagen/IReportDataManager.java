/*
 * Created on Mar 30, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.report.datagen;


/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/report/datagen/IReportDataManager.java#1 $
 */

public interface IReportDataManager {
    byte DB_TYPE_POSTGRESQL = 10;
    byte DB_TYPE_ORACLE     = 20;
    byte DB_TYPE_MS_SQL     = 30;
    
    ResultData runQuery(String query) throws Exception;
    
    byte getDatabaseType();
    
    int getDatabaseVersion();
    
    String getMappedAction(String name);
    
    String convertToTimestamp(String timestampString);
}
