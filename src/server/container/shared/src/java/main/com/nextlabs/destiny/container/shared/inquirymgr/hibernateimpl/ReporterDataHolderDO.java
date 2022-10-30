 /* 
 * All sources, binaries and HTML pages (C) copyright 2004-2009 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc, All rights reserved
 * worldwide.
 */
 package com.nextlabs.destiny.container.shared.inquirymgr.hibernateimpl;

 /**
  * This acts as a book-keeper class for holding various reporter related
  * data. E.g. this can store the timestamp that specified the earliest date
  * from which the reports can be generated. It can also store various other 
  * data such as if the sync process has started or not.
  * 
  * The content is intended to be generic so that it can be used for various 
  * use-cases.
  *
  */
public class ReporterDataHolderDO {
    
    private String property;
    private String value;
    
    public String getProperty() {
        return property;
    }
    
    public void setProperty(String key) {
        this.property = key;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}
