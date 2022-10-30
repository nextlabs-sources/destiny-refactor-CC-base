package com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl;

import com.nextlabs.destiny.container.shared.customapps.hibernateimpl.CustomAppDO;

/**
 * This stores the content of the ui-config.xml file that describes the Report UI
 * for a specific application. At runtime the content is parsed and the appropriate 
 * UI generated. 
 * 
 *  This has a reference back to the custom application.
 * 
 * @author ssen
 * 
 */
public class CustomReportUIDO {
    
    // Used by any class that does a query based on the customAppId
    public static final String CUSTOM_APP_ID_ATTR_NAME = "customAppId";
    
    private Long id;
    
    private Long customAppId;

    private String fileContent;

    // Not persisted
    private CustomAppDO customApp;
    
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

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public CustomAppDO getCustomApp() {
        return customApp;
    }

    public void setCustomApp(CustomAppDO customApp) {
        this.customApp = customApp;
    }
}
