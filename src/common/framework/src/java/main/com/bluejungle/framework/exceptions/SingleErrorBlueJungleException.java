/*
 * Created on Mar 14, 2005
 * 
 * All sources, binaries and HTML pages (C) copyright 2004 by Blue Jungle Inc.,
 * Redwood City CA, Ownership remains with Blue Jungle Inc, All rights reserved
 * worldwide.
 */
package com.bluejungle.framework.exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.bluejungle.framework.messages.ILocalizedMessageRepository;
import com.bluejungle.framework.messages.IMessageRequester;
import com.bluejungle.framework.messages.LocalizedMessageRepositoryFactory;
import com.bluejungle.framework.messages.MessageNotFoundException;

/**
 * Blue Jungle exception hierarchy for single-error exceptions. Extend this
 * exception class and all you need to do is: (1) define constructors to convert
 * message parameters into an Object[], (2) specify the error message format
 * string in the
 * com.bluejungle.framework.exceptions.SingleErrorBlueJungleException.properties
 * file using the class name as the key.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/exceptions/SingleErrorBlueJungleException.java#1 $
 */

public abstract class SingleErrorBlueJungleException extends BlueJungleException implements IMessageRequester {

    /*
     * Constant error messages
     */
    private static final String NO_ERROR_MESSAGE_BUNDLE_FOUND_ERROR = "No error message bundle was found to construct an error message for exception class: ";

    /*
     * These hold instances of ILocalizedMessageRepository
     */
    private static ILocalizedMessageRepository defaultErrorMessages;
    private static ILocalizedMessageRepository localizedErrorMessages;

    /**
     * Static initialization block to initialize the message repository for
     * exceptions in the SingleErrorBlueJungleException hierarchy of classes.
     */
    static {
        SingleErrorBlueJungleException.defaultErrorMessages = LocalizedMessageRepositoryFactory.createMessageRepository(SingleErrorBlueJungleException.class.getName(), Locale.ENGLISH);
        SingleErrorBlueJungleException.localizedErrorMessages = LocalizedMessageRepositoryFactory.createMessageRepository(SingleErrorBlueJungleException.class.getName(), Locale.ENGLISH);
    }

    /*
     * Private variables:
     */
    private List placeholderValues = new ArrayList();

    /**
     * Constructor
     *  
     */
    public SingleErrorBlueJungleException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param cause
     */
    public SingleErrorBlueJungleException(Throwable cause) {
        super(cause);
    }

    /**
     * Returns the NON-localized message for this exception instance.
     * 
     * @return Message
     * @see java.lang.Throwable#getMessage()
     */
    public final String getMessage() {
        return this.getMessage(SingleErrorBlueJungleException.defaultErrorMessages);
    }

    /**
     * Returns the localized message for this exception instance.
     * 
     * @return Message
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    public final String getLocalizedMessage() {
        return this.getMessage(SingleErrorBlueJungleException.localizedErrorMessages);
    }

    /**
     * Returns the appropriate error message referenced by this exception
     * instance.
     * 
     * @param message
     *            repository
     * @return message for this exception
     */
    private String getMessage(ILocalizedMessageRepository messageRepository) {
        String message = null;
        try {
            if (messageRepository != null) {
                message = messageRepository.getFormattedMessageFor(this);
            } else {
                Throwable cause = this.getCause();
                message = NO_ERROR_MESSAGE_BUNDLE_FOUND_ERROR + this.getClass().getName() + "\n" + this.getStackTrace().toString();
            }
        } catch (MessageNotFoundException e) {
            // TODO:
        }
        return message;
    }

    /**
     * @see com.bluejungle.framework.messages.IMessageRequester#getMessageKey()
     */
    public final String getMessageKey() {
        return this.getClass().getName();
    }

    /**
     * @see com.bluejungle.framework.messages.IMessageRequester#getIndexedValues()
     */
    public final Object[] getIndexedValues() {
        return this.placeholderValues.toArray();
    }

    /**
     * This method is exposed to subclasses of this class to add placeholder
     * values for their respective error messages.
     * 
     * @param value
     */
    protected void addNextPlaceholderValue(Object value) {
        this.placeholderValues.add(value);
    }
    
    /**
     * Set the error message parameters
     * @param values the error message parameters
     */
    protected void setPlaceholderValues(Object[] values) {
        if (values == null) {
            throw new NullPointerException("values cannot be null.");
        }
        
        this.placeholderValues.clear();
        for (int i=0; i<values.length; i++) {
            this.placeholderValues.set(i, values[i]);
        }
    }
}