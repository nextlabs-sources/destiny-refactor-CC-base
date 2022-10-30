package com.bluejungle.dictionary;

public class DictionaryChangeEvent implements IDictionaryChangeEvent {

	private IDictionary dictionary = null;
	
	public DictionaryChangeEvent(IDictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public IDictionary getEventSourceDictionary() {	
		return this.dictionary;
	}

}
