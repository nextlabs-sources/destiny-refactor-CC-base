package com.nextlabs.destiny.container.shared.customapps.hibernateimpl;

import java.util.ArrayList;
import java.util.List;

import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportDataDO;
import com.nextlabs.destiny.container.shared.inquirymgr.customapps.hibernateimpl.CustomReportUIDO;

/**
 * This represents each custom application and is stored in the management
 * database. Each custom application contains a list of reports. Each report 
 * consists of one or more report design files. 
 * 
 * The report informations is stored in the activity database. The list of 
 * reports that the custom application contains has to be initialized manually 
 * from the report database. The object that initializes this object is 
 * responsible for manually initializing this list. 
 * 
 * @author ssen
 *
 */
public class CustomAppDO {
    
    private Long id;
    
    private String name;
    
    private String version;
    
    private String description;

    /**
     * Not persisted - need to be initialized from the custom report tables
     * in the activity database.
     */
    private List<CustomReportDataDO> customReports = new ArrayList<CustomReportDataDO>();

    /**
     * Not persisted - need to be initialized from the custom report tables
     * in the activity database.
     */
    private CustomReportUIDO reportUI;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CustomReportDataDO> getCustomReports() {
        return customReports;
    }

    public void setCustomReports(List<CustomReportDataDO> customReports) {
        this.customReports = customReports;
    }
     
    public void addCustomReport(CustomReportDataDO reportData) {
        customReports.add(reportData);
    }

    public CustomReportUIDO getReportUI() {
        return reportUI;
    }

    public void setReportUI(CustomReportUIDO reportUI) {
        this.reportUI = reportUI;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomAppDO other = (CustomAppDO) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }
}
