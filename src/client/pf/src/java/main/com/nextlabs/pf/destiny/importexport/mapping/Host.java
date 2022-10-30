package com.nextlabs.pf.destiny.importexport.mapping;

import com.bluejungle.pf.destiny.lib.LeafObject;


public class Host extends ComponentBase {
	public Host() {
		super();
	}
	
	public Host(LeafObject leafObject) {
		super(leafObject);
	}

	public Host(String name, String login, String sid, long id) {
		super(name, login, sid, id);
	}
}