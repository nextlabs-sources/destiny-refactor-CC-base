/*
 * Created on Dec 8, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.nextlabs.destiny.container.shared.componentsconfigmgr.filebaseimpl;

import java.util.Collection;

import com.bluejungle.framework.utils.CollectionUtils;

/**
 * Exception class for errors that occur during validation and parsing of
 * Destiny Configuration objects - we need this class since there may be more
 * than one error and all errors need to be stored in the same exception object.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class ConfigurationInitException extends Exception {

    private Collection<String> messages;

    /**
     * Constructor
     *  
     */
    public ConfigurationInitException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param message
     */
    public ConfigurationInitException(String message) {
        super(message);
    }

    /**
     * Constructor
     * 
     * @param messages
     */
    public ConfigurationInitException(Collection<String> messages) {
		super(CollectionUtils.asString(messages, ",\n"));
		this.messages = messages;
	}

    /**
     * Returns the collection of messages.
     * 
     * @return Collection object of error messages
     */
    public Collection<String> getMessages() {
        return this.messages;
    }

    /**
     * Constructor
     * 
     * @param message
     * @param cause
     */
    public ConfigurationInitException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public ConfigurationInitException(Throwable cause) {
        super(cause);
    }
}