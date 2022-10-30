package com.bluejungle.dictionary;

public interface IDictionaryEventManager {
		
    /**
     * Listen to a directory change event by local listeners
     * 
     * @param directory change event listener
     */
    void listenForDictionaryChanges(IDictionaryChangeEvent.IDictionaryChangeListener listener);
    
    /**
     * Notify the local listeners of directory change event
     * 
     * @param dictionary change event <code>IDictionaryChangeEvent</code>
     */
    void notifyDictionaryChange(IDictionaryChangeEvent event);
    
    /**
     * Fire IDCCServerEvent to notify remote dictionary change listeners 
     * 
     */
    void fireRemoteDictionaryChangeEvent();

}
