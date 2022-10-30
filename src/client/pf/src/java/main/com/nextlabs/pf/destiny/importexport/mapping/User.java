package com.nextlabs.pf.destiny.importexport.mapping;

import com.bluejungle.pf.destiny.lib.LeafObject;


public class User extends ComponentBase{
	public User() {
		super();
	}
	
	public User(LeafObject leafObject) {
		super(leafObject);
	}

	public User(String name, String login, String sid, long id) {
		super(name, login, sid, id);
	}
}