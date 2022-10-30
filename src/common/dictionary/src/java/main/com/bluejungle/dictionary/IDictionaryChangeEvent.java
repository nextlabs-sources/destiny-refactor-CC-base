/**
 * IDictionaryChangeEvent defines the interface for dictionary change event
 * 
 * The event will be fired when there is a successful change in dictionary
 * i.e.  The lastest consistant date of dictionary has been moved forward
 * 
 */
package com.bluejungle.dictionary;

/**
 * @author atian
 *
 */
public interface IDictionaryChangeEvent {

	/**
	 * keep Dictionary Event source for escaping loop chasing 
	 * @return IDictionary
	 */
	public IDictionary getEventSourceDictionary();
	
	
	/**
	 * The listener for dictionary Change event 
	 */
	public interface IDictionaryChangeListener {

	    /**
	     * This method is called by the ddif layer upon discovering a directory
	     * change (after it has re-initialized itself). We are guaranteed that the
	     * "onDirectoryUpdate" method is called after all write/init locks on the
	     * ddif adapter have been released, so that the listener can expect to gain
	     * a read lock on the ddif and refresh its stale directory dependencies.
	     */
	    public void onDirectoryChange();
	}

}
