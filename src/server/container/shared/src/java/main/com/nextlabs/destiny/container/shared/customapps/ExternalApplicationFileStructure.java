/*
 * Created on Mar 1, 2010
 * 
 * All sources, binaries and HTML pages (C) copyright 2004-2010 by NextLabs, Inc.,
 * San Mateo CA, Ownership remains with NextLabs, Inc., All rights reserved
 * worldwide.
 */
package com.nextlabs.destiny.container.shared.customapps;
/**
 * @author hchan
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/container/shared/src/java/main/com/nextlabs/destiny/container/shared/customapps/ExternalApplicationFileStructure.java#1 $
 */

public interface ExternalApplicationFileStructure {
    /**
      / 
        config
          data-config.xml
          ui-config.xml
        content
          report
            test.rptdesign
      
     */

    // don't put leading slash
    String CONFIG_FOLDER = "config";
    String DATA_CONFIG_XML = CONFIG_FOLDER + "/data-config.xml";
    
    String UI_CONFIG_XML_NAME = "ui-config.xml";
    String UI_CONFIG_XML = CONFIG_FOLDER + "/" + UI_CONFIG_XML_NAME;
    
    
    // don't put leading slash
    String CONTENT_FOLDER = "content";
    
    String REPORT_CONTENT_FOLDER = CONTENT_FOLDER + "/report";
}
