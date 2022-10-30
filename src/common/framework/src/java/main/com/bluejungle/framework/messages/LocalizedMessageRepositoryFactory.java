/*
 * Created on Dec 24, 2004 All sources, binaries and HTML pages (C) copyright
 * 2004 by Blue Jungle Inc., Redwood City CA, Ownership remains with Blue Jungle
 * Inc, All rights reserved worldwide.
 */
package com.bluejungle.framework.messages;

import java.util.Locale;

/**
 * @author safdar
 * @version $Id:
 *          //depot/main/Destiny/main/src/common/framework/com/bluejungle/framework/messages/LocalizedMessageRepositoryFactory.java#1 $:
 */

public class LocalizedMessageRepositoryFactory {

    /**
     * Returns an initialized instance of an ILocalizedMessageRepository.
     * Returns null if the ihnitialization failed.
     * 
     * @param messageBundleName
     * @param locale
     * @return ILocalizedMessageRepository
     */
    public static ILocalizedMessageRepository createMessageRepository(String messageBundleName, Locale locale) {
        try {
            ILocalizedMessageRepository repository = new LocalizedMessageRepository();
            repository.init(messageBundleName, locale);
            return repository;
        } catch (LocalizedMessageRepositoryInitException e) {
            // Ignore this error and just return null.
            return null;
        }
    }
}