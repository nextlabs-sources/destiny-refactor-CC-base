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
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/mapping/CustomReportJO.java#1 $
 */

public class CustomReportJO extends IamJO {
    private String title;
    private String description;
    private List<String> designFiles;
    
    public CustomReportJO(){
        designFiles = new LinkedList<String>();
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
    public List<String> getDesignFiles() {
        return designFiles;
    }
    public void addDesignFile(String designFilename) {
        this.designFiles.add(designFilename);
    }
    
    @Override
    protected String getCurrentNodeName() {
        return "custom-reports/report";
    }

    protected void addRule(Digester digester, String parent, String current) {
        digester.addObjectCreate(current, CustomReportJO.class);
        digester.addBeanPropertySetter(current + "/title", "title");
        digester.addBeanPropertySetter(current + "/description", "description");
        digester.addCallMethod(current + "/design/file", "addDesignFile", 0);
    }
    
    protected String toString(String prefix){
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("CustomReportJO").append("\n")
          .append(prefix).append(" title = ").append(title).append("\n")
          .append(prefix).append(" description = ").append(description).append("\n");
        
        for (String df : designFiles) {
            sb.append(prefix).append("   file = ").append(df).append("\n");
        }
        return sb.toString();
    }
}
