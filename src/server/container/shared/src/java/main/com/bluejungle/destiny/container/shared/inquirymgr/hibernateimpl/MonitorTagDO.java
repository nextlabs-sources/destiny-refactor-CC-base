/**
 * 
 */
package com.bluejungle.destiny.container.shared.inquirymgr.hibernateimpl;


/**
 * 
 * @author nnallagatla
 * 
 */
public class MonitorTagDO {

	private Long id;
	private MonitorDO monitor;
	private String name;
	private String value;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the monitor
	 */
	public MonitorDO getMonitor() {
		return monitor;
	}

	/**
	 * @param monitor
	 *            the monitor to set
	 */
	public void setMonitor(MonitorDO monitor) {
		this.monitor = monitor;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

}
