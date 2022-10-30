/*
 * Created on Mar 21, 2006
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.destiny.tools.enrollment.filereader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.bluejungle.destiny.tools.enrollment.filter.FileFormatException;
import com.bluejungle.destiny.services.enrollment.types.EnrollmentProperty;

/**
 * @author safdar
 * @version $Id: //depot/Destiny/D_Nimbus/release/8.1/main/src/server/tools/enrollment/src/java/main/com/bluejungle/destiny/tools/enrollment/filereader/EnrollmentDefinitionReader.java#1 $
 */

public class EnrollmentDefinitionReader extends BaseFileReader{
    
    public EnrollmentDefinitionReader(File file) throws FileNotFoundException, IOException,
            FileFormatException {
        super(file);
    }

    public List<EnrollmentProperty> getProperties() {
        return this.propertyList;
    }

    @Override
    protected String getFileDescription() {
        return "The enrollment definition file";
    }


    @Override
    protected void validate(Properties properties) throws FileFormatException {
        String login = properties.getProperty("login");
        if (login != null) {
            throw new FileFormatException("Invalid definition file");
        }
    }
}
