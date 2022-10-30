/*
 * Created on Mar 4, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.nextlabs.destiny.container.shared.customapps.hibernateimpl.CustomAppDO;
import com.nextlabs.destiny.container.shared.customapps.mapping.CustomReportJO;
import com.nextlabs.destiny.container.shared.customapps.mapping.PolicyApplicationJO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportDataDO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportFileDO;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/CustomApplicationJtoDConverter.java#1 $
 */

class CustomApplicationJtoDConverter implements ExternalApplicationFileStructure{
    protected final CustomAppJarReader appReader; 
    
    public CustomApplicationJtoDConverter(CustomAppJarReader appReader) {
        this.appReader = appReader;
    }

    /**
     * 
     * @param policyAppJO
     * @param jarFile
     * @return
     * @throws InvalidCustomAppException if the referred file doesn't exist in the jar
     * @throws IOException if error during reading the jar
     */
    protected CustomAppDO convert(PolicyApplicationJO policyAppJO)
            throws InvalidCustomAppException, IOException {
        CustomAppDO appDO = new CustomAppDO();
        
        appDO.setName(policyAppJO.getName());
        appDO.setVersion(policyAppJO.getVersion());
        appDO.setDescription(policyAppJO.getDescription());
        
        List<CustomReportJO> reportJOs = policyAppJO.getCustomReports();
        List<CustomReportDataDO> reportDataDOs = new ArrayList<CustomReportDataDO>(reportJOs.size());
        for(CustomReportJO reportJO : reportJOs){
            CustomReportDataDO reportDataDO = convert(reportJO);
            reportDataDO.setCustomApp(appDO);
            reportDataDOs.add(reportDataDO);
        }
        appDO.setCustomReports(reportDataDOs);
        
        return appDO;
    }

    protected CustomReportDataDO convert(CustomReportJO reportJO)
            throws IOException, InvalidCustomAppException {
        CustomReportDataDO reportDataDO = new CustomReportDataDO();
        
        reportDataDO.setTitle(reportJO.getTitle());
        reportDataDO.setDescription(reportJO.getDescription());

        List<String> designFileNames = reportJO.getDesignFiles();
        List<CustomReportFileDO> reportFileDOs = new ArrayList<CustomReportFileDO>(designFileNames.size());
        
        boolean isFirstOne = true;
        for(String designFileName : designFileNames){
            CustomReportFileDO reportFileDO = convert(designFileName, isFirstOne);
            isFirstOne = false;
            reportFileDO.setReport(reportDataDO);
            reportFileDOs.add(reportFileDO);
        }
        reportDataDO.setReportDesignFiles(reportFileDOs);
        return reportDataDO;
    }

    protected CustomReportFileDO convert(String designFileName, boolean isFirstOne)
            throws IOException, InvalidCustomAppException {
        CustomReportFileDO reportFileDO = new CustomReportFileDO();

        reportFileDO.setTopLevel(isFirstOne);
        
        reportFileDO.setName(designFileName);
        
        String content = appReader.getString(REPORT_CONTENT_FOLDER + "/" + designFileName);
        
        reportFileDO.setContent(content);
        return reportFileDO;
    }
    
}
