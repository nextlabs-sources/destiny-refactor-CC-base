package com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl;

/**
 * This represents each report design file DataObject.
 * 
 * @author ssen
 *
 */
public class CustomReportFileDO {
    
    private Long id;

    private CustomReportDataDO report;

    private Integer position;
    
    private String name;
    
    private String content;
    
    /**
     * Transient. This information has to be obtained from the ds-config file
     * and set after instantiating the class.
     */
    private boolean isTopLevel;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomReportDataDO getReport() {
        return report;
    }

    public void setReport(CustomReportDataDO report) {
        this.report = report;
    }
    
    public Integer getPosition() {
        return report.getReportDesignFiles().indexOf(this);
    }

    public void setPosition(Integer position) {
        // not used, calculated value, see getIndex() method
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isTopLevel() {
        return isTopLevel;
    }

    public void setTopLevel(boolean isTopLevel) {
        this.isTopLevel = isTopLevel;
    }
}
    
