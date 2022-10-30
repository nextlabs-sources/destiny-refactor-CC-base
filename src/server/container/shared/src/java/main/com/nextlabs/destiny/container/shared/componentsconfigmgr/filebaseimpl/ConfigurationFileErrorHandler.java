/*
 * Created on Dec 3, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import java.util.ArrayList;
import java.util.Collection;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class serves as an error handler to the DOMParser that is used to
 * validate the contents of the Destiny configuration file against the
 * configuration schema. Every time an error() call is triggered, an error
 * message is created and stored ready to be accessed at the end of the parse.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/server/base/com/bluejungle/destiny/server/shared/configuration/DestinyConfigurationErrorHandler.java#1 $:
 */

public class ConfigurationFileErrorHandler implements ErrorHandler {

    private Collection<String> errors;

    /**
     * Constructor
     *  
     */
    public ConfigurationFileErrorHandler() {
        super();
        this.errors = new ArrayList<String>();
    }

    /**
     * Returns the list of error messages collected so far.
     * 
     * @return Collection object of error messages
     */
    public Collection<String> getErrorMessages() {
        return this.errors;
    }

    /**
     * Returns whether an error has occured so far.
     * 
     * @return boolean whether errors exist or not
     */
    public boolean doErrorsExist() {
        return (this.errors.size() > 0);
    }

    /**
     * Creates a 'warning' message and stores it in the 'errors' array.
     */
    public void warning(SAXParseException exception) throws SAXException {
        String errorMsg = new String("WARNING :: (Line " + exception.getLineNumber() + ", Column " + exception.getColumnNumber() + ") " + exception.getMessage());
        this.errors.add(errorMsg);
    }

    /**
     * Creates an 'error' message and stores it in the 'errors' array.
     */
    public void error(SAXParseException exception) {
        String errorMsg = new String("ERROR :: (Line " + exception.getLineNumber() + ", Column " + exception.getColumnNumber() + ") " + exception.getMessage());
        this.errors.add(errorMsg);
    }

    /**
     * Creates a 'fatal error' message and stores it in the 'errors' array.
     */
    public void fatalError(SAXParseException exception) throws SAXException {
        String errorMsg = new String("FATAL ERROR:: (Line " + exception.getLineNumber() + ", Column " + exception.getColumnNumber() + ") " + exception.getMessage());
        this.errors.add(errorMsg);
    }
}