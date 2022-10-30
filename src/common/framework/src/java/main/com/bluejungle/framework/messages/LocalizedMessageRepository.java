/*
 * Created on Dec 24, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.messages;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;

import com.bluejungle.framework.comp.IConfiguration;

/**
 * This class provides an API to access localized Blue Jungle messages. The
 * predominant client of this class is the Blue Jungle exception class.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public class LocalizedMessageRepository implements ILocalizedMessageRepository {

    // Configuration and log objects:
    private IConfiguration configuration;
    private Log log;

    // Repository-related objects:
    private Locale locale;
    private String name;
    private String resourceBundleName;
    private ResourceBundle messageBundle;

    /**
     * Constructor
     */
    public LocalizedMessageRepository() {
    }

    /**
     * Sets the configuration
     * 
     * @param config
     * @see com.bluejungle.framework.comp.IConfigurable#setConfiguration(com.bluejungle.framework.comp.IConfiguration)
     */
    public void setConfiguration(IConfiguration config) {
        this.configuration = config;
    }

    /**
     * Gets the configuration
     * 
     * @return IConfiguration
     * @see com.bluejungle.framework.comp.IConfigurable#getConfiguration()
     */
    public IConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Disposes the object
     * 
     * @see com.bluejungle.framework.comp.IDisposable#dispose()
     */
    public void dispose() {
    }

    /**
     * Set logger
     * 
     * @param Log
     * @see com.bluejungle.framework.comp.ILogEnabled#setLog(org.apache.commons.logging.Log)
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * Get the logger
     * 
     * @return Log
     * @see com.bluejungle.framework.comp.ILogEnabled#getLog()
     */
    public Log getLog() {
        return this.log;
    }

    /**
     * Initializes the repository
     * 
     * @see com.bluejungle.framework.comp.IInitializable#init()
     */
    public void init() {
        String resourceBundleName = (String) this.configuration.get(RESOURCE_BUNDLE_NAME);
        Locale locale = (Locale) this.configuration.get(LOCALE);

        try {
            this.init(resourceBundleName, locale);
        } catch (LocalizedMessageRepositoryInitException e) {
            // TODO: We need the Component Manager framework to include an
            // InitException type of thing which we can throw here.
        }
    }

    /**
     * Initializes the message repository from a properties file, without an
     * IConfiguration object.
     * 
     * @param resourceBundleName
     * @param locale
     * @throws LocalizedMessageRepositoryInitException
     */
    public void init(String resourceBundleName, Locale locale) throws LocalizedMessageRepositoryInitException {
        this.locale = locale;
        if (this.locale == null) {
            this.locale = Locale.getDefault();
        }
        this.resourceBundleName = resourceBundleName;
        try {
            this.messageBundle = ResourceBundle.getBundle(this.resourceBundleName, this.locale);
        } catch (Exception e) {
            throw new LocalizedMessageRepositoryInitException("Message Bundle named '" + this.resourceBundleName + "' does not exist.");
        }
    }

    /**
     * It populates the message format referenced by 'key' and returns a
     * formatted string.
     * 
     * @param key -
     *            Key to identify the message that needs to be retrieved
     * @param indexedValues
     *            Object array with values corresponding to the indexed
     *            placeholders in the message format
     * @return String - Formatted Message
     */
    protected String getFormattedMessage(String key, Object[] indexedValues) {
        // Construct requested message from bundle:
        String pattern = this.getPattern(key);
        String formattedMessage = null;

        if (pattern != null) {
            formattedMessage = MessageFormat.format(pattern, indexedValues);
        }

        return formattedMessage;
    }

    /**
     * Returns whether or not a message exists for the given key, in this
     * repository.
     * 
     * @param key
     * @return boolean
     */
    public boolean containsPattern(String key) {
        return (this.getPattern(key) != null);
    }

    /**
     * Returns the pattern corresponding to the key. The only purpose served by
     * this method is to abstract away the actual storage of the message - it
     * could be stored either in a ResourceBundle object, or a Properties object -
     * in case one is needed.
     * 
     * @param key
     * @return Message
     */
    protected String getPattern(String key) {
        String msgTemplate = null;
        if (this.messageBundle != null) {
            msgTemplate = this.messageBundle.getString(key);
        }
        return msgTemplate;
    }

    /**
     * @see com.bluejungle.framework.messages.ILocalizedMessageRepository#getFormattedMessageFor(com.bluejungle.framework.messages.IMessageRequester)
     */
    public String getFormattedMessageFor(IMessageRequester requestor) throws MessageNotFoundException {
        return this.getFormattedMessage(requestor.getMessageKey(), requestor.getIndexedValues());
    }
}