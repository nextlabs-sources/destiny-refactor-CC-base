package com.nextlabs.pf.destiny.importexport.mapping;

import com.bluejungle.pf.destiny.lib.LeafObject;


public class App extends ComponentBase{
	public App() {
		super();
	}
	
	public App(LeafObject leafObject) {
		super(leafObject);
	}

	public App(String name, String login, String sid, long id) {
		super(name, login, sid, id);
	}
	
}