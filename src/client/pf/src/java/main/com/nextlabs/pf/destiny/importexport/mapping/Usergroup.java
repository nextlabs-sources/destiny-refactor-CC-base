package com.nextlabs.pf.destiny.importexport.mapping;

import com.bluejungle.pf.destiny.lib.LeafObject;


public class Usergroup extends ComponentBase{
	public Usergroup() {
		super();
	}
	
	public Usergroup(LeafObject leafObject) {
		super(leafObject);
	}

	public Usergroup(String name, String login, String sid, long id) {
		super(name, login, sid, id);
	}
	
}