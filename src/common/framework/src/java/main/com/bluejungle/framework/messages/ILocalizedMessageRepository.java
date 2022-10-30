package com.bluejungle.framework.messages;

import java.util.Locale;

import com.bluejungle.framework.comp.IConfigurable;
import com.bluejungle.framework.comp.IDisposable;
import com.bluejungle.framework.comp.IInitializable;
import com.bluejungle.framework.comp.ILogEnabled;

/**
 * This interface defines a repository to obtain localized strings.
 * 
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/etc/eclipse/destiny-code-templates.xml#2 $:
 */

public interface ILocalizedMessageRepository extends IConfigurable, ILogEnabled, IDisposable, IInitializable {

    /**
     * These are the configuration parameters that must be set.
     */
    public static final String LOCALE = "Locale"; /* java.util.Locale */

    // If we're using a resource bundle:
    public static final String RESOURCE_BUNDLE_NAME = "ResourceBundleName"; /* java.lang.String */

    /**
     * Returns the message after populating all the indexed value-references in
     * the raw message format. The message should conform to the
     * java.text.MessageFormat formatting rules.
     * 
     * @param message
     *            requester
     * @return Formatted message
     */
    public String getFormattedMessageFor(IMessageRequester requestor) throws MessageNotFoundException;

    /**
     * Returns whether or not this repository contains a message for the given
     * key.
     * 
     * @param key
     * @return boolean indicating whether found or not
     */
    public boolean containsPattern(String key);

    /**
     * Initializes the repository directly from a ResourceBundle, without an
     * IConfiguration object.
     * 
     * @param resourceBundleName
     * @param locale
     * @throws LocalizedMessageRepositoryInitException
     */
    public void init(String resourceBundleName, Locale locale) throws LocalizedMessageRepositoryInitException;
}