package com.nextlabs.pdpevalservice.model;

public class Link {
	
	private String rel = "http://docs.oasis-open.org/ns/xacml/relation/pdp";
	
	private String href;

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

}
