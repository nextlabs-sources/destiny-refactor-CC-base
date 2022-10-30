package com.nextlabs.pf.destiny.importexport.mapping;

import com.bluejungle.pf.destiny.lib.LeafObject;


public class Hostgroup extends ComponentBase{
	public Hostgroup() {
		super();
	}
	
	public Hostgroup(LeafObject leafObject) {
		super(leafObject);
	}

	public Hostgroup(String name, String login, String sid, long id) {
		super(name, login, sid, id);
	}
}