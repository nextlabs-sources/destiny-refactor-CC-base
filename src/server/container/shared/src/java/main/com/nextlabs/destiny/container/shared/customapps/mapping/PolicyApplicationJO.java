/*
 * Created on Mar 1, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps.mapping;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.digester.Digester;

/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/mapping/PolicyApplicationJO.java#1 $
 */

public class PolicyApplicationJO extends IamJO {
    private String name;
    private String version;
    private String description;
    private List<CustomReportJO> customReports;
    
    public PolicyApplicationJO(){
        customReports = new LinkedList<CustomReportJO>();
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
    public List<CustomReportJO> getCustomReports() {
        return customReports;
    }
    public void addCustomReport(CustomReportJO customReport) {
        this.customReports.add(customReport);
    }
    
    @Override
    protected String getCurrentNodeName() {
        return "policy-application";
    }

    @Override
    protected void addRule(Digester digester, String parent, String current){
        digester.addObjectCreate(current, PolicyApplicationJO.class);
        digester.addBeanPropertySetter(current + "/name", "name");
        digester.addBeanPropertySetter(current + "/version", "version");
        digester.addBeanPropertySetter(current + "/description", "description");
        digester.addSetNext(new CustomReportJO().accept(digester, current), "addCustomReport");
    }
    
    @Override
    protected String toString(String prefix){
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("PolicyApplicationJO").append("\n")
          .append(prefix).append(" name = ").append(name).append("\n")
          .append(prefix).append(" version = ").append(version).append("\n")
          .append(prefix).append(" description = ").append(description).append("\n");
        
        for (CustomReportJO report : customReports) {
            sb.append(report.toString(prefix + "    "));
        }
        return sb.toString();
    }
}
