/*
 * Created on Mar 30, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.report.datagen;

import com.nextlabs.destiny.container.shared.inquirymgr.report.impl.InquiryMgrReportDataManger;
import com.nextlabs.destiny.container.shared.inquirymgr.report.impl.ReportDataMangerStandalone;


/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/report/datagen/ReportDataManagerFactory.java#1 $
 */

public class ReportDataManagerFactory {
    
    private static Class<? extends IReportDataManager> clazz;
    static{
        setEclipseMode();
    }
    
    protected static void setClass(Class<? extends IReportDataManager> clazz){
        ReportDataManagerFactory.clazz = clazz;
    }
    
    public static void setServerMode(){
        setClass(InquiryMgrReportDataManger.class);
    }
    
    public static void setEclipseMode(){
        setClass(ReportDataMangerStandalone.class);
    }
    
    public static IReportDataManager getReportDataManager(){
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } 
    }
                                                           
}
