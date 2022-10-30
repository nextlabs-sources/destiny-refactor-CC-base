package com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl;

import java.util.ArrayList;
import java.util.List;

import com.nextlabs.destiny.container.shared.customapps.hibernateimpl.CustomAppDO;

/**
 * This contains the data of each report present in a custom application and
 * the data is stored in the activity database.
 * It contains the reference to the parent application (the information of which
 * is in the management database) and also the list of report design files.
 * 
 * @author ssen
 *
 */
public class CustomReportDataDO {
    
    // Used by any class that does a query based on the customAppId
    public static final String CUSTOM_APP_ID_ATTR_NAME = "customAppId";
    
    private Long id;
    
    private Long customAppId;
    
    // Not persisted
    private CustomAppDO customApp;
    
    private String title;
    
    private String description;

    private List<CustomReportFileDO> reportDesignFiles = new ArrayList<CustomReportFileDO>();
 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomAppId() {
        return customAppId;
    }

    public void setCustomAppId(Long customAppId) {
        this.customAppId = customAppId;
    }

    public CustomAppDO getCustomApp() {
        return customApp;
    }

    public void setCustomApp(CustomAppDO customApp) {
        this.customApp = customApp;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CustomReportFileDO> getReportDesignFiles() {
        return reportDesignFiles;
    }

    public void setReportDesignFiles(List<CustomReportFileDO> reportDesignFiles) {
        if (reportDesignFiles == null) {
            throw new IllegalArgumentException("Cannot set null as report design files");
        }
        this.reportDesignFiles = reportDesignFiles;
        for (CustomReportFileDO thisDesignFile : reportDesignFiles) {
            if (thisDesignFile != null) {
                thisDesignFile.setReport(this);
            }
        }
    }
}
