package com.bluejungle.dictionary;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.destiny.server.shared.context.IDestinySharedContext;
import com.bluejungle.destiny.server.shared.events.IDCCServerEvent;
import com.bluejungle.destiny.server.shared.events.IDestinyEventListener;
import com.bluejungle.destiny.server.shared.events.impl.DCCServerEventImpl;
import com.bluejungle.framework.comp.IComponentManager;
import com.bluejungle.framework.sharedcontext.IDestinySharedContextLocator;

/*
 * manager dictionary event
 */
public class DictionaryEventManager implements IDictionaryEventManager, IDestinyEventListener {
    private static final Log log = LogFactory.getLog(DictionaryEventManager.class.toString());

    /* dictionary who owns the event manager */
    Dictionary dictionary;

    /* directory change event listenvers */
    private Set changeListeners =  new HashSet();

    /* shared context for firing dictionary event */
    private IDestinySharedContext sharedCtx;

    /* the DCC server event property key for IDictioary */
    public static final String DICTIONARY_DCCEVENT = "DictionaryChanged";

    /* defines dictoinary update event */
    private static final IDCCServerEvent DICTIONARY_CHANGE_EVENT = new DCCServerEventImpl(DICTIONARY_DCCEVENT);

    /**
     * Constructor
     * @param dictionary
     * @param manager
     * @param config
     */
    public DictionaryEventManager(Dictionary dictionary, IComponentManager manager) {
        this.dictionary = dictionary;
        // Get the shared context for firing events:
        IDestinySharedContextLocator locator = (IDestinySharedContextLocator) manager.getComponent( IDestinySharedContextLocator.COMP_NAME );
        this.sharedCtx = locator.getSharedContext();
        if (sharedCtx == null) {
            throw new NullPointerException("Dictionary shared context could not be initialized ...");
        } else {
            //add dictionary self as the listener of dictionary update event
            this.sharedCtx.getEventManager().registerForEvent(DICTIONARY_DCCEVENT, this);
        }
    }

    /**
     * Listen to a directory change event by local listeners
     * @see IDictionary#listenForDictionaryChanges(IDictionaryChangeEvent.IDictionaryChangeListener listener)
     * @param directory change event listener
     */
    public void listenForDictionaryChanges(IDictionaryChangeEvent.IDictionaryChangeListener listener) {
        synchronized (this.changeListeners) {
            this.changeListeners.add(listener);
        }
    }

    /*
     *  (non-Javadoc)
     * @see com.bluejungle.dictionary.IDictionaryEventManager#notifyDictionaryChange(com.bluejungle.dictionary.IDictionaryChangeEvent)
     */
    public void notifyDictionaryChange(IDictionaryChangeEvent event) {
        synchronized (this.changeListeners) {
            //Notifying all local listeners of Dictionary change
            Iterator iter = this.changeListeners.iterator();
            while (iter.hasNext()) {
                IDictionaryChangeEvent.IDictionaryChangeListener listener =
                    (IDictionaryChangeEvent.IDictionaryChangeListener) iter.next();
                listener.onDirectoryChange();
            }
        }
    }

    /*
     * Fires remote dictionary change event
     * @see com.bluejungle.dictionary.IDictionaryEventManager#fireRemoteDictionaryChangeEvent()
     */
    public void fireRemoteDictionaryChangeEvent() {
        try {
            if ( this.dictionary.isDictionaryChanged() ) {
                this.sharedCtx.getEventManager().fireEvent(DICTIONARY_CHANGE_EVENT);
            }
        }
        catch (DictionaryException e) {
            // failed to fire dictionary change event
            log.error("DICTIONARY_CHANGE_EVENT fire failed:" + e);
        }
    }

    /**
     * @see com.bluejungle.destiny.server.shared.events.IDestinyEventListener#onDestinyEvent(com.bluejungle.destiny.server.shared.events.IDCCServerEvent)
     */
    public void onDestinyEvent(IDCCServerEvent event) {
        try {
            if ( this.dictionary.isDictionaryChanged() ) {
                notifyDictionaryChange( new DictionaryChangeEvent(this.dictionary) );
            }
        }
        catch (DictionaryException e) {
            // failed to notify dictionary change
            log.error("notifyDictionaryChange failed:" + e);
        }
    }

}
