/*
 * Created on Mar 19, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps.hibernateimpl;

import java.util.List;

import com.nextlabs.destiny.container.shared.customapps.mapping.CustomReportJO;
import com.nextlabs.destiny.container.shared.customapps.mapping.PolicyApplicationJO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportDataDO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportFileDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/hibernateimpl/CustomApplicationDtoJConverter.java#1 $
 */

public class CustomApplicationDtoJConverter {
    
    public static PolicyApplicationJO convert(CustomAppDO customAppDO) {
        PolicyApplicationJO appJO = new PolicyApplicationJO();

        appJO.setName(customAppDO.getName());
        appJO.setVersion(customAppDO.getVersion());
        appJO.setDescription(customAppDO.getDescription());

        List<CustomReportDataDO> reportDOs = customAppDO.getCustomReports();
        for (CustomReportDataDO reportDO : reportDOs) {
            CustomReportJO reportJO = convert(reportDO);
            appJO.addCustomReport(reportJO);
        }
        return appJO;
    }

    public static CustomReportJO convert(CustomReportDataDO reportDO) {
        CustomReportJO reportJO = new CustomReportJO();

        reportJO.setTitle(reportDO.getTitle());
        reportJO.setDescription(reportDO.getDescription());

        List<CustomReportFileDO> designFiles = reportDO.getReportDesignFiles();

        for (CustomReportFileDO designFile : designFiles) {
            reportJO.addDesignFile(designFile.getName());
        }
        return reportJO;
    }
}
