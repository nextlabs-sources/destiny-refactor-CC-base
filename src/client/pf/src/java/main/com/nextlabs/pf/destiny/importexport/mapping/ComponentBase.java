package com.nextlabs.pf.destiny.importexport.mapping;

import com.bluejungle.pf.destiny.lib.LeafObject;

/**
 * Base impl for all component
 *
 * @author hchan
 * @date May 3, 2007
 */
public abstract class ComponentBase {
	private String name;
	private String login;
	private String sid;
	private long id;

	//Constructor
	public ComponentBase(String name, String login, String sid, long id) {
		this.name = name;
		this.login = login;
		this.sid = sid;
		this.id = id;
	}
	
	public ComponentBase(LeafObject leafObject) {
		this(leafObject.getName(), leafObject.getUniqueName(), leafObject.getUid(), leafObject
				.getId().longValue());
	}

	//no-argument constructor used for XML binding
	public ComponentBase() {
	}

	//getters and setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
