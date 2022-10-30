package com.nextlabs.pf.destiny.importexport;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import com.nextlabs.pf.destiny.importexport.mapping.App;
import com.nextlabs.pf.destiny.importexport.mapping.Host;
import com.nextlabs.pf.destiny.importexport.mapping.Hostgroup;
import com.nextlabs.pf.destiny.importexport.mapping.User;
import com.nextlabs.pf.destiny.importexport.mapping.Usergroup;

public class ExportFile {
	private String version;
	private Collection<User> users 					= new ArrayList<User>();
	private Collection<Usergroup> usergroups 		= new ArrayList<Usergroup>();
	private Collection<App> apps 					= new ArrayList<App>();
	private Collection<Host> hosts 					= new ArrayList<Host>();
	private Collection<Hostgroup> hostgroups 		= new ArrayList<Hostgroup>();
	private Collection<ExportEntity> exportEntities = new ArrayList<ExportEntity>();
	
	//no-argument constructor required for XML binding
	public ExportFile() {
	}
	
	//list manipulators for each entity or user
	public Collection<ExportEntity> getExportEntities() {
		return exportEntities;
	}
	public void addExportEntities(ExportEntity exportEntity) {
		exportEntities.add(exportEntity);
	}
	public void setExportEntities(Collection<ExportEntity> exportEntities) {
		this.exportEntities = exportEntities;
	}
	
	public Collection<User> getUsers() {
		return users;
	}
	public void addUser(User user) {
		users.add(user);
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	
	public Collection<Usergroup> getUsergroups() {
		return usergroups;
	}
	public void addUsergroup(Usergroup usergroup) {
		usergroups.add(usergroup);
	}
	public void setUsergroups(List<Usergroup> usergroups) {
		this.usergroups = usergroups;
	}

	public Collection<Host> getHosts() {
		return hosts;
	}
	public void addHost(Host host) {
		hosts.add(host);
	}
	public void setHosts(List<Host> hosts) {
		this.hosts = hosts;
	}
	
	public Collection<Hostgroup> getHostgroups() {
		return hostgroups;
	}
	public void addHostgroup(Hostgroup hostgroup) {
		hostgroups.add(hostgroup);
	}
	public void setHostgroups(List<Hostgroup> hostgroups) {
		this.hostgroups = hostgroups;
	}

	public Collection<App> getApps() {
		return apps;
	}
	public void addApp(App app) {
		apps.add(app);
	}
	public void setApps(List<App> apps) {
		this.apps = apps;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}